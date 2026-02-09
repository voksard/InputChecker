package me.draskov.inputchecker;

import net.minecraft.client.Minecraft;
import net.minecraft.client.settings.KeyBinding;

import java.util.*;

public class InputRuntimeChecker {

    private boolean armed = false;
    private boolean started = false;
    private int tickIndex = 0;

    // previous physical state for PRESS detection
    private final Map<String, Boolean> prevDown = new HashMap<String, Boolean>();

    // continuity of EXPECTED keys (for auto press/hold inference)
    private final Set<String> expectedKeysLastTick = new HashSet<String>();

    // lenient windows (key = key name, NOT press/hold)
    private final Map<String, LenientWindow> lenientWindows = new HashMap<String, LenientWindow>();

    private static class LenientWindow {
        int startTick;     // 0-based
        int lastTick;      // 0-based
        boolean satisfied; // key was DOWN at least once
    }

    private static class TokenSpec {
        Mode mode;        // REQUIRED / LENIENT / IGNORE
        String key;       // internal key ids: "w","a","jump","sprint","sneak"
        boolean isWait;
    }

    private static class Expectation {
        Mode mode;
        Action action;   // PRESS / HOLD / WAIT
        String key;      // internal key ids
        String id;       // "press-jump", "hold-w", "wait"
    }

    private enum Mode { REQUIRED, LENIENT, IGNORE }
    private enum Action { PRESS, HOLD, WAIT }

    private static class TickEntry {
        int tick1;
        String expectedRaw;
        Set<String> down;
        Set<String> pressed;
    }

    private TickEntry lastTick = null;
    private int lastWarnTickIndex = -1;

    // ---------------- Public API ----------------

    public void restart(Minecraft mc) {
        CheckElement active = ElementStore.getActive();
        if (active == null || active.tickInputs == null || active.tickInputs.isEmpty()) {
            HudLog.clear();
            HudLog.setStatus("§bInputChecker§7: no active element");
            HudLog.push("§7Select an active element and add ticks");
            resetAll();
            return;
        }

        armed = true;
        started = false;
        tickIndex = 0;

        prevDown.clear();
        for (String k : keysAll()) {
            prevDown.put(k, isKeyDown(mc, k));
        }

        expectedKeysLastTick.clear();
        lenientWindows.clear();
        lastTick = null;
        lastWarnTickIndex = -1;

        HudLog.clear();
        HudLog.setStatus("§bInputChecker§7: running (" + active.name + ")");
        HudLog.push("§7Right click to start");
    }

    public void tick(Minecraft mc) {
        if (!armed) return;
        if (mc.thePlayer == null || mc.theWorld == null) return;
        if (mc.currentScreen != null) return;

        CheckElement active = ElementStore.getActive();
        if (active == null) {
            HudLog.clear();
            HudLog.setStatus("§bInputChecker§7: No active element");
            resetAll();
            return;
        }

        if (active.tickInputs == null || active.tickInputs.isEmpty()) {
            HudLog.clear();
            HudLog.setStatus("§bInputChecker§7: No active element");
            resetAll();
            return;
        }

        // end of sequence
        if (tickIndex >= active.tickInputs.size()) {
            String lenientFail = closeAllLenientWindowsIfNeeded();
            if (lenientFail != null) {
                // STATS
                StatsTracker.recordFail(lenientFail);
                failStop(active.name, lenientFail);
                return;
            }

            HudLog.clear();
            HudLog.setStatus("§aOK (" + active.name + ")");
            // HudLog.push inserts at TOP, so push bottom->top
            HudLog.push("§7Right click to restart");
            HudLog.push("§aSequence completed");

            // STATS
            StatsTracker.recordOk();

            resetAll();
            return;
        }

        String expectedRaw = active.tickInputs.get(tickIndex);

        List<TokenSpec> specs = parseTokens(expectedRaw);
        List<Expectation> exps = buildExpectations(specs);

        // start condition
        if (!started) {
            if (exps.isEmpty() || containsWait(exps)) {
                started = true;
            } else if (hasAnyRelevantActivityThisTick(mc, specs)) {
                started = true;
            } else {
                updatePrevDown(mc);
                return;
            }
        }

        // close lenient windows that ended
        String winFail = resolveLenientWindowsEndingThisTick(specs);
        if (winFail != null) {
            // STATS
            StatsTracker.recordFail(winFail);
            failStop(active.name, winFail);
            return;
        }

        // REQUIRED missing
        Set<String> missing = new LinkedHashSet<String>();
        updateLenientWindows(mc, specs);

        for (int i = 0; i < exps.size(); i++) {
            Expectation e = exps.get(i);
            if (e.mode != Mode.REQUIRED) continue;

            if (!isSatisfiedThisTick(mc, e, specs)) {
                missing.add(e.id);
            }
        }

        // UNEXPECTED inputs
        Set<String> unexpected = computeUnexpected(mc, specs);

        // Build tick entry
        TickEntry te = new TickEntry();
        te.tick1 = tickIndex + 1;
        te.expectedRaw = expectedRaw;
        te.down = buildActualDown(mc);
        te.pressed = buildActualPressed(mc);
        lastTick = te;

        // FAIL immediately on first wrong tick (clean display)
        if (!missing.isEmpty() || !unexpected.isEmpty()) {
            int failTick1 = tickIndex + 1;

            Set<String> expDisplay = buildDisplayExpected(specs);
            String expStr = join(expDisplay);
            if (expStr.length() == 0) expStr = "nothing";

            Set<String> gotDisplay = buildDisplayGot(te.down, specs);
            String gotStr = join(gotDisplay);
            if (gotStr.length() == 0) gotStr = "nothing";

            HudLog.clear();
            HudLog.setStatus("§cFAIL (" + active.name + ")");
            // push bottom -> top
            HudLog.push("§7Right click to restart");
            HudLog.push("§7Got: §c" + gotStr);
            HudLog.push("§7Expected: §f" + expStr + " §7tick §f" + failTick1);

            // STATS (compact)
            StatsTracker.recordFail("Tick " + failTick1 + " expected [" + expStr + "] got [" + gotStr + "]");

            resetAll();
            updatePrevDown(mc);
            return;
        }

        // OK tick -> advance
        tickIndex++;

        expectedKeysLastTick.clear();
        for (int i = 0; i < specs.size(); i++) {
            TokenSpec ts = specs.get(i);
            if (ts.isWait) continue;
            if (ts.key != null && ts.key.length() > 0) expectedKeysLastTick.add(ts.key);
        }

        updatePrevDown(mc);
    }

    // ---------------- Display helpers ----------------

    private boolean hideSprintInDisplay() {
        // FullSprint ON => never show sprint in Expected/Got
        return InputCheckerConfig.get().fullSprint;
    }

    private Set<String> buildDisplayExpected(List<TokenSpec> specs) {
        Set<String> out = new LinkedHashSet<String>();

        for (int i = 0; i < specs.size(); i++) {
            if (specs.get(i).isWait) {
                out.add("wait");
                return out;
            }
        }

        boolean hideSprint = hideSprintInDisplay();

        for (int i = 0; i < specs.size(); i++) {
            TokenSpec ts = specs.get(i);
            if (ts.isWait) continue;

            if (ts.mode != Mode.REQUIRED) continue;

            if (hideSprint && "sprint".equals(ts.key)) continue;

            out.add(ts.key);
        }

        return out;
    }

    private Set<String> buildDisplayGot(Set<String> actualDown, List<TokenSpec> specs) {
        Set<String> out = new LinkedHashSet<String>();
        boolean hideSprint = hideSprintInDisplay();

        Set<String> hidden = new HashSet<String>();
        for (int i = 0; i < specs.size(); i++) {
            TokenSpec ts = specs.get(i);
            if (ts.isWait) continue;
            if (ts.mode == Mode.IGNORE || ts.mode == Mode.LENIENT) hidden.add(ts.key);
        }

        for (String k : actualDown) {
            if (hideSprint && "sprint".equals(k)) continue;
            if (hidden.contains(k)) continue;
            out.add(k);
        }
        return out;
    }

    // ---------------- Core: tokens & expectations ----------------

    private List<TokenSpec> parseTokens(String raw) {
        List<TokenSpec> out = new ArrayList<TokenSpec>();
        if (raw == null) return out;

        raw = raw.trim().toLowerCase();
        if (raw.length() == 0) return out;

        raw = raw.replace(" ", "+");
        String[] parts = raw.split("\\+");

        for (int i = 0; i < parts.length; i++) {
            String original = parts[i].trim();
            if (original.length() == 0) continue;

            String token = original;

            if ("wait".equals(token)) {
                TokenSpec ts = new TokenSpec();
                ts.mode = Mode.REQUIRED;
                ts.isWait = true;
                ts.key = "";
                out.add(ts);
                continue;
            }

            Mode mode = Mode.REQUIRED;
            if (token.startsWith("lenient-")) {
                mode = Mode.LENIENT;
                token = token.substring("lenient-".length());
            } else if (token.startsWith("ignore-")) {
                mode = Mode.IGNORE;
                token = token.substring("ignore-".length());
            }

            // normalize to internal names: jump/sprint/sneak (and accept old words)
            String key = normalizeKey(token);

            if (!isSupportedKey(key)) {
                warnOncePerTick("Warning: unknown key '" + key + "' in token '" + original + "'");
                continue;
            }

            TokenSpec ts = new TokenSpec();
            ts.mode = mode;
            ts.isWait = false;
            ts.key = key;
            out.add(ts);
        }

        return out;
    }

    private List<Expectation> buildExpectations(List<TokenSpec> specs) {
        List<Expectation> out = new ArrayList<Expectation>();

        boolean hasWait = false;
        for (int i = 0; i < specs.size(); i++) {
            if (specs.get(i).isWait) { hasWait = true; break; }
        }

        if (hasWait) {
            Expectation w = new Expectation();
            w.mode = Mode.REQUIRED;
            w.action = Action.WAIT;
            w.key = "";
            w.id = "wait";
            out.add(w);
            return out;
        }

        for (int i = 0; i < specs.size(); i++) {
            TokenSpec ts = specs.get(i);
            if (ts.isWait) continue;
            if (ts.mode != Mode.REQUIRED) continue;

            Action a = expectedKeysLastTick.contains(ts.key) ? Action.HOLD : Action.PRESS;

            Expectation e = new Expectation();
            e.mode = Mode.REQUIRED;
            e.action = a;
            e.key = ts.key;
            e.id = (a == Action.PRESS ? "press-" : "hold-") + ts.key;
            out.add(e);
        }

        return out;
    }

    private boolean containsWait(List<Expectation> exps) {
        for (int i = 0; i < exps.size(); i++) if (exps.get(i).action == Action.WAIT) return true;
        return false;
    }

    // ---------------- Satisfaction ----------------

    private boolean isSatisfiedThisTick(Minecraft mc, Expectation e, List<TokenSpec> specsThisTick) {
        if (e.action == Action.WAIT) {
            boolean fullSprint = InputCheckerConfig.get().fullSprint;

            List<String> checkKeys = new ArrayList<String>(Arrays.asList("w","a","s","d","jump","sneak"));
            if (!fullSprint) checkKeys.add("sprint");

            for (int i = 0; i < checkKeys.size(); i++) {
                String k = checkKeys.get(i);
                if (tokenIgnoresKey(specsThisTick, k)) continue;
                if (isKeyDown(mc, k)) return false;
            }
            return true;
        }

        boolean down = isKeyDown(mc, e.key);
        if (e.action == Action.HOLD) return down;

        boolean prev = prevDown.containsKey(e.key) ? prevDown.get(e.key).booleanValue() : false;
        return down && !prev;
    }

    // ---------------- Unexpected detection ----------------

    private Set<String> computeUnexpected(Minecraft mc, List<TokenSpec> specs) {
        Set<String> unexpected = new LinkedHashSet<String>();

        boolean fullSprint = InputCheckerConfig.get().fullSprint;

        boolean sprintMentioned = tokenMentionsKey(specs, "sprint");
        boolean sprintIgnoredByDefault = fullSprint && !sprintMentioned;

        for (String k : keysAll()) {

            if (tokenIgnoresKey(specs, k)) continue;

            if ("sprint".equals(k) && sprintIgnoredByDefault) continue;

            if (tokenMentionsKey(specs, k)) continue;

            boolean down = isKeyDown(mc, k);
            if (!down) continue;

            boolean prev = prevDown.containsKey(k) ? prevDown.get(k).booleanValue() : false;
            boolean pressed = down && !prev;

            unexpected.add((pressed ? "press-" : "hold-") + k);
        }

        return unexpected;
    }

    private boolean tokenMentionsKey(List<TokenSpec> specs, String key) {
        for (int i = 0; i < specs.size(); i++) {
            TokenSpec ts = specs.get(i);
            if (!ts.isWait && key.equals(ts.key)) return true;
        }
        return false;
    }

    private boolean tokenIgnoresKey(List<TokenSpec> specs, String key) {
        for (int i = 0; i < specs.size(); i++) {
            TokenSpec ts = specs.get(i);
            if (!ts.isWait && ts.mode == Mode.IGNORE && key.equals(ts.key)) return true;
        }
        return false;
    }

    // ---------------- Lenient windows ----------------

    private void updateLenientWindows(Minecraft mc, List<TokenSpec> specs) {
        for (int i = 0; i < specs.size(); i++) {
            TokenSpec ts = specs.get(i);
            if (ts.isWait) continue;
            if (ts.mode != Mode.LENIENT) continue;

            LenientWindow w = lenientWindows.get(ts.key);
            if (w == null) {
                w = new LenientWindow();
                w.startTick = tickIndex;
                w.lastTick = tickIndex;
                w.satisfied = false;
                lenientWindows.put(ts.key, w);
            } else {
                w.lastTick = tickIndex;
            }

            if (isKeyDown(mc, ts.key)) w.satisfied = true;
        }
    }

    private String resolveLenientWindowsEndingThisTick(List<TokenSpec> specsThisTick) {
        Set<String> lenientNow = new HashSet<String>();
        for (int i = 0; i < specsThisTick.size(); i++) {
            TokenSpec ts = specsThisTick.get(i);
            if (!ts.isWait && ts.mode == Mode.LENIENT) lenientNow.add(ts.key);
        }

        List<String> toClose = new ArrayList<String>();
        for (String key : lenientWindows.keySet()) {
            if (!lenientNow.contains(key)) toClose.add(key);
        }

        for (int i = 0; i < toClose.size(); i++) {
            String key = toClose.get(i);
            LenientWindow w = lenientWindows.remove(key);
            if (w != null && !w.satisfied) {
                int a = w.startTick + 1;
                int b = w.lastTick + 1;
                return "Lenient input not triggered: [" + key + "] in ticks " + a + "-" + b;
            }
        }
        return null;
    }

    private String closeAllLenientWindowsIfNeeded() {
        for (Map.Entry<String, LenientWindow> it : lenientWindows.entrySet()) {
            LenientWindow w = it.getValue();
            if (w != null && !w.satisfied) {
                int a = w.startTick + 1;
                int b = w.lastTick + 1;
                String key = it.getKey();
                lenientWindows.clear();
                return "Lenient input not triggered: [" + key + "] in ticks " + a + "-" + b;
            }
        }
        lenientWindows.clear();
        return null;
    }

    private void failStop(String elementName, String reason) {
        HudLog.clear();
        HudLog.setStatus("§cFAIL (" + elementName + ")");
        HudLog.push("§7Right click to restart");
        HudLog.push("§7" + reason);

        // STATS
        StatsTracker.recordFail(reason);

        resetAll();
    }

    // ---------------- Start condition ----------------

    private boolean hasAnyRelevantActivityThisTick(Minecraft mc, List<TokenSpec> specs) {
        boolean fullSprint = InputCheckerConfig.get().fullSprint;
        boolean allowSprint = (!fullSprint) || tokenMentionsKey(specs, "sprint");

        for (String k : Arrays.asList("w","a","s","d","jump","sneak")) {
            boolean down = isKeyDown(mc, k);
            boolean prev = prevDown.containsKey(k) ? prevDown.get(k).booleanValue() : false;
            if (down && !prev) return true;
        }
        if (allowSprint) {
            boolean down = isKeyDown(mc, "sprint");
            boolean prev = prevDown.containsKey("sprint") ? prevDown.get("sprint").booleanValue() : false;
            if (down && !prev) return true;
        }
        return false;
    }

    // ---------------- Keys / IO ----------------

    private void resetAll() {
        armed = false;
        started = false;
        tickIndex = 0;
        prevDown.clear();
        expectedKeysLastTick.clear();
        lenientWindows.clear();
        lastWarnTickIndex = -1;
        lastTick = null;
    }

    private boolean isKeyDown(Minecraft mc, String key) {
        KeyBinding kb = resolveKeyBinding(mc, key);
        return kb != null && kb.isKeyDown();
    }

    private KeyBinding resolveKeyBinding(Minecraft mc, String key) {
        if (key == null) return null;
        key = key.toLowerCase();

        if (key.equals("w")) return mc.gameSettings.keyBindForward;
        if (key.equals("a")) return mc.gameSettings.keyBindLeft;
        if (key.equals("s")) return mc.gameSettings.keyBindBack;
        if (key.equals("d")) return mc.gameSettings.keyBindRight;

        if (key.equals("jump") || key.equals("space")) return mc.gameSettings.keyBindJump;
        if (key.equals("sneak") || key.equals("shift")) return mc.gameSettings.keyBindSneak;
        if (key.equals("sprint") || key.equals("ctrl")) return mc.gameSettings.keyBindSprint;

        return null;
    }

    private boolean isSupportedKey(String key) {
        return "w".equals(key) || "a".equals(key) || "s".equals(key) || "d".equals(key)
                || "jump".equals(key) || "sneak".equals(key) || "sprint".equals(key);
    }

    private String normalizeKey(String key) {
        if (key == null) return "";
        key = key.trim().toLowerCase();

        // accept old words too
        if (key.equals("space")) return "jump";
        if (key.equals("ctrl")) return "sprint";
        if (key.equals("shift")) return "sneak";

        // accept common aliases (already normalized)
        if (key.equals("jump")) return "jump";
        if (key.equals("sprint")) return "sprint";
        if (key.equals("sneak")) return "sneak";

        return key;
    }

    private void updatePrevDown(Minecraft mc) {
        for (String k : keysAll()) {
            prevDown.put(k, isKeyDown(mc, k));
        }
    }

    private List<String> keysAll() {
        return Arrays.asList("w","a","s","d","jump","sneak","sprint");
    }

    private Set<String> buildActualDown(Minecraft mc) {
        Set<String> s = new LinkedHashSet<String>();
        for (String k : keysAll()) if (isKeyDown(mc, k)) s.add(k);
        return s;
    }

    private Set<String> buildActualPressed(Minecraft mc) {
        Set<String> s = new LinkedHashSet<String>();
        for (String k : keysAll()) {
            boolean down = isKeyDown(mc, k);
            boolean prev = prevDown.containsKey(k) ? prevDown.get(k).booleanValue() : false;
            if (down && !prev) s.add(k);
        }
        return s;
    }

    private void warnOncePerTick(String msg) {
        if (lastWarnTickIndex == tickIndex) return;
        lastWarnTickIndex = tickIndex;
        HudLog.push("§6" + msg);
    }

    private String join(Set<String> s) {
        if (s == null || s.isEmpty()) return "";
        StringBuilder sb = new StringBuilder();
        boolean first = true;
        for (String k : s) {
            if (!first) sb.append("+");
            sb.append(k);
            first = false;
        }
        return sb.toString();
    }
}

package me.draskov.inputchecker;

import net.minecraft.client.Minecraft;
import net.minecraft.client.settings.KeyBinding;

import java.util.*;

public class InputRuntimeChecker {
    private boolean armed = false;
    private boolean started = false;
    private int tickIndex = 0;
    private boolean hasStartedBefore = false; // Track si le check a déjà commencé pour cet élément
    private String lastActiveElementId = null; // Track le dernier élément actif

    private final Map<String, Boolean> prevDown = new HashMap<>();
    private final Set<String> expectedKeysLastTick = new HashSet<>();
    private final Map<String, LenientWindow> lenientWindows = new HashMap<>();

    private static class LenientWindow {
        int startTick;
        int lastTick;
        boolean satisfied;
        Action expectedAction; // PRESS ou HOLD
    }

    private static class LenientFailure {
        String key;
        Action expectedAction;
        int startTick;
        int lastTick;

        LenientFailure(String key, Action expectedAction, int startTick, int lastTick) {
            this.key = key;
            this.expectedAction = expectedAction;
            this.startTick = startTick;
            this.lastTick = lastTick;
        }
    }

    private static class TokenSpec {
        Mode mode;
        String key;
        boolean isWait;
        Action expectedAction; // Pour LENIENT : PRESS ou RELEASE
    }

    private static class Expectation {
        Mode mode;
        Action action;
        String key;
        String id;
    }

    private enum Mode { REQUIRED, LENIENT, IGNORE }
    private enum Action { PRESS, HOLD, WAIT, RELEASE }

    private static class TickEntry {
        int tick1;
        String expectedRaw;
        Set<String> down;
        Set<String> pressed;
    }

    private TickEntry lastTick = null;
    private int lastWarnTickIndex = -1;

    private int findLastNonEmptyTick(CheckElement active) {
        if (active == null || active.tickInputs == null) return -1;

        for (int i = active.tickInputs.size() - 1; i >= 0; i--) {
            String input = active.tickInputs.get(i);
            if (input != null && !input.trim().isEmpty()) {
                return i;
            }
        }
        return -1;
    }

    public void reset() {
        resetAll();
        HudLog.clear();
    }

    public void restart(Minecraft mc) {
        CheckElement active = ElementStore.getActive();
        if (active == null || active.tickInputs == null || active.tickInputs.isEmpty()) {
            HudLog.clear();
            HudLog.setStatus(ColorConfig.getTitleColorCode() + "Inputchecker check:");
            HudLog.push(ColorConfig.getContentColorCode() + "No active element");
            resetAll();
            return;
        }

        // Vérifier si c'est un nouvel élément ou le même
        boolean isNewElement = !active.id.equals(lastActiveElementId);
        if (isNewElement) {
            hasStartedBefore = false;
            lastActiveElementId = active.id;
        }

        // Valider les lenient inputs avant de commencer
        if (!validateLenientInputs(active)) {
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
        HudLog.setStatus(ColorConfig.getTitleColorCode() + "Checking " + active.name + ":");

        // Afficher "Running..." immédiatement au clic droit
        HudLog.push(ColorConfig.getContentColorCode() + "Running...");

        // Marquer que le check a commencé
        hasStartedBefore = true;
    }

    public void tick(Minecraft mc) {
        if (!armed || mc.thePlayer == null || mc.theWorld == null || mc.currentScreen != null) {
            return;
        }

        CheckElement active = ElementStore.getActive();
        if (active == null || active.tickInputs == null || active.tickInputs.isEmpty()) {
            HudLog.clear();
            HudLog.setStatus(ColorConfig.getTitleColorCode() + "Inputchecker check:");
            resetAll();
            return;
        }

        // Trouver le dernier tick non-vide pour arrêter là
        int lastNonEmptyTick = findLastNonEmptyTick(active);

        // Si on a dépassé le dernier tick rempli, la séquence est complète
        if (tickIndex > lastNonEmptyTick) {
            LenientFailure lenientFail = closeAllLenientWindowsIfNeeded();
            if (lenientFail != null) {
                String actionStr = lenientFail.expectedAction == Action.PRESS ? "pressed" : "released";
                String reason = "Expected " + lenientFail.key + " " + actionStr + " in ticks " + (lenientFail.startTick + 1) + "-" + (lenientFail.lastTick + 1);
                StatsTracker.recordFail(reason);
                failStopLenient(active.name, lenientFail);
                return;
            }

            HudLog.clear();
            HudLog.setStatus("§aOk " + active.name + ":");
            HudLog.push(ColorConfig.getContentColorCode() + "Sequence completed");
            StatsTracker.recordOk();

            resetAll();
            return;
        }

        String expectedRaw = active.tickInputs.get(tickIndex);

        // Si la case est vide, vérifier les checkboxes (spr, jmp, snk)
        if (expectedRaw == null || expectedRaw.trim().isEmpty()) {
            // Vérifier si des checkboxes sont cochées
            boolean checkSpr = tickIndex < active.checkSprint.size() && active.checkSprint.get(tickIndex);
            boolean checkJmp = tickIndex < active.checkJump.size() && active.checkJump.get(tickIndex);
            boolean checkSnk = tickIndex < active.checkSneak.size() && active.checkSneak.get(tickIndex);

            // Si aucune checkbox n'est cochée, c'est un vrai WAIT
            if (!checkSpr && !checkJmp && !checkSnk) {
                // Créer une expectation WAIT pour vérifier qu'aucune touche n'est pressée
                List<TokenSpec> specs = new ArrayList<>();
                List<Expectation> exps = new ArrayList<>();

                Expectation waitExp = new Expectation();
                waitExp.mode = Mode.REQUIRED;
                waitExp.action = Action.WAIT;
                waitExp.key = "";
                waitExp.id = "wait";
                exps.add(waitExp);

                // Vérifier que aucune touche n'est pressée (action WAIT)
                if (!isSatisfiedThisTick(mc, waitExp, specs)) {
                    // Une touche est pressée alors qu'on était en WAIT
                    Set<String> actualDown = buildActualDown(mc);

                    int failTick1 = tickIndex + 1;
                    HudLog.clear();
                    HudLog.setStatus("§cFail " + active.name + ":");
                    HudLog.push(ColorConfig.getContentColorCode() + "Got: " + ColorConfig.getTitleColorCode() + join(actualDown));
                    HudLog.push(ColorConfig.getContentColorCode() + "Expected: " + ColorConfig.getTitleColorCode() + "nothing " + ColorConfig.getContentColorCode() + "tick " + ColorConfig.getTitleColorCode() + failTick1);

                    StatsTracker.recordFail("Tick " + failTick1 + " expected nothing got " + join(actualDown));

                    resetAll();
                    updatePrevDown(mc);
                    return;
                }

                tickIndex++;
                updatePrevDown(mc);
                return;
            }

            // Si des checkboxes sont cochées, traiter le tick comme s'il contenait ces touches
            // Créer une chaîne avec les checkboxes cochées et la traiter normalement
            List<String> checkboxes = new ArrayList<>();
            if (checkSpr) checkboxes.add("spr");
            if (checkJmp) checkboxes.add("jmp");
            if (checkSnk) checkboxes.add("snk");

            String checkboxString = String.join("+", checkboxes);
            expectedRaw = checkboxString;
            // Continuer avec le traitement normal ci-dessous
        }

        List<TokenSpec> specs = parseTokens(expectedRaw);
        List<Expectation> exps = buildExpectations(specs);

        if (!started) {
            if (containsWait(exps)) {
                // WAIT démarre immédiatement
                started = true;
            } else if (exps.isEmpty()) {
                // Seulement lenient inputs → attendre une activité pertinente
                if (hasAnyRelevantActivityThisTick(mc, specs)) {
                    started = true;
                } else {
                    updatePrevDown(mc);
                    return;
                }
            } else if (hasAnyRelevantActivityThisTick(mc, specs)) {
                // Inputs normaux → attendre une activité pertinente
                started = true;
            } else {
                updatePrevDown(mc);
                return;
            }
        }

        LenientFailure lenientFail = resolveLenientWindowsEndingThisTick(specs);
        if (lenientFail != null) {
            String actionStr = lenientFail.expectedAction == Action.PRESS ? "pressed" : "released";
            String reason = "Expected " + lenientFail.key + " " + actionStr + " in ticks " + (lenientFail.startTick + 1) + "-" + (lenientFail.lastTick + 1);
            StatsTracker.recordFail(reason);
            failStopLenient(active.name, lenientFail);
            return;
        }

        Set<String> missing = new LinkedHashSet<>();
        updateLenientWindows(mc, specs);

        for (Expectation e : exps) {
            if (e.mode == Mode.REQUIRED && !isSatisfiedThisTick(mc, e, specs)) {
                missing.add(e.id);
            }
        }

        Set<String> unexpected = computeUnexpected(mc, specs);

        TickEntry te = new TickEntry();
        te.tick1 = tickIndex + 1;
        te.expectedRaw = expectedRaw;
        te.down = buildActualDown(mc);
        te.pressed = buildActualPressed(mc);
        lastTick = te;

        if (!missing.isEmpty() || !unexpected.isEmpty()) {
            int failTick1 = tickIndex + 1;
            Set<String> expDisplay = buildDisplayExpected(specs);
            String expStr = expDisplay.isEmpty() ? "nothing" : join(expDisplay);
            Set<String> gotDisplay = buildDisplayGot(te.down, specs);
            String gotStr = gotDisplay.isEmpty() ? "nothing" : join(gotDisplay);

            HudLog.clear();
            HudLog.setStatus("§cFail " + active.name + ":");
            HudLog.push(ColorConfig.getContentColorCode() + "Got: " + ColorConfig.getTitleColorCode() + gotStr);
            HudLog.push(ColorConfig.getContentColorCode() + "Expected: " + ColorConfig.getTitleColorCode() + expStr + " " + ColorConfig.getContentColorCode() + "tick " + ColorConfig.getTitleColorCode() + failTick1);

            StatsTracker.recordFail("Tick " + failTick1 + " expected " + expStr + " got " + gotStr);

            resetAll();
            updatePrevDown(mc);
            return;
        }

        tickIndex++;
        expectedKeysLastTick.clear();
        for (TokenSpec ts : specs) {
            if (!ts.isWait && ts.key != null && !ts.key.isEmpty()) {
                expectedKeysLastTick.add(ts.key);
            }
        }

        // Ajouter aussi les touches cochées via checkboxes à expectedKeysLastTick
        CheckElement active2 = ElementStore.getActive();
        if (active2 != null && (tickIndex - 1) < active2.tickInputs.size()) {
            int prevTickIdx = tickIndex - 1;
            boolean checkSpr = prevTickIdx >= 0 && prevTickIdx < active2.checkSprint.size() && active2.checkSprint.get(prevTickIdx);
            boolean checkJmp = prevTickIdx >= 0 && prevTickIdx < active2.checkJump.size() && active2.checkJump.get(prevTickIdx);
            boolean checkSnk = prevTickIdx >= 0 && prevTickIdx < active2.checkSneak.size() && active2.checkSneak.get(prevTickIdx);

            if (checkSpr) expectedKeysLastTick.add("spr");
            if (checkJmp) expectedKeysLastTick.add("jmp");
            if (checkSnk) expectedKeysLastTick.add("snk");
        }

        updatePrevDown(mc);
    }

    private boolean hideSprintInDisplay() {
        return InputCheckerConfig.get().fullSprint;
    }

    private Set<String> buildDisplayExpected(List<TokenSpec> specs) {
        Set<String> out = new LinkedHashSet<>();

        for (TokenSpec spec : specs) {
            if (spec.isWait) {
                out.add("wait");
                return out;
            }
        }

        boolean hideSprint = hideSprintInDisplay();
        for (TokenSpec ts : specs) {
            if (!ts.isWait && ts.mode == Mode.REQUIRED && !(hideSprint && "sprint".equals(ts.key))) {
                out.add(ts.key);
            }
        }

        // Ajouter les touches cochées via checkboxes
        CheckElement active = ElementStore.getActive();
        if (active != null && tickIndex < active.tickInputs.size()) {
            boolean checkSpr = tickIndex < active.checkSprint.size() && active.checkSprint.get(tickIndex);
            boolean checkJmp = tickIndex < active.checkJump.size() && active.checkJump.get(tickIndex);
            boolean checkSnk = tickIndex < active.checkSneak.size() && active.checkSneak.get(tickIndex);

            if (checkSpr && !out.contains("spr")) {
                out.add("spr");
            }
            if (checkJmp && !out.contains("jmp")) {
                out.add("jmp");
            }
            if (checkSnk && !out.contains("snk")) {
                out.add("snk");
            }
        }

        return out;
    }

    private Set<String> buildDisplayGot(Set<String> actualDown, List<TokenSpec> specs) {
        Set<String> out = new LinkedHashSet<>();
        boolean hideSprint = hideSprintInDisplay();

        Set<String> hidden = new HashSet<>();
        for (TokenSpec ts : specs) {
            if (!ts.isWait && (ts.mode == Mode.IGNORE || ts.mode == Mode.LENIENT)) {
                hidden.add(ts.key);
            }
        }

        // Vérifier quels checkboxes sont cochés et quels "no" sont cochés pour cette partie du code
        CheckElement active = ElementStore.getActive();
        boolean checkSpr = active != null && tickIndex < active.checkSprint.size() && active.checkSprint.get(tickIndex);
        boolean checkJmp = active != null && tickIndex < active.checkJump.size() && active.checkJump.get(tickIndex);
        boolean checkSnk = active != null && tickIndex < active.checkSneak.size() && active.checkSneak.get(tickIndex);

        boolean noSpr = active != null && tickIndex < active.noSprint.size() && active.noSprint.get(tickIndex);
        boolean noJmp = active != null && tickIndex < active.noJump.size() && active.noJump.get(tickIndex);
        boolean noSnk = active != null && tickIndex < active.noSneak.size() && active.noSneak.get(tickIndex);

        for (String k : actualDown) {
            if (!(hideSprint && "sprint".equals(k)) && !hidden.contains(k)) {
                // Exclure spr, snk, jmp s'ils ne sont pas cochés ET ne sont pas dans "no"
                if ("spr".equals(k) && !checkSpr && !noSpr) {
                    continue;
                }
                if ("snk".equals(k) && !checkSnk && !noSnk) {
                    continue;
                }
                if ("jmp".equals(k) && !checkJmp && !noJmp) {
                    continue;
                }
                out.add(k);
            }
        }
        return out;
    }

    private List<TokenSpec> parseTokens(String raw) {
        List<TokenSpec> out = new ArrayList<>();
        if (raw == null) return out;

        raw = raw.trim().toLowerCase().replace(" ", "+");
        String[] parts = raw.split("\\+");

        for (String original : parts) {
            String token = original.trim();
            if (token.isEmpty()) continue;

            if ("wait".equals(token)) {
                TokenSpec ts = new TokenSpec();
                ts.mode = Mode.REQUIRED;
                ts.isWait = true;
                ts.key = "";
                out.add(ts);
                continue;
            }

            Mode mode = Mode.REQUIRED;
            Action expectedAction = null;
            if (token.startsWith("prs-")) {
                mode = Mode.LENIENT;
                expectedAction = Action.PRESS;
                token = token.substring("prs-".length());
            } else if (token.startsWith("rls-")) {
                mode = Mode.LENIENT;
                expectedAction = Action.RELEASE;
                token = token.substring("rls-".length());
            } else if (token.startsWith("ignore-")) {
                mode = Mode.IGNORE;
                token = token.substring("ignore-".length());
            }

            String key = normalizeKey(token);

            if (!isSupportedKey(key)) {
                warnOncePerTick("Warning: unknown key '" + key + "' in token '" + original + "'");
                continue;
            }

            TokenSpec ts = new TokenSpec();
            ts.mode = mode;
            ts.isWait = false;
            ts.key = key;
            ts.expectedAction = expectedAction; // Pour LENIENT
            out.add(ts);
        }

        return out;
    }

    private List<Expectation> buildExpectations(List<TokenSpec> specs) {
        List<Expectation> out = new ArrayList<>();

        boolean hasWait = specs.stream().anyMatch(s -> s.isWait);

        if (hasWait) {
            Expectation w = new Expectation();
            w.mode = Mode.REQUIRED;
            w.action = Action.WAIT;
            w.key = "";
            w.id = "wait";
            out.add(w);
            return out;
        }

        // Ajouter les expectations pour les inputs du texte
        for (TokenSpec ts : specs) {
            if (!ts.isWait && ts.mode == Mode.REQUIRED) {
                Action a = expectedKeysLastTick.contains(ts.key) ? Action.HOLD : Action.PRESS;
                Expectation e = new Expectation();
                e.mode = Mode.REQUIRED;
                e.action = a;
                e.key = ts.key;
                e.id = (a == Action.PRESS ? "press-" : "hold-") + ts.key;
                out.add(e);
            }
        }

        // Ajouter les expectations pour les checkboxes cochées (spr, jmp, snk)
        CheckElement active = ElementStore.getActive();
        if (active != null && tickIndex < active.tickInputs.size()) {
            boolean checkSpr = tickIndex < active.checkSprint.size() && active.checkSprint.get(tickIndex);
            boolean checkJmp = tickIndex < active.checkJump.size() && active.checkJump.get(tickIndex);
            boolean checkSnk = tickIndex < active.checkSneak.size() && active.checkSneak.get(tickIndex);

            // Ajouter spr si coché et pas déjà dans les specs
            // Les checkboxes vérifient que la touche est DOWN (peu importe si c'est une nouvelle pression ou déjà maintenue)
            if (checkSpr && !tokenMentionsKey(specs, "spr")) {
                Expectation e = new Expectation();
                e.mode = Mode.REQUIRED;
                e.action = Action.HOLD; // Toujours vérifier que la touche est maintenue, pas une nouvelle pression
                e.key = "spr";
                e.id = "hold-spr";
                out.add(e);
            }

            // Ajouter jmp si coché et pas déjà dans les specs
            if (checkJmp && !tokenMentionsKey(specs, "jmp")) {
                Expectation e = new Expectation();
                e.mode = Mode.REQUIRED;
                e.action = Action.HOLD; // Toujours vérifier que la touche est maintenue, pas une nouvelle pression
                e.key = "jmp";
                e.id = "hold-jmp";
                out.add(e);
            }

            // Ajouter snk si coché et pas déjà dans les specs
            if (checkSnk && !tokenMentionsKey(specs, "snk")) {
                Expectation e = new Expectation();
                e.mode = Mode.REQUIRED;
                e.action = Action.HOLD; // Toujours vérifier que la touche est maintenue, pas une nouvelle pression
                e.key = "snk";
                e.id = "hold-snk";
                out.add(e);
            }
        }

        return out;
    }

    private boolean containsWait(List<Expectation> exps) {
        return exps.stream().anyMatch(e -> e.action == Action.WAIT);
    }

    private boolean isSatisfiedThisTick(Minecraft mc, Expectation e, List<TokenSpec> specsThisTick) {
        if (e.action == Action.WAIT) {
            CheckElement active = ElementStore.getActive();
            boolean checkSpr = active != null && tickIndex < active.checkSprint.size() && active.checkSprint.get(tickIndex);
            boolean checkJmp = active != null && tickIndex < active.checkJump.size() && active.checkJump.get(tickIndex);
            boolean checkSnk = active != null && tickIndex < active.checkSneak.size() && active.checkSneak.get(tickIndex);

            List<String> checkKeys = new ArrayList<>(Arrays.asList("w", "a", "s", "d"));
            if (checkJmp) checkKeys.add("jmp");
            if (checkSpr) checkKeys.add("spr");
            if (checkSnk) checkKeys.add("snk");

            for (String k : checkKeys) {
                if (isKeyDown(mc, k)) {
                    return false;
                }
            }
            return true;
        }

        boolean down = isKeyDown(mc, e.key);
        if (e.action == Action.HOLD) return down;

        boolean prev = prevDown.getOrDefault(e.key, false);
        return down && !prev;
    }

    private Set<String> computeUnexpected(Minecraft mc, List<TokenSpec> specs) {
        Set<String> unexpected = new LinkedHashSet<>();
        CheckElement active = ElementStore.getActive();

        boolean checkSpr = active != null && tickIndex < active.checkSprint.size() && active.checkSprint.get(tickIndex);
        boolean checkJmp = active != null && tickIndex < active.checkJump.size() && active.checkJump.get(tickIndex);
        boolean checkSnk = active != null && tickIndex < active.checkSneak.size() && active.checkSneak.get(tickIndex);

        boolean noSpr = active != null && tickIndex < active.noSprint.size() && active.noSprint.get(tickIndex);
        boolean noJmp = active != null && tickIndex < active.noJump.size() && active.noJump.get(tickIndex);
        boolean noSnk = active != null && tickIndex < active.noSneak.size() && active.noSneak.get(tickIndex);

        for (String k : keysAll()) {
            // Si la touche est mentionnée dans les specs, skip (elle est expected)
            if (tokenMentionsKey(specs, k)) {
                continue;
            }

            // Si la touche est cochée via checkbox, skip (elle est dans les expectations maintenant)
            if ("spr".equals(k) && checkSpr) {
                continue;
            }
            if ("jmp".equals(k) && checkJmp) {
                continue;
            }
            if ("snk".equals(k) && checkSnk) {
                continue;
            }

            // Vérifier si la touche est interdite (colonnes "no")
            if ("spr".equals(k) && noSpr) {
                boolean down = isKeyDown(mc, k);
                if (down) {
                    boolean prev = prevDown.getOrDefault(k, false);
                    boolean pressed = down && !prev;
                    unexpected.add((pressed ? "press-" : "hold-") + k);
                }
                continue;
            }
            if ("jmp".equals(k) && noJmp) {
                boolean down = isKeyDown(mc, k);
                if (down) {
                    boolean prev = prevDown.getOrDefault(k, false);
                    boolean pressed = down && !prev;
                    unexpected.add((pressed ? "press-" : "hold-") + k);
                }
                continue;
            }
            if ("snk".equals(k) && noSnk) {
                boolean down = isKeyDown(mc, k);
                if (down) {
                    boolean prev = prevDown.getOrDefault(k, false);
                    boolean pressed = down && !prev;
                    unexpected.add((pressed ? "press-" : "hold-") + k);
                }
                continue;
            }

            boolean shouldCheck = false;
            // w, a, s, d sont TOUJOURS vérifiés
            if ("w".equals(k) || "a".equals(k) || "s".equals(k) || "d".equals(k)) {
                shouldCheck = true;
            }

            if (!shouldCheck) {
                continue;
            }

            boolean down = isKeyDown(mc, k);
            if (down) {
                boolean prev = prevDown.getOrDefault(k, false);
                boolean pressed = down && !prev;
                unexpected.add((pressed ? "press-" : "hold-") + k);
            }
        }

        return unexpected;
    }

    private boolean tokenMentionsKey(List<TokenSpec> specs, String key) {
        return specs.stream().anyMatch(s -> !s.isWait && key.equals(s.key));
    }

    private void updateLenientWindows(Minecraft mc, List<TokenSpec> specs) {
        for (TokenSpec ts : specs) {
            if (!ts.isWait && ts.mode == Mode.LENIENT) {
                LenientWindow w = lenientWindows.get(ts.key);
                if (w == null) {
                    // Nouvelle fenêtre lenient
                    w = new LenientWindow();
                    w.startTick = tickIndex;
                    w.lastTick = tickIndex;
                    w.satisfied = false;

                    // Utiliser l'expectedAction du TokenSpec
                    w.expectedAction = ts.expectedAction;

                    if (ts.expectedAction == Action.PRESS) {
                        // Pour un PRESS, vérifier qu'il y a une nouvelle pression
                        boolean down = isKeyDown(mc, ts.key);
                        boolean prev = prevDown.getOrDefault(ts.key, false);
                        if (down && !prev) {
                            w.satisfied = true;
                        }
                    } else if (ts.expectedAction == Action.RELEASE) {
                        // Pour un RELEASE, vérifier qu'il y a un relâchement
                        boolean down = isKeyDown(mc, ts.key);
                        if (!down) {
                            // La touche a été relâchée → satisfait
                            w.satisfied = true;
                        }
                    }

                    lenientWindows.put(ts.key, w);
                } else {
                    // Fenêtre existante, on la prolonge
                    w.lastTick = tickIndex;

                    // Vérifier si satisfait selon le type d'action attendu
                    if (!w.satisfied) {
                        if (w.expectedAction == Action.PRESS) {
                            // Pour un PRESS, vérifier qu'il y a une nouvelle pression
                            boolean down = isKeyDown(mc, ts.key);
                            boolean prev = prevDown.getOrDefault(ts.key, false);
                            if (down && !prev) {
                                w.satisfied = true;
                            }
                        } else if (w.expectedAction == Action.RELEASE) {
                            // Pour un RELEASE, vérifier qu'il y a un relâchement
                            boolean down = isKeyDown(mc, ts.key);
                            if (!down) {
                                // La touche a été relâchée → satisfait
                                w.satisfied = true;
                            }
                        }
                    }
                }
            }
        }
    }

    private LenientFailure resolveLenientWindowsEndingThisTick(List<TokenSpec> specsThisTick) {
        Set<String> lenientNow = new HashSet<>();
        for (TokenSpec ts : specsThisTick) {
            if (!ts.isWait && ts.mode == Mode.LENIENT) {
                lenientNow.add(ts.key);
            }
        }

        List<String> toClose = new ArrayList<>();
        for (String key : lenientWindows.keySet()) {
            if (!lenientNow.contains(key)) toClose.add(key);
        }

        for (String key : toClose) {
            LenientWindow w = lenientWindows.remove(key);
            if (w != null && !w.satisfied) {
                return new LenientFailure(key, w.expectedAction, w.startTick, w.lastTick);
            }
        }
        return null;
    }

    private LenientFailure closeAllLenientWindowsIfNeeded() {
        for (Map.Entry<String, LenientWindow> it : lenientWindows.entrySet()) {
            if (it.getValue() != null && !it.getValue().satisfied) {
                LenientWindow w = it.getValue();
                String key = it.getKey();
                lenientWindows.clear();
                return new LenientFailure(key, w.expectedAction, w.startTick, w.lastTick);
            }
        }
        lenientWindows.clear();
        return null;
    }

    private void failStop(String elementName, String reason) {
        HudLog.clear();
        HudLog.setStatus("§cFail " + elementName + ":");

        // Vérifier si c'est un message lenient échoué
        if (reason.startsWith("Lenient input not triggered: [")) {
            // Format: "Lenient input not triggered: [key] in ticks start-end"
            int keyStart = reason.indexOf('[') + 1;
            int keyEnd = reason.indexOf(']');
            String key = reason.substring(keyStart, keyEnd);

            int ticksIdx = reason.indexOf("in ticks ");
            String ticksStr = reason.substring(ticksIdx + 9);
            String[] ticks = ticksStr.split("-");
            int startTick = Integer.parseInt(ticks[0]);
            int endTick = Integer.parseInt(ticks[1]);

            HudLog.pushLenientFailed(key, startTick, endTick);
        } else {
            HudLog.push(ColorConfig.getContentColorCode() + reason);
        }

        StatsTracker.recordFail(reason);
        resetAll();
    }

    private void failStopLenient(String elementName, LenientFailure failure) {
        HudLog.clear();
        HudLog.setStatus("§cFail " + elementName + ":");

        // Afficher avec les bonnes couleurs
        String actionStr = failure.expectedAction == Action.PRESS ? "pressed" : "released";

        // Inverser l'ordre car HudLog.add(0) ajoute au début
        HudLog.pushLenientFailedNew(failure.key, actionStr, failure.startTick + 1, failure.lastTick + 1);

        resetAll();
    }

    private boolean hasAnyRelevantActivityThisTick(Minecraft mc, List<TokenSpec> specs) {
        // Vérifier les touches de base (toujours checked)
        for (String k : Arrays.asList("w", "a", "s", "d")) {
            boolean down = isKeyDown(mc, k);
            boolean prev = prevDown.getOrDefault(k, false);
            if (down && !prev) return true;
        }

        // Vérifier les touches cochées via checkboxes
        CheckElement active = ElementStore.getActive();
        if (active != null && tickIndex < active.tickInputs.size()) {
            boolean checkSpr = tickIndex < active.checkSprint.size() && active.checkSprint.get(tickIndex);
            boolean checkJmp = tickIndex < active.checkJump.size() && active.checkJump.get(tickIndex);
            boolean checkSnk = tickIndex < active.checkSneak.size() && active.checkSneak.get(tickIndex);

            if (checkSpr) {
                boolean down = isKeyDown(mc, "spr");
                boolean prev = prevDown.getOrDefault("spr", false);
                if (down && !prev) return true;
            }

            if (checkJmp) {
                boolean down = isKeyDown(mc, "jmp");
                boolean prev = prevDown.getOrDefault("jmp", false);
                if (down && !prev) return true;
            }

            if (checkSnk) {
                boolean down = isKeyDown(mc, "snk");
                boolean prev = prevDown.getOrDefault("snk", false);
                if (down && !prev) return true;
            }
        }

        return false;
    }

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

        switch (key) {
            case "w": return mc.gameSettings.keyBindForward;
            case "a": return mc.gameSettings.keyBindLeft;
            case "s": return mc.gameSettings.keyBindBack;
            case "d": return mc.gameSettings.keyBindRight;
            case "jmp": return mc.gameSettings.keyBindJump;
            case "snk": return mc.gameSettings.keyBindSneak;
            case "spr": return mc.gameSettings.keyBindSprint;
            default: return null;
        }
    }

    private boolean isSupportedKey(String key) {
        return "w".equals(key) || "a".equals(key) || "s".equals(key) || "d".equals(key)
                || "jmp".equals(key) || "snk".equals(key) || "spr".equals(key);
    }

    private String normalizeKey(String key) {
        if (key == null) return "";
        key = key.trim().toLowerCase();
        return key;
    }

    private void updatePrevDown(Minecraft mc) {
        for (String k : keysAll()) {
            prevDown.put(k, isKeyDown(mc, k));
        }
    }

    private List<String> keysAll() {
        return Arrays.asList("w", "a", "s", "d", "jmp", "snk", "spr");
    }

    private Set<String> buildActualDown(Minecraft mc) {
        Set<String> s = new LinkedHashSet<>();
        for (String k : keysAll()) {
            if (isKeyDown(mc, k)) s.add(k);
        }
        return s;
    }

    private Set<String> buildActualPressed(Minecraft mc) {
        Set<String> s = new LinkedHashSet<>();
        for (String k : keysAll()) {
            boolean down = isKeyDown(mc, k);
            boolean prev = prevDown.getOrDefault(k, false);
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

    /**
     * Valide que les lenient inputs sont correctement configurés
     * Règle : Un lenient input doit apparaître sur AU MOINS 2 ticks consécutifs
     * Retourne true si valide, false sinon (et affiche le message d'erreur)
     */
    private boolean validateLenientInputs(CheckElement element) {
        if (element == null || element.tickInputs == null) return true;

        for (int i = 0; i < element.tickInputs.size(); i++) {
            String input = element.tickInputs.get(i);
            if (input == null || input.trim().isEmpty()) continue;

            List<TokenSpec> specs = parseTokens(input);
            for (TokenSpec ts : specs) {
                if (!ts.isWait && ts.mode == Mode.LENIENT) {
                    // Vérifier qu'il y a au moins un tick avant ou après avec le même lenient input
                    boolean hasConsecutive = false;

                    // Vérifier le tick précédent
                    if (i > 0) {
                        String prevInput = element.tickInputs.get(i - 1);
                        if (prevInput != null && containsLenientKey(prevInput, ts.key)) {
                            hasConsecutive = true;
                        }
                    }

                    // Vérifier le tick suivant
                    if (i < element.tickInputs.size() - 1) {
                        String nextInput = element.tickInputs.get(i + 1);
                        if (nextInput != null && containsLenientKey(nextInput, ts.key)) {
                            hasConsecutive = true;
                        }
                    }

                    if (!hasConsecutive) {
                        // Afficher l'erreur directement avec les bonnes couleurs
                        HudLog.clear();
                        HudLog.setStatus("§cInvalid configuration:");
                        // Inverser l'ordre car HudLog ajoute au début (0)
                        HudLog.pushValidationText("at least 2 consecutive ticks");

                        // Déterminer le prefix (prs ou rls)
                        String prefix = (ts.expectedAction == Action.PRESS) ? "prs" : "rls";
                        HudLog.pushValidationError(String.valueOf(i + 1), prefix + "-" + ts.key);
                        return false;
                    }
                }
            }
        }

        return true;
    }

    /**
     * Vérifie si un input contient un lenient spécifique (prs- ou rls-)
     */
    private boolean containsLenientKey(String input, String key) {
        if (input == null) return false;
        input = input.toLowerCase();
        return input.contains("prs-" + key) || input.contains("rls-" + key);
    }
}


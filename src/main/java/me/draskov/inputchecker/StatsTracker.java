package me.draskov.inputchecker;

import java.util.*;

public class StatsTracker {
    private static final int MAX_RUNS = 20;

    private static class State {
        List<Boolean> results = new ArrayList<>();
        Map<String, Integer> failReasons = new HashMap<>();
    }

    private static State STATE = new State();
    private static String contextElementId = null;

    public static void resetSession() {
        STATE = new State();
        contextElementId = null;
    }

    public static void ensureContext(String activeElementId) {
        if (activeElementId == null) {
            if (contextElementId != null) {
                resetSession();
            }
            return;
        }

        if (contextElementId == null || !contextElementId.equals(activeElementId)) {
            resetSession();
            contextElementId = activeElementId;
        }
    }

    public static void recordOk() {
        pushResult(true);
    }

    public static void recordFail(String reason) {
        pushResult(false);
        String key = normalizeReason(reason);
        Integer cur = STATE.failReasons.get(key);
        STATE.failReasons.put(key, cur == null ? 1 : (cur + 1));
    }

    public static int getTotal() {
        return STATE.results == null ? 0 : STATE.results.size();
    }

    public static int getOkCount() {
        if (STATE.results == null) return 0;
        int ok = 0;
        for (Boolean b : STATE.results) {
            if (b != null && b) ok++;
        }
        return ok;
    }

    public static int getConsistencyPercent() {
        int total = getTotal();
        return total <= 0 ? 0 : (getOkCount() * 100) / total;
    }

    public static String getMostFrequentMistake() {
        if (STATE.failReasons == null || STATE.failReasons.isEmpty()) {
            return ColorConfig.getTitleColorCode() + "none";
        }

        String best = null;
        int bestCount = -1;

        for (Map.Entry<String, Integer> e : STATE.failReasons.entrySet()) {
            if (e.getKey() == null) continue;
            int c = e.getValue() == null ? 0 : e.getValue();
            if (c > bestCount) {
                bestCount = c;
                best = e.getKey();
            }
        }

        if (best == null || bestCount <= 0) {
            return ColorConfig.getTitleColorCode() + "none";
        }

        // Formater avec les bonnes couleurs
        // Format: "Tick X expected <value> got <value>"
        String formatted = formatMistakeWithColors(best) + ColorConfig.getContentColorCode() + " (" + bestCount + ")";
        return formatted;
    }

    /**
     * Formate une erreur avec les bonnes couleurs
     * Format: "Tick X expected <value> got <value>"
     * Texte en color2, valeurs et numéros en color1 (titleColor)
     */
    private static String formatMistakeWithColors(String mistake) {
        String contentColor = ColorConfig.getContentColorCode();
        String valueColor = ColorConfig.getTitleColorCode();

        // Gérer le nouveau format "Expected {key} {action} in ticks X-Y"
        if (mistake.startsWith("Expected ") && (mistake.contains(" pressed in ticks ") || mistake.contains(" released in ticks "))) {
            // Format: "Expected d pressed in ticks 1-2"
            String afterExpected = mistake.substring(9); // Enlever "Expected "

            String action = "";
            int ticksIdx = -1;
            if (afterExpected.contains(" pressed in ticks ")) {
                action = "pressed";
                ticksIdx = afterExpected.indexOf(" pressed in ticks ");
            } else if (afterExpected.contains(" released in ticks ")) {
                action = "released";
                ticksIdx = afterExpected.indexOf(" released in ticks ");
            }

            String key = afterExpected.substring(0, ticksIdx);
            String ticksStr = afterExpected.substring(ticksIdx + (" " + action + " in ticks ").length());

            return contentColor + "Expected " + valueColor + key + " " + contentColor + action + " between tick " + valueColor + ticksStr.replace("-", contentColor + " and " + valueColor);
        }

        // Gérer le format "Lenient input not triggered: [key] in ticks X-Y"
        if (mistake.startsWith("Lenient input not triggered: [")) {
            int keyStart = mistake.indexOf('[') + 1;
            int keyEnd = mistake.indexOf(']');
            String key = mistake.substring(keyStart, keyEnd);

            int ticksIdx = mistake.indexOf("in ticks ");
            String ticksStr = mistake.substring(ticksIdx + 9);

            return valueColor + key + contentColor + " expected in ticks " + valueColor + ticksStr;
        }

        // Chercher "expected" et "got" dans la phrase
        int expectedIdx = mistake.indexOf("expected ");
        int gotIdx = mistake.indexOf(" got ");

        if (expectedIdx == -1 || gotIdx == -1) {
            // Format non reconnu, retourner tel quel avec la couleur de base
            return contentColor + mistake;
        }

        // Extraire les parties
        // Format: "Tick X expected ..." où X est le numéro de tick
        String tickPart = mistake.substring(0, expectedIdx).trim(); // "Tick X"

        // Séparer "Tick" et le numéro
        String tickNum = "";
        String tickWord = "Tick ";
        if (tickPart.startsWith("Tick ")) {
            tickNum = tickPart.substring(5); // Le numéro après "Tick "
        }

        String expectedValue = mistake.substring(expectedIdx + 9, gotIdx); // valeur expected
        String gotText = " got "; // " got "
        String gotValue = mistake.substring(gotIdx + 5); // valeur got

        return contentColor + tickWord + valueColor + tickNum + contentColor + " expected " + valueColor + expectedValue + contentColor + gotText + valueColor + gotValue;
    }

    public static String colorForPercent(int pct) {
        if (pct >= 80) return "§2";
        if (pct >= 60) return "§a";
        if (pct >= 40) return "§e";
        if (pct >= 20) return "§6";
        return "§4";
    }

    public static List<String> buildHudLines() {
        List<String> out = new ArrayList<>();

        int total = getTotal();
        if (total <= 0) return out;

        int ok = getOkCount();
        int pct = getConsistencyPercent();

        String valueColor = ColorConfig.getTitleColorCode();
        out.add(ColorConfig.getContentColorCode() + "Runs (last " + MAX_RUNS + "): " + valueColor + total);
        out.add(ColorConfig.getContentColorCode() + "Input consistency: " + valueColor + pct + valueColor + "%" + ColorConfig.getContentColorCode() + " (" + ok + "/" + total + ")");
        out.add(ColorConfig.getContentColorCode() + "Most frequent mistake:");
        out.add(getMostFrequentMistake());

        return out;
    }

    private static void pushResult(boolean ok) {
        if (STATE.results == null) STATE.results = new ArrayList<>();

        if (STATE.results.size() >= MAX_RUNS) {
            STATE.results.clear();
            STATE.failReasons.clear();
        }

        STATE.results.add(ok);
    }

    private static String normalizeReason(String r) {
        if (r == null) return "unknown";
        r = r.trim();
        if (r.isEmpty()) return "unknown";
        r = r.replaceAll("§.", "");
        return r.length() > 80 ? r.substring(0, 80) : r;
    }
}

package me.draskov.inputchecker;

import java.util.*;

public class StatsTracker {

    private static final int MAX_RUNS = 20;

    private static class State {
        List<Boolean> results = new ArrayList<Boolean>(); // true=OK, false=FAIL
        Map<String, Integer> failReasons = new HashMap<String, Integer>(); // reason -> count
    }

    private static State STATE = new State();

    // Context (active element)
    private static String contextElementId = null;

    /** Reset stats for the current session (no persistence). */
    public static void resetSession() {
        STATE = new State();
        contextElementId = null;
    }

    /**
     * Ensure stats context matches current active element.
     * If the active element changes (or becomes null), stats are reset.
     */
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
        STATE.failReasons.put(key, Integer.valueOf(cur == null ? 1 : (cur.intValue() + 1)));
    }

    public static int getTotal() {
        return (STATE.results == null) ? 0 : STATE.results.size();
    }

    public static int getOkCount() {
        if (STATE.results == null) return 0;
        int ok = 0;
        for (Boolean b : STATE.results) if (b != null && b.booleanValue()) ok++;
        return ok;
    }

    public static int getConsistencyPercent() {
        int total = getTotal();
        if (total <= 0) return 0;
        int ok = getOkCount();
        return (ok * 100) / total;
    }

    public static String getMostFrequentMistake() {
        if (STATE.failReasons == null || STATE.failReasons.isEmpty()) return "none";

        String best = null;
        int bestCount = -1;

        for (Map.Entry<String, Integer> e : STATE.failReasons.entrySet()) {
            if (e.getKey() == null) continue;
            int c = (e.getValue() == null) ? 0 : e.getValue().intValue();
            if (c > bestCount) {
                bestCount = c;
                best = e.getKey();
            }
        }

        if (best == null || bestCount <= 0) return "none";
        return best + " (" + bestCount + ")";
    }

    public static String colorForPercent(int pct) {
        if (pct >= 80) return "§2";   // dark green
        if (pct >= 60) return "§a";   // green
        if (pct >= 40) return "§e";   // yellow
        if (pct >= 20) return "§6";   // gold/orange
        return "§4";                  // dark red
    }

    /** Lines for the stats panel. If no data, return empty list (= panel looks empty). */
    public static List<String> buildHudLines() {
        List<String> out = new ArrayList<String>();

        int total = getTotal();
        if (total <= 0) return out; // << empty panel when nothing recorded

        int ok = getOkCount();
        int pct = getConsistencyPercent();
        String pctColor = colorForPercent(pct);

        out.add("§7Runs (last " + MAX_RUNS + "): §f" + total);
        out.add("§7Input consistency: " + pctColor + pct + "§7%  §8(" + ok + "/" + total + ")");
        out.add("§7Most frequent mistake:");
        out.add("§c" + getMostFrequentMistake());

        return out;
    }

    // ---- internal ----

    private static void pushResult(boolean ok) {
        if (STATE.results == null) STATE.results = new ArrayList<Boolean>();

        // RESET COMPLET quand on dépasse MAX_RUNS
        if (STATE.results.size() >= MAX_RUNS) {
            STATE.results.clear();
            STATE.failReasons.clear();
        }

        STATE.results.add(Boolean.valueOf(ok));
    }

    private static String normalizeReason(String r) {
        if (r == null) return "unknown";
        r = r.trim();
        if (r.length() == 0) return "unknown";
        r = r.replaceAll("§.", ""); // strip MC color codes
        if (r.length() > 80) r = r.substring(0, 80);
        return r;
    }
}

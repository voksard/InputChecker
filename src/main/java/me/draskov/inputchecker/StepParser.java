package me.draskov.inputchecker;

public class StepParser {

    public enum Type { HOLD, PRESS, RELEASE }

    public static class Step {
        public Type type;
        public int ticks;      // HOLD
        public String key;     // w, a, space...
    }

    public static Step parse(String raw) {
        raw = raw.trim().toLowerCase();
        Step s = new Step();

        // "12t w"
        if (raw.matches("\\d+\\s*t\\s+.+")) {
            String[] parts = raw.split("\\s+");
            s.type = Type.HOLD;
            s.ticks = Integer.parseInt(parts[0].replace("t", ""));
            s.key = parts[1];
            return s;
        }

        // "press space"
        if (raw.startsWith("press ")) {
            s.type = Type.PRESS;
            s.key = raw.substring("press ".length()).trim();
            return s;
        }

        // "release w"
        if (raw.startsWith("release ")) {
            s.type = Type.RELEASE;
            s.key = raw.substring("release ".length()).trim();
            return s;
        }

        // fallback: HOLD 1 tick
        s.type = Type.HOLD;
        s.ticks = 1;
        s.key = raw;
        return s;
    }
}

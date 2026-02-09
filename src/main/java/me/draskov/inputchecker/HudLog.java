package me.draskov.inputchecker;

import java.util.ArrayList;
import java.util.List;

public class HudLog {
    private static final int MAX = 14;

    private static final List<String> lines = new ArrayList<String>();
    private static String statusLine = "§bInputChecker§7: No active element";

    public static void setStatus(String s) {
        statusLine = s;
    }

    public static String getStatus() {
        return statusLine;
    }

    public static void push(String s) {
        lines.add(0, s); // newest on top
        while (lines.size() > MAX) lines.remove(lines.size() - 1);
    }

    public static List<String> getLines() {
        return lines;
    }

    public static void clear() {
        lines.clear();
    }
}

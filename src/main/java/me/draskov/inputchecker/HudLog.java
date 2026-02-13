package me.draskov.inputchecker;

import java.util.ArrayList;
import java.util.List;

public class HudLog {
    private static final int MAX = 14;
    private static final List<HudLine> hudLines = new ArrayList<>();
    private static String statusLine;

    // Énumération des types de contenu pour appliquer les couleurs dynamiquement
    private enum LineType {
        RUNNING,           // "Running {element}"
        NO_ACTIVE,         // "No active element"
        SEQUENCE_COMPLETED, // "Sequence completed"
        GENERIC,           // Contenu générique avec color2
        GOT,               // "Got: {keys}"
        EXPECTED,          // "Expected: {expected} tick {num}"
        RIGHT_CLICK_START, // "Right click to start"
        WARNING            // Messages d'avertissement (couleur dorée)
    }

    private static class HudLine {
        LineType type;
        String content; // Contenu brut sans codes couleur
        String[] parts; // Parties du contenu pour reconstruction

        HudLine(LineType type, String content) {
            this.type = type;
            this.content = content;
            this.parts = null;
        }

        HudLine(LineType type, String[] parts) {
            this.type = type;
            this.content = null;
            this.parts = parts;
        }

        String buildWithCurrentColors() {
            switch (type) {
                case RUNNING:
                case NO_ACTIVE:
                case SEQUENCE_COMPLETED:
                case RIGHT_CLICK_START:
                case GENERIC:
                    return ColorConfig.getContentColorCode() + content;

                case WARNING:
                    // Garder la couleur dorée pour les avertissements
                    return "§6" + content;

                case GOT:
                    // "Got: " + keys
                    return ColorConfig.getContentColorCode() + "Got: " + ColorConfig.getTitleColorCode() + parts[0];

                case EXPECTED:
                    // "Expected: " + expected + " tick " + tickNum
                    return ColorConfig.getContentColorCode() + "Expected: " + ColorConfig.getTitleColorCode() + parts[0] +
                           " " + ColorConfig.getContentColorCode() + "tick " + ColorConfig.getTitleColorCode() + parts[1];

                default:
                    return ColorConfig.getContentColorCode() + content;
            }
        }
    }

    static {
        updateStatusLine();
    }

    public static void setStatus(String s) {
        statusLine = s;
    }

    public static String getStatus() {
        return statusLine;
    }

    public static void push(String s) {
        // Nettoyer la chaîne des codes couleur existants
        String cleaned = s.replaceAll("§.", "");

        // Reconnaître le type de ligne et la stocker
        HudLine line;
        if (cleaned.startsWith("Running ")) {
            line = new HudLine(LineType.RUNNING, cleaned.substring(8));
        } else if (cleaned.equals("No active element")) {
            line = new HudLine(LineType.NO_ACTIVE, cleaned);
        } else if (cleaned.equals("Sequence completed")) {
            line = new HudLine(LineType.SEQUENCE_COMPLETED, cleaned);
        } else if (cleaned.equals("Right click to start")) {
            line = new HudLine(LineType.RIGHT_CLICK_START, cleaned);
        } else if (cleaned.startsWith("Got: ")) {
            String got = cleaned.substring(5);
            line = new HudLine(LineType.GOT, new String[]{got});
        } else if (cleaned.startsWith("Expected: ")) {
            String rest = cleaned.substring(10);
            int tickIdx = rest.lastIndexOf(" tick ");
            if (tickIdx != -1) {
                String expected = rest.substring(0, tickIdx);
                String tickNum = rest.substring(tickIdx + 6);
                line = new HudLine(LineType.EXPECTED, new String[]{expected, tickNum});
            } else {
                line = new HudLine(LineType.GENERIC, cleaned);
            }
        } else if (s.contains("§6")) {
            // C'est un message d'avertissement (couleur dorée)
            line = new HudLine(LineType.WARNING, cleaned);
        } else {
            line = new HudLine(LineType.GENERIC, cleaned);
        }

        hudLines.add(0, line);
        while (hudLines.size() > MAX) {
            hudLines.remove(hudLines.size() - 1);
        }
    }

    public static List<String> getLines() {
        List<String> result = new ArrayList<>();
        for (HudLine line : hudLines) {
            result.add(line.buildWithCurrentColors());
        }
        return result;
    }

    public static void clear() {
        hudLines.clear();
        updateStatusLine();
    }

    private static void updateStatusLine() {
        statusLine = ColorConfig.getTitleColorCode() + "Inputchecker check:";
    }

    public static void updateColors() {
        updateStatusLine();
        // Les lignes vont être reconstruites avec les nouvelles couleurs lors du prochain appel à getLines()
    }
}

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
        WARNING,           // Messages d'avertissement (couleur dorée)
        LENIENT_FAILED,    // "Lenient input not triggered: {key} in ticks {start}-{end}"
        LENIENT_FAILED_NEW, // "Expected {key} {action}" ou "between tick {start} and {end}" avec parties en color1
        VALIDATION_ERROR   // Messages d'erreur de validation avec parties: [tick, key] pour "Tick {tick}: prs-{key}/rls-{key} must span" ou texte simple
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

                case LENIENT_FAILED:
                    // "{key} expected in ticks {start}-{end}" avec key, start, end en color1
                    return ColorConfig.getTitleColorCode() + parts[0] + " " +
                           ColorConfig.getContentColorCode() + "expected in ticks " +
                           ColorConfig.getTitleColorCode() + parts[1] + "-" + parts[2];

                case LENIENT_FAILED_NEW:
                    // Format: "Expected {key} {action}" ou "between tick {start} and {end}"
                    // parts[0] = key, parts[1] = action, parts[2] = start, parts[3] = end
                    if (parts.length == 1) {
                        // Ligne "between tick X and Y"
                        return ColorConfig.getContentColorCode() + content;
                    } else if (parts.length == 4) {
                        // Ligne "Expected {key} {action}"
                        return ColorConfig.getContentColorCode() + "Expected " +
                               ColorConfig.getTitleColorCode() + parts[0] + " " +
                               ColorConfig.getContentColorCode() + parts[1];
                    } else if (parts.length == 2) {
                        // Ligne "between tick X and Y" avec X et Y en color1
                        return ColorConfig.getContentColorCode() + "between tick " +
                               ColorConfig.getTitleColorCode() + parts[0] +
                               ColorConfig.getContentColorCode() + " and " +
                               ColorConfig.getTitleColorCode() + parts[1];
                    } else {
                        return ColorConfig.getContentColorCode() + content;
                    }

                case VALIDATION_ERROR:
                    // Si parts existe: "Tick {tick}: {key} must span" avec tick et key en color1
                    // key contient déjà le prefix (prs- ou rls-)
                    // Sinon: texte simple en color2
                    if (parts != null && parts.length == 2) {
                        return ColorConfig.getContentColorCode() + "Tick " +
                               ColorConfig.getTitleColorCode() + parts[0] +
                               ColorConfig.getContentColorCode() + ": " +
                               ColorConfig.getTitleColorCode() + parts[1] +
                               ColorConfig.getContentColorCode() + " must span";
                    } else {
                        return ColorConfig.getContentColorCode() + content;
                    }

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

    public static void pushLenientFailed(String key, int startTick, int endTick) {
        // "{key} expected in ticks {start}-{end}" avec couleurs appropriées
        HudLine contentLine = new HudLine(LineType.LENIENT_FAILED, new String[]{key, String.valueOf(startTick), String.valueOf(endTick)});
        hudLines.add(0, contentLine);

        while (hudLines.size() > MAX) {
            hudLines.remove(hudLines.size() - 1);
        }
    }

    public static void pushLenientFailedNew(String key, String action, int startTick, int endTick) {
        // Nouveau format: "Expected {key} {action}" avec saut de ligne "between tick {start} and {end}"
        // Inverser l'ordre car add(0) ajoute au début
        HudLine tickLine = new HudLine(LineType.LENIENT_FAILED_NEW, new String[]{String.valueOf(startTick), String.valueOf(endTick)});
        hudLines.add(0, tickLine);

        HudLine expectedLine = new HudLine(LineType.LENIENT_FAILED_NEW, new String[]{key, action, String.valueOf(startTick), String.valueOf(endTick)});
        hudLines.add(0, expectedLine);

        while (hudLines.size() > MAX) {
            hudLines.remove(hudLines.size() - 1);
        }
    }

    public static void pushValidationError(String tick, String key) {
        // "Tick {tick}: {key} must span" avec tick et key en color1
        // key contient déjà le prefix (prs- ou rls-)
        HudLine line = new HudLine(LineType.VALIDATION_ERROR, new String[]{tick, key});
        hudLines.add(0, line);

        while (hudLines.size() > MAX) {
            hudLines.remove(hudLines.size() - 1);
        }
    }

    public static void pushValidationText(String text) {
        // Texte simple pour les messages de validation
        HudLine line = new HudLine(LineType.VALIDATION_ERROR, text);
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

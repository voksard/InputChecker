package me.draskov.inputchecker;

import net.minecraft.command.CommandBase;
import net.minecraft.command.ICommandSender;
import net.minecraft.util.ChatComponentText;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

public class InputCheckerCommand extends CommandBase {

    @Override
    public String getCommandName() {
        return "inputchecker";
    }

    @Override
    public String getCommandUsage(ICommandSender sender) {
        return "/inputchecker help";
    }

    @Override
    public List<String> getCommandAliases() {
        return Arrays.asList("ic");
    }

    @Override
    public void processCommand(ICommandSender sender, String[] args) {
        if (args == null || args.length == 0 || "help".equalsIgnoreCase(args[0])) {
            sendHelp(sender);
            return;
        }

        if ("hud".equalsIgnoreCase(args[0])) {
            sendLine(sender, "§eInputchecker HUD");
            sendLine(sender, "§7Use §fF9 §7to open the HUD editor (move / hide / show).");
            sendLine(sender, "§7Right click in the editor toggles hide/show.");
            return;
        }

        if ("status".equalsIgnoreCase(args[0])) {
            CheckElement active = ElementStore.getActive();
            String activeName = active == null ? "none" : active.name;
            boolean fs = InputCheckerConfig.get().fullSprint;

            sendLine(sender, "§eInputchecker Status");
            sendLine(sender, "§7Active element: §f" + activeName);
            sendLine(sender, "§7HUD visible: §f" + (HudConfig.get().visible ? "yes" : "no"));
            sendLine(sender, "§7Restart checking: §fRight click (in-game)");
            sendLine(sender, "§7FullSprint: §f" + (fs ? "ON" : "OFF"));
            if (fs) {
                sendLine(sender, "§7- sprint is ignored unless explicitly written.");
            } else {
                sendLine(sender, "§7- sprint is checked like other keys.");
            }
            return;
        }

        if ("color1".equalsIgnoreCase(args[0])) {
            if (args.length < 2) {
                sendLine(sender, "§cUsage: /inputchecker color1 <color>");
                return;
            }
            String colorName = args[1].toLowerCase();
            Integer colorValue = ColorConfig.parseColor(colorName);
            if (colorValue == null) {
                sendLine(sender, "§cUnknown color: " + colorName);
                sendLine(sender, "§7Use /inputchecker colorlist to see available colors");
                return;
            }
            ColorConfig.get().titleColor = colorValue;
            ColorConfig.save();
            sendLine(sender, "§aTitle color changed to §f" + colorName);

            // Rafraîchir le HUD pour appliquer la nouvelle couleur immédiatement
            refreshHudDisplay();
            return;
        }

        if ("color2".equalsIgnoreCase(args[0])) {
            if (args.length < 2) {
                sendLine(sender, "§cUsage: /inputchecker color2 <color>");
                return;
            }
            String colorName = args[1].toLowerCase();
            Integer colorValue = ColorConfig.parseColor(colorName);
            if (colorValue == null) {
                sendLine(sender, "§cUnknown color: " + colorName);
                sendLine(sender, "§7Use /inputchecker colorlist to see available colors");
                return;
            }
            ColorConfig.get().contentColor = colorValue;
            ColorConfig.save();
            sendLine(sender, "§aContent color changed to §f" + colorName);

            // Rafraîchir le HUD pour appliquer la nouvelle couleur immédiatement
            refreshHudDisplay();
            return;
        }

        if ("colorlist".equalsIgnoreCase(args[0])) {
            sendLine(sender, "§eAvailable Colors:");

            // Mapping des noms de couleurs aux codes Minecraft
            java.util.Map<String, String> colorCodes = new java.util.LinkedHashMap<>();
            colorCodes.put("black", "§0");
            colorCodes.put("dblue", "§1");
            colorCodes.put("dgreen", "§2");
            colorCodes.put("daqua", "§3");
            colorCodes.put("dred", "§4");
            colorCodes.put("dpurple", "§5");
            colorCodes.put("gold", "§6");
            colorCodes.put("gray", "§7");
            colorCodes.put("dgray", "§8");
            colorCodes.put("blue", "§9");
            colorCodes.put("green", "§a");
            colorCodes.put("aqua", "§b");
            colorCodes.put("red", "§c");
            colorCodes.put("lpurple", "§d");
            colorCodes.put("yellow", "§e");
            colorCodes.put("white", "§f");

            for (java.util.Map.Entry<String, Integer> entry : ColorConfig.getColorMap().entrySet()) {
                String colorName = entry.getKey();
                String colorCode = colorCodes.get(colorName);
                if (colorCode != null) {
                    sendLine(sender, colorCode + colorName + "§r");
                }
            }
            return;
        }

        sendLine(sender, "§cUnknown subcommand. Use §f/inputchecker help§c.");
    }

    private void sendHelp(ICommandSender sender) {
        sendLine(sender, "§eInputchecker Help");
        sendLine(sender, "");

        sendLine(sender, "§7==== §eBasic Commands §7====");
        sendLine(sender, "§fG §7- Open catalog/editor GUI");
        sendLine(sender, "§fRight Click §7- Restart checking active element");
        sendLine(sender, "§fF9 §7- Open HUD editor (drag/hide/show)");
        sendLine(sender, "");

        sendLine(sender, "§7==== §eTimeline Syntax (Per Tick) §7====");
        sendLine(sender, "§7Write tokens separated by §f+");
        sendLine(sender, "§7Example: §fw+a+jmp");
        sendLine(sender, "");

        sendLine(sender, "§7==== §eToken Types §7====");
        sendLine(sender, "§fKEY §7- Required (must press/hold this tick)");
        sendLine(sender, "§flnt-KEY §7- Lenient (optional, but must occur at least once");
        sendLine(sender, "              §7in the lenient window)");
        sendLine(sender, "");

        sendLine(sender, "§7==== §eSupported Keys §7====");
        sendLine(sender, "§fMovement: §fw, a, s, d");
        sendLine(sender, "§fSpecial: §fjmp (jump), snk (sneak), spr (sprint)");
        sendLine(sender, "");

        sendLine(sender, "§7==== §ePress/Hold Logic §7====");
        sendLine(sender, "§7Automatic (do NOT write manually):");
        sendLine(sender, "§7- First occurrence of key = press");
        sendLine(sender, "§7- Consecutive ticks = hold");
        sendLine(sender, "");

        sendLine(sender, "§7==== §eChecking Behavior §7====");
        sendLine(sender, "§7ALWAYS checked: §fw, a, s, d");
        sendLine(sender, "§7OPTIONAL (use GUI checkboxes):");
        sendLine(sender, "  §fjmp §7- enable jmp checkbox");
        sendLine(sender, "  §fsnk §7- enable snk checkbox");
        sendLine(sender, "  §fspr §7- enable spr checkbox");
        sendLine(sender, "§7NOT shown in Got if NOT checked");
        sendLine(sender, "");

        sendLine(sender, "§7==== §eSpecial Checkboxes §7====");
        sendLine(sender, "§fns (no sprint) §7- Forbid sprint on this tick");
        sendLine(sender, "§fnj (no jump) §7- Forbid jump on this tick");
        sendLine(sender, "§fnk (no sneak) §7- Forbid sneak on this tick");
        sendLine(sender, "");

        sendLine(sender, "§7==== §eColor Commands §7====");
        sendLine(sender, "§f/inputchecker color1 <color> §7- Title/value color");
        sendLine(sender, "§f/inputchecker color2 <color> §7- Content/text color");
        sendLine(sender, "§f/inputchecker colorlist §7- Show all colors");
        sendLine(sender, "");
    }

    private void sendLine(ICommandSender sender, String text) {
        sender.addChatMessage(new ChatComponentText(text));
    }

    /**
     * Rafraîchit le HUD pour appliquer les nouvelles couleurs immédiatement
     */
    private void refreshHudDisplay() {
        // Récupérer le statut et les lignes actuels
        String currentStatus = HudLog.getStatus();
        List<String> currentLines = new ArrayList<>(HudLog.getLines());

        // Si le HUD est vide, rien à rafraîchir
        if (currentLines.isEmpty()) {
            return;
        }

        // Reconstruire le HUD avec les nouvelles couleurs
        HudLog.clear();

        // Si on a un statut, le remettre
        if (currentStatus != null && !currentStatus.isEmpty()) {
            HudLog.setStatus(currentStatus);
        }

        // Obtenir le nouveau code de couleur
        String newColor = ColorConfig.getContentColorCode();
        String valueColor = ColorConfig.getTitleColorCode();

        // Reconstruire chaque ligne intelligemment
        for (String line : currentLines) {
            // Supprimer tous les codes de couleur pour analyser le contenu
            String plainText = line.replaceAll("§.", "");

            // Reconstruire la ligne avec les bonnes couleurs selon le contenu
            if (plainText.equals("No active element") || plainText.equals("Right click to start") ||
                plainText.startsWith("Running ") || plainText.equals("Sequence completed") ||
                plainText.equals("No data yet")) {
                // Texte simple avec juste la couleur de contenu
                HudLog.push(newColor + plainText);
            } else if (plainText.startsWith("Got: ")) {
                // Format: "Got: <valeur>" - valeur en color1
                String value = plainText.substring(5); // Enlever "Got: "
                HudLog.push(newColor + "Got: " + valueColor + value);
            } else if (plainText.startsWith("Expected: ")) {
                // Format: "Expected: <valeur> tick <num>" - valeurs en color1
                String rest = plainText.substring(10); // Enlever "Expected: "

                // Chercher " tick " pour séparer la valeur du numéro de tick
                int tickIdx = rest.indexOf(" tick ");
                if (tickIdx != -1) {
                    String expectedValue = rest.substring(0, tickIdx);
                    String tickNum = rest.substring(tickIdx + 6); // +6 pour " tick "
                    HudLog.push(newColor + "Expected: " + valueColor + expectedValue + " " + newColor + "tick " + valueColor + tickNum);
                } else {
                    HudLog.push(newColor + "Expected: " + valueColor + rest);
                }
            } else if (plainText.startsWith("Runs (last ")) {
                // Format: "Runs (last X): <valeur>" - valeur en color1
                int colonIdx = plainText.indexOf(": ");
                if (colonIdx != -1) {
                    String label = plainText.substring(0, colonIdx + 2);
                    String value = plainText.substring(colonIdx + 2);
                    HudLog.push(newColor + label + valueColor + value);
                } else {
                    HudLog.push(newColor + plainText);
                }
            } else if (plainText.startsWith("Input consistency: ")) {
                // Format: "Input consistency: XX% (ok/total)" - valeurs et % en color1
                String rest = plainText.substring(19); // Enlever "Input consistency: "

                // Chercher le pourcentage
                int pctIdx = rest.indexOf("%");
                if (pctIdx != -1) {
                    String pct = rest.substring(0, pctIdx);
                    String afterPct = rest.substring(pctIdx + 1).trim(); // " (ok/total)"
                    HudLog.push(newColor + "Input consistency: " + valueColor + pct + valueColor + "%" + newColor + " " + afterPct);
                } else {
                    HudLog.push(newColor + plainText);
                }
            } else if (plainText.equals("Most frequent mistake:")) {
                // Label en color2
                HudLog.push(newColor + plainText);
            } else if (plainText.contains("expected ") && plainText.contains(" got ")) {
                // Ligne de Most frequent mistake avec format "Tick X expected <value> got <value> (count)"
                // Formater avec les bonnes couleurs
                int expectedIdx = plainText.indexOf("expected ");
                int gotIdx = plainText.indexOf(" got ");

                // Extraire "Tick X"
                String tickPart = plainText.substring(0, expectedIdx).trim(); // "Tick X"
                String tickNum = "";
                String tickWord = "Tick ";
                if (tickPart.startsWith("Tick ")) {
                    tickNum = tickPart.substring(5); // Le numéro après "Tick "
                }

                String expectedValue = plainText.substring(expectedIdx + 9, gotIdx); // valeur expected
                String gotText = " got "; // " got "

                // Trouver où se termine la valeur got (avant le "(count)")
                String after = plainText.substring(gotIdx + 5);
                int parenIdx = after.lastIndexOf(" (");
                String gotValue;
                String countPart = "";
                if (parenIdx != -1) {
                    gotValue = after.substring(0, parenIdx);
                    countPart = after.substring(parenIdx); // " (count)"
                } else {
                    gotValue = after;
                }

                HudLog.push(newColor + tickWord + valueColor + tickNum + newColor + " expected " + valueColor + expectedValue + newColor + gotText + valueColor + gotValue + newColor + countPart);
            } else if (plainText.equals("none")) {
                // "none" doit être en color1
                HudLog.push(valueColor + plainText);
            } else {
                // Pour toutes les autres lignes
                // Appliquer la couleur de contenu
                HudLog.push(newColor + plainText);
            }
        }
    }

    @Override
    public int getRequiredPermissionLevel() {
        return 0; // everyone
    }
}

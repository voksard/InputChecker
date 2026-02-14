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

        if ("export".equalsIgnoreCase(args[0])) {
            if (args.length < 2) {
                sendLine(sender, "§cUsage: /inputchecker export <element_name>");
                return;
            }
            String elementName = String.join(" ", Arrays.copyOfRange(args, 1, args.length));
            CheckElement element = findElementByName(elementName);

            if (element == null) {
                sendLine(sender, "§cElement not found: " + elementName);
                return;
            }

            String filename = ElementStore.exportElement(element);
            if (filename != null) {
                sendLine(sender, "§aElement exported to: §f" + filename);
                sendLine(sender, "§7File location: §fconfig/" + filename);
            } else {
                sendLine(sender, "§cFailed to export element");
            }
            return;
        }

        if ("import".equalsIgnoreCase(args[0])) {
            if (args.length < 2) {
                sendLine(sender, "§cUsage: /inputchecker import <filename>");
                sendLine(sender, "§7Example: /inputchecker import myElement.json");
                return;
            }
            String filename = args[1];
            if (!filename.endsWith(".json")) {
                filename += ".json";
            }

            CheckElement imported = ElementStore.importElement(filename);
            if (imported != null) {
                sendLine(sender, "§aElement imported: §f" + imported.name);
                sendLine(sender, "§7Open the catalog (G) to see it");
            } else {
                sendLine(sender, "§cFailed to import element from: " + filename);
                sendLine(sender, "§7Make sure the file exists in config/ folder");
            }
            return;
        }

        sendLine(sender, "§cUnknown command. Use /inputchecker help");
    }

    private CheckElement findElementByName(String name) {
        for (CheckElement e : ElementStore.elements) {
            if (e.name.equalsIgnoreCase(name)) {
                return e;
            }
        }
        return null;
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
        sendLine(sender, "§fprs-KEY §7- Pressed input (must press at least once)");
        sendLine(sender, "§frls-KEY §7- Released input (must release at least once)");
        sendLine(sender, "§7Note: prs-/rls- must span at least 2 consecutive ticks");
        sendLine(sender, "");

        sendLine(sender, "§7==== §eSupported Keys §7====");
        sendLine(sender, "§fMovement: §fw, a, s, d");
        sendLine(sender, "§fSpecial: §fjmp (jump), snk (sneak), spr (sprint)");
        sendLine(sender, "");

        sendLine(sender, "§7==== §eExamples §7====");
        sendLine(sender, "§7Standard input:");
        sendLine(sender, "  §fw+a §7- Press W and A on this tick");
        sendLine(sender, "");
        sendLine(sender, "§7Pressed lenient (must press at least once):");
        sendLine(sender, "  Tick 1: §fw+prs-d");
        sendLine(sender, "  Tick 2: §fw+prs-d");
        sendLine(sender, "  §7→ D must be pressed on tick 1 OR tick 2");
        sendLine(sender, "");
        sendLine(sender, "§7Released lenient (must release at least once):");
        sendLine(sender, "  Tick 1: §fw+d §7(D is held)");
        sendLine(sender, "  Tick 2: §fw+rls-d");
        sendLine(sender, "  Tick 3: §fw+rls-d");
        sendLine(sender, "  §7→ D must be released on tick 2 OR tick 3");
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

        sendLine(sender, "§7==== §eSharing Elements §7====");
        sendLine(sender, "§f/inputchecker export <name> §7- Export element to file");
        sendLine(sender, "§f/inputchecker import <file> §7- Import element from file");
        sendLine(sender, "§7Files are saved in: §fconfig/");
        sendLine(sender, "");
    }

    private void sendLine(ICommandSender sender, String text) {
        sender.addChatMessage(new ChatComponentText(text));
    }

    /**
     * Rafraîchit le HUD pour appliquer les nouvelles couleurs immédiatement
     */
    private void refreshHudDisplay() {
        HudLog.updateColors();
    }

    @Override
    public int getRequiredPermissionLevel() {
        return 0; // everyone
    }
}

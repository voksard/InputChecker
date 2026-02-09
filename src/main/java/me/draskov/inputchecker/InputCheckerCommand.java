package me.draskov.inputchecker;

import net.minecraft.command.CommandBase;
import net.minecraft.command.ICommandSender;
import net.minecraft.util.ChatComponentText;

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
            sendLine(sender, "§eInputChecker HUD");
            sendLine(sender, "§7Use §fF9 §7to open the HUD editor (move / hide / show).");
            sendLine(sender, "§7Right click in the editor toggles hide/show.");
            return;
        }

        if ("status".equalsIgnoreCase(args[0])) {
            CheckElement active = ElementStore.getActive();
            String activeName = (active == null) ? "none" : active.name;

            boolean fs = InputCheckerConfig.get().fullSprint;

            sendLine(sender, "§eInputChecker Status");
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

        sendLine(sender, "§cUnknown subcommand. Use §f/inputchecker help§c.");
    }

    private void sendHelp(ICommandSender sender) {
        sendLine(sender, "§eInputChecker Help");
        sendLine(sender, "§7Open GUI: §fG");
        sendLine(sender, "§7Restart checking: §fRight click (in-game)");
        sendLine(sender, "§7Edit HUD: §fF9 §7(then drag / right click to hide/show)");
        sendLine(sender, "");

        sendLine(sender, "§eTimeline syntax (per tick)");
        sendLine(sender, "§7Write tokens separated by §f+§7.");
        sendLine(sender, "§7Example: §fw+a+jump");
        sendLine(sender, "");

        sendLine(sender, "§eTokens");
        sendLine(sender, "§fKEY §7= required (must be down on this tick)");
        sendLine(sender, "§flenient-KEY §7= optional each tick, but must happen at least once");
        sendLine(sender, "§7across consecutive lenient ticks (window).");
        sendLine(sender, "§fignore-KEY §7= ignored (never causes fail, never shown in Expected/Got).");
        sendLine(sender, "§fwait §7= mandatory idle tick (no input must happen).");
        sendLine(sender, "");

        sendLine(sender, "§eAuto press/hold logic");
        sendLine(sender, "§7You do §fnot§7 write press/hold manually.");
        sendLine(sender, "§7If a key appears on two consecutive ticks:");
        sendLine(sender, "§7- first tick is treated like a §fpress§7");
        sendLine(sender, "§7- next ticks are treated like §fhold§7");
        sendLine(sender, "");

        sendLine(sender, "§eSupported keys");
        sendLine(sender, "§fw, a, s, d, jump, sneak, sprint");
        sendLine(sender, "");

        sendLine(sender, "§eFullSprint (sprint key)");
        boolean fs = InputCheckerConfig.get().fullSprint;
        sendLine(sender, "§7Current: §f" + (fs ? "ON" : "OFF"));
        sendLine(sender, "§7- ON: sprint is ignored unless you explicitly write §fsprint§7 in a tick.");
        sendLine(sender, "§7- OFF: sprint is checked like other keys (unexpected sprint can fail).");
        sendLine(sender, "");

        sendLine(sender, "§eOutput (when FAIL)");
        sendLine(sender, "§7The HUD shows a clean summary:");
        sendLine(sender, "§fExpected: ... tick X");
        sendLine(sender, "§cGot: ...");
        sendLine(sender, "§7Ignored / lenient keys are not displayed.");
        sendLine(sender, "");

        sendLine(sender, "§eExamples");
        sendLine(sender, "§7Simple run:");
        sendLine(sender, "§fTick1: w");
        sendLine(sender, "§fTick2: w");
        sendLine(sender, "§fTick3: w+jump");
        sendLine(sender, "");
        sendLine(sender, "§7Ignore a key:");
        sendLine(sender, "§fTick1: w+ignore-jump");
        sendLine(sender, "");
        sendLine(sender, "§7Lenient window (must happen at least once across ticks 2-4):");
        sendLine(sender, "§fTick2: lenient-jump");
        sendLine(sender, "§fTick3: lenient-jump");
        sendLine(sender, "§fTick4: lenient-jump");
        sendLine(sender, "");
        sendLine(sender, "§7Wait tick (must be idle):");
        sendLine(sender, "§fTick5: wait");
        sendLine(sender, "");
    }

    private void sendLine(ICommandSender sender, String text) {
        sender.addChatMessage(new ChatComponentText(text));
    }

    @Override
    public int getRequiredPermissionLevel() {
        return 0; // everyone
    }
}

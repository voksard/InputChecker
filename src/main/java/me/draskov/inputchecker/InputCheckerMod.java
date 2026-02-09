package me.draskov.inputchecker;

import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.common.event.FMLInitializationEvent;

@Mod(modid = InputCheckerMod.MODID, name = InputCheckerMod.NAME, version = InputCheckerMod.VERSION, clientSideOnly = true)
public class InputCheckerMod {
    public static final String MODID = "inputchecker";
    public static final String NAME = "InputChecker";
    public static final String VERSION = "1.0";

    @Mod.EventHandler
    public void init(FMLInitializationEvent event) {
        InputCheckerConfig.load();
        ElementStore.load();
        HudConfig.load();

        // ✅ IMPORTANT: stats must be EMPTY when you join / relog
        // So we DO NOT load persisted stats from disk.
        // If you want persistence later, re-add StatsTracker.load().
        StatsTracker.resetSession();

        MinecraftForge.EVENT_BUS.register(new ClientEvents());
        MinecraftForge.EVENT_BUS.register(new HudOverlay());

        // Client command: /inputchecker help
        net.minecraftforge.client.ClientCommandHandler.instance.registerCommand(new InputCheckerCommand());
    }
}

package me.draskov.inputchecker;

import net.minecraftforge.event.world.WorldEvent;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;
import net.minecraftforge.fml.common.network.FMLNetworkEvent;

public class StatsEvents {

    /** Quitte un monde solo (ou world unload) => reset */
    @SubscribeEvent
    public void onWorldUnload(WorldEvent.Unload e) {
        // côté client uniquement en pratique, mais safe
        StatsTracker.resetSession();
    }

    /** Déco d’un serveur => reset */
    @SubscribeEvent
    public void onClientDisconnect(FMLNetworkEvent.ClientDisconnectionFromServerEvent e) {
        StatsTracker.resetSession();
    }

    /** Connexion serveur => reset (optionnel mais propre) */
    @SubscribeEvent
    public void onClientConnect(FMLNetworkEvent.ClientConnectedToServerEvent e) {
        StatsTracker.resetSession();
    }
}

package me.draskov.inputchecker;

import net.minecraftforge.event.world.WorldEvent;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;
import net.minecraftforge.fml.common.network.FMLNetworkEvent;

public class StatsEvents {

    @SubscribeEvent
    public void onWorldUnload(WorldEvent.Unload e) {
        StatsTracker.resetSession();
    }

    @SubscribeEvent
    public void onClientDisconnect(FMLNetworkEvent.ClientDisconnectionFromServerEvent e) {
        StatsTracker.resetSession();
    }

    @SubscribeEvent
    public void onClientConnect(FMLNetworkEvent.ClientConnectedToServerEvent e) {
        StatsTracker.resetSession();
    }
}

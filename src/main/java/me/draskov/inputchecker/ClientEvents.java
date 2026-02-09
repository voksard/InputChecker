package me.draskov.inputchecker;

import net.minecraft.client.Minecraft;
import net.minecraft.client.settings.KeyBinding;
import net.minecraftforge.client.event.MouseEvent;
import net.minecraftforge.fml.client.registry.ClientRegistry;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;
import net.minecraftforge.fml.common.gameevent.InputEvent;
import net.minecraftforge.fml.common.gameevent.TickEvent;
import org.lwjgl.input.Keyboard;
import org.lwjgl.input.Mouse;
import net.minecraftforge.event.world.WorldEvent;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;
import net.minecraftforge.fml.common.network.FMLNetworkEvent;


public class ClientEvents {

    private final KeyBinding openGuiKey;
    private final KeyBinding editHudKey;

    private final InputRuntimeChecker checker = new InputRuntimeChecker();

    public ClientEvents() {
        openGuiKey = new KeyBinding("Open InputChecker GUI", Keyboard.KEY_G, "InputChecker");
        editHudKey = new KeyBinding("Edit InputChecker HUD", Keyboard.KEY_F9, "InputChecker");

        ClientRegistry.registerKeyBinding(openGuiKey);
        ClientRegistry.registerKeyBinding(editHudKey);
    }

    @SubscribeEvent
    public void onKey(InputEvent.KeyInputEvent e) {
        Minecraft mc = Minecraft.getMinecraft();

        if (openGuiKey.isPressed()) {
            mc.displayGuiScreen(new GuiCatalog());
        }

        if (editHudKey.isPressed()) {
            mc.displayGuiScreen(new GuiEditHud());
        }
    }

    // Right click anywhere in-game = restart checking
    @SubscribeEvent
    public void onMouse(MouseEvent e) {
        Minecraft mc = Minecraft.getMinecraft();
        if (mc.thePlayer == null || mc.theWorld == null) return;
        if (mc.currentScreen != null) return;

        if (Mouse.getEventButton() == 1 && Mouse.getEventButtonState()) {
            checker.restart(mc);
        }
    }

    @SubscribeEvent
    public void onClientTick(TickEvent.ClientTickEvent e) {
        if (e.phase != TickEvent.Phase.END) return;

        Minecraft mc = Minecraft.getMinecraft();
        if (mc.thePlayer == null || mc.theWorld == null) return;

        checker.tick(mc);
    }

    @SubscribeEvent
    public void onWorldUnload(WorldEvent.Unload e) {
        // ✅ solo: quit world => reset stats
        StatsTracker.resetSession();
    }

    @SubscribeEvent
    public void onDisconnect(FMLNetworkEvent.ClientDisconnectionFromServerEvent e) {
        // ✅ multi: disconnect => reset stats
        StatsTracker.resetSession();
    }

}

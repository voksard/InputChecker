package me.draskov.inputchecker;

import net.minecraft.client.Minecraft;
import net.minecraft.client.settings.KeyBinding;
import net.minecraftforge.client.event.MouseEvent;
import net.minecraftforge.event.world.WorldEvent;
import net.minecraftforge.fml.client.registry.ClientRegistry;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;
import net.minecraftforge.fml.common.gameevent.InputEvent;
import net.minecraftforge.fml.common.gameevent.TickEvent;
import net.minecraftforge.fml.common.network.FMLNetworkEvent;
import org.lwjgl.input.Keyboard;
import org.lwjgl.input.Mouse;


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
        if (openGuiKey.isPressed()) {
            Minecraft.getMinecraft().displayGuiScreen(new GuiCatalog());
        }

        if (editHudKey.isPressed()) {
            Minecraft.getMinecraft().displayGuiScreen(new GuiEditHud());
        }
    }

    @SubscribeEvent
    public void onMouse(MouseEvent e) {
        Minecraft mc = Minecraft.getMinecraft();
        if (!isInGame(mc) || mc.currentScreen != null) {
            return;
        }

        if (Mouse.getEventButton() == 1 && Mouse.getEventButtonState()) {
            checker.restart(mc);
        }
    }

    @SubscribeEvent
    public void onClientTick(TickEvent.ClientTickEvent e) {
        if (e.phase != TickEvent.Phase.END) {
            return;
        }

        Minecraft mc = Minecraft.getMinecraft();
        if (isInGame(mc)) {
            checker.tick(mc);
        }
    }

    @SubscribeEvent
    public void onWorldUnload(WorldEvent.Unload e) {
        checker.reset();
        ElementStore.clearActive();
        StatsTracker.resetSession();
    }

    @SubscribeEvent
    public void onDisconnect(FMLNetworkEvent.ClientDisconnectionFromServerEvent e) {
        checker.reset();
        ElementStore.clearActive();
        StatsTracker.resetSession();
    }

    private boolean isInGame(Minecraft mc) {
        return mc.thePlayer != null && mc.theWorld != null;
    }

}

package me.draskov.inputchecker;

import net.minecraft.client.Minecraft;
import net.minecraftforge.client.event.RenderGameOverlayEvent;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;

import java.util.ArrayList;
import java.util.List;

public class HudOverlay {

    // bounds HUD principal
    public static int left = 0, top = 0, right = 0, bottom = 0;

    // bounds HUD stats
    public static int sLeft = 0, sTop = 0, sRight = 0, sBottom = 0;

    @SubscribeEvent
    public void onRender(RenderGameOverlayEvent.Text e) {
        Minecraft mc = Minecraft.getMinecraft();
        if (mc.thePlayer == null || mc.theWorld == null) return;
        if (mc.currentScreen != null) return;

        HudConfig cfg = HudConfig.get();

        // --- MAIN HUD ---
        if (cfg.visible) {
            drawPanel(mc, cfg.x, cfg.y, HudLog.getStatus(), HudLog.getLines(), true);
        } else {
            left = top = right = bottom = 0;
        }

        // --- STATS HUD ---
        if (cfg.statsVisible) {
            CheckElement active = ElementStore.getActive();

            String name = (active == null || active.name == null) ? "InputChecker" : active.name;
            String title = "§b" + name + " statistics";

            List<String> lines;

            if (active == null) {
                // No active element => still show the panel with a message
                lines = new ArrayList<String>();
                lines.add("§7No active element");
                // also ensure stats session is cleared so you don't keep stale data
                StatsTracker.resetSession();
            } else {
                // Ensure context is correct (reset if element changed)
                StatsTracker.ensureContext(active.id);

                lines = StatsTracker.buildHudLines();
                if (lines == null) lines = new ArrayList<String>();

                // If no data yet, show a friendly line
                if (lines.isEmpty()) {
                    lines.add("§7No data yet");
                }
            }

            drawPanel(mc, cfg.statsX, cfg.statsY, title, lines, false);
        } else {
            sLeft = sTop = sRight = sBottom = 0;
        }
    }

    private void drawPanel(Minecraft mc, int x, int y, String status, List<String> lines, boolean main) {
        int w = mc.fontRendererObj.getStringWidth(status);
        for (int i = 0; i < lines.size(); i++) {
            w = Math.max(w, mc.fontRendererObj.getStringWidth(lines.get(i)));
        }
        int h = (1 + lines.size()) * 10 + 6;

        int L = x - 3;
        int T = y - 3;
        int R = x + w + 6;
        int B = y + h;

        if (main) {
            left = L; top = T; right = R; bottom = B;
        } else {
            sLeft = L; sTop = T; sRight = R; sBottom = B;
        }

        net.minecraft.client.gui.Gui.drawRect(L, T, R, B, 0x90000000);

        mc.fontRendererObj.drawString(status, x, y, 0xFFFFFF);

        int yy = y + 12;
        for (int i = 0; i < lines.size(); i++) {
            mc.fontRendererObj.drawString(lines.get(i), x, yy, 0xFFFFFF);
            yy += 10;
        }
    }

    public static boolean isMouseInsideMain(int mx, int my) {
        return mx >= left && mx <= right && my >= top && my <= bottom;
    }

    public static boolean isMouseInsideStats(int mx, int my) {
        return mx >= sLeft && mx <= sRight && my >= sTop && my <= sBottom;
    }
}

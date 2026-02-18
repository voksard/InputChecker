package me.draskov.inputchecker;

import net.minecraft.client.Minecraft;
import net.minecraftforge.client.event.RenderGameOverlayEvent;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;

import java.util.ArrayList;
import java.util.List;

public class HudOverlay {
    public static int left = 0, top = 0, right = 0, bottom = 0;
    public static int sLeft = 0, sTop = 0, sRight = 0, sBottom = 0;

    @SubscribeEvent
    public void onRender(RenderGameOverlayEvent.Text e) {
        Minecraft mc = Minecraft.getMinecraft();
        if (mc.thePlayer == null || mc.theWorld == null || mc.currentScreen != null) {
            return;
        }

        HudConfig cfg = HudConfig.get();

        if (cfg.visible) {
            List<String> hudLines = new ArrayList<>(HudLog.getLines());
            if (ElementStore.getActive() == null && hudLines.isEmpty()) {
                hudLines.add(ColorConfig.getContentColorCode() + "No active element");
            }
            drawPanel(mc, cfg.x, cfg.y, HudLog.getStatus(), hudLines, true);
        } else {
            left = top = right = bottom = 0;
        }

        if (cfg.statsVisible) {
            CheckElement active = ElementStore.getActive();
            String title = active == null ? "§bInputchecker statistics:" : "§bStatistics " + active.name + ":";
            List<String> lines;

            if (active == null) {
                lines = new ArrayList<>();
                lines.add(ColorConfig.getContentColorCode() + "No active element");
                StatsTracker.resetSession();
            } else {
                lines = new ArrayList<>();
                StatsTracker.ensureContext(active.id);
                List<String> statsLines = StatsTracker.buildHudLines();
                if (statsLines != null && !statsLines.isEmpty()) {
                    lines.addAll(statsLines);
                } else {
                    lines.add(ColorConfig.getContentColorCode() + "No data yet");
                }
            }

            drawPanel(mc, cfg.statsX, cfg.statsY, title, lines, false);
        } else {
            sLeft = sTop = sRight = sBottom = 0;
        }
    }

    private void drawPanel(Minecraft mc, int x, int y, String status, List<String> lines, boolean main) {
        ColorConfig colors = ColorConfig.get();

        String cleanStatus = status.replaceAll("§.", "");

        int w = TextRenderer.getStringWidth(cleanStatus);
        for (String line : lines) {
            w = Math.max(w, TextRenderer.getStringWidth(line));
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

        net.minecraft.client.gui.Gui.drawRect(L, T, R, B, 0x00000000);

        // Determine title color: red for Fail, green for Ok, otherwise use titleColor
        int titleColor = colors.titleColor;
        if (cleanStatus.startsWith("Fail ")) {
            titleColor = 0xFFFF5555; // Red
        } else if (cleanStatus.startsWith("Ok ")) {
            titleColor = 0xFF55FF55; // Green
        }

        TextRenderer.drawString(cleanStatus, x, y, titleColor, true);

        int yy = y + 12;
        for (String line : lines) {
            TextRenderer.drawString(line, x, yy, colors.contentColor, true);
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

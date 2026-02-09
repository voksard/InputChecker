package me.draskov.inputchecker;

import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiScreen;

import java.io.IOException;
import java.util.List;

public class GuiEditHud extends GuiScreen {

    private boolean draggingMain = false;
    private boolean draggingStats = false;

    private int dragOffsetX;
    private int dragOffsetY;

    @Override
    public void initGui() {
        HudLog.clear();
        HudLog.setStatus("§bInputChecker"); // cyan title
        HudLog.push("§7Left click + drag = move panel");
        HudLog.push("§7Right click on panel = hide/show");
        HudLog.push("§7ESC = exit");
    }

    @Override
    public void drawScreen(int mouseX, int mouseY, float partialTicks) {
        this.drawDefaultBackground();

        // Reuse normal overlay drawing so bounds match exactly
        // (We just call the overlay logic by manually drawing panels here)
        drawPanels(mouseX, mouseY);

        this.drawCenteredString(this.fontRendererObj,
                "InputChecker HUD Editor (F9) - drag panels / right click to toggle",
                this.width / 2, 10, 0xFFFFFF);
    }

    private void drawPanels(int mouseX, int mouseY) {
        Minecraft mc = Minecraft.getMinecraft();
        HudConfig cfg = HudConfig.get();

        // MAIN
        if (cfg.visible) {
            drawPanel(mc, cfg.x, cfg.y, HudLog.getStatus(), HudLog.getLines(),
                    HudOverlay.isMouseInsideMain(mouseX, mouseY));
        } else {
            String t1 = "Main HUD hidden";
            String t2 = "Right click the empty area where it was to show it";
            this.drawCenteredString(this.fontRendererObj, t1, this.width / 2, this.height / 2 - 12, 0xAAAAAA);
            this.drawCenteredString(this.fontRendererObj, t2, this.width / 2, this.height / 2, 0xAAAAAA);
        }

        // STATS
        if (cfg.statsVisible) {
            CheckElement active = ElementStore.getActive();
            String name = (active == null || active.name == null) ? "InputChecker" : active.name;
            String title = "§b" + name + " statistics";

            drawPanel(mc, cfg.statsX, cfg.statsY, title, StatsTracker.buildHudLines(),
                    HudOverlay.isMouseInsideStats(mouseX, mouseY));
        }
    }

    private void drawPanel(Minecraft mc, int x, int y, String status, List<String> lines, boolean hover) {
        int w = mc.fontRendererObj.getStringWidth(status);
        for (String s : lines) w = Math.max(w, mc.fontRendererObj.getStringWidth(s));
        int h = (1 + lines.size()) * 10 + 6;

        int left = x - 3;
        int top = y - 3;
        int right = x + w + 6;
        int bottom = y + h;

        int bg = hover ? 0xA0000000 : 0x80000000;
        drawRect(left, top, right, bottom, bg);

        mc.fontRendererObj.drawString(status, x, y, 0xFFFFFF);

        int yy = y + 12;
        for (String s : lines) {
            mc.fontRendererObj.drawString(s, x, yy, 0xFFFFFF);
            yy += 10;
        }
    }

    @Override
    protected void mouseClicked(int mouseX, int mouseY, int button) throws IOException {
        HudConfig cfg = HudConfig.get();

        boolean overMain = HudOverlay.isMouseInsideMain(mouseX, mouseY);
        boolean overStats = HudOverlay.isMouseInsideStats(mouseX, mouseY);

        // Right click toggles only the panel under mouse
        if (button == 1) {
            if (overMain) {
                cfg.visible = !cfg.visible;
                HudConfig.save();
                return;
            }
            if (overStats) {
                cfg.statsVisible = !cfg.statsVisible;
                HudConfig.save();
                return;
            }
            return;
        }

        // Left click starts drag for panel under mouse
        if (button == 0) {
            if (overMain && cfg.visible) {
                draggingMain = true;
                draggingStats = false;
                dragOffsetX = mouseX - cfg.x;
                dragOffsetY = mouseY - cfg.y;
                return;
            }
            if (overStats && cfg.statsVisible) {
                draggingStats = true;
                draggingMain = false;
                dragOffsetX = mouseX - cfg.statsX;
                dragOffsetY = mouseY - cfg.statsY;
                return;
            }
        }
    }

    @Override
    protected void mouseReleased(int mouseX, int mouseY, int state) {
        if (draggingMain || draggingStats) {
            draggingMain = false;
            draggingStats = false;
            HudConfig.save();
        }
    }

    @Override
    protected void mouseClickMove(int mouseX, int mouseY, int button, long time) {
        if (draggingMain) {
            HudConfig cfg = HudConfig.get();
            cfg.x = mouseX - dragOffsetX;
            cfg.y = mouseY - dragOffsetY;
        } else if (draggingStats) {
            HudConfig cfg = HudConfig.get();
            cfg.statsX = mouseX - dragOffsetX;
            cfg.statsY = mouseY - dragOffsetY;
        }
    }

    @Override
    protected void keyTyped(char typedChar, int keyCode) throws IOException {
        if (keyCode == 1) { // ESC
            this.mc.displayGuiScreen(null);
        }
    }

    @Override
    public boolean doesGuiPauseGame() {
        return false;
    }
}

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

    // Bounds des panneaux pour la détection de la souris
    private int mainLeft, mainTop, mainRight, mainBottom;
    private int statsLeft, statsTop, statsRight, statsBottom;

    @Override
    public void initGui() {
        // Ne pas modifier HudLog ici car ça reste affiché après la fermeture
    }

    @Override
    public void drawScreen(int mouseX, int mouseY, float partialTicks) {
        this.drawDefaultBackground();
        drawPanels(mouseX, mouseY);

        TextRenderer.drawString(
                "Inputchecker HUD Editor (F9) - drag panels / right click to toggle",
                this.width / 2 - TextRenderer.getStringWidth("Inputchecker HUD Editor (F9) - drag panels / right click to toggle") / 2,
                10, 0xFFFFFF, true);
    }

    private void drawPanels(int mouseX, int mouseY) {
        Minecraft mc = Minecraft.getMinecraft();
        HudConfig cfg = HudConfig.get();
        ColorConfig colors = ColorConfig.get();

        if (cfg.visible) {
            boolean hover = isMouseInsidePanel(mouseX, mouseY, mainLeft, mainTop, mainRight, mainBottom);
            drawPanel(mc, cfg.x, cfg.y, HudLog.getStatus(), new java.util.ArrayList<>(), hover, colors.titleColor, true);
        } else {
            drawPlaceholder(mc, cfg.x, cfg.y, "Inputchecker (hidden)");
            // Calculer les bounds même si caché pour le clic droit
            calculatePanelBounds(mc, cfg.x, cfg.y, "Inputchecker (hidden)", java.util.Collections.emptyList(), true);
        }

        if (cfg.statsVisible) {
            CheckElement active = ElementStore.getActive();
            String name = active == null ? "Inputchecker" : active.name;
            String title = "§b" + name + "§7 statistics:";

            boolean hover = isMouseInsidePanel(mouseX, mouseY, statsLeft, statsTop, statsRight, statsBottom);
            drawPanel(mc, cfg.statsX, cfg.statsY, title, new java.util.ArrayList<>(), hover, colors.titleColor, false);
        } else {
            drawPlaceholder(mc, cfg.statsX, cfg.statsY, "Statistics (hidden)");
            // Calculer les bounds même si caché pour le clic droit
            calculatePanelBounds(mc, cfg.statsX, cfg.statsY, "Statistics (hidden)", java.util.Collections.emptyList(), false);
        }
    }

    private boolean isMouseInsidePanel(int mx, int my, int left, int top, int right, int bottom) {
        return mx >= left && mx <= right && my >= top && my <= bottom;
    }

    private void calculatePanelBounds(Minecraft mc, int x, int y, String status, List<String> lines, boolean isMain) {
        status = status.replaceAll("§.", "");

        int w = TextRenderer.getStringWidth(status);
        for (String s : lines) {
            w = Math.max(w, TextRenderer.getStringWidth(s));
        }
        int h = (1 + lines.size()) * 10 + 6;

        int left = x - 3;
        int top = y - 3;
        int right = x + w + 6;
        int bottom = y + h;

        if (isMain) {
            mainLeft = left;
            mainTop = top;
            mainRight = right;
            mainBottom = bottom;
        } else {
            statsLeft = left;
            statsTop = top;
            statsRight = right;
            statsBottom = bottom;
        }
    }

    private void drawPlaceholder(Minecraft mc, int x, int y, String text) {
        int w = TextRenderer.getStringWidth(text) + 6;
        int h = 20;
        net.minecraft.client.gui.Gui.drawRect(x - 3, y - 3, x + w + 3, y + h, 0x80555555);
        TextRenderer.drawString(text, x, y + 5, 0xAAAAAA, true);
    }

    private void drawPanel(Minecraft mc, int x, int y, String status, List<String> lines, boolean hover, int titleColor, boolean isMain) {
        // Calculer et sauvegarder les bounds AVANT de dessiner
        calculatePanelBounds(mc, x, y, status, lines, isMain);

        status = status.replaceAll("§.", "");

        int w = TextRenderer.getStringWidth(status);
        for (String s : lines) w = Math.max(w, TextRenderer.getStringWidth(s));
        int h = (1 + lines.size()) * 10 + 6;

        int left = x - 3;
        int top = y - 3;
        int right = x + w + 6;
        int bottom = y + h;

        int bg = 0x00000000; // Transparent
        drawRect(left, top, right, bottom, bg);

        TextRenderer.drawString(status, x, y, titleColor, true);

        int yy = y + 12;
        for (String s : lines) {
            TextRenderer.drawString(s, x, yy, ColorConfig.get().contentColor, true);
            yy += 10;
        }
    }

    @Override
    protected void mouseClicked(int mouseX, int mouseY, int button) throws IOException {
        HudConfig cfg = HudConfig.get();
        boolean overMain = isMouseInsidePanel(mouseX, mouseY, mainLeft, mainTop, mainRight, mainBottom);
        boolean overStats = isMouseInsidePanel(mouseX, mouseY, statsLeft, statsTop, statsRight, statsBottom);

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

        if (button == 0) {
            if (overMain) {
                draggingMain = true;
                draggingStats = false;
                dragOffsetX = mouseX - cfg.x;
                dragOffsetY = mouseY - cfg.y;
                return;
            }
            if (overStats) {
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
        if (keyCode == 1) {
            this.mc.displayGuiScreen(null);
        }
    }

    @Override
    public boolean doesGuiPauseGame() {
        return false;
    }
}

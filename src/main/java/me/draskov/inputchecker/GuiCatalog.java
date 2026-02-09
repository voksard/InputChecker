package me.draskov.inputchecker;

import net.minecraft.client.gui.GuiButton;
import net.minecraft.client.gui.GuiScreen;
import net.minecraft.client.gui.GuiTextField;
import org.lwjgl.input.Keyboard;

import java.io.IOException;

public class GuiCatalog extends GuiScreen {

    private GuiTextField nameField;

    @Override
    public void initGui() {
        Keyboard.enableRepeatEvents(true);
        int cx = this.width / 2;

        nameField = new GuiTextField(0, this.fontRendererObj, cx - 100, 25, 140, 20);
        nameField.setText(""); // no placeholder

        this.buttonList.clear();
        this.buttonList.add(new GuiButton(1, cx + 45, 25, 80, 20, "Add"));

        // FullSprint toggle (moved DOWN so it won't overlap Name field)
        boolean fullSprint = InputCheckerConfig.get().fullSprint;
        this.buttonList.add(new GuiButton(
                60,
                cx - 140, 50,      // <<< moved from y=25 to y=50
                255, 20,           // wider so the text fits
                "FullSprint: " + (fullSprint ? "ON" : "OFF")
        ));

        // Start list lower because FullSprint now uses space
        int y = 80;              // <<< was 60, moved down
        int idx = 0;
        for (CheckElement el : ElementStore.elements) {
            boolean isActive = (ElementStore.activeId != null && el.id.equals(ElementStore.activeId));

            // Edit
            this.buttonList.add(new GuiButton(
                    1000 + idx,
                    cx - 140, y, 180, 20,
                    el.name + (isActive ? " [ACTIVE]" : "")
            ));

            // Activate / Deactivate
            this.buttonList.add(new GuiButton(
                    2000 + idx,
                    cx + 45, y, 80, 20,
                    isActive ? "Deactivate" : "Activate"
            ));

            // Delete
            this.buttonList.add(new GuiButton(
                    3000 + idx,
                    cx + 130, y, 25, 20,
                    "X"
            ));

            y += 24;
            idx++;
        }

        this.buttonList.add(new GuiButton(2, cx - 40, this.height - 30, 80, 20, "Close"));
    }

    @Override
    protected void actionPerformed(GuiButton b) throws IOException {

        // Toggle FullSprint
        if (b.id == 60) {
            InputCheckerConfig.get().fullSprint = !InputCheckerConfig.get().fullSprint;
            InputCheckerConfig.save();
            this.mc.displayGuiScreen(new GuiCatalog());
            return;
        }

        if (b.id == 1) {
            String n = nameField.getText().trim();
            if (n.isEmpty()) n = "element";
            CheckElement el = new CheckElement(n);
            ElementStore.elements.add(el);
            ElementStore.save();
            this.mc.displayGuiScreen(new GuiCatalog());
            return;
        }

        if (b.id == 2) {
            this.mc.displayGuiScreen(null);
            return;
        }

        // Edit
        if (b.id >= 1000 && b.id < 2000) {
            int i = b.id - 1000;
            if (i >= 0 && i < ElementStore.elements.size()) {
                this.mc.displayGuiScreen(new GuiEditElement(ElementStore.elements.get(i)));
            }
            return;
        }

        // Activate / Deactivate
        if (b.id >= 2000 && b.id < 3000) {
            int i = b.id - 2000;
            if (i >= 0 && i < ElementStore.elements.size()) {
                CheckElement el = ElementStore.elements.get(i);
                boolean isActive = (ElementStore.activeId != null && el.id.equals(ElementStore.activeId));

                if (isActive) {
                    ElementStore.activeId = null;

                    // IMPORTANT: wipe previous OK/FAIL lines
                    HudLog.clear();
                    HudLog.setStatus("§7§bInputChecker§7: no active element");

                    // (optionnel) si tu veux UNE ligne informative, décommente :
                    // HudLog.push("§7No active element");

                } else {
                    ElementStore.activeId = el.id;

                    HudLog.clear();
                    HudLog.setStatus("§7§bInputChecker§7: active = " + el.name);
                    HudLog.push("§7Activated");
                }

                this.mc.displayGuiScreen(new GuiCatalog());
            }
            return;
        }

        // Delete
        if (b.id >= 3000 && b.id < 4000) {
            int i = b.id - 3000;
            if (i >= 0 && i < ElementStore.elements.size()) {
                CheckElement el = ElementStore.elements.get(i);
                ElementStore.elements.remove(i);
                if (ElementStore.activeId != null && el.id.equals(ElementStore.activeId)) {
                    ElementStore.activeId = null;
                }
                ElementStore.save();
                this.mc.displayGuiScreen(new GuiCatalog());
            }
        }
    }

    @Override
    public void onGuiClosed() {
        Keyboard.enableRepeatEvents(false);
    }

    @Override
    protected void keyTyped(char typedChar, int keyCode) throws IOException {
        if (nameField.textboxKeyTyped(typedChar, keyCode)) return;
        super.keyTyped(typedChar, keyCode);
    }

    @Override
    protected void mouseClicked(int mouseX, int mouseY, int mouseButton) throws IOException {
        nameField.mouseClicked(mouseX, mouseY, mouseButton);
        super.mouseClicked(mouseX, mouseY, mouseButton);
    }

    @Override
    public void drawScreen(int mouseX, int mouseY, float partialTicks) {
        this.drawDefaultBackground();
        int cx = this.width / 2;

        this.drawCenteredString(this.fontRendererObj, "InputChecker - Catalog", cx, 8, 0xFFFFFF);
        this.drawString(this.fontRendererObj, "Name:", cx - 140, 31, 0xCCCCCC);
        nameField.drawTextBox();

        // no extra hint line needed anymore (button is already clear)

        super.drawScreen(mouseX, mouseY, partialTicks);
    }
}

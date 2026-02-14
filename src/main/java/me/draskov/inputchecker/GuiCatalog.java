package me.draskov.inputchecker;

import net.minecraft.client.gui.GuiButton;
import net.minecraft.client.gui.GuiScreen;
import net.minecraft.client.gui.GuiTextField;
import org.lwjgl.input.Keyboard;
import org.lwjgl.input.Mouse;

import java.io.IOException;

public class GuiCatalog extends GuiScreen {

    private GuiTextField nameField;
    private GuiButton addButton;
    private static int scrollOffset = 0; // Statique pour persister entre les recréations du GUI
    private static final int VISIBLE_ITEMS = 8;
    private static final int ITEM_HEIGHT = 24;

    @Override
    public void initGui() {
        Keyboard.enableRepeatEvents(true);
        int cx = this.width / 2;

        // S'assurer que scrollOffset reste dans les limites valides
        int maxScroll = Math.max(0, ElementStore.elements.size() - VISIBLE_ITEMS);
        scrollOffset = Math.min(scrollOffset, maxScroll);
        scrollOffset = Math.max(0, scrollOffset);

        nameField = new GuiTextField(0, this.fontRendererObj, cx - 100, 25, 140, 20);
        nameField.setMaxStringLength(20); // Limite à 20 caractères pour éviter le dépassement
        nameField.setText("");

        this.buttonList.clear();
        addButton = new GuiButton(1, cx + 45, 25, 80, 20, "Add");
        this.buttonList.add(addButton);
        updateAddButtonState();

        int baseY = 60;
        int visibleCount = 0;
        int idx = 0;

        for (int i = scrollOffset; i < ElementStore.elements.size() && visibleCount < VISIBLE_ITEMS; i++, idx++) {
            CheckElement el = ElementStore.elements.get(i);
            boolean isActive = ElementStore.activeId != null && el.id.equals(ElementStore.activeId);

            int y = baseY + visibleCount * ITEM_HEIGHT;

            this.buttonList.add(new GuiButton(1000 + i, cx - 140, y, 180, 20, el.name));

            this.buttonList.add(new GuiButton(2000 + i, cx + 45, y, 80, 20,
                    isActive ? "Deactivate" : "Activate"));

            this.buttonList.add(new GuiButton(3000 + i, cx + 130, y, 25, 20, "X"));

            visibleCount++;
        }

        this.buttonList.add(new GuiButton(2, cx - 40, this.height - 30, 80, 20, "Close"));
    }

    @Override
    protected void actionPerformed(GuiButton b) throws IOException {

        if (b.id == 1) {
            String n = nameField.getText().trim();

            // Vérifier si le nom est valide
            if (!isNameValid(n)) {
                return; // Ne rien faire si le nom n'est pas valide
            }

            CheckElement el = new CheckElement(n);
            ElementStore.elements.add(el);
            ElementStore.save();

            // Positionner le scroll sur le nouvel élément (dernier de la liste)
            scrollOffset = Math.max(0, ElementStore.elements.size() - VISIBLE_ITEMS);

            this.mc.displayGuiScreen(new GuiCatalog());
            return;
        }

        if (b.id == 2) {
            scrollOffset = 0; // Réinitialiser le scroll quand on ferme le catalogue
            this.mc.displayGuiScreen(null);
            return;
        }

        if (b.id >= 1000 && b.id < 2000) {
            int i = b.id - 1000;
            if (i >= 0 && i < ElementStore.elements.size()) {
                this.mc.displayGuiScreen(new GuiEditElement(ElementStore.elements.get(i)));
            }
            return;
        }

        if (b.id >= 2000 && b.id < 3000) {
            int i = b.id - 2000;
            if (i >= 0 && i < ElementStore.elements.size()) {
                CheckElement el = ElementStore.elements.get(i);
                boolean isActive = ElementStore.activeId != null && el.id.equals(ElementStore.activeId);

                if (isActive) {
                    ElementStore.activeId = null;
                    HudLog.clear();
                } else {
                    ElementStore.activeId = el.id;
                    HudLog.clear();
                    HudLog.setStatus(ColorConfig.getTitleColorCode() + "Checking " + el.name + ":");
                    HudLog.push(ColorConfig.getContentColorCode() + "Right click to start");
                }

                this.mc.displayGuiScreen(new GuiCatalog());
            }
            return;
        }

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
    public void handleMouseInput() throws IOException {
        super.handleMouseInput();
        int scroll = Mouse.getEventDWheel();
        if (scroll != 0) {
            int maxScroll = Math.max(0, ElementStore.elements.size() - VISIBLE_ITEMS);
            if (scroll > 0) {
                scrollOffset = Math.max(0, scrollOffset - 1);
            } else {
                scrollOffset = Math.min(maxScroll, scrollOffset + 1);
            }
            this.initGui();
        }
    }

    @Override
    protected void keyTyped(char typedChar, int keyCode) throws IOException {
        if (nameField.textboxKeyTyped(typedChar, keyCode)) {
            updateAddButtonState(); // Mettre à jour l'état du bouton après chaque saisie
            return;
        }
        // Réinitialiser le scroll quand on appuie sur Échap (keyCode 1)
        if (keyCode == 1) {
            scrollOffset = 0;
        }
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

        // Afficher les numéros de ligne à gauche de chaque élément
        int baseY = 60;
        int visibleCount = 0;
        for (int i = scrollOffset; i < ElementStore.elements.size() && visibleCount < VISIBLE_ITEMS; i++) {
            int y = baseY + visibleCount * ITEM_HEIGHT;
            int lineNumber = i + 1; // Numéro de ligne (1-based)
            this.drawString(this.fontRendererObj, String.valueOf(lineNumber), cx - 165, y + 6, 0xAAAAAA);
            visibleCount++;
        }

        super.drawScreen(mouseX, mouseY, partialTicks);
    }

    /**
     * Vérifie si le nom saisi est valide
     * @param name Le nom à vérifier
     * @return true si le nom est valide (non vide et n'existe pas déjà), false sinon
     */
    private boolean isNameValid(String name) {
        // Vérifier si le nom est vide
        if (name == null || name.trim().isEmpty()) {
            return false;
        }

        // Vérifier si un élément avec ce nom existe déjà
        for (CheckElement el : ElementStore.elements) {
            if (el.name.equalsIgnoreCase(name.trim())) {
                return false;
            }
        }

        return true;
    }

    /**
     * Met à jour l'état (enabled/disabled) du bouton Add
     */
    private void updateAddButtonState() {
        if (addButton != null) {
            String name = nameField.getText().trim();
            addButton.enabled = isNameValid(name);
        }
    }
}


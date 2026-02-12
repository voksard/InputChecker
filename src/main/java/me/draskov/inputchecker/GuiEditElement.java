package me.draskov.inputchecker;

import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiButton;
import net.minecraft.client.gui.GuiScreen;
import net.minecraft.client.gui.GuiTextField;
import org.lwjgl.input.Keyboard;
import org.lwjgl.input.Mouse;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

public class GuiEditElement extends GuiScreen {
    private final CheckElement element;
    private GuiTextField nameField;
    private final List<GuiTextField> tickFields = new ArrayList<>();
    private final List<Boolean> checkSprintBoxes = new ArrayList<>();
    private final List<Boolean> checkJumpBoxes = new ArrayList<>();
    private final List<Boolean> checkSneakBoxes = new ArrayList<>();
    private final List<Boolean> disableSprintBoxes = new ArrayList<>();
    private final List<Boolean> disableJumpBoxes = new ArrayList<>();
    private final List<Boolean> disableSneakBoxes = new ArrayList<>();
    private final List<Boolean> disableNoSprintBoxes = new ArrayList<>();
    private final List<Boolean> disableNoJumpBoxes = new ArrayList<>();
    private final List<Boolean> disableNoSneakBoxes = new ArrayList<>();
    private final List<Boolean> noSprintBoxes = new ArrayList<>();
    private final List<Boolean> noJumpBoxes = new ArrayList<>();
    private final List<Boolean> noSneakBoxes = new ArrayList<>();

    private static final int MAX_TICKS = 100;
    private static final int VISIBLE_ROWS = 14;
    private static final int ROW_HEIGHT = 20;

    private static final int ID_SAVE = 1;
    private static final int ID_BACK = 2;
    private static final int ID_DELETE_BASE = 3000;
    private static final int ID_DUPLICATE_BASE = 4000;
    private static final int ID_INSERT_ABOVE_BASE = 2000;
    private static final int ID_INSERT_BELOW_BASE = 2500;
    private static final int ID_CHECK_SPRINT_BASE = 5000;
    private static final int ID_CHECK_JUMP_BASE = 6000;
    private static final int ID_CHECK_SNEAK_BASE = 7000;
    private static final int ID_NO_SPRINT_BASE = 8000;
    private static final int ID_NO_JUMP_BASE = 8500;
    private static final int ID_NO_SNEAK_BASE = 9000;

    private int scrollOffset = 0;

    public GuiEditElement(CheckElement element) {
        this.element = element;
        if (element.tickInputs == null || element.tickInputs.isEmpty()) {
            element.tickInputs = new ArrayList<>();
            element.checkSprint = new ArrayList<>();
            element.checkJump = new ArrayList<>();
            element.checkSneak = new ArrayList<>();
            element.noSprint = new ArrayList<>();
            element.noJump = new ArrayList<>();
            element.noSneak = new ArrayList<>();
            for (int i = 0; i < MAX_TICKS; i++) {
                element.tickInputs.add("");
                element.checkSprint.add(false);
                element.checkJump.add(false);
                element.checkSneak.add(false);
                element.noSprint.add(false);
                element.noJump.add(false);
                element.noSneak.add(false);
            }
        }
        while (element.tickInputs.size() < MAX_TICKS) {
            element.tickInputs.add("");
            element.checkSprint.add(false);
            element.checkJump.add(false);
            element.checkSneak.add(false);
            element.noSprint.add(false);
            element.noJump.add(false);
            element.noSneak.add(false);
        }
    }

    @Override
    public void initGui() {
        Keyboard.enableRepeatEvents(true);
        this.buttonList.clear();
        this.tickFields.clear();
        this.checkSprintBoxes.clear();
        this.checkJumpBoxes.clear();
        this.checkSneakBoxes.clear();
        this.disableSprintBoxes.clear();
        this.disableJumpBoxes.clear();
        this.disableSneakBoxes.clear();
        this.disableNoSprintBoxes.clear();
        this.disableNoJumpBoxes.clear();
        this.disableNoSneakBoxes.clear();
        this.noSprintBoxes.clear();
        this.noJumpBoxes.clear();
        this.noSneakBoxes.clear();

        int cx = this.width / 2;

        nameField = new GuiTextField(500, this.fontRendererObj, cx - 110, 20, 220, 20);
        nameField.setText(element.name == null ? "" : element.name);

        int baseY = 60;
        int labelX = cx - 200;

        for (int row = 0; row < VISIBLE_ROWS; row++) {
            int tickIdx = scrollOffset + row;
            if (tickIdx >= MAX_TICKS) break;

            int y = baseY + row * ROW_HEIGHT;
            GuiTextField tf = new GuiTextField(1000 + row, this.fontRendererObj, cx - 50, y, 110, 16);
            String v = element.tickInputs.get(tickIdx);
            tf.setText(v == null ? "" : v);
            tickFields.add(tf);

            Boolean checkSpr = element.checkSprint.get(tickIdx);
            Boolean checkJmp = element.checkJump.get(tickIdx);
            Boolean checkSnk = element.checkSneak.get(tickIdx);
            Boolean noSpr = element.noSprint.get(tickIdx);
            Boolean noJmp = element.noJump.get(tickIdx);
            Boolean noSnk = element.noSneak.get(tickIdx);
            checkSprintBoxes.add(checkSpr != null && checkSpr);
            checkJumpBoxes.add(checkJmp != null && checkJmp);
            checkSneakBoxes.add(checkSnk != null && checkSnk);
            noSprintBoxes.add(noSpr != null && noSpr);
            noJumpBoxes.add(noJmp != null && noJmp);
            noSneakBoxes.add(noSnk != null && noSnk);

            // ...existing code...
            String input = v != null ? v.toLowerCase() : "";
            disableSprintBoxes.add(input.contains("lnt-spr"));
            disableJumpBoxes.add(input.contains("lnt-jmp"));
            disableSneakBoxes.add(input.contains("lnt-snk"));
            disableNoSprintBoxes.add(input.contains("lnt-spr"));
            disableNoJumpBoxes.add(input.contains("lnt-jmp"));
            disableNoSneakBoxes.add(input.contains("lnt-snk"));

            this.buttonList.add(new GuiButton(ID_INSERT_ABOVE_BASE + tickIdx, cx - 165, y, 25, 16, "+↑"));
            this.buttonList.add(new GuiButton(ID_INSERT_BELOW_BASE + tickIdx, cx - 135, y, 25, 16, "+↓"));
            this.buttonList.add(new GuiButton(ID_DUPLICATE_BASE + tickIdx, cx - 105, y, 20, 16, "D"));
            this.buttonList.add(new GuiButton(ID_DELETE_BASE + tickIdx, cx - 80, y, 20, 16, "X"));
            this.buttonList.add(new GuiButton(ID_CHECK_SPRINT_BASE + tickIdx, cx + 66, y, 22, 16, checkSprintBoxes.get(row) ? "✓" : " "));
            this.buttonList.add(new GuiButton(ID_CHECK_JUMP_BASE + tickIdx, cx + 96, y, 22, 16, checkJumpBoxes.get(row) ? "✓" : " "));
            this.buttonList.add(new GuiButton(ID_CHECK_SNEAK_BASE + tickIdx, cx + 126, y, 22, 16, checkSneakBoxes.get(row) ? "✓" : " "));
            this.buttonList.add(new GuiButton(ID_NO_SPRINT_BASE + tickIdx, cx + 156, y, 22, 16, noSprintBoxes.get(row) ? "✓" : " "));
            this.buttonList.add(new GuiButton(ID_NO_JUMP_BASE + tickIdx, cx + 186, y, 22, 16, noJumpBoxes.get(row) ? "✓" : " "));
            this.buttonList.add(new GuiButton(ID_NO_SNEAK_BASE + tickIdx, cx + 216, y, 22, 16, noSneakBoxes.get(row) ? "✓" : " "));
        }

        this.buttonList.add(new GuiButton(ID_SAVE, this.width - 200, 20, 80, 20, "Save"));
        this.buttonList.add(new GuiButton(ID_BACK, this.width - 115, 20, 80, 20, "Back"));
    }

    @Override
    protected void actionPerformed(GuiButton b) throws IOException {
        if (b.id == ID_SAVE) {
            flushFieldsToElement();
            element.name = nameField.getText().trim();
            if (element.name.isEmpty()) element.name = "element";
            ElementStore.save();
            this.mc.displayGuiScreen(new GuiCatalog());
            return;
        }

        if (b.id == ID_BACK) {
            this.mc.displayGuiScreen(new GuiCatalog());
            return;
        }

        if (b.id >= ID_INSERT_ABOVE_BASE && b.id < ID_INSERT_BELOW_BASE) {
            int tickIdx = b.id - ID_INSERT_ABOVE_BASE;
            if (tickIdx >= 0 && tickIdx < element.tickInputs.size()) {
                flushFieldsToElement();
                element.tickInputs.add(tickIdx, "");
                element.checkSprint.add(tickIdx, false);
                element.checkJump.add(tickIdx, false);
                element.checkSneak.add(tickIdx, false);
                element.noSprint.add(tickIdx, false);
                element.noJump.add(tickIdx, false);
                element.noSneak.add(tickIdx, false);
                if (element.tickInputs.size() > MAX_TICKS) {
                    element.tickInputs.remove(element.tickInputs.size() - 1);
                    element.checkSprint.remove(element.checkSprint.size() - 1);
                    element.checkJump.remove(element.checkJump.size() - 1);
                    element.checkSneak.remove(element.checkSneak.size() - 1);
                    element.noSprint.remove(element.noSprint.size() - 1);
                    element.noJump.remove(element.noJump.size() - 1);
                    element.noSneak.remove(element.noSneak.size() - 1);
                }
                ElementStore.save();
                this.initGui();
            }
            return;
        }

        if (b.id >= ID_INSERT_BELOW_BASE && b.id < ID_DELETE_BASE) {
            int tickIdx = b.id - ID_INSERT_BELOW_BASE;
            if (tickIdx >= 0 && tickIdx < element.tickInputs.size()) {
                flushFieldsToElement();
                element.tickInputs.add(tickIdx + 1, "");
                element.checkSprint.add(tickIdx + 1, false);
                element.checkJump.add(tickIdx + 1, false);
                element.checkSneak.add(tickIdx + 1, false);
                element.noSprint.add(tickIdx + 1, false);
                element.noJump.add(tickIdx + 1, false);
                element.noSneak.add(tickIdx + 1, false);
                if (element.tickInputs.size() > MAX_TICKS) {
                    element.tickInputs.remove(element.tickInputs.size() - 1);
                    element.checkSprint.remove(element.checkSprint.size() - 1);
                    element.checkJump.remove(element.checkJump.size() - 1);
                    element.checkSneak.remove(element.checkSneak.size() - 1);
                    element.noSprint.remove(element.noSprint.size() - 1);
                    element.noJump.remove(element.noJump.size() - 1);
                    element.noSneak.remove(element.noSneak.size() - 1);
                }
                ElementStore.save();
                this.initGui();
            }
            return;
        }

        if (b.id >= ID_DELETE_BASE && b.id < ID_DUPLICATE_BASE) {
            int tickIdx = b.id - ID_DELETE_BASE;
            if (tickIdx >= 0 && tickIdx < element.tickInputs.size()) {
                flushFieldsToElement();
                element.tickInputs.remove(tickIdx);
                element.checkSprint.remove(tickIdx);
                element.checkJump.remove(tickIdx);
                element.checkSneak.remove(tickIdx);
                element.noSprint.remove(tickIdx);
                element.noJump.remove(tickIdx);
                element.noSneak.remove(tickIdx);

                if (element.tickInputs.size() < MAX_TICKS) {
                    element.tickInputs.add("");
                    element.checkSprint.add(false);
                    element.checkJump.add(false);
                    element.checkSneak.add(false);
                    element.noSprint.add(false);
                    element.noJump.add(false);
                    element.noSneak.add(false);
                }

                ElementStore.save();
                this.initGui();
            }
            return;
        }

        if (b.id >= ID_DUPLICATE_BASE && b.id < ID_CHECK_SPRINT_BASE) {
            int tickIdx = b.id - ID_DUPLICATE_BASE;
            if (tickIdx >= 0 && tickIdx < element.tickInputs.size()) {
                flushFieldsToElement();
                String content = element.tickInputs.get(tickIdx);
                if (tickIdx + 1 < element.tickInputs.size()) {
                    element.tickInputs.set(tickIdx + 1, content);
                    element.checkSprint.set(tickIdx + 1, element.checkSprint.get(tickIdx));
                    element.checkJump.set(tickIdx + 1, element.checkJump.get(tickIdx));
                    element.checkSneak.set(tickIdx + 1, element.checkSneak.get(tickIdx));
                    element.noSprint.set(tickIdx + 1, element.noSprint.get(tickIdx));
                    element.noJump.set(tickIdx + 1, element.noJump.get(tickIdx));
                    element.noSneak.set(tickIdx + 1, element.noSneak.get(tickIdx));
                }
                ElementStore.save();
                this.initGui();
            }
            return;
        }

        if (b.id >= ID_CHECK_SPRINT_BASE && b.id < ID_CHECK_JUMP_BASE) {
            int tickIdx = b.id - ID_CHECK_SPRINT_BASE;
            if (tickIdx >= 0 && tickIdx < element.checkSprint.size()) {
                // Vérifier si la case est désactivée
                String input = element.tickInputs.get(tickIdx).toLowerCase();

                // Empêcher de cocher spr si ns (noSprint) est déjà coché
                if (element.noSprint.get(tickIdx)) {
                    return;
                }

                if (!input.contains("lnt-spr")) {
                    element.checkSprint.set(tickIdx, !element.checkSprint.get(tickIdx));
                    ElementStore.save();
                    this.initGui();
                }
            }
            return;
        }

        if (b.id >= ID_CHECK_JUMP_BASE && b.id < ID_CHECK_SNEAK_BASE) {
            int tickIdx = b.id - ID_CHECK_JUMP_BASE;
            if (tickIdx >= 0 && tickIdx < element.checkJump.size()) {
                // Vérifier si la case est désactivée
                String input = element.tickInputs.get(tickIdx).toLowerCase();

                // Empêcher de cocher jmp si nj (no jump) est déjà coché
                if (element.noJump.get(tickIdx)) {
                    return;
                }

                if (!input.contains("lnt-jmp")) {
                    element.checkJump.set(tickIdx, !element.checkJump.get(tickIdx));
                    ElementStore.save();
                    this.initGui();
                }
            }
            return;
        }

        if (b.id >= ID_CHECK_SNEAK_BASE && b.id < ID_NO_SPRINT_BASE) {
            int tickIdx = b.id - ID_CHECK_SNEAK_BASE;
            if (tickIdx >= 0 && tickIdx < element.checkSneak.size()) {
                // Vérifier si la case est désactivée
                String input = element.tickInputs.get(tickIdx).toLowerCase();

                // Empêcher de cocher snk si nsnk (no sneak) est déjà coché
                if (element.noSneak.get(tickIdx)) {
                    return;
                }

                if (!input.contains("lnt-snk")) {
                    element.checkSneak.set(tickIdx, !element.checkSneak.get(tickIdx));
                    ElementStore.save();
                    this.initGui();
                }
            }
            return;
        }

        if (b.id >= ID_NO_SPRINT_BASE && b.id < ID_NO_JUMP_BASE) {
            int tickIdx = b.id - ID_NO_SPRINT_BASE;
            if (tickIdx >= 0 && tickIdx < element.noSprint.size()) {
                String input = element.tickInputs.get(tickIdx).toLowerCase();

                // Empêcher de cocher ns si spr (checkSprint) est déjà coché
                if (element.checkSprint.get(tickIdx)) {
                    // Ne rien faire, incompatible
                    return;
                }

                if (!input.contains("lnt-spr")) {
                    element.noSprint.set(tickIdx, !element.noSprint.get(tickIdx));
                    ElementStore.save();
                    this.initGui();
                }
            }
            return;
        }

        if (b.id >= ID_NO_JUMP_BASE && b.id < ID_NO_SNEAK_BASE) {
            int tickIdx = b.id - ID_NO_JUMP_BASE;
            if (tickIdx >= 0 && tickIdx < element.noJump.size()) {
                String input = element.tickInputs.get(tickIdx).toLowerCase();

                // Empêcher de cocher nj si jmp (checkJump) est déjà coché
                if (element.checkJump.get(tickIdx)) {
                    return;
                }

                if (!input.contains("lnt-jmp")) {
                    element.noJump.set(tickIdx, !element.noJump.get(tickIdx));
                    ElementStore.save();
                    this.initGui();
                }
            }
            return;
        }

        if (b.id >= ID_NO_SNEAK_BASE && b.id < ID_NO_SNEAK_BASE + 1000) {
            int tickIdx = b.id - ID_NO_SNEAK_BASE;
            if (tickIdx >= 0 && tickIdx < element.noSneak.size()) {
                String input = element.tickInputs.get(tickIdx).toLowerCase();

                // Empêcher de cocher nsnk si snk (checkSneak) est déjà coché
                if (element.checkSneak.get(tickIdx)) {
                    return;
                }

                if (!input.contains("lnt-snk")) {
                    element.noSneak.set(tickIdx, !element.noSneak.get(tickIdx));
                    ElementStore.save();
                    this.initGui();
                }
            }
            return;
        }
    }

    @Override
    public void handleMouseInput() throws IOException {
        super.handleMouseInput();
        int scroll = Mouse.getEventDWheel();
        if (scroll != 0) {
            flushFieldsToElement();
            if (scroll > 0) {
                scrollOffset = Math.max(0, scrollOffset - 3);
            } else {
                int maxScroll = Math.max(0, MAX_TICKS - VISIBLE_ROWS);
                scrollOffset = Math.min(maxScroll, scrollOffset + 3);
            }
            this.initGui();
        }
    }

    @Override
    protected void keyTyped(char typedChar, int keyCode) throws IOException {
        if (nameField != null && nameField.textboxKeyTyped(typedChar, keyCode)) return;

        for (int i = 0; i < tickFields.size(); i++) {
            GuiTextField tf = tickFields.get(i);
            if (tf.isFocused()) {
                handleTickFieldInput(i, typedChar, keyCode);
                return;
            }
        }

        super.keyTyped(typedChar, keyCode);
    }

    private void handleTickFieldInput(int fieldIdx, char typedChar, int keyCode) {
        int tickIdx = scrollOffset + fieldIdx;
        GuiTextField tf = tickFields.get(fieldIdx);
        String current = tf.getText();

        // Gérer Backspace
        if (keyCode == Keyboard.KEY_BACK) {
            if (!current.isEmpty()) {
                int lastPlus = current.lastIndexOf('+');
                if (lastPlus > 0) {
                    tf.setText(current.substring(0, lastPlus));
                } else {
                    tf.setText("");
                }
                element.tickInputs.set(tickIdx, tf.getText());
                sanitizeCheckboxesForTickInput(tickIdx);
                updateCheckboxesInMemory(tickIdx);
                ElementStore.save();
            }
            return;
        }

        // Gérer les touches spéciales pour lnt-snk, lnt-jmp, lnt-spr
        // Shift → lnt-snk
        if (keyCode == Keyboard.KEY_LSHIFT || keyCode == Keyboard.KEY_RSHIFT) {
            if (current.endsWith("lnt-")) {
                tf.setText(current + "snk");
            } else if (current.isEmpty()) {
                tf.setText("lnt-snk");
            } else {
                tf.setText(current + "+lnt-snk");
            }
            element.tickInputs.set(tickIdx, tf.getText());
            sanitizeCheckboxesForTickInput(tickIdx);
            updateCheckboxesInMemory(tickIdx);
            ElementStore.save();
            return;
        }

        // Space → lnt-jmp
        if (keyCode == Keyboard.KEY_SPACE) {
            if (current.endsWith("lnt-")) {
                tf.setText(current + "jmp");
            } else if (current.isEmpty()) {
                tf.setText("lnt-jmp");
            } else {
                tf.setText(current + "+lnt-jmp");
            }
            element.tickInputs.set(tickIdx, tf.getText());
            sanitizeCheckboxesForTickInput(tickIdx);
            updateCheckboxesInMemory(tickIdx);
            ElementStore.save();
            return;
        }

        // Ctrl → lnt-spr
        if (keyCode == Keyboard.KEY_LCONTROL || keyCode == Keyboard.KEY_RCONTROL) {
            if (current.endsWith("lnt-")) {
                tf.setText(current + "spr");
            } else if (current.isEmpty()) {
                tf.setText("lnt-spr");
            } else {
                tf.setText(current + "+lnt-spr");
            }
            element.tickInputs.set(tickIdx, tf.getText());
            sanitizeCheckboxesForTickInput(tickIdx);
            updateCheckboxesInMemory(tickIdx);
            ElementStore.save();
            return;
        }

        // Gérer la touche 'L' pour auto-compléter "lnt-"
        if (keyCode == Keyboard.KEY_L) {
            String toAdd = "lnt-";
            if (current.isEmpty()) {
                tf.setText(toAdd);
            } else {
                tf.setText(current + "+" + toAdd);
            }
            element.tickInputs.set(tickIdx, tf.getText());
            sanitizeCheckboxesForTickInput(tickIdx);
            updateCheckboxesInMemory(tickIdx);
            ElementStore.save();
            return;
        }

        // Gérer W, A, S, D
        String key = getKeyName(keyCode);
        if (key != null) {
            // Vérifier si on est après "lnt-"
            if (current.endsWith("lnt-")) {
                tf.setText(current + key);
            } else if (current.isEmpty()) {
                tf.setText(key);
            } else {
                tf.setText(current + "+" + key);
            }
            element.tickInputs.set(tickIdx, tf.getText());
            sanitizeCheckboxesForTickInput(tickIdx);
            updateCheckboxesInMemory(tickIdx);
            ElementStore.save();
            return;
        }

        // Gérer les touches pour spr/jmp/snk après "lnt-"
        if (current.endsWith("lnt-")) {
            String lenientKey = getLenientKeyName(keyCode);
            if (lenientKey != null) {
                tf.setText(current + lenientKey);
                element.tickInputs.set(tickIdx, tf.getText());
                sanitizeCheckboxesForTickInput(tickIdx);
                updateCheckboxesInMemory(tickIdx);
                ElementStore.save();
                return;
            }
        }

        // Bloquer toutes les autres touches
        // Ne rien faire = pas d'écriture libre
    }

    /**
     * Met à jour les listes de checkboxes en mémoire pour une ligne spécifique
     * sans reconstruire l'interface entière
     */
    private void updateCheckboxesInMemory(int tickIdx) {
        int row = tickIdx - scrollOffset;
        if (row < 0 || row >= VISIBLE_ROWS) return;

        String input = element.tickInputs.get(tickIdx).toLowerCase();

        // Mettre à jour les listes de checkboxes visibles
        checkSprintBoxes.set(row, element.checkSprint.get(tickIdx));
        checkJumpBoxes.set(row, element.checkJump.get(tickIdx));
        checkSneakBoxes.set(row, element.checkSneak.get(tickIdx));
        noSprintBoxes.set(row, element.noSprint.get(tickIdx));
        noJumpBoxes.set(row, element.noJump.get(tickIdx));
        noSneakBoxes.set(row, element.noSneak.get(tickIdx));

        // Mettre à jour les états de désactivation
        disableSprintBoxes.set(row, input.contains("lnt-spr"));
        disableJumpBoxes.set(row, input.contains("lnt-jmp"));
        disableSneakBoxes.set(row, input.contains("lnt-snk"));
        disableNoSprintBoxes.set(row, input.contains("lnt-spr"));
        disableNoJumpBoxes.set(row, input.contains("lnt-jmp"));
        disableNoSneakBoxes.set(row, input.contains("lnt-snk"));
    }

    /**
     * Décoche automatiquement les checkboxes conflictuelles avec lnt-* pour un tick
     */
    private void sanitizeCheckboxesForTickInput(int tickIdx) {
        if (tickIdx < 0 || tickIdx >= element.tickInputs.size()) return;

        String input = element.tickInputs.get(tickIdx).toLowerCase();

        // Si lnt-jmp est présent, décocher checkJump et noJump
        if (input.contains("lnt-jmp")) {
            element.checkJump.set(tickIdx, false);
            element.noJump.set(tickIdx, false);
        }

        // Si lnt-spr est présent, décocher checkSprint et noSprint
        if (input.contains("lnt-spr")) {
            element.checkSprint.set(tickIdx, false);
            element.noSprint.set(tickIdx, false);
        }

        // Si lnt-snk est présent, décocher checkSneak et noSneak
        if (input.contains("lnt-snk")) {
            element.checkSneak.set(tickIdx, false);
            element.noSneak.set(tickIdx, false);
        }
    }

    private String getLenientKeyName(int keyCode) {
        switch (keyCode) {
            case Keyboard.KEY_W: return "w";
            case Keyboard.KEY_A: return "a";
            case Keyboard.KEY_S: return "s";
            case Keyboard.KEY_D: return "d";
            case Keyboard.KEY_J: return "jmp";
            case Keyboard.KEY_K: return "snk";
            case Keyboard.KEY_P: return "spr";
            default: return null;
        }
    }

    private String getKeyName(int keyCode) {
        switch (keyCode) {
            case Keyboard.KEY_W: return "w";
            case Keyboard.KEY_A: return "a";
            case Keyboard.KEY_S: return "s";
            case Keyboard.KEY_D: return "d";
            default: return null;
        }
    }

    @Override
    protected void mouseClicked(int mouseX, int mouseY, int mouseButton) throws IOException {
        if (nameField != null) nameField.mouseClicked(mouseX, mouseY, mouseButton);

        for (GuiTextField tf : tickFields) {
            tf.mouseClicked(mouseX, mouseY, mouseButton);
        }

        super.mouseClicked(mouseX, mouseY, mouseButton);
    }

    @Override
    public void drawScreen(int mouseX, int mouseY, float partialTicks) {
        this.drawDefaultBackground();
        int cx = this.width / 2;

        this.drawCenteredString(this.fontRendererObj, "InputChecker - Edit Element", cx, 6, 0xFFFFFF);

        this.drawString(this.fontRendererObj, "D: duplicate", 10, 20, 0xAAAAAA);
        this.drawString(this.fontRendererObj, "X: delete", 10, 30, 0xAAAAAA);
        this.drawString(this.fontRendererObj, "+↑: add above", 10, 40, 0xAAAAAA);
        this.drawString(this.fontRendererObj, "+↓: add below", 10, 50, 0xAAAAAA);
        this.drawString(this.fontRendererObj, "Basic checks:", 10, 65, 0xAAAAAA);
        this.drawString(this.fontRendererObj, "spr: sprint", 10, 75, 0xAAAAAA);
        this.drawString(this.fontRendererObj, "jmp: jump", 10, 85, 0xAAAAAA);
        this.drawString(this.fontRendererObj, "snk: sneak", 10, 95, 0xAAAAAA);
        this.drawString(this.fontRendererObj, "Special checks:", 10, 110, 0xAAAAAA);
        this.drawString(this.fontRendererObj, "ns: no sprint", 10, 120, 0xAAAAAA);
        this.drawString(this.fontRendererObj, "nj: no jump", 10, 130, 0xAAAAAA);
        this.drawString(this.fontRendererObj, "nk: no sneak", 10, 140, 0xAAAAAA);
        this.drawString(this.fontRendererObj, "Type lnt-input", 10, 155, 0xAAAAAA);
        this.drawString(this.fontRendererObj, "for lenient input", 10, 165, 0xAAAAAA);

        this.drawString(this.fontRendererObj, "Name:", cx - 140, 26, 0xCCCCCC);
        nameField.drawTextBox();

        int baseY = 60;
        int labelX = cx - 200;

        this.drawString(this.fontRendererObj, "spr", cx + 70, 48, 0xFFFFFF);
        this.drawString(this.fontRendererObj, "jmp", cx + 100, 48, 0xFFFFFF);
        this.drawString(this.fontRendererObj, "snk", cx + 130, 48, 0xFFFFFF);
        this.drawString(this.fontRendererObj, "ns", cx + 163, 48, 0xFFFFFF);
        this.drawString(this.fontRendererObj, "nj", cx + 193, 48, 0xFFFFFF);
        this.drawString(this.fontRendererObj, "nk", cx + 223, 48, 0xFFFFFF);

        for (int row = 0; row < tickFields.size(); row++) {
            int tickIdx = scrollOffset + row;
            int y = baseY + row * ROW_HEIGHT;

            this.drawString(this.fontRendererObj, String.valueOf(tickIdx + 1), labelX, y + 4, 0xAAAAAA);
            tickFields.get(row).drawTextBox();

            // Afficher les checkboxes avec couleur grise si désactivées
            int sprColor = disableSprintBoxes.get(row) ? 0x888888 : 0x00FF00;
            int jmpColor = disableJumpBoxes.get(row) ? 0x888888 : 0x00FF00;
            int snkColor = disableSneakBoxes.get(row) ? 0x888888 : 0x00FF00;

            if (checkSprintBoxes.get(row)) {
                this.drawString(this.fontRendererObj, "✓", cx + 76, y + 4, sprColor);
            } else if (disableSprintBoxes.get(row)) {
                this.drawString(this.fontRendererObj, "✗", cx + 76, y + 4, 0x888888);
            }

            if (checkJumpBoxes.get(row)) {
                this.drawString(this.fontRendererObj, "✓", cx + 106, y + 4, jmpColor);
            } else if (disableJumpBoxes.get(row)) {
                this.drawString(this.fontRendererObj, "✗", cx + 106, y + 4, 0x888888);
            }

            if (checkSneakBoxes.get(row)) {
                this.drawString(this.fontRendererObj, "✓", cx + 136, y + 4, snkColor);
            } else if (disableSneakBoxes.get(row)) {
                this.drawString(this.fontRendererObj, "✗", cx + 136, y + 4, 0x888888);
            }

            if (noSprintBoxes.get(row)) {
                this.drawString(this.fontRendererObj, "✓", cx + 162, y + 4, 0xFF6666);
            } else if (disableNoSprintBoxes.get(row)) {
                this.drawString(this.fontRendererObj, "✗", cx + 162, y + 4, 0x888888);
            }

            if (noJumpBoxes.get(row)) {
                this.drawString(this.fontRendererObj, "✓", cx + 192, y + 4, 0xFF6666);
            } else if (disableNoJumpBoxes.get(row)) {
                this.drawString(this.fontRendererObj, "✗", cx + 192, y + 4, 0x888888);
            }

            if (noSneakBoxes.get(row)) {
                this.drawString(this.fontRendererObj, "✓", cx + 222, y + 4, 0xFF6666);
            } else if (disableNoSneakBoxes.get(row)) {
                this.drawString(this.fontRendererObj, "✗", cx + 222, y + 4, 0x888888);
            }
        }

        super.drawScreen(mouseX, mouseY, partialTicks);
    }

    @Override
    public void onGuiClosed() {
        Keyboard.enableRepeatEvents(false);
    }

    private void flushFieldsToElement() {
        for (int row = 0; row < tickFields.size(); row++) {
            int tickIdx = scrollOffset + row;
            element.tickInputs.set(tickIdx, tickFields.get(row).getText());
        }
    }

    @Override
    public boolean doesGuiPauseGame() {
        return false;
    }
}


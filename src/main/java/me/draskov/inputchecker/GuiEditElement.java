package me.draskov.inputchecker;

import net.minecraft.client.gui.GuiButton;
import net.minecraft.client.gui.GuiScreen;
import net.minecraft.client.gui.GuiTextField;
import org.lwjgl.input.Keyboard;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

public class GuiEditElement extends GuiScreen {

    private final CheckElement element;

    private GuiTextField nameField;
    private final List<GuiTextField> tickFields = new ArrayList<GuiTextField>();

    // max ticks = 50
    private static final int MAX_TICKS = 50;

    private static final int PAGE_SIZE = 24;     // 12 left + 12 right
    private static final int COL_SIZE = 12;

    // Button ID ranges
    private static final int ID_ADD_TICK_END = 1;
    private static final int ID_SAVE = 2;
    private static final int ID_BACK = 3;
    private static final int ID_PAGE_PREV = 10;
    private static final int ID_PAGE_NEXT = 11;

    private static final int ID_DUP_BASE = 2000;
    private static final int ID_DEL_BASE = 3000;
    private static final int ID_INS_BASE = 4000; // insert before index

    private int page = 0;

    public GuiEditElement(CheckElement element) {
        this(element, 0);
    }

    public GuiEditElement(CheckElement element, int page) {
        this.element = element;
        this.page = Math.max(0, page);
        if (this.element.tickInputs == null) {
            this.element.tickInputs = new ArrayList<String>();
        }
    }

    @Override
    public void initGui() {
        Keyboard.enableRepeatEvents(true);
        this.buttonList.clear();
        this.tickFields.clear();

        int cx = this.width / 2;

        // Name
        nameField = new GuiTextField(500, this.fontRendererObj, cx - 110, 20, 220, 20);
        nameField.setText(element.name == null ? "" : element.name);

        // --- SPACING TUNING ---
        int baseY = 64;
        int rowH = 22;

        // Two columns layout
        int leftXLabel = cx - 245;
        int leftXField = cx - 225;

        int rightXLabel = cx + 15;
        int rightXField = cx + 35;

        int fieldW = 145;
        int btnW = 18;

        int startIndex = page * PAGE_SIZE;
        int endIndex = Math.min(element.tickInputs.size(), startIndex + PAGE_SIZE);

        // Build only visible tick fields for current page
        int visibleIdx = 0;
        for (int i = startIndex; i < endIndex; i++) {
            int col = visibleIdx / COL_SIZE;   // 0 left, 1 right
            int row = visibleIdx % COL_SIZE;

            int y = baseY + row * rowH;

            int xLabel = (col == 0) ? leftXLabel : rightXLabel;
            int xField = (col == 0) ? leftXField : rightXField;

            // Insert button (+) BEFORE this tick
            this.buttonList.add(new GuiButton(ID_INS_BASE + i, xLabel - 22, y, btnW, 18, "+"));

            GuiTextField tf = new GuiTextField(1000 + i, this.fontRendererObj, xField, y, fieldW, 18);
            String v = element.tickInputs.get(i);
            tf.setText(v == null ? "" : v);
            tickFields.add(tf);

            // duplicate / delete buttons
            this.buttonList.add(new GuiButton(ID_DUP_BASE + i, xField + fieldW + 4, y, btnW, 18, "D"));
            this.buttonList.add(new GuiButton(ID_DEL_BASE + i, xField + fieldW + 4 + btnW + 2, y, btnW, 18, "-"));

            visibleIdx++;
        }

        // Save / Back
        this.buttonList.add(new GuiButton(ID_SAVE, cx + 60, this.height - 30, 80, 20, "Save"));
        this.buttonList.add(new GuiButton(ID_BACK, cx + 145, this.height - 30, 80, 20, "Back"));

        // Page buttons + Tick on SAME LINE (prevents overlap with boxes)
        int totalPages = (element.tickInputs.size() + PAGE_SIZE - 1) / PAGE_SIZE;
        if (totalPages < 1) totalPages = 1;

        int navY = this.height - 30;

        GuiButton prev = new GuiButton(ID_PAGE_PREV, cx - 140, navY, 40, 20, "<");
        GuiButton addTick = new GuiButton(ID_ADD_TICK_END, cx - 95, navY, 80, 20, "+ Tick");
        GuiButton next = new GuiButton(ID_PAGE_NEXT, cx - 10, navY, 40, 20, ">");

        prev.enabled = (page > 0);
        next.enabled = (page < totalPages - 1);

        this.buttonList.add(prev);
        this.buttonList.add(addTick);
        this.buttonList.add(next);
    }

    @Override
    protected void actionPerformed(GuiButton b) throws IOException {

        // Insert tick BEFORE index
        if (b.id >= ID_INS_BASE && b.id < ID_INS_BASE + 1000) {
            int i = b.id - ID_INS_BASE;
            if (i >= 0 && i <= element.tickInputs.size()) {

                flushFieldsToElement();

                if (element.tickInputs.size() >= MAX_TICKS) {
                    HudLog.push("§6Max ticks reached (" + MAX_TICKS + ")");
                    this.mc.displayGuiScreen(new GuiEditElement(element, page));
                    return;
                }

                element.tickInputs.add(i, "");
                ElementStore.save();

                int newPage = i / PAGE_SIZE;
                this.mc.displayGuiScreen(new GuiEditElement(element, newPage));
            }
            return;
        }

        // Add tick at end
        if (b.id == ID_ADD_TICK_END) {
            flushFieldsToElement();

            if (element.tickInputs.size() < MAX_TICKS) {
                element.tickInputs.add("");
                ElementStore.save();

                int newTotalPages = (element.tickInputs.size() + PAGE_SIZE - 1) / PAGE_SIZE;
                int newPage = Math.max(0, newTotalPages - 1);
                this.mc.displayGuiScreen(new GuiEditElement(element, newPage));
            } else {
                HudLog.push("§6Max ticks reached (" + MAX_TICKS + ")");
                this.mc.displayGuiScreen(new GuiEditElement(element, page));
            }
            return;
        }

        // Save
        if (b.id == ID_SAVE) {
            flushFieldsToElement();
            element.name = nameField.getText().trim();
            if (element.name.length() == 0) element.name = "element";
            ElementStore.save();
            this.mc.displayGuiScreen(new GuiCatalog());
            return;
        }

        // Back (no save)
        if (b.id == ID_BACK) {
            this.mc.displayGuiScreen(new GuiCatalog());
            return;
        }

        // Page prev/next
        if (b.id == ID_PAGE_PREV) {
            flushFieldsToElement();
            this.mc.displayGuiScreen(new GuiEditElement(element, Math.max(0, page - 1)));
            return;
        }
        if (b.id == ID_PAGE_NEXT) {
            flushFieldsToElement();
            this.mc.displayGuiScreen(new GuiEditElement(element, page + 1));
            return;
        }

        // Duplicate tick
        if (b.id >= ID_DUP_BASE && b.id < ID_DEL_BASE) {
            int i = b.id - ID_DUP_BASE;
            if (i >= 0 && i < element.tickInputs.size()) {
                flushFieldsToElement();

                if (element.tickInputs.size() >= MAX_TICKS) {
                    HudLog.push("§6Max ticks reached (" + MAX_TICKS + ")");
                    this.mc.displayGuiScreen(new GuiEditElement(element, page));
                    return;
                }

                String original = element.tickInputs.get(i);
                String copy = (original == null) ? "" : original;
                element.tickInputs.add(i + 1, copy);

                ElementStore.save();

                int newPage = (i / PAGE_SIZE);
                this.mc.displayGuiScreen(new GuiEditElement(element, newPage));
            }
            return;
        }

        // Delete tick
        if (b.id >= ID_DEL_BASE && b.id < ID_INS_BASE) {
            int i = b.id - ID_DEL_BASE;
            if (i >= 0 && i < element.tickInputs.size()) {
                flushFieldsToElement();

                element.tickInputs.remove(i);
                ElementStore.save();

                int totalPages = (element.tickInputs.size() + PAGE_SIZE - 1) / PAGE_SIZE;
                if (totalPages < 1) totalPages = 1;
                int newPage = Math.min(page, totalPages - 1);

                this.mc.displayGuiScreen(new GuiEditElement(element, newPage));
            }
        }
    }

    private void flushFieldsToElement() {
        element.name = nameField.getText();

        int startIndex = page * PAGE_SIZE;
        int endIndex = Math.min(element.tickInputs.size(), startIndex + PAGE_SIZE);

        int visibleIdx = 0;
        for (int i = startIndex; i < endIndex; i++) {
            if (visibleIdx >= tickFields.size()) break;
            element.tickInputs.set(i, tickFields.get(visibleIdx).getText());
            visibleIdx++;
        }
    }

    @Override
    public void onGuiClosed() {
        Keyboard.enableRepeatEvents(false);
    }

    @Override
    protected void keyTyped(char typedChar, int keyCode) throws IOException {
        if (nameField != null && nameField.textboxKeyTyped(typedChar, keyCode)) return;

        for (int i = 0; i < tickFields.size(); i++) {
            if (tickFields.get(i).textboxKeyTyped(typedChar, keyCode)) return;
        }

        super.keyTyped(typedChar, keyCode);
    }

    @Override
    protected void mouseClicked(int mouseX, int mouseY, int mouseButton) throws IOException {
        if (nameField != null) nameField.mouseClicked(mouseX, mouseY, mouseButton);

        for (int i = 0; i < tickFields.size(); i++) {
            tickFields.get(i).mouseClicked(mouseX, mouseY, mouseButton);
        }

        super.mouseClicked(mouseX, mouseY, mouseButton);
    }

    @Override
    public void drawScreen(int mouseX, int mouseY, float partialTicks) {
        this.drawDefaultBackground();
        int cx = this.width / 2;

        this.drawCenteredString(this.fontRendererObj, "InputChecker - Edit Element", cx, 6, 0xFFFFFF);

        this.drawString(this.fontRendererObj, "Name:", cx - 140, 26, 0xCCCCCC);
        nameField.drawTextBox();

        int totalPages = (element.tickInputs.size() + PAGE_SIZE - 1) / PAGE_SIZE;
        if (totalPages < 1) totalPages = 1;

        this.drawString(this.fontRendererObj,
                "Page " + (page + 1) + "/" + totalPages + "  (" + element.tickInputs.size() + "/" + MAX_TICKS + " ticks)",
                cx - 140, 42, 0xAAAAAA);

        // Small helper text (does NOT overlap with boxes)
        this.drawString(
                this.fontRendererObj,
                "Syntax exemple : w | w+a | ignore-jump | lenient-w | wait",
                cx - 140,
                54,
                0x777777
        );

        // same spacing as initGui
        int baseY = 64;
        int rowH = 22;

        int startIndex = page * PAGE_SIZE;
        int endIndex = Math.min(element.tickInputs.size(), startIndex + PAGE_SIZE);

        int leftXLabel = cx - 245;
        int rightXLabel = cx + 15;

        int visibleIdx = 0;
        for (int i = startIndex; i < endIndex; i++) {
            int col = visibleIdx / COL_SIZE;
            int row = visibleIdx % COL_SIZE;
            int y = baseY + row * rowH;

            int xLabel = (col == 0) ? leftXLabel : rightXLabel;

            this.drawString(this.fontRendererObj, String.valueOf(i + 1), xLabel, y + 5, 0xAAAAAA);

            tickFields.get(visibleIdx).drawTextBox();
            visibleIdx++;
        }

        super.drawScreen(mouseX, mouseY, partialTicks);
    }
}

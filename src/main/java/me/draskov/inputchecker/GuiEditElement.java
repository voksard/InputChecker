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
    private GuiButton saveButton;
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
        nameField.setMaxStringLength(20); // Limite à 20 caractères pour éviter le dépassement
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

            // Désactiver les checkboxes en conflit
            String input = v != null ? v.toLowerCase() : "";
            // Sprint désactivé si prs-spr/rls-spr OU si no-sprint est coché
            disableSprintBoxes.add(input.contains("prs-spr") || input.contains("rls-spr") || (noSpr != null && noSpr));
            // Jump désactivé si prs-jmp/rls-jmp OU si no-jump est coché
            disableJumpBoxes.add(input.contains("prs-jmp") || input.contains("rls-jmp") || (noJmp != null && noJmp));
            // Sneak désactivé si prs-snk/rls-snk OU si no-sneak est coché
            disableSneakBoxes.add(input.contains("prs-snk") || input.contains("rls-snk") || (noSnk != null && noSnk));
            // No-sprint désactivé si prs-spr/rls-spr OU si sprint est coché
            disableNoSprintBoxes.add(input.contains("prs-spr") || input.contains("rls-spr") || (checkSpr != null && checkSpr));
            // No-jump désactivé si prs-jmp/rls-jmp OU si jump est coché
            disableNoJumpBoxes.add(input.contains("prs-jmp") || input.contains("rls-jmp") || (checkJmp != null && checkJmp));
            // No-sneak désactivé si prs-snk/rls-snk OU si sneak est coché
            disableNoSneakBoxes.add(input.contains("prs-snk") || input.contains("rls-snk") || (checkSnk != null && checkSnk));

            // Les boutons Insert/Duplicate/Delete et les checkboxes sont maintenant dessinés manuellement dans drawScreen
        }

        this.buttonList.add(new GuiButton(ID_SAVE, this.width - 200, 20, 80, 20, "Save"));
        this.buttonList.add(new GuiButton(ID_BACK, this.width - 115, 20, 80, 20, "Back"));

        saveButton = (GuiButton) this.buttonList.get(this.buttonList.size() - 2); // Le bouton Save est l'avant-dernier
        updateSaveButtonState();
    }

    @Override
    protected void actionPerformed(GuiButton b) throws IOException {
        if (b.id == ID_SAVE) {
            String newName = nameField.getText().trim();

            // Vérifier si le nom est valide
            if (!isNameValid(newName)) {
                return; // Ne rien faire si le nom n'est pas valide
            }

            flushFieldsToElement();
            element.name = newName;
            ElementStore.save();
            this.mc.displayGuiScreen(new GuiCatalog());
            return;
        }

        if (b.id == ID_BACK) {
            String newName = nameField.getText().trim();

            // Vérifier si le nom est valide avant de sauvegarder
            if (isNameValid(newName)) {
                flushFieldsToElement();
                element.name = newName;
                ElementStore.save();
            }

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
                // Pas de sauvegarde automatique (legacy button)
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
                // Pas de sauvegarde automatique (legacy button)
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

                // Pas de sauvegarde automatique (legacy button)
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
                // Pas de sauvegarde automatique (legacy button)
                this.initGui();
            }
            return;
        }

        if (b.id >= ID_CHECK_SPRINT_BASE && b.id < ID_CHECK_JUMP_BASE) {
            // Les checkboxes sont maintenant gérées dans mouseClicked, pas dans actionPerformed
            return;
        }

        if (b.id >= ID_CHECK_JUMP_BASE && b.id < ID_CHECK_SNEAK_BASE) {
            // Les checkboxes sont maintenant gérées dans mouseClicked, pas dans actionPerformed
            return;
        }

        if (b.id >= ID_CHECK_SNEAK_BASE && b.id < ID_NO_SPRINT_BASE) {
            // Les checkboxes sont maintenant gérées dans mouseClicked, pas dans actionPerformed
            return;
        }

        if (b.id >= ID_NO_SPRINT_BASE && b.id < ID_NO_JUMP_BASE) {
            // Les checkboxes sont maintenant gérées dans mouseClicked, pas dans actionPerformed
            return;
        }

        if (b.id >= ID_NO_JUMP_BASE && b.id < ID_NO_SNEAK_BASE) {
            // Les checkboxes sont maintenant gérées dans mouseClicked, pas dans actionPerformed
            return;
        }

        if (b.id >= ID_NO_SNEAK_BASE && b.id < ID_NO_SNEAK_BASE + 1000) {
            // Les checkboxes sont maintenant gérées dans mouseClicked, pas dans actionPerformed
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
        // Détecter la touche Échap (ESC) pour sauvegarder et fermer
        if (keyCode == Keyboard.KEY_ESCAPE) {
            String newName = nameField.getText().trim();

            // Vérifier si le nom est valide avant de sauvegarder
            if (isNameValid(newName)) {
                flushFieldsToElement();
                element.name = newName;
                ElementStore.save();
            }

            super.keyTyped(typedChar, keyCode); // Ferme l'interface
            return;
        }

        if (nameField != null && nameField.textboxKeyTyped(typedChar, keyCode)) {
            updateSaveButtonState(); // Mettre à jour l'état du bouton après chaque saisie
            return;
        }

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
                // Pas de sauvegarde automatique
            }
            return;
        }

        // Gérer les touches spéciales pour prs-snk/rls-snk, prs-jmp/rls-jmp, prs-spr/rls-spr
        // Shift → auto-compléter prs-snk ou rls-snk selon le contexte
        if (keyCode == Keyboard.KEY_LSHIFT || keyCode == Keyboard.KEY_RSHIFT) {
            // Bloquer si snk ou nk est coché sur cette ligne
            if (element.checkSneak.get(tickIdx) || element.noSneak.get(tickIdx)) {
                return; // Ne pas permettre prs-snk/rls-snk si snk ou nk est coché
            }

            // Déterminer si on termine "prs-" ou "rls-"
            if (current.endsWith("prs-")) {
                // Vérifier si prs-snk existe déjà
                if (inputAlreadyExists(current, "prs-snk")) {
                    return; // Bloquer le doublon
                }
                tf.setText(current + "snk");
            } else if (current.endsWith("rls-")) {
                // Vérifier si rls-snk existe déjà
                if (inputAlreadyExists(current, "rls-snk")) {
                    return; // Bloquer le doublon
                }
                tf.setText(current + "snk");
            } else {
                // Par défaut, ne rien faire si pas dans un contexte prs- ou rls-
                return;
            }

            element.tickInputs.set(tickIdx, tf.getText());
            sanitizeCheckboxesForTickInput(tickIdx);
            updateCheckboxesInMemory(tickIdx);
            // Pas de sauvegarde automatique
            return;
        }

        // Space → auto-compléter prs-jmp ou rls-jmp selon le contexte
        if (keyCode == Keyboard.KEY_SPACE) {
            // Bloquer si jmp ou nj est coché sur cette ligne
            if (element.checkJump.get(tickIdx) || element.noJump.get(tickIdx)) {
                return; // Ne pas permettre prs-jmp/rls-jmp si jmp ou nj est coché
            }

            // Déterminer si on termine "prs-" ou "rls-"
            if (current.endsWith("prs-")) {
                // Vérifier si prs-jmp existe déjà
                if (inputAlreadyExists(current, "prs-jmp")) {
                    return; // Bloquer le doublon
                }
                tf.setText(current + "jmp");
            } else if (current.endsWith("rls-")) {
                // Vérifier si rls-jmp existe déjà
                if (inputAlreadyExists(current, "rls-jmp")) {
                    return; // Bloquer le doublon
                }
                tf.setText(current + "jmp");
            } else {
                // Par défaut, ne rien faire si pas dans un contexte prs- ou rls-
                return;
            }

            element.tickInputs.set(tickIdx, tf.getText());
            sanitizeCheckboxesForTickInput(tickIdx);
            updateCheckboxesInMemory(tickIdx);
            // Pas de sauvegarde automatique
            return;
        }

        // Ctrl → auto-compléter prs-spr ou rls-spr selon le contexte
        if (keyCode == Keyboard.KEY_LCONTROL || keyCode == Keyboard.KEY_RCONTROL) {
            // Bloquer si spr ou ns est coché sur cette ligne
            if (element.checkSprint.get(tickIdx) || element.noSprint.get(tickIdx)) {
                return; // Ne pas permettre prs-spr/rls-spr si spr ou ns est coché
            }

            // Déterminer si on termine "prs-" ou "rls-"
            if (current.endsWith("prs-")) {
                // Vérifier si prs-spr existe déjà
                if (inputAlreadyExists(current, "prs-spr")) {
                    return; // Bloquer le doublon
                }
                tf.setText(current + "spr");
            } else if (current.endsWith("rls-")) {
                // Vérifier si rls-spr existe déjà
                if (inputAlreadyExists(current, "rls-spr")) {
                    return; // Bloquer le doublon
                }
                tf.setText(current + "spr");
            } else {
                // Par défaut, ne rien faire si pas dans un contexte prs- ou rls-
                return;
            }

            element.tickInputs.set(tickIdx, tf.getText());
            sanitizeCheckboxesForTickInput(tickIdx);
            updateCheckboxesInMemory(tickIdx);
            // Pas de sauvegarde automatique
            return;
        }

        // Gérer la touche 'P' pour auto-compléter "prs-"
        if (keyCode == Keyboard.KEY_P) {
            // Bloquer si le texte se termine déjà par "prs-" ou "rls-" (incomplet)
            if (current.endsWith("prs-") || current.endsWith("rls-")) {
                return; // Ne pas ajouter un nouveau préfixe si le précédent est incomplet
            }

            String toAdd = "prs-";
            if (current.isEmpty()) {
                tf.setText(toAdd);
            } else {
                tf.setText(current + "+" + toAdd);
            }
            element.tickInputs.set(tickIdx, tf.getText());
            sanitizeCheckboxesForTickInput(tickIdx);
            updateCheckboxesInMemory(tickIdx);
            // Pas de sauvegarde automatique
            return;
        }

        // Gérer la touche 'R' pour auto-compléter "rls-"
        if (keyCode == Keyboard.KEY_R) {
            // Bloquer si le texte se termine déjà par "prs-" ou "rls-" (incomplet)
            if (current.endsWith("prs-") || current.endsWith("rls-")) {
                return; // Ne pas ajouter un nouveau préfixe si le précédent est incomplet
            }

            String toAdd = "rls-";
            if (current.isEmpty()) {
                tf.setText(toAdd);
            } else {
                tf.setText(current + "+" + toAdd);
            }
            element.tickInputs.set(tickIdx, tf.getText());
            sanitizeCheckboxesForTickInput(tickIdx);
            updateCheckboxesInMemory(tickIdx);
            // Pas de sauvegarde automatique
            return;
        }

        // Gérer W, A, S, D
        String key = getKeyName(keyCode);
        if (key != null) {
            // Vérifier si cette touche existe déjà
            if (inputAlreadyExists(current, key)) {
                return; // Bloquer le doublon (ex: w+w)
            }

            // Vérifier les conflits logiques (ex: prs-w+w, rls-w+w, prs-w+rls-w)
            if (hasLogicalConflict(current, key)) {
                return; // Bloquer le conflit logique
            }

            // Vérifier si on est après "prs-" ou "rls-"
            if (current.endsWith("prs-") || current.endsWith("rls-")) {
                tf.setText(current + key);
            } else if (current.isEmpty()) {
                tf.setText(key);
            } else {
                tf.setText(current + "+" + key);
            }
            element.tickInputs.set(tickIdx, tf.getText());
            sanitizeCheckboxesForTickInput(tickIdx);
            updateCheckboxesInMemory(tickIdx);
            // Pas de sauvegarde automatique
            return;
        }

        // Gérer les touches pour spr/jmp/snk après "prs-" ou "rls-"
        if (current.endsWith("prs-") || current.endsWith("rls-")) {
            String lenientKey = getLenientKeyName(keyCode);
            if (lenientKey != null) {
                // Déterminer le prefix
                String prefix = current.endsWith("prs-") ? "prs-" : "rls-";
                String fullInput = prefix + lenientKey;

                // Vérifier si prs-xxx ou rls-xxx existe déjà
                if (inputAlreadyExists(current, fullInput)) {
                    return; // Bloquer le doublon
                }

                // Vérifier les conflits logiques (ex: prs-jmp+rls-jmp)
                if (hasLogicalConflict(current, fullInput)) {
                    return; // Bloquer le conflit logique
                }

                tf.setText(current + lenientKey);
                element.tickInputs.set(tickIdx, tf.getText());
                sanitizeCheckboxesForTickInput(tickIdx);
                updateCheckboxesInMemory(tickIdx);
                // Pas de sauvegarde automatique
                return;
            }
        }

        // Bloquer toutes les autres touches
        // Ne rien faire = pas d'écriture libre
    }

    /**
     * Vérifie si un input spécifique existe déjà dans la chaîne actuelle
     * Pour éviter les doublons comme "w+w" ou "lnt-jmp+lnt-jmp"
     */
    private boolean inputAlreadyExists(String current, String inputToAdd) {
        if (current.isEmpty()) return false;

        // Séparer par '+' et vérifier chaque partie
        String[] parts = current.split("\\+");
        for (String part : parts) {
            if (part.equals(inputToAdd)) {
                return true;
            }
        }
        return false;
    }

    /**
     * Vérifie si l'ajout d'un input créerait un conflit logique
     * Exemples de conflits :
     * - prs-w+rls-w (on ne peut pas presser ET relâcher la même touche)
     * - prs-w+w (redondant, presser implique avoir la touche)
     * - rls-w+w (contradictoire, relâcher implique ne pas avoir la touche)
     *
     * @param current Texte actuel dans la case
     * @param inputToAdd Input qu'on veut ajouter
     * @return true s'il y a un conflit, false sinon
     */
    private boolean hasLogicalConflict(String current, String inputToAdd) {
        if (current.isEmpty()) return false;

        // Extraire la clé de l'input à ajouter (w, a, s, d, jmp, spr, snk)
        String keyToAdd = extractKey(inputToAdd);
        if (keyToAdd == null) return false;

        // Extraire tous les inputs existants
        String[] parts = current.split("\\+");
        for (String part : parts) {
            part = part.trim();
            String existingKey = extractKey(part);
            if (existingKey == null || !existingKey.equals(keyToAdd)) continue;

            // Même clé détectée, vérifier les conflits
            boolean isAddingPrs = inputToAdd.startsWith("prs-");
            boolean isAddingRls = inputToAdd.startsWith("rls-");
            boolean isAddingNormal = !isAddingPrs && !isAddingRls;

            boolean existingIsPrs = part.startsWith("prs-");
            boolean existingIsRls = part.startsWith("rls-");
            boolean existingIsNormal = !existingIsPrs && !existingIsRls;

            // Conflits à bloquer :
            // 1. prs-X + rls-X (presser et relâcher la même touche)
            if ((isAddingPrs && existingIsRls) || (isAddingRls && existingIsPrs)) {
                return true;
            }

            // 2. prs-X + X (redondant, presser implique avoir)
            if ((isAddingPrs && existingIsNormal) || (isAddingNormal && existingIsPrs)) {
                return true;
            }

            // 3. rls-X + X (contradictoire, relâcher implique ne pas avoir)
            if ((isAddingRls && existingIsNormal) || (isAddingNormal && existingIsRls)) {
                return true;
            }
        }

        return false;
    }

    /**
     * Extrait la clé (w, a, s, d, jmp, spr, snk) d'un input
     * @param input L'input (ex: "w", "prs-w", "rls-jmp")
     * @return La clé extraite, ou null si non reconnu
     */
    private String extractKey(String input) {
        if (input == null || input.isEmpty()) return null;

        // Si c'est un input avec préfixe
        if (input.startsWith("prs-") || input.startsWith("rls-")) {
            return input.substring(4); // Extraire après "prs-" ou "rls-"
        }

        // Si c'est un input normal (w, a, s, d, jmp, spr, snk)
        if (input.matches("^(w|a|s|d|jmp|spr|snk)$")) {
            return input;
        }

        return null;
    }

    /**
     * Vérifie si une touche (jmp/spr/snk) est présente sur cette ligne
     * (soit dans l'input texte, soit via checkbox)
     */
    private boolean isKeyPresentOnLine(int tickIdx, String current, String key) {
        // Vérifier dans l'input texte
        if (current.contains(key)) {
            return true;
        }

        // Vérifier dans les checkboxes
        switch (key) {
            case "jmp":
            case "jump":
                return element.checkJump.get(tickIdx);
            case "spr":
            case "sprint":
                return element.checkSprint.get(tickIdx);
            case "snk":
            case "sneak":
                return element.checkSneak.get(tickIdx);
            default:
                // Pour w/a/s/d, ils doivent être dans l'input texte
                return false;
        }
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

        // Mettre à jour les états de désactivation en tenant compte des checkboxes cochées
        boolean checkSpr = element.checkSprint.get(tickIdx);
        boolean checkJmp = element.checkJump.get(tickIdx);
        boolean checkSnk = element.checkSneak.get(tickIdx);
        boolean noSpr = element.noSprint.get(tickIdx);
        boolean noJmp = element.noJump.get(tickIdx);
        boolean noSnk = element.noSneak.get(tickIdx);

        // Sprint désactivé si prs-spr/rls-spr OU si no-sprint est coché
        disableSprintBoxes.set(row, input.contains("prs-spr") || input.contains("rls-spr") || noSpr);
        // Jump désactivé si prs-jmp/rls-jmp OU si no-jump est coché
        disableJumpBoxes.set(row, input.contains("prs-jmp") || input.contains("rls-jmp") || noJmp);
        // Sneak désactivé si prs-snk/rls-snk OU si no-sneak est coché
        disableSneakBoxes.set(row, input.contains("prs-snk") || input.contains("rls-snk") || noSnk);
        // No-sprint désactivé si lnt-spr OU si sprint est coché
        disableNoSprintBoxes.set(row, input.contains("prs-spr") || input.contains("rls-spr") || checkSpr);
        // No-jump désactivé si prs-jmp/rls-jmp OU si jump est coché
        disableNoJumpBoxes.set(row, input.contains("prs-jmp") || input.contains("rls-jmp") || checkJmp);
        // No-sneak désactivé si prs-snk/rls-snk OU si sneak est coché
        disableNoSneakBoxes.set(row, input.contains("prs-snk") || input.contains("rls-snk") || checkSnk);
    }

    /**
     * Décoche automatiquement les checkboxes conflictuelles avec prs- ou rls- pour un tick
     */
    private void sanitizeCheckboxesForTickInput(int tickIdx) {
        if (tickIdx < 0 || tickIdx >= element.tickInputs.size()) return;

        String input = element.tickInputs.get(tickIdx).toLowerCase();

        // Si prs-jmp ou rls-jmp est présent, décocher checkJump et noJump
        if (input.contains("prs-jmp") || input.contains("rls-jmp")) {
            element.checkJump.set(tickIdx, false);
            element.noJump.set(tickIdx, false);
        }

        // Si prs-spr ou rls-spr est présent, décocher checkSprint et noSprint
        if (input.contains("prs-spr") || input.contains("rls-spr")) {
            element.checkSprint.set(tickIdx, false);
            element.noSprint.set(tickIdx, false);
        }

        // Si prs-snk ou rls-snk est présent, décocher checkSneak et noSneak
        if (input.contains("prs-snk") || input.contains("rls-snk")) {
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
            // J, K, P ne sont plus ici car jmp/snk/spr sont saisis via Space/Shift/Ctrl
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

        // Gestion des clics sur les checkboxes et boutons d'action
        int cx = this.width / 2;
        int baseY = 60;

        for (int row = 0; row < tickFields.size(); row++) {
            int tickIdx = scrollOffset + row;
            int y = baseY + row * ROW_HEIGHT;

            // Vérifier si on clique sur un bouton d'action
            if (isMouseInRect(mouseX, mouseY, cx - 165, y, 25, 16)) {
                handleInsertAbove(tickIdx);
                return;
            }
            if (isMouseInRect(mouseX, mouseY, cx - 135, y, 25, 16)) {
                handleInsertBelow(tickIdx);
                return;
            }
            if (isMouseInRect(mouseX, mouseY, cx - 105, y, 20, 16)) {
                handleDuplicate(tickIdx);
                return;
            }
            if (isMouseInRect(mouseX, mouseY, cx - 80, y, 20, 16)) {
                handleDelete(tickIdx);
                return;
            }

            // Vérifier si on clique sur une checkbox (16x16)
            if (isMouseInCheckbox(mouseX, mouseY, cx + 70, y + 2)) {
                handleCheckboxClick(tickIdx, "sprint");
                return;
            }
            if (isMouseInCheckbox(mouseX, mouseY, cx + 100, y + 2)) {
                handleCheckboxClick(tickIdx, "jump");
                return;
            }
            if (isMouseInCheckbox(mouseX, mouseY, cx + 130, y + 2)) {
                handleCheckboxClick(tickIdx, "sneak");
                return;
            }
            if (isMouseInCheckbox(mouseX, mouseY, cx + 160, y + 2)) {
                handleCheckboxClick(tickIdx, "noSprint");
                return;
            }
            if (isMouseInCheckbox(mouseX, mouseY, cx + 190, y + 2)) {
                handleCheckboxClick(tickIdx, "noJump");
                return;
            }
            if (isMouseInCheckbox(mouseX, mouseY, cx + 220, y + 2)) {
                handleCheckboxClick(tickIdx, "noSneak");
                return;
            }
        }

        super.mouseClicked(mouseX, mouseY, mouseButton);
    }

    private boolean isMouseInRect(int mouseX, int mouseY, int x, int y, int width, int height) {
        return mouseX >= x && mouseX <= x + width && mouseY >= y && mouseY <= y + height;
    }

    private boolean isMouseInCheckbox(int mouseX, int mouseY, int boxX, int boxY) {
        int boxWidth = 16;
        int boxHeight = 16;
        return mouseX >= boxX && mouseX <= boxX + boxWidth && mouseY >= boxY && mouseY <= boxY + boxHeight;
    }

    private void handleCheckboxClick(int tickIdx, String type) {
        String input = element.tickInputs.get(tickIdx).toLowerCase();

        switch (type) {
            case "sprint":
                if (element.noSprint.get(tickIdx)) return;
                if (!input.contains("lnt-spr")) {
                    element.checkSprint.set(tickIdx, !element.checkSprint.get(tickIdx));
                    this.initGui();
                }
                break;
            case "jump":
                if (element.noJump.get(tickIdx)) return;
                if (!input.contains("lnt-jmp")) {
                    element.checkJump.set(tickIdx, !element.checkJump.get(tickIdx));
                    this.initGui();
                }
                break;
            case "sneak":
                if (element.noSneak.get(tickIdx)) return;
                if (!input.contains("lnt-snk")) {
                    element.checkSneak.set(tickIdx, !element.checkSneak.get(tickIdx));
                    this.initGui();
                }
                break;
            case "noSprint":
                if (element.checkSprint.get(tickIdx)) return;
                if (!input.contains("lnt-spr")) {
                    element.noSprint.set(tickIdx, !element.noSprint.get(tickIdx));
                    this.initGui();
                }
                break;
            case "noJump":
                if (element.checkJump.get(tickIdx)) return;
                if (!input.contains("lnt-jmp")) {
                    element.noJump.set(tickIdx, !element.noJump.get(tickIdx));
                    this.initGui();
                }
                break;
            case "noSneak":
                if (element.checkSneak.get(tickIdx)) return;
                if (!input.contains("lnt-snk")) {
                    element.noSneak.set(tickIdx, !element.noSneak.get(tickIdx));
                    this.initGui();
                }
                break;
        }
    }

    private void handleInsertAbove(int tickIdx) {
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
            // Pas de sauvegarde automatique - l'utilisateur clique sur "Save" quand il veut
            this.initGui();
        }
    }

    private void handleInsertBelow(int tickIdx) {
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
            // Pas de sauvegarde automatique
            this.initGui();
        }
    }

    private void handleDelete(int tickIdx) {
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

            // Pas de sauvegarde automatique
            this.initGui();
        }
    }

    private void handleDuplicate(int tickIdx) {
        if (tickIdx >= 0 && tickIdx < element.tickInputs.size()) {
            flushFieldsToElement();
            String content = element.tickInputs.get(tickIdx);

            // Insérer une nouvelle ligne à la position tickIdx + 1 (au lieu d'écraser)
            element.tickInputs.add(tickIdx + 1, content);
            element.checkSprint.add(tickIdx + 1, element.checkSprint.get(tickIdx));
            element.checkJump.add(tickIdx + 1, element.checkJump.get(tickIdx));
            element.checkSneak.add(tickIdx + 1, element.checkSneak.get(tickIdx));
            element.noSprint.add(tickIdx + 1, element.noSprint.get(tickIdx));
            element.noJump.add(tickIdx + 1, element.noJump.get(tickIdx));
            element.noSneak.add(tickIdx + 1, element.noSneak.get(tickIdx));

            // Si on dépasse MAX_TICKS, supprimer la dernière ligne
            if (element.tickInputs.size() > MAX_TICKS) {
                element.tickInputs.remove(element.tickInputs.size() - 1);
                element.checkSprint.remove(element.checkSprint.size() - 1);
                element.checkJump.remove(element.checkJump.size() - 1);
                element.checkSneak.remove(element.checkSneak.size() - 1);
                element.noSprint.remove(element.noSprint.size() - 1);
                element.noJump.remove(element.noJump.size() - 1);
                element.noSneak.remove(element.noSneak.size() - 1);
            }

            // Pas de sauvegarde automatique
            this.initGui();
        }
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

        this.drawString(this.fontRendererObj, "Type prs-input", 10, 155, 0xAAAAAA);
        this.drawString(this.fontRendererObj, "or rls-input for", 10, 165, 0xAAAAAA);
        this.drawString(this.fontRendererObj, "press/release", 10, 175, 0xAAAAAA);
        // Ligne vide pour espacement

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

            // Dessiner les boutons d'action avec bordures
            drawActionButton(cx - 165, y, 25, 16, "+↑"); // Insert Above
            drawActionButton(cx - 135, y, 25, 16, "+↓"); // Insert Below
            drawActionButton(cx - 105, y, 20, 16, "D");  // Duplicate
            drawActionButton(cx - 80, y, 20, 16, "X");   // Delete

            // Afficher les checkboxes avec couleur grise si désactivées
            int sprColor = disableSprintBoxes.get(row) ? 0x888888 : 0xFFFFFF;
            int jmpColor = disableJumpBoxes.get(row) ? 0x888888 : 0xFFFFFF;
            int snkColor = disableSneakBoxes.get(row) ? 0x888888 : 0xFFFFFF;
            int nsColor = disableNoSprintBoxes.get(row) ? 0x888888 : 0xFFFFFF;
            int njColor = disableNoJumpBoxes.get(row) ? 0x888888 : 0xFFFFFF;
            int nkColor = disableNoSneakBoxes.get(row) ? 0x888888 : 0xFFFFFF;

            // Sprint - avec bordure
            drawCheckboxWithBorder(cx + 70, y + 2, checkSprintBoxes.get(row), disableSprintBoxes.get(row), sprColor);

            // Jump - avec bordure
            drawCheckboxWithBorder(cx + 100, y + 2, checkJumpBoxes.get(row), disableJumpBoxes.get(row), jmpColor);

            // Sneak - avec bordure
            drawCheckboxWithBorder(cx + 130, y + 2, checkSneakBoxes.get(row), disableSneakBoxes.get(row), snkColor);

            // No Sprint - avec bordure (blanc au lieu de rouge)
            drawCheckboxWithBorder(cx + 160, y + 2, noSprintBoxes.get(row), disableNoSprintBoxes.get(row), nsColor);

            // No Jump - avec bordure (blanc au lieu de rouge)
            drawCheckboxWithBorder(cx + 190, y + 2, noJumpBoxes.get(row), disableNoJumpBoxes.get(row), njColor);

            // No Sneak - avec bordure (blanc au lieu de rouge)
            drawCheckboxWithBorder(cx + 220, y + 2, noSneakBoxes.get(row), disableNoSneakBoxes.get(row), nkColor);
        }

        super.drawScreen(mouseX, mouseY, partialTicks);
    }

    /**
     * Dessine une checkbox avec bordure noire complète sur les 4 côtés
     */
    private void drawCheckboxWithBorder(int x, int y, boolean checked, boolean disabled, int checkColor) {
        int width = 16;
        int height = 16;

        // Fond gris foncé pour mieux voir les symboles
        // Si désactivée, fond beaucoup plus gris/plus sombre
        int bgColor = disabled ? 0xFF303030 : 0xFF505050;
        drawRect(x, y, x + width, y + height, bgColor);

        // Bordure noire complète - 4 côtés
        // Bord haut
        drawRect(x, y, x + width, y + 1, 0xFF000000);
        // Bord bas
        drawRect(x, y + height - 1, x + width, y + height, 0xFF000000);
        // Bord gauche
        drawRect(x, y, x + 1, y + height, 0xFF000000);
        // Bord droit
        drawRect(x + width - 1, y, x + width, y + height, 0xFF000000);

        // Afficher le symbole au centre
        if (checked) {
            this.drawString(this.fontRendererObj, "✓", x + 5, y + 4, checkColor);
        }
        // Ne plus afficher de croix pour les cases désactivées
    }

    /**
     * Dessine un bouton d'action (Insert/Duplicate/Delete) avec bordure noire complète
     */
    private void drawActionButton(int x, int y, int width, int height, String label) {
        // Fond gris foncé
        drawRect(x, y, x + width, y + height, 0xFF505050);

        // Bordure noire complète - 4 côtés
        // Bord haut
        drawRect(x, y, x + width, y + 1, 0xFF000000);
        // Bord bas
        drawRect(x, y + height - 1, x + width, y + height, 0xFF000000);
        // Bord gauche
        drawRect(x, y, x + 1, y + height, 0xFF000000);
        // Bord droit
        drawRect(x + width - 1, y, x + width, y + height, 0xFF000000);

        // Centrer le texte
        int textWidth = this.fontRendererObj.getStringWidth(label);
        int textX = x + (width - textWidth) / 2;
        int textY = y + (height - 8) / 2;
        this.drawString(this.fontRendererObj, label, textX, textY, 0xFFFFFF);
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

    /**
     * Vérifie si le nom saisi est valide
     * @param name Le nom à vérifier
     * @return true si le nom est valide (non vide et n'existe pas déjà pour un autre élément), false sinon
     */
    private boolean isNameValid(String name) {
        // Vérifier si le nom est vide
        if (name == null || name.trim().isEmpty()) {
            return false;
        }

        // Vérifier si un autre élément avec ce nom existe déjà
        // (on autorise le même nom que l'élément actuel)
        for (CheckElement el : ElementStore.elements) {
            if (!el.id.equals(element.id) && el.name.equalsIgnoreCase(name.trim())) {
                return false;
            }
        }

        return true;
    }

    /**
     * Met à jour l'état (enabled/disabled) du bouton Save
     */
    private void updateSaveButtonState() {
        if (saveButton != null && nameField != null) {
            String name = nameField.getText().trim();
            saveButton.enabled = isNameValid(name);
        }
    }
}


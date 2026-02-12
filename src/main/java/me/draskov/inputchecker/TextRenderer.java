package me.draskov.inputchecker;

import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.FontRenderer;
import net.minecraft.client.renderer.GlStateManager;

/**
 * Classe utilitaire pour afficher du texte avec ombre portée dans Minecraft
 */
public class TextRenderer {

    /**
     * Affiche un texte avec ombre portée
     *
     * @param text Le texte à afficher
     * @param x Position X
     * @param y Position Y
     * @param color Couleur du texte (format ARGB, par défaut blanc)
     * @param shadow Si true, ajoute une ombre portée
     */
    public static void drawString(String text, int x, int y, int color, boolean shadow) {
        FontRenderer font = Minecraft.getMinecraft().fontRendererObj;
        GlStateManager.color(1.0F, 1.0F, 1.0F, 1.0F);
        font.drawString(text, x, y, color, shadow);
    }

    /**
     * Affiche un texte blanc avec ombre portée
     *
     * @param text Le texte à afficher
     * @param x Position X
     * @param y Position Y
     */
    public static void drawStringWhite(String text, int x, int y) {
        drawString(text, x, y, 0xFFFFFFFF, true);
    }

    /**
     * Affiche un texte avec ombre portée (format "Label: Valeur")
     *
     * @param label Le label (première partie)
     * @param value La valeur (deuxième partie)
     * @param x Position X
     * @param y Position Y
     * @param labelColor Couleur du label
     * @param valueColor Couleur de la valeur
     */
    public static void drawLabelValue(String label, String value, int x, int y, int labelColor, int valueColor) {
        FontRenderer font = Minecraft.getMinecraft().fontRendererObj;
        GlStateManager.color(1.0F, 1.0F, 1.0F, 1.0F);
        font.drawString(label + ": ", x, y, labelColor, true);
        int offset = font.getStringWidth(label + ": ");
        font.drawString(value, x + offset, y, valueColor, true);
    }

    /**
     * Retourne la largeur d'un texte en pixels
     *
     * @param text Le texte
     * @return La largeur en pixels
     */
    public static int getStringWidth(String text) {
        return Minecraft.getMinecraft().fontRendererObj.getStringWidth(text);
    }
}


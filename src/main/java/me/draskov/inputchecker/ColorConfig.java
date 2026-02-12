package me.draskov.inputchecker;

public class ColorConfig {
    public int titleColor = 0xFFFFFF;
    public int contentColor = 0xAAAAAA;

    private static final com.google.gson.Gson GSON = new com.google.gson.GsonBuilder().setPrettyPrinting().create();
    private static ColorConfig INSTANCE = new ColorConfig();

    public static ColorConfig get() {
        return INSTANCE;
    }

    private static java.io.File getFile() {
        java.io.File configDir = new java.io.File(net.minecraft.client.Minecraft.getMinecraft().mcDataDir, "config");
        if (!configDir.exists()) configDir.mkdirs();
        return new java.io.File(configDir, "inputchecker_colors.json");
    }

    public static void load() {
        java.io.File f = getFile();
        if (!f.exists()) {
            save();
            return;
        }

        try (java.io.Reader r = new java.io.FileReader(f)) {
            ColorConfig loaded = GSON.fromJson(r, ColorConfig.class);
            if (loaded != null) INSTANCE = loaded;
        } catch (Exception ignored) {
        }
    }

    public static void save() {
        java.io.File f = getFile();
        try (java.io.Writer w = new java.io.FileWriter(f)) {
            GSON.toJson(INSTANCE, w);
        } catch (Exception ignored) {
        }
    }

    public static java.util.Map<String, Integer> getColorMap() {
        java.util.Map<String, Integer> map = new java.util.LinkedHashMap<>();
        // Couleurs Minecraft standard (codes §0 à §f)
        map.put("black", 0x000000);      // §0
        map.put("dblue", 0x0000AA);      // §1 (dark blue)
        map.put("dgreen", 0x00AA00);     // §2 (dark green)
        map.put("daqua", 0x00AAAA);      // §3 (dark aqua)
        map.put("dred", 0xAA0000);       // §4 (dark red)
        map.put("dpurple", 0xAA00AA);    // §5 (dark purple)
        map.put("gold", 0xFFAA00);       // §6
        map.put("gray", 0xAAAAAA);       // §7
        map.put("dgray", 0x555555);      // §8 (dark gray)
        map.put("blue", 0x5555FF);       // §9
        map.put("green", 0x55FF55);      // §a
        map.put("aqua", 0x55FFFF);       // §b
        map.put("red", 0xFF5555);        // §c
        map.put("lpurple", 0xFF55FF);    // §d (light purple)
        map.put("yellow", 0xFFFF55);     // §e
        map.put("white", 0xFFFFFF);      // §f
        return map;
    }

    public static Integer parseColor(String colorName) {
        return getColorMap().get(colorName.toLowerCase());
    }

    /**
     * Retourne le code de couleur Minecraft (ex: "§7") basé sur contentColor.
     * Utilise un mapping exact des couleurs Minecraft standard.
     */
    public static String getContentColorCode() {
        int color = get().contentColor;

        // Mapping exact des couleurs Minecraft RGB vers les codes §0-§f
        if (color == 0x000000) return "§0"; // black
        if (color == 0x0000AA) return "§1"; // dark blue
        if (color == 0x00AA00) return "§2"; // dark green
        if (color == 0x00AAAA) return "§3"; // dark aqua
        if (color == 0xAA0000) return "§4"; // dark red
        if (color == 0xAA00AA) return "§5"; // dark purple
        if (color == 0xFFAA00) return "§6"; // gold
        if (color == 0xAAAAAA) return "§7"; // gray
        if (color == 0x555555) return "§8"; // dark gray
        if (color == 0x5555FF) return "§9"; // blue
        if (color == 0x55FF55) return "§a"; // green
        if (color == 0x55FFFF) return "§b"; // aqua
        if (color == 0xFF5555) return "§c"; // red
        if (color == 0xFF55FF) return "§d"; // light purple
        if (color == 0xFFFF55) return "§e"; // yellow
        if (color == 0xFFFFFF) return "§f"; // white

        // Si la couleur n'est pas dans la palette standard, trouver la plus proche
        int minDist = Integer.MAX_VALUE;
        String bestCode = "§7"; // gris par défaut

        java.util.Map<Integer, String> colorMap = new java.util.HashMap<>();
        colorMap.put(0x000000, "§0");
        colorMap.put(0x0000AA, "§1");
        colorMap.put(0x00AA00, "§2");
        colorMap.put(0x00AAAA, "§3");
        colorMap.put(0xAA0000, "§4");
        colorMap.put(0xAA00AA, "§5");
        colorMap.put(0xFFAA00, "§6");
        colorMap.put(0xAAAAAA, "§7");
        colorMap.put(0x555555, "§8");
        colorMap.put(0x5555FF, "§9");
        colorMap.put(0x55FF55, "§a");
        colorMap.put(0x55FFFF, "§b");
        colorMap.put(0xFF5555, "§c");
        colorMap.put(0xFF55FF, "§d");
        colorMap.put(0xFFFF55, "§e");
        colorMap.put(0xFFFFFF, "§f");

        int r1 = (color >> 16) & 0xFF;
        int g1 = (color >> 8) & 0xFF;
        int b1 = color & 0xFF;

        for (java.util.Map.Entry<Integer, String> entry : colorMap.entrySet()) {
            int c = entry.getKey();
            int r2 = (c >> 16) & 0xFF;
            int g2 = (c >> 8) & 0xFF;
            int b2 = c & 0xFF;

            int dist = (r1 - r2) * (r1 - r2) + (g1 - g2) * (g1 - g2) + (b1 - b2) * (b1 - b2);
            if (dist < minDist) {
                minDist = dist;
                bestCode = entry.getValue();
            }
        }

        return bestCode;
    }

    /**
     * Retourne le code de couleur Minecraft pour color1 (titleColor).
     * Utilisé pour les valeurs, pourcentages, inputs, etc.
     */
    public static String getTitleColorCode() {
        int color = get().titleColor;

        // Mapping exact des couleurs Minecraft RGB vers les codes §0-§f
        if (color == 0x000000) return "§0"; // black
        if (color == 0x0000AA) return "§1"; // dark blue
        if (color == 0x00AA00) return "§2"; // dark green
        if (color == 0x00AAAA) return "§3"; // dark aqua
        if (color == 0xAA0000) return "§4"; // dark red
        if (color == 0xAA00AA) return "§5"; // dark purple
        if (color == 0xFFAA00) return "§6"; // gold
        if (color == 0xAAAAAA) return "§7"; // gray
        if (color == 0x555555) return "§8"; // dark gray
        if (color == 0x5555FF) return "§9"; // blue
        if (color == 0x55FF55) return "§a"; // green
        if (color == 0x55FFFF) return "§b"; // aqua
        if (color == 0xFF5555) return "§c"; // red
        if (color == 0xFF55FF) return "§d"; // light purple
        if (color == 0xFFFF55) return "§e"; // yellow
        if (color == 0xFFFFFF) return "§f"; // white

        // Si la couleur n'est pas dans la palette standard, trouver la plus proche
        int minDist = Integer.MAX_VALUE;
        String bestCode = "§7"; // gris par défaut

        java.util.Map<Integer, String> colorMap = new java.util.HashMap<>();
        colorMap.put(0x000000, "§0");
        colorMap.put(0x0000AA, "§1");
        colorMap.put(0x00AA00, "§2");
        colorMap.put(0x00AAAA, "§3");
        colorMap.put(0xAA0000, "§4");
        colorMap.put(0xAA00AA, "§5");
        colorMap.put(0xFFAA00, "§6");
        colorMap.put(0xAAAAAA, "§7");
        colorMap.put(0x555555, "§8");
        colorMap.put(0x5555FF, "§9");
        colorMap.put(0x55FF55, "§a");
        colorMap.put(0x55FFFF, "§b");
        colorMap.put(0xFF5555, "§c");
        colorMap.put(0xFF55FF, "§d");
        colorMap.put(0xFFFF55, "§e");
        colorMap.put(0xFFFFFF, "§f");

        int r1 = (color >> 16) & 0xFF;
        int g1 = (color >> 8) & 0xFF;
        int b1 = color & 0xFF;

        for (java.util.Map.Entry<Integer, String> entry : colorMap.entrySet()) {
            int c = entry.getKey();
            int r2 = (c >> 16) & 0xFF;
            int g2 = (c >> 8) & 0xFF;
            int b2 = c & 0xFF;

            int dist = (r1 - r2) * (r1 - r2) + (g1 - g2) * (g1 - g2) + (b1 - b2) * (b1 - b2);
            if (dist < minDist) {
                minDist = dist;
                bestCode = entry.getValue();
            }
        }

        return bestCode;
    }
}


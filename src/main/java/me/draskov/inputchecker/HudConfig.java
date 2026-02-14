package me.draskov.inputchecker;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import net.minecraft.client.Minecraft;

import java.io.File;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.Reader;
import java.io.Writer;

public class HudConfig {
    public int x = 6;
    public int y = 6;
    public boolean visible = true;
    public int statsX = 6;
    public int statsY = 90;
    public boolean statsVisible = true;

    private static final Gson GSON = new GsonBuilder().setPrettyPrinting().create();
    private static HudConfig INSTANCE = new HudConfig();

    public static HudConfig get() {
        return INSTANCE;
    }

    private static File getFile() {
        File configDir = new File(Minecraft.getMinecraft().mcDataDir, "config");
        if (!configDir.exists()) configDir.mkdirs();
        return new File(configDir, "inputchecker_hud.json");
    }

    public static void load() {
        File f = getFile();
        if (!f.exists()) {
            save();
            return;
        }

        try (Reader r = new FileReader(f)) {
            HudConfig loaded = GSON.fromJson(r, HudConfig.class);
            if (loaded != null) {
                INSTANCE = loaded;
                // Valider et corriger les positions si elles sont hors limites
                validatePositions();
            }
        } catch (Exception ignored) {
        }
    }

    /**
     * Valide et corrige les positions pour s'assurer qu'elles restent dans des limites raisonnables
     * Cela empêche les panneaux d'être perdus hors écran si la résolution change
     */
    private static void validatePositions() {
        Minecraft mc = Minecraft.getMinecraft();
        int screenWidth = mc.displayWidth / 2; // ScaledResolution divise par 2
        int screenHeight = mc.displayHeight / 2;

        // Si les positions sont complètement hors écran, les réinitialiser
        if (INSTANCE.x < -500 || INSTANCE.x > screenWidth + 500) {
            INSTANCE.x = 6;
        }
        if (INSTANCE.y < -500 || INSTANCE.y > screenHeight + 500) {
            INSTANCE.y = 6;
        }
        if (INSTANCE.statsX < -500 || INSTANCE.statsX > screenWidth + 500) {
            INSTANCE.statsX = 6;
        }
        if (INSTANCE.statsY < -500 || INSTANCE.statsY > screenHeight + 500) {
            INSTANCE.statsY = 90;
        }
    }

    public static void save() {
        File f = getFile();
        try (Writer w = new FileWriter(f)) {
            GSON.toJson(INSTANCE, w);
        } catch (Exception ignored) {
        }
    }
}

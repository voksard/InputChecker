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
            if (loaded != null) INSTANCE = loaded;
        } catch (Exception ignored) {
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

package me.draskov.inputchecker;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import net.minecraft.client.Minecraft;

import java.io.File;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.Reader;
import java.io.Writer;

public class InputCheckerConfig {

    // true  = sprint (ctrl) ignored by default unless mentioned (current behavior)
    // false = sprint (ctrl) checked like other keys (strict)
    public boolean fullSprint = true;

    private static final Gson GSON = new GsonBuilder().setPrettyPrinting().create();
    private static InputCheckerConfig INSTANCE = new InputCheckerConfig();

    public static InputCheckerConfig get() {
        return INSTANCE;
    }

    private static File getFile() {
        File configDir = new File(Minecraft.getMinecraft().mcDataDir, "config");
        if (!configDir.exists()) configDir.mkdirs();
        return new File(configDir, "inputchecker_config.json");
    }

    public static void load() {
        File f = getFile();
        if (!f.exists()) {
            save();
            return;
        }
        Reader r = null;
        try {
            r = new FileReader(f);
            InputCheckerConfig loaded = GSON.fromJson(r, InputCheckerConfig.class);
            if (loaded != null) INSTANCE = loaded;
        } catch (Exception ignored) {
        } finally {
            if (r != null) {
                try { r.close(); } catch (Exception ignored2) {}
            }
        }
    }

    public static void save() {
        File f = getFile();
        Writer w = null;
        try {
            w = new FileWriter(f);
            GSON.toJson(INSTANCE, w);
        } catch (Exception ignored) {
        } finally {
            if (w != null) {
                try { w.close(); } catch (Exception ignored2) {}
            }
        }
    }
}

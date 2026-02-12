package me.draskov.inputchecker;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.reflect.TypeToken;
import net.minecraft.client.Minecraft;

import java.io.*;
import java.lang.reflect.Type;
import java.util.ArrayList;
import java.util.List;

public class ElementStore {
    private static final Gson GSON = new GsonBuilder().setPrettyPrinting().create();
    private static final Type LIST_TYPE = new TypeToken<List<CheckElement>>(){}.getType();

    public static List<CheckElement> elements = new ArrayList<>();
    public static String activeId = null;

    private static File getFile() {
        File configDir = new File(Minecraft.getMinecraft().mcDataDir, "config");
        if (!configDir.exists()) configDir.mkdirs();
        return new File(configDir, "inputchecker.json");
    }

    public static void load() {
        File f = getFile();
        if (!f.exists()) {
            save();
            return;
        }

        try (Reader r = new FileReader(f)) {
            List<CheckElement> loaded = GSON.fromJson(r, LIST_TYPE);
            elements = loaded != null ? loaded : new ArrayList<>();
        } catch (Exception ex) {
            elements = new ArrayList<>();
        }
    }

    public static void save() {
        File f = getFile();
        try (Writer w = new FileWriter(f)) {
            GSON.toJson(elements, LIST_TYPE, w);
        } catch (Exception ignored) {
        }
    }

    public static CheckElement getActive() {
        if (activeId == null) return null;
        for (CheckElement e : elements) {
            if (e.id.equals(activeId)) return e;
        }
        return null;
    }

    public static void clearActive() {
        activeId = null;
    }
}

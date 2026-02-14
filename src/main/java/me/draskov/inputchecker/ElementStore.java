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

    /**
     * Exporte un élément vers un fichier JSON séparé
     * @param element L'élément à exporter
     * @return Le nom du fichier créé, ou null en cas d'erreur
     */
    public static String exportElement(CheckElement element) {
        if (element == null) return null;

        // Créer un nom de fichier basé sur le nom de l'élément
        String filename = element.name.replaceAll("[^a-zA-Z0-9_-]", "_") + ".json";

        File configDir = new File(Minecraft.getMinecraft().mcDataDir, "config");
        if (!configDir.exists()) configDir.mkdirs();

        File exportFile = new File(configDir, filename);

        try (Writer w = new FileWriter(exportFile)) {
            GSON.toJson(element, CheckElement.class, w);
            return filename;
        } catch (Exception e) {
            e.printStackTrace();
            return null;
        }
    }

    /**
     * Importe un élément depuis un fichier JSON
     * @param filename Le nom du fichier à importer (dans le dossier config)
     * @return L'élément importé, ou null en cas d'erreur
     */
    public static CheckElement importElement(String filename) {
        File configDir = new File(Minecraft.getMinecraft().mcDataDir, "config");
        File importFile = new File(configDir, filename);

        if (!importFile.exists()) {
            return null;
        }

        try (Reader r = new FileReader(importFile)) {
            CheckElement imported = GSON.fromJson(r, CheckElement.class);

            if (imported == null) return null;

            // Générer un nouvel ID unique pour éviter les conflits
            imported.id = java.util.UUID.randomUUID().toString();

            // Vérifier si un élément avec le même nom existe déjà
            boolean nameExists = false;
            for (CheckElement e : elements) {
                if (e.name.equals(imported.name)) {
                    nameExists = true;
                    break;
                }
            }

            // Si le nom existe, ajouter un suffixe
            if (nameExists) {
                int counter = 1;
                String baseName = imported.name;
                while (nameExists) {
                    imported.name = baseName + " (" + counter + ")";
                    nameExists = false;
                    for (CheckElement e : elements) {
                        if (e.name.equals(imported.name)) {
                            nameExists = true;
                            break;
                        }
                    }
                    counter++;
                }
            }

            // Ajouter l'élément à la liste
            elements.add(imported);
            save();

            return imported;
        } catch (Exception e) {
            e.printStackTrace();
            return null;
        }
    }
}

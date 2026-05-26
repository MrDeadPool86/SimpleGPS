package de.mrdeadpool.simplegps.client;

import com.google.gson.*;
import net.minecraft.client.Minecraft;

import java.io.*;
import java.nio.file.*;
import java.util.ArrayList;
import java.util.List;

public class GPSCategories {

    private static final List<GPSCategory> categories = new ArrayList<>();

    public static void addCategory(String name) {
        boolean exists = categories.stream()
                .anyMatch(c -> c.getName().equalsIgnoreCase(name));
        if (!exists) {
            categories.add(new GPSCategory(name));
            save();
        }
    }

    public static void removeCategory(GPSCategory category) {
        categories.remove(category);
        save();
    }

    public static void addEntryToCategory(GPSCategory category,
                                          GPSHistoryEntry entry) {
        category.addEntry(entry);
        save();
    }

    public static void removeEntryFromCategory(GPSCategory category,
                                               GPSHistoryEntry entry) {
        category.removeEntry(entry);
        save();
    }

    public static List<GPSCategory> getAll() {
        return new ArrayList<>(categories);
    }

    // ── Speichern / Laden ─────────────────────────────────────────────────
    private static Path getSavePath() {
        return Minecraft.getInstance()
                .gameDirectory.toPath()
                .resolve("simplegps_categories.json");
    }

    public static void save() {
        JsonArray arr = new JsonArray();
        for (GPSCategory cat : categories) {
            JsonObject obj = new JsonObject();
            obj.addProperty("name", cat.getName());
            JsonArray entries = new JsonArray();
            for (GPSHistoryEntry e : cat.getEntries()) {
                JsonObject entry = new JsonObject();
                entry.addProperty("name", e.name());
                entry.addProperty("x", e.x());
                entry.addProperty("y", e.y());
                entry.addProperty("z", e.z());
                entry.addProperty("date", e.date());
                entry.addProperty("dimension", e.dimension());
                entries.add(entry);
            }
            obj.add("entries", entries);
            arr.add(obj);
        }
        try {
            Files.writeString(getSavePath(),
                    new GsonBuilder().setPrettyPrinting().create().toJson(arr));
        } catch (IOException ex) {
            ex.printStackTrace();
        }
    }

    public static void load() {
        Path path = getSavePath();
        if (!Files.exists(path)) return;
        try {
            String json = Files.readString(path);
            JsonArray arr = JsonParser.parseString(json).getAsJsonArray();
            categories.clear();
            for (JsonElement el : arr) {
                JsonObject obj = el.getAsJsonObject();
                GPSCategory cat = new GPSCategory(obj.get("name").getAsString());
                JsonArray entries = obj.getAsJsonArray("entries");
                for (JsonElement entryEl : entries) {
                    JsonObject entry = entryEl.getAsJsonObject();
                    cat.addEntry(new GPSHistoryEntry(
                            entry.get("name").getAsString(),
                            entry.get("x").getAsDouble(),
                            entry.get("y").getAsDouble(),
                            entry.get("z").getAsDouble(),
                            entry.get("date").getAsString(),
                            entry.has("dimension") ? entry.get("dimension").getAsString() : "minecraft:overworld"

                    ));
                }
                categories.add(cat);
            }
        } catch (IOException ex) {
            ex.printStackTrace();
        }
    }
}
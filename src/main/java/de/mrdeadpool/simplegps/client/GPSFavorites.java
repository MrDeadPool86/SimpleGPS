package de.mrdeadpool.simplegps.client;

import com.google.gson.*;
import net.minecraft.client.Minecraft;

import java.io.*;
import java.nio.file.*;
import java.util.ArrayList;
import java.util.List;

public class GPSFavorites {

    private static final int MAX_FAVORITES = 10;
    private static final List<GPSHistoryEntry> favorites = new ArrayList<>();

    public static void add(GPSHistoryEntry entry) {
        // Duplikat prüfen
        boolean exists = favorites.stream().anyMatch(f ->
                f.name().equals(entry.name()) &&
                        f.x() == entry.x() &&
                        f.y() == entry.y() &&
                        f.z() == entry.z() &&
                        f.dimension().equals(entry.dimension())
        );
        if (exists) return;
        if (favorites.size() >= MAX_FAVORITES) return; // voll
        favorites.add(entry);
        save();
    }

    public static void remove(GPSHistoryEntry entry) {
        favorites.remove(entry);
        save();
    }

    public static List<GPSHistoryEntry> getAll() {
        return new ArrayList<>(favorites);
    }

    public static boolean isFavorite(GPSHistoryEntry entry) {
        return favorites.stream().anyMatch(f ->
                f.name().equals(entry.name()) &&
                        f.x() == entry.x() &&
                        f.y() == entry.y() &&
                        f.z() == entry.z() &&
                        f.dimension().equals(entry.dimension())
        );
    }

    public static boolean isFull() {
        return favorites.size() >= MAX_FAVORITES;
    }

    // ── Speichern / Laden ─────────────────────────────────────────────────
    private static Path getSavePath() {
        return Minecraft.getInstance()
                .gameDirectory.toPath()
                .resolve("simplegps_favorites.json");
    }

    public static void save() {
        JsonArray arr = new JsonArray();
        for (GPSHistoryEntry e : favorites) {
            JsonObject obj = new JsonObject();
            obj.addProperty("name", e.name());
            obj.addProperty("x", e.x());
            obj.addProperty("y", e.y());
            obj.addProperty("z", e.z());
            obj.addProperty("date", e.date());
            obj.addProperty("dimension", e.dimension());
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
            favorites.clear();
            for (JsonElement el : arr) {
                JsonObject obj = el.getAsJsonObject();

                String dimension = obj.has("dimension")
                        ? obj.get("dimension").getAsString()
                        : "minecraft:overworld";

                favorites.add(new GPSHistoryEntry(
                        obj.get("name").getAsString(),
                        obj.get("x").getAsDouble(),
                        obj.get("y").getAsDouble(),
                        obj.get("z").getAsDouble(),
                        obj.get("date").getAsString(),
                        dimension
                ));
            }
        } catch (IOException ex) {
            ex.printStackTrace();
        }
    }
}
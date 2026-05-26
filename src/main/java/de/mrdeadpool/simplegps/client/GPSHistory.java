package de.mrdeadpool.simplegps.client;

import com.google.gson.*;
import net.minecraft.client.Minecraft;

import java.io.*;
import java.nio.file.*;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayDeque;
import java.util.ArrayList;
import java.util.Deque;
import java.util.List;

public class GPSHistory {

    private static final int MAX_ENTRIES = 50;
    private static final DateTimeFormatter FORMATTER =
            DateTimeFormatter.ofPattern("dd.MM HH:mm");

    // Deque = automatisch FIFO (ältester fliegt raus)
    private static final Deque<GPSHistoryEntry> entries = new ArrayDeque<>();

    // ── Eintrag hinzufügen ────────────────────────────────────────────────
    public static void add(String name, double x, double y, double z) {
        String date = LocalDateTime.now().format(FORMATTER);

        String dimension = "minecraft:overworld";
        Minecraft mc = Minecraft.getInstance();
        if (mc.level != null) {
            dimension = mc.level.dimension().location().toString();
        }

        final String dim = dimension;

        GPSHistoryEntry entry = new GPSHistoryEntry(name, x, y, z, date, dim);

        // Duplikate (gleicher Name + Koordinaten) vorher entfernen
        entries.removeIf(e ->
                e.name().equals(name) &&
                        e.x() == x &&
                        e.y() == y &&
                        e.z() == z &&
                        e.dimension().equals(dim)
        );

        // Ältesten löschen wenn voll
        if (entries.size() >= MAX_ENTRIES) {
            entries.pollFirst();
        }

        entries.addLast(entry);
        save();
    }

    public static void remove(GPSHistoryEntry entry) {
        entries.remove(entry);
        save();
    }

    public static List<GPSHistoryEntry> getAll() {
        // Neueste zuerst
        List<GPSHistoryEntry> list = new ArrayList<>(entries);
        java.util.Collections.reverse(list);
        return list;
    }

    // ── Speichern / Laden ─────────────────────────────────────────────────
    private static Path getSavePath() {
        return Minecraft.getInstance()
                .gameDirectory.toPath()
                .resolve("simplegps_history.json");
    }

    public static void save() {
        JsonArray arr = new JsonArray();
        for (GPSHistoryEntry e : entries) {
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
            entries.clear();
            for (JsonElement el : arr) {
                JsonObject obj = el.getAsJsonObject();

                String dimension = obj.has("dimension")
                        ? obj.get("dimension").getAsString()
                        : "minecraft:overworld";

                entries.addLast(new GPSHistoryEntry(
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
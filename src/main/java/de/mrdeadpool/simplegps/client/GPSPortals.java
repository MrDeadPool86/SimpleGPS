package de.mrdeadpool.simplegps.client;

import com.google.gson.*;
import net.minecraft.client.Minecraft;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;

public class GPSPortals {

    private static final List<GPSPortalLink> portals = new ArrayList<>();

    public static void add(GPSPortalLink link) {
        boolean exists = portals.stream().anyMatch(p ->
                p.name().equals(link.name()) &&
                        p.fromDimension().equals(link.fromDimension()) &&
                        p.fromX() == link.fromX() &&
                        p.fromY() == link.fromY() &&
                        p.fromZ() == link.fromZ() &&
                        p.toDimension().equals(link.toDimension()) &&
                        p.toX() == link.toX() &&
                        p.toY() == link.toY() &&
                        p.toZ() == link.toZ()
        );

        if (!exists) {
            portals.add(link);
            save();
        }
    }

    public static void remove(GPSPortalLink link) {
        portals.remove(link);
        save();
    }

    public static List<GPSPortalLink> getAll() {
        return new ArrayList<>(portals);
    }

    public static void clear() {
        portals.clear();
        save();
    }

    public static GPSPortalLink findBestPortal(String fromDimension, String toDimension,
                                               double playerX, double playerY, double playerZ) {
        GPSPortalLink best = null;
        double bestDistSq = Double.MAX_VALUE;

        for (GPSPortalLink link : portals) {
            if (!link.fromDimension().equals(fromDimension)) continue;
            if (!link.toDimension().equals(toDimension)) continue;

            double dx = link.fromX() - playerX;
            double dy = link.fromY() - playerY;
            double dz = link.fromZ() - playerZ;
            double distSq = dx * dx + dy * dy + dz * dz;

            if (distSq < bestDistSq) {
                bestDistSq = distSq;
                best = link;
            }
        }

        return best;
    }

    private static Path getSavePath() {
        return Minecraft.getInstance()
                .gameDirectory.toPath()
                .resolve("simplegps_portals.json");
    }

    public static void save() {
        JsonArray arr = new JsonArray();

        for (GPSPortalLink p : portals) {
            JsonObject obj = new JsonObject();
            obj.addProperty("name", p.name());

            obj.addProperty("fromDimension", p.fromDimension());
            obj.addProperty("fromX", p.fromX());
            obj.addProperty("fromY", p.fromY());
            obj.addProperty("fromZ", p.fromZ());

            obj.addProperty("toDimension", p.toDimension());
            obj.addProperty("toX", p.toX());
            obj.addProperty("toY", p.toY());
            obj.addProperty("toZ", p.toZ());

            arr.add(obj);
        }

        try {
            Files.writeString(
                    getSavePath(),
                    new GsonBuilder().setPrettyPrinting().create().toJson(arr)
            );
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

            portals.clear();

            for (JsonElement el : arr) {
                JsonObject obj = el.getAsJsonObject();

                portals.add(new GPSPortalLink(
                        obj.get("name").getAsString(),

                        obj.get("fromDimension").getAsString(),
                        obj.get("fromX").getAsDouble(),
                        obj.get("fromY").getAsDouble(),
                        obj.get("fromZ").getAsDouble(),

                        obj.get("toDimension").getAsString(),
                        obj.get("toX").getAsDouble(),
                        obj.get("toY").getAsDouble(),
                        obj.get("toZ").getAsDouble()
                ));
            }

        } catch (Exception ex) {
            ex.printStackTrace();
        }
    }
}
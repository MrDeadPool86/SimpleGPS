package de.mrdeadpool.simplegps.client;

import com.google.gson.*;
import net.minecraft.client.Minecraft;

import java.io.*;
import java.nio.file.*;
import java.util.ArrayList;
import java.util.List;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class GPSRoutes {
    private static final Logger LOGGER = LoggerFactory.getLogger(GPSRoutes.class);

    private static JsonObject routeToJson(GPSRoute route) {
        JsonObject obj = new JsonObject();
        obj.addProperty("name", route.getName());

        JsonArray waypoints = new JsonArray();
        for (GPSHistoryEntry e : route.getWaypoints()) {
            JsonObject wp = new JsonObject();
            wp.addProperty("name", e.name());
            wp.addProperty("x", e.x());
            wp.addProperty("y", e.y());
            wp.addProperty("z", e.z());
            wp.addProperty("date", e.date());
            waypoints.add(wp);
        }

        obj.add("waypoints", waypoints);
        return obj;
    }

    private static final List<GPSRoute> routes = new ArrayList<>();

    public static GPSRoute addRoute(String name) {
        for (GPSRoute route : routes) {
            if (route.getName().equalsIgnoreCase(name)) {
                return route; // vorhandene Route zurückgeben
            }
        }

        GPSRoute newRoute = new GPSRoute(name);
        routes.add(newRoute);
        save();
        return newRoute;
    }

    public static void removeRoute(GPSRoute route) {
        routes.remove(route);
        save();
    }

    public static void addWaypointToRoute(GPSRoute route, GPSHistoryEntry entry) {
        route.addWaypoint(entry);
        save();
    }

    public static void removeWaypointFromRoute(GPSRoute route, GPSHistoryEntry entry) {
        route.removeWaypoint(entry);
        save();
    }

    public static List<GPSRoute> getAll() {
        return new ArrayList<>(routes);
    }

    // ── Speichern / Laden ─────────────────────────────────────────────────
    private static Path getSavePath() {
        return Minecraft.getInstance()
                .gameDirectory.toPath()
                .resolve("simplegps_routes.json");
    }

    public static void save() {
        JsonArray arr = new JsonArray();

        for (GPSRoute route : routes) {
            arr.add(routeToJson(route));
        }

        try {
            Files.writeString(getSavePath(),
                    new GsonBuilder().setPrettyPrinting().create().toJson(arr));
        } catch (IOException ex) {
            LOGGER.error("Fehler beim Speichern der Routen!", ex);
        }
    }

    private static GPSRoute routeFromJson(JsonObject obj) {
        GPSRoute route = new GPSRoute(obj.get("name").getAsString());

        JsonArray waypoints = obj.getAsJsonArray("waypoints");
        for (JsonElement wpEl : waypoints) {
            JsonObject wp = wpEl.getAsJsonObject();
            route.addWaypoint(new GPSHistoryEntry(
                    wp.get("name").getAsString(),
                    wp.get("x").getAsDouble(),
                    wp.get("y").getAsDouble(),
                    wp.get("z").getAsDouble(),
                    wp.get("date").getAsString(),
                    wp.has("dimension") ?
                    wp.get("dimension").getAsString() : "minecraft:overwold"
            ));
        }

        return route;
    }

    public static void load() {
        Path path = getSavePath();
        if (!Files.exists(path)) return;

        try {
            String json = Files.readString(path);
            JsonArray arr = JsonParser.parseString(json).getAsJsonArray();
            routes.clear();

            for (JsonElement el : arr) {
                JsonObject obj = el.getAsJsonObject();
                routes.add(routeFromJson(obj));
            }
        } catch (IOException ex) {
            LOGGER.error("Fehler beim Laden der Routen!", ex);
        }
    }
}
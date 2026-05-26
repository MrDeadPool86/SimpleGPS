package de.mrdeadpool.simplegps;

import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

public class GPSData {
    public record GPSTarget(String name, double x, double y, double z) {
        public double distanceSq(double px, double py, double pz) {
            double dx = px - x, dy = py - y, dz = pz - z;
            return dx*dx + dy*dy + dz*dz;
        }
    }
    private static final Map<UUID, GPSTarget> targets = new HashMap<>();
    public static void set(UUID id, String name, double x, double y, double z) {
        targets.put(id, new GPSTarget(name, x, y, z));
    }
    public static void remove(UUID id) { targets.remove(id); }
    public static GPSTarget get(UUID id) { return targets.get(id); }
    public static boolean has(UUID id) { return targets.containsKey(id); }
}
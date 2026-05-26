package de.mrdeadpool.simplegps.client;

import de.mrdeadpool.simplegps.network.GPSPacket;

public class GPSClientState {

    public static boolean active = false;
    public static GPSPacket current = null;

    public static void set(GPSPacket pkt) {
        active = pkt.active();
        current = pkt;
    }

    public static void clear() {
        active = false;
        current = null;
    }

    public static boolean isActive() {
        return active && current != null;
    }

    public static String getName() {
        return current != null ? current.name() : "";
    }

    public static double getX() {
        return current != null ? current.x() : 0;
    }

    public static double getY() {
        return current != null ? current.y() : 0;
    }

    public static double getZ() {
        return current != null ? current.z() : 0;
    }

    public static String getColor() {
        return current != null ? current.color() : "red";
    }

    public static String getDimension() {
        return current != null ? current.dimension() : "minecraft:overworld";
    }
}
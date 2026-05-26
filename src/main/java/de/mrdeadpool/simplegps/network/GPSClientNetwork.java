package de.mrdeadpool.simplegps.network;

import de.mrdeadpool.simplegps.client.GPSClientData;
import net.neoforged.neoforge.network.PacketDistributor;

public class GPSClientNetwork {

    public static void sendDelete() {
        PacketDistributor.sendToServer(new GPSDeletePacket());
    }

    public static void sendSetWaypoint(String name, double x, double y, double z, String color, String dimension) {
        GPSClientData.set(name, x, y, z, color, dimension);
    }
}
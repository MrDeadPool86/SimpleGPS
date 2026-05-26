package de.mrdeadpool.simplegps.network;

import de.mrdeadpool.simplegps.client.GPSClientData;
import de.mrdeadpool.simplegps.client.GPSHistory;
import net.neoforged.neoforge.network.handling.IPayloadContext;

public class GPSClientHandler {

    public static void handle(GPSPacket pkt, IPayloadContext ctx) {
        ctx.enqueueWork(() -> {

            // 🔥 DEBUG HIER EINBAUEN
            System.out.println("GPS PACKET: active=" + pkt.active() + " name=" + pkt.name());

            if (!pkt.active()) {
                GPSClientData.clear();
                return;
            }

            GPSClientData.set(
                    pkt.name(),
                    pkt.x(),
                    pkt.y(),
                    pkt.z(),
                    pkt.color(),
                    pkt.dimension()
            );

            GPSHistory.add(pkt.name(), pkt.x(), pkt.y(), pkt.z());
        });
    }
}
package de.mrdeadpool.simplegps.network;
import de.mrdeadpool.simplegps.GPSData;
import net.minecraft.server.level.ServerPlayer;
import net.neoforged.neoforge.network.handling.IPayloadContext;

public class GPSDeleteServerHandler {
    public static void handle(GPSDeletePacket pkt, IPayloadContext ctx) {
        ctx.enqueueWork(() -> {
            if (ctx.player() instanceof ServerPlayer sp)
                GPSData.remove(sp.getUUID());
        });
    }
}
package de.mrdeadpool.simplegps.network;

import net.minecraft.server.level.ServerPlayer;
import net.neoforged.bus.api.IEventBus;
import net.neoforged.neoforge.network.PacketDistributor;
import net.neoforged.neoforge.network.event.RegisterPayloadHandlersEvent;
import net.neoforged.neoforge.network.registration.PayloadRegistrar;

public class GPSNetwork {
    public static void register(IEventBus bus) {
        bus.addListener(GPSNetwork::onRegister);
    }
    private static void onRegister(RegisterPayloadHandlersEvent event) {
        PayloadRegistrar r = event.registrar("simplegps");
        r.playToClient(GPSPacket.TYPE, GPSPacket.CODEC,
                GPSClientHandler::handle);
        r.playToServer(GPSDeletePacket.TYPE, GPSDeletePacket.CODEC,
                GPSDeleteServerHandler::handle);
    }
    public static void sendToPlayer(ServerPlayer p, GPSPacket pkt) {
        PacketDistributor.sendToPlayer(p, pkt);
    }
}
package de.mrdeadpool.simplegps.network;

import io.netty.buffer.ByteBuf;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.resources.ResourceLocation;
import de.mrdeadpool.simplegps.SimpleGPS;

public record GPSDeletePacket() implements CustomPacketPayload {
    public static final Type<GPSDeletePacket> TYPE = new Type<>(
            ResourceLocation.fromNamespaceAndPath(SimpleGPS.MOD_ID, "gps_delete"));
    public static final StreamCodec<ByteBuf, GPSDeletePacket> CODEC =
            StreamCodec.unit(new GPSDeletePacket());
    @Override public Type<? extends CustomPacketPayload> type() { return TYPE; }
}
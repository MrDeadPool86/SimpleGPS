package de.mrdeadpool.simplegps.network;

import io.netty.buffer.ByteBuf;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.resources.ResourceLocation;
import de.mrdeadpool.simplegps.SimpleGPS;

public record GPSPacket(boolean active, String name,
                        double x, double y, double z,
                        String color, String dimension)
        implements CustomPacketPayload {

    public static final Type<GPSPacket> TYPE = new Type<>(
            ResourceLocation.fromNamespaceAndPath(SimpleGPS.MOD_ID, "gps_packet"));

    public static final StreamCodec<ByteBuf, GPSPacket> CODEC =
            new StreamCodec<>() {
                @Override
                public GPSPacket decode(ByteBuf buf) {
                    boolean active = ByteBufCodecs.BOOL.decode(buf);
                    String name    = ByteBufCodecs.STRING_UTF8.decode(buf);
                    double x       = ByteBufCodecs.DOUBLE.decode(buf);
                    double y       = ByteBufCodecs.DOUBLE.decode(buf);
                    double z       = ByteBufCodecs.DOUBLE.decode(buf);
                    String color   = ByteBufCodecs.STRING_UTF8.decode(buf);
                    String dimension = ByteBufCodecs.STRING_UTF8.decode(buf);
                    return new GPSPacket(active, name, x, y, z, color, dimension);
                }

                @Override
                public void encode(ByteBuf buf, GPSPacket pkt) {
                    ByteBufCodecs.BOOL.encode(buf, pkt.active());
                    ByteBufCodecs.STRING_UTF8.encode(buf, pkt.name());
                    ByteBufCodecs.DOUBLE.encode(buf, pkt.x());
                    ByteBufCodecs.DOUBLE.encode(buf, pkt.y());
                    ByteBufCodecs.DOUBLE.encode(buf, pkt.z());
                    ByteBufCodecs.STRING_UTF8.encode(buf, pkt.color());
                    ByteBufCodecs.STRING_UTF8.encode(buf, pkt.dimension());
                }
            };

    @Override public Type<? extends CustomPacketPayload> type() { return TYPE; }
}
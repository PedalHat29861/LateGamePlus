/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  io.netty.buffer.ByteBuf
 */
package net.minecraft.network.packet.s2c.play;

import io.netty.buffer.ByteBuf;
import java.lang.invoke.MethodHandle;
import java.lang.runtime.ObjectMethods;
import java.util.Optional;
import net.minecraft.network.codec.PacketCodec;
import net.minecraft.network.codec.PacketCodecs;
import net.minecraft.network.listener.ClientPlayPacketListener;
import net.minecraft.network.packet.Packet;
import net.minecraft.network.packet.PacketType;
import net.minecraft.network.packet.PlayPackets;
import net.minecraft.text.Text;
import net.minecraft.text.TextCodecs;

public record ServerMetadataS2CPacket(Text description, Optional<byte[]> favicon) implements Packet<ClientPlayPacketListener>
{
    public static final PacketCodec<ByteBuf, ServerMetadataS2CPacket> CODEC = PacketCodec.tuple(TextCodecs.PACKET_CODEC, ServerMetadataS2CPacket::description, PacketCodecs.BYTE_ARRAY.collect(PacketCodecs::optional), ServerMetadataS2CPacket::favicon, ServerMetadataS2CPacket::new);

    @Override
    public PacketType<ServerMetadataS2CPacket> getPacketType() {
        return PlayPackets.SERVER_DATA;
    }

    @Override
    public void apply(ClientPlayPacketListener clientPlayPacketListener) {
        clientPlayPacketListener.onServerMetadata(this);
    }

    @Override
    public final String toString() {
        return ObjectMethods.bootstrap("toString", new MethodHandle[]{ServerMetadataS2CPacket.class, "motd;iconBytes", "description", "favicon"}, this);
    }

    @Override
    public final int hashCode() {
        return (int)ObjectMethods.bootstrap("hashCode", new MethodHandle[]{ServerMetadataS2CPacket.class, "motd;iconBytes", "description", "favicon"}, this);
    }

    @Override
    public final boolean equals(Object object) {
        return (boolean)ObjectMethods.bootstrap("equals", new MethodHandle[]{ServerMetadataS2CPacket.class, "motd;iconBytes", "description", "favicon"}, this, object);
    }
}


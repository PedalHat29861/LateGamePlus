/*
 * Decompiled with CFR 0.152.
 */
package net.minecraft.network.packet.s2c.play;

import net.minecraft.network.RegistryByteBuf;
import net.minecraft.network.codec.PacketCodec;
import net.minecraft.network.listener.ClientPlayPacketListener;
import net.minecraft.network.packet.Packet;
import net.minecraft.network.packet.PacketType;
import net.minecraft.network.packet.PlayPackets;
import net.minecraft.util.math.ChunkPos;
import net.minecraft.world.debug.DebugSubscriptionType;

public record ChunkValueDebugS2CPacket(ChunkPos chunkPos, DebugSubscriptionType.OptionalValue<?> update) implements Packet<ClientPlayPacketListener>
{
    public static final PacketCodec<RegistryByteBuf, ChunkValueDebugS2CPacket> PACKET_CODEC = PacketCodec.tuple(ChunkPos.PACKET_CODEC, ChunkValueDebugS2CPacket::chunkPos, DebugSubscriptionType.OptionalValue.PACKET_CODEC, ChunkValueDebugS2CPacket::update, ChunkValueDebugS2CPacket::new);

    @Override
    public PacketType<ChunkValueDebugS2CPacket> getPacketType() {
        return PlayPackets.CHUNK_VALUE_DEBUG;
    }

    @Override
    public void apply(ClientPlayPacketListener clientPlayPacketListener) {
        clientPlayPacketListener.onChunkValueDebug(this);
    }
}


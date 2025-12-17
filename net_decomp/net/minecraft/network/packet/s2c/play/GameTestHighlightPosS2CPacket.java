/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  io.netty.buffer.ByteBuf
 */
package net.minecraft.network.packet.s2c.play;

import io.netty.buffer.ByteBuf;
import net.minecraft.network.codec.PacketCodec;
import net.minecraft.network.listener.ClientPlayPacketListener;
import net.minecraft.network.packet.Packet;
import net.minecraft.network.packet.PacketType;
import net.minecraft.network.packet.PlayPackets;
import net.minecraft.util.math.BlockPos;

public record GameTestHighlightPosS2CPacket(BlockPos absolutePos, BlockPos relativePos) implements Packet<ClientPlayPacketListener>
{
    public static final PacketCodec<ByteBuf, GameTestHighlightPosS2CPacket> PACKET_CODEC = PacketCodec.tuple(BlockPos.PACKET_CODEC, GameTestHighlightPosS2CPacket::absolutePos, BlockPos.PACKET_CODEC, GameTestHighlightPosS2CPacket::relativePos, GameTestHighlightPosS2CPacket::new);

    @Override
    public PacketType<GameTestHighlightPosS2CPacket> getPacketType() {
        return PlayPackets.GAME_TEST_HIGHLIGHT_POS;
    }

    @Override
    public void apply(ClientPlayPacketListener clientPlayPacketListener) {
        clientPlayPacketListener.onGameTestHighlightPos(this);
    }
}


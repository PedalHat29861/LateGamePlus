/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  io.netty.buffer.ByteBuf
 */
package net.minecraft.network.packet.c2s.play;

import io.netty.buffer.ByteBuf;
import net.minecraft.network.codec.PacketCodec;
import net.minecraft.network.codec.PacketCodecs;
import net.minecraft.network.listener.ServerPlayPacketListener;
import net.minecraft.network.packet.Packet;
import net.minecraft.network.packet.PacketType;
import net.minecraft.network.packet.PlayPackets;

public record PickItemFromEntityC2SPacket(int id, boolean includeData) implements Packet<ServerPlayPacketListener>
{
    public static final PacketCodec<ByteBuf, PickItemFromEntityC2SPacket> CODEC = PacketCodec.tuple(PacketCodecs.VAR_INT, PickItemFromEntityC2SPacket::id, PacketCodecs.BOOLEAN, PickItemFromEntityC2SPacket::includeData, PickItemFromEntityC2SPacket::new);

    @Override
    public PacketType<PickItemFromEntityC2SPacket> getPacketType() {
        return PlayPackets.PICK_ITEM_FROM_ENTITY;
    }

    @Override
    public void apply(ServerPlayPacketListener serverPlayPacketListener) {
        serverPlayPacketListener.onPickItemFromEntity(this);
    }
}


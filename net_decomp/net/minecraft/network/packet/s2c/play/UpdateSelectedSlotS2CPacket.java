/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  io.netty.buffer.ByteBuf
 */
package net.minecraft.network.packet.s2c.play;

import io.netty.buffer.ByteBuf;
import net.minecraft.network.codec.PacketCodec;
import net.minecraft.network.codec.PacketCodecs;
import net.minecraft.network.listener.ClientPlayPacketListener;
import net.minecraft.network.packet.Packet;
import net.minecraft.network.packet.PacketType;
import net.minecraft.network.packet.PlayPackets;

public record UpdateSelectedSlotS2CPacket(int slot) implements Packet<ClientPlayPacketListener>
{
    public static final PacketCodec<ByteBuf, UpdateSelectedSlotS2CPacket> CODEC = PacketCodec.tuple(PacketCodecs.VAR_INT, UpdateSelectedSlotS2CPacket::slot, UpdateSelectedSlotS2CPacket::new);

    @Override
    public PacketType<UpdateSelectedSlotS2CPacket> getPacketType() {
        return PlayPackets.SET_CARRIED_ITEM_S2C;
    }

    @Override
    public void apply(ClientPlayPacketListener clientPlayPacketListener) {
        clientPlayPacketListener.onUpdateSelectedSlot(this);
    }
}


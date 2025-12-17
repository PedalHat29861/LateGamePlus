/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  io.netty.buffer.ByteBuf
 */
package net.minecraft.network.packet.s2c.common;

import io.netty.buffer.ByteBuf;
import net.minecraft.dialog.type.Dialog;
import net.minecraft.network.RegistryByteBuf;
import net.minecraft.network.codec.PacketCodec;
import net.minecraft.network.listener.ClientCommonPacketListener;
import net.minecraft.network.packet.CommonPackets;
import net.minecraft.network.packet.Packet;
import net.minecraft.network.packet.PacketType;
import net.minecraft.registry.entry.RegistryEntry;

public record ShowDialogS2CPacket(RegistryEntry<Dialog> dialog) implements Packet<ClientCommonPacketListener>
{
    public static final PacketCodec<RegistryByteBuf, ShowDialogS2CPacket> REGISTRY_CODEC = PacketCodec.tuple(Dialog.ENTRY_PACKET_CODEC, ShowDialogS2CPacket::dialog, ShowDialogS2CPacket::new);
    public static final PacketCodec<ByteBuf, ShowDialogS2CPacket> CODEC = PacketCodec.tuple(Dialog.PACKET_CODEC.xmap(RegistryEntry::of, RegistryEntry::value), ShowDialogS2CPacket::dialog, ShowDialogS2CPacket::new);

    @Override
    public PacketType<ShowDialogS2CPacket> getPacketType() {
        return CommonPackets.SHOW_DIALOG;
    }

    @Override
    public void apply(ClientCommonPacketListener clientCommonPacketListener) {
        clientCommonPacketListener.onShowDialog(this);
    }
}


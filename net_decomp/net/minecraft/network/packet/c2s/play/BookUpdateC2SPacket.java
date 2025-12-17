/*
 * Decompiled with CFR 0.152.
 */
package net.minecraft.network.packet.c2s.play;

import java.util.List;
import java.util.Optional;
import net.minecraft.network.PacketByteBuf;
import net.minecraft.network.codec.PacketCodec;
import net.minecraft.network.codec.PacketCodecs;
import net.minecraft.network.listener.ServerPlayPacketListener;
import net.minecraft.network.packet.Packet;
import net.minecraft.network.packet.PacketType;
import net.minecraft.network.packet.PlayPackets;

public record BookUpdateC2SPacket(int slot, List<String> pages, Optional<String> title) implements Packet<ServerPlayPacketListener>
{
    public static final PacketCodec<PacketByteBuf, BookUpdateC2SPacket> CODEC = PacketCodec.tuple(PacketCodecs.VAR_INT, BookUpdateC2SPacket::slot, PacketCodecs.string(1024).collect(PacketCodecs.toList(100)), BookUpdateC2SPacket::pages, PacketCodecs.string(32).collect(PacketCodecs::optional), BookUpdateC2SPacket::title, BookUpdateC2SPacket::new);

    public BookUpdateC2SPacket {
        pages = List.copyOf(pages);
    }

    @Override
    public PacketType<BookUpdateC2SPacket> getPacketType() {
        return PlayPackets.EDIT_BOOK;
    }

    @Override
    public void apply(ServerPlayPacketListener serverPlayPacketListener) {
        serverPlayPacketListener.onBookUpdate(this);
    }
}


/*
 * Decompiled with CFR 0.152.
 */
package net.minecraft.network.packet.s2c.play;

import java.util.List;
import net.minecraft.network.RegistryByteBuf;
import net.minecraft.network.codec.PacketCodec;
import net.minecraft.network.codec.PacketCodecs;
import net.minecraft.network.listener.ClientPlayPacketListener;
import net.minecraft.network.packet.Packet;
import net.minecraft.network.packet.PacketType;
import net.minecraft.network.packet.PlayPackets;
import net.minecraft.recipe.RecipeDisplayEntry;

public record RecipeBookAddS2CPacket(List<Entry> entries, boolean replace) implements Packet<ClientPlayPacketListener>
{
    public static final PacketCodec<RegistryByteBuf, RecipeBookAddS2CPacket> CODEC = PacketCodec.tuple(Entry.PACKET_CODEC.collect(PacketCodecs.toList()), RecipeBookAddS2CPacket::entries, PacketCodecs.BOOLEAN, RecipeBookAddS2CPacket::replace, RecipeBookAddS2CPacket::new);

    @Override
    public PacketType<RecipeBookAddS2CPacket> getPacketType() {
        return PlayPackets.RECIPE_BOOK_ADD;
    }

    @Override
    public void apply(ClientPlayPacketListener clientPlayPacketListener) {
        clientPlayPacketListener.onRecipeBookAdd(this);
    }

    public record Entry(RecipeDisplayEntry contents, byte flags) {
        public static final byte SHOW_NOTIFICATION = 1;
        public static final byte HIGHLIGHTED = 2;
        public static final PacketCodec<RegistryByteBuf, Entry> PACKET_CODEC = PacketCodec.tuple(RecipeDisplayEntry.PACKET_CODEC, Entry::contents, PacketCodecs.BYTE, Entry::flags, Entry::new);

        public Entry(RecipeDisplayEntry display, boolean showNotification, boolean highlighted) {
            this(display, (byte)((showNotification ? 1 : 0) | (highlighted ? 2 : 0)));
        }

        public boolean shouldShowNotification() {
            return (this.flags & 1) != 0;
        }

        public boolean isHighlighted() {
            return (this.flags & 2) != 0;
        }
    }
}


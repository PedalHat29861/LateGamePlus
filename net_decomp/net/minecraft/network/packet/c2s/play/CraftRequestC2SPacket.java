/*
 * Decompiled with CFR 0.152.
 */
package net.minecraft.network.packet.c2s.play;

import java.lang.invoke.MethodHandle;
import java.lang.runtime.ObjectMethods;
import net.minecraft.network.PacketByteBuf;
import net.minecraft.network.codec.PacketCodec;
import net.minecraft.network.codec.PacketCodecs;
import net.minecraft.network.listener.ServerPlayPacketListener;
import net.minecraft.network.packet.Packet;
import net.minecraft.network.packet.PacketType;
import net.minecraft.network.packet.PlayPackets;
import net.minecraft.recipe.NetworkRecipeId;

public record CraftRequestC2SPacket(int syncId, NetworkRecipeId recipeId, boolean craftAll) implements Packet<ServerPlayPacketListener>
{
    public static final PacketCodec<PacketByteBuf, CraftRequestC2SPacket> CODEC = PacketCodec.tuple(PacketCodecs.SYNC_ID, CraftRequestC2SPacket::syncId, NetworkRecipeId.PACKET_CODEC, CraftRequestC2SPacket::recipeId, PacketCodecs.BOOLEAN, CraftRequestC2SPacket::craftAll, CraftRequestC2SPacket::new);

    @Override
    public PacketType<CraftRequestC2SPacket> getPacketType() {
        return PlayPackets.PLACE_RECIPE;
    }

    @Override
    public void apply(ServerPlayPacketListener serverPlayPacketListener) {
        serverPlayPacketListener.onCraftRequest(this);
    }

    @Override
    public final String toString() {
        return ObjectMethods.bootstrap("toString", new MethodHandle[]{CraftRequestC2SPacket.class, "containerId;recipe;useMaxItems", "syncId", "recipeId", "craftAll"}, this);
    }

    @Override
    public final int hashCode() {
        return (int)ObjectMethods.bootstrap("hashCode", new MethodHandle[]{CraftRequestC2SPacket.class, "containerId;recipe;useMaxItems", "syncId", "recipeId", "craftAll"}, this);
    }

    @Override
    public final boolean equals(Object object) {
        return (boolean)ObjectMethods.bootstrap("equals", new MethodHandle[]{CraftRequestC2SPacket.class, "containerId;recipe;useMaxItems", "syncId", "recipeId", "craftAll"}, this, object);
    }
}


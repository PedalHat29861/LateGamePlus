/*
 * Decompiled with CFR 0.152.
 */
package net.minecraft.network.packet.c2s.play;

import java.lang.invoke.MethodHandle;
import java.lang.runtime.ObjectMethods;
import net.minecraft.network.PacketByteBuf;
import net.minecraft.network.codec.PacketCodec;
import net.minecraft.network.listener.ServerPlayPacketListener;
import net.minecraft.network.packet.Packet;
import net.minecraft.network.packet.PacketType;
import net.minecraft.network.packet.PlayPackets;
import net.minecraft.recipe.NetworkRecipeId;

public record RecipeBookDataC2SPacket(NetworkRecipeId recipeId) implements Packet<ServerPlayPacketListener>
{
    public static final PacketCodec<PacketByteBuf, RecipeBookDataC2SPacket> CODEC = PacketCodec.tuple(NetworkRecipeId.PACKET_CODEC, RecipeBookDataC2SPacket::recipeId, RecipeBookDataC2SPacket::new);

    @Override
    public PacketType<RecipeBookDataC2SPacket> getPacketType() {
        return PlayPackets.RECIPE_BOOK_SEEN_RECIPE;
    }

    @Override
    public void apply(ServerPlayPacketListener serverPlayPacketListener) {
        serverPlayPacketListener.onRecipeBookData(this);
    }

    @Override
    public final String toString() {
        return ObjectMethods.bootstrap("toString", new MethodHandle[]{RecipeBookDataC2SPacket.class, "recipe", "recipeId"}, this);
    }

    @Override
    public final int hashCode() {
        return (int)ObjectMethods.bootstrap("hashCode", new MethodHandle[]{RecipeBookDataC2SPacket.class, "recipe", "recipeId"}, this);
    }

    @Override
    public final boolean equals(Object object) {
        return (boolean)ObjectMethods.bootstrap("equals", new MethodHandle[]{RecipeBookDataC2SPacket.class, "recipe", "recipeId"}, this, object);
    }
}


/*
 * Decompiled with CFR 0.152.
 */
package net.minecraft.network.packet.s2c.play;

import java.lang.invoke.MethodHandle;
import java.lang.runtime.ObjectMethods;
import net.minecraft.network.RegistryByteBuf;
import net.minecraft.network.codec.PacketCodec;
import net.minecraft.network.codec.PacketCodecs;
import net.minecraft.network.listener.ClientPlayPacketListener;
import net.minecraft.network.packet.Packet;
import net.minecraft.network.packet.PacketType;
import net.minecraft.network.packet.PlayPackets;
import net.minecraft.recipe.display.RecipeDisplay;

public record CraftFailedResponseS2CPacket(int syncId, RecipeDisplay recipeDisplay) implements Packet<ClientPlayPacketListener>
{
    public static final PacketCodec<RegistryByteBuf, CraftFailedResponseS2CPacket> CODEC = PacketCodec.tuple(PacketCodecs.SYNC_ID, CraftFailedResponseS2CPacket::syncId, RecipeDisplay.PACKET_CODEC, CraftFailedResponseS2CPacket::recipeDisplay, CraftFailedResponseS2CPacket::new);

    @Override
    public PacketType<CraftFailedResponseS2CPacket> getPacketType() {
        return PlayPackets.PLACE_GHOST_RECIPE;
    }

    @Override
    public void apply(ClientPlayPacketListener clientPlayPacketListener) {
        clientPlayPacketListener.onCraftFailedResponse(this);
    }

    @Override
    public final String toString() {
        return ObjectMethods.bootstrap("toString", new MethodHandle[]{CraftFailedResponseS2CPacket.class, "containerId;recipeDisplay", "syncId", "recipeDisplay"}, this);
    }

    @Override
    public final int hashCode() {
        return (int)ObjectMethods.bootstrap("hashCode", new MethodHandle[]{CraftFailedResponseS2CPacket.class, "containerId;recipeDisplay", "syncId", "recipeDisplay"}, this);
    }

    @Override
    public final boolean equals(Object object) {
        return (boolean)ObjectMethods.bootstrap("equals", new MethodHandle[]{CraftFailedResponseS2CPacket.class, "containerId;recipeDisplay", "syncId", "recipeDisplay"}, this, object);
    }
}


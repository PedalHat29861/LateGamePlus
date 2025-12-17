/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  io.netty.buffer.ByteBuf
 */
package net.minecraft.recipe;

import io.netty.buffer.ByteBuf;
import net.minecraft.network.codec.PacketCodec;
import net.minecraft.network.codec.PacketCodecs;

public record NetworkRecipeId(int index) {
    public static final PacketCodec<ByteBuf, NetworkRecipeId> PACKET_CODEC = PacketCodec.tuple(PacketCodecs.VAR_INT, NetworkRecipeId::index, NetworkRecipeId::new);
}


/*
 * Decompiled with CFR 0.152.
 */
package net.minecraft.network.packet;

import net.minecraft.network.codec.PacketCodec;

@FunctionalInterface
public interface PacketCodecModifier<B, V, C> {
    public PacketCodec<? super B, V> apply(PacketCodec<? super B, V> var1, C var2);
}


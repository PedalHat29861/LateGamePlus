/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  com.mojang.serialization.MapCodec
 */
package net.minecraft.util.dynamic;

import com.mojang.serialization.MapCodec;

public record CodecHolder<A>(MapCodec<A> codec) {
    public static <A> CodecHolder<A> of(MapCodec<A> mapCodec) {
        return new CodecHolder<A>(mapCodec);
    }
}


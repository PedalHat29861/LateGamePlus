/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  com.mojang.datafixers.kinds.App
 *  com.mojang.datafixers.kinds.Applicative
 *  com.mojang.datafixers.util.Either
 *  com.mojang.serialization.Codec
 *  com.mojang.serialization.codecs.RecordCodecBuilder
 */
package net.minecraft.world.attribute;

import com.mojang.datafixers.kinds.App;
import com.mojang.datafixers.kinds.Applicative;
import com.mojang.datafixers.util.Either;
import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;

public record BlendArgument(float value, float alpha) {
    private static final Codec<BlendArgument> INTERNAL_CODEC = RecordCodecBuilder.create(instance -> instance.group((App)Codec.FLOAT.fieldOf("value").forGetter(BlendArgument::value), (App)Codec.floatRange((float)0.0f, (float)1.0f).optionalFieldOf("alpha", (Object)Float.valueOf(1.0f)).forGetter(BlendArgument::alpha)).apply((Applicative)instance, BlendArgument::new));
    public static final Codec<BlendArgument> CODEC = Codec.either((Codec)Codec.FLOAT, INTERNAL_CODEC).xmap(either -> (BlendArgument)either.map(BlendArgument::new, blend -> blend), blend -> blend.alpha() == 1.0f ? Either.left((Object)Float.valueOf(blend.value())) : Either.right((Object)blend));

    public BlendArgument(float value) {
        this(value, 1.0f);
    }
}


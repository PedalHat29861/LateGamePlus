/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  com.mojang.datafixers.kinds.App
 *  com.mojang.datafixers.kinds.Applicative
 *  com.mojang.serialization.Codec
 *  com.mojang.serialization.codecs.RecordCodecBuilder
 */
package net.minecraft.world.attribute.timeline;

import com.mojang.datafixers.kinds.App;
import com.mojang.datafixers.kinds.Applicative;
import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import net.minecraft.util.dynamic.Codecs;

public record Keyframe<T>(int ticks, T value) {
    public static <T> Codec<Keyframe<T>> createCodec(Codec<T> valueCodec) {
        return RecordCodecBuilder.create(instance -> instance.group((App)Codecs.NON_NEGATIVE_INT.fieldOf("ticks").forGetter(Keyframe::ticks), (App)valueCodec.fieldOf("value").forGetter(Keyframe::value)).apply((Applicative)instance, Keyframe::new));
    }
}


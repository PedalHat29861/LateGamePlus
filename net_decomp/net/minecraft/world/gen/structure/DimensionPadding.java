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
package net.minecraft.world.gen.structure;

import com.mojang.datafixers.kinds.App;
import com.mojang.datafixers.kinds.Applicative;
import com.mojang.datafixers.util.Either;
import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import java.util.function.Function;
import net.minecraft.util.dynamic.Codecs;

public record DimensionPadding(int bottom, int top) {
    private static final Codec<DimensionPadding> OBJECT_CODEC = RecordCodecBuilder.create(instance -> instance.group((App)Codecs.NON_NEGATIVE_INT.lenientOptionalFieldOf("bottom", (Object)0).forGetter(padding -> padding.bottom), (App)Codecs.NON_NEGATIVE_INT.lenientOptionalFieldOf("top", (Object)0).forGetter(padding -> padding.top)).apply((Applicative)instance, DimensionPadding::new));
    public static final Codec<DimensionPadding> CODEC = Codec.either(Codecs.NON_NEGATIVE_INT, OBJECT_CODEC).xmap(either -> (DimensionPadding)either.map(DimensionPadding::new, Function.identity()), padding -> padding.paddedBySameDistance() ? Either.left((Object)padding.bottom) : Either.right((Object)padding));
    public static final DimensionPadding NONE = new DimensionPadding(0);

    public DimensionPadding(int value) {
        this(value, value);
    }

    public boolean paddedBySameDistance() {
        return this.top == this.bottom;
    }
}


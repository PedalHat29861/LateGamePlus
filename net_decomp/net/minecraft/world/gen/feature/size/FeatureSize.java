/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  com.mojang.serialization.Codec
 *  com.mojang.serialization.codecs.RecordCodecBuilder
 */
package net.minecraft.world.gen.feature.size;

import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import java.util.Optional;
import java.util.OptionalInt;
import net.minecraft.registry.Registries;
import net.minecraft.world.gen.feature.size.FeatureSizeType;

public abstract class FeatureSize {
    public static final Codec<FeatureSize> TYPE_CODEC = Registries.FEATURE_SIZE_TYPE.getCodec().dispatch(FeatureSize::getType, FeatureSizeType::getCodec);
    protected static final int field_31522 = 16;
    protected final OptionalInt minClippedHeight;

    protected static <S extends FeatureSize> RecordCodecBuilder<S, OptionalInt> createCodec() {
        return Codec.intRange((int)0, (int)80).optionalFieldOf("min_clipped_height").xmap(minClippedHeight -> minClippedHeight.map(OptionalInt::of).orElse(OptionalInt.empty()), minClippedHeight -> minClippedHeight.isPresent() ? Optional.of(minClippedHeight.getAsInt()) : Optional.empty()).forGetter(size -> size.minClippedHeight);
    }

    public FeatureSize(OptionalInt minClippedHeight) {
        this.minClippedHeight = minClippedHeight;
    }

    protected abstract FeatureSizeType<?> getType();

    public abstract int getRadius(int var1, int var2);

    public OptionalInt getMinClippedHeight() {
        return this.minClippedHeight;
    }
}


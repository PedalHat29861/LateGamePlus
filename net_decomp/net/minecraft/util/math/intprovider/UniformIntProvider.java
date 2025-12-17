/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  com.mojang.datafixers.kinds.App
 *  com.mojang.datafixers.kinds.Applicative
 *  com.mojang.serialization.Codec
 *  com.mojang.serialization.DataResult
 *  com.mojang.serialization.MapCodec
 *  com.mojang.serialization.codecs.RecordCodecBuilder
 */
package net.minecraft.util.math.intprovider;

import com.mojang.datafixers.kinds.App;
import com.mojang.datafixers.kinds.Applicative;
import com.mojang.serialization.Codec;
import com.mojang.serialization.DataResult;
import com.mojang.serialization.MapCodec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import net.minecraft.util.math.MathHelper;
import net.minecraft.util.math.intprovider.IntProvider;
import net.minecraft.util.math.intprovider.IntProviderType;
import net.minecraft.util.math.random.Random;

public class UniformIntProvider
extends IntProvider {
    public static final MapCodec<UniformIntProvider> CODEC = RecordCodecBuilder.mapCodec(instance -> instance.group((App)Codec.INT.fieldOf("min_inclusive").forGetter(provider -> provider.min), (App)Codec.INT.fieldOf("max_inclusive").forGetter(provider -> provider.max)).apply((Applicative)instance, UniformIntProvider::new)).validate(provider -> {
        if (provider.max < provider.min) {
            return DataResult.error(() -> "Max must be at least min, min_inclusive: " + uniformIntProvider.min + ", max_inclusive: " + uniformIntProvider.max);
        }
        return DataResult.success((Object)provider);
    });
    private final int min;
    private final int max;

    private UniformIntProvider(int min, int max) {
        this.min = min;
        this.max = max;
    }

    public static UniformIntProvider create(int min, int max) {
        return new UniformIntProvider(min, max);
    }

    @Override
    public int get(Random random) {
        return MathHelper.nextBetween(random, this.min, this.max);
    }

    @Override
    public int getMin() {
        return this.min;
    }

    @Override
    public int getMax() {
        return this.max;
    }

    @Override
    public IntProviderType<?> getType() {
        return IntProviderType.UNIFORM;
    }

    public String toString() {
        return "[" + this.min + "-" + this.max + "]";
    }
}


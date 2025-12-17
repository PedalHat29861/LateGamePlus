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

public class ClampedNormalIntProvider
extends IntProvider {
    public static final MapCodec<ClampedNormalIntProvider> CODEC = RecordCodecBuilder.mapCodec(instance -> instance.group((App)Codec.FLOAT.fieldOf("mean").forGetter(provider -> Float.valueOf(provider.mean)), (App)Codec.FLOAT.fieldOf("deviation").forGetter(provider -> Float.valueOf(provider.deviation)), (App)Codec.INT.fieldOf("min_inclusive").forGetter(provider -> provider.min), (App)Codec.INT.fieldOf("max_inclusive").forGetter(provider -> provider.max)).apply((Applicative)instance, ClampedNormalIntProvider::new)).validate(provider -> {
        if (provider.max < provider.min) {
            return DataResult.error(() -> "Max must be larger than min: [" + clampedNormalIntProvider.min + ", " + clampedNormalIntProvider.max + "]");
        }
        return DataResult.success((Object)provider);
    });
    private final float mean;
    private final float deviation;
    private final int min;
    private final int max;

    public static ClampedNormalIntProvider of(float mean, float deviation, int min, int max) {
        return new ClampedNormalIntProvider(mean, deviation, min, max);
    }

    private ClampedNormalIntProvider(float mean, float deviation, int min, int max) {
        this.mean = mean;
        this.deviation = deviation;
        this.min = min;
        this.max = max;
    }

    @Override
    public int get(Random random) {
        return ClampedNormalIntProvider.next(random, this.mean, this.deviation, this.min, this.max);
    }

    public static int next(Random random, float mean, float deviation, float min, float max) {
        return (int)MathHelper.clamp(MathHelper.nextGaussian(random, mean, deviation), min, max);
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
        return IntProviderType.CLAMPED_NORMAL;
    }

    public String toString() {
        return "normal(" + this.mean + ", " + this.deviation + ") in [" + this.min + "-" + this.max + "]";
    }
}


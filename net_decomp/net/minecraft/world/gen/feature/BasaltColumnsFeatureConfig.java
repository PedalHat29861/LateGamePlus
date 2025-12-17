/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  com.mojang.datafixers.kinds.App
 *  com.mojang.datafixers.kinds.Applicative
 *  com.mojang.serialization.Codec
 *  com.mojang.serialization.codecs.RecordCodecBuilder
 */
package net.minecraft.world.gen.feature;

import com.mojang.datafixers.kinds.App;
import com.mojang.datafixers.kinds.Applicative;
import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import net.minecraft.util.math.intprovider.IntProvider;
import net.minecraft.world.gen.feature.FeatureConfig;

public class BasaltColumnsFeatureConfig
implements FeatureConfig {
    public static final Codec<BasaltColumnsFeatureConfig> CODEC = RecordCodecBuilder.create(instance -> instance.group((App)IntProvider.createValidatingCodec(0, 3).fieldOf("reach").forGetter(config -> config.reach), (App)IntProvider.createValidatingCodec(1, 10).fieldOf("height").forGetter(config -> config.height)).apply((Applicative)instance, BasaltColumnsFeatureConfig::new));
    private final IntProvider reach;
    private final IntProvider height;

    public BasaltColumnsFeatureConfig(IntProvider reach, IntProvider height) {
        this.reach = reach;
        this.height = height;
    }

    public IntProvider getReach() {
        return this.reach;
    }

    public IntProvider getHeight() {
        return this.height;
    }
}


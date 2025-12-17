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
import net.minecraft.block.BlockState;
import net.minecraft.world.dimension.DimensionType;
import net.minecraft.world.gen.feature.FeatureConfig;

public class FillLayerFeatureConfig
implements FeatureConfig {
    public static final Codec<FillLayerFeatureConfig> CODEC = RecordCodecBuilder.create(instance -> instance.group((App)Codec.intRange((int)0, (int)DimensionType.MAX_HEIGHT).fieldOf("height").forGetter(config -> config.height), (App)BlockState.CODEC.fieldOf("state").forGetter(config -> config.state)).apply((Applicative)instance, FillLayerFeatureConfig::new));
    public final int height;
    public final BlockState state;

    public FillLayerFeatureConfig(int height, BlockState state) {
        this.height = height;
        this.state = state;
    }
}


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
import net.minecraft.util.math.floatprovider.FloatProvider;
import net.minecraft.util.math.intprovider.IntProvider;
import net.minecraft.world.gen.feature.FeatureConfig;

public class LargeDripstoneFeatureConfig
implements FeatureConfig {
    public static final Codec<LargeDripstoneFeatureConfig> CODEC = RecordCodecBuilder.create(instance -> instance.group((App)Codec.intRange((int)1, (int)512).fieldOf("floor_to_ceiling_search_range").orElse((Object)30).forGetter(config -> config.floorToCeilingSearchRange), (App)IntProvider.createValidatingCodec(1, 60).fieldOf("column_radius").forGetter(config -> config.columnRadius), (App)FloatProvider.createValidatedCodec(0.0f, 20.0f).fieldOf("height_scale").forGetter(config -> config.heightScale), (App)Codec.floatRange((float)0.1f, (float)1.0f).fieldOf("max_column_radius_to_cave_height_ratio").forGetter(config -> Float.valueOf(config.maxColumnRadiusToCaveHeightRatio)), (App)FloatProvider.createValidatedCodec(0.1f, 10.0f).fieldOf("stalactite_bluntness").forGetter(config -> config.stalactiteBluntness), (App)FloatProvider.createValidatedCodec(0.1f, 10.0f).fieldOf("stalagmite_bluntness").forGetter(config -> config.stalagmiteBluntness), (App)FloatProvider.createValidatedCodec(0.0f, 2.0f).fieldOf("wind_speed").forGetter(config -> config.windSpeed), (App)Codec.intRange((int)0, (int)100).fieldOf("min_radius_for_wind").forGetter(config -> config.minRadiusForWind), (App)Codec.floatRange((float)0.0f, (float)5.0f).fieldOf("min_bluntness_for_wind").forGetter(config -> Float.valueOf(config.minBluntnessForWind))).apply((Applicative)instance, LargeDripstoneFeatureConfig::new));
    public final int floorToCeilingSearchRange;
    public final IntProvider columnRadius;
    public final FloatProvider heightScale;
    public final float maxColumnRadiusToCaveHeightRatio;
    public final FloatProvider stalactiteBluntness;
    public final FloatProvider stalagmiteBluntness;
    public final FloatProvider windSpeed;
    public final int minRadiusForWind;
    public final float minBluntnessForWind;

    public LargeDripstoneFeatureConfig(int floorToCeilingSearchRange, IntProvider columnRadius, FloatProvider heightScale, float maxColumnRadiusToCaveHeightRatio, FloatProvider stalactiteBluntness, FloatProvider stalagmiteBluntness, FloatProvider windSpeed, int minRadiusForWind, float minBluntnessForWind) {
        this.floorToCeilingSearchRange = floorToCeilingSearchRange;
        this.columnRadius = columnRadius;
        this.heightScale = heightScale;
        this.maxColumnRadiusToCaveHeightRatio = maxColumnRadiusToCaveHeightRatio;
        this.stalactiteBluntness = stalactiteBluntness;
        this.stalagmiteBluntness = stalagmiteBluntness;
        this.windSpeed = windSpeed;
        this.minRadiusForWind = minRadiusForWind;
        this.minBluntnessForWind = minBluntnessForWind;
    }
}


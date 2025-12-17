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
import net.minecraft.world.gen.feature.FeatureConfig;

public class UnderwaterMagmaFeatureConfig
implements FeatureConfig {
    public static final Codec<UnderwaterMagmaFeatureConfig> CODEC = RecordCodecBuilder.create(instance -> instance.group((App)Codec.intRange((int)0, (int)512).fieldOf("floor_search_range").forGetter(config -> config.floorSearchRange), (App)Codec.intRange((int)0, (int)64).fieldOf("placement_radius_around_floor").forGetter(config -> config.placementRadiusAroundFloor), (App)Codec.floatRange((float)0.0f, (float)1.0f).fieldOf("placement_probability_per_valid_position").forGetter(config -> Float.valueOf(config.placementProbabilityPerValidPosition))).apply((Applicative)instance, UnderwaterMagmaFeatureConfig::new));
    public final int floorSearchRange;
    public final int placementRadiusAroundFloor;
    public final float placementProbabilityPerValidPosition;

    public UnderwaterMagmaFeatureConfig(int minDistanceBelowSurface, int floorSearchRange, float placementProbabilityPerValidPosition) {
        this.floorSearchRange = minDistanceBelowSurface;
        this.placementRadiusAroundFloor = floorSearchRange;
        this.placementProbabilityPerValidPosition = placementProbabilityPerValidPosition;
    }
}


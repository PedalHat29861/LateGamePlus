/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  com.mojang.datafixers.kinds.App
 *  com.mojang.datafixers.kinds.Applicative
 *  com.mojang.serialization.Codec
 *  com.mojang.serialization.MapCodec
 *  com.mojang.serialization.codecs.RecordCodecBuilder
 */
package net.minecraft.world.gen.placementmodifier;

import com.mojang.datafixers.kinds.App;
import com.mojang.datafixers.kinds.Applicative;
import com.mojang.serialization.Codec;
import com.mojang.serialization.MapCodec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.random.Random;
import net.minecraft.world.Heightmap;
import net.minecraft.world.gen.feature.FeaturePlacementContext;
import net.minecraft.world.gen.placementmodifier.AbstractConditionalPlacementModifier;
import net.minecraft.world.gen.placementmodifier.PlacementModifierType;

public class SurfaceThresholdFilterPlacementModifier
extends AbstractConditionalPlacementModifier {
    public static final MapCodec<SurfaceThresholdFilterPlacementModifier> MODIFIER_CODEC = RecordCodecBuilder.mapCodec(instance -> instance.group((App)Heightmap.Type.CODEC.fieldOf("heightmap").forGetter(placementModifier -> placementModifier.heightmap), (App)Codec.INT.optionalFieldOf("min_inclusive", (Object)Integer.MIN_VALUE).forGetter(placementModifier -> placementModifier.min), (App)Codec.INT.optionalFieldOf("max_inclusive", (Object)Integer.MAX_VALUE).forGetter(placementModifier -> placementModifier.max)).apply((Applicative)instance, SurfaceThresholdFilterPlacementModifier::new));
    private final Heightmap.Type heightmap;
    private final int min;
    private final int max;

    private SurfaceThresholdFilterPlacementModifier(Heightmap.Type heightmap, int min, int max) {
        this.heightmap = heightmap;
        this.min = min;
        this.max = max;
    }

    public static SurfaceThresholdFilterPlacementModifier of(Heightmap.Type heightmap, int min, int max) {
        return new SurfaceThresholdFilterPlacementModifier(heightmap, min, max);
    }

    @Override
    protected boolean shouldPlace(FeaturePlacementContext context, Random random, BlockPos pos) {
        long l = context.getTopY(this.heightmap, pos.getX(), pos.getZ());
        long m = l + (long)this.min;
        long n = l + (long)this.max;
        return m <= (long)pos.getY() && (long)pos.getY() <= n;
    }

    @Override
    public PlacementModifierType<?> getType() {
        return PlacementModifierType.SURFACE_RELATIVE_THRESHOLD_FILTER;
    }
}


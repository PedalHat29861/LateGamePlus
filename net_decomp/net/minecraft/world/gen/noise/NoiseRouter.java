/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  com.mojang.datafixers.kinds.Applicative
 *  com.mojang.serialization.Codec
 *  com.mojang.serialization.codecs.RecordCodecBuilder
 */
package net.minecraft.world.gen.noise;

import com.mojang.datafixers.kinds.Applicative;
import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import java.util.function.Function;
import net.minecraft.world.gen.densityfunction.DensityFunction;

public record NoiseRouter(DensityFunction barrierNoise, DensityFunction fluidLevelFloodednessNoise, DensityFunction fluidLevelSpreadNoise, DensityFunction lavaNoise, DensityFunction temperature, DensityFunction vegetation, DensityFunction continents, DensityFunction erosion, DensityFunction depth, DensityFunction ridges, DensityFunction preliminarySurfaceLevel, DensityFunction finalDensity, DensityFunction veinToggle, DensityFunction veinRidged, DensityFunction veinGap) {
    public static final Codec<NoiseRouter> CODEC = RecordCodecBuilder.create(instance -> instance.group(NoiseRouter.field("barrier", NoiseRouter::barrierNoise), NoiseRouter.field("fluid_level_floodedness", NoiseRouter::fluidLevelFloodednessNoise), NoiseRouter.field("fluid_level_spread", NoiseRouter::fluidLevelSpreadNoise), NoiseRouter.field("lava", NoiseRouter::lavaNoise), NoiseRouter.field("temperature", NoiseRouter::temperature), NoiseRouter.field("vegetation", NoiseRouter::vegetation), NoiseRouter.field("continents", NoiseRouter::continents), NoiseRouter.field("erosion", NoiseRouter::erosion), NoiseRouter.field("depth", NoiseRouter::depth), NoiseRouter.field("ridges", NoiseRouter::ridges), NoiseRouter.field("preliminary_surface_level", NoiseRouter::preliminarySurfaceLevel), NoiseRouter.field("final_density", NoiseRouter::finalDensity), NoiseRouter.field("vein_toggle", NoiseRouter::veinToggle), NoiseRouter.field("vein_ridged", NoiseRouter::veinRidged), NoiseRouter.field("vein_gap", NoiseRouter::veinGap)).apply((Applicative)instance, NoiseRouter::new));

    private static RecordCodecBuilder<NoiseRouter, DensityFunction> field(String name, Function<NoiseRouter, DensityFunction> getter) {
        return DensityFunction.FUNCTION_CODEC.fieldOf(name).forGetter(getter);
    }

    public NoiseRouter apply(DensityFunction.DensityFunctionVisitor visitor) {
        return new NoiseRouter(this.barrierNoise.apply(visitor), this.fluidLevelFloodednessNoise.apply(visitor), this.fluidLevelSpreadNoise.apply(visitor), this.lavaNoise.apply(visitor), this.temperature.apply(visitor), this.vegetation.apply(visitor), this.continents.apply(visitor), this.erosion.apply(visitor), this.depth.apply(visitor), this.ridges.apply(visitor), this.preliminarySurfaceLevel.apply(visitor), this.finalDensity.apply(visitor), this.veinToggle.apply(visitor), this.veinRidged.apply(visitor), this.veinGap.apply(visitor));
    }
}


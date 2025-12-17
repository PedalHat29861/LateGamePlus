/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  com.mojang.datafixers.kinds.App
 *  com.mojang.datafixers.kinds.Applicative
 *  com.mojang.serialization.Codec
 *  com.mojang.serialization.codecs.RecordCodecBuilder
 */
package net.minecraft.world.gen.carver;

import com.mojang.datafixers.kinds.App;
import com.mojang.datafixers.kinds.Applicative;
import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import net.minecraft.block.Block;
import net.minecraft.registry.entry.RegistryEntryList;
import net.minecraft.util.dynamic.Codecs;
import net.minecraft.util.math.floatprovider.FloatProvider;
import net.minecraft.world.gen.YOffset;
import net.minecraft.world.gen.carver.CarverConfig;
import net.minecraft.world.gen.carver.CarverDebugConfig;
import net.minecraft.world.gen.heightprovider.HeightProvider;

public class RavineCarverConfig
extends CarverConfig {
    public static final Codec<RavineCarverConfig> RAVINE_CODEC = RecordCodecBuilder.create(instance -> instance.group((App)CarverConfig.CONFIG_CODEC.forGetter(ravineCarverConfig -> ravineCarverConfig), (App)FloatProvider.VALUE_CODEC.fieldOf("vertical_rotation").forGetter(config -> config.verticalRotation), (App)Shape.CODEC.fieldOf("shape").forGetter(config -> config.shape)).apply((Applicative)instance, RavineCarverConfig::new));
    public final FloatProvider verticalRotation;
    public final Shape shape;

    public RavineCarverConfig(float probability, HeightProvider y, FloatProvider yScale, YOffset lavaLevel, CarverDebugConfig debugConfig, RegistryEntryList<Block> replaceable, FloatProvider verticalRotation, Shape shape) {
        super(probability, y, yScale, lavaLevel, debugConfig, replaceable);
        this.verticalRotation = verticalRotation;
        this.shape = shape;
    }

    public RavineCarverConfig(CarverConfig config, FloatProvider verticalRotation, Shape shape) {
        this(config.probability, config.y, config.yScale, config.lavaLevel, config.debugConfig, config.replaceable, verticalRotation, shape);
    }

    public static class Shape {
        public static final Codec<Shape> CODEC = RecordCodecBuilder.create(instance -> instance.group((App)FloatProvider.VALUE_CODEC.fieldOf("distance_factor").forGetter(shape -> shape.distanceFactor), (App)FloatProvider.VALUE_CODEC.fieldOf("thickness").forGetter(shape -> shape.thickness), (App)Codecs.POSITIVE_INT.fieldOf("width_smoothness").forGetter(shape -> shape.widthSmoothness), (App)FloatProvider.VALUE_CODEC.fieldOf("horizontal_radius_factor").forGetter(shape -> shape.horizontalRadiusFactor), (App)Codec.FLOAT.fieldOf("vertical_radius_default_factor").forGetter(shape -> Float.valueOf(shape.verticalRadiusDefaultFactor)), (App)Codec.FLOAT.fieldOf("vertical_radius_center_factor").forGetter(shape -> Float.valueOf(shape.verticalRadiusCenterFactor))).apply((Applicative)instance, Shape::new));
        public final FloatProvider distanceFactor;
        public final FloatProvider thickness;
        public final int widthSmoothness;
        public final FloatProvider horizontalRadiusFactor;
        public final float verticalRadiusDefaultFactor;
        public final float verticalRadiusCenterFactor;

        public Shape(FloatProvider distanceFactor, FloatProvider thickness, int widthSmoothness, FloatProvider horizontalRadiusFactor, float verticalRadiusDefaultFactor, float verticalRadiusCenterFactor) {
            this.widthSmoothness = widthSmoothness;
            this.horizontalRadiusFactor = horizontalRadiusFactor;
            this.verticalRadiusDefaultFactor = verticalRadiusDefaultFactor;
            this.verticalRadiusCenterFactor = verticalRadiusCenterFactor;
            this.distanceFactor = distanceFactor;
            this.thickness = thickness;
        }
    }
}


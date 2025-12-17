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
package net.minecraft.world.gen.carver;

import com.mojang.datafixers.kinds.App;
import com.mojang.datafixers.kinds.Applicative;
import com.mojang.serialization.Codec;
import com.mojang.serialization.MapCodec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import net.minecraft.block.Block;
import net.minecraft.registry.RegistryCodecs;
import net.minecraft.registry.RegistryKeys;
import net.minecraft.registry.entry.RegistryEntryList;
import net.minecraft.util.math.floatprovider.FloatProvider;
import net.minecraft.world.gen.ProbabilityConfig;
import net.minecraft.world.gen.YOffset;
import net.minecraft.world.gen.carver.CarverDebugConfig;
import net.minecraft.world.gen.heightprovider.HeightProvider;

public class CarverConfig
extends ProbabilityConfig {
    public static final MapCodec<CarverConfig> CONFIG_CODEC = RecordCodecBuilder.mapCodec(instance -> instance.group((App)Codec.floatRange((float)0.0f, (float)1.0f).fieldOf("probability").forGetter(config -> Float.valueOf(config.probability)), (App)HeightProvider.CODEC.fieldOf("y").forGetter(config -> config.y), (App)FloatProvider.VALUE_CODEC.fieldOf("yScale").forGetter(config -> config.yScale), (App)YOffset.OFFSET_CODEC.fieldOf("lava_level").forGetter(config -> config.lavaLevel), (App)CarverDebugConfig.CODEC.optionalFieldOf("debug_settings", (Object)CarverDebugConfig.DEFAULT).forGetter(config -> config.debugConfig), (App)RegistryCodecs.entryList(RegistryKeys.BLOCK).fieldOf("replaceable").forGetter(config -> config.replaceable)).apply((Applicative)instance, CarverConfig::new));
    public final HeightProvider y;
    public final FloatProvider yScale;
    public final YOffset lavaLevel;
    public final CarverDebugConfig debugConfig;
    public final RegistryEntryList<Block> replaceable;

    public CarverConfig(float probability, HeightProvider y, FloatProvider yScale, YOffset lavaLevel, CarverDebugConfig debugConfig, RegistryEntryList<Block> replaceable) {
        super(probability);
        this.y = y;
        this.yScale = yScale;
        this.lavaLevel = lavaLevel;
        this.debugConfig = debugConfig;
        this.replaceable = replaceable;
    }
}


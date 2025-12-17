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
import net.minecraft.block.Block;
import net.minecraft.fluid.FluidState;
import net.minecraft.registry.RegistryCodecs;
import net.minecraft.registry.RegistryKeys;
import net.minecraft.registry.entry.RegistryEntryList;
import net.minecraft.world.gen.feature.FeatureConfig;

public class SpringFeatureConfig
implements FeatureConfig {
    public static final Codec<SpringFeatureConfig> CODEC = RecordCodecBuilder.create(instance -> instance.group((App)FluidState.CODEC.fieldOf("state").forGetter(config -> config.state), (App)Codec.BOOL.fieldOf("requires_block_below").orElse((Object)true).forGetter(config -> config.requiresBlockBelow), (App)Codec.INT.fieldOf("rock_count").orElse((Object)4).forGetter(config -> config.rockCount), (App)Codec.INT.fieldOf("hole_count").orElse((Object)1).forGetter(config -> config.holeCount), (App)RegistryCodecs.entryList(RegistryKeys.BLOCK).fieldOf("valid_blocks").forGetter(config -> config.validBlocks)).apply((Applicative)instance, SpringFeatureConfig::new));
    public final FluidState state;
    public final boolean requiresBlockBelow;
    public final int rockCount;
    public final int holeCount;
    public final RegistryEntryList<Block> validBlocks;

    public SpringFeatureConfig(FluidState state, boolean requiresBlockBelow, int rockCount, int holeCount, RegistryEntryList<Block> validBlocks) {
        this.state = state;
        this.requiresBlockBelow = requiresBlockBelow;
        this.rockCount = rockCount;
        this.holeCount = holeCount;
        this.validBlocks = validBlocks;
    }
}


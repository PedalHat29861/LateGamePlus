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
import net.minecraft.world.gen.blockpredicate.BlockPredicate;
import net.minecraft.world.gen.feature.FeatureConfig;

public class HugeFungusFeatureConfig
implements FeatureConfig {
    public static final Codec<HugeFungusFeatureConfig> CODEC = RecordCodecBuilder.create(instance -> instance.group((App)BlockState.CODEC.fieldOf("valid_base_block").forGetter(config -> config.validBaseBlock), (App)BlockState.CODEC.fieldOf("stem_state").forGetter(config -> config.stemState), (App)BlockState.CODEC.fieldOf("hat_state").forGetter(config -> config.hatState), (App)BlockState.CODEC.fieldOf("decor_state").forGetter(config -> config.decorationState), (App)BlockPredicate.BASE_CODEC.fieldOf("replaceable_blocks").forGetter(config -> config.replaceableBlocks), (App)Codec.BOOL.fieldOf("planted").orElse((Object)false).forGetter(config -> config.planted)).apply((Applicative)instance, HugeFungusFeatureConfig::new));
    public final BlockState validBaseBlock;
    public final BlockState stemState;
    public final BlockState hatState;
    public final BlockState decorationState;
    public final BlockPredicate replaceableBlocks;
    public final boolean planted;

    public HugeFungusFeatureConfig(BlockState validBaseBlock, BlockState stemState, BlockState hatState, BlockState decorationState, BlockPredicate replaceableBlocks, boolean planted) {
        this.validBaseBlock = validBaseBlock;
        this.stemState = stemState;
        this.hatState = hatState;
        this.decorationState = decorationState;
        this.replaceableBlocks = replaceableBlocks;
        this.planted = planted;
    }
}


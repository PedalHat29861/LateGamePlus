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
import java.util.List;
import net.minecraft.block.Block;
import net.minecraft.block.BlockState;
import net.minecraft.registry.RegistryKeys;
import net.minecraft.registry.tag.TagKey;
import net.minecraft.util.dynamic.Codecs;
import net.minecraft.world.gen.stateprovider.BlockStateProvider;

public class GeodeLayerConfig {
    public final BlockStateProvider fillingProvider;
    public final BlockStateProvider innerLayerProvider;
    public final BlockStateProvider alternateInnerLayerProvider;
    public final BlockStateProvider middleLayerProvider;
    public final BlockStateProvider outerLayerProvider;
    public final List<BlockState> innerBlocks;
    public final TagKey<Block> cannotReplace;
    public final TagKey<Block> invalidBlocks;
    public static final Codec<GeodeLayerConfig> CODEC = RecordCodecBuilder.create(instance -> instance.group((App)BlockStateProvider.TYPE_CODEC.fieldOf("filling_provider").forGetter(config -> config.fillingProvider), (App)BlockStateProvider.TYPE_CODEC.fieldOf("inner_layer_provider").forGetter(config -> config.innerLayerProvider), (App)BlockStateProvider.TYPE_CODEC.fieldOf("alternate_inner_layer_provider").forGetter(config -> config.alternateInnerLayerProvider), (App)BlockStateProvider.TYPE_CODEC.fieldOf("middle_layer_provider").forGetter(config -> config.middleLayerProvider), (App)BlockStateProvider.TYPE_CODEC.fieldOf("outer_layer_provider").forGetter(config -> config.outerLayerProvider), (App)Codecs.nonEmptyList(BlockState.CODEC.listOf()).fieldOf("inner_placements").forGetter(config -> config.innerBlocks), (App)TagKey.codec(RegistryKeys.BLOCK).fieldOf("cannot_replace").forGetter(config -> config.cannotReplace), (App)TagKey.codec(RegistryKeys.BLOCK).fieldOf("invalid_blocks").forGetter(config -> config.invalidBlocks)).apply((Applicative)instance, GeodeLayerConfig::new));

    public GeodeLayerConfig(BlockStateProvider fillingProvider, BlockStateProvider innerLayerProvider, BlockStateProvider alternateInnerLayerProvider, BlockStateProvider middleLayerProvider, BlockStateProvider outerLayerProvider, List<BlockState> innerBlocks, TagKey<Block> cannotReplace, TagKey<Block> invalidBlocks) {
        this.fillingProvider = fillingProvider;
        this.innerLayerProvider = innerLayerProvider;
        this.alternateInnerLayerProvider = alternateInnerLayerProvider;
        this.middleLayerProvider = middleLayerProvider;
        this.outerLayerProvider = outerLayerProvider;
        this.innerBlocks = innerBlocks;
        this.cannotReplace = cannotReplace;
        this.invalidBlocks = invalidBlocks;
    }
}


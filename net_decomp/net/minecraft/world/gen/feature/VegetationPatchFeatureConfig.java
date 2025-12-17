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
import net.minecraft.registry.RegistryKeys;
import net.minecraft.registry.entry.RegistryEntry;
import net.minecraft.registry.tag.TagKey;
import net.minecraft.util.math.VerticalSurfaceType;
import net.minecraft.util.math.intprovider.IntProvider;
import net.minecraft.world.gen.feature.FeatureConfig;
import net.minecraft.world.gen.feature.PlacedFeature;
import net.minecraft.world.gen.stateprovider.BlockStateProvider;

public class VegetationPatchFeatureConfig
implements FeatureConfig {
    public static final Codec<VegetationPatchFeatureConfig> CODEC = RecordCodecBuilder.create(instance -> instance.group((App)TagKey.codec(RegistryKeys.BLOCK).fieldOf("replaceable").forGetter(config -> config.replaceable), (App)BlockStateProvider.TYPE_CODEC.fieldOf("ground_state").forGetter(config -> config.groundState), (App)PlacedFeature.REGISTRY_CODEC.fieldOf("vegetation_feature").forGetter(config -> config.vegetationFeature), (App)VerticalSurfaceType.CODEC.fieldOf("surface").forGetter(config -> config.surface), (App)IntProvider.createValidatingCodec(1, 128).fieldOf("depth").forGetter(config -> config.depth), (App)Codec.floatRange((float)0.0f, (float)1.0f).fieldOf("extra_bottom_block_chance").forGetter(config -> Float.valueOf(config.extraBottomBlockChance)), (App)Codec.intRange((int)1, (int)256).fieldOf("vertical_range").forGetter(config -> config.verticalRange), (App)Codec.floatRange((float)0.0f, (float)1.0f).fieldOf("vegetation_chance").forGetter(config -> Float.valueOf(config.vegetationChance)), (App)IntProvider.VALUE_CODEC.fieldOf("xz_radius").forGetter(config -> config.horizontalRadius), (App)Codec.floatRange((float)0.0f, (float)1.0f).fieldOf("extra_edge_column_chance").forGetter(config -> Float.valueOf(config.extraEdgeColumnChance))).apply((Applicative)instance, VegetationPatchFeatureConfig::new));
    public final TagKey<Block> replaceable;
    public final BlockStateProvider groundState;
    public final RegistryEntry<PlacedFeature> vegetationFeature;
    public final VerticalSurfaceType surface;
    public final IntProvider depth;
    public final float extraBottomBlockChance;
    public final int verticalRange;
    public final float vegetationChance;
    public final IntProvider horizontalRadius;
    public final float extraEdgeColumnChance;

    public VegetationPatchFeatureConfig(TagKey<Block> replaceable, BlockStateProvider groundState, RegistryEntry<PlacedFeature> vegetationFeature, VerticalSurfaceType surface, IntProvider depth, float extraBottomBlockChance, int verticalRange, float vegetationChance, IntProvider horizontalRadius, float extraEdgeColumnChance) {
        this.replaceable = replaceable;
        this.groundState = groundState;
        this.vegetationFeature = vegetationFeature;
        this.surface = surface;
        this.depth = depth;
        this.extraBottomBlockChance = extraBottomBlockChance;
        this.verticalRange = verticalRange;
        this.vegetationChance = vegetationChance;
        this.horizontalRadius = horizontalRadius;
        this.extraEdgeColumnChance = extraEdgeColumnChance;
    }
}


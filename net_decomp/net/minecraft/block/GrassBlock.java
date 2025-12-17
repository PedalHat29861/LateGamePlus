/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  com.mojang.serialization.MapCodec
 */
package net.minecraft.block;

import com.mojang.serialization.MapCodec;
import java.util.List;
import java.util.Optional;
import net.minecraft.block.AbstractBlock;
import net.minecraft.block.BlockState;
import net.minecraft.block.Blocks;
import net.minecraft.block.Fertilizable;
import net.minecraft.block.SpreadableBlock;
import net.minecraft.registry.RegistryKeys;
import net.minecraft.registry.entry.RegistryEntry;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.random.Random;
import net.minecraft.world.World;
import net.minecraft.world.WorldView;
import net.minecraft.world.gen.feature.ConfiguredFeature;
import net.minecraft.world.gen.feature.PlacedFeature;
import net.minecraft.world.gen.feature.RandomPatchFeatureConfig;
import net.minecraft.world.gen.feature.VegetationPlacedFeatures;

public class GrassBlock
extends SpreadableBlock
implements Fertilizable {
    public static final MapCodec<GrassBlock> CODEC = GrassBlock.createCodec(GrassBlock::new);

    public MapCodec<GrassBlock> getCodec() {
        return CODEC;
    }

    public GrassBlock(AbstractBlock.Settings settings) {
        super(settings);
    }

    @Override
    public boolean isFertilizable(WorldView world, BlockPos pos, BlockState state) {
        return world.getBlockState(pos.up()).isAir();
    }

    @Override
    public boolean canGrow(World world, Random random, BlockPos pos, BlockState state) {
        return true;
    }

    @Override
    public void grow(ServerWorld world, Random random, BlockPos pos, BlockState state) {
        BlockPos blockPos = pos.up();
        BlockState blockState = Blocks.SHORT_GRASS.getDefaultState();
        Optional optional = world.getRegistryManager().getOrThrow(RegistryKeys.PLACED_FEATURE).getOptional(VegetationPlacedFeatures.GRASS_BONEMEAL);
        block0: for (int i = 0; i < 128; ++i) {
            RegistryEntry<PlacedFeature> registryEntry;
            Fertilizable fertilizable;
            BlockPos blockPos2 = blockPos;
            for (int j = 0; j < i / 16; ++j) {
                if (!world.getBlockState((blockPos2 = blockPos2.add(random.nextInt(3) - 1, (random.nextInt(3) - 1) * random.nextInt(3) / 2, random.nextInt(3) - 1)).down()).isOf(this) || world.getBlockState(blockPos2).isFullCube(world, blockPos2)) continue block0;
            }
            BlockState blockState2 = world.getBlockState(blockPos2);
            if (blockState2.isOf(blockState.getBlock()) && random.nextInt(10) == 0 && (fertilizable = (Fertilizable)((Object)blockState.getBlock())).isFertilizable(world, blockPos2, blockState2)) {
                fertilizable.grow(world, random, blockPos2, blockState2);
            }
            if (!blockState2.isAir()) continue;
            if (random.nextInt(8) == 0) {
                List<ConfiguredFeature<?, ?>> list = world.getBiome(blockPos2).value().getGenerationSettings().getFlowerFeatures();
                if (list.isEmpty()) continue;
                int k = random.nextInt(list.size());
                registryEntry = ((RandomPatchFeatureConfig)list.get(k).config()).feature();
            } else {
                if (!optional.isPresent()) continue;
                registryEntry = (RegistryEntry<PlacedFeature>)optional.get();
            }
            ((PlacedFeature)registryEntry.value()).generateUnregistered(world, world.getChunkManager().getChunkGenerator(), random, blockPos2);
        }
    }

    @Override
    public Fertilizable.FertilizableType getFertilizableType() {
        return Fertilizable.FertilizableType.NEIGHBOR_SPREADER;
    }
}


/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  com.mojang.serialization.Codec
 */
package net.minecraft.world.gen.feature;

import com.mojang.serialization.Codec;
import java.util.List;
import net.minecraft.block.BlockState;
import net.minecraft.block.Blocks;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Direction;
import net.minecraft.util.math.Vec3i;
import net.minecraft.util.math.random.Random;
import net.minecraft.world.StructureWorldAccess;
import net.minecraft.world.WorldAccess;
import net.minecraft.world.gen.feature.Feature;
import net.minecraft.world.gen.feature.MultifaceGrowthFeatureConfig;
import net.minecraft.world.gen.feature.util.FeatureContext;

public class MultifaceGrowthFeature
extends Feature<MultifaceGrowthFeatureConfig> {
    public MultifaceGrowthFeature(Codec<MultifaceGrowthFeatureConfig> codec) {
        super(codec);
    }

    @Override
    public boolean generate(FeatureContext<MultifaceGrowthFeatureConfig> context) {
        StructureWorldAccess structureWorldAccess = context.getWorld();
        BlockPos blockPos = context.getOrigin();
        Random random = context.getRandom();
        MultifaceGrowthFeatureConfig multifaceGrowthFeatureConfig = context.getConfig();
        if (!MultifaceGrowthFeature.isAirOrWater(structureWorldAccess.getBlockState(blockPos))) {
            return false;
        }
        List<Direction> list = multifaceGrowthFeatureConfig.shuffleDirections(random);
        if (MultifaceGrowthFeature.generate(structureWorldAccess, blockPos, structureWorldAccess.getBlockState(blockPos), multifaceGrowthFeatureConfig, random, list)) {
            return true;
        }
        BlockPos.Mutable mutable = blockPos.mutableCopy();
        block0: for (Direction direction : list) {
            mutable.set(blockPos);
            List<Direction> list2 = multifaceGrowthFeatureConfig.shuffleDirections(random, direction.getOpposite());
            for (int i = 0; i < multifaceGrowthFeatureConfig.searchRange; ++i) {
                mutable.set((Vec3i)blockPos, direction);
                BlockState blockState = structureWorldAccess.getBlockState(mutable);
                if (!MultifaceGrowthFeature.isAirOrWater(blockState) && !blockState.isOf(multifaceGrowthFeatureConfig.block)) continue block0;
                if (!MultifaceGrowthFeature.generate(structureWorldAccess, mutable, blockState, multifaceGrowthFeatureConfig, random, list2)) continue;
                return true;
            }
        }
        return false;
    }

    public static boolean generate(StructureWorldAccess world, BlockPos pos, BlockState state, MultifaceGrowthFeatureConfig config, Random random, List<Direction> directions) {
        BlockPos.Mutable mutable = pos.mutableCopy();
        for (Direction direction : directions) {
            BlockState blockState = world.getBlockState(mutable.set((Vec3i)pos, direction));
            if (!blockState.isIn(config.canPlaceOn)) continue;
            BlockState blockState2 = config.block.withDirection(state, world, pos, direction);
            if (blockState2 == null) {
                return false;
            }
            world.setBlockState(pos, blockState2, 3);
            world.getChunk(pos).markBlockForPostProcessing(pos);
            if (random.nextFloat() < config.spreadChance) {
                config.block.getGrower().grow(blockState2, (WorldAccess)world, pos, direction, random, true);
            }
            return true;
        }
        return false;
    }

    private static boolean isAirOrWater(BlockState state) {
        return state.isAir() || state.isOf(Blocks.WATER);
    }
}


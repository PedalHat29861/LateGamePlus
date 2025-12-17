/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  com.mojang.serialization.Codec
 */
package net.minecraft.world.gen.feature;

import com.mojang.serialization.Codec;
import java.util.Optional;
import java.util.OptionalInt;
import java.util.function.Predicate;
import net.minecraft.block.Block;
import net.minecraft.block.BlockState;
import net.minecraft.block.Blocks;
import net.minecraft.util.math.BlockBox;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Direction;
import net.minecraft.util.math.Vec3i;
import net.minecraft.util.math.random.Random;
import net.minecraft.util.shape.VoxelShape;
import net.minecraft.util.shape.VoxelShapes;
import net.minecraft.world.StructureWorldAccess;
import net.minecraft.world.WorldAccess;
import net.minecraft.world.gen.feature.Feature;
import net.minecraft.world.gen.feature.UnderwaterMagmaFeatureConfig;
import net.minecraft.world.gen.feature.util.CaveSurface;
import net.minecraft.world.gen.feature.util.FeatureContext;

public class UnderwaterMagmaFeature
extends Feature<UnderwaterMagmaFeatureConfig> {
    public UnderwaterMagmaFeature(Codec<UnderwaterMagmaFeatureConfig> codec) {
        super(codec);
    }

    @Override
    public boolean generate(FeatureContext<UnderwaterMagmaFeatureConfig> context) {
        Vec3i vec3i;
        StructureWorldAccess structureWorldAccess = context.getWorld();
        BlockPos blockPos = context.getOrigin();
        UnderwaterMagmaFeatureConfig underwaterMagmaFeatureConfig = context.getConfig();
        Random random = context.getRandom();
        OptionalInt optionalInt = UnderwaterMagmaFeature.getFloorHeight(structureWorldAccess, blockPos, underwaterMagmaFeatureConfig);
        if (optionalInt.isEmpty()) {
            return false;
        }
        BlockPos blockPos2 = blockPos.withY(optionalInt.getAsInt());
        BlockBox blockBox = BlockBox.create(blockPos2.subtract(vec3i = new Vec3i(underwaterMagmaFeatureConfig.placementRadiusAroundFloor, underwaterMagmaFeatureConfig.placementRadiusAroundFloor, underwaterMagmaFeatureConfig.placementRadiusAroundFloor)), blockPos2.add(vec3i));
        return BlockPos.stream(blockBox).filter(pos -> random.nextFloat() < underwaterMagmaFeatureConfig.placementProbabilityPerValidPosition).filter(pos -> this.isValidPosition(structureWorldAccess, (BlockPos)pos)).mapToInt(pos -> {
            structureWorldAccess.setBlockState((BlockPos)pos, Blocks.MAGMA_BLOCK.getDefaultState(), 2);
            return 1;
        }).sum() > 0;
    }

    private static OptionalInt getFloorHeight(StructureWorldAccess world, BlockPos pos, UnderwaterMagmaFeatureConfig config) {
        Predicate<BlockState> predicate = state -> state.isOf(Blocks.WATER);
        Predicate<BlockState> predicate2 = state -> !state.isOf(Blocks.WATER);
        Optional<CaveSurface> optional = CaveSurface.create(world, pos, config.floorSearchRange, predicate, predicate2);
        return optional.map(CaveSurface::getFloorHeight).orElseGet(OptionalInt::empty);
    }

    private boolean isValidPosition(StructureWorldAccess world, BlockPos pos) {
        if (UnderwaterMagmaFeature.cannotReplace(world.getBlockState(pos)) || this.isFaceNotFull(world, pos.down(), Direction.UP)) {
            return false;
        }
        for (Direction direction : Direction.Type.HORIZONTAL) {
            if (!this.isFaceNotFull(world, pos.offset(direction), direction.getOpposite())) continue;
            return false;
        }
        return true;
    }

    private static boolean cannotReplace(BlockState state) {
        return state.isOf(Blocks.WATER) || state.isAir();
    }

    private boolean isFaceNotFull(WorldAccess world, BlockPos pos, Direction direction) {
        BlockState blockState = world.getBlockState(pos);
        VoxelShape voxelShape = blockState.getCullingFace(direction);
        return voxelShape == VoxelShapes.empty() || !Block.isShapeFullCube(voxelShape);
    }
}


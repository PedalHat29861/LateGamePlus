/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  com.mojang.serialization.MapCodec
 */
package net.minecraft.block;

import com.mojang.serialization.MapCodec;
import java.util.Collection;
import net.minecraft.block.AbstractBlock;
import net.minecraft.block.Block;
import net.minecraft.block.BlockState;
import net.minecraft.block.Blocks;
import net.minecraft.block.MultifaceBlock;
import net.minecraft.block.MultifaceGrower;
import net.minecraft.block.MultifaceGrowthBlock;
import net.minecraft.block.SculkSpreadable;
import net.minecraft.block.entity.SculkSpreadManager;
import net.minecraft.fluid.FluidState;
import net.minecraft.fluid.Fluids;
import net.minecraft.registry.tag.BlockTags;
import net.minecraft.registry.tag.TagKey;
import net.minecraft.sound.SoundCategory;
import net.minecraft.sound.SoundEvents;
import net.minecraft.state.property.BooleanProperty;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Direction;
import net.minecraft.util.math.MathHelper;
import net.minecraft.util.math.random.Random;
import net.minecraft.world.BlockView;
import net.minecraft.world.WorldAccess;

public class SculkVeinBlock
extends MultifaceGrowthBlock
implements SculkSpreadable {
    public static final MapCodec<SculkVeinBlock> CODEC = SculkVeinBlock.createCodec(SculkVeinBlock::new);
    private final MultifaceGrower allGrowTypeGrower = new MultifaceGrower(new SculkVeinGrowChecker(this, MultifaceGrower.GROW_TYPES));
    private final MultifaceGrower samePositionOnlyGrower = new MultifaceGrower(new SculkVeinGrowChecker(this, MultifaceGrower.GrowType.SAME_POSITION));

    public MapCodec<SculkVeinBlock> getCodec() {
        return CODEC;
    }

    public SculkVeinBlock(AbstractBlock.Settings settings) {
        super(settings);
    }

    @Override
    public MultifaceGrower getGrower() {
        return this.allGrowTypeGrower;
    }

    public MultifaceGrower getSamePositionOnlyGrower() {
        return this.samePositionOnlyGrower;
    }

    public static boolean place(WorldAccess world, BlockPos pos, BlockState state, Collection<Direction> directions) {
        boolean bl = false;
        BlockState blockState = Blocks.SCULK_VEIN.getDefaultState();
        for (Direction direction : directions) {
            if (!SculkVeinBlock.canGrowOn(world, pos, direction)) continue;
            blockState = (BlockState)blockState.with(SculkVeinBlock.getProperty(direction), true);
            bl = true;
        }
        if (!bl) {
            return false;
        }
        if (!state.getFluidState().isEmpty()) {
            blockState = (BlockState)blockState.with(MultifaceBlock.WATERLOGGED, true);
        }
        world.setBlockState(pos, blockState, 3);
        return true;
    }

    @Override
    public void spreadAtSamePosition(WorldAccess world, BlockState state, BlockPos pos, Random random) {
        if (!state.isOf(this)) {
            return;
        }
        for (Direction direction : DIRECTIONS) {
            BooleanProperty booleanProperty = SculkVeinBlock.getProperty(direction);
            if (!state.get(booleanProperty).booleanValue() || !world.getBlockState(pos.offset(direction)).isOf(Blocks.SCULK)) continue;
            state = (BlockState)state.with(booleanProperty, false);
        }
        if (!SculkVeinBlock.hasAnyDirection(state)) {
            FluidState fluidState = world.getFluidState(pos);
            state = (fluidState.isEmpty() ? Blocks.AIR : Blocks.WATER).getDefaultState();
        }
        world.setBlockState(pos, state, 3);
        SculkSpreadable.super.spreadAtSamePosition(world, state, pos, random);
    }

    @Override
    public int spread(SculkSpreadManager.Cursor cursor, WorldAccess world, BlockPos catalystPos, Random random, SculkSpreadManager spreadManager, boolean shouldConvertToBlock) {
        if (shouldConvertToBlock && this.convertToBlock(spreadManager, world, cursor.getPos(), random)) {
            return cursor.getCharge() - 1;
        }
        return random.nextInt(spreadManager.getSpreadChance()) == 0 ? MathHelper.floor((float)cursor.getCharge() * 0.5f) : cursor.getCharge();
    }

    private boolean convertToBlock(SculkSpreadManager spreadManager, WorldAccess world, BlockPos pos, Random random) {
        BlockState blockState = world.getBlockState(pos);
        TagKey<Block> tagKey = spreadManager.getReplaceableTag();
        for (Direction direction : Direction.shuffle(random)) {
            BlockPos blockPos;
            BlockState blockState2;
            if (!SculkVeinBlock.hasDirection(blockState, direction) || !(blockState2 = world.getBlockState(blockPos = pos.offset(direction))).isIn(tagKey)) continue;
            BlockState blockState3 = Blocks.SCULK.getDefaultState();
            world.setBlockState(blockPos, blockState3, 3);
            Block.pushEntitiesUpBeforeBlockChange(blockState2, blockState3, world, blockPos);
            world.playSound(null, blockPos, SoundEvents.BLOCK_SCULK_SPREAD, SoundCategory.BLOCKS, 1.0f, 1.0f);
            this.allGrowTypeGrower.grow(blockState3, world, blockPos, spreadManager.isWorldGen());
            Direction direction2 = direction.getOpposite();
            for (Direction direction3 : DIRECTIONS) {
                BlockPos blockPos2;
                BlockState blockState4;
                if (direction3 == direction2 || !(blockState4 = world.getBlockState(blockPos2 = blockPos.offset(direction3))).isOf(this)) continue;
                this.spreadAtSamePosition(world, blockState4, blockPos2, random);
            }
            return true;
        }
        return false;
    }

    public static boolean veinCoversSculkReplaceable(WorldAccess world, BlockState state, BlockPos pos) {
        if (!state.isOf(Blocks.SCULK_VEIN)) {
            return false;
        }
        for (Direction direction : DIRECTIONS) {
            if (!SculkVeinBlock.hasDirection(state, direction) || !world.getBlockState(pos.offset(direction)).isIn(BlockTags.SCULK_REPLACEABLE)) continue;
            return true;
        }
        return false;
    }

    class SculkVeinGrowChecker
    extends MultifaceGrower.LichenGrowChecker {
        private final MultifaceGrower.GrowType[] growTypes;

        public SculkVeinGrowChecker(SculkVeinBlock block, MultifaceGrower.GrowType ... growTypes) {
            super(block);
            this.growTypes = growTypes;
        }

        @Override
        public boolean canGrow(BlockView world, BlockPos pos, BlockPos growPos, Direction direction, BlockState state) {
            BlockPos blockPos;
            BlockState blockState = world.getBlockState(growPos.offset(direction));
            if (blockState.isOf(Blocks.SCULK) || blockState.isOf(Blocks.SCULK_CATALYST) || blockState.isOf(Blocks.MOVING_PISTON)) {
                return false;
            }
            if (pos.getManhattanDistance(growPos) == 2 && world.getBlockState(blockPos = pos.offset(direction.getOpposite())).isSideSolidFullSquare(world, blockPos, direction)) {
                return false;
            }
            FluidState fluidState = state.getFluidState();
            if (!fluidState.isEmpty() && !fluidState.isOf(Fluids.WATER)) {
                return false;
            }
            if (state.isIn(BlockTags.FIRE)) {
                return false;
            }
            return state.isReplaceable() || super.canGrow(world, pos, growPos, direction, state);
        }

        @Override
        public MultifaceGrower.GrowType[] getGrowTypes() {
            return this.growTypes;
        }

        @Override
        public boolean canGrow(BlockState state) {
            return !state.isOf(Blocks.SCULK_VEIN);
        }
    }
}


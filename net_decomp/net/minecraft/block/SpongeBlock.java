/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  com.mojang.serialization.MapCodec
 *  org.jspecify.annotations.Nullable
 */
package net.minecraft.block;

import com.mojang.serialization.MapCodec;
import net.minecraft.block.AbstractBlock;
import net.minecraft.block.Block;
import net.minecraft.block.BlockState;
import net.minecraft.block.Blocks;
import net.minecraft.block.FluidBlock;
import net.minecraft.block.FluidDrainable;
import net.minecraft.block.entity.BlockEntity;
import net.minecraft.fluid.FluidState;
import net.minecraft.registry.tag.FluidTags;
import net.minecraft.sound.SoundCategory;
import net.minecraft.sound.SoundEvents;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Direction;
import net.minecraft.world.World;
import net.minecraft.world.block.WireOrientation;
import org.jspecify.annotations.Nullable;

public class SpongeBlock
extends Block {
    public static final MapCodec<SpongeBlock> CODEC = SpongeBlock.createCodec(SpongeBlock::new);
    public static final int ABSORB_RADIUS = 6;
    public static final int ABSORB_LIMIT = 64;
    private static final Direction[] DIRECTIONS = Direction.values();

    public MapCodec<SpongeBlock> getCodec() {
        return CODEC;
    }

    protected SpongeBlock(AbstractBlock.Settings settings) {
        super(settings);
    }

    @Override
    protected void onBlockAdded(BlockState state, World world, BlockPos pos, BlockState oldState, boolean notify) {
        if (oldState.isOf(state.getBlock())) {
            return;
        }
        this.update(world, pos);
    }

    @Override
    protected void neighborUpdate(BlockState state, World world, BlockPos pos, Block sourceBlock, @Nullable WireOrientation wireOrientation, boolean notify) {
        this.update(world, pos);
        super.neighborUpdate(state, world, pos, sourceBlock, wireOrientation, notify);
    }

    protected void update(World world, BlockPos pos) {
        if (this.absorbWater(world, pos)) {
            world.setBlockState(pos, Blocks.WET_SPONGE.getDefaultState(), 2);
            world.playSound(null, pos, SoundEvents.BLOCK_SPONGE_ABSORB, SoundCategory.BLOCKS, 1.0f, 1.0f);
        }
    }

    private boolean absorbWater(World world, BlockPos pos) {
        return BlockPos.iterateRecursively(pos, 6, 65, (currentPos, queuer) -> {
            for (Direction direction : DIRECTIONS) {
                queuer.accept(currentPos.offset(direction));
            }
        }, currentPos -> {
            FluidDrainable fluidDrainable;
            if (currentPos.equals(pos)) {
                return BlockPos.IterationState.ACCEPT;
            }
            BlockState blockState = world.getBlockState((BlockPos)currentPos);
            FluidState fluidState = world.getFluidState((BlockPos)currentPos);
            if (!fluidState.isIn(FluidTags.WATER)) {
                return BlockPos.IterationState.SKIP;
            }
            Block block = blockState.getBlock();
            if (block instanceof FluidDrainable && !(fluidDrainable = (FluidDrainable)((Object)block)).tryDrainFluid(null, world, (BlockPos)currentPos, blockState).isEmpty()) {
                return BlockPos.IterationState.ACCEPT;
            }
            if (blockState.getBlock() instanceof FluidBlock) {
                world.setBlockState((BlockPos)currentPos, Blocks.AIR.getDefaultState(), 3);
            } else if (blockState.isOf(Blocks.KELP) || blockState.isOf(Blocks.KELP_PLANT) || blockState.isOf(Blocks.SEAGRASS) || blockState.isOf(Blocks.TALL_SEAGRASS)) {
                BlockEntity blockEntity = blockState.hasBlockEntity() ? world.getBlockEntity((BlockPos)currentPos) : null;
                SpongeBlock.dropStacks(blockState, world, currentPos, blockEntity);
                world.setBlockState((BlockPos)currentPos, Blocks.AIR.getDefaultState(), 3);
            } else {
                return BlockPos.IterationState.SKIP;
            }
            return BlockPos.IterationState.ACCEPT;
        }) > 1;
    }
}


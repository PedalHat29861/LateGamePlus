/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  com.mojang.serialization.MapCodec
 */
package net.minecraft.block;

import com.mojang.serialization.MapCodec;
import net.minecraft.block.AbstractBlock;
import net.minecraft.block.Block;
import net.minecraft.block.BlockState;
import net.minecraft.block.Blocks;
import net.minecraft.block.Fertilizable;
import net.minecraft.block.MultifaceBlock;
import net.minecraft.block.ShapeContext;
import net.minecraft.registry.tag.BlockTags;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.sound.SoundCategory;
import net.minecraft.sound.SoundEvents;
import net.minecraft.state.StateManager;
import net.minecraft.state.property.BooleanProperty;
import net.minecraft.state.property.Properties;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Direction;
import net.minecraft.util.math.random.Random;
import net.minecraft.util.shape.VoxelShape;
import net.minecraft.world.BlockView;
import net.minecraft.world.World;
import net.minecraft.world.WorldView;
import net.minecraft.world.tick.ScheduledTickView;

public class HangingMossBlock
extends Block
implements Fertilizable {
    public static final MapCodec<HangingMossBlock> CODEC = HangingMossBlock.createCodec(HangingMossBlock::new);
    private static final VoxelShape SHAPE = Block.createColumnShape(14.0, 0.0, 16.0);
    private static final VoxelShape TIP_SHAPE = Block.createColumnShape(14.0, 2.0, 16.0);
    public static final BooleanProperty TIP = Properties.TIP;

    public MapCodec<HangingMossBlock> getCodec() {
        return CODEC;
    }

    public HangingMossBlock(AbstractBlock.Settings settings) {
        super(settings);
        this.setDefaultState((BlockState)((BlockState)this.stateManager.getDefaultState()).with(TIP, true));
    }

    @Override
    protected VoxelShape getOutlineShape(BlockState state, BlockView world, BlockPos pos, ShapeContext context) {
        return state.get(TIP) != false ? TIP_SHAPE : SHAPE;
    }

    @Override
    public void randomDisplayTick(BlockState state, World world, BlockPos pos, Random random) {
        BlockState blockState;
        if (random.nextInt(500) == 0 && ((blockState = world.getBlockState(pos.up())).isIn(BlockTags.PALE_OAK_LOGS) || blockState.isOf(Blocks.PALE_OAK_LEAVES))) {
            world.playSoundClient(pos.getX(), pos.getY(), pos.getZ(), SoundEvents.BLOCK_PALE_HANGING_MOSS_IDLE, SoundCategory.AMBIENT, 1.0f, 1.0f, false);
        }
    }

    @Override
    protected boolean isTransparent(BlockState state) {
        return true;
    }

    @Override
    protected boolean canPlaceAt(BlockState state, WorldView world, BlockPos pos) {
        return this.canPlaceAt(world, pos);
    }

    private boolean canPlaceAt(BlockView world, BlockPos pos) {
        BlockState blockState;
        BlockPos blockPos = pos.offset(Direction.UP);
        return MultifaceBlock.canGrowOn(world, Direction.UP, blockPos, blockState = world.getBlockState(blockPos)) || blockState.isOf(Blocks.PALE_HANGING_MOSS);
    }

    @Override
    protected BlockState getStateForNeighborUpdate(BlockState state, WorldView world, ScheduledTickView tickView, BlockPos pos, Direction direction, BlockPos neighborPos, BlockState neighborState, Random random) {
        if (!this.canPlaceAt(world, pos)) {
            tickView.scheduleBlockTick(pos, this, 1);
        }
        return (BlockState)state.with(TIP, !world.getBlockState(pos.down()).isOf(this));
    }

    @Override
    protected void scheduledTick(BlockState state, ServerWorld world, BlockPos pos, Random random) {
        if (!this.canPlaceAt(world, pos)) {
            world.breakBlock(pos, true);
        }
    }

    @Override
    protected void appendProperties(StateManager.Builder<Block, BlockState> builder) {
        builder.add(TIP);
    }

    @Override
    public boolean isFertilizable(WorldView world, BlockPos pos, BlockState state) {
        return this.canGrowInto(world.getBlockState(this.getTipPos(world, pos).down()));
    }

    private boolean canGrowInto(BlockState state) {
        return state.isAir();
    }

    public BlockPos getTipPos(BlockView world, BlockPos pos) {
        BlockState blockState;
        BlockPos.Mutable mutable = pos.mutableCopy();
        do {
            mutable.move(Direction.DOWN);
        } while ((blockState = world.getBlockState(mutable)).isOf(this));
        return ((BlockPos)mutable.offset(Direction.UP)).toImmutable();
    }

    @Override
    public boolean canGrow(World world, Random random, BlockPos pos, BlockState state) {
        return true;
    }

    @Override
    public void grow(ServerWorld world, Random random, BlockPos pos, BlockState state) {
        BlockPos blockPos = this.getTipPos(world, pos).down();
        if (!this.canGrowInto(world.getBlockState(blockPos))) {
            return;
        }
        world.setBlockState(blockPos, (BlockState)state.with(TIP, true));
    }
}


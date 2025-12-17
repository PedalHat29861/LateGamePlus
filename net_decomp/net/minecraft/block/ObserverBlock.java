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
import net.minecraft.block.FacingBlock;
import net.minecraft.item.ItemPlacementContext;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.state.StateManager;
import net.minecraft.state.property.BooleanProperty;
import net.minecraft.state.property.Properties;
import net.minecraft.util.BlockMirror;
import net.minecraft.util.BlockRotation;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Direction;
import net.minecraft.util.math.random.Random;
import net.minecraft.world.BlockView;
import net.minecraft.world.World;
import net.minecraft.world.WorldView;
import net.minecraft.world.block.OrientationHelper;
import net.minecraft.world.block.WireOrientation;
import net.minecraft.world.tick.ScheduledTickView;
import net.minecraft.world.tick.WorldTickScheduler;

public class ObserverBlock
extends FacingBlock {
    public static final MapCodec<ObserverBlock> CODEC = ObserverBlock.createCodec(ObserverBlock::new);
    public static final BooleanProperty POWERED = Properties.POWERED;

    public MapCodec<ObserverBlock> getCodec() {
        return CODEC;
    }

    public ObserverBlock(AbstractBlock.Settings settings) {
        super(settings);
        this.setDefaultState((BlockState)((BlockState)((BlockState)this.stateManager.getDefaultState()).with(FACING, Direction.SOUTH)).with(POWERED, false));
    }

    @Override
    protected void appendProperties(StateManager.Builder<Block, BlockState> builder) {
        builder.add(FACING, POWERED);
    }

    @Override
    protected BlockState rotate(BlockState state, BlockRotation rotation) {
        return (BlockState)state.with(FACING, rotation.rotate((Direction)state.get(FACING)));
    }

    @Override
    protected BlockState mirror(BlockState state, BlockMirror mirror) {
        return state.rotate(mirror.getRotation((Direction)state.get(FACING)));
    }

    @Override
    protected void scheduledTick(BlockState state, ServerWorld world, BlockPos pos, Random random) {
        if (state.get(POWERED).booleanValue()) {
            world.setBlockState(pos, (BlockState)state.with(POWERED, false), 2);
        } else {
            world.setBlockState(pos, (BlockState)state.with(POWERED, true), 2);
            world.scheduleBlockTick(pos, this, 2);
        }
        this.updateNeighbors(world, pos, state);
    }

    @Override
    protected BlockState getStateForNeighborUpdate(BlockState state, WorldView world, ScheduledTickView tickView, BlockPos pos, Direction direction, BlockPos neighborPos, BlockState neighborState, Random random) {
        if (state.get(FACING) == direction && !state.get(POWERED).booleanValue()) {
            this.scheduleTick(world, tickView, pos);
        }
        return super.getStateForNeighborUpdate(state, world, tickView, pos, direction, neighborPos, neighborState, random);
    }

    private void scheduleTick(WorldView world, ScheduledTickView tickView, BlockPos pos) {
        if (!world.isClient() && !tickView.getBlockTickScheduler().isQueued(pos, this)) {
            tickView.scheduleBlockTick(pos, this, 2);
        }
    }

    protected void updateNeighbors(World world, BlockPos pos, BlockState state) {
        Direction direction = (Direction)state.get(FACING);
        BlockPos blockPos = pos.offset(direction.getOpposite());
        WireOrientation wireOrientation = OrientationHelper.getEmissionOrientation(world, direction.getOpposite(), null);
        world.updateNeighbor(blockPos, this, wireOrientation);
        world.updateNeighborsExcept(blockPos, this, direction, wireOrientation);
    }

    @Override
    protected boolean emitsRedstonePower(BlockState state) {
        return true;
    }

    @Override
    protected int getStrongRedstonePower(BlockState state, BlockView world, BlockPos pos, Direction direction) {
        return state.getWeakRedstonePower(world, pos, direction);
    }

    @Override
    protected int getWeakRedstonePower(BlockState state, BlockView world, BlockPos pos, Direction direction) {
        if (state.get(POWERED).booleanValue() && state.get(FACING) == direction) {
            return 15;
        }
        return 0;
    }

    @Override
    protected void onBlockAdded(BlockState state, World world, BlockPos pos, BlockState oldState, boolean notify) {
        if (state.isOf(oldState.getBlock())) {
            return;
        }
        if (!world.isClient() && state.get(POWERED).booleanValue() && !world.getBlockTickScheduler().isQueued(pos, this)) {
            BlockState blockState = (BlockState)state.with(POWERED, false);
            world.setBlockState(pos, blockState, 18);
            this.updateNeighbors(world, pos, blockState);
        }
    }

    @Override
    protected void onStateReplaced(BlockState state, ServerWorld world, BlockPos pos, boolean moved) {
        if (state.get(POWERED).booleanValue() && ((WorldTickScheduler)world.getBlockTickScheduler()).isQueued(pos, this)) {
            this.updateNeighbors(world, pos, (BlockState)state.with(POWERED, false));
        }
    }

    @Override
    public BlockState getPlacementState(ItemPlacementContext ctx) {
        return (BlockState)this.getDefaultState().with(FACING, ctx.getPlayerLookDirection().getOpposite().getOpposite());
    }
}


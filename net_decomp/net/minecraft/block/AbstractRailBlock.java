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
import net.minecraft.block.RailPlacementHelper;
import net.minecraft.block.ShapeContext;
import net.minecraft.block.Waterloggable;
import net.minecraft.block.enums.RailShape;
import net.minecraft.fluid.FluidState;
import net.minecraft.fluid.Fluids;
import net.minecraft.item.ItemPlacementContext;
import net.minecraft.registry.tag.BlockTags;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.state.property.BooleanProperty;
import net.minecraft.state.property.Properties;
import net.minecraft.state.property.Property;
import net.minecraft.util.BlockMirror;
import net.minecraft.util.BlockRotation;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Direction;
import net.minecraft.util.math.random.Random;
import net.minecraft.util.shape.VoxelShape;
import net.minecraft.world.BlockView;
import net.minecraft.world.World;
import net.minecraft.world.WorldView;
import net.minecraft.world.block.WireOrientation;
import net.minecraft.world.tick.ScheduledTickView;
import org.jspecify.annotations.Nullable;

public abstract class AbstractRailBlock
extends Block
implements Waterloggable {
    public static final BooleanProperty WATERLOGGED = Properties.WATERLOGGED;
    private static final VoxelShape STRAIGHT_SHAPE = Block.createColumnShape(16.0, 0.0, 2.0);
    private static final VoxelShape ASCENDING_SHAPE = Block.createColumnShape(16.0, 0.0, 8.0);
    private final boolean forbidCurves;

    public static boolean isRail(World world, BlockPos pos) {
        return AbstractRailBlock.isRail(world.getBlockState(pos));
    }

    public static boolean isRail(BlockState state) {
        return state.isIn(BlockTags.RAILS) && state.getBlock() instanceof AbstractRailBlock;
    }

    protected AbstractRailBlock(boolean forbidCurves, AbstractBlock.Settings settings) {
        super(settings);
        this.forbidCurves = forbidCurves;
    }

    protected abstract MapCodec<? extends AbstractRailBlock> getCodec();

    public boolean cannotMakeCurves() {
        return this.forbidCurves;
    }

    @Override
    protected VoxelShape getOutlineShape(BlockState state, BlockView world, BlockPos pos, ShapeContext context) {
        return state.get(this.getShapeProperty()).isAscending() ? ASCENDING_SHAPE : STRAIGHT_SHAPE;
    }

    @Override
    protected boolean canPlaceAt(BlockState state, WorldView world, BlockPos pos) {
        return AbstractRailBlock.hasTopRim(world, pos.down());
    }

    @Override
    protected void onBlockAdded(BlockState state, World world, BlockPos pos, BlockState oldState, boolean notify) {
        if (oldState.isOf(state.getBlock())) {
            return;
        }
        this.updateCurves(state, world, pos, notify);
    }

    protected BlockState updateCurves(BlockState state, World world, BlockPos pos, boolean notify) {
        state = this.updateBlockState(world, pos, state, true);
        if (this.forbidCurves) {
            world.updateNeighbor(state, pos, this, null, notify);
        }
        return state;
    }

    @Override
    protected void neighborUpdate(BlockState state, World world, BlockPos pos, Block sourceBlock, @Nullable WireOrientation wireOrientation, boolean notify) {
        if (world.isClient() || !world.getBlockState(pos).isOf(this)) {
            return;
        }
        RailShape railShape = state.get(this.getShapeProperty());
        if (AbstractRailBlock.shouldDropRail(pos, world, railShape)) {
            AbstractRailBlock.dropStacks(state, world, pos);
            world.removeBlock(pos, notify);
        } else {
            this.updateBlockState(state, world, pos, sourceBlock);
        }
    }

    private static boolean shouldDropRail(BlockPos pos, World world, RailShape shape) {
        if (!AbstractRailBlock.hasTopRim(world, pos.down())) {
            return true;
        }
        switch (shape) {
            case ASCENDING_EAST: {
                return !AbstractRailBlock.hasTopRim(world, pos.east());
            }
            case ASCENDING_WEST: {
                return !AbstractRailBlock.hasTopRim(world, pos.west());
            }
            case ASCENDING_NORTH: {
                return !AbstractRailBlock.hasTopRim(world, pos.north());
            }
            case ASCENDING_SOUTH: {
                return !AbstractRailBlock.hasTopRim(world, pos.south());
            }
        }
        return false;
    }

    protected void updateBlockState(BlockState state, World world, BlockPos pos, Block neighbor) {
    }

    protected BlockState updateBlockState(World world, BlockPos pos, BlockState state, boolean forceUpdate) {
        if (world.isClient()) {
            return state;
        }
        RailShape railShape = state.get(this.getShapeProperty());
        return new RailPlacementHelper(world, pos, state).updateBlockState(world.isReceivingRedstonePower(pos), forceUpdate, railShape).getBlockState();
    }

    @Override
    protected void onStateReplaced(BlockState state, ServerWorld world, BlockPos pos, boolean moved) {
        if (moved) {
            return;
        }
        if (state.get(this.getShapeProperty()).isAscending()) {
            world.updateNeighbors(pos.up(), this);
        }
        if (this.forbidCurves) {
            world.updateNeighbors(pos, this);
            world.updateNeighbors(pos.down(), this);
        }
    }

    @Override
    public BlockState getPlacementState(ItemPlacementContext ctx) {
        FluidState fluidState = ctx.getWorld().getFluidState(ctx.getBlockPos());
        boolean bl = fluidState.getFluid() == Fluids.WATER;
        BlockState blockState = super.getDefaultState();
        Direction direction = ctx.getHorizontalPlayerFacing();
        boolean bl2 = direction == Direction.EAST || direction == Direction.WEST;
        return (BlockState)((BlockState)blockState.with(this.getShapeProperty(), bl2 ? RailShape.EAST_WEST : RailShape.NORTH_SOUTH)).with(WATERLOGGED, bl);
    }

    public abstract Property<RailShape> getShapeProperty();

    protected RailShape rotateShape(RailShape shape, BlockRotation rotation) {
        return switch (rotation) {
            case BlockRotation.CLOCKWISE_180 -> {
                switch (shape) {
                    default: {
                        throw new MatchException(null, null);
                    }
                    case NORTH_SOUTH: {
                        yield RailShape.NORTH_SOUTH;
                    }
                    case EAST_WEST: {
                        yield RailShape.EAST_WEST;
                    }
                    case ASCENDING_EAST: {
                        yield RailShape.ASCENDING_WEST;
                    }
                    case ASCENDING_WEST: {
                        yield RailShape.ASCENDING_EAST;
                    }
                    case ASCENDING_NORTH: {
                        yield RailShape.ASCENDING_SOUTH;
                    }
                    case ASCENDING_SOUTH: {
                        yield RailShape.ASCENDING_NORTH;
                    }
                    case SOUTH_EAST: {
                        yield RailShape.NORTH_WEST;
                    }
                    case SOUTH_WEST: {
                        yield RailShape.NORTH_EAST;
                    }
                    case NORTH_WEST: {
                        yield RailShape.SOUTH_EAST;
                    }
                    case NORTH_EAST: 
                }
                yield RailShape.SOUTH_WEST;
            }
            case BlockRotation.COUNTERCLOCKWISE_90 -> {
                switch (shape) {
                    default: {
                        throw new MatchException(null, null);
                    }
                    case NORTH_SOUTH: {
                        yield RailShape.EAST_WEST;
                    }
                    case EAST_WEST: {
                        yield RailShape.NORTH_SOUTH;
                    }
                    case ASCENDING_EAST: {
                        yield RailShape.ASCENDING_NORTH;
                    }
                    case ASCENDING_WEST: {
                        yield RailShape.ASCENDING_SOUTH;
                    }
                    case ASCENDING_NORTH: {
                        yield RailShape.ASCENDING_WEST;
                    }
                    case ASCENDING_SOUTH: {
                        yield RailShape.ASCENDING_EAST;
                    }
                    case SOUTH_EAST: {
                        yield RailShape.NORTH_EAST;
                    }
                    case SOUTH_WEST: {
                        yield RailShape.SOUTH_EAST;
                    }
                    case NORTH_WEST: {
                        yield RailShape.SOUTH_WEST;
                    }
                    case NORTH_EAST: 
                }
                yield RailShape.NORTH_WEST;
            }
            case BlockRotation.CLOCKWISE_90 -> {
                switch (shape) {
                    default: {
                        throw new MatchException(null, null);
                    }
                    case NORTH_SOUTH: {
                        yield RailShape.EAST_WEST;
                    }
                    case EAST_WEST: {
                        yield RailShape.NORTH_SOUTH;
                    }
                    case ASCENDING_EAST: {
                        yield RailShape.ASCENDING_SOUTH;
                    }
                    case ASCENDING_WEST: {
                        yield RailShape.ASCENDING_NORTH;
                    }
                    case ASCENDING_NORTH: {
                        yield RailShape.ASCENDING_EAST;
                    }
                    case ASCENDING_SOUTH: {
                        yield RailShape.ASCENDING_WEST;
                    }
                    case SOUTH_EAST: {
                        yield RailShape.SOUTH_WEST;
                    }
                    case SOUTH_WEST: {
                        yield RailShape.NORTH_WEST;
                    }
                    case NORTH_WEST: {
                        yield RailShape.NORTH_EAST;
                    }
                    case NORTH_EAST: 
                }
                yield RailShape.SOUTH_EAST;
            }
            default -> shape;
        };
    }

    protected RailShape mirrorShape(RailShape shape, BlockMirror mirror) {
        return switch (mirror) {
            case BlockMirror.LEFT_RIGHT -> {
                switch (shape) {
                    case ASCENDING_NORTH: {
                        yield RailShape.ASCENDING_SOUTH;
                    }
                    case ASCENDING_SOUTH: {
                        yield RailShape.ASCENDING_NORTH;
                    }
                    case SOUTH_EAST: {
                        yield RailShape.NORTH_EAST;
                    }
                    case SOUTH_WEST: {
                        yield RailShape.NORTH_WEST;
                    }
                    case NORTH_WEST: {
                        yield RailShape.SOUTH_WEST;
                    }
                    case NORTH_EAST: {
                        yield RailShape.SOUTH_EAST;
                    }
                }
                yield shape;
            }
            case BlockMirror.FRONT_BACK -> {
                switch (shape) {
                    case ASCENDING_EAST: {
                        yield RailShape.ASCENDING_WEST;
                    }
                    case ASCENDING_WEST: {
                        yield RailShape.ASCENDING_EAST;
                    }
                    case SOUTH_EAST: {
                        yield RailShape.SOUTH_WEST;
                    }
                    case SOUTH_WEST: {
                        yield RailShape.SOUTH_EAST;
                    }
                    case NORTH_WEST: {
                        yield RailShape.NORTH_EAST;
                    }
                    case NORTH_EAST: {
                        yield RailShape.NORTH_WEST;
                    }
                }
                yield shape;
            }
            default -> shape;
        };
    }

    @Override
    protected BlockState getStateForNeighborUpdate(BlockState state, WorldView world, ScheduledTickView tickView, BlockPos pos, Direction direction, BlockPos neighborPos, BlockState neighborState, Random random) {
        if (state.get(WATERLOGGED).booleanValue()) {
            tickView.scheduleFluidTick(pos, Fluids.WATER, Fluids.WATER.getTickRate(world));
        }
        return super.getStateForNeighborUpdate(state, world, tickView, pos, direction, neighborPos, neighborState, random);
    }

    @Override
    protected FluidState getFluidState(BlockState state) {
        if (state.get(WATERLOGGED).booleanValue()) {
            return Fluids.WATER.getStill(false);
        }
        return super.getFluidState(state);
    }
}


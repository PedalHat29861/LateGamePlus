/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  com.mojang.datafixers.kinds.App
 *  com.mojang.datafixers.kinds.Applicative
 *  com.mojang.serialization.MapCodec
 *  com.mojang.serialization.codecs.RecordCodecBuilder
 */
package net.minecraft.block;

import com.mojang.datafixers.kinds.App;
import com.mojang.datafixers.kinds.Applicative;
import com.mojang.serialization.MapCodec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import java.util.Map;
import net.minecraft.block.AbstractBlock;
import net.minecraft.block.Block;
import net.minecraft.block.BlockState;
import net.minecraft.block.HorizontalFacingBlock;
import net.minecraft.block.ShapeContext;
import net.minecraft.block.Waterloggable;
import net.minecraft.block.enums.BlockHalf;
import net.minecraft.block.enums.StairShape;
import net.minecraft.entity.ai.pathing.NavigationType;
import net.minecraft.fluid.FluidState;
import net.minecraft.fluid.Fluids;
import net.minecraft.item.ItemPlacementContext;
import net.minecraft.state.StateManager;
import net.minecraft.state.property.BooleanProperty;
import net.minecraft.state.property.EnumProperty;
import net.minecraft.state.property.Properties;
import net.minecraft.util.BlockMirror;
import net.minecraft.util.BlockRotation;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Direction;
import net.minecraft.util.math.DirectionTransformation;
import net.minecraft.util.math.random.Random;
import net.minecraft.util.shape.VoxelShape;
import net.minecraft.util.shape.VoxelShapes;
import net.minecraft.world.BlockView;
import net.minecraft.world.WorldView;
import net.minecraft.world.tick.ScheduledTickView;

public class StairsBlock
extends Block
implements Waterloggable {
    public static final MapCodec<StairsBlock> CODEC = RecordCodecBuilder.mapCodec(instance -> instance.group((App)BlockState.CODEC.fieldOf("base_state").forGetter(block -> block.baseBlockState), StairsBlock.createSettingsCodec()).apply((Applicative)instance, StairsBlock::new));
    public static final EnumProperty<Direction> FACING = HorizontalFacingBlock.FACING;
    public static final EnumProperty<BlockHalf> HALF = Properties.BLOCK_HALF;
    public static final EnumProperty<StairShape> SHAPE = Properties.STAIR_SHAPE;
    public static final BooleanProperty WATERLOGGED = Properties.WATERLOGGED;
    private static final VoxelShape OUTER_SHAPE = VoxelShapes.union(Block.createColumnShape(16.0, 0.0, 8.0), Block.createCuboidShape(0.0, 8.0, 0.0, 8.0, 16.0, 8.0));
    private static final VoxelShape STRAIGHT_SHAPE = VoxelShapes.union(OUTER_SHAPE, VoxelShapes.transform(OUTER_SHAPE, DirectionTransformation.field_64511));
    private static final VoxelShape INNER_SHAPE = VoxelShapes.union(STRAIGHT_SHAPE, VoxelShapes.transform(STRAIGHT_SHAPE, DirectionTransformation.field_64511));
    private static final Map<Direction, VoxelShape> OUTER_BOTTOM_SHAPES = VoxelShapes.createHorizontalFacingShapeMap(OUTER_SHAPE);
    private static final Map<Direction, VoxelShape> STRAIGHT_BOTTOM_SHAPES = VoxelShapes.createHorizontalFacingShapeMap(STRAIGHT_SHAPE);
    private static final Map<Direction, VoxelShape> INNER_BOTTOM_SHAPES = VoxelShapes.createHorizontalFacingShapeMap(INNER_SHAPE);
    private static final Map<Direction, VoxelShape> OUTER_TOP_SHAPES = VoxelShapes.createHorizontalFacingShapeMap(OUTER_SHAPE, DirectionTransformation.INVERT_Y);
    private static final Map<Direction, VoxelShape> STRAIGHT_TOP_SHAPES = VoxelShapes.createHorizontalFacingShapeMap(STRAIGHT_SHAPE, DirectionTransformation.INVERT_Y);
    private static final Map<Direction, VoxelShape> INNER_TOP_SHAPES = VoxelShapes.createHorizontalFacingShapeMap(INNER_SHAPE, DirectionTransformation.INVERT_Y);
    private final Block baseBlock;
    protected final BlockState baseBlockState;

    public MapCodec<? extends StairsBlock> getCodec() {
        return CODEC;
    }

    protected StairsBlock(BlockState baseBlockState, AbstractBlock.Settings settings) {
        super(settings);
        this.setDefaultState((BlockState)((BlockState)((BlockState)((BlockState)((BlockState)this.stateManager.getDefaultState()).with(FACING, Direction.NORTH)).with(HALF, BlockHalf.BOTTOM)).with(SHAPE, StairShape.STRAIGHT)).with(WATERLOGGED, false));
        this.baseBlock = baseBlockState.getBlock();
        this.baseBlockState = baseBlockState;
    }

    @Override
    protected boolean hasSidedTransparency(BlockState state) {
        return true;
    }

    @Override
    protected VoxelShape getOutlineShape(BlockState state, BlockView world, BlockPos pos, ShapeContext context) {
        boolean bl = state.get(HALF) == BlockHalf.BOTTOM;
        Direction direction = state.get(FACING);
        return (switch (state.get(SHAPE)) {
            default -> throw new MatchException(null, null);
            case StairShape.STRAIGHT -> {
                if (bl) {
                    yield STRAIGHT_BOTTOM_SHAPES;
                }
                yield STRAIGHT_TOP_SHAPES;
            }
            case StairShape.INNER_RIGHT, StairShape.INNER_LEFT -> {
                if (bl) {
                    yield INNER_BOTTOM_SHAPES;
                }
                yield INNER_TOP_SHAPES;
            }
            case StairShape.OUTER_LEFT, StairShape.OUTER_RIGHT -> bl ? OUTER_BOTTOM_SHAPES : OUTER_TOP_SHAPES;
        }).get(switch (state.get(SHAPE)) {
            default -> throw new MatchException(null, null);
            case StairShape.STRAIGHT, StairShape.OUTER_LEFT, StairShape.INNER_RIGHT -> direction;
            case StairShape.INNER_LEFT -> direction.rotateYCounterclockwise();
            case StairShape.OUTER_RIGHT -> direction.rotateYClockwise();
        });
    }

    @Override
    public float getBlastResistance() {
        return this.baseBlock.getBlastResistance();
    }

    @Override
    public BlockState getPlacementState(ItemPlacementContext ctx) {
        Direction direction = ctx.getSide();
        BlockPos blockPos = ctx.getBlockPos();
        FluidState fluidState = ctx.getWorld().getFluidState(blockPos);
        BlockState blockState = (BlockState)((BlockState)((BlockState)this.getDefaultState().with(FACING, ctx.getHorizontalPlayerFacing())).with(HALF, direction == Direction.DOWN || direction != Direction.UP && ctx.getHitPos().y - (double)blockPos.getY() > 0.5 ? BlockHalf.TOP : BlockHalf.BOTTOM)).with(WATERLOGGED, fluidState.getFluid() == Fluids.WATER);
        return (BlockState)blockState.with(SHAPE, StairsBlock.getStairShape(blockState, ctx.getWorld(), blockPos));
    }

    @Override
    protected BlockState getStateForNeighborUpdate(BlockState state, WorldView world, ScheduledTickView tickView, BlockPos pos, Direction direction, BlockPos neighborPos, BlockState neighborState, Random random) {
        if (state.get(WATERLOGGED).booleanValue()) {
            tickView.scheduleFluidTick(pos, Fluids.WATER, Fluids.WATER.getTickRate(world));
        }
        if (direction.getAxis().isHorizontal()) {
            return (BlockState)state.with(SHAPE, StairsBlock.getStairShape(state, world, pos));
        }
        return super.getStateForNeighborUpdate(state, world, tickView, pos, direction, neighborPos, neighborState, random);
    }

    private static StairShape getStairShape(BlockState state, BlockView world, BlockPos pos) {
        Direction direction3;
        Direction direction2;
        Direction direction = state.get(FACING);
        BlockState blockState = world.getBlockState(pos.offset(direction));
        if (StairsBlock.isStairs(blockState) && state.get(HALF) == blockState.get(HALF) && (direction2 = blockState.get(FACING)).getAxis() != state.get(FACING).getAxis() && StairsBlock.isDifferentOrientation(state, world, pos, direction2.getOpposite())) {
            if (direction2 == direction.rotateYCounterclockwise()) {
                return StairShape.OUTER_LEFT;
            }
            return StairShape.OUTER_RIGHT;
        }
        BlockState blockState2 = world.getBlockState(pos.offset(direction.getOpposite()));
        if (StairsBlock.isStairs(blockState2) && state.get(HALF) == blockState2.get(HALF) && (direction3 = blockState2.get(FACING)).getAxis() != state.get(FACING).getAxis() && StairsBlock.isDifferentOrientation(state, world, pos, direction3)) {
            if (direction3 == direction.rotateYCounterclockwise()) {
                return StairShape.INNER_LEFT;
            }
            return StairShape.INNER_RIGHT;
        }
        return StairShape.STRAIGHT;
    }

    private static boolean isDifferentOrientation(BlockState state, BlockView world, BlockPos pos, Direction dir) {
        BlockState blockState = world.getBlockState(pos.offset(dir));
        return !StairsBlock.isStairs(blockState) || blockState.get(FACING) != state.get(FACING) || blockState.get(HALF) != state.get(HALF);
    }

    public static boolean isStairs(BlockState state) {
        return state.getBlock() instanceof StairsBlock;
    }

    @Override
    protected BlockState rotate(BlockState state, BlockRotation rotation) {
        return (BlockState)state.with(FACING, rotation.rotate(state.get(FACING)));
    }

    @Override
    protected BlockState mirror(BlockState state, BlockMirror mirror) {
        Direction direction = state.get(FACING);
        StairShape stairShape = state.get(SHAPE);
        switch (mirror) {
            case LEFT_RIGHT: {
                if (direction.getAxis() != Direction.Axis.Z) break;
                switch (stairShape) {
                    case INNER_LEFT: {
                        return (BlockState)state.rotate(BlockRotation.CLOCKWISE_180).with(SHAPE, StairShape.INNER_RIGHT);
                    }
                    case INNER_RIGHT: {
                        return (BlockState)state.rotate(BlockRotation.CLOCKWISE_180).with(SHAPE, StairShape.INNER_LEFT);
                    }
                    case OUTER_LEFT: {
                        return (BlockState)state.rotate(BlockRotation.CLOCKWISE_180).with(SHAPE, StairShape.OUTER_RIGHT);
                    }
                    case OUTER_RIGHT: {
                        return (BlockState)state.rotate(BlockRotation.CLOCKWISE_180).with(SHAPE, StairShape.OUTER_LEFT);
                    }
                }
                return state.rotate(BlockRotation.CLOCKWISE_180);
            }
            case FRONT_BACK: {
                if (direction.getAxis() != Direction.Axis.X) break;
                switch (stairShape) {
                    case INNER_LEFT: {
                        return (BlockState)state.rotate(BlockRotation.CLOCKWISE_180).with(SHAPE, StairShape.INNER_LEFT);
                    }
                    case INNER_RIGHT: {
                        return (BlockState)state.rotate(BlockRotation.CLOCKWISE_180).with(SHAPE, StairShape.INNER_RIGHT);
                    }
                    case OUTER_LEFT: {
                        return (BlockState)state.rotate(BlockRotation.CLOCKWISE_180).with(SHAPE, StairShape.OUTER_RIGHT);
                    }
                    case OUTER_RIGHT: {
                        return (BlockState)state.rotate(BlockRotation.CLOCKWISE_180).with(SHAPE, StairShape.OUTER_LEFT);
                    }
                    case STRAIGHT: {
                        return state.rotate(BlockRotation.CLOCKWISE_180);
                    }
                }
                break;
            }
        }
        return super.mirror(state, mirror);
    }

    @Override
    protected void appendProperties(StateManager.Builder<Block, BlockState> builder) {
        builder.add(FACING, HALF, SHAPE, WATERLOGGED);
    }

    @Override
    protected FluidState getFluidState(BlockState state) {
        if (state.get(WATERLOGGED).booleanValue()) {
            return Fluids.WATER.getStill(false);
        }
        return super.getFluidState(state);
    }

    @Override
    protected boolean canPathfindThrough(BlockState state, NavigationType type) {
        return false;
    }
}


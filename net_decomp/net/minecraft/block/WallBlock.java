/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  com.google.common.collect.ImmutableMap
 *  com.google.common.collect.Maps
 *  com.mojang.serialization.MapCodec
 */
package net.minecraft.block;

import com.google.common.collect.ImmutableMap;
import com.google.common.collect.Maps;
import com.mojang.serialization.MapCodec;
import java.util.Map;
import java.util.function.Function;
import net.minecraft.block.AbstractBlock;
import net.minecraft.block.Block;
import net.minecraft.block.BlockState;
import net.minecraft.block.FenceGateBlock;
import net.minecraft.block.PaneBlock;
import net.minecraft.block.ShapeContext;
import net.minecraft.block.Waterloggable;
import net.minecraft.block.enums.WallShape;
import net.minecraft.entity.ai.pathing.NavigationType;
import net.minecraft.fluid.FluidState;
import net.minecraft.fluid.Fluids;
import net.minecraft.item.ItemPlacementContext;
import net.minecraft.registry.tag.BlockTags;
import net.minecraft.state.StateManager;
import net.minecraft.state.property.BooleanProperty;
import net.minecraft.state.property.EnumProperty;
import net.minecraft.state.property.Properties;
import net.minecraft.state.property.Property;
import net.minecraft.util.BlockMirror;
import net.minecraft.util.BlockRotation;
import net.minecraft.util.function.BooleanBiFunction;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Direction;
import net.minecraft.util.math.random.Random;
import net.minecraft.util.shape.VoxelShape;
import net.minecraft.util.shape.VoxelShapes;
import net.minecraft.world.BlockView;
import net.minecraft.world.World;
import net.minecraft.world.WorldView;
import net.minecraft.world.tick.ScheduledTickView;

public class WallBlock
extends Block
implements Waterloggable {
    public static final MapCodec<WallBlock> CODEC = WallBlock.createCodec(WallBlock::new);
    public static final BooleanProperty UP = Properties.UP;
    public static final EnumProperty<WallShape> EAST_WALL_SHAPE = Properties.EAST_WALL_SHAPE;
    public static final EnumProperty<WallShape> NORTH_WALL_SHAPE = Properties.NORTH_WALL_SHAPE;
    public static final EnumProperty<WallShape> SOUTH_WALL_SHAPE = Properties.SOUTH_WALL_SHAPE;
    public static final EnumProperty<WallShape> WEST_WALL_SHAPE = Properties.WEST_WALL_SHAPE;
    public static final Map<Direction, EnumProperty<WallShape>> WALL_SHAPE_PROPERTIES_BY_DIRECTION = ImmutableMap.copyOf((Map)Maps.newEnumMap(Map.of(Direction.NORTH, NORTH_WALL_SHAPE, Direction.EAST, EAST_WALL_SHAPE, Direction.SOUTH, SOUTH_WALL_SHAPE, Direction.WEST, WEST_WALL_SHAPE)));
    public static final BooleanProperty WATERLOGGED = Properties.WATERLOGGED;
    private final Function<BlockState, VoxelShape> outlineShapeFunction;
    private final Function<BlockState, VoxelShape> collisionShapeFunction;
    private static final VoxelShape POST_SHAPE_FOR_TALL_TEST = Block.createColumnShape(2.0, 0.0, 16.0);
    private static final Map<Direction, VoxelShape> WALL_SHAPES_FOR_TALL_TEST_BY_DIRECTION = VoxelShapes.createHorizontalFacingShapeMap(Block.createCuboidZShape(2.0, 16.0, 0.0, 9.0));

    public MapCodec<WallBlock> getCodec() {
        return CODEC;
    }

    public WallBlock(AbstractBlock.Settings settings) {
        super(settings);
        this.setDefaultState((BlockState)((BlockState)((BlockState)((BlockState)((BlockState)((BlockState)((BlockState)this.stateManager.getDefaultState()).with(UP, true)).with(NORTH_WALL_SHAPE, WallShape.NONE)).with(EAST_WALL_SHAPE, WallShape.NONE)).with(SOUTH_WALL_SHAPE, WallShape.NONE)).with(WEST_WALL_SHAPE, WallShape.NONE)).with(WATERLOGGED, false));
        this.outlineShapeFunction = this.createShapeFunction(16.0f, 14.0f);
        this.collisionShapeFunction = this.createShapeFunction(24.0f, 24.0f);
    }

    private Function<BlockState, VoxelShape> createShapeFunction(float tallHeight, float lowHeight) {
        VoxelShape voxelShape = Block.createColumnShape(8.0, 0.0, tallHeight);
        int i = 6;
        Map<Direction, VoxelShape> map = VoxelShapes.createHorizontalFacingShapeMap(Block.createCuboidZShape(6.0, 0.0, lowHeight, 0.0, 11.0));
        Map<Direction, VoxelShape> map2 = VoxelShapes.createHorizontalFacingShapeMap(Block.createCuboidZShape(6.0, 0.0, tallHeight, 0.0, 11.0));
        return this.createShapeFunction(state -> {
            VoxelShape voxelShape2 = state.get(UP) != false ? voxelShape : VoxelShapes.empty();
            for (Map.Entry<Direction, EnumProperty<WallShape>> entry : WALL_SHAPE_PROPERTIES_BY_DIRECTION.entrySet()) {
                voxelShape2 = VoxelShapes.union(voxelShape2, switch ((WallShape)state.get(entry.getValue())) {
                    default -> throw new MatchException(null, null);
                    case WallShape.NONE -> VoxelShapes.empty();
                    case WallShape.LOW -> (VoxelShape)map.get(entry.getKey());
                    case WallShape.TALL -> (VoxelShape)map2.get(entry.getKey());
                });
            }
            return voxelShape2;
        }, WATERLOGGED);
    }

    @Override
    protected VoxelShape getOutlineShape(BlockState state, BlockView world, BlockPos pos, ShapeContext context) {
        return this.outlineShapeFunction.apply(state);
    }

    @Override
    protected VoxelShape getCollisionShape(BlockState state, BlockView world, BlockPos pos, ShapeContext context) {
        return this.collisionShapeFunction.apply(state);
    }

    @Override
    protected boolean canPathfindThrough(BlockState state, NavigationType type) {
        return false;
    }

    private boolean shouldConnectTo(BlockState state, boolean faceFullSquare, Direction side) {
        Block block = state.getBlock();
        boolean bl = block instanceof FenceGateBlock && FenceGateBlock.canWallConnect(state, side);
        return state.isIn(BlockTags.WALLS) || !WallBlock.cannotConnect(state) && faceFullSquare || block instanceof PaneBlock || bl;
    }

    @Override
    public BlockState getPlacementState(ItemPlacementContext ctx) {
        World worldView = ctx.getWorld();
        BlockPos blockPos = ctx.getBlockPos();
        FluidState fluidState = ctx.getWorld().getFluidState(ctx.getBlockPos());
        BlockPos blockPos2 = blockPos.north();
        BlockPos blockPos3 = blockPos.east();
        BlockPos blockPos4 = blockPos.south();
        BlockPos blockPos5 = blockPos.west();
        BlockPos blockPos6 = blockPos.up();
        BlockState blockState = worldView.getBlockState(blockPos2);
        BlockState blockState2 = worldView.getBlockState(blockPos3);
        BlockState blockState3 = worldView.getBlockState(blockPos4);
        BlockState blockState4 = worldView.getBlockState(blockPos5);
        BlockState blockState5 = worldView.getBlockState(blockPos6);
        boolean bl = this.shouldConnectTo(blockState, blockState.isSideSolidFullSquare(worldView, blockPos2, Direction.SOUTH), Direction.SOUTH);
        boolean bl2 = this.shouldConnectTo(blockState2, blockState2.isSideSolidFullSquare(worldView, blockPos3, Direction.WEST), Direction.WEST);
        boolean bl3 = this.shouldConnectTo(blockState3, blockState3.isSideSolidFullSquare(worldView, blockPos4, Direction.NORTH), Direction.NORTH);
        boolean bl4 = this.shouldConnectTo(blockState4, blockState4.isSideSolidFullSquare(worldView, blockPos5, Direction.EAST), Direction.EAST);
        BlockState blockState6 = (BlockState)this.getDefaultState().with(WATERLOGGED, fluidState.getFluid() == Fluids.WATER);
        return this.getStateWith(worldView, blockState6, blockPos6, blockState5, bl, bl2, bl3, bl4);
    }

    @Override
    protected BlockState getStateForNeighborUpdate(BlockState state, WorldView world, ScheduledTickView tickView, BlockPos pos, Direction direction, BlockPos neighborPos, BlockState neighborState, Random random) {
        if (state.get(WATERLOGGED).booleanValue()) {
            tickView.scheduleFluidTick(pos, Fluids.WATER, Fluids.WATER.getTickRate(world));
        }
        if (direction == Direction.DOWN) {
            return super.getStateForNeighborUpdate(state, world, tickView, pos, direction, neighborPos, neighborState, random);
        }
        if (direction == Direction.UP) {
            return this.getStateAt(world, state, neighborPos, neighborState);
        }
        return this.getStateWithNeighbor(world, pos, state, neighborPos, neighborState, direction);
    }

    private static boolean isConnected(BlockState state, Property<WallShape> property) {
        return state.get(property) != WallShape.NONE;
    }

    private static boolean shouldUseTallShape(VoxelShape aboveShape, VoxelShape tallShape) {
        return !VoxelShapes.matchesAnywhere(tallShape, aboveShape, BooleanBiFunction.ONLY_FIRST);
    }

    private BlockState getStateAt(WorldView world, BlockState state, BlockPos pos, BlockState aboveState) {
        boolean bl = WallBlock.isConnected(state, NORTH_WALL_SHAPE);
        boolean bl2 = WallBlock.isConnected(state, EAST_WALL_SHAPE);
        boolean bl3 = WallBlock.isConnected(state, SOUTH_WALL_SHAPE);
        boolean bl4 = WallBlock.isConnected(state, WEST_WALL_SHAPE);
        return this.getStateWith(world, state, pos, aboveState, bl, bl2, bl3, bl4);
    }

    private BlockState getStateWithNeighbor(WorldView world, BlockPos pos, BlockState state, BlockPos neighborPos, BlockState neighborState, Direction direction) {
        Direction direction2 = direction.getOpposite();
        boolean bl = direction == Direction.NORTH ? this.shouldConnectTo(neighborState, neighborState.isSideSolidFullSquare(world, neighborPos, direction2), direction2) : WallBlock.isConnected(state, NORTH_WALL_SHAPE);
        boolean bl2 = direction == Direction.EAST ? this.shouldConnectTo(neighborState, neighborState.isSideSolidFullSquare(world, neighborPos, direction2), direction2) : WallBlock.isConnected(state, EAST_WALL_SHAPE);
        boolean bl3 = direction == Direction.SOUTH ? this.shouldConnectTo(neighborState, neighborState.isSideSolidFullSquare(world, neighborPos, direction2), direction2) : WallBlock.isConnected(state, SOUTH_WALL_SHAPE);
        boolean bl4 = direction == Direction.WEST ? this.shouldConnectTo(neighborState, neighborState.isSideSolidFullSquare(world, neighborPos, direction2), direction2) : WallBlock.isConnected(state, WEST_WALL_SHAPE);
        BlockPos blockPos = pos.up();
        BlockState blockState = world.getBlockState(blockPos);
        return this.getStateWith(world, state, blockPos, blockState, bl, bl2, bl3, bl4);
    }

    private BlockState getStateWith(WorldView world, BlockState state, BlockPos pos, BlockState aboveState, boolean north, boolean east, boolean south, boolean west) {
        VoxelShape voxelShape = aboveState.getCollisionShape(world, pos).getFace(Direction.DOWN);
        BlockState blockState = this.getStateWith(state, north, east, south, west, voxelShape);
        return (BlockState)blockState.with(UP, this.shouldHavePost(blockState, aboveState, voxelShape));
    }

    private boolean shouldHavePost(BlockState state, BlockState aboveState, VoxelShape aboveShape) {
        boolean bl7;
        boolean bl6;
        boolean bl;
        boolean bl2 = bl = aboveState.getBlock() instanceof WallBlock && aboveState.get(UP) != false;
        if (bl) {
            return true;
        }
        WallShape wallShape = state.get(NORTH_WALL_SHAPE);
        WallShape wallShape2 = state.get(SOUTH_WALL_SHAPE);
        WallShape wallShape3 = state.get(EAST_WALL_SHAPE);
        WallShape wallShape4 = state.get(WEST_WALL_SHAPE);
        boolean bl22 = wallShape2 == WallShape.NONE;
        boolean bl3 = wallShape4 == WallShape.NONE;
        boolean bl4 = wallShape3 == WallShape.NONE;
        boolean bl5 = wallShape == WallShape.NONE;
        boolean bl8 = bl6 = bl5 && bl22 && bl3 && bl4 || bl5 != bl22 || bl3 != bl4;
        if (bl6) {
            return true;
        }
        boolean bl9 = bl7 = wallShape == WallShape.TALL && wallShape2 == WallShape.TALL || wallShape3 == WallShape.TALL && wallShape4 == WallShape.TALL;
        if (bl7) {
            return false;
        }
        return aboveState.isIn(BlockTags.WALL_POST_OVERRIDE) || WallBlock.shouldUseTallShape(aboveShape, POST_SHAPE_FOR_TALL_TEST);
    }

    private BlockState getStateWith(BlockState state, boolean north, boolean east, boolean south, boolean west, VoxelShape aboveShape) {
        return (BlockState)((BlockState)((BlockState)((BlockState)state.with(NORTH_WALL_SHAPE, this.getWallShape(north, aboveShape, WALL_SHAPES_FOR_TALL_TEST_BY_DIRECTION.get(Direction.NORTH)))).with(EAST_WALL_SHAPE, this.getWallShape(east, aboveShape, WALL_SHAPES_FOR_TALL_TEST_BY_DIRECTION.get(Direction.EAST)))).with(SOUTH_WALL_SHAPE, this.getWallShape(south, aboveShape, WALL_SHAPES_FOR_TALL_TEST_BY_DIRECTION.get(Direction.SOUTH)))).with(WEST_WALL_SHAPE, this.getWallShape(west, aboveShape, WALL_SHAPES_FOR_TALL_TEST_BY_DIRECTION.get(Direction.WEST)));
    }

    private WallShape getWallShape(boolean connected, VoxelShape aboveShape, VoxelShape tallShape) {
        if (connected) {
            if (WallBlock.shouldUseTallShape(aboveShape, tallShape)) {
                return WallShape.TALL;
            }
            return WallShape.LOW;
        }
        return WallShape.NONE;
    }

    @Override
    protected FluidState getFluidState(BlockState state) {
        if (state.get(WATERLOGGED).booleanValue()) {
            return Fluids.WATER.getStill(false);
        }
        return super.getFluidState(state);
    }

    @Override
    protected boolean isTransparent(BlockState state) {
        return state.get(WATERLOGGED) == false;
    }

    @Override
    protected void appendProperties(StateManager.Builder<Block, BlockState> builder) {
        builder.add(UP, NORTH_WALL_SHAPE, EAST_WALL_SHAPE, WEST_WALL_SHAPE, SOUTH_WALL_SHAPE, WATERLOGGED);
    }

    @Override
    protected BlockState rotate(BlockState state, BlockRotation rotation) {
        switch (rotation) {
            case CLOCKWISE_180: {
                return (BlockState)((BlockState)((BlockState)((BlockState)state.with(NORTH_WALL_SHAPE, state.get(SOUTH_WALL_SHAPE))).with(EAST_WALL_SHAPE, state.get(WEST_WALL_SHAPE))).with(SOUTH_WALL_SHAPE, state.get(NORTH_WALL_SHAPE))).with(WEST_WALL_SHAPE, state.get(EAST_WALL_SHAPE));
            }
            case COUNTERCLOCKWISE_90: {
                return (BlockState)((BlockState)((BlockState)((BlockState)state.with(NORTH_WALL_SHAPE, state.get(EAST_WALL_SHAPE))).with(EAST_WALL_SHAPE, state.get(SOUTH_WALL_SHAPE))).with(SOUTH_WALL_SHAPE, state.get(WEST_WALL_SHAPE))).with(WEST_WALL_SHAPE, state.get(NORTH_WALL_SHAPE));
            }
            case CLOCKWISE_90: {
                return (BlockState)((BlockState)((BlockState)((BlockState)state.with(NORTH_WALL_SHAPE, state.get(WEST_WALL_SHAPE))).with(EAST_WALL_SHAPE, state.get(NORTH_WALL_SHAPE))).with(SOUTH_WALL_SHAPE, state.get(EAST_WALL_SHAPE))).with(WEST_WALL_SHAPE, state.get(SOUTH_WALL_SHAPE));
            }
        }
        return state;
    }

    @Override
    protected BlockState mirror(BlockState state, BlockMirror mirror) {
        switch (mirror) {
            case LEFT_RIGHT: {
                return (BlockState)((BlockState)state.with(NORTH_WALL_SHAPE, state.get(SOUTH_WALL_SHAPE))).with(SOUTH_WALL_SHAPE, state.get(NORTH_WALL_SHAPE));
            }
            case FRONT_BACK: {
                return (BlockState)((BlockState)state.with(EAST_WALL_SHAPE, state.get(WEST_WALL_SHAPE))).with(WEST_WALL_SHAPE, state.get(EAST_WALL_SHAPE));
            }
        }
        return super.mirror(state, mirror);
    }
}


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
import java.util.List;
import java.util.Map;
import net.minecraft.block.AbstractBlock;
import net.minecraft.block.Block;
import net.minecraft.block.BlockState;
import net.minecraft.block.ConnectingBlock;
import net.minecraft.block.HorizontalConnectingBlock;
import net.minecraft.block.ShapeContext;
import net.minecraft.block.TripwireHookBlock;
import net.minecraft.entity.Entity;
import net.minecraft.entity.EntityCollisionHandler;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.ItemPlacementContext;
import net.minecraft.item.Items;
import net.minecraft.registry.Registries;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.state.StateManager;
import net.minecraft.state.property.BooleanProperty;
import net.minecraft.state.property.Properties;
import net.minecraft.util.BlockMirror;
import net.minecraft.util.BlockRotation;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Direction;
import net.minecraft.util.math.random.Random;
import net.minecraft.util.shape.VoxelShape;
import net.minecraft.world.BlockView;
import net.minecraft.world.World;
import net.minecraft.world.WorldView;
import net.minecraft.world.event.GameEvent;
import net.minecraft.world.tick.ScheduledTickView;

public class TripwireBlock
extends Block {
    public static final MapCodec<TripwireBlock> CODEC = RecordCodecBuilder.mapCodec(instance -> instance.group((App)Registries.BLOCK.getCodec().fieldOf("hook").forGetter(block -> block.hookBlock), TripwireBlock.createSettingsCodec()).apply((Applicative)instance, TripwireBlock::new));
    public static final BooleanProperty POWERED = Properties.POWERED;
    public static final BooleanProperty ATTACHED = Properties.ATTACHED;
    public static final BooleanProperty DISARMED = Properties.DISARMED;
    public static final BooleanProperty NORTH = ConnectingBlock.NORTH;
    public static final BooleanProperty EAST = ConnectingBlock.EAST;
    public static final BooleanProperty SOUTH = ConnectingBlock.SOUTH;
    public static final BooleanProperty WEST = ConnectingBlock.WEST;
    private static final Map<Direction, BooleanProperty> FACING_PROPERTIES = HorizontalConnectingBlock.FACING_PROPERTIES;
    private static final VoxelShape ATTACHED_SHAPE = Block.createColumnShape(16.0, 1.0, 2.5);
    private static final VoxelShape UNATTACHED_SHAPE = Block.createColumnShape(16.0, 0.0, 8.0);
    private static final int SCHEDULED_TICK_DELAY = 10;
    private final Block hookBlock;

    public MapCodec<TripwireBlock> getCodec() {
        return CODEC;
    }

    public TripwireBlock(Block hookBlock, AbstractBlock.Settings settings) {
        super(settings);
        this.setDefaultState((BlockState)((BlockState)((BlockState)((BlockState)((BlockState)((BlockState)((BlockState)((BlockState)this.stateManager.getDefaultState()).with(POWERED, false)).with(ATTACHED, false)).with(DISARMED, false)).with(NORTH, false)).with(EAST, false)).with(SOUTH, false)).with(WEST, false));
        this.hookBlock = hookBlock;
    }

    @Override
    protected VoxelShape getOutlineShape(BlockState state, BlockView world, BlockPos pos, ShapeContext context) {
        return state.get(ATTACHED) != false ? ATTACHED_SHAPE : UNATTACHED_SHAPE;
    }

    @Override
    public BlockState getPlacementState(ItemPlacementContext ctx) {
        World blockView = ctx.getWorld();
        BlockPos blockPos = ctx.getBlockPos();
        return (BlockState)((BlockState)((BlockState)((BlockState)this.getDefaultState().with(NORTH, this.shouldConnectTo(blockView.getBlockState(blockPos.north()), Direction.NORTH))).with(EAST, this.shouldConnectTo(blockView.getBlockState(blockPos.east()), Direction.EAST))).with(SOUTH, this.shouldConnectTo(blockView.getBlockState(blockPos.south()), Direction.SOUTH))).with(WEST, this.shouldConnectTo(blockView.getBlockState(blockPos.west()), Direction.WEST));
    }

    @Override
    protected BlockState getStateForNeighborUpdate(BlockState state, WorldView world, ScheduledTickView tickView, BlockPos pos, Direction direction, BlockPos neighborPos, BlockState neighborState, Random random) {
        if (direction.getAxis().isHorizontal()) {
            return (BlockState)state.with(FACING_PROPERTIES.get(direction), this.shouldConnectTo(neighborState, direction));
        }
        return super.getStateForNeighborUpdate(state, world, tickView, pos, direction, neighborPos, neighborState, random);
    }

    @Override
    protected void onBlockAdded(BlockState state, World world, BlockPos pos, BlockState oldState, boolean notify) {
        if (oldState.isOf(state.getBlock())) {
            return;
        }
        this.update(world, pos, state);
    }

    @Override
    protected void onStateReplaced(BlockState state, ServerWorld world, BlockPos pos, boolean moved) {
        if (!moved) {
            this.update(world, pos, (BlockState)state.with(POWERED, true));
        }
    }

    @Override
    public BlockState onBreak(World world, BlockPos pos, BlockState state, PlayerEntity player) {
        if (!world.isClient() && !player.getMainHandStack().isEmpty() && player.getMainHandStack().isOf(Items.SHEARS)) {
            world.setBlockState(pos, (BlockState)state.with(DISARMED, true), 260);
            world.emitGameEvent((Entity)player, GameEvent.SHEAR, pos);
        }
        return super.onBreak(world, pos, state, player);
    }

    private void update(World world, BlockPos pos, BlockState state) {
        block0: for (Direction direction : new Direction[]{Direction.SOUTH, Direction.WEST}) {
            for (int i = 1; i < 42; ++i) {
                BlockPos blockPos = pos.offset(direction, i);
                BlockState blockState = world.getBlockState(blockPos);
                if (blockState.isOf(this.hookBlock)) {
                    if (blockState.get(TripwireHookBlock.FACING) != direction.getOpposite()) continue block0;
                    TripwireHookBlock.update(world, blockPos, blockState, false, true, i, state);
                    continue block0;
                }
                if (!blockState.isOf(this)) continue block0;
            }
        }
    }

    @Override
    protected VoxelShape getInsideCollisionShape(BlockState state, BlockView world, BlockPos pos, Entity entity) {
        return state.getOutlineShape(world, pos);
    }

    @Override
    protected void onEntityCollision(BlockState state, World world, BlockPos pos, Entity entity, EntityCollisionHandler handler, boolean bl) {
        if (world.isClient()) {
            return;
        }
        if (state.get(POWERED).booleanValue()) {
            return;
        }
        this.updatePowered(world, pos, List.of(entity));
    }

    @Override
    protected void scheduledTick(BlockState state, ServerWorld world, BlockPos pos, Random random) {
        if (!world.getBlockState(pos).get(POWERED).booleanValue()) {
            return;
        }
        this.updatePowered(world, pos);
    }

    private void updatePowered(World world, BlockPos pos) {
        BlockState blockState = world.getBlockState(pos);
        List<Entity> list = world.getOtherEntities(null, blockState.getOutlineShape(world, pos).getBoundingBox().offset(pos));
        this.updatePowered(world, pos, list);
    }

    private void updatePowered(World world, BlockPos pos, List<? extends Entity> entities) {
        BlockState blockState = world.getBlockState(pos);
        boolean bl = blockState.get(POWERED);
        boolean bl2 = false;
        if (!entities.isEmpty()) {
            for (Entity entity : entities) {
                if (entity.canAvoidTraps()) continue;
                bl2 = true;
                break;
            }
        }
        if (bl2 != bl) {
            blockState = (BlockState)blockState.with(POWERED, bl2);
            world.setBlockState(pos, blockState, 3);
            this.update(world, pos, blockState);
        }
        if (bl2) {
            world.scheduleBlockTick(new BlockPos(pos), this, 10);
        }
    }

    public boolean shouldConnectTo(BlockState state, Direction facing) {
        if (state.isOf(this.hookBlock)) {
            return state.get(TripwireHookBlock.FACING) == facing.getOpposite();
        }
        return state.isOf(this);
    }

    @Override
    protected BlockState rotate(BlockState state, BlockRotation rotation) {
        switch (rotation) {
            case CLOCKWISE_180: {
                return (BlockState)((BlockState)((BlockState)((BlockState)state.with(NORTH, state.get(SOUTH))).with(EAST, state.get(WEST))).with(SOUTH, state.get(NORTH))).with(WEST, state.get(EAST));
            }
            case COUNTERCLOCKWISE_90: {
                return (BlockState)((BlockState)((BlockState)((BlockState)state.with(NORTH, state.get(EAST))).with(EAST, state.get(SOUTH))).with(SOUTH, state.get(WEST))).with(WEST, state.get(NORTH));
            }
            case CLOCKWISE_90: {
                return (BlockState)((BlockState)((BlockState)((BlockState)state.with(NORTH, state.get(WEST))).with(EAST, state.get(NORTH))).with(SOUTH, state.get(EAST))).with(WEST, state.get(SOUTH));
            }
        }
        return state;
    }

    @Override
    protected BlockState mirror(BlockState state, BlockMirror mirror) {
        switch (mirror) {
            case LEFT_RIGHT: {
                return (BlockState)((BlockState)state.with(NORTH, state.get(SOUTH))).with(SOUTH, state.get(NORTH));
            }
            case FRONT_BACK: {
                return (BlockState)((BlockState)state.with(EAST, state.get(WEST))).with(WEST, state.get(EAST));
            }
        }
        return super.mirror(state, mirror);
    }

    @Override
    protected void appendProperties(StateManager.Builder<Block, BlockState> builder) {
        builder.add(POWERED, ATTACHED, DISARMED, NORTH, EAST, WEST, SOUTH);
    }
}


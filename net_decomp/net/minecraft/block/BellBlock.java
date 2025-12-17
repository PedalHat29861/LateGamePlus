/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  com.mojang.serialization.MapCodec
 *  org.jspecify.annotations.Nullable
 */
package net.minecraft.block;

import com.mojang.serialization.MapCodec;
import java.util.Map;
import java.util.function.BiConsumer;
import net.minecraft.block.AbstractBlock;
import net.minecraft.block.Block;
import net.minecraft.block.BlockState;
import net.minecraft.block.BlockWithEntity;
import net.minecraft.block.Blocks;
import net.minecraft.block.HorizontalFacingBlock;
import net.minecraft.block.ShapeContext;
import net.minecraft.block.WallMountedBlock;
import net.minecraft.block.entity.BellBlockEntity;
import net.minecraft.block.entity.BlockEntity;
import net.minecraft.block.entity.BlockEntityTicker;
import net.minecraft.block.entity.BlockEntityType;
import net.minecraft.block.enums.Attachment;
import net.minecraft.entity.Entity;
import net.minecraft.entity.ai.pathing.NavigationType;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.entity.projectile.ProjectileEntity;
import net.minecraft.item.ItemPlacementContext;
import net.minecraft.item.ItemStack;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.sound.SoundCategory;
import net.minecraft.sound.SoundEvents;
import net.minecraft.stat.Stats;
import net.minecraft.state.StateManager;
import net.minecraft.state.property.BooleanProperty;
import net.minecraft.state.property.EnumProperty;
import net.minecraft.state.property.Properties;
import net.minecraft.util.ActionResult;
import net.minecraft.util.BlockMirror;
import net.minecraft.util.BlockRotation;
import net.minecraft.util.hit.BlockHitResult;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Direction;
import net.minecraft.util.math.random.Random;
import net.minecraft.util.shape.VoxelShape;
import net.minecraft.util.shape.VoxelShapes;
import net.minecraft.world.BlockView;
import net.minecraft.world.World;
import net.minecraft.world.WorldView;
import net.minecraft.world.block.WireOrientation;
import net.minecraft.world.event.GameEvent;
import net.minecraft.world.explosion.Explosion;
import net.minecraft.world.tick.ScheduledTickView;
import org.jspecify.annotations.Nullable;

public class BellBlock
extends BlockWithEntity {
    public static final MapCodec<BellBlock> CODEC = BellBlock.createCodec(BellBlock::new);
    public static final EnumProperty<Direction> FACING = HorizontalFacingBlock.FACING;
    public static final EnumProperty<Attachment> ATTACHMENT = Properties.ATTACHMENT;
    public static final BooleanProperty POWERED = Properties.POWERED;
    private static final VoxelShape BELL_SHAPE = VoxelShapes.union(Block.createColumnShape(6.0, 6.0, 13.0), Block.createColumnShape(8.0, 4.0, 6.0));
    private static final VoxelShape CEILING_SHAPE = VoxelShapes.union(BELL_SHAPE, Block.createColumnShape(2.0, 13.0, 16.0));
    private static final Map<Direction.Axis, VoxelShape> FLOOR_SHAPES = VoxelShapes.createHorizontalAxisShapeMap(Block.createCuboidShape(16.0, 16.0, 8.0));
    private static final Map<Direction.Axis, VoxelShape> DOUBLE_WALL_SHAPES = VoxelShapes.createHorizontalAxisShapeMap(VoxelShapes.union(BELL_SHAPE, Block.createColumnShape(2.0, 16.0, 13.0, 15.0)));
    private static final Map<Direction, VoxelShape> SINGLE_WALL_SHAPES = VoxelShapes.createHorizontalFacingShapeMap(VoxelShapes.union(BELL_SHAPE, Block.createCuboidZShape(2.0, 13.0, 15.0, 0.0, 13.0)));
    public static final int field_31014 = 1;

    public MapCodec<BellBlock> getCodec() {
        return CODEC;
    }

    public BellBlock(AbstractBlock.Settings settings) {
        super(settings);
        this.setDefaultState((BlockState)((BlockState)((BlockState)((BlockState)this.stateManager.getDefaultState()).with(FACING, Direction.NORTH)).with(ATTACHMENT, Attachment.FLOOR)).with(POWERED, false));
    }

    @Override
    protected void neighborUpdate(BlockState state, World world, BlockPos pos, Block sourceBlock, @Nullable WireOrientation wireOrientation, boolean notify) {
        boolean bl = world.isReceivingRedstonePower(pos);
        if (bl != state.get(POWERED)) {
            if (bl) {
                this.ring(world, pos, null);
            }
            world.setBlockState(pos, (BlockState)state.with(POWERED, bl), 3);
        }
    }

    @Override
    protected void onProjectileHit(World world, BlockState state, BlockHitResult hit, ProjectileEntity projectile) {
        PlayerEntity playerEntity;
        Entity entity = projectile.getOwner();
        PlayerEntity playerEntity2 = entity instanceof PlayerEntity ? (playerEntity = (PlayerEntity)entity) : null;
        this.ring(world, state, hit, playerEntity2, true);
    }

    @Override
    protected ActionResult onUse(BlockState state, World world, BlockPos pos, PlayerEntity player, BlockHitResult hit) {
        return this.ring(world, state, hit, player, true) ? ActionResult.SUCCESS : ActionResult.PASS;
    }

    public boolean ring(World world, BlockState state, BlockHitResult hitResult, @Nullable PlayerEntity player, boolean checkHitPos) {
        boolean bl;
        Direction direction = hitResult.getSide();
        BlockPos blockPos = hitResult.getBlockPos();
        boolean bl2 = bl = !checkHitPos || this.isPointOnBell(state, direction, hitResult.getPos().y - (double)blockPos.getY());
        if (bl) {
            boolean bl22 = this.ring(player, world, blockPos, direction);
            if (bl22 && player != null) {
                player.incrementStat(Stats.BELL_RING);
            }
            return true;
        }
        return false;
    }

    private boolean isPointOnBell(BlockState state, Direction side, double y) {
        if (side.getAxis() == Direction.Axis.Y || y > (double)0.8124f) {
            return false;
        }
        Direction direction = state.get(FACING);
        Attachment attachment = state.get(ATTACHMENT);
        switch (attachment) {
            case FLOOR: {
                return direction.getAxis() == side.getAxis();
            }
            case SINGLE_WALL: 
            case DOUBLE_WALL: {
                return direction.getAxis() != side.getAxis();
            }
            case CEILING: {
                return true;
            }
        }
        return false;
    }

    public boolean ring(World world, BlockPos pos, @Nullable Direction direction) {
        return this.ring(null, world, pos, direction);
    }

    public boolean ring(@Nullable Entity entity, World world, BlockPos pos, @Nullable Direction direction) {
        BlockEntity blockEntity = world.getBlockEntity(pos);
        if (!world.isClient() && blockEntity instanceof BellBlockEntity) {
            if (direction == null) {
                direction = world.getBlockState(pos).get(FACING);
            }
            ((BellBlockEntity)blockEntity).activate(direction);
            world.playSound(null, pos, SoundEvents.BLOCK_BELL_USE, SoundCategory.BLOCKS, 2.0f, 1.0f);
            world.emitGameEvent(entity, GameEvent.BLOCK_CHANGE, pos);
            return true;
        }
        return false;
    }

    private VoxelShape getShape(BlockState state) {
        Direction direction = state.get(FACING);
        return switch (state.get(ATTACHMENT)) {
            default -> throw new MatchException(null, null);
            case Attachment.FLOOR -> FLOOR_SHAPES.get(direction.getAxis());
            case Attachment.CEILING -> CEILING_SHAPE;
            case Attachment.SINGLE_WALL -> SINGLE_WALL_SHAPES.get(direction);
            case Attachment.DOUBLE_WALL -> DOUBLE_WALL_SHAPES.get(direction.getAxis());
        };
    }

    @Override
    protected VoxelShape getCollisionShape(BlockState state, BlockView world, BlockPos pos, ShapeContext context) {
        return this.getShape(state);
    }

    @Override
    protected VoxelShape getOutlineShape(BlockState state, BlockView world, BlockPos pos, ShapeContext context) {
        return this.getShape(state);
    }

    @Override
    public @Nullable BlockState getPlacementState(ItemPlacementContext ctx) {
        Direction direction = ctx.getSide();
        BlockPos blockPos = ctx.getBlockPos();
        World world = ctx.getWorld();
        Direction.Axis axis = direction.getAxis();
        if (axis == Direction.Axis.Y) {
            BlockState blockState = (BlockState)((BlockState)this.getDefaultState().with(ATTACHMENT, direction == Direction.DOWN ? Attachment.CEILING : Attachment.FLOOR)).with(FACING, ctx.getHorizontalPlayerFacing());
            if (blockState.canPlaceAt(ctx.getWorld(), blockPos)) {
                return blockState;
            }
        } else {
            boolean bl = axis == Direction.Axis.X && world.getBlockState(blockPos.west()).isSideSolidFullSquare(world, blockPos.west(), Direction.EAST) && world.getBlockState(blockPos.east()).isSideSolidFullSquare(world, blockPos.east(), Direction.WEST) || axis == Direction.Axis.Z && world.getBlockState(blockPos.north()).isSideSolidFullSquare(world, blockPos.north(), Direction.SOUTH) && world.getBlockState(blockPos.south()).isSideSolidFullSquare(world, blockPos.south(), Direction.NORTH);
            BlockState blockState = (BlockState)((BlockState)this.getDefaultState().with(FACING, direction.getOpposite())).with(ATTACHMENT, bl ? Attachment.DOUBLE_WALL : Attachment.SINGLE_WALL);
            if (blockState.canPlaceAt(ctx.getWorld(), ctx.getBlockPos())) {
                return blockState;
            }
            boolean bl2 = world.getBlockState(blockPos.down()).isSideSolidFullSquare(world, blockPos.down(), Direction.UP);
            if ((blockState = (BlockState)blockState.with(ATTACHMENT, bl2 ? Attachment.FLOOR : Attachment.CEILING)).canPlaceAt(ctx.getWorld(), ctx.getBlockPos())) {
                return blockState;
            }
        }
        return null;
    }

    @Override
    protected void onExploded(BlockState state, ServerWorld world, BlockPos pos, Explosion explosion, BiConsumer<ItemStack, BlockPos> stackMerger) {
        if (explosion.canTriggerBlocks()) {
            this.ring(world, pos, null);
        }
        super.onExploded(state, world, pos, explosion, stackMerger);
    }

    @Override
    protected BlockState getStateForNeighborUpdate(BlockState state, WorldView world, ScheduledTickView tickView, BlockPos pos, Direction direction, BlockPos neighborPos, BlockState neighborState, Random random) {
        Attachment attachment = state.get(ATTACHMENT);
        Direction direction2 = BellBlock.getPlacementSide(state).getOpposite();
        if (direction2 == direction && !state.canPlaceAt(world, pos) && attachment != Attachment.DOUBLE_WALL) {
            return Blocks.AIR.getDefaultState();
        }
        if (direction.getAxis() == state.get(FACING).getAxis()) {
            if (attachment == Attachment.DOUBLE_WALL && !neighborState.isSideSolidFullSquare(world, neighborPos, direction)) {
                return (BlockState)((BlockState)state.with(ATTACHMENT, Attachment.SINGLE_WALL)).with(FACING, direction.getOpposite());
            }
            if (attachment == Attachment.SINGLE_WALL && direction2.getOpposite() == direction && neighborState.isSideSolidFullSquare(world, neighborPos, state.get(FACING))) {
                return (BlockState)state.with(ATTACHMENT, Attachment.DOUBLE_WALL);
            }
        }
        return super.getStateForNeighborUpdate(state, world, tickView, pos, direction, neighborPos, neighborState, random);
    }

    @Override
    protected boolean canPlaceAt(BlockState state, WorldView world, BlockPos pos) {
        Direction direction = BellBlock.getPlacementSide(state).getOpposite();
        if (direction == Direction.UP) {
            return Block.sideCoversSmallSquare(world, pos.up(), Direction.DOWN);
        }
        return WallMountedBlock.canPlaceAt(world, pos, direction);
    }

    private static Direction getPlacementSide(BlockState state) {
        switch (state.get(ATTACHMENT)) {
            case CEILING: {
                return Direction.DOWN;
            }
            case FLOOR: {
                return Direction.UP;
            }
        }
        return state.get(FACING).getOpposite();
    }

    @Override
    protected void appendProperties(StateManager.Builder<Block, BlockState> builder) {
        builder.add(FACING, ATTACHMENT, POWERED);
    }

    @Override
    public @Nullable BlockEntity createBlockEntity(BlockPos pos, BlockState state) {
        return new BellBlockEntity(pos, state);
    }

    @Override
    public <T extends BlockEntity> @Nullable BlockEntityTicker<T> getTicker(World world, BlockState state, BlockEntityType<T> type) {
        return BellBlock.validateTicker(type, BlockEntityType.BELL, world.isClient() ? BellBlockEntity::clientTick : BellBlockEntity::serverTick);
    }

    @Override
    protected boolean canPathfindThrough(BlockState state, NavigationType type) {
        return false;
    }

    @Override
    public BlockState rotate(BlockState state, BlockRotation rotation) {
        return (BlockState)state.with(FACING, rotation.rotate(state.get(FACING)));
    }

    @Override
    public BlockState mirror(BlockState state, BlockMirror mirror) {
        return state.rotate(mirror.getRotation(state.get(FACING)));
    }
}


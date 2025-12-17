/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  com.mojang.datafixers.kinds.App
 *  com.mojang.datafixers.kinds.Applicative
 *  com.mojang.serialization.MapCodec
 *  com.mojang.serialization.codecs.RecordCodecBuilder
 *  org.jspecify.annotations.Nullable
 */
package net.minecraft.block;

import com.mojang.datafixers.kinds.App;
import com.mojang.datafixers.kinds.Applicative;
import com.mojang.serialization.MapCodec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import java.util.Map;
import net.minecraft.block.AbstractBlock;
import net.minecraft.block.AbstractSignBlock;
import net.minecraft.block.Block;
import net.minecraft.block.BlockState;
import net.minecraft.block.Blocks;
import net.minecraft.block.HorizontalFacingBlock;
import net.minecraft.block.ShapeContext;
import net.minecraft.block.SideShapeType;
import net.minecraft.block.WoodType;
import net.minecraft.block.entity.BlockEntity;
import net.minecraft.block.entity.BlockEntityTicker;
import net.minecraft.block.entity.BlockEntityType;
import net.minecraft.block.entity.HangingSignBlockEntity;
import net.minecraft.block.entity.SignBlockEntity;
import net.minecraft.entity.ai.pathing.NavigationType;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.fluid.FluidState;
import net.minecraft.fluid.Fluids;
import net.minecraft.item.HangingSignItem;
import net.minecraft.item.ItemPlacementContext;
import net.minecraft.item.ItemStack;
import net.minecraft.registry.tag.BlockTags;
import net.minecraft.state.StateManager;
import net.minecraft.state.property.EnumProperty;
import net.minecraft.util.ActionResult;
import net.minecraft.util.BlockMirror;
import net.minecraft.util.BlockRotation;
import net.minecraft.util.Hand;
import net.minecraft.util.hit.BlockHitResult;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Direction;
import net.minecraft.util.math.random.Random;
import net.minecraft.util.shape.VoxelShape;
import net.minecraft.util.shape.VoxelShapes;
import net.minecraft.world.BlockView;
import net.minecraft.world.World;
import net.minecraft.world.WorldView;
import net.minecraft.world.tick.ScheduledTickView;
import org.jspecify.annotations.Nullable;

public class WallHangingSignBlock
extends AbstractSignBlock {
    public static final MapCodec<WallHangingSignBlock> CODEC = RecordCodecBuilder.mapCodec(instance -> instance.group((App)WoodType.CODEC.fieldOf("wood_type").forGetter(AbstractSignBlock::getWoodType), WallHangingSignBlock.createSettingsCodec()).apply((Applicative)instance, WallHangingSignBlock::new));
    public static final EnumProperty<Direction> FACING = HorizontalFacingBlock.FACING;
    private static final Map<Direction.Axis, VoxelShape> COLLISION_SHAPES_BY_AXIS = VoxelShapes.createHorizontalAxisShapeMap(Block.createColumnShape(16.0, 4.0, 14.0, 16.0));
    private static final Map<Direction.Axis, VoxelShape> OUTLINE_SHAPES_BY_AXIS = VoxelShapes.createHorizontalAxisShapeMap(VoxelShapes.union(COLLISION_SHAPES_BY_AXIS.get(Direction.Axis.Z), Block.createColumnShape(14.0, 2.0, 0.0, 10.0)));

    public MapCodec<WallHangingSignBlock> getCodec() {
        return CODEC;
    }

    public WallHangingSignBlock(WoodType woodType, AbstractBlock.Settings settings) {
        super(woodType, settings.sounds(woodType.hangingSignSoundType()));
        this.setDefaultState((BlockState)((BlockState)((BlockState)this.stateManager.getDefaultState()).with(FACING, Direction.NORTH)).with(WATERLOGGED, false));
    }

    @Override
    protected ActionResult onUseWithItem(ItemStack stack, BlockState state, World world, BlockPos pos, PlayerEntity player, Hand hand, BlockHitResult hit) {
        SignBlockEntity signBlockEntity;
        BlockEntity blockEntity = world.getBlockEntity(pos);
        if (blockEntity instanceof SignBlockEntity && this.shouldTryAttaching(state, player, hit, signBlockEntity = (SignBlockEntity)blockEntity, stack)) {
            return ActionResult.PASS;
        }
        return super.onUseWithItem(stack, state, world, pos, player, hand, hit);
    }

    private boolean shouldTryAttaching(BlockState state, PlayerEntity player, BlockHitResult hitResult, SignBlockEntity sign, ItemStack stack) {
        return !sign.canRunCommandClickEvent(sign.isPlayerFacingFront(player), player) && stack.getItem() instanceof HangingSignItem && !this.isHitOnFacingAxis(hitResult, state);
    }

    private boolean isHitOnFacingAxis(BlockHitResult hitResult, BlockState state) {
        return hitResult.getSide().getAxis() == state.get(FACING).getAxis();
    }

    @Override
    protected VoxelShape getOutlineShape(BlockState state, BlockView world, BlockPos pos, ShapeContext context) {
        return OUTLINE_SHAPES_BY_AXIS.get(state.get(FACING).getAxis());
    }

    @Override
    protected VoxelShape getSidesShape(BlockState state, BlockView world, BlockPos pos) {
        return this.getOutlineShape(state, world, pos, ShapeContext.absent());
    }

    @Override
    protected VoxelShape getCollisionShape(BlockState state, BlockView world, BlockPos pos, ShapeContext context) {
        return COLLISION_SHAPES_BY_AXIS.get(state.get(FACING).getAxis());
    }

    public boolean canAttachAt(BlockState state, WorldView world, BlockPos pos) {
        Direction direction = state.get(FACING).rotateYClockwise();
        Direction direction2 = state.get(FACING).rotateYCounterclockwise();
        return this.canAttachTo(world, state, pos.offset(direction), direction2) || this.canAttachTo(world, state, pos.offset(direction2), direction);
    }

    public boolean canAttachTo(WorldView world, BlockState state, BlockPos toPos, Direction direction) {
        BlockState blockState = world.getBlockState(toPos);
        if (blockState.isIn(BlockTags.WALL_HANGING_SIGNS)) {
            return blockState.get(FACING).getAxis().test(state.get(FACING));
        }
        return blockState.isSideSolid(world, toPos, direction, SideShapeType.FULL);
    }

    @Override
    public @Nullable BlockState getPlacementState(ItemPlacementContext ctx) {
        BlockState blockState = this.getDefaultState();
        FluidState fluidState = ctx.getWorld().getFluidState(ctx.getBlockPos());
        World worldView = ctx.getWorld();
        BlockPos blockPos = ctx.getBlockPos();
        for (Direction direction : ctx.getPlacementDirections()) {
            Direction direction2;
            if (!direction.getAxis().isHorizontal() || direction.getAxis().test(ctx.getSide()) || !(blockState = (BlockState)blockState.with(FACING, direction2 = direction.getOpposite())).canPlaceAt(worldView, blockPos) || !this.canAttachAt(blockState, worldView, blockPos)) continue;
            return (BlockState)blockState.with(WATERLOGGED, fluidState.getFluid() == Fluids.WATER);
        }
        return null;
    }

    @Override
    protected BlockState getStateForNeighborUpdate(BlockState state, WorldView world, ScheduledTickView tickView, BlockPos pos, Direction direction, BlockPos neighborPos, BlockState neighborState, Random random) {
        if (direction.getAxis() == state.get(FACING).rotateYClockwise().getAxis() && !state.canPlaceAt(world, pos)) {
            return Blocks.AIR.getDefaultState();
        }
        return super.getStateForNeighborUpdate(state, world, tickView, pos, direction, neighborPos, neighborState, random);
    }

    @Override
    public float getRotationDegrees(BlockState state) {
        return state.get(FACING).getPositiveHorizontalDegrees();
    }

    @Override
    protected BlockState rotate(BlockState state, BlockRotation rotation) {
        return (BlockState)state.with(FACING, rotation.rotate(state.get(FACING)));
    }

    @Override
    protected BlockState mirror(BlockState state, BlockMirror mirror) {
        return state.rotate(mirror.getRotation(state.get(FACING)));
    }

    @Override
    protected void appendProperties(StateManager.Builder<Block, BlockState> builder) {
        builder.add(FACING, WATERLOGGED);
    }

    @Override
    public BlockEntity createBlockEntity(BlockPos pos, BlockState state) {
        return new HangingSignBlockEntity(pos, state);
    }

    @Override
    protected boolean canPathfindThrough(BlockState state, NavigationType type) {
        return false;
    }

    @Override
    public <T extends BlockEntity> @Nullable BlockEntityTicker<T> getTicker(World world, BlockState state, BlockEntityType<T> type) {
        return WallHangingSignBlock.validateTicker(type, BlockEntityType.HANGING_SIGN, SignBlockEntity::tick);
    }
}


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
import net.minecraft.block.HorizontalFacingBlock;
import net.minecraft.block.ShapeContext;
import net.minecraft.block.Waterloggable;
import net.minecraft.entity.EntityType;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.SpawnReason;
import net.minecraft.entity.ai.pathing.NavigationType;
import net.minecraft.entity.passive.HappyGhastEntity;
import net.minecraft.fluid.FluidState;
import net.minecraft.fluid.Fluids;
import net.minecraft.item.ItemPlacementContext;
import net.minecraft.item.ItemStack;
import net.minecraft.particle.ParticleTypes;
import net.minecraft.registry.tag.BlockTags;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.sound.SoundCategory;
import net.minecraft.sound.SoundEvents;
import net.minecraft.state.StateManager;
import net.minecraft.state.property.BooleanProperty;
import net.minecraft.state.property.IntProperty;
import net.minecraft.state.property.Properties;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Direction;
import net.minecraft.util.math.Vec3d;
import net.minecraft.util.math.random.Random;
import net.minecraft.util.shape.VoxelShape;
import net.minecraft.world.BlockView;
import net.minecraft.world.World;
import net.minecraft.world.WorldAccess;
import net.minecraft.world.WorldView;
import net.minecraft.world.event.GameEvent;
import net.minecraft.world.tick.ScheduledTickView;
import net.minecraft.world.tick.WorldTickScheduler;
import org.jspecify.annotations.Nullable;

public class DriedGhastBlock
extends HorizontalFacingBlock
implements Waterloggable {
    public static final MapCodec<DriedGhastBlock> CODEC = DriedGhastBlock.createCodec(DriedGhastBlock::new);
    public static final int MAX_HYDRATION = 3;
    public static final IntProperty HYDRATION = Properties.HYDRATION;
    public static final BooleanProperty WATERLOGGED = Properties.WATERLOGGED;
    public static final int HYDRATION_TICK_TIME = 5000;
    private static final VoxelShape SHAPE = Block.createColumnShape(10.0, 10.0, 0.0, 10.0);

    public MapCodec<DriedGhastBlock> getCodec() {
        return CODEC;
    }

    public DriedGhastBlock(AbstractBlock.Settings settings) {
        super(settings);
        this.setDefaultState((BlockState)((BlockState)((BlockState)((BlockState)this.stateManager.getDefaultState()).with(FACING, Direction.NORTH)).with(HYDRATION, 0)).with(WATERLOGGED, false));
    }

    @Override
    protected void appendProperties(StateManager.Builder<Block, BlockState> builder) {
        builder.add(FACING, HYDRATION, WATERLOGGED);
    }

    @Override
    protected BlockState getStateForNeighborUpdate(BlockState state, WorldView world, ScheduledTickView tickView, BlockPos pos, Direction direction, BlockPos neighborPos, BlockState neighborState, Random random) {
        if (state.get(WATERLOGGED).booleanValue()) {
            tickView.scheduleFluidTick(pos, Fluids.WATER, Fluids.WATER.getTickRate(world));
        }
        return super.getStateForNeighborUpdate(state, world, tickView, pos, direction, neighborPos, neighborState, random);
    }

    @Override
    public VoxelShape getOutlineShape(BlockState state, BlockView world, BlockPos pos, ShapeContext context) {
        return SHAPE;
    }

    public int getHydration(BlockState state) {
        return state.get(HYDRATION);
    }

    private boolean isFullyHydrated(BlockState state) {
        return this.getHydration(state) == 3;
    }

    @Override
    protected void scheduledTick(BlockState state, ServerWorld world, BlockPos pos, Random random) {
        if (state.get(WATERLOGGED).booleanValue()) {
            this.tickHydration(state, world, pos, random);
            return;
        }
        int i = this.getHydration(state);
        if (i > 0) {
            world.setBlockState(pos, (BlockState)state.with(HYDRATION, i - 1), 2);
            world.emitGameEvent(GameEvent.BLOCK_CHANGE, pos, GameEvent.Emitter.of(state));
        }
    }

    private void tickHydration(BlockState state, ServerWorld world, BlockPos pos, Random random) {
        if (!this.isFullyHydrated(state)) {
            world.playSound(null, pos, SoundEvents.BLOCK_DRIED_GHAST_TRANSITION, SoundCategory.BLOCKS, 1.0f, 1.0f);
            world.setBlockState(pos, (BlockState)state.with(HYDRATION, this.getHydration(state) + 1), 2);
            world.emitGameEvent(GameEvent.BLOCK_CHANGE, pos, GameEvent.Emitter.of(state));
        } else {
            this.spawnGhastling(world, pos, state);
        }
    }

    private void spawnGhastling(ServerWorld world, BlockPos pos, BlockState state) {
        world.removeBlock(pos, false);
        HappyGhastEntity happyGhastEntity = EntityType.HAPPY_GHAST.create(world, SpawnReason.BREEDING);
        if (happyGhastEntity != null) {
            Vec3d vec3d = pos.toBottomCenterPos();
            happyGhastEntity.setBaby(true);
            float f = Direction.getHorizontalDegreesOrThrow((Direction)state.get(FACING));
            happyGhastEntity.setHeadYaw(f);
            happyGhastEntity.refreshPositionAndAngles(vec3d.getX(), vec3d.getY(), vec3d.getZ(), f, 0.0f);
            world.spawnEntity(happyGhastEntity);
            world.playSoundFromEntity(null, happyGhastEntity, SoundEvents.ENTITY_GHASTLING_SPAWN, SoundCategory.BLOCKS, 1.0f, 1.0f);
        }
    }

    @Override
    public void randomDisplayTick(BlockState state, World world, BlockPos pos, Random random) {
        double d = (double)pos.getX() + 0.5;
        double e = (double)pos.getY() + 0.5;
        double f = (double)pos.getZ() + 0.5;
        if (!state.get(WATERLOGGED).booleanValue()) {
            if (random.nextInt(40) == 0 && world.getBlockState(pos.down()).isIn(BlockTags.TRIGGERS_AMBIENT_DRIED_GHAST_BLOCK_SOUNDS)) {
                world.playSoundClient(d, e, f, SoundEvents.BLOCK_DRIED_GHAST_AMBIENT, SoundCategory.BLOCKS, 1.0f, 1.0f, false);
            }
            if (random.nextInt(6) == 0) {
                world.addParticleClient(ParticleTypes.WHITE_SMOKE, d, e, f, 0.0, 0.02, 0.0);
            }
        } else {
            if (random.nextInt(40) == 0) {
                world.playSoundClient(d, e, f, SoundEvents.BLOCK_DRIED_GHAST_AMBIENT_WATER, SoundCategory.BLOCKS, 1.0f, 1.0f, false);
            }
            if (random.nextInt(6) == 0) {
                world.addParticleClient(ParticleTypes.HAPPY_VILLAGER, d + (double)((random.nextFloat() * 2.0f - 1.0f) / 3.0f), e + 0.4, f + (double)((random.nextFloat() * 2.0f - 1.0f) / 3.0f), 0.0, random.nextFloat(), 0.0);
            }
        }
    }

    @Override
    protected void randomTick(BlockState state, ServerWorld world, BlockPos pos, Random random) {
        if ((state.get(WATERLOGGED).booleanValue() || state.get(HYDRATION) > 0) && !((WorldTickScheduler)world.getBlockTickScheduler()).isQueued(pos, this)) {
            world.scheduleBlockTick(pos, this, 5000);
        }
    }

    @Override
    public BlockState getPlacementState(ItemPlacementContext ctx) {
        FluidState fluidState = ctx.getWorld().getFluidState(ctx.getBlockPos());
        boolean bl = fluidState.getFluid() == Fluids.WATER;
        return (BlockState)((BlockState)super.getPlacementState(ctx).with(WATERLOGGED, bl)).with(FACING, ctx.getHorizontalPlayerFacing().getOpposite());
    }

    @Override
    protected FluidState getFluidState(BlockState state) {
        if (state.get(WATERLOGGED).booleanValue()) {
            return Fluids.WATER.getStill(false);
        }
        return super.getFluidState(state);
    }

    @Override
    public boolean tryFillWithFluid(WorldAccess world, BlockPos pos, BlockState state, FluidState fluidState) {
        if (state.get(Properties.WATERLOGGED).booleanValue() || fluidState.getFluid() != Fluids.WATER) {
            return false;
        }
        if (!world.isClient()) {
            world.setBlockState(pos, (BlockState)state.with(Properties.WATERLOGGED, true), 3);
            world.scheduleFluidTick(pos, fluidState.getFluid(), fluidState.getFluid().getTickRate(world));
            world.playSound(null, pos, SoundEvents.BLOCK_DRIED_GHAST_PLACE_IN_WATER, SoundCategory.BLOCKS, 1.0f, 1.0f);
        }
        return true;
    }

    @Override
    public void onPlaced(World world, BlockPos pos, BlockState state, @Nullable LivingEntity placer, ItemStack itemStack) {
        super.onPlaced(world, pos, state, placer, itemStack);
        world.playSound(null, pos, state.get(WATERLOGGED) != false ? SoundEvents.BLOCK_DRIED_GHAST_PLACE_IN_WATER : SoundEvents.BLOCK_DRIED_GHAST_PLACE, SoundCategory.BLOCKS, 1.0f, 1.0f);
    }

    @Override
    public boolean canPathfindThrough(BlockState state, NavigationType type) {
        return false;
    }
}


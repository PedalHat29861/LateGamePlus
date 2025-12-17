/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  com.google.common.annotations.VisibleForTesting
 *  com.mojang.serialization.MapCodec
 *  org.jspecify.annotations.Nullable
 */
package net.minecraft.block;

import com.google.common.annotations.VisibleForTesting;
import com.mojang.serialization.MapCodec;
import java.lang.invoke.MethodHandle;
import java.lang.runtime.ObjectMethods;
import java.util.Optional;
import java.util.function.BiPredicate;
import java.util.function.Predicate;
import net.minecraft.block.AbstractBlock;
import net.minecraft.block.AbstractCauldronBlock;
import net.minecraft.block.Block;
import net.minecraft.block.BlockState;
import net.minecraft.block.Blocks;
import net.minecraft.block.Falling;
import net.minecraft.block.ShapeContext;
import net.minecraft.block.Waterloggable;
import net.minecraft.block.enums.Thickness;
import net.minecraft.entity.Entity;
import net.minecraft.entity.FallingBlockEntity;
import net.minecraft.entity.ai.pathing.NavigationType;
import net.minecraft.entity.damage.DamageSource;
import net.minecraft.entity.projectile.ProjectileEntity;
import net.minecraft.entity.projectile.TridentEntity;
import net.minecraft.fluid.Fluid;
import net.minecraft.fluid.FluidState;
import net.minecraft.fluid.Fluids;
import net.minecraft.item.ItemPlacementContext;
import net.minecraft.particle.ParticleEffect;
import net.minecraft.particle.ParticleTypes;
import net.minecraft.registry.tag.FluidTags;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.state.StateManager;
import net.minecraft.state.property.BooleanProperty;
import net.minecraft.state.property.EnumProperty;
import net.minecraft.state.property.Properties;
import net.minecraft.util.function.BooleanBiFunction;
import net.minecraft.util.hit.BlockHitResult;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Direction;
import net.minecraft.util.math.Vec3d;
import net.minecraft.util.math.random.Random;
import net.minecraft.util.shape.VoxelShape;
import net.minecraft.util.shape.VoxelShapes;
import net.minecraft.world.BlockView;
import net.minecraft.world.World;
import net.minecraft.world.WorldAccess;
import net.minecraft.world.WorldView;
import net.minecraft.world.attribute.EnvironmentAttributes;
import net.minecraft.world.event.GameEvent;
import net.minecraft.world.tick.ScheduledTickView;
import org.jspecify.annotations.Nullable;

public class PointedDripstoneBlock
extends Block
implements Falling,
Waterloggable {
    public static final MapCodec<PointedDripstoneBlock> CODEC = PointedDripstoneBlock.createCodec(PointedDripstoneBlock::new);
    public static final EnumProperty<Direction> VERTICAL_DIRECTION = Properties.VERTICAL_DIRECTION;
    public static final EnumProperty<Thickness> THICKNESS = Properties.THICKNESS;
    public static final BooleanProperty WATERLOGGED = Properties.WATERLOGGED;
    private static final int field_31205 = 11;
    private static final int field_31207 = 2;
    private static final float field_31208 = 0.02f;
    private static final float field_31209 = 0.12f;
    private static final int field_31210 = 11;
    private static final float WATER_DRIP_CHANCE = 0.17578125f;
    private static final float LAVA_DRIP_CHANCE = 0.05859375f;
    private static final double field_31213 = 0.6;
    private static final float field_31214 = 1.0f;
    private static final int field_31215 = 40;
    private static final int field_31200 = 6;
    private static final float field_31201 = 2.5f;
    private static final int field_31202 = 2;
    private static final float field_33566 = 5.0f;
    private static final float field_33567 = 0.011377778f;
    private static final int MAX_STALACTITE_GROWTH = 7;
    private static final int STALACTITE_FLOOR_SEARCH_RANGE = 10;
    private static final VoxelShape TIP_MERGE_SHAPE = Block.createColumnShape(6.0, 0.0, 16.0);
    private static final VoxelShape UP_TIP_SHAPE = Block.createColumnShape(6.0, 0.0, 11.0);
    private static final VoxelShape DOWN_TIP_SHAPE = Block.createColumnShape(6.0, 5.0, 16.0);
    private static final VoxelShape FRUSTUM_SHAPE = Block.createColumnShape(8.0, 0.0, 16.0);
    private static final VoxelShape MIDDLE_SHAPE = Block.createColumnShape(10.0, 0.0, 16.0);
    private static final VoxelShape BASE_SHAPE = Block.createColumnShape(12.0, 0.0, 16.0);
    private static final double DOWN_TIP_Y = DOWN_TIP_SHAPE.getMin(Direction.Axis.Y);
    private static final float MAX_HORIZONTAL_MODEL_OFFSET = (float)BASE_SHAPE.getMin(Direction.Axis.X);
    private static final VoxelShape DRIP_COLLISION_SHAPE = Block.createColumnShape(4.0, 0.0, 16.0);

    public MapCodec<PointedDripstoneBlock> getCodec() {
        return CODEC;
    }

    public PointedDripstoneBlock(AbstractBlock.Settings settings) {
        super(settings);
        this.setDefaultState((BlockState)((BlockState)((BlockState)((BlockState)this.stateManager.getDefaultState()).with(VERTICAL_DIRECTION, Direction.UP)).with(THICKNESS, Thickness.TIP)).with(WATERLOGGED, false));
    }

    @Override
    protected void appendProperties(StateManager.Builder<Block, BlockState> builder) {
        builder.add(VERTICAL_DIRECTION, THICKNESS, WATERLOGGED);
    }

    @Override
    protected boolean canPlaceAt(BlockState state, WorldView world, BlockPos pos) {
        return PointedDripstoneBlock.canPlaceAtWithDirection(world, pos, state.get(VERTICAL_DIRECTION));
    }

    @Override
    protected BlockState getStateForNeighborUpdate(BlockState state, WorldView world, ScheduledTickView tickView, BlockPos pos, Direction direction, BlockPos neighborPos, BlockState neighborState, Random random) {
        if (state.get(WATERLOGGED).booleanValue()) {
            tickView.scheduleFluidTick(pos, Fluids.WATER, Fluids.WATER.getTickRate(world));
        }
        if (direction != Direction.UP && direction != Direction.DOWN) {
            return state;
        }
        Direction direction2 = state.get(VERTICAL_DIRECTION);
        if (direction2 == Direction.DOWN && tickView.getBlockTickScheduler().isQueued(pos, this)) {
            return state;
        }
        if (direction == direction2.getOpposite() && !this.canPlaceAt(state, world, pos)) {
            if (direction2 == Direction.DOWN) {
                tickView.scheduleBlockTick(pos, this, 2);
            } else {
                tickView.scheduleBlockTick(pos, this, 1);
            }
            return state;
        }
        boolean bl = state.get(THICKNESS) == Thickness.TIP_MERGE;
        Thickness thickness = PointedDripstoneBlock.getThickness(world, pos, direction2, bl);
        return (BlockState)state.with(THICKNESS, thickness);
    }

    @Override
    protected void onProjectileHit(World world, BlockState state, BlockHitResult hit, ProjectileEntity projectile) {
        ServerWorld serverWorld;
        if (world.isClient()) {
            return;
        }
        BlockPos blockPos = hit.getBlockPos();
        if (world instanceof ServerWorld && projectile.canModifyAt(serverWorld = (ServerWorld)world, blockPos) && projectile.canBreakBlocks(serverWorld) && projectile instanceof TridentEntity && projectile.getVelocity().length() > 0.6) {
            world.breakBlock(blockPos, true);
        }
    }

    @Override
    public void onLandedUpon(World world, BlockState state, BlockPos pos, Entity entity, double fallDistance) {
        if (state.get(VERTICAL_DIRECTION) == Direction.UP && state.get(THICKNESS) == Thickness.TIP) {
            entity.handleFallDamage(fallDistance + 2.5, 2.0f, world.getDamageSources().stalagmite());
        } else {
            super.onLandedUpon(world, state, pos, entity, fallDistance);
        }
    }

    @Override
    public void randomDisplayTick(BlockState state, World world, BlockPos pos, Random random) {
        if (!PointedDripstoneBlock.canDrip(state)) {
            return;
        }
        float f = random.nextFloat();
        if (f > 0.12f) {
            return;
        }
        PointedDripstoneBlock.getFluid(world, pos, state).filter(fluid -> f < 0.02f || PointedDripstoneBlock.isFluidLiquid(fluid.fluid)).ifPresent(fluid -> PointedDripstoneBlock.createParticle(world, pos, state, fluid.fluid, fluid.pos));
    }

    @Override
    protected void scheduledTick(BlockState state, ServerWorld world, BlockPos pos, Random random) {
        if (PointedDripstoneBlock.isPointingUp(state) && !this.canPlaceAt(state, world, pos)) {
            world.breakBlock(pos, true);
        } else {
            PointedDripstoneBlock.spawnFallingBlock(state, world, pos);
        }
    }

    @Override
    protected void randomTick(BlockState state, ServerWorld world, BlockPos pos, Random random) {
        PointedDripstoneBlock.dripTick(state, world, pos, random.nextFloat());
        if (random.nextFloat() < 0.011377778f && PointedDripstoneBlock.isHeldByPointedDripstone(state, world, pos)) {
            PointedDripstoneBlock.tryGrow(state, world, pos, random);
        }
    }

    @VisibleForTesting
    public static void dripTick(BlockState state, ServerWorld world, BlockPos pos, float dripChance) {
        float f;
        if (dripChance > 0.17578125f && dripChance > 0.05859375f) {
            return;
        }
        if (!PointedDripstoneBlock.isHeldByPointedDripstone(state, world, pos)) {
            return;
        }
        Optional<DrippingFluid> optional = PointedDripstoneBlock.getFluid(world, pos, state);
        if (optional.isEmpty()) {
            return;
        }
        Fluid fluid = optional.get().fluid;
        if (fluid == Fluids.WATER) {
            f = 0.17578125f;
        } else if (fluid == Fluids.LAVA) {
            f = 0.05859375f;
        } else {
            return;
        }
        if (dripChance >= f) {
            return;
        }
        BlockPos blockPos = PointedDripstoneBlock.getTipPos(state, world, pos, 11, false);
        if (blockPos == null) {
            return;
        }
        if (optional.get().sourceState.isOf(Blocks.MUD) && fluid == Fluids.WATER) {
            BlockState blockState = Blocks.CLAY.getDefaultState();
            world.setBlockState(optional.get().pos, blockState);
            Block.pushEntitiesUpBeforeBlockChange(optional.get().sourceState, blockState, world, optional.get().pos);
            world.emitGameEvent(GameEvent.BLOCK_CHANGE, optional.get().pos, GameEvent.Emitter.of(blockState));
            world.syncWorldEvent(1504, blockPos, 0);
            return;
        }
        BlockPos blockPos2 = PointedDripstoneBlock.getCauldronPos(world, blockPos, fluid);
        if (blockPos2 == null) {
            return;
        }
        world.syncWorldEvent(1504, blockPos, 0);
        int i = blockPos.getY() - blockPos2.getY();
        int j = 50 + i;
        BlockState blockState2 = world.getBlockState(blockPos2);
        world.scheduleBlockTick(blockPos2, blockState2.getBlock(), j);
    }

    @Override
    public @Nullable BlockState getPlacementState(ItemPlacementContext ctx) {
        Direction direction;
        BlockPos blockPos;
        World worldAccess = ctx.getWorld();
        Direction direction2 = PointedDripstoneBlock.getDirectionToPlaceAt(worldAccess, blockPos = ctx.getBlockPos(), direction = ctx.getVerticalPlayerLookDirection().getOpposite());
        if (direction2 == null) {
            return null;
        }
        boolean bl = !ctx.shouldCancelInteraction();
        Thickness thickness = PointedDripstoneBlock.getThickness(worldAccess, blockPos, direction2, bl);
        return (BlockState)((BlockState)((BlockState)this.getDefaultState().with(VERTICAL_DIRECTION, direction2)).with(THICKNESS, thickness)).with(WATERLOGGED, worldAccess.getFluidState(blockPos).getFluid() == Fluids.WATER);
    }

    @Override
    protected FluidState getFluidState(BlockState state) {
        if (state.get(WATERLOGGED).booleanValue()) {
            return Fluids.WATER.getStill(false);
        }
        return super.getFluidState(state);
    }

    @Override
    protected VoxelShape getOutlineShape(BlockState state, BlockView world, BlockPos pos, ShapeContext context) {
        VoxelShape voxelShape = switch (state.get(THICKNESS)) {
            default -> throw new MatchException(null, null);
            case Thickness.TIP_MERGE -> TIP_MERGE_SHAPE;
            case Thickness.TIP -> {
                if (state.get(VERTICAL_DIRECTION) == Direction.DOWN) {
                    yield DOWN_TIP_SHAPE;
                }
                yield UP_TIP_SHAPE;
            }
            case Thickness.FRUSTUM -> FRUSTUM_SHAPE;
            case Thickness.MIDDLE -> MIDDLE_SHAPE;
            case Thickness.BASE -> BASE_SHAPE;
        };
        return voxelShape.offset(state.getModelOffset(pos));
    }

    @Override
    protected boolean isShapeFullCube(BlockState state, BlockView world, BlockPos pos) {
        return false;
    }

    @Override
    protected float getMaxHorizontalModelOffset() {
        return MAX_HORIZONTAL_MODEL_OFFSET;
    }

    @Override
    public void onDestroyedOnLanding(World world, BlockPos pos, FallingBlockEntity fallingBlockEntity) {
        if (!fallingBlockEntity.isSilent()) {
            world.syncWorldEvent(1045, pos, 0);
        }
    }

    @Override
    public DamageSource getDamageSource(Entity attacker) {
        return attacker.getDamageSources().fallingStalactite(attacker);
    }

    private static void spawnFallingBlock(BlockState state, ServerWorld world, BlockPos pos) {
        BlockPos.Mutable mutable = pos.mutableCopy();
        BlockState blockState = state;
        while (PointedDripstoneBlock.isPointingDown(blockState)) {
            FallingBlockEntity fallingBlockEntity = FallingBlockEntity.spawnFromBlock(world, mutable, blockState);
            if (PointedDripstoneBlock.isTip(blockState, true)) {
                int i = Math.max(1 + pos.getY() - mutable.getY(), 6);
                float f = 1.0f * (float)i;
                fallingBlockEntity.setHurtEntities(f, 40);
                break;
            }
            mutable.move(Direction.DOWN);
            blockState = world.getBlockState(mutable);
        }
    }

    @VisibleForTesting
    public static void tryGrow(BlockState state, ServerWorld world, BlockPos pos, Random random) {
        BlockState blockState2;
        BlockState blockState = world.getBlockState(pos.up(1));
        if (!PointedDripstoneBlock.canGrow(blockState, blockState2 = world.getBlockState(pos.up(2)))) {
            return;
        }
        BlockPos blockPos = PointedDripstoneBlock.getTipPos(state, world, pos, 7, false);
        if (blockPos == null) {
            return;
        }
        BlockState blockState3 = world.getBlockState(blockPos);
        if (!PointedDripstoneBlock.canDrip(blockState3) || !PointedDripstoneBlock.canGrow(blockState3, world, blockPos)) {
            return;
        }
        if (random.nextBoolean()) {
            PointedDripstoneBlock.tryGrow(world, blockPos, Direction.DOWN);
        } else {
            PointedDripstoneBlock.tryGrowStalagmite(world, blockPos);
        }
    }

    private static void tryGrowStalagmite(ServerWorld world, BlockPos pos) {
        BlockPos.Mutable mutable = pos.mutableCopy();
        for (int i = 0; i < 10; ++i) {
            mutable.move(Direction.DOWN);
            BlockState blockState = world.getBlockState(mutable);
            if (!blockState.getFluidState().isEmpty()) {
                return;
            }
            if (PointedDripstoneBlock.isTip(blockState, Direction.UP) && PointedDripstoneBlock.canGrow(blockState, world, mutable)) {
                PointedDripstoneBlock.tryGrow(world, mutable, Direction.UP);
                return;
            }
            if (PointedDripstoneBlock.canPlaceAtWithDirection(world, mutable, Direction.UP) && !world.isWater((BlockPos)mutable.down())) {
                PointedDripstoneBlock.tryGrow(world, (BlockPos)mutable.down(), Direction.UP);
                return;
            }
            if (PointedDripstoneBlock.canDripThrough(world, mutable, blockState)) continue;
            return;
        }
    }

    private static void tryGrow(ServerWorld world, BlockPos pos, Direction direction) {
        BlockPos blockPos = pos.offset(direction);
        BlockState blockState = world.getBlockState(blockPos);
        if (PointedDripstoneBlock.isTip(blockState, direction.getOpposite())) {
            PointedDripstoneBlock.growMerged(blockState, world, blockPos);
        } else if (blockState.isAir() || blockState.isOf(Blocks.WATER)) {
            PointedDripstoneBlock.place(world, blockPos, direction, Thickness.TIP);
        }
    }

    private static void place(WorldAccess world, BlockPos pos, Direction direction, Thickness thickness) {
        BlockState blockState = (BlockState)((BlockState)((BlockState)Blocks.POINTED_DRIPSTONE.getDefaultState().with(VERTICAL_DIRECTION, direction)).with(THICKNESS, thickness)).with(WATERLOGGED, world.getFluidState(pos).getFluid() == Fluids.WATER);
        world.setBlockState(pos, blockState, 3);
    }

    private static void growMerged(BlockState state, WorldAccess world, BlockPos pos) {
        BlockPos blockPos2;
        BlockPos blockPos;
        if (state.get(VERTICAL_DIRECTION) == Direction.UP) {
            blockPos = pos;
            blockPos2 = pos.up();
        } else {
            blockPos2 = pos;
            blockPos = pos.down();
        }
        PointedDripstoneBlock.place(world, blockPos2, Direction.DOWN, Thickness.TIP_MERGE);
        PointedDripstoneBlock.place(world, blockPos, Direction.UP, Thickness.TIP_MERGE);
    }

    public static void createParticle(World world, BlockPos pos, BlockState state) {
        PointedDripstoneBlock.getFluid(world, pos, state).ifPresent(fluid -> PointedDripstoneBlock.createParticle(world, pos, state, fluid.fluid, fluid.pos));
    }

    private static void createParticle(World world, BlockPos pos, BlockState state, Fluid fluid, BlockPos fluidPos) {
        Vec3d vec3d = state.getModelOffset(pos);
        double d = 0.0625;
        double e = (double)pos.getX() + 0.5 + vec3d.x;
        double f = (double)pos.getY() + DOWN_TIP_Y - 0.0625;
        double g = (double)pos.getZ() + 0.5 + vec3d.z;
        ParticleEffect particleEffect = PointedDripstoneBlock.getParticleEffect(world, fluid, fluidPos);
        world.addParticleClient(particleEffect, e, f, g, 0.0, 0.0, 0.0);
    }

    private static @Nullable BlockPos getTipPos(BlockState state, WorldAccess world, BlockPos pos, int range, boolean allowMerged) {
        if (PointedDripstoneBlock.isTip(state, allowMerged)) {
            return pos;
        }
        Direction direction = state.get(VERTICAL_DIRECTION);
        BiPredicate<BlockPos, BlockState> biPredicate = (posx, statex) -> statex.isOf(Blocks.POINTED_DRIPSTONE) && statex.get(VERTICAL_DIRECTION) == direction;
        return PointedDripstoneBlock.searchInDirection(world, pos, direction.getDirection(), biPredicate, statex -> PointedDripstoneBlock.isTip(statex, allowMerged), range).orElse(null);
    }

    private static @Nullable Direction getDirectionToPlaceAt(WorldView world, BlockPos pos, Direction direction) {
        Direction direction2;
        if (PointedDripstoneBlock.canPlaceAtWithDirection(world, pos, direction)) {
            direction2 = direction;
        } else if (PointedDripstoneBlock.canPlaceAtWithDirection(world, pos, direction.getOpposite())) {
            direction2 = direction.getOpposite();
        } else {
            return null;
        }
        return direction2;
    }

    private static Thickness getThickness(WorldView world, BlockPos pos, Direction direction, boolean tryMerge) {
        Direction direction2 = direction.getOpposite();
        BlockState blockState = world.getBlockState(pos.offset(direction));
        if (PointedDripstoneBlock.isPointedDripstoneFacingDirection(blockState, direction2)) {
            if (tryMerge || blockState.get(THICKNESS) == Thickness.TIP_MERGE) {
                return Thickness.TIP_MERGE;
            }
            return Thickness.TIP;
        }
        if (!PointedDripstoneBlock.isPointedDripstoneFacingDirection(blockState, direction)) {
            return Thickness.TIP;
        }
        Thickness thickness = blockState.get(THICKNESS);
        if (thickness == Thickness.TIP || thickness == Thickness.TIP_MERGE) {
            return Thickness.FRUSTUM;
        }
        BlockState blockState2 = world.getBlockState(pos.offset(direction2));
        if (!PointedDripstoneBlock.isPointedDripstoneFacingDirection(blockState2, direction)) {
            return Thickness.BASE;
        }
        return Thickness.MIDDLE;
    }

    public static boolean canDrip(BlockState state) {
        return PointedDripstoneBlock.isPointingDown(state) && state.get(THICKNESS) == Thickness.TIP && state.get(WATERLOGGED) == false;
    }

    private static boolean canGrow(BlockState state, ServerWorld world, BlockPos pos) {
        Direction direction = state.get(VERTICAL_DIRECTION);
        BlockPos blockPos = pos.offset(direction);
        BlockState blockState = world.getBlockState(blockPos);
        if (!blockState.getFluidState().isEmpty()) {
            return false;
        }
        if (blockState.isAir()) {
            return true;
        }
        return PointedDripstoneBlock.isTip(blockState, direction.getOpposite());
    }

    private static Optional<BlockPos> getSupportingPos(World world, BlockPos pos, BlockState state, int range) {
        Direction direction = state.get(VERTICAL_DIRECTION);
        BiPredicate<BlockPos, BlockState> biPredicate = (posx, statex) -> statex.isOf(Blocks.POINTED_DRIPSTONE) && statex.get(VERTICAL_DIRECTION) == direction;
        return PointedDripstoneBlock.searchInDirection(world, pos, direction.getOpposite().getDirection(), biPredicate, statex -> !statex.isOf(Blocks.POINTED_DRIPSTONE), range);
    }

    private static boolean canPlaceAtWithDirection(WorldView world, BlockPos pos, Direction direction) {
        BlockPos blockPos = pos.offset(direction.getOpposite());
        BlockState blockState = world.getBlockState(blockPos);
        return blockState.isSideSolidFullSquare(world, blockPos, direction) || PointedDripstoneBlock.isPointedDripstoneFacingDirection(blockState, direction);
    }

    private static boolean isTip(BlockState state, boolean allowMerged) {
        if (!state.isOf(Blocks.POINTED_DRIPSTONE)) {
            return false;
        }
        Thickness thickness = state.get(THICKNESS);
        return thickness == Thickness.TIP || allowMerged && thickness == Thickness.TIP_MERGE;
    }

    private static boolean isTip(BlockState state, Direction direction) {
        return PointedDripstoneBlock.isTip(state, false) && state.get(VERTICAL_DIRECTION) == direction;
    }

    private static boolean isPointingDown(BlockState state) {
        return PointedDripstoneBlock.isPointedDripstoneFacingDirection(state, Direction.DOWN);
    }

    private static boolean isPointingUp(BlockState state) {
        return PointedDripstoneBlock.isPointedDripstoneFacingDirection(state, Direction.UP);
    }

    private static boolean isHeldByPointedDripstone(BlockState state, WorldView world, BlockPos pos) {
        return PointedDripstoneBlock.isPointingDown(state) && !world.getBlockState(pos.up()).isOf(Blocks.POINTED_DRIPSTONE);
    }

    @Override
    protected boolean canPathfindThrough(BlockState state, NavigationType type) {
        return false;
    }

    private static boolean isPointedDripstoneFacingDirection(BlockState state, Direction direction) {
        return state.isOf(Blocks.POINTED_DRIPSTONE) && state.get(VERTICAL_DIRECTION) == direction;
    }

    private static @Nullable BlockPos getCauldronPos(World world, BlockPos pos, Fluid fluid) {
        Predicate<BlockState> predicate = state -> state.getBlock() instanceof AbstractCauldronBlock && ((AbstractCauldronBlock)state.getBlock()).canBeFilledByDripstone(fluid);
        BiPredicate<BlockPos, BlockState> biPredicate = (posx, state) -> PointedDripstoneBlock.canDripThrough(world, posx, state);
        return PointedDripstoneBlock.searchInDirection(world, pos, Direction.DOWN.getDirection(), biPredicate, predicate, 11).orElse(null);
    }

    public static @Nullable BlockPos getDripPos(World world, BlockPos pos) {
        BiPredicate<BlockPos, BlockState> biPredicate = (posx, state) -> PointedDripstoneBlock.canDripThrough(world, posx, state);
        return PointedDripstoneBlock.searchInDirection(world, pos, Direction.UP.getDirection(), biPredicate, PointedDripstoneBlock::canDrip, 11).orElse(null);
    }

    public static Fluid getDripFluid(ServerWorld world, BlockPos pos) {
        return PointedDripstoneBlock.getFluid(world, pos, world.getBlockState(pos)).map(fluid -> fluid.fluid).filter(PointedDripstoneBlock::isFluidLiquid).orElse(Fluids.EMPTY);
    }

    private static Optional<DrippingFluid> getFluid(World world, BlockPos pos, BlockState state) {
        if (!PointedDripstoneBlock.isPointingDown(state)) {
            return Optional.empty();
        }
        return PointedDripstoneBlock.getSupportingPos(world, pos, state, 11).map(posx -> {
            BlockPos blockPos = posx.up();
            BlockState blockState = world.getBlockState(blockPos);
            Fluid fluid = blockState.isOf(Blocks.MUD) && world.getEnvironmentAttributes().getAttributeValue(EnvironmentAttributes.WATER_EVAPORATES_GAMEPLAY, blockPos) == false ? Fluids.WATER : world.getFluidState(blockPos).getFluid();
            return new DrippingFluid(blockPos, fluid, blockState);
        });
    }

    private static boolean isFluidLiquid(Fluid fluid) {
        return fluid == Fluids.LAVA || fluid == Fluids.WATER;
    }

    private static boolean canGrow(BlockState dripstoneBlockState, BlockState waterState) {
        return dripstoneBlockState.isOf(Blocks.DRIPSTONE_BLOCK) && waterState.isOf(Blocks.WATER) && waterState.getFluidState().isStill();
    }

    private static ParticleEffect getParticleEffect(World world, Fluid fluid, BlockPos pos) {
        if (fluid.matchesType(Fluids.EMPTY)) {
            return world.getEnvironmentAttributes().getAttributeValue(EnvironmentAttributes.DEFAULT_DRIPSTONE_PARTICLE_VISUAL, pos);
        }
        return fluid.isIn(FluidTags.LAVA) ? ParticleTypes.DRIPPING_DRIPSTONE_LAVA : ParticleTypes.DRIPPING_DRIPSTONE_WATER;
    }

    private static Optional<BlockPos> searchInDirection(WorldAccess world, BlockPos pos, Direction.AxisDirection direction, BiPredicate<BlockPos, BlockState> continuePredicate, Predicate<BlockState> stopPredicate, int range) {
        Direction direction2 = Direction.get(direction, Direction.Axis.Y);
        BlockPos.Mutable mutable = pos.mutableCopy();
        for (int i = 1; i < range; ++i) {
            mutable.move(direction2);
            BlockState blockState = world.getBlockState(mutable);
            if (stopPredicate.test(blockState)) {
                return Optional.of(mutable.toImmutable());
            }
            if (!world.isOutOfHeightLimit(mutable.getY()) && continuePredicate.test(mutable, blockState)) continue;
            return Optional.empty();
        }
        return Optional.empty();
    }

    private static boolean canDripThrough(BlockView world, BlockPos pos, BlockState state) {
        if (state.isAir()) {
            return true;
        }
        if (state.isOpaqueFullCube()) {
            return false;
        }
        if (!state.getFluidState().isEmpty()) {
            return false;
        }
        VoxelShape voxelShape = state.getCollisionShape(world, pos);
        return !VoxelShapes.matchesAnywhere(DRIP_COLLISION_SHAPE, voxelShape, BooleanBiFunction.AND);
    }

    static final class DrippingFluid
    extends Record {
        final BlockPos pos;
        final Fluid fluid;
        final BlockState sourceState;

        DrippingFluid(BlockPos pos, Fluid fluid, BlockState sourceState) {
            this.pos = pos;
            this.fluid = fluid;
            this.sourceState = sourceState;
        }

        @Override
        public final String toString() {
            return ObjectMethods.bootstrap("toString", new MethodHandle[]{DrippingFluid.class, "pos;fluid;sourceState", "pos", "fluid", "sourceState"}, this);
        }

        @Override
        public final int hashCode() {
            return (int)ObjectMethods.bootstrap("hashCode", new MethodHandle[]{DrippingFluid.class, "pos;fluid;sourceState", "pos", "fluid", "sourceState"}, this);
        }

        @Override
        public final boolean equals(Object object) {
            return (boolean)ObjectMethods.bootstrap("equals", new MethodHandle[]{DrippingFluid.class, "pos;fluid;sourceState", "pos", "fluid", "sourceState"}, this, object);
        }

        public BlockPos pos() {
            return this.pos;
        }

        public Fluid fluid() {
            return this.fluid;
        }

        public BlockState sourceState() {
            return this.sourceState;
        }
    }
}


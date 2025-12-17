/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  com.google.common.collect.ImmutableList
 *  com.google.common.collect.ImmutableList$Builder
 *  com.mojang.serialization.MapCodec
 *  org.jspecify.annotations.Nullable
 */
package net.minecraft.block;

import com.google.common.collect.ImmutableList;
import com.mojang.serialization.MapCodec;
import java.util.Optional;
import net.minecraft.block.AbstractBlock;
import net.minecraft.block.Block;
import net.minecraft.block.BlockState;
import net.minecraft.block.Blocks;
import net.minecraft.entity.Dismounting;
import net.minecraft.entity.Entity;
import net.minecraft.entity.EntityType;
import net.minecraft.entity.ai.pathing.NavigationType;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.fluid.FluidState;
import net.minecraft.item.ItemStack;
import net.minecraft.item.Items;
import net.minecraft.particle.ParticleTypes;
import net.minecraft.registry.tag.FluidTags;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.sound.SoundCategory;
import net.minecraft.sound.SoundEvents;
import net.minecraft.state.StateManager;
import net.minecraft.state.property.IntProperty;
import net.minecraft.state.property.Properties;
import net.minecraft.util.ActionResult;
import net.minecraft.util.Hand;
import net.minecraft.util.hit.BlockHitResult;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Direction;
import net.minecraft.util.math.MathHelper;
import net.minecraft.util.math.Vec3d;
import net.minecraft.util.math.Vec3i;
import net.minecraft.util.math.random.Random;
import net.minecraft.world.BlockView;
import net.minecraft.world.CollisionView;
import net.minecraft.world.World;
import net.minecraft.world.WorldProperties;
import net.minecraft.world.attribute.EnvironmentAttributes;
import net.minecraft.world.event.GameEvent;
import net.minecraft.world.explosion.Explosion;
import net.minecraft.world.explosion.ExplosionBehavior;
import org.jspecify.annotations.Nullable;

public class RespawnAnchorBlock
extends Block {
    public static final MapCodec<RespawnAnchorBlock> CODEC = RespawnAnchorBlock.createCodec(RespawnAnchorBlock::new);
    public static final int NO_CHARGES = 0;
    public static final int MAX_CHARGES = 4;
    public static final IntProperty CHARGES = Properties.CHARGES;
    private static final ImmutableList<Vec3i> VALID_HORIZONTAL_SPAWN_OFFSETS = ImmutableList.of((Object)new Vec3i(0, 0, -1), (Object)new Vec3i(-1, 0, 0), (Object)new Vec3i(0, 0, 1), (Object)new Vec3i(1, 0, 0), (Object)new Vec3i(-1, 0, -1), (Object)new Vec3i(1, 0, -1), (Object)new Vec3i(-1, 0, 1), (Object)new Vec3i(1, 0, 1));
    private static final ImmutableList<Vec3i> VALID_SPAWN_OFFSETS = new ImmutableList.Builder().addAll(VALID_HORIZONTAL_SPAWN_OFFSETS).addAll(VALID_HORIZONTAL_SPAWN_OFFSETS.stream().map(Vec3i::down).iterator()).addAll(VALID_HORIZONTAL_SPAWN_OFFSETS.stream().map(Vec3i::up).iterator()).add((Object)new Vec3i(0, 1, 0)).build();

    public MapCodec<RespawnAnchorBlock> getCodec() {
        return CODEC;
    }

    public RespawnAnchorBlock(AbstractBlock.Settings settings) {
        super(settings);
        this.setDefaultState((BlockState)((BlockState)this.stateManager.getDefaultState()).with(CHARGES, 0));
    }

    @Override
    protected ActionResult onUseWithItem(ItemStack stack, BlockState state, World world, BlockPos pos, PlayerEntity player, Hand hand, BlockHitResult hit) {
        if (RespawnAnchorBlock.isChargeItem(stack) && RespawnAnchorBlock.canCharge(state)) {
            RespawnAnchorBlock.charge(player, world, pos, state);
            stack.decrementUnlessCreative(1, player);
            return ActionResult.SUCCESS;
        }
        if (hand == Hand.MAIN_HAND && RespawnAnchorBlock.isChargeItem(player.getStackInHand(Hand.OFF_HAND)) && RespawnAnchorBlock.canCharge(state)) {
            return ActionResult.PASS;
        }
        return ActionResult.PASS_TO_DEFAULT_BLOCK_ACTION;
    }

    @Override
    protected ActionResult onUse(BlockState state, World world, BlockPos pos, PlayerEntity player, BlockHitResult hit) {
        if (state.get(CHARGES) == 0) {
            return ActionResult.PASS;
        }
        if (!(world instanceof ServerWorld)) {
            return ActionResult.CONSUME;
        }
        ServerWorld serverWorld = (ServerWorld)world;
        if (RespawnAnchorBlock.isUsable(serverWorld, pos)) {
            if (player instanceof ServerPlayerEntity) {
                ServerPlayerEntity serverPlayerEntity = (ServerPlayerEntity)player;
                ServerPlayerEntity.Respawn respawn = serverPlayerEntity.getRespawn();
                ServerPlayerEntity.Respawn respawn2 = new ServerPlayerEntity.Respawn(WorldProperties.SpawnPoint.create(serverWorld.getRegistryKey(), pos, 0.0f, 0.0f), false);
                if (respawn == null || !respawn.posEquals(respawn2)) {
                    serverPlayerEntity.setSpawnPoint(respawn2, true);
                    serverWorld.playSound(null, (double)pos.getX() + 0.5, (double)pos.getY() + 0.5, (double)pos.getZ() + 0.5, SoundEvents.BLOCK_RESPAWN_ANCHOR_SET_SPAWN, SoundCategory.BLOCKS, 1.0f, 1.0f);
                    return ActionResult.SUCCESS_SERVER;
                }
            }
            return ActionResult.CONSUME;
        }
        this.explode(state, serverWorld, pos);
        return ActionResult.SUCCESS_SERVER;
    }

    private static boolean isChargeItem(ItemStack stack) {
        return stack.isOf(Items.GLOWSTONE);
    }

    private static boolean canCharge(BlockState state) {
        return state.get(CHARGES) < 4;
    }

    private static boolean hasStillWater(BlockPos pos, World world) {
        FluidState fluidState = world.getFluidState(pos);
        if (!fluidState.isIn(FluidTags.WATER)) {
            return false;
        }
        if (fluidState.isStill()) {
            return true;
        }
        float f = fluidState.getLevel();
        if (f < 2.0f) {
            return false;
        }
        FluidState fluidState2 = world.getFluidState(pos.down());
        return !fluidState2.isIn(FluidTags.WATER);
    }

    private void explode(BlockState state, ServerWorld world, final BlockPos explodedPos) {
        world.removeBlock(explodedPos, false);
        boolean bl = Direction.Type.HORIZONTAL.stream().map(explodedPos::offset).anyMatch(pos -> RespawnAnchorBlock.hasStillWater(pos, world));
        final boolean bl2 = bl || world.getFluidState(explodedPos.up()).isIn(FluidTags.WATER);
        ExplosionBehavior explosionBehavior = new ExplosionBehavior(this){

            @Override
            public Optional<Float> getBlastResistance(Explosion explosion, BlockView world, BlockPos pos, BlockState blockState, FluidState fluidState) {
                if (pos.equals(explodedPos) && bl2) {
                    return Optional.of(Float.valueOf(Blocks.WATER.getBlastResistance()));
                }
                return super.getBlastResistance(explosion, world, pos, blockState, fluidState);
            }
        };
        Vec3d vec3d = explodedPos.toCenterPos();
        world.createExplosion(null, world.getDamageSources().badRespawnPoint(vec3d), explosionBehavior, vec3d, 5.0f, true, World.ExplosionSourceType.BLOCK);
    }

    public static boolean isUsable(ServerWorld world, BlockPos pos) {
        return world.getEnvironmentAttributes().getAttributeValue(EnvironmentAttributes.RESPAWN_ANCHOR_WORKS_GAMEPLAY, pos);
    }

    public static void charge(@Nullable Entity charger, World world, BlockPos pos, BlockState state) {
        BlockState blockState = (BlockState)state.with(CHARGES, state.get(CHARGES) + 1);
        world.setBlockState(pos, blockState, 3);
        world.emitGameEvent(GameEvent.BLOCK_CHANGE, pos, GameEvent.Emitter.of(charger, blockState));
        world.playSound(null, (double)pos.getX() + 0.5, (double)pos.getY() + 0.5, (double)pos.getZ() + 0.5, SoundEvents.BLOCK_RESPAWN_ANCHOR_CHARGE, SoundCategory.BLOCKS, 1.0f, 1.0f);
    }

    @Override
    public void randomDisplayTick(BlockState state, World world, BlockPos pos, Random random) {
        if (state.get(CHARGES) == 0) {
            return;
        }
        if (random.nextInt(100) == 0) {
            world.playSoundAtBlockCenterClient(pos, SoundEvents.BLOCK_RESPAWN_ANCHOR_AMBIENT, SoundCategory.BLOCKS, 1.0f, 1.0f, false);
        }
        double d = (double)pos.getX() + 0.5 + (0.5 - random.nextDouble());
        double e = (double)pos.getY() + 1.0;
        double f = (double)pos.getZ() + 0.5 + (0.5 - random.nextDouble());
        double g = (double)random.nextFloat() * 0.04;
        world.addParticleClient(ParticleTypes.REVERSE_PORTAL, d, e, f, 0.0, g, 0.0);
    }

    @Override
    protected void appendProperties(StateManager.Builder<Block, BlockState> builder) {
        builder.add(CHARGES);
    }

    @Override
    protected boolean hasComparatorOutput(BlockState state) {
        return true;
    }

    public static int getLightLevel(BlockState state, int maxLevel) {
        return MathHelper.floor((float)(state.get(CHARGES) - 0) / 4.0f * (float)maxLevel);
    }

    @Override
    protected int getComparatorOutput(BlockState state, World world, BlockPos pos, Direction direction) {
        return RespawnAnchorBlock.getLightLevel(state, 15);
    }

    public static Optional<Vec3d> findRespawnPosition(EntityType<?> entity, CollisionView world, BlockPos pos) {
        Optional<Vec3d> optional = RespawnAnchorBlock.findRespawnPosition(entity, world, pos, true);
        if (optional.isPresent()) {
            return optional;
        }
        return RespawnAnchorBlock.findRespawnPosition(entity, world, pos, false);
    }

    private static Optional<Vec3d> findRespawnPosition(EntityType<?> entity, CollisionView world, BlockPos pos, boolean ignoreInvalidPos) {
        BlockPos.Mutable mutable = new BlockPos.Mutable();
        for (Vec3i vec3i : VALID_SPAWN_OFFSETS) {
            mutable.set(pos).move(vec3i);
            Vec3d vec3d = Dismounting.findRespawnPos(entity, world, mutable, ignoreInvalidPos);
            if (vec3d == null) continue;
            return Optional.of(vec3d);
        }
        return Optional.empty();
    }

    @Override
    protected boolean canPathfindThrough(BlockState state, NavigationType type) {
        return false;
    }
}


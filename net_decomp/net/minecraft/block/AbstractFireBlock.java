/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  com.mojang.serialization.MapCodec
 */
package net.minecraft.block;

import com.mojang.serialization.MapCodec;
import java.util.Optional;
import net.minecraft.block.AbstractBlock;
import net.minecraft.block.Block;
import net.minecraft.block.BlockState;
import net.minecraft.block.Blocks;
import net.minecraft.block.FireBlock;
import net.minecraft.block.ShapeContext;
import net.minecraft.block.SoulFireBlock;
import net.minecraft.entity.CollisionEvent;
import net.minecraft.entity.Entity;
import net.minecraft.entity.EntityCollisionHandler;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.ItemPlacementContext;
import net.minecraft.particle.ParticleTypes;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.sound.SoundCategory;
import net.minecraft.sound.SoundEvents;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Direction;
import net.minecraft.util.math.random.Random;
import net.minecraft.util.shape.VoxelShape;
import net.minecraft.world.BlockView;
import net.minecraft.world.World;
import net.minecraft.world.dimension.NetherPortal;

public abstract class AbstractFireBlock
extends Block {
    private static final int SET_ON_FIRE_SECONDS = 8;
    private static final int MIN_FIRE_TICK_INCREMENT = 1;
    private static final int MAX_FIRE_TICK_INCREMENT = 3;
    private final float damage;
    protected static final VoxelShape BASE_SHAPE = Block.createColumnShape(16.0, 0.0, 1.0);

    public AbstractFireBlock(AbstractBlock.Settings settings, float damage) {
        super(settings);
        this.damage = damage;
    }

    protected abstract MapCodec<? extends AbstractFireBlock> getCodec();

    @Override
    public BlockState getPlacementState(ItemPlacementContext ctx) {
        return AbstractFireBlock.getState(ctx.getWorld(), ctx.getBlockPos());
    }

    public static BlockState getState(BlockView world, BlockPos pos) {
        BlockPos blockPos = pos.down();
        BlockState blockState = world.getBlockState(blockPos);
        if (SoulFireBlock.isSoulBase(blockState)) {
            return Blocks.SOUL_FIRE.getDefaultState();
        }
        return ((FireBlock)Blocks.FIRE).getStateForPosition(world, pos);
    }

    @Override
    protected VoxelShape getOutlineShape(BlockState state, BlockView world, BlockPos pos, ShapeContext context) {
        return BASE_SHAPE;
    }

    @Override
    public void randomDisplayTick(BlockState state, World world, BlockPos pos, Random random) {
        block12: {
            double f;
            double e;
            double d;
            int i;
            block11: {
                BlockPos blockPos;
                BlockState blockState;
                if (random.nextInt(24) == 0) {
                    world.playSoundClient((double)pos.getX() + 0.5, (double)pos.getY() + 0.5, (double)pos.getZ() + 0.5, SoundEvents.BLOCK_FIRE_AMBIENT, SoundCategory.BLOCKS, 1.0f + random.nextFloat(), random.nextFloat() * 0.7f + 0.3f, false);
                }
                if (!this.isFlammable(blockState = world.getBlockState(blockPos = pos.down())) && !blockState.isSideSolidFullSquare(world, blockPos, Direction.UP)) break block11;
                for (int i2 = 0; i2 < 3; ++i2) {
                    double d2 = (double)pos.getX() + random.nextDouble();
                    double e2 = (double)pos.getY() + random.nextDouble() * 0.5 + 0.5;
                    double f2 = (double)pos.getZ() + random.nextDouble();
                    world.addParticleClient(ParticleTypes.LARGE_SMOKE, d2, e2, f2, 0.0, 0.0, 0.0);
                }
                break block12;
            }
            if (this.isFlammable(world.getBlockState(pos.west()))) {
                for (i = 0; i < 2; ++i) {
                    d = (double)pos.getX() + random.nextDouble() * (double)0.1f;
                    e = (double)pos.getY() + random.nextDouble();
                    f = (double)pos.getZ() + random.nextDouble();
                    world.addParticleClient(ParticleTypes.LARGE_SMOKE, d, e, f, 0.0, 0.0, 0.0);
                }
            }
            if (this.isFlammable(world.getBlockState(pos.east()))) {
                for (i = 0; i < 2; ++i) {
                    d = (double)(pos.getX() + 1) - random.nextDouble() * (double)0.1f;
                    e = (double)pos.getY() + random.nextDouble();
                    f = (double)pos.getZ() + random.nextDouble();
                    world.addParticleClient(ParticleTypes.LARGE_SMOKE, d, e, f, 0.0, 0.0, 0.0);
                }
            }
            if (this.isFlammable(world.getBlockState(pos.north()))) {
                for (i = 0; i < 2; ++i) {
                    d = (double)pos.getX() + random.nextDouble();
                    e = (double)pos.getY() + random.nextDouble();
                    f = (double)pos.getZ() + random.nextDouble() * (double)0.1f;
                    world.addParticleClient(ParticleTypes.LARGE_SMOKE, d, e, f, 0.0, 0.0, 0.0);
                }
            }
            if (this.isFlammable(world.getBlockState(pos.south()))) {
                for (i = 0; i < 2; ++i) {
                    d = (double)pos.getX() + random.nextDouble();
                    e = (double)pos.getY() + random.nextDouble();
                    f = (double)(pos.getZ() + 1) - random.nextDouble() * (double)0.1f;
                    world.addParticleClient(ParticleTypes.LARGE_SMOKE, d, e, f, 0.0, 0.0, 0.0);
                }
            }
            if (!this.isFlammable(world.getBlockState(pos.up()))) break block12;
            for (i = 0; i < 2; ++i) {
                d = (double)pos.getX() + random.nextDouble();
                e = (double)(pos.getY() + 1) - random.nextDouble() * (double)0.1f;
                f = (double)pos.getZ() + random.nextDouble();
                world.addParticleClient(ParticleTypes.LARGE_SMOKE, d, e, f, 0.0, 0.0, 0.0);
            }
        }
    }

    protected abstract boolean isFlammable(BlockState var1);

    @Override
    protected void onEntityCollision(BlockState state, World world, BlockPos pos, Entity entity2, EntityCollisionHandler handler, boolean bl) {
        handler.addEvent(CollisionEvent.CLEAR_FREEZE);
        handler.addEvent(CollisionEvent.FIRE_IGNITE);
        handler.addPostCallback(CollisionEvent.FIRE_IGNITE, entity -> entity.serverDamage(entity.getEntityWorld().getDamageSources().inFire(), this.damage));
    }

    public static void igniteEntity(Entity entity) {
        if (!entity.isFireImmune()) {
            if (entity.getFireTicks() < 0) {
                entity.setFireTicks(entity.getFireTicks() + 1);
            } else if (entity instanceof ServerPlayerEntity) {
                int i = entity.getEntityWorld().getRandom().nextBetweenExclusive(1, 3);
                entity.setFireTicks(entity.getFireTicks() + i);
            }
            if (entity.getFireTicks() >= 0) {
                entity.setOnFireFor(8.0f);
            }
        }
    }

    @Override
    protected void onBlockAdded(BlockState state, World world, BlockPos pos, BlockState oldState, boolean notify) {
        Optional<NetherPortal> optional;
        if (oldState.isOf(state.getBlock())) {
            return;
        }
        if (AbstractFireBlock.isOverworldOrNether(world) && (optional = NetherPortal.getNewPortal(world, pos, Direction.Axis.X)).isPresent()) {
            optional.get().createPortal(world);
            return;
        }
        if (!state.canPlaceAt(world, pos)) {
            world.removeBlock(pos, false);
        }
    }

    private static boolean isOverworldOrNether(World world) {
        return world.getRegistryKey() == World.OVERWORLD || world.getRegistryKey() == World.NETHER;
    }

    @Override
    protected void spawnBreakParticles(World world, PlayerEntity player, BlockPos pos, BlockState state) {
    }

    @Override
    public BlockState onBreak(World world, BlockPos pos, BlockState state, PlayerEntity player) {
        if (!world.isClient()) {
            world.syncWorldEvent(null, 1009, pos, 0);
        }
        return super.onBreak(world, pos, state, player);
    }

    public static boolean canPlaceAt(World world, BlockPos pos, Direction direction) {
        BlockState blockState = world.getBlockState(pos);
        if (!blockState.isAir()) {
            return false;
        }
        return AbstractFireBlock.getState(world, pos).canPlaceAt(world, pos) || AbstractFireBlock.shouldLightPortalAt(world, pos, direction);
    }

    private static boolean shouldLightPortalAt(World world, BlockPos pos, Direction direction) {
        if (!AbstractFireBlock.isOverworldOrNether(world)) {
            return false;
        }
        BlockPos.Mutable mutable = pos.mutableCopy();
        boolean bl = false;
        for (Direction direction2 : Direction.values()) {
            if (!world.getBlockState(mutable.set(pos).move(direction2)).isOf(Blocks.OBSIDIAN)) continue;
            bl = true;
            break;
        }
        if (!bl) {
            return false;
        }
        Direction.Axis axis = direction.getAxis().isHorizontal() ? direction.rotateYCounterclockwise().getAxis() : Direction.Type.HORIZONTAL.randomAxis(world.random);
        return NetherPortal.getNewPortal(world, pos, axis).isPresent();
    }
}


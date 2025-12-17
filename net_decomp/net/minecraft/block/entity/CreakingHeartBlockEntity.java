/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  com.mojang.datafixers.util.Either
 *  org.apache.commons.lang3.mutable.Mutable
 *  org.apache.commons.lang3.mutable.MutableObject
 *  org.jspecify.annotations.Nullable
 */
package net.minecraft.block.entity;

import com.mojang.datafixers.util.Either;
import java.util.Optional;
import java.util.UUID;
import net.minecraft.block.BlockState;
import net.minecraft.block.Blocks;
import net.minecraft.block.CreakingHeartBlock;
import net.minecraft.block.MultifaceBlock;
import net.minecraft.block.entity.BlockEntity;
import net.minecraft.block.entity.BlockEntityType;
import net.minecraft.block.enums.CreakingHeartState;
import net.minecraft.entity.Entity;
import net.minecraft.entity.EntityType;
import net.minecraft.entity.LargeEntitySpawnHelper;
import net.minecraft.entity.SpawnReason;
import net.minecraft.entity.damage.DamageSource;
import net.minecraft.entity.mob.CreakingEntity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.nbt.NbtCompound;
import net.minecraft.network.packet.Packet;
import net.minecraft.network.packet.s2c.play.BlockEntityUpdateS2CPacket;
import net.minecraft.particle.TrailParticleEffect;
import net.minecraft.registry.RegistryWrapper;
import net.minecraft.registry.entry.RegistryEntry;
import net.minecraft.registry.tag.BlockTags;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.sound.SoundCategory;
import net.minecraft.sound.SoundEvents;
import net.minecraft.storage.ReadView;
import net.minecraft.storage.WriteView;
import net.minecraft.util.Util;
import net.minecraft.util.Uuids;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Box;
import net.minecraft.util.math.Direction;
import net.minecraft.util.math.Vec3d;
import net.minecraft.util.math.random.Random;
import net.minecraft.world.World;
import net.minecraft.world.attribute.EnvironmentAttributes;
import net.minecraft.world.event.GameEvent;
import org.apache.commons.lang3.mutable.Mutable;
import org.apache.commons.lang3.mutable.MutableObject;
import org.jspecify.annotations.Nullable;

public class CreakingHeartBlockEntity
extends BlockEntity {
    private static final int field_54776 = 32;
    public static final int field_54775 = 32;
    private static final int field_54777 = 34;
    private static final int field_54778 = 16;
    private static final int field_54779 = 8;
    private static final int field_54780 = 5;
    private static final int field_54781 = 20;
    private static final int field_55498 = 5;
    private static final int field_54782 = 100;
    private static final int field_54783 = 10;
    private static final int field_54784 = 10;
    private static final int field_54785 = 50;
    private static final int field_55085 = 2;
    private static final int field_55086 = 64;
    private static final int field_55499 = 30;
    private static final Optional<CreakingEntity> DEFAULT_CREAKING_PUPPET = Optional.empty();
    private @Nullable Either<CreakingEntity, UUID> creakingPuppet;
    private long ticks;
    private int creakingUpdateTimer;
    private int trailParticlesSpawnTimer;
    private @Nullable Vec3d lastCreakingPuppetPos;
    private int comparatorOutput;

    public CreakingHeartBlockEntity(BlockPos pos, BlockState state) {
        super(BlockEntityType.CREAKING_HEART, pos, state);
    }

    public static void tick(World world, BlockPos pos, BlockState state, CreakingHeartBlockEntity blockEntity) {
        CreakingEntity creakingEntity;
        ++blockEntity.ticks;
        if (!(world instanceof ServerWorld)) {
            return;
        }
        ServerWorld serverWorld = (ServerWorld)world;
        int i = blockEntity.calcComparatorOutput();
        if (blockEntity.comparatorOutput != i) {
            blockEntity.comparatorOutput = i;
            world.updateComparators(pos, Blocks.CREAKING_HEART);
        }
        if (blockEntity.trailParticlesSpawnTimer > 0) {
            if (blockEntity.trailParticlesSpawnTimer > 50) {
                blockEntity.spawnTrailParticles(serverWorld, 1, true);
                blockEntity.spawnTrailParticles(serverWorld, 1, false);
            }
            if (blockEntity.trailParticlesSpawnTimer % 10 == 0 && blockEntity.lastCreakingPuppetPos != null) {
                blockEntity.getCreakingPuppet().ifPresent(creaking -> {
                    creakingHeartBlockEntity.lastCreakingPuppetPos = creaking.getBoundingBox().getCenter();
                });
                Vec3d vec3d = Vec3d.ofCenter(pos);
                float f = 0.2f + 0.8f * (float)(100 - blockEntity.trailParticlesSpawnTimer) / 100.0f;
                Vec3d vec3d2 = vec3d.subtract(blockEntity.lastCreakingPuppetPos).multiply(f).add(blockEntity.lastCreakingPuppetPos);
                BlockPos blockPos = BlockPos.ofFloored(vec3d2);
                float g = (float)blockEntity.trailParticlesSpawnTimer / 2.0f / 100.0f + 0.5f;
                serverWorld.playSound(null, blockPos, SoundEvents.BLOCK_CREAKING_HEART_HURT, SoundCategory.BLOCKS, g, 1.0f);
            }
            --blockEntity.trailParticlesSpawnTimer;
        }
        if (blockEntity.creakingUpdateTimer-- >= 0) {
            return;
        }
        blockEntity.creakingUpdateTimer = blockEntity.world == null ? 20 : blockEntity.world.random.nextInt(5) + 20;
        BlockState blockState = CreakingHeartBlockEntity.getBlockState(world, state, pos, blockEntity);
        if (blockState != state) {
            world.setBlockState(pos, blockState, 3);
            if (blockState.get(CreakingHeartBlock.ACTIVE) == CreakingHeartState.UPROOTED) {
                return;
            }
        }
        if (blockEntity.creakingPuppet != null) {
            Optional<CreakingEntity> optional = blockEntity.getCreakingPuppet();
            if (optional.isPresent()) {
                creakingEntity = optional.get();
                if (world.getEnvironmentAttributes().getAttributeValue(EnvironmentAttributes.CREAKING_ACTIVE_GAMEPLAY, pos) == false && !creakingEntity.isPersistent() || blockEntity.getDistanceToPuppet() > 34.0 || creakingEntity.isStuckWithPlayer()) {
                    blockEntity.killPuppet(null);
                }
            }
            return;
        }
        if (blockState.get(CreakingHeartBlock.ACTIVE) != CreakingHeartState.AWAKE) {
            return;
        }
        if (!serverWorld.shouldSpawnMonsters()) {
            return;
        }
        PlayerEntity playerEntity = world.getClosestPlayer((double)pos.getX(), (double)pos.getY(), (double)pos.getZ(), 32.0, false);
        if (playerEntity != null && (creakingEntity = CreakingHeartBlockEntity.spawnCreakingPuppet(serverWorld, blockEntity)) != null) {
            blockEntity.setCreakingPuppet(creakingEntity);
            creakingEntity.playSound(SoundEvents.ENTITY_CREAKING_SPAWN);
            world.playSound(null, blockEntity.getPos(), SoundEvents.BLOCK_CREAKING_HEART_SPAWN, SoundCategory.BLOCKS, 1.0f, 1.0f);
        }
    }

    private static BlockState getBlockState(World world, BlockState state, BlockPos pos, CreakingHeartBlockEntity creakingHeart) {
        if (!CreakingHeartBlock.shouldBeEnabled(state, world, pos) && creakingHeart.creakingPuppet == null) {
            return (BlockState)state.with(CreakingHeartBlock.ACTIVE, CreakingHeartState.UPROOTED);
        }
        CreakingHeartState creakingHeartState = world.getEnvironmentAttributes().getAttributeValue(EnvironmentAttributes.CREAKING_ACTIVE_GAMEPLAY, pos) != false ? CreakingHeartState.AWAKE : CreakingHeartState.DORMANT;
        return (BlockState)state.with(CreakingHeartBlock.ACTIVE, creakingHeartState);
    }

    private double getDistanceToPuppet() {
        return this.getCreakingPuppet().map(creaking -> Math.sqrt(creaking.squaredDistanceTo(Vec3d.ofBottomCenter(this.getPos())))).orElse(0.0);
    }

    private void clearCreakingPuppet() {
        this.creakingPuppet = null;
        this.markDirty();
    }

    public void setCreakingPuppet(CreakingEntity creakingPuppet) {
        this.creakingPuppet = Either.left((Object)creakingPuppet);
        this.markDirty();
    }

    public void setCreakingPuppetFromUuid(UUID creakingPuppetUuid) {
        this.creakingPuppet = Either.right((Object)creakingPuppetUuid);
        this.ticks = 0L;
        this.markDirty();
    }

    private Optional<CreakingEntity> getCreakingPuppet() {
        World world;
        if (this.creakingPuppet == null) {
            return DEFAULT_CREAKING_PUPPET;
        }
        if (this.creakingPuppet.left().isPresent()) {
            CreakingEntity creakingEntity = (CreakingEntity)this.creakingPuppet.left().get();
            if (!creakingEntity.isRemoved()) {
                return Optional.of(creakingEntity);
            }
            this.setCreakingPuppetFromUuid(creakingEntity.getUuid());
        }
        if ((world = this.world) instanceof ServerWorld) {
            ServerWorld serverWorld = (ServerWorld)world;
            if (this.creakingPuppet.right().isPresent()) {
                UUID uUID = (UUID)this.creakingPuppet.right().get();
                Entity entity = serverWorld.getEntity(uUID);
                if (entity instanceof CreakingEntity) {
                    CreakingEntity creakingEntity2 = (CreakingEntity)entity;
                    this.setCreakingPuppet(creakingEntity2);
                    return Optional.of(creakingEntity2);
                }
                if (this.ticks >= 30L) {
                    this.clearCreakingPuppet();
                }
                return DEFAULT_CREAKING_PUPPET;
            }
        }
        return DEFAULT_CREAKING_PUPPET;
    }

    private static @Nullable CreakingEntity spawnCreakingPuppet(ServerWorld world, CreakingHeartBlockEntity blockEntity) {
        BlockPos blockPos = blockEntity.getPos();
        Optional<CreakingEntity> optional = LargeEntitySpawnHelper.trySpawnAt(EntityType.CREAKING, SpawnReason.SPAWNER, world, blockPos, 5, 16, 8, LargeEntitySpawnHelper.Requirements.CREAKING, true);
        if (optional.isEmpty()) {
            return null;
        }
        CreakingEntity creakingEntity = optional.get();
        world.emitGameEvent((Entity)creakingEntity, GameEvent.ENTITY_PLACE, creakingEntity.getEntityPos());
        world.sendEntityStatus(creakingEntity, (byte)60);
        creakingEntity.initHomePos(blockPos);
        return creakingEntity;
    }

    public BlockEntityUpdateS2CPacket toUpdatePacket() {
        return BlockEntityUpdateS2CPacket.create(this);
    }

    @Override
    public NbtCompound toInitialChunkDataNbt(RegistryWrapper.WrapperLookup registries) {
        return this.createComponentlessNbt(registries);
    }

    public void onPuppetDamage() {
        Object var2_1 = this.getCreakingPuppet().orElse(null);
        if (!(var2_1 instanceof CreakingEntity)) {
            return;
        }
        CreakingEntity creakingEntity = var2_1;
        World world = this.world;
        if (!(world instanceof ServerWorld)) {
            return;
        }
        ServerWorld serverWorld = (ServerWorld)world;
        if (this.trailParticlesSpawnTimer > 0) {
            return;
        }
        this.spawnTrailParticles(serverWorld, 20, false);
        if (this.getCachedState().get(CreakingHeartBlock.ACTIVE) == CreakingHeartState.AWAKE) {
            int i = this.world.getRandom().nextBetween(2, 3);
            for (int j = 0; j < i; ++j) {
                this.findResinGenerationPos(serverWorld).ifPresent(pos -> {
                    this.world.playSound(null, (BlockPos)pos, SoundEvents.BLOCK_RESIN_PLACE, SoundCategory.BLOCKS, 1.0f, 1.0f);
                    this.world.emitGameEvent((RegistryEntry<GameEvent>)GameEvent.BLOCK_PLACE, (BlockPos)pos, GameEvent.Emitter.of(this.getCachedState()));
                });
            }
        }
        this.trailParticlesSpawnTimer = 100;
        this.lastCreakingPuppetPos = creakingEntity.getBoundingBox().getCenter();
    }

    private Optional<BlockPos> findResinGenerationPos(ServerWorld world) {
        MutableObject mutable = new MutableObject(null);
        BlockPos.iterateRecursively(this.pos, 2, 64, (pos, consumer) -> {
            for (Direction direction : Util.copyShuffled(Direction.values(), serverWorld.random)) {
                BlockPos blockPos = pos.offset(direction);
                if (!world.getBlockState(blockPos).isIn(BlockTags.PALE_OAK_LOGS)) continue;
                consumer.accept(blockPos);
            }
        }, arg_0 -> CreakingHeartBlockEntity.method_65169(world, (Mutable)mutable, arg_0));
        return Optional.ofNullable((BlockPos)mutable.get());
    }

    private void spawnTrailParticles(ServerWorld world, int count, boolean towardsPuppet) {
        Object var5_4 = this.getCreakingPuppet().orElse(null);
        if (!(var5_4 instanceof CreakingEntity)) {
            return;
        }
        CreakingEntity creakingEntity = var5_4;
        int i = towardsPuppet ? 16545810 : 0x5F5F5F;
        Random random = world.random;
        for (double d = 0.0; d < (double)count; d += 1.0) {
            Box box = creakingEntity.getBoundingBox();
            Vec3d vec3d = box.getMinPos().add(random.nextDouble() * box.getLengthX(), random.nextDouble() * box.getLengthY(), random.nextDouble() * box.getLengthZ());
            Vec3d vec3d2 = Vec3d.of(this.getPos()).add(random.nextDouble(), random.nextDouble(), random.nextDouble());
            if (towardsPuppet) {
                Vec3d vec3d3 = vec3d;
                vec3d = vec3d2;
                vec3d2 = vec3d3;
            }
            TrailParticleEffect trailParticleEffect = new TrailParticleEffect(vec3d2, i, random.nextInt(40) + 10);
            world.spawnParticles(trailParticleEffect, true, true, vec3d.x, vec3d.y, vec3d.z, 1, 0.0, 0.0, 0.0, 0.0);
        }
    }

    @Override
    public void onBlockReplaced(BlockPos pos, BlockState oldState) {
        this.killPuppet(null);
    }

    public void killPuppet(@Nullable DamageSource damageSource) {
        Object var3_2 = this.getCreakingPuppet().orElse(null);
        if (var3_2 instanceof CreakingEntity) {
            CreakingEntity creakingEntity = var3_2;
            if (damageSource == null) {
                creakingEntity.finishCrumbling();
            } else {
                creakingEntity.killFromHeart(damageSource);
                creakingEntity.setCrumbling();
                creakingEntity.setHealth(0.0f);
            }
            this.clearCreakingPuppet();
        }
    }

    public boolean isPuppet(CreakingEntity creaking) {
        return this.getCreakingPuppet().map(puppet -> puppet == creaking).orElse(false);
    }

    public int getComparatorOutput() {
        return this.comparatorOutput;
    }

    public int calcComparatorOutput() {
        if (this.creakingPuppet == null || this.getCreakingPuppet().isEmpty()) {
            return 0;
        }
        double d = this.getDistanceToPuppet();
        double e = Math.clamp(d, 0.0, 32.0) / 32.0;
        return 15 - (int)Math.floor(e * 15.0);
    }

    @Override
    protected void readData(ReadView view) {
        super.readData(view);
        view.read("creaking", Uuids.INT_STREAM_CODEC).ifPresentOrElse(this::setCreakingPuppetFromUuid, this::clearCreakingPuppet);
    }

    @Override
    protected void writeData(WriteView view) {
        super.writeData(view);
        if (this.creakingPuppet != null) {
            view.put("creaking", Uuids.INT_STREAM_CODEC, (UUID)this.creakingPuppet.map(Entity::getUuid, uuid -> uuid));
        }
    }

    public /* synthetic */ Packet toUpdatePacket() {
        return this.toUpdatePacket();
    }

    private static /* synthetic */ BlockPos.IterationState method_65169(ServerWorld serverWorld, Mutable mutable, BlockPos pos) {
        if (!serverWorld.getBlockState(pos).isIn(BlockTags.PALE_OAK_LOGS)) {
            return BlockPos.IterationState.ACCEPT;
        }
        for (Direction direction : Util.copyShuffled(Direction.values(), serverWorld.random)) {
            BlockPos blockPos = pos.offset(direction);
            BlockState blockState = serverWorld.getBlockState(blockPos);
            Direction direction2 = direction.getOpposite();
            if (blockState.isAir()) {
                blockState = Blocks.RESIN_CLUMP.getDefaultState();
            } else if (blockState.isOf(Blocks.WATER) && blockState.getFluidState().isStill()) {
                blockState = (BlockState)Blocks.RESIN_CLUMP.getDefaultState().with(MultifaceBlock.WATERLOGGED, true);
            }
            if (!blockState.isOf(Blocks.RESIN_CLUMP) || MultifaceBlock.hasDirection(blockState, direction2)) continue;
            serverWorld.setBlockState(blockPos, (BlockState)blockState.with(MultifaceBlock.getProperty(direction2), true), 3);
            mutable.setValue((Object)blockPos);
            return BlockPos.IterationState.STOP;
        }
        return BlockPos.IterationState.ACCEPT;
    }
}


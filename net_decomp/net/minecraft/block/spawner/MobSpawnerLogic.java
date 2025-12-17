/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  com.mojang.logging.LogUtils
 *  org.jspecify.annotations.Nullable
 *  org.slf4j.Logger
 */
package net.minecraft.block.spawner;

import com.mojang.logging.LogUtils;
import java.util.Optional;
import net.minecraft.block.spawner.MobSpawnerEntry;
import net.minecraft.entity.Entity;
import net.minecraft.entity.EntityType;
import net.minecraft.entity.LoadedEntityProcessor;
import net.minecraft.entity.SpawnReason;
import net.minecraft.entity.SpawnRestriction;
import net.minecraft.entity.mob.MobEntity;
import net.minecraft.nbt.NbtCompound;
import net.minecraft.particle.ParticleTypes;
import net.minecraft.predicate.entity.EntityPredicates;
import net.minecraft.registry.Registries;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.storage.NbtReadView;
import net.minecraft.storage.ReadView;
import net.minecraft.storage.WriteView;
import net.minecraft.util.ErrorReporter;
import net.minecraft.util.TypeFilter;
import net.minecraft.util.collection.Pool;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Box;
import net.minecraft.util.math.Vec3d;
import net.minecraft.util.math.random.Random;
import net.minecraft.world.Difficulty;
import net.minecraft.world.World;
import net.minecraft.world.event.GameEvent;
import org.jspecify.annotations.Nullable;
import org.slf4j.Logger;

public abstract class MobSpawnerLogic {
    private static final Logger LOGGER = LogUtils.getLogger();
    public static final String SPAWN_DATA_KEY = "SpawnData";
    private static final int field_30951 = 1;
    private static final int field_57757 = 20;
    private static final int DEFAULT_MIN_SPAWN_DELAY = 200;
    private static final int DEFAULT_MAX_SPAWN_DELAY = 800;
    private static final int DEFAULT_SPAWN_COUNT = 4;
    private static final int DEFAULT_MAX_NEARBY_ENTITIES = 6;
    private static final int DEFAULT_REQUIRED_PLAYER_RANGE = 16;
    private static final int DEFAULT_SPAWN_RANGE = 4;
    private int spawnDelay = 20;
    private Pool<MobSpawnerEntry> spawnPotentials = Pool.empty();
    private @Nullable MobSpawnerEntry spawnEntry;
    private double rotation;
    private double lastRotation;
    private int minSpawnDelay = 200;
    private int maxSpawnDelay = 800;
    private int spawnCount = 4;
    private @Nullable Entity renderedEntity;
    private int maxNearbyEntities = 6;
    private int requiredPlayerRange = 16;
    private int spawnRange = 4;

    public void setEntityId(EntityType<?> type, @Nullable World world, Random random, BlockPos pos) {
        this.getSpawnEntry(world, random, pos).getNbt().putString("id", Registries.ENTITY_TYPE.getId(type).toString());
    }

    private boolean isPlayerInRange(World world, BlockPos pos) {
        return world.isPlayerInRange((double)pos.getX() + 0.5, (double)pos.getY() + 0.5, (double)pos.getZ() + 0.5, this.requiredPlayerRange);
    }

    public void clientTick(World world, BlockPos pos) {
        if (!this.isPlayerInRange(world, pos)) {
            this.lastRotation = this.rotation;
        } else if (this.renderedEntity != null) {
            Random random = world.getRandom();
            double d = (double)pos.getX() + random.nextDouble();
            double e = (double)pos.getY() + random.nextDouble();
            double f = (double)pos.getZ() + random.nextDouble();
            world.addParticleClient(ParticleTypes.SMOKE, d, e, f, 0.0, 0.0, 0.0);
            world.addParticleClient(ParticleTypes.FLAME, d, e, f, 0.0, 0.0, 0.0);
            if (this.spawnDelay > 0) {
                --this.spawnDelay;
            }
            this.lastRotation = this.rotation;
            this.rotation = (this.rotation + (double)(1000.0f / ((float)this.spawnDelay + 200.0f))) % 360.0;
        }
    }

    public void serverTick(ServerWorld world, BlockPos pos) {
        if (!this.isPlayerInRange(world, pos) || !world.areSpawnerBlocksEnabled()) {
            return;
        }
        if (this.spawnDelay == -1) {
            this.updateSpawns(world, pos);
        }
        if (this.spawnDelay > 0) {
            --this.spawnDelay;
            return;
        }
        boolean bl = false;
        Random random = world.getRandom();
        MobSpawnerEntry mobSpawnerEntry = this.getSpawnEntry(world, random, pos);
        for (int i = 0; i < this.spawnCount; ++i) {
            try (ErrorReporter.Logging logging = new ErrorReporter.Logging(this::toString, LOGGER);){
                ReadView readView = NbtReadView.create(logging, world.getRegistryManager(), mobSpawnerEntry.getNbt());
                Optional<EntityType<?>> optional = EntityType.fromData(readView);
                if (optional.isEmpty()) {
                    this.updateSpawns(world, pos);
                    return;
                }
                Vec3d vec3d = readView.read("Pos", Vec3d.CODEC).orElseGet(() -> new Vec3d((double)pos.getX() + (random.nextDouble() - random.nextDouble()) * (double)this.spawnRange + 0.5, pos.getY() + random.nextInt(3) - 1, (double)pos.getZ() + (random.nextDouble() - random.nextDouble()) * (double)this.spawnRange + 0.5));
                if (!world.isSpaceEmpty(optional.get().getSpawnBox(vec3d.x, vec3d.y, vec3d.z))) continue;
                BlockPos blockPos = BlockPos.ofFloored(vec3d);
                if (mobSpawnerEntry.getCustomSpawnRules().isPresent()) {
                    MobSpawnerEntry.CustomSpawnRules customSpawnRules;
                    if (!optional.get().getSpawnGroup().isPeaceful() && world.getDifficulty() == Difficulty.PEACEFUL || !(customSpawnRules = mobSpawnerEntry.getCustomSpawnRules().get()).canSpawn(blockPos, world)) continue;
                } else if (!SpawnRestriction.canSpawn(optional.get(), world, SpawnReason.SPAWNER, blockPos, world.getRandom())) continue;
                Entity entity2 = EntityType.loadEntityWithPassengers(readView, (World)world, SpawnReason.SPAWNER, entity -> {
                    entity.refreshPositionAndAngles(vec3d.x, vec3d.y, vec3d.z, entity.getYaw(), entity.getPitch());
                    return entity;
                });
                if (entity2 == null) {
                    this.updateSpawns(world, pos);
                    return;
                }
                int j = world.getEntitiesByType(TypeFilter.equals(entity2.getClass()), new Box(pos.getX(), pos.getY(), pos.getZ(), pos.getX() + 1, pos.getY() + 1, pos.getZ() + 1).expand(this.spawnRange), EntityPredicates.EXCEPT_SPECTATOR).size();
                if (j >= this.maxNearbyEntities) {
                    this.updateSpawns(world, pos);
                    return;
                }
                entity2.refreshPositionAndAngles(entity2.getX(), entity2.getY(), entity2.getZ(), random.nextFloat() * 360.0f, 0.0f);
                if (entity2 instanceof MobEntity) {
                    boolean bl2;
                    MobEntity mobEntity = (MobEntity)entity2;
                    if (mobSpawnerEntry.getCustomSpawnRules().isEmpty() && !mobEntity.canSpawn(world, SpawnReason.SPAWNER) || !mobEntity.canSpawn(world)) continue;
                    boolean bl3 = bl2 = mobSpawnerEntry.getNbt().getSize() == 1 && mobSpawnerEntry.getNbt().getString("id").isPresent();
                    if (bl2) {
                        ((MobEntity)entity2).initialize(world, world.getLocalDifficulty(entity2.getBlockPos()), SpawnReason.SPAWNER, null);
                    }
                    mobSpawnerEntry.getEquipment().ifPresent(mobEntity::setEquipmentFromTable);
                }
                if (!world.spawnNewEntityAndPassengers(entity2)) {
                    this.updateSpawns(world, pos);
                    return;
                }
                world.syncWorldEvent(2004, pos, 0);
                world.emitGameEvent(entity2, GameEvent.ENTITY_PLACE, blockPos);
                if (entity2 instanceof MobEntity) {
                    ((MobEntity)entity2).playSpawnEffects();
                }
                bl = true;
                continue;
            }
        }
        if (bl) {
            this.updateSpawns(world, pos);
        }
    }

    private void updateSpawns(World world, BlockPos pos) {
        Random random = world.random;
        this.spawnDelay = this.maxSpawnDelay <= this.minSpawnDelay ? this.minSpawnDelay : this.minSpawnDelay + random.nextInt(this.maxSpawnDelay - this.minSpawnDelay);
        this.spawnPotentials.getOrEmpty(random).ifPresent(spawnPotential -> this.setSpawnEntry(world, pos, (MobSpawnerEntry)spawnPotential));
        this.sendStatus(world, pos, 1);
    }

    public void readData(@Nullable World world, BlockPos pos, ReadView view) {
        this.spawnDelay = view.getShort("Delay", (short)20);
        view.read(SPAWN_DATA_KEY, MobSpawnerEntry.CODEC).ifPresent(mobSpawnerEntry -> this.setSpawnEntry(world, pos, (MobSpawnerEntry)mobSpawnerEntry));
        this.spawnPotentials = view.read("SpawnPotentials", MobSpawnerEntry.DATA_POOL_CODEC).orElseGet(() -> Pool.of(this.spawnEntry != null ? this.spawnEntry : new MobSpawnerEntry()));
        this.minSpawnDelay = view.getInt("MinSpawnDelay", 200);
        this.maxSpawnDelay = view.getInt("MaxSpawnDelay", 800);
        this.spawnCount = view.getInt("SpawnCount", 4);
        this.maxNearbyEntities = view.getInt("MaxNearbyEntities", 6);
        this.requiredPlayerRange = view.getInt("RequiredPlayerRange", 16);
        this.spawnRange = view.getInt("SpawnRange", 4);
        this.renderedEntity = null;
    }

    public void writeData(WriteView view) {
        view.putShort("Delay", (short)this.spawnDelay);
        view.putShort("MinSpawnDelay", (short)this.minSpawnDelay);
        view.putShort("MaxSpawnDelay", (short)this.maxSpawnDelay);
        view.putShort("SpawnCount", (short)this.spawnCount);
        view.putShort("MaxNearbyEntities", (short)this.maxNearbyEntities);
        view.putShort("RequiredPlayerRange", (short)this.requiredPlayerRange);
        view.putShort("SpawnRange", (short)this.spawnRange);
        view.putNullable(SPAWN_DATA_KEY, MobSpawnerEntry.CODEC, this.spawnEntry);
        view.put("SpawnPotentials", MobSpawnerEntry.DATA_POOL_CODEC, this.spawnPotentials);
    }

    public @Nullable Entity getRenderedEntity(World world, BlockPos pos) {
        if (this.renderedEntity == null) {
            NbtCompound nbtCompound = this.getSpawnEntry(world, world.getRandom(), pos).getNbt();
            if (nbtCompound.getString("id").isEmpty()) {
                return null;
            }
            this.renderedEntity = EntityType.loadEntityWithPassengers(nbtCompound, world, SpawnReason.SPAWNER, LoadedEntityProcessor.NOOP);
            if (nbtCompound.getSize() != 1 || this.renderedEntity instanceof MobEntity) {
                // empty if block
            }
        }
        return this.renderedEntity;
    }

    public boolean handleStatus(World world, int status) {
        if (status == 1) {
            if (world.isClient()) {
                this.spawnDelay = this.minSpawnDelay;
            }
            return true;
        }
        return false;
    }

    protected void setSpawnEntry(@Nullable World world, BlockPos pos, MobSpawnerEntry spawnEntry) {
        this.spawnEntry = spawnEntry;
    }

    private MobSpawnerEntry getSpawnEntry(@Nullable World world, Random random, BlockPos pos) {
        if (this.spawnEntry != null) {
            return this.spawnEntry;
        }
        this.setSpawnEntry(world, pos, this.spawnPotentials.getOrEmpty(random).orElseGet(MobSpawnerEntry::new));
        return this.spawnEntry;
    }

    public abstract void sendStatus(World var1, BlockPos var2, int var3);

    public double getRotation() {
        return this.rotation;
    }

    public double getLastRotation() {
        return this.lastRotation;
    }
}


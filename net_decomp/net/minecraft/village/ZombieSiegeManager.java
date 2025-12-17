/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  com.mojang.logging.LogUtils
 *  org.jspecify.annotations.Nullable
 *  org.slf4j.Logger
 */
package net.minecraft.village;

import com.mojang.logging.LogUtils;
import net.minecraft.entity.EntityType;
import net.minecraft.entity.SpawnReason;
import net.minecraft.entity.mob.HostileEntity;
import net.minecraft.entity.mob.ZombieEntity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.registry.tag.BiomeTags;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.MathHelper;
import net.minecraft.util.math.Vec3d;
import net.minecraft.world.Heightmap;
import net.minecraft.world.spawner.SpecialSpawner;
import org.jspecify.annotations.Nullable;
import org.slf4j.Logger;

public class ZombieSiegeManager
implements SpecialSpawner {
    private static final Logger LOGGER = LogUtils.getLogger();
    private boolean spawned;
    private State state = State.SIEGE_DONE;
    private int remaining;
    private int countdown;
    private int startX;
    private int startY;
    private int startZ;

    @Override
    public void spawn(ServerWorld world, boolean spawnMonsters) {
        if (world.isDay() || !spawnMonsters) {
            this.state = State.SIEGE_DONE;
            this.spawned = false;
            return;
        }
        long l = world.getTimeOfDay() % 24000L;
        if (l == 18000L) {
            State state = this.state = world.random.nextInt(10) == 0 ? State.SIEGE_TONIGHT : State.SIEGE_DONE;
        }
        if (this.state == State.SIEGE_DONE) {
            return;
        }
        if (!this.spawned) {
            if (this.spawn(world)) {
                this.spawned = true;
            } else {
                return;
            }
        }
        if (this.countdown > 0) {
            --this.countdown;
            return;
        }
        this.countdown = 2;
        if (this.remaining > 0) {
            this.trySpawnZombie(world);
            --this.remaining;
        } else {
            this.state = State.SIEGE_DONE;
        }
    }

    private boolean spawn(ServerWorld world) {
        for (PlayerEntity playerEntity : world.getPlayers()) {
            BlockPos blockPos;
            if (playerEntity.isSpectator() || !world.isNearOccupiedPointOfInterest(blockPos = playerEntity.getBlockPos()) || world.getBiome(blockPos).isIn(BiomeTags.WITHOUT_ZOMBIE_SIEGES)) continue;
            for (int i = 0; i < 10; ++i) {
                float f = world.random.nextFloat() * ((float)Math.PI * 2);
                this.startX = blockPos.getX() + MathHelper.floor(MathHelper.cos(f) * 32.0f);
                this.startY = blockPos.getY();
                this.startZ = blockPos.getZ() + MathHelper.floor(MathHelper.sin(f) * 32.0f);
                if (this.getSpawnVector(world, new BlockPos(this.startX, this.startY, this.startZ)) == null) continue;
                this.countdown = 0;
                this.remaining = 20;
                break;
            }
            return true;
        }
        return false;
    }

    private void trySpawnZombie(ServerWorld world) {
        ZombieEntity zombieEntity;
        Vec3d vec3d = this.getSpawnVector(world, new BlockPos(this.startX, this.startY, this.startZ));
        if (vec3d == null) {
            return;
        }
        try {
            zombieEntity = new ZombieEntity(world);
            zombieEntity.initialize(world, world.getLocalDifficulty(zombieEntity.getBlockPos()), SpawnReason.EVENT, null);
        }
        catch (Exception exception) {
            LOGGER.warn("Failed to create zombie for village siege at {}", (Object)vec3d, (Object)exception);
            return;
        }
        zombieEntity.refreshPositionAndAngles(vec3d.x, vec3d.y, vec3d.z, world.random.nextFloat() * 360.0f, 0.0f);
        world.spawnEntityAndPassengers(zombieEntity);
    }

    private @Nullable Vec3d getSpawnVector(ServerWorld world, BlockPos pos) {
        for (int i = 0; i < 10; ++i) {
            int k;
            int l;
            int j = pos.getX() + world.random.nextInt(16) - 8;
            BlockPos blockPos = new BlockPos(j, l = world.getTopY(Heightmap.Type.WORLD_SURFACE, j, k = pos.getZ() + world.random.nextInt(16) - 8), k);
            if (!world.isNearOccupiedPointOfInterest(blockPos) || !HostileEntity.canSpawnInDark(EntityType.ZOMBIE, world, SpawnReason.EVENT, blockPos, world.random)) continue;
            return Vec3d.ofBottomCenter(blockPos);
        }
        return null;
    }

    static final class State
    extends Enum<State> {
        public static final /* enum */ State SIEGE_CAN_ACTIVATE = new State();
        public static final /* enum */ State SIEGE_TONIGHT = new State();
        public static final /* enum */ State SIEGE_DONE = new State();
        private static final /* synthetic */ State[] field_18483;

        public static State[] values() {
            return (State[])field_18483.clone();
        }

        public static State valueOf(String string) {
            return Enum.valueOf(State.class, string);
        }

        private static /* synthetic */ State[] method_36628() {
            return new State[]{SIEGE_CAN_ACTIVATE, SIEGE_TONIGHT, SIEGE_DONE};
        }

        static {
            field_18483 = State.method_36628();
        }
    }
}


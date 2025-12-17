/*
 * Decompiled with CFR 0.152.
 */
package net.minecraft.world.spawner;

import net.minecraft.block.BlockState;
import net.minecraft.entity.EntityType;
import net.minecraft.entity.SpawnReason;
import net.minecraft.entity.mob.PatrolEntity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.random.Random;
import net.minecraft.world.Heightmap;
import net.minecraft.world.SpawnHelper;
import net.minecraft.world.attribute.EnvironmentAttributes;
import net.minecraft.world.rule.GameRules;
import net.minecraft.world.spawner.SpecialSpawner;

public class PatrolSpawner
implements SpecialSpawner {
    private int cooldown;

    @Override
    public void spawn(ServerWorld world, boolean spawnMonsters) {
        if (!spawnMonsters) {
            return;
        }
        if (!world.getGameRules().getValue(GameRules.SPAWN_PATROLS).booleanValue()) {
            return;
        }
        Random random = world.random;
        --this.cooldown;
        if (this.cooldown > 0) {
            return;
        }
        this.cooldown += 12000 + random.nextInt(1200);
        if (!world.isDay()) {
            return;
        }
        if (random.nextInt(5) != 0) {
            return;
        }
        int i = world.getPlayers().size();
        if (i < 1) {
            return;
        }
        PlayerEntity playerEntity = world.getPlayers().get(random.nextInt(i));
        if (playerEntity.isSpectator()) {
            return;
        }
        if (world.isNearOccupiedPointOfInterest(playerEntity.getBlockPos(), 2)) {
            return;
        }
        int j = (24 + random.nextInt(24)) * (random.nextBoolean() ? -1 : 1);
        int k = (24 + random.nextInt(24)) * (random.nextBoolean() ? -1 : 1);
        BlockPos.Mutable mutable = playerEntity.getBlockPos().mutableCopy().move(j, 0, k);
        int l = 10;
        if (!world.isRegionLoaded(mutable.getX() - 10, mutable.getZ() - 10, mutable.getX() + 10, mutable.getZ() + 10)) {
            return;
        }
        if (!world.getEnvironmentAttributes().getAttributeValue(EnvironmentAttributes.CAN_PILLAGER_PATROL_SPAWN_GAMEPLAY, mutable).booleanValue()) {
            return;
        }
        int m = (int)Math.ceil(world.getLocalDifficulty(mutable).getLocalDifficulty()) + 1;
        for (int n = 0; n < m; ++n) {
            mutable.setY(world.getTopPosition(Heightmap.Type.MOTION_BLOCKING_NO_LEAVES, mutable).getY());
            if (n == 0) {
                if (!this.spawnPillager(world, mutable, random, true)) {
                    break;
                }
            } else {
                this.spawnPillager(world, mutable, random, false);
            }
            mutable.setX(mutable.getX() + random.nextInt(5) - random.nextInt(5));
            mutable.setZ(mutable.getZ() + random.nextInt(5) - random.nextInt(5));
        }
    }

    private boolean spawnPillager(ServerWorld world, BlockPos pos, Random random, boolean captain) {
        BlockState blockState = world.getBlockState(pos);
        if (!SpawnHelper.isClearForSpawn(world, pos, blockState, blockState.getFluidState(), EntityType.PILLAGER)) {
            return false;
        }
        if (!PatrolEntity.canSpawn(EntityType.PILLAGER, world, SpawnReason.PATROL, pos, random)) {
            return false;
        }
        PatrolEntity patrolEntity = EntityType.PILLAGER.create(world, SpawnReason.PATROL);
        if (patrolEntity != null) {
            if (captain) {
                patrolEntity.setPatrolLeader(true);
                patrolEntity.setRandomPatrolTarget();
            }
            patrolEntity.setPosition(pos.getX(), pos.getY(), pos.getZ());
            patrolEntity.initialize(world, world.getLocalDifficulty(pos), SpawnReason.PATROL, null);
            world.spawnEntityAndPassengers(patrolEntity);
            return true;
        }
        return false;
    }
}


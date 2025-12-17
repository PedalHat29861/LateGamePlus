/*
 * Decompiled with CFR 0.152.
 */
package net.minecraft.world.spawner;

import net.minecraft.block.BlockState;
import net.minecraft.entity.EntityData;
import net.minecraft.entity.EntityType;
import net.minecraft.entity.SpawnReason;
import net.minecraft.entity.mob.PhantomEntity;
import net.minecraft.fluid.FluidState;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.stat.ServerStatHandler;
import net.minecraft.stat.Stats;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.MathHelper;
import net.minecraft.util.math.random.Random;
import net.minecraft.world.LocalDifficulty;
import net.minecraft.world.SpawnHelper;
import net.minecraft.world.rule.GameRules;
import net.minecraft.world.spawner.SpecialSpawner;

public class PhantomSpawner
implements SpecialSpawner {
    private int cooldown;

    @Override
    public void spawn(ServerWorld world, boolean spawnMonsters) {
        if (!spawnMonsters) {
            return;
        }
        if (!world.getGameRules().getValue(GameRules.SPAWN_PHANTOMS).booleanValue()) {
            return;
        }
        Random random = world.random;
        --this.cooldown;
        if (this.cooldown > 0) {
            return;
        }
        this.cooldown += (60 + random.nextInt(60)) * 20;
        if (world.getAmbientDarkness() < 5 && world.getDimension().hasSkyLight()) {
            return;
        }
        for (ServerPlayerEntity serverPlayerEntity : world.getPlayers()) {
            FluidState fluidState;
            BlockState blockState;
            BlockPos blockPos2;
            LocalDifficulty localDifficulty;
            if (serverPlayerEntity.isSpectator()) continue;
            BlockPos blockPos = serverPlayerEntity.getBlockPos();
            if (world.getDimension().hasSkyLight() && (blockPos.getY() < world.getSeaLevel() || !world.isSkyVisible(blockPos)) || !(localDifficulty = world.getLocalDifficulty(blockPos)).isHarderThan(random.nextFloat() * 3.0f)) continue;
            ServerStatHandler serverStatHandler = serverPlayerEntity.getStatHandler();
            int i = MathHelper.clamp(serverStatHandler.getStat(Stats.CUSTOM.getOrCreateStat(Stats.TIME_SINCE_REST)), 1, Integer.MAX_VALUE);
            int j = 24000;
            if (random.nextInt(i) < 72000 || !SpawnHelper.isClearForSpawn(world, blockPos2 = blockPos.up(20 + random.nextInt(15)).east(-10 + random.nextInt(21)).south(-10 + random.nextInt(21)), blockState = world.getBlockState(blockPos2), fluidState = world.getFluidState(blockPos2), EntityType.PHANTOM)) continue;
            EntityData entityData = null;
            int k = 1 + random.nextInt(localDifficulty.getGlobalDifficulty().getId() + 1);
            for (int l = 0; l < k; ++l) {
                PhantomEntity phantomEntity = EntityType.PHANTOM.create(world, SpawnReason.NATURAL);
                if (phantomEntity == null) continue;
                phantomEntity.refreshPositionAndAngles(blockPos2, 0.0f, 0.0f);
                entityData = phantomEntity.initialize(world, localDifficulty, SpawnReason.NATURAL, entityData);
                world.spawnEntityAndPassengers(phantomEntity);
            }
        }
    }
}


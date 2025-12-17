/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  com.google.common.collect.ImmutableList
 *  com.google.common.collect.ImmutableSet
 *  com.google.common.collect.Lists
 */
package net.minecraft.entity.ai.brain.sensor;

import com.google.common.collect.ImmutableList;
import com.google.common.collect.ImmutableSet;
import com.google.common.collect.Lists;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.Set;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.ai.brain.Brain;
import net.minecraft.entity.ai.brain.LivingTargetCache;
import net.minecraft.entity.ai.brain.MemoryModuleType;
import net.minecraft.entity.ai.brain.sensor.Sensor;
import net.minecraft.entity.boss.WitherEntity;
import net.minecraft.entity.mob.AbstractPiglinEntity;
import net.minecraft.entity.mob.MobEntity;
import net.minecraft.entity.mob.WitherSkeletonEntity;
import net.minecraft.server.world.ServerWorld;

public class PiglinBruteSpecificSensor
extends Sensor<LivingEntity> {
    @Override
    public Set<MemoryModuleType<?>> getOutputMemoryModules() {
        return ImmutableSet.of(MemoryModuleType.VISIBLE_MOBS, MemoryModuleType.NEAREST_VISIBLE_NEMESIS, MemoryModuleType.NEARBY_ADULT_PIGLINS);
    }

    @Override
    protected void sense(ServerWorld world, LivingEntity entity) {
        Brain<?> brain = entity.getBrain();
        ArrayList list = Lists.newArrayList();
        LivingTargetCache livingTargetCache = brain.getOptionalRegisteredMemory(MemoryModuleType.VISIBLE_MOBS).orElse(LivingTargetCache.empty());
        Optional<MobEntity> optional = livingTargetCache.findFirst(visibleEntity -> visibleEntity instanceof WitherSkeletonEntity || visibleEntity instanceof WitherEntity).map(MobEntity.class::cast);
        List<LivingEntity> list2 = brain.getOptionalRegisteredMemory(MemoryModuleType.MOBS).orElse((List<LivingEntity>)ImmutableList.of());
        for (LivingEntity livingEntity : list2) {
            if (!(livingEntity instanceof AbstractPiglinEntity) || !((AbstractPiglinEntity)livingEntity).isAdult()) continue;
            list.add((AbstractPiglinEntity)livingEntity);
        }
        brain.remember(MemoryModuleType.NEAREST_VISIBLE_NEMESIS, optional);
        brain.remember(MemoryModuleType.NEARBY_ADULT_PIGLINS, list);
    }
}


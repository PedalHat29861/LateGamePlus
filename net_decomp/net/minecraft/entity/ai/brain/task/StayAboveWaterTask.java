/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  com.google.common.collect.ImmutableMap
 */
package net.minecraft.entity.ai.brain.task;

import com.google.common.collect.ImmutableMap;
import java.util.Map;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.ai.brain.MemoryModuleState;
import net.minecraft.entity.ai.brain.MemoryModuleType;
import net.minecraft.entity.ai.brain.task.MultiTickTask;
import net.minecraft.entity.mob.MobEntity;
import net.minecraft.registry.tag.FluidTags;
import net.minecraft.server.world.ServerWorld;

public class StayAboveWaterTask<T extends MobEntity>
extends MultiTickTask<T> {
    private final float chance;

    public StayAboveWaterTask(float chance) {
        super((Map<MemoryModuleType<?>, MemoryModuleState>)ImmutableMap.of());
        this.chance = chance;
    }

    public static <T extends MobEntity> boolean isUnderwater(T entity) {
        return entity.isTouchingWater() && entity.getFluidHeight(FluidTags.WATER) > entity.getSwimHeight() || entity.isInLava();
    }

    @Override
    protected boolean shouldRun(ServerWorld serverWorld, MobEntity mobEntity) {
        return StayAboveWaterTask.isUnderwater(mobEntity);
    }

    @Override
    protected boolean shouldKeepRunning(ServerWorld serverWorld, MobEntity mobEntity, long l) {
        return this.shouldRun(serverWorld, mobEntity);
    }

    @Override
    protected void keepRunning(ServerWorld serverWorld, MobEntity mobEntity, long l) {
        if (mobEntity.getRandom().nextFloat() < this.chance) {
            mobEntity.getJumpControl().setActive();
        }
    }

    @Override
    protected /* synthetic */ void keepRunning(ServerWorld world, LivingEntity entity, long time) {
        this.keepRunning(world, (MobEntity)entity, time);
    }
}


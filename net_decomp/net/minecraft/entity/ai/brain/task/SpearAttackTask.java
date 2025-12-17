/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  org.jspecify.annotations.Nullable
 */
package net.minecraft.entity.ai.brain.task;

import java.util.Map;
import net.minecraft.component.DataComponentTypes;
import net.minecraft.entity.Entity;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.ai.brain.EntityLookTarget;
import net.minecraft.entity.ai.brain.MemoryModuleState;
import net.minecraft.entity.ai.brain.MemoryModuleType;
import net.minecraft.entity.ai.brain.task.MultiTickTask;
import net.minecraft.entity.ai.brain.task.SpearChargeTask;
import net.minecraft.entity.mob.MobEntity;
import net.minecraft.entity.mob.PathAwareEntity;
import net.minecraft.server.world.ServerWorld;
import org.jspecify.annotations.Nullable;

public class SpearAttackTask
extends MultiTickTask<PathAwareEntity> {
    double speed;
    float squaredAttackRange;

    public SpearAttackTask(double speed, float attackRange) {
        super(Map.of(MemoryModuleType.SPEAR_STATUS, MemoryModuleState.VALUE_ABSENT));
        this.speed = speed;
        this.squaredAttackRange = attackRange * attackRange;
    }

    private boolean canRun(PathAwareEntity entity) {
        return this.getAttackTarget(entity) != null && entity.getMainHandStack().contains(DataComponentTypes.KINETIC_WEAPON);
    }

    @Override
    protected boolean shouldRun(ServerWorld serverWorld, PathAwareEntity pathAwareEntity) {
        return this.canRun(pathAwareEntity) && !pathAwareEntity.isUsingItem();
    }

    @Override
    protected void run(ServerWorld serverWorld, PathAwareEntity pathAwareEntity, long l) {
        pathAwareEntity.setAttacking(true);
        pathAwareEntity.getBrain().remember(MemoryModuleType.SPEAR_STATUS, SpearChargeTask.AdvanceState.APPROACH);
        super.run(serverWorld, pathAwareEntity, l);
    }

    private @Nullable LivingEntity getAttackTarget(PathAwareEntity entity) {
        return entity.getBrain().getOptionalRegisteredMemory(MemoryModuleType.ATTACK_TARGET).orElse(null);
    }

    @Override
    protected boolean shouldKeepRunning(ServerWorld serverWorld, PathAwareEntity pathAwareEntity, long l) {
        return this.canRun(pathAwareEntity) && this.isTargetWithinRange(pathAwareEntity);
    }

    private boolean isTargetWithinRange(PathAwareEntity entity) {
        LivingEntity livingEntity = this.getAttackTarget(entity);
        double d = entity.squaredDistanceTo(livingEntity.getX(), livingEntity.getY(), livingEntity.getZ());
        return d > (double)this.squaredAttackRange;
    }

    @Override
    protected void keepRunning(ServerWorld serverWorld, PathAwareEntity pathAwareEntity, long l) {
        LivingEntity livingEntity = this.getAttackTarget(pathAwareEntity);
        Entity entity = pathAwareEntity.getRootVehicle();
        float f = 1.0f;
        if (entity instanceof MobEntity) {
            MobEntity mobEntity = (MobEntity)entity;
            f = mobEntity.getRiderChargingSpeedMultiplier();
        }
        pathAwareEntity.getBrain().remember(MemoryModuleType.LOOK_TARGET, new EntityLookTarget(livingEntity, true));
        pathAwareEntity.getNavigation().startMovingTo(livingEntity, (double)f * this.speed);
    }

    @Override
    protected void finishRunning(ServerWorld serverWorld, PathAwareEntity pathAwareEntity, long l) {
        pathAwareEntity.getNavigation().stop();
        pathAwareEntity.getBrain().remember(MemoryModuleType.SPEAR_STATUS, SpearChargeTask.AdvanceState.CHARGING);
    }

    @Override
    protected boolean isTimeLimitExceeded(long time) {
        return false;
    }

    @Override
    protected /* synthetic */ boolean shouldKeepRunning(ServerWorld world, LivingEntity entity, long time) {
        return this.shouldKeepRunning(world, (PathAwareEntity)entity, time);
    }

    @Override
    protected /* synthetic */ void finishRunning(ServerWorld world, LivingEntity entity, long time) {
        this.finishRunning(world, (PathAwareEntity)entity, time);
    }

    @Override
    protected /* synthetic */ void run(ServerWorld world, LivingEntity entity, long time) {
        this.run(world, (PathAwareEntity)entity, time);
    }
}


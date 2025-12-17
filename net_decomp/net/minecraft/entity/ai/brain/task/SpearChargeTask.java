/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  org.jspecify.annotations.Nullable
 */
package net.minecraft.entity.ai.brain.task;

import java.util.Map;
import java.util.Optional;
import net.minecraft.component.DataComponentTypes;
import net.minecraft.component.type.KineticWeaponComponent;
import net.minecraft.entity.Entity;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.ai.FuzzyTargeting;
import net.minecraft.entity.ai.brain.EntityLookTarget;
import net.minecraft.entity.ai.brain.MemoryModuleState;
import net.minecraft.entity.ai.brain.MemoryModuleType;
import net.minecraft.entity.ai.brain.task.MultiTickTask;
import net.minecraft.entity.mob.MobEntity;
import net.minecraft.entity.mob.PathAwareEntity;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.util.Hand;
import net.minecraft.util.math.Vec3d;
import org.jspecify.annotations.Nullable;

public class SpearChargeTask
extends MultiTickTask<PathAwareEntity> {
    public static final int field_64623 = 6;
    public static final int field_64624 = 7;
    double chargeStartSpeed;
    double chargeSpeed;
    float field_64627;
    float squaredChargeRange;

    public SpearChargeTask(double chargeStartSpeed, double chargeSpeed, float f, float chargeRange) {
        super(Map.of(MemoryModuleType.SPEAR_STATUS, MemoryModuleState.VALUE_PRESENT));
        this.chargeStartSpeed = chargeStartSpeed;
        this.chargeSpeed = chargeSpeed;
        this.field_64627 = f * f;
        this.squaredChargeRange = chargeRange * chargeRange;
    }

    private @Nullable LivingEntity getTarget(PathAwareEntity entity) {
        return entity.getBrain().getOptionalRegisteredMemory(MemoryModuleType.ATTACK_TARGET).orElse(null);
    }

    private boolean shouldAttack(PathAwareEntity entity) {
        return this.getTarget(entity) != null && entity.getMainHandStack().contains(DataComponentTypes.KINETIC_WEAPON);
    }

    private int getSpearUseTicks(PathAwareEntity entity) {
        return Optional.ofNullable(entity.getMainHandStack().get(DataComponentTypes.KINETIC_WEAPON)).map(KineticWeaponComponent::getUseTicks).orElse(0);
    }

    @Override
    protected boolean shouldRun(ServerWorld serverWorld, PathAwareEntity pathAwareEntity) {
        return pathAwareEntity.getBrain().getOptionalRegisteredMemory(MemoryModuleType.SPEAR_STATUS).orElse(AdvanceState.APPROACH) == AdvanceState.CHARGING && this.shouldAttack(pathAwareEntity) && !pathAwareEntity.isUsingItem();
    }

    @Override
    protected void run(ServerWorld serverWorld, PathAwareEntity pathAwareEntity, long l) {
        pathAwareEntity.setAttacking(true);
        pathAwareEntity.getBrain().remember(MemoryModuleType.SPEAR_ENGAGE_TIME, this.getSpearUseTicks(pathAwareEntity));
        pathAwareEntity.getBrain().forget(MemoryModuleType.SPEAR_CHARGE_POSITION);
        pathAwareEntity.setCurrentHand(Hand.MAIN_HAND);
        super.run(serverWorld, pathAwareEntity, l);
    }

    @Override
    protected boolean shouldKeepRunning(ServerWorld serverWorld, PathAwareEntity pathAwareEntity, long l) {
        return pathAwareEntity.getBrain().getOptionalRegisteredMemory(MemoryModuleType.SPEAR_ENGAGE_TIME).orElse(0) > 0 && this.shouldAttack(pathAwareEntity);
    }

    @Override
    protected void keepRunning(ServerWorld serverWorld, PathAwareEntity pathAwareEntity, long l) {
        LivingEntity livingEntity = this.getTarget(pathAwareEntity);
        double d = pathAwareEntity.squaredDistanceTo(livingEntity.getX(), livingEntity.getY(), livingEntity.getZ());
        Entity entity = pathAwareEntity.getRootVehicle();
        float f = 1.0f;
        if (entity instanceof MobEntity) {
            MobEntity mobEntity = (MobEntity)entity;
            f = mobEntity.getRiderChargingSpeedMultiplier();
        }
        int i = pathAwareEntity.hasVehicle() ? 2 : 0;
        pathAwareEntity.getBrain().remember(MemoryModuleType.LOOK_TARGET, new EntityLookTarget(livingEntity, true));
        pathAwareEntity.getBrain().remember(MemoryModuleType.SPEAR_ENGAGE_TIME, pathAwareEntity.getBrain().getOptionalRegisteredMemory(MemoryModuleType.SPEAR_ENGAGE_TIME).orElse(0) - 1);
        Vec3d vec3d = pathAwareEntity.getBrain().getOptionalRegisteredMemory(MemoryModuleType.SPEAR_CHARGE_POSITION).orElse(null);
        if (vec3d != null) {
            pathAwareEntity.getNavigation().startMovingTo(vec3d.x, vec3d.y, vec3d.z, (double)f * this.chargeSpeed);
            if (pathAwareEntity.getNavigation().isIdle()) {
                pathAwareEntity.getBrain().forget(MemoryModuleType.SPEAR_CHARGE_POSITION);
            }
        } else {
            pathAwareEntity.getNavigation().startMovingTo(livingEntity, (double)f * this.chargeStartSpeed);
            if (d < (double)this.squaredChargeRange || pathAwareEntity.getNavigation().isIdle()) {
                double e = Math.sqrt(d);
                Vec3d vec3d2 = FuzzyTargeting.findFrom(pathAwareEntity, (double)(6 + i) - e, (double)(7 + i) - e, 7, livingEntity.getEntityPos());
                pathAwareEntity.getBrain().remember(MemoryModuleType.SPEAR_CHARGE_POSITION, vec3d2);
            }
        }
    }

    @Override
    protected void finishRunning(ServerWorld serverWorld, PathAwareEntity pathAwareEntity, long l) {
        pathAwareEntity.getNavigation().stop();
        pathAwareEntity.clearActiveItem();
        pathAwareEntity.getBrain().forget(MemoryModuleType.SPEAR_CHARGE_POSITION);
        pathAwareEntity.getBrain().forget(MemoryModuleType.SPEAR_ENGAGE_TIME);
        pathAwareEntity.getBrain().remember(MemoryModuleType.SPEAR_STATUS, AdvanceState.RETREAT);
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

    public static final class AdvanceState
    extends Enum<AdvanceState> {
        public static final /* enum */ AdvanceState APPROACH = new AdvanceState();
        public static final /* enum */ AdvanceState CHARGING = new AdvanceState();
        public static final /* enum */ AdvanceState RETREAT = new AdvanceState();
        private static final /* synthetic */ AdvanceState[] field_64632;

        public static AdvanceState[] values() {
            return (AdvanceState[])field_64632.clone();
        }

        public static AdvanceState valueOf(String string) {
            return Enum.valueOf(AdvanceState.class, string);
        }

        private static /* synthetic */ AdvanceState[] method_76713() {
            return new AdvanceState[]{APPROACH, CHARGING, RETREAT};
        }

        static {
            field_64632 = AdvanceState.method_76713();
        }
    }
}


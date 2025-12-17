/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  org.jspecify.annotations.Nullable
 */
package net.minecraft.entity.ai.goal;

import java.util.EnumSet;
import java.util.Optional;
import net.minecraft.component.DataComponentTypes;
import net.minecraft.component.type.KineticWeaponComponent;
import net.minecraft.entity.Entity;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.ai.FuzzyTargeting;
import net.minecraft.entity.ai.goal.Goal;
import net.minecraft.entity.mob.HostileEntity;
import net.minecraft.entity.mob.MobEntity;
import net.minecraft.util.Hand;
import net.minecraft.util.math.Vec3d;
import org.jspecify.annotations.Nullable;

public class ChargeKineticWeaponGoal<T extends HostileEntity>
extends Goal {
    static final int field_64637 = 6;
    static final int field_64638 = 7;
    static final int field_64639 = 9;
    static final int field_64640 = 11;
    static final double CHARGING_TIME_TICKS = ChargeKineticWeaponGoal.toGoalTicks(100);
    private final T entity;
    private @Nullable Data data;
    double speed;
    double targetFollowingSpeed;
    float maxSquaredDistanceToTarget;
    float minSquaredDistanceToTarget;

    public ChargeKineticWeaponGoal(T entity, double speed, double targetFollowingSpeed, float maxDistanceToTarget, float minDistanceToTarget) {
        this.entity = entity;
        this.speed = speed;
        this.targetFollowingSpeed = targetFollowingSpeed;
        this.maxSquaredDistanceToTarget = maxDistanceToTarget * maxDistanceToTarget;
        this.minSquaredDistanceToTarget = minDistanceToTarget * minDistanceToTarget;
        this.setControls(EnumSet.of(Goal.Control.MOVE, Goal.Control.LOOK));
    }

    @Override
    public boolean canStart() {
        return this.canAttack() && !((LivingEntity)this.entity).isUsingItem();
    }

    private boolean canAttack() {
        return ((MobEntity)this.entity).getTarget() != null && ((LivingEntity)this.entity).getMainHandStack().contains(DataComponentTypes.KINETIC_WEAPON);
    }

    private int getUseGoalTicks() {
        int i = Optional.ofNullable(((LivingEntity)this.entity).getMainHandStack().get(DataComponentTypes.KINETIC_WEAPON)).map(KineticWeaponComponent::getUseTicks).orElse(0);
        return ChargeKineticWeaponGoal.toGoalTicks(i);
    }

    @Override
    public boolean shouldContinue() {
        return this.data != null && !this.data.charged && this.canAttack();
    }

    @Override
    public void start() {
        super.start();
        ((MobEntity)this.entity).setAttacking(true);
        this.data = new Data();
    }

    @Override
    public void stop() {
        super.stop();
        ((MobEntity)this.entity).getNavigation().stop();
        ((MobEntity)this.entity).setAttacking(false);
        this.data = null;
        ((LivingEntity)this.entity).clearActiveItem();
    }

    @Override
    public void tick() {
        double e;
        if (this.data == null) {
            return;
        }
        LivingEntity livingEntity = ((MobEntity)this.entity).getTarget();
        double d = ((Entity)this.entity).squaredDistanceTo(livingEntity.getX(), livingEntity.getY(), livingEntity.getZ());
        Entity entity = ((Entity)this.entity).getRootVehicle();
        float f = 1.0f;
        if (entity instanceof MobEntity) {
            MobEntity mobEntity = (MobEntity)entity;
            f = mobEntity.getRiderChargingSpeedMultiplier();
        }
        int i = ((Entity)this.entity).hasVehicle() ? 2 : 0;
        ((MobEntity)this.entity).lookAtEntity(livingEntity, 30.0f, 30.0f);
        ((MobEntity)this.entity).getLookControl().lookAt(livingEntity, 30.0f, 30.0f);
        if (this.data.isIdle()) {
            if (d > (double)this.maxSquaredDistanceToTarget) {
                ((MobEntity)this.entity).getNavigation().startMovingTo(livingEntity, (double)f * this.targetFollowingSpeed);
                return;
            }
            this.data.setRemainingUseTicks(this.getUseGoalTicks());
            ((LivingEntity)this.entity).setCurrentHand(Hand.MAIN_HAND);
        }
        if (this.data.canStartCharging()) {
            ((LivingEntity)this.entity).clearActiveItem();
            e = Math.sqrt(d);
            this.data.startPos = FuzzyTargeting.findFrom(this.entity, Math.max(0.0, (double)(9 + i) - e), Math.max(1.0, (double)(11 + i) - e), 7, livingEntity.getEntityPos());
            this.data.chargeTicks = 1;
        }
        if (this.data.finishedCharging()) {
            return;
        }
        if (this.data.startPos != null) {
            ((MobEntity)this.entity).getNavigation().startMovingTo(this.data.startPos.x, this.data.startPos.y, this.data.startPos.z, (double)f * this.targetFollowingSpeed);
            if (((MobEntity)this.entity).getNavigation().isIdle()) {
                if (this.data.chargeTicks > 0) {
                    this.data.charged = true;
                    return;
                }
                this.data.startPos = null;
            }
        } else {
            ((MobEntity)this.entity).getNavigation().startMovingTo(livingEntity, (double)f * this.speed);
            if (d < (double)this.minSquaredDistanceToTarget || ((MobEntity)this.entity).getNavigation().isIdle()) {
                e = Math.sqrt(d);
                this.data.startPos = FuzzyTargeting.findFrom(this.entity, (double)(6 + i) - e, (double)(7 + i) - e, 7, livingEntity.getEntityPos());
            }
        }
    }

    public static class Data {
        private int remainingUseTicks = -1;
        int chargeTicks = -1;
        @Nullable Vec3d startPos;
        boolean charged = false;

        public boolean isIdle() {
            return this.remainingUseTicks < 0;
        }

        public void setRemainingUseTicks(int remainingUseTicks) {
            this.remainingUseTicks = remainingUseTicks;
        }

        public boolean canStartCharging() {
            if (this.remainingUseTicks > 0) {
                --this.remainingUseTicks;
                if (this.remainingUseTicks == 0) {
                    return true;
                }
            }
            return false;
        }

        public boolean finishedCharging() {
            if (this.chargeTicks > 0) {
                ++this.chargeTicks;
                if ((double)this.chargeTicks > CHARGING_TIME_TICKS) {
                    this.charged = true;
                    return true;
                }
            }
            return false;
        }
    }
}


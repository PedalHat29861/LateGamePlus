/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  org.jspecify.annotations.Nullable
 */
package net.minecraft.entity.ai;

import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.mob.MobEntity;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.world.Difficulty;
import org.jspecify.annotations.Nullable;

public class TargetPredicate {
    public static final TargetPredicate DEFAULT = TargetPredicate.createAttackable();
    private static final double MIN_DISTANCE = 2.0;
    private final boolean attackable;
    private double baseMaxDistance = -1.0;
    private boolean respectsVisibility = true;
    private boolean useDistanceScalingFactor = true;
    private @Nullable EntityPredicate predicate;

    private TargetPredicate(boolean attackable) {
        this.attackable = attackable;
    }

    public static TargetPredicate createAttackable() {
        return new TargetPredicate(true);
    }

    public static TargetPredicate createNonAttackable() {
        return new TargetPredicate(false);
    }

    public TargetPredicate copy() {
        TargetPredicate targetPredicate = this.attackable ? TargetPredicate.createAttackable() : TargetPredicate.createNonAttackable();
        targetPredicate.baseMaxDistance = this.baseMaxDistance;
        targetPredicate.respectsVisibility = this.respectsVisibility;
        targetPredicate.useDistanceScalingFactor = this.useDistanceScalingFactor;
        targetPredicate.predicate = this.predicate;
        return targetPredicate;
    }

    public TargetPredicate setBaseMaxDistance(double baseMaxDistance) {
        this.baseMaxDistance = baseMaxDistance;
        return this;
    }

    public TargetPredicate ignoreVisibility() {
        this.respectsVisibility = false;
        return this;
    }

    public TargetPredicate ignoreDistanceScalingFactor() {
        this.useDistanceScalingFactor = false;
        return this;
    }

    public TargetPredicate setPredicate(@Nullable EntityPredicate predicate) {
        this.predicate = predicate;
        return this;
    }

    public boolean test(ServerWorld world, @Nullable LivingEntity tester, LivingEntity target) {
        if (tester == target) {
            return false;
        }
        if (!target.isPartOfGame()) {
            return false;
        }
        if (this.predicate != null && !this.predicate.test(target, world)) {
            return false;
        }
        if (tester == null) {
            if (this.attackable && (!target.canTakeDamage() || world.getDifficulty() == Difficulty.PEACEFUL)) {
                return false;
            }
        } else {
            MobEntity mobEntity;
            if (this.attackable && (!tester.canTarget(target) || !tester.canTarget(target.getType()) || tester.isTeammate(target))) {
                return false;
            }
            if (this.baseMaxDistance > 0.0) {
                double d = this.useDistanceScalingFactor ? target.getAttackDistanceScalingFactor(tester) : 1.0;
                double e = Math.max(this.baseMaxDistance * d, 2.0);
                double f = tester.squaredDistanceTo(target.getX(), target.getY(), target.getZ());
                if (f > e * e) {
                    return false;
                }
            }
            if (this.respectsVisibility && tester instanceof MobEntity && !(mobEntity = (MobEntity)tester).getVisibilityCache().canSee(target)) {
                return false;
            }
        }
        return true;
    }

    @FunctionalInterface
    public static interface EntityPredicate {
        public boolean test(LivingEntity var1, ServerWorld var2);
    }
}


/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  org.jspecify.annotations.Nullable
 */
package net.minecraft.entity.ai.goal;

import net.minecraft.entity.ai.FuzzyTargeting;
import net.minecraft.entity.ai.goal.WanderAroundGoal;
import net.minecraft.entity.mob.PathAwareEntity;
import net.minecraft.util.math.Vec3d;
import org.jspecify.annotations.Nullable;

public class WanderAroundFarGoal
extends WanderAroundGoal {
    public static final float CHANCE = 0.001f;
    protected final float probability;

    public WanderAroundFarGoal(PathAwareEntity pathAwareEntity, double d) {
        this(pathAwareEntity, d, 0.001f);
    }

    public WanderAroundFarGoal(PathAwareEntity mob, double speed, float probability) {
        super(mob, speed);
        this.probability = probability;
    }

    @Override
    protected @Nullable Vec3d getWanderTarget() {
        if (this.mob.isTouchingWater()) {
            Vec3d vec3d = FuzzyTargeting.find(this.mob, 15, 7);
            return vec3d == null ? super.getWanderTarget() : vec3d;
        }
        if (this.mob.getRandom().nextFloat() >= this.probability) {
            return FuzzyTargeting.find(this.mob, 10, 7);
        }
        return super.getWanderTarget();
    }
}


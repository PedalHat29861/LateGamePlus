/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  org.jspecify.annotations.Nullable
 */
package net.minecraft.entity.ai.goal;

import net.minecraft.entity.ai.AboveGroundTargeting;
import net.minecraft.entity.ai.NoPenaltySolidTargeting;
import net.minecraft.entity.ai.goal.WanderAroundFarGoal;
import net.minecraft.entity.mob.PathAwareEntity;
import net.minecraft.util.math.Vec3d;
import org.jspecify.annotations.Nullable;

public class FlyGoal
extends WanderAroundFarGoal {
    public FlyGoal(PathAwareEntity pathAwareEntity, double d) {
        super(pathAwareEntity, d);
    }

    @Override
    protected @Nullable Vec3d getWanderTarget() {
        Vec3d vec3d = this.mob.getRotationVec(0.0f);
        int i = 8;
        Vec3d vec3d2 = AboveGroundTargeting.find(this.mob, 8, 7, vec3d.x, vec3d.z, 1.5707964f, 3, 1);
        if (vec3d2 != null) {
            return vec3d2;
        }
        return NoPenaltySolidTargeting.find(this.mob, 8, 4, -2, vec3d.x, vec3d.z, 1.5707963705062866);
    }
}


/*
 * Decompiled with CFR 0.152.
 */
package net.minecraft.entity.ai.goal;

import java.util.EnumSet;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.ai.goal.Goal;
import net.minecraft.entity.passive.TameableEntity;

public class SitGoal
extends Goal {
    private final TameableEntity tameable;

    public SitGoal(TameableEntity tameable) {
        this.tameable = tameable;
        this.setControls(EnumSet.of(Goal.Control.JUMP, Goal.Control.MOVE));
    }

    @Override
    public boolean shouldContinue() {
        return this.tameable.isSitting();
    }

    @Override
    public boolean canStart() {
        boolean bl = this.tameable.isSitting();
        if (!bl && !this.tameable.isTamed()) {
            return false;
        }
        if (this.tameable.isTouchingWater()) {
            return false;
        }
        if (!this.tameable.isOnGround()) {
            return false;
        }
        LivingEntity livingEntity = this.tameable.getOwner();
        if (livingEntity == null || livingEntity.getEntityWorld() != this.tameable.getEntityWorld()) {
            return true;
        }
        if (this.tameable.squaredDistanceTo(livingEntity) < 144.0 && livingEntity.getAttacker() != null) {
            return false;
        }
        return bl;
    }

    @Override
    public void start() {
        this.tameable.getNavigation().stop();
        this.tameable.setInSittingPose(true);
    }

    @Override
    public void stop() {
        this.tameable.setInSittingPose(false);
    }
}


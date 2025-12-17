/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  org.jspecify.annotations.Nullable
 */
package net.minecraft.entity.ai.goal;

import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.ai.goal.ActiveTargetGoal;
import net.minecraft.entity.passive.TameableEntity;
import org.jspecify.annotations.Nullable;

public class UntamedActiveTargetGoal<T extends LivingEntity>
extends ActiveTargetGoal<T> {
    private final TameableEntity tameable;

    public UntamedActiveTargetGoal(TameableEntity tameable, Class<T> targetClass, boolean checkVisibility,  @Nullable TargetPredicate.EntityPredicate targetPredicate) {
        super(tameable, targetClass, 10, checkVisibility, false, targetPredicate);
        this.tameable = tameable;
    }

    @Override
    public boolean canStart() {
        return !this.tameable.isTamed() && super.canStart();
    }

    @Override
    public boolean shouldContinue() {
        if (this.targetPredicate != null) {
            return this.targetPredicate.test(UntamedActiveTargetGoal.getServerWorld(this.mob), this.mob, this.targetEntity);
        }
        return super.shouldContinue();
    }
}


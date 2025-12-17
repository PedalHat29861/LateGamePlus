/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  org.jspecify.annotations.Nullable
 */
package net.minecraft.entity.ai.goal;

import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.ai.goal.ActiveTargetGoal;
import net.minecraft.entity.raid.RaiderEntity;
import org.jspecify.annotations.Nullable;

public class RaidGoal<T extends LivingEntity>
extends ActiveTargetGoal<T> {
    private static final int MAX_COOLDOWN = 200;
    private int cooldown = 0;

    public RaidGoal(RaiderEntity raider, Class<T> targetEntityClass, boolean checkVisibility,  @Nullable TargetPredicate.EntityPredicate targetPredicate) {
        super(raider, targetEntityClass, 500, checkVisibility, false, targetPredicate);
    }

    public int getCooldown() {
        return this.cooldown;
    }

    public void decreaseCooldown() {
        --this.cooldown;
    }

    @Override
    public boolean canStart() {
        if (this.cooldown > 0 || !this.mob.getRandom().nextBoolean()) {
            return false;
        }
        if (!((RaiderEntity)this.mob).hasActiveRaid()) {
            return false;
        }
        this.findClosestTarget();
        return this.targetEntity != null;
    }

    @Override
    public void start() {
        this.cooldown = RaidGoal.toGoalTicks(200);
        super.start();
    }
}


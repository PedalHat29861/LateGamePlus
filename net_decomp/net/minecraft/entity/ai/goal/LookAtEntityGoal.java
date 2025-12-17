/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  org.jspecify.annotations.Nullable
 */
package net.minecraft.entity.ai.goal;

import java.util.EnumSet;
import java.util.function.Predicate;
import net.minecraft.entity.Entity;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.ai.TargetPredicate;
import net.minecraft.entity.ai.goal.Goal;
import net.minecraft.entity.mob.MobEntity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.predicate.entity.EntityPredicates;
import net.minecraft.server.world.ServerWorld;
import org.jspecify.annotations.Nullable;

public class LookAtEntityGoal
extends Goal {
    public static final float DEFAULT_CHANCE = 0.02f;
    protected final MobEntity mob;
    protected @Nullable Entity target;
    protected final float range;
    private int lookTime;
    protected final float chance;
    private final boolean lookForward;
    protected final Class<? extends LivingEntity> targetType;
    protected final TargetPredicate targetPredicate;

    public LookAtEntityGoal(MobEntity mob, Class<? extends LivingEntity> targetType, float range) {
        this(mob, targetType, range, 0.02f);
    }

    public LookAtEntityGoal(MobEntity mob, Class<? extends LivingEntity> targetType, float range, float chance) {
        this(mob, targetType, range, chance, false);
    }

    public LookAtEntityGoal(MobEntity mob, Class<? extends LivingEntity> targetType, float range, float chance, boolean lookForward) {
        this.mob = mob;
        this.targetType = targetType;
        this.range = range;
        this.chance = chance;
        this.lookForward = lookForward;
        this.setControls(EnumSet.of(Goal.Control.LOOK));
        if (targetType == PlayerEntity.class) {
            Predicate<Entity> predicate = EntityPredicates.rides(mob);
            this.targetPredicate = TargetPredicate.createNonAttackable().setBaseMaxDistance(range).setPredicate((entity, world) -> predicate.test(entity));
        } else {
            this.targetPredicate = TargetPredicate.createNonAttackable().setBaseMaxDistance(range);
        }
    }

    @Override
    public boolean canStart() {
        if (this.mob.getRandom().nextFloat() >= this.chance) {
            return false;
        }
        if (this.mob.getTarget() != null) {
            this.target = this.mob.getTarget();
        }
        ServerWorld serverWorld = LookAtEntityGoal.getServerWorld(this.mob);
        this.target = this.targetType == PlayerEntity.class ? serverWorld.getClosestPlayer(this.targetPredicate, this.mob, this.mob.getX(), this.mob.getEyeY(), this.mob.getZ()) : serverWorld.getClosestEntity(this.mob.getEntityWorld().getEntitiesByClass(this.targetType, this.mob.getBoundingBox().expand(this.range, 3.0, this.range), livingEntity -> true), this.targetPredicate, this.mob, this.mob.getX(), this.mob.getEyeY(), this.mob.getZ());
        return this.target != null;
    }

    @Override
    public boolean shouldContinue() {
        if (!this.target.isAlive()) {
            return false;
        }
        if (this.mob.squaredDistanceTo(this.target) > (double)(this.range * this.range)) {
            return false;
        }
        return this.lookTime > 0;
    }

    @Override
    public void start() {
        this.lookTime = this.getTickCount(40 + this.mob.getRandom().nextInt(40));
    }

    @Override
    public void stop() {
        this.target = null;
    }

    @Override
    public void tick() {
        if (!this.target.isAlive()) {
            return;
        }
        double d = this.lookForward ? this.mob.getEyeY() : this.target.getEyeY();
        this.mob.getLookControl().lookAt(this.target.getX(), d, this.target.getZ());
        --this.lookTime;
    }
}


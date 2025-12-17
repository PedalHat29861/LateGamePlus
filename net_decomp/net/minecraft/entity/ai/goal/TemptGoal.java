/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  org.jspecify.annotations.Nullable
 */
package net.minecraft.entity.ai.goal;

import java.util.EnumSet;
import java.util.function.Predicate;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.ai.TargetPredicate;
import net.minecraft.entity.ai.goal.Goal;
import net.minecraft.entity.attribute.EntityAttributes;
import net.minecraft.entity.mob.MobEntity;
import net.minecraft.entity.mob.PathAwareEntity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.ItemStack;
import net.minecraft.util.math.Vec3d;
import org.jspecify.annotations.Nullable;

public class TemptGoal
extends Goal {
    private static final TargetPredicate TEMPTING_ENTITY_PREDICATE = TargetPredicate.createNonAttackable().ignoreVisibility();
    private static final double DEFAULT_RANGE = 2.5;
    private final TargetPredicate predicate;
    protected final MobEntity mob;
    protected final double speed;
    private double lastPlayerX;
    private double lastPlayerY;
    private double lastPlayerZ;
    private double lastPlayerPitch;
    private double lastPlayerYaw;
    protected @Nullable PlayerEntity closestPlayer;
    private int cooldown;
    private boolean active;
    private final Predicate<ItemStack> temptItemPredicate;
    private final boolean canBeScared;
    private final double range;

    public TemptGoal(PathAwareEntity entity, double speed, Predicate<ItemStack> temptItemPredicate, boolean canBeScared) {
        this((MobEntity)entity, speed, temptItemPredicate, canBeScared, 2.5);
    }

    public TemptGoal(PathAwareEntity entity, double speed, Predicate<ItemStack> temptItemPredicate, boolean canBeScared, double range) {
        this((MobEntity)entity, speed, temptItemPredicate, canBeScared, range);
    }

    TemptGoal(MobEntity mob, double speed, Predicate<ItemStack> temptItemPredicate, boolean canBeScared, double range) {
        this.mob = mob;
        this.speed = speed;
        this.temptItemPredicate = temptItemPredicate;
        this.canBeScared = canBeScared;
        this.range = range;
        this.setControls(EnumSet.of(Goal.Control.MOVE, Goal.Control.LOOK));
        this.predicate = TEMPTING_ENTITY_PREDICATE.copy().setPredicate((target, world) -> this.isTemptedBy(target));
    }

    @Override
    public boolean canStart() {
        if (this.cooldown > 0) {
            --this.cooldown;
            return false;
        }
        this.closestPlayer = TemptGoal.getServerWorld(this.mob).getClosestPlayer(this.predicate.setBaseMaxDistance(this.mob.getAttributeValue(EntityAttributes.TEMPT_RANGE)), this.mob);
        return this.closestPlayer != null;
    }

    private boolean isTemptedBy(LivingEntity entity) {
        return this.temptItemPredicate.test(entity.getMainHandStack()) || this.temptItemPredicate.test(entity.getOffHandStack());
    }

    @Override
    public boolean shouldContinue() {
        if (this.canBeScared()) {
            if (this.mob.squaredDistanceTo(this.closestPlayer) < 36.0) {
                if (this.closestPlayer.squaredDistanceTo(this.lastPlayerX, this.lastPlayerY, this.lastPlayerZ) > 0.010000000000000002) {
                    return false;
                }
                if (Math.abs((double)this.closestPlayer.getPitch() - this.lastPlayerPitch) > 5.0 || Math.abs((double)this.closestPlayer.getYaw() - this.lastPlayerYaw) > 5.0) {
                    return false;
                }
            } else {
                this.lastPlayerX = this.closestPlayer.getX();
                this.lastPlayerY = this.closestPlayer.getY();
                this.lastPlayerZ = this.closestPlayer.getZ();
            }
            this.lastPlayerPitch = this.closestPlayer.getPitch();
            this.lastPlayerYaw = this.closestPlayer.getYaw();
        }
        return this.canStart();
    }

    protected boolean canBeScared() {
        return this.canBeScared;
    }

    @Override
    public void start() {
        this.lastPlayerX = this.closestPlayer.getX();
        this.lastPlayerY = this.closestPlayer.getY();
        this.lastPlayerZ = this.closestPlayer.getZ();
        this.active = true;
    }

    @Override
    public void stop() {
        this.closestPlayer = null;
        this.stopMoving();
        this.cooldown = TemptGoal.toGoalTicks(100);
        this.active = false;
    }

    @Override
    public void tick() {
        this.mob.getLookControl().lookAt(this.closestPlayer, this.mob.getMaxHeadRotation() + 20, this.mob.getMaxLookPitchChange());
        if (this.mob.squaredDistanceTo(this.closestPlayer) < this.range * this.range) {
            this.stopMoving();
        } else {
            this.startMovingTo(this.closestPlayer);
        }
    }

    protected void stopMoving() {
        this.mob.getNavigation().stop();
    }

    protected void startMovingTo(PlayerEntity player) {
        this.mob.getNavigation().startMovingTo(player, this.speed);
    }

    public boolean isActive() {
        return this.active;
    }

    public static class HappyGhastTemptGoal
    extends TemptGoal {
        public HappyGhastTemptGoal(MobEntity mobEntity, double d, Predicate<ItemStack> predicate, boolean bl, double e) {
            super(mobEntity, d, predicate, bl, e);
        }

        @Override
        protected void stopMoving() {
            this.mob.getMoveControl().setWaiting();
        }

        @Override
        protected void startMovingTo(PlayerEntity player) {
            Vec3d vec3d = player.getEyePos().subtract(this.mob.getEntityPos()).multiply(this.mob.getRandom().nextDouble()).add(this.mob.getEntityPos());
            this.mob.getMoveControl().moveTo(vec3d.x, vec3d.y, vec3d.z, this.speed);
        }
    }
}


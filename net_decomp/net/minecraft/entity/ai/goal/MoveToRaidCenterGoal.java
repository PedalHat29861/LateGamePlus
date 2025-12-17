/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  com.google.common.collect.Sets
 */
package net.minecraft.entity.ai.goal;

import com.google.common.collect.Sets;
import java.util.EnumSet;
import java.util.HashSet;
import java.util.List;
import net.minecraft.entity.Entity;
import net.minecraft.entity.ai.NoPenaltyTargeting;
import net.minecraft.entity.ai.goal.Goal;
import net.minecraft.entity.mob.MobEntity;
import net.minecraft.entity.mob.PathAwareEntity;
import net.minecraft.entity.raid.RaiderEntity;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.util.math.Vec3d;
import net.minecraft.village.raid.Raid;
import net.minecraft.village.raid.RaidManager;

public class MoveToRaidCenterGoal<T extends RaiderEntity>
extends Goal {
    private static final int FREE_RAIDER_CHECK_INTERVAL = 20;
    private static final float WALK_SPEED = 1.0f;
    private final T actor;
    private int nextFreeRaiderCheckAge;

    public MoveToRaidCenterGoal(T actor) {
        this.actor = actor;
        this.setControls(EnumSet.of(Goal.Control.MOVE));
    }

    @Override
    public boolean canStart() {
        return ((MobEntity)this.actor).getTarget() == null && !((Entity)this.actor).hasControllingPassenger() && ((RaiderEntity)this.actor).hasActiveRaid() && !((RaiderEntity)this.actor).getRaid().isFinished() && !MoveToRaidCenterGoal.castToServerWorld(((Entity)this.actor).getEntityWorld()).isNearOccupiedPointOfInterest(((Entity)this.actor).getBlockPos());
    }

    @Override
    public boolean shouldContinue() {
        return ((RaiderEntity)this.actor).hasActiveRaid() && !((RaiderEntity)this.actor).getRaid().isFinished() && !MoveToRaidCenterGoal.castToServerWorld(((Entity)this.actor).getEntityWorld()).isNearOccupiedPointOfInterest(((Entity)this.actor).getBlockPos());
    }

    @Override
    public void tick() {
        if (((RaiderEntity)this.actor).hasActiveRaid()) {
            Vec3d vec3d;
            Raid raid = ((RaiderEntity)this.actor).getRaid();
            if (((RaiderEntity)this.actor).age > this.nextFreeRaiderCheckAge) {
                this.nextFreeRaiderCheckAge = ((RaiderEntity)this.actor).age + 20;
                this.includeFreeRaiders(raid);
            }
            if (!((PathAwareEntity)this.actor).isNavigating() && (vec3d = NoPenaltyTargeting.findTo(this.actor, 15, 4, Vec3d.ofBottomCenter(raid.getCenter()), 1.5707963705062866)) != null) {
                ((MobEntity)this.actor).getNavigation().startMovingTo(vec3d.x, vec3d.y, vec3d.z, 1.0);
            }
        }
    }

    private void includeFreeRaiders(Raid raid) {
        if (raid.isActive()) {
            ServerWorld serverWorld = MoveToRaidCenterGoal.castToServerWorld(((Entity)this.actor).getEntityWorld());
            HashSet set = Sets.newHashSet();
            List<RaiderEntity> list = serverWorld.getEntitiesByClass(RaiderEntity.class, ((Entity)this.actor).getBoundingBox().expand(16.0), raiderEntity -> !raiderEntity.hasActiveRaid() && RaidManager.isValidRaiderFor(raiderEntity));
            set.addAll(list);
            for (RaiderEntity raiderEntity2 : set) {
                raid.addRaider(serverWorld, raid.getGroupsSpawned(), raiderEntity2, null, true);
            }
        }
    }
}


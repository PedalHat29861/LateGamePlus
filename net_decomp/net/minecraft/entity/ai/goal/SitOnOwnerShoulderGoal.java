/*
 * Decompiled with CFR 0.152.
 */
package net.minecraft.entity.ai.goal;

import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.ai.goal.Goal;
import net.minecraft.entity.passive.TameableShoulderEntity;
import net.minecraft.server.network.ServerPlayerEntity;

public class SitOnOwnerShoulderGoal
extends Goal {
    private final TameableShoulderEntity tameable;
    private boolean mounted;

    public SitOnOwnerShoulderGoal(TameableShoulderEntity tameable) {
        this.tameable = tameable;
    }

    @Override
    public boolean canStart() {
        LivingEntity livingEntity = this.tameable.getOwner();
        if (livingEntity instanceof ServerPlayerEntity) {
            ServerPlayerEntity serverPlayerEntity = (ServerPlayerEntity)livingEntity;
            boolean bl = !serverPlayerEntity.isSpectator() && !serverPlayerEntity.getAbilities().flying && !serverPlayerEntity.isTouchingWater() && !serverPlayerEntity.inPowderSnow;
            return !this.tameable.isSitting() && bl && this.tameable.isReadyToSitOnPlayer();
        }
        return false;
    }

    @Override
    public boolean canStop() {
        return !this.mounted;
    }

    @Override
    public void start() {
        this.mounted = false;
    }

    @Override
    public void tick() {
        if (this.mounted || this.tameable.isInSittingPose() || this.tameable.isLeashed()) {
            return;
        }
        LivingEntity livingEntity = this.tameable.getOwner();
        if (livingEntity instanceof ServerPlayerEntity) {
            ServerPlayerEntity serverPlayerEntity = (ServerPlayerEntity)livingEntity;
            if (this.tameable.getBoundingBox().intersects(serverPlayerEntity.getBoundingBox())) {
                this.mounted = this.tameable.mountOnto(serverPlayerEntity);
            }
        }
    }
}


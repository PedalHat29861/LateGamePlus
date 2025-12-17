/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  com.google.common.annotations.VisibleForTesting
 *  com.google.common.collect.ImmutableMap
 */
package net.minecraft.entity.ai.brain.task;

import com.google.common.annotations.VisibleForTesting;
import com.google.common.collect.ImmutableMap;
import java.util.Map;
import net.minecraft.command.argument.EntityAnchorArgumentType;
import net.minecraft.entity.EntityPose;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.ai.brain.Brain;
import net.minecraft.entity.ai.brain.MemoryModuleState;
import net.minecraft.entity.ai.brain.MemoryModuleType;
import net.minecraft.entity.ai.brain.task.MultiTickTask;
import net.minecraft.entity.mob.BreezeEntity;
import net.minecraft.entity.projectile.BreezeWindChargeEntity;
import net.minecraft.entity.projectile.ProjectileEntity;
import net.minecraft.item.ItemStack;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.sound.SoundEvents;
import net.minecraft.util.Unit;
import net.minecraft.world.World;

public class BreezeShootTask
extends MultiTickTask<BreezeEntity> {
    private static final int MAX_SQUARED_RANGE = 256;
    private static final int BASE_PROJECTILE_DIVERGENCY = 5;
    private static final int PROJECTILE_DIVERGENCY_DIFFICULTY_MODIFIER = 4;
    private static final float PROJECTILE_SPEED = 0.7f;
    private static final int SHOOT_CHARGING_EXPIRY = Math.round(15.0f);
    private static final int RECOVER_EXPIRY = Math.round(4.0f);
    private static final int SHOOT_COOLDOWN_EXPIRY = Math.round(10.0f);

    @VisibleForTesting
    public BreezeShootTask() {
        super((Map<MemoryModuleType<?>, MemoryModuleState>)ImmutableMap.of(MemoryModuleType.ATTACK_TARGET, (Object)((Object)MemoryModuleState.VALUE_PRESENT), MemoryModuleType.BREEZE_SHOOT_COOLDOWN, (Object)((Object)MemoryModuleState.VALUE_ABSENT), MemoryModuleType.BREEZE_SHOOT_CHARGING, (Object)((Object)MemoryModuleState.VALUE_ABSENT), MemoryModuleType.BREEZE_SHOOT_RECOVER, (Object)((Object)MemoryModuleState.VALUE_ABSENT), MemoryModuleType.BREEZE_SHOOT, (Object)((Object)MemoryModuleState.VALUE_PRESENT), MemoryModuleType.WALK_TARGET, (Object)((Object)MemoryModuleState.VALUE_ABSENT), MemoryModuleType.BREEZE_JUMP_TARGET, (Object)((Object)MemoryModuleState.VALUE_ABSENT)), SHOOT_CHARGING_EXPIRY + 1 + RECOVER_EXPIRY);
    }

    @Override
    protected boolean shouldRun(ServerWorld serverWorld, BreezeEntity breezeEntity) {
        if (breezeEntity.getPose() != EntityPose.STANDING) {
            return false;
        }
        return breezeEntity.getBrain().getOptionalRegisteredMemory(MemoryModuleType.ATTACK_TARGET).map(target -> BreezeShootTask.isTargetWithinRange(breezeEntity, target)).map(withinRange -> {
            if (!withinRange.booleanValue()) {
                breezeEntity.getBrain().forget(MemoryModuleType.BREEZE_SHOOT);
            }
            return withinRange;
        }).orElse(false);
    }

    @Override
    protected boolean shouldKeepRunning(ServerWorld serverWorld, BreezeEntity breezeEntity, long l) {
        return breezeEntity.getBrain().hasMemoryModule(MemoryModuleType.ATTACK_TARGET) && breezeEntity.getBrain().hasMemoryModule(MemoryModuleType.BREEZE_SHOOT);
    }

    @Override
    protected void run(ServerWorld serverWorld, BreezeEntity breezeEntity, long l) {
        breezeEntity.getBrain().getOptionalRegisteredMemory(MemoryModuleType.ATTACK_TARGET).ifPresent(target -> breezeEntity.setPose(EntityPose.SHOOTING));
        breezeEntity.getBrain().remember(MemoryModuleType.BREEZE_SHOOT_CHARGING, Unit.INSTANCE, SHOOT_CHARGING_EXPIRY);
        breezeEntity.playSound(SoundEvents.ENTITY_BREEZE_INHALE, 1.0f, 1.0f);
    }

    @Override
    protected void finishRunning(ServerWorld serverWorld, BreezeEntity breezeEntity, long l) {
        if (breezeEntity.getPose() == EntityPose.SHOOTING) {
            breezeEntity.setPose(EntityPose.STANDING);
        }
        breezeEntity.getBrain().remember(MemoryModuleType.BREEZE_SHOOT_COOLDOWN, Unit.INSTANCE, SHOOT_COOLDOWN_EXPIRY);
        breezeEntity.getBrain().forget(MemoryModuleType.BREEZE_SHOOT);
    }

    @Override
    protected void keepRunning(ServerWorld serverWorld, BreezeEntity breezeEntity, long l) {
        Brain<BreezeEntity> brain = breezeEntity.getBrain();
        LivingEntity livingEntity = brain.getOptionalRegisteredMemory(MemoryModuleType.ATTACK_TARGET).orElse(null);
        if (livingEntity == null) {
            return;
        }
        breezeEntity.lookAt(EntityAnchorArgumentType.EntityAnchor.EYES, livingEntity.getEntityPos());
        if (brain.getOptionalRegisteredMemory(MemoryModuleType.BREEZE_SHOOT_CHARGING).isPresent() || brain.getOptionalRegisteredMemory(MemoryModuleType.BREEZE_SHOOT_RECOVER).isPresent()) {
            return;
        }
        brain.remember(MemoryModuleType.BREEZE_SHOOT_RECOVER, Unit.INSTANCE, RECOVER_EXPIRY);
        double d = livingEntity.getX() - breezeEntity.getX();
        double e = livingEntity.getBodyY(livingEntity.hasVehicle() ? 0.8 : 0.3) - breezeEntity.getChargeY();
        double f = livingEntity.getZ() - breezeEntity.getZ();
        ProjectileEntity.spawnWithVelocity(new BreezeWindChargeEntity(breezeEntity, (World)serverWorld), serverWorld, ItemStack.EMPTY, d, e, f, 0.7f, 5 - serverWorld.getDifficulty().getId() * 4);
        breezeEntity.playSound(SoundEvents.ENTITY_BREEZE_SHOOT, 1.5f, 1.0f);
    }

    private static boolean isTargetWithinRange(BreezeEntity breeze, LivingEntity target) {
        double d = breeze.getEntityPos().squaredDistanceTo(target.getEntityPos());
        return d < 256.0;
    }

    @Override
    protected /* synthetic */ void finishRunning(ServerWorld world, LivingEntity entity, long time) {
        this.finishRunning(world, (BreezeEntity)entity, time);
    }

    @Override
    protected /* synthetic */ void keepRunning(ServerWorld world, LivingEntity entity, long time) {
        this.keepRunning(world, (BreezeEntity)entity, time);
    }

    @Override
    protected /* synthetic */ void run(ServerWorld world, LivingEntity entity, long time) {
        this.run(world, (BreezeEntity)entity, time);
    }
}


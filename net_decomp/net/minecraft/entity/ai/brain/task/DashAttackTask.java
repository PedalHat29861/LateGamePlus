/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  com.google.common.collect.ImmutableMap
 */
package net.minecraft.entity.ai.brain.task;

import com.google.common.collect.ImmutableMap;
import java.util.ArrayList;
import java.util.Map;
import java.util.Optional;
import net.minecraft.enchantment.EnchantmentHelper;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.ai.TargetPredicate;
import net.minecraft.entity.ai.brain.Brain;
import net.minecraft.entity.ai.brain.MemoryModuleState;
import net.minecraft.entity.ai.brain.MemoryModuleType;
import net.minecraft.entity.ai.brain.task.MultiTickTask;
import net.minecraft.entity.attribute.EntityAttributes;
import net.minecraft.entity.damage.DamageSource;
import net.minecraft.entity.effect.StatusEffects;
import net.minecraft.entity.passive.AnimalEntity;
import net.minecraft.entity.passive.TameableEntity;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.sound.SoundEvent;
import net.minecraft.util.TypeFilter;
import net.minecraft.util.math.MathHelper;
import net.minecraft.util.math.Vec3d;

public class DashAttackTask
extends MultiTickTask<AnimalEntity> {
    private final int cooldownTicks;
    private final TargetPredicate predicate;
    private final float speed;
    private final float knockbackStrength;
    private final double maxDistance;
    private final double maxEntitySpeed;
    private final SoundEvent sound;
    private Vec3d velocity;
    private Vec3d lastPos;

    public DashAttackTask(int cooldownTicks, TargetPredicate predicate, float speed, float knockbackStrength, double maxEntitySpeed, double maxDistance, SoundEvent sound) {
        super((Map<MemoryModuleType<?>, MemoryModuleState>)ImmutableMap.of(MemoryModuleType.CHARGE_COOLDOWN_TICKS, (Object)((Object)MemoryModuleState.VALUE_ABSENT), MemoryModuleType.ATTACK_TARGET, (Object)((Object)MemoryModuleState.VALUE_PRESENT)));
        this.cooldownTicks = cooldownTicks;
        this.predicate = predicate;
        this.speed = speed;
        this.knockbackStrength = knockbackStrength;
        this.maxEntitySpeed = maxEntitySpeed;
        this.maxDistance = maxDistance;
        this.sound = sound;
        this.velocity = Vec3d.ZERO;
        this.lastPos = Vec3d.ZERO;
    }

    @Override
    protected boolean shouldRun(ServerWorld serverWorld, AnimalEntity animalEntity) {
        return animalEntity.getBrain().hasMemoryModule(MemoryModuleType.ATTACK_TARGET);
    }

    @Override
    protected boolean shouldKeepRunning(ServerWorld serverWorld, AnimalEntity animalEntity, long l) {
        TameableEntity tameableEntity;
        Brain<Integer> brain = animalEntity.getBrain();
        Optional<LivingEntity> optional = brain.getOptionalRegisteredMemory(MemoryModuleType.ATTACK_TARGET);
        if (optional.isEmpty()) {
            return false;
        }
        LivingEntity livingEntity = optional.get();
        if (animalEntity instanceof TameableEntity && (tameableEntity = (TameableEntity)animalEntity).isTamed()) {
            return false;
        }
        if (animalEntity.getEntityPos().subtract(this.lastPos).lengthSquared() >= this.maxEntitySpeed * this.maxEntitySpeed) {
            return false;
        }
        if (livingEntity.getEntityPos().subtract(animalEntity.getEntityPos()).lengthSquared() >= this.maxDistance * this.maxDistance) {
            return false;
        }
        if (!animalEntity.canSee(livingEntity)) {
            return false;
        }
        return !brain.hasMemoryModule(MemoryModuleType.CHARGE_COOLDOWN_TICKS);
    }

    @Override
    protected void run(ServerWorld serverWorld, AnimalEntity animalEntity, long l) {
        Brain<?> brain = animalEntity.getBrain();
        this.lastPos = animalEntity.getEntityPos();
        LivingEntity livingEntity = brain.getOptionalRegisteredMemory(MemoryModuleType.ATTACK_TARGET).get();
        Vec3d vec3d = livingEntity.getEntityPos().subtract(animalEntity.getEntityPos()).normalize();
        this.velocity = vec3d.multiply(this.speed);
        if (this.shouldKeepRunning(serverWorld, animalEntity, l)) {
            animalEntity.playSoundIfNotSilent(this.sound);
        }
    }

    @Override
    protected void keepRunning(ServerWorld serverWorld, AnimalEntity animalEntity, long l) {
        Brain<?> brain = animalEntity.getBrain();
        LivingEntity livingEntity = brain.getOptionalRegisteredMemory(MemoryModuleType.ATTACK_TARGET).orElseThrow();
        animalEntity.lookAtEntity(livingEntity, 360.0f, 360.0f);
        animalEntity.setVelocity(this.velocity);
        ArrayList list = new ArrayList(1);
        serverWorld.collectEntitiesByType(TypeFilter.instanceOf(LivingEntity.class), animalEntity.getBoundingBox(), target -> this.predicate.test(serverWorld, animalEntity, (LivingEntity)target), list, 1);
        if (!list.isEmpty()) {
            LivingEntity livingEntity2 = (LivingEntity)list.get(0);
            if (animalEntity.hasPassenger(livingEntity2)) {
                return;
            }
            this.attack(serverWorld, animalEntity, livingEntity2);
            this.knockbackTarget(animalEntity, livingEntity2);
            this.finishRunning(serverWorld, animalEntity, l);
        }
    }

    private void attack(ServerWorld world, AnimalEntity entity, LivingEntity target) {
        float f;
        DamageSource damageSource = world.getDamageSources().mobAttack(entity);
        if (target.damage(world, damageSource, f = (float)entity.getAttributeValue(EntityAttributes.ATTACK_DAMAGE))) {
            EnchantmentHelper.onTargetDamaged(world, target, damageSource);
        }
    }

    private void knockbackTarget(AnimalEntity entity, LivingEntity target) {
        int i = entity.hasStatusEffect(StatusEffects.SPEED) ? entity.getStatusEffect(StatusEffects.SPEED).getAmplifier() + 1 : 0;
        int j = entity.hasStatusEffect(StatusEffects.SLOWNESS) ? entity.getStatusEffect(StatusEffects.SLOWNESS).getAmplifier() + 1 : 0;
        float f = 0.25f * (float)(i - j);
        float g = MathHelper.clamp(this.speed * (float)entity.getAttributeValue(EntityAttributes.MOVEMENT_SPEED), 0.2f, 2.0f) + f;
        entity.knockbackTarget(target, g * this.knockbackStrength, entity.getVelocity());
    }

    @Override
    protected void finishRunning(ServerWorld serverWorld, AnimalEntity animalEntity, long l) {
        animalEntity.getBrain().remember(MemoryModuleType.CHARGE_COOLDOWN_TICKS, this.cooldownTicks);
        animalEntity.getBrain().forget(MemoryModuleType.ATTACK_TARGET);
    }

    @Override
    protected /* synthetic */ void finishRunning(ServerWorld world, LivingEntity entity, long time) {
        this.finishRunning(world, (AnimalEntity)entity, time);
    }

    @Override
    protected /* synthetic */ void run(ServerWorld world, LivingEntity entity, long time) {
        this.run(world, (AnimalEntity)entity, time);
    }
}


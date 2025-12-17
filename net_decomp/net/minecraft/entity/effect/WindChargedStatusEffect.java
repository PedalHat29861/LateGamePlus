/*
 * Decompiled with CFR 0.152.
 */
package net.minecraft.entity.effect;

import net.minecraft.entity.Entity;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.effect.StatusEffect;
import net.minecraft.entity.effect.StatusEffectCategory;
import net.minecraft.entity.projectile.AbstractWindChargeEntity;
import net.minecraft.particle.ParticleTypes;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.sound.SoundEvents;
import net.minecraft.util.collection.Pool;
import net.minecraft.world.World;

class WindChargedStatusEffect
extends StatusEffect {
    protected WindChargedStatusEffect(StatusEffectCategory statusEffectCategory, int i) {
        super(statusEffectCategory, i, ParticleTypes.SMALL_GUST);
    }

    @Override
    public void onEntityRemoval(ServerWorld world, LivingEntity entity, int amplifier, Entity.RemovalReason reason) {
        if (reason == Entity.RemovalReason.KILLED) {
            double d = entity.getX();
            double e = entity.getY() + (double)(entity.getHeight() / 2.0f);
            double f = entity.getZ();
            float g = 3.0f + entity.getRandom().nextFloat() * 2.0f;
            world.createExplosion(entity, null, AbstractWindChargeEntity.EXPLOSION_BEHAVIOR, d, e, f, g, false, World.ExplosionSourceType.TRIGGER, ParticleTypes.GUST_EMITTER_SMALL, ParticleTypes.GUST_EMITTER_LARGE, Pool.empty(), SoundEvents.ENTITY_BREEZE_WIND_BURST);
        }
    }
}


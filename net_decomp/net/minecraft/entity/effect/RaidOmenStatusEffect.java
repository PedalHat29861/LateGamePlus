/*
 * Decompiled with CFR 0.152.
 */
package net.minecraft.entity.effect;

import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.effect.StatusEffect;
import net.minecraft.entity.effect.StatusEffectCategory;
import net.minecraft.particle.ParticleEffect;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.util.math.BlockPos;

class RaidOmenStatusEffect
extends StatusEffect {
    protected RaidOmenStatusEffect(StatusEffectCategory statusEffectCategory, int i, ParticleEffect particleEffect) {
        super(statusEffectCategory, i, particleEffect);
    }

    @Override
    public boolean canApplyUpdateEffect(int duration, int amplifier) {
        return duration == 1;
    }

    @Override
    public boolean applyUpdateEffect(ServerWorld world, LivingEntity entity, int amplifier) {
        if (entity instanceof ServerPlayerEntity) {
            BlockPos blockPos;
            ServerPlayerEntity serverPlayerEntity = (ServerPlayerEntity)entity;
            if (!entity.isSpectator() && (blockPos = serverPlayerEntity.getStartRaidPos()) != null) {
                world.getRaidManager().startRaid(serverPlayerEntity, blockPos);
                serverPlayerEntity.clearStartRaidPos();
                return false;
            }
        }
        return true;
    }
}


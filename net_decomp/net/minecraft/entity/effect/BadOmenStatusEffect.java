/*
 * Decompiled with CFR 0.152.
 */
package net.minecraft.entity.effect;

import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.effect.StatusEffect;
import net.minecraft.entity.effect.StatusEffectCategory;
import net.minecraft.entity.effect.StatusEffectInstance;
import net.minecraft.entity.effect.StatusEffects;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.village.raid.Raid;
import net.minecraft.world.Difficulty;

class BadOmenStatusEffect
extends StatusEffect {
    protected BadOmenStatusEffect(StatusEffectCategory statusEffectCategory, int i) {
        super(statusEffectCategory, i);
    }

    @Override
    public boolean canApplyUpdateEffect(int duration, int amplifier) {
        return true;
    }

    @Override
    public boolean applyUpdateEffect(ServerWorld world, LivingEntity entity, int amplifier) {
        Raid raid;
        ServerPlayerEntity serverPlayerEntity;
        if (entity instanceof ServerPlayerEntity && !(serverPlayerEntity = (ServerPlayerEntity)entity).isSpectator() && world.getDifficulty() != Difficulty.PEACEFUL && world.isNearOccupiedPointOfInterest(serverPlayerEntity.getBlockPos()) && ((raid = world.getRaidAt(serverPlayerEntity.getBlockPos())) == null || raid.getBadOmenLevel() < raid.getMaxAcceptableBadOmenLevel())) {
            serverPlayerEntity.addStatusEffect(new StatusEffectInstance(StatusEffects.RAID_OMEN, 600, amplifier));
            serverPlayerEntity.setStartRaidPos(serverPlayerEntity.getBlockPos());
            return false;
        }
        return true;
    }
}


/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  org.jspecify.annotations.Nullable
 */
package net.minecraft.entity.effect;

import net.minecraft.entity.Entity;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.effect.InstantStatusEffect;
import net.minecraft.entity.effect.StatusEffectCategory;
import net.minecraft.server.world.ServerWorld;
import org.jspecify.annotations.Nullable;

class InstantHealthOrDamageStatusEffect
extends InstantStatusEffect {
    private final boolean damage;

    public InstantHealthOrDamageStatusEffect(StatusEffectCategory category, int color, boolean damage) {
        super(category, color);
        this.damage = damage;
    }

    @Override
    public boolean applyUpdateEffect(ServerWorld world, LivingEntity entity, int amplifier) {
        if (this.damage == entity.hasInvertedHealingAndHarm()) {
            entity.heal(Math.max(4 << amplifier, 0));
        } else {
            entity.damage(world, entity.getDamageSources().magic(), 6 << amplifier);
        }
        return true;
    }

    @Override
    public void applyInstantEffect(ServerWorld world, @Nullable Entity effectEntity, @Nullable Entity attacker, LivingEntity target, int amplifier, double proximity) {
        if (this.damage == target.hasInvertedHealingAndHarm()) {
            int i = (int)(proximity * (double)(4 << amplifier) + 0.5);
            target.heal(i);
        } else {
            int i = (int)(proximity * (double)(6 << amplifier) + 0.5);
            if (effectEntity == null) {
                target.damage(world, target.getDamageSources().magic(), i);
            } else {
                target.damage(world, target.getDamageSources().indirectMagic(effectEntity, attacker), i);
            }
        }
    }
}


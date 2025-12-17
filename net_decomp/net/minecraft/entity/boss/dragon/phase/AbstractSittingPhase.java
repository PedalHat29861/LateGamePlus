/*
 * Decompiled with CFR 0.152.
 */
package net.minecraft.entity.boss.dragon.phase;

import net.minecraft.entity.boss.dragon.EnderDragonEntity;
import net.minecraft.entity.boss.dragon.phase.AbstractPhase;
import net.minecraft.entity.damage.DamageSource;
import net.minecraft.entity.projectile.PersistentProjectileEntity;
import net.minecraft.entity.projectile.WindChargeEntity;

public abstract class AbstractSittingPhase
extends AbstractPhase {
    public AbstractSittingPhase(EnderDragonEntity enderDragonEntity) {
        super(enderDragonEntity);
    }

    @Override
    public boolean isSittingOrHovering() {
        return true;
    }

    @Override
    public float modifyDamageTaken(DamageSource damageSource, float damage) {
        if (damageSource.getSource() instanceof PersistentProjectileEntity || damageSource.getSource() instanceof WindChargeEntity) {
            damageSource.getSource().setOnFireFor(1.0f);
            return 0.0f;
        }
        return super.modifyDamageTaken(damageSource, damage);
    }
}


/*
 * Decompiled with CFR 0.152.
 */
package net.minecraft.entity.projectile.thrown;

import java.util.List;
import net.minecraft.component.DataComponentTypes;
import net.minecraft.component.type.PotionContentsComponent;
import net.minecraft.entity.Entity;
import net.minecraft.entity.EntityType;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.effect.StatusEffect;
import net.minecraft.entity.effect.StatusEffectInstance;
import net.minecraft.entity.projectile.ProjectileUtil;
import net.minecraft.entity.projectile.thrown.PotionEntity;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.item.Items;
import net.minecraft.registry.entry.RegistryEntry;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.util.hit.HitResult;
import net.minecraft.util.math.Box;
import net.minecraft.world.World;

public class SplashPotionEntity
extends PotionEntity {
    public SplashPotionEntity(EntityType<? extends SplashPotionEntity> entityType, World world) {
        super((EntityType<? extends PotionEntity>)entityType, world);
    }

    public SplashPotionEntity(World world, LivingEntity owner, ItemStack stack) {
        super(EntityType.SPLASH_POTION, world, owner, stack);
    }

    public SplashPotionEntity(World world, double x, double y, double z, ItemStack stack) {
        super(EntityType.SPLASH_POTION, world, x, y, z, stack);
    }

    @Override
    protected Item getDefaultItem() {
        return Items.SPLASH_POTION;
    }

    @Override
    public void spawnAreaEffectCloud(ServerWorld world, ItemStack stack, HitResult hitResult) {
        PotionContentsComponent potionContentsComponent = stack.getOrDefault(DataComponentTypes.POTION_CONTENTS, PotionContentsComponent.DEFAULT);
        float f = stack.getOrDefault(DataComponentTypes.POTION_DURATION_SCALE, Float.valueOf(1.0f)).floatValue();
        Iterable<StatusEffectInstance> iterable = potionContentsComponent.getEffects();
        Box box = this.getBoundingBox().offset(hitResult.getPos().subtract(this.getEntityPos()));
        Box box2 = box.expand(4.0, 2.0, 4.0);
        List<LivingEntity> list = this.getEntityWorld().getNonSpectatingEntities(LivingEntity.class, box2);
        float g = ProjectileUtil.getToleranceMargin(this);
        if (!list.isEmpty()) {
            Entity entity = this.getEffectCause();
            for (LivingEntity livingEntity : list) {
                double d;
                if (!livingEntity.isAffectedBySplashPotions() || !((d = box.squaredMagnitude(livingEntity.getBoundingBox().expand(g))) < 16.0)) continue;
                double e = 1.0 - Math.sqrt(d) / 4.0;
                for (StatusEffectInstance statusEffectInstance : iterable) {
                    RegistryEntry<StatusEffect> registryEntry = statusEffectInstance.getEffectType();
                    if (registryEntry.value().isInstant()) {
                        registryEntry.value().applyInstantEffect(world, this, this.getOwner(), livingEntity, statusEffectInstance.getAmplifier(), e);
                        continue;
                    }
                    int i = statusEffectInstance.mapDuration(baseDuration -> (int)(e * (double)baseDuration * (double)f + 0.5));
                    StatusEffectInstance statusEffectInstance2 = new StatusEffectInstance(registryEntry, i, statusEffectInstance.getAmplifier(), statusEffectInstance.isAmbient(), statusEffectInstance.shouldShowParticles());
                    if (statusEffectInstance2.isDurationBelow(20)) continue;
                    livingEntity.addStatusEffect(statusEffectInstance2, entity);
                }
            }
        }
    }
}


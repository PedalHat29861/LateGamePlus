/*
 * Decompiled with CFR 0.152.
 */
package net.minecraft.entity.projectile;

import java.util.List;
import net.minecraft.entity.AreaEffectCloudEntity;
import net.minecraft.entity.Entity;
import net.minecraft.entity.EntityType;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.effect.StatusEffectInstance;
import net.minecraft.entity.effect.StatusEffects;
import net.minecraft.entity.projectile.ExplosiveProjectileEntity;
import net.minecraft.particle.DragonBreathParticleEffect;
import net.minecraft.particle.ParticleEffect;
import net.minecraft.particle.ParticleTypes;
import net.minecraft.util.hit.EntityHitResult;
import net.minecraft.util.hit.HitResult;
import net.minecraft.util.math.Vec3d;
import net.minecraft.world.World;

public class DragonFireballEntity
extends ExplosiveProjectileEntity {
    public static final float DAMAGE_RANGE = 4.0f;

    public DragonFireballEntity(EntityType<? extends DragonFireballEntity> entityType, World world) {
        super((EntityType<? extends ExplosiveProjectileEntity>)entityType, world);
    }

    public DragonFireballEntity(World world, LivingEntity owner, Vec3d velocity) {
        super(EntityType.DRAGON_FIREBALL, owner, velocity, world);
    }

    @Override
    protected void onCollision(HitResult hitResult) {
        super.onCollision(hitResult);
        if (hitResult.getType() == HitResult.Type.ENTITY && this.isOwner(((EntityHitResult)hitResult).getEntity())) {
            return;
        }
        if (!this.getEntityWorld().isClient()) {
            List<LivingEntity> list = this.getEntityWorld().getNonSpectatingEntities(LivingEntity.class, this.getBoundingBox().expand(4.0, 2.0, 4.0));
            AreaEffectCloudEntity areaEffectCloudEntity = new AreaEffectCloudEntity(this.getEntityWorld(), this.getX(), this.getY(), this.getZ());
            Entity entity = this.getOwner();
            if (entity instanceof LivingEntity) {
                areaEffectCloudEntity.setOwner((LivingEntity)entity);
            }
            areaEffectCloudEntity.setParticleType(DragonBreathParticleEffect.of(ParticleTypes.DRAGON_BREATH, 1.0f));
            areaEffectCloudEntity.setRadius(3.0f);
            areaEffectCloudEntity.setDuration(600);
            areaEffectCloudEntity.setRadiusGrowth((7.0f - areaEffectCloudEntity.getRadius()) / (float)areaEffectCloudEntity.getDuration());
            areaEffectCloudEntity.setPotionDurationScale(0.25f);
            areaEffectCloudEntity.addEffect(new StatusEffectInstance(StatusEffects.INSTANT_DAMAGE, 1, 1));
            if (!list.isEmpty()) {
                for (LivingEntity livingEntity : list) {
                    double d = this.squaredDistanceTo(livingEntity);
                    if (!(d < 16.0)) continue;
                    areaEffectCloudEntity.setPosition(livingEntity.getX(), livingEntity.getY(), livingEntity.getZ());
                    break;
                }
            }
            this.getEntityWorld().syncWorldEvent(2006, this.getBlockPos(), this.isSilent() ? -1 : 1);
            this.getEntityWorld().spawnEntity(areaEffectCloudEntity);
            this.discard();
        }
    }

    @Override
    protected ParticleEffect getParticleType() {
        return DragonBreathParticleEffect.of(ParticleTypes.DRAGON_BREATH, 1.0f);
    }

    @Override
    protected boolean isBurning() {
        return false;
    }
}


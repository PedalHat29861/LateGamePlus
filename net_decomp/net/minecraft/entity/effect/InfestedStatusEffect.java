/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  org.joml.Vector3f
 *  org.joml.Vector3fc
 */
package net.minecraft.entity.effect;

import java.util.function.ToIntFunction;
import net.minecraft.entity.EntityType;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.SpawnReason;
import net.minecraft.entity.damage.DamageSource;
import net.minecraft.entity.effect.StatusEffect;
import net.minecraft.entity.effect.StatusEffectCategory;
import net.minecraft.entity.mob.SilverfishEntity;
import net.minecraft.particle.ParticleTypes;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.sound.SoundEvents;
import net.minecraft.util.math.MathHelper;
import net.minecraft.util.math.Vec3d;
import net.minecraft.util.math.random.Random;
import org.joml.Vector3f;
import org.joml.Vector3fc;

class InfestedStatusEffect
extends StatusEffect {
    private final float silverfishChance;
    private final ToIntFunction<Random> silverfishCountFunction;

    protected InfestedStatusEffect(StatusEffectCategory category, int color, float silverfishChance, ToIntFunction<Random> silverfishCountFunction) {
        super(category, color, ParticleTypes.INFESTED);
        this.silverfishChance = silverfishChance;
        this.silverfishCountFunction = silverfishCountFunction;
    }

    @Override
    public void onEntityDamage(ServerWorld world, LivingEntity entity, int amplifier, DamageSource source, float amount) {
        if (entity.getRandom().nextFloat() <= this.silverfishChance) {
            int i = this.silverfishCountFunction.applyAsInt(entity.getRandom());
            for (int j = 0; j < i; ++j) {
                this.spawnSilverfish(world, entity, entity.getX(), entity.getY() + (double)entity.getHeight() / 2.0, entity.getZ());
            }
        }
    }

    private void spawnSilverfish(ServerWorld world, LivingEntity entity, double x, double y, double z) {
        SilverfishEntity silverfishEntity = EntityType.SILVERFISH.create(world, SpawnReason.TRIGGERED);
        if (silverfishEntity == null) {
            return;
        }
        Random random = entity.getRandom();
        float f = 1.5707964f;
        float g = MathHelper.nextBetween(random, -1.5707964f, 1.5707964f);
        Vector3f vector3f = entity.getRotationVector().toVector3f().mul(0.3f).mul(1.0f, 1.5f, 1.0f).rotateY(g);
        silverfishEntity.refreshPositionAndAngles(x, y, z, world.getRandom().nextFloat() * 360.0f, 0.0f);
        silverfishEntity.setVelocity(new Vec3d((Vector3fc)vector3f));
        world.spawnEntity(silverfishEntity);
        silverfishEntity.playSoundIfNotSilent(SoundEvents.ENTITY_SILVERFISH_HURT);
    }
}


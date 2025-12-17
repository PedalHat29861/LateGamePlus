/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  com.mojang.datafixers.kinds.App
 *  com.mojang.datafixers.kinds.Applicative
 *  com.mojang.serialization.MapCodec
 *  com.mojang.serialization.codecs.RecordCodecBuilder
 */
package net.minecraft.enchantment.effect.entity;

import com.mojang.datafixers.kinds.App;
import com.mojang.datafixers.kinds.Applicative;
import com.mojang.serialization.MapCodec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import net.minecraft.enchantment.EnchantmentEffectContext;
import net.minecraft.enchantment.EnchantmentLevelBasedValue;
import net.minecraft.enchantment.effect.EnchantmentEntityEffect;
import net.minecraft.entity.Entity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.util.math.Vec3d;

public record ApplyImpulseEnchantmentEffect(Vec3d direction, Vec3d coordinateScale, EnchantmentLevelBasedValue magnitude) implements EnchantmentEntityEffect
{
    public static final MapCodec<ApplyImpulseEnchantmentEffect> CODEC = RecordCodecBuilder.mapCodec(instance -> instance.group((App)Vec3d.CODEC.fieldOf("direction").forGetter(ApplyImpulseEnchantmentEffect::direction), (App)Vec3d.CODEC.fieldOf("coordinate_scale").forGetter(ApplyImpulseEnchantmentEffect::coordinateScale), (App)EnchantmentLevelBasedValue.CODEC.fieldOf("magnitude").forGetter(ApplyImpulseEnchantmentEffect::magnitude)).apply((Applicative)instance, ApplyImpulseEnchantmentEffect::new));
    private static final int CURRENT_EXPLOSION_RESET_GRACE_TIME = 10;

    @Override
    public void apply(ServerWorld world, int level, EnchantmentEffectContext context, Entity user, Vec3d pos) {
        Vec3d vec3d = user.getRotationVector();
        Vec3d vec3d2 = vec3d.transformLocalPos(this.direction).multiply(this.coordinateScale).multiply(this.magnitude.getValue(level));
        user.addVelocityInternal(vec3d2);
        user.knockedBack = true;
        user.velocityDirty = true;
        if (user instanceof PlayerEntity) {
            PlayerEntity playerEntity = (PlayerEntity)user;
            playerEntity.setCurrentExplosionResetGraceTime(10);
        }
    }

    public MapCodec<ApplyImpulseEnchantmentEffect> getCodec() {
        return CODEC;
    }
}


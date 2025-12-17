/*
 * Decompiled with CFR 0.152.
 */
package net.minecraft.entity.mob;

import net.minecraft.enchantment.EnchantmentHelper;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.attribute.EntityAttributes;
import net.minecraft.entity.damage.DamageSource;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.util.math.Vec3d;

public interface Hoglin {
    public static final int field_30546 = 10;
    public static final float field_52501 = 0.2f;

    public int getMovementCooldownTicks();

    public static boolean tryAttack(ServerWorld world, LivingEntity attacker, LivingEntity target) {
        float f = (float)attacker.getAttributeValue(EntityAttributes.ATTACK_DAMAGE);
        float g = !attacker.isBaby() && (int)f > 0 ? f / 2.0f + (float)world.random.nextInt((int)f) : f;
        DamageSource damageSource = attacker.getDamageSources().mobAttack(attacker);
        boolean bl = target.damage(world, damageSource, g);
        if (bl) {
            EnchantmentHelper.onTargetDamaged(world, target, damageSource);
            if (!attacker.isBaby()) {
                Hoglin.knockback(attacker, target);
            }
        }
        return bl;
    }

    public static void knockback(LivingEntity attacker, LivingEntity target) {
        double e;
        double d = attacker.getAttributeValue(EntityAttributes.ATTACK_KNOCKBACK);
        double f = d - (e = target.getAttributeValue(EntityAttributes.KNOCKBACK_RESISTANCE));
        if (f <= 0.0) {
            return;
        }
        double g = target.getX() - attacker.getX();
        double h = target.getZ() - attacker.getZ();
        float i = attacker.getEntityWorld().random.nextInt(21) - 10;
        double j = f * (double)(attacker.getEntityWorld().random.nextFloat() * 0.5f + 0.2f);
        Vec3d vec3d = new Vec3d(g, 0.0, h).normalize().multiply(j).rotateY(i);
        double k = f * (double)attacker.getEntityWorld().random.nextFloat() * 0.5;
        target.addVelocity(vec3d.x, k, vec3d.z);
        target.knockedBack = true;
    }
}


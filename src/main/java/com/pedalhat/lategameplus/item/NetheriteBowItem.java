package com.pedalhat.lategameplus.item;

import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.projectile.Projectile;
import net.minecraft.world.entity.projectile.arrow.AbstractArrow;
import net.minecraft.world.item.BowItem;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;

public class NetheriteBowItem extends BowItem {
    public NetheriteBowItem(Properties settings) {
        super(settings);
    }

    public Projectile createProjectile(Level world, LivingEntity shooter, ItemStack weaponStack, ItemStack projectileStack, boolean critical) {
        Projectile projectile = super.createProjectile(world, shooter, weaponStack, projectileStack, critical);
        if (projectile instanceof AbstractArrow persistent) {
            persistent.setBaseDamageFromMob(1.5F);
        }
        return projectile;
    }
}

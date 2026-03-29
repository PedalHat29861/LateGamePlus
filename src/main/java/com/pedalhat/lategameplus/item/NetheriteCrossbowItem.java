package com.pedalhat.lategameplus.item;

import com.pedalhat.lategameplus.config.ConfigManager;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.projectile.Projectile;
import net.minecraft.world.entity.projectile.arrow.AbstractArrow;
import net.minecraft.world.item.CrossbowItem;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;

public class NetheriteCrossbowItem extends CrossbowItem {
    public NetheriteCrossbowItem(Properties settings) {
        super(settings);
    }

    @Override
    protected Projectile createProjectile(Level world, LivingEntity shooter, ItemStack weaponStack, ItemStack projectileStack, boolean critical) {
        Projectile projectile = super.createProjectile(world, shooter, weaponStack, projectileStack, critical);
        if (projectile instanceof AbstractArrow persistent) {
            float multiplier = Math.max(0.0F, ConfigManager.get().netheriteCrossbowDamageMultiplier);
            persistent.setBaseDamageFromMob(multiplier);
        }
        return projectile;
    }
}

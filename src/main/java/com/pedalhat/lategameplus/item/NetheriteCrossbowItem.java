package com.pedalhat.lategameplus.item;

import com.pedalhat.lategameplus.config.ConfigManager;

import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.projectile.PersistentProjectileEntity;
import net.minecraft.entity.projectile.ProjectileEntity;
import net.minecraft.item.CrossbowItem;
import net.minecraft.item.ItemStack;
import net.minecraft.world.World;

public class NetheriteCrossbowItem extends CrossbowItem {
    public NetheriteCrossbowItem(Settings settings) {
        super(settings);
    }

    @Override
    protected ProjectileEntity createArrowEntity(World world, LivingEntity shooter, ItemStack weaponStack, ItemStack projectileStack, boolean critical) {
        ProjectileEntity projectile = super.createArrowEntity(world, shooter, weaponStack, projectileStack, critical);
        if (projectile instanceof PersistentProjectileEntity persistent) {
            float multiplier = Math.max(0.0F, ConfigManager.get().netheriteCrossbowDamageMultiplier);
            persistent.applyDamageModifier(multiplier);
        }
        return projectile;
    }
}

/*
 * Decompiled with CFR 0.152.
 */
package net.minecraft.component.type;

import net.minecraft.component.type.ConsumableComponent;
import net.minecraft.entity.LivingEntity;
import net.minecraft.item.ItemStack;
import net.minecraft.world.World;

public interface Consumable {
    public void onConsume(World var1, LivingEntity var2, ItemStack var3, ConsumableComponent var4);
}


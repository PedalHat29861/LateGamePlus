/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  org.jspecify.annotations.Nullable
 */
package net.minecraft.enchantment;

import java.lang.invoke.MethodHandle;
import java.lang.runtime.ObjectMethods;
import java.util.function.Consumer;
import net.minecraft.entity.EquipmentSlot;
import net.minecraft.entity.LivingEntity;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import org.jspecify.annotations.Nullable;

public record EnchantmentEffectContext(ItemStack stack, @Nullable EquipmentSlot slot, @Nullable LivingEntity owner, Consumer<Item> breakCallback) {
    public EnchantmentEffectContext(ItemStack stack, EquipmentSlot slot, LivingEntity owner) {
        this(stack, slot, owner, item -> owner.sendEquipmentBreakStatus((Item)item, slot));
    }

    @Override
    public final String toString() {
        return ObjectMethods.bootstrap("toString", new MethodHandle[]{EnchantmentEffectContext.class, "itemStack;inSlot;owner;onBreak", "stack", "slot", "owner", "breakCallback"}, this);
    }

    @Override
    public final int hashCode() {
        return (int)ObjectMethods.bootstrap("hashCode", new MethodHandle[]{EnchantmentEffectContext.class, "itemStack;inSlot;owner;onBreak", "stack", "slot", "owner", "breakCallback"}, this);
    }

    @Override
    public final boolean equals(Object object) {
        return (boolean)ObjectMethods.bootstrap("equals", new MethodHandle[]{EnchantmentEffectContext.class, "itemStack;inSlot;owner;onBreak", "stack", "slot", "owner", "breakCallback"}, this, object);
    }
}


/*
 * Decompiled with CFR 0.152.
 */
package net.minecraft.inventory;

import java.util.List;
import java.util.function.Consumer;
import java.util.function.Predicate;
import java.util.function.Supplier;
import net.minecraft.entity.EquipmentSlot;
import net.minecraft.entity.LivingEntity;
import net.minecraft.item.ItemStack;

public interface StackReference {
    public ItemStack get();

    public boolean set(ItemStack var1);

    public static StackReference of(final Supplier<ItemStack> getter, final Consumer<ItemStack> setter) {
        return new StackReference(){

            @Override
            public ItemStack get() {
                return (ItemStack)getter.get();
            }

            @Override
            public boolean set(ItemStack stack) {
                setter.accept(stack);
                return true;
            }
        };
    }

    public static StackReference of(final LivingEntity entity, final EquipmentSlot slot, final Predicate<ItemStack> filter) {
        return new StackReference(){

            @Override
            public ItemStack get() {
                return entity.getEquippedStack(slot);
            }

            @Override
            public boolean set(ItemStack stack) {
                if (!filter.test(stack)) {
                    return false;
                }
                entity.equipStack(slot, stack);
                return true;
            }
        };
    }

    public static StackReference of(LivingEntity entity, EquipmentSlot slot) {
        return StackReference.of(entity, slot, stack -> true);
    }

    public static StackReference of(final List<ItemStack> stacks, final int index) {
        return new StackReference(){

            @Override
            public ItemStack get() {
                return (ItemStack)stacks.get(index);
            }

            @Override
            public boolean set(ItemStack stack) {
                stacks.set(index, stack);
                return true;
            }
        };
    }
}


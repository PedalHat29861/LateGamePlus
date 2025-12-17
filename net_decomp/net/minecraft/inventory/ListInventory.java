/*
 * Decompiled with CFR 0.152.
 */
package net.minecraft.inventory;

import java.util.function.Predicate;
import net.minecraft.inventory.Inventories;
import net.minecraft.inventory.Inventory;
import net.minecraft.item.ItemStack;
import net.minecraft.util.collection.DefaultedList;

public interface ListInventory
extends Inventory {
    public DefaultedList<ItemStack> getHeldStacks();

    default public int getFilledSlotCount() {
        return (int)this.getHeldStacks().stream().filter(Predicate.not(ItemStack::isEmpty)).count();
    }

    @Override
    default public int size() {
        return this.getHeldStacks().size();
    }

    @Override
    default public void clear() {
        this.getHeldStacks().clear();
    }

    @Override
    default public boolean isEmpty() {
        return this.getHeldStacks().stream().allMatch(ItemStack::isEmpty);
    }

    @Override
    default public ItemStack getStack(int slot) {
        return this.getHeldStacks().get(slot);
    }

    @Override
    default public ItemStack removeStack(int slot, int amount) {
        ItemStack itemStack = Inventories.splitStack(this.getHeldStacks(), slot, amount);
        if (!itemStack.isEmpty()) {
            this.markDirty();
        }
        return itemStack;
    }

    @Override
    default public ItemStack removeStack(int slot) {
        return Inventories.splitStack(this.getHeldStacks(), slot, this.getMaxCountPerStack());
    }

    @Override
    default public boolean isValid(int slot, ItemStack stack) {
        return this.canAccept(stack) && (this.getStack(slot).isEmpty() || this.getStack(slot).getCount() < this.getMaxCount(stack));
    }

    default public boolean canAccept(ItemStack stack) {
        return true;
    }

    @Override
    default public void setStack(int slot, ItemStack stack) {
        this.setStackNoMarkDirty(slot, stack);
        this.markDirty();
    }

    default public void setStackNoMarkDirty(int slot, ItemStack stack) {
        this.getHeldStacks().set(slot, stack);
        stack.capCount(this.getMaxCount(stack));
    }
}


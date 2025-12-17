/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  com.google.common.collect.Lists
 *  org.jspecify.annotations.Nullable
 */
package net.minecraft.inventory;

import com.google.common.collect.Lists;
import java.util.List;
import java.util.stream.Collectors;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.inventory.Inventories;
import net.minecraft.inventory.Inventory;
import net.minecraft.inventory.InventoryChangedListener;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.recipe.RecipeFinder;
import net.minecraft.recipe.RecipeInputProvider;
import net.minecraft.storage.ReadView;
import net.minecraft.storage.WriteView;
import net.minecraft.util.collection.DefaultedList;
import org.jspecify.annotations.Nullable;

public class SimpleInventory
implements Inventory,
RecipeInputProvider {
    private final int size;
    private final DefaultedList<ItemStack> heldStacks;
    private @Nullable List<InventoryChangedListener> listeners;

    public SimpleInventory(int size) {
        this.size = size;
        this.heldStacks = DefaultedList.ofSize(size, ItemStack.EMPTY);
    }

    public SimpleInventory(ItemStack ... items) {
        this.size = items.length;
        this.heldStacks = DefaultedList.copyOf(ItemStack.EMPTY, items);
    }

    public void addListener(InventoryChangedListener listener) {
        if (this.listeners == null) {
            this.listeners = Lists.newArrayList();
        }
        this.listeners.add(listener);
    }

    public void removeListener(InventoryChangedListener listener) {
        if (this.listeners != null) {
            this.listeners.remove(listener);
        }
    }

    @Override
    public ItemStack getStack(int slot) {
        if (slot < 0 || slot >= this.heldStacks.size()) {
            return ItemStack.EMPTY;
        }
        return this.heldStacks.get(slot);
    }

    public List<ItemStack> clearToList() {
        List<ItemStack> list = this.heldStacks.stream().filter(stack -> !stack.isEmpty()).collect(Collectors.toList());
        this.clear();
        return list;
    }

    @Override
    public ItemStack removeStack(int slot, int amount) {
        ItemStack itemStack = Inventories.splitStack(this.heldStacks, slot, amount);
        if (!itemStack.isEmpty()) {
            this.markDirty();
        }
        return itemStack;
    }

    public ItemStack removeItem(Item item, int count) {
        ItemStack itemStack = new ItemStack(item, 0);
        for (int i = this.size - 1; i >= 0; --i) {
            ItemStack itemStack2 = this.getStack(i);
            if (!itemStack2.getItem().equals(item)) continue;
            int j = count - itemStack.getCount();
            ItemStack itemStack3 = itemStack2.split(j);
            itemStack.increment(itemStack3.getCount());
            if (itemStack.getCount() == count) break;
        }
        if (!itemStack.isEmpty()) {
            this.markDirty();
        }
        return itemStack;
    }

    public ItemStack addStack(ItemStack stack) {
        if (stack.isEmpty()) {
            return ItemStack.EMPTY;
        }
        ItemStack itemStack = stack.copy();
        this.addToExistingSlot(itemStack);
        if (itemStack.isEmpty()) {
            return ItemStack.EMPTY;
        }
        this.addToNewSlot(itemStack);
        if (itemStack.isEmpty()) {
            return ItemStack.EMPTY;
        }
        return itemStack;
    }

    public boolean canInsert(ItemStack stack) {
        boolean bl = false;
        for (ItemStack itemStack : this.heldStacks) {
            if (!itemStack.isEmpty() && (!ItemStack.areItemsAndComponentsEqual(itemStack, stack) || itemStack.getCount() >= itemStack.getMaxCount())) continue;
            bl = true;
            break;
        }
        return bl;
    }

    @Override
    public ItemStack removeStack(int slot) {
        ItemStack itemStack = this.heldStacks.get(slot);
        if (itemStack.isEmpty()) {
            return ItemStack.EMPTY;
        }
        this.heldStacks.set(slot, ItemStack.EMPTY);
        return itemStack;
    }

    @Override
    public void setStack(int slot, ItemStack stack) {
        this.heldStacks.set(slot, stack);
        stack.capCount(this.getMaxCount(stack));
        this.markDirty();
    }

    @Override
    public int size() {
        return this.size;
    }

    @Override
    public boolean isEmpty() {
        for (ItemStack itemStack : this.heldStacks) {
            if (itemStack.isEmpty()) continue;
            return false;
        }
        return true;
    }

    @Override
    public void markDirty() {
        if (this.listeners != null) {
            for (InventoryChangedListener inventoryChangedListener : this.listeners) {
                inventoryChangedListener.onInventoryChanged(this);
            }
        }
    }

    @Override
    public boolean canPlayerUse(PlayerEntity player) {
        return true;
    }

    @Override
    public void clear() {
        this.heldStacks.clear();
        this.markDirty();
    }

    @Override
    public void provideRecipeInputs(RecipeFinder finder) {
        for (ItemStack itemStack : this.heldStacks) {
            finder.addInput(itemStack);
        }
    }

    public String toString() {
        return this.heldStacks.stream().filter(stack -> !stack.isEmpty()).collect(Collectors.toList()).toString();
    }

    private void addToNewSlot(ItemStack stack) {
        for (int i = 0; i < this.size; ++i) {
            ItemStack itemStack = this.getStack(i);
            if (!itemStack.isEmpty()) continue;
            this.setStack(i, stack.copyAndEmpty());
            return;
        }
    }

    private void addToExistingSlot(ItemStack stack) {
        for (int i = 0; i < this.size; ++i) {
            ItemStack itemStack = this.getStack(i);
            if (!ItemStack.areItemsAndComponentsEqual(itemStack, stack)) continue;
            this.transfer(stack, itemStack);
            if (!stack.isEmpty()) continue;
            return;
        }
    }

    private void transfer(ItemStack source, ItemStack target) {
        int i = this.getMaxCount(target);
        int j = Math.min(source.getCount(), i - target.getCount());
        if (j > 0) {
            target.increment(j);
            source.decrement(j);
            this.markDirty();
        }
    }

    public void readDataList(ReadView.TypedListReadView<ItemStack> list) {
        this.clear();
        for (ItemStack itemStack : list) {
            this.addStack(itemStack);
        }
    }

    public void toDataList(WriteView.ListAppender<ItemStack> list) {
        for (int i = 0; i < this.size(); ++i) {
            ItemStack itemStack = this.getStack(i);
            if (itemStack.isEmpty()) continue;
            list.add(itemStack);
        }
    }

    public DefaultedList<ItemStack> getHeldStacks() {
        return this.heldStacks;
    }
}


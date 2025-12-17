/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  com.google.common.collect.Lists
 */
package net.minecraft.recipe;

import com.google.common.collect.Lists;
import java.util.ArrayList;
import java.util.List;
import net.minecraft.entity.player.PlayerInventory;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.recipe.Recipe;
import net.minecraft.recipe.RecipeEntry;
import net.minecraft.recipe.RecipeFinder;
import net.minecraft.recipe.RecipeGridAligner;
import net.minecraft.recipe.input.RecipeInput;
import net.minecraft.registry.entry.RegistryEntry;
import net.minecraft.screen.AbstractRecipeScreenHandler;
import net.minecraft.screen.slot.Slot;

public class InputSlotFiller<R extends Recipe<?>> {
    private static final int field_51523 = -1;
    private final PlayerInventory inventory;
    private final Handler<R> handler;
    private final boolean craftAll;
    private final int width;
    private final int height;
    private final List<Slot> inputSlots;
    private final List<Slot> slotsToReturn;

    public static <I extends RecipeInput, R extends Recipe<I>> AbstractRecipeScreenHandler.PostFillAction fill(Handler<R> handler, int width, int height, List<Slot> inputSlots, List<Slot> slotsToReturn, PlayerInventory inventory, RecipeEntry<R> recipe, boolean craftAll, boolean creative) {
        InputSlotFiller<R> inputSlotFiller = new InputSlotFiller<R>(handler, inventory, craftAll, width, height, inputSlots, slotsToReturn);
        if (!creative && !inputSlotFiller.canReturnInputs()) {
            return AbstractRecipeScreenHandler.PostFillAction.NOTHING;
        }
        RecipeFinder recipeFinder = new RecipeFinder();
        inventory.populateRecipeFinder(recipeFinder);
        handler.populateRecipeFinder(recipeFinder);
        return inputSlotFiller.tryFill(recipe, recipeFinder);
    }

    private InputSlotFiller(Handler<R> handler, PlayerInventory inventory, boolean craftAll, int width, int height, List<Slot> inputSlots, List<Slot> slotsToReturn) {
        this.handler = handler;
        this.inventory = inventory;
        this.craftAll = craftAll;
        this.width = width;
        this.height = height;
        this.inputSlots = inputSlots;
        this.slotsToReturn = slotsToReturn;
    }

    private AbstractRecipeScreenHandler.PostFillAction tryFill(RecipeEntry<R> recipe, RecipeFinder finder) {
        if (finder.isCraftable((Recipe<?>)recipe.value(), null)) {
            this.fill(recipe, finder);
            this.inventory.markDirty();
            return AbstractRecipeScreenHandler.PostFillAction.NOTHING;
        }
        this.returnInputs();
        this.inventory.markDirty();
        return AbstractRecipeScreenHandler.PostFillAction.PLACE_GHOST_RECIPE;
    }

    private void returnInputs() {
        for (Slot slot : this.slotsToReturn) {
            ItemStack itemStack = slot.getStack().copy();
            this.inventory.offer(itemStack, false);
            slot.setStackNoCallbacks(itemStack);
        }
        this.handler.clear();
    }

    private void fill(RecipeEntry<R> recipe, RecipeFinder finder) {
        boolean bl = this.handler.matches(recipe);
        int i = finder.countCrafts((Recipe<?>)recipe.value(), null);
        if (bl) {
            for (Slot slot2 : this.inputSlots) {
                ItemStack itemStack = slot2.getStack();
                if (itemStack.isEmpty() || Math.min(i, itemStack.getMaxCount()) >= itemStack.getCount() + 1) continue;
                return;
            }
        }
        int j = this.calculateCraftAmount(i, bl);
        ArrayList<RegistryEntry<Item>> list = new ArrayList<RegistryEntry<Item>>();
        if (!finder.isCraftable((Recipe<?>)recipe.value(), j, list::add)) {
            return;
        }
        int k = InputSlotFiller.clampToMaxCount(j, list);
        if (k != j) {
            list.clear();
            if (!finder.isCraftable((Recipe<?>)recipe.value(), k, list::add)) {
                return;
            }
        }
        this.returnInputs();
        RecipeGridAligner.alignRecipeToGrid(this.width, this.height, recipe.value(), recipe.value().getIngredientPlacement().getPlacementSlots(), (slot, index, x, y) -> {
            if (slot == -1) {
                return;
            }
            Slot slot2 = this.inputSlots.get(index);
            RegistryEntry registryEntry = (RegistryEntry)list.get((int)slot);
            int j = k;
            while (j > 0) {
                if ((j = this.fillInputSlot(slot2, registryEntry, j)) != -1) continue;
                return;
            }
        });
    }

    private static int clampToMaxCount(int count, List<RegistryEntry<Item>> entries) {
        for (RegistryEntry<Item> registryEntry : entries) {
            count = Math.min(count, registryEntry.value().getMaxCount());
        }
        return count;
    }

    private int calculateCraftAmount(int forCraftAll, boolean match) {
        if (this.craftAll) {
            return forCraftAll;
        }
        if (match) {
            int i = Integer.MAX_VALUE;
            for (Slot slot : this.inputSlots) {
                ItemStack itemStack = slot.getStack();
                if (itemStack.isEmpty() || i <= itemStack.getCount()) continue;
                i = itemStack.getCount();
            }
            if (i != Integer.MAX_VALUE) {
                ++i;
            }
            return i;
        }
        return 1;
    }

    private int fillInputSlot(Slot slot, RegistryEntry<Item> item, int count) {
        ItemStack itemStack = slot.getStack();
        int i = this.inventory.getMatchingSlot(item, itemStack);
        if (i == -1) {
            return -1;
        }
        ItemStack itemStack2 = this.inventory.getStack(i);
        ItemStack itemStack3 = count < itemStack2.getCount() ? this.inventory.removeStack(i, count) : this.inventory.removeStack(i);
        int j = itemStack3.getCount();
        if (itemStack.isEmpty()) {
            slot.setStackNoCallbacks(itemStack3);
        } else {
            itemStack.increment(j);
        }
        return count - j;
    }

    private boolean canReturnInputs() {
        ArrayList list = Lists.newArrayList();
        int i = this.getFreeInventorySlots();
        for (Slot slot : this.inputSlots) {
            ItemStack itemStack = slot.getStack().copy();
            if (itemStack.isEmpty()) continue;
            int j = this.inventory.getOccupiedSlotWithRoomForStack(itemStack);
            if (j == -1 && list.size() <= i) {
                for (ItemStack itemStack2 : list) {
                    if (!ItemStack.areItemsEqual(itemStack2, itemStack) || itemStack2.getCount() == itemStack2.getMaxCount() || itemStack2.getCount() + itemStack.getCount() > itemStack2.getMaxCount()) continue;
                    itemStack2.increment(itemStack.getCount());
                    itemStack.setCount(0);
                    break;
                }
                if (itemStack.isEmpty()) continue;
                if (list.size() < i) {
                    list.add(itemStack);
                    continue;
                }
                return false;
            }
            if (j != -1) continue;
            return false;
        }
        return true;
    }

    private int getFreeInventorySlots() {
        int i = 0;
        for (ItemStack itemStack : this.inventory.getMainStacks()) {
            if (!itemStack.isEmpty()) continue;
            ++i;
        }
        return i;
    }

    public static interface Handler<T extends Recipe<?>> {
        public void populateRecipeFinder(RecipeFinder var1);

        public void clear();

        public boolean matches(RecipeEntry<T> var1);
    }
}


/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  com.google.common.annotations.VisibleForTesting
 *  it.unimi.dsi.fastutil.ints.IntArrayList
 */
package net.minecraft.block.entity;

import com.google.common.annotations.VisibleForTesting;
import it.unimi.dsi.fastutil.ints.IntArrayList;
import java.util.List;
import net.minecraft.block.BlockState;
import net.minecraft.block.CrafterBlock;
import net.minecraft.block.entity.BlockEntityType;
import net.minecraft.block.entity.LootableContainerBlockEntity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.entity.player.PlayerInventory;
import net.minecraft.inventory.Inventories;
import net.minecraft.inventory.Inventory;
import net.minecraft.inventory.RecipeInputInventory;
import net.minecraft.item.ItemStack;
import net.minecraft.recipe.RecipeFinder;
import net.minecraft.screen.CrafterScreenHandler;
import net.minecraft.screen.PropertyDelegate;
import net.minecraft.screen.ScreenHandler;
import net.minecraft.storage.ReadView;
import net.minecraft.storage.WriteView;
import net.minecraft.text.Text;
import net.minecraft.util.collection.DefaultedList;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;

public class CrafterBlockEntity
extends LootableContainerBlockEntity
implements RecipeInputInventory {
    public static final int GRID_WIDTH = 3;
    public static final int GRID_HEIGHT = 3;
    public static final int GRID_SIZE = 9;
    public static final int SLOT_DISABLED = 1;
    public static final int SLOT_ENABLED = 0;
    public static final int TRIGGERED_PROPERTY = 9;
    public static final int PROPERTIES_COUNT = 10;
    private static final int DEFAULT_CRAFTING_TICKS_REMAINING = 0;
    private static final int DEFAULT_TRIGGERED = 0;
    private static final Text CONTAINER_NAME_TEXT = Text.translatable("container.crafter");
    private DefaultedList<ItemStack> inputStacks = DefaultedList.ofSize(9, ItemStack.EMPTY);
    private int craftingTicksRemaining = 0;
    protected final PropertyDelegate propertyDelegate = new PropertyDelegate(this){
        private final int[] disabledSlots = new int[9];
        private int triggered = 0;

        @Override
        public int get(int index) {
            return index == 9 ? this.triggered : this.disabledSlots[index];
        }

        @Override
        public void set(int index, int value) {
            if (index == 9) {
                this.triggered = value;
            } else {
                this.disabledSlots[index] = value;
            }
        }

        @Override
        public int size() {
            return 10;
        }
    };

    public CrafterBlockEntity(BlockPos pos, BlockState state) {
        super(BlockEntityType.CRAFTER, pos, state);
    }

    @Override
    protected Text getContainerName() {
        return CONTAINER_NAME_TEXT;
    }

    @Override
    protected ScreenHandler createScreenHandler(int syncId, PlayerInventory playerInventory) {
        return new CrafterScreenHandler(syncId, playerInventory, this, this.propertyDelegate);
    }

    public void setSlotEnabled(int slot, boolean enabled) {
        if (!this.canToggleSlot(slot)) {
            return;
        }
        this.propertyDelegate.set(slot, enabled ? 0 : 1);
        this.markDirty();
    }

    public boolean isSlotDisabled(int slot) {
        if (slot >= 0 && slot < 9) {
            return this.propertyDelegate.get(slot) == 1;
        }
        return false;
    }

    @Override
    public boolean isValid(int slot, ItemStack stack) {
        if (this.propertyDelegate.get(slot) == 1) {
            return false;
        }
        ItemStack itemStack = this.inputStacks.get(slot);
        int i = itemStack.getCount();
        if (i >= itemStack.getMaxCount()) {
            return false;
        }
        if (itemStack.isEmpty()) {
            return true;
        }
        return !this.betterSlotExists(i, itemStack, slot);
    }

    private boolean betterSlotExists(int count, ItemStack stack, int slot) {
        for (int i = slot + 1; i < 9; ++i) {
            ItemStack itemStack;
            if (this.isSlotDisabled(i) || !(itemStack = this.getStack(i)).isEmpty() && (itemStack.getCount() >= count || !ItemStack.areItemsAndComponentsEqual(itemStack, stack))) continue;
            return true;
        }
        return false;
    }

    @Override
    protected void readData(ReadView view) {
        super.readData(view);
        this.craftingTicksRemaining = view.getInt("crafting_ticks_remaining", 0);
        this.inputStacks = DefaultedList.ofSize(this.size(), ItemStack.EMPTY);
        if (!this.readLootTable(view)) {
            Inventories.readData(view, this.inputStacks);
        }
        for (int i = 0; i < 9; ++i) {
            this.propertyDelegate.set(i, 0);
        }
        view.getOptionalIntArray("disabled_slots").ifPresent(slots -> {
            for (int i : slots) {
                if (!this.canToggleSlot(i)) continue;
                this.propertyDelegate.set(i, 1);
            }
        });
        this.propertyDelegate.set(9, view.getInt("triggered", 0));
    }

    @Override
    protected void writeData(WriteView view) {
        super.writeData(view);
        view.putInt("crafting_ticks_remaining", this.craftingTicksRemaining);
        if (!this.writeLootTable(view)) {
            Inventories.writeData(view, this.inputStacks);
        }
        this.putDisabledSlots(view);
        this.putTriggered(view);
    }

    @Override
    public int size() {
        return 9;
    }

    @Override
    public boolean isEmpty() {
        for (ItemStack itemStack : this.inputStacks) {
            if (itemStack.isEmpty()) continue;
            return false;
        }
        return true;
    }

    @Override
    public ItemStack getStack(int slot) {
        return this.inputStacks.get(slot);
    }

    @Override
    public void setStack(int slot, ItemStack stack) {
        if (this.isSlotDisabled(slot)) {
            this.setSlotEnabled(slot, true);
        }
        super.setStack(slot, stack);
    }

    @Override
    public boolean canPlayerUse(PlayerEntity player) {
        return Inventory.canPlayerUse(this, player);
    }

    @Override
    public DefaultedList<ItemStack> getHeldStacks() {
        return this.inputStacks;
    }

    @Override
    protected void setHeldStacks(DefaultedList<ItemStack> inventory) {
        this.inputStacks = inventory;
    }

    @Override
    public int getWidth() {
        return 3;
    }

    @Override
    public int getHeight() {
        return 3;
    }

    @Override
    public void provideRecipeInputs(RecipeFinder finder) {
        for (ItemStack itemStack : this.inputStacks) {
            finder.addInputIfUsable(itemStack);
        }
    }

    private void putDisabledSlots(WriteView view) {
        IntArrayList intList = new IntArrayList();
        for (int i = 0; i < 9; ++i) {
            if (!this.isSlotDisabled(i)) continue;
            intList.add(i);
        }
        view.putIntArray("disabled_slots", intList.toIntArray());
    }

    private void putTriggered(WriteView view) {
        view.putInt("triggered", this.propertyDelegate.get(9));
    }

    public void setTriggered(boolean triggered) {
        this.propertyDelegate.set(9, triggered ? 1 : 0);
    }

    @VisibleForTesting
    public boolean isTriggered() {
        return this.propertyDelegate.get(9) == 1;
    }

    public static void tickCrafting(World world, BlockPos pos, BlockState state, CrafterBlockEntity blockEntity) {
        int i = blockEntity.craftingTicksRemaining - 1;
        if (i < 0) {
            return;
        }
        blockEntity.craftingTicksRemaining = i;
        if (i == 0) {
            world.setBlockState(pos, (BlockState)state.with(CrafterBlock.CRAFTING, false), 3);
        }
    }

    public void setCraftingTicksRemaining(int craftingTicksRemaining) {
        this.craftingTicksRemaining = craftingTicksRemaining;
    }

    public int getComparatorOutput() {
        int i = 0;
        for (int j = 0; j < this.size(); ++j) {
            ItemStack itemStack = this.getStack(j);
            if (itemStack.isEmpty() && !this.isSlotDisabled(j)) continue;
            ++i;
        }
        return i;
    }

    private boolean canToggleSlot(int slot) {
        return slot > -1 && slot < 9 && this.inputStacks.get(slot).isEmpty();
    }

    public /* synthetic */ List getHeldStacks() {
        return this.getHeldStacks();
    }
}


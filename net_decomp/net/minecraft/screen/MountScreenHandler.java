/*
 * Decompiled with CFR 0.152.
 */
package net.minecraft.screen;

import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.entity.player.PlayerInventory;
import net.minecraft.inventory.Inventory;
import net.minecraft.item.ItemStack;
import net.minecraft.screen.ScreenHandler;
import net.minecraft.screen.slot.Slot;

public abstract class MountScreenHandler
extends ScreenHandler {
    protected final Inventory inventory;
    protected final LivingEntity mount;
    protected final int field_64487 = 0;
    protected final int field_64488 = 1;
    protected final int field_64489 = 2;
    protected static final int field_64490 = 3;

    protected MountScreenHandler(int syncId, PlayerInventory playerInventory, Inventory inventory, LivingEntity mount) {
        super(null, syncId);
        this.inventory = inventory;
        this.mount = mount;
        inventory.onOpen(playerInventory.player);
    }

    protected abstract boolean areInventoriesDifferent(Inventory var1);

    @Override
    public boolean canUse(PlayerEntity player) {
        return !this.areInventoriesDifferent(this.inventory) && this.inventory.canPlayerUse(player) && this.mount.isAlive() && player.canInteractWithEntity(this.mount, 4.0);
    }

    @Override
    public void onClosed(PlayerEntity player) {
        super.onClosed(player);
        this.inventory.onClose(player);
    }

    @Override
    public ItemStack quickMove(PlayerEntity player, int slot) {
        ItemStack itemStack = ItemStack.EMPTY;
        Slot slot2 = (Slot)this.slots.get(slot);
        if (slot2 != null && slot2.hasStack()) {
            ItemStack itemStack2 = slot2.getStack();
            itemStack = itemStack2.copy();
            int i = 2 + this.inventory.size();
            if (slot < i) {
                if (!this.insertItem(itemStack2, i, this.slots.size(), true)) {
                    return ItemStack.EMPTY;
                }
            } else if (this.getSlot(1).canInsert(itemStack2) && !this.getSlot(1).hasStack()) {
                if (!this.insertItem(itemStack2, 1, 2, false)) {
                    return ItemStack.EMPTY;
                }
            } else if (this.getSlot(0).canInsert(itemStack2) && !this.getSlot(0).hasStack()) {
                if (!this.insertItem(itemStack2, 0, 1, false)) {
                    return ItemStack.EMPTY;
                }
            } else if (this.inventory.size() == 0 || !this.insertItem(itemStack2, 2, i, false)) {
                int j;
                int k = j = i + 27;
                int l = k + 9;
                if (slot >= k && slot < l ? !this.insertItem(itemStack2, i, j, false) : (slot >= i && slot < j ? !this.insertItem(itemStack2, k, l, false) : !this.insertItem(itemStack2, k, j, false))) {
                    return ItemStack.EMPTY;
                }
                return ItemStack.EMPTY;
            }
            if (itemStack2.isEmpty()) {
                slot2.setStack(ItemStack.EMPTY);
            } else {
                slot2.markDirty();
            }
        }
        return itemStack;
    }

    public static int getSlotCount(int columns) {
        return columns * 3;
    }
}


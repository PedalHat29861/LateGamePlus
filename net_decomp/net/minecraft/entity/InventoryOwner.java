/*
 * Decompiled with CFR 0.152.
 */
package net.minecraft.entity;

import net.minecraft.entity.ItemEntity;
import net.minecraft.entity.mob.MobEntity;
import net.minecraft.inventory.SimpleInventory;
import net.minecraft.item.ItemStack;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.storage.ReadView;
import net.minecraft.storage.WriteView;

public interface InventoryOwner {
    public static final String INVENTORY_KEY = "Inventory";

    public SimpleInventory getInventory();

    public static void pickUpItem(ServerWorld world, MobEntity entity, InventoryOwner inventoryOwner, ItemEntity item) {
        ItemStack itemStack = item.getStack();
        if (entity.canGather(world, itemStack)) {
            SimpleInventory simpleInventory = inventoryOwner.getInventory();
            boolean bl = simpleInventory.canInsert(itemStack);
            if (!bl) {
                return;
            }
            entity.triggerItemPickedUpByEntityCriteria(item);
            int i = itemStack.getCount();
            ItemStack itemStack2 = simpleInventory.addStack(itemStack);
            entity.sendPickup(item, i - itemStack2.getCount());
            if (itemStack2.isEmpty()) {
                item.discard();
            } else {
                itemStack.setCount(itemStack2.getCount());
            }
        }
    }

    default public void readInventory(ReadView view) {
        view.getOptionalTypedListView(INVENTORY_KEY, ItemStack.CODEC).ifPresent(list -> this.getInventory().readDataList((ReadView.TypedListReadView<ItemStack>)list));
    }

    default public void writeInventory(WriteView view) {
        this.getInventory().toDataList(view.getListAppender(INVENTORY_KEY, ItemStack.CODEC));
    }
}


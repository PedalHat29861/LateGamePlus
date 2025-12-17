/*
 * Decompiled with CFR 0.152.
 */
package net.minecraft.screen;

import net.minecraft.entity.EquipmentSlot;
import net.minecraft.entity.passive.AbstractNautilusEntity;
import net.minecraft.entity.player.PlayerInventory;
import net.minecraft.inventory.Inventory;
import net.minecraft.screen.MountScreenHandler;
import net.minecraft.screen.slot.ArmorSlot;
import net.minecraft.util.Identifier;

public class NautilusScreenHandler
extends MountScreenHandler {
    private static final Identifier EMPTY_SADDLE_SLOT_TEXTURE = Identifier.ofVanilla("container/slot/saddle");
    private static final Identifier EMPTY_NAUTILUS_ARMOR_SLOT_TEXTURE = Identifier.ofVanilla("container/slot/nautilus_armor_inventory");

    public NautilusScreenHandler(int syncId, PlayerInventory playerInventory, Inventory inventory, final AbstractNautilusEntity nautilus, int slotColumnCount) {
        super(syncId, playerInventory, inventory, nautilus);
        Inventory inventory2 = nautilus.createEquipmentInventory(EquipmentSlot.SADDLE);
        this.addSlot(new ArmorSlot(this, inventory2, nautilus, EquipmentSlot.SADDLE, 0, 8, 18, EMPTY_SADDLE_SLOT_TEXTURE){

            @Override
            public boolean isEnabled() {
                return nautilus.canUseSlot(EquipmentSlot.SADDLE);
            }
        });
        Inventory inventory3 = nautilus.createEquipmentInventory(EquipmentSlot.BODY);
        this.addSlot(new ArmorSlot(this, inventory3, nautilus, EquipmentSlot.BODY, 0, 8, 36, EMPTY_NAUTILUS_ARMOR_SLOT_TEXTURE){

            @Override
            public boolean isEnabled() {
                return nautilus.canUseSlot(EquipmentSlot.BODY);
            }
        });
        this.addPlayerSlots(playerInventory, 8, 84);
    }

    @Override
    protected boolean areInventoriesDifferent(Inventory inventory) {
        return ((AbstractNautilusEntity)this.mount).areInventoriesDifferent(inventory);
    }
}


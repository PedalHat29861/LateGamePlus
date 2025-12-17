/*
 * Decompiled with CFR 0.152.
 */
package net.minecraft.entity.player;

import net.minecraft.entity.EntityEquipment;
import net.minecraft.entity.EquipmentSlot;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.ItemStack;

public class PlayerEquipment
extends EntityEquipment {
    private final PlayerEntity player;

    public PlayerEquipment(PlayerEntity player) {
        this.player = player;
    }

    @Override
    public ItemStack put(EquipmentSlot slot, ItemStack itemStack) {
        if (slot == EquipmentSlot.MAINHAND) {
            return this.player.getInventory().setSelectedStack(itemStack);
        }
        return super.put(slot, itemStack);
    }

    @Override
    public ItemStack get(EquipmentSlot slot) {
        if (slot == EquipmentSlot.MAINHAND) {
            return this.player.getInventory().getSelectedStack();
        }
        return super.get(slot);
    }

    @Override
    public boolean isEmpty() {
        return this.player.getInventory().getSelectedStack().isEmpty() && super.isEmpty();
    }
}


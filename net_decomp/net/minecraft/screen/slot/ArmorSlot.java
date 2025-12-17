/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  org.jspecify.annotations.Nullable
 */
package net.minecraft.screen.slot;

import net.minecraft.component.EnchantmentEffectComponentTypes;
import net.minecraft.enchantment.EnchantmentHelper;
import net.minecraft.entity.EquipmentSlot;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.inventory.Inventory;
import net.minecraft.item.ItemStack;
import net.minecraft.screen.slot.Slot;
import net.minecraft.util.Identifier;
import org.jspecify.annotations.Nullable;

class ArmorSlot
extends Slot {
    private final LivingEntity entity;
    private final EquipmentSlot equipmentSlot;
    private final @Nullable Identifier backgroundSprite;

    public ArmorSlot(Inventory inventory, LivingEntity entity, EquipmentSlot equipmentSlot, int index, int x, int y, @Nullable Identifier backgroundSprite) {
        super(inventory, index, x, y);
        this.entity = entity;
        this.equipmentSlot = equipmentSlot;
        this.backgroundSprite = backgroundSprite;
    }

    @Override
    public void setStack(ItemStack stack, ItemStack previousStack) {
        this.entity.onEquipStack(this.equipmentSlot, previousStack, stack);
        super.setStack(stack, previousStack);
    }

    @Override
    public int getMaxItemCount() {
        return 1;
    }

    @Override
    public boolean canInsert(ItemStack stack) {
        return this.entity.canEquip(stack, this.equipmentSlot);
    }

    @Override
    public boolean isEnabled() {
        return this.entity.canUseSlot(this.equipmentSlot);
    }

    @Override
    public boolean canTakeItems(PlayerEntity playerEntity) {
        ItemStack itemStack = this.getStack();
        if (!itemStack.isEmpty() && !playerEntity.isCreative() && EnchantmentHelper.hasAnyEnchantmentsWith(itemStack, EnchantmentEffectComponentTypes.PREVENT_ARMOR_CHANGE)) {
            return false;
        }
        return super.canTakeItems(playerEntity);
    }

    @Override
    public @Nullable Identifier getBackgroundSprite() {
        return this.backgroundSprite;
    }
}


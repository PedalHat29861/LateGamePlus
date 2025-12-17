/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  it.unimi.dsi.fastutil.objects.ObjectArrayList
 *  org.jspecify.annotations.Nullable
 */
package net.minecraft.entity;

import it.unimi.dsi.fastutil.objects.ObjectArrayList;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import net.minecraft.component.DataComponentTypes;
import net.minecraft.component.type.EquippableComponent;
import net.minecraft.entity.EquipmentSlot;
import net.minecraft.entity.EquipmentTable;
import net.minecraft.item.ItemStack;
import net.minecraft.loot.LootTable;
import net.minecraft.loot.context.LootWorldContext;
import net.minecraft.registry.RegistryKey;
import org.jspecify.annotations.Nullable;

public interface EquipmentHolder {
    public void equipStack(EquipmentSlot var1, ItemStack var2);

    public ItemStack getEquippedStack(EquipmentSlot var1);

    public void setEquipmentDropChance(EquipmentSlot var1, float var2);

    default public void setEquipmentFromTable(EquipmentTable equipmentTable, LootWorldContext parameters) {
        this.setEquipmentFromTable(equipmentTable.lootTable(), parameters, equipmentTable.slotDropChances());
    }

    default public void setEquipmentFromTable(RegistryKey<LootTable> lootTable, LootWorldContext parameters, Map<EquipmentSlot, Float> slotDropChances) {
        this.setEquipmentFromTable(lootTable, parameters, 0L, slotDropChances);
    }

    default public void setEquipmentFromTable(RegistryKey<LootTable> lootTable, LootWorldContext parameters, long seed, Map<EquipmentSlot, Float> slotDropChances) {
        LootTable lootTable2 = parameters.getWorld().getServer().getReloadableRegistries().getLootTable(lootTable);
        if (lootTable2 == LootTable.EMPTY) {
            return;
        }
        ObjectArrayList<ItemStack> list = lootTable2.generateLoot(parameters, seed);
        ArrayList<EquipmentSlot> list2 = new ArrayList<EquipmentSlot>();
        for (ItemStack itemStack : list) {
            EquipmentSlot equipmentSlot = this.getSlotForStack(itemStack, list2);
            if (equipmentSlot == null) continue;
            ItemStack itemStack2 = equipmentSlot.split(itemStack);
            this.equipStack(equipmentSlot, itemStack2);
            Float float_ = slotDropChances.get(equipmentSlot);
            if (float_ != null) {
                this.setEquipmentDropChance(equipmentSlot, float_.floatValue());
            }
            list2.add(equipmentSlot);
        }
    }

    default public @Nullable EquipmentSlot getSlotForStack(ItemStack stack, List<EquipmentSlot> slotBlacklist) {
        if (stack.isEmpty()) {
            return null;
        }
        EquippableComponent equippableComponent = stack.get(DataComponentTypes.EQUIPPABLE);
        if (equippableComponent != null) {
            EquipmentSlot equipmentSlot = equippableComponent.slot();
            if (!slotBlacklist.contains(equipmentSlot)) {
                return equipmentSlot;
            }
        } else if (!slotBlacklist.contains(EquipmentSlot.MAINHAND)) {
            return EquipmentSlot.MAINHAND;
        }
        return null;
    }
}


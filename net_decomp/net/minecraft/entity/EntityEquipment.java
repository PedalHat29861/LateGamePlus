/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  com.mojang.serialization.Codec
 */
package net.minecraft.entity;

import com.mojang.serialization.Codec;
import java.util.EnumMap;
import java.util.Map;
import java.util.Objects;
import net.minecraft.entity.Entity;
import net.minecraft.entity.EquipmentSlot;
import net.minecraft.entity.LivingEntity;
import net.minecraft.item.ItemStack;

public class EntityEquipment {
    public static final Codec<EntityEquipment> CODEC = Codec.unboundedMap(EquipmentSlot.CODEC, ItemStack.CODEC).xmap(map -> {
        EnumMap<EquipmentSlot, ItemStack> enumMap = new EnumMap<EquipmentSlot, ItemStack>(EquipmentSlot.class);
        enumMap.putAll((Map<EquipmentSlot, ItemStack>)map);
        return new EntityEquipment(enumMap);
    }, equipment -> {
        EnumMap<EquipmentSlot, ItemStack> map = new EnumMap<EquipmentSlot, ItemStack>(equipment.map);
        map.values().removeIf(ItemStack::isEmpty);
        return map;
    });
    private final EnumMap<EquipmentSlot, ItemStack> map;

    private EntityEquipment(EnumMap<EquipmentSlot, ItemStack> map) {
        this.map = map;
    }

    public EntityEquipment() {
        this(new EnumMap<EquipmentSlot, ItemStack>(EquipmentSlot.class));
    }

    public ItemStack put(EquipmentSlot slot, ItemStack itemStack) {
        return Objects.requireNonNullElse(this.map.put(slot, itemStack), ItemStack.EMPTY);
    }

    public ItemStack get(EquipmentSlot slot) {
        return this.map.getOrDefault(slot, ItemStack.EMPTY);
    }

    public boolean isEmpty() {
        for (ItemStack itemStack : this.map.values()) {
            if (itemStack.isEmpty()) continue;
            return false;
        }
        return true;
    }

    public void tick(Entity entity) {
        for (Map.Entry<EquipmentSlot, ItemStack> entry : this.map.entrySet()) {
            ItemStack itemStack = entry.getValue();
            if (itemStack.isEmpty()) continue;
            itemStack.inventoryTick(entity.getEntityWorld(), entity, entry.getKey());
        }
    }

    public void copyFrom(EntityEquipment equipment) {
        this.map.clear();
        this.map.putAll(equipment.map);
    }

    public void dropAll(LivingEntity entity) {
        for (ItemStack itemStack : this.map.values()) {
            entity.dropItem(itemStack, true, false);
        }
        this.clear();
    }

    public void clear() {
        this.map.replaceAll((slot, stack) -> ItemStack.EMPTY);
    }
}


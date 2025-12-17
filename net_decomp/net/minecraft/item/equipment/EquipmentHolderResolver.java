/*
 * Decompiled with CFR 0.152.
 */
package net.minecraft.item.equipment;

import net.minecraft.entity.EntityType;
import net.minecraft.registry.RegistryEntryLookup;
import net.minecraft.registry.entry.RegistryEntryList;

@FunctionalInterface
public interface EquipmentHolderResolver {
    public RegistryEntryList<EntityType<?>> get(RegistryEntryLookup<EntityType<?>> var1);
}


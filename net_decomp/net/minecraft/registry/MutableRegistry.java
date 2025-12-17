/*
 * Decompiled with CFR 0.152.
 */
package net.minecraft.registry;

import java.util.List;
import net.minecraft.registry.Registry;
import net.minecraft.registry.RegistryEntryLookup;
import net.minecraft.registry.RegistryKey;
import net.minecraft.registry.entry.RegistryEntry;
import net.minecraft.registry.entry.RegistryEntryInfo;
import net.minecraft.registry.tag.TagKey;

public interface MutableRegistry<T>
extends Registry<T> {
    public RegistryEntry.Reference<T> add(RegistryKey<T> var1, T var2, RegistryEntryInfo var3);

    public void setEntries(TagKey<T> var1, List<RegistryEntry<T>> var2);

    public boolean isEmpty();

    public RegistryEntryLookup<T> createMutableRegistryLookup();
}


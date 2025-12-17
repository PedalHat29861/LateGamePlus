/*
 * Decompiled with CFR 0.152.
 */
package net.minecraft.registry;

import net.minecraft.registry.RegistryKey;

@FunctionalInterface
public interface RegistryKeyedValue<T, V> {
    public V get(RegistryKey<T> var1);

    public static <T, V> RegistryKeyedValue<T, V> fixed(V value) {
        return registryKey -> value;
    }
}


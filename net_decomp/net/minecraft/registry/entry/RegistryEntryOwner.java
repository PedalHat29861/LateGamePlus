/*
 * Decompiled with CFR 0.152.
 */
package net.minecraft.registry.entry;

public interface RegistryEntryOwner<T> {
    default public boolean ownerEquals(RegistryEntryOwner<T> other) {
        return other == this;
    }
}


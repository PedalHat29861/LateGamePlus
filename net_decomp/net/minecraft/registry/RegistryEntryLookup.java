/*
 * Decompiled with CFR 0.152.
 */
package net.minecraft.registry;

import java.util.Optional;
import net.minecraft.registry.Registry;
import net.minecraft.registry.RegistryKey;
import net.minecraft.registry.entry.RegistryEntry;
import net.minecraft.registry.entry.RegistryEntryList;
import net.minecraft.registry.tag.TagKey;
import net.minecraft.util.math.random.Random;

public interface RegistryEntryLookup<T> {
    public Optional<RegistryEntry.Reference<T>> getOptional(RegistryKey<T> var1);

    default public RegistryEntry.Reference<T> getOrThrow(RegistryKey<T> key) {
        return this.getOptional(key).orElseThrow(() -> new IllegalStateException("Missing element " + String.valueOf(key)));
    }

    public Optional<RegistryEntryList.Named<T>> getOptional(TagKey<T> var1);

    default public RegistryEntryList.Named<T> getOrThrow(TagKey<T> tag) {
        return this.getOptional(tag).orElseThrow(() -> new IllegalStateException("Missing tag " + String.valueOf(tag)));
    }

    default public Optional<RegistryEntry<T>> getRandomEntry(TagKey<T> tag, Random random) {
        return this.getOptional(tag).flatMap(entryList -> entryList.getRandom(random));
    }

    public static interface RegistryLookup {
        public <T> Optional<? extends RegistryEntryLookup<T>> getOptional(RegistryKey<? extends Registry<? extends T>> var1);

        default public <T> RegistryEntryLookup<T> getOrThrow(RegistryKey<? extends Registry<? extends T>> registryRef) {
            return this.getOptional(registryRef).orElseThrow(() -> new IllegalStateException("Registry " + String.valueOf(registryRef.getValue()) + " not found"));
        }

        default public <T> Optional<RegistryEntry.Reference<T>> getOptionalEntry(RegistryKey<T> registryRef) {
            return this.getOptional(registryRef.getRegistryRef()).flatMap(registryEntryLookup -> registryEntryLookup.getOptional(registryRef));
        }

        default public <T> RegistryEntry.Reference<T> getEntryOrThrow(RegistryKey<T> key) {
            return (RegistryEntry.Reference)this.getOptional(key.getRegistryRef()).flatMap(registry -> registry.getOptional(key)).orElseThrow(() -> new IllegalStateException("Missing element " + String.valueOf(key)));
        }
    }
}


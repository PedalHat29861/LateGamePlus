/*
 * Decompiled with CFR 0.152.
 */
package net.minecraft.entity;

import java.util.Optional;
import java.util.stream.Stream;
import net.minecraft.entity.VariantSelectorProvider;
import net.minecraft.entity.spawn.SpawnContext;
import net.minecraft.registry.DynamicRegistryManager;
import net.minecraft.registry.Registry;
import net.minecraft.registry.RegistryKey;
import net.minecraft.registry.RegistryWrapper;
import net.minecraft.registry.entry.RegistryEntry;
import net.minecraft.storage.ReadView;
import net.minecraft.storage.WriteView;
import net.minecraft.util.Identifier;
import net.minecraft.world.ServerWorldAccess;

public class Variants {
    public static final String VARIANT_NBT_KEY = "variant";

    public static <T> RegistryEntry<T> getOrDefaultOrThrow(DynamicRegistryManager registries, RegistryKey<T> variantKey) {
        RegistryWrapper.Impl registry = registries.getOrThrow(variantKey.getRegistryRef());
        return (RegistryEntry)registry.getOptional(variantKey).or(((Registry)registry)::getDefaultEntry).orElseThrow();
    }

    public static <T> RegistryEntry<T> getDefaultOrThrow(DynamicRegistryManager registries, RegistryKey<? extends Registry<T>> registryRef) {
        return registries.getOrThrow(registryRef).getDefaultEntry().orElseThrow();
    }

    public static <T> void writeData(WriteView view, RegistryEntry<T> variantEntry) {
        variantEntry.getKey().ifPresent(key -> view.put(VARIANT_NBT_KEY, Identifier.CODEC, key.getValue()));
    }

    public static <T> Optional<RegistryEntry<T>> fromData(ReadView view, RegistryKey<? extends Registry<T>> registryRef) {
        return view.read(VARIANT_NBT_KEY, Identifier.CODEC).map(id -> RegistryKey.of(registryRef, id)).flatMap(view.getRegistries()::getOptionalEntry);
    }

    public static <T extends VariantSelectorProvider<SpawnContext, ?>> Optional<RegistryEntry.Reference<T>> select(SpawnContext context, RegistryKey<Registry<T>> registryRef) {
        ServerWorldAccess serverWorldAccess = context.world();
        Stream stream = serverWorldAccess.getRegistryManager().getOrThrow(registryRef).streamEntries();
        return VariantSelectorProvider.select(stream, RegistryEntry::value, serverWorldAccess.getRandom(), context);
    }
}


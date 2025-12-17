/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  com.mojang.datafixers.util.Either
 *  com.mojang.serialization.Codec
 *  com.mojang.serialization.DataResult
 */
package net.minecraft.registry.entry;

import com.mojang.datafixers.util.Either;
import com.mojang.serialization.Codec;
import com.mojang.serialization.DataResult;
import java.util.Optional;
import java.util.function.Function;
import net.minecraft.network.RegistryByteBuf;
import net.minecraft.network.codec.PacketCodec;
import net.minecraft.network.codec.PacketCodecs;
import net.minecraft.registry.Registry;
import net.minecraft.registry.RegistryKey;
import net.minecraft.registry.RegistryWrapper;
import net.minecraft.registry.entry.RegistryEntry;

public record LazyRegistryEntryReference<T>(Either<RegistryEntry<T>, RegistryKey<T>> contents) {
    public LazyRegistryEntryReference(RegistryEntry<T> entry) {
        this(Either.left(entry));
    }

    public LazyRegistryEntryReference(RegistryKey<T> key) {
        this(Either.right(key));
    }

    public static <T> Codec<LazyRegistryEntryReference<T>> createCodec(RegistryKey<Registry<T>> registryRef, Codec<RegistryEntry<T>> entryCodec) {
        return Codec.either(entryCodec, (Codec)RegistryKey.createCodec(registryRef).comapFlatMap(registryKey -> DataResult.error(() -> "Cannot parse as key without registry"), Function.identity())).xmap(LazyRegistryEntryReference::new, LazyRegistryEntryReference::contents);
    }

    public static <T> PacketCodec<RegistryByteBuf, LazyRegistryEntryReference<T>> createPacketCodec(RegistryKey<Registry<T>> registryRef, PacketCodec<RegistryByteBuf, RegistryEntry<T>> entryPacketCodec) {
        return PacketCodec.tuple(PacketCodecs.either(entryPacketCodec, RegistryKey.createPacketCodec(registryRef)), LazyRegistryEntryReference::contents, LazyRegistryEntryReference::new);
    }

    public Optional<T> resolveValue(Registry<T> registry) {
        return (Optional)this.contents.map(entry -> Optional.of(entry.value()), registry::getOptionalValue);
    }

    public Optional<RegistryEntry<T>> resolveEntry(RegistryWrapper.WrapperLookup registries) {
        return (Optional)this.contents.map(Optional::of, key -> registries.getOptionalEntry(key).map(entry -> entry));
    }

    public Optional<RegistryKey<T>> getKey() {
        return (Optional)this.contents.map(RegistryEntry::getKey, Optional::of);
    }
}


/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  com.mojang.datafixers.util.Pair
 *  com.mojang.serialization.Codec
 *  com.mojang.serialization.DataResult
 *  com.mojang.serialization.DynamicOps
 *  com.mojang.serialization.Lifecycle
 */
package net.minecraft.registry.entry;

import com.mojang.datafixers.util.Pair;
import com.mojang.serialization.Codec;
import com.mojang.serialization.DataResult;
import com.mojang.serialization.DynamicOps;
import com.mojang.serialization.Lifecycle;
import java.util.Optional;
import net.minecraft.registry.Registry;
import net.minecraft.registry.RegistryEntryLookup;
import net.minecraft.registry.RegistryKey;
import net.minecraft.registry.RegistryOps;
import net.minecraft.registry.entry.RegistryEntry;
import net.minecraft.util.Identifier;

public final class RegistryElementCodec<E>
implements Codec<RegistryEntry<E>> {
    private final RegistryKey<? extends Registry<E>> registryRef;
    private final Codec<E> elementCodec;
    private final boolean allowInlineDefinitions;

    public static <E> RegistryElementCodec<E> of(RegistryKey<? extends Registry<E>> registryRef, Codec<E> elementCodec) {
        return RegistryElementCodec.of(registryRef, elementCodec, true);
    }

    public static <E> RegistryElementCodec<E> of(RegistryKey<? extends Registry<E>> registryRef, Codec<E> elementCodec, boolean allowInlineDefinitions) {
        return new RegistryElementCodec<E>(registryRef, elementCodec, allowInlineDefinitions);
    }

    private RegistryElementCodec(RegistryKey<? extends Registry<E>> registryRef, Codec<E> elementCodec, boolean allowInlineDefinitions) {
        this.registryRef = registryRef;
        this.elementCodec = elementCodec;
        this.allowInlineDefinitions = allowInlineDefinitions;
    }

    public <T> DataResult<T> encode(RegistryEntry<E> registryEntry, DynamicOps<T> dynamicOps, T object) {
        RegistryOps registryOps;
        Optional optional;
        if (dynamicOps instanceof RegistryOps && (optional = (registryOps = (RegistryOps)dynamicOps).getOwner(this.registryRef)).isPresent()) {
            if (!registryEntry.ownerEquals(optional.get())) {
                return DataResult.error(() -> "Element " + String.valueOf(registryEntry) + " is not valid in current registry set");
            }
            return (DataResult)registryEntry.getKeyOrValue().map(key -> Identifier.CODEC.encode((Object)key.getValue(), dynamicOps, object), value -> this.elementCodec.encode(value, dynamicOps, object));
        }
        return this.elementCodec.encode(registryEntry.value(), dynamicOps, object);
    }

    public <T> DataResult<Pair<RegistryEntry<E>, T>> decode(DynamicOps<T> ops, T input) {
        if (ops instanceof RegistryOps) {
            RegistryOps registryOps = (RegistryOps)ops;
            Optional optional = registryOps.getEntryLookup(this.registryRef);
            if (optional.isEmpty()) {
                return DataResult.error(() -> "Registry does not exist: " + String.valueOf(this.registryRef));
            }
            RegistryEntryLookup registryEntryLookup = optional.get();
            DataResult dataResult = Identifier.CODEC.decode(ops, input);
            if (dataResult.result().isEmpty()) {
                if (!this.allowInlineDefinitions) {
                    return DataResult.error(() -> "Inline definitions not allowed here");
                }
                return this.elementCodec.decode(ops, input).map(pair -> pair.mapFirst(RegistryEntry::of));
            }
            Pair pair2 = (Pair)dataResult.result().get();
            RegistryKey registryKey = RegistryKey.of(this.registryRef, (Identifier)pair2.getFirst());
            return registryEntryLookup.getOptional(registryKey).map(DataResult::success).orElseGet(() -> DataResult.error(() -> "Failed to get element " + String.valueOf(registryKey))).map(reference -> Pair.of((Object)reference, (Object)pair2.getSecond())).setLifecycle(Lifecycle.stable());
        }
        return this.elementCodec.decode(ops, input).map(pair -> pair.mapFirst(RegistryEntry::of));
    }

    public String toString() {
        return "RegistryFileCodec[" + String.valueOf(this.registryRef) + " " + String.valueOf(this.elementCodec) + "]";
    }

    public /* synthetic */ DataResult encode(Object input, DynamicOps ops, Object prefix) {
        return this.encode((RegistryEntry)input, ops, prefix);
    }
}


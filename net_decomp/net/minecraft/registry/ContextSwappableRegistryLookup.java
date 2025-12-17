/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  com.mojang.serialization.Codec
 *  com.mojang.serialization.DataResult
 *  com.mojang.serialization.DynamicOps
 *  com.mojang.serialization.JavaOps
 *  com.mojang.serialization.Lifecycle
 */
package net.minecraft.registry;

import com.mojang.serialization.Codec;
import com.mojang.serialization.DataResult;
import com.mojang.serialization.DynamicOps;
import com.mojang.serialization.JavaOps;
import com.mojang.serialization.Lifecycle;
import java.util.HashMap;
import java.util.Map;
import java.util.Optional;
import net.minecraft.registry.ContextSwapper;
import net.minecraft.registry.Registry;
import net.minecraft.registry.RegistryEntryLookup;
import net.minecraft.registry.RegistryKey;
import net.minecraft.registry.RegistryOps;
import net.minecraft.registry.RegistryWrapper;
import net.minecraft.registry.entry.RegistryEntry;
import net.minecraft.registry.entry.RegistryEntryList;
import net.minecraft.registry.entry.RegistryEntryOwner;
import net.minecraft.registry.tag.TagKey;

public class ContextSwappableRegistryLookup
implements RegistryEntryLookup.RegistryLookup {
    final RegistryWrapper.WrapperLookup delegate;
    final EntryLookupImpl entryLookupImpl = new EntryLookupImpl();
    final Map<RegistryKey<Object>, RegistryEntry.Reference<Object>> entries = new HashMap<RegistryKey<Object>, RegistryEntry.Reference<Object>>();
    final Map<TagKey<Object>, RegistryEntryList.Named<Object>> tags = new HashMap<TagKey<Object>, RegistryEntryList.Named<Object>>();

    public ContextSwappableRegistryLookup(RegistryWrapper.WrapperLookup delegate) {
        this.delegate = delegate;
    }

    @Override
    public <T> Optional<? extends RegistryEntryLookup<T>> getOptional(RegistryKey<? extends Registry<? extends T>> registryRef) {
        return Optional.of(this.entryLookupImpl.asEntryLookup());
    }

    public <V> RegistryOps<V> createRegistryOps(DynamicOps<V> delegateOps) {
        return RegistryOps.of(delegateOps, new RegistryOps.RegistryInfoGetter(){

            @Override
            public <T> Optional<RegistryOps.RegistryInfo<T>> getRegistryInfo(RegistryKey<? extends Registry<? extends T>> registryRef) {
                return ContextSwappableRegistryLookup.this.delegate.getOptional(registryRef).map(RegistryOps.RegistryInfo::fromWrapper).or(() -> Optional.of(new RegistryOps.RegistryInfo(ContextSwappableRegistryLookup.this.entryLookupImpl.asEntryOwner(), ContextSwappableRegistryLookup.this.entryLookupImpl.asEntryLookup(), Lifecycle.experimental())));
            }
        });
    }

    public ContextSwapper createContextSwapper() {
        return new ContextSwapper(){

            @Override
            public <T> DataResult<T> swapContext(Codec<T> codec, T value, RegistryWrapper.WrapperLookup registries) {
                return codec.encodeStart(ContextSwappableRegistryLookup.this.createRegistryOps(JavaOps.INSTANCE), value).flatMap(encodedValue -> codec.parse(registries.getOps(JavaOps.INSTANCE), encodedValue));
            }
        };
    }

    public boolean hasEntries() {
        return !this.entries.isEmpty() || !this.tags.isEmpty();
    }

    class EntryLookupImpl
    implements RegistryEntryLookup<Object>,
    RegistryEntryOwner<Object> {
        EntryLookupImpl() {
        }

        @Override
        public Optional<RegistryEntry.Reference<Object>> getOptional(RegistryKey<Object> key) {
            return Optional.of(this.getOrComputeEntry(key));
        }

        @Override
        public RegistryEntry.Reference<Object> getOrThrow(RegistryKey<Object> key) {
            return this.getOrComputeEntry(key);
        }

        private RegistryEntry.Reference<Object> getOrComputeEntry(RegistryKey<Object> key) {
            return ContextSwappableRegistryLookup.this.entries.computeIfAbsent(key, key2 -> RegistryEntry.Reference.standAlone(this, key2));
        }

        @Override
        public Optional<RegistryEntryList.Named<Object>> getOptional(TagKey<Object> tag) {
            return Optional.of(this.getOrComputeTag(tag));
        }

        @Override
        public RegistryEntryList.Named<Object> getOrThrow(TagKey<Object> tag) {
            return this.getOrComputeTag(tag);
        }

        private RegistryEntryList.Named<Object> getOrComputeTag(TagKey<Object> tag) {
            return ContextSwappableRegistryLookup.this.tags.computeIfAbsent(tag, tagKey -> RegistryEntryList.of(this, tagKey));
        }

        public <T> RegistryEntryLookup<T> asEntryLookup() {
            return this;
        }

        public <T> RegistryEntryOwner<T> asEntryOwner() {
            return this;
        }
    }
}


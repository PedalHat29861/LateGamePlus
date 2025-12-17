/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  com.mojang.datafixers.DataFixUtils
 *  com.mojang.serialization.Codec
 *  com.mojang.serialization.DataResult
 *  com.mojang.serialization.DynamicOps
 *  com.mojang.serialization.Keyable
 *  com.mojang.serialization.Lifecycle
 *  org.jspecify.annotations.Nullable
 */
package net.minecraft.registry;

import com.mojang.datafixers.DataFixUtils;
import com.mojang.serialization.Codec;
import com.mojang.serialization.DataResult;
import com.mojang.serialization.DynamicOps;
import com.mojang.serialization.Keyable;
import com.mojang.serialization.Lifecycle;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Set;
import java.util.stream.Stream;
import java.util.stream.StreamSupport;
import net.minecraft.registry.MutableRegistry;
import net.minecraft.registry.RegistryKey;
import net.minecraft.registry.RegistryWrapper;
import net.minecraft.registry.entry.RegistryEntry;
import net.minecraft.registry.entry.RegistryEntryInfo;
import net.minecraft.registry.entry.RegistryEntryList;
import net.minecraft.registry.tag.TagGroupLoader;
import net.minecraft.registry.tag.TagKey;
import net.minecraft.util.Identifier;
import net.minecraft.util.collection.IndexedIterable;
import net.minecraft.util.dynamic.Codecs;
import net.minecraft.util.math.random.Random;
import org.jspecify.annotations.Nullable;

public interface Registry<T>
extends Keyable,
RegistryWrapper.Impl<T>,
IndexedIterable<T> {
    @Override
    public RegistryKey<? extends Registry<T>> getKey();

    default public Codec<T> getCodec() {
        return this.getReferenceEntryCodec().flatComapMap(RegistryEntry.Reference::value, value -> this.validateReference(this.getEntry(value)));
    }

    default public Codec<RegistryEntry<T>> getEntryCodec() {
        return this.getReferenceEntryCodec().flatComapMap(entry -> entry, this::validateReference);
    }

    private Codec<RegistryEntry.Reference<T>> getReferenceEntryCodec() {
        Codec codec = Identifier.CODEC.comapFlatMap(id -> this.getEntry((Identifier)id).map(DataResult::success).orElseGet(() -> DataResult.error(() -> "Unknown registry key in " + String.valueOf(this.getKey()) + ": " + String.valueOf(id))), entry -> entry.registryKey().getValue());
        return Codecs.withLifecycle(codec, entry -> this.getEntryInfo(entry.registryKey()).map(RegistryEntryInfo::lifecycle).orElse(Lifecycle.experimental()));
    }

    private DataResult<RegistryEntry.Reference<T>> validateReference(RegistryEntry<T> entry) {
        DataResult dataResult;
        if (entry instanceof RegistryEntry.Reference) {
            RegistryEntry.Reference reference = (RegistryEntry.Reference)entry;
            dataResult = DataResult.success((Object)reference);
        } else {
            dataResult = DataResult.error(() -> "Unregistered holder in " + String.valueOf(this.getKey()) + ": " + String.valueOf(entry));
        }
        return dataResult;
    }

    default public <U> Stream<U> keys(DynamicOps<U> ops) {
        return this.getIds().stream().map(id -> ops.createString(id.toString()));
    }

    public @Nullable Identifier getId(T var1);

    public Optional<RegistryKey<T>> getKey(T var1);

    @Override
    public int getRawId(@Nullable T var1);

    public @Nullable T get(@Nullable RegistryKey<T> var1);

    public @Nullable T get(@Nullable Identifier var1);

    public Optional<RegistryEntryInfo> getEntryInfo(RegistryKey<T> var1);

    default public Optional<T> getOptionalValue(@Nullable Identifier id) {
        return Optional.ofNullable(this.get(id));
    }

    default public Optional<T> getOptionalValue(@Nullable RegistryKey<T> key) {
        return Optional.ofNullable(this.get(key));
    }

    public Optional<RegistryEntry.Reference<T>> getDefaultEntry();

    default public T getValueOrThrow(RegistryKey<T> key) {
        T object = this.get(key);
        if (object == null) {
            throw new IllegalStateException("Missing key in " + String.valueOf(this.getKey()) + ": " + String.valueOf(key));
        }
        return object;
    }

    public Set<Identifier> getIds();

    public Set<Map.Entry<RegistryKey<T>, T>> getEntrySet();

    public Set<RegistryKey<T>> getKeys();

    public Optional<RegistryEntry.Reference<T>> getRandom(Random var1);

    default public Stream<T> stream() {
        return StreamSupport.stream(this.spliterator(), false);
    }

    public boolean containsId(Identifier var1);

    public boolean contains(RegistryKey<T> var1);

    public static <T> T register(Registry<? super T> registry, String id, T entry) {
        return Registry.register(registry, Identifier.of(id), entry);
    }

    public static <V, T extends V> T register(Registry<V> registry, Identifier id, T entry) {
        return Registry.register(registry, RegistryKey.of(registry.getKey(), id), entry);
    }

    public static <V, T extends V> T register(Registry<V> registry, RegistryKey<V> key, T entry) {
        ((MutableRegistry)registry).add(key, entry, RegistryEntryInfo.DEFAULT);
        return entry;
    }

    public static <R, T extends R> RegistryEntry.Reference<T> registerReference(Registry<R> registry, RegistryKey<R> key, T entry) {
        return ((MutableRegistry)registry).add(key, entry, RegistryEntryInfo.DEFAULT);
    }

    public static <R, T extends R> RegistryEntry.Reference<T> registerReference(Registry<R> registry, Identifier id, T entry) {
        return Registry.registerReference(registry, RegistryKey.of(registry.getKey(), id), entry);
    }

    public Registry<T> freeze();

    public RegistryEntry.Reference<T> createEntry(T var1);

    public Optional<RegistryEntry.Reference<T>> getEntry(int var1);

    public Optional<RegistryEntry.Reference<T>> getEntry(Identifier var1);

    public RegistryEntry<T> getEntry(T var1);

    default public Iterable<RegistryEntry<T>> iterateEntries(TagKey<T> tag) {
        return (Iterable)DataFixUtils.orElse((Optional)this.getOptional(tag), List.of());
    }

    public Stream<RegistryEntryList.Named<T>> streamTags();

    default public IndexedIterable<RegistryEntry<T>> getIndexedEntries() {
        return new IndexedIterable<RegistryEntry<T>>(){

            @Override
            public int getRawId(RegistryEntry<T> registryEntry) {
                return Registry.this.getRawId(registryEntry.value());
            }

            @Override
            public @Nullable RegistryEntry<T> get(int i) {
                return Registry.this.getEntry(i).orElse(null);
            }

            @Override
            public int size() {
                return Registry.this.size();
            }

            @Override
            public Iterator<RegistryEntry<T>> iterator() {
                return Registry.this.streamEntries().map(entry -> entry).iterator();
            }

            @Override
            public /* synthetic */ @Nullable Object get(int index) {
                return this.get(index);
            }
        };
    }

    public PendingTagLoad<T> startTagReload(TagGroupLoader.RegistryTags<T> var1);

    public static interface PendingTagLoad<T> {
        public RegistryKey<? extends Registry<? extends T>> getKey();

        public RegistryWrapper.Impl<T> getLookup();

        public void apply();

        public int size();
    }
}


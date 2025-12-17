/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  com.google.common.collect.ImmutableMap
 *  com.google.common.collect.ImmutableMap$Builder
 *  com.google.common.collect.Iterators
 *  com.mojang.serialization.Lifecycle
 *  it.unimi.dsi.fastutil.objects.ObjectArrayList
 *  it.unimi.dsi.fastutil.objects.ObjectList
 *  it.unimi.dsi.fastutil.objects.Reference2IntMap
 *  it.unimi.dsi.fastutil.objects.Reference2IntOpenHashMap
 *  org.jspecify.annotations.Nullable
 */
package net.minecraft.registry;

import com.google.common.collect.ImmutableMap;
import com.google.common.collect.Iterators;
import com.mojang.serialization.Lifecycle;
import it.unimi.dsi.fastutil.objects.ObjectArrayList;
import it.unimi.dsi.fastutil.objects.ObjectList;
import it.unimi.dsi.fastutil.objects.Reference2IntMap;
import it.unimi.dsi.fastutil.objects.Reference2IntOpenHashMap;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.IdentityHashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;
import java.util.Set;
import java.util.function.BiConsumer;
import java.util.stream.Stream;
import net.minecraft.registry.MutableRegistry;
import net.minecraft.registry.Registry;
import net.minecraft.registry.RegistryEntryLookup;
import net.minecraft.registry.RegistryKey;
import net.minecraft.registry.RegistryWrapper;
import net.minecraft.registry.entry.RegistryEntry;
import net.minecraft.registry.entry.RegistryEntryInfo;
import net.minecraft.registry.entry.RegistryEntryList;
import net.minecraft.registry.tag.TagGroupLoader;
import net.minecraft.registry.tag.TagKey;
import net.minecraft.util.Identifier;
import net.minecraft.util.Util;
import net.minecraft.util.math.random.Random;
import org.jspecify.annotations.Nullable;

public class SimpleRegistry<T>
implements MutableRegistry<T> {
    private final RegistryKey<? extends Registry<T>> key;
    private final ObjectList<RegistryEntry.Reference<T>> rawIdToEntry = new ObjectArrayList(256);
    private final Reference2IntMap<T> entryToRawId = (Reference2IntMap)Util.make(new Reference2IntOpenHashMap(), map -> map.defaultReturnValue(-1));
    private final Map<Identifier, RegistryEntry.Reference<T>> idToEntry = new HashMap<Identifier, RegistryEntry.Reference<T>>();
    private final Map<RegistryKey<T>, RegistryEntry.Reference<T>> keyToEntry = new HashMap<RegistryKey<T>, RegistryEntry.Reference<T>>();
    private final Map<T, RegistryEntry.Reference<T>> valueToEntry = new IdentityHashMap<T, RegistryEntry.Reference<T>>();
    private final Map<RegistryKey<T>, RegistryEntryInfo> keyToEntryInfo = new IdentityHashMap<RegistryKey<T>, RegistryEntryInfo>();
    private Lifecycle lifecycle;
    private final Map<TagKey<T>, RegistryEntryList.Named<T>> tags = new IdentityHashMap<TagKey<T>, RegistryEntryList.Named<T>>();
    TagLookup<T> tagLookup = TagLookup.ofUnbound();
    private boolean frozen;
    private @Nullable Map<T, RegistryEntry.Reference<T>> intrusiveValueToEntry;

    @Override
    public Stream<RegistryEntryList.Named<T>> getTags() {
        return this.streamTags();
    }

    public SimpleRegistry(RegistryKey<? extends Registry<T>> key, Lifecycle lifecycle) {
        this(key, lifecycle, false);
    }

    public SimpleRegistry(RegistryKey<? extends Registry<T>> key, Lifecycle lifecycle, boolean intrusive) {
        this.key = key;
        this.lifecycle = lifecycle;
        if (intrusive) {
            this.intrusiveValueToEntry = new IdentityHashMap<T, RegistryEntry.Reference<T>>();
        }
    }

    @Override
    public RegistryKey<? extends Registry<T>> getKey() {
        return this.key;
    }

    public String toString() {
        return "Registry[" + String.valueOf(this.key) + " (" + String.valueOf(this.lifecycle) + ")]";
    }

    private void assertNotFrozen() {
        if (this.frozen) {
            throw new IllegalStateException("Registry is already frozen");
        }
    }

    private void assertNotFrozen(RegistryKey<T> key) {
        if (this.frozen) {
            throw new IllegalStateException("Registry is already frozen (trying to add key " + String.valueOf(key) + ")");
        }
    }

    @Override
    public RegistryEntry.Reference<T> add(RegistryKey<T> key, T value, RegistryEntryInfo info) {
        RegistryEntry.Reference reference;
        this.assertNotFrozen(key);
        Objects.requireNonNull(key);
        Objects.requireNonNull(value);
        if (this.idToEntry.containsKey(key.getValue())) {
            throw Util.getFatalOrPause(new IllegalStateException("Adding duplicate key '" + String.valueOf(key) + "' to registry"));
        }
        if (this.valueToEntry.containsKey(value)) {
            throw Util.getFatalOrPause(new IllegalStateException("Adding duplicate value '" + String.valueOf(value) + "' to registry"));
        }
        if (this.intrusiveValueToEntry != null) {
            reference = this.intrusiveValueToEntry.remove(value);
            if (reference == null) {
                throw new AssertionError((Object)("Missing intrusive holder for " + String.valueOf(key) + ":" + String.valueOf(value)));
            }
            reference.setRegistryKey(key);
        } else {
            reference = this.keyToEntry.computeIfAbsent(key, k -> RegistryEntry.Reference.standAlone(this, k));
        }
        this.keyToEntry.put(key, reference);
        this.idToEntry.put(key.getValue(), reference);
        this.valueToEntry.put(value, reference);
        int i = this.rawIdToEntry.size();
        this.rawIdToEntry.add((Object)reference);
        this.entryToRawId.put(value, i);
        this.keyToEntryInfo.put(key, info);
        this.lifecycle = this.lifecycle.add(info.lifecycle());
        return reference;
    }

    @Override
    public @Nullable Identifier getId(T value) {
        RegistryEntry.Reference<T> reference = this.valueToEntry.get(value);
        return reference != null ? reference.registryKey().getValue() : null;
    }

    @Override
    public Optional<RegistryKey<T>> getKey(T entry) {
        return Optional.ofNullable(this.valueToEntry.get(entry)).map(RegistryEntry.Reference::registryKey);
    }

    @Override
    public int getRawId(@Nullable T value) {
        return this.entryToRawId.getInt(value);
    }

    @Override
    public @Nullable T get(@Nullable RegistryKey<T> key) {
        return SimpleRegistry.getValue(this.keyToEntry.get(key));
    }

    @Override
    public @Nullable T get(int index) {
        if (index < 0 || index >= this.rawIdToEntry.size()) {
            return null;
        }
        return ((RegistryEntry.Reference)this.rawIdToEntry.get(index)).value();
    }

    @Override
    public Optional<RegistryEntry.Reference<T>> getEntry(int rawId) {
        if (rawId < 0 || rawId >= this.rawIdToEntry.size()) {
            return Optional.empty();
        }
        return Optional.ofNullable((RegistryEntry.Reference)this.rawIdToEntry.get(rawId));
    }

    @Override
    public Optional<RegistryEntry.Reference<T>> getEntry(Identifier id) {
        return Optional.ofNullable(this.idToEntry.get(id));
    }

    @Override
    public Optional<RegistryEntry.Reference<T>> getOptional(RegistryKey<T> key) {
        return Optional.ofNullable(this.keyToEntry.get(key));
    }

    @Override
    public Optional<RegistryEntry.Reference<T>> getDefaultEntry() {
        return this.rawIdToEntry.isEmpty() ? Optional.empty() : Optional.of((RegistryEntry.Reference)this.rawIdToEntry.getFirst());
    }

    @Override
    public RegistryEntry<T> getEntry(T value) {
        RegistryEntry.Reference<T> reference = this.valueToEntry.get(value);
        return reference != null ? reference : RegistryEntry.of(value);
    }

    RegistryEntry.Reference<T> getOrCreateEntry(RegistryKey<T> key) {
        return this.keyToEntry.computeIfAbsent(key, key2 -> {
            if (this.intrusiveValueToEntry != null) {
                throw new IllegalStateException("This registry can't create new holders without value");
            }
            this.assertNotFrozen((RegistryKey<T>)key2);
            return RegistryEntry.Reference.standAlone(this, key2);
        });
    }

    @Override
    public int size() {
        return this.keyToEntry.size();
    }

    @Override
    public Optional<RegistryEntryInfo> getEntryInfo(RegistryKey<T> key) {
        return Optional.ofNullable(this.keyToEntryInfo.get(key));
    }

    @Override
    public Lifecycle getLifecycle() {
        return this.lifecycle;
    }

    @Override
    public Iterator<T> iterator() {
        return Iterators.transform((Iterator)this.rawIdToEntry.iterator(), RegistryEntry::value);
    }

    @Override
    public @Nullable T get(@Nullable Identifier id) {
        RegistryEntry.Reference<T> reference = this.idToEntry.get(id);
        return SimpleRegistry.getValue(reference);
    }

    private static <T> @Nullable T getValue(@Nullable RegistryEntry.Reference<T> entry) {
        return entry != null ? (T)entry.value() : null;
    }

    @Override
    public Set<Identifier> getIds() {
        return Collections.unmodifiableSet(this.idToEntry.keySet());
    }

    @Override
    public Set<RegistryKey<T>> getKeys() {
        return Collections.unmodifiableSet(this.keyToEntry.keySet());
    }

    @Override
    public Set<Map.Entry<RegistryKey<T>, T>> getEntrySet() {
        return Collections.unmodifiableSet(Util.transformMapValuesLazy(this.keyToEntry, RegistryEntry::value).entrySet());
    }

    @Override
    public Stream<RegistryEntry.Reference<T>> streamEntries() {
        return this.rawIdToEntry.stream();
    }

    @Override
    public Stream<RegistryEntryList.Named<T>> streamTags() {
        return this.tagLookup.stream();
    }

    RegistryEntryList.Named<T> getTag(TagKey<T> key) {
        return this.tags.computeIfAbsent(key, this::createNamedEntryList);
    }

    private RegistryEntryList.Named<T> createNamedEntryList(TagKey<T> tag) {
        return new RegistryEntryList.Named<T>(this, tag);
    }

    @Override
    public boolean isEmpty() {
        return this.keyToEntry.isEmpty();
    }

    @Override
    public Optional<RegistryEntry.Reference<T>> getRandom(Random random) {
        return Util.getRandomOrEmpty(this.rawIdToEntry, random);
    }

    @Override
    public boolean containsId(Identifier id) {
        return this.idToEntry.containsKey(id);
    }

    @Override
    public boolean contains(RegistryKey<T> key) {
        return this.keyToEntry.containsKey(key);
    }

    @Override
    public Registry<T> freeze() {
        if (this.frozen) {
            return this;
        }
        this.frozen = true;
        this.valueToEntry.forEach((? super K value, ? super V entry) -> entry.setValue(value));
        List<Identifier> list = this.keyToEntry.entrySet().stream().filter(entry -> !((RegistryEntry.Reference)entry.getValue()).hasKeyAndValue()).map(entry -> ((RegistryKey)entry.getKey()).getValue()).sorted().toList();
        if (!list.isEmpty()) {
            throw new IllegalStateException("Unbound values in registry " + String.valueOf(this.getKey()) + ": " + String.valueOf(list));
        }
        if (this.intrusiveValueToEntry != null) {
            if (!this.intrusiveValueToEntry.isEmpty()) {
                throw new IllegalStateException("Some intrusive holders were not registered: " + String.valueOf(this.intrusiveValueToEntry.values()));
            }
            this.intrusiveValueToEntry = null;
        }
        if (this.tagLookup.isBound()) {
            throw new IllegalStateException("Tags already present before freezing");
        }
        List<Identifier> list2 = this.tags.entrySet().stream().filter(entry -> !((RegistryEntryList.Named)entry.getValue()).isBound()).map(entry -> ((TagKey)entry.getKey()).id()).sorted().toList();
        if (!list2.isEmpty()) {
            throw new IllegalStateException("Unbound tags in registry " + String.valueOf(this.getKey()) + ": " + String.valueOf(list2));
        }
        this.tagLookup = TagLookup.fromMap(this.tags);
        this.refreshTags();
        return this;
    }

    @Override
    public RegistryEntry.Reference<T> createEntry(T value) {
        if (this.intrusiveValueToEntry == null) {
            throw new IllegalStateException("This registry can't create intrusive holders");
        }
        this.assertNotFrozen();
        return this.intrusiveValueToEntry.computeIfAbsent(value, valuex -> RegistryEntry.Reference.intrusive(this, valuex));
    }

    @Override
    public Optional<RegistryEntryList.Named<T>> getOptional(TagKey<T> tag) {
        return this.tagLookup.getOptional(tag);
    }

    private RegistryEntry.Reference<T> ensureTagable(TagKey<T> key, RegistryEntry<T> entry) {
        if (!entry.ownerEquals(this)) {
            throw new IllegalStateException("Can't create named set " + String.valueOf(key) + " containing value " + String.valueOf(entry) + " from outside registry " + String.valueOf(this));
        }
        if (entry instanceof RegistryEntry.Reference) {
            RegistryEntry.Reference reference = (RegistryEntry.Reference)entry;
            return reference;
        }
        throw new IllegalStateException("Found direct holder " + String.valueOf(entry) + " value in tag " + String.valueOf(key));
    }

    @Override
    public void setEntries(TagKey<T> tag, List<RegistryEntry<T>> entries) {
        this.assertNotFrozen();
        this.getTag(tag).setEntries(entries);
    }

    void refreshTags() {
        IdentityHashMap<RegistryEntry.Reference, List> map = new IdentityHashMap<RegistryEntry.Reference, List>();
        this.keyToEntry.values().forEach(key -> map.put((RegistryEntry.Reference)key, new ArrayList()));
        this.tagLookup.forEach((? super TagKey<T> key, ? super RegistryEntryList.Named<T> value) -> {
            for (RegistryEntry registryEntry : value) {
                RegistryEntry.Reference reference = this.ensureTagable((TagKey<T>)key, registryEntry);
                ((List)map.get(reference)).add(key);
            }
        });
        map.forEach(RegistryEntry.Reference::setTags);
    }

    public void resetTagEntries() {
        this.assertNotFrozen();
        this.tags.values().forEach(tag -> tag.setEntries(List.of()));
    }

    @Override
    public RegistryEntryLookup<T> createMutableRegistryLookup() {
        this.assertNotFrozen();
        return new RegistryEntryLookup<T>(){

            @Override
            public Optional<RegistryEntry.Reference<T>> getOptional(RegistryKey<T> key) {
                return Optional.of(this.getOrThrow(key));
            }

            @Override
            public RegistryEntry.Reference<T> getOrThrow(RegistryKey<T> key) {
                return SimpleRegistry.this.getOrCreateEntry(key);
            }

            @Override
            public Optional<RegistryEntryList.Named<T>> getOptional(TagKey<T> tag) {
                return Optional.of(this.getOrThrow(tag));
            }

            @Override
            public RegistryEntryList.Named<T> getOrThrow(TagKey<T> tag) {
                return SimpleRegistry.this.getTag(tag);
            }
        };
    }

    @Override
    public Registry.PendingTagLoad<T> startTagReload(TagGroupLoader.RegistryTags<T> tags) {
        if (!this.frozen) {
            throw new IllegalStateException("Invalid method used for tag loading");
        }
        ImmutableMap.Builder builder = ImmutableMap.builder();
        final HashMap map = new HashMap();
        tags.tags().forEach((? super K key, ? super V values) -> {
            RegistryEntryList.Named<T> named = this.tags.get(key);
            if (named == null) {
                named = this.createNamedEntryList((TagKey<T>)key);
            }
            builder.put(key, named);
            map.put(key, List.copyOf(values));
        });
        final ImmutableMap immutableMap = builder.build();
        final RegistryWrapper.Impl.Delegating impl = new RegistryWrapper.Impl.Delegating<T>(){

            @Override
            public RegistryWrapper.Impl<T> getBase() {
                return SimpleRegistry.this;
            }

            @Override
            public Optional<RegistryEntryList.Named<T>> getOptional(TagKey<T> tag) {
                return Optional.ofNullable((RegistryEntryList.Named)immutableMap.get(tag));
            }

            @Override
            public Stream<RegistryEntryList.Named<T>> getTags() {
                return immutableMap.values().stream();
            }
        };
        return new Registry.PendingTagLoad<T>(){

            @Override
            public RegistryKey<? extends Registry<? extends T>> getKey() {
                return SimpleRegistry.this.getKey();
            }

            @Override
            public int size() {
                return map.size();
            }

            @Override
            public RegistryWrapper.Impl<T> getLookup() {
                return impl;
            }

            @Override
            public void apply() {
                immutableMap.forEach((tagKey, named) -> {
                    List list = map.getOrDefault(tagKey, List.of());
                    named.setEntries(list);
                });
                SimpleRegistry.this.tagLookup = TagLookup.fromMap(immutableMap);
                SimpleRegistry.this.refreshTags();
            }
        };
    }

    static interface TagLookup<T> {
        public static <T> TagLookup<T> ofUnbound() {
            return new TagLookup<T>(){

                @Override
                public boolean isBound() {
                    return false;
                }

                @Override
                public Optional<RegistryEntryList.Named<T>> getOptional(TagKey<T> key) {
                    throw new IllegalStateException("Tags not bound, trying to access " + String.valueOf(key));
                }

                @Override
                public void forEach(BiConsumer<? super TagKey<T>, ? super RegistryEntryList.Named<T>> consumer) {
                    throw new IllegalStateException("Tags not bound");
                }

                @Override
                public Stream<RegistryEntryList.Named<T>> stream() {
                    throw new IllegalStateException("Tags not bound");
                }
            };
        }

        public static <T> TagLookup<T> fromMap(final Map<TagKey<T>, RegistryEntryList.Named<T>> map) {
            return new TagLookup<T>(){

                @Override
                public boolean isBound() {
                    return true;
                }

                @Override
                public Optional<RegistryEntryList.Named<T>> getOptional(TagKey<T> key) {
                    return Optional.ofNullable((RegistryEntryList.Named)map.get(key));
                }

                @Override
                public void forEach(BiConsumer<? super TagKey<T>, ? super RegistryEntryList.Named<T>> consumer) {
                    map.forEach(consumer);
                }

                @Override
                public Stream<RegistryEntryList.Named<T>> stream() {
                    return map.values().stream();
                }
            };
        }

        public boolean isBound();

        public Optional<RegistryEntryList.Named<T>> getOptional(TagKey<T> var1);

        public void forEach(BiConsumer<? super TagKey<T>, ? super RegistryEntryList.Named<T>> var1);

        public Stream<RegistryEntryList.Named<T>> stream();
    }
}


/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  com.google.gson.JsonElement
 *  com.mojang.datafixers.util.Either
 *  com.mojang.logging.LogUtils
 *  com.mojang.serialization.Dynamic
 *  com.mojang.serialization.DynamicOps
 *  com.mojang.serialization.JsonOps
 *  org.jspecify.annotations.Nullable
 *  org.slf4j.Logger
 */
package net.minecraft.registry.tag;

import com.google.gson.JsonElement;
import com.mojang.datafixers.util.Either;
import com.mojang.logging.LogUtils;
import com.mojang.serialization.Dynamic;
import com.mojang.serialization.DynamicOps;
import com.mojang.serialization.JsonOps;
import java.io.BufferedReader;
import java.io.Reader;
import java.lang.invoke.MethodHandle;
import java.lang.runtime.ObjectMethods;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;
import java.util.function.Consumer;
import java.util.stream.Collectors;
import net.minecraft.registry.DynamicRegistryManager;
import net.minecraft.registry.MutableRegistry;
import net.minecraft.registry.Registry;
import net.minecraft.registry.RegistryEntryLookup;
import net.minecraft.registry.RegistryKey;
import net.minecraft.registry.RegistryKeys;
import net.minecraft.registry.RegistryWrapper;
import net.minecraft.registry.entry.RegistryEntry;
import net.minecraft.registry.tag.TagEntry;
import net.minecraft.registry.tag.TagFile;
import net.minecraft.registry.tag.TagKey;
import net.minecraft.registry.tag.TagPacketSerializer;
import net.minecraft.resource.DependencyTracker;
import net.minecraft.resource.Resource;
import net.minecraft.resource.ResourceFinder;
import net.minecraft.resource.ResourceManager;
import net.minecraft.util.Identifier;
import net.minecraft.util.StrictJsonParser;
import org.jspecify.annotations.Nullable;
import org.slf4j.Logger;

public class TagGroupLoader<T> {
    private static final Logger LOGGER = LogUtils.getLogger();
    final EntrySupplier<T> entrySupplier;
    private final String dataType;

    public TagGroupLoader(EntrySupplier<T> entrySupplier, String dataType) {
        this.entrySupplier = entrySupplier;
        this.dataType = dataType;
    }

    public Map<Identifier, List<TrackedEntry>> loadTags(ResourceManager resourceManager) {
        HashMap<Identifier, List<TrackedEntry>> map = new HashMap<Identifier, List<TrackedEntry>>();
        ResourceFinder resourceFinder = ResourceFinder.json(this.dataType);
        for (Map.Entry<Identifier, List<Resource>> entry2 : resourceFinder.findAllResources(resourceManager).entrySet()) {
            Identifier identifier = entry2.getKey();
            Identifier identifier2 = resourceFinder.toResourceId(identifier);
            for (Resource resource : entry2.getValue()) {
                try {
                    BufferedReader reader = resource.getReader();
                    try {
                        JsonElement jsonElement = StrictJsonParser.parse(reader);
                        List list = map.computeIfAbsent(identifier2, id -> new ArrayList());
                        TagFile tagFile = (TagFile)TagFile.CODEC.parse(new Dynamic((DynamicOps)JsonOps.INSTANCE, (Object)jsonElement)).getOrThrow();
                        if (tagFile.replace()) {
                            list.clear();
                        }
                        String string = resource.getPackId();
                        tagFile.entries().forEach(entry -> list.add(new TrackedEntry((TagEntry)entry, string)));
                    }
                    finally {
                        if (reader == null) continue;
                        ((Reader)reader).close();
                    }
                }
                catch (Exception exception) {
                    LOGGER.error("Couldn't read tag list {} from {} in data pack {}", new Object[]{identifier2, identifier, resource.getPackId(), exception});
                }
            }
        }
        return map;
    }

    private Either<List<TrackedEntry>, List<T>> resolveAll(TagEntry.ValueGetter<T> valueGetter, List<TrackedEntry> entries) {
        LinkedHashSet sequencedSet = new LinkedHashSet();
        ArrayList<TrackedEntry> list = new ArrayList<TrackedEntry>();
        for (TrackedEntry trackedEntry : entries) {
            if (trackedEntry.entry().resolve(valueGetter, sequencedSet::add)) continue;
            list.add(trackedEntry);
        }
        return list.isEmpty() ? Either.right(List.copyOf(sequencedSet)) : Either.left(list);
    }

    public Map<Identifier, List<T>> buildGroup(Map<Identifier, List<TrackedEntry>> tags) {
        final HashMap map = new HashMap();
        TagEntry.ValueGetter valueGetter = new TagEntry.ValueGetter<T>(){

            @Override
            public @Nullable T direct(Identifier id, boolean required) {
                return TagGroupLoader.this.entrySupplier.get(id, required).orElse(null);
            }

            @Override
            public @Nullable Collection<T> tag(Identifier id) {
                return (Collection)map.get(id);
            }
        };
        DependencyTracker<Identifier, TagDependencies> dependencyTracker = new DependencyTracker<Identifier, TagDependencies>();
        tags.forEach((id, entries) -> dependencyTracker.add((Identifier)id, new TagDependencies((List<TrackedEntry>)entries)));
        dependencyTracker.traverse((id, dependencies) -> this.resolveAll(valueGetter, dependencies.entries).ifLeft(missingReferences -> LOGGER.error("Couldn't load tag {} as it is missing following references: {}", id, (Object)missingReferences.stream().map(Objects::toString).collect(Collectors.joining(", ")))).ifRight(values -> map.put((Identifier)id, (List)values)));
        return map;
    }

    public static <T> void loadFromNetwork(TagPacketSerializer.Serialized tags, MutableRegistry<T> registry) {
        tags.toRegistryTags(registry).tags.forEach(registry::setEntries);
    }

    public static List<Registry.PendingTagLoad<?>> startReload(ResourceManager resourceManager, DynamicRegistryManager registryManager) {
        return registryManager.streamAllRegistries().map(registry -> TagGroupLoader.startReload(resourceManager, registry.value())).flatMap(Optional::stream).collect(Collectors.toUnmodifiableList());
    }

    public static <T> void loadInitial(ResourceManager resourceManager, MutableRegistry<T> registry) {
        RegistryKey registryKey = registry.getKey();
        TagGroupLoader<RegistryEntry<T>> tagGroupLoader = new TagGroupLoader<RegistryEntry<T>>(EntrySupplier.forInitial(registry), RegistryKeys.getTagPath(registryKey));
        tagGroupLoader.buildGroup(tagGroupLoader.loadTags(resourceManager)).forEach((id, entries) -> registry.setEntries(TagKey.of(registryKey, id), (List)entries));
    }

    private static <T> Map<TagKey<T>, List<RegistryEntry<T>>> toTagKeyedMap(RegistryKey<? extends Registry<T>> registryRef, Map<Identifier, List<RegistryEntry<T>>> tags) {
        return tags.entrySet().stream().collect(Collectors.toUnmodifiableMap(entry -> TagKey.of(registryRef, (Identifier)entry.getKey()), Map.Entry::getValue));
    }

    private static <T> Optional<Registry.PendingTagLoad<T>> startReload(ResourceManager resourceManager, Registry<T> registry) {
        RegistryKey<Registry<T>> registryKey = registry.getKey();
        TagGroupLoader<RegistryEntry<T>> tagGroupLoader = new TagGroupLoader<RegistryEntry<T>>(EntrySupplier.forReload(registry), RegistryKeys.getTagPath(registryKey));
        RegistryTags<T> registryTags = new RegistryTags<T>(registryKey, TagGroupLoader.toTagKeyedMap(registry.getKey(), tagGroupLoader.buildGroup(tagGroupLoader.loadTags(resourceManager))));
        return registryTags.tags().isEmpty() ? Optional.empty() : Optional.of(registry.startTagReload(registryTags));
    }

    public static List<RegistryWrapper.Impl<?>> collectRegistries(DynamicRegistryManager.Immutable registryManager, List<Registry.PendingTagLoad<?>> tagLoads) {
        ArrayList list = new ArrayList();
        registryManager.streamAllRegistries().forEach(registry -> {
            Registry.PendingTagLoad pendingTagLoad = TagGroupLoader.find(tagLoads, registry.key());
            list.add(pendingTagLoad != null ? pendingTagLoad.getLookup() : registry.value());
        });
        return list;
    }

    private static @Nullable Registry.PendingTagLoad<?> find(List<Registry.PendingTagLoad<?>> pendingTags, RegistryKey<? extends Registry<?>> registryRef) {
        for (Registry.PendingTagLoad<?> pendingTagLoad : pendingTags) {
            if (pendingTagLoad.getKey() != registryRef) continue;
            return pendingTagLoad;
        }
        return null;
    }

    public static interface EntrySupplier<T> {
        public Optional<? extends T> get(Identifier var1, boolean var2);

        public static <T> EntrySupplier<? extends RegistryEntry<T>> forReload(Registry<T> registry) {
            return (id, required) -> registry.getEntry(id);
        }

        public static <T> EntrySupplier<RegistryEntry<T>> forInitial(MutableRegistry<T> registry) {
            RegistryEntryLookup registryEntryLookup = registry.createMutableRegistryLookup();
            return (id, required) -> (required ? registryEntryLookup : registry).getOptional(RegistryKey.of(registry.getKey(), id));
        }
    }

    public static final class TrackedEntry
    extends Record {
        final TagEntry entry;
        private final String source;

        public TrackedEntry(TagEntry entry, String source) {
            this.entry = entry;
            this.source = source;
        }

        @Override
        public String toString() {
            return String.valueOf(this.entry) + " (from " + this.source + ")";
        }

        @Override
        public final int hashCode() {
            return (int)ObjectMethods.bootstrap("hashCode", new MethodHandle[]{TrackedEntry.class, "entry;source", "entry", "source"}, this);
        }

        @Override
        public final boolean equals(Object object) {
            return (boolean)ObjectMethods.bootstrap("equals", new MethodHandle[]{TrackedEntry.class, "entry;source", "entry", "source"}, this, object);
        }

        public TagEntry entry() {
            return this.entry;
        }

        public String source() {
            return this.source;
        }
    }

    public static final class RegistryTags<T>
    extends Record {
        private final RegistryKey<? extends Registry<T>> key;
        final Map<TagKey<T>, List<RegistryEntry<T>>> tags;

        public RegistryTags(RegistryKey<? extends Registry<T>> key, Map<TagKey<T>, List<RegistryEntry<T>>> tags) {
            this.key = key;
            this.tags = tags;
        }

        @Override
        public final String toString() {
            return ObjectMethods.bootstrap("toString", new MethodHandle[]{RegistryTags.class, "key;tags", "key", "tags"}, this);
        }

        @Override
        public final int hashCode() {
            return (int)ObjectMethods.bootstrap("hashCode", new MethodHandle[]{RegistryTags.class, "key;tags", "key", "tags"}, this);
        }

        @Override
        public final boolean equals(Object object) {
            return (boolean)ObjectMethods.bootstrap("equals", new MethodHandle[]{RegistryTags.class, "key;tags", "key", "tags"}, this, object);
        }

        public RegistryKey<? extends Registry<T>> key() {
            return this.key;
        }

        public Map<TagKey<T>, List<RegistryEntry<T>>> tags() {
            return this.tags;
        }
    }

    static final class TagDependencies
    extends Record
    implements DependencyTracker.Dependencies<Identifier> {
        final List<TrackedEntry> entries;

        TagDependencies(List<TrackedEntry> entries) {
            this.entries = entries;
        }

        @Override
        public void forDependencies(Consumer<Identifier> callback) {
            this.entries.forEach(entry -> entry.entry.forEachRequiredTagId(callback));
        }

        @Override
        public void forOptionalDependencies(Consumer<Identifier> callback) {
            this.entries.forEach(entry -> entry.entry.forEachOptionalTagId(callback));
        }

        @Override
        public final String toString() {
            return ObjectMethods.bootstrap("toString", new MethodHandle[]{TagDependencies.class, "entries", "entries"}, this);
        }

        @Override
        public final int hashCode() {
            return (int)ObjectMethods.bootstrap("hashCode", new MethodHandle[]{TagDependencies.class, "entries", "entries"}, this);
        }

        @Override
        public final boolean equals(Object object) {
            return (boolean)ObjectMethods.bootstrap("equals", new MethodHandle[]{TagDependencies.class, "entries", "entries"}, this, object);
        }

        public List<TrackedEntry> entries() {
            return this.entries;
        }
    }
}


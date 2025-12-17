/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  com.google.common.collect.ImmutableSet
 *  com.google.common.collect.Lists
 *  com.google.common.collect.Maps
 *  com.mojang.logging.LogUtils
 *  it.unimi.dsi.fastutil.objects.Object2ObjectArrayMap
 *  org.jspecify.annotations.Nullable
 *  org.slf4j.Logger
 */
package net.minecraft.resource;

import com.google.common.collect.ImmutableSet;
import com.google.common.collect.Lists;
import com.google.common.collect.Maps;
import com.mojang.logging.LogUtils;
import it.unimi.dsi.fastutil.objects.Object2ObjectArrayMap;
import java.io.FilterInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.PrintWriter;
import java.io.StringWriter;
import java.lang.invoke.LambdaMetafactory;
import java.lang.invoke.MethodHandle;
import java.lang.runtime.ObjectMethods;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;
import java.util.Set;
import java.util.TreeMap;
import java.util.function.Function;
import java.util.function.Predicate;
import java.util.function.Supplier;
import java.util.stream.Stream;
import net.minecraft.resource.InputSupplier;
import net.minecraft.resource.Resource;
import net.minecraft.resource.ResourceManager;
import net.minecraft.resource.ResourcePack;
import net.minecraft.resource.ResourceType;
import net.minecraft.resource.metadata.ResourceMetadata;
import net.minecraft.util.Identifier;
import org.jspecify.annotations.Nullable;
import org.slf4j.Logger;

public class NamespaceResourceManager
implements ResourceManager {
    static final Logger LOGGER = LogUtils.getLogger();
    protected final List<FilterablePack> packList = Lists.newArrayList();
    private final ResourceType type;
    private final String namespace;

    public NamespaceResourceManager(ResourceType type, String namespace) {
        this.type = type;
        this.namespace = namespace;
    }

    public void addPack(ResourcePack pack) {
        this.addPack(pack.getId(), pack, null);
    }

    public void addPack(ResourcePack pack, Predicate<Identifier> filter) {
        this.addPack(pack.getId(), pack, filter);
    }

    public void addPack(String id, Predicate<Identifier> filter) {
        this.addPack(id, null, filter);
    }

    private void addPack(String id, @Nullable ResourcePack underlyingPack, @Nullable Predicate<Identifier> filter) {
        this.packList.add(new FilterablePack(id, underlyingPack, filter));
    }

    @Override
    public Set<String> getAllNamespaces() {
        return ImmutableSet.of((Object)this.namespace);
    }

    @Override
    public Optional<Resource> getResource(Identifier identifier) {
        for (int i = this.packList.size() - 1; i >= 0; --i) {
            InputSupplier<InputStream> inputSupplier;
            FilterablePack filterablePack = this.packList.get(i);
            ResourcePack resourcePack = filterablePack.underlying;
            if (resourcePack != null && (inputSupplier = resourcePack.open(this.type, identifier)) != null) {
                InputSupplier<ResourceMetadata> inputSupplier2 = this.createMetadataSupplier(identifier, i);
                return Optional.of(NamespaceResourceManager.createResource(resourcePack, identifier, inputSupplier, inputSupplier2));
            }
            if (!filterablePack.isFiltered(identifier)) continue;
            LOGGER.warn("Resource {} not found, but was filtered by pack {}", (Object)identifier, (Object)filterablePack.name);
            return Optional.empty();
        }
        return Optional.empty();
    }

    private static Resource createResource(ResourcePack pack, Identifier id, InputSupplier<InputStream> supplier, InputSupplier<ResourceMetadata> metadataSupplier) {
        return new Resource(pack, NamespaceResourceManager.wrapForDebug(id, pack, supplier), metadataSupplier);
    }

    private static InputSupplier<InputStream> wrapForDebug(Identifier id, ResourcePack pack, InputSupplier<InputStream> supplier) {
        if (LOGGER.isDebugEnabled()) {
            return () -> new DebugInputStream((InputStream)supplier.get(), id, pack.getId());
        }
        return supplier;
    }

    @Override
    public List<Resource> getAllResources(Identifier id) {
        Identifier identifier = NamespaceResourceManager.getMetadataPath(id);
        ArrayList<Resource> list = new ArrayList<Resource>();
        boolean bl = false;
        String string = null;
        for (int i = this.packList.size() - 1; i >= 0; --i) {
            InputSupplier<InputStream> inputSupplier;
            FilterablePack filterablePack = this.packList.get(i);
            ResourcePack resourcePack = filterablePack.underlying;
            if (resourcePack != null && (inputSupplier = resourcePack.open(this.type, id)) != null) {
                InputSupplier<ResourceMetadata> inputSupplier2 = bl ? ResourceMetadata.NONE_SUPPLIER : () -> {
                    InputSupplier<InputStream> inputSupplier = resourcePack.open(this.type, identifier);
                    return inputSupplier != null ? NamespaceResourceManager.loadMetadata(inputSupplier) : ResourceMetadata.NONE;
                };
                list.add(new Resource(resourcePack, inputSupplier, inputSupplier2));
            }
            if (filterablePack.isFiltered(id)) {
                string = filterablePack.name;
                break;
            }
            if (!filterablePack.isFiltered(identifier)) continue;
            bl = true;
        }
        if (list.isEmpty() && string != null) {
            LOGGER.warn("Resource {} not found, but was filtered by pack {}", (Object)id, string);
        }
        return Lists.reverse(list);
    }

    private static boolean isMcmeta(Identifier id) {
        return id.getPath().endsWith(".mcmeta");
    }

    private static Identifier getMetadataFileName(Identifier id) {
        String string = id.getPath().substring(0, id.getPath().length() - ".mcmeta".length());
        return id.withPath(string);
    }

    static Identifier getMetadataPath(Identifier id) {
        return id.withPath(id.getPath() + ".mcmeta");
    }

    @Override
    public Map<Identifier, Resource> findResources(String startingPath, Predicate<Identifier> allowedPathPredicate) {
        final class Result
        extends Record {
            final ResourcePack pack;
            final InputSupplier<InputStream> supplier;
            final int packIndex;

            Result(ResourcePack pack, InputSupplier<InputStream> supplier, int packIndex) {
                this.pack = pack;
                this.supplier = supplier;
                this.packIndex = packIndex;
            }

            @Override
            public final String toString() {
                return ObjectMethods.bootstrap("toString", new MethodHandle[]{Result.class, "packResources;resource;packIndex", "pack", "supplier", "packIndex"}, this);
            }

            @Override
            public final int hashCode() {
                return (int)ObjectMethods.bootstrap("hashCode", new MethodHandle[]{Result.class, "packResources;resource;packIndex", "pack", "supplier", "packIndex"}, this);
            }

            @Override
            public final boolean equals(Object object) {
                return (boolean)ObjectMethods.bootstrap("equals", new MethodHandle[]{Result.class, "packResources;resource;packIndex", "pack", "supplier", "packIndex"}, this, object);
            }

            public ResourcePack pack() {
                return this.pack;
            }

            public InputSupplier<InputStream> supplier() {
                return this.supplier;
            }

            public int packIndex() {
                return this.packIndex;
            }
        }
        HashMap<Identifier, Result> map = new HashMap<Identifier, Result>();
        HashMap map2 = new HashMap();
        int i = this.packList.size();
        for (int j = 0; j < i; ++j) {
            FilterablePack filterablePack = this.packList.get(j);
            filterablePack.removeFiltered(map.keySet());
            filterablePack.removeFiltered(map2.keySet());
            ResourcePack resourcePack = filterablePack.underlying;
            if (resourcePack == null) continue;
            int k = j;
            resourcePack.findResources(this.type, this.namespace, startingPath, (id, supplier) -> {
                if (NamespaceResourceManager.isMcmeta(id)) {
                    if (allowedPathPredicate.test(NamespaceResourceManager.getMetadataFileName(id))) {
                        map2.put(id, new Result(resourcePack, (InputSupplier<InputStream>)supplier, k));
                    }
                } else if (allowedPathPredicate.test((Identifier)id)) {
                    map.put((Identifier)id, new Result(resourcePack, (InputSupplier<InputStream>)supplier, k));
                }
            });
        }
        TreeMap map3 = Maps.newTreeMap();
        map.forEach((id, result) -> {
            Identifier identifier = NamespaceResourceManager.getMetadataPath(id);
            Result result2 = (Result)map2.get(identifier);
            InputSupplier<ResourceMetadata> inputSupplier = result2 != null && result2.packIndex >= result.packIndex ? NamespaceResourceManager.getMetadataSupplier(result2.supplier) : ResourceMetadata.NONE_SUPPLIER;
            map3.put(id, NamespaceResourceManager.createResource(result.pack, id, result.supplier, inputSupplier));
        });
        return map3;
    }

    private InputSupplier<ResourceMetadata> createMetadataSupplier(Identifier id, int index) {
        return () -> {
            Identifier identifier2 = NamespaceResourceManager.getMetadataPath(id);
            for (int j = this.packList.size() - 1; j >= index; --j) {
                InputSupplier<InputStream> inputSupplier;
                FilterablePack filterablePack = this.packList.get(j);
                ResourcePack resourcePack = filterablePack.underlying;
                if (resourcePack != null && (inputSupplier = resourcePack.open(this.type, identifier2)) != null) {
                    return NamespaceResourceManager.loadMetadata(inputSupplier);
                }
                if (filterablePack.isFiltered(identifier2)) break;
            }
            return ResourceMetadata.NONE;
        };
    }

    private static InputSupplier<ResourceMetadata> getMetadataSupplier(InputSupplier<InputStream> supplier) {
        return () -> NamespaceResourceManager.loadMetadata(supplier);
    }

    private static ResourceMetadata loadMetadata(InputSupplier<InputStream> supplier) throws IOException {
        try (InputStream inputStream = supplier.get();){
            ResourceMetadata resourceMetadata = ResourceMetadata.create(inputStream);
            return resourceMetadata;
        }
    }

    private static void applyFilter(FilterablePack pack, Map<Identifier, EntryList> idToEntryList) {
        for (EntryList entryList : idToEntryList.values()) {
            if (pack.isFiltered(entryList.id)) {
                entryList.fileSources.clear();
                continue;
            }
            if (!pack.isFiltered(entryList.metadataId())) continue;
            entryList.metaSources.clear();
        }
    }

    private void findAndAdd(FilterablePack pack, String startingPath, Predicate<Identifier> allowedPathPredicate, Map<Identifier, EntryList> idToEntryList) {
        ResourcePack resourcePack = pack.underlying;
        if (resourcePack == null) {
            return;
        }
        resourcePack.findResources(this.type, this.namespace, startingPath, (id, supplier) -> {
            if (NamespaceResourceManager.isMcmeta(id)) {
                Identifier identifier = NamespaceResourceManager.getMetadataFileName(id);
                if (!allowedPathPredicate.test(identifier)) {
                    return;
                }
                map.computeIfAbsent(identifier, (Function<Identifier, EntryList>)LambdaMetafactory.metafactory(null, null, null, (Ljava/lang/Object;)Ljava/lang/Object;, <init>(net.minecraft.util.Identifier ), (Lnet/minecraft/util/Identifier;)Lnet/minecraft/resource/NamespaceResourceManager$EntryList;)()).metaSources.put(resourcePack, (InputSupplier<InputStream>)supplier);
            } else {
                if (!allowedPathPredicate.test((Identifier)id)) {
                    return;
                }
                map.computeIfAbsent(id, (Function<Identifier, EntryList>)LambdaMetafactory.metafactory(null, null, null, (Ljava/lang/Object;)Ljava/lang/Object;, <init>(net.minecraft.util.Identifier ), (Lnet/minecraft/util/Identifier;)Lnet/minecraft/resource/NamespaceResourceManager$EntryList;)()).fileSources.add(new FileSource(resourcePack, (InputSupplier<InputStream>)supplier));
            }
        });
    }

    @Override
    public Map<Identifier, List<Resource>> findAllResources(String startingPath, Predicate<Identifier> allowedPathPredicate) {
        HashMap map = Maps.newHashMap();
        for (FilterablePack filterablePack : this.packList) {
            NamespaceResourceManager.applyFilter(filterablePack, map);
            this.findAndAdd(filterablePack, startingPath, allowedPathPredicate, map);
        }
        TreeMap treeMap = Maps.newTreeMap();
        for (EntryList entryList : map.values()) {
            if (entryList.fileSources.isEmpty()) continue;
            ArrayList<Resource> list = new ArrayList<Resource>();
            for (FileSource fileSource : entryList.fileSources) {
                ResourcePack resourcePack = fileSource.sourcePack;
                InputSupplier<InputStream> inputSupplier = entryList.metaSources.get(resourcePack);
                InputSupplier<ResourceMetadata> inputSupplier2 = inputSupplier != null ? NamespaceResourceManager.getMetadataSupplier(inputSupplier) : ResourceMetadata.NONE_SUPPLIER;
                list.add(NamespaceResourceManager.createResource(resourcePack, entryList.id, fileSource.supplier, inputSupplier2));
            }
            treeMap.put(entryList.id, list);
        }
        return treeMap;
    }

    @Override
    public Stream<ResourcePack> streamResourcePacks() {
        return this.packList.stream().map(pack -> pack.underlying).filter(Objects::nonNull);
    }

    static final class FilterablePack
    extends Record {
        final String name;
        final @Nullable ResourcePack underlying;
        private final @Nullable Predicate<Identifier> filter;

        FilterablePack(String name, @Nullable ResourcePack underlying, @Nullable Predicate<Identifier> filter) {
            this.name = name;
            this.underlying = underlying;
            this.filter = filter;
        }

        public void removeFiltered(Collection<Identifier> ids) {
            if (this.filter != null) {
                ids.removeIf(this.filter);
            }
        }

        public boolean isFiltered(Identifier id) {
            return this.filter != null && this.filter.test(id);
        }

        @Override
        public final String toString() {
            return ObjectMethods.bootstrap("toString", new MethodHandle[]{FilterablePack.class, "name;resources;filter", "name", "underlying", "filter"}, this);
        }

        @Override
        public final int hashCode() {
            return (int)ObjectMethods.bootstrap("hashCode", new MethodHandle[]{FilterablePack.class, "name;resources;filter", "name", "underlying", "filter"}, this);
        }

        @Override
        public final boolean equals(Object object) {
            return (boolean)ObjectMethods.bootstrap("equals", new MethodHandle[]{FilterablePack.class, "name;resources;filter", "name", "underlying", "filter"}, this, object);
        }

        public String name() {
            return this.name;
        }

        public @Nullable ResourcePack underlying() {
            return this.underlying;
        }

        public @Nullable Predicate<Identifier> filter() {
            return this.filter;
        }
    }

    static final class EntryList
    extends Record {
        final Identifier id;
        private final Identifier metadataId;
        final List<FileSource> fileSources;
        final Map<ResourcePack, InputSupplier<InputStream>> metaSources;

        EntryList(Identifier id) {
            this(id, NamespaceResourceManager.getMetadataPath(id), new ArrayList<FileSource>(), (Map<ResourcePack, InputSupplier<InputStream>>)new Object2ObjectArrayMap());
        }

        private EntryList(Identifier id, Identifier metadataId, List<FileSource> fileSources, Map<ResourcePack, InputSupplier<InputStream>> metaSources) {
            this.id = id;
            this.metadataId = metadataId;
            this.fileSources = fileSources;
            this.metaSources = metaSources;
        }

        @Override
        public final String toString() {
            return ObjectMethods.bootstrap("toString", new MethodHandle[]{EntryList.class, "fileLocation;metadataLocation;fileSources;metaSources", "id", "metadataId", "fileSources", "metaSources"}, this);
        }

        @Override
        public final int hashCode() {
            return (int)ObjectMethods.bootstrap("hashCode", new MethodHandle[]{EntryList.class, "fileLocation;metadataLocation;fileSources;metaSources", "id", "metadataId", "fileSources", "metaSources"}, this);
        }

        @Override
        public final boolean equals(Object object) {
            return (boolean)ObjectMethods.bootstrap("equals", new MethodHandle[]{EntryList.class, "fileLocation;metadataLocation;fileSources;metaSources", "id", "metadataId", "fileSources", "metaSources"}, this, object);
        }

        public Identifier id() {
            return this.id;
        }

        public Identifier metadataId() {
            return this.metadataId;
        }

        public List<FileSource> fileSources() {
            return this.fileSources;
        }

        public Map<ResourcePack, InputSupplier<InputStream>> metaSources() {
            return this.metaSources;
        }
    }

    static final class FileSource
    extends Record {
        final ResourcePack sourcePack;
        final InputSupplier<InputStream> supplier;

        FileSource(ResourcePack sourcePack, InputSupplier<InputStream> supplier) {
            this.sourcePack = sourcePack;
            this.supplier = supplier;
        }

        @Override
        public final String toString() {
            return ObjectMethods.bootstrap("toString", new MethodHandle[]{FileSource.class, "source;resource", "sourcePack", "supplier"}, this);
        }

        @Override
        public final int hashCode() {
            return (int)ObjectMethods.bootstrap("hashCode", new MethodHandle[]{FileSource.class, "source;resource", "sourcePack", "supplier"}, this);
        }

        @Override
        public final boolean equals(Object object) {
            return (boolean)ObjectMethods.bootstrap("equals", new MethodHandle[]{FileSource.class, "source;resource", "sourcePack", "supplier"}, this, object);
        }

        public ResourcePack sourcePack() {
            return this.sourcePack;
        }

        public InputSupplier<InputStream> supplier() {
            return this.supplier;
        }
    }

    static class DebugInputStream
    extends FilterInputStream {
        private final Supplier<String> leakMessage;
        private boolean closed;

        public DebugInputStream(InputStream parent, Identifier id, String packId) {
            super(parent);
            Exception exception = new Exception("Stacktrace");
            this.leakMessage = () -> {
                StringWriter stringWriter = new StringWriter();
                exception.printStackTrace(new PrintWriter(stringWriter));
                return "Leaked resource: '" + String.valueOf(id) + "' loaded from pack: '" + packId + "'\n" + String.valueOf(stringWriter);
            };
        }

        @Override
        public void close() throws IOException {
            super.close();
            this.closed = true;
        }

        protected void finalize() throws Throwable {
            if (!this.closed) {
                LOGGER.warn("{}", (Object)this.leakMessage.get());
            }
            super.finalize();
        }
    }
}


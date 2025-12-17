/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  com.google.gson.JsonElement
 *  com.mojang.logging.LogUtils
 *  com.mojang.serialization.Codec
 *  com.mojang.serialization.DataResult
 *  com.mojang.serialization.Decoder
 *  com.mojang.serialization.JsonOps
 *  com.mojang.serialization.Lifecycle
 *  org.slf4j.Logger
 */
package net.minecraft.registry;

import com.google.gson.JsonElement;
import com.mojang.logging.LogUtils;
import com.mojang.serialization.Codec;
import com.mojang.serialization.DataResult;
import com.mojang.serialization.Decoder;
import com.mojang.serialization.JsonOps;
import com.mojang.serialization.Lifecycle;
import java.io.BufferedReader;
import java.io.IOException;
import java.io.PrintWriter;
import java.io.StringWriter;
import java.lang.invoke.MethodHandle;
import java.lang.runtime.ObjectMethods;
import java.util.Comparator;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Optional;
import java.util.function.BiConsumer;
import java.util.function.Function;
import java.util.stream.Collectors;
import net.minecraft.block.entity.BannerPattern;
import net.minecraft.block.jukebox.JukeboxSong;
import net.minecraft.block.spawner.TrialSpawnerConfig;
import net.minecraft.dialog.type.Dialog;
import net.minecraft.enchantment.Enchantment;
import net.minecraft.enchantment.provider.EnchantmentProvider;
import net.minecraft.entity.damage.DamageType;
import net.minecraft.entity.decoration.painting.PaintingVariant;
import net.minecraft.entity.mob.ZombieNautilusVariant;
import net.minecraft.entity.passive.CatVariant;
import net.minecraft.entity.passive.ChickenVariant;
import net.minecraft.entity.passive.CowVariant;
import net.minecraft.entity.passive.FrogVariant;
import net.minecraft.entity.passive.PigVariant;
import net.minecraft.entity.passive.WolfSoundVariant;
import net.minecraft.entity.passive.WolfVariant;
import net.minecraft.item.Instrument;
import net.minecraft.item.equipment.trim.ArmorTrimMaterial;
import net.minecraft.item.equipment.trim.ArmorTrimPattern;
import net.minecraft.nbt.NbtElement;
import net.minecraft.nbt.NbtOps;
import net.minecraft.network.message.MessageType;
import net.minecraft.registry.DynamicRegistryManager;
import net.minecraft.registry.MutableRegistry;
import net.minecraft.registry.Registry;
import net.minecraft.registry.RegistryKey;
import net.minecraft.registry.RegistryKeys;
import net.minecraft.registry.RegistryOps;
import net.minecraft.registry.RegistryWrapper;
import net.minecraft.registry.SerializableRegistries;
import net.minecraft.registry.SimpleRegistry;
import net.minecraft.registry.VersionedIdentifier;
import net.minecraft.registry.entry.RegistryEntryInfo;
import net.minecraft.registry.tag.TagGroupLoader;
import net.minecraft.registry.tag.TagPacketSerializer;
import net.minecraft.resource.Resource;
import net.minecraft.resource.ResourceFactory;
import net.minecraft.resource.ResourceFinder;
import net.minecraft.resource.ResourceManager;
import net.minecraft.structure.StructureSet;
import net.minecraft.structure.pool.StructurePool;
import net.minecraft.structure.processor.StructureProcessorList;
import net.minecraft.structure.processor.StructureProcessorType;
import net.minecraft.test.TestEnvironmentDefinition;
import net.minecraft.test.TestInstance;
import net.minecraft.util.Identifier;
import net.minecraft.util.StrictJsonParser;
import net.minecraft.util.Util;
import net.minecraft.util.crash.CrashException;
import net.minecraft.util.crash.CrashReport;
import net.minecraft.util.crash.CrashReportSection;
import net.minecraft.util.math.noise.DoublePerlinNoiseSampler;
import net.minecraft.world.attribute.timeline.Timeline;
import net.minecraft.world.biome.Biome;
import net.minecraft.world.biome.source.MultiNoiseBiomeSourceParameterList;
import net.minecraft.world.dimension.DimensionOptions;
import net.minecraft.world.dimension.DimensionType;
import net.minecraft.world.gen.FlatLevelGeneratorPreset;
import net.minecraft.world.gen.WorldPreset;
import net.minecraft.world.gen.carver.ConfiguredCarver;
import net.minecraft.world.gen.chunk.ChunkGeneratorSettings;
import net.minecraft.world.gen.densityfunction.DensityFunction;
import net.minecraft.world.gen.feature.ConfiguredFeature;
import net.minecraft.world.gen.feature.PlacedFeature;
import net.minecraft.world.gen.structure.Structure;
import org.slf4j.Logger;

public class RegistryLoader {
    private static final Logger LOGGER = LogUtils.getLogger();
    private static final Comparator<RegistryKey<?>> KEY_COMPARATOR = Comparator.comparing(RegistryKey::getRegistry).thenComparing(RegistryKey::getValue);
    private static final RegistryEntryInfo EXPERIMENTAL_ENTRY_INFO = new RegistryEntryInfo(Optional.empty(), Lifecycle.experimental());
    private static final Function<Optional<VersionedIdentifier>, RegistryEntryInfo> RESOURCE_ENTRY_INFO_GETTER = Util.memoize(knownPacks -> {
        Lifecycle lifecycle = knownPacks.map(VersionedIdentifier::isVanilla).map(vanilla -> Lifecycle.stable()).orElse(Lifecycle.experimental());
        return new RegistryEntryInfo((Optional<VersionedIdentifier>)knownPacks, lifecycle);
    });
    public static final List<Entry<?>> DYNAMIC_REGISTRIES = List.of(new Entry<DimensionType>(RegistryKeys.DIMENSION_TYPE, DimensionType.CODEC), new Entry<Biome>(RegistryKeys.BIOME, Biome.CODEC), new Entry<MessageType>(RegistryKeys.MESSAGE_TYPE, MessageType.CODEC), new Entry(RegistryKeys.CONFIGURED_CARVER, ConfiguredCarver.CODEC), new Entry(RegistryKeys.CONFIGURED_FEATURE, ConfiguredFeature.CODEC), new Entry<PlacedFeature>(RegistryKeys.PLACED_FEATURE, PlacedFeature.CODEC), new Entry<Structure>(RegistryKeys.STRUCTURE, Structure.STRUCTURE_CODEC), new Entry<StructureSet>(RegistryKeys.STRUCTURE_SET, StructureSet.CODEC), new Entry<StructureProcessorList>(RegistryKeys.PROCESSOR_LIST, StructureProcessorType.PROCESSORS_CODEC), new Entry<StructurePool>(RegistryKeys.TEMPLATE_POOL, StructurePool.CODEC), new Entry<ChunkGeneratorSettings>(RegistryKeys.CHUNK_GENERATOR_SETTINGS, ChunkGeneratorSettings.CODEC), new Entry<DoublePerlinNoiseSampler.NoiseParameters>(RegistryKeys.NOISE_PARAMETERS, DoublePerlinNoiseSampler.NoiseParameters.CODEC), new Entry<DensityFunction>(RegistryKeys.DENSITY_FUNCTION, DensityFunction.CODEC), new Entry<WorldPreset>(RegistryKeys.WORLD_PRESET, WorldPreset.CODEC), new Entry<FlatLevelGeneratorPreset>(RegistryKeys.FLAT_LEVEL_GENERATOR_PRESET, FlatLevelGeneratorPreset.CODEC), new Entry<ArmorTrimPattern>(RegistryKeys.TRIM_PATTERN, ArmorTrimPattern.CODEC), new Entry<ArmorTrimMaterial>(RegistryKeys.TRIM_MATERIAL, ArmorTrimMaterial.CODEC), new Entry<TrialSpawnerConfig>(RegistryKeys.TRIAL_SPAWNER, TrialSpawnerConfig.CODEC), new Entry<WolfVariant>(RegistryKeys.WOLF_VARIANT, WolfVariant.CODEC, true), new Entry<WolfSoundVariant>(RegistryKeys.WOLF_SOUND_VARIANT, WolfSoundVariant.CODEC, true), new Entry<PigVariant>(RegistryKeys.PIG_VARIANT, PigVariant.CODEC, true), new Entry<FrogVariant>(RegistryKeys.FROG_VARIANT, FrogVariant.CODEC, true), new Entry<CatVariant>(RegistryKeys.CAT_VARIANT, CatVariant.CODEC, true), new Entry<CowVariant>(RegistryKeys.COW_VARIANT, CowVariant.CODEC, true), new Entry<ChickenVariant>(RegistryKeys.CHICKEN_VARIANT, ChickenVariant.CODEC, true), new Entry<ZombieNautilusVariant>(RegistryKeys.ZOMBIE_NAUTILUS_VARIANT, ZombieNautilusVariant.CODEC, true), new Entry<PaintingVariant>(RegistryKeys.PAINTING_VARIANT, PaintingVariant.CODEC, true), new Entry<DamageType>(RegistryKeys.DAMAGE_TYPE, DamageType.CODEC), new Entry<MultiNoiseBiomeSourceParameterList>(RegistryKeys.MULTI_NOISE_BIOME_SOURCE_PARAMETER_LIST, MultiNoiseBiomeSourceParameterList.CODEC), new Entry<BannerPattern>(RegistryKeys.BANNER_PATTERN, BannerPattern.CODEC), new Entry<Enchantment>(RegistryKeys.ENCHANTMENT, Enchantment.CODEC), new Entry<EnchantmentProvider>(RegistryKeys.ENCHANTMENT_PROVIDER, EnchantmentProvider.CODEC), new Entry<JukeboxSong>(RegistryKeys.JUKEBOX_SONG, JukeboxSong.CODEC), new Entry<Instrument>(RegistryKeys.INSTRUMENT, Instrument.CODEC), new Entry<TestEnvironmentDefinition>(RegistryKeys.TEST_ENVIRONMENT, TestEnvironmentDefinition.CODEC), new Entry<TestInstance>(RegistryKeys.TEST_INSTANCE, TestInstance.CODEC), new Entry<Dialog>(RegistryKeys.DIALOG, Dialog.CODEC), new Entry<Timeline>(RegistryKeys.TIMELINE, Timeline.CODEC));
    public static final List<Entry<?>> DIMENSION_REGISTRIES = List.of(new Entry<DimensionOptions>(RegistryKeys.DIMENSION, DimensionOptions.CODEC));
    public static final List<Entry<?>> SYNCED_REGISTRIES = List.of(new Entry<Biome>(RegistryKeys.BIOME, Biome.NETWORK_CODEC), new Entry<MessageType>(RegistryKeys.MESSAGE_TYPE, MessageType.CODEC), new Entry<ArmorTrimPattern>(RegistryKeys.TRIM_PATTERN, ArmorTrimPattern.CODEC), new Entry<ArmorTrimMaterial>(RegistryKeys.TRIM_MATERIAL, ArmorTrimMaterial.CODEC), new Entry<WolfVariant>(RegistryKeys.WOLF_VARIANT, WolfVariant.NETWORK_CODEC, true), new Entry<WolfSoundVariant>(RegistryKeys.WOLF_SOUND_VARIANT, WolfSoundVariant.NETWORK_CODEC, true), new Entry<PigVariant>(RegistryKeys.PIG_VARIANT, PigVariant.NETWORK_CODEC, true), new Entry<FrogVariant>(RegistryKeys.FROG_VARIANT, FrogVariant.NETWORK_CODEC, true), new Entry<CatVariant>(RegistryKeys.CAT_VARIANT, CatVariant.NETWORK_CODEC, true), new Entry<CowVariant>(RegistryKeys.COW_VARIANT, CowVariant.NETWORK_CODEC, true), new Entry<ChickenVariant>(RegistryKeys.CHICKEN_VARIANT, ChickenVariant.NETWORK_CODEC, true), new Entry<ZombieNautilusVariant>(RegistryKeys.ZOMBIE_NAUTILUS_VARIANT, ZombieNautilusVariant.NETWORK_CODEC, true), new Entry<PaintingVariant>(RegistryKeys.PAINTING_VARIANT, PaintingVariant.CODEC, true), new Entry<DimensionType>(RegistryKeys.DIMENSION_TYPE, DimensionType.NETWORK_CODEC), new Entry<DamageType>(RegistryKeys.DAMAGE_TYPE, DamageType.CODEC), new Entry<BannerPattern>(RegistryKeys.BANNER_PATTERN, BannerPattern.CODEC), new Entry<Enchantment>(RegistryKeys.ENCHANTMENT, Enchantment.CODEC), new Entry<JukeboxSong>(RegistryKeys.JUKEBOX_SONG, JukeboxSong.CODEC), new Entry<Instrument>(RegistryKeys.INSTRUMENT, Instrument.CODEC), new Entry<TestEnvironmentDefinition>(RegistryKeys.TEST_ENVIRONMENT, TestEnvironmentDefinition.CODEC), new Entry<TestInstance>(RegistryKeys.TEST_INSTANCE, TestInstance.CODEC), new Entry<Dialog>(RegistryKeys.DIALOG, Dialog.CODEC), new Entry<Timeline>(RegistryKeys.TIMELINE, Timeline.NETWORK_CODEC));

    public static DynamicRegistryManager.Immutable loadFromResource(ResourceManager resourceManager, List<RegistryWrapper.Impl<?>> registries, List<Entry<?>> entries) {
        return RegistryLoader.load((loader, infoGetter) -> loader.loadFromResource(resourceManager, infoGetter), registries, entries);
    }

    public static DynamicRegistryManager.Immutable loadFromNetwork(Map<RegistryKey<? extends Registry<?>>, ElementsAndTags> data, ResourceFactory factory, List<RegistryWrapper.Impl<?>> registries, List<Entry<?>> entries) {
        return RegistryLoader.load((loader, infoGetter) -> loader.loadFromNetwork(data, factory, infoGetter), registries, entries);
    }

    private static DynamicRegistryManager.Immutable load(RegistryLoadable loadable, List<RegistryWrapper.Impl<?>> registries, List<Entry<?>> entries) {
        HashMap map = new HashMap();
        List<Loader<?>> list = entries.stream().map(entry -> entry.getLoader(Lifecycle.stable(), map)).collect(Collectors.toUnmodifiableList());
        RegistryOps.RegistryInfoGetter registryInfoGetter = RegistryLoader.createInfoGetter(registries, list);
        list.forEach(loader -> loadable.apply((Loader<?>)loader, registryInfoGetter));
        list.forEach(loader -> {
            MutableRegistry registry = loader.registry();
            try {
                registry.freeze();
            }
            catch (Exception exception) {
                map.put(registry.getKey(), exception);
            }
            if (loader.data.requiredNonEmpty && registry.size() == 0) {
                map.put(registry.getKey(), new IllegalStateException("Registry must be non-empty: " + String.valueOf(registry.getKey().getValue())));
            }
        });
        if (!map.isEmpty()) {
            throw RegistryLoader.writeAndCreateLoadingException(map);
        }
        return new DynamicRegistryManager.ImmutableImpl(list.stream().map(Loader::registry).toList()).toImmutable();
    }

    private static RegistryOps.RegistryInfoGetter createInfoGetter(List<RegistryWrapper.Impl<?>> registries, List<Loader<?>> additionalRegistries) {
        final HashMap map = new HashMap();
        registries.forEach(registry -> map.put(registry.getKey(), RegistryLoader.createInfo(registry)));
        additionalRegistries.forEach(loader -> map.put(loader.registry.getKey(), RegistryLoader.createInfo(loader.registry)));
        return new RegistryOps.RegistryInfoGetter(){

            @Override
            public <T> Optional<RegistryOps.RegistryInfo<T>> getRegistryInfo(RegistryKey<? extends Registry<? extends T>> registryRef) {
                return Optional.ofNullable((RegistryOps.RegistryInfo)map.get(registryRef));
            }
        };
    }

    private static <T> RegistryOps.RegistryInfo<T> createInfo(MutableRegistry<T> registry) {
        return new RegistryOps.RegistryInfo<T>(registry, registry.createMutableRegistryLookup(), registry.getLifecycle());
    }

    private static <T> RegistryOps.RegistryInfo<T> createInfo(RegistryWrapper.Impl<T> registry) {
        return new RegistryOps.RegistryInfo<T>(registry, registry, registry.getLifecycle());
    }

    private static CrashException writeAndCreateLoadingException(Map<RegistryKey<?>, Exception> exceptions) {
        RegistryLoader.writeLoadingError(exceptions);
        return RegistryLoader.createLoadingException(exceptions);
    }

    private static void writeLoadingError(Map<RegistryKey<?>, Exception> exceptions) {
        StringWriter stringWriter = new StringWriter();
        PrintWriter printWriter = new PrintWriter(stringWriter);
        Map<Identifier, Map<Identifier, Exception>> map = exceptions.entrySet().stream().collect(Collectors.groupingBy(entry -> ((RegistryKey)entry.getKey()).getRegistry(), Collectors.toMap(entry -> ((RegistryKey)entry.getKey()).getValue(), Map.Entry::getValue)));
        map.entrySet().stream().sorted(Map.Entry.comparingByKey()).forEach(entry -> {
            printWriter.printf(Locale.ROOT, "> Errors in registry %s:%n", entry.getKey());
            ((Map)entry.getValue()).entrySet().stream().sorted(Map.Entry.comparingByKey()).forEach(element -> {
                printWriter.printf(Locale.ROOT, ">> Errors in element %s:%n", element.getKey());
                ((Exception)element.getValue()).printStackTrace(printWriter);
            });
        });
        printWriter.flush();
        LOGGER.error("Registry loading errors:\n{}", (Object)stringWriter);
    }

    private static CrashException createLoadingException(Map<RegistryKey<?>, Exception> exceptions) {
        CrashReport crashReport = CrashReport.create(new IllegalStateException("Failed to load registries due to errors"), "Registry Loading");
        CrashReportSection crashReportSection = crashReport.addElement("Loading info");
        crashReportSection.add("Errors", () -> {
            StringBuilder stringBuilder = new StringBuilder();
            exceptions.entrySet().stream().sorted(Map.Entry.comparingByKey(KEY_COMPARATOR)).forEach(entry -> stringBuilder.append("\n\t\t").append(((RegistryKey)entry.getKey()).getRegistry()).append("/").append(((RegistryKey)entry.getKey()).getValue()).append(": ").append(((Exception)entry.getValue()).getMessage()));
            return stringBuilder.toString();
        });
        return new CrashException(crashReport);
    }

    private static <E> void parseAndAdd(MutableRegistry<E> registry, Decoder<E> decoder, RegistryOps<JsonElement> ops, RegistryKey<E> key, Resource resource, RegistryEntryInfo entryInfo) throws IOException {
        try (BufferedReader reader = resource.getReader();){
            JsonElement jsonElement = StrictJsonParser.parse(reader);
            DataResult dataResult = decoder.parse(ops, (Object)jsonElement);
            Object object = dataResult.getOrThrow();
            registry.add(key, object, entryInfo);
        }
    }

    static <E> void loadFromResource(ResourceManager resourceManager, RegistryOps.RegistryInfoGetter infoGetter, MutableRegistry<E> registry, Decoder<E> elementDecoder, Map<RegistryKey<?>, Exception> errors) {
        ResourceFinder resourceFinder = ResourceFinder.json(registry.getKey());
        RegistryOps<JsonElement> registryOps = RegistryOps.of(JsonOps.INSTANCE, infoGetter);
        for (Map.Entry<Identifier, Resource> entry : resourceFinder.findResources(resourceManager).entrySet()) {
            Identifier identifier = entry.getKey();
            RegistryKey registryKey = RegistryKey.of(registry.getKey(), resourceFinder.toResourceId(identifier));
            Resource resource = entry.getValue();
            RegistryEntryInfo registryEntryInfo = RESOURCE_ENTRY_INFO_GETTER.apply(resource.getKnownPackInfo());
            try {
                RegistryLoader.parseAndAdd(registry, elementDecoder, registryOps, registryKey, resource, registryEntryInfo);
            }
            catch (Exception exception) {
                errors.put(registryKey, new IllegalStateException(String.format(Locale.ROOT, "Failed to parse %s from pack %s", identifier, resource.getPackId()), exception));
            }
        }
        TagGroupLoader.loadInitial(resourceManager, registry);
    }

    static <E> void loadFromNetwork(Map<RegistryKey<? extends Registry<?>>, ElementsAndTags> data, ResourceFactory factory, RegistryOps.RegistryInfoGetter infoGetter, MutableRegistry<E> registry, Decoder<E> decoder, Map<RegistryKey<?>, Exception> loadingErrors) {
        ElementsAndTags elementsAndTags = data.get(registry.getKey());
        if (elementsAndTags == null) {
            return;
        }
        RegistryOps<NbtElement> registryOps = RegistryOps.of(NbtOps.INSTANCE, infoGetter);
        RegistryOps<JsonElement> registryOps2 = RegistryOps.of(JsonOps.INSTANCE, infoGetter);
        ResourceFinder resourceFinder = ResourceFinder.json(registry.getKey());
        for (SerializableRegistries.SerializedRegistryEntry serializedRegistryEntry : elementsAndTags.elements) {
            RegistryKey registryKey = RegistryKey.of(registry.getKey(), serializedRegistryEntry.id());
            Optional<NbtElement> optional = serializedRegistryEntry.data();
            if (optional.isPresent()) {
                try {
                    DataResult dataResult = decoder.parse(registryOps, (Object)optional.get());
                    Object object = dataResult.getOrThrow();
                    registry.add(registryKey, object, EXPERIMENTAL_ENTRY_INFO);
                }
                catch (Exception exception) {
                    loadingErrors.put(registryKey, new IllegalStateException(String.format(Locale.ROOT, "Failed to parse value %s from server", optional.get()), exception));
                }
                continue;
            }
            Identifier identifier = resourceFinder.toResourcePath(serializedRegistryEntry.id());
            try {
                Resource resource = factory.getResourceOrThrow(identifier);
                RegistryLoader.parseAndAdd(registry, decoder, registryOps2, registryKey, resource, EXPERIMENTAL_ENTRY_INFO);
            }
            catch (Exception exception2) {
                loadingErrors.put(registryKey, new IllegalStateException("Failed to parse local data", exception2));
            }
        }
        TagGroupLoader.loadFromNetwork(elementsAndTags.tags, registry);
    }

    @FunctionalInterface
    static interface RegistryLoadable {
        public void apply(Loader<?> var1, RegistryOps.RegistryInfoGetter var2);
    }

    public static final class ElementsAndTags
    extends Record {
        final List<SerializableRegistries.SerializedRegistryEntry> elements;
        final TagPacketSerializer.Serialized tags;

        public ElementsAndTags(List<SerializableRegistries.SerializedRegistryEntry> elements, TagPacketSerializer.Serialized tags) {
            this.elements = elements;
            this.tags = tags;
        }

        @Override
        public final String toString() {
            return ObjectMethods.bootstrap("toString", new MethodHandle[]{ElementsAndTags.class, "elements;tags", "elements", "tags"}, this);
        }

        @Override
        public final int hashCode() {
            return (int)ObjectMethods.bootstrap("hashCode", new MethodHandle[]{ElementsAndTags.class, "elements;tags", "elements", "tags"}, this);
        }

        @Override
        public final boolean equals(Object object) {
            return (boolean)ObjectMethods.bootstrap("equals", new MethodHandle[]{ElementsAndTags.class, "elements;tags", "elements", "tags"}, this, object);
        }

        public List<SerializableRegistries.SerializedRegistryEntry> elements() {
            return this.elements;
        }

        public TagPacketSerializer.Serialized tags() {
            return this.tags;
        }
    }

    static final class Loader<T>
    extends Record {
        final Entry<T> data;
        final MutableRegistry<T> registry;
        private final Map<RegistryKey<?>, Exception> loadingErrors;

        Loader(Entry<T> data, MutableRegistry<T> registry, Map<RegistryKey<?>, Exception> loadingErrors) {
            this.data = data;
            this.registry = registry;
            this.loadingErrors = loadingErrors;
        }

        public void loadFromResource(ResourceManager resourceManager, RegistryOps.RegistryInfoGetter infoGetter) {
            RegistryLoader.loadFromResource(resourceManager, infoGetter, this.registry, this.data.elementCodec, this.loadingErrors);
        }

        public void loadFromNetwork(Map<RegistryKey<? extends Registry<?>>, ElementsAndTags> data, ResourceFactory factory, RegistryOps.RegistryInfoGetter infoGetter) {
            RegistryLoader.loadFromNetwork(data, factory, infoGetter, this.registry, this.data.elementCodec, this.loadingErrors);
        }

        @Override
        public final String toString() {
            return ObjectMethods.bootstrap("toString", new MethodHandle[]{Loader.class, "data;registry;loadingErrors", "data", "registry", "loadingErrors"}, this);
        }

        @Override
        public final int hashCode() {
            return (int)ObjectMethods.bootstrap("hashCode", new MethodHandle[]{Loader.class, "data;registry;loadingErrors", "data", "registry", "loadingErrors"}, this);
        }

        @Override
        public final boolean equals(Object object) {
            return (boolean)ObjectMethods.bootstrap("equals", new MethodHandle[]{Loader.class, "data;registry;loadingErrors", "data", "registry", "loadingErrors"}, this, object);
        }

        public Entry<T> data() {
            return this.data;
        }

        public MutableRegistry<T> registry() {
            return this.registry;
        }

        public Map<RegistryKey<?>, Exception> loadingErrors() {
            return this.loadingErrors;
        }
    }

    public static final class Entry<T>
    extends Record {
        private final RegistryKey<? extends Registry<T>> key;
        final Codec<T> elementCodec;
        final boolean requiredNonEmpty;

        Entry(RegistryKey<? extends Registry<T>> key, Codec<T> codec) {
            this(key, codec, false);
        }

        public Entry(RegistryKey<? extends Registry<T>> key, Codec<T> elementCodec, boolean requiredNonEmpty) {
            this.key = key;
            this.elementCodec = elementCodec;
            this.requiredNonEmpty = requiredNonEmpty;
        }

        Loader<T> getLoader(Lifecycle lifecycle, Map<RegistryKey<?>, Exception> errors) {
            SimpleRegistry mutableRegistry = new SimpleRegistry(this.key, lifecycle);
            return new Loader(this, mutableRegistry, errors);
        }

        public void addToCloner(BiConsumer<RegistryKey<? extends Registry<T>>, Codec<T>> callback) {
            callback.accept(this.key, this.elementCodec);
        }

        @Override
        public final String toString() {
            return ObjectMethods.bootstrap("toString", new MethodHandle[]{Entry.class, "key;elementCodec;requiredNonEmpty", "key", "elementCodec", "requiredNonEmpty"}, this);
        }

        @Override
        public final int hashCode() {
            return (int)ObjectMethods.bootstrap("hashCode", new MethodHandle[]{Entry.class, "key;elementCodec;requiredNonEmpty", "key", "elementCodec", "requiredNonEmpty"}, this);
        }

        @Override
        public final boolean equals(Object object) {
            return (boolean)ObjectMethods.bootstrap("equals", new MethodHandle[]{Entry.class, "key;elementCodec;requiredNonEmpty", "key", "elementCodec", "requiredNonEmpty"}, this, object);
        }

        public RegistryKey<? extends Registry<T>> key() {
            return this.key;
        }

        public Codec<T> elementCodec() {
            return this.elementCodec;
        }

        public boolean requiredNonEmpty() {
            return this.requiredNonEmpty;
        }
    }
}


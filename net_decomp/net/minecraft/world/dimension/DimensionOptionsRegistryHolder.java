/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  com.google.common.collect.ImmutableMap
 *  com.google.common.collect.ImmutableMap$Builder
 *  com.google.common.collect.ImmutableSet
 *  com.mojang.datafixers.kinds.App
 *  com.mojang.datafixers.kinds.Applicative
 *  com.mojang.serialization.Codec
 *  com.mojang.serialization.Lifecycle
 *  com.mojang.serialization.MapCodec
 *  com.mojang.serialization.codecs.RecordCodecBuilder
 */
package net.minecraft.world.dimension;

import com.google.common.collect.ImmutableMap;
import com.google.common.collect.ImmutableSet;
import com.mojang.datafixers.kinds.App;
import com.mojang.datafixers.kinds.Applicative;
import com.mojang.serialization.Codec;
import com.mojang.serialization.Lifecycle;
import com.mojang.serialization.MapCodec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import java.lang.invoke.MethodHandle;
import java.lang.runtime.ObjectMethods;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Set;
import java.util.stream.Collectors;
import java.util.stream.Stream;
import net.minecraft.registry.DynamicRegistryManager;
import net.minecraft.registry.Registry;
import net.minecraft.registry.RegistryEntryLookup;
import net.minecraft.registry.RegistryKey;
import net.minecraft.registry.RegistryKeys;
import net.minecraft.registry.RegistryWrapper;
import net.minecraft.registry.SimpleRegistry;
import net.minecraft.registry.entry.RegistryEntry;
import net.minecraft.registry.entry.RegistryEntryInfo;
import net.minecraft.world.World;
import net.minecraft.world.biome.source.BiomeSource;
import net.minecraft.world.biome.source.MultiNoiseBiomeSource;
import net.minecraft.world.biome.source.MultiNoiseBiomeSourceParameterLists;
import net.minecraft.world.biome.source.TheEndBiomeSource;
import net.minecraft.world.dimension.DimensionOptions;
import net.minecraft.world.dimension.DimensionType;
import net.minecraft.world.dimension.DimensionTypes;
import net.minecraft.world.gen.chunk.ChunkGenerator;
import net.minecraft.world.gen.chunk.ChunkGeneratorSettings;
import net.minecraft.world.gen.chunk.DebugChunkGenerator;
import net.minecraft.world.gen.chunk.FlatChunkGenerator;
import net.minecraft.world.gen.chunk.NoiseChunkGenerator;
import net.minecraft.world.level.LevelProperties;

public record DimensionOptionsRegistryHolder(Map<RegistryKey<DimensionOptions>, DimensionOptions> dimensions) {
    public static final MapCodec<DimensionOptionsRegistryHolder> CODEC = RecordCodecBuilder.mapCodec(instance -> instance.group((App)Codec.unboundedMap(RegistryKey.createCodec(RegistryKeys.DIMENSION), DimensionOptions.CODEC).fieldOf("dimensions").forGetter(DimensionOptionsRegistryHolder::dimensions)).apply((Applicative)instance, instance.stable(DimensionOptionsRegistryHolder::new)));
    private static final Set<RegistryKey<DimensionOptions>> VANILLA_KEYS = ImmutableSet.of(DimensionOptions.OVERWORLD, DimensionOptions.NETHER, DimensionOptions.END);
    private static final int VANILLA_KEY_COUNT = VANILLA_KEYS.size();

    public DimensionOptionsRegistryHolder {
        DimensionOptions dimensionOptions = dimensions.get(DimensionOptions.OVERWORLD);
        if (dimensionOptions == null) {
            throw new IllegalStateException("Overworld settings missing");
        }
    }

    public DimensionOptionsRegistryHolder(Registry<DimensionOptions> dimensionOptionsRegistry) {
        this(dimensionOptionsRegistry.streamEntries().collect(Collectors.toMap(RegistryEntry.Reference::registryKey, RegistryEntry.Reference::value)));
    }

    public static Stream<RegistryKey<DimensionOptions>> streamAll(Stream<RegistryKey<DimensionOptions>> otherKeys) {
        return Stream.concat(VANILLA_KEYS.stream(), otherKeys.filter(key -> !VANILLA_KEYS.contains(key)));
    }

    public DimensionOptionsRegistryHolder with(RegistryWrapper.WrapperLookup registries, ChunkGenerator chunkGenerator) {
        RegistryEntryLookup registryWrapper = registries.getOrThrow(RegistryKeys.DIMENSION_TYPE);
        Map<RegistryKey<DimensionOptions>, DimensionOptions> map = DimensionOptionsRegistryHolder.createRegistry((RegistryWrapper<DimensionType>)registryWrapper, this.dimensions, chunkGenerator);
        return new DimensionOptionsRegistryHolder(map);
    }

    public static Map<RegistryKey<DimensionOptions>, DimensionOptions> createRegistry(RegistryWrapper<DimensionType> dimensionTypeRegistry, Map<RegistryKey<DimensionOptions>, DimensionOptions> dimensionOptions, ChunkGenerator chunkGenerator) {
        DimensionOptions dimensionOptions2 = dimensionOptions.get(DimensionOptions.OVERWORLD);
        RegistryEntry<DimensionType> registryEntry = dimensionOptions2 == null ? dimensionTypeRegistry.getOrThrow(DimensionTypes.OVERWORLD) : dimensionOptions2.dimensionTypeEntry();
        return DimensionOptionsRegistryHolder.createRegistry(dimensionOptions, registryEntry, chunkGenerator);
    }

    public static Map<RegistryKey<DimensionOptions>, DimensionOptions> createRegistry(Map<RegistryKey<DimensionOptions>, DimensionOptions> dimensionOptions, RegistryEntry<DimensionType> overworld, ChunkGenerator chunkGenerator) {
        ImmutableMap.Builder builder = ImmutableMap.builder();
        builder.putAll(dimensionOptions);
        builder.put(DimensionOptions.OVERWORLD, (Object)new DimensionOptions(overworld, chunkGenerator));
        return builder.buildKeepingLast();
    }

    public ChunkGenerator getChunkGenerator() {
        DimensionOptions dimensionOptions = this.dimensions.get(DimensionOptions.OVERWORLD);
        if (dimensionOptions == null) {
            throw new IllegalStateException("Overworld settings missing");
        }
        return dimensionOptions.chunkGenerator();
    }

    public Optional<DimensionOptions> getOrEmpty(RegistryKey<DimensionOptions> key) {
        return Optional.ofNullable(this.dimensions.get(key));
    }

    public ImmutableSet<RegistryKey<World>> getWorldKeys() {
        return (ImmutableSet)this.dimensions().keySet().stream().map(RegistryKeys::toWorldKey).collect(ImmutableSet.toImmutableSet());
    }

    public boolean isDebug() {
        return this.getChunkGenerator() instanceof DebugChunkGenerator;
    }

    private static LevelProperties.SpecialProperty getSpecialProperty(Registry<DimensionOptions> dimensionOptionsRegistry) {
        return dimensionOptionsRegistry.getOptionalValue(DimensionOptions.OVERWORLD).map(overworldEntry -> {
            ChunkGenerator chunkGenerator = overworldEntry.chunkGenerator();
            if (chunkGenerator instanceof DebugChunkGenerator) {
                return LevelProperties.SpecialProperty.DEBUG;
            }
            if (chunkGenerator instanceof FlatChunkGenerator) {
                return LevelProperties.SpecialProperty.FLAT;
            }
            return LevelProperties.SpecialProperty.NONE;
        }).orElse(LevelProperties.SpecialProperty.NONE);
    }

    static Lifecycle getLifecycle(RegistryKey<DimensionOptions> key, DimensionOptions dimensionOptions) {
        return DimensionOptionsRegistryHolder.isVanilla(key, dimensionOptions) ? Lifecycle.stable() : Lifecycle.experimental();
    }

    private static boolean isVanilla(RegistryKey<DimensionOptions> key, DimensionOptions dimensionOptions) {
        if (key == DimensionOptions.OVERWORLD) {
            return DimensionOptionsRegistryHolder.isOverworldVanilla(dimensionOptions);
        }
        if (key == DimensionOptions.NETHER) {
            return DimensionOptionsRegistryHolder.isNetherVanilla(dimensionOptions);
        }
        if (key == DimensionOptions.END) {
            return DimensionOptionsRegistryHolder.isTheEndVanilla(dimensionOptions);
        }
        return false;
    }

    private static boolean isOverworldVanilla(DimensionOptions dimensionOptions) {
        MultiNoiseBiomeSource multiNoiseBiomeSource;
        RegistryEntry<DimensionType> registryEntry = dimensionOptions.dimensionTypeEntry();
        if (!registryEntry.matchesKey(DimensionTypes.OVERWORLD) && !registryEntry.matchesKey(DimensionTypes.OVERWORLD_CAVES)) {
            return false;
        }
        BiomeSource biomeSource = dimensionOptions.chunkGenerator().getBiomeSource();
        return !(biomeSource instanceof MultiNoiseBiomeSource) || (multiNoiseBiomeSource = (MultiNoiseBiomeSource)biomeSource).matchesInstance(MultiNoiseBiomeSourceParameterLists.OVERWORLD);
    }

    private static boolean isNetherVanilla(DimensionOptions dimensionOptions) {
        MultiNoiseBiomeSource multiNoiseBiomeSource;
        NoiseChunkGenerator noiseChunkGenerator;
        Object object;
        return dimensionOptions.dimensionTypeEntry().matchesKey(DimensionTypes.THE_NETHER) && (object = dimensionOptions.chunkGenerator()) instanceof NoiseChunkGenerator && (noiseChunkGenerator = (NoiseChunkGenerator)object).matchesSettings(ChunkGeneratorSettings.NETHER) && (object = noiseChunkGenerator.getBiomeSource()) instanceof MultiNoiseBiomeSource && (multiNoiseBiomeSource = (MultiNoiseBiomeSource)object).matchesInstance(MultiNoiseBiomeSourceParameterLists.NETHER);
    }

    private static boolean isTheEndVanilla(DimensionOptions dimensionOptions) {
        NoiseChunkGenerator noiseChunkGenerator;
        ChunkGenerator chunkGenerator;
        return dimensionOptions.dimensionTypeEntry().matchesKey(DimensionTypes.THE_END) && (chunkGenerator = dimensionOptions.chunkGenerator()) instanceof NoiseChunkGenerator && (noiseChunkGenerator = (NoiseChunkGenerator)chunkGenerator).matchesSettings(ChunkGeneratorSettings.END) && noiseChunkGenerator.getBiomeSource() instanceof TheEndBiomeSource;
    }

    public DimensionsConfig toConfig(Registry<DimensionOptions> existingRegistry) {
        final class Entry
        extends Record {
            final RegistryKey<DimensionOptions> key;
            final DimensionOptions value;

            Entry(RegistryKey<DimensionOptions> key, DimensionOptions value) {
                this.key = key;
                this.value = value;
            }

            RegistryEntryInfo toEntryInfo() {
                return new RegistryEntryInfo(Optional.empty(), DimensionOptionsRegistryHolder.getLifecycle(this.key, this.value));
            }

            @Override
            public final String toString() {
                return ObjectMethods.bootstrap("toString", new MethodHandle[]{Entry.class, "key;value", "key", "value"}, this);
            }

            @Override
            public final int hashCode() {
                return (int)ObjectMethods.bootstrap("hashCode", new MethodHandle[]{Entry.class, "key;value", "key", "value"}, this);
            }

            @Override
            public final boolean equals(Object object) {
                return (boolean)ObjectMethods.bootstrap("equals", new MethodHandle[]{Entry.class, "key;value", "key", "value"}, this, object);
            }

            public RegistryKey<DimensionOptions> key() {
                return this.key;
            }

            public DimensionOptions value() {
                return this.value;
            }
        }
        Stream<RegistryKey<DimensionOptions>> stream = Stream.concat(existingRegistry.getKeys().stream(), this.dimensions.keySet().stream()).distinct();
        ArrayList list = new ArrayList();
        DimensionOptionsRegistryHolder.streamAll(stream).forEach(key -> existingRegistry.getOptionalValue((RegistryKey<DimensionOptions>)key).or(() -> Optional.ofNullable(this.dimensions.get(key))).ifPresent(dimensionOptions -> list.add(new Entry((RegistryKey<DimensionOptions>)key, (DimensionOptions)dimensionOptions))));
        Lifecycle lifecycle = list.size() == VANILLA_KEY_COUNT ? Lifecycle.stable() : Lifecycle.experimental();
        SimpleRegistry<DimensionOptions> mutableRegistry = new SimpleRegistry<DimensionOptions>(RegistryKeys.DIMENSION, lifecycle);
        list.forEach(entry -> mutableRegistry.add(entry.key, entry.value, entry.toEntryInfo()));
        Registry<DimensionOptions> registry = mutableRegistry.freeze();
        LevelProperties.SpecialProperty specialProperty = DimensionOptionsRegistryHolder.getSpecialProperty(registry);
        return new DimensionsConfig(registry.freeze(), specialProperty);
    }

    public record DimensionsConfig(Registry<DimensionOptions> dimensions, LevelProperties.SpecialProperty specialWorldProperty) {
        public Lifecycle getLifecycle() {
            return this.dimensions.getLifecycle();
        }

        public DynamicRegistryManager.Immutable toDynamicRegistryManager() {
            return new DynamicRegistryManager.ImmutableImpl(List.of(this.dimensions)).toImmutable();
        }
    }
}


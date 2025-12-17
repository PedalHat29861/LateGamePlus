/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  com.google.common.collect.ImmutableList
 *  com.google.common.collect.ImmutableList$Builder
 *  com.mojang.datafixers.kinds.App
 *  com.mojang.datafixers.kinds.Applicative
 *  com.mojang.datafixers.util.Pair
 *  com.mojang.serialization.Codec
 *  com.mojang.serialization.DataResult
 *  com.mojang.serialization.codecs.RecordCodecBuilder
 */
package net.minecraft.world.biome.source;

import com.google.common.collect.ImmutableList;
import com.mojang.datafixers.kinds.App;
import com.mojang.datafixers.kinds.Applicative;
import com.mojang.datafixers.util.Pair;
import com.mojang.serialization.Codec;
import com.mojang.serialization.DataResult;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import java.lang.invoke.MethodHandle;
import java.lang.runtime.ObjectMethods;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.function.Function;
import java.util.stream.Collectors;
import java.util.stream.Stream;
import net.minecraft.registry.RegistryEntryLookup;
import net.minecraft.registry.RegistryKey;
import net.minecraft.registry.RegistryKeys;
import net.minecraft.registry.RegistryOps;
import net.minecraft.registry.entry.RegistryElementCodec;
import net.minecraft.registry.entry.RegistryEntry;
import net.minecraft.util.Identifier;
import net.minecraft.world.biome.Biome;
import net.minecraft.world.biome.BiomeKeys;
import net.minecraft.world.biome.source.util.MultiNoiseUtil;
import net.minecraft.world.biome.source.util.VanillaBiomeParameters;

public class MultiNoiseBiomeSourceParameterList {
    public static final Codec<MultiNoiseBiomeSourceParameterList> CODEC = RecordCodecBuilder.create(instance -> instance.group((App)Preset.CODEC.fieldOf("preset").forGetter(multiNoiseBiomeSourceParameterList -> multiNoiseBiomeSourceParameterList.preset), RegistryOps.getEntryLookupCodec(RegistryKeys.BIOME)).apply((Applicative)instance, MultiNoiseBiomeSourceParameterList::new));
    public static final Codec<RegistryEntry<MultiNoiseBiomeSourceParameterList>> REGISTRY_CODEC = RegistryElementCodec.of(RegistryKeys.MULTI_NOISE_BIOME_SOURCE_PARAMETER_LIST, CODEC);
    private final Preset preset;
    private final MultiNoiseUtil.Entries<RegistryEntry<Biome>> entries;

    public MultiNoiseBiomeSourceParameterList(Preset preset, RegistryEntryLookup<Biome> biomeLookup) {
        this.preset = preset;
        this.entries = preset.biomeSourceFunction.apply(biomeLookup::getOrThrow);
    }

    public MultiNoiseUtil.Entries<RegistryEntry<Biome>> getEntries() {
        return this.entries;
    }

    public static Map<Preset, MultiNoiseUtil.Entries<RegistryKey<Biome>>> getPresetToEntriesMap() {
        return Preset.BY_IDENTIFIER.values().stream().collect(Collectors.toMap(preset -> preset, preset -> preset.biomeSourceFunction().apply(biomeKey -> biomeKey)));
    }

    public static final class Preset
    extends Record {
        private final Identifier id;
        final BiomeSourceFunction biomeSourceFunction;
        public static final Preset NETHER = new Preset(Identifier.ofVanilla("nether"), new BiomeSourceFunction(){

            @Override
            public <T> MultiNoiseUtil.Entries<T> apply(Function<RegistryKey<Biome>, T> function) {
                return new MultiNoiseUtil.Entries(List.of(Pair.of((Object)MultiNoiseUtil.createNoiseHypercube(0.0f, 0.0f, 0.0f, 0.0f, 0.0f, 0.0f, 0.0f), function.apply(BiomeKeys.NETHER_WASTES)), Pair.of((Object)MultiNoiseUtil.createNoiseHypercube(0.0f, -0.5f, 0.0f, 0.0f, 0.0f, 0.0f, 0.0f), function.apply(BiomeKeys.SOUL_SAND_VALLEY)), Pair.of((Object)MultiNoiseUtil.createNoiseHypercube(0.4f, 0.0f, 0.0f, 0.0f, 0.0f, 0.0f, 0.0f), function.apply(BiomeKeys.CRIMSON_FOREST)), Pair.of((Object)MultiNoiseUtil.createNoiseHypercube(0.0f, 0.5f, 0.0f, 0.0f, 0.0f, 0.0f, 0.375f), function.apply(BiomeKeys.WARPED_FOREST)), Pair.of((Object)MultiNoiseUtil.createNoiseHypercube(-0.5f, 0.0f, 0.0f, 0.0f, 0.0f, 0.0f, 0.175f), function.apply(BiomeKeys.BASALT_DELTAS))));
            }
        });
        public static final Preset OVERWORLD = new Preset(Identifier.ofVanilla("overworld"), new BiomeSourceFunction(){

            @Override
            public <T> MultiNoiseUtil.Entries<T> apply(Function<RegistryKey<Biome>, T> function) {
                return Preset.getOverworldEntries(function);
            }
        });
        static final Map<Identifier, Preset> BY_IDENTIFIER = Stream.of(NETHER, OVERWORLD).collect(Collectors.toMap(Preset::id, preset -> preset));
        public static final Codec<Preset> CODEC = Identifier.CODEC.flatXmap(id -> Optional.ofNullable(BY_IDENTIFIER.get(id)).map(DataResult::success).orElseGet(() -> DataResult.error(() -> "Unknown preset: " + String.valueOf(id))), preset -> DataResult.success((Object)preset.id));

        public Preset(Identifier id, BiomeSourceFunction biomeSourceFunction) {
            this.id = id;
            this.biomeSourceFunction = biomeSourceFunction;
        }

        static <T> MultiNoiseUtil.Entries<T> getOverworldEntries(Function<RegistryKey<Biome>, T> biomeEntryGetter) {
            ImmutableList.Builder builder = ImmutableList.builder();
            new VanillaBiomeParameters().writeOverworldBiomeParameters(pair -> builder.add((Object)pair.mapSecond(biomeEntryGetter)));
            return new MultiNoiseUtil.Entries(builder.build());
        }

        public Stream<RegistryKey<Biome>> biomeStream() {
            return this.biomeSourceFunction.apply(biomeKey -> biomeKey).getEntries().stream().map(Pair::getSecond).distinct();
        }

        @Override
        public final String toString() {
            return ObjectMethods.bootstrap("toString", new MethodHandle[]{Preset.class, "id;provider", "id", "biomeSourceFunction"}, this);
        }

        @Override
        public final int hashCode() {
            return (int)ObjectMethods.bootstrap("hashCode", new MethodHandle[]{Preset.class, "id;provider", "id", "biomeSourceFunction"}, this);
        }

        @Override
        public final boolean equals(Object object) {
            return (boolean)ObjectMethods.bootstrap("equals", new MethodHandle[]{Preset.class, "id;provider", "id", "biomeSourceFunction"}, this, object);
        }

        public Identifier id() {
            return this.id;
        }

        public BiomeSourceFunction biomeSourceFunction() {
            return this.biomeSourceFunction;
        }

        @FunctionalInterface
        static interface BiomeSourceFunction {
            public <T> MultiNoiseUtil.Entries<T> apply(Function<RegistryKey<Biome>, T> var1);
        }
    }
}


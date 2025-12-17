/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  com.mojang.datafixers.kinds.App
 *  com.mojang.datafixers.kinds.Applicative
 *  com.mojang.serialization.Codec
 *  com.mojang.serialization.MapCodec
 *  com.mojang.serialization.codecs.RecordCodecBuilder
 */
package net.minecraft.world.biome.source;

import com.mojang.datafixers.kinds.App;
import com.mojang.datafixers.kinds.Applicative;
import com.mojang.serialization.Codec;
import com.mojang.serialization.MapCodec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import java.util.stream.Stream;
import net.minecraft.registry.entry.RegistryEntry;
import net.minecraft.registry.entry.RegistryEntryList;
import net.minecraft.world.biome.Biome;
import net.minecraft.world.biome.source.BiomeSource;
import net.minecraft.world.biome.source.util.MultiNoiseUtil;

public class CheckerboardBiomeSource
extends BiomeSource {
    public static final MapCodec<CheckerboardBiomeSource> CODEC = RecordCodecBuilder.mapCodec(instance -> instance.group((App)Biome.REGISTRY_ENTRY_LIST_CODEC.fieldOf("biomes").forGetter(biomeSource -> biomeSource.biomeArray), (App)Codec.intRange((int)0, (int)62).fieldOf("scale").orElse((Object)2).forGetter(biomeSource -> biomeSource.scale)).apply((Applicative)instance, CheckerboardBiomeSource::new));
    private final RegistryEntryList<Biome> biomeArray;
    private final int gridSize;
    private final int scale;

    public CheckerboardBiomeSource(RegistryEntryList<Biome> biomes, int size) {
        this.biomeArray = biomes;
        this.gridSize = size + 2;
        this.scale = size;
    }

    @Override
    protected Stream<RegistryEntry<Biome>> biomeStream() {
        return this.biomeArray.stream();
    }

    @Override
    protected MapCodec<? extends BiomeSource> getCodec() {
        return CODEC;
    }

    @Override
    public RegistryEntry<Biome> getBiome(int x, int y, int z, MultiNoiseUtil.MultiNoiseSampler noise) {
        return this.biomeArray.get(Math.floorMod((x >> this.gridSize) + (z >> this.gridSize), this.biomeArray.size()));
    }
}


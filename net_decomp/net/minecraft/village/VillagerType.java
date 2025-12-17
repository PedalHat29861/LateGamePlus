/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  com.google.common.collect.Maps
 *  com.mojang.serialization.Codec
 */
package net.minecraft.village;

import com.google.common.collect.Maps;
import com.mojang.serialization.Codec;
import java.util.Map;
import net.minecraft.network.RegistryByteBuf;
import net.minecraft.network.codec.PacketCodec;
import net.minecraft.network.codec.PacketCodecs;
import net.minecraft.registry.Registry;
import net.minecraft.registry.RegistryKey;
import net.minecraft.registry.RegistryKeys;
import net.minecraft.registry.entry.RegistryEntry;
import net.minecraft.registry.entry.RegistryFixedCodec;
import net.minecraft.util.Identifier;
import net.minecraft.util.Util;
import net.minecraft.world.biome.Biome;
import net.minecraft.world.biome.BiomeKeys;

public final class VillagerType {
    public static final RegistryKey<VillagerType> DESERT = VillagerType.of("desert");
    public static final RegistryKey<VillagerType> JUNGLE = VillagerType.of("jungle");
    public static final RegistryKey<VillagerType> PLAINS = VillagerType.of("plains");
    public static final RegistryKey<VillagerType> SAVANNA = VillagerType.of("savanna");
    public static final RegistryKey<VillagerType> SNOW = VillagerType.of("snow");
    public static final RegistryKey<VillagerType> SWAMP = VillagerType.of("swamp");
    public static final RegistryKey<VillagerType> TAIGA = VillagerType.of("taiga");
    public static final Codec<RegistryEntry<VillagerType>> CODEC = RegistryFixedCodec.of(RegistryKeys.VILLAGER_TYPE);
    public static final PacketCodec<RegistryByteBuf, RegistryEntry<VillagerType>> PACKET_CODEC = PacketCodecs.registryEntry(RegistryKeys.VILLAGER_TYPE);
    private static final Map<RegistryKey<Biome>, RegistryKey<VillagerType>> BIOME_TO_TYPE = Util.make(Maps.newHashMap(), map -> {
        map.put(BiomeKeys.BADLANDS, DESERT);
        map.put(BiomeKeys.DESERT, DESERT);
        map.put(BiomeKeys.ERODED_BADLANDS, DESERT);
        map.put(BiomeKeys.WOODED_BADLANDS, DESERT);
        map.put(BiomeKeys.BAMBOO_JUNGLE, JUNGLE);
        map.put(BiomeKeys.JUNGLE, JUNGLE);
        map.put(BiomeKeys.SPARSE_JUNGLE, JUNGLE);
        map.put(BiomeKeys.SAVANNA_PLATEAU, SAVANNA);
        map.put(BiomeKeys.SAVANNA, SAVANNA);
        map.put(BiomeKeys.WINDSWEPT_SAVANNA, SAVANNA);
        map.put(BiomeKeys.DEEP_FROZEN_OCEAN, SNOW);
        map.put(BiomeKeys.FROZEN_OCEAN, SNOW);
        map.put(BiomeKeys.FROZEN_RIVER, SNOW);
        map.put(BiomeKeys.ICE_SPIKES, SNOW);
        map.put(BiomeKeys.SNOWY_BEACH, SNOW);
        map.put(BiomeKeys.SNOWY_TAIGA, SNOW);
        map.put(BiomeKeys.SNOWY_PLAINS, SNOW);
        map.put(BiomeKeys.GROVE, SNOW);
        map.put(BiomeKeys.SNOWY_SLOPES, SNOW);
        map.put(BiomeKeys.FROZEN_PEAKS, SNOW);
        map.put(BiomeKeys.JAGGED_PEAKS, SNOW);
        map.put(BiomeKeys.SWAMP, SWAMP);
        map.put(BiomeKeys.MANGROVE_SWAMP, SWAMP);
        map.put(BiomeKeys.OLD_GROWTH_SPRUCE_TAIGA, TAIGA);
        map.put(BiomeKeys.OLD_GROWTH_PINE_TAIGA, TAIGA);
        map.put(BiomeKeys.WINDSWEPT_GRAVELLY_HILLS, TAIGA);
        map.put(BiomeKeys.WINDSWEPT_HILLS, TAIGA);
        map.put(BiomeKeys.TAIGA, TAIGA);
        map.put(BiomeKeys.WINDSWEPT_FOREST, TAIGA);
    });

    private static RegistryKey<VillagerType> of(String id) {
        return RegistryKey.of(RegistryKeys.VILLAGER_TYPE, Identifier.ofVanilla(id));
    }

    private static VillagerType create(Registry<VillagerType> registry, RegistryKey<VillagerType> key) {
        return Registry.register(registry, key, new VillagerType());
    }

    public static VillagerType registerAndGetDefault(Registry<VillagerType> registry) {
        VillagerType.create(registry, DESERT);
        VillagerType.create(registry, JUNGLE);
        VillagerType.create(registry, PLAINS);
        VillagerType.create(registry, SAVANNA);
        VillagerType.create(registry, SNOW);
        VillagerType.create(registry, SWAMP);
        return VillagerType.create(registry, TAIGA);
    }

    public static RegistryKey<VillagerType> forBiome(RegistryEntry<Biome> biomeEntry) {
        return biomeEntry.getKey().map(BIOME_TO_TYPE::get).orElse(PLAINS);
    }
}


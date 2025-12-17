/*
 * Decompiled with CFR 0.152.
 */
package net.minecraft.data.tag.vanilla;

import java.util.Collection;
import java.util.List;
import java.util.concurrent.CompletableFuture;
import net.minecraft.data.DataOutput;
import net.minecraft.data.tag.SimpleTagProvider;
import net.minecraft.registry.RegistryKey;
import net.minecraft.registry.RegistryKeys;
import net.minecraft.registry.RegistryWrapper;
import net.minecraft.registry.tag.BiomeTags;
import net.minecraft.world.biome.Biome;
import net.minecraft.world.biome.BiomeKeys;
import net.minecraft.world.biome.source.MultiNoiseBiomeSourceParameterList;

public class VanillaBiomeTagProvider
extends SimpleTagProvider<Biome> {
    public VanillaBiomeTagProvider(DataOutput output, CompletableFuture<RegistryWrapper.WrapperLookup> registriesFuture) {
        super(output, RegistryKeys.BIOME, registriesFuture);
    }

    @Override
    protected void configure(RegistryWrapper.WrapperLookup registries) {
        this.builder(BiomeTags.IS_DEEP_OCEAN).add(BiomeKeys.DEEP_FROZEN_OCEAN).add(BiomeKeys.DEEP_COLD_OCEAN).add(BiomeKeys.DEEP_OCEAN).add(BiomeKeys.DEEP_LUKEWARM_OCEAN);
        this.builder(BiomeTags.IS_OCEAN).addTag(BiomeTags.IS_DEEP_OCEAN).add(BiomeKeys.FROZEN_OCEAN).add(BiomeKeys.OCEAN).add(BiomeKeys.COLD_OCEAN).add(BiomeKeys.LUKEWARM_OCEAN).add(BiomeKeys.WARM_OCEAN);
        this.builder(BiomeTags.IS_BEACH).add(BiomeKeys.BEACH).add(BiomeKeys.SNOWY_BEACH);
        this.builder(BiomeTags.IS_RIVER).add(BiomeKeys.RIVER).add(BiomeKeys.FROZEN_RIVER);
        this.builder(BiomeTags.IS_MOUNTAIN).add(BiomeKeys.MEADOW).add(BiomeKeys.FROZEN_PEAKS).add(BiomeKeys.JAGGED_PEAKS).add(BiomeKeys.STONY_PEAKS).add(BiomeKeys.SNOWY_SLOPES).add(BiomeKeys.CHERRY_GROVE);
        this.builder(BiomeTags.IS_BADLANDS).add(BiomeKeys.BADLANDS).add(BiomeKeys.ERODED_BADLANDS).add(BiomeKeys.WOODED_BADLANDS);
        this.builder(BiomeTags.IS_HILL).add(BiomeKeys.WINDSWEPT_HILLS).add(BiomeKeys.WINDSWEPT_FOREST).add(BiomeKeys.WINDSWEPT_GRAVELLY_HILLS);
        this.builder(BiomeTags.IS_TAIGA).add(BiomeKeys.TAIGA).add(BiomeKeys.SNOWY_TAIGA).add(BiomeKeys.OLD_GROWTH_PINE_TAIGA).add(BiomeKeys.OLD_GROWTH_SPRUCE_TAIGA);
        this.builder(BiomeTags.IS_JUNGLE).add(BiomeKeys.BAMBOO_JUNGLE).add(BiomeKeys.JUNGLE).add(BiomeKeys.SPARSE_JUNGLE);
        this.builder(BiomeTags.IS_FOREST).add(BiomeKeys.FOREST).add(BiomeKeys.FLOWER_FOREST).add(BiomeKeys.BIRCH_FOREST).add(BiomeKeys.OLD_GROWTH_BIRCH_FOREST).add(BiomeKeys.DARK_FOREST).add(BiomeKeys.PALE_GARDEN).add(BiomeKeys.GROVE);
        this.builder(BiomeTags.IS_SAVANNA).add(BiomeKeys.SAVANNA).add(BiomeKeys.SAVANNA_PLATEAU).add(BiomeKeys.WINDSWEPT_SAVANNA);
        this.builder(BiomeTags.IS_NETHER).add(MultiNoiseBiomeSourceParameterList.Preset.NETHER.biomeStream());
        List<RegistryKey<Biome>> list = MultiNoiseBiomeSourceParameterList.Preset.OVERWORLD.biomeStream().toList();
        this.builder(BiomeTags.IS_OVERWORLD).add((Collection<RegistryKey<Biome>>)list);
        this.builder(BiomeTags.IS_END).add(BiomeKeys.THE_END).add(BiomeKeys.END_HIGHLANDS).add(BiomeKeys.END_MIDLANDS).add(BiomeKeys.SMALL_END_ISLANDS).add(BiomeKeys.END_BARRENS);
        this.builder(BiomeTags.BURIED_TREASURE_HAS_STRUCTURE).addTag(BiomeTags.IS_BEACH);
        this.builder(BiomeTags.DESERT_PYRAMID_HAS_STRUCTURE).add(BiomeKeys.DESERT);
        this.builder(BiomeTags.IGLOO_HAS_STRUCTURE).add(BiomeKeys.SNOWY_TAIGA).add(BiomeKeys.SNOWY_PLAINS).add(BiomeKeys.SNOWY_SLOPES);
        this.builder(BiomeTags.JUNGLE_TEMPLE_HAS_STRUCTURE).add(BiomeKeys.BAMBOO_JUNGLE).add(BiomeKeys.JUNGLE);
        this.builder(BiomeTags.MINESHAFT_HAS_STRUCTURE).addTag(BiomeTags.IS_OCEAN).addTag(BiomeTags.IS_RIVER).addTag(BiomeTags.IS_BEACH).addTag(BiomeTags.IS_MOUNTAIN).addTag(BiomeTags.IS_HILL).addTag(BiomeTags.IS_TAIGA).addTag(BiomeTags.IS_JUNGLE).addTag(BiomeTags.IS_FOREST).add(BiomeKeys.STONY_SHORE).add(BiomeKeys.MUSHROOM_FIELDS).add(BiomeKeys.ICE_SPIKES).add(BiomeKeys.WINDSWEPT_SAVANNA).add(BiomeKeys.DESERT).add(BiomeKeys.SAVANNA).add(BiomeKeys.SNOWY_PLAINS).add(BiomeKeys.PLAINS).add(BiomeKeys.SUNFLOWER_PLAINS).add(BiomeKeys.SWAMP).add(BiomeKeys.MANGROVE_SWAMP).add(BiomeKeys.SAVANNA_PLATEAU).add(BiomeKeys.DRIPSTONE_CAVES).add(BiomeKeys.LUSH_CAVES);
        this.builder(BiomeTags.MINESHAFT_MESA_HAS_STRUCTURE).addTag(BiomeTags.IS_BADLANDS);
        this.builder(BiomeTags.MINESHAFT_BLOCKING).add(BiomeKeys.DEEP_DARK);
        this.builder(BiomeTags.OCEAN_MONUMENT_HAS_STRUCTURE).addTag(BiomeTags.IS_DEEP_OCEAN);
        this.builder(BiomeTags.REQUIRED_OCEAN_MONUMENT_SURROUNDING).addTag(BiomeTags.IS_OCEAN).addTag(BiomeTags.IS_RIVER);
        this.builder(BiomeTags.OCEAN_RUIN_COLD_HAS_STRUCTURE).add(BiomeKeys.FROZEN_OCEAN).add(BiomeKeys.COLD_OCEAN).add(BiomeKeys.OCEAN).add(BiomeKeys.DEEP_FROZEN_OCEAN).add(BiomeKeys.DEEP_COLD_OCEAN).add(BiomeKeys.DEEP_OCEAN);
        this.builder(BiomeTags.OCEAN_RUIN_WARM_HAS_STRUCTURE).add(BiomeKeys.LUKEWARM_OCEAN).add(BiomeKeys.WARM_OCEAN).add(BiomeKeys.DEEP_LUKEWARM_OCEAN);
        this.builder(BiomeTags.PILLAGER_OUTPOST_HAS_STRUCTURE).add(BiomeKeys.DESERT).add(BiomeKeys.PLAINS).add(BiomeKeys.SAVANNA).add(BiomeKeys.SNOWY_PLAINS).add(BiomeKeys.TAIGA).addTag(BiomeTags.IS_MOUNTAIN).add(BiomeKeys.GROVE);
        this.builder(BiomeTags.RUINED_PORTAL_DESERT_HAS_STRUCTURE).add(BiomeKeys.DESERT);
        this.builder(BiomeTags.RUINED_PORTAL_JUNGLE_HAS_STRUCTURE).addTag(BiomeTags.IS_JUNGLE);
        this.builder(BiomeTags.RUINED_PORTAL_OCEAN_HAS_STRUCTURE).addTag(BiomeTags.IS_OCEAN);
        this.builder(BiomeTags.RUINED_PORTAL_SWAMP_HAS_STRUCTURE).add(BiomeKeys.SWAMP).add(BiomeKeys.MANGROVE_SWAMP);
        this.builder(BiomeTags.RUINED_PORTAL_MOUNTAIN_HAS_STRUCTURE).addTag(BiomeTags.IS_BADLANDS).addTag(BiomeTags.IS_HILL).add(BiomeKeys.SAVANNA_PLATEAU).add(BiomeKeys.WINDSWEPT_SAVANNA).add(BiomeKeys.STONY_SHORE).addTag(BiomeTags.IS_MOUNTAIN);
        this.builder(BiomeTags.RUINED_PORTAL_STANDARD_HAS_STRUCTURE).addTag(BiomeTags.IS_BEACH).addTag(BiomeTags.IS_RIVER).addTag(BiomeTags.IS_TAIGA).addTag(BiomeTags.IS_FOREST).add(BiomeKeys.MUSHROOM_FIELDS).add(BiomeKeys.ICE_SPIKES).add(BiomeKeys.DRIPSTONE_CAVES).add(BiomeKeys.LUSH_CAVES).add(BiomeKeys.SAVANNA).add(BiomeKeys.SNOWY_PLAINS).add(BiomeKeys.PLAINS).add(BiomeKeys.SUNFLOWER_PLAINS);
        this.builder(BiomeTags.SHIPWRECK_BEACHED_HAS_STRUCTURE).addTag(BiomeTags.IS_BEACH);
        this.builder(BiomeTags.SHIPWRECK_HAS_STRUCTURE).addTag(BiomeTags.IS_OCEAN);
        this.builder(BiomeTags.SWAMP_HUT_HAS_STRUCTURE).add(BiomeKeys.SWAMP);
        this.builder(BiomeTags.VILLAGE_DESERT_HAS_STRUCTURE).add(BiomeKeys.DESERT);
        this.builder(BiomeTags.VILLAGE_PLAINS_HAS_STRUCTURE).add(BiomeKeys.PLAINS).add(BiomeKeys.MEADOW);
        this.builder(BiomeTags.VILLAGE_SAVANNA_HAS_STRUCTURE).add(BiomeKeys.SAVANNA);
        this.builder(BiomeTags.VILLAGE_SNOWY_HAS_STRUCTURE).add(BiomeKeys.SNOWY_PLAINS);
        this.builder(BiomeTags.VILLAGE_TAIGA_HAS_STRUCTURE).add(BiomeKeys.TAIGA);
        this.builder(BiomeTags.TRAIL_RUINS_HAS_STRUCTURE).add(BiomeKeys.TAIGA).add(BiomeKeys.SNOWY_TAIGA).add(BiomeKeys.OLD_GROWTH_PINE_TAIGA).add(BiomeKeys.OLD_GROWTH_SPRUCE_TAIGA).add(BiomeKeys.OLD_GROWTH_BIRCH_FOREST).add(BiomeKeys.JUNGLE);
        this.builder(BiomeTags.WOODLAND_MANSION_HAS_STRUCTURE).add(BiomeKeys.DARK_FOREST).add(BiomeKeys.PALE_GARDEN);
        this.builder(BiomeTags.STRONGHOLD_BIASED_TO).add(BiomeKeys.PLAINS).add(BiomeKeys.SUNFLOWER_PLAINS).add(BiomeKeys.SNOWY_PLAINS).add(BiomeKeys.ICE_SPIKES).add(BiomeKeys.DESERT).add(BiomeKeys.FOREST).add(BiomeKeys.FLOWER_FOREST).add(BiomeKeys.BIRCH_FOREST).add(BiomeKeys.DARK_FOREST).add(BiomeKeys.PALE_GARDEN).add(BiomeKeys.OLD_GROWTH_BIRCH_FOREST).add(BiomeKeys.OLD_GROWTH_PINE_TAIGA).add(BiomeKeys.OLD_GROWTH_SPRUCE_TAIGA).add(BiomeKeys.TAIGA).add(BiomeKeys.SNOWY_TAIGA).add(BiomeKeys.SAVANNA).add(BiomeKeys.SAVANNA_PLATEAU).add(BiomeKeys.WINDSWEPT_HILLS).add(BiomeKeys.WINDSWEPT_GRAVELLY_HILLS).add(BiomeKeys.WINDSWEPT_FOREST).add(BiomeKeys.WINDSWEPT_SAVANNA).add(BiomeKeys.JUNGLE).add(BiomeKeys.SPARSE_JUNGLE).add(BiomeKeys.BAMBOO_JUNGLE).add(BiomeKeys.BADLANDS).add(BiomeKeys.ERODED_BADLANDS).add(BiomeKeys.WOODED_BADLANDS).add(BiomeKeys.MEADOW).add(BiomeKeys.CHERRY_GROVE).add(BiomeKeys.GROVE).add(BiomeKeys.SNOWY_SLOPES).add(BiomeKeys.FROZEN_PEAKS).add(BiomeKeys.JAGGED_PEAKS).add(BiomeKeys.STONY_PEAKS).add(BiomeKeys.MUSHROOM_FIELDS).add(BiomeKeys.DRIPSTONE_CAVES).add(BiomeKeys.LUSH_CAVES);
        this.builder(BiomeTags.STRONGHOLD_HAS_STRUCTURE).addTag(BiomeTags.IS_OVERWORLD);
        this.builder(BiomeTags.TRIAL_CHAMBERS_HAS_STRUCTURE).add(list.stream().filter(registryKey -> registryKey != BiomeKeys.DEEP_DARK));
        this.builder(BiomeTags.NETHER_FORTRESS_HAS_STRUCTURE).addTag(BiomeTags.IS_NETHER);
        this.builder(BiomeTags.NETHER_FOSSIL_HAS_STRUCTURE).add(BiomeKeys.SOUL_SAND_VALLEY);
        this.builder(BiomeTags.BASTION_REMNANT_HAS_STRUCTURE).add(BiomeKeys.CRIMSON_FOREST).add(BiomeKeys.NETHER_WASTES).add(BiomeKeys.SOUL_SAND_VALLEY).add(BiomeKeys.WARPED_FOREST);
        this.builder(BiomeTags.ANCIENT_CITY_HAS_STRUCTURE).add(BiomeKeys.DEEP_DARK);
        this.builder(BiomeTags.RUINED_PORTAL_NETHER_HAS_STRUCTURE).addTag(BiomeTags.IS_NETHER);
        this.builder(BiomeTags.END_CITY_HAS_STRUCTURE).add(BiomeKeys.END_HIGHLANDS).add(BiomeKeys.END_MIDLANDS);
        this.builder(BiomeTags.PRODUCES_CORALS_FROM_BONEMEAL).add(BiomeKeys.WARM_OCEAN);
        this.builder(BiomeTags.WATER_ON_MAP_OUTLINES).addTag(BiomeTags.IS_OCEAN).addTag(BiomeTags.IS_RIVER).add(BiomeKeys.SWAMP).add(BiomeKeys.MANGROVE_SWAMP);
        this.builder(BiomeTags.WITHOUT_ZOMBIE_SIEGES).add(BiomeKeys.MUSHROOM_FIELDS);
        this.builder(BiomeTags.WITHOUT_WANDERING_TRADER_SPAWNS).add(BiomeKeys.THE_VOID);
        this.builder(BiomeTags.SPAWNS_COLD_VARIANT_FROGS).add(BiomeKeys.SNOWY_PLAINS).add(BiomeKeys.ICE_SPIKES).add(BiomeKeys.FROZEN_PEAKS).add(BiomeKeys.JAGGED_PEAKS).add(BiomeKeys.SNOWY_SLOPES).add(BiomeKeys.FROZEN_OCEAN).add(BiomeKeys.DEEP_FROZEN_OCEAN).add(BiomeKeys.GROVE).add(BiomeKeys.DEEP_DARK).add(BiomeKeys.FROZEN_RIVER).add(BiomeKeys.SNOWY_TAIGA).add(BiomeKeys.SNOWY_BEACH).addTag(BiomeTags.IS_END);
        this.builder(BiomeTags.SPAWNS_WARM_VARIANT_FROGS).add(BiomeKeys.DESERT).add(BiomeKeys.WARM_OCEAN).addTag(BiomeTags.IS_JUNGLE).addTag(BiomeTags.IS_SAVANNA).addTag(BiomeTags.IS_NETHER).addTag(BiomeTags.IS_BADLANDS).add(BiomeKeys.MANGROVE_SWAMP);
        this.builder(BiomeTags.SPAWNS_COLD_VARIANT_FARM_ANIMALS).add(BiomeKeys.SNOWY_PLAINS).add(BiomeKeys.ICE_SPIKES).add(BiomeKeys.FROZEN_PEAKS).add(BiomeKeys.JAGGED_PEAKS).add(BiomeKeys.SNOWY_SLOPES).add(BiomeKeys.FROZEN_OCEAN).add(BiomeKeys.DEEP_FROZEN_OCEAN).add(BiomeKeys.GROVE).add(BiomeKeys.DEEP_DARK).add(BiomeKeys.FROZEN_RIVER).add(BiomeKeys.SNOWY_TAIGA).add(BiomeKeys.SNOWY_BEACH).addTag(BiomeTags.IS_END).add(BiomeKeys.COLD_OCEAN).add(BiomeKeys.DEEP_COLD_OCEAN).add(BiomeKeys.OLD_GROWTH_PINE_TAIGA).add(BiomeKeys.OLD_GROWTH_SPRUCE_TAIGA).add(BiomeKeys.TAIGA).add(BiomeKeys.WINDSWEPT_FOREST).add(BiomeKeys.WINDSWEPT_GRAVELLY_HILLS).add(BiomeKeys.WINDSWEPT_HILLS).add(BiomeKeys.STONY_PEAKS);
        this.builder(BiomeTags.SPAWNS_WARM_VARIANT_FARM_ANIMALS).add(BiomeKeys.DESERT).add(BiomeKeys.WARM_OCEAN).addTag(BiomeTags.IS_JUNGLE).addTag(BiomeTags.IS_SAVANNA).addTag(BiomeTags.IS_NETHER).addTag(BiomeTags.IS_BADLANDS).add(BiomeKeys.MANGROVE_SWAMP).add(BiomeKeys.DEEP_LUKEWARM_OCEAN).add(BiomeKeys.LUKEWARM_OCEAN);
        this.builder(BiomeTags.SPAWNS_GOLD_RABBITS).add(BiomeKeys.DESERT);
        this.builder(BiomeTags.SPAWNS_WHITE_RABBITS).add(BiomeKeys.SNOWY_PLAINS).add(BiomeKeys.ICE_SPIKES).add(BiomeKeys.FROZEN_OCEAN).add(BiomeKeys.SNOWY_TAIGA).add(BiomeKeys.FROZEN_RIVER).add(BiomeKeys.SNOWY_BEACH).add(BiomeKeys.FROZEN_PEAKS).add(BiomeKeys.JAGGED_PEAKS).add(BiomeKeys.SNOWY_SLOPES).add(BiomeKeys.GROVE);
        this.builder(BiomeTags.REDUCE_WATER_AMBIENT_SPAWNS).addTag(BiomeTags.IS_RIVER);
        this.builder(BiomeTags.ALLOWS_TROPICAL_FISH_SPAWNS_AT_ANY_HEIGHT).add(BiomeKeys.LUSH_CAVES);
        this.builder(BiomeTags.POLAR_BEARS_SPAWN_ON_ALTERNATE_BLOCKS).add(BiomeKeys.FROZEN_OCEAN).add(BiomeKeys.DEEP_FROZEN_OCEAN);
        this.builder(BiomeTags.MORE_FREQUENT_DROWNED_SPAWNS).addTag(BiomeTags.IS_RIVER);
        this.builder(BiomeTags.ALLOWS_SURFACE_SLIME_SPAWNS).add(BiomeKeys.SWAMP).add(BiomeKeys.MANGROVE_SWAMP);
        this.builder(BiomeTags.SPAWNS_SNOW_FOXES).add(BiomeKeys.SNOWY_PLAINS).add(BiomeKeys.ICE_SPIKES).add(BiomeKeys.FROZEN_OCEAN).add(BiomeKeys.SNOWY_TAIGA).add(BiomeKeys.FROZEN_RIVER).add(BiomeKeys.SNOWY_BEACH).add(BiomeKeys.FROZEN_PEAKS).add(BiomeKeys.JAGGED_PEAKS).add(BiomeKeys.SNOWY_SLOPES).add(BiomeKeys.GROVE);
        this.builder(BiomeTags.SPAWNS_CORAL_VARIANT_ZOMBIE_NAUTILUS).add(BiomeKeys.WARM_OCEAN);
    }
}


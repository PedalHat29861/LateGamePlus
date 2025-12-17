/*
 * Decompiled with CFR 0.152.
 */
package net.minecraft.world.gen.feature;

import net.minecraft.entity.EntityType;
import net.minecraft.entity.SpawnGroup;
import net.minecraft.world.biome.GenerationSettings;
import net.minecraft.world.biome.SpawnSettings;
import net.minecraft.world.gen.GenerationStep;
import net.minecraft.world.gen.carver.ConfiguredCarvers;
import net.minecraft.world.gen.feature.MiscPlacedFeatures;
import net.minecraft.world.gen.feature.OceanPlacedFeatures;
import net.minecraft.world.gen.feature.OrePlacedFeatures;
import net.minecraft.world.gen.feature.UndergroundPlacedFeatures;
import net.minecraft.world.gen.feature.VegetationPlacedFeatures;

public class DefaultBiomeFeatures {
    public static void addLandCarvers(GenerationSettings.LookupBackedBuilder builder) {
        builder.carver(ConfiguredCarvers.CAVE);
        builder.carver(ConfiguredCarvers.CAVE_EXTRA_UNDERGROUND);
        builder.carver(ConfiguredCarvers.CANYON);
        builder.feature(GenerationStep.Feature.LAKES, MiscPlacedFeatures.LAKE_LAVA_UNDERGROUND);
        builder.feature(GenerationStep.Feature.LAKES, MiscPlacedFeatures.LAKE_LAVA_SURFACE);
    }

    public static void addDungeons(GenerationSettings.LookupBackedBuilder builder) {
        builder.feature(GenerationStep.Feature.UNDERGROUND_STRUCTURES, UndergroundPlacedFeatures.MONSTER_ROOM);
        builder.feature(GenerationStep.Feature.UNDERGROUND_STRUCTURES, UndergroundPlacedFeatures.MONSTER_ROOM_DEEP);
    }

    public static void addMineables(GenerationSettings.LookupBackedBuilder builder) {
        builder.feature(GenerationStep.Feature.UNDERGROUND_ORES, OrePlacedFeatures.ORE_DIRT);
        builder.feature(GenerationStep.Feature.UNDERGROUND_ORES, OrePlacedFeatures.ORE_GRAVEL);
        builder.feature(GenerationStep.Feature.UNDERGROUND_ORES, OrePlacedFeatures.ORE_GRANITE_UPPER);
        builder.feature(GenerationStep.Feature.UNDERGROUND_ORES, OrePlacedFeatures.ORE_GRANITE_LOWER);
        builder.feature(GenerationStep.Feature.UNDERGROUND_ORES, OrePlacedFeatures.ORE_DIORITE_UPPER);
        builder.feature(GenerationStep.Feature.UNDERGROUND_ORES, OrePlacedFeatures.ORE_DIORITE_LOWER);
        builder.feature(GenerationStep.Feature.UNDERGROUND_ORES, OrePlacedFeatures.ORE_ANDESITE_UPPER);
        builder.feature(GenerationStep.Feature.UNDERGROUND_ORES, OrePlacedFeatures.ORE_ANDESITE_LOWER);
        builder.feature(GenerationStep.Feature.UNDERGROUND_ORES, OrePlacedFeatures.ORE_TUFF);
        builder.feature(GenerationStep.Feature.VEGETAL_DECORATION, UndergroundPlacedFeatures.GLOW_LICHEN);
    }

    public static void addDripstone(GenerationSettings.LookupBackedBuilder builder) {
        builder.feature(GenerationStep.Feature.LOCAL_MODIFICATIONS, UndergroundPlacedFeatures.LARGE_DRIPSTONE);
        builder.feature(GenerationStep.Feature.UNDERGROUND_DECORATION, UndergroundPlacedFeatures.DRIPSTONE_CLUSTER);
        builder.feature(GenerationStep.Feature.UNDERGROUND_DECORATION, UndergroundPlacedFeatures.POINTED_DRIPSTONE);
    }

    public static void addSculk(GenerationSettings.LookupBackedBuilder builder) {
        builder.feature(GenerationStep.Feature.UNDERGROUND_DECORATION, UndergroundPlacedFeatures.SCULK_VEIN);
        builder.feature(GenerationStep.Feature.UNDERGROUND_DECORATION, UndergroundPlacedFeatures.SCULK_PATCH_DEEP_DARK);
    }

    public static void addDefaultOres(GenerationSettings.LookupBackedBuilder builder) {
        DefaultBiomeFeatures.addDefaultOres(builder, false);
    }

    public static void addDefaultOres(GenerationSettings.LookupBackedBuilder builder, boolean largeCopperOreBlob) {
        builder.feature(GenerationStep.Feature.UNDERGROUND_ORES, OrePlacedFeatures.ORE_COAL_UPPER);
        builder.feature(GenerationStep.Feature.UNDERGROUND_ORES, OrePlacedFeatures.ORE_COAL_LOWER);
        builder.feature(GenerationStep.Feature.UNDERGROUND_ORES, OrePlacedFeatures.ORE_IRON_UPPER);
        builder.feature(GenerationStep.Feature.UNDERGROUND_ORES, OrePlacedFeatures.ORE_IRON_MIDDLE);
        builder.feature(GenerationStep.Feature.UNDERGROUND_ORES, OrePlacedFeatures.ORE_IRON_SMALL);
        builder.feature(GenerationStep.Feature.UNDERGROUND_ORES, OrePlacedFeatures.ORE_GOLD);
        builder.feature(GenerationStep.Feature.UNDERGROUND_ORES, OrePlacedFeatures.ORE_GOLD_LOWER);
        builder.feature(GenerationStep.Feature.UNDERGROUND_ORES, OrePlacedFeatures.ORE_REDSTONE);
        builder.feature(GenerationStep.Feature.UNDERGROUND_ORES, OrePlacedFeatures.ORE_REDSTONE_LOWER);
        builder.feature(GenerationStep.Feature.UNDERGROUND_ORES, OrePlacedFeatures.ORE_DIAMOND);
        builder.feature(GenerationStep.Feature.UNDERGROUND_ORES, OrePlacedFeatures.ORE_DIAMOND_MEDIUM);
        builder.feature(GenerationStep.Feature.UNDERGROUND_ORES, OrePlacedFeatures.ORE_DIAMOND_LARGE);
        builder.feature(GenerationStep.Feature.UNDERGROUND_ORES, OrePlacedFeatures.ORE_DIAMOND_BURIED);
        builder.feature(GenerationStep.Feature.UNDERGROUND_ORES, OrePlacedFeatures.ORE_LAPIS);
        builder.feature(GenerationStep.Feature.UNDERGROUND_ORES, OrePlacedFeatures.ORE_LAPIS_BURIED);
        builder.feature(GenerationStep.Feature.UNDERGROUND_ORES, largeCopperOreBlob ? OrePlacedFeatures.ORE_COPPER_LARGE : OrePlacedFeatures.ORE_COPPER);
        builder.feature(GenerationStep.Feature.UNDERGROUND_ORES, UndergroundPlacedFeatures.UNDERWATER_MAGMA);
    }

    public static void addExtraGoldOre(GenerationSettings.LookupBackedBuilder builder) {
        builder.feature(GenerationStep.Feature.UNDERGROUND_ORES, OrePlacedFeatures.ORE_GOLD_EXTRA);
    }

    public static void addEmeraldOre(GenerationSettings.LookupBackedBuilder builder) {
        builder.feature(GenerationStep.Feature.UNDERGROUND_ORES, OrePlacedFeatures.ORE_EMERALD);
    }

    public static void addInfestedStone(GenerationSettings.LookupBackedBuilder builder) {
        builder.feature(GenerationStep.Feature.UNDERGROUND_DECORATION, OrePlacedFeatures.ORE_INFESTED);
    }

    public static void addDefaultDisks(GenerationSettings.LookupBackedBuilder builder) {
        builder.feature(GenerationStep.Feature.UNDERGROUND_ORES, MiscPlacedFeatures.DISK_SAND);
        builder.feature(GenerationStep.Feature.UNDERGROUND_ORES, MiscPlacedFeatures.DISK_CLAY);
        builder.feature(GenerationStep.Feature.UNDERGROUND_ORES, MiscPlacedFeatures.DISK_GRAVEL);
    }

    public static void addClayDisk(GenerationSettings.LookupBackedBuilder builder) {
        builder.feature(GenerationStep.Feature.UNDERGROUND_ORES, MiscPlacedFeatures.DISK_CLAY);
    }

    public static void addGrassAndClayDisks(GenerationSettings.LookupBackedBuilder builder) {
        builder.feature(GenerationStep.Feature.UNDERGROUND_ORES, MiscPlacedFeatures.DISK_GRASS);
        builder.feature(GenerationStep.Feature.UNDERGROUND_ORES, MiscPlacedFeatures.DISK_CLAY);
    }

    public static void addMossyRocks(GenerationSettings.LookupBackedBuilder builder) {
        builder.feature(GenerationStep.Feature.LOCAL_MODIFICATIONS, MiscPlacedFeatures.FOREST_ROCK);
    }

    public static void addLargeFerns(GenerationSettings.LookupBackedBuilder builder) {
        builder.feature(GenerationStep.Feature.VEGETAL_DECORATION, VegetationPlacedFeatures.PATCH_LARGE_FERN);
    }

    public static void addBushes(GenerationSettings.LookupBackedBuilder builder) {
        builder.feature(GenerationStep.Feature.VEGETAL_DECORATION, VegetationPlacedFeatures.PATCH_BUSH);
    }

    public static void addSweetBerryBushesSnowy(GenerationSettings.LookupBackedBuilder builder) {
        builder.feature(GenerationStep.Feature.VEGETAL_DECORATION, VegetationPlacedFeatures.PATCH_BERRY_RARE);
    }

    public static void addSweetBerryBushes(GenerationSettings.LookupBackedBuilder builder) {
        builder.feature(GenerationStep.Feature.VEGETAL_DECORATION, VegetationPlacedFeatures.PATCH_BERRY_COMMON);
    }

    public static void addBamboo(GenerationSettings.LookupBackedBuilder builder) {
        builder.feature(GenerationStep.Feature.VEGETAL_DECORATION, VegetationPlacedFeatures.BAMBOO_LIGHT);
    }

    public static void addBambooJungleTrees(GenerationSettings.LookupBackedBuilder builder) {
        builder.feature(GenerationStep.Feature.VEGETAL_DECORATION, VegetationPlacedFeatures.BAMBOO);
        builder.feature(GenerationStep.Feature.VEGETAL_DECORATION, VegetationPlacedFeatures.BAMBOO_VEGETATION);
    }

    public static void addTaigaTrees(GenerationSettings.LookupBackedBuilder builder) {
        builder.feature(GenerationStep.Feature.VEGETAL_DECORATION, VegetationPlacedFeatures.TREES_TAIGA);
    }

    public static void addGroveTrees(GenerationSettings.LookupBackedBuilder builder) {
        builder.feature(GenerationStep.Feature.VEGETAL_DECORATION, VegetationPlacedFeatures.TREES_GROVE);
    }

    public static void addWaterBiomeOakTrees(GenerationSettings.LookupBackedBuilder builder) {
        builder.feature(GenerationStep.Feature.VEGETAL_DECORATION, VegetationPlacedFeatures.TREES_WATER);
    }

    public static void addBirchTrees(GenerationSettings.LookupBackedBuilder builder) {
        builder.feature(GenerationStep.Feature.VEGETAL_DECORATION, VegetationPlacedFeatures.TREES_BIRCH);
    }

    public static void addForestTrees(GenerationSettings.LookupBackedBuilder builder) {
        builder.feature(GenerationStep.Feature.VEGETAL_DECORATION, VegetationPlacedFeatures.TREES_BIRCH_AND_OAK);
    }

    public static void addTallBirchTrees(GenerationSettings.LookupBackedBuilder builder) {
        builder.feature(GenerationStep.Feature.VEGETAL_DECORATION, VegetationPlacedFeatures.BIRCH_TALL);
    }

    public static void addBirchForestWildflowers(GenerationSettings.LookupBackedBuilder builder) {
        builder.feature(GenerationStep.Feature.VEGETAL_DECORATION, VegetationPlacedFeatures.WILDFLOWERS_BIRCH_FOREST);
    }

    public static void addSavannaTrees(GenerationSettings.LookupBackedBuilder builder) {
        builder.feature(GenerationStep.Feature.VEGETAL_DECORATION, VegetationPlacedFeatures.TREES_SAVANNA);
    }

    public static void addExtraSavannaTrees(GenerationSettings.LookupBackedBuilder builder) {
        builder.feature(GenerationStep.Feature.VEGETAL_DECORATION, VegetationPlacedFeatures.TREES_WINDSWEPT_SAVANNA);
    }

    public static void addLushCavesDecoration(GenerationSettings.LookupBackedBuilder builder) {
        builder.feature(GenerationStep.Feature.VEGETAL_DECORATION, UndergroundPlacedFeatures.LUSH_CAVES_CEILING_VEGETATION);
        builder.feature(GenerationStep.Feature.VEGETAL_DECORATION, UndergroundPlacedFeatures.CAVE_VINES);
        builder.feature(GenerationStep.Feature.VEGETAL_DECORATION, UndergroundPlacedFeatures.LUSH_CAVES_CLAY);
        builder.feature(GenerationStep.Feature.VEGETAL_DECORATION, UndergroundPlacedFeatures.LUSH_CAVES_VEGETATION);
        builder.feature(GenerationStep.Feature.VEGETAL_DECORATION, UndergroundPlacedFeatures.ROOTED_AZALEA_TREE);
        builder.feature(GenerationStep.Feature.VEGETAL_DECORATION, UndergroundPlacedFeatures.SPORE_BLOSSOM);
        builder.feature(GenerationStep.Feature.VEGETAL_DECORATION, UndergroundPlacedFeatures.CLASSIC_VINES_CAVE_FEATURE);
    }

    public static void addClayOre(GenerationSettings.LookupBackedBuilder builder) {
        builder.feature(GenerationStep.Feature.UNDERGROUND_ORES, OrePlacedFeatures.ORE_CLAY);
    }

    public static void addWindsweptHillsTrees(GenerationSettings.LookupBackedBuilder builder) {
        builder.feature(GenerationStep.Feature.VEGETAL_DECORATION, VegetationPlacedFeatures.TREES_WINDSWEPT_HILLS);
    }

    public static void addWindsweptForestTrees(GenerationSettings.LookupBackedBuilder builder) {
        builder.feature(GenerationStep.Feature.VEGETAL_DECORATION, VegetationPlacedFeatures.TREES_WINDSWEPT_FOREST);
    }

    public static void addJungleTrees(GenerationSettings.LookupBackedBuilder builder) {
        builder.feature(GenerationStep.Feature.VEGETAL_DECORATION, VegetationPlacedFeatures.TREES_JUNGLE);
    }

    public static void addSparseJungleTrees(GenerationSettings.LookupBackedBuilder builder) {
        builder.feature(GenerationStep.Feature.VEGETAL_DECORATION, VegetationPlacedFeatures.TREES_SPARSE_JUNGLE);
    }

    public static void addBadlandsPlateauTrees(GenerationSettings.LookupBackedBuilder builder) {
        builder.feature(GenerationStep.Feature.VEGETAL_DECORATION, VegetationPlacedFeatures.TREES_BADLANDS);
    }

    public static void addSnowySpruceTrees(GenerationSettings.LookupBackedBuilder builder) {
        builder.feature(GenerationStep.Feature.VEGETAL_DECORATION, VegetationPlacedFeatures.TREES_SNOWY);
    }

    public static void addJungleGrass(GenerationSettings.LookupBackedBuilder builder) {
        builder.feature(GenerationStep.Feature.VEGETAL_DECORATION, VegetationPlacedFeatures.PATCH_GRASS_JUNGLE);
    }

    public static void addSavannaTallGrass(GenerationSettings.LookupBackedBuilder builder) {
        builder.feature(GenerationStep.Feature.VEGETAL_DECORATION, VegetationPlacedFeatures.PATCH_TALL_GRASS);
    }

    public static void addWindsweptSavannaGrass(GenerationSettings.LookupBackedBuilder builder) {
        builder.feature(GenerationStep.Feature.VEGETAL_DECORATION, VegetationPlacedFeatures.PATCH_GRASS_NORMAL);
    }

    public static void addSavannaGrass(GenerationSettings.LookupBackedBuilder builder) {
        builder.feature(GenerationStep.Feature.VEGETAL_DECORATION, VegetationPlacedFeatures.PATCH_GRASS_SAVANNA);
    }

    public static void addBadlandsGrass(GenerationSettings.LookupBackedBuilder builder) {
        builder.feature(GenerationStep.Feature.VEGETAL_DECORATION, VegetationPlacedFeatures.PATCH_GRASS_BADLANDS);
        builder.feature(GenerationStep.Feature.VEGETAL_DECORATION, VegetationPlacedFeatures.PATCH_DRY_GRASS_BADLANDS);
        builder.feature(GenerationStep.Feature.VEGETAL_DECORATION, VegetationPlacedFeatures.PATCH_DEAD_BUSH_BADLANDS);
    }

    public static void addForestFlowers(GenerationSettings.LookupBackedBuilder builder) {
        builder.feature(GenerationStep.Feature.VEGETAL_DECORATION, VegetationPlacedFeatures.FOREST_FLOWERS);
    }

    public static void addForestGrass(GenerationSettings.LookupBackedBuilder builder) {
        builder.feature(GenerationStep.Feature.VEGETAL_DECORATION, VegetationPlacedFeatures.PATCH_GRASS_FOREST);
    }

    public static void addSwampFeatures(GenerationSettings.LookupBackedBuilder builder) {
        builder.feature(GenerationStep.Feature.VEGETAL_DECORATION, VegetationPlacedFeatures.TREES_SWAMP);
        builder.feature(GenerationStep.Feature.VEGETAL_DECORATION, VegetationPlacedFeatures.FLOWER_SWAMP);
        builder.feature(GenerationStep.Feature.VEGETAL_DECORATION, VegetationPlacedFeatures.PATCH_GRASS_NORMAL);
        builder.feature(GenerationStep.Feature.VEGETAL_DECORATION, VegetationPlacedFeatures.PATCH_DEAD_BUSH);
        builder.feature(GenerationStep.Feature.VEGETAL_DECORATION, VegetationPlacedFeatures.PATCH_WATERLILY);
        builder.feature(GenerationStep.Feature.VEGETAL_DECORATION, VegetationPlacedFeatures.BROWN_MUSHROOM_SWAMP);
        builder.feature(GenerationStep.Feature.VEGETAL_DECORATION, VegetationPlacedFeatures.RED_MUSHROOM_SWAMP);
    }

    public static void addMangroveSwampFeatures(GenerationSettings.LookupBackedBuilder builder) {
        builder.feature(GenerationStep.Feature.VEGETAL_DECORATION, VegetationPlacedFeatures.TREES_MANGROVE);
        builder.feature(GenerationStep.Feature.VEGETAL_DECORATION, VegetationPlacedFeatures.PATCH_GRASS_NORMAL);
        builder.feature(GenerationStep.Feature.VEGETAL_DECORATION, VegetationPlacedFeatures.PATCH_DEAD_BUSH);
        builder.feature(GenerationStep.Feature.VEGETAL_DECORATION, VegetationPlacedFeatures.PATCH_WATERLILY);
    }

    public static void addMushroomFieldsFeatures(GenerationSettings.LookupBackedBuilder builder) {
        builder.feature(GenerationStep.Feature.VEGETAL_DECORATION, VegetationPlacedFeatures.MUSHROOM_ISLAND_VEGETATION);
        builder.feature(GenerationStep.Feature.VEGETAL_DECORATION, VegetationPlacedFeatures.BROWN_MUSHROOM_TAIGA);
        builder.feature(GenerationStep.Feature.VEGETAL_DECORATION, VegetationPlacedFeatures.RED_MUSHROOM_TAIGA);
    }

    public static void addPlainsFeatures(GenerationSettings.LookupBackedBuilder builder) {
        builder.feature(GenerationStep.Feature.VEGETAL_DECORATION, VegetationPlacedFeatures.TREES_PLAINS);
        builder.feature(GenerationStep.Feature.VEGETAL_DECORATION, VegetationPlacedFeatures.FLOWER_PLAIN);
        builder.feature(GenerationStep.Feature.VEGETAL_DECORATION, VegetationPlacedFeatures.PATCH_GRASS_PLAIN);
    }

    public static void addDesertDryVegetation(GenerationSettings.LookupBackedBuilder builder) {
        builder.feature(GenerationStep.Feature.VEGETAL_DECORATION, VegetationPlacedFeatures.PATCH_DRY_GRASS_DESERT);
        builder.feature(GenerationStep.Feature.VEGETAL_DECORATION, VegetationPlacedFeatures.PATCH_DEAD_BUSH_2);
    }

    public static void addGiantTaigaGrass(GenerationSettings.LookupBackedBuilder builder) {
        builder.feature(GenerationStep.Feature.VEGETAL_DECORATION, VegetationPlacedFeatures.PATCH_GRASS_TAIGA);
        builder.feature(GenerationStep.Feature.VEGETAL_DECORATION, VegetationPlacedFeatures.PATCH_DEAD_BUSH);
        builder.feature(GenerationStep.Feature.VEGETAL_DECORATION, VegetationPlacedFeatures.BROWN_MUSHROOM_OLD_GROWTH);
        builder.feature(GenerationStep.Feature.VEGETAL_DECORATION, VegetationPlacedFeatures.RED_MUSHROOM_OLD_GROWTH);
    }

    public static void addDefaultFlowers(GenerationSettings.LookupBackedBuilder builder) {
        builder.feature(GenerationStep.Feature.VEGETAL_DECORATION, VegetationPlacedFeatures.FLOWER_DEFAULT);
    }

    public static void addCherryGroveFeatures(GenerationSettings.LookupBackedBuilder builder) {
        builder.feature(GenerationStep.Feature.VEGETAL_DECORATION, VegetationPlacedFeatures.PATCH_GRASS_PLAIN);
        builder.feature(GenerationStep.Feature.VEGETAL_DECORATION, VegetationPlacedFeatures.FLOWER_CHERRY);
        builder.feature(GenerationStep.Feature.VEGETAL_DECORATION, VegetationPlacedFeatures.TREES_CHERRY);
    }

    public static void addMeadowFlowers(GenerationSettings.LookupBackedBuilder builder) {
        builder.feature(GenerationStep.Feature.VEGETAL_DECORATION, VegetationPlacedFeatures.PATCH_GRASS_MEADOW);
        builder.feature(GenerationStep.Feature.VEGETAL_DECORATION, VegetationPlacedFeatures.FLOWER_MEADOW);
        builder.feature(GenerationStep.Feature.VEGETAL_DECORATION, VegetationPlacedFeatures.TREES_MEADOW);
        builder.feature(GenerationStep.Feature.VEGETAL_DECORATION, VegetationPlacedFeatures.WILDFLOWERS_MEADOW);
    }

    public static void addExtraDefaultFlowers(GenerationSettings.LookupBackedBuilder builder) {
        builder.feature(GenerationStep.Feature.VEGETAL_DECORATION, VegetationPlacedFeatures.FLOWER_WARM);
    }

    public static void addDefaultGrass(GenerationSettings.LookupBackedBuilder builder) {
        builder.feature(GenerationStep.Feature.VEGETAL_DECORATION, VegetationPlacedFeatures.PATCH_GRASS_BADLANDS);
    }

    public static void addTaigaGrass(GenerationSettings.LookupBackedBuilder builder) {
        builder.feature(GenerationStep.Feature.VEGETAL_DECORATION, VegetationPlacedFeatures.PATCH_GRASS_TAIGA_2);
        builder.feature(GenerationStep.Feature.VEGETAL_DECORATION, VegetationPlacedFeatures.BROWN_MUSHROOM_TAIGA);
        builder.feature(GenerationStep.Feature.VEGETAL_DECORATION, VegetationPlacedFeatures.RED_MUSHROOM_TAIGA);
    }

    public static void addPlainsTallGrass(GenerationSettings.LookupBackedBuilder builder) {
        builder.feature(GenerationStep.Feature.VEGETAL_DECORATION, VegetationPlacedFeatures.PATCH_TALL_GRASS_2);
    }

    public static void addDefaultMushrooms(GenerationSettings.LookupBackedBuilder builder) {
        builder.feature(GenerationStep.Feature.VEGETAL_DECORATION, VegetationPlacedFeatures.BROWN_MUSHROOM_NORMAL);
        builder.feature(GenerationStep.Feature.VEGETAL_DECORATION, VegetationPlacedFeatures.RED_MUSHROOM_NORMAL);
    }

    public static void addDefaultVegetation(GenerationSettings.LookupBackedBuilder builder, boolean includeNearWater) {
        builder.feature(GenerationStep.Feature.VEGETAL_DECORATION, VegetationPlacedFeatures.PATCH_PUMPKIN);
        if (includeNearWater) {
            DefaultBiomeFeatures.addDefaultVegetationNearWater(builder);
        }
    }

    public static void addDefaultVegetationNearWater(GenerationSettings.LookupBackedBuilder builder) {
        builder.feature(GenerationStep.Feature.VEGETAL_DECORATION, VegetationPlacedFeatures.PATCH_SUGAR_CANE);
        builder.feature(GenerationStep.Feature.VEGETAL_DECORATION, VegetationPlacedFeatures.PATCH_FIREFLY_BUSH_NEAR_WATER);
    }

    public static void addLeafLitter(GenerationSettings.LookupBackedBuilder builder) {
        builder.feature(GenerationStep.Feature.VEGETAL_DECORATION, VegetationPlacedFeatures.PATCH_LEAF_LITTER);
    }

    public static void addBadlandsVegetation(GenerationSettings.LookupBackedBuilder builder) {
        builder.feature(GenerationStep.Feature.VEGETAL_DECORATION, VegetationPlacedFeatures.PATCH_SUGAR_CANE_BADLANDS);
        builder.feature(GenerationStep.Feature.VEGETAL_DECORATION, VegetationPlacedFeatures.PATCH_PUMPKIN);
        builder.feature(GenerationStep.Feature.VEGETAL_DECORATION, VegetationPlacedFeatures.PATCH_CACTUS_DECORATED);
        builder.feature(GenerationStep.Feature.VEGETAL_DECORATION, VegetationPlacedFeatures.PATCH_FIREFLY_BUSH_NEAR_WATER);
    }

    public static void addMelons(GenerationSettings.LookupBackedBuilder builder) {
        builder.feature(GenerationStep.Feature.VEGETAL_DECORATION, VegetationPlacedFeatures.PATCH_MELON);
    }

    public static void addSparseMelons(GenerationSettings.LookupBackedBuilder builder) {
        builder.feature(GenerationStep.Feature.VEGETAL_DECORATION, VegetationPlacedFeatures.PATCH_MELON_SPARSE);
    }

    public static void addVines(GenerationSettings.LookupBackedBuilder builder) {
        builder.feature(GenerationStep.Feature.VEGETAL_DECORATION, VegetationPlacedFeatures.VINES);
    }

    public static void addDesertVegetation(GenerationSettings.LookupBackedBuilder builder) {
        builder.feature(GenerationStep.Feature.VEGETAL_DECORATION, VegetationPlacedFeatures.PATCH_SUGAR_CANE_DESERT);
        builder.feature(GenerationStep.Feature.VEGETAL_DECORATION, VegetationPlacedFeatures.PATCH_PUMPKIN);
        builder.feature(GenerationStep.Feature.VEGETAL_DECORATION, VegetationPlacedFeatures.PATCH_CACTUS_DESERT);
    }

    public static void addSwampVegetation(GenerationSettings.LookupBackedBuilder builder) {
        builder.feature(GenerationStep.Feature.VEGETAL_DECORATION, VegetationPlacedFeatures.PATCH_SUGAR_CANE_SWAMP);
        builder.feature(GenerationStep.Feature.VEGETAL_DECORATION, VegetationPlacedFeatures.PATCH_PUMPKIN);
        builder.feature(GenerationStep.Feature.VEGETAL_DECORATION, VegetationPlacedFeatures.PATCH_FIREFLY_BUSH);
        builder.feature(GenerationStep.Feature.VEGETAL_DECORATION, VegetationPlacedFeatures.PATCH_FIREFLY_BUSH_NEAR_WATER_SWAMP);
    }

    public static void addMangroveSwampAquaticFeatures(GenerationSettings.LookupBackedBuilder builder) {
        builder.feature(GenerationStep.Feature.VEGETAL_DECORATION, OceanPlacedFeatures.SEAGRASS_SWAMP);
        builder.feature(GenerationStep.Feature.VEGETAL_DECORATION, VegetationPlacedFeatures.PATCH_FIREFLY_BUSH_NEAR_WATER);
    }

    public static void addDesertFeatures(GenerationSettings.LookupBackedBuilder builder) {
        builder.feature(GenerationStep.Feature.SURFACE_STRUCTURES, MiscPlacedFeatures.DESERT_WELL);
    }

    public static void addFossils(GenerationSettings.LookupBackedBuilder builder) {
        builder.feature(GenerationStep.Feature.UNDERGROUND_STRUCTURES, UndergroundPlacedFeatures.FOSSIL_UPPER);
        builder.feature(GenerationStep.Feature.UNDERGROUND_STRUCTURES, UndergroundPlacedFeatures.FOSSIL_LOWER);
    }

    public static void addKelp(GenerationSettings.LookupBackedBuilder builder) {
        builder.feature(GenerationStep.Feature.VEGETAL_DECORATION, OceanPlacedFeatures.KELP_COLD);
    }

    public static void addLessKelp(GenerationSettings.LookupBackedBuilder builder) {
        builder.feature(GenerationStep.Feature.VEGETAL_DECORATION, OceanPlacedFeatures.KELP_WARM);
    }

    public static void addSprings(GenerationSettings.LookupBackedBuilder builder) {
        builder.feature(GenerationStep.Feature.FLUID_SPRINGS, MiscPlacedFeatures.SPRING_WATER);
        builder.feature(GenerationStep.Feature.FLUID_SPRINGS, MiscPlacedFeatures.SPRING_LAVA);
    }

    public static void addFrozenLavaSpring(GenerationSettings.LookupBackedBuilder builder) {
        builder.feature(GenerationStep.Feature.FLUID_SPRINGS, MiscPlacedFeatures.SPRING_LAVA_FROZEN);
    }

    public static void addIcebergs(GenerationSettings.LookupBackedBuilder builder) {
        builder.feature(GenerationStep.Feature.LOCAL_MODIFICATIONS, MiscPlacedFeatures.ICEBERG_PACKED);
        builder.feature(GenerationStep.Feature.LOCAL_MODIFICATIONS, MiscPlacedFeatures.ICEBERG_BLUE);
    }

    public static void addBlueIce(GenerationSettings.LookupBackedBuilder builder) {
        builder.feature(GenerationStep.Feature.SURFACE_STRUCTURES, MiscPlacedFeatures.BLUE_ICE);
    }

    public static void addFrozenTopLayer(GenerationSettings.LookupBackedBuilder builder) {
        builder.feature(GenerationStep.Feature.TOP_LAYER_MODIFICATION, MiscPlacedFeatures.FREEZE_TOP_LAYER);
    }

    public static void addNetherMineables(GenerationSettings.LookupBackedBuilder builder) {
        builder.feature(GenerationStep.Feature.UNDERGROUND_DECORATION, OrePlacedFeatures.ORE_GRAVEL_NETHER);
        builder.feature(GenerationStep.Feature.UNDERGROUND_DECORATION, OrePlacedFeatures.ORE_BLACKSTONE);
        builder.feature(GenerationStep.Feature.UNDERGROUND_DECORATION, OrePlacedFeatures.ORE_GOLD_NETHER);
        builder.feature(GenerationStep.Feature.UNDERGROUND_DECORATION, OrePlacedFeatures.ORE_QUARTZ_NETHER);
        DefaultBiomeFeatures.addAncientDebris(builder);
    }

    public static void addAncientDebris(GenerationSettings.LookupBackedBuilder builder) {
        builder.feature(GenerationStep.Feature.UNDERGROUND_DECORATION, OrePlacedFeatures.ORE_ANCIENT_DEBRIS_LARGE);
        builder.feature(GenerationStep.Feature.UNDERGROUND_DECORATION, OrePlacedFeatures.ORE_DEBRIS_SMALL);
    }

    public static void addAmethystGeodes(GenerationSettings.LookupBackedBuilder builder) {
        builder.feature(GenerationStep.Feature.LOCAL_MODIFICATIONS, UndergroundPlacedFeatures.AMETHYST_GEODE);
    }

    public static void addFarmAnimals(SpawnSettings.Builder builder) {
        builder.spawn(SpawnGroup.CREATURE, 12, new SpawnSettings.SpawnEntry(EntityType.SHEEP, 4, 4));
        builder.spawn(SpawnGroup.CREATURE, 10, new SpawnSettings.SpawnEntry(EntityType.PIG, 4, 4));
        builder.spawn(SpawnGroup.CREATURE, 10, new SpawnSettings.SpawnEntry(EntityType.CHICKEN, 4, 4));
        builder.spawn(SpawnGroup.CREATURE, 8, new SpawnSettings.SpawnEntry(EntityType.COW, 4, 4));
    }

    public static void addCaveMobs(SpawnSettings.Builder builder) {
        builder.spawn(SpawnGroup.AMBIENT, 10, new SpawnSettings.SpawnEntry(EntityType.BAT, 8, 8));
        builder.spawn(SpawnGroup.UNDERGROUND_WATER_CREATURE, 10, new SpawnSettings.SpawnEntry(EntityType.GLOW_SQUID, 4, 6));
    }

    public static void addCaveAndMonsters(SpawnSettings.Builder builder) {
        DefaultBiomeFeatures.addCaveAndMonsters(builder, 100);
    }

    public static void addCaveAndMonsters(SpawnSettings.Builder builder, int skeletonWeight) {
        DefaultBiomeFeatures.addCaveMobs(builder);
        DefaultBiomeFeatures.addMonsters(builder, 95, 5, 0, skeletonWeight, false);
    }

    public static void addCaveAndMonstersAndZombieHorse(SpawnSettings.Builder builder) {
        DefaultBiomeFeatures.addCaveMobs(builder);
        DefaultBiomeFeatures.addMonsters(builder, 90, 5, 5, 100, false);
    }

    public static void addSwampMobs(SpawnSettings.Builder builder, int skeletonWeight) {
        DefaultBiomeFeatures.addCaveAndMonsters(builder, skeletonWeight);
        builder.spawn(SpawnGroup.MONSTER, 1, new SpawnSettings.SpawnEntry(EntityType.SLIME, 1, 1));
        builder.spawn(SpawnGroup.MONSTER, 30, new SpawnSettings.SpawnEntry(EntityType.BOGGED, 4, 4));
        builder.spawn(SpawnGroup.CREATURE, 10, new SpawnSettings.SpawnEntry(EntityType.FROG, 2, 5));
    }

    public static void addOceanMobs(SpawnSettings.Builder builder, int squidWeight, int squidMaxGroupSize, int codWeight) {
        builder.spawn(SpawnGroup.WATER_CREATURE, squidWeight, new SpawnSettings.SpawnEntry(EntityType.SQUID, 1, squidMaxGroupSize));
        builder.spawn(SpawnGroup.WATER_AMBIENT, codWeight, new SpawnSettings.SpawnEntry(EntityType.COD, 3, 6));
        DefaultBiomeFeatures.addCaveAndMonsters(builder);
        builder.spawn(SpawnGroup.MONSTER, 5, new SpawnSettings.SpawnEntry(EntityType.DROWNED, 1, 1));
    }

    public static void addWarmOceanMobs(SpawnSettings.Builder builder, int squidWeight, int squidMinGroupSize) {
        builder.spawn(SpawnGroup.WATER_CREATURE, squidWeight, new SpawnSettings.SpawnEntry(EntityType.SQUID, squidMinGroupSize, 4));
        builder.spawn(SpawnGroup.WATER_AMBIENT, 25, new SpawnSettings.SpawnEntry(EntityType.TROPICAL_FISH, 8, 8));
        builder.spawn(SpawnGroup.WATER_CREATURE, 2, new SpawnSettings.SpawnEntry(EntityType.DOLPHIN, 1, 2));
        builder.spawn(SpawnGroup.MONSTER, 5, new SpawnSettings.SpawnEntry(EntityType.DROWNED, 1, 1));
        DefaultBiomeFeatures.addCaveAndMonsters(builder);
    }

    public static void addPlainsMobs(SpawnSettings.Builder builder) {
        DefaultBiomeFeatures.addFarmAnimals(builder);
        builder.spawn(SpawnGroup.CREATURE, 5, new SpawnSettings.SpawnEntry(EntityType.HORSE, 2, 6));
        builder.spawn(SpawnGroup.CREATURE, 1, new SpawnSettings.SpawnEntry(EntityType.DONKEY, 1, 3));
        DefaultBiomeFeatures.addCaveAndMonstersAndZombieHorse(builder);
    }

    public static void addSnowyMobs(SpawnSettings.Builder builder, boolean zombieHorse) {
        builder.spawn(SpawnGroup.CREATURE, 10, new SpawnSettings.SpawnEntry(EntityType.RABBIT, 2, 3));
        builder.spawn(SpawnGroup.CREATURE, 1, new SpawnSettings.SpawnEntry(EntityType.POLAR_BEAR, 1, 2));
        DefaultBiomeFeatures.addCaveMobs(builder);
        DefaultBiomeFeatures.addMonsters(builder, zombieHorse ? 90 : 95, 5, zombieHorse ? 5 : 0, 20, false);
        builder.spawn(SpawnGroup.MONSTER, 80, new SpawnSettings.SpawnEntry(EntityType.STRAY, 4, 4));
    }

    public static void addDesertMobs(SpawnSettings.Builder builder) {
        builder.spawn(SpawnGroup.CREATURE, 12, new SpawnSettings.SpawnEntry(EntityType.RABBIT, 2, 3));
        builder.spawn(SpawnGroup.CREATURE, 1, new SpawnSettings.SpawnEntry(EntityType.CAMEL, 1, 1));
        DefaultBiomeFeatures.addCaveMobs(builder);
        DefaultBiomeFeatures.addMonsters(builder, 19, 1, 0, 50, false);
        builder.spawn(SpawnGroup.MONSTER, 80, new SpawnSettings.SpawnEntry(EntityType.HUSK, 4, 4));
        builder.spawn(SpawnGroup.MONSTER, 50, new SpawnSettings.SpawnEntry(EntityType.PARCHED, 4, 4));
    }

    public static void addDripstoneCaveMobs(SpawnSettings.Builder builder) {
        DefaultBiomeFeatures.addCaveMobs(builder);
        int i = 95;
        DefaultBiomeFeatures.addMonsters(builder, 95, 5, 0, 100, false);
        builder.spawn(SpawnGroup.MONSTER, 95, new SpawnSettings.SpawnEntry(EntityType.DROWNED, 4, 4));
    }

    public static void addMonsters(SpawnSettings.Builder builder, int zombieWeight, int zombieVillagerWeight, int zombieHorseWeight, int skeletonWeight, boolean drowned) {
        builder.spawn(SpawnGroup.MONSTER, 100, new SpawnSettings.SpawnEntry(EntityType.SPIDER, 4, 4));
        builder.spawn(SpawnGroup.MONSTER, zombieWeight, new SpawnSettings.SpawnEntry(drowned ? EntityType.DROWNED : EntityType.ZOMBIE, 4, 4));
        builder.spawn(SpawnGroup.MONSTER, zombieVillagerWeight, new SpawnSettings.SpawnEntry(EntityType.ZOMBIE_VILLAGER, 1, 1));
        if (zombieHorseWeight > 0) {
            builder.spawn(SpawnGroup.MONSTER, zombieHorseWeight, new SpawnSettings.SpawnEntry(EntityType.ZOMBIE_HORSE, 1, 1));
        }
        builder.spawn(SpawnGroup.MONSTER, skeletonWeight, new SpawnSettings.SpawnEntry(EntityType.SKELETON, 4, 4));
        builder.spawn(SpawnGroup.MONSTER, 100, new SpawnSettings.SpawnEntry(EntityType.CREEPER, 4, 4));
        builder.spawn(SpawnGroup.MONSTER, 100, new SpawnSettings.SpawnEntry(EntityType.SLIME, 4, 4));
        builder.spawn(SpawnGroup.MONSTER, 10, new SpawnSettings.SpawnEntry(EntityType.ENDERMAN, 1, 4));
        builder.spawn(SpawnGroup.MONSTER, 5, new SpawnSettings.SpawnEntry(EntityType.WITCH, 1, 1));
    }

    public static void addMushroomMobs(SpawnSettings.Builder builder) {
        builder.spawn(SpawnGroup.CREATURE, 8, new SpawnSettings.SpawnEntry(EntityType.MOOSHROOM, 4, 8));
        DefaultBiomeFeatures.addCaveMobs(builder);
    }

    public static void addJungleMobs(SpawnSettings.Builder builder) {
        DefaultBiomeFeatures.addFarmAnimals(builder);
        builder.spawn(SpawnGroup.CREATURE, 10, new SpawnSettings.SpawnEntry(EntityType.CHICKEN, 4, 4));
        DefaultBiomeFeatures.addCaveAndMonsters(builder);
    }

    public static void addEndMobs(SpawnSettings.Builder builder) {
        builder.spawn(SpawnGroup.MONSTER, 10, new SpawnSettings.SpawnEntry(EntityType.ENDERMAN, 4, 4));
    }
}


/*
 * Decompiled with CFR 0.152.
 */
package net.minecraft.world.biome;

import net.minecraft.entity.EntityType;
import net.minecraft.entity.SpawnGroup;
import net.minecraft.registry.RegistryEntryLookup;
import net.minecraft.sound.MusicType;
import net.minecraft.sound.SoundEvents;
import net.minecraft.util.math.ColorHelper;
import net.minecraft.util.math.MathHelper;
import net.minecraft.world.attribute.BackgroundMusic;
import net.minecraft.world.attribute.EnvironmentAttributeMap;
import net.minecraft.world.attribute.EnvironmentAttributes;
import net.minecraft.world.attribute.FloatModifier;
import net.minecraft.world.biome.Biome;
import net.minecraft.world.biome.BiomeEffects;
import net.minecraft.world.biome.GenerationSettings;
import net.minecraft.world.biome.SpawnSettings;
import net.minecraft.world.gen.GenerationStep;
import net.minecraft.world.gen.carver.ConfiguredCarver;
import net.minecraft.world.gen.carver.ConfiguredCarvers;
import net.minecraft.world.gen.feature.DefaultBiomeFeatures;
import net.minecraft.world.gen.feature.MiscPlacedFeatures;
import net.minecraft.world.gen.feature.OceanPlacedFeatures;
import net.minecraft.world.gen.feature.PlacedFeature;
import net.minecraft.world.gen.feature.VegetationPlacedFeatures;

public class OverworldBiomeCreator {
    protected static final int DEFAULT_WATER_COLOR = 4159204;
    private static final int DEFAULT_DRY_FOLIAGE_COLOR = 8082228;
    public static final int SWAMP_SKELETON_WEIGHT = 70;

    public static int getSkyColor(float temperature) {
        float f = temperature;
        f /= 3.0f;
        f = MathHelper.clamp(f, -1.0f, 1.0f);
        return ColorHelper.fullAlpha(MathHelper.hsvToRgb(0.62222224f - f * 0.05f, 0.5f + f * 0.1f, 1.0f));
    }

    private static Biome.Builder biome(float temperature, float downfall) {
        return new Biome.Builder().precipitation(true).temperature(temperature).downfall(downfall).setEnvironmentAttribute(EnvironmentAttributes.SKY_COLOR_VISUAL, OverworldBiomeCreator.getSkyColor(temperature)).effects(new BiomeEffects.Builder().waterColor(4159204).build());
    }

    private static void addBasicFeatures(GenerationSettings.LookupBackedBuilder generationSettings) {
        DefaultBiomeFeatures.addLandCarvers(generationSettings);
        DefaultBiomeFeatures.addAmethystGeodes(generationSettings);
        DefaultBiomeFeatures.addDungeons(generationSettings);
        DefaultBiomeFeatures.addMineables(generationSettings);
        DefaultBiomeFeatures.addSprings(generationSettings);
        DefaultBiomeFeatures.addFrozenTopLayer(generationSettings);
    }

    public static Biome createOldGrowthTaiga(RegistryEntryLookup<PlacedFeature> featureLookup, RegistryEntryLookup<ConfiguredCarver<?>> carverLookup, boolean spruce) {
        SpawnSettings.Builder builder = new SpawnSettings.Builder();
        DefaultBiomeFeatures.addFarmAnimals(builder);
        builder.spawn(SpawnGroup.CREATURE, 8, new SpawnSettings.SpawnEntry(EntityType.WOLF, 4, 4));
        builder.spawn(SpawnGroup.CREATURE, 4, new SpawnSettings.SpawnEntry(EntityType.RABBIT, 2, 3));
        builder.spawn(SpawnGroup.CREATURE, 8, new SpawnSettings.SpawnEntry(EntityType.FOX, 2, 4));
        if (spruce) {
            DefaultBiomeFeatures.addCaveAndMonsters(builder);
        } else {
            DefaultBiomeFeatures.addCaveMobs(builder);
            DefaultBiomeFeatures.addMonsters(builder, 100, 25, 0, 100, false);
        }
        GenerationSettings.LookupBackedBuilder lookupBackedBuilder = new GenerationSettings.LookupBackedBuilder(featureLookup, carverLookup);
        OverworldBiomeCreator.addBasicFeatures(lookupBackedBuilder);
        DefaultBiomeFeatures.addMossyRocks(lookupBackedBuilder);
        DefaultBiomeFeatures.addLargeFerns(lookupBackedBuilder);
        DefaultBiomeFeatures.addDefaultOres(lookupBackedBuilder);
        DefaultBiomeFeatures.addDefaultDisks(lookupBackedBuilder);
        lookupBackedBuilder.feature(GenerationStep.Feature.VEGETAL_DECORATION, spruce ? VegetationPlacedFeatures.TREES_OLD_GROWTH_SPRUCE_TAIGA : VegetationPlacedFeatures.TREES_OLD_GROWTH_PINE_TAIGA);
        DefaultBiomeFeatures.addDefaultFlowers(lookupBackedBuilder);
        DefaultBiomeFeatures.addGiantTaigaGrass(lookupBackedBuilder);
        DefaultBiomeFeatures.addDefaultMushrooms(lookupBackedBuilder);
        DefaultBiomeFeatures.addDefaultVegetation(lookupBackedBuilder, true);
        DefaultBiomeFeatures.addSweetBerryBushes(lookupBackedBuilder);
        return OverworldBiomeCreator.biome(spruce ? 0.25f : 0.3f, 0.8f).setEnvironmentAttribute(EnvironmentAttributes.BACKGROUND_MUSIC_AUDIO, new BackgroundMusic(SoundEvents.MUSIC_OVERWORLD_OLD_GROWTH_TAIGA)).spawnSettings(builder.build()).generationSettings(lookupBackedBuilder.build()).build();
    }

    public static Biome createSparseJungle(RegistryEntryLookup<PlacedFeature> featureLookup, RegistryEntryLookup<ConfiguredCarver<?>> carverLookup) {
        SpawnSettings.Builder builder = new SpawnSettings.Builder();
        DefaultBiomeFeatures.addJungleMobs(builder);
        builder.spawn(SpawnGroup.CREATURE, 8, new SpawnSettings.SpawnEntry(EntityType.WOLF, 2, 4));
        return OverworldBiomeCreator.createJungleFeatures(featureLookup, carverLookup, 0.8f, false, true, false).spawnSettings(builder.build()).setEnvironmentAttribute(EnvironmentAttributes.BACKGROUND_MUSIC_AUDIO, new BackgroundMusic(SoundEvents.MUSIC_OVERWORLD_SPARSE_JUNGLE)).build();
    }

    public static Biome createJungle(RegistryEntryLookup<PlacedFeature> featureLookup, RegistryEntryLookup<ConfiguredCarver<?>> carverLookup) {
        SpawnSettings.Builder builder = new SpawnSettings.Builder();
        DefaultBiomeFeatures.addJungleMobs(builder);
        builder.spawn(SpawnGroup.CREATURE, 40, new SpawnSettings.SpawnEntry(EntityType.PARROT, 1, 2)).spawn(SpawnGroup.MONSTER, 2, new SpawnSettings.SpawnEntry(EntityType.OCELOT, 1, 3)).spawn(SpawnGroup.CREATURE, 1, new SpawnSettings.SpawnEntry(EntityType.PANDA, 1, 2));
        return OverworldBiomeCreator.createJungleFeatures(featureLookup, carverLookup, 0.9f, false, false, true).spawnSettings(builder.build()).setEnvironmentAttribute(EnvironmentAttributes.BACKGROUND_MUSIC_AUDIO, new BackgroundMusic(SoundEvents.MUSIC_OVERWORLD_JUNGLE)).setEnvironmentAttribute(EnvironmentAttributes.INCREASED_FIRE_BURNOUT_GAMEPLAY, true).build();
    }

    public static Biome createNormalBambooJungle(RegistryEntryLookup<PlacedFeature> featureLookup, RegistryEntryLookup<ConfiguredCarver<?>> carverLookup) {
        SpawnSettings.Builder builder = new SpawnSettings.Builder();
        DefaultBiomeFeatures.addJungleMobs(builder);
        builder.spawn(SpawnGroup.CREATURE, 40, new SpawnSettings.SpawnEntry(EntityType.PARROT, 1, 2)).spawn(SpawnGroup.CREATURE, 80, new SpawnSettings.SpawnEntry(EntityType.PANDA, 1, 2)).spawn(SpawnGroup.MONSTER, 2, new SpawnSettings.SpawnEntry(EntityType.OCELOT, 1, 1));
        return OverworldBiomeCreator.createJungleFeatures(featureLookup, carverLookup, 0.9f, true, false, true).spawnSettings(builder.build()).setEnvironmentAttribute(EnvironmentAttributes.BACKGROUND_MUSIC_AUDIO, new BackgroundMusic(SoundEvents.MUSIC_OVERWORLD_BAMBOO_JUNGLE)).setEnvironmentAttribute(EnvironmentAttributes.INCREASED_FIRE_BURNOUT_GAMEPLAY, true).build();
    }

    private static Biome.Builder createJungleFeatures(RegistryEntryLookup<PlacedFeature> featureLookup, RegistryEntryLookup<ConfiguredCarver<?>> carverLookup, float depth, boolean bamboo, boolean sparse, boolean unmodified) {
        GenerationSettings.LookupBackedBuilder lookupBackedBuilder = new GenerationSettings.LookupBackedBuilder(featureLookup, carverLookup);
        OverworldBiomeCreator.addBasicFeatures(lookupBackedBuilder);
        DefaultBiomeFeatures.addDefaultOres(lookupBackedBuilder);
        DefaultBiomeFeatures.addDefaultDisks(lookupBackedBuilder);
        if (bamboo) {
            DefaultBiomeFeatures.addBambooJungleTrees(lookupBackedBuilder);
        } else {
            if (unmodified) {
                DefaultBiomeFeatures.addBamboo(lookupBackedBuilder);
            }
            if (sparse) {
                DefaultBiomeFeatures.addSparseJungleTrees(lookupBackedBuilder);
            } else {
                DefaultBiomeFeatures.addJungleTrees(lookupBackedBuilder);
            }
        }
        DefaultBiomeFeatures.addExtraDefaultFlowers(lookupBackedBuilder);
        DefaultBiomeFeatures.addJungleGrass(lookupBackedBuilder);
        DefaultBiomeFeatures.addDefaultMushrooms(lookupBackedBuilder);
        DefaultBiomeFeatures.addDefaultVegetation(lookupBackedBuilder, true);
        DefaultBiomeFeatures.addVines(lookupBackedBuilder);
        if (sparse) {
            DefaultBiomeFeatures.addSparseMelons(lookupBackedBuilder);
        } else {
            DefaultBiomeFeatures.addMelons(lookupBackedBuilder);
        }
        return OverworldBiomeCreator.biome(0.95f, depth).generationSettings(lookupBackedBuilder.build());
    }

    public static Biome createWindsweptHills(RegistryEntryLookup<PlacedFeature> featureLookup, RegistryEntryLookup<ConfiguredCarver<?>> carverLookup, boolean forest) {
        SpawnSettings.Builder builder = new SpawnSettings.Builder();
        DefaultBiomeFeatures.addFarmAnimals(builder);
        builder.spawn(SpawnGroup.CREATURE, 5, new SpawnSettings.SpawnEntry(EntityType.LLAMA, 4, 6));
        DefaultBiomeFeatures.addCaveAndMonsters(builder);
        GenerationSettings.LookupBackedBuilder lookupBackedBuilder = new GenerationSettings.LookupBackedBuilder(featureLookup, carverLookup);
        OverworldBiomeCreator.addBasicFeatures(lookupBackedBuilder);
        DefaultBiomeFeatures.addDefaultOres(lookupBackedBuilder);
        DefaultBiomeFeatures.addDefaultDisks(lookupBackedBuilder);
        if (forest) {
            DefaultBiomeFeatures.addWindsweptForestTrees(lookupBackedBuilder);
        } else {
            DefaultBiomeFeatures.addWindsweptHillsTrees(lookupBackedBuilder);
        }
        DefaultBiomeFeatures.addBushes(lookupBackedBuilder);
        DefaultBiomeFeatures.addDefaultFlowers(lookupBackedBuilder);
        DefaultBiomeFeatures.addDefaultGrass(lookupBackedBuilder);
        DefaultBiomeFeatures.addDefaultMushrooms(lookupBackedBuilder);
        DefaultBiomeFeatures.addDefaultVegetation(lookupBackedBuilder, true);
        DefaultBiomeFeatures.addEmeraldOre(lookupBackedBuilder);
        DefaultBiomeFeatures.addInfestedStone(lookupBackedBuilder);
        return OverworldBiomeCreator.biome(0.2f, 0.3f).spawnSettings(builder.build()).generationSettings(lookupBackedBuilder.build()).build();
    }

    public static Biome createDesert(RegistryEntryLookup<PlacedFeature> featureLookup, RegistryEntryLookup<ConfiguredCarver<?>> carverLookup) {
        SpawnSettings.Builder builder = new SpawnSettings.Builder();
        DefaultBiomeFeatures.addDesertMobs(builder);
        GenerationSettings.LookupBackedBuilder lookupBackedBuilder = new GenerationSettings.LookupBackedBuilder(featureLookup, carverLookup);
        DefaultBiomeFeatures.addFossils(lookupBackedBuilder);
        OverworldBiomeCreator.addBasicFeatures(lookupBackedBuilder);
        DefaultBiomeFeatures.addDefaultOres(lookupBackedBuilder);
        DefaultBiomeFeatures.addDefaultDisks(lookupBackedBuilder);
        DefaultBiomeFeatures.addDefaultFlowers(lookupBackedBuilder);
        DefaultBiomeFeatures.addDefaultGrass(lookupBackedBuilder);
        DefaultBiomeFeatures.addDesertDryVegetation(lookupBackedBuilder);
        DefaultBiomeFeatures.addDefaultMushrooms(lookupBackedBuilder);
        DefaultBiomeFeatures.addDesertVegetation(lookupBackedBuilder);
        DefaultBiomeFeatures.addDesertFeatures(lookupBackedBuilder);
        return OverworldBiomeCreator.biome(2.0f, 0.0f).precipitation(false).setEnvironmentAttribute(EnvironmentAttributes.BACKGROUND_MUSIC_AUDIO, new BackgroundMusic(SoundEvents.MUSIC_OVERWORLD_DESERT)).setEnvironmentAttribute(EnvironmentAttributes.SNOW_GOLEM_MELTS_GAMEPLAY, true).spawnSettings(builder.build()).generationSettings(lookupBackedBuilder.build()).build();
    }

    public static Biome createPlains(RegistryEntryLookup<PlacedFeature> featureLookup, RegistryEntryLookup<ConfiguredCarver<?>> carverLookup, boolean sunflower, boolean snowy, boolean iceSpikes) {
        SpawnSettings.Builder builder = new SpawnSettings.Builder();
        GenerationSettings.LookupBackedBuilder lookupBackedBuilder = new GenerationSettings.LookupBackedBuilder(featureLookup, carverLookup);
        OverworldBiomeCreator.addBasicFeatures(lookupBackedBuilder);
        if (snowy) {
            builder.creatureSpawnProbability(0.07f);
            DefaultBiomeFeatures.addSnowyMobs(builder, !iceSpikes);
            if (iceSpikes) {
                lookupBackedBuilder.feature(GenerationStep.Feature.SURFACE_STRUCTURES, MiscPlacedFeatures.ICE_SPIKE);
                lookupBackedBuilder.feature(GenerationStep.Feature.SURFACE_STRUCTURES, MiscPlacedFeatures.ICE_PATCH);
            }
        } else {
            DefaultBiomeFeatures.addPlainsMobs(builder);
            DefaultBiomeFeatures.addPlainsTallGrass(lookupBackedBuilder);
            if (sunflower) {
                lookupBackedBuilder.feature(GenerationStep.Feature.VEGETAL_DECORATION, VegetationPlacedFeatures.PATCH_SUNFLOWER);
            } else {
                DefaultBiomeFeatures.addBushes(lookupBackedBuilder);
            }
        }
        DefaultBiomeFeatures.addDefaultOres(lookupBackedBuilder);
        DefaultBiomeFeatures.addDefaultDisks(lookupBackedBuilder);
        if (snowy) {
            DefaultBiomeFeatures.addSnowySpruceTrees(lookupBackedBuilder);
            DefaultBiomeFeatures.addDefaultFlowers(lookupBackedBuilder);
            DefaultBiomeFeatures.addDefaultGrass(lookupBackedBuilder);
        } else {
            DefaultBiomeFeatures.addPlainsFeatures(lookupBackedBuilder);
        }
        DefaultBiomeFeatures.addDefaultMushrooms(lookupBackedBuilder);
        DefaultBiomeFeatures.addDefaultVegetation(lookupBackedBuilder, true);
        return OverworldBiomeCreator.biome(snowy ? 0.0f : 0.8f, snowy ? 0.5f : 0.4f).spawnSettings(builder.build()).generationSettings(lookupBackedBuilder.build()).build();
    }

    public static Biome createMushroomFields(RegistryEntryLookup<PlacedFeature> featureLookup, RegistryEntryLookup<ConfiguredCarver<?>> carverLookup) {
        SpawnSettings.Builder builder = new SpawnSettings.Builder();
        DefaultBiomeFeatures.addMushroomMobs(builder);
        GenerationSettings.LookupBackedBuilder lookupBackedBuilder = new GenerationSettings.LookupBackedBuilder(featureLookup, carverLookup);
        OverworldBiomeCreator.addBasicFeatures(lookupBackedBuilder);
        DefaultBiomeFeatures.addDefaultOres(lookupBackedBuilder);
        DefaultBiomeFeatures.addDefaultDisks(lookupBackedBuilder);
        DefaultBiomeFeatures.addMushroomFieldsFeatures(lookupBackedBuilder);
        DefaultBiomeFeatures.addDefaultVegetationNearWater(lookupBackedBuilder);
        return OverworldBiomeCreator.biome(0.9f, 1.0f).setEnvironmentAttribute(EnvironmentAttributes.INCREASED_FIRE_BURNOUT_GAMEPLAY, true).setEnvironmentAttribute(EnvironmentAttributes.CAN_PILLAGER_PATROL_SPAWN_GAMEPLAY, false).spawnSettings(builder.build()).generationSettings(lookupBackedBuilder.build()).build();
    }

    public static Biome createSavanna(RegistryEntryLookup<PlacedFeature> featureLookup, RegistryEntryLookup<ConfiguredCarver<?>> carverLookup, boolean windswept, boolean plateau) {
        GenerationSettings.LookupBackedBuilder lookupBackedBuilder = new GenerationSettings.LookupBackedBuilder(featureLookup, carverLookup);
        OverworldBiomeCreator.addBasicFeatures(lookupBackedBuilder);
        if (!windswept) {
            DefaultBiomeFeatures.addSavannaTallGrass(lookupBackedBuilder);
        }
        DefaultBiomeFeatures.addDefaultOres(lookupBackedBuilder);
        DefaultBiomeFeatures.addDefaultDisks(lookupBackedBuilder);
        if (windswept) {
            DefaultBiomeFeatures.addExtraSavannaTrees(lookupBackedBuilder);
            DefaultBiomeFeatures.addDefaultFlowers(lookupBackedBuilder);
            DefaultBiomeFeatures.addWindsweptSavannaGrass(lookupBackedBuilder);
        } else {
            DefaultBiomeFeatures.addSavannaTrees(lookupBackedBuilder);
            DefaultBiomeFeatures.addExtraDefaultFlowers(lookupBackedBuilder);
            DefaultBiomeFeatures.addSavannaGrass(lookupBackedBuilder);
        }
        DefaultBiomeFeatures.addDefaultMushrooms(lookupBackedBuilder);
        DefaultBiomeFeatures.addDefaultVegetation(lookupBackedBuilder, true);
        SpawnSettings.Builder builder = new SpawnSettings.Builder();
        DefaultBiomeFeatures.addFarmAnimals(builder);
        builder.spawn(SpawnGroup.CREATURE, 1, new SpawnSettings.SpawnEntry(EntityType.HORSE, 2, 6)).spawn(SpawnGroup.CREATURE, 1, new SpawnSettings.SpawnEntry(EntityType.DONKEY, 1, 1)).spawn(SpawnGroup.CREATURE, 10, new SpawnSettings.SpawnEntry(EntityType.ARMADILLO, 2, 3));
        DefaultBiomeFeatures.addCaveAndMonstersAndZombieHorse(builder);
        if (plateau) {
            builder.spawn(SpawnGroup.CREATURE, 8, new SpawnSettings.SpawnEntry(EntityType.LLAMA, 4, 4));
            builder.spawn(SpawnGroup.CREATURE, 8, new SpawnSettings.SpawnEntry(EntityType.WOLF, 4, 8));
        }
        return OverworldBiomeCreator.biome(2.0f, 0.0f).precipitation(false).setEnvironmentAttribute(EnvironmentAttributes.SNOW_GOLEM_MELTS_GAMEPLAY, true).spawnSettings(builder.build()).generationSettings(lookupBackedBuilder.build()).build();
    }

    public static Biome createBadlands(RegistryEntryLookup<PlacedFeature> featureLookup, RegistryEntryLookup<ConfiguredCarver<?>> carverLookup, boolean plateau) {
        SpawnSettings.Builder builder = new SpawnSettings.Builder();
        DefaultBiomeFeatures.addFarmAnimals(builder);
        DefaultBiomeFeatures.addCaveAndMonsters(builder);
        builder.spawn(SpawnGroup.CREATURE, 6, new SpawnSettings.SpawnEntry(EntityType.ARMADILLO, 1, 2));
        builder.creatureSpawnProbability(0.03f);
        if (plateau) {
            builder.spawn(SpawnGroup.CREATURE, 2, new SpawnSettings.SpawnEntry(EntityType.WOLF, 4, 8));
            builder.creatureSpawnProbability(0.04f);
        }
        GenerationSettings.LookupBackedBuilder lookupBackedBuilder = new GenerationSettings.LookupBackedBuilder(featureLookup, carverLookup);
        OverworldBiomeCreator.addBasicFeatures(lookupBackedBuilder);
        DefaultBiomeFeatures.addDefaultOres(lookupBackedBuilder);
        DefaultBiomeFeatures.addExtraGoldOre(lookupBackedBuilder);
        DefaultBiomeFeatures.addDefaultDisks(lookupBackedBuilder);
        if (plateau) {
            DefaultBiomeFeatures.addBadlandsPlateauTrees(lookupBackedBuilder);
        }
        DefaultBiomeFeatures.addBadlandsGrass(lookupBackedBuilder);
        DefaultBiomeFeatures.addDefaultMushrooms(lookupBackedBuilder);
        DefaultBiomeFeatures.addBadlandsVegetation(lookupBackedBuilder);
        return OverworldBiomeCreator.biome(2.0f, 0.0f).precipitation(false).setEnvironmentAttribute(EnvironmentAttributes.BACKGROUND_MUSIC_AUDIO, new BackgroundMusic(SoundEvents.MUSIC_OVERWORLD_BADLANDS)).setEnvironmentAttribute(EnvironmentAttributes.SNOW_GOLEM_MELTS_GAMEPLAY, true).effects(new BiomeEffects.Builder().waterColor(4159204).foliageColor(10387789).grassColor(9470285).build()).spawnSettings(builder.build()).generationSettings(lookupBackedBuilder.build()).build();
    }

    private static Biome.Builder createOcean() {
        return OverworldBiomeCreator.biome(0.5f, 0.5f).setEnvironmentAttribute(EnvironmentAttributes.BACKGROUND_MUSIC_AUDIO, BackgroundMusic.DEFAULT.withUnderwater(MusicType.UNDERWATER));
    }

    private static GenerationSettings.LookupBackedBuilder createOceanGenerationSettings(RegistryEntryLookup<PlacedFeature> featureLookup, RegistryEntryLookup<ConfiguredCarver<?>> carverLookup) {
        GenerationSettings.LookupBackedBuilder lookupBackedBuilder = new GenerationSettings.LookupBackedBuilder(featureLookup, carverLookup);
        OverworldBiomeCreator.addBasicFeatures(lookupBackedBuilder);
        DefaultBiomeFeatures.addDefaultOres(lookupBackedBuilder);
        DefaultBiomeFeatures.addDefaultDisks(lookupBackedBuilder);
        DefaultBiomeFeatures.addWaterBiomeOakTrees(lookupBackedBuilder);
        DefaultBiomeFeatures.addDefaultFlowers(lookupBackedBuilder);
        DefaultBiomeFeatures.addDefaultGrass(lookupBackedBuilder);
        DefaultBiomeFeatures.addDefaultMushrooms(lookupBackedBuilder);
        DefaultBiomeFeatures.addDefaultVegetation(lookupBackedBuilder, true);
        return lookupBackedBuilder;
    }

    public static Biome createColdOcean(RegistryEntryLookup<PlacedFeature> featureLookup, RegistryEntryLookup<ConfiguredCarver<?>> carverLookup, boolean deep) {
        SpawnSettings.Builder builder = new SpawnSettings.Builder();
        DefaultBiomeFeatures.addOceanMobs(builder, 3, 4, 15);
        builder.spawn(SpawnGroup.WATER_AMBIENT, 15, new SpawnSettings.SpawnEntry(EntityType.SALMON, 1, 5));
        builder.spawn(SpawnGroup.WATER_CREATURE, 2, new SpawnSettings.SpawnEntry(EntityType.NAUTILUS, 1, 1));
        GenerationSettings.LookupBackedBuilder lookupBackedBuilder = OverworldBiomeCreator.createOceanGenerationSettings(featureLookup, carverLookup);
        lookupBackedBuilder.feature(GenerationStep.Feature.VEGETAL_DECORATION, deep ? OceanPlacedFeatures.SEAGRASS_DEEP_COLD : OceanPlacedFeatures.SEAGRASS_COLD);
        DefaultBiomeFeatures.addKelp(lookupBackedBuilder);
        return OverworldBiomeCreator.createOcean().effects(new BiomeEffects.Builder().waterColor(4020182).build()).spawnSettings(builder.build()).generationSettings(lookupBackedBuilder.build()).build();
    }

    public static Biome createNormalOcean(RegistryEntryLookup<PlacedFeature> featureLookup, RegistryEntryLookup<ConfiguredCarver<?>> carverLookup, boolean deep) {
        SpawnSettings.Builder builder = new SpawnSettings.Builder();
        DefaultBiomeFeatures.addOceanMobs(builder, 1, 4, 10);
        builder.spawn(SpawnGroup.WATER_CREATURE, 1, new SpawnSettings.SpawnEntry(EntityType.DOLPHIN, 1, 2)).spawn(SpawnGroup.WATER_CREATURE, 5, new SpawnSettings.SpawnEntry(EntityType.NAUTILUS, 1, 1));
        GenerationSettings.LookupBackedBuilder lookupBackedBuilder = OverworldBiomeCreator.createOceanGenerationSettings(featureLookup, carverLookup);
        lookupBackedBuilder.feature(GenerationStep.Feature.VEGETAL_DECORATION, deep ? OceanPlacedFeatures.SEAGRASS_DEEP : OceanPlacedFeatures.SEAGRASS_NORMAL);
        DefaultBiomeFeatures.addKelp(lookupBackedBuilder);
        return OverworldBiomeCreator.createOcean().spawnSettings(builder.build()).generationSettings(lookupBackedBuilder.build()).build();
    }

    public static Biome createLukewarmOcean(RegistryEntryLookup<PlacedFeature> featureLookup, RegistryEntryLookup<ConfiguredCarver<?>> carverLookup, boolean deep) {
        SpawnSettings.Builder builder = new SpawnSettings.Builder();
        if (deep) {
            DefaultBiomeFeatures.addOceanMobs(builder, 8, 4, 8);
        } else {
            DefaultBiomeFeatures.addOceanMobs(builder, 10, 2, 15);
        }
        builder.spawn(SpawnGroup.WATER_AMBIENT, 5, new SpawnSettings.SpawnEntry(EntityType.PUFFERFISH, 1, 3)).spawn(SpawnGroup.WATER_AMBIENT, 25, new SpawnSettings.SpawnEntry(EntityType.TROPICAL_FISH, 8, 8)).spawn(SpawnGroup.WATER_CREATURE, 2, new SpawnSettings.SpawnEntry(EntityType.DOLPHIN, 1, 2)).spawn(SpawnGroup.WATER_CREATURE, 5, new SpawnSettings.SpawnEntry(EntityType.NAUTILUS, 1, 1));
        GenerationSettings.LookupBackedBuilder lookupBackedBuilder = OverworldBiomeCreator.createOceanGenerationSettings(featureLookup, carverLookup);
        lookupBackedBuilder.feature(GenerationStep.Feature.VEGETAL_DECORATION, deep ? OceanPlacedFeatures.SEAGRASS_DEEP_WARM : OceanPlacedFeatures.SEAGRASS_WARM);
        DefaultBiomeFeatures.addLessKelp(lookupBackedBuilder);
        return OverworldBiomeCreator.createOcean().setEnvironmentAttribute(EnvironmentAttributes.WATER_FOG_COLOR_VISUAL, -16509389).effects(new BiomeEffects.Builder().waterColor(4566514).build()).spawnSettings(builder.build()).generationSettings(lookupBackedBuilder.build()).build();
    }

    public static Biome createWarmOcean(RegistryEntryLookup<PlacedFeature> featureLookup, RegistryEntryLookup<ConfiguredCarver<?>> carverLookup) {
        SpawnSettings.Builder builder = new SpawnSettings.Builder().spawn(SpawnGroup.WATER_AMBIENT, 15, new SpawnSettings.SpawnEntry(EntityType.PUFFERFISH, 1, 3)).spawn(SpawnGroup.WATER_CREATURE, 5, new SpawnSettings.SpawnEntry(EntityType.NAUTILUS, 1, 1));
        DefaultBiomeFeatures.addWarmOceanMobs(builder, 10, 4);
        GenerationSettings.LookupBackedBuilder lookupBackedBuilder = OverworldBiomeCreator.createOceanGenerationSettings(featureLookup, carverLookup).feature(GenerationStep.Feature.VEGETAL_DECORATION, OceanPlacedFeatures.WARM_OCEAN_VEGETATION).feature(GenerationStep.Feature.VEGETAL_DECORATION, OceanPlacedFeatures.SEAGRASS_WARM).feature(GenerationStep.Feature.VEGETAL_DECORATION, OceanPlacedFeatures.SEA_PICKLE);
        return OverworldBiomeCreator.createOcean().setEnvironmentAttribute(EnvironmentAttributes.WATER_FOG_COLOR_VISUAL, -16507085).effects(new BiomeEffects.Builder().waterColor(4445678).build()).spawnSettings(builder.build()).generationSettings(lookupBackedBuilder.build()).build();
    }

    public static Biome createFrozenOcean(RegistryEntryLookup<PlacedFeature> featureLookup, RegistryEntryLookup<ConfiguredCarver<?>> carverLookup, boolean deep) {
        SpawnSettings.Builder builder = new SpawnSettings.Builder().spawn(SpawnGroup.WATER_CREATURE, 1, new SpawnSettings.SpawnEntry(EntityType.SQUID, 1, 4)).spawn(SpawnGroup.WATER_AMBIENT, 15, new SpawnSettings.SpawnEntry(EntityType.SALMON, 1, 5)).spawn(SpawnGroup.CREATURE, 1, new SpawnSettings.SpawnEntry(EntityType.POLAR_BEAR, 1, 2)).spawn(SpawnGroup.WATER_CREATURE, 2, new SpawnSettings.SpawnEntry(EntityType.NAUTILUS, 1, 1));
        DefaultBiomeFeatures.addCaveAndMonsters(builder);
        builder.spawn(SpawnGroup.MONSTER, 5, new SpawnSettings.SpawnEntry(EntityType.DROWNED, 1, 1));
        float f = deep ? 0.5f : 0.0f;
        GenerationSettings.LookupBackedBuilder lookupBackedBuilder = new GenerationSettings.LookupBackedBuilder(featureLookup, carverLookup);
        DefaultBiomeFeatures.addIcebergs(lookupBackedBuilder);
        OverworldBiomeCreator.addBasicFeatures(lookupBackedBuilder);
        DefaultBiomeFeatures.addBlueIce(lookupBackedBuilder);
        DefaultBiomeFeatures.addDefaultOres(lookupBackedBuilder);
        DefaultBiomeFeatures.addDefaultDisks(lookupBackedBuilder);
        DefaultBiomeFeatures.addWaterBiomeOakTrees(lookupBackedBuilder);
        DefaultBiomeFeatures.addDefaultFlowers(lookupBackedBuilder);
        DefaultBiomeFeatures.addDefaultGrass(lookupBackedBuilder);
        DefaultBiomeFeatures.addDefaultMushrooms(lookupBackedBuilder);
        DefaultBiomeFeatures.addDefaultVegetation(lookupBackedBuilder, true);
        return OverworldBiomeCreator.biome(f, 0.5f).temperatureModifier(Biome.TemperatureModifier.FROZEN).effects(new BiomeEffects.Builder().waterColor(3750089).build()).spawnSettings(builder.build()).generationSettings(lookupBackedBuilder.build()).build();
    }

    public static Biome createNormalForest(RegistryEntryLookup<PlacedFeature> featureLookup, RegistryEntryLookup<ConfiguredCarver<?>> carverLookup, boolean birch, boolean oldGrowth, boolean flower) {
        BackgroundMusic backgroundMusic;
        GenerationSettings.LookupBackedBuilder lookupBackedBuilder = new GenerationSettings.LookupBackedBuilder(featureLookup, carverLookup);
        OverworldBiomeCreator.addBasicFeatures(lookupBackedBuilder);
        if (flower) {
            backgroundMusic = new BackgroundMusic(SoundEvents.MUSIC_OVERWORLD_FLOWER_FOREST);
            lookupBackedBuilder.feature(GenerationStep.Feature.VEGETAL_DECORATION, VegetationPlacedFeatures.FLOWER_FOREST_FLOWERS);
        } else {
            backgroundMusic = new BackgroundMusic(SoundEvents.MUSIC_OVERWORLD_FOREST);
            DefaultBiomeFeatures.addForestFlowers(lookupBackedBuilder);
        }
        DefaultBiomeFeatures.addDefaultOres(lookupBackedBuilder);
        DefaultBiomeFeatures.addDefaultDisks(lookupBackedBuilder);
        if (flower) {
            lookupBackedBuilder.feature(GenerationStep.Feature.VEGETAL_DECORATION, VegetationPlacedFeatures.TREES_FLOWER_FOREST);
            lookupBackedBuilder.feature(GenerationStep.Feature.VEGETAL_DECORATION, VegetationPlacedFeatures.FLOWER_FLOWER_FOREST);
            DefaultBiomeFeatures.addDefaultGrass(lookupBackedBuilder);
        } else {
            if (birch) {
                DefaultBiomeFeatures.addBirchForestWildflowers(lookupBackedBuilder);
                if (oldGrowth) {
                    DefaultBiomeFeatures.addTallBirchTrees(lookupBackedBuilder);
                } else {
                    DefaultBiomeFeatures.addBirchTrees(lookupBackedBuilder);
                }
            } else {
                DefaultBiomeFeatures.addForestTrees(lookupBackedBuilder);
            }
            DefaultBiomeFeatures.addBushes(lookupBackedBuilder);
            DefaultBiomeFeatures.addDefaultFlowers(lookupBackedBuilder);
            DefaultBiomeFeatures.addForestGrass(lookupBackedBuilder);
        }
        DefaultBiomeFeatures.addDefaultMushrooms(lookupBackedBuilder);
        DefaultBiomeFeatures.addDefaultVegetation(lookupBackedBuilder, true);
        SpawnSettings.Builder builder = new SpawnSettings.Builder();
        DefaultBiomeFeatures.addFarmAnimals(builder);
        DefaultBiomeFeatures.addCaveAndMonsters(builder);
        if (flower) {
            builder.spawn(SpawnGroup.CREATURE, 4, new SpawnSettings.SpawnEntry(EntityType.RABBIT, 2, 3));
        } else if (!birch) {
            builder.spawn(SpawnGroup.CREATURE, 5, new SpawnSettings.SpawnEntry(EntityType.WOLF, 4, 4));
        }
        return OverworldBiomeCreator.biome(birch ? 0.6f : 0.7f, birch ? 0.6f : 0.8f).setEnvironmentAttribute(EnvironmentAttributes.BACKGROUND_MUSIC_AUDIO, backgroundMusic).spawnSettings(builder.build()).generationSettings(lookupBackedBuilder.build()).build();
    }

    public static Biome createTaiga(RegistryEntryLookup<PlacedFeature> featureLookup, RegistryEntryLookup<ConfiguredCarver<?>> carverLookup, boolean snowy) {
        SpawnSettings.Builder builder = new SpawnSettings.Builder();
        DefaultBiomeFeatures.addFarmAnimals(builder);
        builder.spawn(SpawnGroup.CREATURE, 8, new SpawnSettings.SpawnEntry(EntityType.WOLF, 4, 4)).spawn(SpawnGroup.CREATURE, 4, new SpawnSettings.SpawnEntry(EntityType.RABBIT, 2, 3)).spawn(SpawnGroup.CREATURE, 8, new SpawnSettings.SpawnEntry(EntityType.FOX, 2, 4));
        DefaultBiomeFeatures.addCaveAndMonsters(builder);
        GenerationSettings.LookupBackedBuilder lookupBackedBuilder = new GenerationSettings.LookupBackedBuilder(featureLookup, carverLookup);
        OverworldBiomeCreator.addBasicFeatures(lookupBackedBuilder);
        DefaultBiomeFeatures.addLargeFerns(lookupBackedBuilder);
        DefaultBiomeFeatures.addDefaultOres(lookupBackedBuilder);
        DefaultBiomeFeatures.addDefaultDisks(lookupBackedBuilder);
        DefaultBiomeFeatures.addTaigaTrees(lookupBackedBuilder);
        DefaultBiomeFeatures.addDefaultFlowers(lookupBackedBuilder);
        DefaultBiomeFeatures.addTaigaGrass(lookupBackedBuilder);
        DefaultBiomeFeatures.addDefaultVegetation(lookupBackedBuilder, true);
        if (snowy) {
            DefaultBiomeFeatures.addSweetBerryBushesSnowy(lookupBackedBuilder);
        } else {
            DefaultBiomeFeatures.addSweetBerryBushes(lookupBackedBuilder);
        }
        int i = snowy ? 4020182 : 4159204;
        return OverworldBiomeCreator.biome(snowy ? -0.5f : 0.25f, snowy ? 0.4f : 0.8f).effects(new BiomeEffects.Builder().waterColor(i).build()).spawnSettings(builder.build()).generationSettings(lookupBackedBuilder.build()).build();
    }

    public static Biome createDenseForest(RegistryEntryLookup<PlacedFeature> featureLookup, RegistryEntryLookup<ConfiguredCarver<?>> carverLookup, boolean paleGarden) {
        SpawnSettings.Builder builder = new SpawnSettings.Builder();
        if (!paleGarden) {
            DefaultBiomeFeatures.addFarmAnimals(builder);
        }
        DefaultBiomeFeatures.addCaveAndMonsters(builder);
        GenerationSettings.LookupBackedBuilder lookupBackedBuilder = new GenerationSettings.LookupBackedBuilder(featureLookup, carverLookup);
        OverworldBiomeCreator.addBasicFeatures(lookupBackedBuilder);
        lookupBackedBuilder.feature(GenerationStep.Feature.VEGETAL_DECORATION, paleGarden ? VegetationPlacedFeatures.PALE_GARDEN_VEGETATION : VegetationPlacedFeatures.DARK_FOREST_VEGETATION);
        if (!paleGarden) {
            DefaultBiomeFeatures.addForestFlowers(lookupBackedBuilder);
        } else {
            lookupBackedBuilder.feature(GenerationStep.Feature.VEGETAL_DECORATION, VegetationPlacedFeatures.PALE_MOSS_PATCH);
            lookupBackedBuilder.feature(GenerationStep.Feature.VEGETAL_DECORATION, VegetationPlacedFeatures.PALE_GARDEN_FLOWERS);
        }
        DefaultBiomeFeatures.addDefaultOres(lookupBackedBuilder);
        DefaultBiomeFeatures.addDefaultDisks(lookupBackedBuilder);
        if (!paleGarden) {
            DefaultBiomeFeatures.addDefaultFlowers(lookupBackedBuilder);
        } else {
            lookupBackedBuilder.feature(GenerationStep.Feature.VEGETAL_DECORATION, VegetationPlacedFeatures.FLOWER_PALE_GARDEN);
        }
        DefaultBiomeFeatures.addForestGrass(lookupBackedBuilder);
        if (!paleGarden) {
            DefaultBiomeFeatures.addDefaultMushrooms(lookupBackedBuilder);
            DefaultBiomeFeatures.addLeafLitter(lookupBackedBuilder);
        }
        DefaultBiomeFeatures.addDefaultVegetation(lookupBackedBuilder, true);
        EnvironmentAttributeMap environmentAttributeMap = EnvironmentAttributeMap.builder().with(EnvironmentAttributes.SKY_COLOR_VISUAL, -4605511).with(EnvironmentAttributes.FOG_COLOR_VISUAL, -8292496).with(EnvironmentAttributes.WATER_FOG_COLOR_VISUAL, -11179648).with(EnvironmentAttributes.BACKGROUND_MUSIC_AUDIO, BackgroundMusic.EMPTY).with(EnvironmentAttributes.MUSIC_VOLUME_AUDIO, Float.valueOf(0.0f)).build();
        EnvironmentAttributeMap environmentAttributeMap2 = EnvironmentAttributeMap.builder().with(EnvironmentAttributes.BACKGROUND_MUSIC_AUDIO, new BackgroundMusic(SoundEvents.MUSIC_OVERWORLD_FOREST)).build();
        return OverworldBiomeCreator.biome(0.7f, 0.8f).addEnvironmentAttributes(paleGarden ? environmentAttributeMap : environmentAttributeMap2).effects(paleGarden ? new BiomeEffects.Builder().waterColor(7768221).grassColor(0x778272).foliageColor(8883574).dryFoliageColor(10528412).build() : new BiomeEffects.Builder().waterColor(4159204).dryFoliageColor(8082228).grassColorModifier(BiomeEffects.GrassColorModifier.DARK_FOREST).build()).spawnSettings(builder.build()).generationSettings(lookupBackedBuilder.build()).build();
    }

    public static Biome createSwamp(RegistryEntryLookup<PlacedFeature> featureLookup, RegistryEntryLookup<ConfiguredCarver<?>> carverLookup) {
        SpawnSettings.Builder builder = new SpawnSettings.Builder();
        DefaultBiomeFeatures.addFarmAnimals(builder);
        DefaultBiomeFeatures.addSwampMobs(builder, 70);
        GenerationSettings.LookupBackedBuilder lookupBackedBuilder = new GenerationSettings.LookupBackedBuilder(featureLookup, carverLookup);
        DefaultBiomeFeatures.addFossils(lookupBackedBuilder);
        OverworldBiomeCreator.addBasicFeatures(lookupBackedBuilder);
        DefaultBiomeFeatures.addDefaultOres(lookupBackedBuilder);
        DefaultBiomeFeatures.addClayDisk(lookupBackedBuilder);
        DefaultBiomeFeatures.addSwampFeatures(lookupBackedBuilder);
        DefaultBiomeFeatures.addDefaultMushrooms(lookupBackedBuilder);
        DefaultBiomeFeatures.addSwampVegetation(lookupBackedBuilder);
        lookupBackedBuilder.feature(GenerationStep.Feature.VEGETAL_DECORATION, OceanPlacedFeatures.SEAGRASS_SWAMP);
        return OverworldBiomeCreator.biome(0.8f, 0.9f).setEnvironmentAttribute(EnvironmentAttributes.WATER_FOG_COLOR_VISUAL, -14474473).setEnvironmentAttributeModifier(EnvironmentAttributes.WATER_FOG_END_DISTANCE_VISUAL, FloatModifier.MULTIPLY, Float.valueOf(0.85f)).setEnvironmentAttribute(EnvironmentAttributes.BACKGROUND_MUSIC_AUDIO, new BackgroundMusic(SoundEvents.MUSIC_OVERWORLD_SWAMP)).setEnvironmentAttribute(EnvironmentAttributes.INCREASED_FIRE_BURNOUT_GAMEPLAY, true).effects(new BiomeEffects.Builder().waterColor(6388580).foliageColor(6975545).dryFoliageColor(8082228).grassColorModifier(BiomeEffects.GrassColorModifier.SWAMP).build()).spawnSettings(builder.build()).generationSettings(lookupBackedBuilder.build()).build();
    }

    public static Biome createMangroveSwamp(RegistryEntryLookup<PlacedFeature> featureLookup, RegistryEntryLookup<ConfiguredCarver<?>> carverLookup) {
        SpawnSettings.Builder builder = new SpawnSettings.Builder();
        DefaultBiomeFeatures.addSwampMobs(builder, 70);
        builder.spawn(SpawnGroup.WATER_AMBIENT, 25, new SpawnSettings.SpawnEntry(EntityType.TROPICAL_FISH, 8, 8));
        GenerationSettings.LookupBackedBuilder lookupBackedBuilder = new GenerationSettings.LookupBackedBuilder(featureLookup, carverLookup);
        DefaultBiomeFeatures.addFossils(lookupBackedBuilder);
        OverworldBiomeCreator.addBasicFeatures(lookupBackedBuilder);
        DefaultBiomeFeatures.addDefaultOres(lookupBackedBuilder);
        DefaultBiomeFeatures.addGrassAndClayDisks(lookupBackedBuilder);
        DefaultBiomeFeatures.addMangroveSwampFeatures(lookupBackedBuilder);
        DefaultBiomeFeatures.addMangroveSwampAquaticFeatures(lookupBackedBuilder);
        return OverworldBiomeCreator.biome(0.8f, 0.9f).setEnvironmentAttribute(EnvironmentAttributes.FOG_COLOR_VISUAL, -4138753).setEnvironmentAttribute(EnvironmentAttributes.WATER_FOG_COLOR_VISUAL, -11699616).setEnvironmentAttributeModifier(EnvironmentAttributes.WATER_FOG_END_DISTANCE_VISUAL, FloatModifier.MULTIPLY, Float.valueOf(0.85f)).setEnvironmentAttribute(EnvironmentAttributes.BACKGROUND_MUSIC_AUDIO, new BackgroundMusic(SoundEvents.MUSIC_OVERWORLD_SWAMP)).setEnvironmentAttribute(EnvironmentAttributes.INCREASED_FIRE_BURNOUT_GAMEPLAY, true).effects(new BiomeEffects.Builder().waterColor(3832426).foliageColor(9285927).dryFoliageColor(8082228).grassColorModifier(BiomeEffects.GrassColorModifier.SWAMP).build()).spawnSettings(builder.build()).generationSettings(lookupBackedBuilder.build()).build();
    }

    public static Biome createRiver(RegistryEntryLookup<PlacedFeature> featureLookup, RegistryEntryLookup<ConfiguredCarver<?>> carverLookup, boolean frozen) {
        SpawnSettings.Builder builder = new SpawnSettings.Builder().spawn(SpawnGroup.WATER_CREATURE, 2, new SpawnSettings.SpawnEntry(EntityType.SQUID, 1, 4)).spawn(SpawnGroup.WATER_AMBIENT, 5, new SpawnSettings.SpawnEntry(EntityType.SALMON, 1, 5));
        DefaultBiomeFeatures.addCaveAndMonsters(builder);
        builder.spawn(SpawnGroup.MONSTER, frozen ? 1 : 100, new SpawnSettings.SpawnEntry(EntityType.DROWNED, 1, 1));
        GenerationSettings.LookupBackedBuilder lookupBackedBuilder = new GenerationSettings.LookupBackedBuilder(featureLookup, carverLookup);
        OverworldBiomeCreator.addBasicFeatures(lookupBackedBuilder);
        DefaultBiomeFeatures.addDefaultOres(lookupBackedBuilder);
        DefaultBiomeFeatures.addDefaultDisks(lookupBackedBuilder);
        DefaultBiomeFeatures.addWaterBiomeOakTrees(lookupBackedBuilder);
        DefaultBiomeFeatures.addBushes(lookupBackedBuilder);
        DefaultBiomeFeatures.addDefaultFlowers(lookupBackedBuilder);
        DefaultBiomeFeatures.addDefaultGrass(lookupBackedBuilder);
        DefaultBiomeFeatures.addDefaultMushrooms(lookupBackedBuilder);
        DefaultBiomeFeatures.addDefaultVegetation(lookupBackedBuilder, true);
        if (!frozen) {
            lookupBackedBuilder.feature(GenerationStep.Feature.VEGETAL_DECORATION, OceanPlacedFeatures.SEAGRASS_RIVER);
        }
        return OverworldBiomeCreator.biome(frozen ? 0.0f : 0.5f, 0.5f).setEnvironmentAttribute(EnvironmentAttributes.BACKGROUND_MUSIC_AUDIO, BackgroundMusic.DEFAULT.withUnderwater(MusicType.UNDERWATER)).effects(new BiomeEffects.Builder().waterColor(frozen ? 3750089 : 4159204).build()).spawnSettings(builder.build()).generationSettings(lookupBackedBuilder.build()).build();
    }

    public static Biome createBeach(RegistryEntryLookup<PlacedFeature> featureLookup, RegistryEntryLookup<ConfiguredCarver<?>> carverLookup, boolean snowy, boolean stony) {
        boolean bl;
        SpawnSettings.Builder builder = new SpawnSettings.Builder();
        boolean bl2 = bl = !stony && !snowy;
        if (bl) {
            builder.spawn(SpawnGroup.CREATURE, 5, new SpawnSettings.SpawnEntry(EntityType.TURTLE, 2, 5));
        }
        DefaultBiomeFeatures.addCaveAndMonsters(builder);
        GenerationSettings.LookupBackedBuilder lookupBackedBuilder = new GenerationSettings.LookupBackedBuilder(featureLookup, carverLookup);
        OverworldBiomeCreator.addBasicFeatures(lookupBackedBuilder);
        DefaultBiomeFeatures.addDefaultOres(lookupBackedBuilder);
        DefaultBiomeFeatures.addDefaultDisks(lookupBackedBuilder);
        DefaultBiomeFeatures.addDefaultFlowers(lookupBackedBuilder);
        DefaultBiomeFeatures.addDefaultGrass(lookupBackedBuilder);
        DefaultBiomeFeatures.addDefaultMushrooms(lookupBackedBuilder);
        DefaultBiomeFeatures.addDefaultVegetation(lookupBackedBuilder, true);
        float f = snowy ? 0.05f : (stony ? 0.2f : 0.8f);
        int i = snowy ? 4020182 : 4159204;
        return OverworldBiomeCreator.biome(f, bl ? 0.4f : 0.3f).effects(new BiomeEffects.Builder().waterColor(i).build()).spawnSettings(builder.build()).generationSettings(lookupBackedBuilder.build()).build();
    }

    public static Biome createTheVoid(RegistryEntryLookup<PlacedFeature> featureLookup, RegistryEntryLookup<ConfiguredCarver<?>> carverLookup) {
        GenerationSettings.LookupBackedBuilder lookupBackedBuilder = new GenerationSettings.LookupBackedBuilder(featureLookup, carverLookup);
        lookupBackedBuilder.feature(GenerationStep.Feature.TOP_LAYER_MODIFICATION, MiscPlacedFeatures.VOID_START_PLATFORM);
        return OverworldBiomeCreator.biome(0.5f, 0.5f).precipitation(false).spawnSettings(new SpawnSettings.Builder().build()).generationSettings(lookupBackedBuilder.build()).build();
    }

    public static Biome createMeadow(RegistryEntryLookup<PlacedFeature> featureLookup, RegistryEntryLookup<ConfiguredCarver<?>> carverLookup, boolean cherryGrove) {
        GenerationSettings.LookupBackedBuilder lookupBackedBuilder = new GenerationSettings.LookupBackedBuilder(featureLookup, carverLookup);
        SpawnSettings.Builder builder = new SpawnSettings.Builder();
        builder.spawn(SpawnGroup.CREATURE, 1, new SpawnSettings.SpawnEntry(cherryGrove ? EntityType.PIG : EntityType.DONKEY, 1, 2)).spawn(SpawnGroup.CREATURE, 2, new SpawnSettings.SpawnEntry(EntityType.RABBIT, 2, 6)).spawn(SpawnGroup.CREATURE, 2, new SpawnSettings.SpawnEntry(EntityType.SHEEP, 2, 4));
        DefaultBiomeFeatures.addCaveAndMonsters(builder);
        OverworldBiomeCreator.addBasicFeatures(lookupBackedBuilder);
        DefaultBiomeFeatures.addPlainsTallGrass(lookupBackedBuilder);
        DefaultBiomeFeatures.addDefaultOres(lookupBackedBuilder);
        DefaultBiomeFeatures.addDefaultDisks(lookupBackedBuilder);
        if (cherryGrove) {
            DefaultBiomeFeatures.addCherryGroveFeatures(lookupBackedBuilder);
        } else {
            DefaultBiomeFeatures.addMeadowFlowers(lookupBackedBuilder);
        }
        DefaultBiomeFeatures.addEmeraldOre(lookupBackedBuilder);
        DefaultBiomeFeatures.addInfestedStone(lookupBackedBuilder);
        if (cherryGrove) {
            BiomeEffects.Builder builder2 = new BiomeEffects.Builder().waterColor(6141935).grassColor(11983713).foliageColor(11983713);
            return OverworldBiomeCreator.biome(0.5f, 0.8f).setEnvironmentAttribute(EnvironmentAttributes.WATER_FOG_COLOR_VISUAL, -10635281).setEnvironmentAttribute(EnvironmentAttributes.BACKGROUND_MUSIC_AUDIO, new BackgroundMusic(SoundEvents.MUSIC_OVERWORLD_CHERRY_GROVE)).effects(builder2.build()).spawnSettings(builder.build()).generationSettings(lookupBackedBuilder.build()).build();
        }
        return OverworldBiomeCreator.biome(0.5f, 0.8f).setEnvironmentAttribute(EnvironmentAttributes.BACKGROUND_MUSIC_AUDIO, new BackgroundMusic(SoundEvents.MUSIC_OVERWORLD_MEADOW)).effects(new BiomeEffects.Builder().waterColor(937679).build()).spawnSettings(builder.build()).generationSettings(lookupBackedBuilder.build()).build();
    }

    private static Biome.Builder createColdPeaks(RegistryEntryLookup<PlacedFeature> featureLookup, RegistryEntryLookup<ConfiguredCarver<?>> carverLookup) {
        GenerationSettings.LookupBackedBuilder lookupBackedBuilder = new GenerationSettings.LookupBackedBuilder(featureLookup, carverLookup);
        SpawnSettings.Builder builder = new SpawnSettings.Builder();
        builder.spawn(SpawnGroup.CREATURE, 5, new SpawnSettings.SpawnEntry(EntityType.GOAT, 1, 3));
        DefaultBiomeFeatures.addCaveAndMonsters(builder);
        OverworldBiomeCreator.addBasicFeatures(lookupBackedBuilder);
        DefaultBiomeFeatures.addFrozenLavaSpring(lookupBackedBuilder);
        DefaultBiomeFeatures.addDefaultOres(lookupBackedBuilder);
        DefaultBiomeFeatures.addDefaultDisks(lookupBackedBuilder);
        DefaultBiomeFeatures.addEmeraldOre(lookupBackedBuilder);
        DefaultBiomeFeatures.addInfestedStone(lookupBackedBuilder);
        return OverworldBiomeCreator.biome(-0.7f, 0.9f).setEnvironmentAttribute(EnvironmentAttributes.INCREASED_FIRE_BURNOUT_GAMEPLAY, true).spawnSettings(builder.build()).generationSettings(lookupBackedBuilder.build());
    }

    public static Biome createFrozenPeaks(RegistryEntryLookup<PlacedFeature> featureLookup, RegistryEntryLookup<ConfiguredCarver<?>> carverLookup) {
        return OverworldBiomeCreator.createColdPeaks(featureLookup, carverLookup).setEnvironmentAttribute(EnvironmentAttributes.BACKGROUND_MUSIC_AUDIO, new BackgroundMusic(SoundEvents.MUSIC_OVERWORLD_FROZEN_PEAKS)).build();
    }

    public static Biome createJaggedPeaks(RegistryEntryLookup<PlacedFeature> featureLookup, RegistryEntryLookup<ConfiguredCarver<?>> carverLookup) {
        return OverworldBiomeCreator.createColdPeaks(featureLookup, carverLookup).setEnvironmentAttribute(EnvironmentAttributes.BACKGROUND_MUSIC_AUDIO, new BackgroundMusic(SoundEvents.MUSIC_OVERWORLD_JAGGED_PEAKS)).build();
    }

    public static Biome createStonyPeaks(RegistryEntryLookup<PlacedFeature> featureLookup, RegistryEntryLookup<ConfiguredCarver<?>> carverLookup) {
        GenerationSettings.LookupBackedBuilder lookupBackedBuilder = new GenerationSettings.LookupBackedBuilder(featureLookup, carverLookup);
        SpawnSettings.Builder builder = new SpawnSettings.Builder();
        DefaultBiomeFeatures.addCaveAndMonsters(builder);
        OverworldBiomeCreator.addBasicFeatures(lookupBackedBuilder);
        DefaultBiomeFeatures.addDefaultOres(lookupBackedBuilder);
        DefaultBiomeFeatures.addDefaultDisks(lookupBackedBuilder);
        DefaultBiomeFeatures.addEmeraldOre(lookupBackedBuilder);
        DefaultBiomeFeatures.addInfestedStone(lookupBackedBuilder);
        return OverworldBiomeCreator.biome(1.0f, 0.3f).setEnvironmentAttribute(EnvironmentAttributes.BACKGROUND_MUSIC_AUDIO, new BackgroundMusic(SoundEvents.MUSIC_OVERWORLD_STONY_PEAKS)).spawnSettings(builder.build()).generationSettings(lookupBackedBuilder.build()).build();
    }

    public static Biome createSnowySlopes(RegistryEntryLookup<PlacedFeature> featureLookup, RegistryEntryLookup<ConfiguredCarver<?>> carverLookup) {
        GenerationSettings.LookupBackedBuilder lookupBackedBuilder = new GenerationSettings.LookupBackedBuilder(featureLookup, carverLookup);
        SpawnSettings.Builder builder = new SpawnSettings.Builder();
        builder.spawn(SpawnGroup.CREATURE, 4, new SpawnSettings.SpawnEntry(EntityType.RABBIT, 2, 3)).spawn(SpawnGroup.CREATURE, 5, new SpawnSettings.SpawnEntry(EntityType.GOAT, 1, 3));
        DefaultBiomeFeatures.addCaveAndMonsters(builder);
        OverworldBiomeCreator.addBasicFeatures(lookupBackedBuilder);
        DefaultBiomeFeatures.addFrozenLavaSpring(lookupBackedBuilder);
        DefaultBiomeFeatures.addDefaultOres(lookupBackedBuilder);
        DefaultBiomeFeatures.addDefaultDisks(lookupBackedBuilder);
        DefaultBiomeFeatures.addDefaultVegetation(lookupBackedBuilder, false);
        DefaultBiomeFeatures.addEmeraldOre(lookupBackedBuilder);
        DefaultBiomeFeatures.addInfestedStone(lookupBackedBuilder);
        return OverworldBiomeCreator.biome(-0.3f, 0.9f).setEnvironmentAttribute(EnvironmentAttributes.BACKGROUND_MUSIC_AUDIO, new BackgroundMusic(SoundEvents.MUSIC_OVERWORLD_SNOWY_SLOPES)).setEnvironmentAttribute(EnvironmentAttributes.INCREASED_FIRE_BURNOUT_GAMEPLAY, true).spawnSettings(builder.build()).generationSettings(lookupBackedBuilder.build()).build();
    }

    public static Biome createGrove(RegistryEntryLookup<PlacedFeature> featureLookup, RegistryEntryLookup<ConfiguredCarver<?>> carverLookup) {
        GenerationSettings.LookupBackedBuilder lookupBackedBuilder = new GenerationSettings.LookupBackedBuilder(featureLookup, carverLookup);
        SpawnSettings.Builder builder = new SpawnSettings.Builder();
        builder.spawn(SpawnGroup.CREATURE, 1, new SpawnSettings.SpawnEntry(EntityType.WOLF, 1, 1)).spawn(SpawnGroup.CREATURE, 8, new SpawnSettings.SpawnEntry(EntityType.RABBIT, 2, 3)).spawn(SpawnGroup.CREATURE, 4, new SpawnSettings.SpawnEntry(EntityType.FOX, 2, 4));
        DefaultBiomeFeatures.addCaveAndMonsters(builder);
        OverworldBiomeCreator.addBasicFeatures(lookupBackedBuilder);
        DefaultBiomeFeatures.addFrozenLavaSpring(lookupBackedBuilder);
        DefaultBiomeFeatures.addDefaultOres(lookupBackedBuilder);
        DefaultBiomeFeatures.addDefaultDisks(lookupBackedBuilder);
        DefaultBiomeFeatures.addGroveTrees(lookupBackedBuilder);
        DefaultBiomeFeatures.addDefaultVegetation(lookupBackedBuilder, false);
        DefaultBiomeFeatures.addEmeraldOre(lookupBackedBuilder);
        DefaultBiomeFeatures.addInfestedStone(lookupBackedBuilder);
        return OverworldBiomeCreator.biome(-0.2f, 0.8f).setEnvironmentAttribute(EnvironmentAttributes.BACKGROUND_MUSIC_AUDIO, new BackgroundMusic(SoundEvents.MUSIC_OVERWORLD_GROVE)).spawnSettings(builder.build()).generationSettings(lookupBackedBuilder.build()).build();
    }

    public static Biome createLushCaves(RegistryEntryLookup<PlacedFeature> featureLookup, RegistryEntryLookup<ConfiguredCarver<?>> carverLookup) {
        SpawnSettings.Builder builder = new SpawnSettings.Builder();
        builder.spawn(SpawnGroup.AXOLOTLS, 10, new SpawnSettings.SpawnEntry(EntityType.AXOLOTL, 4, 6));
        builder.spawn(SpawnGroup.WATER_AMBIENT, 25, new SpawnSettings.SpawnEntry(EntityType.TROPICAL_FISH, 8, 8));
        DefaultBiomeFeatures.addCaveAndMonsters(builder);
        GenerationSettings.LookupBackedBuilder lookupBackedBuilder = new GenerationSettings.LookupBackedBuilder(featureLookup, carverLookup);
        OverworldBiomeCreator.addBasicFeatures(lookupBackedBuilder);
        DefaultBiomeFeatures.addPlainsTallGrass(lookupBackedBuilder);
        DefaultBiomeFeatures.addDefaultOres(lookupBackedBuilder);
        DefaultBiomeFeatures.addClayOre(lookupBackedBuilder);
        DefaultBiomeFeatures.addDefaultDisks(lookupBackedBuilder);
        DefaultBiomeFeatures.addLushCavesDecoration(lookupBackedBuilder);
        return OverworldBiomeCreator.biome(0.5f, 0.5f).setEnvironmentAttribute(EnvironmentAttributes.BACKGROUND_MUSIC_AUDIO, new BackgroundMusic(SoundEvents.MUSIC_OVERWORLD_LUSH_CAVES)).spawnSettings(builder.build()).generationSettings(lookupBackedBuilder.build()).build();
    }

    public static Biome createDripstoneCaves(RegistryEntryLookup<PlacedFeature> featureLookup, RegistryEntryLookup<ConfiguredCarver<?>> carverLookup) {
        SpawnSettings.Builder builder = new SpawnSettings.Builder();
        DefaultBiomeFeatures.addDripstoneCaveMobs(builder);
        GenerationSettings.LookupBackedBuilder lookupBackedBuilder = new GenerationSettings.LookupBackedBuilder(featureLookup, carverLookup);
        OverworldBiomeCreator.addBasicFeatures(lookupBackedBuilder);
        DefaultBiomeFeatures.addPlainsTallGrass(lookupBackedBuilder);
        DefaultBiomeFeatures.addDefaultOres(lookupBackedBuilder, true);
        DefaultBiomeFeatures.addDefaultDisks(lookupBackedBuilder);
        DefaultBiomeFeatures.addPlainsFeatures(lookupBackedBuilder);
        DefaultBiomeFeatures.addDefaultMushrooms(lookupBackedBuilder);
        DefaultBiomeFeatures.addDefaultVegetation(lookupBackedBuilder, false);
        DefaultBiomeFeatures.addDripstone(lookupBackedBuilder);
        return OverworldBiomeCreator.biome(0.8f, 0.4f).setEnvironmentAttribute(EnvironmentAttributes.BACKGROUND_MUSIC_AUDIO, new BackgroundMusic(SoundEvents.MUSIC_OVERWORLD_DRIPSTONE_CAVES)).spawnSettings(builder.build()).generationSettings(lookupBackedBuilder.build()).build();
    }

    public static Biome createDeepDark(RegistryEntryLookup<PlacedFeature> featureLookup, RegistryEntryLookup<ConfiguredCarver<?>> carverLookup) {
        SpawnSettings.Builder builder = new SpawnSettings.Builder();
        GenerationSettings.LookupBackedBuilder lookupBackedBuilder = new GenerationSettings.LookupBackedBuilder(featureLookup, carverLookup);
        lookupBackedBuilder.carver(ConfiguredCarvers.CAVE);
        lookupBackedBuilder.carver(ConfiguredCarvers.CAVE_EXTRA_UNDERGROUND);
        lookupBackedBuilder.carver(ConfiguredCarvers.CANYON);
        DefaultBiomeFeatures.addAmethystGeodes(lookupBackedBuilder);
        DefaultBiomeFeatures.addDungeons(lookupBackedBuilder);
        DefaultBiomeFeatures.addMineables(lookupBackedBuilder);
        DefaultBiomeFeatures.addFrozenTopLayer(lookupBackedBuilder);
        DefaultBiomeFeatures.addPlainsTallGrass(lookupBackedBuilder);
        DefaultBiomeFeatures.addDefaultOres(lookupBackedBuilder);
        DefaultBiomeFeatures.addDefaultDisks(lookupBackedBuilder);
        DefaultBiomeFeatures.addPlainsFeatures(lookupBackedBuilder);
        DefaultBiomeFeatures.addDefaultMushrooms(lookupBackedBuilder);
        DefaultBiomeFeatures.addDefaultVegetation(lookupBackedBuilder, false);
        DefaultBiomeFeatures.addSculk(lookupBackedBuilder);
        return OverworldBiomeCreator.biome(0.8f, 0.4f).setEnvironmentAttribute(EnvironmentAttributes.BACKGROUND_MUSIC_AUDIO, new BackgroundMusic(SoundEvents.MUSIC_OVERWORLD_DEEP_DARK)).spawnSettings(builder.build()).generationSettings(lookupBackedBuilder.build()).build();
    }
}


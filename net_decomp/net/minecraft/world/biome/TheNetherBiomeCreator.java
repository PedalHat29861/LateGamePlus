/*
 * Decompiled with CFR 0.152.
 */
package net.minecraft.world.biome;

import java.util.List;
import java.util.Optional;
import net.minecraft.entity.EntityType;
import net.minecraft.entity.SpawnGroup;
import net.minecraft.particle.ParticleTypes;
import net.minecraft.registry.RegistryEntryLookup;
import net.minecraft.sound.BiomeAdditionsSound;
import net.minecraft.sound.BiomeMoodSound;
import net.minecraft.sound.SoundEvents;
import net.minecraft.world.attribute.AmbientParticle;
import net.minecraft.world.attribute.AmbientSounds;
import net.minecraft.world.attribute.BackgroundMusic;
import net.minecraft.world.attribute.EnvironmentAttributes;
import net.minecraft.world.biome.Biome;
import net.minecraft.world.biome.BiomeEffects;
import net.minecraft.world.biome.GenerationSettings;
import net.minecraft.world.biome.SpawnSettings;
import net.minecraft.world.gen.GenerationStep;
import net.minecraft.world.gen.carver.ConfiguredCarver;
import net.minecraft.world.gen.carver.ConfiguredCarvers;
import net.minecraft.world.gen.feature.DefaultBiomeFeatures;
import net.minecraft.world.gen.feature.MiscPlacedFeatures;
import net.minecraft.world.gen.feature.NetherPlacedFeatures;
import net.minecraft.world.gen.feature.OrePlacedFeatures;
import net.minecraft.world.gen.feature.PlacedFeature;
import net.minecraft.world.gen.feature.TreePlacedFeatures;
import net.minecraft.world.gen.feature.VegetationPlacedFeatures;

public class TheNetherBiomeCreator {
    private static Biome.Builder biome() {
        return new Biome.Builder().precipitation(false).temperature(2.0f).downfall(0.0f).effects(new BiomeEffects.Builder().waterColor(4159204).build());
    }

    public static Biome createNetherWastes(RegistryEntryLookup<PlacedFeature> featureLookup, RegistryEntryLookup<ConfiguredCarver<?>> carverLookup) {
        SpawnSettings spawnSettings = new SpawnSettings.Builder().spawn(SpawnGroup.MONSTER, 50, new SpawnSettings.SpawnEntry(EntityType.GHAST, 4, 4)).spawn(SpawnGroup.MONSTER, 100, new SpawnSettings.SpawnEntry(EntityType.ZOMBIFIED_PIGLIN, 4, 4)).spawn(SpawnGroup.MONSTER, 2, new SpawnSettings.SpawnEntry(EntityType.MAGMA_CUBE, 4, 4)).spawn(SpawnGroup.MONSTER, 1, new SpawnSettings.SpawnEntry(EntityType.ENDERMAN, 4, 4)).spawn(SpawnGroup.MONSTER, 15, new SpawnSettings.SpawnEntry(EntityType.PIGLIN, 4, 4)).spawn(SpawnGroup.CREATURE, 60, new SpawnSettings.SpawnEntry(EntityType.STRIDER, 1, 2)).build();
        GenerationSettings.LookupBackedBuilder lookupBackedBuilder = new GenerationSettings.LookupBackedBuilder(featureLookup, carverLookup).carver(ConfiguredCarvers.NETHER_CAVE).feature(GenerationStep.Feature.VEGETAL_DECORATION, MiscPlacedFeatures.SPRING_LAVA);
        DefaultBiomeFeatures.addDefaultMushrooms(lookupBackedBuilder);
        lookupBackedBuilder.feature(GenerationStep.Feature.UNDERGROUND_DECORATION, NetherPlacedFeatures.SPRING_OPEN).feature(GenerationStep.Feature.UNDERGROUND_DECORATION, NetherPlacedFeatures.PATCH_FIRE).feature(GenerationStep.Feature.UNDERGROUND_DECORATION, NetherPlacedFeatures.PATCH_SOUL_FIRE).feature(GenerationStep.Feature.UNDERGROUND_DECORATION, NetherPlacedFeatures.GLOWSTONE_EXTRA).feature(GenerationStep.Feature.UNDERGROUND_DECORATION, NetherPlacedFeatures.GLOWSTONE).feature(GenerationStep.Feature.UNDERGROUND_DECORATION, VegetationPlacedFeatures.BROWN_MUSHROOM_NETHER).feature(GenerationStep.Feature.UNDERGROUND_DECORATION, VegetationPlacedFeatures.RED_MUSHROOM_NETHER).feature(GenerationStep.Feature.UNDERGROUND_DECORATION, OrePlacedFeatures.ORE_MAGMA).feature(GenerationStep.Feature.UNDERGROUND_DECORATION, NetherPlacedFeatures.SPRING_CLOSED);
        DefaultBiomeFeatures.addNetherMineables(lookupBackedBuilder);
        return TheNetherBiomeCreator.biome().setEnvironmentAttribute(EnvironmentAttributes.FOG_COLOR_VISUAL, -13432824).setEnvironmentAttribute(EnvironmentAttributes.BACKGROUND_MUSIC_AUDIO, new BackgroundMusic(SoundEvents.MUSIC_NETHER_NETHER_WASTES)).setEnvironmentAttribute(EnvironmentAttributes.AMBIENT_SOUNDS_AUDIO, new AmbientSounds(Optional.of(SoundEvents.AMBIENT_NETHER_WASTES_LOOP), Optional.of(new BiomeMoodSound(SoundEvents.AMBIENT_NETHER_WASTES_MOOD, 6000, 8, 2.0)), List.of(new BiomeAdditionsSound(SoundEvents.AMBIENT_NETHER_WASTES_ADDITIONS, 0.0111)))).spawnSettings(spawnSettings).generationSettings(lookupBackedBuilder.build()).build();
    }

    public static Biome createSoulSandValley(RegistryEntryLookup<PlacedFeature> featureLookup, RegistryEntryLookup<ConfiguredCarver<?>> carverLookup) {
        double d = 0.7;
        double e = 0.15;
        SpawnSettings spawnSettings = new SpawnSettings.Builder().spawn(SpawnGroup.MONSTER, 20, new SpawnSettings.SpawnEntry(EntityType.SKELETON, 5, 5)).spawn(SpawnGroup.MONSTER, 50, new SpawnSettings.SpawnEntry(EntityType.GHAST, 4, 4)).spawn(SpawnGroup.MONSTER, 1, new SpawnSettings.SpawnEntry(EntityType.ENDERMAN, 4, 4)).spawn(SpawnGroup.CREATURE, 60, new SpawnSettings.SpawnEntry(EntityType.STRIDER, 1, 2)).spawnCost(EntityType.SKELETON, 0.7, 0.15).spawnCost(EntityType.GHAST, 0.7, 0.15).spawnCost(EntityType.ENDERMAN, 0.7, 0.15).spawnCost(EntityType.STRIDER, 0.7, 0.15).build();
        GenerationSettings.LookupBackedBuilder lookupBackedBuilder = new GenerationSettings.LookupBackedBuilder(featureLookup, carverLookup).carver(ConfiguredCarvers.NETHER_CAVE).feature(GenerationStep.Feature.VEGETAL_DECORATION, MiscPlacedFeatures.SPRING_LAVA).feature(GenerationStep.Feature.LOCAL_MODIFICATIONS, NetherPlacedFeatures.BASALT_PILLAR).feature(GenerationStep.Feature.UNDERGROUND_DECORATION, NetherPlacedFeatures.SPRING_OPEN).feature(GenerationStep.Feature.UNDERGROUND_DECORATION, NetherPlacedFeatures.PATCH_FIRE).feature(GenerationStep.Feature.UNDERGROUND_DECORATION, NetherPlacedFeatures.PATCH_SOUL_FIRE).feature(GenerationStep.Feature.UNDERGROUND_DECORATION, NetherPlacedFeatures.GLOWSTONE_EXTRA).feature(GenerationStep.Feature.UNDERGROUND_DECORATION, NetherPlacedFeatures.GLOWSTONE).feature(GenerationStep.Feature.UNDERGROUND_DECORATION, NetherPlacedFeatures.PATCH_CRIMSON_ROOTS).feature(GenerationStep.Feature.UNDERGROUND_DECORATION, OrePlacedFeatures.ORE_MAGMA).feature(GenerationStep.Feature.UNDERGROUND_DECORATION, NetherPlacedFeatures.SPRING_CLOSED).feature(GenerationStep.Feature.UNDERGROUND_DECORATION, OrePlacedFeatures.ORE_SOUL_SAND);
        DefaultBiomeFeatures.addNetherMineables(lookupBackedBuilder);
        return TheNetherBiomeCreator.biome().setEnvironmentAttribute(EnvironmentAttributes.FOG_COLOR_VISUAL, -14989499).setEnvironmentAttribute(EnvironmentAttributes.BACKGROUND_MUSIC_AUDIO, new BackgroundMusic(SoundEvents.MUSIC_NETHER_SOUL_SAND_VALLEY)).setEnvironmentAttribute(EnvironmentAttributes.AMBIENT_PARTICLES_VISUAL, AmbientParticle.of(ParticleTypes.ASH, 0.00625f)).setEnvironmentAttribute(EnvironmentAttributes.AMBIENT_SOUNDS_AUDIO, new AmbientSounds(Optional.of(SoundEvents.AMBIENT_SOUL_SAND_VALLEY_LOOP), Optional.of(new BiomeMoodSound(SoundEvents.AMBIENT_SOUL_SAND_VALLEY_MOOD, 6000, 8, 2.0)), List.of(new BiomeAdditionsSound(SoundEvents.AMBIENT_SOUL_SAND_VALLEY_ADDITIONS, 0.0111)))).spawnSettings(spawnSettings).generationSettings(lookupBackedBuilder.build()).build();
    }

    public static Biome createBasaltDeltas(RegistryEntryLookup<PlacedFeature> featureLookup, RegistryEntryLookup<ConfiguredCarver<?>> carverLookup) {
        SpawnSettings spawnSettings = new SpawnSettings.Builder().spawn(SpawnGroup.MONSTER, 40, new SpawnSettings.SpawnEntry(EntityType.GHAST, 1, 1)).spawn(SpawnGroup.MONSTER, 100, new SpawnSettings.SpawnEntry(EntityType.MAGMA_CUBE, 2, 5)).spawn(SpawnGroup.CREATURE, 60, new SpawnSettings.SpawnEntry(EntityType.STRIDER, 1, 2)).build();
        GenerationSettings.LookupBackedBuilder lookupBackedBuilder = new GenerationSettings.LookupBackedBuilder(featureLookup, carverLookup).carver(ConfiguredCarvers.NETHER_CAVE).feature(GenerationStep.Feature.SURFACE_STRUCTURES, NetherPlacedFeatures.DELTA).feature(GenerationStep.Feature.SURFACE_STRUCTURES, NetherPlacedFeatures.SMALL_BASALT_COLUMNS).feature(GenerationStep.Feature.SURFACE_STRUCTURES, NetherPlacedFeatures.LARGE_BASALT_COLUMNS).feature(GenerationStep.Feature.UNDERGROUND_DECORATION, NetherPlacedFeatures.BASALT_BLOBS).feature(GenerationStep.Feature.UNDERGROUND_DECORATION, NetherPlacedFeatures.BLACKSTONE_BLOBS).feature(GenerationStep.Feature.UNDERGROUND_DECORATION, NetherPlacedFeatures.SPRING_DELTA).feature(GenerationStep.Feature.UNDERGROUND_DECORATION, NetherPlacedFeatures.PATCH_FIRE).feature(GenerationStep.Feature.UNDERGROUND_DECORATION, NetherPlacedFeatures.PATCH_SOUL_FIRE).feature(GenerationStep.Feature.UNDERGROUND_DECORATION, NetherPlacedFeatures.GLOWSTONE_EXTRA).feature(GenerationStep.Feature.UNDERGROUND_DECORATION, NetherPlacedFeatures.GLOWSTONE).feature(GenerationStep.Feature.UNDERGROUND_DECORATION, VegetationPlacedFeatures.BROWN_MUSHROOM_NETHER).feature(GenerationStep.Feature.UNDERGROUND_DECORATION, VegetationPlacedFeatures.RED_MUSHROOM_NETHER).feature(GenerationStep.Feature.UNDERGROUND_DECORATION, OrePlacedFeatures.ORE_MAGMA).feature(GenerationStep.Feature.UNDERGROUND_DECORATION, NetherPlacedFeatures.SPRING_CLOSED_DOUBLE).feature(GenerationStep.Feature.UNDERGROUND_DECORATION, OrePlacedFeatures.ORE_GOLD_DELTAS).feature(GenerationStep.Feature.UNDERGROUND_DECORATION, OrePlacedFeatures.ORE_QUARTZ_DELTAS);
        DefaultBiomeFeatures.addAncientDebris(lookupBackedBuilder);
        return TheNetherBiomeCreator.biome().setEnvironmentAttribute(EnvironmentAttributes.FOG_COLOR_VISUAL, -9937040).setEnvironmentAttribute(EnvironmentAttributes.AMBIENT_PARTICLES_VISUAL, AmbientParticle.of(ParticleTypes.WHITE_ASH, 0.118093334f)).setEnvironmentAttribute(EnvironmentAttributes.BACKGROUND_MUSIC_AUDIO, new BackgroundMusic(SoundEvents.MUSIC_NETHER_BASALT_DELTAS)).setEnvironmentAttribute(EnvironmentAttributes.AMBIENT_SOUNDS_AUDIO, new AmbientSounds(Optional.of(SoundEvents.AMBIENT_BASALT_DELTAS_LOOP), Optional.of(new BiomeMoodSound(SoundEvents.AMBIENT_BASALT_DELTAS_MOOD, 6000, 8, 2.0)), List.of(new BiomeAdditionsSound(SoundEvents.AMBIENT_BASALT_DELTAS_ADDITIONS, 0.0111)))).spawnSettings(spawnSettings).generationSettings(lookupBackedBuilder.build()).build();
    }

    public static Biome createCrimsonForest(RegistryEntryLookup<PlacedFeature> featureLookup, RegistryEntryLookup<ConfiguredCarver<?>> carverLookup) {
        SpawnSettings spawnSettings = new SpawnSettings.Builder().spawn(SpawnGroup.MONSTER, 1, new SpawnSettings.SpawnEntry(EntityType.ZOMBIFIED_PIGLIN, 2, 4)).spawn(SpawnGroup.MONSTER, 9, new SpawnSettings.SpawnEntry(EntityType.HOGLIN, 3, 4)).spawn(SpawnGroup.MONSTER, 5, new SpawnSettings.SpawnEntry(EntityType.PIGLIN, 3, 4)).spawn(SpawnGroup.CREATURE, 60, new SpawnSettings.SpawnEntry(EntityType.STRIDER, 1, 2)).build();
        GenerationSettings.LookupBackedBuilder lookupBackedBuilder = new GenerationSettings.LookupBackedBuilder(featureLookup, carverLookup).carver(ConfiguredCarvers.NETHER_CAVE).feature(GenerationStep.Feature.VEGETAL_DECORATION, MiscPlacedFeatures.SPRING_LAVA);
        DefaultBiomeFeatures.addDefaultMushrooms(lookupBackedBuilder);
        lookupBackedBuilder.feature(GenerationStep.Feature.UNDERGROUND_DECORATION, NetherPlacedFeatures.SPRING_OPEN).feature(GenerationStep.Feature.UNDERGROUND_DECORATION, NetherPlacedFeatures.PATCH_FIRE).feature(GenerationStep.Feature.UNDERGROUND_DECORATION, NetherPlacedFeatures.GLOWSTONE_EXTRA).feature(GenerationStep.Feature.UNDERGROUND_DECORATION, NetherPlacedFeatures.GLOWSTONE).feature(GenerationStep.Feature.UNDERGROUND_DECORATION, OrePlacedFeatures.ORE_MAGMA).feature(GenerationStep.Feature.UNDERGROUND_DECORATION, NetherPlacedFeatures.SPRING_CLOSED).feature(GenerationStep.Feature.VEGETAL_DECORATION, NetherPlacedFeatures.WEEPING_VINES).feature(GenerationStep.Feature.VEGETAL_DECORATION, TreePlacedFeatures.CRIMSON_FUNGI).feature(GenerationStep.Feature.VEGETAL_DECORATION, NetherPlacedFeatures.CRIMSON_FOREST_VEGETATION);
        DefaultBiomeFeatures.addNetherMineables(lookupBackedBuilder);
        return TheNetherBiomeCreator.biome().setEnvironmentAttribute(EnvironmentAttributes.FOG_COLOR_VISUAL, -13434109).setEnvironmentAttribute(EnvironmentAttributes.BACKGROUND_MUSIC_AUDIO, new BackgroundMusic(SoundEvents.MUSIC_NETHER_CRIMSON_FOREST)).setEnvironmentAttribute(EnvironmentAttributes.AMBIENT_PARTICLES_VISUAL, AmbientParticle.of(ParticleTypes.CRIMSON_SPORE, 0.025f)).setEnvironmentAttribute(EnvironmentAttributes.AMBIENT_SOUNDS_AUDIO, new AmbientSounds(Optional.of(SoundEvents.AMBIENT_CRIMSON_FOREST_LOOP), Optional.of(new BiomeMoodSound(SoundEvents.AMBIENT_CRIMSON_FOREST_MOOD, 6000, 8, 2.0)), List.of(new BiomeAdditionsSound(SoundEvents.AMBIENT_CRIMSON_FOREST_ADDITIONS, 0.0111)))).spawnSettings(spawnSettings).generationSettings(lookupBackedBuilder.build()).build();
    }

    public static Biome createWarpedForest(RegistryEntryLookup<PlacedFeature> featureLookup, RegistryEntryLookup<ConfiguredCarver<?>> carverLookup) {
        SpawnSettings spawnSettings = new SpawnSettings.Builder().spawn(SpawnGroup.MONSTER, 1, new SpawnSettings.SpawnEntry(EntityType.ENDERMAN, 4, 4)).spawn(SpawnGroup.CREATURE, 60, new SpawnSettings.SpawnEntry(EntityType.STRIDER, 1, 2)).spawnCost(EntityType.ENDERMAN, 1.0, 0.12).build();
        GenerationSettings.LookupBackedBuilder lookupBackedBuilder = new GenerationSettings.LookupBackedBuilder(featureLookup, carverLookup).carver(ConfiguredCarvers.NETHER_CAVE).feature(GenerationStep.Feature.VEGETAL_DECORATION, MiscPlacedFeatures.SPRING_LAVA);
        DefaultBiomeFeatures.addDefaultMushrooms(lookupBackedBuilder);
        lookupBackedBuilder.feature(GenerationStep.Feature.UNDERGROUND_DECORATION, NetherPlacedFeatures.SPRING_OPEN).feature(GenerationStep.Feature.UNDERGROUND_DECORATION, NetherPlacedFeatures.PATCH_FIRE).feature(GenerationStep.Feature.UNDERGROUND_DECORATION, NetherPlacedFeatures.PATCH_SOUL_FIRE).feature(GenerationStep.Feature.UNDERGROUND_DECORATION, NetherPlacedFeatures.GLOWSTONE_EXTRA).feature(GenerationStep.Feature.UNDERGROUND_DECORATION, NetherPlacedFeatures.GLOWSTONE).feature(GenerationStep.Feature.UNDERGROUND_DECORATION, OrePlacedFeatures.ORE_MAGMA).feature(GenerationStep.Feature.UNDERGROUND_DECORATION, NetherPlacedFeatures.SPRING_CLOSED).feature(GenerationStep.Feature.VEGETAL_DECORATION, TreePlacedFeatures.WARPED_FUNGI).feature(GenerationStep.Feature.VEGETAL_DECORATION, NetherPlacedFeatures.WARPED_FOREST_VEGETATION).feature(GenerationStep.Feature.VEGETAL_DECORATION, NetherPlacedFeatures.NETHER_SPROUTS).feature(GenerationStep.Feature.VEGETAL_DECORATION, NetherPlacedFeatures.TWISTING_VINES);
        DefaultBiomeFeatures.addNetherMineables(lookupBackedBuilder);
        return TheNetherBiomeCreator.biome().setEnvironmentAttribute(EnvironmentAttributes.FOG_COLOR_VISUAL, -15071974).setEnvironmentAttribute(EnvironmentAttributes.BACKGROUND_MUSIC_AUDIO, new BackgroundMusic(SoundEvents.MUSIC_NETHER_WARPED_FOREST)).setEnvironmentAttribute(EnvironmentAttributes.AMBIENT_PARTICLES_VISUAL, AmbientParticle.of(ParticleTypes.WARPED_SPORE, 0.01428f)).setEnvironmentAttribute(EnvironmentAttributes.AMBIENT_SOUNDS_AUDIO, new AmbientSounds(Optional.of(SoundEvents.AMBIENT_WARPED_FOREST_LOOP), Optional.of(new BiomeMoodSound(SoundEvents.AMBIENT_WARPED_FOREST_MOOD, 6000, 8, 2.0)), List.of(new BiomeAdditionsSound(SoundEvents.AMBIENT_WARPED_FOREST_ADDITIONS, 0.0111)))).spawnSettings(spawnSettings).generationSettings(lookupBackedBuilder.build()).build();
    }
}


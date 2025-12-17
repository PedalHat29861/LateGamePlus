/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  com.mojang.datafixers.kinds.App
 *  com.mojang.datafixers.kinds.Applicative
 *  com.mojang.serialization.Codec
 *  com.mojang.serialization.codecs.RecordCodecBuilder
 */
package net.minecraft.world.gen.chunk;

import com.mojang.datafixers.kinds.App;
import com.mojang.datafixers.kinds.Applicative;
import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import java.lang.invoke.MethodHandle;
import java.lang.runtime.ObjectMethods;
import java.util.List;
import net.minecraft.SharedConstants;
import net.minecraft.block.BlockState;
import net.minecraft.block.Blocks;
import net.minecraft.registry.Registerable;
import net.minecraft.registry.RegistryKey;
import net.minecraft.registry.RegistryKeys;
import net.minecraft.registry.entry.RegistryElementCodec;
import net.minecraft.registry.entry.RegistryEntry;
import net.minecraft.util.Identifier;
import net.minecraft.util.math.random.ChunkRandom;
import net.minecraft.world.biome.source.util.MultiNoiseUtil;
import net.minecraft.world.biome.source.util.VanillaBiomeParameters;
import net.minecraft.world.gen.chunk.GenerationShapeConfig;
import net.minecraft.world.gen.densityfunction.DensityFunctions;
import net.minecraft.world.gen.noise.NoiseRouter;
import net.minecraft.world.gen.surfacebuilder.MaterialRules;
import net.minecraft.world.gen.surfacebuilder.VanillaSurfaceRules;

public record ChunkGeneratorSettings(GenerationShapeConfig generationShapeConfig, BlockState defaultBlock, BlockState defaultFluid, NoiseRouter noiseRouter, MaterialRules.MaterialRule surfaceRule, List<MultiNoiseUtil.NoiseHypercube> spawnTarget, int seaLevel, boolean mobGenerationDisabled, boolean aquifers, boolean oreVeins, boolean usesLegacyRandom) {
    private final boolean oreVeins;
    public static final Codec<ChunkGeneratorSettings> CODEC = RecordCodecBuilder.create(instance -> instance.group((App)GenerationShapeConfig.CODEC.fieldOf("noise").forGetter(ChunkGeneratorSettings::generationShapeConfig), (App)BlockState.CODEC.fieldOf("default_block").forGetter(ChunkGeneratorSettings::defaultBlock), (App)BlockState.CODEC.fieldOf("default_fluid").forGetter(ChunkGeneratorSettings::defaultFluid), (App)NoiseRouter.CODEC.fieldOf("noise_router").forGetter(ChunkGeneratorSettings::noiseRouter), (App)MaterialRules.MaterialRule.CODEC.fieldOf("surface_rule").forGetter(ChunkGeneratorSettings::surfaceRule), (App)MultiNoiseUtil.NoiseHypercube.CODEC.listOf().fieldOf("spawn_target").forGetter(ChunkGeneratorSettings::spawnTarget), (App)Codec.INT.fieldOf("sea_level").forGetter(ChunkGeneratorSettings::seaLevel), (App)Codec.BOOL.fieldOf("disable_mob_generation").forGetter(ChunkGeneratorSettings::mobGenerationDisabled), (App)Codec.BOOL.fieldOf("aquifers_enabled").forGetter(ChunkGeneratorSettings::hasAquifers), (App)Codec.BOOL.fieldOf("ore_veins_enabled").forGetter(ChunkGeneratorSettings::oreVeins), (App)Codec.BOOL.fieldOf("legacy_random_source").forGetter(ChunkGeneratorSettings::usesLegacyRandom)).apply((Applicative)instance, ChunkGeneratorSettings::new));
    public static final Codec<RegistryEntry<ChunkGeneratorSettings>> REGISTRY_CODEC = RegistryElementCodec.of(RegistryKeys.CHUNK_GENERATOR_SETTINGS, CODEC);
    public static final RegistryKey<ChunkGeneratorSettings> OVERWORLD = RegistryKey.of(RegistryKeys.CHUNK_GENERATOR_SETTINGS, Identifier.ofVanilla("overworld"));
    public static final RegistryKey<ChunkGeneratorSettings> LARGE_BIOMES = RegistryKey.of(RegistryKeys.CHUNK_GENERATOR_SETTINGS, Identifier.ofVanilla("large_biomes"));
    public static final RegistryKey<ChunkGeneratorSettings> AMPLIFIED = RegistryKey.of(RegistryKeys.CHUNK_GENERATOR_SETTINGS, Identifier.ofVanilla("amplified"));
    public static final RegistryKey<ChunkGeneratorSettings> NETHER = RegistryKey.of(RegistryKeys.CHUNK_GENERATOR_SETTINGS, Identifier.ofVanilla("nether"));
    public static final RegistryKey<ChunkGeneratorSettings> END = RegistryKey.of(RegistryKeys.CHUNK_GENERATOR_SETTINGS, Identifier.ofVanilla("end"));
    public static final RegistryKey<ChunkGeneratorSettings> CAVES = RegistryKey.of(RegistryKeys.CHUNK_GENERATOR_SETTINGS, Identifier.ofVanilla("caves"));
    public static final RegistryKey<ChunkGeneratorSettings> FLOATING_ISLANDS = RegistryKey.of(RegistryKeys.CHUNK_GENERATOR_SETTINGS, Identifier.ofVanilla("floating_islands"));

    public boolean hasAquifers() {
        return this.aquifers && !SharedConstants.DISABLE_AQUIFERS;
    }

    public boolean oreVeins() {
        return this.oreVeins && !SharedConstants.DISABLE_ORE_VEINS;
    }

    public ChunkRandom.RandomProvider getRandomProvider() {
        return this.usesLegacyRandom ? ChunkRandom.RandomProvider.LEGACY : ChunkRandom.RandomProvider.XOROSHIRO;
    }

    public static void bootstrap(Registerable<ChunkGeneratorSettings> chunkGenerationSettingsRegisterable) {
        chunkGenerationSettingsRegisterable.register(OVERWORLD, ChunkGeneratorSettings.createSurfaceSettings(chunkGenerationSettingsRegisterable, false, false));
        chunkGenerationSettingsRegisterable.register(LARGE_BIOMES, ChunkGeneratorSettings.createSurfaceSettings(chunkGenerationSettingsRegisterable, false, true));
        chunkGenerationSettingsRegisterable.register(AMPLIFIED, ChunkGeneratorSettings.createSurfaceSettings(chunkGenerationSettingsRegisterable, true, false));
        chunkGenerationSettingsRegisterable.register(NETHER, ChunkGeneratorSettings.createNetherSettings(chunkGenerationSettingsRegisterable));
        chunkGenerationSettingsRegisterable.register(END, ChunkGeneratorSettings.createEndSettings(chunkGenerationSettingsRegisterable));
        chunkGenerationSettingsRegisterable.register(CAVES, ChunkGeneratorSettings.createCavesSettings(chunkGenerationSettingsRegisterable));
        chunkGenerationSettingsRegisterable.register(FLOATING_ISLANDS, ChunkGeneratorSettings.createFloatingIslandsSettings(chunkGenerationSettingsRegisterable));
    }

    private static ChunkGeneratorSettings createEndSettings(Registerable<?> registerable) {
        return new ChunkGeneratorSettings(GenerationShapeConfig.END, Blocks.END_STONE.getDefaultState(), Blocks.AIR.getDefaultState(), DensityFunctions.createEndNoiseRouter(registerable.getRegistryLookup(RegistryKeys.DENSITY_FUNCTION)), VanillaSurfaceRules.getEndStoneRule(), List.of(), 0, true, false, false, true);
    }

    private static ChunkGeneratorSettings createNetherSettings(Registerable<?> registerable) {
        return new ChunkGeneratorSettings(GenerationShapeConfig.NETHER, Blocks.NETHERRACK.getDefaultState(), Blocks.LAVA.getDefaultState(), DensityFunctions.createNetherNoiseRouter(registerable.getRegistryLookup(RegistryKeys.DENSITY_FUNCTION), registerable.getRegistryLookup(RegistryKeys.NOISE_PARAMETERS)), VanillaSurfaceRules.createNetherSurfaceRule(), List.of(), 32, false, false, false, true);
    }

    private static ChunkGeneratorSettings createSurfaceSettings(Registerable<?> registerable, boolean amplified, boolean largeBiomes) {
        return new ChunkGeneratorSettings(GenerationShapeConfig.SURFACE, Blocks.STONE.getDefaultState(), Blocks.WATER.getDefaultState(), DensityFunctions.createSurfaceNoiseRouter(registerable.getRegistryLookup(RegistryKeys.DENSITY_FUNCTION), registerable.getRegistryLookup(RegistryKeys.NOISE_PARAMETERS), largeBiomes, amplified), VanillaSurfaceRules.createOverworldSurfaceRule(), new VanillaBiomeParameters().getSpawnSuitabilityNoises(), 63, false, true, true, false);
    }

    private static ChunkGeneratorSettings createCavesSettings(Registerable<?> registerable) {
        return new ChunkGeneratorSettings(GenerationShapeConfig.CAVES, Blocks.STONE.getDefaultState(), Blocks.WATER.getDefaultState(), DensityFunctions.createCavesNoiseRouter(registerable.getRegistryLookup(RegistryKeys.DENSITY_FUNCTION), registerable.getRegistryLookup(RegistryKeys.NOISE_PARAMETERS)), VanillaSurfaceRules.createDefaultRule(false, true, true), List.of(), 32, false, false, false, true);
    }

    private static ChunkGeneratorSettings createFloatingIslandsSettings(Registerable<?> registerable) {
        return new ChunkGeneratorSettings(GenerationShapeConfig.FLOATING_ISLANDS, Blocks.STONE.getDefaultState(), Blocks.WATER.getDefaultState(), DensityFunctions.createFloatingIslandsNoiseRouter(registerable.getRegistryLookup(RegistryKeys.DENSITY_FUNCTION), registerable.getRegistryLookup(RegistryKeys.NOISE_PARAMETERS)), VanillaSurfaceRules.createDefaultRule(false, false, false), List.of(), -64, false, false, false, true);
    }

    public static ChunkGeneratorSettings createMissingSettings() {
        return new ChunkGeneratorSettings(GenerationShapeConfig.SURFACE, Blocks.STONE.getDefaultState(), Blocks.AIR.getDefaultState(), DensityFunctions.createMissingNoiseRouter(), VanillaSurfaceRules.getAirRule(), List.of(), 63, true, false, false, false);
    }

    @Override
    public final String toString() {
        return ObjectMethods.bootstrap("toString", new MethodHandle[]{ChunkGeneratorSettings.class, "noiseSettings;defaultBlock;defaultFluid;noiseRouter;surfaceRule;spawnTarget;seaLevel;disableMobGeneration;aquifersEnabled;oreVeinsEnabled;useLegacyRandomSource", "generationShapeConfig", "defaultBlock", "defaultFluid", "noiseRouter", "surfaceRule", "spawnTarget", "seaLevel", "mobGenerationDisabled", "aquifers", "oreVeins", "usesLegacyRandom"}, this);
    }

    @Override
    public final int hashCode() {
        return (int)ObjectMethods.bootstrap("hashCode", new MethodHandle[]{ChunkGeneratorSettings.class, "noiseSettings;defaultBlock;defaultFluid;noiseRouter;surfaceRule;spawnTarget;seaLevel;disableMobGeneration;aquifersEnabled;oreVeinsEnabled;useLegacyRandomSource", "generationShapeConfig", "defaultBlock", "defaultFluid", "noiseRouter", "surfaceRule", "spawnTarget", "seaLevel", "mobGenerationDisabled", "aquifers", "oreVeins", "usesLegacyRandom"}, this);
    }

    @Override
    public final boolean equals(Object object) {
        return (boolean)ObjectMethods.bootstrap("equals", new MethodHandle[]{ChunkGeneratorSettings.class, "noiseSettings;defaultBlock;defaultFluid;noiseRouter;surfaceRule;spawnTarget;seaLevel;disableMobGeneration;aquifersEnabled;oreVeinsEnabled;useLegacyRandomSource", "generationShapeConfig", "defaultBlock", "defaultFluid", "noiseRouter", "surfaceRule", "spawnTarget", "seaLevel", "mobGenerationDisabled", "aquifers", "oreVeins", "usesLegacyRandom"}, this, object);
    }
}


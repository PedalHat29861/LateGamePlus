/*
 * Decompiled with CFR 0.152.
 */
package net.minecraft.world.dimension;

import net.minecraft.particle.ParticleTypes;
import net.minecraft.registry.Registerable;
import net.minecraft.registry.RegistryEntryLookup;
import net.minecraft.registry.RegistryKeys;
import net.minecraft.registry.tag.BlockTags;
import net.minecraft.registry.tag.TimelineTags;
import net.minecraft.sound.MusicType;
import net.minecraft.util.math.ColorHelper;
import net.minecraft.util.math.intprovider.ConstantIntProvider;
import net.minecraft.util.math.intprovider.UniformIntProvider;
import net.minecraft.world.attribute.AmbientSounds;
import net.minecraft.world.attribute.BackgroundMusic;
import net.minecraft.world.attribute.BedRule;
import net.minecraft.world.attribute.EnvironmentAttributeMap;
import net.minecraft.world.attribute.EnvironmentAttributes;
import net.minecraft.world.attribute.timeline.Timeline;
import net.minecraft.world.attribute.timeline.Timelines;
import net.minecraft.world.biome.OverworldBiomeCreator;
import net.minecraft.world.dimension.DimensionType;
import net.minecraft.world.dimension.DimensionTypes;

public class DimensionTypeRegistrar {
    public static void bootstrap(Registerable<DimensionType> dimensionTypeRegisterable) {
        RegistryEntryLookup<Timeline> registryEntryLookup = dimensionTypeRegisterable.getRegistryLookup(RegistryKeys.TIMELINE);
        EnvironmentAttributeMap environmentAttributeMap = EnvironmentAttributeMap.builder().with(EnvironmentAttributes.FOG_COLOR_VISUAL, -4138753).with(EnvironmentAttributes.SKY_COLOR_VISUAL, OverworldBiomeCreator.getSkyColor(0.8f)).with(EnvironmentAttributes.CLOUD_COLOR_VISUAL, ColorHelper.getWhite(0.8f)).with(EnvironmentAttributes.CLOUD_HEIGHT_VISUAL, Float.valueOf(192.33f)).with(EnvironmentAttributes.BACKGROUND_MUSIC_AUDIO, BackgroundMusic.DEFAULT).with(EnvironmentAttributes.BED_RULE_GAMEPLAY, BedRule.OVERWORLD).with(EnvironmentAttributes.RESPAWN_ANCHOR_WORKS_GAMEPLAY, false).with(EnvironmentAttributes.NETHER_PORTAL_SPAWNS_PIGLIN_GAMEPLAY, true).with(EnvironmentAttributes.AMBIENT_SOUNDS_AUDIO, AmbientSounds.CAVE).build();
        dimensionTypeRegisterable.register(DimensionTypes.OVERWORLD, new DimensionType(false, true, false, 1.0, -64, 384, 384, BlockTags.INFINIBURN_OVERWORLD, 0.0f, new DimensionType.MonsterSettings(UniformIntProvider.create(0, 7), 0), DimensionType.Skybox.OVERWORLD, DimensionType.CardinalLightType.DEFAULT, environmentAttributeMap, registryEntryLookup.getOrThrow(TimelineTags.IN_OVERWORLD)));
        dimensionTypeRegisterable.register(DimensionTypes.THE_NETHER, new DimensionType(true, false, true, 8.0, 0, 256, 128, BlockTags.INFINIBURN_NETHER, 0.1f, new DimensionType.MonsterSettings(ConstantIntProvider.create(7), 15), DimensionType.Skybox.NONE, DimensionType.CardinalLightType.NETHER, EnvironmentAttributeMap.builder().with(EnvironmentAttributes.FOG_START_DISTANCE_VISUAL, Float.valueOf(10.0f)).with(EnvironmentAttributes.FOG_END_DISTANCE_VISUAL, Float.valueOf(96.0f)).with(EnvironmentAttributes.SKY_LIGHT_COLOR_VISUAL, Timelines.NIGHT_SKY_LIGHT_COLOR).with(EnvironmentAttributes.SKY_LIGHT_LEVEL_GAMEPLAY, Float.valueOf(4.0f)).with(EnvironmentAttributes.SKY_LIGHT_FACTOR_VISUAL, Float.valueOf(0.0f)).with(EnvironmentAttributes.DEFAULT_DRIPSTONE_PARTICLE_VISUAL, ParticleTypes.DRIPPING_DRIPSTONE_LAVA).with(EnvironmentAttributes.BED_RULE_GAMEPLAY, BedRule.OTHER_DIMENSION).with(EnvironmentAttributes.RESPAWN_ANCHOR_WORKS_GAMEPLAY, true).with(EnvironmentAttributes.WATER_EVAPORATES_GAMEPLAY, true).with(EnvironmentAttributes.FAST_LAVA_GAMEPLAY, true).with(EnvironmentAttributes.PIGLINS_ZOMBIFY_GAMEPLAY, false).with(EnvironmentAttributes.CAN_START_RAID_GAMEPLAY, false).with(EnvironmentAttributes.SNOW_GOLEM_MELTS_GAMEPLAY, true).build(), registryEntryLookup.getOrThrow(TimelineTags.IN_NETHER)));
        dimensionTypeRegisterable.register(DimensionTypes.THE_END, new DimensionType(true, true, false, 1.0, 0, 256, 256, BlockTags.INFINIBURN_END, 0.25f, new DimensionType.MonsterSettings(ConstantIntProvider.create(15), 0), DimensionType.Skybox.END, DimensionType.CardinalLightType.DEFAULT, EnvironmentAttributeMap.builder().with(EnvironmentAttributes.FOG_COLOR_VISUAL, -15199464).with(EnvironmentAttributes.SKY_LIGHT_COLOR_VISUAL, -1736449).with(EnvironmentAttributes.SKY_COLOR_VISUAL, -16777216).with(EnvironmentAttributes.SKY_LIGHT_FACTOR_VISUAL, Float.valueOf(0.0f)).with(EnvironmentAttributes.BACKGROUND_MUSIC_AUDIO, new BackgroundMusic(MusicType.END)).with(EnvironmentAttributes.AMBIENT_SOUNDS_AUDIO, AmbientSounds.CAVE).with(EnvironmentAttributes.BED_RULE_GAMEPLAY, BedRule.OTHER_DIMENSION).with(EnvironmentAttributes.RESPAWN_ANCHOR_WORKS_GAMEPLAY, false).build(), registryEntryLookup.getOrThrow(TimelineTags.IN_END)));
        dimensionTypeRegisterable.register(DimensionTypes.OVERWORLD_CAVES, new DimensionType(false, true, true, 1.0, -64, 384, 384, BlockTags.INFINIBURN_OVERWORLD, 0.0f, new DimensionType.MonsterSettings(UniformIntProvider.create(0, 7), 0), DimensionType.Skybox.OVERWORLD, DimensionType.CardinalLightType.DEFAULT, environmentAttributeMap, registryEntryLookup.getOrThrow(TimelineTags.IN_OVERWORLD)));
    }
}


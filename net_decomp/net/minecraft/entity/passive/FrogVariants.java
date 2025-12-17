/*
 * Decompiled with CFR 0.152.
 */
package net.minecraft.entity.passive;

import net.minecraft.entity.passive.AnimalTemperature;
import net.minecraft.entity.passive.FrogVariant;
import net.minecraft.entity.spawn.BiomeSpawnCondition;
import net.minecraft.entity.spawn.SpawnConditionSelectors;
import net.minecraft.registry.Registerable;
import net.minecraft.registry.RegistryKey;
import net.minecraft.registry.RegistryKeys;
import net.minecraft.registry.entry.RegistryEntryList;
import net.minecraft.registry.tag.BiomeTags;
import net.minecraft.registry.tag.TagKey;
import net.minecraft.util.AssetInfo;
import net.minecraft.util.Identifier;
import net.minecraft.world.biome.Biome;

public interface FrogVariants {
    public static final RegistryKey<FrogVariant> TEMPERATE = FrogVariants.of(AnimalTemperature.TEMPERATE);
    public static final RegistryKey<FrogVariant> WARM = FrogVariants.of(AnimalTemperature.WARM);
    public static final RegistryKey<FrogVariant> COLD = FrogVariants.of(AnimalTemperature.COLD);

    private static RegistryKey<FrogVariant> of(Identifier id) {
        return RegistryKey.of(RegistryKeys.FROG_VARIANT, id);
    }

    public static void bootstrap(Registerable<FrogVariant> registry) {
        FrogVariants.register(registry, TEMPERATE, "entity/frog/temperate_frog", SpawnConditionSelectors.createFallback(0));
        FrogVariants.register(registry, WARM, "entity/frog/warm_frog", BiomeTags.SPAWNS_WARM_VARIANT_FROGS);
        FrogVariants.register(registry, COLD, "entity/frog/cold_frog", BiomeTags.SPAWNS_COLD_VARIANT_FROGS);
    }

    private static void register(Registerable<FrogVariant> registry, RegistryKey<FrogVariant> key, String assetId, TagKey<Biome> requiredBiomes) {
        RegistryEntryList.Named<Biome> registryEntryList = registry.getRegistryLookup(RegistryKeys.BIOME).getOrThrow(requiredBiomes);
        FrogVariants.register(registry, key, assetId, SpawnConditionSelectors.createSingle(new BiomeSpawnCondition(registryEntryList), 1));
    }

    private static void register(Registerable<FrogVariant> registry, RegistryKey<FrogVariant> key, String assetId, SpawnConditionSelectors spawnConditions) {
        registry.register(key, new FrogVariant(new AssetInfo.TextureAssetInfo(Identifier.ofVanilla(assetId)), spawnConditions));
    }
}


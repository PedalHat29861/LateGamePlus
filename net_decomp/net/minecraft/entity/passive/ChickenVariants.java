/*
 * Decompiled with CFR 0.152.
 */
package net.minecraft.entity.passive;

import net.minecraft.entity.passive.AnimalTemperature;
import net.minecraft.entity.passive.ChickenVariant;
import net.minecraft.entity.spawn.BiomeSpawnCondition;
import net.minecraft.entity.spawn.SpawnConditionSelectors;
import net.minecraft.registry.Registerable;
import net.minecraft.registry.RegistryKey;
import net.minecraft.registry.RegistryKeys;
import net.minecraft.registry.entry.RegistryEntryList;
import net.minecraft.registry.tag.BiomeTags;
import net.minecraft.registry.tag.TagKey;
import net.minecraft.util.Identifier;
import net.minecraft.util.ModelAndTexture;
import net.minecraft.world.biome.Biome;

public class ChickenVariants {
    public static final RegistryKey<ChickenVariant> TEMPERATE = ChickenVariants.of(AnimalTemperature.TEMPERATE);
    public static final RegistryKey<ChickenVariant> WARM = ChickenVariants.of(AnimalTemperature.WARM);
    public static final RegistryKey<ChickenVariant> COLD = ChickenVariants.of(AnimalTemperature.COLD);
    public static final RegistryKey<ChickenVariant> DEFAULT = TEMPERATE;

    private static RegistryKey<ChickenVariant> of(Identifier id) {
        return RegistryKey.of(RegistryKeys.CHICKEN_VARIANT, id);
    }

    public static void bootstrap(Registerable<ChickenVariant> registry) {
        ChickenVariants.register(registry, TEMPERATE, ChickenVariant.Model.NORMAL, "temperate_chicken", SpawnConditionSelectors.createFallback(0));
        ChickenVariants.register(registry, WARM, ChickenVariant.Model.NORMAL, "warm_chicken", BiomeTags.SPAWNS_WARM_VARIANT_FARM_ANIMALS);
        ChickenVariants.register(registry, COLD, ChickenVariant.Model.COLD, "cold_chicken", BiomeTags.SPAWNS_COLD_VARIANT_FARM_ANIMALS);
    }

    private static void register(Registerable<ChickenVariant> registry, RegistryKey<ChickenVariant> key, ChickenVariant.Model model, String textureName, TagKey<Biome> biomes) {
        RegistryEntryList.Named<Biome> registryEntryList = registry.getRegistryLookup(RegistryKeys.BIOME).getOrThrow(biomes);
        ChickenVariants.register(registry, key, model, textureName, SpawnConditionSelectors.createSingle(new BiomeSpawnCondition(registryEntryList), 1));
    }

    private static void register(Registerable<ChickenVariant> registry, RegistryKey<ChickenVariant> key, ChickenVariant.Model model, String textureName, SpawnConditionSelectors spawnConditions) {
        Identifier identifier = Identifier.ofVanilla("entity/chicken/" + textureName);
        registry.register(key, new ChickenVariant(new ModelAndTexture<ChickenVariant.Model>(model, identifier), spawnConditions));
    }
}


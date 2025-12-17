/*
 * Decompiled with CFR 0.152.
 */
package net.minecraft.entity.passive;

import net.minecraft.entity.passive.AnimalTemperature;
import net.minecraft.entity.passive.PigVariant;
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

public class PigVariants {
    public static final RegistryKey<PigVariant> TEMPERATE = PigVariants.of(AnimalTemperature.TEMPERATE);
    public static final RegistryKey<PigVariant> WARM = PigVariants.of(AnimalTemperature.WARM);
    public static final RegistryKey<PigVariant> COLD = PigVariants.of(AnimalTemperature.COLD);
    public static final RegistryKey<PigVariant> DEFAULT = TEMPERATE;

    private static RegistryKey<PigVariant> of(Identifier id) {
        return RegistryKey.of(RegistryKeys.PIG_VARIANT, id);
    }

    public static void bootstrap(Registerable<PigVariant> registry) {
        PigVariants.register(registry, TEMPERATE, PigVariant.Model.NORMAL, "temperate_pig", SpawnConditionSelectors.createFallback(0));
        PigVariants.register(registry, WARM, PigVariant.Model.NORMAL, "warm_pig", BiomeTags.SPAWNS_WARM_VARIANT_FARM_ANIMALS);
        PigVariants.register(registry, COLD, PigVariant.Model.COLD, "cold_pig", BiomeTags.SPAWNS_COLD_VARIANT_FARM_ANIMALS);
    }

    private static void register(Registerable<PigVariant> registry, RegistryKey<PigVariant> key, PigVariant.Model model, String textureName, TagKey<Biome> biomes) {
        RegistryEntryList.Named<Biome> registryEntryList = registry.getRegistryLookup(RegistryKeys.BIOME).getOrThrow(biomes);
        PigVariants.register(registry, key, model, textureName, SpawnConditionSelectors.createSingle(new BiomeSpawnCondition(registryEntryList), 1));
    }

    private static void register(Registerable<PigVariant> registry, RegistryKey<PigVariant> key, PigVariant.Model model, String textureName, SpawnConditionSelectors spawnConditions) {
        Identifier identifier = Identifier.ofVanilla("entity/pig/" + textureName);
        registry.register(key, new PigVariant(new ModelAndTexture<PigVariant.Model>(model, identifier), spawnConditions));
    }
}


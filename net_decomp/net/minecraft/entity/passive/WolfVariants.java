/*
 * Decompiled with CFR 0.152.
 */
package net.minecraft.entity.passive;

import net.minecraft.entity.passive.WolfVariant;
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
import net.minecraft.world.biome.BiomeKeys;

public class WolfVariants {
    public static final RegistryKey<WolfVariant> PALE = WolfVariants.of("pale");
    public static final RegistryKey<WolfVariant> SPOTTED = WolfVariants.of("spotted");
    public static final RegistryKey<WolfVariant> SNOWY = WolfVariants.of("snowy");
    public static final RegistryKey<WolfVariant> BLACK = WolfVariants.of("black");
    public static final RegistryKey<WolfVariant> ASHEN = WolfVariants.of("ashen");
    public static final RegistryKey<WolfVariant> RUSTY = WolfVariants.of("rusty");
    public static final RegistryKey<WolfVariant> WOODS = WolfVariants.of("woods");
    public static final RegistryKey<WolfVariant> CHESTNUT = WolfVariants.of("chestnut");
    public static final RegistryKey<WolfVariant> STRIPED = WolfVariants.of("striped");
    public static final RegistryKey<WolfVariant> DEFAULT = PALE;

    private static RegistryKey<WolfVariant> of(String id) {
        return RegistryKey.of(RegistryKeys.WOLF_VARIANT, Identifier.ofVanilla(id));
    }

    private static void register(Registerable<WolfVariant> registry, RegistryKey<WolfVariant> key, String textureName, RegistryKey<Biome> biome) {
        WolfVariants.register(registry, key, textureName, WolfVariants.createSpawnConditions(RegistryEntryList.of(registry.getRegistryLookup(RegistryKeys.BIOME).getOrThrow(biome))));
    }

    private static void register(Registerable<WolfVariant> registry, RegistryKey<WolfVariant> key, String textureName, TagKey<Biome> biomeTag) {
        WolfVariants.register(registry, key, textureName, WolfVariants.createSpawnConditions(registry.getRegistryLookup(RegistryKeys.BIOME).getOrThrow(biomeTag)));
    }

    private static SpawnConditionSelectors createSpawnConditions(RegistryEntryList<Biome> requiredBiomes) {
        return SpawnConditionSelectors.createSingle(new BiomeSpawnCondition(requiredBiomes), 1);
    }

    private static void register(Registerable<WolfVariant> registry, RegistryKey<WolfVariant> key, String textureName, SpawnConditionSelectors spawnConditions) {
        Identifier identifier = Identifier.ofVanilla("entity/wolf/" + textureName);
        Identifier identifier2 = Identifier.ofVanilla("entity/wolf/" + textureName + "_tame");
        Identifier identifier3 = Identifier.ofVanilla("entity/wolf/" + textureName + "_angry");
        registry.register(key, new WolfVariant(new WolfVariant.WolfAssetInfo(new AssetInfo.TextureAssetInfo(identifier), new AssetInfo.TextureAssetInfo(identifier2), new AssetInfo.TextureAssetInfo(identifier3)), spawnConditions));
    }

    public static void bootstrap(Registerable<WolfVariant> registry) {
        WolfVariants.register(registry, PALE, "wolf", SpawnConditionSelectors.createFallback(0));
        WolfVariants.register(registry, SPOTTED, "wolf_spotted", BiomeTags.IS_SAVANNA);
        WolfVariants.register(registry, SNOWY, "wolf_snowy", BiomeKeys.GROVE);
        WolfVariants.register(registry, BLACK, "wolf_black", BiomeKeys.OLD_GROWTH_PINE_TAIGA);
        WolfVariants.register(registry, ASHEN, "wolf_ashen", BiomeKeys.SNOWY_TAIGA);
        WolfVariants.register(registry, RUSTY, "wolf_rusty", BiomeTags.IS_JUNGLE);
        WolfVariants.register(registry, WOODS, "wolf_woods", BiomeKeys.FOREST);
        WolfVariants.register(registry, CHESTNUT, "wolf_chestnut", BiomeKeys.OLD_GROWTH_SPRUCE_TAIGA);
        WolfVariants.register(registry, STRIPED, "wolf_striped", BiomeTags.IS_BADLANDS);
    }
}


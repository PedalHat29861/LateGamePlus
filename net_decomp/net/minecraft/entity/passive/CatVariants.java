/*
 * Decompiled with CFR 0.152.
 */
package net.minecraft.entity.passive;

import java.util.List;
import net.minecraft.entity.VariantSelectorProvider;
import net.minecraft.entity.passive.CatVariant;
import net.minecraft.entity.spawn.MoonBrightnessSpawnCondition;
import net.minecraft.entity.spawn.SpawnConditionSelectors;
import net.minecraft.entity.spawn.StructureSpawnCondition;
import net.minecraft.predicate.NumberRange;
import net.minecraft.registry.Registerable;
import net.minecraft.registry.RegistryEntryLookup;
import net.minecraft.registry.RegistryKey;
import net.minecraft.registry.RegistryKeys;
import net.minecraft.registry.tag.StructureTags;
import net.minecraft.util.AssetInfo;
import net.minecraft.util.Identifier;
import net.minecraft.world.gen.structure.Structure;

public interface CatVariants {
    public static final RegistryKey<CatVariant> TABBY = CatVariants.of("tabby");
    public static final RegistryKey<CatVariant> BLACK = CatVariants.of("black");
    public static final RegistryKey<CatVariant> RED = CatVariants.of("red");
    public static final RegistryKey<CatVariant> SIAMESE = CatVariants.of("siamese");
    public static final RegistryKey<CatVariant> BRITISH_SHORTHAIR = CatVariants.of("british_shorthair");
    public static final RegistryKey<CatVariant> CALICO = CatVariants.of("calico");
    public static final RegistryKey<CatVariant> PERSIAN = CatVariants.of("persian");
    public static final RegistryKey<CatVariant> RAGDOLL = CatVariants.of("ragdoll");
    public static final RegistryKey<CatVariant> WHITE = CatVariants.of("white");
    public static final RegistryKey<CatVariant> JELLIE = CatVariants.of("jellie");
    public static final RegistryKey<CatVariant> ALL_BLACK = CatVariants.of("all_black");

    private static RegistryKey<CatVariant> of(String id) {
        return RegistryKey.of(RegistryKeys.CAT_VARIANT, Identifier.ofVanilla(id));
    }

    public static void bootstrap(Registerable<CatVariant> registry) {
        RegistryEntryLookup<Structure> registryEntryLookup = registry.getRegistryLookup(RegistryKeys.STRUCTURE);
        CatVariants.register(registry, TABBY, "entity/cat/tabby");
        CatVariants.register(registry, BLACK, "entity/cat/black");
        CatVariants.register(registry, RED, "entity/cat/red");
        CatVariants.register(registry, SIAMESE, "entity/cat/siamese");
        CatVariants.register(registry, BRITISH_SHORTHAIR, "entity/cat/british_shorthair");
        CatVariants.register(registry, CALICO, "entity/cat/calico");
        CatVariants.register(registry, PERSIAN, "entity/cat/persian");
        CatVariants.register(registry, RAGDOLL, "entity/cat/ragdoll");
        CatVariants.register(registry, WHITE, "entity/cat/white");
        CatVariants.register(registry, JELLIE, "entity/cat/jellie");
        CatVariants.register(registry, ALL_BLACK, "entity/cat/all_black", new SpawnConditionSelectors(List.of(new VariantSelectorProvider.Selector(new StructureSpawnCondition(registryEntryLookup.getOrThrow(StructureTags.CATS_SPAWN_AS_BLACK)), 1), new VariantSelectorProvider.Selector(new MoonBrightnessSpawnCondition(NumberRange.DoubleRange.atLeast(0.9)), 0))));
    }

    private static void register(Registerable<CatVariant> registry, RegistryKey<CatVariant> key, String assetId) {
        CatVariants.register(registry, key, assetId, SpawnConditionSelectors.createFallback(0));
    }

    private static void register(Registerable<CatVariant> registry, RegistryKey<CatVariant> key, String assetId, SpawnConditionSelectors spawnConditions) {
        registry.register(key, new CatVariant(new AssetInfo.TextureAssetInfo(Identifier.ofVanilla(assetId)), spawnConditions));
    }
}


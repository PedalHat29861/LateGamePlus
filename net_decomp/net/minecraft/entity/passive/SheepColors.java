/*
 * Decompiled with CFR 0.152.
 */
package net.minecraft.entity.passive;

import net.minecraft.registry.entry.RegistryEntry;
import net.minecraft.registry.tag.BiomeTags;
import net.minecraft.util.DyeColor;
import net.minecraft.util.collection.Pool;
import net.minecraft.util.math.random.Random;
import net.minecraft.world.biome.Biome;

public class SheepColors {
    private static final SpawnConfig TEMPERATE = new SpawnConfig(SheepColors.createCombinedSelector(SheepColors.poolBuilder().add(SheepColors.createSingleSelector(DyeColor.BLACK), 5).add(SheepColors.createSingleSelector(DyeColor.GRAY), 5).add(SheepColors.createSingleSelector(DyeColor.LIGHT_GRAY), 5).add(SheepColors.createSingleSelector(DyeColor.BROWN), 3).add(SheepColors.createDefaultSelector(DyeColor.WHITE), 82).build()));
    private static final SpawnConfig WARM = new SpawnConfig(SheepColors.createCombinedSelector(SheepColors.poolBuilder().add(SheepColors.createSingleSelector(DyeColor.GRAY), 5).add(SheepColors.createSingleSelector(DyeColor.LIGHT_GRAY), 5).add(SheepColors.createSingleSelector(DyeColor.WHITE), 5).add(SheepColors.createSingleSelector(DyeColor.BLACK), 3).add(SheepColors.createDefaultSelector(DyeColor.BROWN), 82).build()));
    private static final SpawnConfig COLD = new SpawnConfig(SheepColors.createCombinedSelector(SheepColors.poolBuilder().add(SheepColors.createSingleSelector(DyeColor.LIGHT_GRAY), 5).add(SheepColors.createSingleSelector(DyeColor.GRAY), 5).add(SheepColors.createSingleSelector(DyeColor.WHITE), 5).add(SheepColors.createSingleSelector(DyeColor.BROWN), 3).add(SheepColors.createDefaultSelector(DyeColor.BLACK), 82).build()));

    private static ColorSelector createDefaultSelector(DyeColor color) {
        return SheepColors.createCombinedSelector(SheepColors.poolBuilder().add(SheepColors.createSingleSelector(color), 499).add(SheepColors.createSingleSelector(DyeColor.PINK), 1).build());
    }

    public static DyeColor select(RegistryEntry<Biome> biome, Random random) {
        SpawnConfig spawnConfig = SheepColors.getSpawnConfig(biome);
        return spawnConfig.colors().get(random);
    }

    private static SpawnConfig getSpawnConfig(RegistryEntry<Biome> biome) {
        if (biome.isIn(BiomeTags.SPAWNS_WARM_VARIANT_FARM_ANIMALS)) {
            return WARM;
        }
        if (biome.isIn(BiomeTags.SPAWNS_COLD_VARIANT_FARM_ANIMALS)) {
            return COLD;
        }
        return TEMPERATE;
    }

    private static ColorSelector createCombinedSelector(Pool<ColorSelector> pool) {
        if (pool.isEmpty()) {
            throw new IllegalArgumentException("List must be non-empty");
        }
        return random -> ((ColorSelector)pool.get(random)).get(random);
    }

    private static ColorSelector createSingleSelector(DyeColor color) {
        return random -> color;
    }

    private static Pool.Builder<ColorSelector> poolBuilder() {
        return Pool.builder();
    }

    @FunctionalInterface
    static interface ColorSelector {
        public DyeColor get(Random var1);
    }

    record SpawnConfig(ColorSelector colors) {
    }
}


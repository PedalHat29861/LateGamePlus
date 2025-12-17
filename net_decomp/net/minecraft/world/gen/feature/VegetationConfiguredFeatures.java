/*
 * Decompiled with CFR 0.152.
 */
package net.minecraft.world.gen.feature;

import java.util.List;
import net.minecraft.block.Block;
import net.minecraft.block.BlockState;
import net.minecraft.block.Blocks;
import net.minecraft.block.FlowerbedBlock;
import net.minecraft.block.LeafLitterBlock;
import net.minecraft.block.SweetBerryBushBlock;
import net.minecraft.fluid.Fluids;
import net.minecraft.registry.Registerable;
import net.minecraft.registry.RegistryEntryLookup;
import net.minecraft.registry.RegistryKey;
import net.minecraft.registry.RegistryKeys;
import net.minecraft.registry.entry.RegistryEntry;
import net.minecraft.registry.entry.RegistryEntryList;
import net.minecraft.registry.tag.BlockTags;
import net.minecraft.state.property.EnumProperty;
import net.minecraft.state.property.IntProperty;
import net.minecraft.util.collection.Pool;
import net.minecraft.util.dynamic.Range;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Direction;
import net.minecraft.util.math.Vec3i;
import net.minecraft.util.math.VerticalSurfaceType;
import net.minecraft.util.math.intprovider.BiasedToBottomIntProvider;
import net.minecraft.util.math.intprovider.ConstantIntProvider;
import net.minecraft.util.math.intprovider.UniformIntProvider;
import net.minecraft.util.math.intprovider.WeightedListIntProvider;
import net.minecraft.util.math.noise.DoublePerlinNoiseSampler;
import net.minecraft.world.gen.ProbabilityConfig;
import net.minecraft.world.gen.blockpredicate.BlockPredicate;
import net.minecraft.world.gen.feature.BlockColumnFeatureConfig;
import net.minecraft.world.gen.feature.ConfiguredFeature;
import net.minecraft.world.gen.feature.ConfiguredFeatures;
import net.minecraft.world.gen.feature.Feature;
import net.minecraft.world.gen.feature.PlacedFeature;
import net.minecraft.world.gen.feature.PlacedFeatures;
import net.minecraft.world.gen.feature.RandomBooleanFeatureConfig;
import net.minecraft.world.gen.feature.RandomFeatureConfig;
import net.minecraft.world.gen.feature.RandomFeatureEntry;
import net.minecraft.world.gen.feature.RandomPatchFeatureConfig;
import net.minecraft.world.gen.feature.SimpleBlockFeatureConfig;
import net.minecraft.world.gen.feature.SimpleRandomFeatureConfig;
import net.minecraft.world.gen.feature.TreeConfiguredFeatures;
import net.minecraft.world.gen.feature.TreePlacedFeatures;
import net.minecraft.world.gen.feature.VegetationPatchFeatureConfig;
import net.minecraft.world.gen.placementmodifier.BlockFilterPlacementModifier;
import net.minecraft.world.gen.placementmodifier.PlacementModifier;
import net.minecraft.world.gen.stateprovider.BlockStateProvider;
import net.minecraft.world.gen.stateprovider.DualNoiseBlockStateProvider;
import net.minecraft.world.gen.stateprovider.NoiseBlockStateProvider;
import net.minecraft.world.gen.stateprovider.NoiseThresholdBlockStateProvider;
import net.minecraft.world.gen.stateprovider.WeightedBlockStateProvider;

public class VegetationConfiguredFeatures {
    public static final RegistryKey<ConfiguredFeature<?, ?>> BAMBOO_NO_PODZOL = ConfiguredFeatures.of("bamboo_no_podzol");
    public static final RegistryKey<ConfiguredFeature<?, ?>> BAMBOO_SOME_PODZOL = ConfiguredFeatures.of("bamboo_some_podzol");
    public static final RegistryKey<ConfiguredFeature<?, ?>> VINES = ConfiguredFeatures.of("vines");
    public static final RegistryKey<ConfiguredFeature<?, ?>> PATCH_BROWN_MUSHROOM = ConfiguredFeatures.of("patch_brown_mushroom");
    public static final RegistryKey<ConfiguredFeature<?, ?>> PATCH_RED_MUSHROOM = ConfiguredFeatures.of("patch_red_mushroom");
    public static final RegistryKey<ConfiguredFeature<?, ?>> PATCH_SUNFLOWER = ConfiguredFeatures.of("patch_sunflower");
    public static final RegistryKey<ConfiguredFeature<?, ?>> PATCH_PUMPKIN = ConfiguredFeatures.of("patch_pumpkin");
    public static final RegistryKey<ConfiguredFeature<?, ?>> PATCH_BERRY_BUSH = ConfiguredFeatures.of("patch_berry_bush");
    public static final RegistryKey<ConfiguredFeature<?, ?>> PATCH_TAIGA_GRASS = ConfiguredFeatures.of("patch_taiga_grass");
    public static final RegistryKey<ConfiguredFeature<?, ?>> PATCH_GRASS = ConfiguredFeatures.of("patch_grass");
    public static final RegistryKey<ConfiguredFeature<?, ?>> PATCH_GRASS_MEADOW = ConfiguredFeatures.of("patch_grass_meadow");
    public static final RegistryKey<ConfiguredFeature<?, ?>> PATCH_GRASS_JUNGLE = ConfiguredFeatures.of("patch_grass_jungle");
    public static final RegistryKey<ConfiguredFeature<?, ?>> SINGLE_PIECE_OF_GRASS = ConfiguredFeatures.of("single_piece_of_grass");
    public static final RegistryKey<ConfiguredFeature<?, ?>> PATCH_DEAD_BUSH = ConfiguredFeatures.of("patch_dead_bush");
    public static final RegistryKey<ConfiguredFeature<?, ?>> PATCH_DRY_GRASS = ConfiguredFeatures.of("patch_dry_grass");
    public static final RegistryKey<ConfiguredFeature<?, ?>> PATCH_MELON = ConfiguredFeatures.of("patch_melon");
    public static final RegistryKey<ConfiguredFeature<?, ?>> PATCH_WATERLILY = ConfiguredFeatures.of("patch_waterlily");
    public static final RegistryKey<ConfiguredFeature<?, ?>> PATCH_TALL_GRASS = ConfiguredFeatures.of("patch_tall_grass");
    public static final RegistryKey<ConfiguredFeature<?, ?>> PATCH_LARGE_FERN = ConfiguredFeatures.of("patch_large_fern");
    public static final RegistryKey<ConfiguredFeature<?, ?>> PATCH_BUSH = ConfiguredFeatures.of("patch_bush");
    public static final RegistryKey<ConfiguredFeature<?, ?>> PATCH_LEAF_LITTER = ConfiguredFeatures.of("patch_leaf_litter");
    public static final RegistryKey<ConfiguredFeature<?, ?>> PATCH_FIREFLY_BUSH = ConfiguredFeatures.of("patch_firefly_bush");
    public static final RegistryKey<ConfiguredFeature<?, ?>> PATCH_CACTUS = ConfiguredFeatures.of("patch_cactus");
    public static final RegistryKey<ConfiguredFeature<?, ?>> PATCH_SUGAR_CANE = ConfiguredFeatures.of("patch_sugar_cane");
    public static final RegistryKey<ConfiguredFeature<?, ?>> FLOWER_DEFAULT = ConfiguredFeatures.of("flower_default");
    public static final RegistryKey<ConfiguredFeature<?, ?>> FLOWER_FLOWER_FOREST = ConfiguredFeatures.of("flower_flower_forest");
    public static final RegistryKey<ConfiguredFeature<?, ?>> FLOWER_SWAMP = ConfiguredFeatures.of("flower_swamp");
    public static final RegistryKey<ConfiguredFeature<?, ?>> FLOWER_PLAIN = ConfiguredFeatures.of("flower_plain");
    public static final RegistryKey<ConfiguredFeature<?, ?>> FLOWER_MEADOW = ConfiguredFeatures.of("flower_meadow");
    public static final RegistryKey<ConfiguredFeature<?, ?>> FLOWER_CHERRY = ConfiguredFeatures.of("flower_cherry");
    public static final RegistryKey<ConfiguredFeature<?, ?>> FLOWER_PALE_GARDEN = ConfiguredFeatures.of("flower_pale_garden");
    public static final RegistryKey<ConfiguredFeature<?, ?>> WILDFLOWERS_BIRCH_FOREST = ConfiguredFeatures.of("wildflowers_birch_forest");
    public static final RegistryKey<ConfiguredFeature<?, ?>> WILDFLOWERS_MEADOW = ConfiguredFeatures.of("wildflowers_meadow");
    public static final RegistryKey<ConfiguredFeature<?, ?>> FOREST_FLOWERS = ConfiguredFeatures.of("forest_flowers");
    public static final RegistryKey<ConfiguredFeature<?, ?>> PALE_FOREST_FLOWERS = ConfiguredFeatures.of("pale_forest_flowers");
    public static final RegistryKey<ConfiguredFeature<?, ?>> DARK_FOREST_VEGETATION = ConfiguredFeatures.of("dark_forest_vegetation");
    public static final RegistryKey<ConfiguredFeature<?, ?>> PALE_GARDEN_VEGETATION = ConfiguredFeatures.of("pale_garden_vegetation");
    public static final RegistryKey<ConfiguredFeature<?, ?>> PALE_MOSS_VEGETATION = ConfiguredFeatures.of("pale_moss_vegetation");
    public static final RegistryKey<ConfiguredFeature<?, ?>> PALE_MOSS_PATCH = ConfiguredFeatures.of("pale_moss_patch");
    public static final RegistryKey<ConfiguredFeature<?, ?>> PALE_MOSS_PATCH_BONEMEAL = ConfiguredFeatures.of("pale_moss_patch_bonemeal");
    public static final RegistryKey<ConfiguredFeature<?, ?>> TREES_FLOWER_FOREST = ConfiguredFeatures.of("trees_flower_forest");
    public static final RegistryKey<ConfiguredFeature<?, ?>> MEADOW_TREES = ConfiguredFeatures.of("meadow_trees");
    public static final RegistryKey<ConfiguredFeature<?, ?>> TREES_TAIGA = ConfiguredFeatures.of("trees_taiga");
    public static final RegistryKey<ConfiguredFeature<?, ?>> TREES_BADLANDS = ConfiguredFeatures.of("trees_badlands");
    public static final RegistryKey<ConfiguredFeature<?, ?>> TREES_GROVE = ConfiguredFeatures.of("trees_grove");
    public static final RegistryKey<ConfiguredFeature<?, ?>> TREES_SAVANNA = ConfiguredFeatures.of("trees_savanna");
    public static final RegistryKey<ConfiguredFeature<?, ?>> TREES_SNOWY = ConfiguredFeatures.of("trees_snowy");
    public static final RegistryKey<ConfiguredFeature<?, ?>> TREES_BIRCH = ConfiguredFeatures.of("trees_birch");
    public static final RegistryKey<ConfiguredFeature<?, ?>> BIRCH_TALL = ConfiguredFeatures.of("birch_tall");
    public static final RegistryKey<ConfiguredFeature<?, ?>> TREES_WINDSWEPT_HILLS = ConfiguredFeatures.of("trees_windswept_hills");
    public static final RegistryKey<ConfiguredFeature<?, ?>> TREES_WATER = ConfiguredFeatures.of("trees_water");
    public static final RegistryKey<ConfiguredFeature<?, ?>> TREES_BIRCH_AND_OAK = ConfiguredFeatures.of("trees_birch_and_oak_leaf_litter");
    public static final RegistryKey<ConfiguredFeature<?, ?>> TREES_PLAINS = ConfiguredFeatures.of("trees_plains");
    public static final RegistryKey<ConfiguredFeature<?, ?>> TREES_SPARSE_JUNGLE = ConfiguredFeatures.of("trees_sparse_jungle");
    public static final RegistryKey<ConfiguredFeature<?, ?>> TREES_OLD_GROWTH_SPRUCE_TAIGA = ConfiguredFeatures.of("trees_old_growth_spruce_taiga");
    public static final RegistryKey<ConfiguredFeature<?, ?>> TREES_OLD_GROWTH_PINE_TAIGA = ConfiguredFeatures.of("trees_old_growth_pine_taiga");
    public static final RegistryKey<ConfiguredFeature<?, ?>> TREES_JUNGLE = ConfiguredFeatures.of("trees_jungle");
    public static final RegistryKey<ConfiguredFeature<?, ?>> BAMBOO_VEGETATION = ConfiguredFeatures.of("bamboo_vegetation");
    public static final RegistryKey<ConfiguredFeature<?, ?>> MUSHROOM_ISLAND_VEGETATION = ConfiguredFeatures.of("mushroom_island_vegetation");
    public static final RegistryKey<ConfiguredFeature<?, ?>> MANGROVE_VEGETATION = ConfiguredFeatures.of("mangrove_vegetation");
    private static final float FALLEN_TREE_RARITY = 80.0f;

    private static RandomPatchFeatureConfig createRandomPatchFeatureConfig(BlockStateProvider block, int tries) {
        return ConfiguredFeatures.createRandomPatchFeatureConfig(tries, PlacedFeatures.createEntry(Feature.SIMPLE_BLOCK, new SimpleBlockFeatureConfig(block)));
    }

    public static void bootstrap(Registerable<ConfiguredFeature<?, ?>> featureRegisterable) {
        RegistryEntryLookup<ConfiguredFeature<?, ?>> registryEntryLookup = featureRegisterable.getRegistryLookup(RegistryKeys.CONFIGURED_FEATURE);
        RegistryEntry.Reference<ConfiguredFeature<?, ?>> registryEntry = registryEntryLookup.getOrThrow(TreeConfiguredFeatures.HUGE_BROWN_MUSHROOM);
        RegistryEntry.Reference<ConfiguredFeature<?, ?>> registryEntry2 = registryEntryLookup.getOrThrow(TreeConfiguredFeatures.HUGE_RED_MUSHROOM);
        RegistryEntry.Reference<ConfiguredFeature<?, ?>> registryEntry3 = registryEntryLookup.getOrThrow(TreeConfiguredFeatures.FANCY_OAK_BEES_005);
        RegistryEntry.Reference<ConfiguredFeature<?, ?>> registryEntry4 = registryEntryLookup.getOrThrow(TreeConfiguredFeatures.OAK_BEES_005);
        RegistryEntry.Reference<ConfiguredFeature<?, ?>> registryEntry5 = registryEntryLookup.getOrThrow(PATCH_GRASS_JUNGLE);
        RegistryEntryLookup<PlacedFeature> registryEntryLookup2 = featureRegisterable.getRegistryLookup(RegistryKeys.PLACED_FEATURE);
        RegistryEntry.Reference<PlacedFeature> registryEntry6 = registryEntryLookup2.getOrThrow(TreePlacedFeatures.PALE_OAK_CHECKED);
        RegistryEntry.Reference<PlacedFeature> registryEntry7 = registryEntryLookup2.getOrThrow(TreePlacedFeatures.PALE_OAK_CREAKING_CHECKED);
        RegistryEntry.Reference<PlacedFeature> registryEntry8 = registryEntryLookup2.getOrThrow(TreePlacedFeatures.FANCY_OAK_CHECKED);
        RegistryEntry.Reference<PlacedFeature> registryEntry9 = registryEntryLookup2.getOrThrow(TreePlacedFeatures.BIRCH_BEES_002);
        RegistryEntry.Reference<PlacedFeature> registryEntry10 = registryEntryLookup2.getOrThrow(TreePlacedFeatures.FANCY_OAK_BEES_002);
        RegistryEntry.Reference<PlacedFeature> registryEntry11 = registryEntryLookup2.getOrThrow(TreePlacedFeatures.FANCY_OAK_BEES);
        RegistryEntry.Reference<PlacedFeature> registryEntry12 = registryEntryLookup2.getOrThrow(TreePlacedFeatures.PINE_CHECKED);
        RegistryEntry.Reference<PlacedFeature> registryEntry13 = registryEntryLookup2.getOrThrow(TreePlacedFeatures.SPRUCE_CHECKED);
        RegistryEntry.Reference<PlacedFeature> registryEntry14 = registryEntryLookup2.getOrThrow(TreePlacedFeatures.PINE_ON_SNOW);
        RegistryEntry.Reference<PlacedFeature> registryEntry15 = registryEntryLookup2.getOrThrow(TreePlacedFeatures.ACACIA_CHECKED);
        RegistryEntry.Reference<PlacedFeature> registryEntry16 = registryEntryLookup2.getOrThrow(TreePlacedFeatures.SUPER_BIRCH_BEES_0002);
        RegistryEntry.Reference<PlacedFeature> registryEntry17 = registryEntryLookup2.getOrThrow(TreePlacedFeatures.BIRCH_BEES_0002);
        RegistryEntry.Reference<PlacedFeature> registryEntry18 = registryEntryLookup2.getOrThrow(TreePlacedFeatures.BIRCH_BEES_002_LEAF_LITTER);
        RegistryEntry.Reference<PlacedFeature> registryEntry19 = registryEntryLookup2.getOrThrow(TreePlacedFeatures.FANCY_OAK_BEES_0002);
        RegistryEntry.Reference<PlacedFeature> registryEntry20 = registryEntryLookup2.getOrThrow(TreePlacedFeatures.JUNGLE_BUSH);
        RegistryEntry.Reference<PlacedFeature> registryEntry21 = registryEntryLookup2.getOrThrow(TreePlacedFeatures.MEGA_SPRUCE_CHECKED);
        RegistryEntry.Reference<PlacedFeature> registryEntry22 = registryEntryLookup2.getOrThrow(TreePlacedFeatures.MEGA_PINE_CHECKED);
        RegistryEntry.Reference<PlacedFeature> registryEntry23 = registryEntryLookup2.getOrThrow(TreePlacedFeatures.MEGA_JUNGLE_TREE_CHECKED);
        RegistryEntry.Reference<PlacedFeature> registryEntry24 = registryEntryLookup2.getOrThrow(TreePlacedFeatures.TALL_MANGROVE_CHECKED);
        RegistryEntry.Reference<PlacedFeature> registryEntry25 = registryEntryLookup2.getOrThrow(TreePlacedFeatures.OAK_CHECKED);
        RegistryEntry.Reference<PlacedFeature> registryEntry26 = registryEntryLookup2.getOrThrow(TreePlacedFeatures.OAK_BEES_002);
        RegistryEntry.Reference<PlacedFeature> registryEntry27 = registryEntryLookup2.getOrThrow(TreePlacedFeatures.SUPER_BIRCH_BEES);
        RegistryEntry.Reference<PlacedFeature> registryEntry28 = registryEntryLookup2.getOrThrow(TreePlacedFeatures.SPRUCE_ON_SNOW);
        RegistryEntry.Reference<PlacedFeature> registryEntry29 = registryEntryLookup2.getOrThrow(TreePlacedFeatures.OAK_BEES_0002);
        RegistryEntry.Reference<PlacedFeature> registryEntry30 = registryEntryLookup2.getOrThrow(TreePlacedFeatures.JUNGLE_TREE);
        RegistryEntry.Reference<PlacedFeature> registryEntry31 = registryEntryLookup2.getOrThrow(TreePlacedFeatures.MANGROVE_CHECKED);
        RegistryEntry.Reference<PlacedFeature> registryEntry32 = registryEntryLookup2.getOrThrow(TreePlacedFeatures.OAK_LEAF_LITTER);
        RegistryEntry.Reference<PlacedFeature> registryEntry33 = registryEntryLookup2.getOrThrow(TreePlacedFeatures.DARK_OAK_LEAF_LITTER);
        RegistryEntry.Reference<PlacedFeature> registryEntry34 = registryEntryLookup2.getOrThrow(TreePlacedFeatures.BIRCH_LEAF_LITTER);
        RegistryEntry.Reference<PlacedFeature> registryEntry35 = registryEntryLookup2.getOrThrow(TreePlacedFeatures.FANCY_OAK_LEAF_LITTER);
        RegistryEntry.Reference<PlacedFeature> registryEntry36 = registryEntryLookup2.getOrThrow(TreePlacedFeatures.FALLEN_OAK_TREE);
        RegistryEntry.Reference<PlacedFeature> registryEntry37 = registryEntryLookup2.getOrThrow(TreePlacedFeatures.FALLEN_BIRCH_TREE);
        RegistryEntry.Reference<PlacedFeature> registryEntry38 = registryEntryLookup2.getOrThrow(TreePlacedFeatures.FALLEN_SUPER_BIRCH_TREE);
        RegistryEntry.Reference<PlacedFeature> registryEntry39 = registryEntryLookup2.getOrThrow(TreePlacedFeatures.FALLEN_JUNGLE_TREE);
        RegistryEntry.Reference<PlacedFeature> registryEntry40 = registryEntryLookup2.getOrThrow(TreePlacedFeatures.FALLEN_SPRUCE_TREE);
        ConfiguredFeatures.register(featureRegisterable, BAMBOO_NO_PODZOL, Feature.BAMBOO, new ProbabilityConfig(0.0f));
        ConfiguredFeatures.register(featureRegisterable, BAMBOO_SOME_PODZOL, Feature.BAMBOO, new ProbabilityConfig(0.2f));
        ConfiguredFeatures.register(featureRegisterable, VINES, Feature.VINES);
        ConfiguredFeatures.register(featureRegisterable, PATCH_BROWN_MUSHROOM, Feature.RANDOM_PATCH, ConfiguredFeatures.createRandomPatchFeatureConfig(Feature.SIMPLE_BLOCK, new SimpleBlockFeatureConfig(BlockStateProvider.of(Blocks.BROWN_MUSHROOM))));
        ConfiguredFeatures.register(featureRegisterable, PATCH_RED_MUSHROOM, Feature.RANDOM_PATCH, ConfiguredFeatures.createRandomPatchFeatureConfig(Feature.SIMPLE_BLOCK, new SimpleBlockFeatureConfig(BlockStateProvider.of(Blocks.RED_MUSHROOM))));
        ConfiguredFeatures.register(featureRegisterable, PATCH_SUNFLOWER, Feature.RANDOM_PATCH, ConfiguredFeatures.createRandomPatchFeatureConfig(Feature.SIMPLE_BLOCK, new SimpleBlockFeatureConfig(BlockStateProvider.of(Blocks.SUNFLOWER))));
        ConfiguredFeatures.register(featureRegisterable, PATCH_PUMPKIN, Feature.RANDOM_PATCH, ConfiguredFeatures.createRandomPatchFeatureConfig(Feature.SIMPLE_BLOCK, new SimpleBlockFeatureConfig(BlockStateProvider.of(Blocks.PUMPKIN)), List.of(Blocks.GRASS_BLOCK)));
        ConfiguredFeatures.register(featureRegisterable, PATCH_BERRY_BUSH, Feature.RANDOM_PATCH, ConfiguredFeatures.createRandomPatchFeatureConfig(Feature.SIMPLE_BLOCK, new SimpleBlockFeatureConfig(BlockStateProvider.of((BlockState)Blocks.SWEET_BERRY_BUSH.getDefaultState().with(SweetBerryBushBlock.AGE, 3))), List.of(Blocks.GRASS_BLOCK)));
        ConfiguredFeatures.register(featureRegisterable, PATCH_TAIGA_GRASS, Feature.RANDOM_PATCH, VegetationConfiguredFeatures.createRandomPatchFeatureConfig(new WeightedBlockStateProvider(Pool.builder().add(Blocks.SHORT_GRASS.getDefaultState(), 1).add(Blocks.FERN.getDefaultState(), 4)), 32));
        ConfiguredFeatures.register(featureRegisterable, PATCH_GRASS, Feature.RANDOM_PATCH, VegetationConfiguredFeatures.createRandomPatchFeatureConfig(BlockStateProvider.of(Blocks.SHORT_GRASS), 32));
        ConfiguredFeatures.register(featureRegisterable, PATCH_GRASS_MEADOW, Feature.RANDOM_PATCH, VegetationConfiguredFeatures.createRandomPatchFeatureConfig(BlockStateProvider.of(Blocks.SHORT_GRASS), 16));
        ConfiguredFeatures.register(featureRegisterable, PATCH_LEAF_LITTER, Feature.RANDOM_PATCH, ConfiguredFeatures.createRandomPatchFeatureConfig(32, PlacedFeatures.createEntry(Feature.SIMPLE_BLOCK, new SimpleBlockFeatureConfig(new WeightedBlockStateProvider(VegetationConfiguredFeatures.leafLitter(1, 3))), BlockPredicate.bothOf(BlockPredicate.IS_AIR, BlockPredicate.matchingBlocks(Direction.DOWN.getVector(), Blocks.GRASS_BLOCK)))));
        ConfiguredFeatures.register(featureRegisterable, PATCH_GRASS_JUNGLE, Feature.RANDOM_PATCH, new RandomPatchFeatureConfig(32, 7, 3, PlacedFeatures.createEntry(Feature.SIMPLE_BLOCK, new SimpleBlockFeatureConfig(new WeightedBlockStateProvider(Pool.builder().add(Blocks.SHORT_GRASS.getDefaultState(), 3).add(Blocks.FERN.getDefaultState(), 1))), BlockPredicate.bothOf(BlockPredicate.IS_AIR, BlockPredicate.not(BlockPredicate.matchingBlocks(Direction.DOWN.getVector(), Blocks.PODZOL))))));
        ConfiguredFeatures.register(featureRegisterable, SINGLE_PIECE_OF_GRASS, Feature.SIMPLE_BLOCK, new SimpleBlockFeatureConfig(BlockStateProvider.of(Blocks.SHORT_GRASS.getDefaultState())));
        ConfiguredFeatures.register(featureRegisterable, PATCH_DEAD_BUSH, Feature.RANDOM_PATCH, VegetationConfiguredFeatures.createRandomPatchFeatureConfig(BlockStateProvider.of(Blocks.DEAD_BUSH), 4));
        ConfiguredFeatures.register(featureRegisterable, PATCH_DRY_GRASS, Feature.RANDOM_PATCH, VegetationConfiguredFeatures.createRandomPatchFeatureConfig(new WeightedBlockStateProvider(Pool.builder().add(Blocks.SHORT_DRY_GRASS.getDefaultState(), 1).add(Blocks.TALL_DRY_GRASS.getDefaultState(), 1)), 64));
        ConfiguredFeatures.register(featureRegisterable, PATCH_MELON, Feature.RANDOM_PATCH, new RandomPatchFeatureConfig(64, 7, 3, PlacedFeatures.createEntry(Feature.SIMPLE_BLOCK, new SimpleBlockFeatureConfig(BlockStateProvider.of(Blocks.MELON)), BlockPredicate.allOf(BlockPredicate.replaceable(), BlockPredicate.noFluid(), BlockPredicate.matchingBlocks(Direction.DOWN.getVector(), Blocks.GRASS_BLOCK)))));
        ConfiguredFeatures.register(featureRegisterable, PATCH_WATERLILY, Feature.RANDOM_PATCH, new RandomPatchFeatureConfig(10, 7, 3, PlacedFeatures.createEntry(Feature.SIMPLE_BLOCK, new SimpleBlockFeatureConfig(BlockStateProvider.of(Blocks.LILY_PAD)))));
        ConfiguredFeatures.register(featureRegisterable, PATCH_TALL_GRASS, Feature.RANDOM_PATCH, ConfiguredFeatures.createRandomPatchFeatureConfig(Feature.SIMPLE_BLOCK, new SimpleBlockFeatureConfig(BlockStateProvider.of(Blocks.TALL_GRASS))));
        ConfiguredFeatures.register(featureRegisterable, PATCH_LARGE_FERN, Feature.RANDOM_PATCH, ConfiguredFeatures.createRandomPatchFeatureConfig(Feature.SIMPLE_BLOCK, new SimpleBlockFeatureConfig(BlockStateProvider.of(Blocks.LARGE_FERN))));
        ConfiguredFeatures.register(featureRegisterable, PATCH_BUSH, Feature.RANDOM_PATCH, new RandomPatchFeatureConfig(24, 5, 3, PlacedFeatures.createEntry(Feature.SIMPLE_BLOCK, new SimpleBlockFeatureConfig(BlockStateProvider.of(Blocks.BUSH)))));
        ConfiguredFeatures.register(featureRegisterable, PATCH_CACTUS, Feature.RANDOM_PATCH, ConfiguredFeatures.createRandomPatchFeatureConfig(10, PlacedFeatures.createEntry(Feature.BLOCK_COLUMN, new BlockColumnFeatureConfig(List.of(BlockColumnFeatureConfig.createLayer(BiasedToBottomIntProvider.create(1, 3), BlockStateProvider.of(Blocks.CACTUS)), BlockColumnFeatureConfig.createLayer(new WeightedListIntProvider(Pool.builder().add(ConstantIntProvider.create(0), 3).add(ConstantIntProvider.create(1), 1).build()), BlockStateProvider.of(Blocks.CACTUS_FLOWER))), Direction.UP, BlockPredicate.IS_AIR, false), BlockFilterPlacementModifier.of(BlockPredicate.bothOf(BlockPredicate.IS_AIR, BlockPredicate.wouldSurvive(Blocks.CACTUS.getDefaultState(), BlockPos.ORIGIN))))));
        ConfiguredFeatures.register(featureRegisterable, PATCH_SUGAR_CANE, Feature.RANDOM_PATCH, new RandomPatchFeatureConfig(20, 4, 0, PlacedFeatures.createEntry(Feature.BLOCK_COLUMN, BlockColumnFeatureConfig.create(BiasedToBottomIntProvider.create(2, 4), BlockStateProvider.of(Blocks.SUGAR_CANE)), VegetationConfiguredFeatures.wouldSurviveNearWaterModifier(Blocks.SUGAR_CANE))));
        ConfiguredFeatures.register(featureRegisterable, PATCH_FIREFLY_BUSH, Feature.RANDOM_PATCH, new RandomPatchFeatureConfig(20, 4, 3, PlacedFeatures.createEntry(Feature.SIMPLE_BLOCK, new SimpleBlockFeatureConfig(BlockStateProvider.of(Blocks.FIREFLY_BUSH)))));
        ConfiguredFeatures.register(featureRegisterable, FLOWER_DEFAULT, Feature.FLOWER, VegetationConfiguredFeatures.createRandomPatchFeatureConfig(new WeightedBlockStateProvider(Pool.builder().add(Blocks.POPPY.getDefaultState(), 2).add(Blocks.DANDELION.getDefaultState(), 1)), 64));
        ConfiguredFeatures.register(featureRegisterable, FLOWER_FLOWER_FOREST, Feature.FLOWER, new RandomPatchFeatureConfig(96, 6, 2, PlacedFeatures.createEntry(Feature.SIMPLE_BLOCK, new SimpleBlockFeatureConfig(new NoiseBlockStateProvider(2345L, new DoublePerlinNoiseSampler.NoiseParameters(0, 1.0, new double[0]), 0.020833334f, List.of(Blocks.DANDELION.getDefaultState(), Blocks.POPPY.getDefaultState(), Blocks.ALLIUM.getDefaultState(), Blocks.AZURE_BLUET.getDefaultState(), Blocks.RED_TULIP.getDefaultState(), Blocks.ORANGE_TULIP.getDefaultState(), Blocks.WHITE_TULIP.getDefaultState(), Blocks.PINK_TULIP.getDefaultState(), Blocks.OXEYE_DAISY.getDefaultState(), Blocks.CORNFLOWER.getDefaultState(), Blocks.LILY_OF_THE_VALLEY.getDefaultState()))))));
        ConfiguredFeatures.register(featureRegisterable, FLOWER_SWAMP, Feature.FLOWER, new RandomPatchFeatureConfig(64, 6, 2, PlacedFeatures.createEntry(Feature.SIMPLE_BLOCK, new SimpleBlockFeatureConfig(BlockStateProvider.of(Blocks.BLUE_ORCHID)))));
        ConfiguredFeatures.register(featureRegisterable, FLOWER_PLAIN, Feature.FLOWER, new RandomPatchFeatureConfig(64, 6, 2, PlacedFeatures.createEntry(Feature.SIMPLE_BLOCK, new SimpleBlockFeatureConfig(new NoiseThresholdBlockStateProvider(2345L, new DoublePerlinNoiseSampler.NoiseParameters(0, 1.0, new double[0]), 0.005f, -0.8f, 0.33333334f, Blocks.DANDELION.getDefaultState(), List.of(Blocks.ORANGE_TULIP.getDefaultState(), Blocks.RED_TULIP.getDefaultState(), Blocks.PINK_TULIP.getDefaultState(), Blocks.WHITE_TULIP.getDefaultState()), List.of(Blocks.POPPY.getDefaultState(), Blocks.AZURE_BLUET.getDefaultState(), Blocks.OXEYE_DAISY.getDefaultState(), Blocks.CORNFLOWER.getDefaultState()))))));
        ConfiguredFeatures.register(featureRegisterable, FLOWER_MEADOW, Feature.FLOWER, new RandomPatchFeatureConfig(96, 6, 2, PlacedFeatures.createEntry(Feature.SIMPLE_BLOCK, new SimpleBlockFeatureConfig(new DualNoiseBlockStateProvider(new Range<Integer>(1, 3), new DoublePerlinNoiseSampler.NoiseParameters(-10, 1.0, new double[0]), 1.0f, 2345L, new DoublePerlinNoiseSampler.NoiseParameters(-3, 1.0, new double[0]), 1.0f, List.of(Blocks.TALL_GRASS.getDefaultState(), Blocks.ALLIUM.getDefaultState(), Blocks.POPPY.getDefaultState(), Blocks.AZURE_BLUET.getDefaultState(), Blocks.DANDELION.getDefaultState(), Blocks.CORNFLOWER.getDefaultState(), Blocks.OXEYE_DAISY.getDefaultState(), Blocks.SHORT_GRASS.getDefaultState()))))));
        ConfiguredFeatures.register(featureRegisterable, FLOWER_CHERRY, Feature.FLOWER, new RandomPatchFeatureConfig(96, 6, 2, PlacedFeatures.createEntry(Feature.SIMPLE_BLOCK, new SimpleBlockFeatureConfig(new WeightedBlockStateProvider(VegetationConfiguredFeatures.flowerbed(Blocks.PINK_PETALS))))));
        ConfiguredFeatures.register(featureRegisterable, WILDFLOWERS_BIRCH_FOREST, Feature.FLOWER, new RandomPatchFeatureConfig(64, 6, 2, PlacedFeatures.createEntry(Feature.SIMPLE_BLOCK, new SimpleBlockFeatureConfig(new WeightedBlockStateProvider(VegetationConfiguredFeatures.flowerbed(Blocks.WILDFLOWERS))))));
        ConfiguredFeatures.register(featureRegisterable, WILDFLOWERS_MEADOW, Feature.FLOWER, new RandomPatchFeatureConfig(8, 6, 2, PlacedFeatures.createEntry(Feature.SIMPLE_BLOCK, new SimpleBlockFeatureConfig(new WeightedBlockStateProvider(VegetationConfiguredFeatures.flowerbed(Blocks.WILDFLOWERS))))));
        ConfiguredFeatures.register(featureRegisterable, FLOWER_PALE_GARDEN, Feature.FLOWER, new RandomPatchFeatureConfig(1, 0, 0, PlacedFeatures.createEntry(Feature.SIMPLE_BLOCK, new SimpleBlockFeatureConfig(BlockStateProvider.of(Blocks.CLOSED_EYEBLOSSOM), true))));
        ConfiguredFeatures.register(featureRegisterable, FOREST_FLOWERS, Feature.SIMPLE_RANDOM_SELECTOR, new SimpleRandomFeatureConfig(RegistryEntryList.of(PlacedFeatures.createEntry(Feature.RANDOM_PATCH, ConfiguredFeatures.createRandomPatchFeatureConfig(Feature.SIMPLE_BLOCK, new SimpleBlockFeatureConfig(BlockStateProvider.of(Blocks.LILAC))), new PlacementModifier[0]), PlacedFeatures.createEntry(Feature.RANDOM_PATCH, ConfiguredFeatures.createRandomPatchFeatureConfig(Feature.SIMPLE_BLOCK, new SimpleBlockFeatureConfig(BlockStateProvider.of(Blocks.ROSE_BUSH))), new PlacementModifier[0]), PlacedFeatures.createEntry(Feature.RANDOM_PATCH, ConfiguredFeatures.createRandomPatchFeatureConfig(Feature.SIMPLE_BLOCK, new SimpleBlockFeatureConfig(BlockStateProvider.of(Blocks.PEONY))), new PlacementModifier[0]), PlacedFeatures.createEntry(Feature.NO_BONEMEAL_FLOWER, ConfiguredFeatures.createRandomPatchFeatureConfig(Feature.SIMPLE_BLOCK, new SimpleBlockFeatureConfig(BlockStateProvider.of(Blocks.LILY_OF_THE_VALLEY))), new PlacementModifier[0]))));
        ConfiguredFeatures.register(featureRegisterable, PALE_FOREST_FLOWERS, Feature.RANDOM_PATCH, ConfiguredFeatures.createRandomPatchFeatureConfig(Feature.SIMPLE_BLOCK, new SimpleBlockFeatureConfig(BlockStateProvider.of(Blocks.CLOSED_EYEBLOSSOM), true)));
        ConfiguredFeatures.register(featureRegisterable, DARK_FOREST_VEGETATION, Feature.RANDOM_SELECTOR, new RandomFeatureConfig(List.of(new RandomFeatureEntry(PlacedFeatures.createEntry(registryEntry, new PlacementModifier[0]), 0.025f), new RandomFeatureEntry(PlacedFeatures.createEntry(registryEntry2, new PlacementModifier[0]), 0.05f), new RandomFeatureEntry(registryEntry33, 0.6666667f), new RandomFeatureEntry(registryEntry37, 0.0025f), new RandomFeatureEntry(registryEntry34, 0.2f), new RandomFeatureEntry(registryEntry36, 0.0125f), new RandomFeatureEntry(registryEntry35, 0.1f)), registryEntry32));
        ConfiguredFeatures.register(featureRegisterable, PALE_GARDEN_VEGETATION, Feature.RANDOM_SELECTOR, new RandomFeatureConfig(List.of(new RandomFeatureEntry(registryEntry7, 0.1f), new RandomFeatureEntry(registryEntry6, 0.9f)), registryEntry6));
        ConfiguredFeatures.register(featureRegisterable, PALE_MOSS_VEGETATION, Feature.SIMPLE_BLOCK, new SimpleBlockFeatureConfig(new WeightedBlockStateProvider(Pool.builder().add(Blocks.PALE_MOSS_CARPET.getDefaultState(), 25).add(Blocks.SHORT_GRASS.getDefaultState(), 25).add(Blocks.TALL_GRASS.getDefaultState(), 10))));
        ConfiguredFeatures.register(featureRegisterable, PALE_MOSS_PATCH, Feature.VEGETATION_PATCH, new VegetationPatchFeatureConfig(BlockTags.MOSS_REPLACEABLE, BlockStateProvider.of(Blocks.PALE_MOSS_BLOCK), PlacedFeatures.createEntry(registryEntryLookup.getOrThrow(PALE_MOSS_VEGETATION), new PlacementModifier[0]), VerticalSurfaceType.FLOOR, ConstantIntProvider.create(1), 0.0f, 5, 0.3f, UniformIntProvider.create(2, 4), 0.75f));
        ConfiguredFeatures.register(featureRegisterable, PALE_MOSS_PATCH_BONEMEAL, Feature.VEGETATION_PATCH, new VegetationPatchFeatureConfig(BlockTags.MOSS_REPLACEABLE, BlockStateProvider.of(Blocks.PALE_MOSS_BLOCK), PlacedFeatures.createEntry(registryEntryLookup.getOrThrow(PALE_MOSS_VEGETATION), new PlacementModifier[0]), VerticalSurfaceType.FLOOR, ConstantIntProvider.create(1), 0.0f, 5, 0.6f, UniformIntProvider.create(1, 2), 0.75f));
        ConfiguredFeatures.register(featureRegisterable, TREES_FLOWER_FOREST, Feature.RANDOM_SELECTOR, new RandomFeatureConfig(List.of(new RandomFeatureEntry(registryEntry37, 0.0025f), new RandomFeatureEntry(registryEntry9, 0.2f), new RandomFeatureEntry(registryEntry10, 0.1f)), registryEntry26));
        ConfiguredFeatures.register(featureRegisterable, MEADOW_TREES, Feature.RANDOM_SELECTOR, new RandomFeatureConfig(List.of(new RandomFeatureEntry(registryEntry11, 0.5f)), registryEntry27));
        ConfiguredFeatures.register(featureRegisterable, TREES_TAIGA, Feature.RANDOM_SELECTOR, new RandomFeatureConfig(List.of(new RandomFeatureEntry(registryEntry12, 0.33333334f), new RandomFeatureEntry(registryEntry40, 0.0125f)), registryEntry13));
        ConfiguredFeatures.register(featureRegisterable, TREES_BADLANDS, Feature.RANDOM_SELECTOR, new RandomFeatureConfig(List.of(new RandomFeatureEntry(registryEntry36, 0.0125f)), registryEntry32));
        ConfiguredFeatures.register(featureRegisterable, TREES_GROVE, Feature.RANDOM_SELECTOR, new RandomFeatureConfig(List.of(new RandomFeatureEntry(registryEntry14, 0.33333334f)), registryEntry28));
        ConfiguredFeatures.register(featureRegisterable, TREES_SAVANNA, Feature.RANDOM_SELECTOR, new RandomFeatureConfig(List.of(new RandomFeatureEntry(registryEntry15, 0.8f), new RandomFeatureEntry(registryEntry36, 0.0125f)), registryEntry25));
        ConfiguredFeatures.register(featureRegisterable, TREES_SNOWY, Feature.RANDOM_SELECTOR, new RandomFeatureConfig(List.of(new RandomFeatureEntry(registryEntry40, 0.0125f)), registryEntry13));
        ConfiguredFeatures.register(featureRegisterable, TREES_BIRCH, Feature.RANDOM_SELECTOR, new RandomFeatureConfig(List.of(new RandomFeatureEntry(registryEntry37, 0.0125f)), registryEntry17));
        ConfiguredFeatures.register(featureRegisterable, BIRCH_TALL, Feature.RANDOM_SELECTOR, new RandomFeatureConfig(List.of(new RandomFeatureEntry(registryEntry38, 0.00625f), new RandomFeatureEntry(registryEntry16, 0.5f), new RandomFeatureEntry(registryEntry37, 0.0125f)), registryEntry17));
        ConfiguredFeatures.register(featureRegisterable, TREES_WINDSWEPT_HILLS, Feature.RANDOM_SELECTOR, new RandomFeatureConfig(List.of(new RandomFeatureEntry(registryEntry40, 0.008325f), new RandomFeatureEntry(registryEntry13, 0.666f), new RandomFeatureEntry(registryEntry8, 0.1f), new RandomFeatureEntry(registryEntry36, 0.0125f)), registryEntry25));
        ConfiguredFeatures.register(featureRegisterable, TREES_WATER, Feature.RANDOM_SELECTOR, new RandomFeatureConfig(List.of(new RandomFeatureEntry(registryEntry8, 0.1f)), registryEntry25));
        ConfiguredFeatures.register(featureRegisterable, TREES_BIRCH_AND_OAK, Feature.RANDOM_SELECTOR, new RandomFeatureConfig(List.of(new RandomFeatureEntry(registryEntry37, 0.0025f), new RandomFeatureEntry(registryEntry18, 0.2f), new RandomFeatureEntry(registryEntry19, 0.1f), new RandomFeatureEntry(registryEntry36, 0.0125f)), registryEntry29));
        ConfiguredFeatures.register(featureRegisterable, TREES_PLAINS, Feature.RANDOM_SELECTOR, new RandomFeatureConfig(List.of(new RandomFeatureEntry(PlacedFeatures.createEntry(registryEntry3, new PlacementModifier[0]), 0.33333334f), new RandomFeatureEntry(registryEntry36, 0.0125f)), PlacedFeatures.createEntry(registryEntry4, new PlacementModifier[0])));
        ConfiguredFeatures.register(featureRegisterable, TREES_SPARSE_JUNGLE, Feature.RANDOM_SELECTOR, new RandomFeatureConfig(List.of(new RandomFeatureEntry(registryEntry8, 0.1f), new RandomFeatureEntry(registryEntry20, 0.5f), new RandomFeatureEntry(registryEntry39, 0.0125f)), registryEntry30));
        ConfiguredFeatures.register(featureRegisterable, TREES_OLD_GROWTH_SPRUCE_TAIGA, Feature.RANDOM_SELECTOR, new RandomFeatureConfig(List.of(new RandomFeatureEntry(registryEntry21, 0.33333334f), new RandomFeatureEntry(registryEntry12, 0.33333334f), new RandomFeatureEntry(registryEntry40, 0.0125f)), registryEntry13));
        ConfiguredFeatures.register(featureRegisterable, TREES_OLD_GROWTH_PINE_TAIGA, Feature.RANDOM_SELECTOR, new RandomFeatureConfig(List.of(new RandomFeatureEntry(registryEntry21, 0.025641026f), new RandomFeatureEntry(registryEntry22, 0.30769232f), new RandomFeatureEntry(registryEntry12, 0.33333334f), new RandomFeatureEntry(registryEntry40, 0.0125f)), registryEntry13));
        ConfiguredFeatures.register(featureRegisterable, TREES_JUNGLE, Feature.RANDOM_SELECTOR, new RandomFeatureConfig(List.of(new RandomFeatureEntry(registryEntry8, 0.1f), new RandomFeatureEntry(registryEntry20, 0.5f), new RandomFeatureEntry(registryEntry23, 0.33333334f), new RandomFeatureEntry(registryEntry39, 0.0125f)), registryEntry30));
        ConfiguredFeatures.register(featureRegisterable, BAMBOO_VEGETATION, Feature.RANDOM_SELECTOR, new RandomFeatureConfig(List.of(new RandomFeatureEntry(registryEntry8, 0.05f), new RandomFeatureEntry(registryEntry20, 0.15f), new RandomFeatureEntry(registryEntry23, 0.7f)), PlacedFeatures.createEntry(registryEntry5, new PlacementModifier[0])));
        ConfiguredFeatures.register(featureRegisterable, MUSHROOM_ISLAND_VEGETATION, Feature.RANDOM_BOOLEAN_SELECTOR, new RandomBooleanFeatureConfig(PlacedFeatures.createEntry(registryEntry2, new PlacementModifier[0]), PlacedFeatures.createEntry(registryEntry, new PlacementModifier[0])));
        ConfiguredFeatures.register(featureRegisterable, MANGROVE_VEGETATION, Feature.RANDOM_SELECTOR, new RandomFeatureConfig(List.of(new RandomFeatureEntry(registryEntry24, 0.85f)), registryEntry31));
    }

    private static Pool.Builder<BlockState> flowerbed(Block flowerbed) {
        return VegetationConfiguredFeatures.segmentedBlock(flowerbed, 1, 4, FlowerbedBlock.FLOWER_AMOUNT, FlowerbedBlock.HORIZONTAL_FACING);
    }

    public static Pool.Builder<BlockState> leafLitter(int min, int max) {
        return VegetationConfiguredFeatures.segmentedBlock(Blocks.LEAF_LITTER, min, max, LeafLitterBlock.SEGMENT_AMOUNT, LeafLitterBlock.HORIZONTAL_FACING);
    }

    private static Pool.Builder<BlockState> segmentedBlock(Block block, int min, int max, IntProperty amountProperty, EnumProperty<Direction> facingProperty) {
        Pool.Builder<BlockState> builder = Pool.builder();
        for (int i = min; i <= max; ++i) {
            for (Direction direction : Direction.Type.HORIZONTAL) {
                builder.add((BlockState)((BlockState)block.getDefaultState().with(amountProperty, i)).with(facingProperty, direction), 1);
            }
        }
        return builder;
    }

    public static BlockFilterPlacementModifier wouldSurviveNearWaterModifier(Block block) {
        return BlockFilterPlacementModifier.of(BlockPredicate.allOf(BlockPredicate.IS_AIR, BlockPredicate.wouldSurvive(block.getDefaultState(), BlockPos.ORIGIN), BlockPredicate.anyOf(BlockPredicate.matchingFluids((Vec3i)new BlockPos(1, -1, 0), Fluids.WATER, Fluids.FLOWING_WATER), BlockPredicate.matchingFluids((Vec3i)new BlockPos(-1, -1, 0), Fluids.WATER, Fluids.FLOWING_WATER), BlockPredicate.matchingFluids((Vec3i)new BlockPos(0, -1, 1), Fluids.WATER, Fluids.FLOWING_WATER), BlockPredicate.matchingFluids((Vec3i)new BlockPos(0, -1, -1), Fluids.WATER, Fluids.FLOWING_WATER))));
    }
}


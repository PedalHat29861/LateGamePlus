/*
 * Decompiled with CFR 0.152.
 */
package net.minecraft.data.loottable;

import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Set;
import java.util.function.BiConsumer;
import java.util.function.Function;
import java.util.stream.IntStream;
import net.minecraft.block.BeehiveBlock;
import net.minecraft.block.Block;
import net.minecraft.block.Blocks;
import net.minecraft.block.CandleBlock;
import net.minecraft.block.CaveVines;
import net.minecraft.block.CopperGolemStatueBlock;
import net.minecraft.block.DoorBlock;
import net.minecraft.block.FlowerPotBlock;
import net.minecraft.block.MultifaceBlock;
import net.minecraft.block.PaleMossCarpetBlock;
import net.minecraft.block.Segmented;
import net.minecraft.block.SlabBlock;
import net.minecraft.block.StemBlock;
import net.minecraft.block.TallPlantBlock;
import net.minecraft.block.enums.DoubleBlockHalf;
import net.minecraft.block.enums.SlabType;
import net.minecraft.component.DataComponentTypes;
import net.minecraft.data.loottable.LootTableGenerator;
import net.minecraft.enchantment.Enchantments;
import net.minecraft.item.Item;
import net.minecraft.item.ItemConvertible;
import net.minecraft.item.Items;
import net.minecraft.loot.LootPool;
import net.minecraft.loot.LootTable;
import net.minecraft.loot.condition.BlockStatePropertyLootCondition;
import net.minecraft.loot.condition.LocationCheckLootCondition;
import net.minecraft.loot.condition.LootCondition;
import net.minecraft.loot.condition.LootConditionConsumingBuilder;
import net.minecraft.loot.condition.MatchToolLootCondition;
import net.minecraft.loot.condition.RandomChanceLootCondition;
import net.minecraft.loot.condition.SurvivesExplosionLootCondition;
import net.minecraft.loot.condition.TableBonusLootCondition;
import net.minecraft.loot.context.LootContextParameters;
import net.minecraft.loot.entry.AlternativeEntry;
import net.minecraft.loot.entry.ItemEntry;
import net.minecraft.loot.entry.LeafEntry;
import net.minecraft.loot.entry.LootPoolEntry;
import net.minecraft.loot.function.ApplyBonusLootFunction;
import net.minecraft.loot.function.CopyComponentsLootFunction;
import net.minecraft.loot.function.CopyStateLootFunction;
import net.minecraft.loot.function.ExplosionDecayLootFunction;
import net.minecraft.loot.function.LimitCountLootFunction;
import net.minecraft.loot.function.LootFunction;
import net.minecraft.loot.function.LootFunctionConsumingBuilder;
import net.minecraft.loot.function.SetCountLootFunction;
import net.minecraft.loot.operator.BoundedIntUnaryOperator;
import net.minecraft.loot.provider.number.BinomialLootNumberProvider;
import net.minecraft.loot.provider.number.ConstantLootNumberProvider;
import net.minecraft.loot.provider.number.LootNumberProvider;
import net.minecraft.loot.provider.number.UniformLootNumberProvider;
import net.minecraft.predicate.BlockPredicate;
import net.minecraft.predicate.NumberRange;
import net.minecraft.predicate.StatePredicate;
import net.minecraft.predicate.component.ComponentPredicateTypes;
import net.minecraft.predicate.component.ComponentsPredicate;
import net.minecraft.predicate.entity.LocationPredicate;
import net.minecraft.predicate.item.EnchantmentPredicate;
import net.minecraft.predicate.item.EnchantmentsPredicate;
import net.minecraft.predicate.item.ItemPredicate;
import net.minecraft.registry.Registries;
import net.minecraft.registry.RegistryEntryLookup;
import net.minecraft.registry.RegistryKey;
import net.minecraft.registry.RegistryKeys;
import net.minecraft.registry.RegistryWrapper;
import net.minecraft.resource.featuretoggle.FeatureSet;
import net.minecraft.state.property.Property;
import net.minecraft.util.StringIdentifiable;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Direction;

public abstract class BlockLootTableGenerator
implements LootTableGenerator {
    protected final RegistryWrapper.WrapperLookup registries;
    protected final Set<Item> explosionImmuneItems;
    protected final FeatureSet requiredFeatures;
    protected final Map<RegistryKey<LootTable>, LootTable.Builder> lootTables;
    protected static final float[] SAPLING_DROP_CHANCE = new float[]{0.05f, 0.0625f, 0.083333336f, 0.1f};
    private static final float[] LEAVES_STICK_DROP_CHANCE = new float[]{0.02f, 0.022222223f, 0.025f, 0.033333335f, 0.1f};

    protected LootCondition.Builder createSilkTouchCondition() {
        return MatchToolLootCondition.builder(ItemPredicate.Builder.create().components(ComponentsPredicate.Builder.create().partial(ComponentPredicateTypes.ENCHANTMENTS, EnchantmentsPredicate.enchantments(List.of(new EnchantmentPredicate(this.registries.getOrThrow(RegistryKeys.ENCHANTMENT).getOrThrow(Enchantments.SILK_TOUCH), NumberRange.IntRange.atLeast(1))))).build()));
    }

    protected LootCondition.Builder createWithoutSilkTouchCondition() {
        return this.createSilkTouchCondition().invert();
    }

    protected LootCondition.Builder createWithShearsCondition() {
        return MatchToolLootCondition.builder(ItemPredicate.Builder.create().items(this.registries.getOrThrow(RegistryKeys.ITEM), Items.SHEARS));
    }

    private LootCondition.Builder createWithSilkTouchOrShearsCondition() {
        return this.createWithShearsCondition().or(this.createSilkTouchCondition());
    }

    private LootCondition.Builder createWithoutShearsOrSilkTouchCondition() {
        return this.createWithSilkTouchOrShearsCondition().invert();
    }

    protected BlockLootTableGenerator(Set<Item> explosionImmuneItems, FeatureSet requiredFeatures, RegistryWrapper.WrapperLookup registries) {
        this(explosionImmuneItems, requiredFeatures, new HashMap<RegistryKey<LootTable>, LootTable.Builder>(), registries);
    }

    protected BlockLootTableGenerator(Set<Item> explosionImmuneItems, FeatureSet requiredFeatures, Map<RegistryKey<LootTable>, LootTable.Builder> lootTables, RegistryWrapper.WrapperLookup registries) {
        this.explosionImmuneItems = explosionImmuneItems;
        this.requiredFeatures = requiredFeatures;
        this.lootTables = lootTables;
        this.registries = registries;
    }

    protected <T extends LootFunctionConsumingBuilder<T>> T applyExplosionDecay(ItemConvertible drop, LootFunctionConsumingBuilder<T> builder) {
        if (!this.explosionImmuneItems.contains(drop.asItem())) {
            return builder.apply(ExplosionDecayLootFunction.builder());
        }
        return builder.getThisFunctionConsumingBuilder();
    }

    protected <T extends LootConditionConsumingBuilder<T>> T addSurvivesExplosionCondition(ItemConvertible drop, LootConditionConsumingBuilder<T> builder) {
        if (!this.explosionImmuneItems.contains(drop.asItem())) {
            return builder.conditionally(SurvivesExplosionLootCondition.builder());
        }
        return builder.getThisConditionConsumingBuilder();
    }

    public LootTable.Builder drops(ItemConvertible drop) {
        return LootTable.builder().pool(this.addSurvivesExplosionCondition(drop, LootPool.builder().rolls(ConstantLootNumberProvider.create(1.0f)).with(ItemEntry.builder(drop))));
    }

    private static LootTable.Builder drops(Block drop, LootCondition.Builder conditionBuilder, LootPoolEntry.Builder<?> child) {
        return LootTable.builder().pool(LootPool.builder().rolls(ConstantLootNumberProvider.create(1.0f)).with(((LeafEntry.Builder)ItemEntry.builder(drop).conditionally(conditionBuilder)).alternatively(child)));
    }

    protected LootTable.Builder dropsWithSilkTouch(Block block, LootPoolEntry.Builder<?> loot) {
        return BlockLootTableGenerator.drops(block, this.createSilkTouchCondition(), loot);
    }

    protected LootTable.Builder dropsWithShears(Block block, LootPoolEntry.Builder<?> loot) {
        return BlockLootTableGenerator.drops(block, this.createWithShearsCondition(), loot);
    }

    protected LootTable.Builder dropsWithSilkTouchOrShears(Block block, LootPoolEntry.Builder<?> loot) {
        return BlockLootTableGenerator.drops(block, this.createWithSilkTouchOrShearsCondition(), loot);
    }

    protected LootTable.Builder drops(Block withSilkTouch, ItemConvertible withoutSilkTouch) {
        return this.dropsWithSilkTouch(withSilkTouch, (LootPoolEntry.Builder)this.addSurvivesExplosionCondition(withSilkTouch, ItemEntry.builder(withoutSilkTouch)));
    }

    protected LootTable.Builder drops(ItemConvertible drop, LootNumberProvider count) {
        return LootTable.builder().pool(LootPool.builder().rolls(ConstantLootNumberProvider.create(1.0f)).with((LootPoolEntry.Builder)this.applyExplosionDecay(drop, ItemEntry.builder(drop).apply(SetCountLootFunction.builder(count)))));
    }

    protected LootTable.Builder drops(Block block, ItemConvertible drop, LootNumberProvider count) {
        return this.dropsWithSilkTouch(block, (LootPoolEntry.Builder)this.applyExplosionDecay(block, ItemEntry.builder(drop).apply(SetCountLootFunction.builder(count))));
    }

    private LootTable.Builder dropsWithSilkTouch(ItemConvertible drop) {
        return LootTable.builder().pool(LootPool.builder().conditionally(this.createSilkTouchCondition()).rolls(ConstantLootNumberProvider.create(1.0f)).with(ItemEntry.builder(drop)));
    }

    private LootTable.Builder pottedPlantDrops(ItemConvertible drop) {
        return LootTable.builder().pool(this.addSurvivesExplosionCondition(Blocks.FLOWER_POT, LootPool.builder().rolls(ConstantLootNumberProvider.create(1.0f)).with(ItemEntry.builder(Blocks.FLOWER_POT)))).pool(this.addSurvivesExplosionCondition(drop, LootPool.builder().rolls(ConstantLootNumberProvider.create(1.0f)).with(ItemEntry.builder(drop))));
    }

    protected LootTable.Builder slabDrops(Block drop) {
        return LootTable.builder().pool(LootPool.builder().rolls(ConstantLootNumberProvider.create(1.0f)).with((LootPoolEntry.Builder)this.applyExplosionDecay(drop, ItemEntry.builder(drop).apply((LootFunction.Builder)((Object)SetCountLootFunction.builder(ConstantLootNumberProvider.create(2.0f)).conditionally(BlockStatePropertyLootCondition.builder(drop).properties(StatePredicate.Builder.create().exactMatch(SlabBlock.TYPE, SlabType.DOUBLE))))))));
    }

    protected <T extends Comparable<T> & StringIdentifiable> LootTable.Builder dropsWithProperty(Block drop, Property<T> property, T value) {
        return LootTable.builder().pool(this.addSurvivesExplosionCondition(drop, LootPool.builder().rolls(ConstantLootNumberProvider.create(1.0f)).with((LootPoolEntry.Builder<?>)ItemEntry.builder(drop).conditionally(BlockStatePropertyLootCondition.builder(drop).properties(StatePredicate.Builder.create().exactMatch(property, value))))));
    }

    protected LootTable.Builder nameableContainerDrops(Block drop) {
        return LootTable.builder().pool(this.addSurvivesExplosionCondition(drop, LootPool.builder().rolls(ConstantLootNumberProvider.create(1.0f)).with((LootPoolEntry.Builder<?>)((Object)ItemEntry.builder(drop).apply(CopyComponentsLootFunction.blockEntity(LootContextParameters.BLOCK_ENTITY).include(DataComponentTypes.CUSTOM_NAME))))));
    }

    protected LootTable.Builder shulkerBoxDrops(Block drop) {
        return LootTable.builder().pool(this.addSurvivesExplosionCondition(drop, LootPool.builder().rolls(ConstantLootNumberProvider.create(1.0f)).with((LootPoolEntry.Builder<?>)((Object)ItemEntry.builder(drop).apply(CopyComponentsLootFunction.blockEntity(LootContextParameters.BLOCK_ENTITY).include(DataComponentTypes.CUSTOM_NAME).include(DataComponentTypes.CONTAINER).include(DataComponentTypes.LOCK).include(DataComponentTypes.CONTAINER_LOOT))))));
    }

    protected LootTable.Builder copperOreDrops(Block drop) {
        RegistryEntryLookup impl = this.registries.getOrThrow(RegistryKeys.ENCHANTMENT);
        return this.dropsWithSilkTouch(drop, (LootPoolEntry.Builder)this.applyExplosionDecay(drop, ((LeafEntry.Builder)ItemEntry.builder(Items.RAW_COPPER).apply(SetCountLootFunction.builder(UniformLootNumberProvider.create(2.0f, 5.0f)))).apply(ApplyBonusLootFunction.oreDrops(impl.getOrThrow(Enchantments.FORTUNE)))));
    }

    protected LootTable.Builder lapisOreDrops(Block drop) {
        RegistryEntryLookup impl = this.registries.getOrThrow(RegistryKeys.ENCHANTMENT);
        return this.dropsWithSilkTouch(drop, (LootPoolEntry.Builder)this.applyExplosionDecay(drop, ((LeafEntry.Builder)ItemEntry.builder(Items.LAPIS_LAZULI).apply(SetCountLootFunction.builder(UniformLootNumberProvider.create(4.0f, 9.0f)))).apply(ApplyBonusLootFunction.oreDrops(impl.getOrThrow(Enchantments.FORTUNE)))));
    }

    protected LootTable.Builder redstoneOreDrops(Block drop) {
        RegistryEntryLookup impl = this.registries.getOrThrow(RegistryKeys.ENCHANTMENT);
        return this.dropsWithSilkTouch(drop, (LootPoolEntry.Builder)this.applyExplosionDecay(drop, ((LeafEntry.Builder)ItemEntry.builder(Items.REDSTONE).apply(SetCountLootFunction.builder(UniformLootNumberProvider.create(4.0f, 5.0f)))).apply(ApplyBonusLootFunction.uniformBonusCount(impl.getOrThrow(Enchantments.FORTUNE)))));
    }

    protected LootTable.Builder bannerDrops(Block drop) {
        return LootTable.builder().pool(this.addSurvivesExplosionCondition(drop, LootPool.builder().rolls(ConstantLootNumberProvider.create(1.0f)).with((LootPoolEntry.Builder<?>)((Object)ItemEntry.builder(drop).apply(CopyComponentsLootFunction.blockEntity(LootContextParameters.BLOCK_ENTITY).include(DataComponentTypes.CUSTOM_NAME).include(DataComponentTypes.ITEM_NAME).include(DataComponentTypes.TOOLTIP_DISPLAY).include(DataComponentTypes.BANNER_PATTERNS).include(DataComponentTypes.RARITY))))));
    }

    protected LootTable.Builder beeNestDrops(Block drop) {
        return LootTable.builder().pool(LootPool.builder().conditionally(this.createSilkTouchCondition()).rolls(ConstantLootNumberProvider.create(1.0f)).with((LootPoolEntry.Builder<?>)((Object)((LeafEntry.Builder)ItemEntry.builder(drop).apply(CopyComponentsLootFunction.blockEntity(LootContextParameters.BLOCK_ENTITY).include(DataComponentTypes.BEES))).apply(CopyStateLootFunction.builder(drop).addProperty(BeehiveBlock.HONEY_LEVEL)))));
    }

    protected LootTable.Builder beehiveDrops(Block drop) {
        return LootTable.builder().pool(LootPool.builder().rolls(ConstantLootNumberProvider.create(1.0f)).with(((LootPoolEntry.Builder)((Object)((LeafEntry.Builder)((LeafEntry.Builder)ItemEntry.builder(drop).conditionally(this.createSilkTouchCondition())).apply(CopyComponentsLootFunction.blockEntity(LootContextParameters.BLOCK_ENTITY).include(DataComponentTypes.BEES))).apply(CopyStateLootFunction.builder(drop).addProperty(BeehiveBlock.HONEY_LEVEL)))).alternatively(ItemEntry.builder(drop))));
    }

    protected LootTable.Builder glowBerryDrops(Block drop) {
        return LootTable.builder().pool(LootPool.builder().with(ItemEntry.builder(Items.GLOW_BERRIES)).conditionally(BlockStatePropertyLootCondition.builder(drop).properties(StatePredicate.Builder.create().exactMatch(CaveVines.BERRIES, true))));
    }

    protected LootTable.Builder copperGolemStatueDrops(Block drop) {
        return LootTable.builder().pool(this.addSurvivesExplosionCondition(drop, LootPool.builder().rolls(ConstantLootNumberProvider.create(1.0f)).with((LootPoolEntry.Builder<?>)((Object)((LeafEntry.Builder)ItemEntry.builder(drop).apply(CopyComponentsLootFunction.blockEntity(LootContextParameters.BLOCK_ENTITY).include(DataComponentTypes.CUSTOM_NAME))).apply(CopyStateLootFunction.builder(drop).addProperty(CopperGolemStatueBlock.POSE))))));
    }

    protected LootTable.Builder oreDrops(Block withSilkTouch, Item withoutSilkTouch) {
        RegistryEntryLookup impl = this.registries.getOrThrow(RegistryKeys.ENCHANTMENT);
        return this.dropsWithSilkTouch(withSilkTouch, (LootPoolEntry.Builder)this.applyExplosionDecay(withSilkTouch, ItemEntry.builder(withoutSilkTouch).apply(ApplyBonusLootFunction.oreDrops(impl.getOrThrow(Enchantments.FORTUNE)))));
    }

    protected LootTable.Builder mushroomBlockDrops(Block withSilkTouch, ItemConvertible withoutSilkTouch) {
        return this.dropsWithSilkTouch(withSilkTouch, (LootPoolEntry.Builder)this.applyExplosionDecay(withSilkTouch, ((LeafEntry.Builder)ItemEntry.builder(withoutSilkTouch).apply(SetCountLootFunction.builder(UniformLootNumberProvider.create(-6.0f, 2.0f)))).apply(LimitCountLootFunction.builder(BoundedIntUnaryOperator.createMin(0)))));
    }

    protected LootTable.Builder shortPlantDrops(Block withShears) {
        RegistryEntryLookup impl = this.registries.getOrThrow(RegistryKeys.ENCHANTMENT);
        return this.dropsWithShears(withShears, (LootPoolEntry.Builder)this.applyExplosionDecay(withShears, ((LeafEntry.Builder)ItemEntry.builder(Items.WHEAT_SEEDS).conditionally(RandomChanceLootCondition.builder(0.125f))).apply(ApplyBonusLootFunction.uniformBonusCount(impl.getOrThrow(Enchantments.FORTUNE), 2))));
    }

    public LootTable.Builder cropStemDrops(Block stem, Item drop) {
        return LootTable.builder().pool(this.applyExplosionDecay(stem, LootPool.builder().rolls(ConstantLootNumberProvider.create(1.0f)).with((LootPoolEntry.Builder)ItemEntry.builder(drop).apply(StemBlock.AGE.getValues(), age -> SetCountLootFunction.builder(BinomialLootNumberProvider.create(3, (float)(age + 1) / 15.0f)).conditionally(BlockStatePropertyLootCondition.builder(stem).properties(StatePredicate.Builder.create().exactMatch(StemBlock.AGE, age.intValue())))))));
    }

    public LootTable.Builder attachedCropStemDrops(Block stem, Item drop) {
        return LootTable.builder().pool(this.applyExplosionDecay(stem, LootPool.builder().rolls(ConstantLootNumberProvider.create(1.0f)).with((LootPoolEntry.Builder<?>)((Object)ItemEntry.builder(drop).apply(SetCountLootFunction.builder(BinomialLootNumberProvider.create(3, 0.53333336f)))))));
    }

    protected LootTable.Builder dropsWithShears(ItemConvertible item) {
        return LootTable.builder().pool(LootPool.builder().rolls(ConstantLootNumberProvider.create(1.0f)).conditionally(this.createWithShearsCondition()).with(ItemEntry.builder(item)));
    }

    protected LootTable.Builder dropsWithSilkTouchOrShears(ItemConvertible item) {
        return LootTable.builder().pool(LootPool.builder().rolls(ConstantLootNumberProvider.create(1.0f)).conditionally(this.createWithSilkTouchOrShearsCondition()).with(ItemEntry.builder(item)));
    }

    protected LootTable.Builder multifaceGrowthDrops(Block drop, LootCondition.Builder condition) {
        return LootTable.builder().pool(LootPool.builder().with((LootPoolEntry.Builder)this.applyExplosionDecay(drop, ((LeafEntry.Builder)((LeafEntry.Builder)ItemEntry.builder(drop).conditionally(condition)).apply(Direction.values(), direction -> SetCountLootFunction.builder(ConstantLootNumberProvider.create(1.0f), true).conditionally(BlockStatePropertyLootCondition.builder(drop).properties(StatePredicate.Builder.create().exactMatch(MultifaceBlock.getProperty(direction), true))))).apply(SetCountLootFunction.builder(ConstantLootNumberProvider.create(-1.0f), true)))));
    }

    protected LootTable.Builder multifaceGrowthDrops(Block drop) {
        return LootTable.builder().pool(LootPool.builder().with((LootPoolEntry.Builder)this.applyExplosionDecay(drop, ((LeafEntry.Builder)ItemEntry.builder(drop).apply(Direction.values(), direction -> SetCountLootFunction.builder(ConstantLootNumberProvider.create(1.0f), true).conditionally(BlockStatePropertyLootCondition.builder(drop).properties(StatePredicate.Builder.create().exactMatch(MultifaceBlock.getProperty(direction), true))))).apply(SetCountLootFunction.builder(ConstantLootNumberProvider.create(-1.0f), true)))));
    }

    protected LootTable.Builder paleMossCarpetDrops(Block block) {
        return LootTable.builder().pool(LootPool.builder().with((LootPoolEntry.Builder)this.applyExplosionDecay(block, (LootFunctionConsumingBuilder)((Object)ItemEntry.builder(block).conditionally(BlockStatePropertyLootCondition.builder(block).properties(StatePredicate.Builder.create().exactMatch(PaleMossCarpetBlock.BOTTOM, true)))))));
    }

    protected LootTable.Builder leavesDrops(Block leaves, Block sapling, float ... saplingChance) {
        RegistryEntryLookup impl = this.registries.getOrThrow(RegistryKeys.ENCHANTMENT);
        return this.dropsWithSilkTouchOrShears(leaves, (LootPoolEntry.Builder<?>)((LeafEntry.Builder)this.addSurvivesExplosionCondition(leaves, ItemEntry.builder(sapling))).conditionally(TableBonusLootCondition.builder(impl.getOrThrow(Enchantments.FORTUNE), saplingChance))).pool(LootPool.builder().rolls(ConstantLootNumberProvider.create(1.0f)).conditionally(this.createWithoutShearsOrSilkTouchCondition()).with((LootPoolEntry.Builder<?>)((LeafEntry.Builder)this.applyExplosionDecay(leaves, ItemEntry.builder(Items.STICK).apply(SetCountLootFunction.builder(UniformLootNumberProvider.create(1.0f, 2.0f))))).conditionally(TableBonusLootCondition.builder(impl.getOrThrow(Enchantments.FORTUNE), LEAVES_STICK_DROP_CHANCE))));
    }

    protected LootTable.Builder oakLeavesDrops(Block leaves, Block sapling, float ... saplingChance) {
        RegistryEntryLookup impl = this.registries.getOrThrow(RegistryKeys.ENCHANTMENT);
        return this.leavesDrops(leaves, sapling, saplingChance).pool(LootPool.builder().rolls(ConstantLootNumberProvider.create(1.0f)).conditionally(this.createWithoutShearsOrSilkTouchCondition()).with((LootPoolEntry.Builder<?>)((LeafEntry.Builder)this.addSurvivesExplosionCondition(leaves, ItemEntry.builder(Items.APPLE))).conditionally(TableBonusLootCondition.builder(impl.getOrThrow(Enchantments.FORTUNE), 0.005f, 0.0055555557f, 0.00625f, 0.008333334f, 0.025f))));
    }

    protected LootTable.Builder mangroveLeavesDrops(Block leaves) {
        RegistryEntryLookup impl = this.registries.getOrThrow(RegistryKeys.ENCHANTMENT);
        return this.dropsWithSilkTouchOrShears(leaves, (LootPoolEntry.Builder<?>)((LeafEntry.Builder)this.applyExplosionDecay(Blocks.MANGROVE_LEAVES, ItemEntry.builder(Items.STICK).apply(SetCountLootFunction.builder(UniformLootNumberProvider.create(1.0f, 2.0f))))).conditionally(TableBonusLootCondition.builder(impl.getOrThrow(Enchantments.FORTUNE), LEAVES_STICK_DROP_CHANCE)));
    }

    protected LootTable.Builder cropDrops(Block crop, Item product, Item seeds, LootCondition.Builder condition) {
        RegistryEntryLookup impl = this.registries.getOrThrow(RegistryKeys.ENCHANTMENT);
        return this.applyExplosionDecay(crop, LootTable.builder().pool(LootPool.builder().with(((LeafEntry.Builder)ItemEntry.builder(product).conditionally(condition)).alternatively(ItemEntry.builder(seeds)))).pool(LootPool.builder().conditionally(condition).with((LootPoolEntry.Builder<?>)((Object)ItemEntry.builder(seeds).apply(ApplyBonusLootFunction.binomialWithBonusCount(impl.getOrThrow(Enchantments.FORTUNE), 0.5714286f, 3))))));
    }

    protected LootTable.Builder seagrassDrops(Block seagrass) {
        return LootTable.builder().pool(LootPool.builder().conditionally(this.createWithShearsCondition()).with((LootPoolEntry.Builder<?>)((Object)ItemEntry.builder(seagrass).apply(SetCountLootFunction.builder(ConstantLootNumberProvider.create(2.0f))))));
    }

    protected LootTable.Builder tallPlantDrops(Block tallPlant, Block shortPlant) {
        RegistryEntryLookup impl = this.registries.getOrThrow(RegistryKeys.BLOCK);
        AlternativeEntry.Builder builder = ((LeafEntry.Builder)((LootPoolEntry.Builder)((Object)ItemEntry.builder(shortPlant).apply(SetCountLootFunction.builder(ConstantLootNumberProvider.create(2.0f))))).conditionally(this.createWithShearsCondition())).alternatively((LootPoolEntry.Builder<?>)((LeafEntry.Builder)this.addSurvivesExplosionCondition(tallPlant, ItemEntry.builder(Items.WHEAT_SEEDS))).conditionally(RandomChanceLootCondition.builder(0.125f)));
        return LootTable.builder().pool(LootPool.builder().with(builder).conditionally(BlockStatePropertyLootCondition.builder(tallPlant).properties(StatePredicate.Builder.create().exactMatch(TallPlantBlock.HALF, DoubleBlockHalf.LOWER))).conditionally(LocationCheckLootCondition.builder(LocationPredicate.Builder.create().block(BlockPredicate.Builder.create().blocks((RegistryEntryLookup<Block>)impl, tallPlant).state(StatePredicate.Builder.create().exactMatch(TallPlantBlock.HALF, DoubleBlockHalf.UPPER))), new BlockPos(0, 1, 0)))).pool(LootPool.builder().with(builder).conditionally(BlockStatePropertyLootCondition.builder(tallPlant).properties(StatePredicate.Builder.create().exactMatch(TallPlantBlock.HALF, DoubleBlockHalf.UPPER))).conditionally(LocationCheckLootCondition.builder(LocationPredicate.Builder.create().block(BlockPredicate.Builder.create().blocks((RegistryEntryLookup<Block>)impl, tallPlant).state(StatePredicate.Builder.create().exactMatch(TallPlantBlock.HALF, DoubleBlockHalf.LOWER))), new BlockPos(0, -1, 0))));
    }

    protected LootTable.Builder candleDrops(Block candle) {
        return LootTable.builder().pool(LootPool.builder().rolls(ConstantLootNumberProvider.create(1.0f)).with((LootPoolEntry.Builder)this.applyExplosionDecay(candle, (LootFunctionConsumingBuilder)ItemEntry.builder(candle).apply(List.of(Integer.valueOf(2), Integer.valueOf(3), Integer.valueOf(4)), candles -> SetCountLootFunction.builder(ConstantLootNumberProvider.create(candles.intValue())).conditionally(BlockStatePropertyLootCondition.builder(candle).properties(StatePredicate.Builder.create().exactMatch(CandleBlock.CANDLES, candles.intValue())))))));
    }

    public LootTable.Builder segmentedDrops(Block segmented) {
        if (segmented instanceof Segmented) {
            Segmented segmented2 = (Segmented)((Object)segmented);
            return LootTable.builder().pool(LootPool.builder().rolls(ConstantLootNumberProvider.create(1.0f)).with((LootPoolEntry.Builder)this.applyExplosionDecay(segmented, (LootFunctionConsumingBuilder)ItemEntry.builder(segmented).apply(IntStream.rangeClosed(1, 4).boxed().toList(), count -> SetCountLootFunction.builder(ConstantLootNumberProvider.create(count.intValue())).conditionally(BlockStatePropertyLootCondition.builder(segmented).properties(StatePredicate.Builder.create().exactMatch(segmented2.getAmountProperty(), count.intValue())))))));
        }
        return BlockLootTableGenerator.dropsNothing();
    }

    protected static LootTable.Builder candleCakeDrops(Block candleCake) {
        return LootTable.builder().pool(LootPool.builder().rolls(ConstantLootNumberProvider.create(1.0f)).with(ItemEntry.builder(candleCake)));
    }

    public static LootTable.Builder dropsNothing() {
        return LootTable.builder();
    }

    protected abstract void generate();

    @Override
    public void accept(BiConsumer<RegistryKey<LootTable>, LootTable.Builder> lootTableBiConsumer) {
        this.generate();
        HashSet set = new HashSet();
        for (Block block : Registries.BLOCK) {
            if (!block.isEnabled(this.requiredFeatures)) continue;
            block.getLootTableKey().ifPresent(lootTableKey -> {
                if (set.add(lootTableKey)) {
                    LootTable.Builder builder = this.lootTables.remove(lootTableKey);
                    if (builder == null) {
                        throw new IllegalStateException(String.format(Locale.ROOT, "Missing loottable '%s' for '%s'", lootTableKey.getValue(), Registries.BLOCK.getId(block)));
                    }
                    lootTableBiConsumer.accept((RegistryKey<LootTable>)lootTableKey, builder);
                }
            });
        }
        if (!this.lootTables.isEmpty()) {
            throw new IllegalStateException("Created block loot tables for non-blocks: " + String.valueOf(this.lootTables.keySet()));
        }
    }

    protected void addVinePlantDrop(Block vine, Block vinePlant) {
        RegistryEntryLookup impl = this.registries.getOrThrow(RegistryKeys.ENCHANTMENT);
        LootTable.Builder builder = this.dropsWithSilkTouchOrShears(vine, (LootPoolEntry.Builder<?>)ItemEntry.builder(vine).conditionally(TableBonusLootCondition.builder(impl.getOrThrow(Enchantments.FORTUNE), 0.33f, 0.55f, 0.77f, 1.0f)));
        this.addDrop(vine, builder);
        this.addDrop(vinePlant, builder);
    }

    protected LootTable.Builder doorDrops(Block block) {
        return this.dropsWithProperty(block, DoorBlock.HALF, DoubleBlockHalf.LOWER);
    }

    protected void addPottedPlantDrops(Block block) {
        this.addDrop(block, (Block flowerPot) -> this.pottedPlantDrops(((FlowerPotBlock)flowerPot).getContent()));
    }

    protected void addDropWithSilkTouch(Block block, Block drop) {
        this.addDrop(block, this.dropsWithSilkTouch(drop));
    }

    protected void addDrop(Block block, ItemConvertible drop) {
        this.addDrop(block, this.drops(drop));
    }

    protected void addDropWithSilkTouch(Block block) {
        this.addDropWithSilkTouch(block, block);
    }

    protected void addDrop(Block block) {
        this.addDrop(block, block);
    }

    protected void addDrop(Block block, Function<Block, LootTable.Builder> lootTableFunction) {
        this.addDrop(block, lootTableFunction.apply(block));
    }

    protected void addDrop(Block block, LootTable.Builder lootTable) {
        this.lootTables.put(block.getLootTableKey().orElseThrow(() -> new IllegalStateException("Block " + String.valueOf(block) + " does not have loot table")), lootTable);
    }
}


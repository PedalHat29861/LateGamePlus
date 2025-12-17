/*
 * Decompiled with CFR 0.152.
 */
package net.minecraft.data.loottable.vanilla;

import java.util.function.BiConsumer;
import net.minecraft.block.Blocks;
import net.minecraft.block.SweetBerryBushBlock;
import net.minecraft.data.loottable.LootTableGenerator;
import net.minecraft.item.Items;
import net.minecraft.loot.LootPool;
import net.minecraft.loot.LootTable;
import net.minecraft.loot.LootTables;
import net.minecraft.loot.condition.BlockStatePropertyLootCondition;
import net.minecraft.loot.entry.ItemEntry;
import net.minecraft.loot.entry.LootPoolEntry;
import net.minecraft.loot.function.SetCountLootFunction;
import net.minecraft.loot.provider.number.ConstantLootNumberProvider;
import net.minecraft.loot.provider.number.UniformLootNumberProvider;
import net.minecraft.predicate.StatePredicate;
import net.minecraft.registry.RegistryKey;
import net.minecraft.registry.RegistryWrapper;

public record VanillaHarvestLootTableGenerator(RegistryWrapper.WrapperLookup registries) implements LootTableGenerator
{
    @Override
    public void accept(BiConsumer<RegistryKey<LootTable>, LootTable.Builder> lootTableBiConsumer) {
        lootTableBiConsumer.accept(LootTables.BEEHIVE_HARVEST, LootTable.builder().pool(LootPool.builder().rolls(ConstantLootNumberProvider.create(1.0f)).with((LootPoolEntry.Builder<?>)((Object)ItemEntry.builder(Items.HONEYCOMB).apply(SetCountLootFunction.builder(ConstantLootNumberProvider.create(3.0f)))))));
        lootTableBiConsumer.accept(LootTables.CAVE_VINE_HARVEST, LootTable.builder().pool(LootPool.builder().rolls(ConstantLootNumberProvider.create(1.0f)).with(ItemEntry.builder(Items.GLOW_BERRIES))));
        lootTableBiConsumer.accept(LootTables.SWEET_BERRY_BUSH_HARVEST, LootTable.builder().pool(LootPool.builder().with((LootPoolEntry.Builder<?>)((LootPoolEntry.Builder)((Object)ItemEntry.builder(Items.SWEET_BERRIES).apply(SetCountLootFunction.builder(ConstantLootNumberProvider.create(1.0f))))).conditionally(BlockStatePropertyLootCondition.builder(Blocks.SWEET_BERRY_BUSH).properties(StatePredicate.Builder.create().exactMatch(SweetBerryBushBlock.AGE, 3))))).pool(LootPool.builder().with((LootPoolEntry.Builder<?>)((Object)ItemEntry.builder(Items.SWEET_BERRIES).apply(SetCountLootFunction.builder(UniformLootNumberProvider.create(1.0f, 2.0f)))))));
        lootTableBiConsumer.accept(LootTables.PUMPKIN_CARVE, LootTable.builder().pool(LootPool.builder().rolls(ConstantLootNumberProvider.create(1.0f)).with((LootPoolEntry.Builder<?>)((Object)ItemEntry.builder(Items.PUMPKIN_SEEDS).apply(SetCountLootFunction.builder(ConstantLootNumberProvider.create(4.0f)))))));
    }
}


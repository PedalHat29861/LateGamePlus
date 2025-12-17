/*
 * Decompiled with CFR 0.152.
 */
package net.minecraft.data.loottable.vanilla;

import java.util.function.BiConsumer;
import net.minecraft.data.loottable.LootTableGenerator;
import net.minecraft.item.Items;
import net.minecraft.loot.LootPool;
import net.minecraft.loot.LootTable;
import net.minecraft.loot.LootTables;
import net.minecraft.loot.entry.ItemEntry;
import net.minecraft.loot.provider.number.ConstantLootNumberProvider;
import net.minecraft.registry.RegistryKey;
import net.minecraft.registry.RegistryWrapper;

public record VanillaBrushLootTableGenerator(RegistryWrapper.WrapperLookup registries) implements LootTableGenerator
{
    @Override
    public void accept(BiConsumer<RegistryKey<LootTable>, LootTable.Builder> lootTableBiConsumer) {
        lootTableBiConsumer.accept(LootTables.ARMADILLO_BRUSH, LootTable.builder().pool(LootPool.builder().rolls(ConstantLootNumberProvider.create(1.0f)).with(ItemEntry.builder(Items.ARMADILLO_SCUTE))));
    }
}


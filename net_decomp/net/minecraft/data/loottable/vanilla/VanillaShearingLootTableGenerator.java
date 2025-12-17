/*
 * Decompiled with CFR 0.152.
 */
package net.minecraft.data.loottable.vanilla;

import java.util.function.BiConsumer;
import net.minecraft.component.DataComponentTypes;
import net.minecraft.data.loottable.EntityLootTableGenerator;
import net.minecraft.data.loottable.LootTableData;
import net.minecraft.data.loottable.LootTableGenerator;
import net.minecraft.entity.passive.MooshroomEntity;
import net.minecraft.item.Items;
import net.minecraft.loot.LootPool;
import net.minecraft.loot.LootTable;
import net.minecraft.loot.LootTables;
import net.minecraft.loot.condition.EntityPropertiesLootCondition;
import net.minecraft.loot.context.LootContext;
import net.minecraft.loot.entry.AlternativeEntry;
import net.minecraft.loot.entry.ItemEntry;
import net.minecraft.loot.entry.LootPoolEntry;
import net.minecraft.loot.entry.LootTableEntry;
import net.minecraft.loot.function.SetCountLootFunction;
import net.minecraft.loot.provider.number.ConstantLootNumberProvider;
import net.minecraft.loot.provider.number.UniformLootNumberProvider;
import net.minecraft.predicate.component.ComponentMapPredicate;
import net.minecraft.predicate.component.ComponentsPredicate;
import net.minecraft.predicate.entity.EntityPredicate;
import net.minecraft.registry.RegistryKey;
import net.minecraft.registry.RegistryWrapper;

public record VanillaShearingLootTableGenerator(RegistryWrapper.WrapperLookup registries) implements LootTableGenerator
{
    @Override
    public void accept(BiConsumer<RegistryKey<LootTable>, LootTable.Builder> lootTableBiConsumer) {
        lootTableBiConsumer.accept(LootTables.BOGGED_SHEARING, LootTable.builder().pool(LootPool.builder().rolls(ConstantLootNumberProvider.create(2.0f)).with((LootPoolEntry.Builder<?>)((Object)ItemEntry.builder(Items.BROWN_MUSHROOM).apply(SetCountLootFunction.builder(ConstantLootNumberProvider.create(1.0f))))).with((LootPoolEntry.Builder<?>)((Object)ItemEntry.builder(Items.RED_MUSHROOM).apply(SetCountLootFunction.builder(ConstantLootNumberProvider.create(1.0f)))))));
        LootTableData.WOOL_FROM_DYE_COLOR.forEach((color, wool) -> lootTableBiConsumer.accept(LootTables.SHEEP_SHEARING_FROM_DYE_COLOR.get(color), LootTable.builder().pool(LootPool.builder().rolls(UniformLootNumberProvider.create(1.0f, 3.0f)).with(ItemEntry.builder(wool)))));
        lootTableBiConsumer.accept(LootTables.SHEEP_SHEARING, LootTable.builder().pool(EntityLootTableGenerator.createForSheep(LootTables.SHEEP_SHEARING_FROM_DYE_COLOR)));
        lootTableBiConsumer.accept(LootTables.MOOSHROOM_SHEARING, LootTable.builder().pool(LootPool.builder().with(AlternativeEntry.builder(new LootPoolEntry.Builder[]{LootTableEntry.builder(LootTables.MOOSHROOM_RED_SHEARING).conditionally(EntityPropertiesLootCondition.builder(LootContext.EntityReference.THIS, EntityPredicate.Builder.create().components(ComponentsPredicate.Builder.create().exact(ComponentMapPredicate.of(DataComponentTypes.MOOSHROOM_VARIANT, MooshroomEntity.Variant.RED)).build()))), LootTableEntry.builder(LootTables.MOOSHROOM_BROWN_SHEARING).conditionally(EntityPropertiesLootCondition.builder(LootContext.EntityReference.THIS, EntityPredicate.Builder.create().components(ComponentsPredicate.Builder.create().exact(ComponentMapPredicate.of(DataComponentTypes.MOOSHROOM_VARIANT, MooshroomEntity.Variant.BROWN)).build())))}))));
        lootTableBiConsumer.accept(LootTables.MOOSHROOM_RED_SHEARING, LootTable.builder().pool(LootPool.builder().rolls(ConstantLootNumberProvider.create(5.0f)).with(ItemEntry.builder(Items.RED_MUSHROOM))));
        lootTableBiConsumer.accept(LootTables.MOOSHROOM_BROWN_SHEARING, LootTable.builder().pool(LootPool.builder().rolls(ConstantLootNumberProvider.create(5.0f)).with(ItemEntry.builder(Items.BROWN_MUSHROOM))));
        lootTableBiConsumer.accept(LootTables.SNOW_GOLEM_SHEARING, LootTable.builder().pool(LootPool.builder().rolls(ConstantLootNumberProvider.create(1.0f)).with(ItemEntry.builder(Items.CARVED_PUMPKIN))));
    }
}


package com.pedalhat.lategameplus.event;

import com.pedalhat.lategameplus.registry.ModItems;

import net.fabricmc.fabric.api.loot.v3.LootTableEvents;
import net.fabricmc.fabric.api.loot.v3.LootTableSource;

import net.minecraft.entity.EntityType;
import net.minecraft.loot.LootPool;
import net.minecraft.loot.LootTable;
import net.minecraft.loot.condition.RandomChanceLootCondition;
import net.minecraft.loot.entry.ItemEntry;
import net.minecraft.loot.function.SetCountLootFunction;
import net.minecraft.loot.provider.number.UniformLootNumberProvider;
import net.minecraft.registry.RegistryKey;
import net.minecraft.registry.RegistryWrapper;

public final class ModEvents {
    public static void register() {
        // v3: (RegistryKey<LootTable> key, LootTable.Builder table, LootTableSource source, WrapperLookup registries)
        LootTableEvents.MODIFY.register((RegistryKey<LootTable> key,
                                         LootTable.Builder table,
                                         LootTableSource source,
                                         RegistryWrapper.WrapperLookup registries) -> {
            if (!source.isBuiltin()) return; // no tocar tablas de otros mods

            var bruteKey = EntityType.PIGLIN_BRUTE.getLootTableKey().orElse(null);
            if (bruteKey != null && key.equals(bruteKey)) {
                table.pool(
                    LootPool.builder()
                        .conditionally(RandomChanceLootCondition.builder(0.33f))
                        .with(ItemEntry.builder(ModItems.NETHERITE_NUGGET))
                        .apply(SetCountLootFunction.builder(
                            UniformLootNumberProvider.create(1.0f, 3.0f) // 1–3 nuggets
                        ))
                );
            }
        });
    }
}

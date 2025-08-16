package com.pedalhat.lategameplus;

import com.pedalhat.lategameplus.registry.ModItems;
import net.fabricmc.api.ModInitializer;
import net.fabricmc.fabric.api.itemgroup.v1.ItemGroupEvents;
import net.fabricmc.fabric.api.loot.v2.LootTableEvents;
import net.minecraft.entity.EntityType;
import net.minecraft.item.ItemGroups;
import net.minecraft.item.Items;
import net.minecraft.loot.LootPool;
import net.minecraft.loot.condition.RandomChanceLootCondition;
import net.minecraft.loot.entry.ItemEntry;
import net.minecraft.loot.function.SetCountLootFunction;
import net.minecraft.loot.provider.number.UniformLootNumberProvider;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class LateGamePlus implements ModInitializer {
    public static final String MOD_ID = "lategameplus";
    public static final Logger LOGGER = LoggerFactory.getLogger(MOD_ID);

    @Override
    public void onInitialize() {
        LOGGER.info("Hello Fabric world! Late Game Plus is initializing...");
        ModItems.init();

        ItemGroupEvents.modifyEntriesEvent(ItemGroups.INGREDIENTS).register(entries -> {
            entries.addAfter(Items.NETHERITE_INGOT, ModItems.NETHERITE_NUGGET);
        });

        LootTableEvents.MODIFY.register((id, tableBuilder, source) -> {
            if (id.equals(EntityType.PIGLIN_BRUTE.getLootTableKey().orElseThrow())) {
                LootPool.Builder pool = LootPool.builder()
                        .conditionally(RandomChanceLootCondition.builder(0.125f))
                        .with(ItemEntry.builder(ModItems.NETHERITE_NUGGET))
                        .apply(SetCountLootFunction.builder(UniformLootNumberProvider.create(1.0f, 2.0f)));
                tableBuilder.pool(pool);
            }
        });
    }
}
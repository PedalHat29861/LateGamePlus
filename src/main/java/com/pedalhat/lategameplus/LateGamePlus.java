package com.pedalhat.lategameplus;

import com.pedalhat.lategameplus.event.ModEvents;
import com.pedalhat.lategameplus.registry.ModItems;
import net.fabricmc.api.ModInitializer;
import net.fabricmc.fabric.api.itemgroup.v1.ItemGroupEvents;
import net.minecraft.item.ItemGroups;
import net.minecraft.item.Items;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class LateGamePlus implements ModInitializer {
    public static final String MOD_ID = "lategameplus";
    public static final Logger LOGGER = LoggerFactory.getLogger(MOD_ID);

    @Override
    public void onInitialize() {
        LOGGER.info("Hello Fabric world! Late Game Plus is initializing...");
        ModItems.init();

    ItemGroupEvents.modifyEntriesEvent(ItemGroups.INGREDIENTS)
        .register(e -> e.addAfter(Items.NETHERITE_INGOT, ModItems.NETHERITE_NUGGET));
    ItemGroupEvents.modifyEntriesEvent(ItemGroups.FOOD_AND_DRINK)
        .register(e -> e.addAfter(Items.GOLDEN_APPLE, ModItems.NETHERITE_APPLE));
    ItemGroupEvents.modifyEntriesEvent(ItemGroups.FOOD_AND_DRINK)
        .register(e -> e.addAfter(Items.ENCHANTED_GOLDEN_APPLE, ModItems.ENCHANTED_NETHERITE_APPLE));
    ModEvents.register();
    }
}
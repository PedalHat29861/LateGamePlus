package com.pedalhat.lategameplus;

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

        ItemGroupEvents.modifyEntriesEvent(ItemGroups.INGREDIENTS).register(entries -> {
            entries.addAfter(Items.NETHERITE_INGOT, ModItems.NETHERITE_NUGGET);
        });
    }
}
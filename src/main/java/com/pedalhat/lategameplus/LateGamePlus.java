package com.pedalhat.lategameplus;

import com.pedalhat.lategameplus.command.ModCommands;
import com.pedalhat.lategameplus.config.ConfigManager;
import com.pedalhat.lategameplus.config.ModConfig;
import com.pedalhat.lategameplus.event.ModEvents;
import com.pedalhat.lategameplus.item.DebrisResonatorItem;
import com.pedalhat.lategameplus.recipe.ModRecipes;
import com.pedalhat.lategameplus.registry.ModBlocks;
import com.pedalhat.lategameplus.registry.ModBlockEntities;
import com.pedalhat.lategameplus.registry.ModItemGroups;
import com.pedalhat.lategameplus.registry.ModItems;
import com.pedalhat.lategameplus.registry.ModEffects;
import com.pedalhat.lategameplus.registry.ModPotions;
import com.pedalhat.lategameplus.registry.ModScreenHandlers;

import net.fabricmc.api.ModInitializer;
import net.minecraft.block.cauldron.CauldronBehavior;
import net.fabricmc.fabric.api.event.lifecycle.v1.ServerLifecycleEvents;
import net.fabricmc.fabric.api.itemgroup.v1.ItemGroupEvents;
import net.fabricmc.fabric.api.recipe.v1.sync.RecipeSynchronization;
import net.minecraft.item.ItemGroups;
import net.minecraft.item.Items;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;


public class LateGamePlus implements ModInitializer {
    public static final String MOD_ID = "lategameplus";
    public static final Logger LOGGER = LoggerFactory.getLogger(MOD_ID);

    @Override
    public void onInitialize() {
        ConfigManager.load();
        ModConfig cfg = ConfigManager.get();
        LOGGER.info("Hello Fabric world! Late Game Plus is initializing...");
        ModItems.init(cfg);
        ModBlocks.init();
        ModBlockEntities.init();
        ModScreenHandlers.init();
        ModItemGroups.init();
        ModEffects.init();
        ModPotions.init();
        ModEvents.register(cfg);
        ModCommands.register();
        ModRecipes.init();
        RecipeSynchronization.synchronizeRecipeSerializer(ModRecipes.FUSION_FORGE_SERIALIZER);
        DebrisResonatorItem.DebrisResonatorHooks.init();

        var wolfArmorCleaning = CauldronBehavior.WATER_CAULDRON_BEHAVIOR.map().get(Items.WOLF_ARMOR);
        if (wolfArmorCleaning != null) {
            CauldronBehavior.WATER_CAULDRON_BEHAVIOR.map()
                .put(ModItems.NETHERITE_WOLF_ARMOR, wolfArmorCleaning);
        }

        LOGGER.info("Initialization complete, have fun!");

        ItemGroupEvents.modifyEntriesEvent(ItemGroups.INGREDIENTS)
                .register(e -> e.addAfter(Items.NETHERITE_INGOT, ModItems.NETHERITE_NUGGET));
        ItemGroupEvents.modifyEntriesEvent(ItemGroups.FOOD_AND_DRINK)
                .register(e -> e.addAfter(Items.GOLDEN_APPLE, ModItems.NETHERITE_APPLE));
        ItemGroupEvents.modifyEntriesEvent(ItemGroups.FOOD_AND_DRINK)
                .register(e -> e.addAfter(Items.ENCHANTED_GOLDEN_APPLE, ModItems.ENCHANTED_NETHERITE_APPLE));
        ItemGroupEvents.modifyEntriesEvent(ItemGroups.COMBAT)
                .register(e -> e.addAfter(Items.TOTEM_OF_UNDYING, ModItems.TOTEM_OF_NETHERDYING));
        ItemGroupEvents.modifyEntriesEvent(ItemGroups.COMBAT)
                .register(e -> e.addAfter(Items.BOW, ModItems.NETHERITE_BOW));
        ItemGroupEvents.modifyEntriesEvent(ItemGroups.COMBAT)
                .register(e -> e.addAfter(Items.CROSSBOW, ModItems.NETHERITE_CROSSBOW));
        ItemGroupEvents.modifyEntriesEvent(ItemGroups.COMBAT)
                .register(e -> e.addAfter(Items.WOLF_ARMOR, ModItems.NETHERITE_WOLF_ARMOR));
        ItemGroupEvents.modifyEntriesEvent(ItemGroups.TOOLS)
                .register(e -> e.addAfter(Items.ELYTRA, ModItems.NETHERITE_ELYTRA));
        ItemGroupEvents.modifyEntriesEvent(ItemGroups.TOOLS)
                .register(e -> e.addAfter(Items.FISHING_ROD, ModItems.NETHERITE_FISHING_ROD));
        ItemGroupEvents.modifyEntriesEvent(ItemGroups.TOOLS)
                .register(e -> e.addAfter(Items.PINK_HARNESS, ModItems.orderedNetheriteHarnesses()));
        ItemGroupEvents.modifyEntriesEvent(ItemGroups.TOOLS)
                .register(e -> e.addAfter(Items.COMPASS, ModItems.LODESTONE_WARP));
        ItemGroupEvents.modifyEntriesEvent(ItemGroups.FUNCTIONAL)
                .register(e -> e.addAfter(Items.ANVIL,ModBlocks.NETHERITE_ANVIL.asItem()));
        ItemGroupEvents.modifyEntriesEvent(ItemGroups.FUNCTIONAL)
                .register(e -> e.addAfter(Items.DAMAGED_ANVIL,ModBlocks.FUSION_FORGE.asItem()));
        ItemGroupEvents.modifyEntriesEvent(ItemGroups.TOOLS)
                .register(e -> e.addAfter(ModItems.LODESTONE_WARP, ModItems.DEBRIS_RESONATOR));
        ItemGroupEvents.modifyEntriesEvent(ItemGroups.FOOD_AND_DRINK)
                .register(e -> e.addAfter(ModItems.ENCHANTED_NETHERITE_APPLE, ModItems.POMPEII_WORM));
        
        ServerLifecycleEvents.SERVER_STOPPING.register(server -> {
                DebrisResonatorItem.DebrisResonatorHooks.shutdown();
        });
    }
}

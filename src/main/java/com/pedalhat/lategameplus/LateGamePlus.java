package com.pedalhat.lategameplus;

import com.pedalhat.lategameplus.command.ModCommands;
import com.pedalhat.lategameplus.config.ConfigManager;
import com.pedalhat.lategameplus.config.ModConfig;
import com.pedalhat.lategameplus.event.ModEvents;
import com.pedalhat.lategameplus.item.DebrisResonatorItem;
import com.pedalhat.lategameplus.mixin.CauldronInteractionDispatcherAccessor;
import com.pedalhat.lategameplus.recipe.ModRecipes;
import com.pedalhat.lategameplus.registry.ModBlocks;
import com.pedalhat.lategameplus.registry.ModBlockEntities;
import com.pedalhat.lategameplus.registry.ModItemGroups;
import com.pedalhat.lategameplus.registry.ModItems;
import com.pedalhat.lategameplus.registry.ModEffects;
import com.pedalhat.lategameplus.registry.ModPotions;
import com.pedalhat.lategameplus.registry.ModScreenHandlers;

import net.fabricmc.api.ModInitializer;
import net.fabricmc.fabric.api.creativetab.v1.CreativeModeTabEvents;
import net.fabricmc.fabric.api.event.lifecycle.v1.ServerLifecycleEvents;
import net.fabricmc.fabric.api.recipe.v1.sync.RecipeSynchronization;
import net.minecraft.core.cauldron.CauldronInteraction;
import net.minecraft.core.cauldron.CauldronInteractions;
import net.minecraft.world.item.CreativeModeTabs;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
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
        ModEffects.init();
        ModBlocks.init();
        ModBlockEntities.init();
        ModScreenHandlers.init();
        ModItemGroups.init();
        ModPotions.init();
        ModEvents.register(cfg);
        ModCommands.register();
        ModRecipes.init();
        ModItems.init(cfg);
        RecipeSynchronization.synchronizeRecipeSerializer(ModRecipes.FUSION_FORGE_SERIALIZER);
        DebrisResonatorItem.DebrisResonatorHooks.init();

        LOGGER.info("Initialization complete, have fun!");

        CreativeModeTabEvents.modifyOutputEvent(CreativeModeTabs.INGREDIENTS)
                .register(output -> output.insertAfter(Items.NETHERITE_INGOT, ModItems.NETHERITE_NUGGET));
        CreativeModeTabEvents.modifyOutputEvent(CreativeModeTabs.FOOD_AND_DRINKS)
                .register(output -> output.insertAfter(Items.GOLDEN_APPLE, ModItems.NETHERITE_APPLE));
        CreativeModeTabEvents.modifyOutputEvent(CreativeModeTabs.FOOD_AND_DRINKS)
                .register(output -> output.insertAfter(Items.ENCHANTED_GOLDEN_APPLE, ModItems.ENCHANTED_NETHERITE_APPLE));
        CreativeModeTabEvents.modifyOutputEvent(CreativeModeTabs.COMBAT)
                .register(output -> output.insertAfter(Items.TOTEM_OF_UNDYING, ModItems.TOTEM_OF_NETHERDYING));
        CreativeModeTabEvents.modifyOutputEvent(CreativeModeTabs.COMBAT)
                .register(output -> output.insertAfter(Items.BOW, ModItems.NETHERITE_BOW));
        CreativeModeTabEvents.modifyOutputEvent(CreativeModeTabs.COMBAT)
                .register(output -> output.insertAfter(Items.CROSSBOW, ModItems.NETHERITE_CROSSBOW));
        CreativeModeTabEvents.modifyOutputEvent(CreativeModeTabs.COMBAT)
                .register(output -> output.insertAfter(Items.WOLF_ARMOR, ModItems.NETHERITE_WOLF_ARMOR));
        CreativeModeTabEvents.modifyOutputEvent(CreativeModeTabs.TOOLS_AND_UTILITIES)
                .register(output -> output.insertAfter(Items.ELYTRA, ModItems.NETHERITE_ELYTRA));
        CreativeModeTabEvents.modifyOutputEvent(CreativeModeTabs.TOOLS_AND_UTILITIES)
                .register(output -> output.insertAfter(Items.FISHING_ROD, ModItems.NETHERITE_FISHING_ROD));
        CreativeModeTabEvents.modifyOutputEvent(CreativeModeTabs.TOOLS_AND_UTILITIES)
                .register(output -> output.insertAfter(Items.PINK_HARNESS, ModItems.orderedNetheriteHarnesses()));
        CreativeModeTabEvents.modifyOutputEvent(CreativeModeTabs.TOOLS_AND_UTILITIES)
                .register(output -> output.insertAfter(Items.COMPASS, ModItems.LODESTONE_WARP));
        CreativeModeTabEvents.modifyOutputEvent(CreativeModeTabs.FUNCTIONAL_BLOCKS)
                .register(output -> output.insertAfter(Items.ANVIL, ModBlocks.NETHERITE_ANVIL.asItem()));
        CreativeModeTabEvents.modifyOutputEvent(CreativeModeTabs.FUNCTIONAL_BLOCKS)
                .register(output -> output.insertAfter(Items.DAMAGED_ANVIL, ModBlocks.FUSION_FORGE.asItem()));
        CreativeModeTabEvents.modifyOutputEvent(CreativeModeTabs.TOOLS_AND_UTILITIES)
                .register(output -> output.insertAfter(ModItems.LODESTONE_WARP, ModItems.DEBRIS_RESONATOR));
        CreativeModeTabEvents.modifyOutputEvent(CreativeModeTabs.FOOD_AND_DRINKS)
                .register(output -> output.insertAfter(ModItems.ENCHANTED_NETHERITE_APPLE, ModItems.POMPEII_WORM));
        CreativeModeTabEvents.modifyOutputEvent(CreativeModeTabs.FOOD_AND_DRINKS)
                .register(output -> output.insertAfter(ModItems.POMPEII_WORM, ModItems.BLIND_SHRIMP));
        CreativeModeTabEvents.modifyOutputEvent(CreativeModeTabs.INGREDIENTS)
                .register(output -> output.insertAfter(Items.BLAZE_POWDER, ModItems.VOLCANIC_CONCOCTION));
        
        ServerLifecycleEvents.SERVER_STARTED.register(server -> {
            CauldronInteraction wolfArmorCleaning = CauldronInteractions.WATER.get(new ItemStack(Items.WOLF_ARMOR));
            if (wolfArmorCleaning != null) {
                ((CauldronInteractionDispatcherAccessor) (Object) CauldronInteractions.WATER)
                    .lategameplus$put(ModItems.NETHERITE_WOLF_ARMOR, wolfArmorCleaning);
            }
        });

        ServerLifecycleEvents.SERVER_STOPPING.register(server -> {
            DebrisResonatorItem.DebrisResonatorHooks.shutdown();
        });
    }
}

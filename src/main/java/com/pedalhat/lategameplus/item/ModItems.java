package com.pedalhat.lategameplus.item;

import net.fabricmc.fabric.api.itemgroup.v1.ItemGroupEvents;
import net.minecraft.item.Item;
import net.minecraft.item.ItemGroups;
import net.minecraft.registry.Registries;
import net.minecraft.registry.Registry;
import net.minecraft.util.Identifier;

import com.pedalhat.lategameplus.LateGamePlus;

public class ModItems {
    public static final Item NETHERITE_NUGGET = registerItem("netherite_nugget", new Item(new Item.Settings()));

    private static Item registerItem(String name, Item item) {
        return Registry.register(Registries.ITEM, Identifier.of(LateGamePlus.MOD_ID, name), item);
    }

    public static void registerModItems() {
        LateGamePlus.LOGGER.info("Registering Mod Items for " + LateGamePlus.MOD_ID);
        ItemGroupEvents.modifyEntriesEvent(ItemGroups.INGREDIENTS).register(entries -> entries.add(NETHERITE_NUGGET));
    }
}

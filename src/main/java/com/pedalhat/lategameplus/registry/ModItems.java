package com.pedalhat.lategameplus.registry;

import com.pedalhat.lategameplus.LateGamePlus;
import net.minecraft.item.Item;
import net.minecraft.registry.Registries;
import net.minecraft.registry.Registry;
import net.minecraft.registry.RegistryKey;
import net.minecraft.registry.RegistryKeys;
import net.minecraft.util.Identifier;

public class ModItems {
        private static RegistryKey<Item> key(String name) {
        return RegistryKey.of(RegistryKeys.ITEM, Identifier.of(LateGamePlus.MOD_ID, name));
    }
        private static Item.Settings settings(String name) {
        return new Item.Settings().registryKey(key(name));
    }
    private static <T extends Item> T register(String name, T item) {
        return Registry.register(Registries.ITEM, key(name), item);
    }
    public static final Item NETHERITE_NUGGET = register("netherite_nugget", new Item(settings("netherite_nugget").fireproof()));


    public static void init() {
        // Force the class to load and run the static initializers
    }
}

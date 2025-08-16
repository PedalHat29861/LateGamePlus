package com.pedalhat.lategameplus.registry;

import com.pedalhat.lategameplus.LateGamePlus;
import net.minecraft.item.Item;
import net.minecraft.registry.Registries;
import net.minecraft.registry.Registry;
import net.minecraft.registry.RegistryKey;
import net.minecraft.registry.RegistryKeys;
import net.minecraft.util.Identifier;

public class ModItems {
    public static final Item NETHERITE_NUGGET = register("netherite_nugget");

    private static Item register(String name) {
        Identifier id = Identifier.of(LateGamePlus.MOD_ID, name);
        RegistryKey<Item> key = RegistryKey.of(RegistryKeys.ITEM, id);
        Item.Settings settings = new Item.Settings().useItemPrefixedTranslationKey().registryKey(key);
        return Registry.register(Registries.ITEM, key, new Item(settings));
    }

    public static void init() {
        // Force the class to load and run the static initializers
    }
}

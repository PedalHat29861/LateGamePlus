package com.pedalhat.lategameplus.registry;

import com.pedalhat.lategameplus.LateGamePlus;

import net.minecraft.component.DataComponentTypes;
import net.minecraft.component.type.ConsumableComponents;
import net.minecraft.component.type.FoodComponent;
import net.minecraft.entity.effect.StatusEffectInstance;
import net.minecraft.entity.effect.StatusEffects;
import net.minecraft.item.Item;
import net.minecraft.item.consume.ApplyEffectsConsumeEffect;
import net.minecraft.registry.Registries;
import net.minecraft.registry.Registry;
import net.minecraft.registry.RegistryKey;
import net.minecraft.registry.RegistryKeys;
import net.minecraft.util.Identifier;
import net.minecraft.util.Rarity;

public class ModItems {
    // HELPERS
        private static RegistryKey<Item> key(String name) {
        return RegistryKey.of(RegistryKeys.ITEM, Identifier.of(LateGamePlus.MOD_ID, name));
    }
        private static Item.Settings settings(String name) {
        return new Item.Settings().registryKey(key(name));
    }
    private static <T extends Item> T register(String name, T item) {
        return Registry.register(Registries.ITEM, key(name), item);
    }

    // ITEMS
    public static final Item NETHERITE_NUGGET = register("netherite_nugget", new Item(settings("netherite_nugget").fireproof()));

    public static final Item NETHERITE_APPLE = register("netherite_apple",
        new Item(settings("netherite_apple")
            .fireproof()
            .food(
                new FoodComponent.Builder()
                    .nutrition(6)
                    .saturationModifier(1.3F)
                    .alwaysEdible()
                    .build(),
                ConsumableComponents.food()
                    .consumeEffect(new ApplyEffectsConsumeEffect(new StatusEffectInstance(StatusEffects.REGENERATION, 200, 1), 1.0F))
                    .consumeEffect(new ApplyEffectsConsumeEffect(new StatusEffectInstance(StatusEffects.FIRE_RESISTANCE, 3000, 0), 1.0F))
                    .consumeEffect(new ApplyEffectsConsumeEffect(new StatusEffectInstance(StatusEffects.ABSORPTION, 2400, 2), 1.0F))
                    .consumeEffect(new ApplyEffectsConsumeEffect(new StatusEffectInstance(StatusEffects.RESISTANCE, 300, 3), 1.0F))
                    .build()
            ).rarity(Rarity.RARE)
        ));

    public static final Item ENCHANTED_NETHERITE_APPLE = register("enchanted_netherite_apple",
        new Item(settings("enchanted_netherite_apple")
            .fireproof()
            .food(
                new FoodComponent.Builder()
                    .nutrition(10)
                    .saturationModifier(1.5F)
                    .alwaysEdible()
                    .build(),
                ConsumableComponents.food()
                    .consumeEffect(new ApplyEffectsConsumeEffect(new StatusEffectInstance(StatusEffects.REGENERATION, 800, 2), 1.0F))
                    .consumeEffect(new ApplyEffectsConsumeEffect(new StatusEffectInstance(StatusEffects.RESISTANCE, 10000, 1), 1.0F))
                    .consumeEffect(new ApplyEffectsConsumeEffect(new StatusEffectInstance(StatusEffects.FIRE_RESISTANCE, 12000, 0), 1.0F))
                    .consumeEffect(new ApplyEffectsConsumeEffect(new StatusEffectInstance(StatusEffects.ABSORPTION, 4800, 5), 1.0F))
                    .consumeEffect(new ApplyEffectsConsumeEffect(new StatusEffectInstance(StatusEffects.SPEED, 300, 1), 1.0F))
                    .consumeEffect(new ApplyEffectsConsumeEffect(new StatusEffectInstance(StatusEffects.STRENGTH, 300, 1), 1.0F))
                    .build()
            ).rarity(Rarity.EPIC)
            .component(DataComponentTypes.ENCHANTMENT_GLINT_OVERRIDE, true)
        ));

    public static void init() {
        // Force the class to load and run the static initializers
    }
}

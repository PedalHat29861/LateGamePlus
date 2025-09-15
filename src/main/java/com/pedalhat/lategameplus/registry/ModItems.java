package com.pedalhat.lategameplus.registry;

import com.pedalhat.lategameplus.LateGamePlus;
import com.pedalhat.lategameplus.config.ConfigManager;
import com.pedalhat.lategameplus.config.ModConfig;
import com.pedalhat.lategameplus.item.LodestoneWarpItem;
import com.pedalhat.lategameplus.item.NetheriteBowItem;

import net.minecraft.component.ComponentMap;
import net.minecraft.component.DataComponentTypes;
import net.minecraft.component.type.ConsumableComponents;
import net.minecraft.component.type.EquippableComponent;
import net.minecraft.component.type.FoodComponent;
import net.minecraft.component.type.RepairableComponent;
import net.minecraft.entity.EquipmentSlot;
import net.minecraft.entity.effect.StatusEffectInstance;
import net.minecraft.entity.effect.StatusEffects;
import net.minecraft.item.Item;
import net.minecraft.item.Items;
import net.minecraft.item.consume.ApplyEffectsConsumeEffect;
import net.minecraft.item.equipment.EquipmentAssetKeys;
import net.minecraft.registry.Registries;
import net.minecraft.registry.Registry;
import net.minecraft.registry.RegistryKey;
import net.minecraft.registry.RegistryKeys;
import net.minecraft.registry.entry.RegistryEntryList;
import net.minecraft.util.Identifier;
import net.minecraft.util.Rarity;

public class ModItems {
    private static ComponentMap getChestplateAttributesForLevel(int lvl) {
        int clamped = Math.max(0, Math.min(4, lvl));
        return switch (clamped) {
            case 0 -> ComponentMap.EMPTY;
            case 1 -> Items.GOLDEN_CHESTPLATE.getComponents();
            case 2 -> Items.IRON_CHESTPLATE.getComponents();
            case 3 -> Items.DIAMOND_CHESTPLATE.getComponents();
            case 4 -> Items.NETHERITE_CHESTPLATE.getComponents();
            default -> Items.IRON_CHESTPLATE.getComponents();
        };
    }

    private static RepairableComponent repairsWithNuggetAndIngot() {
        RegistryEntryList<Item> mats = RegistryEntryList.of(
            Registries.ITEM.getEntry(NETHERITE_NUGGET),
            Registries.ITEM.getEntry(Items.NETHERITE_INGOT)
        );
        return new RepairableComponent(mats);
    }

    private static RegistryKey<Item> key(String name) {
        return RegistryKey.of(RegistryKeys.ITEM, Identifier.of(LateGamePlus.MOD_ID, name));
    }

    private static Item.Settings settings(String name) {
        return new Item.Settings().registryKey(key(name));
    }

    private static <T extends Item> T register(String name, T item) {
        return Registry.register(Registries.ITEM, key(name), item);
    }

    public static Item NETHERITE_NUGGET;
    public static Item NETHERITE_ELYTRA;
    public static Item NETHERITE_APPLE;
    public static Item ENCHANTED_NETHERITE_APPLE;
    public static Item NETHERITE_BOW;
    public static Item TOTEM_OF_NETHERDYING;
    public static Item LODESTONE_WARP;
    public static Item VOID_CRYSTAL;

    public static void init(ModConfig cfg) {
        NETHERITE_NUGGET = register("netherite_nugget",
            new Item(settings("netherite_nugget").fireproof()));

        var equip = EquippableComponent.builder(EquipmentSlot.CHEST)
            .model(RegistryKey.of(EquipmentAssetKeys.REGISTRY_KEY,
                    Identifier.of(LateGamePlus.MOD_ID, "wings/netherite_elytra")))
            .dispensable(true)
            .damageOnHurt(true)
            .build();

        Item.Settings elytraSettings = settings("netherite_elytra")
            .maxCount(1)
            .maxDamage(648)
            .fireproof()
            .component(DataComponentTypes.EQUIPPABLE, equip)
            .component(DataComponentTypes.GLIDER, net.minecraft.util.Unit.INSTANCE)
            .component(DataComponentTypes.REPAIRABLE, repairsWithNuggetAndIngot())
            .rarity(Rarity.EPIC);

        var base = getChestplateAttributesForLevel(cfg.netheriteElytraProtectionLevel);
        var attr = base.get(DataComponentTypes.ATTRIBUTE_MODIFIERS);
        if (attr != null) {
            elytraSettings.component(DataComponentTypes.ATTRIBUTE_MODIFIERS, attr);
        }
        NETHERITE_ELYTRA = register("netherite_elytra", new Item(elytraSettings));

        NETHERITE_APPLE = register("netherite_apple",
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
            )
        );

        ENCHANTED_NETHERITE_APPLE = register("enchanted_netherite_apple",
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
            )
        );

        NETHERITE_BOW = register("netherite_bow",
            new NetheriteBowItem(
                settings("netherite_bow")
                    .maxDamage(500)
                    .enchantable(15)
                    .fireproof()
                    .component(DataComponentTypes.REPAIRABLE, repairsWithNuggetAndIngot())
                    .rarity(Rarity.RARE)
            )
        );

        TOTEM_OF_NETHERDYING = register("totem_of_netherdying",
            new Item(settings("totem_of_netherdying")
                .fireproof()
                .rarity(Rarity.UNCOMMON)
                .maxCount(1)
                .maxDamage(Math.max(1, ConfigManager.get().netheriteTotemUses))
            )
        );

        LODESTONE_WARP = register("lodestone_warp",
            new LodestoneWarpItem(
                settings("lodestone_warp")
                    .maxDamage(1)
            )
        );

    }
}

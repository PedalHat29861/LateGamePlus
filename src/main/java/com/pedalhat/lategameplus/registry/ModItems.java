package com.pedalhat.lategameplus.registry;

import com.pedalhat.lategameplus.LateGamePlus;
import com.pedalhat.lategameplus.config.ConfigManager;
import com.pedalhat.lategameplus.config.ModConfig;
import com.pedalhat.lategameplus.item.LodestoneWarpItem;
import com.pedalhat.lategameplus.item.NetheriteBowItem;
import com.pedalhat.lategameplus.item.NetheriteCrossbowItem;
import com.pedalhat.lategameplus.item.DebrisResonatorItem;
import com.pedalhat.lategameplus.item.PompeiiWormItem;
import com.pedalhat.lategameplus.tag.LGPItemTags;

import net.minecraft.component.ComponentMap;
import net.minecraft.component.DataComponentTypes;
import net.minecraft.component.type.AttributeModifierSlot;
import net.minecraft.component.type.AttributeModifiersComponent;
import net.minecraft.component.type.ConsumableComponents;
import net.minecraft.component.type.EquippableComponent;
import net.minecraft.component.type.FoodComponent;
import net.minecraft.component.type.RepairableComponent;
import net.minecraft.component.type.NbtComponent;
import net.minecraft.entity.EntityType;
import net.minecraft.entity.EquipmentSlot;
import net.minecraft.entity.attribute.EntityAttributeModifier;
import net.minecraft.entity.attribute.EntityAttributes;
import net.minecraft.entity.effect.StatusEffectInstance;
import net.minecraft.entity.effect.StatusEffects;
import net.minecraft.item.Item;
import net.minecraft.item.Items;
import net.minecraft.item.FishingRodItem;
import net.minecraft.item.equipment.ArmorMaterial;
import net.minecraft.item.equipment.ArmorMaterials;
import net.minecraft.item.equipment.EquipmentType;
import net.minecraft.item.consume.ApplyEffectsConsumeEffect;
import net.minecraft.item.equipment.EquipmentAssetKeys;
import net.minecraft.registry.Registries;
import net.minecraft.registry.Registry;
import net.minecraft.registry.RegistryKey;
import net.minecraft.registry.RegistryKeys;
import net.minecraft.registry.entry.RegistryEntryList;
import net.minecraft.util.Identifier;
import net.minecraft.util.Rarity;
import net.minecraft.util.DyeColor;
import net.minecraft.sound.SoundEvents;

import java.util.EnumMap;
import java.util.Map;
import java.util.Arrays;

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
    public static Item NETHERITE_CROSSBOW;
    public static Item NETHERITE_FISHING_ROD;
    public static Item TOTEM_OF_NETHERDYING;
    public static Item LODESTONE_WARP;
    public static Item VOID_CRYSTAL;
    public static Item DEBRIS_RESONATOR;
    public static Item NETHERITE_WOLF_ARMOR;
    public static Item POMPEII_WORM;
    public static Map<DyeColor, Item> NETHERITE_HARNESSES;

    private static final RegistryKey<net.minecraft.item.equipment.EquipmentAsset> NETHERITE_WOLF_ARMOR_ASSET =
        RegistryKey.of(EquipmentAssetKeys.REGISTRY_KEY, Identifier.of(LateGamePlus.MOD_ID, "netherite_armadillo_scute"));
    private static final ArmorMaterial NETHERITE_WOLF_ARMOR_MATERIAL =
        new ArmorMaterial(
            ArmorMaterials.ARMADILLO_SCUTE.durability() * 2,
            Map.of(EquipmentType.BODY, 22),
            ArmorMaterials.NETHERITE.enchantmentValue(),
            SoundEvents.ITEM_ARMOR_EQUIP_WOLF,
            ArmorMaterials.NETHERITE.toughness(),
            ArmorMaterials.NETHERITE.knockbackResistance(),
            LGPItemTags.REPAIRS_NETHERITE_WOLF_ARMOR,
            NETHERITE_WOLF_ARMOR_ASSET
        );

    private static AttributeModifiersComponent createHarnessAttributes(String name) {
        return AttributeModifiersComponent.builder()
            .add(
                EntityAttributes.ARMOR,
                new EntityAttributeModifier(
                    Identifier.of(LateGamePlus.MOD_ID, name + "_armor"),
                    10.0,
                    EntityAttributeModifier.Operation.ADD_VALUE
                ),
                AttributeModifierSlot.BODY
            ).build();
    }

    private static RegistryKey<net.minecraft.item.equipment.EquipmentAsset> harnessAsset(DyeColor color) {
        return RegistryKey.of(
            EquipmentAssetKeys.REGISTRY_KEY,
            Identifier.of(LateGamePlus.MOD_ID, color.asString() + "_netherite_harness")
        );
    }

    private static EquippableComponent createHarnessEquippable(DyeColor color) {
        RegistryEntryList<EntityType<?>> allowed = RegistryEntryList.of(
            Registries.ENTITY_TYPE.getEntry(EntityType.HAPPY_GHAST)
        );

        return EquippableComponent.builder(EquipmentSlot.BODY)
                .equipSound(SoundEvents.ENTITY_HAPPY_GHAST_EQUIP)
                .model(harnessAsset(color))
                .allowedEntities(allowed)
                .equipOnInteract(true)
                .canBeSheared(true)
                .shearingSound(Registries.SOUND_EVENT.getEntry(SoundEvents.ENTITY_HAPPY_GHAST_UNEQUIP))
                .build();
    }

    private static Item registerHarness(DyeColor color) {
        String name = color.asString() + "_netherite_harness";
        return register(name,
            new Item(
                settings(name)
                    .maxCount(1)
                    .fireproof()
                    .component(DataComponentTypes.EQUIPPABLE, createHarnessEquippable(color))
                    .component(DataComponentTypes.ATTRIBUTE_MODIFIERS, createHarnessAttributes(name))
            )
        );
    }

    public static Item[] orderedNetheriteHarnesses() {
        if (NETHERITE_HARNESSES == null || NETHERITE_HARNESSES.isEmpty()) {
            return new Item[0];
        }
        return Arrays.stream(DyeColor.values())
            .map(NETHERITE_HARNESSES::get)
            .filter(java.util.Objects::nonNull)
            .toArray(Item[]::new);
    }

    @SuppressWarnings("null")
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
                    .rarity(Rarity.COMMON)
            )
        );

        NETHERITE_CROSSBOW = register("netherite_crossbow",
            new NetheriteCrossbowItem(
                settings("netherite_crossbow")
                    .maxDamage(700)
                    .enchantable(1)
                    .fireproof()
                    .component(DataComponentTypes.REPAIRABLE, repairsWithNuggetAndIngot())
                    .component(DataComponentTypes.ITEM_MODEL, Identifier.of(LateGamePlus.MOD_ID, "netherite_crossbow"))
                    .rarity(Rarity.COMMON)
            )
        );

        NETHERITE_FISHING_ROD = register("netherite_fishing_rod",
            new FishingRodItem(
                settings("netherite_fishing_rod")
                    .maxDamage(Items.FISHING_ROD.getDefaultStack().getMaxDamage() * 4)
                    .enchantable(15)
                    .fireproof()
                    .component(DataComponentTypes.REPAIRABLE, repairsWithNuggetAndIngot())
                    .rarity(Rarity.COMMON)
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

        NETHERITE_WOLF_ARMOR = register("netherite_wolf_armor",
            new Item(
                settings("netherite_wolf_armor")
                    .wolfArmor(NETHERITE_WOLF_ARMOR_MATERIAL)
                    .fireproof()
            )
        );

        LODESTONE_WARP = register("lodestone_warp",
            new LodestoneWarpItem(
                settings("lodestone_warp")
                    .maxDamage(1)
            )
        );
        DEBRIS_RESONATOR = register("debris_resonator",
            new DebrisResonatorItem(
                settings("debris_resonator")
                    .maxCount(1)
                    .fireproof()
                    .rarity(Rarity.RARE)
                    .component(DataComponentTypes.ITEM_MODEL, Identifier.of(LateGamePlus.MOD_ID, "debris_resonator"))
                    .component(DataComponentTypes.CUSTOM_DATA, NbtComponent.DEFAULT)
            )
        );

        NETHERITE_HARNESSES = new EnumMap<>(DyeColor.class);
        for (DyeColor color : DyeColor.values()) {
            NETHERITE_HARNESSES.put(color, registerHarness(color));
        }
        POMPEII_WORM = register("pompeii_worm",
            new PompeiiWormItem(settings("pompeii_worm")
                .maxCount(16)
                .fireproof()
                .food(
                    new FoodComponent.Builder()
                        .nutrition(4)
                        .saturationModifier(0.8F)
                        .alwaysEdible()
                        .build(),
                    ConsumableComponents.food()
                        .consumeEffect(new ApplyEffectsConsumeEffect(new StatusEffectInstance(StatusEffects.FIRE_RESISTANCE, 70, 0), 1.0F))
                        .consumeEffect(new ApplyEffectsConsumeEffect(new StatusEffectInstance(StatusEffects.FIRE_RESISTANCE, 140, 0), 0.1F))
                        .build()
                ).rarity(Rarity.COMMON)
            )
        );
    }
}

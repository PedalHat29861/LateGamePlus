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
import java.util.EnumMap;
import java.util.Map;
import net.minecraft.core.HolderSet;
import net.minecraft.core.Registry;
import net.minecraft.core.component.DataComponents;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.core.registries.Registries;
import net.minecraft.resources.Identifier;
import net.minecraft.resources.ResourceKey;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.effect.MobEffects;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.entity.EquipmentSlotGroup;
import net.minecraft.world.entity.ai.attributes.AttributeModifier;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.minecraft.world.food.FoodProperties;
import net.minecraft.world.item.DyeColor;
import net.minecraft.world.item.FishingRodItem;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.Items;
import net.minecraft.world.item.Rarity;
import net.minecraft.world.item.component.Consumables;
import net.minecraft.world.item.component.CustomData;
import net.minecraft.world.item.component.ItemAttributeModifiers;
import net.minecraft.world.item.consume_effects.ApplyStatusEffectsConsumeEffect;
import net.minecraft.world.item.enchantment.Repairable;
import net.minecraft.world.item.equipment.ArmorMaterial;
import net.minecraft.world.item.equipment.ArmorMaterials;
import net.minecraft.world.item.equipment.ArmorType;
import net.minecraft.world.item.equipment.EquipmentAssets;
import net.minecraft.world.item.equipment.Equippable;
import java.util.Arrays;

public class ModItems {
    public static ItemAttributeModifiers getChestplateAttributesForLevel(int lvl) {
        int armor = switch (Math.max(0, Math.min(4, lvl))) {
            case 0 -> 0;
            case 1 -> 5;
            case 2 -> 6;
            case 3, 4 -> 8;
            default -> 6;
        };
        double toughness = switch (Math.max(0, Math.min(4, lvl))) {
            case 3 -> 2.0;
            case 4 -> 3.0;
            default -> 0.0;
        };
        double knockbackResistance = lvl >= 4 ? 0.1 : 0.0;

        if (armor <= 0 && toughness <= 0.0 && knockbackResistance <= 0.0) {
            return null;
        }

        ItemAttributeModifiers.Builder builder = ItemAttributeModifiers.builder()
            .add(
                Attributes.ARMOR,
                new AttributeModifier(
                    Identifier.fromNamespaceAndPath(LateGamePlus.MOD_ID, "netherite_elytra_armor"),
                    armor,
                    AttributeModifier.Operation.ADD_VALUE
                ),
                EquipmentSlotGroup.CHEST
            );

        if (toughness > 0.0) {
            builder.add(
                Attributes.ARMOR_TOUGHNESS,
                new AttributeModifier(
                    Identifier.fromNamespaceAndPath(LateGamePlus.MOD_ID, "netherite_elytra_armor_toughness"),
                    toughness,
                    AttributeModifier.Operation.ADD_VALUE
                ),
                EquipmentSlotGroup.CHEST
            );
        }

        if (knockbackResistance > 0.0) {
            builder.add(
                Attributes.KNOCKBACK_RESISTANCE,
                new AttributeModifier(
                    Identifier.fromNamespaceAndPath(LateGamePlus.MOD_ID, "netherite_elytra_knockback_resistance"),
                    knockbackResistance,
                    AttributeModifier.Operation.ADD_VALUE
                ),
                EquipmentSlotGroup.CHEST
            );
        }

        return builder.build();
    }

    private static Repairable repairsWithNuggetAndIngot() {
        HolderSet<Item> mats = HolderSet.direct(
            BuiltInRegistries.ITEM.wrapAsHolder(NETHERITE_NUGGET),
            BuiltInRegistries.ITEM.wrapAsHolder(Items.NETHERITE_INGOT)
        );
        return new Repairable(mats);
    }

    private static ResourceKey<Item> key(String name) {
        return ResourceKey.create(Registries.ITEM, Identifier.fromNamespaceAndPath(LateGamePlus.MOD_ID, name));
    }

    private static Item.Properties settings(String name) {
        return new Item.Properties().setId(key(name));
    }

    private static <T extends Item> T register(String name, T item) {
        return Registry.register(BuiltInRegistries.ITEM, key(name), item);
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
    public static Item BLIND_SHRIMP;
    public static Item VOLCANIC_CONCOCTION;
    public static Item LAVA_VISION_ICON;
    public static Map<DyeColor, Item> NETHERITE_HARNESSES;
    private static final int VANILLA_FISHING_ROD_DURABILITY = 64;

    private static final ResourceKey<net.minecraft.world.item.equipment.EquipmentAsset> NETHERITE_WOLF_ARMOR_ASSET =
        ResourceKey.create(EquipmentAssets.ROOT_ID, Identifier.fromNamespaceAndPath(LateGamePlus.MOD_ID, "netherite_armadillo_scute"));
    private static final ArmorMaterial NETHERITE_WOLF_ARMOR_MATERIAL =
        new ArmorMaterial(
            ArmorMaterials.ARMADILLO_SCUTE.durability() * 2,
            Map.of(ArmorType.BODY, 22),
            ArmorMaterials.NETHERITE.enchantmentValue(),
            SoundEvents.ARMOR_EQUIP_WOLF,
            ArmorMaterials.NETHERITE.toughness(),
            ArmorMaterials.NETHERITE.knockbackResistance(),
            LGPItemTags.REPAIRS_NETHERITE_WOLF_ARMOR,
            NETHERITE_WOLF_ARMOR_ASSET
        );

    private static ItemAttributeModifiers createHarnessAttributes(String name) {
        return ItemAttributeModifiers.builder()
            .add(
                Attributes.ARMOR,
                new AttributeModifier(
                    Identifier.fromNamespaceAndPath(LateGamePlus.MOD_ID, name + "_armor"),
                    10.0,
                    AttributeModifier.Operation.ADD_VALUE
                ),
                EquipmentSlotGroup.BODY
            ).build();
    }

    private static ResourceKey<net.minecraft.world.item.equipment.EquipmentAsset> harnessAsset(DyeColor color) {
        return ResourceKey.create(
            EquipmentAssets.ROOT_ID,
            Identifier.fromNamespaceAndPath(LateGamePlus.MOD_ID, color.getSerializedName() + "_netherite_harness")
        );
    }

    private static Equippable createHarnessEquippable(DyeColor color) {
        HolderSet<EntityType<?>> allowed = HolderSet.direct(
            BuiltInRegistries.ENTITY_TYPE.wrapAsHolder(EntityType.HAPPY_GHAST)
        );

        return Equippable.builder(EquipmentSlot.BODY)
                .setEquipSound(SoundEvents.HARNESS_EQUIP)
                .setAsset(harnessAsset(color))
                .setAllowedEntities(allowed)
                .setEquipOnInteract(true)
                .setCanBeSheared(true)
                .setShearingSound(BuiltInRegistries.SOUND_EVENT.wrapAsHolder(SoundEvents.HARNESS_UNEQUIP))
                .build();
    }

    private static Item registerHarness(DyeColor color) {
        String name = color.getSerializedName() + "_netherite_harness";
        return register(name,
            new Item(
                settings(name)
                    .stacksTo(1)
                    .fireResistant()
                    .component(DataComponents.EQUIPPABLE, createHarnessEquippable(color))
                    .component(DataComponents.ATTRIBUTE_MODIFIERS, createHarnessAttributes(name))
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

    public static void init(ModConfig cfg) {
        NETHERITE_NUGGET = register("netherite_nugget",
            new Item(settings("netherite_nugget").fireResistant()));

        var equip = Equippable.builder(EquipmentSlot.CHEST)
            .setAsset(ResourceKey.create(EquipmentAssets.ROOT_ID,
                    Identifier.fromNamespaceAndPath(LateGamePlus.MOD_ID, "wings/netherite_elytra")))
            .setDispensable(true)
            .setDamageOnHurt(true)
            .build();

        Item.Properties elytraSettings = settings("netherite_elytra")
            .stacksTo(1)
            .durability(648)
            .fireResistant()
            .component(DataComponents.EQUIPPABLE, equip)
            .component(DataComponents.GLIDER, net.minecraft.util.Unit.INSTANCE)
            .component(DataComponents.REPAIRABLE, repairsWithNuggetAndIngot())
            .rarity(Rarity.EPIC);

        var attr = getChestplateAttributesForLevel(cfg.netheriteElytraProtectionLevel);
        if (attr != null) {
            elytraSettings.component(DataComponents.ATTRIBUTE_MODIFIERS, attr);
        }
        NETHERITE_ELYTRA = register("netherite_elytra", new Item(elytraSettings));

        NETHERITE_APPLE = register("netherite_apple",
            new Item(settings("netherite_apple")
                .fireResistant()
                .food(
                    new FoodProperties.Builder()
                        .nutrition(6)
                        .saturationModifier(1.3F)
                        .alwaysEdible()
                        .build(),
                    Consumables.defaultFood()
                        .onConsume(new ApplyStatusEffectsConsumeEffect(new MobEffectInstance(MobEffects.REGENERATION, 200, 1), 1.0F))
                        .onConsume(new ApplyStatusEffectsConsumeEffect(new MobEffectInstance(MobEffects.FIRE_RESISTANCE, 3000, 0), 1.0F))
                        .onConsume(new ApplyStatusEffectsConsumeEffect(new MobEffectInstance(MobEffects.ABSORPTION, 2400, 2), 1.0F))
                        .onConsume(new ApplyStatusEffectsConsumeEffect(new MobEffectInstance(MobEffects.RESISTANCE, 300, 3), 1.0F))
                        .build()
                ).rarity(Rarity.RARE)
            )
        );

        ENCHANTED_NETHERITE_APPLE = register("enchanted_netherite_apple",
            new Item(settings("enchanted_netherite_apple")
                .fireResistant()
                .food(
                    new FoodProperties.Builder()
                        .nutrition(10)
                        .saturationModifier(1.5F)
                        .alwaysEdible()
                        .build(),
                    Consumables.defaultFood()
                        .onConsume(new ApplyStatusEffectsConsumeEffect(new MobEffectInstance(MobEffects.REGENERATION, 800, 2), 1.0F))
                        .onConsume(new ApplyStatusEffectsConsumeEffect(new MobEffectInstance(MobEffects.RESISTANCE, 10000, 1), 1.0F))
                        .onConsume(new ApplyStatusEffectsConsumeEffect(new MobEffectInstance(MobEffects.FIRE_RESISTANCE, 12000, 0), 1.0F))
                        .onConsume(new ApplyStatusEffectsConsumeEffect(new MobEffectInstance(MobEffects.ABSORPTION, 4800, 5), 1.0F))
                        .onConsume(new ApplyStatusEffectsConsumeEffect(new MobEffectInstance(MobEffects.SPEED, 300, 1), 1.0F))
                        .onConsume(new ApplyStatusEffectsConsumeEffect(new MobEffectInstance(MobEffects.STRENGTH, 300, 1), 1.0F))
                        .build()
                ).rarity(Rarity.EPIC)
                .component(DataComponents.ENCHANTMENT_GLINT_OVERRIDE, true)
            )
        );

        NETHERITE_BOW = register("netherite_bow",
            new NetheriteBowItem(
                settings("netherite_bow")
                    .durability(500)
                    .enchantable(15)
                    .fireResistant()
                    .component(DataComponents.REPAIRABLE, repairsWithNuggetAndIngot())
                    .rarity(Rarity.COMMON)
            )
        );

        NETHERITE_CROSSBOW = register("netherite_crossbow",
            new NetheriteCrossbowItem(
                settings("netherite_crossbow")
                    .durability(700)
                    .enchantable(1)
                    .fireResistant()
                    .component(DataComponents.REPAIRABLE, repairsWithNuggetAndIngot())
                    .component(DataComponents.ITEM_MODEL, Identifier.fromNamespaceAndPath(LateGamePlus.MOD_ID, "netherite_crossbow"))
                    .rarity(Rarity.COMMON)
            )
        );

        NETHERITE_FISHING_ROD = register("netherite_fishing_rod",
            new FishingRodItem(
                settings("netherite_fishing_rod")
                    .durability(VANILLA_FISHING_ROD_DURABILITY * 4)
                    .enchantable(15)
                    .fireResistant()
                    .component(DataComponents.REPAIRABLE, repairsWithNuggetAndIngot())
                    .rarity(Rarity.COMMON)
            )
        );

        TOTEM_OF_NETHERDYING = register("totem_of_netherdying",
            new Item(settings("totem_of_netherdying")
                .fireResistant()
                .rarity(Rarity.UNCOMMON)
                .stacksTo(1)
                .durability(Math.max(1, ConfigManager.get().netheriteTotemUses))
            )
        );

        NETHERITE_WOLF_ARMOR = register("netherite_wolf_armor",
            new Item(
                settings("netherite_wolf_armor")
                    .wolfArmor(NETHERITE_WOLF_ARMOR_MATERIAL)
                    .fireResistant()
            )
        );

        LODESTONE_WARP = register("lodestone_warp",
            new LodestoneWarpItem(
                settings("lodestone_warp")
                    .durability(1)
            )
        );
        DEBRIS_RESONATOR = register("debris_resonator",
            new DebrisResonatorItem(
                settings("debris_resonator")
                    .stacksTo(1)
                    .fireResistant()
                    .rarity(Rarity.RARE)
                    .component(DataComponents.ITEM_MODEL, Identifier.fromNamespaceAndPath(LateGamePlus.MOD_ID, "debris_resonator"))
                    .component(DataComponents.CUSTOM_DATA, CustomData.EMPTY)
            )
        );

        NETHERITE_HARNESSES = new EnumMap<>(DyeColor.class);
        for (DyeColor color : DyeColor.values()) {
            NETHERITE_HARNESSES.put(color, registerHarness(color));
        }
        POMPEII_WORM = register("pompeii_worm",
            new PompeiiWormItem(settings("pompeii_worm")
                .stacksTo(64)
                .fireResistant()
                .food(
                    new FoodProperties.Builder()
                        .nutrition(4)
                        .saturationModifier(0.8F)
                        .alwaysEdible()
                        .build(),
                    Consumables.defaultFood()
                        .onConsume(new ApplyStatusEffectsConsumeEffect(new MobEffectInstance(MobEffects.FIRE_RESISTANCE, 70, 0), 1.0F))
                        .onConsume(new ApplyStatusEffectsConsumeEffect(new MobEffectInstance(MobEffects.FIRE_RESISTANCE, 140, 0), 0.1F))
                        .build()
                ).rarity(Rarity.COMMON)
            )
        );
        BLIND_SHRIMP = register("blind_shrimp",
            new Item(settings("blind_shrimp")
                .stacksTo(64)
                .fireResistant()
                .food(
                    new FoodProperties.Builder()
                        .nutrition(1)
                        .saturationModifier(0.4F)
                        .alwaysEdible()
                        .build(),
                    Consumables.defaultFood()
                        .onConsume(new ApplyStatusEffectsConsumeEffect(new MobEffectInstance(ModEffects.VOLCANIC_INFUSION, 400, 0), 1.0F))
                        .onConsume(new ApplyStatusEffectsConsumeEffect(new MobEffectInstance(ModEffects.LAVA_VISION, 400, 0), 1.0F))
                        .build()
                ).rarity(Rarity.COMMON)
            )
        );
        VOLCANIC_CONCOCTION = register("volcanic_concoction",
            new Item(settings("volcanic_concoction")
                .stacksTo(16)
                .rarity(Rarity.COMMON)
            )
        );
        LAVA_VISION_ICON = register("lava_vision_icon",
            new Item(settings("lava_vision_icon")
                .stacksTo(1)
                .rarity(Rarity.COMMON)
            )
        );
    }
}

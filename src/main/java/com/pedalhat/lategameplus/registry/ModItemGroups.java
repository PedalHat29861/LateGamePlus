package com.pedalhat.lategameplus.registry;

import com.pedalhat.lategameplus.LateGamePlus;
import net.fabricmc.fabric.api.creativetab.v1.FabricCreativeModeTab;
import net.minecraft.core.Registry;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.core.registries.Registries;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.Identifier;
import net.minecraft.resources.ResourceKey;
import net.minecraft.world.item.CreativeModeTab;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.item.alchemy.PotionContents;

public final class ModItemGroups {
    public static final ResourceKey<CreativeModeTab> LATEGAMEPLUS_GROUP = ResourceKey.create(
        Registries.CREATIVE_MODE_TAB,
        Identifier.fromNamespaceAndPath(LateGamePlus.MOD_ID, "lategameplus")
    );

    public static final ResourceKey<CreativeModeTab> LATEGAMEPLUS_GROUP_POTIONS = ResourceKey.create(
        Registries.CREATIVE_MODE_TAB,
        Identifier.fromNamespaceAndPath(LateGamePlus.MOD_ID, "lategameplus_potions")
    );

    public static void init() {
        Registry.register(BuiltInRegistries.CREATIVE_MODE_TAB, LATEGAMEPLUS_GROUP, FabricCreativeModeTab.builder()
            .icon(() -> new ItemStack(ModItems.ENCHANTED_NETHERITE_APPLE))
            .title(Component.translatable("itemGroup.lategameplus.lategameplus"))
            .displayItems((context, entries) -> {
                entries.accept(ModItems.NETHERITE_NUGGET);
                entries.accept(ModItems.NETHERITE_APPLE);
                entries.accept(ModItems.ENCHANTED_NETHERITE_APPLE);
                entries.accept(ModItems.TOTEM_OF_NETHERDYING);
                entries.accept(ModItems.NETHERITE_ELYTRA);
                entries.accept(ModItems.NETHERITE_BOW);
                entries.accept(ModItems.NETHERITE_CROSSBOW);
                entries.accept(ModItems.NETHERITE_FISHING_ROD);
                entries.accept(ModItems.NETHERITE_WOLF_ARMOR);
                for (var harness : ModItems.orderedNetheriteHarnesses()) {
                    entries.accept(harness);
                }
                entries.accept(ModItems.LODESTONE_WARP);
                entries.accept(ModItems.DEBRIS_RESONATOR);
                entries.accept(ModBlocks.NETHERITE_ANVIL);
                entries.accept(ModBlocks.FUSION_FORGE);
                entries.accept(ModItems.POMPEII_WORM);
                entries.accept(ModItems.BLIND_SHRIMP);
                entries.accept(ModItems.VOLCANIC_CONCOCTION);
            })
            .build());
        Registry.register(BuiltInRegistries.CREATIVE_MODE_TAB, LATEGAMEPLUS_GROUP_POTIONS, FabricCreativeModeTab.builder()
            .icon(() -> new ItemStack(ModItems.LAVA_VISION_ICON))
            .title(Component.translatable("itemGroup.lategameplus.potions"))
            .displayItems((context, entries) -> {
                entries.accept(PotionContents.createItemStack(Items.POTION, ModPotions.LAVA_VISION));
                entries.accept(PotionContents.createItemStack(Items.POTION, ModPotions.STRONG_LAVA_VISION));
                entries.accept(PotionContents.createItemStack(Items.POTION, ModPotions.LONG_LAVA_VISION));
                entries.accept(PotionContents.createItemStack(Items.POTION, ModPotions.VOLCANIC_INFUSION));
                entries.accept(PotionContents.createItemStack(Items.POTION, ModPotions.STRONG_VOLCANIC_INFUSION));
                entries.accept(PotionContents.createItemStack(Items.POTION, ModPotions.LONG_VOLCANIC_INFUSION));
                entries.accept(PotionContents.createItemStack(Items.SPLASH_POTION, ModPotions.LAVA_VISION));
                entries.accept(PotionContents.createItemStack(Items.SPLASH_POTION, ModPotions.STRONG_LAVA_VISION));
                entries.accept(PotionContents.createItemStack(Items.SPLASH_POTION, ModPotions.LONG_LAVA_VISION));
                entries.accept(PotionContents.createItemStack(Items.SPLASH_POTION, ModPotions.VOLCANIC_INFUSION));
                entries.accept(PotionContents.createItemStack(Items.SPLASH_POTION, ModPotions.STRONG_VOLCANIC_INFUSION));
                entries.accept(PotionContents.createItemStack(Items.SPLASH_POTION, ModPotions.LONG_VOLCANIC_INFUSION));
                entries.accept(PotionContents.createItemStack(Items.LINGERING_POTION, ModPotions.LAVA_VISION));
                entries.accept(PotionContents.createItemStack(Items.LINGERING_POTION, ModPotions.STRONG_LAVA_VISION));
                entries.accept(PotionContents.createItemStack(Items.LINGERING_POTION, ModPotions.LONG_LAVA_VISION));
                entries.accept(PotionContents.createItemStack(Items.LINGERING_POTION, ModPotions.VOLCANIC_INFUSION));
                entries.accept(PotionContents.createItemStack(Items.LINGERING_POTION, ModPotions.STRONG_VOLCANIC_INFUSION));
                entries.accept(PotionContents.createItemStack(Items.LINGERING_POTION, ModPotions.LONG_VOLCANIC_INFUSION));
                entries.accept(PotionContents.createItemStack(Items.POTION, ModPotions.VOLCANIC_MASTER));
                entries.accept(PotionContents.createItemStack(Items.POTION, ModPotions.STRONG_VOLCANIC_MASTER));
                entries.accept(PotionContents.createItemStack(Items.POTION, ModPotions.LONG_VOLCANIC_MASTER));
                entries.accept(PotionContents.createItemStack(Items.SPLASH_POTION, ModPotions.VOLCANIC_MASTER));
                entries.accept(PotionContents.createItemStack(Items.SPLASH_POTION, ModPotions.STRONG_VOLCANIC_MASTER));
                entries.accept(PotionContents.createItemStack(Items.SPLASH_POTION, ModPotions.LONG_VOLCANIC_MASTER));
                entries.accept(PotionContents.createItemStack(Items.LINGERING_POTION, ModPotions.VOLCANIC_MASTER));
                entries.accept(PotionContents.createItemStack(Items.LINGERING_POTION, ModPotions.STRONG_VOLCANIC_MASTER));
                entries.accept(PotionContents.createItemStack(Items.LINGERING_POTION, ModPotions.LONG_VOLCANIC_MASTER));
            })
            .build());
    }

    private ModItemGroups() {}
}

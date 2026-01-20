package com.pedalhat.lategameplus.registry;

import com.pedalhat.lategameplus.LateGamePlus;
import net.fabricmc.fabric.api.itemgroup.v1.FabricItemGroup;
import net.minecraft.item.ItemGroup;
import net.minecraft.item.ItemStack;
import net.minecraft.registry.Registries;
import net.minecraft.registry.Registry;
import net.minecraft.registry.RegistryKey;
import net.minecraft.registry.RegistryKeys;
import net.minecraft.text.Text;
import net.minecraft.util.Identifier;

public final class ModItemGroups {
    public static final RegistryKey<ItemGroup> LATEGAMEPLUS_GROUP = RegistryKey.of(
        RegistryKeys.ITEM_GROUP,
        Identifier.of(LateGamePlus.MOD_ID, "lategameplus")
    );

    public static void init() {
        Registry.register(Registries.ITEM_GROUP, LATEGAMEPLUS_GROUP, FabricItemGroup.builder()
            .icon(() -> new ItemStack(ModItems.ENCHANTED_NETHERITE_APPLE))
            .displayName(Text.translatable("itemGroup.lategameplus.lategameplus"))
            .entries((context, entries) -> {
                entries.add(ModItems.NETHERITE_NUGGET);
                entries.add(ModItems.NETHERITE_APPLE);
                entries.add(ModItems.ENCHANTED_NETHERITE_APPLE);
                entries.add(ModItems.TOTEM_OF_NETHERDYING);
                entries.add(ModItems.NETHERITE_ELYTRA);
                entries.add(ModItems.NETHERITE_BOW);
                entries.add(ModItems.NETHERITE_CROSSBOW);
                entries.add(ModItems.NETHERITE_FISHING_ROD);
                entries.add(ModItems.NETHERITE_WOLF_ARMOR);
                for (var harness : ModItems.orderedNetheriteHarnesses()) {
                    entries.add(harness);
                }
                entries.add(ModItems.LODESTONE_WARP);
                entries.add(ModItems.DEBRIS_RESONATOR);
                entries.add(ModBlocks.NETHERITE_ANVIL);
                entries.add(ModBlocks.FUSION_FORGE);
                entries.add(ModItems.POMPEII_WORM);
            })
            .build());
    }

    private ModItemGroups() {}
}

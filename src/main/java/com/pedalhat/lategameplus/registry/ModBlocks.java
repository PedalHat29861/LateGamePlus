package com.pedalhat.lategameplus.registry;

import com.pedalhat.lategameplus.LateGamePlus;
import com.pedalhat.lategameplus.block.FusionForgeBlock;
import com.pedalhat.lategameplus.block.NetheriteAnvilBlock;

import net.minecraft.block.Block;
import net.minecraft.block.AbstractBlock;
import net.minecraft.sound.BlockSoundGroup;
import net.minecraft.item.BlockItem;
import net.minecraft.item.Item;
import net.minecraft.registry.Registries;
import net.minecraft.registry.Registry;
import net.minecraft.registry.RegistryKey;
import net.minecraft.registry.RegistryKeys;
import net.minecraft.util.Identifier;
import net.minecraft.util.Rarity;

public final class ModBlocks {
    private static RegistryKey<Block> blockKey(String name) {
        return RegistryKey.of(RegistryKeys.BLOCK, Identifier.of(LateGamePlus.MOD_ID, name));
    }

    private static RegistryKey<Item> itemKey(String name) {
        return RegistryKey.of(RegistryKeys.ITEM, Identifier.of(LateGamePlus.MOD_ID, name));
    }

    private static <T extends Block> T registerBlock(String name, T block) {
        return Registry.register(Registries.BLOCK, blockKey(name), block);
    }

    private static BlockItem registerBlockItem(String name, Block block, Item.Settings itemSettings) {
        return Registry.register(Registries.ITEM, itemKey(name),
            new BlockItem(block, itemSettings.registryKey(itemKey(name))));
    }

    private static <T extends Block> T registerWithItem(String name, T block, Rarity rarity, boolean fireproof) {
        T registered = registerBlock(name, block);
        Item.Settings is = new Item.Settings().rarity(rarity);
        if (fireproof) is = is.fireproof();
        registerBlockItem(name, registered, is);
        return registered;
    }

    public static Block NETHERITE_ANVIL;
    public static Block FUSION_FORGE;

    public static void init() {
        // Block tuning: anvil sounds, netherite hardness/resistance (50F/1200F),
        // requires tool; mining level is controlled via data tags.
        AbstractBlock.Settings netheriteAnvilSettings = AbstractBlock.Settings
            .create()
            .sounds(BlockSoundGroup.ANVIL)
            .strength(60.0F, 1200.0F)
            .requiresTool()
            .luminance(state -> 3)
            .registryKey(blockKey("netherite_anvil"));

        NETHERITE_ANVIL = registerWithItem(
            "netherite_anvil",
            new NetheriteAnvilBlock(netheriteAnvilSettings),
            Rarity.COMMON,
            true
        );

        AbstractBlock.Settings fusionForgeSettings = AbstractBlock.Settings
            .create()
            .sounds(BlockSoundGroup.METAL)
            .strength(4.5F, 1200.0F)
            .requiresTool()
            .registryKey(blockKey("fusion_forge"));

        FUSION_FORGE = registerWithItem(
            "fusion_forge",
            new FusionForgeBlock(fusionForgeSettings),
            Rarity.UNCOMMON,
            true
        );
    }

    private ModBlocks() {}
}

package com.pedalhat.lategameplus.registry;

import com.pedalhat.lategameplus.LateGamePlus;
import com.pedalhat.lategameplus.block.FusionForgeBlock;
import com.pedalhat.lategameplus.block.FusionForgeState;
import com.pedalhat.lategameplus.block.NetheriteAnvilBlock;
import com.pedalhat.lategameplus.block.VolcanicObsidianBlock;
import net.minecraft.core.Registry;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.core.registries.Registries;
import net.minecraft.resources.Identifier;
import net.minecraft.resources.ResourceKey;
import net.minecraft.world.item.BlockItem;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.Rarity;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.SoundType;
import net.minecraft.world.level.block.state.BlockBehaviour;

public final class ModBlocks {
    private static ResourceKey<Block> blockKey(String name) {
        return ResourceKey.create(Registries.BLOCK, Identifier.fromNamespaceAndPath(LateGamePlus.MOD_ID, name));
    }

    private static ResourceKey<Item> itemKey(String name) {
        return ResourceKey.create(Registries.ITEM, Identifier.fromNamespaceAndPath(LateGamePlus.MOD_ID, name));
    }

    private static <T extends Block> T registerBlock(String name, T block) {
        return Registry.register(BuiltInRegistries.BLOCK, blockKey(name), block);
    }

    private static BlockItem registerBlockItem(String name, Block block, Item.Properties itemSettings) {
        return Registry.register(BuiltInRegistries.ITEM, itemKey(name),
            new BlockItem(block, itemSettings.setId(itemKey(name))));
    }

    private static <T extends Block> T registerWithItem(String name, T block, Rarity rarity, boolean fireproof) {
        T registered = registerBlock(name, block);
        Item.Properties is = new Item.Properties().rarity(rarity);
        if (fireproof) is = is.fireResistant();
        registerBlockItem(name, registered, is);
        return registered;
    }

    public static Block NETHERITE_ANVIL;
    public static Block FUSION_FORGE;
    public static Block VOLCANIC_OBSIDIAN;

    public static void init() {
        // Block tuning: anvil sounds, netherite hardness/resistance (50F/1200F),
        // requires tool; mining level is controlled via data tags.
        BlockBehaviour.Properties netheriteAnvilSettings = BlockBehaviour.Properties
            .of()
            .sound(SoundType.ANVIL)
            .strength(60.0F, 1200.0F)
            .requiresCorrectToolForDrops()
            .lightLevel(state -> 3)
            .setId(blockKey("netherite_anvil"));

        NETHERITE_ANVIL = registerWithItem(
            "netherite_anvil",
            new NetheriteAnvilBlock(netheriteAnvilSettings),
            Rarity.COMMON,
            true
        );

        BlockBehaviour.Properties fusionForgeSettings = BlockBehaviour.Properties
            .of()
            .sound(SoundType.METAL)
            .strength(4.5F, 1200.0F)
            .requiresCorrectToolForDrops()
            .lightLevel(state -> {
                if (!state.hasProperty(FusionForgeBlock.STATE)) {
                    return 0;
                }
                FusionForgeState forgeState = state.getValue(FusionForgeBlock.STATE);
                return switch (forgeState) {
                    case NETHER_WORKING -> 15;
                    case NETHER_DISABLED -> 10;
                    case WORKING -> 13;
                    case DISABLED -> 0;
                };
            })
            .setId(blockKey("fusion_forge"));

        FUSION_FORGE = registerWithItem(
            "fusion_forge",
            new FusionForgeBlock(fusionForgeSettings),
            Rarity.COMMON,
            true
        );

        BlockBehaviour.Properties volcanicObsidianSettings = BlockBehaviour.Properties
            .ofFullCopy(Blocks.OBSIDIAN)
            .noLootTable()
            .setId(blockKey("volcanic_obsidian"));

        VOLCANIC_OBSIDIAN = registerBlock(
            "volcanic_obsidian",
            new VolcanicObsidianBlock(volcanicObsidianSettings)
        );
    }

    private ModBlocks() {}
}

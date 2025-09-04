package com.pedalhat.lategameplus.registry;

import com.pedalhat.lategameplus.LateGamePlus;
import com.pedalhat.lategameplus.block.NetheriteShulkerBoxBlock;
import net.minecraft.block.Block;
import net.minecraft.block.Blocks;
import com.pedalhat.lategameplus.block.entity.NetheriteShulkerBoxBlockEntity;
import net.minecraft.block.entity.BlockEntityType;
import net.fabricmc.fabric.api.object.builder.v1.block.entity.FabricBlockEntityTypeBuilder;
import net.minecraft.item.BlockItem;
import net.minecraft.item.Item;
import net.minecraft.registry.Registries;
import net.minecraft.registry.Registry;
import net.minecraft.util.Identifier;
import net.minecraft.loot.LootTable;
import net.minecraft.registry.RegistryKey;
import net.minecraft.registry.RegistryKeys;
import java.util.Optional;

/** Registers mod blocks and block entities. */
public class ModBlocks {
    public static Block NETHERITE_SHULKER_BOX;
    public static BlockEntityType<NetheriteShulkerBoxBlockEntity> NETHERITE_SHULKER_BOX_ENTITY;
    public static Item NETHERITE_SHULKER_BOX_ITEM;

    public static void init() {
        Identifier netheriteShulkerId = Identifier.of(LateGamePlus.MOD_ID, "netherite_shulker_box");
        RegistryKey<Block> netheriteShulkerKey = RegistryKey.of(RegistryKeys.BLOCK, netheriteShulkerId);
        NETHERITE_SHULKER_BOX = Registry.register(Registries.BLOCK,
                netheriteShulkerId,
                new NetheriteShulkerBoxBlock(
                        net.minecraft.block.AbstractBlock.Settings.copy(Blocks.SHULKER_BOX)
                                .registryKey(netheriteShulkerKey)
                ));

        NETHERITE_SHULKER_BOX_ENTITY = Registry.register(Registries.BLOCK_ENTITY_TYPE,
                Identifier.of(LateGamePlus.MOD_ID, "netherite_shulker_box"),
                FabricBlockEntityTypeBuilder.create(NetheriteShulkerBoxBlockEntity::new, NETHERITE_SHULKER_BOX).build());

        var itemId = Identifier.of(LateGamePlus.MOD_ID, "netherite_shulker_box");
        var itemKey = RegistryKey.of(RegistryKeys.ITEM, itemId);
        NETHERITE_SHULKER_BOX_ITEM = Registry.register(Registries.ITEM,
                itemId,
                new BlockItem(NETHERITE_SHULKER_BOX, new Item.Settings().registryKey(itemKey).fireproof().maxCount(1)));
    }
}

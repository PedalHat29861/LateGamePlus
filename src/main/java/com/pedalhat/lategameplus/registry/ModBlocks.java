package com.pedalhat.lategameplus.registry;

import com.pedalhat.lategameplus.LateGamePlus;
import com.pedalhat.lategameplus.block.NetheriteShulkerBoxBlock;
import com.pedalhat.lategameplus.block.entity.NetheriteShulkerBoxBlockEntity;
import net.minecraft.block.Block;
import net.minecraft.block.Blocks;
import net.minecraft.block.entity.BlockEntityType;
import net.minecraft.item.BlockItem;
import net.minecraft.item.Item;
import net.minecraft.registry.Registries;
import net.minecraft.registry.Registry;
import net.minecraft.util.Identifier;

/** Registers mod blocks and block entities. */
public class ModBlocks {
    public static Block NETHERITE_SHULKER_BOX;
    public static BlockEntityType<NetheriteShulkerBoxBlockEntity> NETHERITE_SHULKER_BOX_ENTITY;
    public static Item NETHERITE_SHULKER_BOX_ITEM;

    public static void init() {
        NETHERITE_SHULKER_BOX = Registry.register(Registries.BLOCK,
                Identifier.of(LateGamePlus.MOD_ID, "netherite_shulker_box"),
                new NetheriteShulkerBoxBlock(Block.Settings.copy(Blocks.SHULKER_BOX)));

        NETHERITE_SHULKER_BOX_ENTITY = Registry.register(Registries.BLOCK_ENTITY_TYPE,
                Identifier.of(LateGamePlus.MOD_ID, "netherite_shulker_box"),
                BlockEntityType.Builder.create(NetheriteShulkerBoxBlockEntity::new, NETHERITE_SHULKER_BOX).build(null));

        NETHERITE_SHULKER_BOX_ITEM = Registry.register(Registries.ITEM,
                Identifier.of(LateGamePlus.MOD_ID, "netherite_shulker_box"),
                new BlockItem(NETHERITE_SHULKER_BOX, new Item.Settings().fireproof().maxCount(1)));
    }
}

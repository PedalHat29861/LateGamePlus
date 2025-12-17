/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  org.jspecify.annotations.Nullable
 */
package net.minecraft.item;

import net.minecraft.block.Block;
import net.minecraft.block.BlockState;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.BlockItem;
import net.minecraft.item.Item;
import net.minecraft.item.ItemPlacementContext;
import org.jspecify.annotations.Nullable;

public class OperatorOnlyBlockItem
extends BlockItem {
    public OperatorOnlyBlockItem(Block block, Item.Settings settings) {
        super(block, settings);
    }

    @Override
    protected @Nullable BlockState getPlacementState(ItemPlacementContext context) {
        PlayerEntity playerEntity = context.getPlayer();
        return playerEntity == null || playerEntity.isCreativeLevelTwoOp() ? super.getPlacementState(context) : null;
    }
}


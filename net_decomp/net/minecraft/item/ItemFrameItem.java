/*
 * Decompiled with CFR 0.152.
 */
package net.minecraft.item;

import net.minecraft.entity.EntityType;
import net.minecraft.entity.decoration.AbstractDecorationEntity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.DecorationItem;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Direction;

public class ItemFrameItem
extends DecorationItem {
    public ItemFrameItem(EntityType<? extends AbstractDecorationEntity> entityType, Item.Settings settings) {
        super(entityType, settings);
    }

    @Override
    protected boolean canPlaceOn(PlayerEntity player, Direction side, ItemStack stack, BlockPos pos) {
        return !player.getEntityWorld().isOutOfHeightLimit(pos) && player.canPlaceOn(pos, side, stack);
    }
}


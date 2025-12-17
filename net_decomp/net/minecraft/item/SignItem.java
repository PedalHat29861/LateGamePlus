/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  org.jspecify.annotations.Nullable
 */
package net.minecraft.item;

import net.minecraft.block.AbstractSignBlock;
import net.minecraft.block.Block;
import net.minecraft.block.BlockState;
import net.minecraft.block.entity.SignBlockEntity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.item.VerticallyAttachableBlockItem;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Direction;
import net.minecraft.world.World;
import org.jspecify.annotations.Nullable;

public class SignItem
extends VerticallyAttachableBlockItem {
    public SignItem(Block standingBlock, Block wallBlock, Item.Settings settings) {
        super(standingBlock, wallBlock, Direction.DOWN, settings);
    }

    public SignItem(Item.Settings settings, Block standingBlock, Block wallBlock, Direction verticalAttachmentDirection) {
        super(standingBlock, wallBlock, verticalAttachmentDirection, settings);
    }

    @Override
    protected boolean postPlacement(BlockPos pos, World world, @Nullable PlayerEntity player, ItemStack stack, BlockState state) {
        Object object;
        boolean bl = super.postPlacement(pos, world, player, stack, state);
        if (!world.isClient() && !bl && player != null && (object = world.getBlockEntity(pos)) instanceof SignBlockEntity) {
            SignBlockEntity signBlockEntity = (SignBlockEntity)object;
            object = world.getBlockState(pos).getBlock();
            if (object instanceof AbstractSignBlock) {
                AbstractSignBlock abstractSignBlock = (AbstractSignBlock)object;
                abstractSignBlock.openEditScreen(player, signBlockEntity, true);
            }
        }
        return bl;
    }
}


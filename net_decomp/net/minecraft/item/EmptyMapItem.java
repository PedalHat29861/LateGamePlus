/*
 * Decompiled with CFR 0.152.
 */
package net.minecraft.item;

import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.FilledMapItem;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.sound.SoundEvents;
import net.minecraft.stat.Stats;
import net.minecraft.util.ActionResult;
import net.minecraft.util.Hand;
import net.minecraft.world.World;

public class EmptyMapItem
extends Item {
    public EmptyMapItem(Item.Settings settings) {
        super(settings);
    }

    @Override
    public ActionResult use(World world, PlayerEntity user, Hand hand) {
        ItemStack itemStack = user.getStackInHand(hand);
        if (!(world instanceof ServerWorld)) {
            return ActionResult.SUCCESS;
        }
        ServerWorld serverWorld = (ServerWorld)world;
        itemStack.decrementUnlessCreative(1, user);
        user.incrementStat(Stats.USED.getOrCreateStat(this));
        serverWorld.playSoundFromEntity(null, user, SoundEvents.UI_CARTOGRAPHY_TABLE_TAKE_RESULT, user.getSoundCategory(), 1.0f, 1.0f);
        ItemStack itemStack2 = FilledMapItem.createMap(serverWorld, user.getBlockX(), user.getBlockZ(), (byte)0, true, false);
        if (itemStack.isEmpty()) {
            return ActionResult.SUCCESS.withNewHandStack(itemStack2);
        }
        if (!user.getInventory().insertStack(itemStack2.copy())) {
            user.dropItem(itemStack2, false);
        }
        return ActionResult.SUCCESS;
    }
}


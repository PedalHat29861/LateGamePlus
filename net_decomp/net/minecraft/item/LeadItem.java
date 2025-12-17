/*
 * Decompiled with CFR 0.152.
 */
package net.minecraft.item;

import java.util.List;
import net.minecraft.block.BlockState;
import net.minecraft.entity.Leashable;
import net.minecraft.entity.decoration.LeashKnotEntity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.Item;
import net.minecraft.item.ItemUsageContext;
import net.minecraft.registry.tag.BlockTags;
import net.minecraft.util.ActionResult;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Vec3d;
import net.minecraft.world.World;
import net.minecraft.world.event.GameEvent;

public class LeadItem
extends Item {
    public LeadItem(Item.Settings settings) {
        super(settings);
    }

    @Override
    public ActionResult useOnBlock(ItemUsageContext context) {
        BlockPos blockPos;
        World world = context.getWorld();
        BlockState blockState = world.getBlockState(blockPos = context.getBlockPos());
        if (blockState.isIn(BlockTags.FENCES)) {
            PlayerEntity playerEntity = context.getPlayer();
            if (!world.isClient() && playerEntity != null) {
                return LeadItem.attachHeldMobsToBlock(playerEntity, world, blockPos);
            }
        }
        return ActionResult.PASS;
    }

    public static ActionResult attachHeldMobsToBlock(PlayerEntity player, World world, BlockPos pos) {
        LeashKnotEntity leashKnotEntity = null;
        List<Leashable> list = Leashable.collectLeashablesAround(world, Vec3d.ofCenter(pos), entity -> entity.getLeashHolder() == player);
        boolean bl = false;
        for (Leashable leashable : list) {
            if (leashKnotEntity == null) {
                leashKnotEntity = LeashKnotEntity.getOrCreate(world, pos);
                leashKnotEntity.onPlace();
            }
            if (!leashable.canBeLeashedTo(leashKnotEntity)) continue;
            leashable.attachLeash(leashKnotEntity, true);
            bl = true;
        }
        if (bl) {
            world.emitGameEvent(GameEvent.BLOCK_ATTACH, pos, GameEvent.Emitter.of(player));
            return ActionResult.SUCCESS_SERVER;
        }
        return ActionResult.PASS;
    }
}


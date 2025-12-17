/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  com.google.common.collect.ImmutableMap$Builder
 *  com.google.common.collect.Maps
 */
package net.minecraft.item;

import com.google.common.collect.ImmutableMap;
import com.google.common.collect.Maps;
import java.util.Map;
import net.minecraft.block.Block;
import net.minecraft.block.BlockState;
import net.minecraft.block.Blocks;
import net.minecraft.block.CampfireBlock;
import net.minecraft.entity.Entity;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.Item;
import net.minecraft.item.ItemUsageContext;
import net.minecraft.item.ToolMaterial;
import net.minecraft.sound.SoundCategory;
import net.minecraft.sound.SoundEvents;
import net.minecraft.util.ActionResult;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Direction;
import net.minecraft.world.World;
import net.minecraft.world.event.GameEvent;

public class ShovelItem
extends Item {
    protected static final Map<Block, BlockState> PATH_STATES = Maps.newHashMap((Map)new ImmutableMap.Builder().put((Object)Blocks.GRASS_BLOCK, (Object)Blocks.DIRT_PATH.getDefaultState()).put((Object)Blocks.DIRT, (Object)Blocks.DIRT_PATH.getDefaultState()).put((Object)Blocks.PODZOL, (Object)Blocks.DIRT_PATH.getDefaultState()).put((Object)Blocks.COARSE_DIRT, (Object)Blocks.DIRT_PATH.getDefaultState()).put((Object)Blocks.MYCELIUM, (Object)Blocks.DIRT_PATH.getDefaultState()).put((Object)Blocks.ROOTED_DIRT, (Object)Blocks.DIRT_PATH.getDefaultState()).build());

    public ShovelItem(ToolMaterial material, float attackDamage, float attackSpeed, Item.Settings settings) {
        super(settings.shovel(material, attackDamage, attackSpeed));
    }

    @Override
    public ActionResult useOnBlock(ItemUsageContext context) {
        World world = context.getWorld();
        BlockPos blockPos = context.getBlockPos();
        BlockState blockState = world.getBlockState(blockPos);
        if (context.getSide() != Direction.DOWN) {
            PlayerEntity playerEntity = context.getPlayer();
            BlockState blockState2 = PATH_STATES.get(blockState.getBlock());
            BlockState blockState3 = null;
            if (blockState2 != null && world.getBlockState(blockPos.up()).isAir()) {
                world.playSound((Entity)playerEntity, blockPos, SoundEvents.ITEM_SHOVEL_FLATTEN, SoundCategory.BLOCKS, 1.0f, 1.0f);
                blockState3 = blockState2;
            } else if (blockState.getBlock() instanceof CampfireBlock && blockState.get(CampfireBlock.LIT).booleanValue()) {
                if (!world.isClient()) {
                    world.syncWorldEvent(null, 1009, blockPos, 0);
                }
                CampfireBlock.extinguish(context.getPlayer(), world, blockPos, blockState);
                blockState3 = (BlockState)blockState.with(CampfireBlock.LIT, false);
            }
            if (blockState3 != null) {
                if (!world.isClient()) {
                    world.setBlockState(blockPos, blockState3, 11);
                    world.emitGameEvent(GameEvent.BLOCK_CHANGE, blockPos, GameEvent.Emitter.of(playerEntity, blockState3));
                    if (playerEntity != null) {
                        context.getStack().damage(1, (LivingEntity)playerEntity, context.getHand().getEquipmentSlot());
                    }
                }
                return ActionResult.SUCCESS;
            }
            return ActionResult.PASS;
        }
        return ActionResult.PASS;
    }
}


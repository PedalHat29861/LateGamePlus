/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  com.google.common.collect.ImmutableMap$Builder
 *  org.jspecify.annotations.Nullable
 */
package net.minecraft.item;

import com.google.common.collect.ImmutableMap;
import java.util.Map;
import java.util.Optional;
import net.minecraft.advancement.criterion.Criteria;
import net.minecraft.block.Block;
import net.minecraft.block.BlockState;
import net.minecraft.block.Blocks;
import net.minecraft.block.ChestBlock;
import net.minecraft.block.Oxidizable;
import net.minecraft.block.PillarBlock;
import net.minecraft.block.enums.ChestType;
import net.minecraft.component.DataComponentTypes;
import net.minecraft.entity.Entity;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.HoneycombItem;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.item.ItemUsageContext;
import net.minecraft.item.ToolMaterial;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.sound.SoundCategory;
import net.minecraft.sound.SoundEvent;
import net.minecraft.sound.SoundEvents;
import net.minecraft.util.ActionResult;
import net.minecraft.util.Hand;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;
import net.minecraft.world.event.GameEvent;
import org.jspecify.annotations.Nullable;

public class AxeItem
extends Item {
    protected static final Map<Block, Block> STRIPPED_BLOCKS = new ImmutableMap.Builder().put((Object)Blocks.OAK_WOOD, (Object)Blocks.STRIPPED_OAK_WOOD).put((Object)Blocks.OAK_LOG, (Object)Blocks.STRIPPED_OAK_LOG).put((Object)Blocks.DARK_OAK_WOOD, (Object)Blocks.STRIPPED_DARK_OAK_WOOD).put((Object)Blocks.DARK_OAK_LOG, (Object)Blocks.STRIPPED_DARK_OAK_LOG).put((Object)Blocks.PALE_OAK_WOOD, (Object)Blocks.STRIPPED_PALE_OAK_WOOD).put((Object)Blocks.PALE_OAK_LOG, (Object)Blocks.STRIPPED_PALE_OAK_LOG).put((Object)Blocks.ACACIA_WOOD, (Object)Blocks.STRIPPED_ACACIA_WOOD).put((Object)Blocks.ACACIA_LOG, (Object)Blocks.STRIPPED_ACACIA_LOG).put((Object)Blocks.CHERRY_WOOD, (Object)Blocks.STRIPPED_CHERRY_WOOD).put((Object)Blocks.CHERRY_LOG, (Object)Blocks.STRIPPED_CHERRY_LOG).put((Object)Blocks.BIRCH_WOOD, (Object)Blocks.STRIPPED_BIRCH_WOOD).put((Object)Blocks.BIRCH_LOG, (Object)Blocks.STRIPPED_BIRCH_LOG).put((Object)Blocks.JUNGLE_WOOD, (Object)Blocks.STRIPPED_JUNGLE_WOOD).put((Object)Blocks.JUNGLE_LOG, (Object)Blocks.STRIPPED_JUNGLE_LOG).put((Object)Blocks.SPRUCE_WOOD, (Object)Blocks.STRIPPED_SPRUCE_WOOD).put((Object)Blocks.SPRUCE_LOG, (Object)Blocks.STRIPPED_SPRUCE_LOG).put((Object)Blocks.WARPED_STEM, (Object)Blocks.STRIPPED_WARPED_STEM).put((Object)Blocks.WARPED_HYPHAE, (Object)Blocks.STRIPPED_WARPED_HYPHAE).put((Object)Blocks.CRIMSON_STEM, (Object)Blocks.STRIPPED_CRIMSON_STEM).put((Object)Blocks.CRIMSON_HYPHAE, (Object)Blocks.STRIPPED_CRIMSON_HYPHAE).put((Object)Blocks.MANGROVE_WOOD, (Object)Blocks.STRIPPED_MANGROVE_WOOD).put((Object)Blocks.MANGROVE_LOG, (Object)Blocks.STRIPPED_MANGROVE_LOG).put((Object)Blocks.BAMBOO_BLOCK, (Object)Blocks.STRIPPED_BAMBOO_BLOCK).build();

    public AxeItem(ToolMaterial material, float attackDamage, float attackSpeed, Item.Settings settings) {
        super(settings.axe(material, attackDamage, attackSpeed));
    }

    @Override
    public ActionResult useOnBlock(ItemUsageContext context) {
        World world = context.getWorld();
        BlockPos blockPos = context.getBlockPos();
        PlayerEntity playerEntity = context.getPlayer();
        if (AxeItem.shouldCancelStripAttempt(context)) {
            return ActionResult.PASS;
        }
        Optional<BlockState> optional = this.tryStrip(world, blockPos, playerEntity, world.getBlockState(blockPos));
        if (optional.isEmpty()) {
            return ActionResult.PASS;
        }
        ItemStack itemStack = context.getStack();
        if (playerEntity instanceof ServerPlayerEntity) {
            Criteria.ITEM_USED_ON_BLOCK.trigger((ServerPlayerEntity)playerEntity, blockPos, itemStack);
        }
        world.setBlockState(blockPos, optional.get(), 11);
        world.emitGameEvent(GameEvent.BLOCK_CHANGE, blockPos, GameEvent.Emitter.of(playerEntity, optional.get()));
        if (playerEntity != null) {
            itemStack.damage(1, (LivingEntity)playerEntity, context.getHand().getEquipmentSlot());
        }
        return ActionResult.SUCCESS;
    }

    private static boolean shouldCancelStripAttempt(ItemUsageContext context) {
        PlayerEntity playerEntity = context.getPlayer();
        return context.getHand().equals((Object)Hand.MAIN_HAND) && playerEntity.getOffHandStack().contains(DataComponentTypes.BLOCKS_ATTACKS) && !playerEntity.shouldCancelInteraction();
    }

    private Optional<BlockState> tryStrip(World world, BlockPos pos, @Nullable PlayerEntity player, BlockState state) {
        Optional<BlockState> optional = this.getStrippedState(state);
        if (optional.isPresent()) {
            world.playSound((Entity)player, pos, SoundEvents.ITEM_AXE_STRIP, SoundCategory.BLOCKS, 1.0f, 1.0f);
            return optional;
        }
        Optional<BlockState> optional2 = Oxidizable.getDecreasedOxidationState(state);
        if (optional2.isPresent()) {
            AxeItem.strip(world, pos, player, state, SoundEvents.ITEM_AXE_SCRAPE, 3005);
            return optional2;
        }
        Optional<BlockState> optional3 = Optional.ofNullable((Block)HoneycombItem.WAXED_TO_UNWAXED_BLOCKS.get().get((Object)state.getBlock())).map(block -> block.getStateWithProperties(state));
        if (optional3.isPresent()) {
            AxeItem.strip(world, pos, player, state, SoundEvents.ITEM_AXE_WAX_OFF, 3004);
            return optional3;
        }
        return Optional.empty();
    }

    private static void strip(World world, BlockPos pos, @Nullable PlayerEntity player, BlockState state, SoundEvent sound, int worldEvent) {
        world.playSound((Entity)player, pos, sound, SoundCategory.BLOCKS, 1.0f, 1.0f);
        world.syncWorldEvent(player, worldEvent, pos, 0);
        if (state.getBlock() instanceof ChestBlock && state.get(ChestBlock.CHEST_TYPE) != ChestType.SINGLE) {
            BlockPos blockPos = ChestBlock.getPosInFrontOf(pos, state);
            world.emitGameEvent(GameEvent.BLOCK_CHANGE, blockPos, GameEvent.Emitter.of(player, world.getBlockState(blockPos)));
            world.syncWorldEvent(player, worldEvent, blockPos, 0);
        }
    }

    private Optional<BlockState> getStrippedState(BlockState state) {
        return Optional.ofNullable(STRIPPED_BLOCKS.get(state.getBlock())).map(block -> (BlockState)block.getDefaultState().with(PillarBlock.AXIS, state.get(PillarBlock.AXIS)));
    }
}


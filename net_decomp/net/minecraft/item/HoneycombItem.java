/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  com.google.common.base.Suppliers
 *  com.google.common.collect.BiMap
 *  com.google.common.collect.ImmutableBiMap
 *  com.google.common.collect.ImmutableMap
 *  com.mojang.datafixers.util.Pair
 */
package net.minecraft.item;

import com.google.common.base.Suppliers;
import com.google.common.collect.BiMap;
import com.google.common.collect.ImmutableBiMap;
import com.google.common.collect.ImmutableMap;
import com.mojang.datafixers.util.Pair;
import java.util.Optional;
import java.util.function.Supplier;
import net.minecraft.advancement.criterion.Criteria;
import net.minecraft.block.Block;
import net.minecraft.block.BlockState;
import net.minecraft.block.Blocks;
import net.minecraft.block.ChestBlock;
import net.minecraft.block.entity.SignBlockEntity;
import net.minecraft.block.entity.SignText;
import net.minecraft.block.enums.ChestType;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.item.ItemUsageContext;
import net.minecraft.item.SignChangingItem;
import net.minecraft.recipe.book.RecipeCategory;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.util.ActionResult;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;
import net.minecraft.world.event.GameEvent;

public class HoneycombItem
extends Item
implements SignChangingItem {
    public static final Supplier<BiMap<Block, Block>> UNWAXED_TO_WAXED_BLOCKS = Suppliers.memoize(() -> ImmutableBiMap.builder().put((Object)Blocks.COPPER_BLOCK, (Object)Blocks.WAXED_COPPER_BLOCK).put((Object)Blocks.EXPOSED_COPPER, (Object)Blocks.WAXED_EXPOSED_COPPER).put((Object)Blocks.WEATHERED_COPPER, (Object)Blocks.WAXED_WEATHERED_COPPER).put((Object)Blocks.OXIDIZED_COPPER, (Object)Blocks.WAXED_OXIDIZED_COPPER).put((Object)Blocks.CUT_COPPER, (Object)Blocks.WAXED_CUT_COPPER).put((Object)Blocks.EXPOSED_CUT_COPPER, (Object)Blocks.WAXED_EXPOSED_CUT_COPPER).put((Object)Blocks.WEATHERED_CUT_COPPER, (Object)Blocks.WAXED_WEATHERED_CUT_COPPER).put((Object)Blocks.OXIDIZED_CUT_COPPER, (Object)Blocks.WAXED_OXIDIZED_CUT_COPPER).put((Object)Blocks.CUT_COPPER_SLAB, (Object)Blocks.WAXED_CUT_COPPER_SLAB).put((Object)Blocks.EXPOSED_CUT_COPPER_SLAB, (Object)Blocks.WAXED_EXPOSED_CUT_COPPER_SLAB).put((Object)Blocks.WEATHERED_CUT_COPPER_SLAB, (Object)Blocks.WAXED_WEATHERED_CUT_COPPER_SLAB).put((Object)Blocks.OXIDIZED_CUT_COPPER_SLAB, (Object)Blocks.WAXED_OXIDIZED_CUT_COPPER_SLAB).put((Object)Blocks.CUT_COPPER_STAIRS, (Object)Blocks.WAXED_CUT_COPPER_STAIRS).put((Object)Blocks.EXPOSED_CUT_COPPER_STAIRS, (Object)Blocks.WAXED_EXPOSED_CUT_COPPER_STAIRS).put((Object)Blocks.WEATHERED_CUT_COPPER_STAIRS, (Object)Blocks.WAXED_WEATHERED_CUT_COPPER_STAIRS).put((Object)Blocks.OXIDIZED_CUT_COPPER_STAIRS, (Object)Blocks.WAXED_OXIDIZED_CUT_COPPER_STAIRS).put((Object)Blocks.CHISELED_COPPER, (Object)Blocks.WAXED_CHISELED_COPPER).put((Object)Blocks.EXPOSED_CHISELED_COPPER, (Object)Blocks.WAXED_EXPOSED_CHISELED_COPPER).put((Object)Blocks.WEATHERED_CHISELED_COPPER, (Object)Blocks.WAXED_WEATHERED_CHISELED_COPPER).put((Object)Blocks.OXIDIZED_CHISELED_COPPER, (Object)Blocks.WAXED_OXIDIZED_CHISELED_COPPER).put((Object)Blocks.COPPER_DOOR, (Object)Blocks.WAXED_COPPER_DOOR).put((Object)Blocks.EXPOSED_COPPER_DOOR, (Object)Blocks.WAXED_EXPOSED_COPPER_DOOR).put((Object)Blocks.WEATHERED_COPPER_DOOR, (Object)Blocks.WAXED_WEATHERED_COPPER_DOOR).put((Object)Blocks.OXIDIZED_COPPER_DOOR, (Object)Blocks.WAXED_OXIDIZED_COPPER_DOOR).put((Object)Blocks.COPPER_TRAPDOOR, (Object)Blocks.WAXED_COPPER_TRAPDOOR).put((Object)Blocks.EXPOSED_COPPER_TRAPDOOR, (Object)Blocks.WAXED_EXPOSED_COPPER_TRAPDOOR).put((Object)Blocks.WEATHERED_COPPER_TRAPDOOR, (Object)Blocks.WAXED_WEATHERED_COPPER_TRAPDOOR).put((Object)Blocks.OXIDIZED_COPPER_TRAPDOOR, (Object)Blocks.WAXED_OXIDIZED_COPPER_TRAPDOOR).putAll(Blocks.COPPER_BARS.getWaxingMap()).put((Object)Blocks.COPPER_GRATE, (Object)Blocks.WAXED_COPPER_GRATE).put((Object)Blocks.EXPOSED_COPPER_GRATE, (Object)Blocks.WAXED_EXPOSED_COPPER_GRATE).put((Object)Blocks.WEATHERED_COPPER_GRATE, (Object)Blocks.WAXED_WEATHERED_COPPER_GRATE).put((Object)Blocks.OXIDIZED_COPPER_GRATE, (Object)Blocks.WAXED_OXIDIZED_COPPER_GRATE).put((Object)Blocks.COPPER_BULB, (Object)Blocks.WAXED_COPPER_BULB).put((Object)Blocks.EXPOSED_COPPER_BULB, (Object)Blocks.WAXED_EXPOSED_COPPER_BULB).put((Object)Blocks.WEATHERED_COPPER_BULB, (Object)Blocks.WAXED_WEATHERED_COPPER_BULB).put((Object)Blocks.OXIDIZED_COPPER_BULB, (Object)Blocks.WAXED_OXIDIZED_COPPER_BULB).put((Object)Blocks.COPPER_CHEST, (Object)Blocks.WAXED_COPPER_CHEST).put((Object)Blocks.EXPOSED_COPPER_CHEST, (Object)Blocks.WAXED_EXPOSED_COPPER_CHEST).put((Object)Blocks.WEATHERED_COPPER_CHEST, (Object)Blocks.WAXED_WEATHERED_COPPER_CHEST).put((Object)Blocks.OXIDIZED_COPPER_CHEST, (Object)Blocks.WAXED_OXIDIZED_COPPER_CHEST).put((Object)Blocks.COPPER_GOLEM_STATUE, (Object)Blocks.WAXED_COPPER_GOLEM_STATUE).put((Object)Blocks.EXPOSED_COPPER_GOLEM_STATUE, (Object)Blocks.WAXED_EXPOSED_COPPER_GOLEM_STATUE).put((Object)Blocks.WEATHERED_COPPER_GOLEM_STATUE, (Object)Blocks.WAXED_WEATHERED_COPPER_GOLEM_STATUE).put((Object)Blocks.OXIDIZED_COPPER_GOLEM_STATUE, (Object)Blocks.WAXED_OXIDIZED_COPPER_GOLEM_STATUE).put((Object)Blocks.LIGHTNING_ROD, (Object)Blocks.WAXED_LIGHTNING_ROD).put((Object)Blocks.EXPOSED_LIGHTNING_ROD, (Object)Blocks.WAXED_EXPOSED_LIGHTNING_ROD).put((Object)Blocks.WEATHERED_LIGHTNING_ROD, (Object)Blocks.WAXED_WEATHERED_LIGHTNING_ROD).put((Object)Blocks.OXIDIZED_LIGHTNING_ROD, (Object)Blocks.WAXED_OXIDIZED_LIGHTNING_ROD).putAll(Blocks.COPPER_LANTERNS.getWaxingMap()).putAll(Blocks.COPPER_CHAINS.getWaxingMap()).build());
    public static final Supplier<BiMap<Block, Block>> WAXED_TO_UNWAXED_BLOCKS = Suppliers.memoize(() -> UNWAXED_TO_WAXED_BLOCKS.get().inverse());
    private static final String WAXED_COPPER_DOOR_GROUP = "waxed_copper_door";
    private static final String WAXED_COPPER_TRAPDOOR_GROUP = "waxed_copper_trapdoor";
    private static final String WAXED_COPPER_GOLEM_STATUE_GROUP = "waxed_copper_golem_statue";
    private static final String WAXED_COPPER_CHEST_GROUP = "waxed_copper_chest";
    private static final String WAXED_LIGHTNING_ROD_GROUP = "waxed_lightning_rod";
    private static final String WAXED_COPPER_BAR_GROUP = "waxed_copper_bar";
    private static final String WAXED_COPPER_CHAIN_GROUP = "waxed_copper_chain";
    private static final String WAXED_COPPER_LANTERN_GROUP = "waxed_copper_lantern";
    private static final String WAXED_COPPER_BLOCK_GROUP = "waxed_copper_block";
    public static final ImmutableMap<Block, Pair<RecipeCategory, String>> WAXED_RECIPE_GROUPS = ImmutableMap.builder().put((Object)Blocks.WAXED_COPPER_BULB, (Object)Pair.of((Object)((Object)RecipeCategory.REDSTONE), (Object)"waxed_copper_bulb")).put((Object)Blocks.WAXED_WEATHERED_COPPER_BULB, (Object)Pair.of((Object)((Object)RecipeCategory.REDSTONE), (Object)"waxed_weathered_copper_bulb")).put((Object)Blocks.WAXED_EXPOSED_COPPER_BULB, (Object)Pair.of((Object)((Object)RecipeCategory.REDSTONE), (Object)"waxed_exposed_copper_bulb")).put((Object)Blocks.WAXED_OXIDIZED_COPPER_BULB, (Object)Pair.of((Object)((Object)RecipeCategory.REDSTONE), (Object)"waxed_oxidized_copper_bulb")).put((Object)Blocks.WAXED_COPPER_DOOR, (Object)Pair.of((Object)((Object)RecipeCategory.REDSTONE), (Object)"waxed_copper_door")).put((Object)Blocks.WAXED_WEATHERED_COPPER_DOOR, (Object)Pair.of((Object)((Object)RecipeCategory.REDSTONE), (Object)"waxed_copper_door")).put((Object)Blocks.WAXED_EXPOSED_COPPER_DOOR, (Object)Pair.of((Object)((Object)RecipeCategory.REDSTONE), (Object)"waxed_copper_door")).put((Object)Blocks.WAXED_OXIDIZED_COPPER_DOOR, (Object)Pair.of((Object)((Object)RecipeCategory.REDSTONE), (Object)"waxed_copper_door")).put((Object)Blocks.WAXED_COPPER_TRAPDOOR, (Object)Pair.of((Object)((Object)RecipeCategory.REDSTONE), (Object)"waxed_copper_trapdoor")).put((Object)Blocks.WAXED_WEATHERED_COPPER_TRAPDOOR, (Object)Pair.of((Object)((Object)RecipeCategory.REDSTONE), (Object)"waxed_copper_trapdoor")).put((Object)Blocks.WAXED_EXPOSED_COPPER_TRAPDOOR, (Object)Pair.of((Object)((Object)RecipeCategory.REDSTONE), (Object)"waxed_copper_trapdoor")).put((Object)Blocks.WAXED_OXIDIZED_COPPER_TRAPDOOR, (Object)Pair.of((Object)((Object)RecipeCategory.REDSTONE), (Object)"waxed_copper_trapdoor")).put((Object)Blocks.WAXED_COPPER_GOLEM_STATUE, (Object)Pair.of((Object)((Object)RecipeCategory.BUILDING_BLOCKS), (Object)"waxed_copper_golem_statue")).put((Object)Blocks.WAXED_WEATHERED_COPPER_GOLEM_STATUE, (Object)Pair.of((Object)((Object)RecipeCategory.BUILDING_BLOCKS), (Object)"waxed_copper_golem_statue")).put((Object)Blocks.WAXED_EXPOSED_COPPER_GOLEM_STATUE, (Object)Pair.of((Object)((Object)RecipeCategory.BUILDING_BLOCKS), (Object)"waxed_copper_golem_statue")).put((Object)Blocks.WAXED_OXIDIZED_COPPER_GOLEM_STATUE, (Object)Pair.of((Object)((Object)RecipeCategory.BUILDING_BLOCKS), (Object)"waxed_copper_golem_statue")).put((Object)Blocks.WAXED_COPPER_CHEST, (Object)Pair.of((Object)((Object)RecipeCategory.BUILDING_BLOCKS), (Object)"waxed_copper_chest")).put((Object)Blocks.WAXED_WEATHERED_COPPER_CHEST, (Object)Pair.of((Object)((Object)RecipeCategory.BUILDING_BLOCKS), (Object)"waxed_copper_chest")).put((Object)Blocks.WAXED_EXPOSED_COPPER_CHEST, (Object)Pair.of((Object)((Object)RecipeCategory.BUILDING_BLOCKS), (Object)"waxed_copper_chest")).put((Object)Blocks.WAXED_OXIDIZED_COPPER_CHEST, (Object)Pair.of((Object)((Object)RecipeCategory.BUILDING_BLOCKS), (Object)"waxed_copper_chest")).put((Object)Blocks.WAXED_LIGHTNING_ROD, (Object)Pair.of((Object)((Object)RecipeCategory.BUILDING_BLOCKS), (Object)"waxed_lightning_rod")).put((Object)Blocks.WAXED_WEATHERED_LIGHTNING_ROD, (Object)Pair.of((Object)((Object)RecipeCategory.BUILDING_BLOCKS), (Object)"waxed_lightning_rod")).put((Object)Blocks.WAXED_EXPOSED_LIGHTNING_ROD, (Object)Pair.of((Object)((Object)RecipeCategory.BUILDING_BLOCKS), (Object)"waxed_lightning_rod")).put((Object)Blocks.WAXED_OXIDIZED_LIGHTNING_ROD, (Object)Pair.of((Object)((Object)RecipeCategory.BUILDING_BLOCKS), (Object)"waxed_lightning_rod")).put((Object)Blocks.COPPER_BARS.waxed(), (Object)Pair.of((Object)((Object)RecipeCategory.BUILDING_BLOCKS), (Object)"waxed_copper_bar")).put((Object)Blocks.COPPER_BARS.waxedWeathered(), (Object)Pair.of((Object)((Object)RecipeCategory.BUILDING_BLOCKS), (Object)"waxed_copper_bar")).put((Object)Blocks.COPPER_BARS.waxedExposed(), (Object)Pair.of((Object)((Object)RecipeCategory.BUILDING_BLOCKS), (Object)"waxed_copper_bar")).put((Object)Blocks.COPPER_BARS.waxedOxidized(), (Object)Pair.of((Object)((Object)RecipeCategory.BUILDING_BLOCKS), (Object)"waxed_copper_bar")).put((Object)Blocks.COPPER_CHAINS.waxed(), (Object)Pair.of((Object)((Object)RecipeCategory.BUILDING_BLOCKS), (Object)"waxed_copper_chain")).put((Object)Blocks.COPPER_CHAINS.waxedWeathered(), (Object)Pair.of((Object)((Object)RecipeCategory.BUILDING_BLOCKS), (Object)"waxed_copper_chain")).put((Object)Blocks.COPPER_CHAINS.waxedExposed(), (Object)Pair.of((Object)((Object)RecipeCategory.BUILDING_BLOCKS), (Object)"waxed_copper_chain")).put((Object)Blocks.COPPER_CHAINS.waxedOxidized(), (Object)Pair.of((Object)((Object)RecipeCategory.BUILDING_BLOCKS), (Object)"waxed_copper_chain")).put((Object)Blocks.COPPER_LANTERNS.waxed(), (Object)Pair.of((Object)((Object)RecipeCategory.BUILDING_BLOCKS), (Object)"waxed_copper_lantern")).put((Object)Blocks.COPPER_LANTERNS.waxedWeathered(), (Object)Pair.of((Object)((Object)RecipeCategory.BUILDING_BLOCKS), (Object)"waxed_copper_lantern")).put((Object)Blocks.COPPER_LANTERNS.waxedExposed(), (Object)Pair.of((Object)((Object)RecipeCategory.BUILDING_BLOCKS), (Object)"waxed_copper_lantern")).put((Object)Blocks.COPPER_LANTERNS.waxedOxidized(), (Object)Pair.of((Object)((Object)RecipeCategory.BUILDING_BLOCKS), (Object)"waxed_copper_lantern")).put((Object)Blocks.WAXED_COPPER_BLOCK, (Object)Pair.of((Object)((Object)RecipeCategory.BUILDING_BLOCKS), (Object)"waxed_copper_block")).put((Object)Blocks.WAXED_WEATHERED_COPPER, (Object)Pair.of((Object)((Object)RecipeCategory.BUILDING_BLOCKS), (Object)"waxed_copper_block")).put((Object)Blocks.WAXED_EXPOSED_COPPER, (Object)Pair.of((Object)((Object)RecipeCategory.BUILDING_BLOCKS), (Object)"waxed_copper_block")).put((Object)Blocks.WAXED_OXIDIZED_COPPER, (Object)Pair.of((Object)((Object)RecipeCategory.BUILDING_BLOCKS), (Object)"waxed_copper_block")).build();

    public HoneycombItem(Item.Settings settings) {
        super(settings);
    }

    @Override
    public ActionResult useOnBlock(ItemUsageContext context) {
        World world = context.getWorld();
        BlockPos blockPos = context.getBlockPos();
        BlockState blockState = world.getBlockState(blockPos);
        return HoneycombItem.getWaxedState(blockState).map(state -> {
            PlayerEntity playerEntity = context.getPlayer();
            ItemStack itemStack = context.getStack();
            if (playerEntity instanceof ServerPlayerEntity) {
                ServerPlayerEntity serverPlayerEntity = (ServerPlayerEntity)playerEntity;
                Criteria.ITEM_USED_ON_BLOCK.trigger(serverPlayerEntity, blockPos, itemStack);
            }
            itemStack.decrement(1);
            world.setBlockState(blockPos, (BlockState)state, 11);
            world.emitGameEvent(GameEvent.BLOCK_CHANGE, blockPos, GameEvent.Emitter.of(playerEntity, state));
            world.syncWorldEvent(playerEntity, 3003, blockPos, 0);
            if (blockState.getBlock() instanceof ChestBlock && blockState.get(ChestBlock.CHEST_TYPE) != ChestType.SINGLE) {
                BlockPos blockPos2 = ChestBlock.getPosInFrontOf(blockPos, blockState);
                world.emitGameEvent(GameEvent.BLOCK_CHANGE, blockPos2, GameEvent.Emitter.of(playerEntity, world.getBlockState(blockPos2)));
                world.syncWorldEvent(playerEntity, 3003, blockPos2, 0);
            }
            return ActionResult.SUCCESS;
        }).orElse(ActionResult.PASS);
    }

    public static Optional<BlockState> getWaxedState(BlockState state) {
        return Optional.ofNullable((Block)UNWAXED_TO_WAXED_BLOCKS.get().get((Object)state.getBlock())).map(block -> block.getStateWithProperties(state));
    }

    @Override
    public boolean useOnSign(World world, SignBlockEntity signBlockEntity, boolean front, PlayerEntity player) {
        if (signBlockEntity.setWaxed(true)) {
            world.syncWorldEvent(null, 3003, signBlockEntity.getPos(), 0);
            return true;
        }
        return false;
    }

    @Override
    public boolean canUseOnSignText(SignText signText, PlayerEntity player) {
        return true;
    }
}


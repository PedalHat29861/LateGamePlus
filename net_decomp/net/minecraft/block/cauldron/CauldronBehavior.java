/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  com.mojang.serialization.Codec
 *  it.unimi.dsi.fastutil.objects.Object2ObjectArrayMap
 *  it.unimi.dsi.fastutil.objects.Object2ObjectOpenHashMap
 */
package net.minecraft.block.cauldron;

import com.mojang.serialization.Codec;
import it.unimi.dsi.fastutil.objects.Object2ObjectArrayMap;
import it.unimi.dsi.fastutil.objects.Object2ObjectOpenHashMap;
import java.util.Map;
import java.util.function.Predicate;
import net.minecraft.block.Block;
import net.minecraft.block.BlockState;
import net.minecraft.block.Blocks;
import net.minecraft.block.LeveledCauldronBlock;
import net.minecraft.block.ShulkerBoxBlock;
import net.minecraft.component.DataComponentTypes;
import net.minecraft.component.type.BannerPatternsComponent;
import net.minecraft.component.type.PotionContentsComponent;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.fluid.FluidState;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.item.ItemUsage;
import net.minecraft.item.Items;
import net.minecraft.potion.Potions;
import net.minecraft.registry.tag.FluidTags;
import net.minecraft.registry.tag.ItemTags;
import net.minecraft.sound.SoundCategory;
import net.minecraft.sound.SoundEvent;
import net.minecraft.sound.SoundEvents;
import net.minecraft.stat.Stats;
import net.minecraft.util.ActionResult;
import net.minecraft.util.Hand;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;
import net.minecraft.world.event.GameEvent;

public interface CauldronBehavior {
    public static final Map<String, CauldronBehaviorMap> BEHAVIOR_MAPS = new Object2ObjectArrayMap();
    public static final Codec<CauldronBehaviorMap> CODEC = Codec.stringResolver(CauldronBehaviorMap::name, BEHAVIOR_MAPS::get);
    public static final CauldronBehaviorMap EMPTY_CAULDRON_BEHAVIOR = CauldronBehavior.createMap("empty");
    public static final CauldronBehaviorMap WATER_CAULDRON_BEHAVIOR = CauldronBehavior.createMap("water");
    public static final CauldronBehaviorMap LAVA_CAULDRON_BEHAVIOR = CauldronBehavior.createMap("lava");
    public static final CauldronBehaviorMap POWDER_SNOW_CAULDRON_BEHAVIOR = CauldronBehavior.createMap("powder_snow");

    public static CauldronBehaviorMap createMap(String name) {
        Object2ObjectOpenHashMap object2ObjectOpenHashMap = new Object2ObjectOpenHashMap();
        object2ObjectOpenHashMap.defaultReturnValue((state, world, pos, player, hand, stack) -> ActionResult.PASS_TO_DEFAULT_BLOCK_ACTION);
        CauldronBehaviorMap cauldronBehaviorMap = new CauldronBehaviorMap(name, (Map<Item, CauldronBehavior>)object2ObjectOpenHashMap);
        BEHAVIOR_MAPS.put(name, cauldronBehaviorMap);
        return cauldronBehaviorMap;
    }

    public ActionResult interact(BlockState var1, World var2, BlockPos var3, PlayerEntity var4, Hand var5, ItemStack var6);

    public static void registerBehavior() {
        Map<Item, CauldronBehavior> map = EMPTY_CAULDRON_BEHAVIOR.map();
        CauldronBehavior.registerBucketBehavior(map);
        map.put(Items.POTION, (state, world, pos, player, hand, stack) -> {
            PotionContentsComponent potionContentsComponent = stack.get(DataComponentTypes.POTION_CONTENTS);
            if (potionContentsComponent == null || !potionContentsComponent.matches(Potions.WATER)) {
                return ActionResult.PASS_TO_DEFAULT_BLOCK_ACTION;
            }
            if (!world.isClient()) {
                Item item = stack.getItem();
                player.setStackInHand(hand, ItemUsage.exchangeStack(stack, player, new ItemStack(Items.GLASS_BOTTLE)));
                player.incrementStat(Stats.USE_CAULDRON);
                player.incrementStat(Stats.USED.getOrCreateStat(item));
                world.setBlockState(pos, Blocks.WATER_CAULDRON.getDefaultState());
                world.playSound(null, pos, SoundEvents.ITEM_BOTTLE_EMPTY, SoundCategory.BLOCKS, 1.0f, 1.0f);
                world.emitGameEvent(null, GameEvent.FLUID_PLACE, pos);
            }
            return ActionResult.SUCCESS;
        });
        Map<Item, CauldronBehavior> map2 = WATER_CAULDRON_BEHAVIOR.map();
        CauldronBehavior.registerBucketBehavior(map2);
        map2.put(Items.BUCKET, (state, world, pos, player, hand, stack) -> CauldronBehavior.emptyCauldron(state, world, pos, player, hand, stack, new ItemStack(Items.WATER_BUCKET), statex -> statex.get(LeveledCauldronBlock.LEVEL) == 3, SoundEvents.ITEM_BUCKET_FILL));
        map2.put(Items.GLASS_BOTTLE, (state, world, pos, player, hand, stack) -> {
            if (!world.isClient()) {
                Item item = stack.getItem();
                player.setStackInHand(hand, ItemUsage.exchangeStack(stack, player, PotionContentsComponent.createStack(Items.POTION, Potions.WATER)));
                player.incrementStat(Stats.USE_CAULDRON);
                player.incrementStat(Stats.USED.getOrCreateStat(item));
                LeveledCauldronBlock.decrementFluidLevel(state, world, pos);
                world.playSound(null, pos, SoundEvents.ITEM_BOTTLE_FILL, SoundCategory.BLOCKS, 1.0f, 1.0f);
                world.emitGameEvent(null, GameEvent.FLUID_PICKUP, pos);
            }
            return ActionResult.SUCCESS;
        });
        map2.put(Items.POTION, (state, world, pos, player, hand, stack) -> {
            if (state.get(LeveledCauldronBlock.LEVEL) == 3) {
                return ActionResult.PASS_TO_DEFAULT_BLOCK_ACTION;
            }
            PotionContentsComponent potionContentsComponent = stack.get(DataComponentTypes.POTION_CONTENTS);
            if (potionContentsComponent == null || !potionContentsComponent.matches(Potions.WATER)) {
                return ActionResult.PASS_TO_DEFAULT_BLOCK_ACTION;
            }
            if (!world.isClient()) {
                player.setStackInHand(hand, ItemUsage.exchangeStack(stack, player, new ItemStack(Items.GLASS_BOTTLE)));
                player.incrementStat(Stats.USE_CAULDRON);
                player.incrementStat(Stats.USED.getOrCreateStat(stack.getItem()));
                world.setBlockState(pos, (BlockState)state.cycle(LeveledCauldronBlock.LEVEL));
                world.playSound(null, pos, SoundEvents.ITEM_BOTTLE_EMPTY, SoundCategory.BLOCKS, 1.0f, 1.0f);
                world.emitGameEvent(null, GameEvent.FLUID_PLACE, pos);
            }
            return ActionResult.SUCCESS;
        });
        map2.put(Items.LEATHER_BOOTS, CauldronBehavior::cleanArmor);
        map2.put(Items.LEATHER_LEGGINGS, CauldronBehavior::cleanArmor);
        map2.put(Items.LEATHER_CHESTPLATE, CauldronBehavior::cleanArmor);
        map2.put(Items.LEATHER_HELMET, CauldronBehavior::cleanArmor);
        map2.put(Items.LEATHER_HORSE_ARMOR, CauldronBehavior::cleanArmor);
        map2.put(Items.WOLF_ARMOR, CauldronBehavior::cleanArmor);
        map2.put(Items.WHITE_BANNER, CauldronBehavior::cleanBanner);
        map2.put(Items.GRAY_BANNER, CauldronBehavior::cleanBanner);
        map2.put(Items.BLACK_BANNER, CauldronBehavior::cleanBanner);
        map2.put(Items.BLUE_BANNER, CauldronBehavior::cleanBanner);
        map2.put(Items.BROWN_BANNER, CauldronBehavior::cleanBanner);
        map2.put(Items.CYAN_BANNER, CauldronBehavior::cleanBanner);
        map2.put(Items.GREEN_BANNER, CauldronBehavior::cleanBanner);
        map2.put(Items.LIGHT_BLUE_BANNER, CauldronBehavior::cleanBanner);
        map2.put(Items.LIGHT_GRAY_BANNER, CauldronBehavior::cleanBanner);
        map2.put(Items.LIME_BANNER, CauldronBehavior::cleanBanner);
        map2.put(Items.MAGENTA_BANNER, CauldronBehavior::cleanBanner);
        map2.put(Items.ORANGE_BANNER, CauldronBehavior::cleanBanner);
        map2.put(Items.PINK_BANNER, CauldronBehavior::cleanBanner);
        map2.put(Items.PURPLE_BANNER, CauldronBehavior::cleanBanner);
        map2.put(Items.RED_BANNER, CauldronBehavior::cleanBanner);
        map2.put(Items.YELLOW_BANNER, CauldronBehavior::cleanBanner);
        map2.put(Items.WHITE_SHULKER_BOX, CauldronBehavior::cleanShulkerBox);
        map2.put(Items.GRAY_SHULKER_BOX, CauldronBehavior::cleanShulkerBox);
        map2.put(Items.BLACK_SHULKER_BOX, CauldronBehavior::cleanShulkerBox);
        map2.put(Items.BLUE_SHULKER_BOX, CauldronBehavior::cleanShulkerBox);
        map2.put(Items.BROWN_SHULKER_BOX, CauldronBehavior::cleanShulkerBox);
        map2.put(Items.CYAN_SHULKER_BOX, CauldronBehavior::cleanShulkerBox);
        map2.put(Items.GREEN_SHULKER_BOX, CauldronBehavior::cleanShulkerBox);
        map2.put(Items.LIGHT_BLUE_SHULKER_BOX, CauldronBehavior::cleanShulkerBox);
        map2.put(Items.LIGHT_GRAY_SHULKER_BOX, CauldronBehavior::cleanShulkerBox);
        map2.put(Items.LIME_SHULKER_BOX, CauldronBehavior::cleanShulkerBox);
        map2.put(Items.MAGENTA_SHULKER_BOX, CauldronBehavior::cleanShulkerBox);
        map2.put(Items.ORANGE_SHULKER_BOX, CauldronBehavior::cleanShulkerBox);
        map2.put(Items.PINK_SHULKER_BOX, CauldronBehavior::cleanShulkerBox);
        map2.put(Items.PURPLE_SHULKER_BOX, CauldronBehavior::cleanShulkerBox);
        map2.put(Items.RED_SHULKER_BOX, CauldronBehavior::cleanShulkerBox);
        map2.put(Items.YELLOW_SHULKER_BOX, CauldronBehavior::cleanShulkerBox);
        Map<Item, CauldronBehavior> map3 = LAVA_CAULDRON_BEHAVIOR.map();
        map3.put(Items.BUCKET, (state, world, pos, player, hand, stack) -> CauldronBehavior.emptyCauldron(state, world, pos, player, hand, stack, new ItemStack(Items.LAVA_BUCKET), statex -> true, SoundEvents.ITEM_BUCKET_FILL_LAVA));
        CauldronBehavior.registerBucketBehavior(map3);
        Map<Item, CauldronBehavior> map4 = POWDER_SNOW_CAULDRON_BEHAVIOR.map();
        map4.put(Items.BUCKET, (state, world, pos, player, hand, stack) -> CauldronBehavior.emptyCauldron(state, world, pos, player, hand, stack, new ItemStack(Items.POWDER_SNOW_BUCKET), statex -> statex.get(LeveledCauldronBlock.LEVEL) == 3, SoundEvents.ITEM_BUCKET_FILL_POWDER_SNOW));
        CauldronBehavior.registerBucketBehavior(map4);
    }

    public static void registerBucketBehavior(Map<Item, CauldronBehavior> behavior) {
        behavior.put(Items.LAVA_BUCKET, CauldronBehavior::tryFillWithLava);
        behavior.put(Items.WATER_BUCKET, CauldronBehavior::tryFillWithWater);
        behavior.put(Items.POWDER_SNOW_BUCKET, CauldronBehavior::tryFillWithPowderSnow);
    }

    public static ActionResult emptyCauldron(BlockState state, World world, BlockPos pos, PlayerEntity player, Hand hand, ItemStack stack, ItemStack output, Predicate<BlockState> fullPredicate, SoundEvent soundEvent) {
        if (!fullPredicate.test(state)) {
            return ActionResult.PASS_TO_DEFAULT_BLOCK_ACTION;
        }
        if (!world.isClient()) {
            Item item = stack.getItem();
            player.setStackInHand(hand, ItemUsage.exchangeStack(stack, player, output));
            player.incrementStat(Stats.USE_CAULDRON);
            player.incrementStat(Stats.USED.getOrCreateStat(item));
            world.setBlockState(pos, Blocks.CAULDRON.getDefaultState());
            world.playSound(null, pos, soundEvent, SoundCategory.BLOCKS, 1.0f, 1.0f);
            world.emitGameEvent(null, GameEvent.FLUID_PICKUP, pos);
        }
        return ActionResult.SUCCESS;
    }

    public static ActionResult fillCauldron(World world, BlockPos pos, PlayerEntity player, Hand hand, ItemStack stack, BlockState state, SoundEvent soundEvent) {
        if (!world.isClient()) {
            Item item = stack.getItem();
            player.setStackInHand(hand, ItemUsage.exchangeStack(stack, player, new ItemStack(Items.BUCKET)));
            player.incrementStat(Stats.FILL_CAULDRON);
            player.incrementStat(Stats.USED.getOrCreateStat(item));
            world.setBlockState(pos, state);
            world.playSound(null, pos, soundEvent, SoundCategory.BLOCKS, 1.0f, 1.0f);
            world.emitGameEvent(null, GameEvent.FLUID_PLACE, pos);
        }
        return ActionResult.SUCCESS;
    }

    private static ActionResult tryFillWithWater(BlockState state, World world, BlockPos pos, PlayerEntity player, Hand hand, ItemStack stack) {
        return CauldronBehavior.fillCauldron(world, pos, player, hand, stack, (BlockState)Blocks.WATER_CAULDRON.getDefaultState().with(LeveledCauldronBlock.LEVEL, 3), SoundEvents.ITEM_BUCKET_EMPTY);
    }

    private static ActionResult tryFillWithLava(BlockState state, World world, BlockPos pos, PlayerEntity player, Hand hand, ItemStack stack) {
        return CauldronBehavior.isUnderwater(world, pos) ? ActionResult.CONSUME : CauldronBehavior.fillCauldron(world, pos, player, hand, stack, Blocks.LAVA_CAULDRON.getDefaultState(), SoundEvents.ITEM_BUCKET_EMPTY_LAVA);
    }

    private static ActionResult tryFillWithPowderSnow(BlockState state, World world, BlockPos pos, PlayerEntity player, Hand hand, ItemStack stack) {
        return CauldronBehavior.isUnderwater(world, pos) ? ActionResult.CONSUME : CauldronBehavior.fillCauldron(world, pos, player, hand, stack, (BlockState)Blocks.POWDER_SNOW_CAULDRON.getDefaultState().with(LeveledCauldronBlock.LEVEL, 3), SoundEvents.ITEM_BUCKET_EMPTY_POWDER_SNOW);
    }

    private static ActionResult cleanShulkerBox(BlockState state, World world, BlockPos pos, PlayerEntity player, Hand hand, ItemStack stack) {
        Block block = Block.getBlockFromItem(stack.getItem());
        if (!(block instanceof ShulkerBoxBlock)) {
            return ActionResult.PASS_TO_DEFAULT_BLOCK_ACTION;
        }
        if (!world.isClient()) {
            ItemStack itemStack = stack.copyComponentsToNewStack(Blocks.SHULKER_BOX, 1);
            player.setStackInHand(hand, ItemUsage.exchangeStack(stack, player, itemStack, false));
            player.incrementStat(Stats.CLEAN_SHULKER_BOX);
            LeveledCauldronBlock.decrementFluidLevel(state, world, pos);
        }
        return ActionResult.SUCCESS;
    }

    private static ActionResult cleanBanner(BlockState state, World world, BlockPos pos, PlayerEntity player, Hand hand, ItemStack stack) {
        BannerPatternsComponent bannerPatternsComponent = stack.getOrDefault(DataComponentTypes.BANNER_PATTERNS, BannerPatternsComponent.DEFAULT);
        if (bannerPatternsComponent.layers().isEmpty()) {
            return ActionResult.PASS_TO_DEFAULT_BLOCK_ACTION;
        }
        if (!world.isClient()) {
            ItemStack itemStack = stack.copyWithCount(1);
            itemStack.set(DataComponentTypes.BANNER_PATTERNS, bannerPatternsComponent.withoutTopLayer());
            player.setStackInHand(hand, ItemUsage.exchangeStack(stack, player, itemStack, false));
            player.incrementStat(Stats.CLEAN_BANNER);
            LeveledCauldronBlock.decrementFluidLevel(state, world, pos);
        }
        return ActionResult.SUCCESS;
    }

    private static ActionResult cleanArmor(BlockState state, World world, BlockPos pos, PlayerEntity player, Hand hand, ItemStack stack) {
        if (!stack.isIn(ItemTags.DYEABLE)) {
            return ActionResult.PASS_TO_DEFAULT_BLOCK_ACTION;
        }
        if (!stack.contains(DataComponentTypes.DYED_COLOR)) {
            return ActionResult.PASS_TO_DEFAULT_BLOCK_ACTION;
        }
        if (!world.isClient()) {
            stack.remove(DataComponentTypes.DYED_COLOR);
            player.incrementStat(Stats.CLEAN_ARMOR);
            LeveledCauldronBlock.decrementFluidLevel(state, world, pos);
        }
        return ActionResult.SUCCESS;
    }

    private static boolean isUnderwater(World world, BlockPos pos) {
        FluidState fluidState = world.getFluidState(pos.up());
        return fluidState.isIn(FluidTags.WATER);
    }

    public record CauldronBehaviorMap(String name, Map<Item, CauldronBehavior> map) {
    }
}


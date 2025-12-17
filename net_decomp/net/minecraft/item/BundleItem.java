/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  org.apache.commons.lang3.math.Fraction
 */
package net.minecraft.item;

import java.util.List;
import java.util.Optional;
import java.util.stream.Stream;
import net.minecraft.component.DataComponentTypes;
import net.minecraft.component.type.BundleContentsComponent;
import net.minecraft.component.type.TooltipDisplayComponent;
import net.minecraft.entity.Entity;
import net.minecraft.entity.ItemEntity;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.inventory.StackReference;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.item.ItemUsage;
import net.minecraft.item.Items;
import net.minecraft.item.consume.UseAction;
import net.minecraft.item.tooltip.BundleTooltipData;
import net.minecraft.item.tooltip.TooltipData;
import net.minecraft.screen.ScreenHandler;
import net.minecraft.screen.slot.Slot;
import net.minecraft.sound.SoundCategory;
import net.minecraft.sound.SoundEvents;
import net.minecraft.stat.Stats;
import net.minecraft.util.ActionResult;
import net.minecraft.util.ClickType;
import net.minecraft.util.DyeColor;
import net.minecraft.util.Hand;
import net.minecraft.util.math.ColorHelper;
import net.minecraft.util.math.MathHelper;
import net.minecraft.world.World;
import org.apache.commons.lang3.math.Fraction;

public class BundleItem
extends Item {
    public static final int TOOLTIP_STACKS_COLUMNS = 4;
    public static final int TOOLTIP_STACKS_ROWS = 3;
    public static final int MAX_TOOLTIP_STACKS_SHOWN = 12;
    public static final int MAX_TOOLTIP_STACKS_SHOWN_WHEN_TOO_MANY_TYPES = 11;
    private static final int FULL_ITEM_BAR_COLOR = ColorHelper.fromFloats(1.0f, 1.0f, 0.33f, 0.33f);
    private static final int ITEM_BAR_COLOR = ColorHelper.fromFloats(1.0f, 0.44f, 0.53f, 1.0f);
    private static final int field_54109 = 10;
    private static final int field_54110 = 2;
    private static final int MAX_USE_TIME = 200;

    public BundleItem(Item.Settings settings) {
        super(settings);
    }

    public static float getAmountFilled(ItemStack stack) {
        BundleContentsComponent bundleContentsComponent = stack.getOrDefault(DataComponentTypes.BUNDLE_CONTENTS, BundleContentsComponent.DEFAULT);
        return bundleContentsComponent.getOccupancy().floatValue();
    }

    @Override
    public boolean onStackClicked(ItemStack stack, Slot slot, ClickType clickType, PlayerEntity player) {
        BundleContentsComponent bundleContentsComponent = stack.get(DataComponentTypes.BUNDLE_CONTENTS);
        if (bundleContentsComponent == null) {
            return false;
        }
        ItemStack itemStack = slot.getStack();
        BundleContentsComponent.Builder builder = new BundleContentsComponent.Builder(bundleContentsComponent);
        if (clickType == ClickType.LEFT && !itemStack.isEmpty()) {
            if (builder.add(slot, player) > 0) {
                BundleItem.playInsertSound(player);
            } else {
                BundleItem.playInsertFailSound(player);
            }
            stack.set(DataComponentTypes.BUNDLE_CONTENTS, builder.build());
            this.onContentChanged(player);
            return true;
        }
        if (clickType == ClickType.RIGHT && itemStack.isEmpty()) {
            ItemStack itemStack2 = builder.removeSelected();
            if (itemStack2 != null) {
                ItemStack itemStack3 = slot.insertStack(itemStack2);
                if (itemStack3.getCount() > 0) {
                    builder.add(itemStack3);
                } else {
                    BundleItem.playRemoveOneSound(player);
                }
            }
            stack.set(DataComponentTypes.BUNDLE_CONTENTS, builder.build());
            this.onContentChanged(player);
            return true;
        }
        return false;
    }

    @Override
    public boolean onClicked(ItemStack stack, ItemStack otherStack, Slot slot, ClickType clickType, PlayerEntity player, StackReference cursorStackReference) {
        if (clickType == ClickType.LEFT && otherStack.isEmpty()) {
            BundleItem.setSelectedStackIndex(stack, -1);
            return false;
        }
        BundleContentsComponent bundleContentsComponent = stack.get(DataComponentTypes.BUNDLE_CONTENTS);
        if (bundleContentsComponent == null) {
            return false;
        }
        BundleContentsComponent.Builder builder = new BundleContentsComponent.Builder(bundleContentsComponent);
        if (clickType == ClickType.LEFT && !otherStack.isEmpty()) {
            if (slot.canTakePartial(player) && builder.add(otherStack) > 0) {
                BundleItem.playInsertSound(player);
            } else {
                BundleItem.playInsertFailSound(player);
            }
            stack.set(DataComponentTypes.BUNDLE_CONTENTS, builder.build());
            this.onContentChanged(player);
            return true;
        }
        if (clickType == ClickType.RIGHT && otherStack.isEmpty()) {
            ItemStack itemStack;
            if (slot.canTakePartial(player) && (itemStack = builder.removeSelected()) != null) {
                BundleItem.playRemoveOneSound(player);
                cursorStackReference.set(itemStack);
            }
            stack.set(DataComponentTypes.BUNDLE_CONTENTS, builder.build());
            this.onContentChanged(player);
            return true;
        }
        BundleItem.setSelectedStackIndex(stack, -1);
        return false;
    }

    @Override
    public ActionResult use(World world, PlayerEntity user, Hand hand) {
        user.setCurrentHand(hand);
        return ActionResult.SUCCESS;
    }

    private void dropContentsOnUse(World world, PlayerEntity player, ItemStack stack) {
        if (this.dropFirstBundledStack(stack, player)) {
            BundleItem.playDropContentsSound(world, player);
            player.incrementStat(Stats.USED.getOrCreateStat(this));
        }
    }

    @Override
    public boolean isItemBarVisible(ItemStack stack) {
        BundleContentsComponent bundleContentsComponent = stack.getOrDefault(DataComponentTypes.BUNDLE_CONTENTS, BundleContentsComponent.DEFAULT);
        return bundleContentsComponent.getOccupancy().compareTo(Fraction.ZERO) > 0;
    }

    @Override
    public int getItemBarStep(ItemStack stack) {
        BundleContentsComponent bundleContentsComponent = stack.getOrDefault(DataComponentTypes.BUNDLE_CONTENTS, BundleContentsComponent.DEFAULT);
        return Math.min(1 + MathHelper.multiplyFraction(bundleContentsComponent.getOccupancy(), 12), 13);
    }

    @Override
    public int getItemBarColor(ItemStack stack) {
        BundleContentsComponent bundleContentsComponent = stack.getOrDefault(DataComponentTypes.BUNDLE_CONTENTS, BundleContentsComponent.DEFAULT);
        return bundleContentsComponent.getOccupancy().compareTo(Fraction.ONE) >= 0 ? FULL_ITEM_BAR_COLOR : ITEM_BAR_COLOR;
    }

    public static void setSelectedStackIndex(ItemStack stack, int selectedStackIndex) {
        BundleContentsComponent bundleContentsComponent = stack.get(DataComponentTypes.BUNDLE_CONTENTS);
        if (bundleContentsComponent == null) {
            return;
        }
        BundleContentsComponent.Builder builder = new BundleContentsComponent.Builder(bundleContentsComponent);
        builder.setSelectedStackIndex(selectedStackIndex);
        stack.set(DataComponentTypes.BUNDLE_CONTENTS, builder.build());
    }

    public static boolean hasSelectedStack(ItemStack stack) {
        BundleContentsComponent bundleContentsComponent = stack.get(DataComponentTypes.BUNDLE_CONTENTS);
        return bundleContentsComponent != null && bundleContentsComponent.getSelectedStackIndex() != -1;
    }

    public static int getSelectedStackIndex(ItemStack stack) {
        BundleContentsComponent bundleContentsComponent = stack.getOrDefault(DataComponentTypes.BUNDLE_CONTENTS, BundleContentsComponent.DEFAULT);
        return bundleContentsComponent.getSelectedStackIndex();
    }

    public static ItemStack getSelectedStack(ItemStack stack) {
        BundleContentsComponent bundleContentsComponent = stack.get(DataComponentTypes.BUNDLE_CONTENTS);
        if (bundleContentsComponent != null && bundleContentsComponent.getSelectedStackIndex() != -1) {
            return bundleContentsComponent.get(bundleContentsComponent.getSelectedStackIndex());
        }
        return ItemStack.EMPTY;
    }

    public static int getNumberOfStacksShown(ItemStack stack) {
        BundleContentsComponent bundleContentsComponent = stack.getOrDefault(DataComponentTypes.BUNDLE_CONTENTS, BundleContentsComponent.DEFAULT);
        return bundleContentsComponent.getNumberOfStacksShown();
    }

    private boolean dropFirstBundledStack(ItemStack stack, PlayerEntity player) {
        BundleContentsComponent bundleContentsComponent = stack.get(DataComponentTypes.BUNDLE_CONTENTS);
        if (bundleContentsComponent == null || bundleContentsComponent.isEmpty()) {
            return false;
        }
        Optional<ItemStack> optional = BundleItem.popFirstBundledStack(stack, player, bundleContentsComponent);
        if (optional.isPresent()) {
            player.dropItem(optional.get(), true);
            return true;
        }
        return false;
    }

    private static Optional<ItemStack> popFirstBundledStack(ItemStack stack, PlayerEntity player, BundleContentsComponent contents) {
        BundleContentsComponent.Builder builder = new BundleContentsComponent.Builder(contents);
        ItemStack itemStack = builder.removeSelected();
        if (itemStack != null) {
            BundleItem.playRemoveOneSound(player);
            stack.set(DataComponentTypes.BUNDLE_CONTENTS, builder.build());
            return Optional.of(itemStack);
        }
        return Optional.empty();
    }

    @Override
    public void usageTick(World world, LivingEntity user, ItemStack stack, int remainingUseTicks) {
        if (user instanceof PlayerEntity) {
            boolean bl;
            PlayerEntity playerEntity = (PlayerEntity)user;
            int i = this.getMaxUseTime(stack, user);
            boolean bl2 = bl = remainingUseTicks == i;
            if (bl || remainingUseTicks < i - 10 && remainingUseTicks % 2 == 0) {
                this.dropContentsOnUse(world, playerEntity, stack);
            }
        }
    }

    @Override
    public int getMaxUseTime(ItemStack stack, LivingEntity user) {
        return 200;
    }

    @Override
    public UseAction getUseAction(ItemStack stack) {
        return UseAction.BUNDLE;
    }

    @Override
    public Optional<TooltipData> getTooltipData(ItemStack stack) {
        TooltipDisplayComponent tooltipDisplayComponent = stack.getOrDefault(DataComponentTypes.TOOLTIP_DISPLAY, TooltipDisplayComponent.DEFAULT);
        if (!tooltipDisplayComponent.shouldDisplay(DataComponentTypes.BUNDLE_CONTENTS)) {
            return Optional.empty();
        }
        return Optional.ofNullable(stack.get(DataComponentTypes.BUNDLE_CONTENTS)).map(BundleTooltipData::new);
    }

    @Override
    public void onItemEntityDestroyed(ItemEntity entity) {
        BundleContentsComponent bundleContentsComponent = entity.getStack().get(DataComponentTypes.BUNDLE_CONTENTS);
        if (bundleContentsComponent == null) {
            return;
        }
        entity.getStack().set(DataComponentTypes.BUNDLE_CONTENTS, BundleContentsComponent.DEFAULT);
        ItemUsage.spawnItemContents(entity, bundleContentsComponent.iterateCopy());
    }

    public static List<BundleItem> getBundles() {
        return Stream.of(Items.BUNDLE, Items.WHITE_BUNDLE, Items.ORANGE_BUNDLE, Items.MAGENTA_BUNDLE, Items.LIGHT_BLUE_BUNDLE, Items.YELLOW_BUNDLE, Items.LIME_BUNDLE, Items.PINK_BUNDLE, Items.GRAY_BUNDLE, Items.LIGHT_GRAY_BUNDLE, Items.CYAN_BUNDLE, Items.BLACK_BUNDLE, Items.BROWN_BUNDLE, Items.GREEN_BUNDLE, Items.RED_BUNDLE, Items.BLUE_BUNDLE, Items.PURPLE_BUNDLE).map(item -> (BundleItem)item).toList();
    }

    public static Item getBundle(DyeColor color) {
        return switch (color) {
            default -> throw new MatchException(null, null);
            case DyeColor.WHITE -> Items.WHITE_BUNDLE;
            case DyeColor.ORANGE -> Items.ORANGE_BUNDLE;
            case DyeColor.MAGENTA -> Items.MAGENTA_BUNDLE;
            case DyeColor.LIGHT_BLUE -> Items.LIGHT_BLUE_BUNDLE;
            case DyeColor.YELLOW -> Items.YELLOW_BUNDLE;
            case DyeColor.LIME -> Items.LIME_BUNDLE;
            case DyeColor.PINK -> Items.PINK_BUNDLE;
            case DyeColor.GRAY -> Items.GRAY_BUNDLE;
            case DyeColor.LIGHT_GRAY -> Items.LIGHT_GRAY_BUNDLE;
            case DyeColor.CYAN -> Items.CYAN_BUNDLE;
            case DyeColor.BLUE -> Items.BLUE_BUNDLE;
            case DyeColor.BROWN -> Items.BROWN_BUNDLE;
            case DyeColor.GREEN -> Items.GREEN_BUNDLE;
            case DyeColor.RED -> Items.RED_BUNDLE;
            case DyeColor.BLACK -> Items.BLACK_BUNDLE;
            case DyeColor.PURPLE -> Items.PURPLE_BUNDLE;
        };
    }

    private static void playRemoveOneSound(Entity entity) {
        entity.playSound(SoundEvents.ITEM_BUNDLE_REMOVE_ONE, 0.8f, 0.8f + entity.getEntityWorld().getRandom().nextFloat() * 0.4f);
    }

    private static void playInsertSound(Entity entity) {
        entity.playSound(SoundEvents.ITEM_BUNDLE_INSERT, 0.8f, 0.8f + entity.getEntityWorld().getRandom().nextFloat() * 0.4f);
    }

    private static void playInsertFailSound(Entity entity) {
        entity.playSound(SoundEvents.ITEM_BUNDLE_INSERT_FAIL, 1.0f, 1.0f);
    }

    private static void playDropContentsSound(World world, Entity entity) {
        world.playSound(null, entity.getBlockPos(), SoundEvents.ITEM_BUNDLE_DROP_CONTENTS, SoundCategory.PLAYERS, 0.8f, 0.8f + entity.getEntityWorld().getRandom().nextFloat() * 0.4f);
    }

    private void onContentChanged(PlayerEntity user) {
        ScreenHandler screenHandler = user.currentScreenHandler;
        if (screenHandler != null) {
            screenHandler.onContentChanged(user.getInventory());
        }
    }
}


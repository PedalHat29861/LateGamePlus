/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  org.jspecify.annotations.Nullable
 */
package net.minecraft.item;

import java.util.Optional;
import net.minecraft.block.Blocks;
import net.minecraft.component.DataComponentTypes;
import net.minecraft.component.type.LodestoneTrackerComponent;
import net.minecraft.entity.Entity;
import net.minecraft.entity.EquipmentSlot;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.item.ItemUsageContext;
import net.minecraft.item.Items;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.sound.SoundCategory;
import net.minecraft.sound.SoundEvents;
import net.minecraft.text.Text;
import net.minecraft.util.ActionResult;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.GlobalPos;
import net.minecraft.world.World;
import org.jspecify.annotations.Nullable;

public class CompassItem
extends Item {
    private static final Text LODESTONE_COMPASS_NAME = Text.translatable("item.minecraft.lodestone_compass");

    public CompassItem(Item.Settings settings) {
        super(settings);
    }

    @Override
    public boolean hasGlint(ItemStack stack) {
        return stack.contains(DataComponentTypes.LODESTONE_TRACKER) || super.hasGlint(stack);
    }

    @Override
    public void inventoryTick(ItemStack stack, ServerWorld world, Entity entity, @Nullable EquipmentSlot slot) {
        LodestoneTrackerComponent lodestoneTrackerComponent2;
        LodestoneTrackerComponent lodestoneTrackerComponent = stack.get(DataComponentTypes.LODESTONE_TRACKER);
        if (lodestoneTrackerComponent != null && (lodestoneTrackerComponent2 = lodestoneTrackerComponent.forWorld(world)) != lodestoneTrackerComponent) {
            stack.set(DataComponentTypes.LODESTONE_TRACKER, lodestoneTrackerComponent2);
        }
    }

    @Override
    public ActionResult useOnBlock(ItemUsageContext context) {
        BlockPos blockPos = context.getBlockPos();
        World world = context.getWorld();
        if (world.getBlockState(blockPos).isOf(Blocks.LODESTONE)) {
            world.playSound(null, blockPos, SoundEvents.ITEM_LODESTONE_COMPASS_LOCK, SoundCategory.PLAYERS, 1.0f, 1.0f);
            PlayerEntity playerEntity = context.getPlayer();
            ItemStack itemStack = context.getStack();
            boolean bl = !playerEntity.isInCreativeMode() && itemStack.getCount() == 1;
            LodestoneTrackerComponent lodestoneTrackerComponent = new LodestoneTrackerComponent(Optional.of(GlobalPos.create(world.getRegistryKey(), blockPos)), true);
            if (bl) {
                itemStack.set(DataComponentTypes.LODESTONE_TRACKER, lodestoneTrackerComponent);
            } else {
                ItemStack itemStack2 = itemStack.copyComponentsToNewStack(Items.COMPASS, 1);
                itemStack.decrementUnlessCreative(1, playerEntity);
                itemStack2.set(DataComponentTypes.LODESTONE_TRACKER, lodestoneTrackerComponent);
                if (!playerEntity.getInventory().insertStack(itemStack2)) {
                    playerEntity.dropItem(itemStack2, false);
                }
            }
            return ActionResult.SUCCESS;
        }
        return super.useOnBlock(context);
    }

    @Override
    public Text getName(ItemStack stack) {
        return stack.contains(DataComponentTypes.LODESTONE_TRACKER) ? LODESTONE_COMPASS_NAME : super.getName(stack);
    }
}


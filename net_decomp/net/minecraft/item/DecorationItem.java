/*
 * Decompiled with CFR 0.152.
 */
package net.minecraft.item;

import java.util.Optional;
import java.util.function.Consumer;
import net.minecraft.component.DataComponentTypes;
import net.minecraft.component.type.TooltipDisplayComponent;
import net.minecraft.entity.Entity;
import net.minecraft.entity.EntityType;
import net.minecraft.entity.decoration.AbstractDecorationEntity;
import net.minecraft.entity.decoration.GlowItemFrameEntity;
import net.minecraft.entity.decoration.ItemFrameEntity;
import net.minecraft.entity.decoration.painting.PaintingEntity;
import net.minecraft.entity.decoration.painting.PaintingVariant;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.item.ItemUsageContext;
import net.minecraft.item.tooltip.TooltipType;
import net.minecraft.registry.entry.RegistryEntry;
import net.minecraft.text.Text;
import net.minecraft.util.ActionResult;
import net.minecraft.util.Formatting;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Direction;
import net.minecraft.world.World;
import net.minecraft.world.event.GameEvent;

public class DecorationItem
extends Item {
    private static final Text RANDOM_TEXT = Text.translatable("painting.random").formatted(Formatting.GRAY);
    private final EntityType<? extends AbstractDecorationEntity> entityType;

    public DecorationItem(EntityType<? extends AbstractDecorationEntity> type, Item.Settings settings) {
        super(settings);
        this.entityType = type;
    }

    @Override
    public ActionResult useOnBlock(ItemUsageContext context) {
        AbstractDecorationEntity abstractDecorationEntity;
        BlockPos blockPos = context.getBlockPos();
        Direction direction = context.getSide();
        BlockPos blockPos2 = blockPos.offset(direction);
        PlayerEntity playerEntity = context.getPlayer();
        ItemStack itemStack = context.getStack();
        if (playerEntity != null && !this.canPlaceOn(playerEntity, direction, itemStack, blockPos2)) {
            return ActionResult.FAIL;
        }
        World world = context.getWorld();
        if (this.entityType == EntityType.PAINTING) {
            Optional<PaintingEntity> optional = PaintingEntity.placePainting(world, blockPos2, direction);
            if (optional.isEmpty()) {
                return ActionResult.CONSUME;
            }
            abstractDecorationEntity = optional.get();
        } else if (this.entityType == EntityType.ITEM_FRAME) {
            abstractDecorationEntity = new ItemFrameEntity(world, blockPos2, direction);
        } else if (this.entityType == EntityType.GLOW_ITEM_FRAME) {
            abstractDecorationEntity = new GlowItemFrameEntity(world, blockPos2, direction);
        } else {
            return ActionResult.SUCCESS;
        }
        EntityType.copier(world, itemStack, playerEntity).accept(abstractDecorationEntity);
        if (abstractDecorationEntity.canStayAttached()) {
            if (!world.isClient()) {
                abstractDecorationEntity.onPlace();
                world.emitGameEvent((Entity)playerEntity, GameEvent.ENTITY_PLACE, abstractDecorationEntity.getEntityPos());
                world.spawnEntity(abstractDecorationEntity);
            }
            itemStack.decrement(1);
            return ActionResult.SUCCESS;
        }
        return ActionResult.CONSUME;
    }

    protected boolean canPlaceOn(PlayerEntity player, Direction side, ItemStack stack, BlockPos pos) {
        return !side.getAxis().isVertical() && player.canPlaceOn(pos, side, stack);
    }

    @Override
    public void appendTooltip(ItemStack stack, Item.TooltipContext context, TooltipDisplayComponent displayComponent, Consumer<Text> textConsumer, TooltipType type) {
        if (this.entityType == EntityType.PAINTING && displayComponent.shouldDisplay(DataComponentTypes.PAINTING_VARIANT)) {
            RegistryEntry<PaintingVariant> registryEntry = stack.get(DataComponentTypes.PAINTING_VARIANT);
            if (registryEntry != null) {
                registryEntry.value().title().ifPresent(textConsumer);
                registryEntry.value().author().ifPresent(textConsumer);
                textConsumer.accept(Text.translatable("painting.dimensions", registryEntry.value().width(), registryEntry.value().height()));
            } else if (type.isCreative()) {
                textConsumer.accept(RANDOM_TEXT);
            }
        }
    }
}


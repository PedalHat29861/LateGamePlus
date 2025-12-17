/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  org.jspecify.annotations.Nullable
 */
package net.minecraft.item;

import java.util.Map;
import net.minecraft.advancement.criterion.Criteria;
import net.minecraft.block.Block;
import net.minecraft.block.BlockState;
import net.minecraft.block.ShapeContext;
import net.minecraft.block.ShulkerBoxBlock;
import net.minecraft.block.entity.BlockEntity;
import net.minecraft.block.entity.BlockEntityType;
import net.minecraft.command.DefaultPermissions;
import net.minecraft.component.DataComponentTypes;
import net.minecraft.component.type.BlockStateComponent;
import net.minecraft.component.type.ContainerComponent;
import net.minecraft.entity.Entity;
import net.minecraft.entity.ItemEntity;
import net.minecraft.entity.TypedEntityData;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.Item;
import net.minecraft.item.ItemPlacementContext;
import net.minecraft.item.ItemStack;
import net.minecraft.item.ItemUsage;
import net.minecraft.item.ItemUsageContext;
import net.minecraft.resource.featuretoggle.FeatureSet;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.sound.BlockSoundGroup;
import net.minecraft.sound.SoundCategory;
import net.minecraft.sound.SoundEvent;
import net.minecraft.storage.NbtWriteView;
import net.minecraft.util.ActionResult;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;
import net.minecraft.world.event.GameEvent;
import org.jspecify.annotations.Nullable;

public class BlockItem
extends Item {
    @Deprecated
    private final Block block;

    public BlockItem(Block block, Item.Settings settings) {
        super(settings);
        this.block = block;
    }

    @Override
    public ActionResult useOnBlock(ItemUsageContext context) {
        ActionResult actionResult = this.place(new ItemPlacementContext(context));
        if (!actionResult.isAccepted() && context.getStack().contains(DataComponentTypes.CONSUMABLE)) {
            return super.use(context.getWorld(), context.getPlayer(), context.getHand());
        }
        return actionResult;
    }

    public ActionResult place(ItemPlacementContext context) {
        if (!this.getBlock().isEnabled(context.getWorld().getEnabledFeatures())) {
            return ActionResult.FAIL;
        }
        if (!context.canPlace()) {
            return ActionResult.FAIL;
        }
        ItemPlacementContext itemPlacementContext = this.getPlacementContext(context);
        if (itemPlacementContext == null) {
            return ActionResult.FAIL;
        }
        BlockState blockState = this.getPlacementState(itemPlacementContext);
        if (blockState == null) {
            return ActionResult.FAIL;
        }
        if (!this.place(itemPlacementContext, blockState)) {
            return ActionResult.FAIL;
        }
        BlockPos blockPos = itemPlacementContext.getBlockPos();
        World world = itemPlacementContext.getWorld();
        PlayerEntity playerEntity = itemPlacementContext.getPlayer();
        ItemStack itemStack = itemPlacementContext.getStack();
        BlockState blockState2 = world.getBlockState(blockPos);
        if (blockState2.isOf(blockState.getBlock())) {
            blockState2 = this.placeFromNbt(blockPos, world, itemStack, blockState2);
            this.postPlacement(blockPos, world, playerEntity, itemStack, blockState2);
            BlockItem.copyComponentsToBlockEntity(world, blockPos, itemStack);
            blockState2.getBlock().onPlaced(world, blockPos, blockState2, playerEntity, itemStack);
            if (playerEntity instanceof ServerPlayerEntity) {
                Criteria.PLACED_BLOCK.trigger((ServerPlayerEntity)playerEntity, blockPos, itemStack);
            }
        }
        BlockSoundGroup blockSoundGroup = blockState2.getSoundGroup();
        world.playSound((Entity)playerEntity, blockPos, this.getPlaceSound(blockState2), SoundCategory.BLOCKS, (blockSoundGroup.getVolume() + 1.0f) / 2.0f, blockSoundGroup.getPitch() * 0.8f);
        world.emitGameEvent(GameEvent.BLOCK_PLACE, blockPos, GameEvent.Emitter.of(playerEntity, blockState2));
        itemStack.decrementUnlessCreative(1, playerEntity);
        return ActionResult.SUCCESS;
    }

    protected SoundEvent getPlaceSound(BlockState state) {
        return state.getSoundGroup().getPlaceSound();
    }

    public @Nullable ItemPlacementContext getPlacementContext(ItemPlacementContext context) {
        return context;
    }

    private static void copyComponentsToBlockEntity(World world, BlockPos pos, ItemStack stack) {
        BlockEntity blockEntity = world.getBlockEntity(pos);
        if (blockEntity != null) {
            blockEntity.readComponents(stack);
            blockEntity.markDirty();
        }
    }

    protected boolean postPlacement(BlockPos pos, World world, @Nullable PlayerEntity player, ItemStack stack, BlockState state) {
        return BlockItem.writeNbtToBlockEntity(world, player, pos, stack);
    }

    protected @Nullable BlockState getPlacementState(ItemPlacementContext context) {
        BlockState blockState = this.getBlock().getPlacementState(context);
        return blockState != null && this.canPlace(context, blockState) ? blockState : null;
    }

    private BlockState placeFromNbt(BlockPos pos, World world, ItemStack stack, BlockState state) {
        BlockStateComponent blockStateComponent = stack.getOrDefault(DataComponentTypes.BLOCK_STATE, BlockStateComponent.DEFAULT);
        if (blockStateComponent.isEmpty()) {
            return state;
        }
        BlockState blockState = blockStateComponent.applyToState(state);
        if (blockState != state) {
            world.setBlockState(pos, blockState, 2);
        }
        return blockState;
    }

    protected boolean canPlace(ItemPlacementContext context, BlockState state) {
        PlayerEntity playerEntity = context.getPlayer();
        return (!this.checkStatePlacement() || state.canPlaceAt(context.getWorld(), context.getBlockPos())) && context.getWorld().canPlace(state, context.getBlockPos(), ShapeContext.ofPlacement(playerEntity));
    }

    protected boolean checkStatePlacement() {
        return true;
    }

    protected boolean place(ItemPlacementContext context, BlockState state) {
        return context.getWorld().setBlockState(context.getBlockPos(), state, 11);
    }

    public static boolean writeNbtToBlockEntity(World world, @Nullable PlayerEntity player, BlockPos pos, ItemStack stack) {
        BlockEntity blockEntity;
        if (world.isClient()) {
            return false;
        }
        TypedEntityData<BlockEntityType<?>> typedEntityData = stack.get(DataComponentTypes.BLOCK_ENTITY_DATA);
        if (typedEntityData != null && (blockEntity = world.getBlockEntity(pos)) != null) {
            BlockEntityType<?> blockEntityType = blockEntity.getType();
            if (blockEntityType != typedEntityData.getType()) {
                return false;
            }
            if (blockEntityType.canPotentiallyExecuteCommands() && (player == null || !player.isCreativeLevelTwoOp())) {
                return false;
            }
            return typedEntityData.applyToBlockEntity(blockEntity, world.getRegistryManager());
        }
        return false;
    }

    @Override
    public boolean shouldShowOperatorBlockWarnings(ItemStack stack, @Nullable PlayerEntity player) {
        TypedEntityData<BlockEntityType<?>> typedEntityData;
        if (player != null && player.getPermissions().hasPermission(DefaultPermissions.GAMEMASTERS) && (typedEntityData = stack.get(DataComponentTypes.BLOCK_ENTITY_DATA)) != null) {
            return typedEntityData.getType().canPotentiallyExecuteCommands();
        }
        return false;
    }

    public Block getBlock() {
        return this.block;
    }

    public void appendBlocks(Map<Block, Item> map, Item item) {
        map.put(this.getBlock(), item);
    }

    @Override
    public boolean canBeNested() {
        return !(this.getBlock() instanceof ShulkerBoxBlock);
    }

    @Override
    public void onItemEntityDestroyed(ItemEntity entity) {
        ContainerComponent containerComponent = entity.getStack().set(DataComponentTypes.CONTAINER, ContainerComponent.DEFAULT);
        if (containerComponent != null) {
            ItemUsage.spawnItemContents(entity, containerComponent.iterateNonEmptyCopy());
        }
    }

    public static void setBlockEntityData(ItemStack stack, BlockEntityType<?> type, NbtWriteView view) {
        view.remove("id");
        if (view.isEmpty()) {
            stack.remove(DataComponentTypes.BLOCK_ENTITY_DATA);
        } else {
            BlockEntity.writeId(view, type);
            stack.set(DataComponentTypes.BLOCK_ENTITY_DATA, TypedEntityData.create(type, view.getNbt()));
        }
    }

    @Override
    public FeatureSet getRequiredFeatures() {
        return this.getBlock().getRequiredFeatures();
    }
}


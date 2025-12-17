/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  com.mojang.serialization.MapCodec
 *  org.jspecify.annotations.Nullable
 */
package net.minecraft.block;

import com.mojang.serialization.MapCodec;
import java.util.List;
import java.util.OptionalInt;
import net.minecraft.block.AbstractBlock;
import net.minecraft.block.Block;
import net.minecraft.block.BlockState;
import net.minecraft.block.BlockWithEntity;
import net.minecraft.block.HorizontalFacingBlock;
import net.minecraft.block.InteractibleSlotContainer;
import net.minecraft.block.entity.BlockEntity;
import net.minecraft.block.entity.ChiseledBookshelfBlockEntity;
import net.minecraft.entity.Entity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.ItemPlacementContext;
import net.minecraft.item.ItemStack;
import net.minecraft.item.Items;
import net.minecraft.registry.tag.ItemTags;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.sound.SoundCategory;
import net.minecraft.sound.SoundEvent;
import net.minecraft.sound.SoundEvents;
import net.minecraft.stat.Stats;
import net.minecraft.state.StateManager;
import net.minecraft.state.property.BooleanProperty;
import net.minecraft.state.property.EnumProperty;
import net.minecraft.state.property.Properties;
import net.minecraft.state.property.Property;
import net.minecraft.util.ActionResult;
import net.minecraft.util.BlockMirror;
import net.minecraft.util.BlockRotation;
import net.minecraft.util.Hand;
import net.minecraft.util.ItemScatterer;
import net.minecraft.util.hit.BlockHitResult;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Direction;
import net.minecraft.world.World;
import net.minecraft.world.event.GameEvent;
import org.jspecify.annotations.Nullable;

public class ChiseledBookshelfBlock
extends BlockWithEntity
implements InteractibleSlotContainer {
    public static final MapCodec<ChiseledBookshelfBlock> CODEC = ChiseledBookshelfBlock.createCodec(ChiseledBookshelfBlock::new);
    public static final EnumProperty<Direction> FACING = HorizontalFacingBlock.FACING;
    public static final BooleanProperty SLOT_0_OCCUPIED = Properties.SLOT_0_OCCUPIED;
    public static final BooleanProperty SLOT_1_OCCUPIED = Properties.SLOT_1_OCCUPIED;
    public static final BooleanProperty SLOT_2_OCCUPIED = Properties.SLOT_2_OCCUPIED;
    public static final BooleanProperty SLOT_3_OCCUPIED = Properties.SLOT_3_OCCUPIED;
    public static final BooleanProperty SLOT_4_OCCUPIED = Properties.SLOT_4_OCCUPIED;
    public static final BooleanProperty SLOT_5_OCCUPIED = Properties.SLOT_5_OCCUPIED;
    private static final int MAX_BOOK_COUNT = 6;
    private static final int BOOK_HEIGHT = 3;
    public static final List<BooleanProperty> SLOT_OCCUPIED_PROPERTIES = List.of(SLOT_0_OCCUPIED, SLOT_1_OCCUPIED, SLOT_2_OCCUPIED, SLOT_3_OCCUPIED, SLOT_4_OCCUPIED, SLOT_5_OCCUPIED);

    public MapCodec<ChiseledBookshelfBlock> getCodec() {
        return CODEC;
    }

    @Override
    public int getRows() {
        return 2;
    }

    @Override
    public int getColumns() {
        return 3;
    }

    public ChiseledBookshelfBlock(AbstractBlock.Settings settings) {
        super(settings);
        BlockState blockState = (BlockState)((BlockState)this.stateManager.getDefaultState()).with(FACING, Direction.NORTH);
        for (BooleanProperty booleanProperty : SLOT_OCCUPIED_PROPERTIES) {
            blockState = (BlockState)blockState.with(booleanProperty, false);
        }
        this.setDefaultState(blockState);
    }

    @Override
    protected ActionResult onUseWithItem(ItemStack stack, BlockState state, World world, BlockPos pos, PlayerEntity player, Hand hand, BlockHitResult hit) {
        BlockEntity blockEntity = world.getBlockEntity(pos);
        if (!(blockEntity instanceof ChiseledBookshelfBlockEntity)) {
            return ActionResult.PASS;
        }
        ChiseledBookshelfBlockEntity chiseledBookshelfBlockEntity = (ChiseledBookshelfBlockEntity)blockEntity;
        if (!stack.isIn(ItemTags.BOOKSHELF_BOOKS)) {
            return ActionResult.PASS_TO_DEFAULT_BLOCK_ACTION;
        }
        OptionalInt optionalInt = this.getHitSlot(hit, state.get(FACING));
        if (optionalInt.isEmpty()) {
            return ActionResult.PASS;
        }
        if (((Boolean)state.get(SLOT_OCCUPIED_PROPERTIES.get(optionalInt.getAsInt()))).booleanValue()) {
            return ActionResult.PASS_TO_DEFAULT_BLOCK_ACTION;
        }
        ChiseledBookshelfBlock.tryAddBook(world, pos, player, chiseledBookshelfBlockEntity, stack, optionalInt.getAsInt());
        return ActionResult.SUCCESS;
    }

    @Override
    protected ActionResult onUse(BlockState state, World world, BlockPos pos, PlayerEntity player, BlockHitResult hit) {
        BlockEntity blockEntity = world.getBlockEntity(pos);
        if (!(blockEntity instanceof ChiseledBookshelfBlockEntity)) {
            return ActionResult.PASS;
        }
        ChiseledBookshelfBlockEntity chiseledBookshelfBlockEntity = (ChiseledBookshelfBlockEntity)blockEntity;
        OptionalInt optionalInt = this.getHitSlot(hit, state.get(FACING));
        if (optionalInt.isEmpty()) {
            return ActionResult.PASS;
        }
        if (!((Boolean)state.get(SLOT_OCCUPIED_PROPERTIES.get(optionalInt.getAsInt()))).booleanValue()) {
            return ActionResult.CONSUME;
        }
        ChiseledBookshelfBlock.tryRemoveBook(world, pos, player, chiseledBookshelfBlockEntity, optionalInt.getAsInt());
        return ActionResult.SUCCESS;
    }

    private static void tryAddBook(World world, BlockPos pos, PlayerEntity player, ChiseledBookshelfBlockEntity blockEntity, ItemStack stack, int slot) {
        if (world.isClient()) {
            return;
        }
        player.incrementStat(Stats.USED.getOrCreateStat(stack.getItem()));
        SoundEvent soundEvent = stack.isOf(Items.ENCHANTED_BOOK) ? SoundEvents.BLOCK_CHISELED_BOOKSHELF_INSERT_ENCHANTED : SoundEvents.BLOCK_CHISELED_BOOKSHELF_INSERT;
        blockEntity.setStack(slot, stack.splitUnlessCreative(1, player));
        world.playSound(null, pos, soundEvent, SoundCategory.BLOCKS, 1.0f, 1.0f);
    }

    private static void tryRemoveBook(World world, BlockPos pos, PlayerEntity player, ChiseledBookshelfBlockEntity blockEntity, int slot) {
        if (world.isClient()) {
            return;
        }
        ItemStack itemStack = blockEntity.removeStack(slot, 1);
        SoundEvent soundEvent = itemStack.isOf(Items.ENCHANTED_BOOK) ? SoundEvents.BLOCK_CHISELED_BOOKSHELF_PICKUP_ENCHANTED : SoundEvents.BLOCK_CHISELED_BOOKSHELF_PICKUP;
        world.playSound(null, pos, soundEvent, SoundCategory.BLOCKS, 1.0f, 1.0f);
        if (!player.getInventory().insertStack(itemStack)) {
            player.dropItem(itemStack, false);
        }
        world.emitGameEvent((Entity)player, GameEvent.BLOCK_CHANGE, pos);
    }

    @Override
    public @Nullable BlockEntity createBlockEntity(BlockPos pos, BlockState state) {
        return new ChiseledBookshelfBlockEntity(pos, state);
    }

    @Override
    protected void appendProperties(StateManager.Builder<Block, BlockState> builder) {
        builder.add(FACING);
        SLOT_OCCUPIED_PROPERTIES.forEach(property -> builder.add((Property<?>)property));
    }

    @Override
    protected void onStateReplaced(BlockState state, ServerWorld world, BlockPos pos, boolean moved) {
        ItemScatterer.onStateReplaced(state, world, pos);
    }

    @Override
    public BlockState getPlacementState(ItemPlacementContext ctx) {
        return (BlockState)this.getDefaultState().with(FACING, ctx.getHorizontalPlayerFacing().getOpposite());
    }

    @Override
    public BlockState rotate(BlockState state, BlockRotation rotation) {
        return (BlockState)state.with(FACING, rotation.rotate(state.get(FACING)));
    }

    @Override
    public BlockState mirror(BlockState state, BlockMirror mirror) {
        return state.rotate(mirror.getRotation(state.get(FACING)));
    }

    @Override
    protected boolean hasComparatorOutput(BlockState state) {
        return true;
    }

    @Override
    protected int getComparatorOutput(BlockState state, World world, BlockPos pos, Direction direction) {
        if (world.isClient()) {
            return 0;
        }
        BlockEntity blockEntity = world.getBlockEntity(pos);
        if (blockEntity instanceof ChiseledBookshelfBlockEntity) {
            ChiseledBookshelfBlockEntity chiseledBookshelfBlockEntity = (ChiseledBookshelfBlockEntity)blockEntity;
            return chiseledBookshelfBlockEntity.getLastInteractedSlot() + 1;
        }
        return 0;
    }
}


/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  com.mojang.serialization.MapCodec
 *  org.jspecify.annotations.Nullable
 */
package net.minecraft.block;

import com.mojang.serialization.MapCodec;
import net.minecraft.block.AbstractBlock;
import net.minecraft.block.Block;
import net.minecraft.block.BlockState;
import net.minecraft.block.BlockWithEntity;
import net.minecraft.block.entity.BlockEntity;
import net.minecraft.block.entity.BlockEntityTicker;
import net.minecraft.block.entity.BlockEntityType;
import net.minecraft.block.entity.JukeboxBlockEntity;
import net.minecraft.component.DataComponentTypes;
import net.minecraft.component.type.JukeboxPlayableComponent;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.TypedEntityData;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.ItemStack;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.state.StateManager;
import net.minecraft.state.property.BooleanProperty;
import net.minecraft.state.property.Properties;
import net.minecraft.util.ActionResult;
import net.minecraft.util.Hand;
import net.minecraft.util.ItemScatterer;
import net.minecraft.util.hit.BlockHitResult;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Direction;
import net.minecraft.world.BlockView;
import net.minecraft.world.World;
import org.jspecify.annotations.Nullable;

public class JukeboxBlock
extends BlockWithEntity {
    public static final MapCodec<JukeboxBlock> CODEC = JukeboxBlock.createCodec(JukeboxBlock::new);
    public static final BooleanProperty HAS_RECORD = Properties.HAS_RECORD;

    public MapCodec<JukeboxBlock> getCodec() {
        return CODEC;
    }

    protected JukeboxBlock(AbstractBlock.Settings settings) {
        super(settings);
        this.setDefaultState((BlockState)((BlockState)this.stateManager.getDefaultState()).with(HAS_RECORD, false));
    }

    @Override
    public void onPlaced(World world, BlockPos pos, BlockState state, @Nullable LivingEntity placer, ItemStack itemStack) {
        super.onPlaced(world, pos, state, placer, itemStack);
        TypedEntityData<BlockEntityType<?>> typedEntityData = itemStack.get(DataComponentTypes.BLOCK_ENTITY_DATA);
        if (typedEntityData != null && typedEntityData.contains("RecordItem")) {
            world.setBlockState(pos, (BlockState)state.with(HAS_RECORD, true), 2);
        }
    }

    @Override
    protected ActionResult onUse(BlockState state, World world, BlockPos pos, PlayerEntity player, BlockHitResult hit) {
        BlockEntity blockEntity;
        if (state.get(HAS_RECORD).booleanValue() && (blockEntity = world.getBlockEntity(pos)) instanceof JukeboxBlockEntity) {
            JukeboxBlockEntity jukeboxBlockEntity = (JukeboxBlockEntity)blockEntity;
            jukeboxBlockEntity.dropRecord();
            return ActionResult.SUCCESS;
        }
        return ActionResult.PASS;
    }

    @Override
    protected ActionResult onUseWithItem(ItemStack stack, BlockState state, World world, BlockPos pos, PlayerEntity player, Hand hand, BlockHitResult hit) {
        if (state.get(HAS_RECORD).booleanValue()) {
            return ActionResult.PASS_TO_DEFAULT_BLOCK_ACTION;
        }
        ItemStack itemStack = player.getStackInHand(hand);
        ActionResult actionResult = JukeboxPlayableComponent.tryPlayStack(world, pos, itemStack, player);
        if (!actionResult.isAccepted()) {
            return ActionResult.PASS_TO_DEFAULT_BLOCK_ACTION;
        }
        return actionResult;
    }

    @Override
    protected void onStateReplaced(BlockState state, ServerWorld world, BlockPos pos, boolean moved) {
        ItemScatterer.onStateReplaced(state, world, pos);
    }

    @Override
    public BlockEntity createBlockEntity(BlockPos pos, BlockState state) {
        return new JukeboxBlockEntity(pos, state);
    }

    @Override
    public boolean emitsRedstonePower(BlockState state) {
        return true;
    }

    @Override
    public int getWeakRedstonePower(BlockState state, BlockView world, BlockPos pos, Direction direction) {
        JukeboxBlockEntity jukeboxBlockEntity;
        BlockEntity blockEntity = world.getBlockEntity(pos);
        if (blockEntity instanceof JukeboxBlockEntity && (jukeboxBlockEntity = (JukeboxBlockEntity)blockEntity).getManager().isPlaying()) {
            return 15;
        }
        return 0;
    }

    @Override
    protected boolean hasComparatorOutput(BlockState state) {
        return true;
    }

    @Override
    protected int getComparatorOutput(BlockState state, World world, BlockPos pos, Direction direction) {
        BlockEntity blockEntity = world.getBlockEntity(pos);
        if (blockEntity instanceof JukeboxBlockEntity) {
            JukeboxBlockEntity jukeboxBlockEntity = (JukeboxBlockEntity)blockEntity;
            return jukeboxBlockEntity.getComparatorOutput();
        }
        return 0;
    }

    @Override
    protected void appendProperties(StateManager.Builder<Block, BlockState> builder) {
        builder.add(HAS_RECORD);
    }

    @Override
    public <T extends BlockEntity> @Nullable BlockEntityTicker<T> getTicker(World world, BlockState state, BlockEntityType<T> type) {
        if (state.get(HAS_RECORD).booleanValue()) {
            return JukeboxBlock.validateTicker(type, BlockEntityType.JUKEBOX, JukeboxBlockEntity::tick);
        }
        return null;
    }
}


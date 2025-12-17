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
import net.minecraft.block.Blocks;
import net.minecraft.block.SkullBlock;
import net.minecraft.block.entity.BlockEntity;
import net.minecraft.block.entity.BlockEntityTicker;
import net.minecraft.block.entity.BlockEntityType;
import net.minecraft.block.entity.SkullBlockEntity;
import net.minecraft.entity.ai.pathing.NavigationType;
import net.minecraft.item.ItemPlacementContext;
import net.minecraft.state.StateManager;
import net.minecraft.state.property.BooleanProperty;
import net.minecraft.state.property.Properties;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;
import net.minecraft.world.block.WireOrientation;
import org.jspecify.annotations.Nullable;

public abstract class AbstractSkullBlock
extends BlockWithEntity {
    public static final BooleanProperty POWERED = Properties.POWERED;
    private final SkullBlock.SkullType type;

    public AbstractSkullBlock(SkullBlock.SkullType type, AbstractBlock.Settings settings) {
        super(settings);
        this.type = type;
        this.setDefaultState((BlockState)((BlockState)this.stateManager.getDefaultState()).with(POWERED, false));
    }

    protected abstract MapCodec<? extends AbstractSkullBlock> getCodec();

    @Override
    public BlockEntity createBlockEntity(BlockPos pos, BlockState state) {
        return new SkullBlockEntity(pos, state);
    }

    @Override
    public <T extends BlockEntity> @Nullable BlockEntityTicker<T> getTicker(World world, BlockState state, BlockEntityType<T> type) {
        if (world.isClient()) {
            boolean bl;
            boolean bl2 = bl = state.isOf(Blocks.DRAGON_HEAD) || state.isOf(Blocks.DRAGON_WALL_HEAD) || state.isOf(Blocks.PIGLIN_HEAD) || state.isOf(Blocks.PIGLIN_WALL_HEAD);
            if (bl) {
                return AbstractSkullBlock.validateTicker(type, BlockEntityType.SKULL, SkullBlockEntity::tick);
            }
        }
        return null;
    }

    public SkullBlock.SkullType getSkullType() {
        return this.type;
    }

    @Override
    protected boolean canPathfindThrough(BlockState state, NavigationType type) {
        return false;
    }

    @Override
    protected void appendProperties(StateManager.Builder<Block, BlockState> builder) {
        builder.add(POWERED);
    }

    @Override
    public BlockState getPlacementState(ItemPlacementContext ctx) {
        return (BlockState)this.getDefaultState().with(POWERED, ctx.getWorld().isReceivingRedstonePower(ctx.getBlockPos()));
    }

    @Override
    protected void neighborUpdate(BlockState state, World world, BlockPos pos, Block sourceBlock, @Nullable WireOrientation wireOrientation, boolean notify) {
        if (world.isClient()) {
            return;
        }
        boolean bl = world.isReceivingRedstonePower(pos);
        if (bl != state.get(POWERED)) {
            world.setBlockState(pos, (BlockState)state.with(POWERED, bl), 2);
        }
    }
}


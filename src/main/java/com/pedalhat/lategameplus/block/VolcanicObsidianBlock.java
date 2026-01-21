package com.pedalhat.lategameplus.block;

import com.mojang.serialization.MapCodec;
import net.minecraft.block.AbstractBlock;
import net.minecraft.block.Block;
import net.minecraft.block.BlockState;
import net.minecraft.block.Blocks;
import net.minecraft.item.ItemStack;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.state.StateManager;
import net.minecraft.state.property.IntProperty;
import net.minecraft.state.property.Properties;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Direction;
import net.minecraft.util.math.MathHelper;
import net.minecraft.util.math.random.Random;
import net.minecraft.world.BlockView;
import net.minecraft.world.World;
import net.minecraft.world.WorldView;
import net.minecraft.world.block.WireOrientation;
import org.jspecify.annotations.Nullable;

public class VolcanicObsidianBlock extends Block {
    public static final MapCodec<VolcanicObsidianBlock> CODEC = createCodec(VolcanicObsidianBlock::new);
    public static final IntProperty AGE = Properties.AGE_3;
    public static final int MAX_AGE = 3;
    private static final int NEIGHBORS_CHECKED_ON_SCHEDULED_TICK = 4;
    private static final int NEIGHBORS_CHECKED_ON_NEIGHBOR_UPDATE = 2;

    public VolcanicObsidianBlock(AbstractBlock.Settings settings) {
        super(settings);
        this.setDefaultState(this.stateManager.getDefaultState().with(AGE, 0));
    }

    @Override
    public MapCodec<? extends Block> getCodec() {
        return CODEC;
    }

    @Override
    public void onBlockAdded(BlockState state, World world, BlockPos pos, BlockState oldState, boolean notify) {
        world.scheduleBlockTick(pos, this, MathHelper.nextInt(world.getRandom(), 60, 120));
    }

    @Override
    protected void scheduledTick(BlockState state, ServerWorld world, BlockPos pos, Random random) {
        if (random.nextInt(3) == 0 || this.canMelt(world, pos, NEIGHBORS_CHECKED_ON_SCHEDULED_TICK)) {
            int lightLevel = world.getRegistryKey() == World.END
                ? world.getLightLevel(net.minecraft.world.LightType.BLOCK, pos)
                : world.getLightLevel(pos);
            if (lightLevel > 11 - state.get(AGE) - state.getOpacity() && this.increaseAge(state, world, pos)) {
                BlockPos.Mutable mutable = new BlockPos.Mutable();
                for (Direction direction : Direction.values()) {
                    mutable.set(pos, direction);
                    BlockState neighborState = world.getBlockState(mutable);
                    if (!neighborState.isOf(this) || this.increaseAge(neighborState, world, mutable)) {
                        continue;
                    }
                    world.scheduleBlockTick(mutable, this, MathHelper.nextInt(random, 20, 40));
                }
                return;
            }
        }
        world.scheduleBlockTick(pos, this, MathHelper.nextInt(random, 20, 40));
    }

    private boolean increaseAge(BlockState state, World world, BlockPos pos) {
        int age = state.get(AGE);
        if (age < MAX_AGE) {
            world.setBlockState(pos, state.with(AGE, age + 1), Block.NOTIFY_LISTENERS);
            return false;
        }
        this.melt(world, pos);
        return true;
    }

    @Override
    protected void neighborUpdate(
        BlockState state,
        World world,
        BlockPos pos,
        Block sourceBlock,
        @Nullable WireOrientation wireOrientation,
        boolean notify
    ) {
        if (sourceBlock.getDefaultState().isOf(this) && this.canMelt(world, pos, NEIGHBORS_CHECKED_ON_NEIGHBOR_UPDATE)) {
            this.melt(world, pos);
        }
        super.neighborUpdate(state, world, pos, sourceBlock, wireOrientation, notify);
    }

    private boolean canMelt(BlockView world, BlockPos pos, int maxNeighbors) {
        int neighbors = 0;
        BlockPos.Mutable mutable = new BlockPos.Mutable();
        for (Direction direction : Direction.values()) {
            mutable.set(pos, direction);
            if (!world.getBlockState(mutable).isOf(this) || ++neighbors < maxNeighbors) {
                continue;
            }
            return false;
        }
        return true;
    }

    private void melt(World world, BlockPos pos) {
        world.setBlockState(pos, Blocks.LAVA.getDefaultState(), Block.NOTIFY_ALL);
        world.updateNeighbor(pos, Blocks.LAVA, null);
    }

    @Override
    protected void appendProperties(StateManager.Builder<Block, BlockState> builder) {
        builder.add(AGE);
    }

    @Override
    protected ItemStack getPickStack(WorldView world, BlockPos pos, BlockState state, boolean includeData) {
        return ItemStack.EMPTY;
    }
}

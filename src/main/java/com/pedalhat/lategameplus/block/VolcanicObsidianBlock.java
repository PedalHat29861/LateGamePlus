package com.pedalhat.lategameplus.block;

import com.mojang.serialization.MapCodec;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.util.Mth;
import net.minecraft.util.RandomSource;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.BlockGetter;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.LevelReader;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.state.BlockBehaviour;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.StateDefinition;
import net.minecraft.world.level.block.state.properties.BlockStateProperties;
import net.minecraft.world.level.block.state.properties.IntegerProperty;
import net.minecraft.world.level.redstone.Orientation;
import org.jspecify.annotations.Nullable;

public class VolcanicObsidianBlock extends Block {
    public static final MapCodec<VolcanicObsidianBlock> CODEC = simpleCodec(VolcanicObsidianBlock::new);
    public static final IntegerProperty AGE = BlockStateProperties.AGE_3;
    public static final int MAX_AGE = 3;
    private static final int NEIGHBORS_CHECKED_ON_SCHEDULED_TICK = 4;
    private static final int NEIGHBORS_CHECKED_ON_NEIGHBOR_UPDATE = 2;

    public VolcanicObsidianBlock(BlockBehaviour.Properties settings) {
        super(settings);
        this.registerDefaultState(this.stateDefinition.any().setValue(AGE, 0));
    }

    @Override
    public MapCodec<? extends Block> codec() {
        return CODEC;
    }

    @Override
    public void onPlace(BlockState state, Level world, BlockPos pos, BlockState oldState, boolean notify) {
        world.scheduleTick(pos, this, Mth.nextInt(world.getRandom(), 60, 120));
    }

    @Override
    protected void tick(BlockState state, ServerLevel world, BlockPos pos, RandomSource random) {
        if (random.nextInt(3) == 0 || this.canMelt(world, pos, NEIGHBORS_CHECKED_ON_SCHEDULED_TICK)) {
            int lightLevel = world.dimension() == Level.END
                ? world.getBrightness(net.minecraft.world.level.LightLayer.BLOCK, pos)
                : world.getMaxLocalRawBrightness(pos);
            if (lightLevel > 11 - state.getValue(AGE) - state.getLightDampening() && this.increaseAge(state, world, pos)) {
                BlockPos.MutableBlockPos mutable = new BlockPos.MutableBlockPos();
                for (Direction direction : Direction.values()) {
                    mutable.setWithOffset(pos, direction);
                    BlockState neighborState = world.getBlockState(mutable);
                    if (!neighborState.is(this) || this.increaseAge(neighborState, world, mutable)) {
                        continue;
                    }
                    world.scheduleTick(mutable, this, Mth.nextInt(random, 20, 40));
                }
                return;
            }
        }
        world.scheduleTick(pos, this, Mth.nextInt(random, 20, 40));
    }

    private boolean increaseAge(BlockState state, Level world, BlockPos pos) {
        int age = state.getValue(AGE);
        if (age < MAX_AGE) {
            world.setBlock(pos, state.setValue(AGE, age + 1), Block.UPDATE_CLIENTS);
            return false;
        }
        this.melt(world, pos);
        return true;
    }

    @Override
    protected void neighborChanged(
        BlockState state,
        Level world,
        BlockPos pos,
        Block sourceBlock,
        @Nullable Orientation wireOrientation,
        boolean notify
    ) {
        if (sourceBlock.defaultBlockState().is(this) && this.canMelt(world, pos, NEIGHBORS_CHECKED_ON_NEIGHBOR_UPDATE)) {
            this.melt(world, pos);
        }
        super.neighborChanged(state, world, pos, sourceBlock, wireOrientation, notify);
    }

    private boolean canMelt(BlockGetter world, BlockPos pos, int maxNeighbors) {
        int neighbors = 0;
        BlockPos.MutableBlockPos mutable = new BlockPos.MutableBlockPos();
        for (Direction direction : Direction.values()) {
            mutable.setWithOffset(pos, direction);
            if (!world.getBlockState(mutable).is(this) || ++neighbors < maxNeighbors) {
                continue;
            }
            return false;
        }
        return true;
    }

    private void melt(Level world, BlockPos pos) {
        world.setBlock(pos, Blocks.LAVA.defaultBlockState(), Block.UPDATE_ALL);
        world.neighborChanged(pos, Blocks.LAVA, null);
    }

    @Override
    protected void createBlockStateDefinition(StateDefinition.Builder<Block, BlockState> builder) {
        builder.add(AGE);
    }

    @Override
    protected ItemStack getCloneItemStack(LevelReader world, BlockPos pos, BlockState state, boolean includeData) {
        return ItemStack.EMPTY;
    }
}

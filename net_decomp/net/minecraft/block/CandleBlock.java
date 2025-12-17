/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  com.mojang.serialization.MapCodec
 *  it.unimi.dsi.fastutil.ints.Int2ObjectMap
 *  it.unimi.dsi.fastutil.ints.Int2ObjectOpenHashMap
 */
package net.minecraft.block;

import com.mojang.serialization.MapCodec;
import it.unimi.dsi.fastutil.ints.Int2ObjectMap;
import it.unimi.dsi.fastutil.ints.Int2ObjectOpenHashMap;
import java.util.List;
import java.util.function.ToIntFunction;
import net.minecraft.block.AbstractBlock;
import net.minecraft.block.AbstractCandleBlock;
import net.minecraft.block.Block;
import net.minecraft.block.BlockState;
import net.minecraft.block.ShapeContext;
import net.minecraft.block.Waterloggable;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.fluid.FluidState;
import net.minecraft.fluid.Fluids;
import net.minecraft.item.ItemPlacementContext;
import net.minecraft.item.ItemStack;
import net.minecraft.registry.tag.BlockTags;
import net.minecraft.state.StateManager;
import net.minecraft.state.property.BooleanProperty;
import net.minecraft.state.property.IntProperty;
import net.minecraft.state.property.Properties;
import net.minecraft.util.ActionResult;
import net.minecraft.util.Hand;
import net.minecraft.util.Util;
import net.minecraft.util.hit.BlockHitResult;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Direction;
import net.minecraft.util.math.Vec3d;
import net.minecraft.util.math.random.Random;
import net.minecraft.util.shape.VoxelShape;
import net.minecraft.world.BlockView;
import net.minecraft.world.World;
import net.minecraft.world.WorldAccess;
import net.minecraft.world.WorldView;
import net.minecraft.world.tick.ScheduledTickView;

public class CandleBlock
extends AbstractCandleBlock
implements Waterloggable {
    public static final MapCodec<CandleBlock> CODEC = CandleBlock.createCodec(CandleBlock::new);
    public static final int field_31050 = 1;
    public static final int MAX_CANDLE_AMOUNT = 4;
    public static final IntProperty CANDLES = Properties.CANDLES;
    public static final BooleanProperty LIT = AbstractCandleBlock.LIT;
    public static final BooleanProperty WATERLOGGED = Properties.WATERLOGGED;
    public static final ToIntFunction<BlockState> STATE_TO_LUMINANCE = state -> state.get(LIT) != false ? 3 * state.get(CANDLES) : 0;
    private static final Int2ObjectMap<List<Vec3d>> CANDLES_TO_PARTICLE_OFFSETS = (Int2ObjectMap)Util.make(new Int2ObjectOpenHashMap(4), int2ObjectOpenHashMap -> {
        float f = 0.0625f;
        int2ObjectOpenHashMap.put(1, List.of(new Vec3d(8.0, 8.0, 8.0).multiply(0.0625)));
        int2ObjectOpenHashMap.put(2, List.of(new Vec3d(6.0, 7.0, 8.0).multiply(0.0625), new Vec3d(10.0, 8.0, 7.0).multiply(0.0625)));
        int2ObjectOpenHashMap.put(3, List.of(new Vec3d(8.0, 5.0, 10.0).multiply(0.0625), new Vec3d(6.0, 7.0, 8.0).multiply(0.0625), new Vec3d(9.0, 8.0, 7.0).multiply(0.0625)));
        int2ObjectOpenHashMap.put(4, List.of(new Vec3d(7.0, 5.0, 9.0).multiply(0.0625), new Vec3d(10.0, 7.0, 9.0).multiply(0.0625), new Vec3d(6.0, 7.0, 6.0).multiply(0.0625), new Vec3d(9.0, 8.0, 6.0).multiply(0.0625)));
    });
    private static final VoxelShape[] SHAPES_BY_CANDLES = new VoxelShape[]{Block.createColumnShape(2.0, 0.0, 6.0), Block.createCuboidShape(5.0, 0.0, 6.0, 11.0, 6.0, 9.0), Block.createCuboidShape(5.0, 0.0, 6.0, 10.0, 6.0, 11.0), Block.createCuboidShape(5.0, 0.0, 5.0, 11.0, 6.0, 10.0)};

    public MapCodec<CandleBlock> getCodec() {
        return CODEC;
    }

    public CandleBlock(AbstractBlock.Settings settings) {
        super(settings);
        this.setDefaultState((BlockState)((BlockState)((BlockState)((BlockState)this.stateManager.getDefaultState()).with(CANDLES, 1)).with(LIT, false)).with(WATERLOGGED, false));
    }

    @Override
    protected ActionResult onUseWithItem(ItemStack stack, BlockState state, World world, BlockPos pos, PlayerEntity player, Hand hand, BlockHitResult hit) {
        if (stack.isEmpty() && player.getAbilities().allowModifyWorld && state.get(LIT).booleanValue()) {
            CandleBlock.extinguish(player, state, world, pos);
            return ActionResult.SUCCESS;
        }
        return super.onUseWithItem(stack, state, world, pos, player, hand, hit);
    }

    @Override
    protected boolean canReplace(BlockState state, ItemPlacementContext context) {
        if (!context.shouldCancelInteraction() && context.getStack().getItem() == this.asItem() && state.get(CANDLES) < 4) {
            return true;
        }
        return super.canReplace(state, context);
    }

    @Override
    public BlockState getPlacementState(ItemPlacementContext ctx) {
        BlockState blockState = ctx.getWorld().getBlockState(ctx.getBlockPos());
        if (blockState.isOf(this)) {
            return (BlockState)blockState.cycle(CANDLES);
        }
        FluidState fluidState = ctx.getWorld().getFluidState(ctx.getBlockPos());
        boolean bl = fluidState.getFluid() == Fluids.WATER;
        return (BlockState)super.getPlacementState(ctx).with(WATERLOGGED, bl);
    }

    @Override
    protected BlockState getStateForNeighborUpdate(BlockState state, WorldView world, ScheduledTickView tickView, BlockPos pos, Direction direction, BlockPos neighborPos, BlockState neighborState, Random random) {
        if (state.get(WATERLOGGED).booleanValue()) {
            tickView.scheduleFluidTick(pos, Fluids.WATER, Fluids.WATER.getTickRate(world));
        }
        return super.getStateForNeighborUpdate(state, world, tickView, pos, direction, neighborPos, neighborState, random);
    }

    @Override
    protected FluidState getFluidState(BlockState state) {
        if (state.get(WATERLOGGED).booleanValue()) {
            return Fluids.WATER.getStill(false);
        }
        return super.getFluidState(state);
    }

    @Override
    protected VoxelShape getOutlineShape(BlockState state, BlockView world, BlockPos pos, ShapeContext context) {
        return SHAPES_BY_CANDLES[state.get(CANDLES) - 1];
    }

    @Override
    protected void appendProperties(StateManager.Builder<Block, BlockState> builder) {
        builder.add(CANDLES, LIT, WATERLOGGED);
    }

    @Override
    public boolean tryFillWithFluid(WorldAccess world, BlockPos pos, BlockState state, FluidState fluidState) {
        if (state.get(WATERLOGGED).booleanValue() || fluidState.getFluid() != Fluids.WATER) {
            return false;
        }
        BlockState blockState = (BlockState)state.with(WATERLOGGED, true);
        if (state.get(LIT).booleanValue()) {
            CandleBlock.extinguish(null, blockState, world, pos);
        } else {
            world.setBlockState(pos, blockState, 3);
        }
        world.scheduleFluidTick(pos, fluidState.getFluid(), fluidState.getFluid().getTickRate(world));
        return true;
    }

    public static boolean canBeLit(BlockState state) {
        return state.isIn(BlockTags.CANDLES, statex -> statex.contains(LIT) && statex.contains(WATERLOGGED)) && state.get(LIT) == false && state.get(WATERLOGGED) == false;
    }

    @Override
    protected Iterable<Vec3d> getParticleOffsets(BlockState state) {
        return (Iterable)CANDLES_TO_PARTICLE_OFFSETS.get(state.get(CANDLES).intValue());
    }

    @Override
    protected boolean isNotLit(BlockState state) {
        return state.get(WATERLOGGED) == false && super.isNotLit(state);
    }

    @Override
    protected boolean canPlaceAt(BlockState state, WorldView world, BlockPos pos) {
        return Block.sideCoversSmallSquare(world, pos.down(), Direction.UP);
    }
}


/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  com.mojang.serialization.MapCodec
 *  org.jspecify.annotations.Nullable
 */
package net.minecraft.block;

import com.mojang.serialization.MapCodec;
import java.lang.invoke.MethodHandle;
import java.lang.runtime.ObjectMethods;
import java.util.function.Function;
import net.minecraft.block.AbstractBlock;
import net.minecraft.block.Block;
import net.minecraft.block.BlockState;
import net.minecraft.block.Blocks;
import net.minecraft.block.CropBlock;
import net.minecraft.block.Fertilizable;
import net.minecraft.block.ShapeContext;
import net.minecraft.block.TallPlantBlock;
import net.minecraft.block.enums.DoubleBlockHalf;
import net.minecraft.entity.Entity;
import net.minecraft.entity.EntityCollisionHandler;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.mob.RavagerEntity;
import net.minecraft.item.ItemPlacementContext;
import net.minecraft.item.ItemStack;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.state.StateManager;
import net.minecraft.state.property.EnumProperty;
import net.minecraft.state.property.IntProperty;
import net.minecraft.state.property.Properties;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Direction;
import net.minecraft.util.math.random.Random;
import net.minecraft.util.shape.VoxelShape;
import net.minecraft.util.shape.VoxelShapes;
import net.minecraft.world.BlockView;
import net.minecraft.world.World;
import net.minecraft.world.WorldView;
import net.minecraft.world.rule.GameRules;
import net.minecraft.world.tick.ScheduledTickView;
import org.jspecify.annotations.Nullable;

public class PitcherCropBlock
extends TallPlantBlock
implements Fertilizable {
    public static final MapCodec<PitcherCropBlock> CODEC = PitcherCropBlock.createCodec(PitcherCropBlock::new);
    public static final int field_43240 = 4;
    public static final IntProperty AGE = Properties.AGE_4;
    public static final EnumProperty<DoubleBlockHalf> HALF = TallPlantBlock.HALF;
    private static final int field_43241 = 3;
    private static final int field_43391 = 1;
    private static final VoxelShape AGE_0_SHAPE = Block.createColumnShape(6.0, -1.0, 3.0);
    private static final VoxelShape LOWER_COLLISION_SHAPE = Block.createColumnShape(10.0, -1.0, 5.0);
    private final Function<BlockState, VoxelShape> shapeFunction = this.createShapeFunction();

    public MapCodec<PitcherCropBlock> getCodec() {
        return CODEC;
    }

    public PitcherCropBlock(AbstractBlock.Settings settings) {
        super(settings);
    }

    private Function<BlockState, VoxelShape> createShapeFunction() {
        int[] is = new int[]{0, 9, 11, 22, 26};
        return this.createShapeFunction(state -> {
            int i = (state.get(AGE) == 0 ? 4 : 6) + is[state.get(AGE)];
            int j = state.get(AGE) == 0 ? 6 : 10;
            return switch (state.get(HALF)) {
                default -> throw new MatchException(null, null);
                case DoubleBlockHalf.LOWER -> Block.createColumnShape(j, -1.0, Math.min(16, -1 + i));
                case DoubleBlockHalf.UPPER -> Block.createColumnShape(j, 0.0, Math.max(0, -1 + i - 16));
            };
        });
    }

    @Override
    public @Nullable BlockState getPlacementState(ItemPlacementContext ctx) {
        return this.getDefaultState();
    }

    @Override
    public VoxelShape getOutlineShape(BlockState state, BlockView world, BlockPos pos, ShapeContext context) {
        return this.shapeFunction.apply(state);
    }

    @Override
    public VoxelShape getCollisionShape(BlockState state, BlockView world, BlockPos pos, ShapeContext context) {
        if (state.get(HALF) == DoubleBlockHalf.LOWER) {
            return state.get(AGE) == 0 ? AGE_0_SHAPE : LOWER_COLLISION_SHAPE;
        }
        return VoxelShapes.empty();
    }

    @Override
    public BlockState getStateForNeighborUpdate(BlockState state, WorldView world, ScheduledTickView tickView, BlockPos pos, Direction direction, BlockPos neighborPos, BlockState neighborState, Random random) {
        if (PitcherCropBlock.isDoubleTallAtAge(state.get(AGE))) {
            return super.getStateForNeighborUpdate(state, world, tickView, pos, direction, neighborPos, neighborState, random);
        }
        return state.canPlaceAt(world, pos) ? state : Blocks.AIR.getDefaultState();
    }

    @Override
    public boolean canPlaceAt(BlockState state, WorldView world, BlockPos pos) {
        if (PitcherCropBlock.isLowerHalf(state) && !PitcherCropBlock.canPlaceAt(world, pos)) {
            return false;
        }
        return super.canPlaceAt(state, world, pos);
    }

    @Override
    protected boolean canPlantOnTop(BlockState floor, BlockView world, BlockPos pos) {
        return floor.isOf(Blocks.FARMLAND);
    }

    @Override
    protected void appendProperties(StateManager.Builder<Block, BlockState> builder) {
        builder.add(AGE);
        super.appendProperties(builder);
    }

    @Override
    public void onEntityCollision(BlockState state, World world, BlockPos pos, Entity entity, EntityCollisionHandler handler, boolean bl) {
        if (world instanceof ServerWorld) {
            ServerWorld serverWorld = (ServerWorld)world;
            if (entity instanceof RavagerEntity && serverWorld.getGameRules().getValue(GameRules.DO_MOB_GRIEFING).booleanValue()) {
                serverWorld.breakBlock(pos, true, entity);
            }
        }
    }

    @Override
    public boolean canReplace(BlockState state, ItemPlacementContext context) {
        return false;
    }

    @Override
    public void onPlaced(World world, BlockPos pos, BlockState state, @Nullable LivingEntity placer, ItemStack itemStack) {
    }

    @Override
    public boolean hasRandomTicks(BlockState state) {
        return state.get(HALF) == DoubleBlockHalf.LOWER && !this.isFullyGrown(state);
    }

    @Override
    public void randomTick(BlockState state, ServerWorld world, BlockPos pos, Random random) {
        boolean bl;
        float f = CropBlock.getAvailableMoisture(this, world, pos);
        boolean bl2 = bl = random.nextInt((int)(25.0f / f) + 1) == 0;
        if (bl) {
            this.tryGrow(world, state, pos, 1);
        }
    }

    private void tryGrow(ServerWorld world, BlockState state, BlockPos pos, int amount) {
        int i = Math.min(state.get(AGE) + amount, 4);
        if (!this.canGrow((WorldView)world, pos, state, i)) {
            return;
        }
        BlockState blockState = (BlockState)state.with(AGE, i);
        world.setBlockState(pos, blockState, 2);
        if (PitcherCropBlock.isDoubleTallAtAge(i)) {
            world.setBlockState(pos.up(), (BlockState)blockState.with(HALF, DoubleBlockHalf.UPPER), 3);
        }
    }

    private static boolean canGrowAt(WorldView world, BlockPos pos) {
        BlockState blockState = world.getBlockState(pos);
        return blockState.isAir() || blockState.isOf(Blocks.PITCHER_CROP);
    }

    private static boolean canPlaceAt(WorldView world, BlockPos pos) {
        return CropBlock.hasEnoughLightAt(world, pos);
    }

    private static boolean isLowerHalf(BlockState state) {
        return state.isOf(Blocks.PITCHER_CROP) && state.get(HALF) == DoubleBlockHalf.LOWER;
    }

    private static boolean isDoubleTallAtAge(int age) {
        return age >= 3;
    }

    private boolean canGrow(WorldView world, BlockPos pos, BlockState state, int age) {
        return !this.isFullyGrown(state) && PitcherCropBlock.canPlaceAt(world, pos) && (!PitcherCropBlock.isDoubleTallAtAge(age) || PitcherCropBlock.canGrowAt(world, pos.up()));
    }

    private boolean isFullyGrown(BlockState state) {
        return state.get(AGE) >= 4;
    }

    private @Nullable LowerHalfContext getLowerHalfContext(WorldView world, BlockPos pos, BlockState state) {
        if (PitcherCropBlock.isLowerHalf(state)) {
            return new LowerHalfContext(pos, state);
        }
        BlockPos blockPos = pos.down();
        BlockState blockState = world.getBlockState(blockPos);
        if (PitcherCropBlock.isLowerHalf(blockState)) {
            return new LowerHalfContext(blockPos, blockState);
        }
        return null;
    }

    @Override
    public boolean isFertilizable(WorldView world, BlockPos pos, BlockState state) {
        LowerHalfContext lowerHalfContext = this.getLowerHalfContext(world, pos, state);
        if (lowerHalfContext == null) {
            return false;
        }
        return this.canGrow(world, lowerHalfContext.pos, lowerHalfContext.state, lowerHalfContext.state.get(AGE) + 1);
    }

    @Override
    public boolean canGrow(World world, Random random, BlockPos pos, BlockState state) {
        return true;
    }

    @Override
    public void grow(ServerWorld world, Random random, BlockPos pos, BlockState state) {
        LowerHalfContext lowerHalfContext = this.getLowerHalfContext(world, pos, state);
        if (lowerHalfContext == null) {
            return;
        }
        this.tryGrow(world, lowerHalfContext.state, lowerHalfContext.pos, 1);
    }

    static final class LowerHalfContext
    extends Record {
        final BlockPos pos;
        final BlockState state;

        LowerHalfContext(BlockPos pos, BlockState state) {
            this.pos = pos;
            this.state = state;
        }

        @Override
        public final String toString() {
            return ObjectMethods.bootstrap("toString", new MethodHandle[]{LowerHalfContext.class, "pos;state", "pos", "state"}, this);
        }

        @Override
        public final int hashCode() {
            return (int)ObjectMethods.bootstrap("hashCode", new MethodHandle[]{LowerHalfContext.class, "pos;state", "pos", "state"}, this);
        }

        @Override
        public final boolean equals(Object object) {
            return (boolean)ObjectMethods.bootstrap("equals", new MethodHandle[]{LowerHalfContext.class, "pos;state", "pos", "state"}, this, object);
        }

        public BlockPos pos() {
            return this.pos;
        }

        public BlockState state() {
            return this.state;
        }
    }
}


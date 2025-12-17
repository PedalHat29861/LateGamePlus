/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  com.google.common.collect.Maps
 *  it.unimi.dsi.fastutil.objects.Object2ByteLinkedOpenHashMap
 *  it.unimi.dsi.fastutil.shorts.Short2BooleanMap
 *  it.unimi.dsi.fastutil.shorts.Short2BooleanOpenHashMap
 *  it.unimi.dsi.fastutil.shorts.Short2ObjectMap
 *  it.unimi.dsi.fastutil.shorts.Short2ObjectOpenHashMap
 */
package net.minecraft.fluid;

import com.google.common.collect.Maps;
import it.unimi.dsi.fastutil.objects.Object2ByteLinkedOpenHashMap;
import it.unimi.dsi.fastutil.shorts.Short2BooleanMap;
import it.unimi.dsi.fastutil.shorts.Short2BooleanOpenHashMap;
import it.unimi.dsi.fastutil.shorts.Short2ObjectMap;
import it.unimi.dsi.fastutil.shorts.Short2ObjectOpenHashMap;
import java.lang.invoke.MethodHandle;
import java.lang.runtime.ObjectMethods;
import java.util.EnumMap;
import java.util.Map;
import net.minecraft.SharedConstants;
import net.minecraft.block.Block;
import net.minecraft.block.BlockState;
import net.minecraft.block.Blocks;
import net.minecraft.block.DoorBlock;
import net.minecraft.block.FluidFillable;
import net.minecraft.block.IceBlock;
import net.minecraft.fluid.Fluid;
import net.minecraft.fluid.FluidState;
import net.minecraft.fluid.Fluids;
import net.minecraft.registry.tag.BlockTags;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.state.StateManager;
import net.minecraft.state.property.BooleanProperty;
import net.minecraft.state.property.IntProperty;
import net.minecraft.state.property.Properties;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Direction;
import net.minecraft.util.math.Vec3d;
import net.minecraft.util.math.Vec3i;
import net.minecraft.util.shape.VoxelShape;
import net.minecraft.util.shape.VoxelShapes;
import net.minecraft.world.BlockView;
import net.minecraft.world.World;
import net.minecraft.world.WorldAccess;
import net.minecraft.world.WorldView;

public abstract class FlowableFluid
extends Fluid {
    public static final BooleanProperty FALLING = Properties.FALLING;
    public static final IntProperty LEVEL = Properties.LEVEL_1_8;
    private static final int CACHE_SIZE = 200;
    private static final ThreadLocal<Object2ByteLinkedOpenHashMap<NeighborGroup>> field_15901 = ThreadLocal.withInitial(() -> {
        Object2ByteLinkedOpenHashMap<NeighborGroup> object2ByteLinkedOpenHashMap = new Object2ByteLinkedOpenHashMap<NeighborGroup>(200){

            protected void rehash(int i) {
            }
        };
        object2ByteLinkedOpenHashMap.defaultReturnValue((byte)127);
        return object2ByteLinkedOpenHashMap;
    });
    private final Map<FluidState, VoxelShape> shapeCache = Maps.newIdentityHashMap();

    @Override
    protected void appendProperties(StateManager.Builder<Fluid, FluidState> builder) {
        builder.add(FALLING);
    }

    @Override
    public Vec3d getVelocity(BlockView world, BlockPos pos, FluidState state) {
        double d = 0.0;
        double e = 0.0;
        BlockPos.Mutable mutable = new BlockPos.Mutable();
        for (Direction direction : Direction.Type.HORIZONTAL) {
            mutable.set((Vec3i)pos, direction);
            FluidState fluidState = world.getFluidState(mutable);
            if (!this.isEmptyOrThis(fluidState)) continue;
            float f = fluidState.getHeight();
            float g = 0.0f;
            if (f == 0.0f) {
                Vec3i blockPos;
                FluidState fluidState2;
                if (!world.getBlockState(mutable).blocksMovement() && this.isEmptyOrThis(fluidState2 = world.getFluidState((BlockPos)(blockPos = mutable.down()))) && (f = fluidState2.getHeight()) > 0.0f) {
                    g = state.getHeight() - (f - 0.8888889f);
                }
            } else if (f > 0.0f) {
                g = state.getHeight() - f;
            }
            if (g == 0.0f) continue;
            d += (double)((float)direction.getOffsetX() * g);
            e += (double)((float)direction.getOffsetZ() * g);
        }
        Vec3d vec3d = new Vec3d(d, 0.0, e);
        if (state.get(FALLING).booleanValue()) {
            for (Direction direction2 : Direction.Type.HORIZONTAL) {
                mutable.set((Vec3i)pos, direction2);
                if (!this.isFlowBlocked(world, mutable, direction2) && !this.isFlowBlocked(world, (BlockPos)mutable.up(), direction2)) continue;
                vec3d = vec3d.normalize().add(0.0, -6.0, 0.0);
                break;
            }
        }
        return vec3d.normalize();
    }

    private boolean isEmptyOrThis(FluidState state) {
        return state.isEmpty() || state.getFluid().matchesType(this);
    }

    protected boolean isFlowBlocked(BlockView world, BlockPos pos, Direction direction) {
        BlockState blockState = world.getBlockState(pos);
        FluidState fluidState = world.getFluidState(pos);
        if (fluidState.getFluid().matchesType(this)) {
            return false;
        }
        if (direction == Direction.UP) {
            return true;
        }
        if (blockState.getBlock() instanceof IceBlock) {
            return false;
        }
        return blockState.isSideSolidFullSquare(world, pos, direction);
    }

    protected void tryFlow(ServerWorld world, BlockPos fluidPos, BlockState blockState, FluidState fluidState) {
        FluidState fluidState3;
        Fluid fluid;
        FluidState fluidState2;
        BlockState blockState2;
        if (fluidState.isEmpty()) {
            return;
        }
        BlockPos blockPos = fluidPos.down();
        if (this.canFlowThrough(world, fluidPos, blockState, Direction.DOWN, blockPos, blockState2 = world.getBlockState(blockPos), fluidState2 = blockState2.getFluidState()) && fluidState2.canBeReplacedWith(world, blockPos, fluid = (fluidState3 = this.getUpdatedState(world, blockPos, blockState2)).getFluid(), Direction.DOWN) && FlowableFluid.canFillWithFluid(world, blockPos, blockState2, fluid)) {
            this.flow(world, blockPos, blockState2, Direction.DOWN, fluidState3);
            if (this.countNeighboringSources(world, fluidPos) >= 3) {
                this.flowToSides(world, fluidPos, fluidState, blockState);
            }
            return;
        }
        if (fluidState.isStill() || !this.canFlowDownTo(world, fluidPos, blockState, blockPos, blockState2)) {
            this.flowToSides(world, fluidPos, fluidState, blockState);
        }
    }

    private void flowToSides(ServerWorld world, BlockPos pos, FluidState fluidState, BlockState blockState) {
        int i = fluidState.getLevel() - this.getLevelDecreasePerBlock(world);
        if (fluidState.get(FALLING).booleanValue()) {
            i = 7;
        }
        if (i <= 0) {
            return;
        }
        Map<Direction, FluidState> map = this.getSpread(world, pos, blockState);
        for (Map.Entry<Direction, FluidState> entry : map.entrySet()) {
            Direction direction = entry.getKey();
            FluidState fluidState2 = entry.getValue();
            BlockPos blockPos = pos.offset(direction);
            this.flow(world, blockPos, world.getBlockState(blockPos), direction, fluidState2);
        }
    }

    protected FluidState getUpdatedState(ServerWorld world, BlockPos pos, BlockState state) {
        BlockPos.Mutable blockPos2;
        BlockState blockState3;
        FluidState fluidState3;
        int i = 0;
        int j = 0;
        BlockPos.Mutable mutable = new BlockPos.Mutable();
        for (Direction direction : Direction.Type.HORIZONTAL) {
            BlockPos.Mutable blockPos = mutable.set((Vec3i)pos, direction);
            BlockState blockState = world.getBlockState(blockPos);
            FluidState fluidState = blockState.getFluidState();
            if (!fluidState.getFluid().matchesType(this) || !FlowableFluid.receivesFlow(direction, world, pos, state, blockPos, blockState)) continue;
            if (fluidState.isStill()) {
                ++j;
            }
            i = Math.max(i, fluidState.getLevel());
        }
        if (j >= 2 && this.isInfinite(world)) {
            BlockState blockState2 = world.getBlockState(mutable.set((Vec3i)pos, Direction.DOWN));
            FluidState fluidState2 = blockState2.getFluidState();
            if (blockState2.isSolid() || this.isMatchingAndStill(fluidState2)) {
                return this.getStill(false);
            }
        }
        if (!(fluidState3 = (blockState3 = world.getBlockState(blockPos2 = mutable.set((Vec3i)pos, Direction.UP))).getFluidState()).isEmpty() && fluidState3.getFluid().matchesType(this) && FlowableFluid.receivesFlow(Direction.UP, world, pos, state, blockPos2, blockState3)) {
            return this.getFlowing(8, true);
        }
        int k = i - this.getLevelDecreasePerBlock(world);
        if (k <= 0) {
            return Fluids.EMPTY.getDefaultState();
        }
        return this.getFlowing(k, false);
    }

    private static boolean receivesFlow(Direction face, BlockView world, BlockPos pos, BlockState state, BlockPos fromPos, BlockState fromState) {
        boolean bl;
        NeighborGroup neighborGroup;
        if (SharedConstants.DISABLE_LIQUID_SPREADING || SharedConstants.ONLY_GENERATE_HALF_THE_WORLD && fromPos.getZ() < 0) {
            return false;
        }
        VoxelShape voxelShape = fromState.getCollisionShape(world, fromPos);
        if (voxelShape == VoxelShapes.fullCube()) {
            return false;
        }
        VoxelShape voxelShape2 = state.getCollisionShape(world, pos);
        if (voxelShape2 == VoxelShapes.fullCube()) {
            return false;
        }
        if (voxelShape2 == VoxelShapes.empty() && voxelShape == VoxelShapes.empty()) {
            return true;
        }
        Object2ByteLinkedOpenHashMap<NeighborGroup> object2ByteLinkedOpenHashMap = state.getBlock().hasDynamicBounds() || fromState.getBlock().hasDynamicBounds() ? null : field_15901.get();
        if (object2ByteLinkedOpenHashMap != null) {
            neighborGroup = new NeighborGroup(state, fromState, face);
            byte b = object2ByteLinkedOpenHashMap.getAndMoveToFirst((Object)neighborGroup);
            if (b != 127) {
                return b != 0;
            }
        } else {
            neighborGroup = null;
        }
        boolean bl2 = bl = !VoxelShapes.adjacentSidesCoverSquare(voxelShape2, voxelShape, face);
        if (object2ByteLinkedOpenHashMap != null) {
            if (object2ByteLinkedOpenHashMap.size() == 200) {
                object2ByteLinkedOpenHashMap.removeLastByte();
            }
            object2ByteLinkedOpenHashMap.putAndMoveToFirst((Object)neighborGroup, (byte)(bl ? 1 : 0));
        }
        return bl;
    }

    public abstract Fluid getFlowing();

    public FluidState getFlowing(int level, boolean falling) {
        return (FluidState)((FluidState)this.getFlowing().getDefaultState().with(LEVEL, level)).with(FALLING, falling);
    }

    public abstract Fluid getStill();

    public FluidState getStill(boolean falling) {
        return (FluidState)this.getStill().getDefaultState().with(FALLING, falling);
    }

    protected abstract boolean isInfinite(ServerWorld var1);

    protected void flow(WorldAccess world, BlockPos pos, BlockState state, Direction direction, FluidState fluidState) {
        Block block = state.getBlock();
        if (block instanceof FluidFillable) {
            FluidFillable fluidFillable = (FluidFillable)((Object)block);
            fluidFillable.tryFillWithFluid(world, pos, state, fluidState);
        } else {
            if (!state.isAir()) {
                this.beforeBreakingBlock(world, pos, state);
            }
            world.setBlockState(pos, fluidState.getBlockState(), 3);
        }
    }

    protected abstract void beforeBreakingBlock(WorldAccess var1, BlockPos var2, BlockState var3);

    protected int getMinFlowDownDistance(WorldView world, BlockPos pos, int i, Direction direction, BlockState state, SpreadCache spreadCache) {
        int j = 1000;
        for (Direction direction2 : Direction.Type.HORIZONTAL) {
            int k;
            if (direction2 == direction) continue;
            BlockPos blockPos = pos.offset(direction2);
            BlockState blockState = spreadCache.getBlockState(blockPos);
            FluidState fluidState = blockState.getFluidState();
            if (!this.canFlowThrough(world, this.getFlowing(), pos, state, direction2, blockPos, blockState, fluidState)) continue;
            if (spreadCache.canFlowDownTo(blockPos)) {
                return i;
            }
            if (i >= this.getMaxFlowDistance(world) || (k = this.getMinFlowDownDistance(world, blockPos, i + 1, direction2.getOpposite(), blockState, spreadCache)) >= j) continue;
            j = k;
        }
        return j;
    }

    boolean canFlowDownTo(BlockView world, BlockPos pos, BlockState state, BlockPos fromPos, BlockState fromState) {
        if (!FlowableFluid.receivesFlow(Direction.DOWN, world, pos, state, fromPos, fromState)) {
            return false;
        }
        if (fromState.getFluidState().getFluid().matchesType(this)) {
            return true;
        }
        return FlowableFluid.canFill(world, fromPos, fromState, this.getFlowing());
    }

    private boolean canFlowThrough(BlockView world, Fluid fluid, BlockPos pos, BlockState state, Direction face, BlockPos fromPos, BlockState fromState, FluidState fluidState) {
        return this.canFlowThrough(world, pos, state, face, fromPos, fromState, fluidState) && FlowableFluid.canFillWithFluid(world, fromPos, fromState, fluid);
    }

    private boolean canFlowThrough(BlockView world, BlockPos pos, BlockState state, Direction face, BlockPos fromPos, BlockState fromState, FluidState fluidState) {
        return !this.isMatchingAndStill(fluidState) && FlowableFluid.canFill(fromState) && FlowableFluid.receivesFlow(face, world, pos, state, fromPos, fromState);
    }

    private boolean isMatchingAndStill(FluidState state) {
        return state.getFluid().matchesType(this) && state.isStill();
    }

    protected abstract int getMaxFlowDistance(WorldView var1);

    private int countNeighboringSources(WorldView world, BlockPos pos) {
        int i = 0;
        for (Direction direction : Direction.Type.HORIZONTAL) {
            BlockPos blockPos = pos.offset(direction);
            FluidState fluidState = world.getFluidState(blockPos);
            if (!this.isMatchingAndStill(fluidState)) continue;
            ++i;
        }
        return i;
    }

    protected Map<Direction, FluidState> getSpread(ServerWorld world, BlockPos pos, BlockState state) {
        int i = 1000;
        EnumMap map = Maps.newEnumMap(Direction.class);
        SpreadCache spreadCache = null;
        for (Direction direction : Direction.Type.HORIZONTAL) {
            int j;
            FluidState fluidState2;
            FluidState fluidState;
            BlockState blockState;
            BlockPos blockPos;
            if (!this.canFlowThrough(world, pos, state, direction, blockPos = pos.offset(direction), blockState = world.getBlockState(blockPos), fluidState = blockState.getFluidState()) || !FlowableFluid.canFillWithFluid(world, blockPos, blockState, (fluidState2 = this.getUpdatedState(world, blockPos, blockState)).getFluid())) continue;
            if (spreadCache == null) {
                spreadCache = new SpreadCache(world, pos);
            }
            if ((j = spreadCache.canFlowDownTo(blockPos) ? 0 : this.getMinFlowDownDistance(world, blockPos, 1, direction.getOpposite(), blockState, spreadCache)) < i) {
                map.clear();
            }
            if (j > i) continue;
            if (fluidState.canBeReplacedWith(world, blockPos, fluidState2.getFluid(), direction)) {
                map.put(direction, fluidState2);
            }
            i = j;
        }
        return map;
    }

    private static boolean canFill(BlockState state) {
        Block block = state.getBlock();
        if (block instanceof FluidFillable) {
            return true;
        }
        if (state.blocksMovement()) {
            return false;
        }
        return !(block instanceof DoorBlock) && !state.isIn(BlockTags.SIGNS) && !state.isOf(Blocks.LADDER) && !state.isOf(Blocks.SUGAR_CANE) && !state.isOf(Blocks.BUBBLE_COLUMN) && !state.isOf(Blocks.NETHER_PORTAL) && !state.isOf(Blocks.END_PORTAL) && !state.isOf(Blocks.END_GATEWAY) && !state.isOf(Blocks.STRUCTURE_VOID);
    }

    private static boolean canFill(BlockView world, BlockPos pos, BlockState state, Fluid fluid) {
        return FlowableFluid.canFill(state) && FlowableFluid.canFillWithFluid(world, pos, state, fluid);
    }

    private static boolean canFillWithFluid(BlockView world, BlockPos pos, BlockState state, Fluid fluid) {
        Block block = state.getBlock();
        if (block instanceof FluidFillable) {
            FluidFillable fluidFillable = (FluidFillable)((Object)block);
            return fluidFillable.canFillWithFluid(null, world, pos, state, fluid);
        }
        return true;
    }

    protected abstract int getLevelDecreasePerBlock(WorldView var1);

    protected int getNextTickDelay(World world, BlockPos pos, FluidState oldState, FluidState newState) {
        return this.getTickRate(world);
    }

    @Override
    public void onScheduledTick(ServerWorld world, BlockPos pos, BlockState blockState, FluidState fluidState) {
        if (!fluidState.isStill()) {
            FluidState fluidState2 = this.getUpdatedState(world, pos, world.getBlockState(pos));
            int i = this.getNextTickDelay(world, pos, fluidState, fluidState2);
            if (fluidState2.isEmpty()) {
                fluidState = fluidState2;
                blockState = Blocks.AIR.getDefaultState();
                world.setBlockState(pos, blockState, 3);
            } else if (fluidState2 != fluidState) {
                fluidState = fluidState2;
                blockState = fluidState.getBlockState();
                world.setBlockState(pos, blockState, 3);
                world.scheduleFluidTick(pos, fluidState.getFluid(), i);
            }
        }
        this.tryFlow(world, pos, blockState, fluidState);
    }

    protected static int getBlockStateLevel(FluidState state) {
        if (state.isStill()) {
            return 0;
        }
        return 8 - Math.min(state.getLevel(), 8) + (state.get(FALLING) != false ? 8 : 0);
    }

    private static boolean isFluidAboveEqual(FluidState state, BlockView world, BlockPos pos) {
        return state.getFluid().matchesType(world.getFluidState(pos.up()).getFluid());
    }

    @Override
    public float getHeight(FluidState state, BlockView world, BlockPos pos) {
        if (FlowableFluid.isFluidAboveEqual(state, world, pos)) {
            return 1.0f;
        }
        return state.getHeight();
    }

    @Override
    public float getHeight(FluidState state) {
        return (float)state.getLevel() / 9.0f;
    }

    @Override
    public abstract int getLevel(FluidState var1);

    @Override
    public VoxelShape getShape(FluidState state, BlockView world, BlockPos pos) {
        if (state.getLevel() == 9 && FlowableFluid.isFluidAboveEqual(state, world, pos)) {
            return VoxelShapes.fullCube();
        }
        return this.shapeCache.computeIfAbsent(state, state2 -> VoxelShapes.cuboid(0.0, 0.0, 0.0, 1.0, state2.getHeight(world, pos), 1.0));
    }

    record NeighborGroup(BlockState self, BlockState other, Direction facing) {
        /*
         * Enabled force condition propagation
         * Lifted jumps to return sites
         */
        @Override
        public boolean equals(Object o) {
            if (!(o instanceof NeighborGroup)) return false;
            NeighborGroup neighborGroup = (NeighborGroup)o;
            if (this.self != neighborGroup.self) return false;
            if (this.other != neighborGroup.other) return false;
            if (this.facing != neighborGroup.facing) return false;
            return true;
        }

        @Override
        public int hashCode() {
            int i = System.identityHashCode(this.self);
            i = 31 * i + System.identityHashCode(this.other);
            i = 31 * i + this.facing.hashCode();
            return i;
        }

        @Override
        public final String toString() {
            return ObjectMethods.bootstrap("toString", new MethodHandle[]{NeighborGroup.class, "first;second;direction", "self", "other", "facing"}, this);
        }
    }

    protected class SpreadCache {
        private final BlockView world;
        private final BlockPos startPos;
        private final Short2ObjectMap<BlockState> stateCache = new Short2ObjectOpenHashMap();
        private final Short2BooleanMap flowDownCache = new Short2BooleanOpenHashMap();

        SpreadCache(BlockView world, BlockPos startPos) {
            this.world = world;
            this.startPos = startPos;
        }

        public BlockState getBlockState(BlockPos pos) {
            return this.getBlockState(pos, this.pack(pos));
        }

        private BlockState getBlockState(BlockPos pos, short packed) {
            return (BlockState)this.stateCache.computeIfAbsent(packed, packedPos -> this.world.getBlockState(pos));
        }

        public boolean canFlowDownTo(BlockPos pos) {
            return this.flowDownCache.computeIfAbsent(this.pack(pos), packed -> {
                BlockState blockState = this.getBlockState(pos, packed);
                BlockPos blockPos2 = pos.down();
                BlockState blockState2 = this.world.getBlockState(blockPos2);
                return FlowableFluid.this.canFlowDownTo(this.world, pos, blockState, blockPos2, blockState2);
            });
        }

        private short pack(BlockPos pos) {
            int i = pos.getX() - this.startPos.getX();
            int j = pos.getZ() - this.startPos.getZ();
            return (short)((i + 128 & 0xFF) << 8 | j + 128 & 0xFF);
        }
    }
}


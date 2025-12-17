/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  com.google.common.annotations.VisibleForTesting
 *  org.jspecify.annotations.Nullable
 */
package net.minecraft.block;

import com.google.common.annotations.VisibleForTesting;
import java.util.Optional;
import net.minecraft.block.BlockState;
import net.minecraft.block.Blocks;
import net.minecraft.block.MultifaceBlock;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Direction;
import net.minecraft.util.math.random.Random;
import net.minecraft.world.BlockView;
import net.minecraft.world.WorldAccess;
import org.jspecify.annotations.Nullable;

public class MultifaceGrower {
    public static final GrowType[] GROW_TYPES = new GrowType[]{GrowType.SAME_POSITION, GrowType.SAME_PLANE, GrowType.WRAP_AROUND};
    private final GrowChecker growChecker;

    public MultifaceGrower(MultifaceBlock lichen) {
        this(new LichenGrowChecker(lichen));
    }

    public MultifaceGrower(GrowChecker growChecker) {
        this.growChecker = growChecker;
    }

    public boolean canGrow(BlockState state, BlockView world, BlockPos pos, Direction direction) {
        return Direction.stream().anyMatch(direction2 -> this.getGrowPos(state, world, pos, direction, (Direction)direction2, this.growChecker::canGrow).isPresent());
    }

    public Optional<GrowPos> grow(BlockState state, WorldAccess world, BlockPos pos, Random random) {
        return Direction.shuffle(random).stream().filter(direction -> this.growChecker.canGrow(state, (Direction)direction)).map(direction -> this.grow(state, world, pos, (Direction)direction, random, false)).filter(Optional::isPresent).findFirst().orElse(Optional.empty());
    }

    public long grow(BlockState state, WorldAccess world, BlockPos pos, boolean markForPostProcessing) {
        return Direction.stream().filter(direction -> this.growChecker.canGrow(state, (Direction)direction)).map(direction -> this.grow(state, world, pos, (Direction)direction, markForPostProcessing)).reduce(0L, Long::sum);
    }

    public Optional<GrowPos> grow(BlockState state, WorldAccess world, BlockPos pos, Direction direction, Random random, boolean markForPostProcessing) {
        return Direction.shuffle(random).stream().map(direction2 -> this.grow(state, world, pos, direction, (Direction)direction2, markForPostProcessing)).filter(Optional::isPresent).findFirst().orElse(Optional.empty());
    }

    private long grow(BlockState state, WorldAccess world, BlockPos pos, Direction direction, boolean markForPostProcessing) {
        return Direction.stream().map(direction2 -> this.grow(state, world, pos, direction, (Direction)direction2, markForPostProcessing)).filter(Optional::isPresent).count();
    }

    @VisibleForTesting
    public Optional<GrowPos> grow(BlockState state, WorldAccess world, BlockPos pos, Direction oldDirection, Direction newDirection, boolean markForPostProcessing) {
        return this.getGrowPos(state, world, pos, oldDirection, newDirection, this.growChecker::canGrow).flatMap(growPos -> this.place(world, (GrowPos)growPos, markForPostProcessing));
    }

    public Optional<GrowPos> getGrowPos(BlockState state, BlockView world, BlockPos pos, Direction oldDirection, Direction newDirection, GrowPosPredicate predicate) {
        if (newDirection.getAxis() == oldDirection.getAxis()) {
            return Optional.empty();
        }
        if (!(this.growChecker.canGrow(state) || this.growChecker.hasDirection(state, oldDirection) && !this.growChecker.hasDirection(state, newDirection))) {
            return Optional.empty();
        }
        for (GrowType growType : this.growChecker.getGrowTypes()) {
            GrowPos growPos = growType.getGrowPos(pos, newDirection, oldDirection);
            if (!predicate.test(world, pos, growPos)) continue;
            return Optional.of(growPos);
        }
        return Optional.empty();
    }

    public Optional<GrowPos> place(WorldAccess world, GrowPos pos, boolean markForPostProcessing) {
        BlockState blockState = world.getBlockState(pos.pos());
        if (this.growChecker.place(world, pos, blockState, markForPostProcessing)) {
            return Optional.of(pos);
        }
        return Optional.empty();
    }

    public static class LichenGrowChecker
    implements GrowChecker {
        protected MultifaceBlock lichen;

        public LichenGrowChecker(MultifaceBlock lichen) {
            this.lichen = lichen;
        }

        @Override
        public @Nullable BlockState getStateWithDirection(BlockState state, BlockView world, BlockPos pos, Direction direction) {
            return this.lichen.withDirection(state, world, pos, direction);
        }

        protected boolean canGrow(BlockView world, BlockPos pos, BlockPos growPos, Direction direction, BlockState state) {
            return state.isAir() || state.isOf(this.lichen) || state.isOf(Blocks.WATER) && state.getFluidState().isStill();
        }

        @Override
        public boolean canGrow(BlockView world, BlockPos pos, GrowPos growPos) {
            BlockState blockState = world.getBlockState(growPos.pos());
            return this.canGrow(world, pos, growPos.pos(), growPos.face(), blockState) && this.lichen.canGrowWithDirection(world, blockState, growPos.pos(), growPos.face());
        }
    }

    public static interface GrowChecker {
        public @Nullable BlockState getStateWithDirection(BlockState var1, BlockView var2, BlockPos var3, Direction var4);

        public boolean canGrow(BlockView var1, BlockPos var2, GrowPos var3);

        default public GrowType[] getGrowTypes() {
            return GROW_TYPES;
        }

        default public boolean hasDirection(BlockState state, Direction direction) {
            return MultifaceBlock.hasDirection(state, direction);
        }

        default public boolean canGrow(BlockState state) {
            return false;
        }

        default public boolean canGrow(BlockState state, Direction direction) {
            return this.canGrow(state) || this.hasDirection(state, direction);
        }

        default public boolean place(WorldAccess world, GrowPos growPos, BlockState state, boolean markForPostProcessing) {
            BlockState blockState = this.getStateWithDirection(state, world, growPos.pos(), growPos.face());
            if (blockState != null) {
                if (markForPostProcessing) {
                    world.getChunk(growPos.pos()).markBlockForPostProcessing(growPos.pos());
                }
                return world.setBlockState(growPos.pos(), blockState, 2);
            }
            return false;
        }
    }

    @FunctionalInterface
    public static interface GrowPosPredicate {
        public boolean test(BlockView var1, BlockPos var2, GrowPos var3);
    }

    public static abstract sealed class GrowType
    extends Enum<GrowType> {
        public static final /* enum */ GrowType SAME_POSITION = new GrowType(){

            @Override
            public GrowPos getGrowPos(BlockPos pos, Direction newDirection, Direction oldDirection) {
                return new GrowPos(pos, newDirection);
            }
        };
        public static final /* enum */ GrowType SAME_PLANE = new GrowType(){

            @Override
            public GrowPos getGrowPos(BlockPos pos, Direction newDirection, Direction oldDirection) {
                return new GrowPos(pos.offset(newDirection), oldDirection);
            }
        };
        public static final /* enum */ GrowType WRAP_AROUND = new GrowType(){

            @Override
            public GrowPos getGrowPos(BlockPos pos, Direction newDirection, Direction oldDirection) {
                return new GrowPos(pos.offset(newDirection).offset(oldDirection), newDirection.getOpposite());
            }
        };
        private static final /* synthetic */ GrowType[] field_37601;

        public static GrowType[] values() {
            return (GrowType[])field_37601.clone();
        }

        public static GrowType valueOf(String string) {
            return Enum.valueOf(GrowType.class, string);
        }

        public abstract GrowPos getGrowPos(BlockPos var1, Direction var2, Direction var3);

        private static /* synthetic */ GrowType[] method_41465() {
            return new GrowType[]{SAME_POSITION, SAME_PLANE, WRAP_AROUND};
        }

        static {
            field_37601 = GrowType.method_41465();
        }
    }

    public record GrowPos(BlockPos pos, Direction face) {
    }
}


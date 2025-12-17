/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  com.google.common.annotations.VisibleForTesting
 *  com.mojang.datafixers.util.Pair
 *  it.unimi.dsi.fastutil.ints.IntArrayList
 */
package net.minecraft.world;

import com.google.common.annotations.VisibleForTesting;
import com.mojang.datafixers.util.Pair;
import it.unimi.dsi.fastutil.ints.IntArrayList;
import java.util.Optional;
import java.util.function.Predicate;
import net.minecraft.block.Block;
import net.minecraft.block.BlockState;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Direction;
import net.minecraft.world.BlockView;

public class BlockLocating {
    public static Rectangle getLargestRectangle(BlockPos center, Direction.Axis primaryAxis, int primaryMaxBlocks, Direction.Axis secondaryAxis, int secondaryMaxBlocks, Predicate<BlockPos> predicate) {
        IntBounds intBounds;
        int m;
        BlockPos.Mutable mutable = center.mutableCopy();
        Direction direction = Direction.get(Direction.AxisDirection.NEGATIVE, primaryAxis);
        Direction direction2 = direction.getOpposite();
        Direction direction3 = Direction.get(Direction.AxisDirection.NEGATIVE, secondaryAxis);
        Direction direction4 = direction3.getOpposite();
        int i = BlockLocating.moveWhile(predicate, mutable.set(center), direction, primaryMaxBlocks);
        int j = BlockLocating.moveWhile(predicate, mutable.set(center), direction2, primaryMaxBlocks);
        int k = i;
        IntBounds[] intBoundss = new IntBounds[k + 1 + j];
        intBoundss[k] = new IntBounds(BlockLocating.moveWhile(predicate, mutable.set(center), direction3, secondaryMaxBlocks), BlockLocating.moveWhile(predicate, mutable.set(center), direction4, secondaryMaxBlocks));
        int l = intBoundss[k].min;
        for (m = 1; m <= i; ++m) {
            intBounds = intBoundss[k - (m - 1)];
            intBoundss[k - m] = new IntBounds(BlockLocating.moveWhile(predicate, mutable.set(center).move(direction, m), direction3, intBounds.min), BlockLocating.moveWhile(predicate, mutable.set(center).move(direction, m), direction4, intBounds.max));
        }
        for (m = 1; m <= j; ++m) {
            intBounds = intBoundss[k + m - 1];
            intBoundss[k + m] = new IntBounds(BlockLocating.moveWhile(predicate, mutable.set(center).move(direction2, m), direction3, intBounds.min), BlockLocating.moveWhile(predicate, mutable.set(center).move(direction2, m), direction4, intBounds.max));
        }
        m = 0;
        int n = 0;
        int o = 0;
        int p = 0;
        int[] is = new int[intBoundss.length];
        for (int q = l; q >= 0; --q) {
            int t;
            int s;
            IntBounds intBounds2;
            for (int r = 0; r < intBoundss.length; ++r) {
                intBounds2 = intBoundss[r];
                s = l - intBounds2.min;
                t = l + intBounds2.max;
                is[r] = q >= s && q <= t ? t + 1 - q : 0;
            }
            Pair<IntBounds, Integer> pair = BlockLocating.findLargestRectangle(is);
            intBounds2 = (IntBounds)pair.getFirst();
            s = 1 + intBounds2.max - intBounds2.min;
            t = (Integer)pair.getSecond();
            if (s * t <= o * p) continue;
            m = intBounds2.min;
            n = q;
            o = s;
            p = t;
        }
        return new Rectangle(center.offset(primaryAxis, m - k).offset(secondaryAxis, n - l), o, p);
    }

    private static int moveWhile(Predicate<BlockPos> predicate, BlockPos.Mutable pos, Direction direction, int max) {
        int i;
        for (i = 0; i < max && predicate.test(pos.move(direction)); ++i) {
        }
        return i;
    }

    @VisibleForTesting
    static Pair<IntBounds, Integer> findLargestRectangle(int[] heights) {
        int i = 0;
        int j = 0;
        int k = 0;
        IntArrayList intStack = new IntArrayList();
        intStack.push(0);
        for (int l = 1; l <= heights.length; ++l) {
            int m;
            int n = m = l == heights.length ? 0 : heights[l];
            while (!intStack.isEmpty()) {
                int n2 = heights[intStack.topInt()];
                if (m >= n2) {
                    intStack.push(l);
                    break;
                }
                intStack.popInt();
                int o = intStack.isEmpty() ? 0 : intStack.topInt() + 1;
                if (n2 * (l - o) <= k * (j - i)) continue;
                j = l;
                i = o;
                k = n2;
            }
            if (!intStack.isEmpty()) continue;
            intStack.push(l);
        }
        return new Pair((Object)new IntBounds(i, j - 1), (Object)k);
    }

    public static Optional<BlockPos> findColumnEnd(BlockView world, BlockPos pos, Block intermediateBlock, Direction direction, Block endBlock) {
        BlockState blockState;
        BlockPos.Mutable mutable = pos.mutableCopy();
        do {
            mutable.move(direction);
        } while ((blockState = world.getBlockState(mutable)).isOf(intermediateBlock));
        if (blockState.isOf(endBlock)) {
            return Optional.of(mutable);
        }
        return Optional.empty();
    }

    public static class IntBounds {
        public final int min;
        public final int max;

        public IntBounds(int min, int max) {
            this.min = min;
            this.max = max;
        }

        public String toString() {
            return "IntBounds{min=" + this.min + ", max=" + this.max + "}";
        }
    }

    public static class Rectangle {
        public final BlockPos lowerLeft;
        public final int width;
        public final int height;

        public Rectangle(BlockPos lowerLeft, int width, int height) {
            this.lowerLeft = lowerLeft;
            this.width = width;
            this.height = height;
        }
    }
}


/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  com.google.common.annotations.VisibleForTesting
 *  org.jspecify.annotations.Nullable
 */
package net.minecraft.entity.ai;

import com.google.common.annotations.VisibleForTesting;
import java.util.function.Predicate;
import java.util.function.Supplier;
import java.util.function.ToDoubleFunction;
import net.minecraft.entity.mob.PathAwareEntity;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Direction;
import net.minecraft.util.math.MathHelper;
import net.minecraft.util.math.Vec3d;
import net.minecraft.util.math.random.Random;
import org.jspecify.annotations.Nullable;

public class FuzzyPositions {
    private static final int GAUSS_RANGE = 10;

    public static BlockPos localFuzz(Random random, int horizontalRange, int verticalRange) {
        int i = random.nextInt(2 * horizontalRange + 1) - horizontalRange;
        int j = random.nextInt(2 * verticalRange + 1) - verticalRange;
        int k = random.nextInt(2 * horizontalRange + 1) - horizontalRange;
        return new BlockPos(i, j, k);
    }

    public static @Nullable BlockPos localFuzz(Random random, double minHorizontalRange, double maxHorizontalRange, int verticalRange, int startHeight, double directionX, double directionZ, double angleRange) {
        double d = MathHelper.atan2(directionZ, directionX) - 1.5707963705062866;
        double e = d + (double)(2.0f * random.nextFloat() - 1.0f) * angleRange;
        double f = MathHelper.lerp(Math.sqrt(random.nextDouble()), minHorizontalRange, maxHorizontalRange) * (double)MathHelper.SQUARE_ROOT_OF_TWO;
        double g = -f * Math.sin(e);
        double h = f * Math.cos(e);
        if (Math.abs(g) > maxHorizontalRange || Math.abs(h) > maxHorizontalRange) {
            return null;
        }
        int i = random.nextInt(2 * verticalRange + 1) - verticalRange + startHeight;
        return BlockPos.ofFloored(g, i, h);
    }

    @VisibleForTesting
    public static BlockPos upWhile(BlockPos pos, int maxY, Predicate<BlockPos> condition) {
        if (condition.test(pos)) {
            BlockPos.Mutable mutable = pos.mutableCopy().move(Direction.UP);
            while (mutable.getY() <= maxY && condition.test(mutable)) {
                mutable.move(Direction.UP);
            }
            return mutable.toImmutable();
        }
        return pos;
    }

    @VisibleForTesting
    public static BlockPos upWhile(BlockPos pos, int extraAbove, int max, Predicate<BlockPos> condition) {
        if (extraAbove < 0) {
            throw new IllegalArgumentException("aboveSolidAmount was " + extraAbove + ", expected >= 0");
        }
        if (condition.test(pos)) {
            BlockPos.Mutable mutable = pos.mutableCopy().move(Direction.UP);
            while (mutable.getY() <= max && condition.test(mutable)) {
                mutable.move(Direction.UP);
            }
            int i = mutable.getY();
            while (mutable.getY() <= max && mutable.getY() - i < extraAbove) {
                mutable.move(Direction.UP);
                if (!condition.test(mutable)) continue;
                mutable.move(Direction.DOWN);
                break;
            }
            return mutable.toImmutable();
        }
        return pos;
    }

    public static @Nullable Vec3d guessBestPathTarget(PathAwareEntity entity, Supplier<@Nullable BlockPos> factory) {
        return FuzzyPositions.guessBest(factory, entity::getPathfindingFavor);
    }

    public static @Nullable Vec3d guessBest(Supplier<@Nullable BlockPos> factory, ToDoubleFunction<BlockPos> scorer) {
        double d = Double.NEGATIVE_INFINITY;
        BlockPos blockPos = null;
        for (int i = 0; i < 10; ++i) {
            double e;
            BlockPos blockPos2 = factory.get();
            if (blockPos2 == null || !((e = scorer.applyAsDouble(blockPos2)) > d)) continue;
            d = e;
            blockPos = blockPos2;
        }
        return blockPos != null ? Vec3d.ofBottomCenter(blockPos) : null;
    }

    public static BlockPos towardTarget(PathAwareEntity entity, double horizontalRange, Random random, BlockPos fuzz) {
        double d = fuzz.getX();
        double e = fuzz.getZ();
        if (entity.hasPositionTarget() && horizontalRange > 1.0) {
            BlockPos blockPos = entity.getPositionTarget();
            d = entity.getX() > (double)blockPos.getX() ? (d -= random.nextDouble() * horizontalRange / 2.0) : (d += random.nextDouble() * horizontalRange / 2.0);
            e = entity.getZ() > (double)blockPos.getZ() ? (e -= random.nextDouble() * horizontalRange / 2.0) : (e += random.nextDouble() * horizontalRange / 2.0);
        }
        return BlockPos.ofFloored(d + entity.getX(), (double)fuzz.getY() + entity.getY(), e + entity.getZ());
    }
}


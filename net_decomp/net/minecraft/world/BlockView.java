/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  it.unimi.dsi.fastutil.longs.LongOpenHashSet
 *  it.unimi.dsi.fastutil.longs.LongSet
 *  org.jspecify.annotations.Nullable
 */
package net.minecraft.world;

import it.unimi.dsi.fastutil.longs.LongOpenHashSet;
import it.unimi.dsi.fastutil.longs.LongSet;
import java.util.Optional;
import java.util.function.BiFunction;
import java.util.function.Function;
import java.util.function.Supplier;
import java.util.stream.Stream;
import net.minecraft.block.BlockState;
import net.minecraft.block.entity.BlockEntity;
import net.minecraft.block.entity.BlockEntityType;
import net.minecraft.fluid.FluidState;
import net.minecraft.util.hit.BlockHitResult;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Box;
import net.minecraft.util.math.Direction;
import net.minecraft.util.math.MathHelper;
import net.minecraft.util.math.Vec3d;
import net.minecraft.util.math.Vec3i;
import net.minecraft.util.shape.VoxelShape;
import net.minecraft.world.BlockStateRaycastContext;
import net.minecraft.world.HeightLimitView;
import net.minecraft.world.RaycastContext;
import org.jspecify.annotations.Nullable;

public interface BlockView
extends HeightLimitView {
    public @Nullable BlockEntity getBlockEntity(BlockPos var1);

    default public <T extends BlockEntity> Optional<T> getBlockEntity(BlockPos pos, BlockEntityType<T> type) {
        BlockEntity blockEntity = this.getBlockEntity(pos);
        if (blockEntity == null || blockEntity.getType() != type) {
            return Optional.empty();
        }
        return Optional.of(blockEntity);
    }

    public BlockState getBlockState(BlockPos var1);

    public FluidState getFluidState(BlockPos var1);

    default public int getLuminance(BlockPos pos) {
        return this.getBlockState(pos).getLuminance();
    }

    default public Stream<BlockState> getStatesInBox(Box box) {
        return BlockPos.stream(box).map(this::getBlockState);
    }

    default public BlockHitResult raycast(BlockStateRaycastContext context) {
        return BlockView.raycast(context.getStart(), context.getEnd(), context, (innerContext, pos) -> {
            BlockState blockState = this.getBlockState((BlockPos)pos);
            Vec3d vec3d = innerContext.getStart().subtract(innerContext.getEnd());
            return innerContext.getStatePredicate().test(blockState) ? new BlockHitResult(innerContext.getEnd(), Direction.getFacing(vec3d.x, vec3d.y, vec3d.z), BlockPos.ofFloored(innerContext.getEnd()), false) : null;
        }, innerContext -> {
            Vec3d vec3d = innerContext.getStart().subtract(innerContext.getEnd());
            return BlockHitResult.createMissed(innerContext.getEnd(), Direction.getFacing(vec3d.x, vec3d.y, vec3d.z), BlockPos.ofFloored(innerContext.getEnd()));
        });
    }

    default public BlockHitResult raycast(RaycastContext context) {
        return BlockView.raycast(context.getStart(), context.getEnd(), context, (innerContext, pos) -> {
            BlockState blockState = this.getBlockState((BlockPos)pos);
            FluidState fluidState = this.getFluidState((BlockPos)pos);
            Vec3d vec3d = innerContext.getStart();
            Vec3d vec3d2 = innerContext.getEnd();
            VoxelShape voxelShape = innerContext.getBlockShape(blockState, this, (BlockPos)pos);
            BlockHitResult blockHitResult = this.raycastBlock(vec3d, vec3d2, (BlockPos)pos, voxelShape, blockState);
            VoxelShape voxelShape2 = innerContext.getFluidShape(fluidState, this, (BlockPos)pos);
            BlockHitResult blockHitResult2 = voxelShape2.raycast(vec3d, vec3d2, (BlockPos)pos);
            double d = blockHitResult == null ? Double.MAX_VALUE : innerContext.getStart().squaredDistanceTo(blockHitResult.getPos());
            double e = blockHitResult2 == null ? Double.MAX_VALUE : innerContext.getStart().squaredDistanceTo(blockHitResult2.getPos());
            return d <= e ? blockHitResult : blockHitResult2;
        }, innerContext -> {
            Vec3d vec3d = innerContext.getStart().subtract(innerContext.getEnd());
            return BlockHitResult.createMissed(innerContext.getEnd(), Direction.getFacing(vec3d.x, vec3d.y, vec3d.z), BlockPos.ofFloored(innerContext.getEnd()));
        });
    }

    default public @Nullable BlockHitResult raycastBlock(Vec3d start, Vec3d end, BlockPos pos, VoxelShape shape, BlockState state) {
        BlockHitResult blockHitResult2;
        BlockHitResult blockHitResult = shape.raycast(start, end, pos);
        if (blockHitResult != null && (blockHitResult2 = state.getRaycastShape(this, pos).raycast(start, end, pos)) != null && blockHitResult2.getPos().subtract(start).lengthSquared() < blockHitResult.getPos().subtract(start).lengthSquared()) {
            return blockHitResult.withSide(blockHitResult2.getSide());
        }
        return blockHitResult;
    }

    default public double getDismountHeight(VoxelShape blockCollisionShape, Supplier<VoxelShape> belowBlockCollisionShapeGetter) {
        if (!blockCollisionShape.isEmpty()) {
            return blockCollisionShape.getMax(Direction.Axis.Y);
        }
        double d = belowBlockCollisionShapeGetter.get().getMax(Direction.Axis.Y);
        if (d >= 1.0) {
            return d - 1.0;
        }
        return Double.NEGATIVE_INFINITY;
    }

    default public double getDismountHeight(BlockPos pos) {
        return this.getDismountHeight(this.getBlockState(pos).getCollisionShape(this, pos), () -> {
            BlockPos blockPos2 = pos.down();
            return this.getBlockState(blockPos2).getCollisionShape(this, blockPos2);
        });
    }

    public static <T, C> T raycast(Vec3d start, Vec3d end, C context, BiFunction<C, BlockPos, @Nullable T> blockHitFactory, Function<C, T> missFactory) {
        int l;
        int k;
        if (start.equals(end)) {
            return missFactory.apply(context);
        }
        double d = MathHelper.lerp(-1.0E-7, end.x, start.x);
        double e = MathHelper.lerp(-1.0E-7, end.y, start.y);
        double f = MathHelper.lerp(-1.0E-7, end.z, start.z);
        double g = MathHelper.lerp(-1.0E-7, start.x, end.x);
        double h = MathHelper.lerp(-1.0E-7, start.y, end.y);
        double i = MathHelper.lerp(-1.0E-7, start.z, end.z);
        int j = MathHelper.floor(g);
        BlockPos.Mutable mutable = new BlockPos.Mutable(j, k = MathHelper.floor(h), l = MathHelper.floor(i));
        T object = blockHitFactory.apply(context, mutable);
        if (object != null) {
            return object;
        }
        double m = d - g;
        double n = e - h;
        double o = f - i;
        int p = MathHelper.sign(m);
        int q = MathHelper.sign(n);
        int r = MathHelper.sign(o);
        double s = p == 0 ? Double.MAX_VALUE : (double)p / m;
        double t = q == 0 ? Double.MAX_VALUE : (double)q / n;
        double u = r == 0 ? Double.MAX_VALUE : (double)r / o;
        double v = s * (p > 0 ? 1.0 - MathHelper.fractionalPart(g) : MathHelper.fractionalPart(g));
        double w = t * (q > 0 ? 1.0 - MathHelper.fractionalPart(h) : MathHelper.fractionalPart(h));
        double x = u * (r > 0 ? 1.0 - MathHelper.fractionalPart(i) : MathHelper.fractionalPart(i));
        while (v <= 1.0 || w <= 1.0 || x <= 1.0) {
            T object2;
            if (v < w) {
                if (v < x) {
                    j += p;
                    v += s;
                } else {
                    l += r;
                    x += u;
                }
            } else if (w < x) {
                k += q;
                w += t;
            } else {
                l += r;
                x += u;
            }
            if ((object2 = blockHitFactory.apply(context, mutable.set(j, k, l))) == null) continue;
            return object2;
        }
        return missFactory.apply(context);
    }

    public static boolean collectCollisionsBetween(Vec3d from, Vec3d to, Box box, CollisionVisitor visitor) {
        Vec3d vec3d = to.subtract(from);
        if (vec3d.lengthSquared() < (double)MathHelper.square(1.0E-5f)) {
            for (BlockPos blockPos : BlockPos.iterate(box)) {
                if (visitor.visit(blockPos, 0)) continue;
                return false;
            }
            return true;
        }
        LongOpenHashSet longSet = new LongOpenHashSet();
        for (BlockPos blockPos2 : BlockPos.iterateCollisionOrder(box.offset(vec3d.multiply(-1.0)), vec3d)) {
            if (!visitor.visit(blockPos2, 0)) {
                return false;
            }
            longSet.add(blockPos2.asLong());
        }
        int i = BlockView.collectCollisionsBetween((LongSet)longSet, vec3d, box, visitor);
        if (i < 0) {
            return false;
        }
        for (BlockPos blockPos3 : BlockPos.iterateCollisionOrder(box, vec3d)) {
            if (!longSet.add(blockPos3.asLong()) || visitor.visit(blockPos3, i + 1)) continue;
            return false;
        }
        return true;
    }

    private static int collectCollisionsBetween(LongSet visited, Vec3d delta, Box box, CollisionVisitor visitor) {
        double d = box.getLengthX();
        double e = box.getLengthY();
        double f = box.getLengthZ();
        Vec3i vec3i = BlockView.method_73110(delta);
        Vec3d vec3d = box.getCenter();
        Vec3d vec3d2 = new Vec3d(vec3d.getX() + d * 0.5 * (double)vec3i.getX(), vec3d.getY() + e * 0.5 * (double)vec3i.getY(), vec3d.getZ() + f * 0.5 * (double)vec3i.getZ());
        Vec3d vec3d3 = vec3d2.subtract(delta);
        int i = MathHelper.floor(vec3d3.x);
        int j = MathHelper.floor(vec3d3.y);
        int k = MathHelper.floor(vec3d3.z);
        int l = MathHelper.sign(delta.x);
        int m = MathHelper.sign(delta.y);
        int n = MathHelper.sign(delta.z);
        double g = l == 0 ? Double.MAX_VALUE : (double)l / delta.x;
        double h = m == 0 ? Double.MAX_VALUE : (double)m / delta.y;
        double o = n == 0 ? Double.MAX_VALUE : (double)n / delta.z;
        double p = g * (l > 0 ? 1.0 - MathHelper.fractionalPart(vec3d3.x) : MathHelper.fractionalPart(vec3d3.x));
        double q = h * (m > 0 ? 1.0 - MathHelper.fractionalPart(vec3d3.y) : MathHelper.fractionalPart(vec3d3.y));
        double r = o * (n > 0 ? 1.0 - MathHelper.fractionalPart(vec3d3.z) : MathHelper.fractionalPart(vec3d3.z));
        int s = 0;
        while (p <= 1.0 || q <= 1.0 || r <= 1.0) {
            if (p < q) {
                if (p < r) {
                    i += l;
                    p += g;
                } else {
                    k += n;
                    r += o;
                }
            } else if (q < r) {
                j += m;
                q += h;
            } else {
                k += n;
                r += o;
            }
            Optional<Vec3d> optional = Box.raycast(i, j, k, i + 1, j + 1, k + 1, vec3d3, vec3d2);
            if (optional.isEmpty()) continue;
            Vec3d vec3d4 = optional.get();
            double t = MathHelper.clamp(vec3d4.x, (double)i + (double)1.0E-5f, (double)i + 1.0 - (double)1.0E-5f);
            double u = MathHelper.clamp(vec3d4.y, (double)j + (double)1.0E-5f, (double)j + 1.0 - (double)1.0E-5f);
            double v = MathHelper.clamp(vec3d4.z, (double)k + (double)1.0E-5f, (double)k + 1.0 - (double)1.0E-5f);
            int w = MathHelper.floor(t - d * (double)vec3i.getX());
            int x = MathHelper.floor(u - e * (double)vec3i.getY());
            int y = MathHelper.floor(v - f * (double)vec3i.getZ());
            int z = ++s;
            for (BlockPos blockPos : BlockPos.iterateCollisionOrder(i, j, k, w, x, y, delta)) {
                if (!visited.add(blockPos.asLong()) || visitor.visit(blockPos, z)) continue;
                return -1;
            }
        }
        return s;
    }

    private static Vec3i method_73110(Vec3d vec3d) {
        int k;
        double d = Math.abs(Vec3d.X.dotProduct(vec3d));
        double e = Math.abs(Vec3d.Y.dotProduct(vec3d));
        double f = Math.abs(Vec3d.Z.dotProduct(vec3d));
        int i = vec3d.x >= 0.0 ? 1 : -1;
        int j = vec3d.y >= 0.0 ? 1 : -1;
        int n = k = vec3d.z >= 0.0 ? 1 : -1;
        if (d <= e && d <= f) {
            return new Vec3i(-i, -k, j);
        }
        if (e <= f) {
            return new Vec3i(k, -j, -i);
        }
        return new Vec3i(-j, i, -k);
    }

    @FunctionalInterface
    public static interface CollisionVisitor {
        public boolean visit(BlockPos var1, int var2);
    }
}


/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  com.google.common.collect.Lists
 *  com.google.common.math.DoubleMath
 *  it.unimi.dsi.fastutil.doubles.DoubleList
 *  org.apache.commons.lang3.mutable.MutableObject
 *  org.jspecify.annotations.Nullable
 */
package net.minecraft.util.shape;

import com.google.common.collect.Lists;
import com.google.common.math.DoubleMath;
import it.unimi.dsi.fastutil.doubles.DoubleList;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import java.util.Optional;
import net.minecraft.util.Util;
import net.minecraft.util.function.BooleanBiFunction;
import net.minecraft.util.hit.BlockHitResult;
import net.minecraft.util.math.AxisCycleDirection;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Box;
import net.minecraft.util.math.Direction;
import net.minecraft.util.math.MathHelper;
import net.minecraft.util.math.Vec3d;
import net.minecraft.util.math.Vec3i;
import net.minecraft.util.shape.ArrayVoxelShape;
import net.minecraft.util.shape.OffsetDoubleList;
import net.minecraft.util.shape.SlicedVoxelShape;
import net.minecraft.util.shape.VoxelSet;
import net.minecraft.util.shape.VoxelShapes;
import org.apache.commons.lang3.mutable.MutableObject;
import org.jspecify.annotations.Nullable;

public abstract class VoxelShape {
    protected final VoxelSet voxels;
    private @Nullable VoxelShape @Nullable [] shapeCache;

    protected VoxelShape(VoxelSet voxels) {
        this.voxels = voxels;
    }

    public double getMin(Direction.Axis axis) {
        int i = this.voxels.getMin(axis);
        if (i >= this.voxels.getSize(axis)) {
            return Double.POSITIVE_INFINITY;
        }
        return this.getPointPosition(axis, i);
    }

    public double getMax(Direction.Axis axis) {
        int i = this.voxels.getMax(axis);
        if (i <= 0) {
            return Double.NEGATIVE_INFINITY;
        }
        return this.getPointPosition(axis, i);
    }

    public Box getBoundingBox() {
        if (this.isEmpty()) {
            throw Util.getFatalOrPause(new UnsupportedOperationException("No bounds for empty shape."));
        }
        return new Box(this.getMin(Direction.Axis.X), this.getMin(Direction.Axis.Y), this.getMin(Direction.Axis.Z), this.getMax(Direction.Axis.X), this.getMax(Direction.Axis.Y), this.getMax(Direction.Axis.Z));
    }

    public VoxelShape asCuboid() {
        if (this.isEmpty()) {
            return VoxelShapes.empty();
        }
        return VoxelShapes.cuboid(this.getMin(Direction.Axis.X), this.getMin(Direction.Axis.Y), this.getMin(Direction.Axis.Z), this.getMax(Direction.Axis.X), this.getMax(Direction.Axis.Y), this.getMax(Direction.Axis.Z));
    }

    protected double getPointPosition(Direction.Axis axis, int index) {
        return this.getPointPositions(axis).getDouble(index);
    }

    public abstract DoubleList getPointPositions(Direction.Axis var1);

    public boolean isEmpty() {
        return this.voxels.isEmpty();
    }

    public VoxelShape offset(Vec3d vec3d) {
        return this.offset(vec3d.x, vec3d.y, vec3d.z);
    }

    public VoxelShape offset(Vec3i vec) {
        return this.offset(vec.getX(), vec.getY(), vec.getZ());
    }

    public VoxelShape offset(double x, double y, double z) {
        if (this.isEmpty()) {
            return VoxelShapes.empty();
        }
        return new ArrayVoxelShape(this.voxels, (DoubleList)new OffsetDoubleList(this.getPointPositions(Direction.Axis.X), x), (DoubleList)new OffsetDoubleList(this.getPointPositions(Direction.Axis.Y), y), (DoubleList)new OffsetDoubleList(this.getPointPositions(Direction.Axis.Z), z));
    }

    public VoxelShape simplify() {
        VoxelShape[] voxelShapes = new VoxelShape[]{VoxelShapes.empty()};
        this.forEachBox((minX, minY, minZ, maxX, maxY, maxZ) -> {
            voxelShapes[0] = VoxelShapes.combine(voxelShapes[0], VoxelShapes.cuboid(minX, minY, minZ, maxX, maxY, maxZ), BooleanBiFunction.OR);
        });
        return voxelShapes[0];
    }

    public void forEachEdge(VoxelShapes.BoxConsumer consumer) {
        this.voxels.forEachEdge((minX, minY, minZ, maxX, maxY, maxZ) -> consumer.consume(this.getPointPosition(Direction.Axis.X, minX), this.getPointPosition(Direction.Axis.Y, minY), this.getPointPosition(Direction.Axis.Z, minZ), this.getPointPosition(Direction.Axis.X, maxX), this.getPointPosition(Direction.Axis.Y, maxY), this.getPointPosition(Direction.Axis.Z, maxZ)), true);
    }

    public void forEachBox(VoxelShapes.BoxConsumer consumer) {
        DoubleList doubleList = this.getPointPositions(Direction.Axis.X);
        DoubleList doubleList2 = this.getPointPositions(Direction.Axis.Y);
        DoubleList doubleList3 = this.getPointPositions(Direction.Axis.Z);
        this.voxels.forEachBox((minX, minY, minZ, maxX, maxY, maxZ) -> consumer.consume(doubleList.getDouble(minX), doubleList2.getDouble(minY), doubleList3.getDouble(minZ), doubleList.getDouble(maxX), doubleList2.getDouble(maxY), doubleList3.getDouble(maxZ)), true);
    }

    public List<Box> getBoundingBoxes() {
        ArrayList list = Lists.newArrayList();
        this.forEachBox((x1, y1, z1, x2, y2, z2) -> list.add(new Box(x1, y1, z1, x2, y2, z2)));
        return list;
    }

    public double getStartingCoord(Direction.Axis axis, double from, double to) {
        int j;
        Direction.Axis axis2 = AxisCycleDirection.FORWARD.cycle(axis);
        Direction.Axis axis3 = AxisCycleDirection.BACKWARD.cycle(axis);
        int i = this.getCoordIndex(axis2, from);
        int k = this.voxels.getStartingAxisCoord(axis, i, j = this.getCoordIndex(axis3, to));
        if (k >= this.voxels.getSize(axis)) {
            return Double.POSITIVE_INFINITY;
        }
        return this.getPointPosition(axis, k);
    }

    public double getEndingCoord(Direction.Axis axis, double from, double to) {
        int j;
        Direction.Axis axis2 = AxisCycleDirection.FORWARD.cycle(axis);
        Direction.Axis axis3 = AxisCycleDirection.BACKWARD.cycle(axis);
        int i = this.getCoordIndex(axis2, from);
        int k = this.voxels.getEndingAxisCoord(axis, i, j = this.getCoordIndex(axis3, to));
        if (k <= 0) {
            return Double.NEGATIVE_INFINITY;
        }
        return this.getPointPosition(axis, k);
    }

    protected int getCoordIndex(Direction.Axis axis, double coord) {
        return MathHelper.binarySearch(0, this.voxels.getSize(axis) + 1, i -> coord < this.getPointPosition(axis, i)) - 1;
    }

    public @Nullable BlockHitResult raycast(Vec3d start, Vec3d end, BlockPos pos) {
        if (this.isEmpty()) {
            return null;
        }
        Vec3d vec3d = end.subtract(start);
        if (vec3d.lengthSquared() < 1.0E-7) {
            return null;
        }
        Vec3d vec3d2 = start.add(vec3d.multiply(0.001));
        if (this.voxels.inBoundsAndContains(this.getCoordIndex(Direction.Axis.X, vec3d2.x - (double)pos.getX()), this.getCoordIndex(Direction.Axis.Y, vec3d2.y - (double)pos.getY()), this.getCoordIndex(Direction.Axis.Z, vec3d2.z - (double)pos.getZ()))) {
            return new BlockHitResult(vec3d2, Direction.getFacing(vec3d.x, vec3d.y, vec3d.z).getOpposite(), pos, true);
        }
        return Box.raycast(this.getBoundingBoxes(), start, end, pos);
    }

    /*
     * Issues handling annotations - annotations may be inaccurate
     */
    public Optional<Vec3d> getClosestPointTo(Vec3d target) {
        if (this.isEmpty()) {
            return Optional.empty();
        }
        @Nullable MutableObject mutableObject = new MutableObject();
        this.forEachBox((minX, minY, minZ, maxX, maxY, maxZ) -> {
            double d = MathHelper.clamp(target.getX(), minX, maxX);
            double e = MathHelper.clamp(target.getY(), minY, maxY);
            double f = MathHelper.clamp(target.getZ(), minZ, maxZ);
            Vec3d vec3d2 = (Vec3d)mutableObject.get();
            if (vec3d2 == null || target.squaredDistanceTo(d, e, f) < target.squaredDistanceTo(vec3d2)) {
                mutableObject.setValue((Object)new Vec3d(d, e, f));
            }
        });
        return Optional.of(Objects.requireNonNull((Vec3d)mutableObject.get()));
    }

    public VoxelShape getFace(Direction facing) {
        VoxelShape voxelShape;
        if (this.isEmpty() || this == VoxelShapes.fullCube()) {
            return this;
        }
        if (this.shapeCache != null) {
            voxelShape = this.shapeCache[facing.ordinal()];
            if (voxelShape != null) {
                return voxelShape;
            }
        } else {
            this.shapeCache = new VoxelShape[6];
        }
        this.shapeCache[facing.ordinal()] = voxelShape = this.getUncachedFace(facing);
        return voxelShape;
    }

    private VoxelShape getUncachedFace(Direction facing) {
        Direction.Axis axis = facing.getAxis();
        if (this.isSquare(axis)) {
            return this;
        }
        Direction.AxisDirection axisDirection = facing.getDirection();
        int i = this.getCoordIndex(axis, axisDirection == Direction.AxisDirection.POSITIVE ? 0.9999999 : 1.0E-7);
        SlicedVoxelShape slicedVoxelShape = new SlicedVoxelShape(this, axis, i);
        if (slicedVoxelShape.isEmpty()) {
            return VoxelShapes.empty();
        }
        if (slicedVoxelShape.isCube()) {
            return VoxelShapes.fullCube();
        }
        return slicedVoxelShape;
    }

    protected boolean isCube() {
        for (Direction.Axis axis : Direction.Axis.VALUES) {
            if (this.isSquare(axis)) continue;
            return false;
        }
        return true;
    }

    private boolean isSquare(Direction.Axis axis) {
        DoubleList doubleList = this.getPointPositions(axis);
        return doubleList.size() == 2 && DoubleMath.fuzzyEquals((double)doubleList.getDouble(0), (double)0.0, (double)1.0E-7) && DoubleMath.fuzzyEquals((double)doubleList.getDouble(1), (double)1.0, (double)1.0E-7);
    }

    public double calculateMaxDistance(Direction.Axis axis, Box box, double maxDist) {
        return this.calculateMaxDistance(AxisCycleDirection.between(axis, Direction.Axis.X), box, maxDist);
    }

    protected double calculateMaxDistance(AxisCycleDirection axisCycle, Box box, double maxDist) {
        block11: {
            int n;
            int l;
            double e;
            Direction.Axis axis;
            AxisCycleDirection axisCycleDirection;
            block10: {
                if (this.isEmpty()) {
                    return maxDist;
                }
                if (Math.abs(maxDist) < 1.0E-7) {
                    return 0.0;
                }
                axisCycleDirection = axisCycle.opposite();
                axis = axisCycleDirection.cycle(Direction.Axis.X);
                Direction.Axis axis2 = axisCycleDirection.cycle(Direction.Axis.Y);
                Direction.Axis axis3 = axisCycleDirection.cycle(Direction.Axis.Z);
                double d = box.getMax(axis);
                e = box.getMin(axis);
                int i = this.getCoordIndex(axis, e + 1.0E-7);
                int j = this.getCoordIndex(axis, d - 1.0E-7);
                int k = Math.max(0, this.getCoordIndex(axis2, box.getMin(axis2) + 1.0E-7));
                l = Math.min(this.voxels.getSize(axis2), this.getCoordIndex(axis2, box.getMax(axis2) - 1.0E-7) + 1);
                int m = Math.max(0, this.getCoordIndex(axis3, box.getMin(axis3) + 1.0E-7));
                n = Math.min(this.voxels.getSize(axis3), this.getCoordIndex(axis3, box.getMax(axis3) - 1.0E-7) + 1);
                int o = this.voxels.getSize(axis);
                if (!(maxDist > 0.0)) break block10;
                for (int p = j + 1; p < o; ++p) {
                    for (int q = k; q < l; ++q) {
                        for (int r = m; r < n; ++r) {
                            if (!this.voxels.inBoundsAndContains(axisCycleDirection, p, q, r)) continue;
                            double f = this.getPointPosition(axis, p) - d;
                            if (f >= -1.0E-7) {
                                maxDist = Math.min(maxDist, f);
                            }
                            return maxDist;
                        }
                    }
                }
                break block11;
            }
            if (!(maxDist < 0.0)) break block11;
            for (int p = i - 1; p >= 0; --p) {
                for (int q = k; q < l; ++q) {
                    for (int r = m; r < n; ++r) {
                        if (!this.voxels.inBoundsAndContains(axisCycleDirection, p, q, r)) continue;
                        double f = this.getPointPosition(axis, p + 1) - e;
                        if (f <= 1.0E-7) {
                            maxDist = Math.max(maxDist, f);
                        }
                        return maxDist;
                    }
                }
            }
        }
        return maxDist;
    }

    public boolean equals(Object object) {
        return super.equals(object);
    }

    public String toString() {
        return this.isEmpty() ? "EMPTY" : "VoxelShape[" + String.valueOf(this.getBoundingBox()) + "]";
    }
}


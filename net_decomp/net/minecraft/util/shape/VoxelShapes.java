/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  com.google.common.annotations.VisibleForTesting
 *  com.google.common.collect.Maps
 *  com.google.common.math.DoubleMath
 *  com.google.common.math.IntMath
 *  it.unimi.dsi.fastutil.doubles.DoubleArrayList
 *  it.unimi.dsi.fastutil.doubles.DoubleList
 */
package net.minecraft.util.shape;

import com.google.common.annotations.VisibleForTesting;
import com.google.common.collect.Maps;
import com.google.common.math.DoubleMath;
import com.google.common.math.IntMath;
import it.unimi.dsi.fastutil.doubles.DoubleArrayList;
import it.unimi.dsi.fastutil.doubles.DoubleList;
import java.util.Arrays;
import java.util.Map;
import java.util.Objects;
import net.minecraft.block.enums.BlockFace;
import net.minecraft.util.Util;
import net.minecraft.util.function.BooleanBiFunction;
import net.minecraft.util.math.AxisCycleDirection;
import net.minecraft.util.math.Box;
import net.minecraft.util.math.Direction;
import net.minecraft.util.math.DirectionTransformation;
import net.minecraft.util.math.Vec3d;
import net.minecraft.util.shape.ArrayVoxelShape;
import net.minecraft.util.shape.BitSetVoxelSet;
import net.minecraft.util.shape.DisjointPairList;
import net.minecraft.util.shape.FractionalDoubleList;
import net.minecraft.util.shape.FractionalPairList;
import net.minecraft.util.shape.IdentityPairList;
import net.minecraft.util.shape.PairList;
import net.minecraft.util.shape.SimplePairList;
import net.minecraft.util.shape.SimpleVoxelShape;
import net.minecraft.util.shape.SlicedVoxelShape;
import net.minecraft.util.shape.VoxelSet;
import net.minecraft.util.shape.VoxelShape;

public final class VoxelShapes {
    public static final double MIN_SIZE = 1.0E-7;
    public static final double field_31881 = 1.0E-6;
    private static final VoxelShape FULL_CUBE = Util.make(() -> {
        BitSetVoxelSet voxelSet = new BitSetVoxelSet(1, 1, 1);
        ((VoxelSet)voxelSet).set(0, 0, 0);
        return new SimpleVoxelShape(voxelSet);
    });
    private static final Vec3d BLOCK_CENTER = new Vec3d(0.5, 0.5, 0.5);
    public static final VoxelShape UNBOUNDED = VoxelShapes.cuboid(Double.NEGATIVE_INFINITY, Double.NEGATIVE_INFINITY, Double.NEGATIVE_INFINITY, Double.POSITIVE_INFINITY, Double.POSITIVE_INFINITY, Double.POSITIVE_INFINITY);
    private static final VoxelShape EMPTY = new ArrayVoxelShape((VoxelSet)new BitSetVoxelSet(0, 0, 0), (DoubleList)new DoubleArrayList(new double[]{0.0}), (DoubleList)new DoubleArrayList(new double[]{0.0}), (DoubleList)new DoubleArrayList(new double[]{0.0}));

    public static VoxelShape empty() {
        return EMPTY;
    }

    public static VoxelShape fullCube() {
        return FULL_CUBE;
    }

    public static VoxelShape cuboid(double minX, double minY, double minZ, double maxX, double maxY, double maxZ) {
        if (minX > maxX || minY > maxY || minZ > maxZ) {
            throw new IllegalArgumentException("The min values need to be smaller or equals to the max values");
        }
        return VoxelShapes.cuboidUnchecked(minX, minY, minZ, maxX, maxY, maxZ);
    }

    public static VoxelShape cuboidUnchecked(double minX, double minY, double minZ, double maxX, double maxY, double maxZ) {
        if (maxX - minX < 1.0E-7 || maxY - minY < 1.0E-7 || maxZ - minZ < 1.0E-7) {
            return VoxelShapes.empty();
        }
        int i = VoxelShapes.findRequiredBitResolution(minX, maxX);
        int j = VoxelShapes.findRequiredBitResolution(minY, maxY);
        int k = VoxelShapes.findRequiredBitResolution(minZ, maxZ);
        if (i < 0 || j < 0 || k < 0) {
            return new ArrayVoxelShape(VoxelShapes.FULL_CUBE.voxels, (DoubleList)DoubleArrayList.wrap((double[])new double[]{minX, maxX}), (DoubleList)DoubleArrayList.wrap((double[])new double[]{minY, maxY}), (DoubleList)DoubleArrayList.wrap((double[])new double[]{minZ, maxZ}));
        }
        if (i == 0 && j == 0 && k == 0) {
            return VoxelShapes.fullCube();
        }
        int l = 1 << i;
        int m = 1 << j;
        int n = 1 << k;
        BitSetVoxelSet bitSetVoxelSet = BitSetVoxelSet.create(l, m, n, (int)Math.round(minX * (double)l), (int)Math.round(minY * (double)m), (int)Math.round(minZ * (double)n), (int)Math.round(maxX * (double)l), (int)Math.round(maxY * (double)m), (int)Math.round(maxZ * (double)n));
        return new SimpleVoxelShape(bitSetVoxelSet);
    }

    public static VoxelShape cuboid(Box box) {
        return VoxelShapes.cuboidUnchecked(box.minX, box.minY, box.minZ, box.maxX, box.maxY, box.maxZ);
    }

    @VisibleForTesting
    protected static int findRequiredBitResolution(double min, double max) {
        if (min < -1.0E-7 || max > 1.0000001) {
            return -1;
        }
        for (int i = 0; i <= 3; ++i) {
            boolean bl2;
            int j = 1 << i;
            double d = min * (double)j;
            double e = max * (double)j;
            boolean bl = Math.abs(d - (double)Math.round(d)) < 1.0E-7 * (double)j;
            boolean bl3 = bl2 = Math.abs(e - (double)Math.round(e)) < 1.0E-7 * (double)j;
            if (!bl || !bl2) continue;
            return i;
        }
        return -1;
    }

    protected static long lcm(int a, int b) {
        return (long)a * (long)(b / IntMath.gcd((int)a, (int)b));
    }

    public static VoxelShape union(VoxelShape first, VoxelShape second) {
        return VoxelShapes.combineAndSimplify(first, second, BooleanBiFunction.OR);
    }

    public static VoxelShape union(VoxelShape first, VoxelShape ... others) {
        return Arrays.stream(others).reduce(first, VoxelShapes::union);
    }

    public static VoxelShape combineAndSimplify(VoxelShape first, VoxelShape second, BooleanBiFunction function) {
        return VoxelShapes.combine(first, second, function).simplify();
    }

    public static VoxelShape combine(VoxelShape one, VoxelShape two, BooleanBiFunction function) {
        if (function.apply(false, false)) {
            throw Util.getFatalOrPause(new IllegalArgumentException());
        }
        if (one == two) {
            return function.apply(true, true) ? one : VoxelShapes.empty();
        }
        boolean bl = function.apply(true, false);
        boolean bl2 = function.apply(false, true);
        if (one.isEmpty()) {
            return bl2 ? two : VoxelShapes.empty();
        }
        if (two.isEmpty()) {
            return bl ? one : VoxelShapes.empty();
        }
        PairList pairList = VoxelShapes.createListPair(1, one.getPointPositions(Direction.Axis.X), two.getPointPositions(Direction.Axis.X), bl, bl2);
        PairList pairList2 = VoxelShapes.createListPair(pairList.size() - 1, one.getPointPositions(Direction.Axis.Y), two.getPointPositions(Direction.Axis.Y), bl, bl2);
        PairList pairList3 = VoxelShapes.createListPair((pairList.size() - 1) * (pairList2.size() - 1), one.getPointPositions(Direction.Axis.Z), two.getPointPositions(Direction.Axis.Z), bl, bl2);
        BitSetVoxelSet bitSetVoxelSet = BitSetVoxelSet.combine(one.voxels, two.voxels, pairList, pairList2, pairList3, function);
        if (pairList instanceof FractionalPairList && pairList2 instanceof FractionalPairList && pairList3 instanceof FractionalPairList) {
            return new SimpleVoxelShape(bitSetVoxelSet);
        }
        return new ArrayVoxelShape((VoxelSet)bitSetVoxelSet, pairList.getPairs(), pairList2.getPairs(), pairList3.getPairs());
    }

    public static boolean matchesAnywhere(VoxelShape shape1, VoxelShape shape2, BooleanBiFunction predicate) {
        if (predicate.apply(false, false)) {
            throw Util.getFatalOrPause(new IllegalArgumentException());
        }
        boolean bl = shape1.isEmpty();
        boolean bl2 = shape2.isEmpty();
        if (bl || bl2) {
            return predicate.apply(!bl, !bl2);
        }
        if (shape1 == shape2) {
            return predicate.apply(true, true);
        }
        boolean bl3 = predicate.apply(true, false);
        boolean bl4 = predicate.apply(false, true);
        for (Direction.Axis axis : AxisCycleDirection.AXES) {
            if (shape1.getMax(axis) < shape2.getMin(axis) - 1.0E-7) {
                return bl3 || bl4;
            }
            if (!(shape2.getMax(axis) < shape1.getMin(axis) - 1.0E-7)) continue;
            return bl3 || bl4;
        }
        PairList pairList = VoxelShapes.createListPair(1, shape1.getPointPositions(Direction.Axis.X), shape2.getPointPositions(Direction.Axis.X), bl3, bl4);
        PairList pairList2 = VoxelShapes.createListPair(pairList.size() - 1, shape1.getPointPositions(Direction.Axis.Y), shape2.getPointPositions(Direction.Axis.Y), bl3, bl4);
        PairList pairList3 = VoxelShapes.createListPair((pairList.size() - 1) * (pairList2.size() - 1), shape1.getPointPositions(Direction.Axis.Z), shape2.getPointPositions(Direction.Axis.Z), bl3, bl4);
        return VoxelShapes.matchesAnywhere(pairList, pairList2, pairList3, shape1.voxels, shape2.voxels, predicate);
    }

    private static boolean matchesAnywhere(PairList mergedX, PairList mergedY, PairList mergedZ, VoxelSet shape1, VoxelSet shape2, BooleanBiFunction predicate) {
        return !mergedX.forEachPair((x1, x2, index1) -> mergedY.forEachPair((y1, y2, index2) -> mergedZ.forEachPair((z1, z2, index3) -> !predicate.apply(shape1.inBoundsAndContains(x1, y1, z1), shape2.inBoundsAndContains(x2, y2, z2)))));
    }

    public static double calculateMaxOffset(Direction.Axis axis, Box box, Iterable<VoxelShape> shapes, double maxDist) {
        for (VoxelShape voxelShape : shapes) {
            if (Math.abs(maxDist) < 1.0E-7) {
                return 0.0;
            }
            maxDist = voxelShape.calculateMaxDistance(axis, box, maxDist);
        }
        return maxDist;
    }

    public static boolean isSideCovered(VoxelShape shape, VoxelShape neighbor, Direction direction) {
        if (shape == VoxelShapes.fullCube() && neighbor == VoxelShapes.fullCube()) {
            return true;
        }
        if (neighbor.isEmpty()) {
            return false;
        }
        Direction.Axis axis = direction.getAxis();
        Direction.AxisDirection axisDirection = direction.getDirection();
        VoxelShape voxelShape = axisDirection == Direction.AxisDirection.POSITIVE ? shape : neighbor;
        VoxelShape voxelShape2 = axisDirection == Direction.AxisDirection.POSITIVE ? neighbor : shape;
        BooleanBiFunction booleanBiFunction = axisDirection == Direction.AxisDirection.POSITIVE ? BooleanBiFunction.ONLY_FIRST : BooleanBiFunction.ONLY_SECOND;
        return DoubleMath.fuzzyEquals((double)voxelShape.getMax(axis), (double)1.0, (double)1.0E-7) && DoubleMath.fuzzyEquals((double)voxelShape2.getMin(axis), (double)0.0, (double)1.0E-7) && !VoxelShapes.matchesAnywhere(new SlicedVoxelShape(voxelShape, axis, voxelShape.voxels.getSize(axis) - 1), new SlicedVoxelShape(voxelShape2, axis, 0), booleanBiFunction);
    }

    public static boolean adjacentSidesCoverSquare(VoxelShape one, VoxelShape two, Direction direction) {
        VoxelShape voxelShape2;
        if (one == VoxelShapes.fullCube() || two == VoxelShapes.fullCube()) {
            return true;
        }
        Direction.Axis axis = direction.getAxis();
        Direction.AxisDirection axisDirection = direction.getDirection();
        VoxelShape voxelShape = axisDirection == Direction.AxisDirection.POSITIVE ? one : two;
        VoxelShape voxelShape3 = voxelShape2 = axisDirection == Direction.AxisDirection.POSITIVE ? two : one;
        if (!DoubleMath.fuzzyEquals((double)voxelShape.getMax(axis), (double)1.0, (double)1.0E-7)) {
            voxelShape = VoxelShapes.empty();
        }
        if (!DoubleMath.fuzzyEquals((double)voxelShape2.getMin(axis), (double)0.0, (double)1.0E-7)) {
            voxelShape2 = VoxelShapes.empty();
        }
        return !VoxelShapes.matchesAnywhere(VoxelShapes.fullCube(), VoxelShapes.combine(new SlicedVoxelShape(voxelShape, axis, voxelShape.voxels.getSize(axis) - 1), new SlicedVoxelShape(voxelShape2, axis, 0), BooleanBiFunction.OR), BooleanBiFunction.ONLY_FIRST);
    }

    public static boolean unionCoversFullCube(VoxelShape one, VoxelShape two) {
        if (one == VoxelShapes.fullCube() || two == VoxelShapes.fullCube()) {
            return true;
        }
        if (one.isEmpty() && two.isEmpty()) {
            return false;
        }
        return !VoxelShapes.matchesAnywhere(VoxelShapes.fullCube(), VoxelShapes.combine(one, two, BooleanBiFunction.OR), BooleanBiFunction.ONLY_FIRST);
    }

    @VisibleForTesting
    protected static PairList createListPair(int size, DoubleList first, DoubleList second, boolean includeFirst, boolean includeSecond) {
        long l;
        int i = first.size() - 1;
        int j = second.size() - 1;
        if (first instanceof FractionalDoubleList && second instanceof FractionalDoubleList && (long)size * (l = VoxelShapes.lcm(i, j)) <= 256L) {
            return new FractionalPairList(i, j);
        }
        if (first.getDouble(i) < second.getDouble(0) - 1.0E-7) {
            return new DisjointPairList(first, second, false);
        }
        if (second.getDouble(j) < first.getDouble(0) - 1.0E-7) {
            return new DisjointPairList(second, first, true);
        }
        if (i == j && Objects.equals(first, second)) {
            return new IdentityPairList(first);
        }
        return new SimplePairList(first, second, includeFirst, includeSecond);
    }

    public static VoxelShape transform(VoxelShape shape, DirectionTransformation transformation) {
        return VoxelShapes.transform(shape, transformation, BLOCK_CENTER);
    }

    public static VoxelShape transform(VoxelShape shape, DirectionTransformation transformation, Vec3d anchor) {
        if (transformation == DirectionTransformation.IDENTITY) {
            return shape;
        }
        VoxelSet voxelSet = shape.voxels.transform(transformation);
        if (shape instanceof SimpleVoxelShape && BLOCK_CENTER.equals(anchor)) {
            return new SimpleVoxelShape(voxelSet);
        }
        Direction.Axis axis = transformation.getAxisTransformation().map(Direction.Axis.X);
        Direction.Axis axis2 = transformation.getAxisTransformation().map(Direction.Axis.Y);
        Direction.Axis axis3 = transformation.getAxisTransformation().map(Direction.Axis.Z);
        DoubleList doubleList = shape.getPointPositions(axis);
        DoubleList doubleList2 = shape.getPointPositions(axis2);
        DoubleList doubleList3 = shape.getPointPositions(axis3);
        boolean bl = transformation.shouldFlipDirection(Direction.Axis.X);
        boolean bl2 = transformation.shouldFlipDirection(Direction.Axis.Y);
        boolean bl3 = transformation.shouldFlipDirection(Direction.Axis.Z);
        return new ArrayVoxelShape(voxelSet, VoxelShapes.transform(doubleList, bl, anchor.getComponentAlongAxis(axis), anchor.x), VoxelShapes.transform(doubleList2, bl2, anchor.getComponentAlongAxis(axis2), anchor.y), VoxelShapes.transform(doubleList3, bl3, anchor.getComponentAlongAxis(axis3), anchor.z));
    }

    @VisibleForTesting
    static DoubleList transform(DoubleList pointPositions, boolean flip, double component, double anchor) {
        if (!flip && component == anchor) {
            return pointPositions;
        }
        int i = pointPositions.size();
        DoubleArrayList doubleList = new DoubleArrayList(i);
        if (flip) {
            for (int j = i - 1; j >= 0; --j) {
                doubleList.add(-(pointPositions.getDouble(j) - component) + anchor);
            }
        } else {
            for (int j = 0; j >= 0 && j < i; ++j) {
                doubleList.add(pointPositions.getDouble(j) - component + anchor);
            }
        }
        return doubleList;
    }

    public static boolean equal(VoxelShape shape1, VoxelShape shape2) {
        return !VoxelShapes.matchesAnywhere(shape1, shape2, BooleanBiFunction.NOT_SAME);
    }

    public static Map<Direction.Axis, VoxelShape> createHorizontalAxisShapeMap(VoxelShape shape) {
        return VoxelShapes.createHorizontalAxisShapeMap(shape, BLOCK_CENTER);
    }

    public static Map<Direction.Axis, VoxelShape> createHorizontalAxisShapeMap(VoxelShape shape, Vec3d anchor) {
        return Maps.newEnumMap(Map.of(Direction.Axis.Z, shape, Direction.Axis.X, VoxelShapes.transform(shape, DirectionTransformation.field_64511, anchor)));
    }

    public static Map<Direction.Axis, VoxelShape> createAxisShapeMap(VoxelShape shape) {
        return VoxelShapes.createAxisShapeMap(shape, BLOCK_CENTER);
    }

    public static Map<Direction.Axis, VoxelShape> createAxisShapeMap(VoxelShape shape, Vec3d anchor) {
        return Maps.newEnumMap(Map.of(Direction.Axis.Z, shape, Direction.Axis.X, VoxelShapes.transform(shape, DirectionTransformation.field_64511, anchor), Direction.Axis.Y, VoxelShapes.transform(shape, DirectionTransformation.field_64508, anchor)));
    }

    public static Map<Direction, VoxelShape> createHorizontalFacingShapeMap(VoxelShape shape) {
        return VoxelShapes.createHorizontalFacingShapeMap(shape, DirectionTransformation.IDENTITY, BLOCK_CENTER);
    }

    public static Map<Direction, VoxelShape> createHorizontalFacingShapeMap(VoxelShape shape, DirectionTransformation transformation) {
        return VoxelShapes.createHorizontalFacingShapeMap(shape, transformation, BLOCK_CENTER);
    }

    public static Map<Direction, VoxelShape> createHorizontalFacingShapeMap(VoxelShape shape, DirectionTransformation transformation, Vec3d anchor) {
        return Maps.newEnumMap(Map.of(Direction.NORTH, VoxelShapes.transform(shape, transformation), Direction.EAST, VoxelShapes.transform(shape, DirectionTransformation.field_64511.prepend(transformation), anchor), Direction.SOUTH, VoxelShapes.transform(shape, DirectionTransformation.field_64510.prepend(transformation), anchor), Direction.WEST, VoxelShapes.transform(shape, DirectionTransformation.field_64509.prepend(transformation), anchor)));
    }

    public static Map<Direction, VoxelShape> createFacingShapeMap(VoxelShape shape) {
        return VoxelShapes.createFacingShapeMap(shape, DirectionTransformation.IDENTITY, BLOCK_CENTER);
    }

    public static Map<Direction, VoxelShape> createFacingShapeMap(VoxelShape shape, Vec3d anchor) {
        return VoxelShapes.createFacingShapeMap(shape, DirectionTransformation.IDENTITY, anchor);
    }

    public static Map<Direction, VoxelShape> createFacingShapeMap(VoxelShape shape, DirectionTransformation transformation, Vec3d anchor) {
        return Maps.newEnumMap(Map.of(Direction.NORTH, VoxelShapes.transform(shape, transformation), Direction.EAST, VoxelShapes.transform(shape, DirectionTransformation.field_64511.prepend(transformation), anchor), Direction.SOUTH, VoxelShapes.transform(shape, DirectionTransformation.field_64510.prepend(transformation), anchor), Direction.WEST, VoxelShapes.transform(shape, DirectionTransformation.field_64509.prepend(transformation), anchor), Direction.UP, VoxelShapes.transform(shape, DirectionTransformation.field_64506.prepend(transformation), anchor), Direction.DOWN, VoxelShapes.transform(shape, DirectionTransformation.field_64508.prepend(transformation), anchor)));
    }

    public static Map<BlockFace, Map<Direction, VoxelShape>> createBlockFaceHorizontalFacingShapeMap(VoxelShape shape) {
        return VoxelShapes.createBlockFaceHorizontalFacingShapeMap(shape, DirectionTransformation.IDENTITY);
    }

    public static Map<BlockFace, Map<Direction, VoxelShape>> createBlockFaceHorizontalFacingShapeMap(VoxelShape shape, DirectionTransformation transformation) {
        return Map.of(BlockFace.WALL, VoxelShapes.createHorizontalFacingShapeMap(shape, transformation), BlockFace.FLOOR, VoxelShapes.createHorizontalFacingShapeMap(shape, DirectionTransformation.field_64506.prepend(transformation)), BlockFace.CEILING, VoxelShapes.createHorizontalFacingShapeMap(shape, DirectionTransformation.field_64510.prepend(DirectionTransformation.field_64508).prepend(transformation)));
    }

    public static interface BoxConsumer {
        public void consume(double var1, double var3, double var5, double var7, double var9, double var11);
    }
}


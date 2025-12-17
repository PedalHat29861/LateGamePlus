/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  it.unimi.dsi.fastutil.doubles.DoubleArrayList
 *  it.unimi.dsi.fastutil.doubles.DoubleList
 */
package net.minecraft.util.shape;

import it.unimi.dsi.fastutil.doubles.DoubleArrayList;
import it.unimi.dsi.fastutil.doubles.DoubleList;
import java.util.Arrays;
import net.minecraft.util.Util;
import net.minecraft.util.math.Direction;
import net.minecraft.util.shape.VoxelSet;
import net.minecraft.util.shape.VoxelShape;

public class ArrayVoxelShape
extends VoxelShape {
    private final DoubleList xPoints;
    private final DoubleList yPoints;
    private final DoubleList zPoints;

    protected ArrayVoxelShape(VoxelSet shape, double[] xPoints, double[] yPoints, double[] zPoints) {
        this(shape, (DoubleList)DoubleArrayList.wrap((double[])Arrays.copyOf(xPoints, shape.getXSize() + 1)), (DoubleList)DoubleArrayList.wrap((double[])Arrays.copyOf(yPoints, shape.getYSize() + 1)), (DoubleList)DoubleArrayList.wrap((double[])Arrays.copyOf(zPoints, shape.getZSize() + 1)));
    }

    ArrayVoxelShape(VoxelSet shape, DoubleList xPoints, DoubleList yPoints, DoubleList zPoints) {
        super(shape);
        int i = shape.getXSize() + 1;
        int j = shape.getYSize() + 1;
        int k = shape.getZSize() + 1;
        if (i != xPoints.size() || j != yPoints.size() || k != zPoints.size()) {
            throw Util.getFatalOrPause(new IllegalArgumentException("Lengths of point arrays must be consistent with the size of the VoxelShape."));
        }
        this.xPoints = xPoints;
        this.yPoints = yPoints;
        this.zPoints = zPoints;
    }

    @Override
    public DoubleList getPointPositions(Direction.Axis axis) {
        return switch (axis) {
            default -> throw new MatchException(null, null);
            case Direction.Axis.X -> this.xPoints;
            case Direction.Axis.Y -> this.yPoints;
            case Direction.Axis.Z -> this.zPoints;
        };
    }
}


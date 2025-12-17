/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  org.joml.Matrix3f
 *  org.joml.Matrix3fc
 *  org.joml.Vector3f
 *  org.joml.Vector3i
 */
package net.minecraft.util.math;

import java.util.Arrays;
import net.minecraft.util.Util;
import net.minecraft.util.math.Direction;
import org.joml.Matrix3f;
import org.joml.Matrix3fc;
import org.joml.Vector3f;
import org.joml.Vector3i;

public final class AxisTransformation
extends Enum<AxisTransformation> {
    public static final /* enum */ AxisTransformation P123 = new AxisTransformation(0, 1, 2);
    public static final /* enum */ AxisTransformation P213 = new AxisTransformation(1, 0, 2);
    public static final /* enum */ AxisTransformation P132 = new AxisTransformation(0, 2, 1);
    public static final /* enum */ AxisTransformation P312 = new AxisTransformation(2, 0, 1);
    public static final /* enum */ AxisTransformation P231 = new AxisTransformation(1, 2, 0);
    public static final /* enum */ AxisTransformation P321 = new AxisTransformation(2, 1, 0);
    private final int xMapping;
    private final int yMapping;
    private final int zMapping;
    private final Matrix3fc matrix;
    private static final AxisTransformation[][] COMBINATIONS;
    private static final AxisTransformation[] INVERSE;
    private static final /* synthetic */ AxisTransformation[] field_23371;

    public static AxisTransformation[] values() {
        return (AxisTransformation[])field_23371.clone();
    }

    public static AxisTransformation valueOf(String string) {
        return Enum.valueOf(AxisTransformation.class, string);
    }

    private AxisTransformation(int xMapping, int yMapping, int zMapping) {
        this.xMapping = xMapping;
        this.yMapping = yMapping;
        this.zMapping = zMapping;
        this.matrix = new Matrix3f().zero().set(this.map(0), 0, 1.0f).set(this.map(1), 1, 1.0f).set(this.map(2), 2, 1.0f);
    }

    public AxisTransformation prepend(AxisTransformation transformation) {
        return COMBINATIONS[this.ordinal()][transformation.ordinal()];
    }

    public AxisTransformation getInverse() {
        return INVERSE[this.ordinal()];
    }

    public int map(int axis) {
        return switch (axis) {
            case 0 -> this.xMapping;
            case 1 -> this.yMapping;
            case 2 -> this.zMapping;
            default -> throw new IllegalArgumentException("Must be 0, 1 or 2, but got " + axis);
        };
    }

    public Direction.Axis map(Direction.Axis axis) {
        return Direction.Axis.VALUES[this.map(axis.ordinal())];
    }

    public Vector3f map(Vector3f vec) {
        float f = vec.get(this.xMapping);
        float g = vec.get(this.yMapping);
        float h = vec.get(this.zMapping);
        return vec.set(f, g, h);
    }

    public Vector3i map(Vector3i vec) {
        int i = vec.get(this.xMapping);
        int j = vec.get(this.yMapping);
        int k = vec.get(this.zMapping);
        return vec.set(i, j, k);
    }

    public Matrix3fc getMatrix() {
        return this.matrix;
    }

    private static /* synthetic */ AxisTransformation[] method_36937() {
        return new AxisTransformation[]{P123, P213, P132, P312, P231, P321};
    }

    static {
        field_23371 = AxisTransformation.method_36937();
        COMBINATIONS = Util.make(() -> {
            AxisTransformation[] axisTransformations = AxisTransformation.values();
            AxisTransformation[][] axisTransformations2 = new AxisTransformation[axisTransformations.length][axisTransformations.length];
            for (AxisTransformation axisTransformation : axisTransformations) {
                for (AxisTransformation axisTransformation2 : axisTransformations) {
                    AxisTransformation axisTransformation3;
                    int i = axisTransformation.map(axisTransformation2.xMapping);
                    int j = axisTransformation.map(axisTransformation2.yMapping);
                    int k = axisTransformation.map(axisTransformation2.zMapping);
                    axisTransformations2[axisTransformation.ordinal()][axisTransformation2.ordinal()] = axisTransformation3 = Arrays.stream(axisTransformations).filter(transformation -> transformation.xMapping == i && transformation.yMapping == j && transformation.zMapping == k).findFirst().get();
                }
            }
            return axisTransformations2;
        });
        INVERSE = Util.make(() -> {
            AxisTransformation[] axisTransformations = AxisTransformation.values();
            return (AxisTransformation[])Arrays.stream(axisTransformations).map((? super T a) -> Arrays.stream(AxisTransformation.values()).filter(b -> a.prepend((AxisTransformation)((Object)((Object)((Object)b)))) == P123).findAny().get()).toArray(AxisTransformation[]::new);
        });
    }
}


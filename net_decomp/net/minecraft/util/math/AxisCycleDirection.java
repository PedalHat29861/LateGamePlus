/*
 * Decompiled with CFR 0.152.
 */
package net.minecraft.util.math;

import net.minecraft.util.math.Direction;

public abstract sealed class AxisCycleDirection
extends Enum<AxisCycleDirection> {
    public static final /* enum */ AxisCycleDirection NONE = new AxisCycleDirection(){

        @Override
        public int choose(int x, int y, int z, Direction.Axis axis) {
            return axis.choose(x, y, z);
        }

        @Override
        public double choose(double x, double y, double z, Direction.Axis axis) {
            return axis.choose(x, y, z);
        }

        @Override
        public Direction.Axis cycle(Direction.Axis axis) {
            return axis;
        }

        @Override
        public AxisCycleDirection opposite() {
            return this;
        }
    };
    public static final /* enum */ AxisCycleDirection FORWARD = new AxisCycleDirection(){

        @Override
        public int choose(int x, int y, int z, Direction.Axis axis) {
            return axis.choose(z, x, y);
        }

        @Override
        public double choose(double x, double y, double z, Direction.Axis axis) {
            return axis.choose(z, x, y);
        }

        @Override
        public Direction.Axis cycle(Direction.Axis axis) {
            return AXES[Math.floorMod(axis.ordinal() + 1, 3)];
        }

        @Override
        public AxisCycleDirection opposite() {
            return BACKWARD;
        }
    };
    public static final /* enum */ AxisCycleDirection BACKWARD = new AxisCycleDirection(){

        @Override
        public int choose(int x, int y, int z, Direction.Axis axis) {
            return axis.choose(y, z, x);
        }

        @Override
        public double choose(double x, double y, double z, Direction.Axis axis) {
            return axis.choose(y, z, x);
        }

        @Override
        public Direction.Axis cycle(Direction.Axis axis) {
            return AXES[Math.floorMod(axis.ordinal() - 1, 3)];
        }

        @Override
        public AxisCycleDirection opposite() {
            return FORWARD;
        }
    };
    public static final Direction.Axis[] AXES;
    public static final AxisCycleDirection[] VALUES;
    private static final /* synthetic */ AxisCycleDirection[] field_10964;

    public static AxisCycleDirection[] values() {
        return (AxisCycleDirection[])field_10964.clone();
    }

    public static AxisCycleDirection valueOf(String string) {
        return Enum.valueOf(AxisCycleDirection.class, string);
    }

    public abstract int choose(int var1, int var2, int var3, Direction.Axis var4);

    public abstract double choose(double var1, double var3, double var5, Direction.Axis var7);

    public abstract Direction.Axis cycle(Direction.Axis var1);

    public abstract AxisCycleDirection opposite();

    public static AxisCycleDirection between(Direction.Axis from, Direction.Axis to) {
        return VALUES[Math.floorMod(to.ordinal() - from.ordinal(), 3)];
    }

    private static /* synthetic */ AxisCycleDirection[] method_36930() {
        return new AxisCycleDirection[]{NONE, FORWARD, BACKWARD};
    }

    static {
        field_10964 = AxisCycleDirection.method_36930();
        AXES = Direction.Axis.values();
        VALUES = AxisCycleDirection.values();
    }
}


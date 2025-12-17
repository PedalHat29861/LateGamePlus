/*
 * Decompiled with CFR 0.152.
 */
package net.minecraft.util.math;

import net.minecraft.util.math.MathHelper;
import net.minecraft.util.math.Vec3d;

public class WeightedInterpolation {
    private static final int FIRST_SEGMENT_OFFSET = 2;
    private static final int NUM_SEGMENTS = 6;
    private static final double[] ENDPOINT_WEIGHTS = new double[]{0.0, 1.0, 4.0, 6.0, 4.0, 1.0, 0.0};

    public static <V> void interpolate(Vec3d pos, PositionalFunction<V> f, Accumulator<V> accum) {
        pos = pos.subtract(0.5, 0.5, 0.5);
        int i = MathHelper.floor(pos.getX());
        int j = MathHelper.floor(pos.getY());
        int k = MathHelper.floor(pos.getZ());
        double d = pos.getX() - (double)i;
        double e = pos.getY() - (double)j;
        double g = pos.getZ() - (double)k;
        for (int l = 0; l < 6; ++l) {
            double h = MathHelper.lerp(g, ENDPOINT_WEIGHTS[l + 1], ENDPOINT_WEIGHTS[l]);
            int m = k - 2 + l;
            for (int n = 0; n < 6; ++n) {
                double o = MathHelper.lerp(d, ENDPOINT_WEIGHTS[n + 1], ENDPOINT_WEIGHTS[n]);
                int p = i - 2 + n;
                for (int q = 0; q < 6; ++q) {
                    double r = MathHelper.lerp(e, ENDPOINT_WEIGHTS[q + 1], ENDPOINT_WEIGHTS[q]);
                    int s = j - 2 + q;
                    double t = o * r * h;
                    V object = f.get(p, s, m);
                    accum.accumulate(t, object);
                }
            }
        }
    }

    @FunctionalInterface
    public static interface PositionalFunction<V> {
        public V get(int var1, int var2, int var3);
    }

    @FunctionalInterface
    public static interface Accumulator<V> {
        public void accumulate(double var1, V var3);
    }
}


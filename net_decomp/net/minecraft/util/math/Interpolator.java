/*
 * Decompiled with CFR 0.152.
 */
package net.minecraft.util.math;

import net.minecraft.util.math.ColorHelper;
import net.minecraft.util.math.MathHelper;

public interface Interpolator<T> {
    public static Interpolator<Float> ofFloat() {
        return MathHelper::lerp;
    }

    public static Interpolator<Float> angle(float maxDeviation) {
        return (t, a, b) -> {
            float g = MathHelper.wrapDegrees(b.floatValue() - a.floatValue());
            if (Math.abs(g) >= maxDeviation) {
                return b;
            }
            return Float.valueOf(a.floatValue() + t * g);
        };
    }

    public static <T> Interpolator<T> first() {
        return (t, a, b) -> a;
    }

    public static <T> Interpolator<T> threshold(float threshold) {
        return (t, a, b) -> t >= threshold ? b : a;
    }

    public static Interpolator<Integer> ofColor() {
        return ColorHelper::lerp;
    }

    public T apply(float var1, T var2, T var3);
}


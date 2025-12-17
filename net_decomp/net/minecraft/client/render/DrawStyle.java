/*
 * Decompiled with CFR 0.152.
 */
package net.minecraft.client.render;

import net.minecraft.util.math.ColorHelper;

public record DrawStyle(int stroke, float strokeWidth, int fill) {
    private static final float DEFAULT_STROKE_WIDTH = 2.5f;

    public static DrawStyle stroked(int stroke) {
        return new DrawStyle(stroke, 2.5f, 0);
    }

    public static DrawStyle stroked(int stroke, float strokeWidth) {
        return new DrawStyle(stroke, strokeWidth, 0);
    }

    public static DrawStyle filled(int fill) {
        return new DrawStyle(0, 0.0f, fill);
    }

    public static DrawStyle filledAndStroked(int stroke, float strokeWidth, int fill) {
        return new DrawStyle(stroke, strokeWidth, fill);
    }

    public boolean hasFill() {
        return this.fill != 0;
    }

    public boolean hasStroke() {
        return this.stroke != 0 && this.strokeWidth > 0.0f;
    }

    public int stroke(float opacity) {
        return ColorHelper.scaleAlpha(this.stroke, opacity);
    }

    public int fill(float opacity) {
        return ColorHelper.scaleAlpha(this.fill, opacity);
    }
}


/*
 * Decompiled with CFR 0.152.
 */
package net.minecraft.util.math;

import net.minecraft.util.math.MathHelper;

public class Easing {
    public static float inBack(float t) {
        float f = 1.70158f;
        float g = 2.70158f;
        return MathHelper.square(t) * (2.70158f * t - 1.70158f);
    }

    public static float inBounce(float t) {
        return 1.0f - Easing.outBounce(1.0f - t);
    }

    public static float inCubic(float t) {
        return MathHelper.cube(t);
    }

    public static float inElastic(float t) {
        if (t == 0.0f) {
            return 0.0f;
        }
        if (t == 1.0f) {
            return 1.0f;
        }
        float f = 2.0943952f;
        return (float)(-Math.pow(2.0, 10.0 * (double)t - 10.0) * Math.sin(((double)t * 10.0 - 10.75) * 2.094395160675049));
    }

    public static float inExpo(float t) {
        return t == 0.0f ? 0.0f : (float)Math.pow(2.0, 10.0 * (double)t - 10.0);
    }

    public static float inQuart(float t) {
        return MathHelper.square(MathHelper.square(t));
    }

    public static float inQuint(float t) {
        return MathHelper.square(MathHelper.square(t)) * t;
    }

    public static float inSine(float t) {
        return 1.0f - MathHelper.cos(t * 1.5707964f);
    }

    public static float inOutBounce(float t) {
        if (t < 0.5f) {
            return (1.0f - Easing.outBounce(1.0f - 2.0f * t)) / 2.0f;
        }
        return (1.0f + Easing.outBounce(2.0f * t - 1.0f)) / 2.0f;
    }

    public static float inOutCirc(float t) {
        if (t < 0.5f) {
            return (float)((1.0 - Math.sqrt(1.0 - Math.pow(2.0 * (double)t, 2.0))) / 2.0);
        }
        return (float)((Math.sqrt(1.0 - Math.pow(-2.0 * (double)t + 2.0, 2.0)) + 1.0) / 2.0);
    }

    public static float inOutCubic(float t) {
        if (t < 0.5f) {
            return 4.0f * MathHelper.cube(t);
        }
        return (float)(1.0 - Math.pow(-2.0 * (double)t + 2.0, 3.0) / 2.0);
    }

    public static float inOutQuad(float t) {
        if (t < 0.5f) {
            return 2.0f * MathHelper.square(t);
        }
        return (float)(1.0 - Math.pow(-2.0 * (double)t + 2.0, 2.0) / 2.0);
    }

    public static float inOutQuart(float t) {
        if (t < 0.5f) {
            return 8.0f * MathHelper.square(MathHelper.square(t));
        }
        return (float)(1.0 - Math.pow(-2.0 * (double)t + 2.0, 4.0) / 2.0);
    }

    public static float inOutQuint(float t) {
        if ((double)t < 0.5) {
            return 16.0f * t * t * t * t * t;
        }
        return (float)(1.0 - Math.pow(-2.0 * (double)t + 2.0, 5.0) / 2.0);
    }

    public static float outBounce(float t) {
        float f = 7.5625f;
        float g = 2.75f;
        if (t < 0.36363637f) {
            return 7.5625f * MathHelper.square(t);
        }
        if (t < 0.72727275f) {
            return 7.5625f * MathHelper.square(t - 0.54545456f) + 0.75f;
        }
        if ((double)t < 0.9090909090909091) {
            return 7.5625f * MathHelper.square(t - 0.8181818f) + 0.9375f;
        }
        return 7.5625f * MathHelper.square(t - 0.95454544f) + 0.984375f;
    }

    public static float outElastic(float t) {
        float f = 2.0943952f;
        if (t == 0.0f) {
            return 0.0f;
        }
        if (t == 1.0f) {
            return 1.0f;
        }
        return (float)(Math.pow(2.0, -10.0 * (double)t) * Math.sin(((double)t * 10.0 - 0.75) * 2.094395160675049) + 1.0);
    }

    public static float outExpo(float t) {
        if (t == 1.0f) {
            return 1.0f;
        }
        return 1.0f - (float)Math.pow(2.0, -10.0 * (double)t);
    }

    public static float outQuad(float t) {
        return 1.0f - MathHelper.square(1.0f - t);
    }

    public static float outQuint(float t) {
        return 1.0f - (float)Math.pow(1.0 - (double)t, 5.0);
    }

    public static float outSine(float t) {
        return MathHelper.sin(t * 1.5707964f);
    }

    public static float inOutSine(float t) {
        return -(MathHelper.cos((float)Math.PI * t) - 1.0f) / 2.0f;
    }

    public static float outBack(float t) {
        float f = 1.70158f;
        float g = 2.70158f;
        return 1.0f + 2.70158f * MathHelper.cube(t - 1.0f) + 1.70158f * MathHelper.square(t - 1.0f);
    }

    public static float outQuart(float t) {
        return 1.0f - MathHelper.square(MathHelper.square(1.0f - t));
    }

    public static float outCubic(float t) {
        return 1.0f - MathHelper.cube(1.0f - t);
    }

    public static float inOutExpo(float t) {
        if (t < 0.5f) {
            return t == 0.0f ? 0.0f : (float)(Math.pow(2.0, 20.0 * (double)t - 10.0) / 2.0);
        }
        return t == 1.0f ? 1.0f : (float)((2.0 - Math.pow(2.0, -20.0 * (double)t + 10.0)) / 2.0);
    }

    public static float inQuad(float t) {
        return t * t;
    }

    public static float outCirc(float t) {
        return (float)Math.sqrt(1.0f - MathHelper.square(t - 1.0f));
    }

    public static float inOutElastic(float t) {
        float f = 1.3962635f;
        if (t == 0.0f) {
            return 0.0f;
        }
        if (t == 1.0f) {
            return 1.0f;
        }
        double d = Math.sin((20.0 * (double)t - 11.125) * 1.3962634801864624);
        if (t < 0.5f) {
            return (float)(-(Math.pow(2.0, 20.0 * (double)t - 10.0) * d) / 2.0);
        }
        return (float)(Math.pow(2.0, -20.0 * (double)t + 10.0) * d / 2.0 + 1.0);
    }

    public static float inCirc(float f) {
        return (float)(-Math.sqrt(1.0f - f * f)) + 1.0f;
    }

    public static float inOutBack(float t) {
        float f = 1.70158f;
        float g = 2.5949094f;
        if (t < 0.5f) {
            return 4.0f * t * t * (7.189819f * t - 2.5949094f) / 2.0f;
        }
        float h = 2.0f * t - 2.0f;
        return (h * h * (3.5949094f * h + 2.5949094f) + 2.0f) / 2.0f;
    }
}


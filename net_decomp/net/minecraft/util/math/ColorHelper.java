/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  org.joml.Vector3f
 *  org.joml.Vector4f
 */
package net.minecraft.util.math;

import net.minecraft.util.Util;
import net.minecraft.util.math.MathHelper;
import net.minecraft.util.math.Vec3d;
import org.joml.Vector3f;
import org.joml.Vector4f;

public class ColorHelper {
    private static final int LINEAR_TO_SRGB_LUT_LENGTH = 1024;
    private static final short[] SRGB_TO_LINEAR = Util.make(new short[256], out -> {
        for (int i = 0; i < ((short[])out).length; ++i) {
            float f = (float)i / 255.0f;
            out[i] = (short)Math.round(ColorHelper.computeSrgbToLinear(f) * 1023.0f);
        }
    });
    private static final byte[] LINEAR_TO_SRGB = Util.make(new byte[1024], out -> {
        for (int i = 0; i < ((byte[])out).length; ++i) {
            float f = (float)i / 1023.0f;
            out[i] = (byte)Math.round(ColorHelper.computeLinearToSrgb(f) * 255.0f);
        }
    });

    private static float computeSrgbToLinear(float srgb) {
        if (srgb >= 0.04045f) {
            return (float)Math.pow(((double)srgb + 0.055) / 1.055, 2.4);
        }
        return srgb / 12.92f;
    }

    private static float computeLinearToSrgb(float linear) {
        if (linear >= 0.0031308f) {
            return (float)(1.055 * Math.pow(linear, 0.4166666666666667) - 0.055);
        }
        return 12.92f * linear;
    }

    public static float srgbToLinear(int srgb) {
        return (float)SRGB_TO_LINEAR[srgb] / 1023.0f;
    }

    public static int linearToSrgb(float linear) {
        return LINEAR_TO_SRGB[MathHelper.floor(linear * 1023.0f)] & 0xFF;
    }

    public static int interpolate(int a, int b, int c, int d) {
        return ColorHelper.getArgb((ColorHelper.getAlpha(a) + ColorHelper.getAlpha(b) + ColorHelper.getAlpha(c) + ColorHelper.getAlpha(d)) / 4, ColorHelper.averageSrgbIntensities(ColorHelper.getRed(a), ColorHelper.getRed(b), ColorHelper.getRed(c), ColorHelper.getRed(d)), ColorHelper.averageSrgbIntensities(ColorHelper.getGreen(a), ColorHelper.getGreen(b), ColorHelper.getGreen(c), ColorHelper.getGreen(d)), ColorHelper.averageSrgbIntensities(ColorHelper.getBlue(a), ColorHelper.getBlue(b), ColorHelper.getBlue(c), ColorHelper.getBlue(d)));
    }

    private static int averageSrgbIntensities(int a, int b, int c, int d) {
        int i = (SRGB_TO_LINEAR[a] + SRGB_TO_LINEAR[b] + SRGB_TO_LINEAR[c] + SRGB_TO_LINEAR[d]) / 4;
        return LINEAR_TO_SRGB[i] & 0xFF;
    }

    public static int getAlpha(int argb) {
        return argb >>> 24;
    }

    public static int getRed(int argb) {
        return argb >> 16 & 0xFF;
    }

    public static int getGreen(int argb) {
        return argb >> 8 & 0xFF;
    }

    public static int getBlue(int argb) {
        return argb & 0xFF;
    }

    public static int getArgb(int alpha, int red, int green, int blue) {
        return (alpha & 0xFF) << 24 | (red & 0xFF) << 16 | (green & 0xFF) << 8 | blue & 0xFF;
    }

    public static int getArgb(int red, int green, int blue) {
        return ColorHelper.getArgb(255, red, green, blue);
    }

    public static int getArgb(Vec3d rgb) {
        return ColorHelper.getArgb(ColorHelper.channelFromFloat((float)rgb.getX()), ColorHelper.channelFromFloat((float)rgb.getY()), ColorHelper.channelFromFloat((float)rgb.getZ()));
    }

    public static int mix(int first, int second) {
        if (first == -1) {
            return second;
        }
        if (second == -1) {
            return first;
        }
        return ColorHelper.getArgb(ColorHelper.getAlpha(first) * ColorHelper.getAlpha(second) / 255, ColorHelper.getRed(first) * ColorHelper.getRed(second) / 255, ColorHelper.getGreen(first) * ColorHelper.getGreen(second) / 255, ColorHelper.getBlue(first) * ColorHelper.getBlue(second) / 255);
    }

    public static int add(int a, int b) {
        return ColorHelper.getArgb(ColorHelper.getAlpha(a), Math.min(ColorHelper.getRed(a) + ColorHelper.getRed(b), 255), Math.min(ColorHelper.getGreen(a) + ColorHelper.getGreen(b), 255), Math.min(ColorHelper.getBlue(a) + ColorHelper.getBlue(b), 255));
    }

    public static int subtract(int a, int b) {
        return ColorHelper.getArgb(ColorHelper.getAlpha(a), Math.max(ColorHelper.getRed(a) - ColorHelper.getRed(b), 0), Math.max(ColorHelper.getGreen(a) - ColorHelper.getGreen(b), 0), Math.max(ColorHelper.getBlue(a) - ColorHelper.getBlue(b), 0));
    }

    public static int scaleAlpha(int argb, float scale) {
        if (argb == 0 || scale <= 0.0f) {
            return 0;
        }
        if (scale >= 1.0f) {
            return argb;
        }
        return ColorHelper.withAlpha(ColorHelper.getAlphaFloat(argb) * scale, argb);
    }

    public static int scaleRgb(int argb, float scale) {
        return ColorHelper.scaleRgb(argb, scale, scale, scale);
    }

    public static int scaleRgb(int argb, float redScale, float greenScale, float blueScale) {
        return ColorHelper.getArgb(ColorHelper.getAlpha(argb), Math.clamp((long)((int)((float)ColorHelper.getRed(argb) * redScale)), 0, 255), Math.clamp((long)((int)((float)ColorHelper.getGreen(argb) * greenScale)), 0, 255), Math.clamp((long)((int)((float)ColorHelper.getBlue(argb) * blueScale)), 0, 255));
    }

    public static int scaleRgb(int argb, int scale) {
        return ColorHelper.getArgb(ColorHelper.getAlpha(argb), Math.clamp((long)ColorHelper.getRed(argb) * (long)scale / 255L, 0, 255), Math.clamp((long)ColorHelper.getGreen(argb) * (long)scale / 255L, 0, 255), Math.clamp((long)ColorHelper.getBlue(argb) * (long)scale / 255L, 0, 255));
    }

    public static int grayscale(int argb) {
        int i = (int)((float)ColorHelper.getRed(argb) * 0.3f + (float)ColorHelper.getGreen(argb) * 0.59f + (float)ColorHelper.getBlue(argb) * 0.11f);
        return ColorHelper.getArgb(ColorHelper.getAlpha(argb), i, i, i);
    }

    public static int alphaBlend(int a, int b) {
        int i = ColorHelper.getAlpha(a);
        int j = ColorHelper.getAlpha(b);
        if (j == 255) {
            return b;
        }
        if (j == 0) {
            return a;
        }
        int k = j + i * (255 - j) / 255;
        return ColorHelper.getArgb(k, ColorHelper.blend(k, j, ColorHelper.getRed(a), ColorHelper.getRed(b)), ColorHelper.blend(k, j, ColorHelper.getGreen(a), ColorHelper.getGreen(b)), ColorHelper.blend(k, j, ColorHelper.getBlue(a), ColorHelper.getBlue(b)));
    }

    private static int blend(int blendedAlpha, int alpha, int a, int b) {
        return (b * alpha + a * (blendedAlpha - alpha)) / blendedAlpha;
    }

    public static int lerp(float delta, int start, int end) {
        int i = MathHelper.lerp(delta, ColorHelper.getAlpha(start), ColorHelper.getAlpha(end));
        int j = MathHelper.lerp(delta, ColorHelper.getRed(start), ColorHelper.getRed(end));
        int k = MathHelper.lerp(delta, ColorHelper.getGreen(start), ColorHelper.getGreen(end));
        int l = MathHelper.lerp(delta, ColorHelper.getBlue(start), ColorHelper.getBlue(end));
        return ColorHelper.getArgb(i, j, k, l);
    }

    public static int lerpLinear(float delta, int start, int end) {
        return ColorHelper.getArgb(MathHelper.lerp(delta, ColorHelper.getAlpha(start), ColorHelper.getAlpha(end)), LINEAR_TO_SRGB[MathHelper.lerp(delta, SRGB_TO_LINEAR[ColorHelper.getRed(start)], SRGB_TO_LINEAR[ColorHelper.getRed(end)])] & 0xFF, LINEAR_TO_SRGB[MathHelper.lerp(delta, SRGB_TO_LINEAR[ColorHelper.getGreen(start)], SRGB_TO_LINEAR[ColorHelper.getGreen(end)])] & 0xFF, LINEAR_TO_SRGB[MathHelper.lerp(delta, SRGB_TO_LINEAR[ColorHelper.getBlue(start)], SRGB_TO_LINEAR[ColorHelper.getBlue(end)])] & 0xFF);
    }

    public static int fullAlpha(int argb) {
        return argb | 0xFF000000;
    }

    public static int zeroAlpha(int argb) {
        return argb & 0xFFFFFF;
    }

    public static int withAlpha(int alpha, int rgb) {
        return alpha << 24 | rgb & 0xFFFFFF;
    }

    public static int withAlpha(float alpha, int color) {
        return ColorHelper.channelFromFloat(alpha) << 24 | color & 0xFFFFFF;
    }

    public static int getWhite(float alpha) {
        return ColorHelper.channelFromFloat(alpha) << 24 | 0xFFFFFF;
    }

    public static int whiteWithAlpha(int alpha) {
        return alpha << 24 | 0xFFFFFF;
    }

    public static int toAlpha(float alpha) {
        return ColorHelper.channelFromFloat(alpha) << 24;
    }

    public static int toAlpha(int alpha) {
        return alpha << 24;
    }

    public static int fromFloats(float alpha, float red, float green, float blue) {
        return ColorHelper.getArgb(ColorHelper.channelFromFloat(alpha), ColorHelper.channelFromFloat(red), ColorHelper.channelFromFloat(green), ColorHelper.channelFromFloat(blue));
    }

    public static Vector3f toRgbVector(int rgb) {
        return new Vector3f(ColorHelper.getRedFloat(rgb), ColorHelper.getGreenFloat(rgb), ColorHelper.getBlueFloat(rgb));
    }

    public static Vector4f toRgbaVector(int argb) {
        return new Vector4f(ColorHelper.getRedFloat(argb), ColorHelper.getGreenFloat(argb), ColorHelper.getBlueFloat(argb), ColorHelper.getAlphaFloat(argb));
    }

    public static int average(int first, int second) {
        return ColorHelper.getArgb((ColorHelper.getAlpha(first) + ColorHelper.getAlpha(second)) / 2, (ColorHelper.getRed(first) + ColorHelper.getRed(second)) / 2, (ColorHelper.getGreen(first) + ColorHelper.getGreen(second)) / 2, (ColorHelper.getBlue(first) + ColorHelper.getBlue(second)) / 2);
    }

    public static int channelFromFloat(float value) {
        return MathHelper.floor(value * 255.0f);
    }

    public static float getAlphaFloat(int argb) {
        return ColorHelper.floatFromChannel(ColorHelper.getAlpha(argb));
    }

    public static float getRedFloat(int argb) {
        return ColorHelper.floatFromChannel(ColorHelper.getRed(argb));
    }

    public static float getGreenFloat(int argb) {
        return ColorHelper.floatFromChannel(ColorHelper.getGreen(argb));
    }

    public static float getBlueFloat(int argb) {
        return ColorHelper.floatFromChannel(ColorHelper.getBlue(argb));
    }

    private static float floatFromChannel(int channel) {
        return (float)channel / 255.0f;
    }

    public static int toAbgr(int argb) {
        return argb & 0xFF00FF00 | (argb & 0xFF0000) >> 16 | (argb & 0xFF) << 16;
    }

    public static int fromAbgr(int abgr) {
        return ColorHelper.toAbgr(abgr);
    }

    public static int withBrightness(int argb, float brightness) {
        float q;
        float p;
        float o;
        float h;
        int i = ColorHelper.getRed(argb);
        int j = ColorHelper.getGreen(argb);
        int k = ColorHelper.getBlue(argb);
        int l = ColorHelper.getAlpha(argb);
        int m = Math.max(Math.max(i, j), k);
        int n = Math.min(Math.min(i, j), k);
        float f = m - n;
        float g = m != 0 ? f / (float)m : 0.0f;
        if (g == 0.0f) {
            h = 0.0f;
        } else {
            o = (float)(m - i) / f;
            p = (float)(m - j) / f;
            q = (float)(m - k) / f;
            h = i == m ? q - p : (j == m ? 2.0f + o - q : 4.0f + p - o);
            if ((h /= 6.0f) < 0.0f) {
                h += 1.0f;
            }
        }
        if (g == 0.0f) {
            j = k = Math.round(brightness * 255.0f);
            i = k;
            return ColorHelper.getArgb(l, i, j, k);
        }
        o = (h - (float)Math.floor(h)) * 6.0f;
        p = o - (float)Math.floor(o);
        q = brightness * (1.0f - g);
        float r = brightness * (1.0f - g * p);
        float s = brightness * (1.0f - g * (1.0f - p));
        switch ((int)o) {
            case 0: {
                i = Math.round(brightness * 255.0f);
                j = Math.round(s * 255.0f);
                k = Math.round(q * 255.0f);
                break;
            }
            case 1: {
                i = Math.round(r * 255.0f);
                j = Math.round(brightness * 255.0f);
                k = Math.round(q * 255.0f);
                break;
            }
            case 2: {
                i = Math.round(q * 255.0f);
                j = Math.round(brightness * 255.0f);
                k = Math.round(s * 255.0f);
                break;
            }
            case 3: {
                i = Math.round(q * 255.0f);
                j = Math.round(r * 255.0f);
                k = Math.round(brightness * 255.0f);
                break;
            }
            case 4: {
                i = Math.round(s * 255.0f);
                j = Math.round(q * 255.0f);
                k = Math.round(brightness * 255.0f);
                break;
            }
            case 5: {
                i = Math.round(brightness * 255.0f);
                j = Math.round(q * 255.0f);
                k = Math.round(r * 255.0f);
            }
        }
        return ColorHelper.getArgb(l, i, j, k);
    }
}


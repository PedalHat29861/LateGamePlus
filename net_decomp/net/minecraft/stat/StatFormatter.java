/*
 * Decompiled with CFR 0.152.
 */
package net.minecraft.stat;

import java.text.DecimalFormat;
import java.text.DecimalFormatSymbols;
import java.text.NumberFormat;
import java.util.Locale;

public interface StatFormatter {
    public static final DecimalFormat DECIMAL_FORMAT = new DecimalFormat("########0.00", DecimalFormatSymbols.getInstance(Locale.ROOT));
    public static final StatFormatter DEFAULT = NumberFormat.getIntegerInstance(Locale.US)::format;
    public static final StatFormatter DIVIDE_BY_TEN = i -> DECIMAL_FORMAT.format((double)i * 0.1);
    public static final StatFormatter DISTANCE = cm -> {
        double d = (double)cm / 100.0;
        double e = d / 1000.0;
        if (e > 0.5) {
            return DECIMAL_FORMAT.format(e) + " km";
        }
        if (d > 0.5) {
            return DECIMAL_FORMAT.format(d) + " m";
        }
        return cm + " cm";
    };
    public static final StatFormatter TIME = ticks -> {
        double d = (double)ticks / 20.0;
        double e = d / 60.0;
        double f = e / 60.0;
        double g = f / 24.0;
        double h = g / 365.0;
        if (h > 0.5) {
            return DECIMAL_FORMAT.format(h) + " y";
        }
        if (g > 0.5) {
            return DECIMAL_FORMAT.format(g) + " d";
        }
        if (f > 0.5) {
            return DECIMAL_FORMAT.format(f) + " h";
        }
        if (e > 0.5) {
            return DECIMAL_FORMAT.format(e) + " min";
        }
        return d + " s";
    };

    public String format(int var1);
}


/*
 * Decompiled with CFR 0.152.
 */
package net.minecraft.world.biome;

import net.minecraft.world.biome.BiomeColors;

public class DryFoliageColors {
    public static final int DEFAULT = -10732494;
    private static int[] colorMap = new int[65536];

    public static void setColorMap(int[] pixels) {
        colorMap = pixels;
    }

    public static int getColor(double temperature, double downfall) {
        return BiomeColors.getColor(temperature, downfall, colorMap, -10732494);
    }
}


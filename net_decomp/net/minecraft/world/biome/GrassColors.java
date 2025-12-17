/*
 * Decompiled with CFR 0.152.
 */
package net.minecraft.world.biome;

import net.minecraft.world.biome.BiomeColors;

public class GrassColors {
    private static int[] colorMap = new int[65536];

    public static void setColorMap(int[] map) {
        colorMap = map;
    }

    public static int getColor(double temperature, double downfall) {
        return BiomeColors.getColor(temperature, downfall, colorMap, -65281);
    }

    public static int getDefaultColor() {
        return GrassColors.getColor(0.5, 1.0);
    }
}


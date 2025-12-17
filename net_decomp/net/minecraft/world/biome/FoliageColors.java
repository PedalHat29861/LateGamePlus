/*
 * Decompiled with CFR 0.152.
 */
package net.minecraft.world.biome;

import net.minecraft.world.biome.BiomeColors;

public class FoliageColors {
    public static final int SPRUCE = -10380959;
    public static final int BIRCH = -8345771;
    public static final int DEFAULT = -12012264;
    public static final int MANGROVE = -7158200;
    private static int[] colorMap = new int[65536];

    public static void setColorMap(int[] pixels) {
        colorMap = pixels;
    }

    public static int getColor(double temperature, double downfall) {
        return BiomeColors.getColor(temperature, downfall, colorMap, -12012264);
    }
}


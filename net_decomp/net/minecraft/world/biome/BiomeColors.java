/*
 * Decompiled with CFR 0.152.
 */
package net.minecraft.world.biome;

public interface BiomeColors {
    public static int getColor(double temperature, double downfall, int[] colormap, int fallback) {
        int j = (int)((1.0 - (downfall *= temperature)) * 255.0);
        int i = (int)((1.0 - temperature) * 255.0);
        int k = j << 8 | i;
        if (k >= colormap.length) {
            return fallback;
        }
        return colormap[k];
    }
}


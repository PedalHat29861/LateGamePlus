/*
 * Decompiled with CFR 0.152.
 */
package net.minecraft.world.border;

import net.minecraft.world.border.WorldBorder;

public interface WorldBorderListener {
    public void onSizeChange(WorldBorder var1, double var2);

    public void onInterpolateSize(WorldBorder var1, double var2, double var4, long var6, long var8);

    public void onCenterChanged(WorldBorder var1, double var2, double var4);

    public void onWarningTimeChanged(WorldBorder var1, int var2);

    public void onWarningBlocksChanged(WorldBorder var1, int var2);

    public void onDamagePerBlockChanged(WorldBorder var1, double var2);

    public void onSafeZoneChanged(WorldBorder var1, double var2);
}


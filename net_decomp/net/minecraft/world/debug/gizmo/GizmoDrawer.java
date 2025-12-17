/*
 * Decompiled with CFR 0.152.
 */
package net.minecraft.world.debug.gizmo;

import net.minecraft.util.math.Vec3d;
import net.minecraft.world.debug.gizmo.TextGizmo;

public interface GizmoDrawer {
    public void addPoint(Vec3d var1, int var2, float var3);

    public void addLine(Vec3d var1, Vec3d var2, int var3, float var4);

    public void addPolygon(Vec3d[] var1, int var2);

    public void addQuad(Vec3d var1, Vec3d var2, Vec3d var3, Vec3d var4, int var5);

    public void addText(Vec3d var1, String var2, TextGizmo.Style var3);
}


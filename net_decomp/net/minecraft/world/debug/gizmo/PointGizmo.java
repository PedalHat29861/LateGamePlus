/*
 * Decompiled with CFR 0.152.
 */
package net.minecraft.world.debug.gizmo;

import net.minecraft.util.math.ColorHelper;
import net.minecraft.util.math.Vec3d;
import net.minecraft.world.debug.gizmo.Gizmo;
import net.minecraft.world.debug.gizmo.GizmoDrawer;

public record PointGizmo(Vec3d pos, int color, float size) implements Gizmo
{
    @Override
    public void draw(GizmoDrawer consumer, float opacity) {
        consumer.addPoint(this.pos, ColorHelper.scaleAlpha(this.color, opacity), this.size);
    }
}


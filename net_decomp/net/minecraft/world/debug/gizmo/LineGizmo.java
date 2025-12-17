/*
 * Decompiled with CFR 0.152.
 */
package net.minecraft.world.debug.gizmo;

import net.minecraft.util.math.ColorHelper;
import net.minecraft.util.math.Vec3d;
import net.minecraft.world.debug.gizmo.Gizmo;
import net.minecraft.world.debug.gizmo.GizmoDrawer;

public record LineGizmo(Vec3d start, Vec3d end, int color, float width) implements Gizmo
{
    public static final float field_63659 = 3.0f;

    @Override
    public void draw(GizmoDrawer consumer, float opacity) {
        consumer.addLine(this.start, this.end, ColorHelper.scaleAlpha(this.color, opacity), this.width);
    }
}


/*
 * Decompiled with CFR 0.152.
 */
package net.minecraft.world.debug.gizmo;

import net.minecraft.client.render.DrawStyle;
import net.minecraft.util.math.Box;
import net.minecraft.util.math.ColorHelper;
import net.minecraft.util.math.Vec3d;
import net.minecraft.world.debug.gizmo.Gizmo;
import net.minecraft.world.debug.gizmo.GizmoDrawer;

public record BoxGizmo(Box aabb, DrawStyle style, boolean coloredCornerStroke) implements Gizmo
{
    @Override
    public void draw(GizmoDrawer consumer, float opacity) {
        int j;
        double d = this.aabb.minX;
        double e = this.aabb.minY;
        double f = this.aabb.minZ;
        double g = this.aabb.maxX;
        double h = this.aabb.maxY;
        double i = this.aabb.maxZ;
        if (this.style.hasFill()) {
            j = this.style.fill(opacity);
            consumer.addQuad(new Vec3d(g, e, f), new Vec3d(g, h, f), new Vec3d(g, h, i), new Vec3d(g, e, i), j);
            consumer.addQuad(new Vec3d(d, e, f), new Vec3d(d, e, i), new Vec3d(d, h, i), new Vec3d(d, h, f), j);
            consumer.addQuad(new Vec3d(d, e, f), new Vec3d(d, h, f), new Vec3d(g, h, f), new Vec3d(g, e, f), j);
            consumer.addQuad(new Vec3d(d, e, i), new Vec3d(g, e, i), new Vec3d(g, h, i), new Vec3d(d, h, i), j);
            consumer.addQuad(new Vec3d(d, h, f), new Vec3d(d, h, i), new Vec3d(g, h, i), new Vec3d(g, h, f), j);
            consumer.addQuad(new Vec3d(d, e, f), new Vec3d(g, e, f), new Vec3d(g, e, i), new Vec3d(d, e, i), j);
        }
        if (this.style.hasStroke()) {
            j = this.style.stroke(opacity);
            consumer.addLine(new Vec3d(d, e, f), new Vec3d(g, e, f), this.coloredCornerStroke ? ColorHelper.mix(j, -34953) : j, this.style.strokeWidth());
            consumer.addLine(new Vec3d(d, e, f), new Vec3d(d, h, f), this.coloredCornerStroke ? ColorHelper.mix(j, -8913033) : j, this.style.strokeWidth());
            consumer.addLine(new Vec3d(d, e, f), new Vec3d(d, e, i), this.coloredCornerStroke ? ColorHelper.mix(j, -8947713) : j, this.style.strokeWidth());
            consumer.addLine(new Vec3d(g, e, f), new Vec3d(g, h, f), j, this.style.strokeWidth());
            consumer.addLine(new Vec3d(g, h, f), new Vec3d(d, h, f), j, this.style.strokeWidth());
            consumer.addLine(new Vec3d(d, h, f), new Vec3d(d, h, i), j, this.style.strokeWidth());
            consumer.addLine(new Vec3d(d, h, i), new Vec3d(d, e, i), j, this.style.strokeWidth());
            consumer.addLine(new Vec3d(d, e, i), new Vec3d(g, e, i), j, this.style.strokeWidth());
            consumer.addLine(new Vec3d(g, e, i), new Vec3d(g, e, f), j, this.style.strokeWidth());
            consumer.addLine(new Vec3d(d, h, i), new Vec3d(g, h, i), j, this.style.strokeWidth());
            consumer.addLine(new Vec3d(g, e, i), new Vec3d(g, h, i), j, this.style.strokeWidth());
            consumer.addLine(new Vec3d(g, h, f), new Vec3d(g, h, i), j, this.style.strokeWidth());
        }
    }
}


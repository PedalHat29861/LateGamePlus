/*
 * Decompiled with CFR 0.152.
 */
package net.minecraft.world.debug.gizmo;

import net.minecraft.client.render.DrawStyle;
import net.minecraft.util.math.Vec3d;
import net.minecraft.world.debug.gizmo.Gizmo;
import net.minecraft.world.debug.gizmo.GizmoDrawer;

public record CircleGizmo(Vec3d pos, float radius, DrawStyle style) implements Gizmo
{
    private static final int NUM_VERTICES = 20;
    private static final float ANGLE_INTERVAL = 0.31415927f;

    @Override
    public void draw(GizmoDrawer consumer, float opacity) {
        int i;
        if (!this.style.hasStroke() && !this.style.hasFill()) {
            return;
        }
        Vec3d[] vec3ds = new Vec3d[21];
        for (i = 0; i < 20; ++i) {
            Vec3d vec3d;
            float f = (float)i * 0.31415927f;
            vec3ds[i] = vec3d = this.pos.add((float)((double)this.radius * Math.cos(f)), 0.0, (float)((double)this.radius * Math.sin(f)));
        }
        vec3ds[20] = vec3ds[0];
        if (this.style.hasFill()) {
            i = this.style.fill(opacity);
            consumer.addPolygon(vec3ds, i);
        }
        if (this.style.hasStroke()) {
            i = this.style.stroke(opacity);
            for (int j = 0; j < 20; ++j) {
                consumer.addLine(vec3ds[j], vec3ds[j + 1], i, this.style.strokeWidth());
            }
        }
    }
}


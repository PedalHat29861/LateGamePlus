/*
 * Decompiled with CFR 0.152.
 */
package net.minecraft.world.debug.gizmo;

import java.lang.invoke.MethodHandle;
import java.lang.runtime.ObjectMethods;
import java.util.OptionalDouble;
import net.minecraft.util.math.ColorHelper;
import net.minecraft.util.math.Vec3d;
import net.minecraft.world.debug.gizmo.Gizmo;
import net.minecraft.world.debug.gizmo.GizmoDrawer;

public record TextGizmo(Vec3d pos, String text, Style style) implements Gizmo
{
    @Override
    public void draw(GizmoDrawer consumer, float opacity) {
        Style style = opacity < 1.0f ? new Style(ColorHelper.scaleAlpha(this.style.color, opacity), this.style.scale, this.style.adjustLeft) : this.style;
        consumer.addText(this.pos, this.text, style);
    }

    public static final class Style
    extends Record {
        final int color;
        final float scale;
        final OptionalDouble adjustLeft;
        public static final float DEFAULT_SCALE = 0.32f;

        public Style(int color, float scale, OptionalDouble adjustLeft) {
            this.color = color;
            this.scale = scale;
            this.adjustLeft = adjustLeft;
        }

        public static Style left() {
            return new Style(-1, 0.32f, OptionalDouble.empty());
        }

        public static Style left(int color) {
            return new Style(color, 0.32f, OptionalDouble.empty());
        }

        public static Style centered(int color) {
            return new Style(color, 0.32f, OptionalDouble.of(0.0));
        }

        public Style scaled(float scale) {
            return new Style(this.color, scale, this.adjustLeft);
        }

        public Style adjusted(float adjustLeft) {
            return new Style(this.color, this.scale, OptionalDouble.of(adjustLeft));
        }

        @Override
        public final String toString() {
            return ObjectMethods.bootstrap("toString", new MethodHandle[]{Style.class, "color;scale;adjustLeft", "color", "scale", "adjustLeft"}, this);
        }

        @Override
        public final int hashCode() {
            return (int)ObjectMethods.bootstrap("hashCode", new MethodHandle[]{Style.class, "color;scale;adjustLeft", "color", "scale", "adjustLeft"}, this);
        }

        @Override
        public final boolean equals(Object object) {
            return (boolean)ObjectMethods.bootstrap("equals", new MethodHandle[]{Style.class, "color;scale;adjustLeft", "color", "scale", "adjustLeft"}, this, object);
        }

        public int color() {
            return this.color;
        }

        public float scale() {
            return this.scale;
        }

        public OptionalDouble adjustLeft() {
            return this.adjustLeft;
        }
    }
}


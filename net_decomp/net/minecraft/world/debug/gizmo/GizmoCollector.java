/*
 * Decompiled with CFR 0.152.
 */
package net.minecraft.world.debug.gizmo;

import net.minecraft.world.debug.gizmo.Gizmo;
import net.minecraft.world.debug.gizmo.VisibilityConfigurable;

public interface GizmoCollector {
    public static final VisibilityConfigurable NOOP_CONFIGURABLE = new VisibilityConfigurable(){

        @Override
        public VisibilityConfigurable ignoreOcclusion() {
            return this;
        }

        @Override
        public VisibilityConfigurable withLifespan(int lifespan) {
            return this;
        }

        @Override
        public VisibilityConfigurable fadeOut() {
            return this;
        }
    };
    public static final GizmoCollector EMPTY = gizmo -> NOOP_CONFIGURABLE;

    public VisibilityConfigurable collect(Gizmo var1);
}


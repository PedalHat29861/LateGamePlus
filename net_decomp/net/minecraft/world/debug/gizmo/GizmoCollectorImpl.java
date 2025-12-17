/*
 * Decompiled with CFR 0.152.
 */
package net.minecraft.world.debug.gizmo;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import net.minecraft.util.Util;
import net.minecraft.util.math.MathHelper;
import net.minecraft.world.debug.gizmo.Gizmo;
import net.minecraft.world.debug.gizmo.GizmoCollector;
import net.minecraft.world.debug.gizmo.VisibilityConfigurable;

public class GizmoCollectorImpl
implements GizmoCollector {
    private final List<Entry> gizmos = new ArrayList<Entry>();
    private final List<Entry> pendingGizmos = new ArrayList<Entry>();

    @Override
    public VisibilityConfigurable collect(Gizmo gizmo) {
        Entry entry = new Entry(gizmo);
        this.gizmos.add(entry);
        return entry;
    }

    public List<Entry> extractGizmos() {
        ArrayList<Entry> arrayList = new ArrayList<Entry>(this.gizmos);
        arrayList.addAll(this.pendingGizmos);
        long l = Util.getMeasuringTimeMs();
        this.gizmos.removeIf(entry -> entry.getRemovalTime() < l);
        this.pendingGizmos.clear();
        return arrayList;
    }

    public List<Entry> getGizmos() {
        return this.gizmos;
    }

    public void add(Collection<Entry> gizmos) {
        this.pendingGizmos.addAll(gizmos);
    }

    public static class Entry
    implements VisibilityConfigurable {
        private final Gizmo gizmo;
        private boolean ignoreOcclusion;
        private long creationTime;
        private long removalTime;
        private boolean fadeOut;

        Entry(Gizmo gizmo) {
            this.gizmo = gizmo;
        }

        @Override
        public VisibilityConfigurable ignoreOcclusion() {
            this.ignoreOcclusion = true;
            return this;
        }

        @Override
        public VisibilityConfigurable withLifespan(int lifespan) {
            this.creationTime = Util.getMeasuringTimeMs();
            this.removalTime = this.creationTime + (long)lifespan;
            return this;
        }

        @Override
        public VisibilityConfigurable fadeOut() {
            this.fadeOut = true;
            return this;
        }

        public float getOpacity(long time) {
            if (this.fadeOut) {
                long l = this.removalTime - this.creationTime;
                long m = time - this.creationTime;
                return 1.0f - MathHelper.clamp((float)m / (float)l, 0.0f, 1.0f);
            }
            return 1.0f;
        }

        public boolean ignoresOcclusion() {
            return this.ignoreOcclusion;
        }

        public long getRemovalTime() {
            return this.removalTime;
        }

        public Gizmo getGizmo() {
            return this.gizmo;
        }
    }
}


/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  org.jspecify.annotations.Nullable
 */
package net.minecraft.util.profiler;

import java.util.function.Supplier;
import net.minecraft.util.profiler.Profiler;
import org.jspecify.annotations.Nullable;

public class ScopedProfiler
implements AutoCloseable {
    public static final ScopedProfiler DUMMY = new ScopedProfiler(null);
    private final @Nullable Profiler wrapped;

    ScopedProfiler(@Nullable Profiler wrapped) {
        this.wrapped = wrapped;
    }

    public ScopedProfiler addLabel(String label) {
        if (this.wrapped != null) {
            this.wrapped.addZoneText(label);
        }
        return this;
    }

    public ScopedProfiler addLabel(Supplier<String> labelSupplier) {
        if (this.wrapped != null) {
            this.wrapped.addZoneText(labelSupplier.get());
        }
        return this;
    }

    public ScopedProfiler addValue(long value) {
        if (this.wrapped != null) {
            this.wrapped.addZoneValue(value);
        }
        return this;
    }

    public ScopedProfiler setColor(int color) {
        if (this.wrapped != null) {
            this.wrapped.setZoneColor(color);
        }
        return this;
    }

    @Override
    public void close() {
        if (this.wrapped != null) {
            this.wrapped.pop();
        }
    }
}


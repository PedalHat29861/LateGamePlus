/*
 * Decompiled with CFR 0.152.
 */
package net.minecraft.util.profiler;

import java.util.function.BooleanSupplier;
import java.util.function.IntSupplier;
import java.util.function.LongSupplier;
import net.minecraft.util.profiler.DummyProfiler;
import net.minecraft.util.profiler.ProfileResult;
import net.minecraft.util.profiler.Profiler;
import net.minecraft.util.profiler.ProfilerSystem;
import net.minecraft.util.profiler.ReadableProfiler;

public class TickTimeTracker {
    private final LongSupplier timeGetter;
    private final IntSupplier tickGetter;
    private final BooleanSupplier timeoutDisabled;
    private ReadableProfiler profiler = DummyProfiler.INSTANCE;

    public TickTimeTracker(LongSupplier timeGetter, IntSupplier tickGetter, BooleanSupplier timeoutDisabled) {
        this.timeGetter = timeGetter;
        this.tickGetter = tickGetter;
        this.timeoutDisabled = timeoutDisabled;
    }

    public boolean isActive() {
        return this.profiler != DummyProfiler.INSTANCE;
    }

    public void disable() {
        this.profiler = DummyProfiler.INSTANCE;
    }

    public void enable() {
        this.profiler = new ProfilerSystem(this.timeGetter, this.tickGetter, this.timeoutDisabled);
    }

    public Profiler getProfiler() {
        return this.profiler;
    }

    public ProfileResult getResult() {
        return this.profiler.getResult();
    }
}


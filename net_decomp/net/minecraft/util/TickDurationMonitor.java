/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  com.mojang.logging.LogUtils
 *  org.jspecify.annotations.Nullable
 *  org.slf4j.Logger
 */
package net.minecraft.util;

import com.mojang.logging.LogUtils;
import java.io.File;
import java.util.function.LongSupplier;
import net.minecraft.SharedConstants;
import net.minecraft.util.Util;
import net.minecraft.util.profiler.DummyProfiler;
import net.minecraft.util.profiler.ProfileResult;
import net.minecraft.util.profiler.Profiler;
import net.minecraft.util.profiler.ProfilerSystem;
import net.minecraft.util.profiler.ReadableProfiler;
import org.jspecify.annotations.Nullable;
import org.slf4j.Logger;

public class TickDurationMonitor {
    private static final Logger LOGGER = LogUtils.getLogger();
    private final LongSupplier timeGetter;
    private final long overtime;
    private int tickCount;
    private final File tickResultsDirectory;
    private ReadableProfiler profiler = DummyProfiler.INSTANCE;

    public TickDurationMonitor(LongSupplier timeGetter, String filename, long overtime) {
        this.timeGetter = timeGetter;
        this.tickResultsDirectory = new File("debug", filename);
        this.overtime = overtime;
    }

    public Profiler nextProfiler() {
        this.profiler = new ProfilerSystem(this.timeGetter, () -> this.tickCount, () -> true);
        ++this.tickCount;
        return this.profiler;
    }

    public void endTick() {
        if (this.profiler == DummyProfiler.INSTANCE) {
            return;
        }
        ProfileResult profileResult = this.profiler.getResult();
        this.profiler = DummyProfiler.INSTANCE;
        if (profileResult.getTimeSpan() >= this.overtime) {
            File file = new File(this.tickResultsDirectory, "tick-results-" + Util.getFormattedCurrentTime() + ".txt");
            profileResult.save(file.toPath());
            LOGGER.info("Recorded long tick -- wrote info to: {}", (Object)file.getAbsolutePath());
        }
    }

    public static @Nullable TickDurationMonitor create(String name) {
        if (SharedConstants.MONITOR_TICK_TIMES) {
            return new TickDurationMonitor(Util.nanoTimeSupplier, name, SharedConstants.field_22251);
        }
        return null;
    }

    public static Profiler tickProfiler(Profiler profiler, @Nullable TickDurationMonitor monitor) {
        if (monitor != null) {
            return Profiler.union(monitor.nextProfiler(), profiler);
        }
        return profiler;
    }
}


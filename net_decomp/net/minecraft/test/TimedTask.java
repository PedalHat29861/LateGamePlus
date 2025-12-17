/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  org.jspecify.annotations.Nullable
 */
package net.minecraft.test;

import org.jspecify.annotations.Nullable;

class TimedTask {
    public final @Nullable Long duration;
    public final Runnable task;

    private TimedTask(@Nullable Long duration, Runnable task) {
        this.duration = duration;
        this.task = task;
    }

    static TimedTask create(Runnable task) {
        return new TimedTask(null, task);
    }

    static TimedTask create(long duration, Runnable task) {
        return new TimedTask(duration, task);
    }
}


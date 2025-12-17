/*
 * Decompiled with CFR 0.152.
 */
package net.minecraft.util.profiler;

import net.minecraft.util.profiler.Profiler;

public interface Recorder {
    public void stop();

    public void forceStop();

    public void startTick();

    public boolean isActive();

    public Profiler getProfiler();

    public void endTick();
}


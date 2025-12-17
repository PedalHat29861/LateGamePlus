/*
 * Decompiled with CFR 0.152.
 */
package net.minecraft.util.thread;

import java.util.List;
import net.minecraft.util.profiler.Sampler;

public interface SampleableExecutor {
    public List<Sampler> createSamplers();
}


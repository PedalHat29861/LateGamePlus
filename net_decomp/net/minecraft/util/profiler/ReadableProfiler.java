/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  org.apache.commons.lang3.tuple.Pair
 *  org.jspecify.annotations.Nullable
 */
package net.minecraft.util.profiler;

import java.util.Set;
import net.minecraft.util.profiler.ProfileResult;
import net.minecraft.util.profiler.Profiler;
import net.minecraft.util.profiler.SampleType;
import org.apache.commons.lang3.tuple.Pair;
import org.jspecify.annotations.Nullable;

public interface ReadableProfiler
extends Profiler {
    public ProfileResult getResult();

    public  @Nullable ProfilerSystem.LocatedInfo getInfo(String var1);

    public Set<Pair<String, SampleType>> getSampleTargets();
}


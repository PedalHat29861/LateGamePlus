/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  com.google.common.collect.ImmutableSet
 *  org.apache.commons.lang3.tuple.Pair
 *  org.jspecify.annotations.Nullable
 */
package net.minecraft.util.profiler;

import com.google.common.collect.ImmutableSet;
import java.util.Set;
import java.util.function.Supplier;
import net.minecraft.util.profiler.EmptyProfileResult;
import net.minecraft.util.profiler.ProfileResult;
import net.minecraft.util.profiler.ReadableProfiler;
import net.minecraft.util.profiler.SampleType;
import net.minecraft.util.profiler.ScopedProfiler;
import org.apache.commons.lang3.tuple.Pair;
import org.jspecify.annotations.Nullable;

public class DummyProfiler
implements ReadableProfiler {
    public static final DummyProfiler INSTANCE = new DummyProfiler();

    private DummyProfiler() {
    }

    @Override
    public void startTick() {
    }

    @Override
    public void endTick() {
    }

    @Override
    public void push(String location) {
    }

    @Override
    public void push(Supplier<String> locationGetter) {
    }

    @Override
    public void markSampleType(SampleType type) {
    }

    @Override
    public void pop() {
    }

    @Override
    public void swap(String location) {
    }

    @Override
    public void swap(Supplier<String> locationGetter) {
    }

    @Override
    public ScopedProfiler scoped(String name) {
        return ScopedProfiler.DUMMY;
    }

    @Override
    public ScopedProfiler scoped(Supplier<String> nameSupplier) {
        return ScopedProfiler.DUMMY;
    }

    @Override
    public void visit(String marker, int num) {
    }

    @Override
    public void visit(Supplier<String> markerGetter, int num) {
    }

    @Override
    public ProfileResult getResult() {
        return EmptyProfileResult.INSTANCE;
    }

    @Override
    public  @Nullable ProfilerSystem.LocatedInfo getInfo(String name) {
        return null;
    }

    @Override
    public Set<Pair<String, SampleType>> getSampleTargets() {
        return ImmutableSet.of();
    }
}


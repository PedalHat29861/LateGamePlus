/*
 * Decompiled with CFR 0.152.
 */
package net.minecraft.util.profiler;

import java.util.function.Supplier;
import net.minecraft.util.profiler.DummyProfiler;
import net.minecraft.util.profiler.SampleType;
import net.minecraft.util.profiler.ScopedProfiler;

public interface Profiler {
    public static final String ROOT_NAME = "root";

    public void startTick();

    public void endTick();

    public void push(String var1);

    public void push(Supplier<String> var1);

    public void pop();

    public void swap(String var1);

    public void swap(Supplier<String> var1);

    default public void addZoneText(String label) {
    }

    default public void addZoneValue(long value) {
    }

    default public void setZoneColor(int color) {
    }

    default public ScopedProfiler scoped(String name) {
        this.push(name);
        return new ScopedProfiler(this);
    }

    default public ScopedProfiler scoped(Supplier<String> nameSupplier) {
        this.push(nameSupplier);
        return new ScopedProfiler(this);
    }

    public void markSampleType(SampleType var1);

    default public void visit(String marker) {
        this.visit(marker, 1);
    }

    public void visit(String var1, int var2);

    default public void visit(Supplier<String> markerGetter) {
        this.visit(markerGetter, 1);
    }

    public void visit(Supplier<String> var1, int var2);

    public static Profiler union(Profiler first, Profiler second) {
        if (first == DummyProfiler.INSTANCE) {
            return second;
        }
        if (second == DummyProfiler.INSTANCE) {
            return first;
        }
        return new UnionProfiler(first, second);
    }

    public static class UnionProfiler
    implements Profiler {
        private final Profiler first;
        private final Profiler second;

        public UnionProfiler(Profiler first, Profiler second) {
            this.first = first;
            this.second = second;
        }

        @Override
        public void startTick() {
            this.first.startTick();
            this.second.startTick();
        }

        @Override
        public void endTick() {
            this.first.endTick();
            this.second.endTick();
        }

        @Override
        public void push(String location) {
            this.first.push(location);
            this.second.push(location);
        }

        @Override
        public void push(Supplier<String> locationGetter) {
            this.first.push(locationGetter);
            this.second.push(locationGetter);
        }

        @Override
        public void markSampleType(SampleType type) {
            this.first.markSampleType(type);
            this.second.markSampleType(type);
        }

        @Override
        public void pop() {
            this.first.pop();
            this.second.pop();
        }

        @Override
        public void swap(String location) {
            this.first.swap(location);
            this.second.swap(location);
        }

        @Override
        public void swap(Supplier<String> locationGetter) {
            this.first.swap(locationGetter);
            this.second.swap(locationGetter);
        }

        @Override
        public void visit(String marker, int num) {
            this.first.visit(marker, num);
            this.second.visit(marker, num);
        }

        @Override
        public void visit(Supplier<String> markerGetter, int num) {
            this.first.visit(markerGetter, num);
            this.second.visit(markerGetter, num);
        }

        @Override
        public void addZoneText(String label) {
            this.first.addZoneText(label);
            this.second.addZoneText(label);
        }

        @Override
        public void addZoneValue(long value) {
            this.first.addZoneValue(value);
            this.second.addZoneValue(value);
        }

        @Override
        public void setZoneColor(int color) {
            this.first.setZoneColor(color);
            this.second.setZoneColor(color);
        }
    }
}


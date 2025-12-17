/*
 * Decompiled with CFR 0.152.
 */
package net.minecraft.util.profiling.jfr.sample;

import java.lang.invoke.MethodHandle;
import java.lang.runtime.ObjectMethods;
import java.time.Duration;
import java.time.Instant;
import jdk.jfr.consumer.RecordedEvent;

public record ServerTickTimeSample(Instant time, Duration averageTickMs) {
    public static ServerTickTimeSample fromEvent(RecordedEvent event) {
        return new ServerTickTimeSample(event.getStartTime(), event.getDuration("averageTickDuration"));
    }

    @Override
    public final String toString() {
        return ObjectMethods.bootstrap("toString", new MethodHandle[]{ServerTickTimeSample.class, "timestamp;currentAverage", "time", "averageTickMs"}, this);
    }

    @Override
    public final int hashCode() {
        return (int)ObjectMethods.bootstrap("hashCode", new MethodHandle[]{ServerTickTimeSample.class, "timestamp;currentAverage", "time", "averageTickMs"}, this);
    }

    @Override
    public final boolean equals(Object o) {
        return (boolean)ObjectMethods.bootstrap("equals", new MethodHandle[]{ServerTickTimeSample.class, "timestamp;currentAverage", "time", "averageTickMs"}, this, o);
    }
}


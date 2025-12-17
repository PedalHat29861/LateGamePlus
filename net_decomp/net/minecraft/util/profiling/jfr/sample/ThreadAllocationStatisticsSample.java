/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  com.google.common.base.MoreObjects
 */
package net.minecraft.util.profiling.jfr.sample;

import com.google.common.base.MoreObjects;
import java.lang.invoke.MethodHandle;
import java.lang.runtime.ObjectMethods;
import java.time.Duration;
import java.time.Instant;
import java.util.List;
import java.util.Map;
import java.util.TreeMap;
import java.util.stream.Collectors;
import jdk.jfr.consumer.RecordedEvent;
import jdk.jfr.consumer.RecordedThread;

public record ThreadAllocationStatisticsSample(Instant time, String threadName, long allocated) {
    private static final String UNKNOWN = "unknown";

    public static ThreadAllocationStatisticsSample fromEvent(RecordedEvent event) {
        RecordedThread recordedThread = event.getThread("thread");
        String string = recordedThread == null ? UNKNOWN : (String)MoreObjects.firstNonNull((Object)recordedThread.getJavaName(), (Object)UNKNOWN);
        return new ThreadAllocationStatisticsSample(event.getStartTime(), string, event.getLong("allocated"));
    }

    public static AllocationMap toAllocationMap(List<ThreadAllocationStatisticsSample> samples) {
        TreeMap<String, Double> map = new TreeMap<String, Double>();
        Map<String, List<ThreadAllocationStatisticsSample>> map2 = samples.stream().collect(Collectors.groupingBy(sample -> sample.threadName));
        map2.forEach((threadName, groupedSamples) -> {
            if (groupedSamples.size() < 2) {
                return;
            }
            ThreadAllocationStatisticsSample threadAllocationStatisticsSample = (ThreadAllocationStatisticsSample)groupedSamples.get(0);
            ThreadAllocationStatisticsSample threadAllocationStatisticsSample2 = (ThreadAllocationStatisticsSample)groupedSamples.get(groupedSamples.size() - 1);
            long l = Duration.between(threadAllocationStatisticsSample.time, threadAllocationStatisticsSample2.time).getSeconds();
            long m = threadAllocationStatisticsSample2.allocated - threadAllocationStatisticsSample.allocated;
            map.put((String)threadName, (double)m / (double)l);
        });
        return new AllocationMap(map);
    }

    @Override
    public final String toString() {
        return ObjectMethods.bootstrap("toString", new MethodHandle[]{ThreadAllocationStatisticsSample.class, "timestamp;threadName;totalBytes", "time", "threadName", "allocated"}, this);
    }

    @Override
    public final int hashCode() {
        return (int)ObjectMethods.bootstrap("hashCode", new MethodHandle[]{ThreadAllocationStatisticsSample.class, "timestamp;threadName;totalBytes", "time", "threadName", "allocated"}, this);
    }

    @Override
    public final boolean equals(Object o) {
        return (boolean)ObjectMethods.bootstrap("equals", new MethodHandle[]{ThreadAllocationStatisticsSample.class, "timestamp;threadName;totalBytes", "time", "threadName", "allocated"}, this, o);
    }

    public record AllocationMap(Map<String, Double> allocations) {
        @Override
        public final String toString() {
            return ObjectMethods.bootstrap("toString", new MethodHandle[]{AllocationMap.class, "allocationsPerSecondByThread", "allocations"}, this);
        }

        @Override
        public final int hashCode() {
            return (int)ObjectMethods.bootstrap("hashCode", new MethodHandle[]{AllocationMap.class, "allocationsPerSecondByThread", "allocations"}, this);
        }

        @Override
        public final boolean equals(Object o) {
            return (boolean)ObjectMethods.bootstrap("equals", new MethodHandle[]{AllocationMap.class, "allocationsPerSecondByThread", "allocations"}, this, o);
        }
    }
}


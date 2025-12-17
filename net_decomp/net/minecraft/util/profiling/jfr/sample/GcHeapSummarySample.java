/*
 * Decompiled with CFR 0.152.
 */
package net.minecraft.util.profiling.jfr.sample;

import java.lang.invoke.MethodHandle;
import java.lang.runtime.ObjectMethods;
import java.time.Duration;
import java.time.Instant;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;
import jdk.jfr.consumer.RecordedEvent;

public record GcHeapSummarySample(Instant time, long heapUsed, SummaryType summaryType) {
    public static GcHeapSummarySample fromEvent(RecordedEvent event) {
        return new GcHeapSummarySample(event.getStartTime(), event.getLong("heapUsed"), event.getString("when").equalsIgnoreCase("before gc") ? SummaryType.BEFORE_GC : SummaryType.AFTER_GC);
    }

    public static Statistics toStatistics(Duration duration, List<GcHeapSummarySample> samples, Duration gcDuration, int count) {
        return new Statistics(duration, gcDuration, count, GcHeapSummarySample.getAllocatedBytesPerSecond(samples));
    }

    private static double getAllocatedBytesPerSecond(List<GcHeapSummarySample> samples) {
        long l = 0L;
        Map<SummaryType, List<GcHeapSummarySample>> map = samples.stream().collect(Collectors.groupingBy(gcHeapSummarySample -> gcHeapSummarySample.summaryType));
        List<GcHeapSummarySample> list = map.get((Object)SummaryType.BEFORE_GC);
        List<GcHeapSummarySample> list2 = map.get((Object)SummaryType.AFTER_GC);
        for (int i = 1; i < list.size(); ++i) {
            GcHeapSummarySample gcHeapSummarySample2 = list.get(i);
            GcHeapSummarySample gcHeapSummarySample22 = list2.get(i - 1);
            l += gcHeapSummarySample2.heapUsed - gcHeapSummarySample22.heapUsed;
        }
        Duration duration = Duration.between(samples.get((int)1).time, samples.get((int)(samples.size() - 1)).time);
        return (double)l / (double)duration.getSeconds();
    }

    @Override
    public final String toString() {
        return ObjectMethods.bootstrap("toString", new MethodHandle[]{GcHeapSummarySample.class, "timestamp;heapUsed;timing", "time", "heapUsed", "summaryType"}, this);
    }

    @Override
    public final int hashCode() {
        return (int)ObjectMethods.bootstrap("hashCode", new MethodHandle[]{GcHeapSummarySample.class, "timestamp;heapUsed;timing", "time", "heapUsed", "summaryType"}, this);
    }

    @Override
    public final boolean equals(Object o) {
        return (boolean)ObjectMethods.bootstrap("equals", new MethodHandle[]{GcHeapSummarySample.class, "timestamp;heapUsed;timing", "time", "heapUsed", "summaryType"}, this, o);
    }

    static final class SummaryType
    extends Enum<SummaryType> {
        public static final /* enum */ SummaryType BEFORE_GC = new SummaryType();
        public static final /* enum */ SummaryType AFTER_GC = new SummaryType();
        private static final /* synthetic */ SummaryType[] field_34445;

        public static SummaryType[] values() {
            return (SummaryType[])field_34445.clone();
        }

        public static SummaryType valueOf(String string) {
            return Enum.valueOf(SummaryType.class, string);
        }

        private static /* synthetic */ SummaryType[] method_38044() {
            return new SummaryType[]{BEFORE_GC, AFTER_GC};
        }

        static {
            field_34445 = SummaryType.method_38044();
        }
    }

    public record Statistics(Duration duration, Duration gcDuration, int count, double allocatedBytesPerSecond) {
        public float getGcDurationRatio() {
            return (float)this.gcDuration.toMillis() / (float)this.duration.toMillis();
        }

        @Override
        public final String toString() {
            return ObjectMethods.bootstrap("toString", new MethodHandle[]{Statistics.class, "duration;gcTotalDuration;totalGCs;allocationRateBytesPerSecond", "duration", "gcDuration", "count", "allocatedBytesPerSecond"}, this);
        }

        @Override
        public final int hashCode() {
            return (int)ObjectMethods.bootstrap("hashCode", new MethodHandle[]{Statistics.class, "duration;gcTotalDuration;totalGCs;allocationRateBytesPerSecond", "duration", "gcDuration", "count", "allocatedBytesPerSecond"}, this);
        }

        @Override
        public final boolean equals(Object o) {
            return (boolean)ObjectMethods.bootstrap("equals", new MethodHandle[]{Statistics.class, "duration;gcTotalDuration;totalGCs;allocationRateBytesPerSecond", "duration", "gcDuration", "count", "allocatedBytesPerSecond"}, this, o);
        }
    }
}


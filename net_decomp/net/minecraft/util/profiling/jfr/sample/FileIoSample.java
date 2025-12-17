/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  com.mojang.datafixers.util.Pair
 *  org.jspecify.annotations.Nullable
 */
package net.minecraft.util.profiling.jfr.sample;

import com.mojang.datafixers.util.Pair;
import java.lang.invoke.MethodHandle;
import java.lang.runtime.ObjectMethods;
import java.time.Duration;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;
import org.jspecify.annotations.Nullable;

public record FileIoSample(Duration duration, @Nullable String path, long bytes) {
    public static Statistics toStatistics(Duration duration, List<FileIoSample> samples) {
        long l = samples.stream().mapToLong(sample -> sample.bytes).sum();
        return new Statistics(l, (double)l / (double)duration.getSeconds(), samples.size(), (double)samples.size() / (double)duration.getSeconds(), samples.stream().map(FileIoSample::duration).reduce(Duration.ZERO, Duration::plus), samples.stream().filter(sample -> sample.path != null).collect(Collectors.groupingBy(sample -> sample.path, Collectors.summingLong(sample -> sample.bytes))).entrySet().stream().sorted(Map.Entry.comparingByValue().reversed()).map(entry -> Pair.of((Object)((String)entry.getKey()), (Object)((Long)entry.getValue()))).limit(10L).toList());
    }

    public record Statistics(long totalBytes, double bytesPerSecond, long count, double countPerSecond, Duration totalDuration, List<Pair<String, Long>> topContributors) {
        @Override
        public final String toString() {
            return ObjectMethods.bootstrap("toString", new MethodHandle[]{Statistics.class, "totalBytes;bytesPerSecond;counts;countsPerSecond;timeSpentInIO;topTenContributorsByTotalBytes", "totalBytes", "bytesPerSecond", "count", "countPerSecond", "totalDuration", "topContributors"}, this);
        }

        @Override
        public final int hashCode() {
            return (int)ObjectMethods.bootstrap("hashCode", new MethodHandle[]{Statistics.class, "totalBytes;bytesPerSecond;counts;countsPerSecond;timeSpentInIO;topTenContributorsByTotalBytes", "totalBytes", "bytesPerSecond", "count", "countPerSecond", "totalDuration", "topContributors"}, this);
        }

        @Override
        public final boolean equals(Object o) {
            return (boolean)ObjectMethods.bootstrap("equals", new MethodHandle[]{Statistics.class, "totalBytes;bytesPerSecond;counts;countsPerSecond;timeSpentInIO;topTenContributorsByTotalBytes", "totalBytes", "bytesPerSecond", "count", "countPerSecond", "totalDuration", "topContributors"}, this, o);
        }
    }
}


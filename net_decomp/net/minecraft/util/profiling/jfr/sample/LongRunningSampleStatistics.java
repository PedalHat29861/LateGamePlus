/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  org.jspecify.annotations.Nullable
 */
package net.minecraft.util.profiling.jfr.sample;

import java.lang.invoke.MethodHandle;
import java.lang.runtime.ObjectMethods;
import java.time.Duration;
import java.util.Comparator;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import net.minecraft.util.math.Quantiles;
import net.minecraft.util.profiling.jfr.sample.LongRunningSample;
import org.jspecify.annotations.Nullable;

public record LongRunningSampleStatistics<T extends LongRunningSample>(T fastestSample, T slowestSample, @Nullable T secondSlowestSample, int count, Map<Integer, Double> quantiles, Duration totalDuration) {
    public static <T extends LongRunningSample> Optional<LongRunningSampleStatistics<T>> fromSamples(List<T> samples) {
        if (samples.isEmpty()) {
            return Optional.empty();
        }
        List<LongRunningSample> list = samples.stream().sorted(Comparator.comparing(LongRunningSample::duration)).toList();
        Duration duration = list.stream().map(LongRunningSample::duration).reduce(Duration::plus).orElse(Duration.ZERO);
        LongRunningSample longRunningSample = list.getFirst();
        LongRunningSample longRunningSample2 = list.getLast();
        LongRunningSample longRunningSample3 = list.size() > 1 ? list.get(list.size() - 2) : null;
        int i = list.size();
        Map<Integer, Double> map = Quantiles.create(list.stream().mapToLong(sample -> sample.duration().toNanos()).toArray());
        return Optional.of(new LongRunningSampleStatistics<LongRunningSample>(longRunningSample, longRunningSample2, longRunningSample3, i, map, duration));
    }

    @Override
    public final String toString() {
        return ObjectMethods.bootstrap("toString", new MethodHandle[]{LongRunningSampleStatistics.class, "fastest;slowest;secondSlowest;count;percentilesNanos;totalDuration", "fastestSample", "slowestSample", "secondSlowestSample", "count", "quantiles", "totalDuration"}, this);
    }

    @Override
    public final int hashCode() {
        return (int)ObjectMethods.bootstrap("hashCode", new MethodHandle[]{LongRunningSampleStatistics.class, "fastest;slowest;secondSlowest;count;percentilesNanos;totalDuration", "fastestSample", "slowestSample", "secondSlowestSample", "count", "quantiles", "totalDuration"}, this);
    }

    @Override
    public final boolean equals(Object o) {
        return (boolean)ObjectMethods.bootstrap("equals", new MethodHandle[]{LongRunningSampleStatistics.class, "fastest;slowest;secondSlowest;count;percentilesNanos;totalDuration", "fastestSample", "slowestSample", "secondSlowestSample", "count", "quantiles", "totalDuration"}, this, o);
    }
}


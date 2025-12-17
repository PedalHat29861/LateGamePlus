/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  com.google.common.collect.ImmutableSet
 */
package net.minecraft.util;

import com.google.common.collect.ImmutableSet;
import java.util.Map;
import java.util.Set;
import java.util.function.Consumer;

public final class TopologicalSorts {
    private TopologicalSorts() {
    }

    public static <T> boolean sort(Map<T, Set<T>> successors, Set<T> visited, Set<T> visiting, Consumer<T> reversedOrderConsumer, T now) {
        if (visited.contains(now)) {
            return false;
        }
        if (visiting.contains(now)) {
            return true;
        }
        visiting.add(now);
        for (Object object : (Set)successors.getOrDefault(now, (Set<T>)ImmutableSet.of())) {
            if (!TopologicalSorts.sort(successors, visited, visiting, reversedOrderConsumer, object)) continue;
            return true;
        }
        visiting.remove(now);
        visited.add(now);
        reversedOrderConsumer.accept(now);
        return false;
    }
}


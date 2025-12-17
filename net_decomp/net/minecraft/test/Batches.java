/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  com.google.common.collect.Lists
 *  com.google.common.collect.Streams
 */
package net.minecraft.test;

import com.google.common.collect.Lists;
import com.google.common.collect.Streams;
import java.util.Collection;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.stream.Collectors;
import java.util.stream.Stream;
import net.minecraft.registry.entry.RegistryEntry;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.test.GameTestBatch;
import net.minecraft.test.GameTestState;
import net.minecraft.test.TestAttemptConfig;
import net.minecraft.test.TestEnvironmentDefinition;
import net.minecraft.test.TestInstance;
import net.minecraft.test.TestRunContext;
import net.minecraft.util.BlockRotation;

public class Batches {
    private static final int BATCH_SIZE = 50;
    public static final Decorator DEFAULT_DECORATOR = (instance, world) -> Stream.of(new GameTestState(instance, BlockRotation.NONE, world, TestAttemptConfig.once()));

    public static List<GameTestBatch> batch(Collection<RegistryEntry.Reference<TestInstance>> instances, Decorator decorator, ServerWorld world) {
        Map<RegistryEntry, List<GameTestState>> map = instances.stream().flatMap(instance -> decorator.decorate((RegistryEntry.Reference<TestInstance>)instance, world)).collect(Collectors.groupingBy(state -> state.getInstance().getEnvironment()));
        return map.entrySet().stream().flatMap(entry -> {
            RegistryEntry registryEntry = (RegistryEntry)entry.getKey();
            List list = (List)entry.getValue();
            return Streams.mapWithIndex(Lists.partition((List)list, (int)50).stream(), (states, index) -> Batches.create(states, registryEntry, (int)index));
        }).toList();
    }

    public static TestRunContext.Batcher defaultBatcher() {
        return Batches.batcher(50);
    }

    public static TestRunContext.Batcher batcher(int batchSize) {
        return states -> {
            Map<RegistryEntry, List<GameTestState>> map = states.stream().filter(Objects::nonNull).collect(Collectors.groupingBy(state -> state.getInstance().getEnvironment()));
            return map.entrySet().stream().flatMap(entry -> {
                RegistryEntry registryEntry = (RegistryEntry)entry.getKey();
                List list = (List)entry.getValue();
                return Streams.mapWithIndex(Lists.partition((List)list, (int)batchSize).stream(), (states, index) -> Batches.create(List.copyOf(states), registryEntry, (int)index));
            }).toList();
        };
    }

    public static GameTestBatch create(Collection<GameTestState> states, RegistryEntry<TestEnvironmentDefinition> environment, int index) {
        return new GameTestBatch(index, states, environment);
    }

    @FunctionalInterface
    public static interface Decorator {
        public Stream<GameTestState> decorate(RegistryEntry.Reference<TestInstance> var1, ServerWorld var2);
    }
}


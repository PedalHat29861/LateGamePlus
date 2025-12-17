/*
 * Decompiled with CFR 0.152.
 */
package net.minecraft.test;

import java.lang.invoke.MethodHandle;
import java.lang.runtime.ObjectMethods;
import java.util.Collection;
import net.minecraft.registry.entry.RegistryEntry;
import net.minecraft.test.GameTestState;
import net.minecraft.test.TestEnvironmentDefinition;

public record GameTestBatch(int index, Collection<GameTestState> states, RegistryEntry<TestEnvironmentDefinition> environment) {
    public GameTestBatch {
        if (testFunctions.isEmpty()) {
            throw new IllegalArgumentException("A GameTestBatch must include at least one GameTestInfo!");
        }
    }

    @Override
    public final String toString() {
        return ObjectMethods.bootstrap("toString", new MethodHandle[]{GameTestBatch.class, "index;gameTestInfos;environment", "index", "states", "environment"}, this);
    }

    @Override
    public final int hashCode() {
        return (int)ObjectMethods.bootstrap("hashCode", new MethodHandle[]{GameTestBatch.class, "index;gameTestInfos;environment", "index", "states", "environment"}, this);
    }

    @Override
    public final boolean equals(Object object) {
        return (boolean)ObjectMethods.bootstrap("equals", new MethodHandle[]{GameTestBatch.class, "index;gameTestInfos;environment", "index", "states", "environment"}, this, object);
    }
}


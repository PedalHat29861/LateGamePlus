/*
 * Decompiled with CFR 0.152.
 */
package net.minecraft.test;

import java.util.function.Consumer;
import net.minecraft.registry.Registerable;
import net.minecraft.registry.RegistryEntryLookup;
import net.minecraft.registry.RegistryKey;
import net.minecraft.registry.RegistryKeys;
import net.minecraft.registry.entry.RegistryEntry;
import net.minecraft.test.BuiltinTestFunctions;
import net.minecraft.test.FunctionTestInstance;
import net.minecraft.test.TestContext;
import net.minecraft.test.TestData;
import net.minecraft.test.TestEnvironmentDefinition;
import net.minecraft.test.TestEnvironments;
import net.minecraft.test.TestInstance;
import net.minecraft.util.Identifier;

public interface TestInstances {
    public static final RegistryKey<TestInstance> ALWAYS_PASS = TestInstances.of("always_pass");

    public static void bootstrap(Registerable<TestInstance> registry) {
        RegistryEntryLookup<Consumer<TestContext>> registryEntryLookup = registry.getRegistryLookup(RegistryKeys.TEST_FUNCTION);
        RegistryEntryLookup<TestEnvironmentDefinition> registryEntryLookup2 = registry.getRegistryLookup(RegistryKeys.TEST_ENVIRONMENT);
        registry.register(ALWAYS_PASS, new FunctionTestInstance(BuiltinTestFunctions.ALWAYS_PASS, new TestData<RegistryEntry<TestEnvironmentDefinition>>(registryEntryLookup2.getOrThrow(TestEnvironments.DEFAULT), Identifier.ofVanilla("empty"), 1, 1, false)));
    }

    private static RegistryKey<TestInstance> of(String id) {
        return RegistryKey.of(RegistryKeys.TEST_INSTANCE, Identifier.ofVanilla(id));
    }
}


/*
 * Decompiled with CFR 0.152.
 */
package net.minecraft.test;

import java.util.List;
import net.minecraft.registry.Registerable;
import net.minecraft.registry.RegistryKey;
import net.minecraft.registry.RegistryKeys;
import net.minecraft.test.TestEnvironmentDefinition;
import net.minecraft.util.Identifier;

public interface TestEnvironments {
    public static final String DEFAULT_ID = "default";
    public static final RegistryKey<TestEnvironmentDefinition> DEFAULT = TestEnvironments.of("default");

    private static RegistryKey<TestEnvironmentDefinition> of(String id) {
        return RegistryKey.of(RegistryKeys.TEST_ENVIRONMENT, Identifier.ofVanilla(id));
    }

    public static void bootstrap(Registerable<TestEnvironmentDefinition> registry) {
        registry.register(DEFAULT, new TestEnvironmentDefinition.AllOf(List.of()));
    }
}


/*
 * Decompiled with CFR 0.152.
 */
package net.minecraft.test;

import java.util.ArrayList;
import java.util.List;
import java.util.function.BiConsumer;
import java.util.function.Consumer;
import net.minecraft.registry.Registry;
import net.minecraft.registry.RegistryKey;
import net.minecraft.test.TestContext;

public abstract class TestFunctionProvider {
    private static final List<TestFunctionProvider> PROVIDERS = new ArrayList<TestFunctionProvider>();

    public static void addProvider(TestFunctionProvider provider) {
        PROVIDERS.add(provider);
    }

    public static void registerAll(Registry<Consumer<TestContext>> registry) {
        for (TestFunctionProvider testFunctionProvider : PROVIDERS) {
            testFunctionProvider.register((key, value) -> Registry.register(registry, key, value));
        }
    }

    public abstract void register(BiConsumer<RegistryKey<Consumer<TestContext>>, Consumer<TestContext>> var1);
}


/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  com.mojang.serialization.Codec
 *  com.mojang.serialization.MapCodec
 */
package net.minecraft.test;

import com.mojang.serialization.Codec;
import com.mojang.serialization.MapCodec;
import net.minecraft.registry.Registries;
import net.minecraft.registry.Registry;
import net.minecraft.registry.RegistryKey;
import net.minecraft.registry.RegistryKeys;
import net.minecraft.registry.entry.RegistryEntry;
import net.minecraft.test.BlockBasedTestInstance;
import net.minecraft.test.FunctionTestInstance;
import net.minecraft.test.TestContext;
import net.minecraft.test.TestData;
import net.minecraft.test.TestEnvironmentDefinition;
import net.minecraft.text.MutableText;
import net.minecraft.text.Text;
import net.minecraft.util.BlockRotation;
import net.minecraft.util.Formatting;
import net.minecraft.util.Identifier;

public abstract class TestInstance {
    public static final Codec<TestInstance> CODEC = Registries.TEST_INSTANCE_TYPE.getCodec().dispatch(TestInstance::getCodec, codec -> codec);
    private final TestData<RegistryEntry<TestEnvironmentDefinition>> data;

    public static MapCodec<? extends TestInstance> registerAndGetDefault(Registry<MapCodec<? extends TestInstance>> registry) {
        TestInstance.register(registry, "block_based", BlockBasedTestInstance.CODEC);
        return TestInstance.register(registry, "function", FunctionTestInstance.CODEC);
    }

    private static MapCodec<? extends TestInstance> register(Registry<MapCodec<? extends TestInstance>> registry, String id, MapCodec<? extends TestInstance> codec) {
        return Registry.register(registry, RegistryKey.of(RegistryKeys.TEST_INSTANCE_TYPE, Identifier.ofVanilla(id)), codec);
    }

    protected TestInstance(TestData<RegistryEntry<TestEnvironmentDefinition>> data) {
        this.data = data;
    }

    public abstract void start(TestContext var1);

    public abstract MapCodec<? extends TestInstance> getCodec();

    public RegistryEntry<TestEnvironmentDefinition> getEnvironment() {
        return this.data.environment();
    }

    public Identifier getStructure() {
        return this.data.structure();
    }

    public int getMaxTicks() {
        return this.data.maxTicks();
    }

    public int getSetupTicks() {
        return this.data.setupTicks();
    }

    public boolean isRequired() {
        return this.data.required();
    }

    public boolean isManualOnly() {
        return this.data.manualOnly();
    }

    public int getMaxAttempts() {
        return this.data.maxAttempts();
    }

    public int getRequiredSuccesses() {
        return this.data.requiredSuccesses();
    }

    public boolean requiresSkyAccess() {
        return this.data.skyAccess();
    }

    public BlockRotation getRotation() {
        return this.data.rotation();
    }

    protected TestData<RegistryEntry<TestEnvironmentDefinition>> getData() {
        return this.data;
    }

    protected abstract MutableText getTypeDescription();

    public Text getDescription() {
        return this.getFormattedTypeDescription().append(this.getStructureAndBatchDescription());
    }

    protected MutableText getFormattedTypeDescription() {
        return this.getFormattedDescription("test_instance.description.type", this.getTypeDescription());
    }

    protected Text getStructureAndBatchDescription() {
        return this.getFormattedDescription("test_instance.description.structure", this.data.structure().toString()).append(this.getFormattedDescription("test_instance.description.batch", this.data.environment().getIdAsString()));
    }

    protected MutableText getFormattedDescription(String key, String description) {
        return this.getFormattedDescription(key, Text.literal(description));
    }

    protected MutableText getFormattedDescription(String key, MutableText description) {
        return Text.translatable(key, description.formatted(Formatting.BLUE)).append(Text.literal("\n"));
    }
}


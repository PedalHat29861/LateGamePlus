/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  com.mojang.datafixers.kinds.App
 *  com.mojang.datafixers.kinds.Applicative
 *  com.mojang.serialization.MapCodec
 *  com.mojang.serialization.codecs.RecordCodecBuilder
 */
package net.minecraft.test;

import com.mojang.datafixers.kinds.App;
import com.mojang.datafixers.kinds.Applicative;
import com.mojang.serialization.MapCodec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import java.util.function.Consumer;
import net.minecraft.registry.RegistryKey;
import net.minecraft.registry.RegistryKeys;
import net.minecraft.registry.entry.RegistryEntry;
import net.minecraft.test.TestContext;
import net.minecraft.test.TestData;
import net.minecraft.test.TestEnvironmentDefinition;
import net.minecraft.test.TestInstance;
import net.minecraft.text.MutableText;
import net.minecraft.text.Text;

public class FunctionTestInstance
extends TestInstance {
    public static final MapCodec<FunctionTestInstance> CODEC = RecordCodecBuilder.mapCodec(instance -> instance.group((App)RegistryKey.createCodec(RegistryKeys.TEST_FUNCTION).fieldOf("function").forGetter(FunctionTestInstance::getFunction), (App)TestData.CODEC.forGetter(TestInstance::getData)).apply((Applicative)instance, FunctionTestInstance::new));
    private final RegistryKey<Consumer<TestContext>> function;

    public FunctionTestInstance(RegistryKey<Consumer<TestContext>> function, TestData<RegistryEntry<TestEnvironmentDefinition>> data) {
        super(data);
        this.function = function;
    }

    @Override
    public void start(TestContext context) {
        context.getWorld().getRegistryManager().getOptionalEntry(this.function).map(RegistryEntry.Reference::value).orElseThrow(() -> new IllegalStateException("Trying to access missing test function: " + String.valueOf(this.function.getValue()))).accept(context);
    }

    private RegistryKey<Consumer<TestContext>> getFunction() {
        return this.function;
    }

    public MapCodec<FunctionTestInstance> getCodec() {
        return CODEC;
    }

    @Override
    protected MutableText getTypeDescription() {
        return Text.translatable("test_instance.type.function");
    }

    @Override
    public Text getDescription() {
        return this.getFormattedTypeDescription().append(this.getFormattedDescription("test_instance.description.function", this.function.getValue().toString())).append(this.getStructureAndBatchDescription());
    }
}


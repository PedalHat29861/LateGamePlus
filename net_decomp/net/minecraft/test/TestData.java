/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  com.mojang.datafixers.kinds.App
 *  com.mojang.datafixers.kinds.Applicative
 *  com.mojang.serialization.Codec
 *  com.mojang.serialization.MapCodec
 *  com.mojang.serialization.codecs.RecordCodecBuilder
 */
package net.minecraft.test;

import com.mojang.datafixers.kinds.App;
import com.mojang.datafixers.kinds.Applicative;
import com.mojang.serialization.Codec;
import com.mojang.serialization.MapCodec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import java.util.function.Function;
import net.minecraft.registry.entry.RegistryEntry;
import net.minecraft.test.TestEnvironmentDefinition;
import net.minecraft.util.BlockRotation;
import net.minecraft.util.Identifier;
import net.minecraft.util.dynamic.Codecs;

public record TestData<EnvironmentType>(EnvironmentType environment, Identifier structure, int maxTicks, int setupTicks, boolean required, BlockRotation rotation, boolean manualOnly, int maxAttempts, int requiredSuccesses, boolean skyAccess) {
    public static final MapCodec<TestData<RegistryEntry<TestEnvironmentDefinition>>> CODEC = RecordCodecBuilder.mapCodec(instance -> instance.group((App)TestEnvironmentDefinition.ENTRY_CODEC.fieldOf("environment").forGetter(TestData::environment), (App)Identifier.CODEC.fieldOf("structure").forGetter(TestData::structure), (App)Codecs.POSITIVE_INT.fieldOf("max_ticks").forGetter(TestData::maxTicks), (App)Codecs.NON_NEGATIVE_INT.optionalFieldOf("setup_ticks", (Object)0).forGetter(TestData::setupTicks), (App)Codec.BOOL.optionalFieldOf("required", (Object)true).forGetter(TestData::required), (App)BlockRotation.CODEC.optionalFieldOf("rotation", (Object)BlockRotation.NONE).forGetter(TestData::rotation), (App)Codec.BOOL.optionalFieldOf("manual_only", (Object)false).forGetter(TestData::manualOnly), (App)Codecs.POSITIVE_INT.optionalFieldOf("max_attempts", (Object)1).forGetter(TestData::maxAttempts), (App)Codecs.POSITIVE_INT.optionalFieldOf("required_successes", (Object)1).forGetter(TestData::requiredSuccesses), (App)Codec.BOOL.optionalFieldOf("sky_access", (Object)false).forGetter(TestData::skyAccess)).apply((Applicative)instance, TestData::new));

    public TestData(EnvironmentType environment, Identifier structure, int maxTicks, int setupTicks, boolean required, BlockRotation rotation) {
        this(environment, structure, maxTicks, setupTicks, required, rotation, false, 1, 1, false);
    }

    public TestData(EnvironmentType environment, Identifier structure, int maxTicks, int setupTicks, boolean required) {
        this(environment, structure, maxTicks, setupTicks, required, BlockRotation.NONE);
    }

    public <T> TestData<T> applyToEnvironment(Function<EnvironmentType, T> environmentFunction) {
        return new TestData<T>(environmentFunction.apply(this.environment), this.structure, this.maxTicks, this.setupTicks, this.required, this.rotation, this.manualOnly, this.maxAttempts, this.requiredSuccesses, this.skyAccess);
    }
}


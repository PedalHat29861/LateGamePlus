/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  com.mojang.datafixers.kinds.App
 *  com.mojang.datafixers.kinds.Applicative
 *  com.mojang.serialization.Codec
 *  com.mojang.serialization.DataResult
 *  com.mojang.serialization.DynamicOps
 *  com.mojang.serialization.codecs.RecordCodecBuilder
 */
package net.minecraft.world.level;

import com.mojang.datafixers.kinds.App;
import com.mojang.datafixers.kinds.Applicative;
import com.mojang.serialization.Codec;
import com.mojang.serialization.DataResult;
import com.mojang.serialization.DynamicOps;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import java.lang.invoke.MethodHandle;
import java.lang.runtime.ObjectMethods;
import net.minecraft.registry.DynamicRegistryManager;
import net.minecraft.registry.Registry;
import net.minecraft.registry.RegistryKeys;
import net.minecraft.world.dimension.DimensionOptions;
import net.minecraft.world.dimension.DimensionOptionsRegistryHolder;
import net.minecraft.world.gen.GeneratorOptions;

public record WorldGenSettings(GeneratorOptions generatorOptions, DimensionOptionsRegistryHolder dimensionOptionsRegistryHolder) {
    public static final Codec<WorldGenSettings> CODEC = RecordCodecBuilder.create(instance -> instance.group((App)GeneratorOptions.CODEC.forGetter(WorldGenSettings::generatorOptions), (App)DimensionOptionsRegistryHolder.CODEC.forGetter(WorldGenSettings::dimensionOptionsRegistryHolder)).apply((Applicative)instance, instance.stable(WorldGenSettings::new)));

    public static <T> DataResult<T> encode(DynamicOps<T> registryOps, GeneratorOptions generatorOptions, DimensionOptionsRegistryHolder dimensionOptionsRegistryHolder) {
        return CODEC.encodeStart(registryOps, (Object)new WorldGenSettings(generatorOptions, dimensionOptionsRegistryHolder));
    }

    public static <T> DataResult<T> encode(DynamicOps<T> registryOps, GeneratorOptions generatorOptions, DynamicRegistryManager dynamicRegistryManager) {
        return WorldGenSettings.encode(registryOps, generatorOptions, new DimensionOptionsRegistryHolder((Registry<DimensionOptions>)dynamicRegistryManager.getOrThrow(RegistryKeys.DIMENSION)));
    }

    @Override
    public final String toString() {
        return ObjectMethods.bootstrap("toString", new MethodHandle[]{WorldGenSettings.class, "options;dimensions", "generatorOptions", "dimensionOptionsRegistryHolder"}, this);
    }

    @Override
    public final int hashCode() {
        return (int)ObjectMethods.bootstrap("hashCode", new MethodHandle[]{WorldGenSettings.class, "options;dimensions", "generatorOptions", "dimensionOptionsRegistryHolder"}, this);
    }

    @Override
    public final boolean equals(Object object) {
        return (boolean)ObjectMethods.bootstrap("equals", new MethodHandle[]{WorldGenSettings.class, "options;dimensions", "generatorOptions", "dimensionOptionsRegistryHolder"}, this, object);
    }
}


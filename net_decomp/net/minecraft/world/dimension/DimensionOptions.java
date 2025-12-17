/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  com.mojang.datafixers.kinds.App
 *  com.mojang.datafixers.kinds.Applicative
 *  com.mojang.serialization.Codec
 *  com.mojang.serialization.codecs.RecordCodecBuilder
 */
package net.minecraft.world.dimension;

import com.mojang.datafixers.kinds.App;
import com.mojang.datafixers.kinds.Applicative;
import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import java.lang.invoke.MethodHandle;
import java.lang.runtime.ObjectMethods;
import net.minecraft.registry.RegistryKey;
import net.minecraft.registry.RegistryKeys;
import net.minecraft.registry.entry.RegistryEntry;
import net.minecraft.util.Identifier;
import net.minecraft.world.dimension.DimensionType;
import net.minecraft.world.gen.chunk.ChunkGenerator;

public record DimensionOptions(RegistryEntry<DimensionType> dimensionTypeEntry, ChunkGenerator chunkGenerator) {
    public static final Codec<DimensionOptions> CODEC = RecordCodecBuilder.create(instance -> instance.group((App)DimensionType.REGISTRY_CODEC.fieldOf("type").forGetter(DimensionOptions::dimensionTypeEntry), (App)ChunkGenerator.CODEC.fieldOf("generator").forGetter(DimensionOptions::chunkGenerator)).apply((Applicative)instance, instance.stable(DimensionOptions::new)));
    public static final RegistryKey<DimensionOptions> OVERWORLD = RegistryKey.of(RegistryKeys.DIMENSION, Identifier.ofVanilla("overworld"));
    public static final RegistryKey<DimensionOptions> NETHER = RegistryKey.of(RegistryKeys.DIMENSION, Identifier.ofVanilla("the_nether"));
    public static final RegistryKey<DimensionOptions> END = RegistryKey.of(RegistryKeys.DIMENSION, Identifier.ofVanilla("the_end"));

    @Override
    public final String toString() {
        return ObjectMethods.bootstrap("toString", new MethodHandle[]{DimensionOptions.class, "type;generator", "dimensionTypeEntry", "chunkGenerator"}, this);
    }

    @Override
    public final int hashCode() {
        return (int)ObjectMethods.bootstrap("hashCode", new MethodHandle[]{DimensionOptions.class, "type;generator", "dimensionTypeEntry", "chunkGenerator"}, this);
    }

    @Override
    public final boolean equals(Object object) {
        return (boolean)ObjectMethods.bootstrap("equals", new MethodHandle[]{DimensionOptions.class, "type;generator", "dimensionTypeEntry", "chunkGenerator"}, this, object);
    }
}


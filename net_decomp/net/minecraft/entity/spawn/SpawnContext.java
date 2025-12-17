/*
 * Decompiled with CFR 0.152.
 */
package net.minecraft.entity.spawn;

import java.lang.invoke.MethodHandle;
import java.lang.runtime.ObjectMethods;
import net.minecraft.registry.entry.RegistryEntry;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.ServerWorldAccess;
import net.minecraft.world.attribute.EnvironmentAttributeAccess;
import net.minecraft.world.biome.Biome;

public record SpawnContext(BlockPos pos, ServerWorldAccess world, EnvironmentAttributeAccess environmentAttributes, RegistryEntry<Biome> biome) {
    public static SpawnContext of(ServerWorldAccess world, BlockPos pos) {
        RegistryEntry<Biome> registryEntry = world.getBiome(pos);
        return new SpawnContext(pos, world, world.getEnvironmentAttributes(), registryEntry);
    }

    @Override
    public final String toString() {
        return ObjectMethods.bootstrap("toString", new MethodHandle[]{SpawnContext.class, "pos;level;environmentAttributes;biome", "pos", "world", "environmentAttributes", "biome"}, this);
    }

    @Override
    public final int hashCode() {
        return (int)ObjectMethods.bootstrap("hashCode", new MethodHandle[]{SpawnContext.class, "pos;level;environmentAttributes;biome", "pos", "world", "environmentAttributes", "biome"}, this);
    }

    @Override
    public final boolean equals(Object object) {
        return (boolean)ObjectMethods.bootstrap("equals", new MethodHandle[]{SpawnContext.class, "pos;level;environmentAttributes;biome", "pos", "world", "environmentAttributes", "biome"}, this, object);
    }
}


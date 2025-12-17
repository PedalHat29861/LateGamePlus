/*
 * Decompiled with CFR 0.152.
 */
package net.minecraft.world.chunk;

import java.lang.invoke.MethodHandle;
import java.lang.runtime.ObjectMethods;
import java.util.concurrent.Executor;
import net.minecraft.server.world.ServerLightingProvider;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.structure.StructureTemplateManager;
import net.minecraft.world.chunk.WorldChunk;
import net.minecraft.world.gen.chunk.ChunkGenerator;

public record ChunkGenerationContext(ServerWorld world, ChunkGenerator generator, StructureTemplateManager structureManager, ServerLightingProvider lightingProvider, Executor mainThreadExecutor, WorldChunk.UnsavedListener unsavedListener) {
    @Override
    public final String toString() {
        return ObjectMethods.bootstrap("toString", new MethodHandle[]{ChunkGenerationContext.class, "level;generator;structureManager;lightEngine;mainThreadExecutor;unsavedListener", "world", "generator", "structureManager", "lightingProvider", "mainThreadExecutor", "unsavedListener"}, this);
    }

    @Override
    public final int hashCode() {
        return (int)ObjectMethods.bootstrap("hashCode", new MethodHandle[]{ChunkGenerationContext.class, "level;generator;structureManager;lightEngine;mainThreadExecutor;unsavedListener", "world", "generator", "structureManager", "lightingProvider", "mainThreadExecutor", "unsavedListener"}, this);
    }

    @Override
    public final boolean equals(Object object) {
        return (boolean)ObjectMethods.bootstrap("equals", new MethodHandle[]{ChunkGenerationContext.class, "level;generator;structureManager;lightEngine;mainThreadExecutor;unsavedListener", "world", "generator", "structureManager", "lightingProvider", "mainThreadExecutor", "unsavedListener"}, this, object);
    }
}


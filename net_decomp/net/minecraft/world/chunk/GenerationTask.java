/*
 * Decompiled with CFR 0.152.
 */
package net.minecraft.world.chunk;

import java.util.concurrent.CompletableFuture;
import net.minecraft.util.collection.BoundedRegionArray;
import net.minecraft.world.chunk.AbstractChunkHolder;
import net.minecraft.world.chunk.Chunk;
import net.minecraft.world.chunk.ChunkGenerationContext;
import net.minecraft.world.chunk.ChunkGenerationStep;

@FunctionalInterface
public interface GenerationTask {
    public CompletableFuture<Chunk> doWork(ChunkGenerationContext var1, ChunkGenerationStep var2, BoundedRegionArray<AbstractChunkHolder> var3, Chunk var4);
}


/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  com.google.common.collect.ImmutableList
 *  org.jspecify.annotations.Nullable
 */
package net.minecraft.world.chunk;

import com.google.common.collect.ImmutableList;
import java.lang.invoke.MethodHandle;
import java.lang.runtime.ObjectMethods;
import java.util.Arrays;
import java.util.concurrent.CompletableFuture;
import net.minecraft.util.collection.BoundedRegionArray;
import net.minecraft.util.function.Finishable;
import net.minecraft.util.profiling.jfr.FlightProfiler;
import net.minecraft.world.chunk.AbstractChunkHolder;
import net.minecraft.world.chunk.Chunk;
import net.minecraft.world.chunk.ChunkGenerating;
import net.minecraft.world.chunk.ChunkGenerationContext;
import net.minecraft.world.chunk.ChunkStatus;
import net.minecraft.world.chunk.GenerationDependencies;
import net.minecraft.world.chunk.GenerationTask;
import net.minecraft.world.chunk.ProtoChunk;
import org.jspecify.annotations.Nullable;

public final class ChunkGenerationStep
extends Record {
    final ChunkStatus targetStatus;
    private final GenerationDependencies directDependencies;
    final GenerationDependencies accumulatedDependencies;
    private final int blockStateWriteRadius;
    private final GenerationTask task;

    public ChunkGenerationStep(ChunkStatus targetStatus, GenerationDependencies directDependencies, GenerationDependencies accumulatedDependencies, int blockStateWriteRadius, GenerationTask task) {
        this.targetStatus = targetStatus;
        this.directDependencies = directDependencies;
        this.accumulatedDependencies = accumulatedDependencies;
        this.blockStateWriteRadius = blockStateWriteRadius;
        this.task = task;
    }

    public int getAdditionalLevel(ChunkStatus status) {
        if (status == this.targetStatus) {
            return 0;
        }
        return this.accumulatedDependencies.getAdditionalLevel(status);
    }

    public CompletableFuture<Chunk> run(ChunkGenerationContext context, BoundedRegionArray<AbstractChunkHolder> boundedRegionArray, Chunk chunk) {
        if (chunk.getStatus().isEarlierThan(this.targetStatus)) {
            Finishable finishable = FlightProfiler.INSTANCE.startChunkGenerationProfiling(chunk.getPos(), context.world().getRegistryKey(), this.targetStatus.getId());
            return this.task.doWork(context, this, boundedRegionArray, chunk).thenApply(generated -> this.finalizeGeneration((Chunk)generated, finishable));
        }
        return this.task.doWork(context, this, boundedRegionArray, chunk);
    }

    private Chunk finalizeGeneration(Chunk chunk, @Nullable Finishable finishCallback) {
        ProtoChunk protoChunk;
        if (chunk instanceof ProtoChunk && (protoChunk = (ProtoChunk)chunk).getStatus().isEarlierThan(this.targetStatus)) {
            protoChunk.setStatus(this.targetStatus);
        }
        if (finishCallback != null) {
            finishCallback.finish(true);
        }
        return chunk;
    }

    @Override
    public final String toString() {
        return ObjectMethods.bootstrap("toString", new MethodHandle[]{ChunkGenerationStep.class, "targetStatus;directDependencies;accumulatedDependencies;blockStateWriteRadius;task", "targetStatus", "directDependencies", "accumulatedDependencies", "blockStateWriteRadius", "task"}, this);
    }

    @Override
    public final int hashCode() {
        return (int)ObjectMethods.bootstrap("hashCode", new MethodHandle[]{ChunkGenerationStep.class, "targetStatus;directDependencies;accumulatedDependencies;blockStateWriteRadius;task", "targetStatus", "directDependencies", "accumulatedDependencies", "blockStateWriteRadius", "task"}, this);
    }

    @Override
    public final boolean equals(Object object) {
        return (boolean)ObjectMethods.bootstrap("equals", new MethodHandle[]{ChunkGenerationStep.class, "targetStatus;directDependencies;accumulatedDependencies;blockStateWriteRadius;task", "targetStatus", "directDependencies", "accumulatedDependencies", "blockStateWriteRadius", "task"}, this, object);
    }

    public ChunkStatus targetStatus() {
        return this.targetStatus;
    }

    public GenerationDependencies directDependencies() {
        return this.directDependencies;
    }

    public GenerationDependencies accumulatedDependencies() {
        return this.accumulatedDependencies;
    }

    public int blockStateWriteRadius() {
        return this.blockStateWriteRadius;
    }

    public GenerationTask task() {
        return this.task;
    }

    public static class Builder {
        private final ChunkStatus targetStatus;
        private final @Nullable ChunkGenerationStep previousStep;
        private ChunkStatus[] directDependencies;
        private int blockStateWriteRadius = -1;
        private GenerationTask task = ChunkGenerating::noop;

        protected Builder(ChunkStatus targetStatus) {
            if (targetStatus.getPrevious() != targetStatus) {
                throw new IllegalArgumentException("Not starting with the first status: " + String.valueOf(targetStatus));
            }
            this.targetStatus = targetStatus;
            this.previousStep = null;
            this.directDependencies = new ChunkStatus[0];
        }

        protected Builder(ChunkStatus blockStateWriteRadius, ChunkGenerationStep previousStep) {
            if (previousStep.targetStatus.getIndex() != blockStateWriteRadius.getIndex() - 1) {
                throw new IllegalArgumentException("Out of order status: " + String.valueOf(blockStateWriteRadius));
            }
            this.targetStatus = blockStateWriteRadius;
            this.previousStep = previousStep;
            this.directDependencies = new ChunkStatus[]{previousStep.targetStatus};
        }

        public Builder dependsOn(ChunkStatus status, int level) {
            if (status.isAtLeast(this.targetStatus)) {
                throw new IllegalArgumentException("Status " + String.valueOf(status) + " can not be required by " + String.valueOf(this.targetStatus));
            }
            int i = level + 1;
            ChunkStatus[] chunkStatuss = this.directDependencies;
            if (i > chunkStatuss.length) {
                this.directDependencies = new ChunkStatus[i];
                Arrays.fill(this.directDependencies, status);
            }
            for (int j = 0; j < Math.min(i, chunkStatuss.length); ++j) {
                this.directDependencies[j] = ChunkStatus.max(chunkStatuss[j], status);
            }
            return this;
        }

        public Builder blockStateWriteRadius(int blockStateWriteRadius) {
            this.blockStateWriteRadius = blockStateWriteRadius;
            return this;
        }

        public Builder task(GenerationTask task) {
            this.task = task;
            return this;
        }

        public ChunkGenerationStep build() {
            return new ChunkGenerationStep(this.targetStatus, new GenerationDependencies((ImmutableList<ChunkStatus>)ImmutableList.copyOf((Object[])this.directDependencies)), new GenerationDependencies((ImmutableList<ChunkStatus>)ImmutableList.copyOf((Object[])this.accumulateDependencies())), this.blockStateWriteRadius, this.task);
        }

        private ChunkStatus[] accumulateDependencies() {
            if (this.previousStep == null) {
                return this.directDependencies;
            }
            int i = this.getParentStatus(this.previousStep.targetStatus);
            GenerationDependencies generationDependencies = this.previousStep.accumulatedDependencies;
            ChunkStatus[] chunkStatuss = new ChunkStatus[Math.max(i + generationDependencies.size(), this.directDependencies.length)];
            for (int j = 0; j < chunkStatuss.length; ++j) {
                int k = j - i;
                chunkStatuss[j] = k < 0 || k >= generationDependencies.size() ? this.directDependencies[j] : (j >= this.directDependencies.length ? generationDependencies.get(k) : ChunkStatus.max(this.directDependencies[j], generationDependencies.get(k)));
            }
            return chunkStatuss;
        }

        private int getParentStatus(ChunkStatus status) {
            for (int i = this.directDependencies.length - 1; i >= 0; --i) {
                if (!this.directDependencies[i].isAtLeast(status)) continue;
                return i;
            }
            return 0;
        }
    }
}


/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  com.google.common.annotations.VisibleForTesting
 *  it.unimi.dsi.fastutil.longs.LongOpenHashSet
 *  it.unimi.dsi.fastutil.longs.LongSet
 *  org.jspecify.annotations.Nullable
 */
package net.minecraft.server.world;

import com.google.common.annotations.VisibleForTesting;
import it.unimi.dsi.fastutil.longs.LongOpenHashSet;
import it.unimi.dsi.fastutil.longs.LongSet;
import java.util.concurrent.Executor;
import java.util.stream.Collectors;
import net.minecraft.server.world.ChunkTaskScheduler;
import net.minecraft.server.world.LevelPrioritizedQueue;
import net.minecraft.util.math.ChunkPos;
import net.minecraft.util.thread.TaskExecutor;
import org.jspecify.annotations.Nullable;

public class ThrottledChunkTaskScheduler
extends ChunkTaskScheduler {
    private final LongSet chunks = new LongOpenHashSet();
    private final int maxConcurrentChunks;
    private final String name;

    public ThrottledChunkTaskScheduler(TaskExecutor<Runnable> executor, Executor dispatchExecutor, int maxConcurrentChunks) {
        super(executor, dispatchExecutor);
        this.maxConcurrentChunks = maxConcurrentChunks;
        this.name = executor.getName();
    }

    @Override
    protected void onRemove(long chunkPos) {
        this.chunks.remove(chunkPos);
    }

    @Override
    protected @Nullable LevelPrioritizedQueue.Entry poll() {
        return this.chunks.size() < this.maxConcurrentChunks ? super.poll() : null;
    }

    @Override
    protected void schedule(LevelPrioritizedQueue.Entry entry) {
        this.chunks.add(entry.chunkPos());
        super.schedule(entry);
    }

    @VisibleForTesting
    public String toDumpString() {
        return this.name + "=[" + this.chunks.longStream().mapToObj(chunkPos -> chunkPos + ":" + String.valueOf(new ChunkPos(chunkPos))).collect(Collectors.joining(",")) + "], s=" + this.pollOnUpdate;
    }
}


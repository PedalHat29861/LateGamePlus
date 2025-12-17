/*
 * Decompiled with CFR 0.152.
 */
package net.minecraft.world.chunk;

import net.minecraft.registry.RegistryKey;
import net.minecraft.util.math.ChunkPos;
import net.minecraft.util.math.MathHelper;
import net.minecraft.world.World;
import net.minecraft.world.chunk.ChunkLoadProgress;

public class DeltaChunkLoadProgress
implements ChunkLoadProgress {
    private static final int INITIAL_CHUNKS = 10;
    private static final int PLAYER_CHUNKS = MathHelper.square(7);
    private final boolean player;
    private int totalChunks;
    private int previousLoadedChunks;
    private int chunks;
    private float fullyLoadedChunksRatio;
    private volatile float loadProgress;

    public DeltaChunkLoadProgress(boolean player) {
        this.player = player;
    }

    @Override
    public void init(ChunkLoadProgress.Stage stage, int chunks) {
        if (!this.shouldLoad(stage)) {
            return;
        }
        switch (stage) {
            case LOAD_INITIAL_CHUNKS: {
                int i = this.player ? PLAYER_CHUNKS : 0;
                this.totalChunks = 10 + chunks + i;
                this.init(10);
                this.finish();
                this.init(chunks);
                break;
            }
            case LOAD_PLAYER_CHUNKS: {
                this.init(PLAYER_CHUNKS);
            }
        }
    }

    private void init(int chunks) {
        this.chunks = chunks;
        this.fullyLoadedChunksRatio = 0.0f;
        this.recalculateLoadProgress();
    }

    @Override
    public void progress(ChunkLoadProgress.Stage stage, int fullChunks, int totalChunks) {
        if (this.shouldLoad(stage)) {
            this.fullyLoadedChunksRatio = totalChunks == 0 ? 0.0f : (float)fullChunks / (float)totalChunks;
            this.recalculateLoadProgress();
        }
    }

    @Override
    public void finish(ChunkLoadProgress.Stage stage) {
        if (this.shouldLoad(stage)) {
            this.finish();
        }
    }

    private void finish() {
        this.previousLoadedChunks += this.chunks;
        this.chunks = 0;
        this.recalculateLoadProgress();
    }

    private boolean shouldLoad(ChunkLoadProgress.Stage stage) {
        return switch (stage) {
            case ChunkLoadProgress.Stage.LOAD_INITIAL_CHUNKS -> true;
            case ChunkLoadProgress.Stage.LOAD_PLAYER_CHUNKS -> this.player;
            default -> false;
        };
    }

    private void recalculateLoadProgress() {
        if (this.totalChunks == 0) {
            this.loadProgress = 0.0f;
        } else {
            float f = (float)this.previousLoadedChunks + this.fullyLoadedChunksRatio * (float)this.chunks;
            this.loadProgress = f / (float)this.totalChunks;
        }
    }

    public float getLoadProgress() {
        return this.loadProgress;
    }

    @Override
    public void initSpawnPos(RegistryKey<World> worldKey, ChunkPos spawnChunk) {
    }
}


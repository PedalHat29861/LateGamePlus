/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  it.unimi.dsi.fastutil.longs.Long2ObjectOpenHashMap
 *  org.jspecify.annotations.Nullable
 */
package net.minecraft.world.chunk;

import it.unimi.dsi.fastutil.longs.Long2ObjectOpenHashMap;
import net.minecraft.world.chunk.ChunkNibbleArray;
import org.jspecify.annotations.Nullable;

public abstract class ChunkToNibbleArrayMap<M extends ChunkToNibbleArrayMap<M>> {
    private static final int field_31705 = 2;
    private final long[] cachePositions = new long[2];
    private final @Nullable ChunkNibbleArray[] cacheArrays = new ChunkNibbleArray[2];
    private boolean cacheEnabled;
    protected final Long2ObjectOpenHashMap<ChunkNibbleArray> arrays;

    protected ChunkToNibbleArrayMap(Long2ObjectOpenHashMap<ChunkNibbleArray> arrays) {
        this.arrays = arrays;
        this.clearCache();
        this.cacheEnabled = true;
    }

    public abstract M copy();

    public ChunkNibbleArray replaceWithCopy(long pos) {
        ChunkNibbleArray chunkNibbleArray = ((ChunkNibbleArray)this.arrays.get(pos)).copy();
        this.arrays.put(pos, (Object)chunkNibbleArray);
        this.clearCache();
        return chunkNibbleArray;
    }

    public boolean containsKey(long chunkPos) {
        return this.arrays.containsKey(chunkPos);
    }

    public @Nullable ChunkNibbleArray get(long chunkPos) {
        ChunkNibbleArray chunkNibbleArray;
        if (this.cacheEnabled) {
            for (int i = 0; i < 2; ++i) {
                if (chunkPos != this.cachePositions[i]) continue;
                return this.cacheArrays[i];
            }
        }
        if ((chunkNibbleArray = (ChunkNibbleArray)this.arrays.get(chunkPos)) != null) {
            if (this.cacheEnabled) {
                for (int j = 1; j > 0; --j) {
                    this.cachePositions[j] = this.cachePositions[j - 1];
                    this.cacheArrays[j] = this.cacheArrays[j - 1];
                }
                this.cachePositions[0] = chunkPos;
                this.cacheArrays[0] = chunkNibbleArray;
            }
            return chunkNibbleArray;
        }
        return null;
    }

    public @Nullable ChunkNibbleArray removeChunk(long chunkPos) {
        return (ChunkNibbleArray)this.arrays.remove(chunkPos);
    }

    public void put(long pos, ChunkNibbleArray data) {
        this.arrays.put(pos, (Object)data);
    }

    public void clearCache() {
        for (int i = 0; i < 2; ++i) {
            this.cachePositions[i] = Long.MAX_VALUE;
            this.cacheArrays[i] = null;
        }
    }

    public void disableCache() {
        this.cacheEnabled = false;
    }
}


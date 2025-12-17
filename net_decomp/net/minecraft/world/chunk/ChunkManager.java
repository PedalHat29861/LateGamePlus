/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  it.unimi.dsi.fastutil.longs.LongSet
 *  org.jspecify.annotations.Nullable
 */
package net.minecraft.world.chunk;

import it.unimi.dsi.fastutil.longs.LongSet;
import java.io.IOException;
import java.util.function.BooleanSupplier;
import net.minecraft.util.math.ChunkPos;
import net.minecraft.world.chunk.Chunk;
import net.minecraft.world.chunk.ChunkProvider;
import net.minecraft.world.chunk.ChunkStatus;
import net.minecraft.world.chunk.WorldChunk;
import net.minecraft.world.chunk.light.LightSourceView;
import net.minecraft.world.chunk.light.LightingProvider;
import org.jspecify.annotations.Nullable;

public abstract class ChunkManager
implements ChunkProvider,
AutoCloseable {
    public @Nullable WorldChunk getWorldChunk(int chunkX, int chunkZ, boolean create) {
        return (WorldChunk)this.getChunk(chunkX, chunkZ, ChunkStatus.FULL, create);
    }

    public @Nullable WorldChunk getWorldChunk(int chunkX, int chunkZ) {
        return this.getWorldChunk(chunkX, chunkZ, false);
    }

    @Override
    public @Nullable LightSourceView getChunk(int chunkX, int chunkZ) {
        return this.getChunk(chunkX, chunkZ, ChunkStatus.EMPTY, false);
    }

    public boolean isChunkLoaded(int x, int z) {
        return this.getChunk(x, z, ChunkStatus.FULL, false) != null;
    }

    public abstract @Nullable Chunk getChunk(int var1, int var2, ChunkStatus var3, boolean var4);

    public abstract void tick(BooleanSupplier var1, boolean var2);

    public void onSectionStatusChanged(int x, int sectionY, int z, boolean previouslyEmpty) {
    }

    public abstract String getDebugString();

    public abstract int getLoadedChunkCount();

    @Override
    public void close() throws IOException {
    }

    public abstract LightingProvider getLightingProvider();

    public void setMobSpawnOptions(boolean spawnMonsters) {
    }

    public boolean setChunkForced(ChunkPos pos, boolean forced) {
        return false;
    }

    public LongSet getForcedChunks() {
        return LongSet.of();
    }
}


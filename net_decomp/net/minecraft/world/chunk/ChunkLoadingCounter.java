/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  it.unimi.dsi.fastutil.longs.LongOpenHashSet
 *  it.unimi.dsi.fastutil.longs.LongSet
 */
package net.minecraft.world.chunk;

import it.unimi.dsi.fastutil.longs.LongOpenHashSet;
import it.unimi.dsi.fastutil.longs.LongSet;
import java.util.ArrayList;
import java.util.List;
import net.minecraft.server.world.ChunkHolder;
import net.minecraft.server.world.ServerChunkManager;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.world.chunk.ChunkStatus;

public class ChunkLoadingCounter {
    private final List<ChunkHolder> nonFullChunks = new ArrayList<ChunkHolder>();
    private int totalChunks;

    public void load(ServerWorld world, Runnable runnable) {
        ServerChunkManager serverChunkManager = world.getChunkManager();
        LongOpenHashSet longSet = new LongOpenHashSet();
        serverChunkManager.updateChunks();
        serverChunkManager.chunkLoadingManager.getChunkHolders(ChunkStatus.FULL).forEach(arg_0 -> ChunkLoadingCounter.method_72250((LongSet)longSet, arg_0));
        runnable.run();
        serverChunkManager.updateChunks();
        serverChunkManager.chunkLoadingManager.getChunkHolders(ChunkStatus.FULL).forEach(arg_0 -> this.method_72248((LongSet)longSet, arg_0));
    }

    public int getFullChunks() {
        return this.totalChunks - this.getNonFullChunks();
    }

    public int getNonFullChunks() {
        this.nonFullChunks.removeIf(holder -> holder.getLatestStatus() == ChunkStatus.FULL);
        return this.nonFullChunks.size();
    }

    public int getTotalChunks() {
        return this.totalChunks;
    }

    private /* synthetic */ void method_72248(LongSet longSet, ChunkHolder holder) {
        if (!longSet.contains(holder.getPos().toLong())) {
            this.nonFullChunks.add(holder);
            ++this.totalChunks;
        }
    }

    private static /* synthetic */ void method_72250(LongSet longSet, ChunkHolder holder) {
        longSet.add(holder.getPos().toLong());
    }
}


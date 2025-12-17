/*
 * Decompiled with CFR 0.152.
 */
package net.minecraft.world.chunk;

import net.minecraft.server.world.ChunkLevelType;
import net.minecraft.util.math.ChunkPos;

@FunctionalInterface
public interface ChunkStatusChangeListener {
    public void onChunkStatusChange(ChunkPos var1, ChunkLevelType var2);
}


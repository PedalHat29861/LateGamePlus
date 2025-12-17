/*
 * Decompiled with CFR 0.152.
 */
package net.minecraft.world.updater;

import java.util.function.Supplier;
import net.minecraft.nbt.NbtCompound;
import net.minecraft.util.math.ChunkPos;

@FunctionalInterface
public interface ChunkUpdater {
    public static final Supplier<ChunkUpdater> PASSTHROUGH_FACTORY = () -> nbt -> nbt;

    public NbtCompound applyFix(NbtCompound var1);

    default public void markChunkDone(ChunkPos chunkPos) {
    }

    default public int targetDataVersion() {
        return -1;
    }
}


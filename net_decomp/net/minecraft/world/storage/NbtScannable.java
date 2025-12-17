/*
 * Decompiled with CFR 0.152.
 */
package net.minecraft.world.storage;

import java.util.concurrent.CompletableFuture;
import net.minecraft.nbt.scanner.NbtScanner;
import net.minecraft.util.math.ChunkPos;

public interface NbtScannable {
    public CompletableFuture<Void> scanChunk(ChunkPos var1, NbtScanner var2);
}


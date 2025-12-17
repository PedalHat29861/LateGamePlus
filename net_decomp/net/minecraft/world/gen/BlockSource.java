/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  org.jspecify.annotations.Nullable
 */
package net.minecraft.world.gen;

import net.minecraft.block.BlockState;
import net.minecraft.world.gen.chunk.ChunkNoiseSampler;
import org.jspecify.annotations.Nullable;

public interface BlockSource {
    public @Nullable BlockState apply(ChunkNoiseSampler var1, int var2, int var3, int var4);
}


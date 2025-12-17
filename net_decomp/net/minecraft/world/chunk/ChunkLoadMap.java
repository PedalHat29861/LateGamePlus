/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  org.jspecify.annotations.Nullable
 */
package net.minecraft.world.chunk;

import net.minecraft.registry.RegistryKey;
import net.minecraft.util.math.ChunkPos;
import net.minecraft.world.World;
import net.minecraft.world.chunk.ChunkStatus;
import org.jspecify.annotations.Nullable;

public interface ChunkLoadMap {
    public void initSpawnPos(RegistryKey<World> var1, ChunkPos var2);

    public @Nullable ChunkStatus getStatus(int var1, int var2);

    public int getRadius();
}


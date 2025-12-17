/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  org.jspecify.annotations.Nullable
 */
package net.minecraft.world.debug;

import java.util.function.BiConsumer;
import net.minecraft.entity.Entity;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.ChunkPos;
import net.minecraft.world.debug.DebugSubscriptionType;
import org.jspecify.annotations.Nullable;

public interface DebugDataStore {
    public <T> void forEachChunkData(DebugSubscriptionType<T> var1, BiConsumer<ChunkPos, T> var2);

    public <T> @Nullable T getChunkData(DebugSubscriptionType<T> var1, ChunkPos var2);

    public <T> void forEachBlockData(DebugSubscriptionType<T> var1, BiConsumer<BlockPos, T> var2);

    public <T> @Nullable T getBlockData(DebugSubscriptionType<T> var1, BlockPos var2);

    public <T> void forEachEntityData(DebugSubscriptionType<T> var1, BiConsumer<Entity, T> var2);

    public <T> @Nullable T getEntityData(DebugSubscriptionType<T> var1, Entity var2);

    public <T> void forEachEvent(DebugSubscriptionType<T> var1, EventConsumer<T> var2);

    @FunctionalInterface
    public static interface EventConsumer<T> {
        public void accept(T var1, int var2, int var3);
    }
}


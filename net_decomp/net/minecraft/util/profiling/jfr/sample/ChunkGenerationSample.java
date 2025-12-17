/*
 * Decompiled with CFR 0.152.
 */
package net.minecraft.util.profiling.jfr.sample;

import java.lang.invoke.MethodHandle;
import java.lang.runtime.ObjectMethods;
import java.time.Duration;
import jdk.jfr.consumer.RecordedEvent;
import net.minecraft.util.math.ChunkPos;
import net.minecraft.util.math.ColumnPos;
import net.minecraft.util.profiling.jfr.sample.LongRunningSample;
import net.minecraft.world.chunk.ChunkStatus;

public record ChunkGenerationSample(Duration duration, ChunkPos chunkPos, ColumnPos centerPos, ChunkStatus chunkStatus, String worldKey) implements LongRunningSample
{
    public static ChunkGenerationSample fromEvent(RecordedEvent event) {
        return new ChunkGenerationSample(event.getDuration(), new ChunkPos(event.getInt("chunkPosX"), event.getInt("chunkPosX")), new ColumnPos(event.getInt("worldPosX"), event.getInt("worldPosZ")), ChunkStatus.byId(event.getString("status")), event.getString("level"));
    }

    @Override
    public final String toString() {
        return ObjectMethods.bootstrap("toString", new MethodHandle[]{ChunkGenerationSample.class, "duration;chunkPos;worldPos;status;level", "duration", "chunkPos", "centerPos", "chunkStatus", "worldKey"}, this);
    }

    @Override
    public final int hashCode() {
        return (int)ObjectMethods.bootstrap("hashCode", new MethodHandle[]{ChunkGenerationSample.class, "duration;chunkPos;worldPos;status;level", "duration", "chunkPos", "centerPos", "chunkStatus", "worldKey"}, this);
    }

    @Override
    public final boolean equals(Object o) {
        return (boolean)ObjectMethods.bootstrap("equals", new MethodHandle[]{ChunkGenerationSample.class, "duration;chunkPos;worldPos;status;level", "duration", "chunkPos", "centerPos", "chunkStatus", "worldKey"}, this, o);
    }
}


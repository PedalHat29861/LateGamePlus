/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  com.mojang.logging.LogUtils
 *  it.unimi.dsi.fastutil.longs.LongOpenHashSet
 *  it.unimi.dsi.fastutil.longs.LongSet
 *  org.slf4j.Logger
 */
package net.minecraft.world.storage;

import com.mojang.logging.LogUtils;
import it.unimi.dsi.fastutil.longs.LongOpenHashSet;
import it.unimi.dsi.fastutil.longs.LongSet;
import java.io.IOException;
import java.util.List;
import java.util.Objects;
import java.util.Optional;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.Executor;
import net.minecraft.entity.Entity;
import net.minecraft.entity.EntityType;
import net.minecraft.entity.SpawnReason;
import net.minecraft.nbt.NbtCompound;
import net.minecraft.nbt.NbtHelper;
import net.minecraft.nbt.NbtList;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.storage.NbtReadView;
import net.minecraft.storage.NbtWriteView;
import net.minecraft.storage.ReadView;
import net.minecraft.util.ErrorReporter;
import net.minecraft.util.math.ChunkPos;
import net.minecraft.util.thread.SimpleConsecutiveExecutor;
import net.minecraft.world.chunk.Chunk;
import net.minecraft.world.storage.ChunkDataAccess;
import net.minecraft.world.storage.ChunkDataList;
import net.minecraft.world.storage.StorageIoWorker;
import net.minecraft.world.storage.VersionedChunkStorage;
import org.slf4j.Logger;

public class EntityChunkDataAccess
implements ChunkDataAccess<Entity> {
    private static final Logger LOGGER = LogUtils.getLogger();
    private static final String ENTITIES_KEY = "Entities";
    private static final String POSITION_KEY = "Position";
    private final ServerWorld world;
    private final VersionedChunkStorage storage;
    private final LongSet emptyChunks = new LongOpenHashSet();
    private final SimpleConsecutiveExecutor taskExecutor;

    public EntityChunkDataAccess(VersionedChunkStorage storage, ServerWorld world, Executor executor) {
        this.storage = storage;
        this.world = world;
        this.taskExecutor = new SimpleConsecutiveExecutor(executor, "entity-deserializer");
    }

    @Override
    public CompletableFuture<ChunkDataList<Entity>> readChunkData(ChunkPos pos) {
        if (this.emptyChunks.contains(pos.toLong())) {
            return CompletableFuture.completedFuture(EntityChunkDataAccess.emptyDataList(pos));
        }
        CompletableFuture<Optional<NbtCompound>> completableFuture = this.storage.getNbt(pos);
        this.handleLoadFailure(completableFuture, pos);
        return completableFuture.thenApplyAsync(nbt -> {
            if (nbt.isEmpty()) {
                this.emptyChunks.add(pos.toLong());
                return EntityChunkDataAccess.emptyDataList(pos);
            }
            try {
                ChunkPos chunkPos2 = ((NbtCompound)nbt.get()).get(POSITION_KEY, ChunkPos.CODEC).orElseThrow();
                if (!Objects.equals(pos, chunkPos2)) {
                    LOGGER.error("Chunk file at {} is in the wrong location. (Expected {}, got {})", new Object[]{pos, pos, chunkPos2});
                    this.world.getServer().onChunkMisplacement(chunkPos2, pos, this.storage.getStorageKey());
                }
            }
            catch (Exception exception) {
                LOGGER.warn("Failed to parse chunk {} position info", (Object)pos, (Object)exception);
                this.world.getServer().onChunkLoadFailure(exception, this.storage.getStorageKey(), pos);
            }
            NbtCompound nbtCompound = this.storage.updateChunkNbt((NbtCompound)nbt.get(), -1);
            try (ErrorReporter.Logging logging = new ErrorReporter.Logging(Chunk.createErrorReporterContext(pos), LOGGER);){
                ReadView readView = NbtReadView.create(logging, this.world.getRegistryManager(), nbtCompound);
                ReadView.ListReadView listReadView = readView.getListReadView(ENTITIES_KEY);
                List<Entity> list = EntityType.streamFromData(listReadView, this.world, SpawnReason.LOAD).toList();
                ChunkDataList<Entity> chunkDataList = new ChunkDataList<Entity>(pos, list);
                return chunkDataList;
            }
        }, this.taskExecutor::send);
    }

    private static ChunkDataList<Entity> emptyDataList(ChunkPos pos) {
        return new ChunkDataList<Entity>(pos, List.of());
    }

    @Override
    public void writeChunkData(ChunkDataList<Entity> dataList) {
        ChunkPos chunkPos = dataList.getChunkPos();
        if (dataList.isEmpty()) {
            if (this.emptyChunks.add(chunkPos.toLong())) {
                this.handleSaveFailure(this.storage.set(chunkPos, StorageIoWorker.NULL_NBT_SUPPLIER), chunkPos);
            }
            return;
        }
        try (ErrorReporter.Logging logging = new ErrorReporter.Logging(Chunk.createErrorReporterContext(chunkPos), LOGGER);){
            NbtList nbtList = new NbtList();
            dataList.stream().forEach(entity -> {
                NbtWriteView nbtWriteView = NbtWriteView.create(logging.makeChild(entity.getErrorReporterContext()), entity.getRegistryManager());
                if (entity.saveData(nbtWriteView)) {
                    NbtCompound nbtCompound = nbtWriteView.getNbt();
                    nbtList.add(nbtCompound);
                }
            });
            NbtCompound nbtCompound = NbtHelper.putDataVersion(new NbtCompound());
            nbtCompound.put(ENTITIES_KEY, nbtList);
            nbtCompound.put(POSITION_KEY, ChunkPos.CODEC, chunkPos);
            this.handleSaveFailure(this.storage.setNbt(chunkPos, nbtCompound), chunkPos);
            this.emptyChunks.remove(chunkPos.toLong());
        }
    }

    private void handleSaveFailure(CompletableFuture<?> future, ChunkPos pos) {
        future.exceptionally(throwable -> {
            LOGGER.error("Failed to store entity chunk {}", (Object)pos, throwable);
            this.world.getServer().onChunkSaveFailure((Throwable)throwable, this.storage.getStorageKey(), pos);
            return null;
        });
    }

    private void handleLoadFailure(CompletableFuture<?> future, ChunkPos pos) {
        future.exceptionally(throwable -> {
            LOGGER.error("Failed to load entity chunk {}", (Object)pos, throwable);
            this.world.getServer().onChunkLoadFailure((Throwable)throwable, this.storage.getStorageKey(), pos);
            return null;
        });
    }

    @Override
    public void awaitAll(boolean sync) {
        this.storage.completeAll(sync).join();
        this.taskExecutor.runAll();
    }

    @Override
    public void close() throws IOException {
        this.storage.close();
    }
}


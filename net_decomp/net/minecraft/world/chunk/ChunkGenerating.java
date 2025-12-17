/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  com.mojang.logging.LogUtils
 *  org.slf4j.Logger
 */
package net.minecraft.world.chunk;

import com.mojang.logging.LogUtils;
import java.util.EnumSet;
import java.util.concurrent.CompletableFuture;
import net.minecraft.SharedConstants;
import net.minecraft.entity.EntityType;
import net.minecraft.entity.SpawnReason;
import net.minecraft.server.world.ServerLightingProvider;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.storage.NbtReadView;
import net.minecraft.storage.ReadView;
import net.minecraft.util.ErrorReporter;
import net.minecraft.util.collection.BoundedRegionArray;
import net.minecraft.util.math.ChunkPos;
import net.minecraft.world.ChunkRegion;
import net.minecraft.world.Heightmap;
import net.minecraft.world.chunk.AbstractChunkHolder;
import net.minecraft.world.chunk.BelowZeroRetrogen;
import net.minecraft.world.chunk.Chunk;
import net.minecraft.world.chunk.ChunkGenerationContext;
import net.minecraft.world.chunk.ChunkGenerationStep;
import net.minecraft.world.chunk.ChunkStatus;
import net.minecraft.world.chunk.ProtoChunk;
import net.minecraft.world.chunk.WorldChunk;
import net.minecraft.world.chunk.WrapperProtoChunk;
import net.minecraft.world.gen.chunk.Blender;
import org.slf4j.Logger;

public class ChunkGenerating {
    private static final Logger LOGGER = LogUtils.getLogger();

    private static boolean isLightOn(Chunk chunk) {
        return chunk.getStatus().isAtLeast(ChunkStatus.LIGHT) && chunk.isLightOn();
    }

    static CompletableFuture<Chunk> noop(ChunkGenerationContext context, ChunkGenerationStep step, BoundedRegionArray<AbstractChunkHolder> chunks, Chunk chunk) {
        return CompletableFuture.completedFuture(chunk);
    }

    static CompletableFuture<Chunk> generateStructures(ChunkGenerationContext context, ChunkGenerationStep step, BoundedRegionArray<AbstractChunkHolder> chunks, Chunk chunk) {
        ServerWorld serverWorld = context.world();
        if (serverWorld.getServer().getSaveProperties().getGeneratorOptions().shouldGenerateStructures()) {
            context.generator().setStructureStarts(serverWorld.getRegistryManager(), serverWorld.getChunkManager().getStructurePlacementCalculator(), serverWorld.getStructureAccessor(), chunk, context.structureManager(), serverWorld.getRegistryKey());
        }
        serverWorld.cacheStructures(chunk);
        return CompletableFuture.completedFuture(chunk);
    }

    static CompletableFuture<Chunk> loadStructures(ChunkGenerationContext context, ChunkGenerationStep step, BoundedRegionArray<AbstractChunkHolder> chunks, Chunk chunk) {
        context.world().cacheStructures(chunk);
        return CompletableFuture.completedFuture(chunk);
    }

    static CompletableFuture<Chunk> generateStructureReferences(ChunkGenerationContext context, ChunkGenerationStep step, BoundedRegionArray<AbstractChunkHolder> chunks, Chunk chunk) {
        ServerWorld serverWorld = context.world();
        ChunkRegion chunkRegion = new ChunkRegion(serverWorld, chunks, step, chunk);
        context.generator().addStructureReferences(chunkRegion, serverWorld.getStructureAccessor().forRegion(chunkRegion), chunk);
        return CompletableFuture.completedFuture(chunk);
    }

    static CompletableFuture<Chunk> populateBiomes(ChunkGenerationContext context, ChunkGenerationStep step, BoundedRegionArray<AbstractChunkHolder> chunks, Chunk chunk) {
        ServerWorld serverWorld = context.world();
        ChunkRegion chunkRegion = new ChunkRegion(serverWorld, chunks, step, chunk);
        return context.generator().populateBiomes(serverWorld.getChunkManager().getNoiseConfig(), Blender.getBlender(chunkRegion), serverWorld.getStructureAccessor().forRegion(chunkRegion), chunk);
    }

    static CompletableFuture<Chunk> populateNoise(ChunkGenerationContext context, ChunkGenerationStep step, BoundedRegionArray<AbstractChunkHolder> chunks, Chunk chunk) {
        ServerWorld serverWorld = context.world();
        ChunkRegion chunkRegion = new ChunkRegion(serverWorld, chunks, step, chunk);
        return context.generator().populateNoise(Blender.getBlender(chunkRegion), serverWorld.getChunkManager().getNoiseConfig(), serverWorld.getStructureAccessor().forRegion(chunkRegion), chunk).thenApply(populated -> {
            ProtoChunk protoChunk;
            BelowZeroRetrogen belowZeroRetrogen;
            if (populated instanceof ProtoChunk && (belowZeroRetrogen = (protoChunk = (ProtoChunk)populated).getBelowZeroRetrogen()) != null) {
                BelowZeroRetrogen.replaceOldBedrock(protoChunk);
                if (belowZeroRetrogen.hasMissingBedrock()) {
                    belowZeroRetrogen.fillColumnsWithAirIfMissingBedrock(protoChunk);
                }
            }
            return populated;
        });
    }

    static CompletableFuture<Chunk> buildSurface(ChunkGenerationContext context, ChunkGenerationStep step, BoundedRegionArray<AbstractChunkHolder> chunks, Chunk chunk) {
        ServerWorld serverWorld = context.world();
        ChunkRegion chunkRegion = new ChunkRegion(serverWorld, chunks, step, chunk);
        context.generator().buildSurface(chunkRegion, serverWorld.getStructureAccessor().forRegion(chunkRegion), serverWorld.getChunkManager().getNoiseConfig(), chunk);
        return CompletableFuture.completedFuture(chunk);
    }

    static CompletableFuture<Chunk> carve(ChunkGenerationContext context, ChunkGenerationStep step, BoundedRegionArray<AbstractChunkHolder> chunks, Chunk chunk) {
        ServerWorld serverWorld = context.world();
        ChunkRegion chunkRegion = new ChunkRegion(serverWorld, chunks, step, chunk);
        if (chunk instanceof ProtoChunk) {
            ProtoChunk protoChunk = (ProtoChunk)chunk;
            Blender.createCarvingMasks(chunkRegion, protoChunk);
        }
        context.generator().carve(chunkRegion, serverWorld.getSeed(), serverWorld.getChunkManager().getNoiseConfig(), serverWorld.getBiomeAccess(), serverWorld.getStructureAccessor().forRegion(chunkRegion), chunk);
        return CompletableFuture.completedFuture(chunk);
    }

    static CompletableFuture<Chunk> generateFeatures(ChunkGenerationContext context, ChunkGenerationStep step, BoundedRegionArray<AbstractChunkHolder> chunks, Chunk chunk) {
        ServerWorld serverWorld = context.world();
        Heightmap.populateHeightmaps(chunk, EnumSet.of(Heightmap.Type.MOTION_BLOCKING, Heightmap.Type.MOTION_BLOCKING_NO_LEAVES, Heightmap.Type.OCEAN_FLOOR, Heightmap.Type.WORLD_SURFACE));
        ChunkRegion chunkRegion = new ChunkRegion(serverWorld, chunks, step, chunk);
        if (!SharedConstants.DISABLE_FEATURES) {
            context.generator().generateFeatures(chunkRegion, chunk, serverWorld.getStructureAccessor().forRegion(chunkRegion));
        }
        Blender.tickLeavesAndFluids(chunkRegion, chunk);
        return CompletableFuture.completedFuture(chunk);
    }

    static CompletableFuture<Chunk> initializeLight(ChunkGenerationContext context, ChunkGenerationStep step, BoundedRegionArray<AbstractChunkHolder> chunks, Chunk chunk) {
        ServerLightingProvider serverLightingProvider = context.lightingProvider();
        chunk.refreshSurfaceY();
        ((ProtoChunk)chunk).setLightingProvider(serverLightingProvider);
        boolean bl = ChunkGenerating.isLightOn(chunk);
        return serverLightingProvider.initializeLight(chunk, bl);
    }

    static CompletableFuture<Chunk> light(ChunkGenerationContext context, ChunkGenerationStep step, BoundedRegionArray<AbstractChunkHolder> chunks, Chunk chunk) {
        boolean bl = ChunkGenerating.isLightOn(chunk);
        return context.lightingProvider().light(chunk, bl);
    }

    static CompletableFuture<Chunk> generateEntities(ChunkGenerationContext context, ChunkGenerationStep step, BoundedRegionArray<AbstractChunkHolder> chunks, Chunk chunk) {
        if (!chunk.hasBelowZeroRetrogen()) {
            context.generator().populateEntities(new ChunkRegion(context.world(), chunks, step, chunk));
        }
        return CompletableFuture.completedFuture(chunk);
    }

    static CompletableFuture<Chunk> convertToFullChunk(ChunkGenerationContext context, ChunkGenerationStep step, BoundedRegionArray<AbstractChunkHolder> chunks, Chunk chunk) {
        ChunkPos chunkPos = chunk.getPos();
        AbstractChunkHolder abstractChunkHolder = chunks.get(chunkPos.x, chunkPos.z);
        return CompletableFuture.supplyAsync(() -> {
            WorldChunk worldChunk2;
            ProtoChunk protoChunk = (ProtoChunk)chunk;
            ServerWorld serverWorld = context.world();
            if (protoChunk instanceof WrapperProtoChunk) {
                WrapperProtoChunk wrapperProtoChunk = (WrapperProtoChunk)protoChunk;
                worldChunk2 = wrapperProtoChunk.getWrappedChunk();
            } else {
                worldChunk2 = new WorldChunk(serverWorld, protoChunk, worldChunk -> {
                    try (ErrorReporter.Logging logging = new ErrorReporter.Logging(chunk.getErrorReporterContext(), LOGGER);){
                        ChunkGenerating.addEntities(serverWorld, NbtReadView.createList(logging, serverWorld.getRegistryManager(), protoChunk.getEntities()));
                    }
                });
                abstractChunkHolder.replaceWith(new WrapperProtoChunk(worldChunk2, false));
            }
            worldChunk2.setLevelTypeProvider(abstractChunkHolder::getLevelType);
            worldChunk2.loadEntities();
            worldChunk2.setLoadedToWorld(true);
            worldChunk2.updateAllBlockEntities();
            worldChunk2.addChunkTickSchedulers(serverWorld);
            worldChunk2.setUnsavedListener(context.unsavedListener());
            return worldChunk2;
        }, context.mainThreadExecutor());
    }

    private static void addEntities(ServerWorld world, ReadView.ListReadView entities) {
        if (!entities.isEmpty()) {
            world.addEntities(EntityType.streamFromData(entities, world, SpawnReason.LOAD));
        }
    }
}


/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  org.jspecify.annotations.Nullable
 */
package net.minecraft.server.network;

import java.util.Optional;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.Executor;
import java.util.function.Supplier;
import net.minecraft.SharedConstants;
import net.minecraft.block.Block;
import net.minecraft.block.BlockState;
import net.minecraft.entity.EntityDimensions;
import net.minecraft.entity.EntityType;
import net.minecraft.server.world.ChunkTicketType;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.util.crash.CrashException;
import net.minecraft.util.crash.CrashReport;
import net.minecraft.util.crash.CrashReportSection;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.ChunkPos;
import net.minecraft.util.math.ChunkSectionPos;
import net.minecraft.util.math.Direction;
import net.minecraft.util.math.MathHelper;
import net.minecraft.util.math.Vec3d;
import net.minecraft.util.math.random.Random;
import net.minecraft.world.CollisionView;
import net.minecraft.world.GameMode;
import net.minecraft.world.Heightmap;
import net.minecraft.world.chunk.WorldChunk;
import net.minecraft.world.rule.GameRules;
import org.jspecify.annotations.Nullable;

public class SpawnLocating {
    private static final EntityDimensions PLAYER_DIMENSIONS = EntityType.PLAYER.getDimensions();
    private static final int MAX_SPAWN_AREA = 1024;
    private final ServerWorld world;
    private final BlockPos spawnPos;
    private final int spawnRadius;
    private final int spawnArea;
    private final int shiftAmount;
    private final int offset;
    private int attempt;
    private final CompletableFuture<Vec3d> future = new CompletableFuture();

    private SpawnLocating(ServerWorld world, BlockPos spawnPos, int spawnRadius) {
        this.world = world;
        this.spawnPos = spawnPos;
        this.spawnRadius = spawnRadius;
        long l = (long)spawnRadius * 2L + 1L;
        this.spawnArea = (int)Math.min(1024L, l * l);
        this.shiftAmount = SpawnLocating.calculateShiftAmount(this.spawnArea);
        this.offset = Random.create().nextInt(this.spawnArea);
    }

    public static CompletableFuture<Vec3d> locateSpawnPos(ServerWorld world, BlockPos spawnPos) {
        if (!world.getDimension().hasSkyLight() || world.getServer().getSaveProperties().getGameMode() == GameMode.ADVENTURE) {
            return CompletableFuture.completedFuture(SpawnLocating.findPosInColumn(world, spawnPos));
        }
        int i = Math.max(0, world.getGameRules().getValue(GameRules.RESPAWN_RADIUS));
        int j = MathHelper.floor(world.getWorldBorder().getDistanceInsideBorder(spawnPos.getX(), spawnPos.getZ()));
        if (j < i) {
            i = j;
        }
        if (j <= 1) {
            i = 1;
        }
        SpawnLocating spawnLocating = new SpawnLocating(world, spawnPos, i);
        spawnLocating.scheduleNextSearch();
        return spawnLocating.future;
    }

    private void scheduleNextSearch() {
        int i;
        if ((i = this.attempt++) < this.spawnArea) {
            int j = (this.offset + this.shiftAmount * i) % this.spawnArea;
            int k = j % (this.spawnRadius * 2 + 1);
            int l = j / (this.spawnRadius * 2 + 1);
            int m = this.spawnPos.getX() + k - this.spawnRadius;
            int n = this.spawnPos.getZ() + l - this.spawnRadius;
            this.scheduleSearch(m, n, i, () -> {
                BlockPos blockPos = SpawnLocating.findOverworldSpawn(this.world, m, n);
                if (blockPos != null && SpawnLocating.isSpaceEmpty(this.world, blockPos)) {
                    return Optional.of(Vec3d.ofBottomCenter(blockPos));
                }
                return Optional.empty();
            });
        } else {
            this.scheduleSearch(this.spawnPos.getX(), this.spawnPos.getZ(), i, () -> Optional.of(SpawnLocating.findPosInColumn(this.world, this.spawnPos)));
        }
    }

    private static Vec3d findPosInColumn(CollisionView world, BlockPos pos) {
        BlockPos.Mutable mutable = pos.mutableCopy();
        while (!SpawnLocating.isSpaceEmpty(world, mutable) && mutable.getY() < world.getTopYInclusive()) {
            mutable.move(Direction.UP);
        }
        mutable.move(Direction.DOWN);
        while (SpawnLocating.isSpaceEmpty(world, mutable) && mutable.getY() > world.getBottomY()) {
            mutable.move(Direction.DOWN);
        }
        mutable.move(Direction.UP);
        return Vec3d.ofBottomCenter(mutable);
    }

    private static boolean isSpaceEmpty(CollisionView world, BlockPos pos) {
        return world.isSpaceEmpty(null, PLAYER_DIMENSIONS.getBoxAt(pos.toBottomCenterPos()), true);
    }

    private static int calculateShiftAmount(int spawnArea) {
        return spawnArea <= 16 ? spawnArea - 1 : 17;
    }

    private void scheduleSearch(int x, int z, int index, Supplier<Optional<Vec3d>> spawnFinder) {
        if (this.future.isDone()) {
            return;
        }
        int i = ChunkSectionPos.getSectionCoord(x);
        int j = ChunkSectionPos.getSectionCoord(z);
        this.world.getChunkManager().addChunkLoadingTicket(ChunkTicketType.SPAWN_SEARCH, new ChunkPos(i, j), 0).whenCompleteAsync((object, throwable) -> {
            if (throwable == null) {
                try {
                    Optional optional = (Optional)spawnFinder.get();
                    if (optional.isPresent()) {
                        this.future.complete((Vec3d)optional.get());
                    } else {
                        this.scheduleNextSearch();
                    }
                }
                catch (Throwable throwable2) {
                    throwable = throwable2;
                }
            }
            if (throwable != null) {
                CrashReport crashReport = CrashReport.create(throwable, "Searching for spawn");
                CrashReportSection crashReportSection = crashReport.addElement("Spawn Lookup");
                crashReportSection.add("Origin", this.spawnPos::toString);
                crashReportSection.add("Radius", () -> Integer.toString(this.spawnRadius));
                crashReportSection.add("Candidate", () -> "[" + x + "," + z + "]");
                crashReportSection.add("Progress", () -> index + " out of " + this.spawnArea);
                this.future.completeExceptionally(new CrashException(crashReport));
            }
        }, (Executor)this.world.getServer());
    }

    protected static @Nullable BlockPos findOverworldSpawn(ServerWorld world, int x, int z) {
        int i;
        boolean bl = world.getDimension().hasCeiling();
        WorldChunk worldChunk = world.getChunk(ChunkSectionPos.getSectionCoord(x), ChunkSectionPos.getSectionCoord(z));
        int n = i = bl ? world.getChunkManager().getChunkGenerator().getSpawnHeight(world) : worldChunk.sampleHeightmap(Heightmap.Type.MOTION_BLOCKING, x & 0xF, z & 0xF);
        if (i < world.getBottomY()) {
            return null;
        }
        int j = worldChunk.sampleHeightmap(Heightmap.Type.WORLD_SURFACE, x & 0xF, z & 0xF);
        if (j <= i && j > worldChunk.sampleHeightmap(Heightmap.Type.OCEAN_FLOOR, x & 0xF, z & 0xF)) {
            return null;
        }
        BlockPos.Mutable mutable = new BlockPos.Mutable();
        for (int k = i + 1; k >= world.getBottomY(); --k) {
            mutable.set(x, k, z);
            BlockState blockState = world.getBlockState(mutable);
            if (!blockState.getFluidState().isEmpty()) break;
            if (!Block.isFaceFullSquare(blockState.getCollisionShape(world, mutable), Direction.UP)) continue;
            return ((BlockPos)mutable.up()).toImmutable();
        }
        return null;
    }

    public static @Nullable BlockPos findServerSpawnPoint(ServerWorld world, ChunkPos chunkPos) {
        if (SharedConstants.isOutsideGenerationArea(chunkPos)) {
            return null;
        }
        for (int i = chunkPos.getStartX(); i <= chunkPos.getEndX(); ++i) {
            for (int j = chunkPos.getStartZ(); j <= chunkPos.getEndZ(); ++j) {
                BlockPos blockPos = SpawnLocating.findOverworldSpawn(world, i, j);
                if (blockPos == null) continue;
                return blockPos;
            }
        }
        return null;
    }
}


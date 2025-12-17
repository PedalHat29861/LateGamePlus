/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  com.mojang.logging.LogUtils
 *  org.jspecify.annotations.Nullable
 *  org.slf4j.Logger
 */
package net.minecraft.server.network;

import com.mojang.logging.LogUtils;
import java.lang.runtime.SwitchBootstraps;
import java.util.Optional;
import java.util.concurrent.CompletableFuture;
import java.util.function.Consumer;
import net.minecraft.network.ClientConnection;
import net.minecraft.network.packet.Packet;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.PlayerConfigEntry;
import net.minecraft.server.network.ConnectedClientData;
import net.minecraft.server.network.ServerPlayerConfigurationTask;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.server.network.SpawnLocating;
import net.minecraft.server.world.ChunkTicketType;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.storage.NbtReadView;
import net.minecraft.storage.ReadView;
import net.minecraft.util.ErrorReporter;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.ChunkPos;
import net.minecraft.util.math.Vec2f;
import net.minecraft.util.math.Vec3d;
import net.minecraft.world.WorldProperties;
import net.minecraft.world.chunk.ChunkLoadProgress;
import net.minecraft.world.chunk.ChunkLoadingCounter;
import org.jspecify.annotations.Nullable;
import org.slf4j.Logger;

public class PrepareSpawnTask
implements ServerPlayerConfigurationTask {
    static final Logger LOGGER = LogUtils.getLogger();
    public static final ServerPlayerConfigurationTask.Key KEY = new ServerPlayerConfigurationTask.Key("prepare_spawn");
    public static final int CHUNK_LOAD_RADIUS = 3;
    final MinecraftServer server;
    final PlayerConfigEntry player;
    final ChunkLoadProgress chunkLoadProgress;
    private @Nullable Stage stage;

    public PrepareSpawnTask(MinecraftServer server, PlayerConfigEntry player) {
        this.server = server;
        this.player = player;
        this.chunkLoadProgress = server.getChunkLoadProgress();
    }

    @Override
    public void sendPacket(Consumer<Packet<?>> sender) {
        try (ErrorReporter.Logging logging = new ErrorReporter.Logging(LOGGER);){
            Optional<ReadView> optional = this.server.getPlayerManager().loadPlayerData(this.player).map(nbt -> NbtReadView.create(logging, this.server.getRegistryManager(), nbt));
            ServerPlayerEntity.SavePos savePos = optional.flatMap(view -> view.read(ServerPlayerEntity.SavePos.CODEC)).orElse(ServerPlayerEntity.SavePos.EMPTY);
            WorldProperties.SpawnPoint spawnPoint = this.server.getSaveProperties().getMainWorldProperties().getSpawnPoint();
            ServerWorld serverWorld = savePos.dimension().map(this.server::getWorld).orElseGet(() -> {
                ServerWorld serverWorld = this.server.getWorld(spawnPoint.getDimension());
                return serverWorld != null ? serverWorld : this.server.getOverworld();
            });
            CompletableFuture completableFuture = savePos.position().map(CompletableFuture::completedFuture).orElseGet(() -> SpawnLocating.locateSpawnPos(serverWorld, spawnPoint.getPos()));
            Vec2f vec2f = savePos.rotation().orElse(new Vec2f(spawnPoint.yaw(), spawnPoint.pitch()));
            this.stage = new LoadPlayerChunks(serverWorld, completableFuture, vec2f);
        }
    }

    @Override
    public boolean hasFinished() {
        Stage stage = this.stage;
        int n = 0;
        return switch (SwitchBootstraps.typeSwitch("typeSwitch", new Object[]{LoadPlayerChunks.class, PlayerSpawn.class}, (Object)stage, n)) {
            default -> throw new MatchException(null, null);
            case 0 -> {
                LoadPlayerChunks loadPlayerChunks = (LoadPlayerChunks)stage;
                PlayerSpawn playerSpawn = loadPlayerChunks.tryFinish();
                if (playerSpawn != null) {
                    this.stage = playerSpawn;
                    yield true;
                }
                yield false;
            }
            case 1 -> {
                PlayerSpawn playerSpawn = (PlayerSpawn)stage;
                yield true;
            }
            case -1 -> false;
        };
    }

    public ServerPlayerEntity onReady(ClientConnection connection, ConnectedClientData clientData) {
        Stage stage = this.stage;
        if (stage instanceof PlayerSpawn) {
            PlayerSpawn playerSpawn = (PlayerSpawn)stage;
            return playerSpawn.onReady(connection, clientData);
        }
        throw new IllegalStateException("Player spawn was not ready");
    }

    public void tick() {
        Stage stage = this.stage;
        if (stage instanceof PlayerSpawn) {
            PlayerSpawn playerSpawn = (PlayerSpawn)stage;
            playerSpawn.tick();
        }
    }

    public void onDisconnected() {
        Stage stage = this.stage;
        if (stage instanceof LoadPlayerChunks) {
            LoadPlayerChunks loadPlayerChunks = (LoadPlayerChunks)stage;
            loadPlayerChunks.cancel();
        }
        this.stage = null;
    }

    @Override
    public ServerPlayerConfigurationTask.Key getKey() {
        return KEY;
    }

    final class LoadPlayerChunks
    implements Stage {
        private final ServerWorld world;
        private final CompletableFuture<Vec3d> spawnPos;
        private final Vec2f rotation;
        private @Nullable CompletableFuture<?> chunkLoadingFuture;
        private final ChunkLoadingCounter chunkCounter = new ChunkLoadingCounter();

        LoadPlayerChunks(ServerWorld world, CompletableFuture<Vec3d> spawnPos, Vec2f rotation) {
            this.world = world;
            this.spawnPos = spawnPos;
            this.rotation = rotation;
        }

        public void cancel() {
            this.spawnPos.cancel(false);
        }

        public @Nullable PlayerSpawn tryFinish() {
            if (!this.spawnPos.isDone()) {
                return null;
            }
            Vec3d vec3d = this.spawnPos.join();
            if (this.chunkLoadingFuture == null) {
                ChunkPos chunkPos = new ChunkPos(BlockPos.ofFloored(vec3d));
                this.chunkCounter.load(this.world, () -> {
                    this.chunkLoadingFuture = this.world.getChunkManager().addChunkLoadingTicket(ChunkTicketType.PLAYER_SPAWN, chunkPos, 3);
                });
                PrepareSpawnTask.this.chunkLoadProgress.init(ChunkLoadProgress.Stage.LOAD_PLAYER_CHUNKS, this.chunkCounter.getTotalChunks());
                PrepareSpawnTask.this.chunkLoadProgress.initSpawnPos(this.world.getRegistryKey(), chunkPos);
            }
            PrepareSpawnTask.this.chunkLoadProgress.progress(ChunkLoadProgress.Stage.LOAD_PLAYER_CHUNKS, this.chunkCounter.getFullChunks(), this.chunkCounter.getTotalChunks());
            if (!this.chunkLoadingFuture.isDone()) {
                return null;
            }
            PrepareSpawnTask.this.chunkLoadProgress.finish(ChunkLoadProgress.Stage.LOAD_PLAYER_CHUNKS);
            return new PlayerSpawn(this.world, vec3d, this.rotation);
        }
    }

    static sealed interface Stage
    permits LoadPlayerChunks, PlayerSpawn {
    }

    final class PlayerSpawn
    implements Stage {
        private final ServerWorld world;
        private final Vec3d spawnPos;
        private final Vec2f rotation;

        PlayerSpawn(ServerWorld world, Vec3d spawnPos, Vec2f rotation) {
            this.world = world;
            this.spawnPos = spawnPos;
            this.rotation = rotation;
        }

        public void tick() {
            this.world.getChunkManager().addTicket(ChunkTicketType.PLAYER_SPAWN, new ChunkPos(BlockPos.ofFloored(this.spawnPos)), 3);
        }

        public ServerPlayerEntity onReady(ClientConnection connection, ConnectedClientData clientData) {
            ChunkPos chunkPos = new ChunkPos(BlockPos.ofFloored(this.spawnPos));
            this.world.loadChunks(chunkPos, 3);
            ServerPlayerEntity serverPlayerEntity = new ServerPlayerEntity(PrepareSpawnTask.this.server, this.world, clientData.gameProfile(), clientData.syncedOptions());
            try (ErrorReporter.Logging logging = new ErrorReporter.Logging(serverPlayerEntity.getErrorReporterContext(), LOGGER);){
                Optional<ReadView> optional = PrepareSpawnTask.this.server.getPlayerManager().loadPlayerData(PrepareSpawnTask.this.player).map(playerData -> NbtReadView.create(logging, PrepareSpawnTask.this.server.getRegistryManager(), playerData));
                optional.ifPresent(serverPlayerEntity::readData);
                serverPlayerEntity.refreshPositionAndAngles(this.spawnPos, this.rotation.x, this.rotation.y);
                PrepareSpawnTask.this.server.getPlayerManager().onPlayerConnect(connection, serverPlayerEntity, clientData);
                optional.ifPresent(playerData -> {
                    serverPlayerEntity.readEnderPearls((ReadView)playerData);
                    serverPlayerEntity.readRootVehicle((ReadView)playerData);
                });
                ServerPlayerEntity serverPlayerEntity2 = serverPlayerEntity;
                return serverPlayerEntity2;
            }
        }
    }
}


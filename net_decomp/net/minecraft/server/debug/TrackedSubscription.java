/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  it.unimi.dsi.fastutil.objects.ObjectOpenHashSet
 *  org.jspecify.annotations.Nullable
 */
package net.minecraft.server.debug;

import it.unimi.dsi.fastutil.objects.ObjectOpenHashSet;
import java.util.HashMap;
import java.util.Map;
import java.util.Objects;
import java.util.Set;
import java.util.UUID;
import java.util.function.BiConsumer;
import net.minecraft.entity.Entity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.network.listener.ClientPlayPacketListener;
import net.minecraft.network.packet.Packet;
import net.minecraft.network.packet.s2c.play.BlockValueDebugS2CPacket;
import net.minecraft.network.packet.s2c.play.ChunkValueDebugS2CPacket;
import net.minecraft.network.packet.s2c.play.EntityValueDebugS2CPacket;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.server.world.ServerChunkLoadingManager;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.util.Unit;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.ChunkPos;
import net.minecraft.util.math.ChunkSectionPos;
import net.minecraft.world.debug.DebugSubscriptionType;
import net.minecraft.world.debug.DebugSubscriptionTypes;
import net.minecraft.world.debug.DebugTrackable;
import net.minecraft.world.debug.data.PoiDebugData;
import net.minecraft.world.poi.PointOfInterest;
import net.minecraft.world.poi.PointOfInterestStorage;
import org.jspecify.annotations.Nullable;

public abstract class TrackedSubscription<T> {
    protected final DebugSubscriptionType<T> type;
    private final Set<UUID> subscribingPlayers = new ObjectOpenHashSet();

    public TrackedSubscription(DebugSubscriptionType<T> type) {
        this.type = type;
    }

    public final void refreshTracking(ServerWorld world) {
        for (ServerPlayerEntity serverPlayerEntity : world.getPlayers()) {
            boolean bl = this.subscribingPlayers.contains(serverPlayerEntity.getUuid());
            boolean bl2 = serverPlayerEntity.getSubscribedTypes().contains(this.type);
            if (bl2 == bl) continue;
            if (bl2) {
                this.startTracking(serverPlayerEntity);
                continue;
            }
            this.subscribingPlayers.remove(serverPlayerEntity.getUuid());
        }
        this.subscribingPlayers.removeIf(uuid -> world.getPlayerByUuid((UUID)uuid) == null);
        if (!this.subscribingPlayers.isEmpty()) {
            this.sendUpdate(world);
        }
    }

    private void startTracking(ServerPlayerEntity player) {
        this.subscribingPlayers.add(player.getUuid());
        player.getChunkFilter().forEach(chunkPos -> {
            if (!serverPlayerEntity.networkHandler.chunkDataSender.isInNextBatch(chunkPos.toLong())) {
                this.sendInitialIfSubscribed(player, (ChunkPos)chunkPos);
            }
        });
        player.getEntityWorld().getChunkManager().chunkLoadingManager.forEachEntityTrackedBy(player, entity -> this.sendInitialIfSubscribed(player, (Entity)entity));
    }

    protected final void sendToTrackingPlayers(ServerWorld world, ChunkPos chunkPos, Packet<? super ClientPlayPacketListener> packet) {
        ServerChunkLoadingManager serverChunkLoadingManager = world.getChunkManager().chunkLoadingManager;
        for (UUID uUID : this.subscribingPlayers) {
            ServerPlayerEntity serverPlayerEntity;
            PlayerEntity playerEntity = world.getPlayerByUuid(uUID);
            if (!(playerEntity instanceof ServerPlayerEntity) || !serverChunkLoadingManager.isTracked(serverPlayerEntity = (ServerPlayerEntity)playerEntity, chunkPos.x, chunkPos.z)) continue;
            serverPlayerEntity.networkHandler.sendPacket(packet);
        }
    }

    protected final void sendToTrackingPlayers(ServerWorld world, Entity entity, Packet<? super ClientPlayPacketListener> packet) {
        ServerChunkLoadingManager serverChunkLoadingManager = world.getChunkManager().chunkLoadingManager;
        serverChunkLoadingManager.sendToOtherNearbyPlayersIf(entity, packet, player -> this.subscribingPlayers.contains(player.getUuid()));
    }

    public final void sendInitialIfSubscribed(ServerPlayerEntity player, ChunkPos chunkPos) {
        if (this.subscribingPlayers.contains(player.getUuid())) {
            this.sendInitial(player, chunkPos);
        }
    }

    public final void sendInitialIfSubscribed(ServerPlayerEntity player, Entity entity) {
        if (this.subscribingPlayers.contains(player.getUuid())) {
            this.sendInitial(player, entity);
        }
    }

    protected void clear() {
    }

    protected void sendUpdate(ServerWorld world) {
    }

    protected void sendInitial(ServerPlayerEntity player, ChunkPos chunkPos) {
    }

    protected void sendInitial(ServerPlayerEntity player, Entity entity) {
    }

    public static class TrackedVillageSections
    extends TrackedSubscription<Unit> {
        public TrackedVillageSections() {
            super(DebugSubscriptionTypes.VILLAGE_SECTIONS);
        }

        @Override
        protected void sendInitial(ServerPlayerEntity player, ChunkPos chunkPos) {
            ServerWorld serverWorld = player.getEntityWorld();
            PointOfInterestStorage pointOfInterestStorage = serverWorld.getPointOfInterestStorage();
            pointOfInterestStorage.getInChunk(type -> true, chunkPos, PointOfInterestStorage.OccupationStatus.ANY).forEach(poi -> {
                ChunkSectionPos chunkSectionPos = ChunkSectionPos.from(poi.getPos());
                TrackedVillageSections.forEachSurrounding(serverWorld, chunkSectionPos, (sectionPos, nearOccupiedPoi) -> {
                    BlockPos blockPos = sectionPos.getCenterPos();
                    serverPlayerEntity.networkHandler.sendPacket(new BlockValueDebugS2CPacket(blockPos, this.type.optionalValueFor(nearOccupiedPoi != false ? Unit.INSTANCE : null)));
                });
            });
        }

        public void onPoiAdded(ServerWorld world, PointOfInterest poi) {
            this.handlePoiUpdate(world, poi.getPos());
        }

        public void onPoiRemoved(ServerWorld world, BlockPos pos) {
            this.handlePoiUpdate(world, pos);
        }

        private void handlePoiUpdate(ServerWorld world, BlockPos pos) {
            TrackedVillageSections.forEachSurrounding(world, ChunkSectionPos.from(pos), (sectionPos, nearOccupiedPoi) -> {
                BlockPos blockPos = sectionPos.getCenterPos();
                if (nearOccupiedPoi.booleanValue()) {
                    this.sendToTrackingPlayers(world, new ChunkPos(blockPos), (Packet<ClientPlayPacketListener>)new BlockValueDebugS2CPacket(blockPos, this.type.optionalValueFor(Unit.INSTANCE)));
                } else {
                    this.sendToTrackingPlayers(world, new ChunkPos(blockPos), (Packet<ClientPlayPacketListener>)new BlockValueDebugS2CPacket(blockPos, this.type.optionalValueFor()));
                }
            });
        }

        private static void forEachSurrounding(ServerWorld world, ChunkSectionPos sectionPos, BiConsumer<ChunkSectionPos, Boolean> action) {
            for (int i = -1; i <= 1; ++i) {
                for (int j = -1; j <= 1; ++j) {
                    for (int k = -1; k <= 1; ++k) {
                        ChunkSectionPos chunkSectionPos = sectionPos.add(j, k, i);
                        if (world.isNearOccupiedPointOfInterest(chunkSectionPos.getCenterPos())) {
                            action.accept(chunkSectionPos, true);
                            continue;
                        }
                        action.accept(chunkSectionPos, false);
                    }
                }
            }
        }
    }

    public static class TrackedPoi
    extends TrackedSubscription<PoiDebugData> {
        public TrackedPoi() {
            super(DebugSubscriptionTypes.POIS);
        }

        @Override
        protected void sendInitial(ServerPlayerEntity player, ChunkPos chunkPos) {
            ServerWorld serverWorld = player.getEntityWorld();
            PointOfInterestStorage pointOfInterestStorage = serverWorld.getPointOfInterestStorage();
            pointOfInterestStorage.getInChunk(type -> true, chunkPos, PointOfInterestStorage.OccupationStatus.ANY).forEach(poi -> serverPlayerEntity.networkHandler.sendPacket(new BlockValueDebugS2CPacket(poi.getPos(), this.type.optionalValueFor(new PoiDebugData((PointOfInterest)poi)))));
        }

        public void onPoiAdded(ServerWorld world, PointOfInterest poi) {
            this.sendToTrackingPlayers(world, new ChunkPos(poi.getPos()), (Packet<ClientPlayPacketListener>)new BlockValueDebugS2CPacket(poi.getPos(), this.type.optionalValueFor(new PoiDebugData(poi))));
        }

        public void onPoiRemoved(ServerWorld world, BlockPos pos) {
            this.sendToTrackingPlayers(world, new ChunkPos(pos), (Packet<ClientPlayPacketListener>)new BlockValueDebugS2CPacket(pos, this.type.optionalValueFor()));
        }

        public void onPoiUpdated(ServerWorld world, BlockPos pos) {
            this.sendToTrackingPlayers(world, new ChunkPos(pos), (Packet<ClientPlayPacketListener>)new BlockValueDebugS2CPacket(pos, this.type.optionalValueFor(world.getPointOfInterestStorage().getDebugData(pos))));
        }
    }

    static class UpdateQuerier<T> {
        private final DebugTrackable.DebugDataSupplier<T> dataSupplier;
        @Nullable T lastData;

        UpdateQuerier(DebugTrackable.DebugDataSupplier<T> dataSupplier) {
            this.dataSupplier = dataSupplier;
        }

        public @Nullable DebugSubscriptionType.OptionalValue<T> queryUpdate(DebugSubscriptionType<T> type) {
            T object = this.dataSupplier.get();
            if (!Objects.equals(object, this.lastData)) {
                this.lastData = object;
                return type.optionalValueFor(object);
            }
            return null;
        }
    }

    public static class UpdateTrackedSubscription<T>
    extends TrackedSubscription<T> {
        private final Map<ChunkPos, UpdateQuerier<T>> trackedChunks = new HashMap<ChunkPos, UpdateQuerier<T>>();
        private final Map<BlockPos, UpdateQuerier<T>> trackedBlockEntities = new HashMap<BlockPos, UpdateQuerier<T>>();
        private final Map<UUID, UpdateQuerier<T>> trackedEntities = new HashMap<UUID, UpdateQuerier<T>>();

        public UpdateTrackedSubscription(DebugSubscriptionType<T> debugSubscriptionType) {
            super(debugSubscriptionType);
        }

        @Override
        protected void clear() {
            this.trackedChunks.clear();
            this.trackedBlockEntities.clear();
            this.trackedEntities.clear();
        }

        @Override
        protected void sendUpdate(ServerWorld world) {
            DebugSubscriptionType.OptionalValue<T> optionalValue;
            for (Map.Entry<ChunkPos, UpdateQuerier<T>> entry : this.trackedChunks.entrySet()) {
                optionalValue = entry.getValue().queryUpdate(this.type);
                if (optionalValue == null) continue;
                ChunkPos chunkPos = entry.getKey();
                this.sendToTrackingPlayers(world, chunkPos, (Packet<ClientPlayPacketListener>)new ChunkValueDebugS2CPacket(chunkPos, optionalValue));
            }
            for (Map.Entry<Object, UpdateQuerier<T>> entry : this.trackedBlockEntities.entrySet()) {
                optionalValue = entry.getValue().queryUpdate(this.type);
                if (optionalValue == null) continue;
                BlockPos blockPos = (BlockPos)entry.getKey();
                ChunkPos chunkPos2 = new ChunkPos(blockPos);
                this.sendToTrackingPlayers(world, chunkPos2, (Packet<ClientPlayPacketListener>)new BlockValueDebugS2CPacket(blockPos, optionalValue));
            }
            for (Map.Entry<Object, UpdateQuerier<T>> entry : this.trackedEntities.entrySet()) {
                optionalValue = entry.getValue().queryUpdate(this.type);
                if (optionalValue == null) continue;
                Entity entity = Objects.requireNonNull(world.getEntity((UUID)entry.getKey()));
                this.sendToTrackingPlayers(world, entity, (Packet<ClientPlayPacketListener>)new EntityValueDebugS2CPacket(entity.getId(), optionalValue));
            }
        }

        public void trackChunk(ChunkPos chunkPos, DebugTrackable.DebugDataSupplier<T> dataSupplier) {
            this.trackedChunks.put(chunkPos, new UpdateQuerier<T>(dataSupplier));
        }

        public void trackBlockEntity(BlockPos chunkPos, DebugTrackable.DebugDataSupplier<T> dataSupplier) {
            this.trackedBlockEntities.put(chunkPos, new UpdateQuerier<T>(dataSupplier));
        }

        public void trackEntity(UUID uuid, DebugTrackable.DebugDataSupplier<T> dataSupplier) {
            this.trackedEntities.put(uuid, new UpdateQuerier<T>(dataSupplier));
        }

        public void untrackChunk(ChunkPos chunkPos) {
            this.trackedChunks.remove(chunkPos);
            this.trackedBlockEntities.keySet().removeIf(chunkPos::contains);
        }

        public void untrackBlockEntity(ServerWorld world, BlockPos pos) {
            UpdateQuerier<T> updateQuerier = this.trackedBlockEntities.remove(pos);
            if (updateQuerier != null) {
                ChunkPos chunkPos = new ChunkPos(pos);
                this.sendToTrackingPlayers(world, chunkPos, (Packet<ClientPlayPacketListener>)new BlockValueDebugS2CPacket(pos, this.type.optionalValueFor()));
            }
        }

        public void untrackEntity(Entity entity) {
            this.trackedEntities.remove(entity.getUuid());
        }

        @Override
        protected void sendInitial(ServerPlayerEntity player, ChunkPos chunkPos) {
            UpdateQuerier<T> updateQuerier = this.trackedChunks.get(chunkPos);
            if (updateQuerier != null && updateQuerier.lastData != null) {
                player.networkHandler.sendPacket(new ChunkValueDebugS2CPacket(chunkPos, this.type.optionalValueFor(updateQuerier.lastData)));
            }
            for (Map.Entry<BlockPos, UpdateQuerier<T>> entry : this.trackedBlockEntities.entrySet()) {
                BlockPos blockPos;
                Object object = entry.getValue().lastData;
                if (object == null || !chunkPos.contains(blockPos = entry.getKey())) continue;
                player.networkHandler.sendPacket(new BlockValueDebugS2CPacket(blockPos, this.type.optionalValueFor(object)));
            }
        }

        @Override
        protected void sendInitial(ServerPlayerEntity player, Entity entity) {
            UpdateQuerier<T> updateQuerier = this.trackedEntities.get(entity.getUuid());
            if (updateQuerier != null && updateQuerier.lastData != null) {
                player.networkHandler.sendPacket(new EntityValueDebugS2CPacket(entity.getId(), this.type.optionalValueFor(updateQuerier.lastData)));
            }
        }
    }
}


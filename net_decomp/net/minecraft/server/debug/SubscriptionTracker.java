/*
 * Decompiled with CFR 0.152.
 */
package net.minecraft.server.debug;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import net.minecraft.block.entity.BlockEntity;
import net.minecraft.entity.Entity;
import net.minecraft.network.listener.ClientPlayPacketListener;
import net.minecraft.network.packet.Packet;
import net.minecraft.network.packet.s2c.play.BlockValueDebugS2CPacket;
import net.minecraft.network.packet.s2c.play.EntityValueDebugS2CPacket;
import net.minecraft.network.packet.s2c.play.EventDebugS2CPacket;
import net.minecraft.registry.Registries;
import net.minecraft.server.debug.SubscriberTracker;
import net.minecraft.server.debug.TrackedSubscription;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.server.world.ServerChunkLoadingManager;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.ChunkPos;
import net.minecraft.world.chunk.WorldChunk;
import net.minecraft.world.debug.DebugSubscriptionType;
import net.minecraft.world.debug.DebugTrackable;
import net.minecraft.world.poi.PointOfInterest;

public class SubscriptionTracker {
    private final ServerWorld world;
    private final List<TrackedSubscription<?>> subscriptions = new ArrayList();
    private final Map<DebugSubscriptionType<?>, TrackedSubscription.UpdateTrackedSubscription<?>> subscriptionsByTypes = new HashMap();
    private final TrackedSubscription.TrackedPoi trackedPoi = new TrackedSubscription.TrackedPoi();
    private final TrackedSubscription.TrackedVillageSections trackedVillageSections = new TrackedSubscription.TrackedVillageSections();
    private boolean stopped = true;
    private Set<DebugSubscriptionType<?>> subscribedTypes = Set.of();

    public SubscriptionTracker(ServerWorld world) {
        this.world = world;
        for (DebugSubscriptionType debugSubscriptionType : Registries.DEBUG_SUBSCRIPTION) {
            if (debugSubscriptionType.getPacketCodec() == null) continue;
            this.subscriptionsByTypes.put(debugSubscriptionType, new TrackedSubscription.UpdateTrackedSubscription(debugSubscriptionType));
        }
        this.subscriptions.addAll(this.subscriptionsByTypes.values());
        this.subscriptions.add(this.trackedPoi);
        this.subscriptions.add(this.trackedVillageSections);
    }

    public void tick(SubscriberTracker subscriberTracker) {
        this.subscribedTypes = subscriberTracker.getSubscribedTypes();
        boolean bl = this.subscribedTypes.isEmpty();
        if (this.stopped != bl) {
            this.stopped = bl;
            if (bl) {
                for (TrackedSubscription<?> trackedSubscription : this.subscriptions) {
                    trackedSubscription.clear();
                }
            } else {
                this.startTracking();
            }
        }
        if (!this.stopped) {
            for (TrackedSubscription<?> trackedSubscription : this.subscriptions) {
                trackedSubscription.refreshTracking(this.world);
            }
        }
    }

    private void startTracking() {
        ServerChunkLoadingManager serverChunkLoadingManager = this.world.getChunkManager().chunkLoadingManager;
        serverChunkLoadingManager.forEachChunk(this::trackChunk);
        for (Entity entity : this.world.iterateEntities()) {
            if (!serverChunkLoadingManager.hasTrackingPlayer(entity)) continue;
            this.trackEntity(entity);
        }
    }

    <T> TrackedSubscription.UpdateTrackedSubscription<T> get(DebugSubscriptionType<T> type) {
        return this.subscriptionsByTypes.get(type);
    }

    public void trackChunk(final WorldChunk chunk) {
        if (this.stopped) {
            return;
        }
        chunk.registerTracking(this.world, new DebugTrackable.Tracker(){

            @Override
            public <T> void track(DebugSubscriptionType<T> type, DebugTrackable.DebugDataSupplier<T> dataSupplier) {
                SubscriptionTracker.this.get(type).trackChunk(chunk.getPos(), dataSupplier);
            }
        });
        chunk.getBlockEntities().values().forEach(this::trackBlockEntity);
    }

    public void untrackChunk(ChunkPos chunkPos) {
        if (this.stopped) {
            return;
        }
        for (TrackedSubscription.UpdateTrackedSubscription<?> updateTrackedSubscription : this.subscriptionsByTypes.values()) {
            updateTrackedSubscription.untrackChunk(chunkPos);
        }
    }

    public void trackBlockEntity(final BlockEntity blockEntity) {
        if (this.stopped) {
            return;
        }
        blockEntity.registerTracking(this.world, new DebugTrackable.Tracker(){

            @Override
            public <T> void track(DebugSubscriptionType<T> type, DebugTrackable.DebugDataSupplier<T> dataSupplier) {
                SubscriptionTracker.this.get(type).trackBlockEntity(blockEntity.getPos(), dataSupplier);
            }
        });
    }

    public void untrackBlockEntity(BlockPos pos) {
        if (this.stopped) {
            return;
        }
        for (TrackedSubscription.UpdateTrackedSubscription<?> updateTrackedSubscription : this.subscriptionsByTypes.values()) {
            updateTrackedSubscription.untrackBlockEntity(this.world, pos);
        }
    }

    public void trackEntity(final Entity entity) {
        if (this.stopped) {
            return;
        }
        entity.registerTracking(this.world, new DebugTrackable.Tracker(){

            @Override
            public <T> void track(DebugSubscriptionType<T> type, DebugTrackable.DebugDataSupplier<T> dataSupplier) {
                SubscriptionTracker.this.get(type).trackEntity(entity.getUuid(), dataSupplier);
            }
        });
    }

    public void untrackEntity(Entity entity) {
        if (this.stopped) {
            return;
        }
        for (TrackedSubscription.UpdateTrackedSubscription<?> updateTrackedSubscription : this.subscriptionsByTypes.values()) {
            updateTrackedSubscription.untrackEntity(entity);
        }
    }

    public void sendInitialIfSubscribed(ServerPlayerEntity player, ChunkPos chunkPos) {
        if (this.stopped) {
            return;
        }
        for (TrackedSubscription<?> trackedSubscription : this.subscriptions) {
            trackedSubscription.sendInitialIfSubscribed(player, chunkPos);
        }
    }

    public void sendInitialIfSubscribed(ServerPlayerEntity player, Entity entity) {
        if (this.stopped) {
            return;
        }
        for (TrackedSubscription<?> trackedSubscription : this.subscriptions) {
            trackedSubscription.sendInitialIfSubscribed(player, entity);
        }
    }

    public void onPoiAdded(PointOfInterest poi) {
        if (this.stopped) {
            return;
        }
        this.trackedPoi.onPoiAdded(this.world, poi);
        this.trackedVillageSections.onPoiAdded(this.world, poi);
    }

    public void onPoiUpdated(BlockPos pos) {
        if (this.stopped) {
            return;
        }
        this.trackedPoi.onPoiUpdated(this.world, pos);
    }

    public void onPoiRemoved(BlockPos pos) {
        if (this.stopped) {
            return;
        }
        this.trackedPoi.onPoiRemoved(this.world, pos);
        this.trackedVillageSections.onPoiRemoved(this.world, pos);
    }

    public boolean isSubscribed(DebugSubscriptionType<?> type) {
        return this.subscribedTypes.contains(type);
    }

    public <T> void sendBlockDebugData(BlockPos pos, DebugSubscriptionType<T> type, T value) {
        if (this.isSubscribed(type)) {
            this.sendToTrackingPlayers(new ChunkPos(pos), type, (Packet<? super ClientPlayPacketListener>)new BlockValueDebugS2CPacket(pos, type.optionalValueFor(value)));
        }
    }

    public <T> void removeBlockDebugData(BlockPos pos, DebugSubscriptionType<T> type) {
        if (this.isSubscribed(type)) {
            this.sendToTrackingPlayers(new ChunkPos(pos), type, (Packet<? super ClientPlayPacketListener>)new BlockValueDebugS2CPacket(pos, type.optionalValueFor()));
        }
    }

    public <T> void sendEntityDebugData(Entity entity, DebugSubscriptionType<T> type, T value) {
        if (this.isSubscribed(type)) {
            this.sendToTrackingPlayers(entity, type, (Packet<? super ClientPlayPacketListener>)new EntityValueDebugS2CPacket(entity.getId(), type.optionalValueFor(value)));
        }
    }

    public <T> void removeEntityDebugData(Entity entity, DebugSubscriptionType<T> type) {
        if (this.isSubscribed(type)) {
            this.sendToTrackingPlayers(entity, type, (Packet<? super ClientPlayPacketListener>)new EntityValueDebugS2CPacket(entity.getId(), type.optionalValueFor()));
        }
    }

    public <T> void sendEventDebugData(BlockPos pos, DebugSubscriptionType<T> type, T value) {
        if (this.isSubscribed(type)) {
            this.sendToTrackingPlayers(new ChunkPos(pos), type, (Packet<? super ClientPlayPacketListener>)new EventDebugS2CPacket(type.valueFor(value)));
        }
    }

    private void sendToTrackingPlayers(ChunkPos chunkPos, DebugSubscriptionType<?> type, Packet<? super ClientPlayPacketListener> packet) {
        ServerChunkLoadingManager serverChunkLoadingManager = this.world.getChunkManager().chunkLoadingManager;
        for (ServerPlayerEntity serverPlayerEntity : serverChunkLoadingManager.getPlayersWatchingChunk(chunkPos, false)) {
            if (!serverPlayerEntity.getSubscribedTypes().contains(type)) continue;
            serverPlayerEntity.networkHandler.sendPacket(packet);
        }
    }

    private void sendToTrackingPlayers(Entity entity, DebugSubscriptionType<?> type, Packet<? super ClientPlayPacketListener> packet) {
        ServerChunkLoadingManager serverChunkLoadingManager = this.world.getChunkManager().chunkLoadingManager;
        serverChunkLoadingManager.sendToOtherNearbyPlayersIf(entity, packet, player -> player.getSubscribedTypes().contains(type));
    }
}


/*
 * Decompiled with CFR 0.152.
 */
package net.minecraft.server.debug;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import net.minecraft.SharedConstants;
import net.minecraft.network.packet.Packet;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.PlayerConfigEntry;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.world.debug.DebugSubscriptionType;

public class SubscriberTracker {
    private final MinecraftServer server;
    private final Map<DebugSubscriptionType<?>, List<ServerPlayerEntity>> subscribers = new HashMap();

    public SubscriberTracker(MinecraftServer server) {
        this.server = server;
    }

    private List<ServerPlayerEntity> getSubscribers(DebugSubscriptionType<?> type) {
        return this.subscribers.getOrDefault(type, List.of());
    }

    public void tick() {
        this.subscribers.values().forEach(List::clear);
        for (ServerPlayerEntity serverPlayerEntity : this.server.getPlayerManager().getPlayerList()) {
            for (DebugSubscriptionType<?> debugSubscriptionType : serverPlayerEntity.getSubscribedTypes()) {
                this.subscribers.computeIfAbsent(debugSubscriptionType, type -> new ArrayList()).add(serverPlayerEntity);
            }
        }
        this.subscribers.values().removeIf(List::isEmpty);
    }

    public void send(DebugSubscriptionType<?> type, Packet<?> packet) {
        for (ServerPlayerEntity serverPlayerEntity : this.getSubscribers(type)) {
            serverPlayerEntity.networkHandler.sendPacket(packet);
        }
    }

    public Set<DebugSubscriptionType<?>> getSubscribedTypes() {
        return Set.copyOf(this.subscribers.keySet());
    }

    public boolean hasSubscriber(DebugSubscriptionType<?> type) {
        return !this.getSubscribers(type).isEmpty();
    }

    public boolean canSubscribe(ServerPlayerEntity player) {
        PlayerConfigEntry playerConfigEntry = player.getPlayerConfigEntry();
        if (SharedConstants.isDevelopment && this.server.isHost(playerConfigEntry)) {
            return true;
        }
        return this.server.getPlayerManager().isOperator(playerConfigEntry);
    }
}


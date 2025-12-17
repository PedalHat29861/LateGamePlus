/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  org.jspecify.annotations.Nullable
 */
package net.minecraft.server.dedicated.management.handler;

import java.util.List;
import java.util.Optional;
import java.util.UUID;
import net.minecraft.server.PlayerConfigEntry;
import net.minecraft.server.dedicated.MinecraftDedicatedServer;
import net.minecraft.server.dedicated.management.ManagementLogger;
import net.minecraft.server.dedicated.management.handler.PlayerListManagementHandler;
import net.minecraft.server.dedicated.management.network.ManagementConnectionId;
import net.minecraft.server.network.ServerPlayerEntity;
import org.jspecify.annotations.Nullable;

public class PlayerListManagementHandlerImpl
implements PlayerListManagementHandler {
    private final ManagementLogger logger;
    private final MinecraftDedicatedServer server;

    public PlayerListManagementHandlerImpl(MinecraftDedicatedServer server, ManagementLogger logger) {
        this.logger = logger;
        this.server = server;
    }

    @Override
    public List<ServerPlayerEntity> getPlayerList() {
        return this.server.getPlayerManager().getPlayerList();
    }

    @Override
    public @Nullable ServerPlayerEntity getPlayer(UUID uuid) {
        return this.server.getPlayerManager().getPlayer(uuid);
    }

    @Override
    public Optional<PlayerConfigEntry> findByName(String name) {
        return this.server.getApiServices().nameToIdCache().findByName(name);
    }

    @Override
    public Optional<PlayerConfigEntry> fetchPlayer(UUID uuid) {
        return Optional.ofNullable(this.server.getApiServices().sessionService().fetchProfile(uuid, true)).map(result -> new PlayerConfigEntry(result.profile()));
    }

    @Override
    public Optional<PlayerConfigEntry> getByUuid(UUID uuid) {
        return this.server.getApiServices().nameToIdCache().getByUuid(uuid);
    }

    @Override
    public Optional<ServerPlayerEntity> getPlayer(Optional<UUID> uuid, Optional<String> name) {
        if (uuid.isPresent()) {
            return Optional.ofNullable(this.server.getPlayerManager().getPlayer(uuid.get()));
        }
        if (name.isPresent()) {
            return Optional.ofNullable(this.server.getPlayerManager().getPlayer(name.get()));
        }
        return Optional.empty();
    }

    @Override
    public List<ServerPlayerEntity> getPlayersByIpAddress(String ipAddress) {
        return this.server.getPlayerManager().getPlayersByIp(ipAddress);
    }

    @Override
    public void removePlayer(ServerPlayerEntity player, ManagementConnectionId remote) {
        this.server.getPlayerManager().remove(player);
        this.logger.logAction(remote, "Remove player '{}'", player.getStringifiedName());
    }

    @Override
    public @Nullable ServerPlayerEntity getPlayer(String name) {
        return this.server.getPlayerManager().getPlayer(name);
    }
}


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
import java.util.concurrent.CompletableFuture;
import net.minecraft.server.PlayerConfigEntry;
import net.minecraft.server.dedicated.management.network.ManagementConnectionId;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.util.Util;
import org.jspecify.annotations.Nullable;

public interface PlayerListManagementHandler {
    public List<ServerPlayerEntity> getPlayerList();

    public @Nullable ServerPlayerEntity getPlayer(UUID var1);

    default public CompletableFuture<Optional<PlayerConfigEntry>> getPlayerAsync(Optional<UUID> uuid, Optional<String> name) {
        if (uuid.isPresent()) {
            Optional<PlayerConfigEntry> optional = this.getByUuid(uuid.get());
            if (optional.isPresent()) {
                return CompletableFuture.completedFuture(optional);
            }
            return CompletableFuture.supplyAsync(() -> this.fetchPlayer((UUID)uuid.get()), Util.getDownloadWorkerExecutor());
        }
        if (name.isPresent()) {
            return CompletableFuture.supplyAsync(() -> this.findByName((String)name.get()), Util.getDownloadWorkerExecutor());
        }
        return CompletableFuture.completedFuture(Optional.empty());
    }

    public Optional<PlayerConfigEntry> findByName(String var1);

    public Optional<PlayerConfigEntry> fetchPlayer(UUID var1);

    public Optional<PlayerConfigEntry> getByUuid(UUID var1);

    public Optional<ServerPlayerEntity> getPlayer(Optional<UUID> var1, Optional<String> var2);

    public List<ServerPlayerEntity> getPlayersByIpAddress(String var1);

    public @Nullable ServerPlayerEntity getPlayer(String var1);

    public void removePlayer(ServerPlayerEntity var1, ManagementConnectionId var2);
}


/*
 * Decompiled with CFR 0.152.
 */
package net.minecraft.server.dedicated.management.handler;

import java.util.Collection;
import java.util.List;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.server.dedicated.MinecraftDedicatedServer;
import net.minecraft.server.dedicated.management.ManagementLogger;
import net.minecraft.server.dedicated.management.handler.ServerManagementHandler;
import net.minecraft.server.dedicated.management.network.ManagementConnectionId;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.text.Text;

public class ServerManagementHandlerImpl
implements ServerManagementHandler {
    private final MinecraftDedicatedServer server;
    private final ManagementLogger logger;

    public ServerManagementHandlerImpl(MinecraftDedicatedServer server, ManagementLogger logger) {
        this.server = server;
        this.logger = logger;
    }

    @Override
    public boolean isLoading() {
        return this.server.isLoading();
    }

    @Override
    public boolean save(boolean suppressLogs, boolean flush, boolean force, ManagementConnectionId remote) {
        this.logger.logAction(remote, "Save everything. SuppressLogs: {}, flush: {}, force: {}", suppressLogs, flush, force);
        return this.server.saveAll(suppressLogs, flush, force);
    }

    @Override
    public void stop(boolean waitForShutdown, ManagementConnectionId remote) {
        this.logger.logAction(remote, "Halt server. WaitForShutdown: {}", waitForShutdown);
        this.server.stop(waitForShutdown);
    }

    @Override
    public void broadcastMessage(Text message, ManagementConnectionId remote) {
        this.logger.logAction(remote, "Send system message: '{}'", message.getString());
        this.server.sendMessage(message);
    }

    @Override
    public void sendMessageTo(Text message, boolean overlay, Collection<ServerPlayerEntity> players, ManagementConnectionId remote) {
        List<String> list = players.stream().map(PlayerEntity::getStringifiedName).toList();
        this.logger.logAction(remote, "Send system message to '{}' players (overlay: {}): '{}'", list.size(), overlay, message.getString());
        for (ServerPlayerEntity serverPlayerEntity : players) {
            if (overlay) {
                serverPlayerEntity.sendMessageToClient(message, true);
                continue;
            }
            serverPlayerEntity.sendMessage(message);
        }
    }

    @Override
    public void broadcastMessage(Text message, boolean overlay, ManagementConnectionId remote) {
        this.logger.logAction(remote, "Broadcast system message (overlay: {}): '{}'", overlay, message.getString());
        for (ServerPlayerEntity serverPlayerEntity : this.server.getPlayerManager().getPlayerList()) {
            if (overlay) {
                serverPlayerEntity.sendMessageToClient(message, true);
                continue;
            }
            serverPlayerEntity.sendMessage(message);
        }
    }
}


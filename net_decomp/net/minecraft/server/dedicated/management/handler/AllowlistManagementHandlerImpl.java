/*
 * Decompiled with CFR 0.152.
 */
package net.minecraft.server.dedicated.management.handler;

import java.util.Collection;
import net.minecraft.server.PlayerConfigEntry;
import net.minecraft.server.WhitelistEntry;
import net.minecraft.server.dedicated.MinecraftDedicatedServer;
import net.minecraft.server.dedicated.management.ManagementLogger;
import net.minecraft.server.dedicated.management.handler.AllowlistManagementHandler;
import net.minecraft.server.dedicated.management.network.ManagementConnectionId;

public class AllowlistManagementHandlerImpl
implements AllowlistManagementHandler {
    private final MinecraftDedicatedServer server;
    private final ManagementLogger logger;

    public AllowlistManagementHandlerImpl(MinecraftDedicatedServer server, ManagementLogger logger) {
        this.server = server;
        this.logger = logger;
    }

    @Override
    public Collection<WhitelistEntry> getAllowlist() {
        return this.server.getPlayerManager().getWhitelist().values();
    }

    @Override
    public boolean add(WhitelistEntry player, ManagementConnectionId remote) {
        this.logger.logAction(remote, "Add player '{}' to allowlist", player.getKey());
        return this.server.getPlayerManager().getWhitelist().add(player);
    }

    @Override
    public void clear(ManagementConnectionId remote) {
        this.logger.logAction(remote, "Clear allowlist", new Object[0]);
        this.server.getPlayerManager().getWhitelist().clear();
    }

    @Override
    public void remove(PlayerConfigEntry player, ManagementConnectionId remote) {
        this.logger.logAction(remote, "Remove player '{}' from allowlist", player);
        this.server.getPlayerManager().getWhitelist().remove(player);
    }

    @Override
    public void kickUnlisted(ManagementConnectionId remote) {
        this.logger.logAction(remote, "Kick unlisted players", new Object[0]);
        this.server.kickNonWhitelistedPlayers();
    }
}


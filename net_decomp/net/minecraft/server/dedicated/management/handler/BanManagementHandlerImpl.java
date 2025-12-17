/*
 * Decompiled with CFR 0.152.
 */
package net.minecraft.server.dedicated.management.handler;

import java.util.Collection;
import net.minecraft.server.BannedIpEntry;
import net.minecraft.server.BannedPlayerEntry;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.PlayerConfigEntry;
import net.minecraft.server.dedicated.management.ManagementLogger;
import net.minecraft.server.dedicated.management.handler.BanManagementHandler;
import net.minecraft.server.dedicated.management.network.ManagementConnectionId;

public class BanManagementHandlerImpl
implements BanManagementHandler {
    private final MinecraftServer server;
    private final ManagementLogger logger;

    public BanManagementHandlerImpl(MinecraftServer server, ManagementLogger logger) {
        this.server = server;
        this.logger = logger;
    }

    @Override
    public void addPlayer(BannedPlayerEntry entry, ManagementConnectionId remote) {
        this.logger.logAction(remote, "Add player '{}' to banlist. Reason: '{}'", entry.toText(), entry.getReasonText().getString());
        this.server.getPlayerManager().getUserBanList().add(entry);
    }

    @Override
    public void removePlayer(PlayerConfigEntry entry, ManagementConnectionId remote) {
        this.logger.logAction(remote, "Remove player '{}' from banlist", entry);
        this.server.getPlayerManager().getUserBanList().remove(entry);
    }

    @Override
    public void clearBanList(ManagementConnectionId remote) {
        this.server.getPlayerManager().getUserBanList().clear();
    }

    @Override
    public Collection<BannedPlayerEntry> getUserBanList() {
        return this.server.getPlayerManager().getUserBanList().values();
    }

    @Override
    public Collection<BannedIpEntry> getIpBanList() {
        return this.server.getPlayerManager().getIpBanList().values();
    }

    @Override
    public void addIpAddress(BannedIpEntry entry, ManagementConnectionId remote) {
        this.logger.logAction(remote, "Add ip '{}' to ban list", entry.getKey());
        this.server.getPlayerManager().getIpBanList().add(entry);
    }

    @Override
    public void clearIpBanList(ManagementConnectionId remote) {
        this.logger.logAction(remote, "Clear ip ban list", new Object[0]);
        this.server.getPlayerManager().getIpBanList().clear();
    }

    @Override
    public void removeIpAddress(String ipAddress, ManagementConnectionId remote) {
        this.logger.logAction(remote, "Remove ip '{}' from ban list", ipAddress);
        this.server.getPlayerManager().getIpBanList().remove(ipAddress);
    }
}


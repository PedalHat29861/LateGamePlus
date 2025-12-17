/*
 * Decompiled with CFR 0.152.
 */
package net.minecraft.server.dedicated.management.handler;

import net.minecraft.command.permission.LeveledPermissionPredicate;
import net.minecraft.server.dedicated.MinecraftDedicatedServer;
import net.minecraft.server.dedicated.management.ManagementLogger;
import net.minecraft.server.dedicated.management.handler.PropertiesManagementHandler;
import net.minecraft.server.dedicated.management.network.ManagementConnectionId;
import net.minecraft.world.Difficulty;
import net.minecraft.world.GameMode;

public class PropertiesManagementHandlerImpl
implements PropertiesManagementHandler {
    private final MinecraftDedicatedServer server;
    private final ManagementLogger logger;

    public PropertiesManagementHandlerImpl(MinecraftDedicatedServer server, ManagementLogger logger) {
        this.server = server;
        this.logger = logger;
    }

    @Override
    public boolean getAutosave() {
        return this.server.getAutosave();
    }

    @Override
    public boolean setAutosave(boolean autosaveEnabled, ManagementConnectionId remote) {
        this.logger.logAction(remote, "Update autosave from {} to {}", this.getAutosave(), autosaveEnabled);
        this.server.setAutosave(autosaveEnabled);
        return this.getAutosave();
    }

    @Override
    public Difficulty getDifficulty() {
        return this.server.getSaveProperties().getDifficulty();
    }

    @Override
    public Difficulty setDifficulty(Difficulty difficulty, ManagementConnectionId remote) {
        this.logger.logAction(remote, "Update difficulty from '{}' to '{}'", this.getDifficulty(), difficulty);
        this.server.setDifficulty(difficulty);
        return this.getDifficulty();
    }

    @Override
    public boolean getEnforceAllowlist() {
        return this.server.isEnforceWhitelist();
    }

    @Override
    public boolean setEnforceAllowlist(boolean enforceAllowlist, ManagementConnectionId remote) {
        this.logger.logAction(remote, "Update enforce allowlist from {} to {}", this.getEnforceAllowlist(), enforceAllowlist);
        this.server.setEnforceWhitelist(enforceAllowlist);
        this.server.kickNonWhitelistedPlayers();
        return this.getEnforceAllowlist();
    }

    @Override
    public boolean getUseAllowlist() {
        return this.server.getUseAllowlist();
    }

    @Override
    public boolean setUseAllowlist(boolean useAllowlist, ManagementConnectionId remote) {
        this.logger.logAction(remote, "Update using allowlist from {} to {}", this.getUseAllowlist(), useAllowlist);
        this.server.setUseAllowlist(useAllowlist);
        this.server.kickNonWhitelistedPlayers();
        return this.getUseAllowlist();
    }

    @Override
    public int getMaxPlayers() {
        return this.server.getMaxPlayerCount();
    }

    @Override
    public int setMaxPlayers(int maxPlayers, ManagementConnectionId remote) {
        this.logger.logAction(remote, "Update max players from {} to {}", this.getMaxPlayers(), maxPlayers);
        this.server.setMaxPlayers(maxPlayers);
        return this.getMaxPlayers();
    }

    @Override
    public int getPauseWhenEmptySeconds() {
        return this.server.getPauseWhenEmptySeconds();
    }

    @Override
    public int setPauseWhenEmptySeconds(int pauseWhenEmptySeconds, ManagementConnectionId remote) {
        this.logger.logAction(remote, "Update pause when empty from {} seconds to {} seconds", this.getPauseWhenEmptySeconds(), pauseWhenEmptySeconds);
        this.server.setPauseWhenEmptySeconds(pauseWhenEmptySeconds);
        return this.getPauseWhenEmptySeconds();
    }

    @Override
    public int getPlayerIdleTimeout() {
        return this.server.getPlayerIdleTimeout();
    }

    @Override
    public int setPlayerIdleTimeout(int playerIdleTimeout, ManagementConnectionId remote) {
        this.logger.logAction(remote, "Update player idle timeout from {} minutes to {} minutes", this.getPlayerIdleTimeout(), playerIdleTimeout);
        this.server.setPlayerIdleTimeout(playerIdleTimeout);
        return this.getPlayerIdleTimeout();
    }

    @Override
    public boolean getAllowFlight() {
        return this.server.isFlightEnabled();
    }

    @Override
    public boolean setAllowFlight(boolean allowFlight, ManagementConnectionId remote) {
        this.logger.logAction(remote, "Update allow flight from {} to {}", this.getAllowFlight(), allowFlight);
        this.server.setAllowFlight(allowFlight);
        return this.getAllowFlight();
    }

    @Override
    public int getSpawnProtectionRadius() {
        return this.server.getSpawnProtectionRadius();
    }

    @Override
    public int setSpawnProtectionRadius(int spawnProtectionRadius, ManagementConnectionId remote) {
        this.logger.logAction(remote, "Update spawn protection radius from {} to {}", this.getSpawnProtectionRadius(), spawnProtectionRadius);
        this.server.setSpawnProtectionRadius(spawnProtectionRadius);
        return this.getSpawnProtectionRadius();
    }

    @Override
    public String getMotd() {
        return this.server.getServerMotd();
    }

    @Override
    public String setMotd(String motd, ManagementConnectionId remote) {
        this.logger.logAction(remote, "Update MOTD from '{}' to '{}'", this.getMotd(), motd);
        this.server.setMotd(motd);
        return this.getMotd();
    }

    @Override
    public boolean getForceGameMode() {
        return this.server.getForceGameMode();
    }

    @Override
    public boolean setForceGameMode(boolean forceGameMode, ManagementConnectionId remote) {
        this.logger.logAction(remote, "Update force game mode from {} to {}", this.getForceGameMode(), forceGameMode);
        this.server.setForceGameMode(forceGameMode);
        return this.getForceGameMode();
    }

    @Override
    public GameMode getGameMode() {
        return this.server.getGameMode();
    }

    @Override
    public GameMode setGameMode(GameMode gameMode, ManagementConnectionId remote) {
        this.logger.logAction(remote, "Update game mode from '{}' to '{}'", this.getGameMode(), gameMode);
        this.server.setGameMode(gameMode);
        return this.getGameMode();
    }

    @Override
    public int getViewDistance() {
        return this.server.getViewDistance();
    }

    @Override
    public int setViewDistance(int viewDistance, ManagementConnectionId remote) {
        this.logger.logAction(remote, "Update view distance from {} to {}", this.getViewDistance(), viewDistance);
        this.server.setViewDistance(viewDistance);
        return this.getViewDistance();
    }

    @Override
    public int getSimulationDistance() {
        return this.server.getSimulationDistance();
    }

    @Override
    public int setSimulationDistance(int simulationDistance, ManagementConnectionId remote) {
        this.logger.logAction(remote, "Update simulation distance from {} to {}", this.getSimulationDistance(), simulationDistance);
        this.server.setSimulationDistance(simulationDistance);
        return this.getSimulationDistance();
    }

    @Override
    public boolean getAcceptTransfers() {
        return this.server.acceptsTransfers();
    }

    @Override
    public boolean setAcceptTransfers(boolean acceptTransfers, ManagementConnectionId remote) {
        this.logger.logAction(remote, "Update accepts transfers from {} to {}", this.getAcceptTransfers(), acceptTransfers);
        this.server.setAcceptTransfers(acceptTransfers);
        return this.getAcceptTransfers();
    }

    @Override
    public int getStatusHeartbeatInterval() {
        return this.server.getStatusHeartbeatInterval();
    }

    @Override
    public int setStatusHeartbeatInterval(int statusHeartbeatInterval, ManagementConnectionId remote) {
        this.logger.logAction(remote, "Update status heartbeat interval from {} to {}", this.getStatusHeartbeatInterval(), statusHeartbeatInterval);
        this.server.setStatusHeartbeatInterval(statusHeartbeatInterval);
        return this.getStatusHeartbeatInterval();
    }

    @Override
    public LeveledPermissionPredicate getOperatorUserPermissionLevel() {
        return this.server.getOpPermissionLevel();
    }

    @Override
    public LeveledPermissionPredicate setOperatorUserPermissionLevel(LeveledPermissionPredicate operatorUserPermissionLevel, ManagementConnectionId remote) {
        this.logger.logAction(remote, "Update operator user permission level from {} to {}", this.getOperatorUserPermissionLevel(), operatorUserPermissionLevel.getLevel());
        this.server.setOperatorUserPermissionLevel(operatorUserPermissionLevel);
        return this.getOperatorUserPermissionLevel();
    }

    @Override
    public boolean getHideOnlinePlayers() {
        return this.server.hideOnlinePlayers();
    }

    @Override
    public boolean setHideOnlinePlayers(boolean hideOnlinePlayers, ManagementConnectionId remote) {
        this.logger.logAction(remote, "Update hides online players from {} to {}", this.getHideOnlinePlayers(), hideOnlinePlayers);
        this.server.setHideOnlinePlayers(hideOnlinePlayers);
        return this.getHideOnlinePlayers();
    }

    @Override
    public boolean getStatusReplies() {
        return this.server.acceptsStatusQuery();
    }

    @Override
    public boolean setStatusReplies(boolean repliesToStatus, ManagementConnectionId remote) {
        this.logger.logAction(remote, "Update replies to status from {} to {}", this.getStatusReplies(), repliesToStatus);
        this.server.setStatusReplies(repliesToStatus);
        return this.getStatusReplies();
    }

    @Override
    public int getEntityBroadcastRange() {
        return this.server.getEntityBroadcastRange();
    }

    @Override
    public int setEntityBroadcastRange(int entityBroadcastRange, ManagementConnectionId remote) {
        this.logger.logAction(remote, "Update entity broadcast range percentage from {}% to {}%", this.getEntityBroadcastRange(), entityBroadcastRange);
        this.server.setEntityBroadcastRange(entityBroadcastRange);
        return this.getEntityBroadcastRange();
    }
}


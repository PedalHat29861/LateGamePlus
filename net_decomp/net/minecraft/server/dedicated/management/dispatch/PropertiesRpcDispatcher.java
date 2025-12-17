/*
 * Decompiled with CFR 0.152.
 */
package net.minecraft.server.dedicated.management.dispatch;

import net.minecraft.command.permission.LeveledPermissionPredicate;
import net.minecraft.command.permission.PermissionLevel;
import net.minecraft.server.dedicated.management.dispatch.ManagementHandlerDispatcher;
import net.minecraft.server.dedicated.management.network.ManagementConnectionId;
import net.minecraft.world.Difficulty;
import net.minecraft.world.GameMode;

public class PropertiesRpcDispatcher {
    public static boolean getAutosave(ManagementHandlerDispatcher dispatcher) {
        return dispatcher.getPropertiesHandler().getAutosave();
    }

    public static boolean setAutosave(ManagementHandlerDispatcher dispatcher, boolean autosave, ManagementConnectionId remote) {
        return dispatcher.getPropertiesHandler().setAutosave(autosave, remote);
    }

    public static Difficulty getDifficulty(ManagementHandlerDispatcher dispatcher) {
        return dispatcher.getPropertiesHandler().getDifficulty();
    }

    public static Difficulty setDifficulty(ManagementHandlerDispatcher dispatcher, Difficulty difficulty, ManagementConnectionId remote) {
        return dispatcher.getPropertiesHandler().setDifficulty(difficulty, remote);
    }

    public static boolean getEnforceAllowlist(ManagementHandlerDispatcher dispatcher) {
        return dispatcher.getPropertiesHandler().getEnforceAllowlist();
    }

    public static boolean setEnforceAllowlist(ManagementHandlerDispatcher dispatcher, boolean enforceAllowlist, ManagementConnectionId remote) {
        return dispatcher.getPropertiesHandler().setEnforceAllowlist(enforceAllowlist, remote);
    }

    public static boolean getUseAllowlist(ManagementHandlerDispatcher dispatcher) {
        return dispatcher.getPropertiesHandler().getUseAllowlist();
    }

    public static boolean setUseAllowlist(ManagementHandlerDispatcher dispatcher, boolean useAllowlist, ManagementConnectionId remote) {
        return dispatcher.getPropertiesHandler().setUseAllowlist(useAllowlist, remote);
    }

    public static int getMaxPlayers(ManagementHandlerDispatcher dispatcher) {
        return dispatcher.getPropertiesHandler().getMaxPlayers();
    }

    public static int setMaxPlayers(ManagementHandlerDispatcher dispatcher, int maxPlayers, ManagementConnectionId remote) {
        return dispatcher.getPropertiesHandler().setMaxPlayers(maxPlayers, remote);
    }

    public static int getPauseWhenEmptySeconds(ManagementHandlerDispatcher dispatcher) {
        return dispatcher.getPropertiesHandler().getPauseWhenEmptySeconds();
    }

    public static int setPauseWhenEmptySeconds(ManagementHandlerDispatcher dispatcher, int pauseWhenEmptySeconds, ManagementConnectionId remote) {
        return dispatcher.getPropertiesHandler().setPauseWhenEmptySeconds(pauseWhenEmptySeconds, remote);
    }

    public static int getPlayerIdleTimeout(ManagementHandlerDispatcher dispatcher) {
        return dispatcher.getPropertiesHandler().getPlayerIdleTimeout();
    }

    public static int setPlayerIdleTimeout(ManagementHandlerDispatcher dispatcher, int playerIdleTimeout, ManagementConnectionId remote) {
        return dispatcher.getPropertiesHandler().setPlayerIdleTimeout(playerIdleTimeout, remote);
    }

    public static boolean getAllowFlight(ManagementHandlerDispatcher dispatcher) {
        return dispatcher.getPropertiesHandler().getAllowFlight();
    }

    public static boolean setAllowFlight(ManagementHandlerDispatcher dispatcher, boolean allowFlight, ManagementConnectionId remote) {
        return dispatcher.getPropertiesHandler().setAllowFlight(allowFlight, remote);
    }

    public static int getSpawnProtectionRadius(ManagementHandlerDispatcher dispatcher) {
        return dispatcher.getPropertiesHandler().getSpawnProtectionRadius();
    }

    public static int setSpawnProtectionRadius(ManagementHandlerDispatcher dispatcher, int spawnProtectionRadius, ManagementConnectionId remote) {
        return dispatcher.getPropertiesHandler().setSpawnProtectionRadius(spawnProtectionRadius, remote);
    }

    public static String getMotd(ManagementHandlerDispatcher dispatcher) {
        return dispatcher.getPropertiesHandler().getMotd();
    }

    public static String setMotd(ManagementHandlerDispatcher dispatcher, String motd, ManagementConnectionId remote) {
        return dispatcher.getPropertiesHandler().setMotd(motd, remote);
    }

    public static boolean getForceGameMode(ManagementHandlerDispatcher dispatcher) {
        return dispatcher.getPropertiesHandler().getForceGameMode();
    }

    public static boolean setForceGameMode(ManagementHandlerDispatcher dispatcher, boolean forceGameMode, ManagementConnectionId remote) {
        return dispatcher.getPropertiesHandler().setForceGameMode(forceGameMode, remote);
    }

    public static GameMode getGameMode(ManagementHandlerDispatcher dispatcher) {
        return dispatcher.getPropertiesHandler().getGameMode();
    }

    public static GameMode setGameMode(ManagementHandlerDispatcher dispatcher, GameMode gameMode, ManagementConnectionId remote) {
        return dispatcher.getPropertiesHandler().setGameMode(gameMode, remote);
    }

    public static int getViewDistance(ManagementHandlerDispatcher dispatcher) {
        return dispatcher.getPropertiesHandler().getViewDistance();
    }

    public static int setViewDistance(ManagementHandlerDispatcher dispatcher, int viewDistance, ManagementConnectionId remote) {
        return dispatcher.getPropertiesHandler().setViewDistance(viewDistance, remote);
    }

    public static int getSimulationDistance(ManagementHandlerDispatcher dispatcher) {
        return dispatcher.getPropertiesHandler().getSimulationDistance();
    }

    public static int setSimulationDistance(ManagementHandlerDispatcher dispatcher, int simulationDistance, ManagementConnectionId remote) {
        return dispatcher.getPropertiesHandler().setSimulationDistance(simulationDistance, remote);
    }

    public static boolean getAcceptTransfers(ManagementHandlerDispatcher dispatcher) {
        return dispatcher.getPropertiesHandler().getAcceptTransfers();
    }

    public static boolean setAcceptTransfers(ManagementHandlerDispatcher dispatcher, boolean acceptTransfers, ManagementConnectionId remote) {
        return dispatcher.getPropertiesHandler().setAcceptTransfers(acceptTransfers, remote);
    }

    public static int getStatusHeartbeatInterval(ManagementHandlerDispatcher dispatcher) {
        return dispatcher.getPropertiesHandler().getStatusHeartbeatInterval();
    }

    public static int setStatusHeartbeatInterval(ManagementHandlerDispatcher dispatcher, int statusHeartbeatInterval, ManagementConnectionId remote) {
        return dispatcher.getPropertiesHandler().setStatusHeartbeatInterval(statusHeartbeatInterval, remote);
    }

    public static PermissionLevel getOperatorUserPermissionLevel(ManagementHandlerDispatcher dispatcher) {
        return dispatcher.getPropertiesHandler().getOperatorUserPermissionLevel().getLevel();
    }

    public static PermissionLevel setOperatorUserPermissionLevel(ManagementHandlerDispatcher dispatcher, PermissionLevel permissionLevel, ManagementConnectionId remote) {
        return dispatcher.getPropertiesHandler().setOperatorUserPermissionLevel(LeveledPermissionPredicate.fromLevel(permissionLevel), remote).getLevel();
    }

    public static boolean getHideOnlinePlayers(ManagementHandlerDispatcher dispatcher) {
        return dispatcher.getPropertiesHandler().getHideOnlinePlayers();
    }

    public static boolean setHideOnlinePlayers(ManagementHandlerDispatcher dispatcher, boolean hideOnlinePlayers, ManagementConnectionId remote) {
        return dispatcher.getPropertiesHandler().setHideOnlinePlayers(hideOnlinePlayers, remote);
    }

    public static boolean getStatusReplies(ManagementHandlerDispatcher dispatcher) {
        return dispatcher.getPropertiesHandler().getStatusReplies();
    }

    public static boolean setStatusReplies(ManagementHandlerDispatcher dispatcher, boolean statusReplies, ManagementConnectionId remote) {
        return dispatcher.getPropertiesHandler().setStatusReplies(statusReplies, remote);
    }

    public static int getEntityBroadcastRange(ManagementHandlerDispatcher dispatcher) {
        return dispatcher.getPropertiesHandler().getEntityBroadcastRange();
    }

    public static int setEntityBroadcastRange(ManagementHandlerDispatcher dispatcher, int entityBroadcastRange, ManagementConnectionId remote) {
        return dispatcher.getPropertiesHandler().setEntityBroadcastRange(entityBroadcastRange, remote);
    }
}


/*
 * Decompiled with CFR 0.152.
 */
package net.minecraft.server.dedicated.management.listener;

import net.minecraft.registry.entry.RegistryEntry;
import net.minecraft.server.BannedIpEntry;
import net.minecraft.server.BannedPlayerEntry;
import net.minecraft.server.OperatorEntry;
import net.minecraft.server.PlayerConfigEntry;
import net.minecraft.server.dedicated.management.ManagementServer;
import net.minecraft.server.dedicated.management.OutgoingRpcMethod;
import net.minecraft.server.dedicated.management.OutgoingRpcMethods;
import net.minecraft.server.dedicated.management.RpcPlayer;
import net.minecraft.server.dedicated.management.dispatch.GameRuleRpcDispatcher;
import net.minecraft.server.dedicated.management.dispatch.IpBansRpcDispatcher;
import net.minecraft.server.dedicated.management.dispatch.ManagementHandlerDispatcher;
import net.minecraft.server.dedicated.management.dispatch.OperatorsRpcDispatcher;
import net.minecraft.server.dedicated.management.dispatch.PlayerBansRpcDispatcher;
import net.minecraft.server.dedicated.management.dispatch.ServerRpcDispatcher;
import net.minecraft.server.dedicated.management.listener.ManagementListener;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.world.rule.GameRule;

public class NotificationManagementListener
implements ManagementListener {
    private final ManagementServer managementServer;
    private final ManagementHandlerDispatcher dispatcher;

    public NotificationManagementListener(ManagementHandlerDispatcher dispatcher, ManagementServer managementServer) {
        this.dispatcher = dispatcher;
        this.managementServer = managementServer;
    }

    @Override
    public void onPlayerJoined(ServerPlayerEntity player) {
        this.notifyAll(OutgoingRpcMethods.PLAYER_JOINED, RpcPlayer.of(player));
    }

    @Override
    public void onPlayerLeft(ServerPlayerEntity player) {
        this.notifyAll(OutgoingRpcMethods.PLAYER_LEFT, RpcPlayer.of(player));
    }

    @Override
    public void onServerStarted() {
        this.notifyAll(OutgoingRpcMethods.SERVER_STARTED);
    }

    @Override
    public void onServerStopping() {
        this.notifyAll(OutgoingRpcMethods.SERVER_STOPPING);
    }

    @Override
    public void onServerSaving() {
        this.notifyAll(OutgoingRpcMethods.SERVER_SAVING);
    }

    @Override
    public void onServerSaved() {
        this.notifyAll(OutgoingRpcMethods.SERVER_SAVED);
    }

    @Override
    public void onServerActivity() {
        this.notifyAll(OutgoingRpcMethods.SERVER_ACTIVITY);
    }

    @Override
    public void onOperatorAdded(OperatorEntry operator) {
        this.notifyAll(OutgoingRpcMethods.OPERATOR_ADDED, OperatorsRpcDispatcher.RpcEntry.of(operator));
    }

    @Override
    public void onOperatorRemoved(OperatorEntry operator) {
        this.notifyAll(OutgoingRpcMethods.OPERATOR_REMOVED, OperatorsRpcDispatcher.RpcEntry.of(operator));
    }

    @Override
    public void onAllowlistAdded(PlayerConfigEntry player) {
        this.notifyAll(OutgoingRpcMethods.ALLOWLIST_ADDED, RpcPlayer.of(player));
    }

    @Override
    public void onAllowlistRemoved(PlayerConfigEntry player) {
        this.notifyAll(OutgoingRpcMethods.ALLOWLIST_REMOVED, RpcPlayer.of(player));
    }

    @Override
    public void onIpBanAdded(BannedIpEntry entry) {
        this.notifyAll(OutgoingRpcMethods.IP_BAN_ADDED, IpBansRpcDispatcher.IpBanData.fromBannedIpEntry(entry));
    }

    @Override
    public void onIpBanRemoved(String string) {
        this.notifyAll(OutgoingRpcMethods.IP_BAN_REMOVED, string);
    }

    @Override
    public void onBanAdded(BannedPlayerEntry entry) {
        this.notifyAll(OutgoingRpcMethods.BAN_ADDED, PlayerBansRpcDispatcher.RpcEntry.of(entry));
    }

    @Override
    public void onBanRemoved(PlayerConfigEntry player) {
        this.notifyAll(OutgoingRpcMethods.BAN_REMOVED, RpcPlayer.of(player));
    }

    @Override
    public <T> void onGameRuleUpdated(GameRule<T> rule, T value) {
        this.notifyAll(OutgoingRpcMethods.GAMERULE_UPDATED, GameRuleRpcDispatcher.toEntry(this.dispatcher, rule, value));
    }

    @Override
    public void onServerStatusHeartbeat() {
        this.notifyAll(OutgoingRpcMethods.SERVER_STATUS_HEARTBEAT, ServerRpcDispatcher.status(this.dispatcher));
    }

    private void notifyAll(RegistryEntry.Reference<? extends OutgoingRpcMethod<Void, ?>> method) {
        this.managementServer.forEachConnection(connection -> connection.sendNotification(method));
    }

    private <Params> void notifyAll(RegistryEntry.Reference<? extends OutgoingRpcMethod<Params, ?>> method, Params params) {
        this.managementServer.forEachConnection(connection -> connection.sendNotification(method, params));
    }
}


/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  com.google.common.collect.Lists
 */
package net.minecraft.server.dedicated.management.listener;

import com.google.common.collect.Lists;
import java.util.List;
import net.minecraft.server.BannedIpEntry;
import net.minecraft.server.BannedPlayerEntry;
import net.minecraft.server.OperatorEntry;
import net.minecraft.server.PlayerConfigEntry;
import net.minecraft.server.dedicated.management.listener.ManagementListener;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.world.rule.GameRule;

public class CompositeManagementListener
implements ManagementListener {
    private final List<ManagementListener> listeners = Lists.newArrayList();

    public void addListener(ManagementListener listener) {
        this.listeners.add(listener);
    }

    @Override
    public void onPlayerJoined(ServerPlayerEntity player) {
        this.listeners.forEach(listener -> listener.onPlayerJoined(player));
    }

    @Override
    public void onPlayerLeft(ServerPlayerEntity player) {
        this.listeners.forEach(listener -> listener.onPlayerLeft(player));
    }

    @Override
    public void onServerStarted() {
        this.listeners.forEach(ManagementListener::onServerStarted);
    }

    @Override
    public void onServerStopping() {
        this.listeners.forEach(ManagementListener::onServerStopping);
    }

    @Override
    public void onServerSaving() {
        this.listeners.forEach(ManagementListener::onServerSaving);
    }

    @Override
    public void onServerSaved() {
        this.listeners.forEach(ManagementListener::onServerSaved);
    }

    @Override
    public void onServerActivity() {
        this.listeners.forEach(ManagementListener::onServerActivity);
    }

    @Override
    public void onOperatorAdded(OperatorEntry operator) {
        this.listeners.forEach(listener -> listener.onOperatorAdded(operator));
    }

    @Override
    public void onOperatorRemoved(OperatorEntry operator) {
        this.listeners.forEach(listener -> listener.onOperatorRemoved(operator));
    }

    @Override
    public void onAllowlistAdded(PlayerConfigEntry player) {
        this.listeners.forEach(listener -> listener.onAllowlistAdded(player));
    }

    @Override
    public void onAllowlistRemoved(PlayerConfigEntry player) {
        this.listeners.forEach(listener -> listener.onAllowlistRemoved(player));
    }

    @Override
    public void onIpBanAdded(BannedIpEntry entry) {
        this.listeners.forEach(listener -> listener.onIpBanAdded(entry));
    }

    @Override
    public void onIpBanRemoved(String string) {
        this.listeners.forEach(listener -> listener.onIpBanRemoved(string));
    }

    @Override
    public void onBanAdded(BannedPlayerEntry entry) {
        this.listeners.forEach(listener -> listener.onBanAdded(entry));
    }

    @Override
    public void onBanRemoved(PlayerConfigEntry player) {
        this.listeners.forEach(listener -> listener.onBanRemoved(player));
    }

    @Override
    public <T> void onGameRuleUpdated(GameRule<T> rule, T value) {
        this.listeners.forEach(listener -> listener.onGameRuleUpdated(rule, value));
    }

    @Override
    public void onServerStatusHeartbeat() {
        this.listeners.forEach(ManagementListener::onServerStatusHeartbeat);
    }
}


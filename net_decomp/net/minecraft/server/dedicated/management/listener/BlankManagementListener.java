/*
 * Decompiled with CFR 0.152.
 */
package net.minecraft.server.dedicated.management.listener;

import net.minecraft.server.BannedIpEntry;
import net.minecraft.server.BannedPlayerEntry;
import net.minecraft.server.OperatorEntry;
import net.minecraft.server.PlayerConfigEntry;
import net.minecraft.server.dedicated.management.listener.ManagementListener;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.world.rule.GameRule;

public class BlankManagementListener
implements ManagementListener {
    @Override
    public void onPlayerJoined(ServerPlayerEntity player) {
    }

    @Override
    public void onPlayerLeft(ServerPlayerEntity player) {
    }

    @Override
    public void onServerStarted() {
    }

    @Override
    public void onServerStopping() {
    }

    @Override
    public void onServerSaving() {
    }

    @Override
    public void onServerSaved() {
    }

    @Override
    public void onServerActivity() {
    }

    @Override
    public void onOperatorAdded(OperatorEntry operator) {
    }

    @Override
    public void onOperatorRemoved(OperatorEntry operator) {
    }

    @Override
    public void onAllowlistAdded(PlayerConfigEntry player) {
    }

    @Override
    public void onAllowlistRemoved(PlayerConfigEntry player) {
    }

    @Override
    public void onIpBanAdded(BannedIpEntry entry) {
    }

    @Override
    public void onIpBanRemoved(String string) {
    }

    @Override
    public void onBanAdded(BannedPlayerEntry entry) {
    }

    @Override
    public void onBanRemoved(PlayerConfigEntry player) {
    }

    @Override
    public <T> void onGameRuleUpdated(GameRule<T> rule, T value) {
    }

    @Override
    public void onServerStatusHeartbeat() {
    }
}


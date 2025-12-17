/*
 * Decompiled with CFR 0.152.
 */
package net.minecraft.server.dedicated.management.listener;

import net.minecraft.server.BannedIpEntry;
import net.minecraft.server.BannedPlayerEntry;
import net.minecraft.server.OperatorEntry;
import net.minecraft.server.PlayerConfigEntry;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.world.rule.GameRule;

public interface ManagementListener {
    public void onPlayerJoined(ServerPlayerEntity var1);

    public void onPlayerLeft(ServerPlayerEntity var1);

    public void onServerStarted();

    public void onServerStopping();

    public void onServerSaving();

    public void onServerSaved();

    public void onServerActivity();

    public void onOperatorAdded(OperatorEntry var1);

    public void onOperatorRemoved(OperatorEntry var1);

    public void onAllowlistAdded(PlayerConfigEntry var1);

    public void onAllowlistRemoved(PlayerConfigEntry var1);

    public void onIpBanAdded(BannedIpEntry var1);

    public void onIpBanRemoved(String var1);

    public void onBanAdded(BannedPlayerEntry var1);

    public void onBanRemoved(PlayerConfigEntry var1);

    public <T> void onGameRuleUpdated(GameRule<T> var1, T var2);

    public void onServerStatusHeartbeat();
}


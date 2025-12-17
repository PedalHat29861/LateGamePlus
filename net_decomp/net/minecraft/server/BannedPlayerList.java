/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  com.google.gson.JsonObject
 */
package net.minecraft.server;

import com.google.gson.JsonObject;
import java.io.File;
import java.util.Objects;
import net.minecraft.server.BannedPlayerEntry;
import net.minecraft.server.PlayerConfigEntry;
import net.minecraft.server.ServerConfigEntry;
import net.minecraft.server.ServerConfigList;
import net.minecraft.server.dedicated.management.listener.ManagementListener;

public class BannedPlayerList
extends ServerConfigList<PlayerConfigEntry, BannedPlayerEntry> {
    public BannedPlayerList(File file, ManagementListener managementListener) {
        super(file, managementListener);
    }

    @Override
    protected ServerConfigEntry<PlayerConfigEntry> fromJson(JsonObject json) {
        return new BannedPlayerEntry(json);
    }

    @Override
    public boolean contains(PlayerConfigEntry player) {
        return this.contains(player);
    }

    @Override
    public String[] getNames() {
        return (String[])this.values().stream().map(ServerConfigEntry::getKey).filter(Objects::nonNull).map(PlayerConfigEntry::name).toArray(String[]::new);
    }

    @Override
    protected String toString(PlayerConfigEntry playerConfigEntry) {
        return playerConfigEntry.id().toString();
    }

    @Override
    public boolean add(BannedPlayerEntry bannedPlayerEntry) {
        if (super.add(bannedPlayerEntry)) {
            if (bannedPlayerEntry.getKey() != null) {
                this.field_62420.onBanAdded(bannedPlayerEntry);
            }
            return true;
        }
        return false;
    }

    @Override
    public boolean remove(PlayerConfigEntry playerConfigEntry) {
        if (super.remove(playerConfigEntry)) {
            this.field_62420.onBanRemoved(playerConfigEntry);
            return true;
        }
        return false;
    }

    @Override
    public void clear() {
        for (BannedPlayerEntry bannedPlayerEntry : this.values()) {
            if (bannedPlayerEntry.getKey() == null) continue;
            this.field_62420.onBanRemoved((PlayerConfigEntry)bannedPlayerEntry.getKey());
        }
        super.clear();
    }

    @Override
    public /* synthetic */ boolean remove(Object key) {
        return this.remove((PlayerConfigEntry)key);
    }
}


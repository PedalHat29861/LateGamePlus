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
import net.minecraft.server.PlayerConfigEntry;
import net.minecraft.server.ServerConfigEntry;
import net.minecraft.server.ServerConfigList;
import net.minecraft.server.WhitelistEntry;
import net.minecraft.server.dedicated.management.listener.ManagementListener;

public class Whitelist
extends ServerConfigList<PlayerConfigEntry, WhitelistEntry> {
    public Whitelist(File file, ManagementListener managementListener) {
        super(file, managementListener);
    }

    @Override
    protected ServerConfigEntry<PlayerConfigEntry> fromJson(JsonObject json) {
        return new WhitelistEntry(json);
    }

    public boolean isAllowed(PlayerConfigEntry playerConfigEntry) {
        return this.contains(playerConfigEntry);
    }

    @Override
    public boolean add(WhitelistEntry whitelistEntry) {
        if (super.add(whitelistEntry)) {
            if (whitelistEntry.getKey() != null) {
                this.field_62420.onAllowlistAdded((PlayerConfigEntry)whitelistEntry.getKey());
            }
            return true;
        }
        return false;
    }

    @Override
    public boolean remove(PlayerConfigEntry playerConfigEntry) {
        if (super.remove(playerConfigEntry)) {
            this.field_62420.onAllowlistRemoved(playerConfigEntry);
            return true;
        }
        return false;
    }

    @Override
    public void clear() {
        for (WhitelistEntry whitelistEntry : this.values()) {
            if (whitelistEntry.getKey() == null) continue;
            this.field_62420.onAllowlistRemoved((PlayerConfigEntry)whitelistEntry.getKey());
        }
        super.clear();
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
    protected /* synthetic */ String toString(Object profile) {
        return this.toString((PlayerConfigEntry)profile);
    }

    @Override
    public /* synthetic */ boolean remove(Object key) {
        return this.remove((PlayerConfigEntry)key);
    }
}


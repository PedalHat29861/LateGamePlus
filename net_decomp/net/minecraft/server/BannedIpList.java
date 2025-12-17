/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  com.google.gson.JsonObject
 *  org.jspecify.annotations.Nullable
 */
package net.minecraft.server;

import com.google.gson.JsonObject;
import java.io.File;
import java.net.SocketAddress;
import net.minecraft.server.BannedIpEntry;
import net.minecraft.server.ServerConfigEntry;
import net.minecraft.server.ServerConfigList;
import net.minecraft.server.dedicated.management.listener.ManagementListener;
import org.jspecify.annotations.Nullable;

public class BannedIpList
extends ServerConfigList<String, BannedIpEntry> {
    public BannedIpList(File file, ManagementListener managementListener) {
        super(file, managementListener);
    }

    @Override
    protected ServerConfigEntry<String> fromJson(JsonObject json) {
        return new BannedIpEntry(json);
    }

    public boolean isBanned(SocketAddress ip) {
        String string = this.stringifyAddress(ip);
        return this.contains(string);
    }

    public boolean isBanned(String ip) {
        return this.contains(ip);
    }

    @Override
    public @Nullable BannedIpEntry get(SocketAddress address) {
        String string = this.stringifyAddress(address);
        return (BannedIpEntry)this.get(string);
    }

    private String stringifyAddress(SocketAddress address) {
        String string = address.toString();
        if (string.contains("/")) {
            string = string.substring(string.indexOf(47) + 1);
        }
        if (string.contains(":")) {
            string = string.substring(0, string.indexOf(58));
        }
        return string;
    }

    @Override
    public boolean add(BannedIpEntry bannedIpEntry) {
        if (super.add(bannedIpEntry)) {
            if (bannedIpEntry.getKey() != null) {
                this.field_62420.onIpBanAdded(bannedIpEntry);
            }
            return true;
        }
        return false;
    }

    @Override
    public boolean remove(String string) {
        if (super.remove(string)) {
            this.field_62420.onIpBanRemoved(string);
            return true;
        }
        return false;
    }

    @Override
    public void clear() {
        for (BannedIpEntry bannedIpEntry : this.values()) {
            if (bannedIpEntry.getKey() == null) continue;
            this.field_62420.onIpBanRemoved((String)bannedIpEntry.getKey());
        }
        super.clear();
    }

    @Override
    public /* synthetic */ boolean remove(Object key) {
        return this.remove((String)key);
    }
}


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
import net.minecraft.server.OperatorEntry;
import net.minecraft.server.PlayerConfigEntry;
import net.minecraft.server.ServerConfigEntry;
import net.minecraft.server.ServerConfigList;
import net.minecraft.server.dedicated.management.listener.ManagementListener;

public class OperatorList
extends ServerConfigList<PlayerConfigEntry, OperatorEntry> {
    public OperatorList(File file, ManagementListener managementListener) {
        super(file, managementListener);
    }

    @Override
    protected ServerConfigEntry<PlayerConfigEntry> fromJson(JsonObject json) {
        return new OperatorEntry(json);
    }

    @Override
    public String[] getNames() {
        return (String[])this.values().stream().map(ServerConfigEntry::getKey).filter(Objects::nonNull).map(PlayerConfigEntry::name).toArray(String[]::new);
    }

    @Override
    public boolean add(OperatorEntry operatorEntry) {
        if (super.add(operatorEntry)) {
            if (operatorEntry.getKey() != null) {
                this.field_62420.onOperatorAdded(operatorEntry);
            }
            return true;
        }
        return false;
    }

    @Override
    public boolean remove(PlayerConfigEntry playerConfigEntry) {
        OperatorEntry operatorEntry = (OperatorEntry)this.get(playerConfigEntry);
        if (super.remove(playerConfigEntry)) {
            if (operatorEntry != null) {
                this.field_62420.onOperatorRemoved(operatorEntry);
            }
            return true;
        }
        return false;
    }

    @Override
    public void clear() {
        for (OperatorEntry operatorEntry : this.values()) {
            if (operatorEntry.getKey() == null) continue;
            this.field_62420.onOperatorRemoved(operatorEntry);
        }
        super.clear();
    }

    public boolean canBypassPlayerLimit(PlayerConfigEntry playerConfigEntry) {
        OperatorEntry operatorEntry = (OperatorEntry)this.get(playerConfigEntry);
        if (operatorEntry != null) {
            return operatorEntry.canBypassPlayerLimit();
        }
        return false;
    }

    @Override
    protected String toString(PlayerConfigEntry playerConfigEntry) {
        return playerConfigEntry.id().toString();
    }

    @Override
    protected /* synthetic */ String toString(Object profile) {
        return this.toString((PlayerConfigEntry)profile);
    }
}


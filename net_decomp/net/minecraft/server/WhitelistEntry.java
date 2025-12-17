/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  com.google.gson.JsonObject
 */
package net.minecraft.server;

import com.google.gson.JsonObject;
import net.minecraft.server.PlayerConfigEntry;
import net.minecraft.server.ServerConfigEntry;

public class WhitelistEntry
extends ServerConfigEntry<PlayerConfigEntry> {
    public WhitelistEntry(PlayerConfigEntry player) {
        super(player);
    }

    public WhitelistEntry(JsonObject json) {
        super(PlayerConfigEntry.read(json));
    }

    @Override
    protected void write(JsonObject json) {
        if (this.getKey() == null) {
            return;
        }
        ((PlayerConfigEntry)this.getKey()).write(json);
    }
}


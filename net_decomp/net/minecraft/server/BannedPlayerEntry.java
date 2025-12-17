/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  com.google.gson.JsonObject
 *  org.jspecify.annotations.Nullable
 */
package net.minecraft.server;

import com.google.gson.JsonObject;
import java.util.Date;
import net.minecraft.server.BanEntry;
import net.minecraft.server.PlayerConfigEntry;
import net.minecraft.text.Text;
import org.jspecify.annotations.Nullable;

public class BannedPlayerEntry
extends BanEntry<PlayerConfigEntry> {
    private static final Text field_61165 = Text.translatable("commands.banlist.entry.unknown");

    public BannedPlayerEntry(@Nullable PlayerConfigEntry playerConfigEntry) {
        this(playerConfigEntry, (Date)null, (String)null, (Date)null, (String)null);
    }

    public BannedPlayerEntry(@Nullable PlayerConfigEntry playerConfigEntry, @Nullable Date created, @Nullable String source, @Nullable Date expiry, @Nullable String reason) {
        super(playerConfigEntry, created, source, expiry, reason);
    }

    public BannedPlayerEntry(JsonObject json) {
        super(PlayerConfigEntry.read(json), json);
    }

    @Override
    protected void write(JsonObject json) {
        if (this.getKey() == null) {
            return;
        }
        ((PlayerConfigEntry)this.getKey()).write(json);
        super.write(json);
    }

    @Override
    public Text toText() {
        PlayerConfigEntry playerConfigEntry = (PlayerConfigEntry)this.getKey();
        return playerConfigEntry != null ? Text.literal(playerConfigEntry.name()) : field_61165;
    }
}


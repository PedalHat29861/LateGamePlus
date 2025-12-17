/*
 * Decompiled with CFR 0.152.
 */
package net.minecraft.util;

import java.util.Optional;
import java.util.UUID;
import net.minecraft.server.PlayerConfigEntry;

public interface NameToIdCache {
    public void add(PlayerConfigEntry var1);

    public Optional<PlayerConfigEntry> findByName(String var1);

    public Optional<PlayerConfigEntry> getByUuid(UUID var1);

    public void setOfflineMode(boolean var1);

    public void save();
}


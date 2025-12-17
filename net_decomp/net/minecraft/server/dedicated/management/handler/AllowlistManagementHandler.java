/*
 * Decompiled with CFR 0.152.
 */
package net.minecraft.server.dedicated.management.handler;

import java.util.Collection;
import net.minecraft.server.PlayerConfigEntry;
import net.minecraft.server.WhitelistEntry;
import net.minecraft.server.dedicated.management.network.ManagementConnectionId;

public interface AllowlistManagementHandler {
    public Collection<WhitelistEntry> getAllowlist();

    public boolean add(WhitelistEntry var1, ManagementConnectionId var2);

    public void clear(ManagementConnectionId var1);

    public void remove(PlayerConfigEntry var1, ManagementConnectionId var2);

    public void kickUnlisted(ManagementConnectionId var1);
}


/*
 * Decompiled with CFR 0.152.
 */
package net.minecraft.server.dedicated.management.handler;

import java.util.Collection;
import java.util.Optional;
import net.minecraft.command.permission.PermissionLevel;
import net.minecraft.server.OperatorEntry;
import net.minecraft.server.PlayerConfigEntry;
import net.minecraft.server.dedicated.management.network.ManagementConnectionId;

public interface OperatorManagementHandler {
    public Collection<OperatorEntry> getOperators();

    public void addToOperators(PlayerConfigEntry var1, Optional<PermissionLevel> var2, Optional<Boolean> var3, ManagementConnectionId var4);

    public void addToOperators(PlayerConfigEntry var1, ManagementConnectionId var2);

    public void removeFromOperators(PlayerConfigEntry var1, ManagementConnectionId var2);

    public void clearOperators(ManagementConnectionId var1);
}


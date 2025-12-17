/*
 * Decompiled with CFR 0.152.
 */
package net.minecraft.server.dedicated.management.handler;

import java.util.Collection;
import java.util.Optional;
import net.minecraft.command.permission.LeveledPermissionPredicate;
import net.minecraft.command.permission.PermissionLevel;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.OperatorEntry;
import net.minecraft.server.PlayerConfigEntry;
import net.minecraft.server.dedicated.management.ManagementLogger;
import net.minecraft.server.dedicated.management.handler.OperatorManagementHandler;
import net.minecraft.server.dedicated.management.network.ManagementConnectionId;

public class OperatorManagementHandlerImpl
implements OperatorManagementHandler {
    private final MinecraftServer server;
    private final ManagementLogger logger;

    public OperatorManagementHandlerImpl(MinecraftServer server, ManagementLogger logger) {
        this.server = server;
        this.logger = logger;
    }

    @Override
    public Collection<OperatorEntry> getOperators() {
        return this.server.getPlayerManager().getOpList().values();
    }

    @Override
    public void addToOperators(PlayerConfigEntry player, Optional<PermissionLevel> permissionLevel, Optional<Boolean> canBypassPlayerLimit, ManagementConnectionId remote) {
        this.logger.logAction(remote, "Op '{}'", player);
        this.server.getPlayerManager().addToOperators(player, permissionLevel.map(LeveledPermissionPredicate::fromLevel), canBypassPlayerLimit);
    }

    @Override
    public void addToOperators(PlayerConfigEntry player, ManagementConnectionId remote) {
        this.logger.logAction(remote, "Op '{}'", player);
        this.server.getPlayerManager().addToOperators(player);
    }

    @Override
    public void removeFromOperators(PlayerConfigEntry player, ManagementConnectionId remote) {
        this.logger.logAction(remote, "Deop '{}'", player);
        this.server.getPlayerManager().removeFromOperators(player);
    }

    @Override
    public void clearOperators(ManagementConnectionId remote) {
        this.logger.logAction(remote, "Clear operator list", new Object[0]);
        this.server.getPlayerManager().getOpList().clear();
    }
}


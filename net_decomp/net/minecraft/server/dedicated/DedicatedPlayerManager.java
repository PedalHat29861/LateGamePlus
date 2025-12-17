/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  com.mojang.logging.LogUtils
 *  org.slf4j.Logger
 */
package net.minecraft.server.dedicated;

import com.mojang.logging.LogUtils;
import java.io.IOException;
import net.minecraft.registry.CombinedDynamicRegistries;
import net.minecraft.registry.ServerDynamicRegistryType;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.PlayerConfigEntry;
import net.minecraft.server.PlayerManager;
import net.minecraft.server.dedicated.MinecraftDedicatedServer;
import net.minecraft.world.PlayerSaveHandler;
import org.slf4j.Logger;

public class DedicatedPlayerManager
extends PlayerManager {
    private static final Logger LOGGER = LogUtils.getLogger();

    public DedicatedPlayerManager(MinecraftDedicatedServer minecraftDedicatedServer, CombinedDynamicRegistries<ServerDynamicRegistryType> tracker, PlayerSaveHandler saveHandler) {
        super(minecraftDedicatedServer, tracker, saveHandler, minecraftDedicatedServer.getManagementListener());
        this.setViewDistance(minecraftDedicatedServer.getViewDistance());
        this.setSimulationDistance(minecraftDedicatedServer.getSimulationDistance());
        this.loadUserBanList();
        this.saveUserBanList();
        this.loadIpBanList();
        this.saveIpBanList();
        this.loadOpList();
        this.loadWhitelist();
        this.saveOpList();
        if (!this.getWhitelist().getFile().exists()) {
            this.saveWhitelist();
        }
    }

    @Override
    public void reloadWhitelist() {
        this.loadWhitelist();
    }

    private void saveIpBanList() {
        try {
            this.getIpBanList().save();
        }
        catch (IOException iOException) {
            LOGGER.warn("Failed to save ip banlist: ", (Throwable)iOException);
        }
    }

    private void saveUserBanList() {
        try {
            this.getUserBanList().save();
        }
        catch (IOException iOException) {
            LOGGER.warn("Failed to save user banlist: ", (Throwable)iOException);
        }
    }

    private void loadIpBanList() {
        try {
            this.getIpBanList().load();
        }
        catch (IOException iOException) {
            LOGGER.warn("Failed to load ip banlist: ", (Throwable)iOException);
        }
    }

    private void loadUserBanList() {
        try {
            this.getUserBanList().load();
        }
        catch (IOException iOException) {
            LOGGER.warn("Failed to load user banlist: ", (Throwable)iOException);
        }
    }

    private void loadOpList() {
        try {
            this.getOpList().load();
        }
        catch (Exception exception) {
            LOGGER.warn("Failed to load operators list: ", (Throwable)exception);
        }
    }

    private void saveOpList() {
        try {
            this.getOpList().save();
        }
        catch (Exception exception) {
            LOGGER.warn("Failed to save operators list: ", (Throwable)exception);
        }
    }

    private void loadWhitelist() {
        try {
            this.getWhitelist().load();
        }
        catch (Exception exception) {
            LOGGER.warn("Failed to load white-list: ", (Throwable)exception);
        }
    }

    private void saveWhitelist() {
        try {
            this.getWhitelist().save();
        }
        catch (Exception exception) {
            LOGGER.warn("Failed to save white-list: ", (Throwable)exception);
        }
    }

    @Override
    public boolean isWhitelisted(PlayerConfigEntry player) {
        return !this.isWhitelistEnabled() || this.isOperator(player) || this.getWhitelist().isAllowed(player);
    }

    @Override
    public MinecraftDedicatedServer getServer() {
        return (MinecraftDedicatedServer)super.getServer();
    }

    @Override
    public boolean canBypassPlayerLimit(PlayerConfigEntry configEntry) {
        return this.getOpList().canBypassPlayerLimit(configEntry);
    }

    @Override
    public /* synthetic */ MinecraftServer getServer() {
        return this.getServer();
    }
}


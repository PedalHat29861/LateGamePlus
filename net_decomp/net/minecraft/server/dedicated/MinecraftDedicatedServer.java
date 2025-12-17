/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  com.google.common.collect.ImmutableMap
 *  com.google.common.collect.ImmutableMap$Builder
 *  com.google.common.collect.Lists
 *  com.google.common.net.HostAndPort
 *  com.mojang.datafixers.DataFixer
 *  com.mojang.logging.LogUtils
 *  io.netty.handler.ssl.SslContext
 *  org.jspecify.annotations.Nullable
 *  org.slf4j.Logger
 */
package net.minecraft.server.dedicated;

import com.google.common.collect.ImmutableMap;
import com.google.common.collect.Lists;
import com.google.common.net.HostAndPort;
import com.mojang.datafixers.DataFixer;
import com.mojang.logging.LogUtils;
import io.netty.handler.ssl.SslContext;
import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.InetAddress;
import java.net.Proxy;
import java.net.URI;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.LinkOption;
import java.nio.file.OpenOption;
import java.nio.file.Path;
import java.util.Collections;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Optional;
import java.util.function.BooleanSupplier;
import java.util.stream.Stream;
import net.minecraft.SharedConstants;
import net.minecraft.command.permission.LeveledPermissionPredicate;
import net.minecraft.command.permission.PermissionPredicate;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.network.encryption.BearerToken;
import net.minecraft.resource.ResourcePackManager;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.PlayerConfigEntry;
import net.minecraft.server.PlayerManager;
import net.minecraft.server.SaveLoader;
import net.minecraft.server.ServerConfigHandler;
import net.minecraft.server.ServerLinks;
import net.minecraft.server.command.ServerCommandSource;
import net.minecraft.server.dedicated.DedicatedPlayerManager;
import net.minecraft.server.dedicated.DedicatedServer;
import net.minecraft.server.dedicated.DedicatedServerWatchdog;
import net.minecraft.server.dedicated.PendingServerCommand;
import net.minecraft.server.dedicated.ServerMBean;
import net.minecraft.server.dedicated.ServerPropertiesHandler;
import net.minecraft.server.dedicated.ServerPropertiesLoader;
import net.minecraft.server.dedicated.gui.DedicatedServerGui;
import net.minecraft.server.dedicated.management.ManagementServer;
import net.minecraft.server.dedicated.management.ManagementServerEncryption;
import net.minecraft.server.dedicated.management.dispatch.ManagementHandlerDispatcher;
import net.minecraft.server.dedicated.management.listener.NotificationManagementListener;
import net.minecraft.server.dedicated.management.network.BearerAuthenticationHandler;
import net.minecraft.server.filter.AbstractTextFilterer;
import net.minecraft.server.filter.TextStream;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.server.rcon.QueryResponseHandler;
import net.minecraft.server.rcon.RconCommandOutput;
import net.minecraft.server.rcon.RconListener;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.util.ApiServices;
import net.minecraft.util.StringHelper;
import net.minecraft.util.SystemDetails;
import net.minecraft.util.TimeHelper;
import net.minecraft.util.Util;
import net.minecraft.util.logging.UncaughtExceptionHandler;
import net.minecraft.util.logging.UncaughtExceptionLogger;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.MathHelper;
import net.minecraft.util.profiler.ServerTickType;
import net.minecraft.util.profiler.log.DebugSampleLog;
import net.minecraft.util.profiler.log.DebugSampleType;
import net.minecraft.util.profiler.log.SubscribableDebugSampleLog;
import net.minecraft.world.Difficulty;
import net.minecraft.world.GameMode;
import net.minecraft.world.WorldProperties;
import net.minecraft.world.chunk.LoggingChunkLoadProgress;
import net.minecraft.world.debug.DebugSubscriptionTypes;
import net.minecraft.world.level.storage.LevelStorage;
import net.minecraft.world.rule.GameRules;
import org.jspecify.annotations.Nullable;
import org.slf4j.Logger;

public class MinecraftDedicatedServer
extends MinecraftServer
implements DedicatedServer {
    static final Logger LOGGER = LogUtils.getLogger();
    private static final int field_29662 = 5000;
    private static final int field_29663 = 2;
    private final List<PendingServerCommand> commandQueue = Collections.synchronizedList(Lists.newArrayList());
    private @Nullable QueryResponseHandler queryResponseHandler;
    private final RconCommandOutput rconCommandOutput;
    private @Nullable RconListener rconServer;
    private final ServerPropertiesLoader propertiesLoader;
    private @Nullable DedicatedServerGui gui;
    private final @Nullable AbstractTextFilterer filterer;
    private @Nullable SubscribableDebugSampleLog debugSampleLog;
    private boolean shouldPushTickTimeLog;
    private final ServerLinks serverLinks;
    private final Map<String, String> codeOfConductLanguages;
    private @Nullable ManagementServer managementServer;
    private long lastManagementHeartbeatTime;

    public MinecraftDedicatedServer(Thread serverThread, LevelStorage.Session session, ResourcePackManager dataPackManager, SaveLoader saveLoader, ServerPropertiesLoader propertiesLoader, DataFixer dataFixer, ApiServices apiServices) {
        super(serverThread, session, dataPackManager, saveLoader, Proxy.NO_PROXY, dataFixer, apiServices, LoggingChunkLoadProgress.withoutPlayer());
        this.propertiesLoader = propertiesLoader;
        this.rconCommandOutput = new RconCommandOutput(this);
        this.filterer = AbstractTextFilterer.createTextFilter(propertiesLoader.getPropertiesHandler());
        this.serverLinks = MinecraftDedicatedServer.loadServerLinks(propertiesLoader);
        this.codeOfConductLanguages = propertiesLoader.getPropertiesHandler().enableCodeOfConduct ? MinecraftDedicatedServer.loadCodeOfConductLanguages() : Map.of();
    }

    /*
     * Enabled force condition propagation
     * Lifted jumps to return sites
     */
    private static Map<String, String> loadCodeOfConductLanguages() {
        Path path = Path.of("codeofconduct", new String[0]);
        if (!Files.isDirectory(path, LinkOption.NOFOLLOW_LINKS)) {
            throw new IllegalArgumentException("Code of Conduct folder does not exist: " + String.valueOf(path));
        }
        try {
            ImmutableMap.Builder builder = ImmutableMap.builder();
            try (Stream<Path> stream = Files.list(path);){
                for (Path path2 : stream.toList()) {
                    String string = path2.getFileName().toString();
                    if (!string.endsWith(".txt")) continue;
                    String string2 = string.substring(0, string.length() - 4).toLowerCase(Locale.ROOT);
                    if (!path2.toRealPath(new LinkOption[0]).getParent().equals(path.toAbsolutePath())) {
                        throw new IllegalArgumentException("Failed to read Code of Conduct file \"" + string + "\" because it links to a file outside the allowed directory");
                    }
                    try {
                        String string3 = String.join((CharSequence)"\n", Files.readAllLines(path2, StandardCharsets.UTF_8));
                        builder.put((Object)string2, (Object)StringHelper.stripTextFormat(string3));
                    }
                    catch (IOException iOException) {
                        throw new IllegalArgumentException("Failed to read Code of Conduct file " + string, iOException);
                        return builder.build();
                    }
                }
            }
        }
        catch (IOException iOException2) {
            throw new IllegalArgumentException("Failed to read Code of Conduct folder", iOException2);
        }
    }

    private SslContext createManagementSslContext() {
        try {
            return ManagementServerEncryption.createContext(this.getProperties().managementServerTlsKeystore, this.getProperties().managementServerKeystorePassword);
        }
        catch (Exception exception) {
            ManagementServerEncryption.logInstructions();
            throw new IllegalStateException("Failed to configure TLS for the server management protocol", exception);
        }
    }

    @Override
    public boolean setupServer() throws IOException {
        int i = this.getProperties().managementServerPort;
        if (this.getProperties().managementServerEnabled) {
            String string = this.propertiesLoader.getPropertiesHandler().managementServerSecret;
            if (!BearerToken.isValid(string)) {
                throw new IllegalStateException("Invalid management server secret, must be 40 alphanumeric characters");
            }
            String string2 = this.getProperties().managementServerHost;
            HostAndPort hostAndPort = HostAndPort.fromParts((String)string2, (int)i);
            BearerToken bearerToken = new BearerToken(string);
            String string3 = this.getProperties().managementServerAllowedOrigin;
            BearerAuthenticationHandler bearerAuthenticationHandler = new BearerAuthenticationHandler(bearerToken, string3);
            LOGGER.info("Starting json RPC server on {}", (Object)hostAndPort);
            this.managementServer = new ManagementServer(hostAndPort, bearerAuthenticationHandler);
            ManagementHandlerDispatcher managementHandlerDispatcher = ManagementHandlerDispatcher.create(this);
            managementHandlerDispatcher.getListener().addListener(new NotificationManagementListener(managementHandlerDispatcher, this.managementServer));
            if (this.getProperties().managementServerTlsEnabled) {
                SslContext sslContext = this.createManagementSslContext();
                this.managementServer.listenEncrypted(managementHandlerDispatcher, sslContext);
            } else {
                this.managementServer.listenUnencrypted(managementHandlerDispatcher);
            }
        }
        Thread thread = new Thread("Server console handler"){

            @Override
            public void run() {
                BufferedReader bufferedReader = new BufferedReader(new InputStreamReader(System.in, StandardCharsets.UTF_8));
                try {
                    String string;
                    while (!MinecraftDedicatedServer.this.isStopped() && MinecraftDedicatedServer.this.isRunning() && (string = bufferedReader.readLine()) != null) {
                        MinecraftDedicatedServer.this.enqueueCommand(string, MinecraftDedicatedServer.this.getCommandSource());
                    }
                }
                catch (IOException iOException) {
                    LOGGER.error("Exception handling console input", (Throwable)iOException);
                }
            }
        };
        thread.setDaemon(true);
        thread.setUncaughtExceptionHandler(new UncaughtExceptionLogger(LOGGER));
        thread.start();
        LOGGER.info("Starting minecraft server version {}", (Object)SharedConstants.getGameVersion().name());
        if (Runtime.getRuntime().maxMemory() / 1024L / 1024L < 512L) {
            LOGGER.warn("To start the server with more ram, launch it as \"java -Xmx1024M -Xms1024M -jar minecraft_server.jar\"");
        }
        LOGGER.info("Loading properties");
        ServerPropertiesHandler serverPropertiesHandler = this.propertiesLoader.getPropertiesHandler();
        if (this.isSingleplayer()) {
            this.setServerIp("127.0.0.1");
        } else {
            this.setOnlineMode(serverPropertiesHandler.onlineMode);
            this.setPreventProxyConnections(serverPropertiesHandler.preventProxyConnections);
            this.setServerIp(serverPropertiesHandler.serverIp);
        }
        this.saveProperties.setGameMode(serverPropertiesHandler.gameMode.get());
        LOGGER.info("Default game type: {}", (Object)serverPropertiesHandler.gameMode.get());
        InetAddress inetAddress = null;
        if (!this.getServerIp().isEmpty()) {
            inetAddress = InetAddress.getByName(this.getServerIp());
        }
        if (this.getServerPort() < 0) {
            this.setServerPort(serverPropertiesHandler.serverPort);
        }
        this.generateKeyPair();
        LOGGER.info("Starting Minecraft server on {}:{}", (Object)(this.getServerIp().isEmpty() ? "*" : this.getServerIp()), (Object)this.getServerPort());
        try {
            this.getNetworkIo().bind(inetAddress, this.getServerPort());
        }
        catch (IOException iOException) {
            LOGGER.warn("**** FAILED TO BIND TO PORT!");
            LOGGER.warn("The exception was: {}", (Object)iOException.toString());
            LOGGER.warn("Perhaps a server is already running on that port?");
            return false;
        }
        if (!this.isOnlineMode()) {
            LOGGER.warn("**** SERVER IS RUNNING IN OFFLINE/INSECURE MODE!");
            LOGGER.warn("The server will make no attempt to authenticate usernames. Beware.");
            LOGGER.warn("While this makes the game possible to play without internet access, it also opens up the ability for hackers to connect with any username they choose.");
            LOGGER.warn("To change this, set \"online-mode\" to \"true\" in the server.properties file.");
        }
        if (this.convertData()) {
            this.apiServices.nameToIdCache().save();
        }
        if (!ServerConfigHandler.checkSuccess(this)) {
            return false;
        }
        this.setPlayerManager(new DedicatedPlayerManager(this, this.getCombinedDynamicRegistries(), this.saveHandler));
        this.debugSampleLog = new SubscribableDebugSampleLog(ServerTickType.values().length, this.getSubscriberTracker(), DebugSampleType.TICK_TIME);
        long l = Util.getMeasuringTimeNano();
        this.apiServices.nameToIdCache().setOfflineMode(!this.isOnlineMode());
        LOGGER.info("Preparing level \"{}\"", (Object)this.getLevelName());
        this.loadWorld();
        long m = Util.getMeasuringTimeNano() - l;
        String string4 = String.format(Locale.ROOT, "%.3fs", (double)m / 1.0E9);
        LOGGER.info("Done ({})! For help, type \"help\"", (Object)string4);
        if (serverPropertiesHandler.announcePlayerAchievements != null) {
            this.saveProperties.getGameRules().setValue(GameRules.ANNOUNCE_ADVANCEMENTS, serverPropertiesHandler.announcePlayerAchievements, this);
        }
        if (serverPropertiesHandler.enableQuery) {
            LOGGER.info("Starting GS4 status listener");
            this.queryResponseHandler = QueryResponseHandler.create(this);
        }
        if (serverPropertiesHandler.enableRcon) {
            LOGGER.info("Starting remote control listener");
            this.rconServer = RconListener.create(this);
        }
        if (this.getMaxTickTime() > 0L) {
            Thread thread2 = new Thread(new DedicatedServerWatchdog(this));
            thread2.setUncaughtExceptionHandler(new UncaughtExceptionHandler(LOGGER));
            thread2.setName("Server Watchdog");
            thread2.setDaemon(true);
            thread2.start();
        }
        if (serverPropertiesHandler.enableJmxMonitoring) {
            ServerMBean.register(this);
            LOGGER.info("JMX monitoring enabled");
        }
        this.getManagementListener().onServerStarted();
        return true;
    }

    @Override
    public boolean isEnforceWhitelist() {
        return this.propertiesLoader.getPropertiesHandler().enforceWhitelist.get();
    }

    @Override
    public void setEnforceWhitelist(boolean enforceWhitelist) {
        this.propertiesLoader.apply(handler -> (ServerPropertiesHandler)handler.enforceWhitelist.set(this.getRegistryManager(), enforceWhitelist));
    }

    @Override
    public boolean getUseAllowlist() {
        return this.propertiesLoader.getPropertiesHandler().whiteList.get();
    }

    @Override
    public void setUseAllowlist(boolean useAllowlist) {
        this.propertiesLoader.apply(handler -> (ServerPropertiesHandler)handler.whiteList.set(this.getRegistryManager(), useAllowlist));
    }

    @Override
    public void tick(BooleanSupplier shouldKeepTicking) {
        long m;
        super.tick(shouldKeepTicking);
        if (this.managementServer != null) {
            this.managementServer.processTimeouts();
        }
        long l = Util.getMeasuringTimeMs();
        int i = this.getStatusHeartbeatInterval();
        if (i > 0 && l - this.lastManagementHeartbeatTime >= (m = (long)i * TimeHelper.SECOND_IN_MILLIS)) {
            this.lastManagementHeartbeatTime = l;
            this.getManagementListener().onServerStatusHeartbeat();
        }
    }

    @Override
    public boolean save(boolean suppressLogs, boolean flush, boolean force) {
        this.getManagementListener().onServerSaving();
        boolean bl = super.save(suppressLogs, flush, force);
        this.getManagementListener().onServerSaved();
        return bl;
    }

    @Override
    public boolean isFlightEnabled() {
        return this.propertiesLoader.getPropertiesHandler().allowFlight.get();
    }

    public void setAllowFlight(boolean allowFlight) {
        this.propertiesLoader.apply(handler -> (ServerPropertiesHandler)handler.allowFlight.set(this.getRegistryManager(), allowFlight));
    }

    @Override
    public ServerPropertiesHandler getProperties() {
        return this.propertiesLoader.getPropertiesHandler();
    }

    public void setDifficulty(Difficulty difficulty) {
        this.propertiesLoader.apply(handler -> (ServerPropertiesHandler)handler.difficulty.set(this.getRegistryManager(), difficulty));
        this.updateDifficulty();
    }

    @Override
    public void updateDifficulty() {
        this.setDifficulty(this.getProperties().difficulty.get(), true);
    }

    public int getViewDistance() {
        return this.propertiesLoader.getPropertiesHandler().viewDistance.get();
    }

    public void setViewDistance(int viewDistance) {
        this.propertiesLoader.apply(handler -> (ServerPropertiesHandler)handler.viewDistance.set(this.getRegistryManager(), viewDistance));
        this.getPlayerManager().setViewDistance(viewDistance);
    }

    public int getSimulationDistance() {
        return this.propertiesLoader.getPropertiesHandler().simulationDistance.get();
    }

    public void setSimulationDistance(int simulationDistance) {
        this.propertiesLoader.apply(handler -> (ServerPropertiesHandler)handler.simulationDistance.set(this.getRegistryManager(), simulationDistance));
        this.getPlayerManager().setSimulationDistance(simulationDistance);
    }

    @Override
    public SystemDetails addExtraSystemDetails(SystemDetails details) {
        details.addSection("Is Modded", () -> this.getModStatus().getMessage());
        details.addSection("Type", () -> "Dedicated Server (map_server.txt)");
        return details;
    }

    @Override
    public void dumpProperties(Path file) throws IOException {
        ServerPropertiesHandler serverPropertiesHandler = this.getProperties();
        try (BufferedWriter writer = Files.newBufferedWriter(file, new OpenOption[0]);){
            writer.write(String.format(Locale.ROOT, "sync-chunk-writes=%s%n", serverPropertiesHandler.syncChunkWrites));
            writer.write(String.format(Locale.ROOT, "gamemode=%s%n", serverPropertiesHandler.gameMode.get()));
            writer.write(String.format(Locale.ROOT, "entity-broadcast-range-percentage=%d%n", serverPropertiesHandler.entityBroadcastRangePercentage.get()));
            writer.write(String.format(Locale.ROOT, "max-world-size=%d%n", serverPropertiesHandler.maxWorldSize));
            writer.write(String.format(Locale.ROOT, "view-distance=%d%n", serverPropertiesHandler.viewDistance.get()));
            writer.write(String.format(Locale.ROOT, "simulation-distance=%d%n", serverPropertiesHandler.simulationDistance.get()));
            writer.write(String.format(Locale.ROOT, "generate-structures=%s%n", serverPropertiesHandler.generatorOptions.shouldGenerateStructures()));
            writer.write(String.format(Locale.ROOT, "use-native=%s%n", serverPropertiesHandler.useNativeTransport));
            writer.write(String.format(Locale.ROOT, "rate-limit=%d%n", serverPropertiesHandler.rateLimit));
        }
    }

    @Override
    public void exit() {
        if (this.filterer != null) {
            this.filterer.close();
        }
        if (this.gui != null) {
            this.gui.stop();
        }
        if (this.rconServer != null) {
            this.rconServer.stop();
        }
        if (this.queryResponseHandler != null) {
            this.queryResponseHandler.stop();
        }
        if (this.managementServer != null) {
            try {
                this.managementServer.stop(true);
            }
            catch (InterruptedException interruptedException) {
                LOGGER.error("Interrupted while stopping the management server", (Throwable)interruptedException);
            }
        }
    }

    @Override
    public void tickNetworkIo() {
        super.tickNetworkIo();
        this.executeQueuedCommands();
    }

    public void enqueueCommand(String command, ServerCommandSource commandSource) {
        this.commandQueue.add(new PendingServerCommand(command, commandSource));
    }

    public void executeQueuedCommands() {
        while (!this.commandQueue.isEmpty()) {
            PendingServerCommand pendingServerCommand = this.commandQueue.remove(0);
            this.getCommandManager().parseAndExecute(pendingServerCommand.source, pendingServerCommand.command);
        }
    }

    @Override
    public boolean isDedicated() {
        return true;
    }

    @Override
    public int getRateLimit() {
        return this.getProperties().rateLimit;
    }

    @Override
    public boolean isUsingNativeTransport() {
        return this.getProperties().useNativeTransport;
    }

    @Override
    public DedicatedPlayerManager getPlayerManager() {
        return (DedicatedPlayerManager)super.getPlayerManager();
    }

    @Override
    public int getMaxPlayerCount() {
        return this.propertiesLoader.getPropertiesHandler().maxPlayers.get();
    }

    public void setMaxPlayers(int maxPlayers) {
        this.propertiesLoader.apply(handler -> (ServerPropertiesHandler)handler.maxPlayers.set(this.getRegistryManager(), maxPlayers));
    }

    @Override
    public boolean isRemote() {
        return true;
    }

    @Override
    public String getHostname() {
        return this.getServerIp();
    }

    @Override
    public int getPort() {
        return this.getServerPort();
    }

    @Override
    public String getMotd() {
        return this.getServerMotd();
    }

    public void createGui() {
        if (this.gui == null) {
            this.gui = DedicatedServerGui.create(this);
        }
    }

    public int getSpawnProtectionRadius() {
        return this.getProperties().spawnProtection.get();
    }

    public void setSpawnProtectionRadius(int spawnProtectionRadius) {
        this.propertiesLoader.apply(handler -> (ServerPropertiesHandler)handler.spawnProtection.set(this.getRegistryManager(), spawnProtectionRadius));
    }

    @Override
    public boolean isSpawnProtected(ServerWorld world, BlockPos pos, PlayerEntity player) {
        int j;
        WorldProperties.SpawnPoint spawnPoint = world.getSpawnPoint();
        if (world.getRegistryKey() != spawnPoint.getDimension()) {
            return false;
        }
        if (this.getPlayerManager().getOpList().isEmpty()) {
            return false;
        }
        if (this.getPlayerManager().isOperator(player.getPlayerConfigEntry())) {
            return false;
        }
        if (this.getSpawnProtectionRadius() <= 0) {
            return false;
        }
        BlockPos blockPos = spawnPoint.getPos();
        int i = MathHelper.abs(pos.getX() - blockPos.getX());
        int k = Math.max(i, j = MathHelper.abs(pos.getZ() - blockPos.getZ()));
        return k <= this.getSpawnProtectionRadius();
    }

    @Override
    public boolean acceptsStatusQuery() {
        return this.getProperties().enableStatus.get();
    }

    public void setStatusReplies(boolean statusReplies) {
        this.propertiesLoader.apply(handler -> (ServerPropertiesHandler)handler.enableStatus.set(this.getRegistryManager(), statusReplies));
    }

    @Override
    public boolean hideOnlinePlayers() {
        return this.getProperties().hideOnlinePlayers.get();
    }

    public void setHideOnlinePlayers(boolean hideOnlinePlayers) {
        this.propertiesLoader.apply(handler -> (ServerPropertiesHandler)handler.hideOnlinePlayers.set(this.getRegistryManager(), hideOnlinePlayers));
    }

    @Override
    public LeveledPermissionPredicate getOpPermissionLevel() {
        return this.getProperties().opPermissionLevel.get();
    }

    public void setOperatorUserPermissionLevel(LeveledPermissionPredicate operatorUserPermissionLevel) {
        this.propertiesLoader.apply(handler -> (ServerPropertiesHandler)handler.opPermissionLevel.set(this.getRegistryManager(), operatorUserPermissionLevel));
    }

    @Override
    public PermissionPredicate getFunctionPermissions() {
        return this.getProperties().functionPermissionLevel;
    }

    @Override
    public int getPlayerIdleTimeout() {
        return this.propertiesLoader.getPropertiesHandler().playerIdleTimeout.get();
    }

    @Override
    public void setPlayerIdleTimeout(int playerIdleTimeout) {
        this.propertiesLoader.apply(serverPropertiesHandler -> (ServerPropertiesHandler)serverPropertiesHandler.playerIdleTimeout.set(this.getRegistryManager(), playerIdleTimeout));
    }

    public int getStatusHeartbeatInterval() {
        return this.propertiesLoader.getPropertiesHandler().statusHeartbeatInterval.get();
    }

    public void setStatusHeartbeatInterval(int statusHeartbeatInterval) {
        this.propertiesLoader.apply(handler -> (ServerPropertiesHandler)handler.statusHeartbeatInterval.set(this.getRegistryManager(), statusHeartbeatInterval));
    }

    @Override
    public String getServerMotd() {
        return this.propertiesLoader.getPropertiesHandler().motd.get();
    }

    @Override
    public void setMotd(String motd) {
        this.propertiesLoader.apply(handler -> (ServerPropertiesHandler)handler.motd.set(this.getRegistryManager(), motd));
    }

    @Override
    public boolean shouldBroadcastRconToOps() {
        return this.getProperties().broadcastRconToOps;
    }

    @Override
    public boolean shouldBroadcastConsoleToOps() {
        return this.getProperties().broadcastConsoleToOps;
    }

    @Override
    public int getMaxWorldBorderRadius() {
        return this.getProperties().maxWorldSize;
    }

    @Override
    public int getNetworkCompressionThreshold() {
        return this.getProperties().networkCompressionThreshold;
    }

    @Override
    public boolean shouldEnforceSecureProfile() {
        ServerPropertiesHandler serverPropertiesHandler = this.getProperties();
        return serverPropertiesHandler.enforceSecureProfile && serverPropertiesHandler.onlineMode && this.apiServices.providesProfileKeys();
    }

    @Override
    public boolean shouldLogIps() {
        return this.getProperties().logIps;
    }

    protected boolean convertData() {
        int i;
        boolean bl = false;
        for (i = 0; !bl && i <= 2; ++i) {
            if (i > 0) {
                LOGGER.warn("Encountered a problem while converting the user banlist, retrying in a few seconds");
                this.sleepFiveSeconds();
            }
            bl = ServerConfigHandler.convertBannedPlayers(this);
        }
        boolean bl2 = false;
        for (i = 0; !bl2 && i <= 2; ++i) {
            if (i > 0) {
                LOGGER.warn("Encountered a problem while converting the ip banlist, retrying in a few seconds");
                this.sleepFiveSeconds();
            }
            bl2 = ServerConfigHandler.convertBannedIps(this);
        }
        boolean bl3 = false;
        for (i = 0; !bl3 && i <= 2; ++i) {
            if (i > 0) {
                LOGGER.warn("Encountered a problem while converting the op list, retrying in a few seconds");
                this.sleepFiveSeconds();
            }
            bl3 = ServerConfigHandler.convertOperators(this);
        }
        boolean bl4 = false;
        for (i = 0; !bl4 && i <= 2; ++i) {
            if (i > 0) {
                LOGGER.warn("Encountered a problem while converting the whitelist, retrying in a few seconds");
                this.sleepFiveSeconds();
            }
            bl4 = ServerConfigHandler.convertWhitelist(this);
        }
        boolean bl5 = false;
        for (i = 0; !bl5 && i <= 2; ++i) {
            if (i > 0) {
                LOGGER.warn("Encountered a problem while converting the player save files, retrying in a few seconds");
                this.sleepFiveSeconds();
            }
            bl5 = ServerConfigHandler.convertPlayerFiles(this);
        }
        return bl || bl2 || bl3 || bl4 || bl5;
    }

    private void sleepFiveSeconds() {
        try {
            Thread.sleep(5000L);
        }
        catch (InterruptedException interruptedException) {
            return;
        }
    }

    public long getMaxTickTime() {
        return this.getProperties().maxTickTime;
    }

    @Override
    public int getMaxChainedNeighborUpdates() {
        return this.getProperties().maxChainedNeighborUpdates;
    }

    @Override
    public String getPlugins() {
        return "";
    }

    @Override
    public String executeRconCommand(String command) {
        this.rconCommandOutput.clear();
        this.submitAndJoin(() -> this.getCommandManager().parseAndExecute(this.rconCommandOutput.createRconCommandSource(), command));
        return this.rconCommandOutput.asString();
    }

    @Override
    public void shutdown() {
        this.getManagementListener().onServerStopping();
        super.shutdown();
        Util.shutdownExecutors();
    }

    @Override
    public boolean isHost(PlayerConfigEntry player) {
        return false;
    }

    @Override
    public int adjustTrackingDistance(int initialDistance) {
        return this.getEntityBroadcastRange() * initialDistance / 100;
    }

    public int getEntityBroadcastRange() {
        return this.getProperties().entityBroadcastRangePercentage.get();
    }

    public void setEntityBroadcastRange(int entityBroadcastRange) {
        this.propertiesLoader.apply(handler -> (ServerPropertiesHandler)handler.entityBroadcastRangePercentage.set(this.getRegistryManager(), entityBroadcastRange));
    }

    @Override
    public String getLevelName() {
        return this.session.getDirectoryName();
    }

    @Override
    public boolean syncChunkWrites() {
        return this.propertiesLoader.getPropertiesHandler().syncChunkWrites;
    }

    @Override
    public TextStream createFilterer(ServerPlayerEntity player) {
        if (this.filterer != null) {
            return this.filterer.createFilterer(player.getGameProfile());
        }
        return TextStream.UNFILTERED;
    }

    @Override
    public @Nullable GameMode getForcedGameMode() {
        return this.getForceGameMode() ? this.saveProperties.getGameMode() : null;
    }

    public boolean getForceGameMode() {
        return this.propertiesLoader.getPropertiesHandler().forceGameMode.get();
    }

    public void setForceGameMode(boolean forceGameMode) {
        this.propertiesLoader.apply(handler -> (ServerPropertiesHandler)handler.forceGameMode.set(this.getRegistryManager(), forceGameMode));
        this.changeGameModeGlobally(this.getForcedGameMode());
    }

    public GameMode getGameMode() {
        return this.getProperties().gameMode.get();
    }

    public void setGameMode(GameMode gameMode) {
        this.propertiesLoader.apply(handler -> (ServerPropertiesHandler)handler.gameMode.set(this.getRegistryManager(), gameMode));
        this.saveProperties.setGameMode(this.getGameMode());
        this.changeGameModeGlobally(this.getForcedGameMode());
    }

    @Override
    public Optional<MinecraftServer.ServerResourcePackProperties> getResourcePackProperties() {
        return this.propertiesLoader.getPropertiesHandler().serverResourcePackProperties;
    }

    @Override
    public void endTickMetrics() {
        super.endTickMetrics();
        this.shouldPushTickTimeLog = this.getSubscriberTracker().hasSubscriber(DebugSubscriptionTypes.DEDICATED_SERVER_TICK_TIME);
    }

    @Override
    public DebugSampleLog getDebugSampleLog() {
        return this.debugSampleLog;
    }

    @Override
    public boolean shouldPushTickTimeLog() {
        return this.shouldPushTickTimeLog;
    }

    @Override
    public boolean acceptsTransfers() {
        return this.propertiesLoader.getPropertiesHandler().acceptsTransfers.get();
    }

    public void setAcceptTransfers(boolean acceptTransfers) {
        this.propertiesLoader.apply(handler -> (ServerPropertiesHandler)handler.acceptsTransfers.set(this.getRegistryManager(), acceptTransfers));
    }

    @Override
    public ServerLinks getServerLinks() {
        return this.serverLinks;
    }

    @Override
    public int getPauseWhenEmptySeconds() {
        return this.propertiesLoader.getPropertiesHandler().pauseWhenEmptySeconds.get();
    }

    public void setPauseWhenEmptySeconds(int pauseWhenEmptySeconds) {
        this.propertiesLoader.apply(handler -> (ServerPropertiesHandler)handler.pauseWhenEmptySeconds.set(this.getRegistryManager(), pauseWhenEmptySeconds));
    }

    private static ServerLinks loadServerLinks(ServerPropertiesLoader propertiesLoader) {
        Optional<URI> optional = MinecraftDedicatedServer.parseBugReportLink(propertiesLoader.getPropertiesHandler());
        return optional.map(uri -> new ServerLinks(List.of(ServerLinks.Known.BUG_REPORT.createEntry((URI)uri)))).orElse(ServerLinks.EMPTY);
    }

    private static Optional<URI> parseBugReportLink(ServerPropertiesHandler propertiesHandler) {
        String string = propertiesHandler.bugReportLink;
        if (string.isEmpty()) {
            return Optional.empty();
        }
        try {
            return Optional.of(Util.validateUri(string));
        }
        catch (Exception exception) {
            LOGGER.warn("Failed to parse bug link {}", (Object)string, (Object)exception);
            return Optional.empty();
        }
    }

    @Override
    public Map<String, String> getCodeOfConductLanguages() {
        return this.codeOfConductLanguages;
    }

    @Override
    public /* synthetic */ PlayerManager getPlayerManager() {
        return this.getPlayerManager();
    }
}


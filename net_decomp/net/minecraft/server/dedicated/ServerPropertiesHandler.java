/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  com.google.common.base.Splitter
 *  com.google.common.base.Strings
 *  com.google.gson.JsonElement
 *  com.google.gson.JsonObject
 *  com.mojang.logging.LogUtils
 *  com.mojang.serialization.Dynamic
 *  com.mojang.serialization.JsonOps
 *  org.jspecify.annotations.Nullable
 *  org.slf4j.Logger
 */
package net.minecraft.server.dedicated;

import com.google.common.base.Splitter;
import com.google.common.base.Strings;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.mojang.logging.LogUtils;
import com.mojang.serialization.Dynamic;
import com.mojang.serialization.JsonOps;
import java.nio.charset.StandardCharsets;
import java.nio.file.Path;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Optional;
import java.util.Properties;
import java.util.UUID;
import java.util.concurrent.TimeUnit;
import java.util.regex.Pattern;
import net.minecraft.command.permission.LeveledPermissionPredicate;
import net.minecraft.command.permission.PermissionLevel;
import net.minecraft.network.encryption.BearerToken;
import net.minecraft.registry.DynamicRegistryManager;
import net.minecraft.registry.RegistryEntryLookup;
import net.minecraft.registry.RegistryKey;
import net.minecraft.registry.RegistryKeys;
import net.minecraft.registry.RegistryOps;
import net.minecraft.registry.RegistryWrapper;
import net.minecraft.registry.entry.RegistryEntry;
import net.minecraft.resource.DataConfiguration;
import net.minecraft.resource.DataPackSettings;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.dedicated.AbstractPropertiesHandler;
import net.minecraft.text.Text;
import net.minecraft.text.TextCodecs;
import net.minecraft.util.Identifier;
import net.minecraft.util.JsonHelper;
import net.minecraft.util.StrictJsonParser;
import net.minecraft.util.math.MathHelper;
import net.minecraft.world.Difficulty;
import net.minecraft.world.GameMode;
import net.minecraft.world.dimension.DimensionOptionsRegistryHolder;
import net.minecraft.world.gen.GeneratorOptions;
import net.minecraft.world.gen.WorldPreset;
import net.minecraft.world.gen.WorldPresets;
import net.minecraft.world.gen.chunk.FlatChunkGenerator;
import net.minecraft.world.gen.chunk.FlatChunkGeneratorConfig;
import org.jspecify.annotations.Nullable;
import org.slf4j.Logger;

public class ServerPropertiesHandler
extends AbstractPropertiesHandler<ServerPropertiesHandler> {
    static final Logger LOGGER = LogUtils.getLogger();
    private static final Pattern SHA1_PATTERN = Pattern.compile("^[a-fA-F0-9]{40}$");
    private static final Splitter COMMA_SPLITTER = Splitter.on((char)',').trimResults();
    public static final String MANAGEMENT_SERVER_TLS_ENABLED = "management-server-tls-enabled";
    public static final String MANAGEMENT_SERVER_TLS_KEYSTORE = "management-server-tls-keystore";
    public static final String MANAGEMENT_SERVER_TLS_KEYSTORE_PASSWORD = "management-server-tls-keystore-password";
    public final boolean onlineMode = this.parseBoolean("online-mode", true);
    public final boolean preventProxyConnections = this.parseBoolean("prevent-proxy-connections", false);
    public final String serverIp = this.getString("server-ip", "");
    public final AbstractPropertiesHandler.PropertyAccessor<Boolean> allowFlight = this.booleanAccessor("allow-flight", false);
    public final AbstractPropertiesHandler.PropertyAccessor<String> motd = this.stringAccessor("motd", "A Minecraft Server");
    public final boolean enableCodeOfConduct = this.parseBoolean("enable-code-of-conduct", false);
    public final String bugReportLink = this.getString("bug-report-link", "");
    public final AbstractPropertiesHandler.PropertyAccessor<Boolean> forceGameMode = this.booleanAccessor("force-gamemode", false);
    public final AbstractPropertiesHandler.PropertyAccessor<Boolean> enforceWhitelist = this.booleanAccessor("enforce-whitelist", false);
    public final AbstractPropertiesHandler.PropertyAccessor<Difficulty> difficulty = this.accessor("difficulty", ServerPropertiesHandler.combineParser(Difficulty::byId, Difficulty::byName), Difficulty::getName, Difficulty.EASY);
    public final AbstractPropertiesHandler.PropertyAccessor<GameMode> gameMode = this.accessor("gamemode", ServerPropertiesHandler.combineParser(GameMode::byIndex, GameMode::byId), GameMode::getId, GameMode.SURVIVAL);
    public final String levelName = this.getString("level-name", "world");
    public final int serverPort = this.getInt("server-port", 25565);
    public final boolean managementServerEnabled = this.parseBoolean("management-server-enabled", false);
    public final String managementServerHost = this.getString("management-server-host", "localhost");
    public final int managementServerPort = this.getInt("management-server-port", 0);
    public final String managementServerSecret = this.getString("management-server-secret", BearerToken.generate());
    public final boolean managementServerTlsEnabled = this.parseBoolean("management-server-tls-enabled", true);
    public final String managementServerTlsKeystore = this.getString("management-server-tls-keystore", "");
    public final String managementServerKeystorePassword = this.getString("management-server-tls-keystore-password", "");
    public final String managementServerAllowedOrigin = this.getString("management-server-allowed-origins", "");
    public final @Nullable Boolean announcePlayerAchievements = this.getDeprecatedBoolean("announce-player-achievements");
    public final boolean enableQuery = this.parseBoolean("enable-query", false);
    public final int queryPort = this.getInt("query.port", 25565);
    public final boolean enableRcon = this.parseBoolean("enable-rcon", false);
    public final int rconPort = this.getInt("rcon.port", 25575);
    public final String rconPassword = this.getString("rcon.password", "");
    public final boolean hardcore = this.parseBoolean("hardcore", false);
    public final boolean useNativeTransport = this.parseBoolean("use-native-transport", true);
    public final AbstractPropertiesHandler.PropertyAccessor<Integer> spawnProtection = this.intAccessor("spawn-protection", 16);
    public final AbstractPropertiesHandler.PropertyAccessor<LeveledPermissionPredicate> opPermissionLevel = this.accessor("op-permission-level", ServerPropertiesHandler::parsePermissionLevel, ServerPropertiesHandler::permissionLevelToString, LeveledPermissionPredicate.OWNERS);
    public final LeveledPermissionPredicate functionPermissionLevel = this.get("function-permission-level", ServerPropertiesHandler::parsePermissionLevel, ServerPropertiesHandler::permissionLevelToString, LeveledPermissionPredicate.GAMEMASTERS);
    public final long maxTickTime = this.parseLong("max-tick-time", TimeUnit.MINUTES.toMillis(1L));
    public final int maxChainedNeighborUpdates = this.getInt("max-chained-neighbor-updates", 1000000);
    public final int rateLimit = this.getInt("rate-limit", 0);
    public final AbstractPropertiesHandler.PropertyAccessor<Integer> viewDistance = this.intAccessor("view-distance", 10);
    public final AbstractPropertiesHandler.PropertyAccessor<Integer> simulationDistance = this.intAccessor("simulation-distance", 10);
    public final AbstractPropertiesHandler.PropertyAccessor<Integer> maxPlayers = this.intAccessor("max-players", 20);
    public final int networkCompressionThreshold = this.getInt("network-compression-threshold", 256);
    public final boolean broadcastRconToOps = this.parseBoolean("broadcast-rcon-to-ops", true);
    public final boolean broadcastConsoleToOps = this.parseBoolean("broadcast-console-to-ops", true);
    public final int maxWorldSize = this.transformedParseInt("max-world-size", maxWorldSize -> MathHelper.clamp(maxWorldSize, 1, 29999984), 29999984);
    public final boolean syncChunkWrites = this.parseBoolean("sync-chunk-writes", true);
    public final String regionFileCompression = this.getString("region-file-compression", "deflate");
    public final boolean enableJmxMonitoring = this.parseBoolean("enable-jmx-monitoring", false);
    public final AbstractPropertiesHandler.PropertyAccessor<Boolean> enableStatus = this.booleanAccessor("enable-status", true);
    public final AbstractPropertiesHandler.PropertyAccessor<Boolean> hideOnlinePlayers = this.booleanAccessor("hide-online-players", false);
    public final AbstractPropertiesHandler.PropertyAccessor<Integer> entityBroadcastRangePercentage = this.accessor("entity-broadcast-range-percentage", value -> MathHelper.clamp(Integer.parseInt(value), 10, 1000), 100);
    public final String textFilteringConfig = this.getString("text-filtering-config", "");
    public final int textFilteringVersion = this.getInt("text-filtering-version", 0);
    public final Optional<MinecraftServer.ServerResourcePackProperties> serverResourcePackProperties;
    public final DataPackSettings dataPackSettings;
    public final AbstractPropertiesHandler.PropertyAccessor<Integer> playerIdleTimeout = this.intAccessor("player-idle-timeout", 0);
    public final AbstractPropertiesHandler.PropertyAccessor<Integer> statusHeartbeatInterval = this.intAccessor("status-heartbeat-interval", 0);
    public final AbstractPropertiesHandler.PropertyAccessor<Boolean> whiteList = this.booleanAccessor("white-list", false);
    public final boolean enforceSecureProfile = this.parseBoolean("enforce-secure-profile", true);
    public final boolean logIps = this.parseBoolean("log-ips", true);
    public final AbstractPropertiesHandler.PropertyAccessor<Integer> pauseWhenEmptySeconds = this.intAccessor("pause-when-empty-seconds", 60);
    private final WorldGenProperties worldGenProperties;
    public final GeneratorOptions generatorOptions;
    public AbstractPropertiesHandler.PropertyAccessor<Boolean> acceptsTransfers = this.booleanAccessor("accepts-transfers", false);

    public ServerPropertiesHandler(Properties properties) {
        super(properties);
        String string = this.getString("level-seed", "");
        boolean bl = this.parseBoolean("generate-structures", true);
        long l = GeneratorOptions.parseSeed(string).orElse(GeneratorOptions.getRandomSeed());
        this.generatorOptions = new GeneratorOptions(l, bl, false);
        this.worldGenProperties = new WorldGenProperties(this.get("generator-settings", generatorSettings -> JsonHelper.deserialize(!generatorSettings.isEmpty() ? generatorSettings : "{}"), new JsonObject()), this.get("level-type", type -> type.toLowerCase(Locale.ROOT), WorldPresets.DEFAULT.getValue().toString()));
        this.serverResourcePackProperties = ServerPropertiesHandler.getServerResourcePackProperties(this.getString("resource-pack-id", ""), this.getString("resource-pack", ""), this.getString("resource-pack-sha1", ""), this.getDeprecatedString("resource-pack-hash"), this.parseBoolean("require-resource-pack", false), this.getString("resource-pack-prompt", ""));
        this.dataPackSettings = ServerPropertiesHandler.parseDataPackSettings(this.getString("initial-enabled-packs", String.join((CharSequence)",", DataConfiguration.SAFE_MODE.dataPacks().getEnabled())), this.getString("initial-disabled-packs", String.join((CharSequence)",", DataConfiguration.SAFE_MODE.dataPacks().getDisabled())));
    }

    public static ServerPropertiesHandler load(Path path) {
        return new ServerPropertiesHandler(ServerPropertiesHandler.loadProperties(path));
    }

    @Override
    protected ServerPropertiesHandler create(DynamicRegistryManager dynamicRegistryManager, Properties properties) {
        return new ServerPropertiesHandler(properties);
    }

    private static @Nullable Text parseResourcePackPrompt(String prompt) {
        if (!Strings.isNullOrEmpty((String)prompt)) {
            try {
                JsonElement jsonElement = StrictJsonParser.parse(prompt);
                return TextCodecs.CODEC.parse(DynamicRegistryManager.EMPTY.getOps(JsonOps.INSTANCE), (Object)jsonElement).resultOrPartial(error -> LOGGER.warn("Failed to parse resource pack prompt '{}': {}", (Object)prompt, error)).orElse(null);
            }
            catch (Exception exception) {
                LOGGER.warn("Failed to parse resource pack prompt '{}'", (Object)prompt, (Object)exception);
            }
        }
        return null;
    }

    private static Optional<MinecraftServer.ServerResourcePackProperties> getServerResourcePackProperties(String id, String url, String sha1, @Nullable String hash, boolean required, String prompt) {
        UUID uUID;
        String string;
        if (url.isEmpty()) {
            return Optional.empty();
        }
        if (!sha1.isEmpty()) {
            string = sha1;
            if (!Strings.isNullOrEmpty((String)hash)) {
                LOGGER.warn("resource-pack-hash is deprecated and found along side resource-pack-sha1. resource-pack-hash will be ignored.");
            }
        } else if (!Strings.isNullOrEmpty((String)hash)) {
            LOGGER.warn("resource-pack-hash is deprecated. Please use resource-pack-sha1 instead.");
            string = hash;
        } else {
            string = "";
        }
        if (string.isEmpty()) {
            LOGGER.warn("You specified a resource pack without providing a sha1 hash. Pack will be updated on the client only if you change the name of the pack.");
        } else if (!SHA1_PATTERN.matcher(string).matches()) {
            LOGGER.warn("Invalid sha1 for resource-pack-sha1");
        }
        Text text = ServerPropertiesHandler.parseResourcePackPrompt(prompt);
        if (id.isEmpty()) {
            uUID = UUID.nameUUIDFromBytes(url.getBytes(StandardCharsets.UTF_8));
            LOGGER.warn("resource-pack-id missing, using default of {}", (Object)uUID);
        } else {
            try {
                uUID = UUID.fromString(id);
            }
            catch (IllegalArgumentException illegalArgumentException) {
                LOGGER.warn("Failed to parse '{}' into UUID", (Object)id);
                return Optional.empty();
            }
        }
        return Optional.of(new MinecraftServer.ServerResourcePackProperties(uUID, url, string, required, text));
    }

    private static DataPackSettings parseDataPackSettings(String enabled, String disabled) {
        List list = COMMA_SPLITTER.splitToList((CharSequence)enabled);
        List list2 = COMMA_SPLITTER.splitToList((CharSequence)disabled);
        return new DataPackSettings(list, list2);
    }

    public static @Nullable LeveledPermissionPredicate parsePermissionLevel(String value) {
        try {
            PermissionLevel permissionLevel = PermissionLevel.fromLevel(Integer.parseInt(value));
            return LeveledPermissionPredicate.fromLevel(permissionLevel);
        }
        catch (NumberFormatException numberFormatException) {
            return null;
        }
    }

    public static String permissionLevelToString(LeveledPermissionPredicate level) {
        return Integer.toString(level.getLevel().getLevel());
    }

    public DimensionOptionsRegistryHolder createDimensionsRegistryHolder(RegistryWrapper.WrapperLookup registries) {
        return this.worldGenProperties.createDimensionsRegistryHolder(registries);
    }

    @Override
    protected /* synthetic */ AbstractPropertiesHandler create(DynamicRegistryManager registryManager, Properties properties) {
        return this.create(registryManager, properties);
    }

    record WorldGenProperties(JsonObject generatorSettings, String levelType) {
        private static final Map<String, RegistryKey<WorldPreset>> LEVEL_TYPE_TO_PRESET_KEY = Map.of("default", WorldPresets.DEFAULT, "largebiomes", WorldPresets.LARGE_BIOMES);

        public DimensionOptionsRegistryHolder createDimensionsRegistryHolder(RegistryWrapper.WrapperLookup registries) {
            RegistryEntryLookup registryWrapper = registries.getOrThrow(RegistryKeys.WORLD_PRESET);
            RegistryEntry.Reference<WorldPreset> reference = registryWrapper.getOptional(WorldPresets.DEFAULT).or(() -> WorldGenProperties.method_41241((RegistryWrapper)registryWrapper)).orElseThrow(() -> new IllegalStateException("Invalid datapack contents: can't find default preset"));
            RegistryEntry registryEntry = Optional.ofNullable(Identifier.tryParse(this.levelType)).map(levelTypeId -> RegistryKey.of(RegistryKeys.WORLD_PRESET, levelTypeId)).or(() -> Optional.ofNullable(LEVEL_TYPE_TO_PRESET_KEY.get(this.levelType))).flatMap(((RegistryWrapper)registryWrapper)::getOptional).orElseGet(() -> {
                LOGGER.warn("Failed to parse level-type {}, defaulting to {}", (Object)this.levelType, (Object)reference.registryKey().getValue());
                return reference;
            });
            DimensionOptionsRegistryHolder dimensionOptionsRegistryHolder = ((WorldPreset)registryEntry.value()).createDimensionsRegistryHolder();
            if (registryEntry.matchesKey(WorldPresets.FLAT)) {
                RegistryOps registryOps = registries.getOps(JsonOps.INSTANCE);
                Optional optional = FlatChunkGeneratorConfig.CODEC.parse(new Dynamic(registryOps, (Object)this.generatorSettings())).resultOrPartial(arg_0 -> ((Logger)LOGGER).error(arg_0));
                if (optional.isPresent()) {
                    return dimensionOptionsRegistryHolder.with(registries, new FlatChunkGenerator((FlatChunkGeneratorConfig)optional.get()));
                }
            }
            return dimensionOptionsRegistryHolder;
        }

        private static /* synthetic */ Optional method_41241(RegistryWrapper registryWrapper) {
            return registryWrapper.streamEntries().findAny();
        }
    }
}


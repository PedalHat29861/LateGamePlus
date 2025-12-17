/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  com.mojang.authlib.yggdrasil.YggdrasilAuthenticationService
 *  com.mojang.datafixers.DataFixer
 *  com.mojang.logging.LogUtils
 *  com.mojang.serialization.Dynamic
 *  com.mojang.serialization.Lifecycle
 *  joptsimple.AbstractOptionSpec
 *  joptsimple.ArgumentAcceptingOptionSpec
 *  joptsimple.NonOptionArgumentSpec
 *  joptsimple.OptionParser
 *  joptsimple.OptionSet
 *  joptsimple.OptionSpec
 *  joptsimple.OptionSpecBuilder
 *  joptsimple.ValueConverter
 *  joptsimple.util.PathConverter
 *  joptsimple.util.PathProperties
 *  org.jspecify.annotations.Nullable
 *  org.slf4j.Logger
 */
package net.minecraft.server;

import com.mojang.authlib.yggdrasil.YggdrasilAuthenticationService;
import com.mojang.datafixers.DataFixer;
import com.mojang.logging.LogUtils;
import com.mojang.serialization.Dynamic;
import com.mojang.serialization.Lifecycle;
import java.awt.GraphicsEnvironment;
import java.io.File;
import java.io.IOException;
import java.io.OutputStream;
import java.io.UncheckedIOException;
import java.net.Proxy;
import java.nio.file.Files;
import java.nio.file.OpenOption;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Optional;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.Executor;
import java.util.function.BooleanSupplier;
import joptsimple.AbstractOptionSpec;
import joptsimple.ArgumentAcceptingOptionSpec;
import joptsimple.NonOptionArgumentSpec;
import joptsimple.OptionParser;
import joptsimple.OptionSet;
import joptsimple.OptionSpec;
import joptsimple.OptionSpecBuilder;
import joptsimple.ValueConverter;
import joptsimple.util.PathConverter;
import joptsimple.util.PathProperties;
import net.minecraft.Bootstrap;
import net.minecraft.SharedConstants;
import net.minecraft.datafixer.Schemas;
import net.minecraft.nbt.NbtCrashException;
import net.minecraft.nbt.NbtException;
import net.minecraft.obfuscate.DontObfuscate;
import net.minecraft.registry.DynamicRegistryManager;
import net.minecraft.registry.Registry;
import net.minecraft.registry.RegistryKeys;
import net.minecraft.registry.RegistryWrapper;
import net.minecraft.resource.DataConfiguration;
import net.minecraft.resource.ResourcePackManager;
import net.minecraft.resource.VanillaDataPackProvider;
import net.minecraft.resource.featuretoggle.FeatureFlags;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.SaveLoader;
import net.minecraft.server.SaveLoading;
import net.minecraft.server.command.CommandManager;
import net.minecraft.server.dedicated.EulaReader;
import net.minecraft.server.dedicated.MinecraftDedicatedServer;
import net.minecraft.server.dedicated.ServerPropertiesHandler;
import net.minecraft.server.dedicated.ServerPropertiesLoader;
import net.minecraft.text.Text;
import net.minecraft.util.ApiServices;
import net.minecraft.util.Util;
import net.minecraft.util.annotation.SuppressLinter;
import net.minecraft.util.crash.CrashReport;
import net.minecraft.util.logging.UncaughtExceptionLogger;
import net.minecraft.util.math.MathHelper;
import net.minecraft.util.profiling.jfr.FlightProfiler;
import net.minecraft.util.profiling.jfr.InstanceType;
import net.minecraft.world.SaveProperties;
import net.minecraft.world.dimension.DimensionOptions;
import net.minecraft.world.dimension.DimensionOptionsRegistryHolder;
import net.minecraft.world.gen.GeneratorOptions;
import net.minecraft.world.gen.WorldPresets;
import net.minecraft.world.level.LevelInfo;
import net.minecraft.world.level.LevelProperties;
import net.minecraft.world.level.storage.LevelStorage;
import net.minecraft.world.level.storage.LevelSummary;
import net.minecraft.world.level.storage.ParsedSaveProperties;
import net.minecraft.world.rule.GameRules;
import net.minecraft.world.storage.ChunkCompressionFormat;
import net.minecraft.world.updater.WorldUpdater;
import org.jspecify.annotations.Nullable;
import org.slf4j.Logger;

public class Main {
    private static final Logger LOGGER = LogUtils.getLogger();

    @SuppressLinter(reason="System.out needed before bootstrap")
    @DontObfuscate
    public static void main(String[] args) {
        SharedConstants.createGameVersion();
        OptionParser optionParser = new OptionParser();
        OptionSpecBuilder optionSpec = optionParser.accepts("nogui");
        OptionSpecBuilder optionSpec2 = optionParser.accepts("initSettings", "Initializes 'server.properties' and 'eula.txt', then quits");
        OptionSpecBuilder optionSpec3 = optionParser.accepts("demo");
        OptionSpecBuilder optionSpec4 = optionParser.accepts("bonusChest");
        OptionSpecBuilder optionSpec5 = optionParser.accepts("forceUpgrade");
        OptionSpecBuilder optionSpec6 = optionParser.accepts("eraseCache");
        OptionSpecBuilder optionSpec7 = optionParser.accepts("recreateRegionFiles");
        OptionSpecBuilder optionSpec8 = optionParser.accepts("safeMode", "Loads level with vanilla datapack only");
        AbstractOptionSpec optionSpec9 = optionParser.accepts("help").forHelp();
        ArgumentAcceptingOptionSpec optionSpec10 = optionParser.accepts("universe").withRequiredArg().defaultsTo((Object)".", (Object[])new String[0]);
        ArgumentAcceptingOptionSpec optionSpec11 = optionParser.accepts("world").withRequiredArg();
        ArgumentAcceptingOptionSpec optionSpec12 = optionParser.accepts("port").withRequiredArg().ofType(Integer.class).defaultsTo((Object)-1, (Object[])new Integer[0]);
        ArgumentAcceptingOptionSpec optionSpec13 = optionParser.accepts("serverId").withRequiredArg();
        OptionSpecBuilder optionSpec14 = optionParser.accepts("jfrProfile");
        ArgumentAcceptingOptionSpec optionSpec15 = optionParser.accepts("pidFile").withRequiredArg().withValuesConvertedBy((ValueConverter)new PathConverter(new PathProperties[0]));
        NonOptionArgumentSpec optionSpec16 = optionParser.nonOptions();
        try {
            SaveLoader saveLoader;
            Dynamic<?> dynamic;
            OptionSet optionSet = optionParser.parse(args);
            if (optionSet.has((OptionSpec)optionSpec9)) {
                optionParser.printHelpOn((OutputStream)System.err);
                return;
            }
            Path path = (Path)optionSet.valueOf((OptionSpec)optionSpec15);
            if (path != null) {
                Main.writePidFile(path);
            }
            CrashReport.initCrashReport();
            if (optionSet.has((OptionSpec)optionSpec14)) {
                FlightProfiler.INSTANCE.start(InstanceType.SERVER);
            }
            Bootstrap.initialize();
            Bootstrap.logMissing();
            Util.startTimerHack();
            Path path2 = Paths.get("server.properties", new String[0]);
            ServerPropertiesLoader serverPropertiesLoader = new ServerPropertiesLoader(path2);
            serverPropertiesLoader.store();
            ChunkCompressionFormat.setCurrentFormat(serverPropertiesLoader.getPropertiesHandler().regionFileCompression);
            Path path3 = Paths.get("eula.txt", new String[0]);
            EulaReader eulaReader = new EulaReader(path3);
            if (optionSet.has((OptionSpec)optionSpec2)) {
                LOGGER.info("Initialized '{}' and '{}'", (Object)path2.toAbsolutePath(), (Object)path3.toAbsolutePath());
                return;
            }
            if (!eulaReader.isEulaAgreedTo()) {
                LOGGER.info("You need to agree to the EULA in order to run the server. Go to eula.txt for more info.");
                return;
            }
            File file = new File((String)optionSet.valueOf((OptionSpec)optionSpec10));
            ApiServices apiServices = ApiServices.create(new YggdrasilAuthenticationService(Proxy.NO_PROXY), file);
            String string = Optional.ofNullable((String)optionSet.valueOf((OptionSpec)optionSpec11)).orElse(serverPropertiesLoader.getPropertiesHandler().levelName);
            LevelStorage levelStorage = LevelStorage.create(file.toPath());
            LevelStorage.Session session = levelStorage.createSession(string);
            if (session.levelDatExists()) {
                LevelSummary levelSummary;
                try {
                    dynamic = session.readLevelProperties();
                    levelSummary = session.getLevelSummary(dynamic);
                }
                catch (IOException | NbtCrashException | NbtException exception) {
                    LevelStorage.LevelSave levelSave = session.getDirectory();
                    LOGGER.warn("Failed to load world data from {}", (Object)levelSave.getLevelDatPath(), (Object)exception);
                    LOGGER.info("Attempting to use fallback");
                    try {
                        dynamic = session.readOldLevelProperties();
                        levelSummary = session.getLevelSummary(dynamic);
                    }
                    catch (IOException | NbtCrashException | NbtException exception2) {
                        LOGGER.error("Failed to load world data from {}", (Object)levelSave.getLevelDatOldPath(), (Object)exception2);
                        LOGGER.error("Failed to load world data from {} and {}. World files may be corrupted. Shutting down.", (Object)levelSave.getLevelDatPath(), (Object)levelSave.getLevelDatOldPath());
                        return;
                    }
                    session.tryRestoreBackup();
                }
                if (levelSummary.requiresConversion()) {
                    LOGGER.info("This world must be opened in an older version (like 1.6.4) to be safely converted");
                    return;
                }
                if (!levelSummary.isVersionAvailable()) {
                    LOGGER.info("This world was created by an incompatible version.");
                    return;
                }
            } else {
                dynamic = null;
            }
            Dynamic<?> dynamic2 = dynamic;
            boolean bl = optionSet.has((OptionSpec)optionSpec8);
            if (bl) {
                LOGGER.warn("Safe mode active, only vanilla datapack will be loaded");
            }
            ResourcePackManager resourcePackManager = VanillaDataPackProvider.createManager(session);
            try {
                SaveLoading.ServerConfig serverConfig = Main.createServerConfig(serverPropertiesLoader.getPropertiesHandler(), dynamic2, bl, resourcePackManager);
                saveLoader = (SaveLoader)Util.waitAndApply(arg_0 -> Main.method_43612(serverConfig, dynamic2, serverPropertiesLoader, optionSet, (OptionSpec)optionSpec3, (OptionSpec)optionSpec4, arg_0)).get();
            }
            catch (Exception exception3) {
                LOGGER.warn("Failed to load datapacks, can't proceed with server load. You can either fix your datapacks or reset to vanilla with --safeMode", (Throwable)exception3);
                return;
            }
            DynamicRegistryManager.Immutable immutable = saveLoader.combinedDynamicRegistries().getCombinedRegistryManager();
            SaveProperties saveProperties = saveLoader.saveProperties();
            boolean bl2 = optionSet.has((OptionSpec)optionSpec7);
            if (optionSet.has((OptionSpec)optionSpec5) || bl2) {
                Main.forceUpgradeWorld(session, saveProperties, Schemas.getFixer(), optionSet.has((OptionSpec)optionSpec6), () -> true, immutable, bl2);
            }
            session.backupLevelDataFile(immutable, saveProperties);
            final MinecraftDedicatedServer minecraftDedicatedServer = MinecraftServer.startServer(arg_0 -> Main.method_29734(session, resourcePackManager, saveLoader, serverPropertiesLoader, apiServices, optionSet, (OptionSpec)optionSpec12, (OptionSpec)optionSpec3, (OptionSpec)optionSpec13, (OptionSpec)optionSpec, (OptionSpec)optionSpec16, arg_0));
            Thread thread = new Thread("Server Shutdown Thread"){

                @Override
                public void run() {
                    minecraftDedicatedServer.stop(true);
                }
            };
            thread.setUncaughtExceptionHandler(new UncaughtExceptionLogger(LOGGER));
            Runtime.getRuntime().addShutdownHook(thread);
        }
        catch (Throwable throwable) {
            LOGGER.error(LogUtils.FATAL_MARKER, "Failed to start the minecraft server", throwable);
        }
    }

    private static SaveLoading.LoadContext<SaveProperties> createWorld(ServerPropertiesLoader serverPropertiesLoader, SaveLoading.LoadContextSupplierContext loadContextSupplierContext, Registry<DimensionOptions> registry, boolean bl, boolean bl2) {
        DimensionOptionsRegistryHolder dimensionOptionsRegistryHolder;
        GeneratorOptions generatorOptions;
        LevelInfo levelInfo;
        if (bl) {
            levelInfo = MinecraftServer.DEMO_LEVEL_INFO;
            generatorOptions = GeneratorOptions.DEMO_OPTIONS;
            dimensionOptionsRegistryHolder = WorldPresets.createDemoOptions(loadContextSupplierContext.worldGenRegistryManager());
        } else {
            ServerPropertiesHandler serverPropertiesHandler = serverPropertiesLoader.getPropertiesHandler();
            levelInfo = new LevelInfo(serverPropertiesHandler.levelName, serverPropertiesHandler.gameMode.get(), serverPropertiesHandler.hardcore, serverPropertiesHandler.difficulty.get(), false, new GameRules(loadContextSupplierContext.dataConfiguration().enabledFeatures()), loadContextSupplierContext.dataConfiguration());
            generatorOptions = bl2 ? serverPropertiesHandler.generatorOptions.withBonusChest(true) : serverPropertiesHandler.generatorOptions;
            dimensionOptionsRegistryHolder = serverPropertiesHandler.createDimensionsRegistryHolder(loadContextSupplierContext.worldGenRegistryManager());
        }
        DimensionOptionsRegistryHolder.DimensionsConfig dimensionsConfig = dimensionOptionsRegistryHolder.toConfig(registry);
        Lifecycle lifecycle = dimensionsConfig.getLifecycle().add(loadContextSupplierContext.worldGenRegistryManager().getLifecycle());
        return new SaveLoading.LoadContext<SaveProperties>(new LevelProperties(levelInfo, generatorOptions, dimensionsConfig.specialWorldProperty(), lifecycle), dimensionsConfig.toDynamicRegistryManager());
    }

    private static void writePidFile(Path path) {
        try {
            long l = ProcessHandle.current().pid();
            Files.writeString(path, (CharSequence)Long.toString(l), new OpenOption[0]);
        }
        catch (IOException iOException) {
            throw new UncheckedIOException(iOException);
        }
    }

    private static SaveLoading.ServerConfig createServerConfig(ServerPropertiesHandler serverPropertiesHandler, @Nullable Dynamic<?> dynamic, boolean safeMode, ResourcePackManager dataPackManager) {
        DataConfiguration dataConfiguration2;
        boolean bl;
        if (dynamic != null) {
            DataConfiguration dataConfiguration = LevelStorage.parseDataPackSettings(dynamic);
            bl = false;
            dataConfiguration2 = dataConfiguration;
        } else {
            bl = true;
            dataConfiguration2 = new DataConfiguration(serverPropertiesHandler.dataPackSettings, FeatureFlags.DEFAULT_ENABLED_FEATURES);
        }
        SaveLoading.DataPacks dataPacks = new SaveLoading.DataPacks(dataPackManager, dataConfiguration2, safeMode, bl);
        return new SaveLoading.ServerConfig(dataPacks, CommandManager.RegistrationEnvironment.DEDICATED, serverPropertiesHandler.functionPermissionLevel);
    }

    private static void forceUpgradeWorld(LevelStorage.Session session, SaveProperties saveProperties, DataFixer dataFixer, boolean eraseCache, BooleanSupplier continueCheck, DynamicRegistryManager registries, boolean recreateRegionFiles) {
        LOGGER.info("Forcing world upgrade!");
        try (WorldUpdater worldUpdater = new WorldUpdater(session, dataFixer, saveProperties, registries, eraseCache, recreateRegionFiles);){
            Text text = null;
            while (!worldUpdater.isDone()) {
                int i;
                Text text2 = worldUpdater.getStatus();
                if (text != text2) {
                    text = text2;
                    LOGGER.info(worldUpdater.getStatus().getString());
                }
                if ((i = worldUpdater.getTotalChunkCount()) > 0) {
                    int j = worldUpdater.getUpgradedChunkCount() + worldUpdater.getSkippedChunkCount();
                    LOGGER.info("{}% completed ({} / {} chunks)...", new Object[]{MathHelper.floor((float)j / (float)i * 100.0f), j, i});
                }
                if (!continueCheck.getAsBoolean()) {
                    worldUpdater.cancel();
                    continue;
                }
                try {
                    Thread.sleep(1000L);
                }
                catch (InterruptedException interruptedException) {}
            }
        }
    }

    private static /* synthetic */ MinecraftDedicatedServer method_29734(LevelStorage.Session session, ResourcePackManager resourcePackManager, SaveLoader saveLoader, ServerPropertiesLoader serverPropertiesLoader, ApiServices apiServices, OptionSet optionSet, OptionSpec optionSpec, OptionSpec optionSpec2, OptionSpec optionSpec3, OptionSpec optionSpec4, OptionSpec optionSpec5, Thread thread) {
        boolean bl;
        MinecraftDedicatedServer minecraftDedicatedServer = new MinecraftDedicatedServer(thread, session, resourcePackManager, saveLoader, serverPropertiesLoader, Schemas.getFixer(), apiServices);
        minecraftDedicatedServer.setServerPort((Integer)optionSet.valueOf(optionSpec));
        minecraftDedicatedServer.setDemo(optionSet.has(optionSpec2));
        minecraftDedicatedServer.setServerId((String)optionSet.valueOf(optionSpec3));
        boolean bl2 = bl = !optionSet.has(optionSpec4) && !optionSet.valuesOf(optionSpec5).contains("nogui");
        if (bl && !GraphicsEnvironment.isHeadless()) {
            minecraftDedicatedServer.createGui();
        }
        return minecraftDedicatedServer;
    }

    private static /* synthetic */ CompletableFuture method_43612(SaveLoading.ServerConfig serverConfig, Dynamic dynamic, ServerPropertiesLoader serverPropertiesLoader, OptionSet optionSet, OptionSpec optionSpec, OptionSpec optionSpec2, Executor applyExecutor) {
        return SaveLoading.load(serverConfig, loadContextSupplierContext -> {
            RegistryWrapper.Impl registry = loadContextSupplierContext.dimensionsRegistryManager().getOrThrow(RegistryKeys.DIMENSION);
            if (dynamic != null) {
                ParsedSaveProperties parsedSaveProperties = LevelStorage.parseSaveProperties(dynamic, loadContextSupplierContext.dataConfiguration(), (Registry<DimensionOptions>)registry, loadContextSupplierContext.worldGenRegistryManager());
                return new SaveLoading.LoadContext<SaveProperties>(parsedSaveProperties.properties(), parsedSaveProperties.dimensions().toDynamicRegistryManager());
            }
            LOGGER.info("No existing world data, creating new world");
            return Main.createWorld(serverPropertiesLoader, loadContextSupplierContext, (Registry<DimensionOptions>)registry, optionSet.has(optionSpec), optionSet.has(optionSpec2));
        }, SaveLoader::new, Util.getMainWorkerExecutor(), applyExecutor);
    }
}


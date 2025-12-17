/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  com.google.common.base.Stopwatch
 *  com.mojang.authlib.GameProfile
 *  com.mojang.authlib.yggdrasil.ServicesKeySet
 *  com.mojang.brigadier.StringReader
 *  com.mojang.logging.LogUtils
 *  com.mojang.serialization.Lifecycle
 *  org.jspecify.annotations.Nullable
 *  org.slf4j.Logger
 */
package net.minecraft.test;

import com.google.common.base.Stopwatch;
import com.mojang.authlib.GameProfile;
import com.mojang.authlib.yggdrasil.ServicesKeySet;
import com.mojang.brigadier.StringReader;
import com.mojang.logging.LogUtils;
import com.mojang.serialization.Lifecycle;
import java.net.Proxy;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Optional;
import java.util.Set;
import java.util.UUID;
import java.util.concurrent.TimeUnit;
import java.util.function.BooleanSupplier;
import java.util.stream.Stream;
import net.minecraft.command.argument.RegistrySelectorArgumentType;
import net.minecraft.command.permission.LeveledPermissionPredicate;
import net.minecraft.command.permission.PermissionPredicate;
import net.minecraft.datafixer.Schemas;
import net.minecraft.registry.DynamicRegistryManager;
import net.minecraft.registry.Registry;
import net.minecraft.registry.RegistryKeys;
import net.minecraft.registry.RegistryWrapper;
import net.minecraft.registry.SimpleRegistry;
import net.minecraft.registry.entry.RegistryEntry;
import net.minecraft.resource.DataConfiguration;
import net.minecraft.resource.DataPackSettings;
import net.minecraft.resource.ResourcePackManager;
import net.minecraft.resource.featuretoggle.FeatureFlags;
import net.minecraft.resource.featuretoggle.FeatureSet;
import net.minecraft.server.GameProfileResolver;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.PlayerConfigEntry;
import net.minecraft.server.PlayerManager;
import net.minecraft.server.SaveLoader;
import net.minecraft.server.SaveLoading;
import net.minecraft.server.command.CommandManager;
import net.minecraft.server.dedicated.management.listener.BlankManagementListener;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.test.Batches;
import net.minecraft.test.GameTestBatch;
import net.minecraft.test.GameTestState;
import net.minecraft.test.TestAttemptConfig;
import net.minecraft.test.TestFailureLogger;
import net.minecraft.test.TestInstance;
import net.minecraft.test.TestRunContext;
import net.minecraft.test.TestSet;
import net.minecraft.test.TestStructurePlacer;
import net.minecraft.util.ApiServices;
import net.minecraft.util.BlockRotation;
import net.minecraft.util.NameToIdCache;
import net.minecraft.util.SystemDetails;
import net.minecraft.util.Util;
import net.minecraft.util.crash.CrashReport;
import net.minecraft.util.crash.ReportType;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.profiler.MultiValueDebugSampleLogImpl;
import net.minecraft.util.profiler.log.DebugSampleLog;
import net.minecraft.world.Difficulty;
import net.minecraft.world.GameMode;
import net.minecraft.world.WorldProperties;
import net.minecraft.world.chunk.LoggingChunkLoadProgress;
import net.minecraft.world.debug.gizmo.GizmoCollector;
import net.minecraft.world.debug.gizmo.GizmoDrawing;
import net.minecraft.world.dimension.DimensionOptions;
import net.minecraft.world.dimension.DimensionOptionsRegistryHolder;
import net.minecraft.world.gen.GeneratorOptions;
import net.minecraft.world.gen.WorldPresets;
import net.minecraft.world.level.LevelInfo;
import net.minecraft.world.level.LevelProperties;
import net.minecraft.world.level.storage.LevelStorage;
import net.minecraft.world.rule.GameRules;
import org.jspecify.annotations.Nullable;
import org.slf4j.Logger;

public class TestServer
extends MinecraftServer {
    private static final Logger LOGGER = LogUtils.getLogger();
    private static final int RESULT_STRING_LOG_INTERVAL = 20;
    private static final int TEST_POS_XZ_RANGE = 14999992;
    private static final ApiServices NONE_API_SERVICES = new ApiServices(null, ServicesKeySet.EMPTY, null, new DummyNameToIdCache(), new DummyGameProfileResolver());
    private static final FeatureSet ENABLED_FEATURES = FeatureFlags.FEATURE_MANAGER.getFeatureSet().subtract(FeatureSet.of(FeatureFlags.REDSTONE_EXPERIMENTS, FeatureFlags.MINECART_IMPROVEMENTS));
    private final MultiValueDebugSampleLogImpl debugSampleLog = new MultiValueDebugSampleLogImpl(4);
    private final Optional<String> tests;
    private final boolean verify;
    private List<GameTestBatch> batches = new ArrayList<GameTestBatch>();
    private final Stopwatch stopwatch = Stopwatch.createUnstarted();
    private static final GeneratorOptions TEST_LEVEL = new GeneratorOptions(0L, false, false);
    private @Nullable TestSet testSet;

    public static TestServer create(Thread thread, LevelStorage.Session session, ResourcePackManager resourcePackManager, Optional<String> tests, boolean verify) {
        resourcePackManager.scanPacks();
        ArrayList<String> arrayList = new ArrayList<String>(resourcePackManager.getIds());
        arrayList.remove("vanilla");
        arrayList.addFirst("vanilla");
        DataConfiguration dataConfiguration = new DataConfiguration(new DataPackSettings(arrayList, List.of()), ENABLED_FEATURES);
        LevelInfo levelInfo = new LevelInfo("Test Level", GameMode.CREATIVE, false, Difficulty.NORMAL, true, new GameRules(ENABLED_FEATURES), dataConfiguration);
        SaveLoading.DataPacks dataPacks = new SaveLoading.DataPacks(resourcePackManager, dataConfiguration, false, true);
        SaveLoading.ServerConfig serverConfig = new SaveLoading.ServerConfig(dataPacks, CommandManager.RegistrationEnvironment.DEDICATED, LeveledPermissionPredicate.OWNERS);
        try {
            LOGGER.debug("Starting resource loading");
            Stopwatch stopwatch = Stopwatch.createStarted();
            SaveLoader saveLoader = (SaveLoader)Util.waitAndApply(executor -> SaveLoading.load(serverConfig, context -> {
                Registry<DimensionOptions> registry = new SimpleRegistry<DimensionOptions>(RegistryKeys.DIMENSION, Lifecycle.stable()).freeze();
                DimensionOptionsRegistryHolder.DimensionsConfig dimensionsConfig = context.worldGenRegistryManager().getOrThrow(RegistryKeys.WORLD_PRESET).getOrThrow(WorldPresets.FLAT).value().createDimensionsRegistryHolder().toConfig(registry);
                return new SaveLoading.LoadContext<LevelProperties>(new LevelProperties(levelInfo, TEST_LEVEL, dimensionsConfig.specialWorldProperty(), dimensionsConfig.getLifecycle()), dimensionsConfig.toDynamicRegistryManager());
            }, SaveLoader::new, Util.getMainWorkerExecutor(), executor)).get();
            stopwatch.stop();
            LOGGER.debug("Finished resource loading after {} ms", (Object)stopwatch.elapsed(TimeUnit.MILLISECONDS));
            return new TestServer(thread, session, resourcePackManager, saveLoader, tests, verify);
        }
        catch (Exception exception) {
            LOGGER.warn("Failed to load vanilla datapack, bit oops", (Throwable)exception);
            System.exit(-1);
            throw new IllegalStateException();
        }
    }

    private TestServer(Thread serverThread, LevelStorage.Session session, ResourcePackManager dataPackManager, SaveLoader saveLoader, Optional<String> tests, boolean verify) {
        super(serverThread, session, dataPackManager, saveLoader, Proxy.NO_PROXY, Schemas.getFixer(), NONE_API_SERVICES, LoggingChunkLoadProgress.withoutPlayer());
        this.tests = tests;
        this.verify = verify;
    }

    @Override
    public boolean setupServer() {
        this.setPlayerManager(new PlayerManager(this, this, this.getCombinedDynamicRegistries(), this.saveHandler, new BlankManagementListener()){});
        GizmoDrawing.using(GizmoCollector.EMPTY);
        this.loadWorld();
        ServerWorld serverWorld = this.getOverworld();
        this.batches = this.batch(serverWorld);
        LOGGER.info("Started game test server");
        return true;
    }

    private List<GameTestBatch> batch(ServerWorld world) {
        Batches.Decorator decorator;
        List<RegistryEntry.Reference<TestInstance>> collection;
        RegistryWrapper.Impl registry = world.getRegistryManager().getOrThrow(RegistryKeys.TEST_INSTANCE);
        if (this.tests.isPresent()) {
            collection = TestServer.selectInstances(world.getRegistryManager(), this.tests.get()).filter(instance -> !((TestInstance)instance.value()).isManualOnly()).toList();
            if (this.verify) {
                decorator = TestServer::makeVerificationBatches;
                LOGGER.info("Verify requested. Will run each test that matches {} {} times", (Object)this.tests.get(), (Object)(100 * BlockRotation.values().length));
            } else {
                decorator = Batches.DEFAULT_DECORATOR;
                LOGGER.info("Will run tests matching {} ({} tests)", (Object)this.tests.get(), (Object)collection.size());
            }
        } else {
            collection = registry.streamEntries().filter(instance -> !((TestInstance)instance.value()).isManualOnly()).toList();
            decorator = Batches.DEFAULT_DECORATOR;
        }
        return Batches.batch(collection, decorator, world);
    }

    private static Stream<GameTestState> makeVerificationBatches(RegistryEntry.Reference<TestInstance> instance, ServerWorld world) {
        Stream.Builder<GameTestState> builder = Stream.builder();
        for (BlockRotation blockRotation : BlockRotation.values()) {
            for (int i = 0; i < 100; ++i) {
                builder.add(new GameTestState(instance, blockRotation, world, TestAttemptConfig.once()));
            }
        }
        return builder.build();
    }

    public static Stream<RegistryEntry.Reference<TestInstance>> selectInstances(DynamicRegistryManager registryManager, String selector) {
        return RegistrySelectorArgumentType.select(new StringReader(selector), registryManager.getOrThrow(RegistryKeys.TEST_INSTANCE)).stream();
    }

    @Override
    public void tick(BooleanSupplier shouldKeepTicking) {
        super.tick(shouldKeepTicking);
        ServerWorld serverWorld = this.getOverworld();
        if (!this.isTesting()) {
            this.runTestBatches(serverWorld);
        }
        if (serverWorld.getTime() % 20L == 0L) {
            LOGGER.info(this.testSet.getResultString());
        }
        if (this.testSet.isDone()) {
            this.stop(false);
            LOGGER.info(this.testSet.getResultString());
            TestFailureLogger.stop();
            LOGGER.info("========= {} GAME TESTS COMPLETE IN {} ======================", (Object)this.testSet.getTestCount(), (Object)this.stopwatch.stop());
            if (this.testSet.failed()) {
                LOGGER.info("{} required tests failed :(", (Object)this.testSet.getFailedRequiredTestCount());
                this.testSet.getRequiredTests().forEach(TestServer::logFailure);
            } else {
                LOGGER.info("All {} required tests passed :)", (Object)this.testSet.getTestCount());
            }
            if (this.testSet.hasFailedOptionalTests()) {
                LOGGER.info("{} optional tests failed", (Object)this.testSet.getFailedOptionalTestCount());
                this.testSet.getOptionalTests().forEach(TestServer::logFailure);
            }
            LOGGER.info("====================================================");
        }
    }

    private static void logFailure(GameTestState state) {
        if (state.getRotation() != BlockRotation.NONE) {
            LOGGER.info("   - {} with rotation {}: {}", new Object[]{state.getId(), state.getRotation().asString(), state.getThrowable().getText().getString()});
        } else {
            LOGGER.info("   - {}: {}", (Object)state.getId(), (Object)state.getThrowable().getText().getString());
        }
    }

    @Override
    public DebugSampleLog getDebugSampleLog() {
        return this.debugSampleLog;
    }

    @Override
    public boolean shouldPushTickTimeLog() {
        return false;
    }

    @Override
    public void runTasksTillTickEnd() {
        this.runTasks();
    }

    @Override
    public SystemDetails addExtraSystemDetails(SystemDetails details) {
        details.addSection("Type", "Game test server");
        return details;
    }

    @Override
    public void exit() {
        super.exit();
        LOGGER.info("Game test server shutting down");
        System.exit(this.testSet != null ? this.testSet.getFailedRequiredTestCount() : -1);
    }

    @Override
    public void setCrashReport(CrashReport report) {
        super.setCrashReport(report);
        LOGGER.error("Game test server crashed\n{}", (Object)report.asString(ReportType.MINECRAFT_CRASH_REPORT));
        System.exit(1);
    }

    private void runTestBatches(ServerWorld world) {
        BlockPos blockPos = new BlockPos(world.random.nextBetween(-14999992, 14999992), -59, world.random.nextBetween(-14999992, 14999992));
        world.setSpawnPoint(WorldProperties.SpawnPoint.create(world.getRegistryKey(), blockPos, 0.0f, 0.0f));
        TestRunContext testRunContext = TestRunContext.Builder.of(this.batches, world).initialSpawner(new TestStructurePlacer(blockPos, 8, false)).build();
        List<GameTestState> collection = testRunContext.getStates();
        this.testSet = new TestSet(collection);
        LOGGER.info("{} tests are now running at position {}!", (Object)this.testSet.getTestCount(), (Object)blockPos.toShortString());
        this.stopwatch.reset();
        this.stopwatch.start();
        testRunContext.start();
    }

    private boolean isTesting() {
        return this.testSet != null;
    }

    @Override
    public boolean isHardcore() {
        return false;
    }

    @Override
    public LeveledPermissionPredicate getOpPermissionLevel() {
        return LeveledPermissionPredicate.ALL;
    }

    @Override
    public PermissionPredicate getFunctionPermissions() {
        return LeveledPermissionPredicate.OWNERS;
    }

    @Override
    public boolean shouldBroadcastRconToOps() {
        return false;
    }

    @Override
    public boolean isDedicated() {
        return false;
    }

    @Override
    public int getRateLimit() {
        return 0;
    }

    @Override
    public boolean isUsingNativeTransport() {
        return false;
    }

    @Override
    public boolean isRemote() {
        return false;
    }

    @Override
    public boolean shouldBroadcastConsoleToOps() {
        return false;
    }

    @Override
    public boolean isHost(PlayerConfigEntry player) {
        return false;
    }

    @Override
    public int getMaxPlayerCount() {
        return 1;
    }

    static class DummyNameToIdCache
    implements NameToIdCache {
        private final Set<PlayerConfigEntry> players = new HashSet<PlayerConfigEntry>();

        DummyNameToIdCache() {
        }

        @Override
        public void add(PlayerConfigEntry player) {
            this.players.add(player);
        }

        @Override
        public Optional<PlayerConfigEntry> findByName(String name) {
            return this.players.stream().filter(player -> player.name().equals(name)).findFirst().or(() -> Optional.of(PlayerConfigEntry.fromNickname(name)));
        }

        @Override
        public Optional<PlayerConfigEntry> getByUuid(UUID uuid) {
            return this.players.stream().filter(player -> player.id().equals(uuid)).findFirst();
        }

        @Override
        public void setOfflineMode(boolean offlineMode) {
        }

        @Override
        public void save() {
        }
    }

    static class DummyGameProfileResolver
    implements GameProfileResolver {
        DummyGameProfileResolver() {
        }

        @Override
        public Optional<GameProfile> getProfileByName(String name) {
            return Optional.empty();
        }

        @Override
        public Optional<GameProfile> getProfileById(UUID id) {
            return Optional.empty();
        }
    }
}


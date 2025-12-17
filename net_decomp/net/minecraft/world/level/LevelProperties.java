/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  com.google.common.collect.ImmutableSet
 *  com.google.common.collect.Sets
 *  com.mojang.logging.LogUtils
 *  com.mojang.serialization.Dynamic
 *  com.mojang.serialization.Lifecycle
 *  org.jspecify.annotations.Nullable
 *  org.slf4j.Logger
 */
package net.minecraft.world.level;

import com.google.common.collect.ImmutableSet;
import com.google.common.collect.Sets;
import com.mojang.logging.LogUtils;
import com.mojang.serialization.Dynamic;
import com.mojang.serialization.Lifecycle;
import java.util.HashSet;
import java.util.Optional;
import java.util.Set;
import java.util.UUID;
import java.util.stream.Collectors;
import net.minecraft.SharedConstants;
import net.minecraft.entity.boss.dragon.EnderDragonFight;
import net.minecraft.nbt.NbtCompound;
import net.minecraft.nbt.NbtElement;
import net.minecraft.nbt.NbtHelper;
import net.minecraft.nbt.NbtList;
import net.minecraft.nbt.NbtOps;
import net.minecraft.nbt.NbtString;
import net.minecraft.registry.DynamicRegistryManager;
import net.minecraft.registry.RegistryOps;
import net.minecraft.resource.DataConfiguration;
import net.minecraft.server.MinecraftServer;
import net.minecraft.util.Util;
import net.minecraft.util.Uuids;
import net.minecraft.util.crash.CrashReportSection;
import net.minecraft.world.Difficulty;
import net.minecraft.world.GameMode;
import net.minecraft.world.HeightLimitView;
import net.minecraft.world.SaveProperties;
import net.minecraft.world.WorldProperties;
import net.minecraft.world.border.WorldBorder;
import net.minecraft.world.gen.GeneratorOptions;
import net.minecraft.world.level.LevelInfo;
import net.minecraft.world.level.ServerWorldProperties;
import net.minecraft.world.level.WorldGenSettings;
import net.minecraft.world.level.storage.SaveVersionInfo;
import net.minecraft.world.rule.GameRules;
import net.minecraft.world.timer.Timer;
import net.minecraft.world.timer.TimerCallbackSerializer;
import org.jspecify.annotations.Nullable;
import org.slf4j.Logger;

public class LevelProperties
implements ServerWorldProperties,
SaveProperties {
    private static final Logger LOGGER = LogUtils.getLogger();
    public static final String LEVEL_NAME_KEY = "LevelName";
    protected static final String PLAYER_KEY = "Player";
    protected static final String WORLD_GEN_SETTINGS_KEY = "WorldGenSettings";
    private LevelInfo levelInfo;
    private final GeneratorOptions generatorOptions;
    private final SpecialProperty specialProperty;
    private final Lifecycle lifecycle;
    private WorldProperties.SpawnPoint spawnPoint;
    private long time;
    private long timeOfDay;
    private final @Nullable NbtCompound playerData;
    private final int version;
    private int clearWeatherTime;
    private boolean raining;
    private int rainTime;
    private boolean thundering;
    private int thunderTime;
    private boolean initialized;
    private boolean difficultyLocked;
    @Deprecated
    private Optional<WorldBorder.Properties> worldBorder;
    private EnderDragonFight.Data dragonFight;
    private @Nullable NbtCompound customBossEvents;
    private int wanderingTraderSpawnDelay;
    private int wanderingTraderSpawnChance;
    private @Nullable UUID wanderingTraderId;
    private final Set<String> serverBrands;
    private boolean modded;
    private final Set<String> removedFeatures;
    private final Timer<MinecraftServer> scheduledEvents;

    private LevelProperties(@Nullable NbtCompound playerData, boolean modded, WorldProperties.SpawnPoint spawnPoint, long time, long timeOfDay, int version, int clearWeatherTime, int rainTime, boolean raining, int thunderTime, boolean thundering, boolean initialized, boolean difficultyLocked, Optional<WorldBorder.Properties> worldBorder, int wanderingTraderSpawnDelay, int wanderingTraderSpawnChance, @Nullable UUID wanderingTraderId, Set<String> serverBrands, Set<String> removedFeatures, Timer<MinecraftServer> scheduledEvents, @Nullable NbtCompound customBossEvents, EnderDragonFight.Data dragonFight, LevelInfo levelInfo, GeneratorOptions generatorOptions, SpecialProperty specialProperty, Lifecycle lifecycle) {
        this.modded = modded;
        this.spawnPoint = spawnPoint;
        this.time = time;
        this.timeOfDay = timeOfDay;
        this.version = version;
        this.clearWeatherTime = clearWeatherTime;
        this.rainTime = rainTime;
        this.raining = raining;
        this.thunderTime = thunderTime;
        this.thundering = thundering;
        this.initialized = initialized;
        this.difficultyLocked = difficultyLocked;
        this.worldBorder = worldBorder;
        this.wanderingTraderSpawnDelay = wanderingTraderSpawnDelay;
        this.wanderingTraderSpawnChance = wanderingTraderSpawnChance;
        this.wanderingTraderId = wanderingTraderId;
        this.serverBrands = serverBrands;
        this.removedFeatures = removedFeatures;
        this.playerData = playerData;
        this.scheduledEvents = scheduledEvents;
        this.customBossEvents = customBossEvents;
        this.dragonFight = dragonFight;
        this.levelInfo = levelInfo;
        this.generatorOptions = generatorOptions;
        this.specialProperty = specialProperty;
        this.lifecycle = lifecycle;
    }

    public LevelProperties(LevelInfo levelInfo, GeneratorOptions generatorOptions, SpecialProperty specialProperty, Lifecycle lifecycle) {
        this(null, false, WorldProperties.SpawnPoint.DEFAULT, 0L, 0L, 19133, 0, 0, false, 0, false, false, false, Optional.empty(), 0, 0, null, Sets.newLinkedHashSet(), new HashSet<String>(), new Timer<MinecraftServer>(TimerCallbackSerializer.INSTANCE), null, EnderDragonFight.Data.DEFAULT, levelInfo.withCopiedGameRules(), generatorOptions, specialProperty, lifecycle);
    }

    public static <T> LevelProperties readProperties(Dynamic<T> dynamic, LevelInfo info, SpecialProperty specialProperty, GeneratorOptions generatorOptions, Lifecycle lifecycle) {
        long l = dynamic.get("Time").asLong(0L);
        return new LevelProperties(dynamic.get(PLAYER_KEY).flatMap(arg_0 -> NbtCompound.CODEC.parse(arg_0)).result().orElse(null), dynamic.get("WasModded").asBoolean(false), dynamic.get("spawn").read(WorldProperties.SpawnPoint.CODEC).result().orElse(WorldProperties.SpawnPoint.DEFAULT), l, dynamic.get("DayTime").asLong(l), SaveVersionInfo.fromDynamic(dynamic).getLevelFormatVersion(), dynamic.get("clearWeatherTime").asInt(0), dynamic.get("rainTime").asInt(0), dynamic.get("raining").asBoolean(false), dynamic.get("thunderTime").asInt(0), dynamic.get("thundering").asBoolean(false), dynamic.get("initialized").asBoolean(true), dynamic.get("DifficultyLocked").asBoolean(false), WorldBorder.Properties.CODEC.parse(dynamic.get("world_border").orElseEmptyMap()).result(), dynamic.get("WanderingTraderSpawnDelay").asInt(0), dynamic.get("WanderingTraderSpawnChance").asInt(0), dynamic.get("WanderingTraderId").read(Uuids.INT_STREAM_CODEC).result().orElse(null), dynamic.get("ServerBrands").asStream().flatMap(serverBrands -> serverBrands.asString().result().stream()).collect(Collectors.toCollection(Sets::newLinkedHashSet)), dynamic.get("removed_features").asStream().flatMap(removedFeatures -> removedFeatures.asString().result().stream()).collect(Collectors.toSet()), new Timer<MinecraftServer>(TimerCallbackSerializer.INSTANCE, dynamic.get("ScheduledEvents").asStream()), (NbtCompound)dynamic.get("CustomBossEvents").orElseEmptyMap().getValue(), dynamic.get("DragonFight").read(EnderDragonFight.Data.CODEC).resultOrPartial(arg_0 -> ((Logger)LOGGER).error(arg_0)).orElse(EnderDragonFight.Data.DEFAULT), info, generatorOptions, specialProperty, lifecycle);
    }

    @Override
    public NbtCompound cloneWorldNbt(DynamicRegistryManager registryManager, @Nullable NbtCompound playerNbt) {
        if (playerNbt == null) {
            playerNbt = this.playerData;
        }
        NbtCompound nbtCompound = new NbtCompound();
        this.updateProperties(registryManager, nbtCompound, playerNbt);
        return nbtCompound;
    }

    private void updateProperties(DynamicRegistryManager registryManager, NbtCompound levelNbt, @Nullable NbtCompound playerNbt) {
        levelNbt.put("ServerBrands", LevelProperties.createStringList(this.serverBrands));
        levelNbt.putBoolean("WasModded", this.modded);
        if (!this.removedFeatures.isEmpty()) {
            levelNbt.put("removed_features", LevelProperties.createStringList(this.removedFeatures));
        }
        NbtCompound nbtCompound = new NbtCompound();
        nbtCompound.putString("Name", SharedConstants.getGameVersion().name());
        nbtCompound.putInt("Id", SharedConstants.getGameVersion().dataVersion().id());
        nbtCompound.putBoolean("Snapshot", !SharedConstants.getGameVersion().stable());
        nbtCompound.putString("Series", SharedConstants.getGameVersion().dataVersion().series());
        levelNbt.put("Version", nbtCompound);
        NbtHelper.putDataVersion(levelNbt);
        RegistryOps<NbtElement> dynamicOps = registryManager.getOps(NbtOps.INSTANCE);
        WorldGenSettings.encode(dynamicOps, this.generatorOptions, registryManager).resultOrPartial(Util.addPrefix("WorldGenSettings: ", arg_0 -> ((Logger)LOGGER).error(arg_0))).ifPresent(worldGenSettings -> levelNbt.put(WORLD_GEN_SETTINGS_KEY, (NbtElement)worldGenSettings));
        levelNbt.putInt("GameType", this.levelInfo.getGameMode().getIndex());
        levelNbt.put("spawn", WorldProperties.SpawnPoint.CODEC, this.spawnPoint);
        levelNbt.putLong("Time", this.time);
        levelNbt.putLong("DayTime", this.timeOfDay);
        levelNbt.putLong("LastPlayed", Util.getEpochTimeMs());
        levelNbt.putString(LEVEL_NAME_KEY, this.levelInfo.getLevelName());
        levelNbt.putInt("version", 19133);
        levelNbt.putInt("clearWeatherTime", this.clearWeatherTime);
        levelNbt.putInt("rainTime", this.rainTime);
        levelNbt.putBoolean("raining", this.raining);
        levelNbt.putInt("thunderTime", this.thunderTime);
        levelNbt.putBoolean("thundering", this.thundering);
        levelNbt.putBoolean("hardcore", this.levelInfo.isHardcore());
        levelNbt.putBoolean("allowCommands", this.levelInfo.areCommandsAllowed());
        levelNbt.putBoolean("initialized", this.initialized);
        this.worldBorder.ifPresent(worldBorder -> levelNbt.put("world_border", WorldBorder.Properties.CODEC, worldBorder));
        levelNbt.putByte("Difficulty", (byte)this.levelInfo.getDifficulty().getId());
        levelNbt.putBoolean("DifficultyLocked", this.difficultyLocked);
        levelNbt.put("game_rules", GameRules.createCodec(this.getEnabledFeatures()), this.levelInfo.getGameRules());
        levelNbt.put("DragonFight", EnderDragonFight.Data.CODEC, this.dragonFight);
        if (playerNbt != null) {
            levelNbt.put(PLAYER_KEY, playerNbt);
        }
        levelNbt.copyFromCodec(DataConfiguration.MAP_CODEC, this.levelInfo.getDataConfiguration());
        if (this.customBossEvents != null) {
            levelNbt.put("CustomBossEvents", this.customBossEvents);
        }
        levelNbt.put("ScheduledEvents", this.scheduledEvents.toNbt());
        levelNbt.putInt("WanderingTraderSpawnDelay", this.wanderingTraderSpawnDelay);
        levelNbt.putInt("WanderingTraderSpawnChance", this.wanderingTraderSpawnChance);
        levelNbt.putNullable("WanderingTraderId", Uuids.INT_STREAM_CODEC, this.wanderingTraderId);
    }

    private static NbtList createStringList(Set<String> strings) {
        NbtList nbtList = new NbtList();
        strings.stream().map(NbtString::of).forEach(nbtList::add);
        return nbtList;
    }

    @Override
    public WorldProperties.SpawnPoint getSpawnPoint() {
        return this.spawnPoint;
    }

    @Override
    public long getTime() {
        return this.time;
    }

    @Override
    public long getTimeOfDay() {
        return this.timeOfDay;
    }

    @Override
    public @Nullable NbtCompound getPlayerData() {
        return this.playerData;
    }

    @Override
    public void setTime(long time) {
        this.time = time;
    }

    @Override
    public void setTimeOfDay(long timeOfDay) {
        this.timeOfDay = timeOfDay;
    }

    @Override
    public void setSpawnPoint(WorldProperties.SpawnPoint spawnPoint) {
        this.spawnPoint = spawnPoint;
    }

    @Override
    public String getLevelName() {
        return this.levelInfo.getLevelName();
    }

    @Override
    public int getVersion() {
        return this.version;
    }

    @Override
    public int getClearWeatherTime() {
        return this.clearWeatherTime;
    }

    @Override
    public void setClearWeatherTime(int clearWeatherTime) {
        this.clearWeatherTime = clearWeatherTime;
    }

    @Override
    public boolean isThundering() {
        return this.thundering;
    }

    @Override
    public void setThundering(boolean thundering) {
        this.thundering = thundering;
    }

    @Override
    public int getThunderTime() {
        return this.thunderTime;
    }

    @Override
    public void setThunderTime(int thunderTime) {
        this.thunderTime = thunderTime;
    }

    @Override
    public boolean isRaining() {
        return this.raining;
    }

    @Override
    public void setRaining(boolean raining) {
        this.raining = raining;
    }

    @Override
    public int getRainTime() {
        return this.rainTime;
    }

    @Override
    public void setRainTime(int rainTime) {
        this.rainTime = rainTime;
    }

    @Override
    public GameMode getGameMode() {
        return this.levelInfo.getGameMode();
    }

    @Override
    public void setGameMode(GameMode gameMode) {
        this.levelInfo = this.levelInfo.withGameMode(gameMode);
    }

    @Override
    public boolean isHardcore() {
        return this.levelInfo.isHardcore();
    }

    @Override
    public boolean areCommandsAllowed() {
        return this.levelInfo.areCommandsAllowed();
    }

    @Override
    public boolean isInitialized() {
        return this.initialized;
    }

    @Override
    public void setInitialized(boolean initialized) {
        this.initialized = initialized;
    }

    @Override
    public GameRules getGameRules() {
        return this.levelInfo.getGameRules();
    }

    @Override
    public Optional<WorldBorder.Properties> getWorldBorder() {
        return this.worldBorder;
    }

    @Override
    public void setWorldBorder(Optional<WorldBorder.Properties> worldBorder) {
        this.worldBorder = worldBorder;
    }

    @Override
    public Difficulty getDifficulty() {
        return this.levelInfo.getDifficulty();
    }

    @Override
    public void setDifficulty(Difficulty difficulty) {
        this.levelInfo = this.levelInfo.withDifficulty(difficulty);
    }

    @Override
    public boolean isDifficultyLocked() {
        return this.difficultyLocked;
    }

    @Override
    public void setDifficultyLocked(boolean difficultyLocked) {
        this.difficultyLocked = difficultyLocked;
    }

    @Override
    public Timer<MinecraftServer> getScheduledEvents() {
        return this.scheduledEvents;
    }

    @Override
    public void populateCrashReport(CrashReportSection reportSection, HeightLimitView world) {
        ServerWorldProperties.super.populateCrashReport(reportSection, world);
        SaveProperties.super.populateCrashReport(reportSection);
    }

    @Override
    public GeneratorOptions getGeneratorOptions() {
        return this.generatorOptions;
    }

    @Override
    public boolean isFlatWorld() {
        return this.specialProperty == SpecialProperty.FLAT;
    }

    @Override
    public boolean isDebugWorld() {
        return this.specialProperty == SpecialProperty.DEBUG;
    }

    @Override
    public Lifecycle getLifecycle() {
        return this.lifecycle;
    }

    @Override
    public EnderDragonFight.Data getDragonFight() {
        return this.dragonFight;
    }

    @Override
    public void setDragonFight(EnderDragonFight.Data dragonFight) {
        this.dragonFight = dragonFight;
    }

    @Override
    public DataConfiguration getDataConfiguration() {
        return this.levelInfo.getDataConfiguration();
    }

    @Override
    public void updateLevelInfo(DataConfiguration dataConfiguration) {
        this.levelInfo = this.levelInfo.withDataConfiguration(dataConfiguration);
    }

    @Override
    public @Nullable NbtCompound getCustomBossEvents() {
        return this.customBossEvents;
    }

    @Override
    public void setCustomBossEvents(@Nullable NbtCompound customBossEvents) {
        this.customBossEvents = customBossEvents;
    }

    @Override
    public int getWanderingTraderSpawnDelay() {
        return this.wanderingTraderSpawnDelay;
    }

    @Override
    public void setWanderingTraderSpawnDelay(int wanderingTraderSpawnDelay) {
        this.wanderingTraderSpawnDelay = wanderingTraderSpawnDelay;
    }

    @Override
    public int getWanderingTraderSpawnChance() {
        return this.wanderingTraderSpawnChance;
    }

    @Override
    public void setWanderingTraderSpawnChance(int wanderingTraderSpawnChance) {
        this.wanderingTraderSpawnChance = wanderingTraderSpawnChance;
    }

    @Override
    public @Nullable UUID getWanderingTraderId() {
        return this.wanderingTraderId;
    }

    @Override
    public void setWanderingTraderId(UUID wanderingTraderId) {
        this.wanderingTraderId = wanderingTraderId;
    }

    @Override
    public void addServerBrand(String brand, boolean modded) {
        this.serverBrands.add(brand);
        this.modded |= modded;
    }

    @Override
    public boolean isModded() {
        return this.modded;
    }

    @Override
    public Set<String> getServerBrands() {
        return ImmutableSet.copyOf(this.serverBrands);
    }

    @Override
    public Set<String> getRemovedFeatures() {
        return Set.copyOf(this.removedFeatures);
    }

    @Override
    public ServerWorldProperties getMainWorldProperties() {
        return this;
    }

    @Override
    public LevelInfo getLevelInfo() {
        return this.levelInfo.withCopiedGameRules();
    }

    @Deprecated
    public static final class SpecialProperty
    extends Enum<SpecialProperty> {
        public static final /* enum */ SpecialProperty NONE = new SpecialProperty();
        public static final /* enum */ SpecialProperty FLAT = new SpecialProperty();
        public static final /* enum */ SpecialProperty DEBUG = new SpecialProperty();
        private static final /* synthetic */ SpecialProperty[] field_40376;

        public static SpecialProperty[] values() {
            return (SpecialProperty[])field_40376.clone();
        }

        public static SpecialProperty valueOf(String string) {
            return Enum.valueOf(SpecialProperty.class, string);
        }

        private static /* synthetic */ SpecialProperty[] method_45559() {
            return new SpecialProperty[]{NONE, FLAT, DEBUG};
        }

        static {
            field_40376 = SpecialProperty.method_45559();
        }
    }
}


/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  com.google.common.collect.Lists
 *  com.google.common.collect.Maps
 *  com.google.common.collect.Sets
 *  com.mojang.authlib.GameProfile
 *  com.mojang.logging.LogUtils
 *  org.jspecify.annotations.Nullable
 *  org.slf4j.Logger
 */
package net.minecraft.server;

import com.google.common.collect.Lists;
import com.google.common.collect.Maps;
import com.google.common.collect.Sets;
import com.mojang.authlib.GameProfile;
import com.mojang.logging.LogUtils;
import java.io.File;
import java.io.IOException;
import java.net.SocketAddress;
import java.nio.file.CopyOption;
import java.nio.file.Files;
import java.nio.file.LinkOption;
import java.nio.file.Path;
import java.text.SimpleDateFormat;
import java.time.Instant;
import java.util.ArrayList;
import java.util.Collection;
import java.util.EnumSet;
import java.util.HashSet;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Optional;
import java.util.Set;
import java.util.UUID;
import java.util.function.Function;
import java.util.function.Predicate;
import net.minecraft.advancement.PlayerAdvancementTracker;
import net.minecraft.block.BlockState;
import net.minecraft.block.Blocks;
import net.minecraft.command.permission.LeveledPermissionPredicate;
import net.minecraft.command.permission.PermissionLevel;
import net.minecraft.entity.Entity;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.effect.StatusEffectInstance;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.entity.projectile.thrown.EnderPearlEntity;
import net.minecraft.nbt.NbtCompound;
import net.minecraft.network.ClientConnection;
import net.minecraft.network.RegistryByteBuf;
import net.minecraft.network.message.MessageType;
import net.minecraft.network.message.SentMessage;
import net.minecraft.network.message.SignedMessage;
import net.minecraft.network.packet.Packet;
import net.minecraft.network.packet.s2c.common.SynchronizeTagsS2CPacket;
import net.minecraft.network.packet.s2c.play.ChunkLoadDistanceS2CPacket;
import net.minecraft.network.packet.s2c.play.DifficultyS2CPacket;
import net.minecraft.network.packet.s2c.play.EntityStatusEffectS2CPacket;
import net.minecraft.network.packet.s2c.play.EntityStatusS2CPacket;
import net.minecraft.network.packet.s2c.play.ExperienceBarUpdateS2CPacket;
import net.minecraft.network.packet.s2c.play.GameJoinS2CPacket;
import net.minecraft.network.packet.s2c.play.GameStateChangeS2CPacket;
import net.minecraft.network.packet.s2c.play.PlaySoundS2CPacket;
import net.minecraft.network.packet.s2c.play.PlayerAbilitiesS2CPacket;
import net.minecraft.network.packet.s2c.play.PlayerListS2CPacket;
import net.minecraft.network.packet.s2c.play.PlayerRemoveS2CPacket;
import net.minecraft.network.packet.s2c.play.PlayerRespawnS2CPacket;
import net.minecraft.network.packet.s2c.play.PlayerSpawnPositionS2CPacket;
import net.minecraft.network.packet.s2c.play.SimulationDistanceS2CPacket;
import net.minecraft.network.packet.s2c.play.SynchronizeRecipesS2CPacket;
import net.minecraft.network.packet.s2c.play.TeamS2CPacket;
import net.minecraft.network.packet.s2c.play.UpdateSelectedSlotS2CPacket;
import net.minecraft.network.packet.s2c.play.WorldBorderCenterChangedS2CPacket;
import net.minecraft.network.packet.s2c.play.WorldBorderInitializeS2CPacket;
import net.minecraft.network.packet.s2c.play.WorldBorderInterpolateSizeS2CPacket;
import net.minecraft.network.packet.s2c.play.WorldBorderSizeChangedS2CPacket;
import net.minecraft.network.packet.s2c.play.WorldBorderWarningBlocksChangedS2CPacket;
import net.minecraft.network.packet.s2c.play.WorldBorderWarningTimeChangedS2CPacket;
import net.minecraft.network.packet.s2c.play.WorldTimeUpdateS2CPacket;
import net.minecraft.network.state.PlayStateFactories;
import net.minecraft.recipe.ServerRecipeManager;
import net.minecraft.registry.CombinedDynamicRegistries;
import net.minecraft.registry.RegistryKey;
import net.minecraft.registry.ServerDynamicRegistryType;
import net.minecraft.registry.tag.TagPacketSerializer;
import net.minecraft.scoreboard.AbstractTeam;
import net.minecraft.scoreboard.ScoreboardDisplaySlot;
import net.minecraft.scoreboard.ScoreboardObjective;
import net.minecraft.scoreboard.ServerScoreboard;
import net.minecraft.scoreboard.Team;
import net.minecraft.server.BannedIpEntry;
import net.minecraft.server.BannedIpList;
import net.minecraft.server.BannedPlayerEntry;
import net.minecraft.server.BannedPlayerList;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.OperatorEntry;
import net.minecraft.server.OperatorList;
import net.minecraft.server.PlayerConfigEntry;
import net.minecraft.server.ServerMetadata;
import net.minecraft.server.Whitelist;
import net.minecraft.server.command.ServerCommandSource;
import net.minecraft.server.dedicated.management.listener.ManagementListener;
import net.minecraft.server.network.ConnectedClientData;
import net.minecraft.server.network.ServerPlayNetworkHandler;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.sound.SoundCategory;
import net.minecraft.sound.SoundEvents;
import net.minecraft.stat.ServerStatHandler;
import net.minecraft.stat.Stats;
import net.minecraft.text.MutableText;
import net.minecraft.text.Text;
import net.minecraft.util.Formatting;
import net.minecraft.util.NameToIdCache;
import net.minecraft.util.WorldSavePath;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Vec3d;
import net.minecraft.util.path.PathUtil;
import net.minecraft.world.PlayerSaveHandler;
import net.minecraft.world.TeleportTarget;
import net.minecraft.world.World;
import net.minecraft.world.WorldProperties;
import net.minecraft.world.border.WorldBorder;
import net.minecraft.world.border.WorldBorderListener;
import net.minecraft.world.rule.GameRules;
import org.jspecify.annotations.Nullable;
import org.slf4j.Logger;

public abstract class PlayerManager {
    public static final File BANNED_PLAYERS_FILE = new File("banned-players.json");
    public static final File BANNED_IPS_FILE = new File("banned-ips.json");
    public static final File OPERATORS_FILE = new File("ops.json");
    public static final File WHITELIST_FILE = new File("whitelist.json");
    public static final Text FILTERED_FULL_TEXT = Text.translatable("chat.filtered_full");
    public static final Text DUPLICATE_LOGIN_TEXT = Text.translatable("multiplayer.disconnect.duplicate_login");
    private static final Logger LOGGER = LogUtils.getLogger();
    private static final int LATENCY_UPDATE_INTERVAL = 600;
    private static final SimpleDateFormat DATE_FORMATTER = new SimpleDateFormat("yyyy-MM-dd 'at' HH:mm:ss z", Locale.ROOT);
    private final MinecraftServer server;
    private final List<ServerPlayerEntity> players = Lists.newArrayList();
    private final Map<UUID, ServerPlayerEntity> playerMap = Maps.newHashMap();
    private final BannedPlayerList bannedProfiles;
    private final BannedIpList bannedIps;
    private final OperatorList ops;
    private final Whitelist whitelist;
    private final Map<UUID, ServerStatHandler> statisticsMap = Maps.newHashMap();
    private final Map<UUID, PlayerAdvancementTracker> advancementTrackers = Maps.newHashMap();
    private final PlayerSaveHandler saveHandler;
    private final CombinedDynamicRegistries<ServerDynamicRegistryType> registryManager;
    private int viewDistance;
    private int simulationDistance;
    private boolean cheatsAllowed;
    private int latencyUpdateTimer;

    public PlayerManager(MinecraftServer server, CombinedDynamicRegistries<ServerDynamicRegistryType> registryManager, PlayerSaveHandler saveHandler, ManagementListener managementListener) {
        this.server = server;
        this.registryManager = registryManager;
        this.saveHandler = saveHandler;
        this.whitelist = new Whitelist(WHITELIST_FILE, managementListener);
        this.ops = new OperatorList(OPERATORS_FILE, managementListener);
        this.bannedProfiles = new BannedPlayerList(BANNED_PLAYERS_FILE, managementListener);
        this.bannedIps = new BannedIpList(BANNED_IPS_FILE, managementListener);
    }

    public void onPlayerConnect(ClientConnection connection, ServerPlayerEntity player, ConnectedClientData clientData) {
        PlayerConfigEntry playerConfigEntry = player.getPlayerConfigEntry();
        NameToIdCache nameToIdCache = this.server.getApiServices().nameToIdCache();
        Optional<PlayerConfigEntry> optional = nameToIdCache.getByUuid(playerConfigEntry.id());
        String string = optional.map(PlayerConfigEntry::name).orElse(playerConfigEntry.name());
        nameToIdCache.add(playerConfigEntry);
        ServerWorld serverWorld = player.getEntityWorld();
        String string2 = connection.getAddressAsString(this.server.shouldLogIps());
        LOGGER.info("{}[{}] logged in with entity id {} at ({}, {}, {})", new Object[]{player.getStringifiedName(), string2, player.getId(), player.getX(), player.getY(), player.getZ()});
        WorldProperties worldProperties = serverWorld.getLevelProperties();
        ServerPlayNetworkHandler serverPlayNetworkHandler = new ServerPlayNetworkHandler(this.server, connection, player, clientData);
        connection.transitionInbound(PlayStateFactories.C2S.bind(RegistryByteBuf.makeFactory(this.server.getRegistryManager()), serverPlayNetworkHandler), serverPlayNetworkHandler);
        serverPlayNetworkHandler.disableFlush();
        GameRules gameRules = serverWorld.getGameRules();
        boolean bl = gameRules.getValue(GameRules.DO_IMMEDIATE_RESPAWN);
        boolean bl2 = gameRules.getValue(GameRules.REDUCED_DEBUG_INFO);
        boolean bl3 = gameRules.getValue(GameRules.LIMITED_CRAFTING);
        serverPlayNetworkHandler.sendPacket(new GameJoinS2CPacket(player.getId(), worldProperties.isHardcore(), this.server.getWorldRegistryKeys(), this.getMaxPlayerCount(), this.getViewDistance(), this.getSimulationDistance(), bl2, !bl, bl3, player.createCommonPlayerSpawnInfo(serverWorld), this.server.shouldEnforceSecureProfile()));
        serverPlayNetworkHandler.sendPacket(new DifficultyS2CPacket(worldProperties.getDifficulty(), worldProperties.isDifficultyLocked()));
        serverPlayNetworkHandler.sendPacket(new PlayerAbilitiesS2CPacket(player.getAbilities()));
        serverPlayNetworkHandler.sendPacket(new UpdateSelectedSlotS2CPacket(player.getInventory().getSelectedSlot()));
        ServerRecipeManager serverRecipeManager = this.server.getRecipeManager();
        serverPlayNetworkHandler.sendPacket(new SynchronizeRecipesS2CPacket(serverRecipeManager.getPropertySets(), serverRecipeManager.getStonecutterRecipeForSync()));
        this.sendCommandTree(player);
        player.getStatHandler().updateStatSet();
        player.getRecipeBook().sendInitRecipesPacket(player);
        this.sendScoreboard(serverWorld.getScoreboard(), player);
        this.server.forcePlayerSampleUpdate();
        MutableText mutableText = player.getGameProfile().name().equalsIgnoreCase(string) ? Text.translatable("multiplayer.player.joined", player.getDisplayName()) : Text.translatable("multiplayer.player.joined.renamed", player.getDisplayName(), string);
        this.broadcast(mutableText.formatted(Formatting.YELLOW), false);
        serverPlayNetworkHandler.requestTeleport(player.getX(), player.getY(), player.getZ(), player.getYaw(), player.getPitch());
        ServerMetadata serverMetadata = this.server.getServerMetadata();
        if (serverMetadata != null && !clientData.transferred()) {
            player.sendServerMetadata(serverMetadata);
        }
        player.networkHandler.sendPacket(PlayerListS2CPacket.entryFromPlayer(this.players));
        this.players.add(player);
        this.playerMap.put(player.getUuid(), player);
        this.sendToAll(PlayerListS2CPacket.entryFromPlayer(List.of(player)));
        this.sendWorldInfo(player, serverWorld);
        serverWorld.onPlayerConnected(player);
        this.server.getBossBarManager().onPlayerConnect(player);
        this.sendStatusEffects(player);
        player.onSpawn();
        this.server.getManagementListener().onPlayerJoined(player);
        serverPlayNetworkHandler.enableFlush();
    }

    protected void sendScoreboard(ServerScoreboard scoreboard, ServerPlayerEntity player) {
        HashSet set = Sets.newHashSet();
        for (Team team : scoreboard.getTeams()) {
            player.networkHandler.sendPacket(TeamS2CPacket.updateTeam(team, true));
        }
        for (ScoreboardDisplaySlot scoreboardDisplaySlot : ScoreboardDisplaySlot.values()) {
            ScoreboardObjective scoreboardObjective = scoreboard.getObjectiveForSlot(scoreboardDisplaySlot);
            if (scoreboardObjective == null || set.contains(scoreboardObjective)) continue;
            List<Packet<?>> list = scoreboard.createChangePackets(scoreboardObjective);
            for (Packet<?> packet : list) {
                player.networkHandler.sendPacket(packet);
            }
            set.add(scoreboardObjective);
        }
    }

    public void setMainWorld(final ServerWorld world) {
        world.getWorldBorder().addListener(new WorldBorderListener(){

            @Override
            public void onSizeChange(WorldBorder border, double size) {
                PlayerManager.this.sendToDimension(new WorldBorderSizeChangedS2CPacket(border), world.getRegistryKey());
            }

            @Override
            public void onInterpolateSize(WorldBorder border, double fromSize, double toSize, long time, long l) {
                PlayerManager.this.sendToDimension(new WorldBorderInterpolateSizeS2CPacket(border), world.getRegistryKey());
            }

            @Override
            public void onCenterChanged(WorldBorder border, double centerX, double centerZ) {
                PlayerManager.this.sendToDimension(new WorldBorderCenterChangedS2CPacket(border), world.getRegistryKey());
            }

            @Override
            public void onWarningTimeChanged(WorldBorder border, int warningTime) {
                PlayerManager.this.sendToDimension(new WorldBorderWarningTimeChangedS2CPacket(border), world.getRegistryKey());
            }

            @Override
            public void onWarningBlocksChanged(WorldBorder border, int warningBlockDistance) {
                PlayerManager.this.sendToDimension(new WorldBorderWarningBlocksChangedS2CPacket(border), world.getRegistryKey());
            }

            @Override
            public void onDamagePerBlockChanged(WorldBorder border, double damagePerBlock) {
            }

            @Override
            public void onSafeZoneChanged(WorldBorder border, double safeZoneRadius) {
            }
        });
    }

    public Optional<NbtCompound> loadPlayerData(PlayerConfigEntry player) {
        NbtCompound nbtCompound = this.server.getSaveProperties().getPlayerData();
        if (this.server.isHost(player) && nbtCompound != null) {
            LOGGER.debug("loading single player");
            return Optional.of(nbtCompound);
        }
        return this.saveHandler.loadPlayerData(player);
    }

    protected void savePlayerData(ServerPlayerEntity player) {
        PlayerAdvancementTracker playerAdvancementTracker;
        this.saveHandler.savePlayerData(player);
        ServerStatHandler serverStatHandler = this.statisticsMap.get(player.getUuid());
        if (serverStatHandler != null) {
            serverStatHandler.save();
        }
        if ((playerAdvancementTracker = this.advancementTrackers.get(player.getUuid())) != null) {
            playerAdvancementTracker.save();
        }
    }

    public void remove(ServerPlayerEntity player) {
        Object entity2;
        ServerWorld serverWorld = player.getEntityWorld();
        player.incrementStat(Stats.LEAVE_GAME);
        this.savePlayerData(player);
        if (player.hasVehicle() && ((Entity)(entity2 = player.getRootVehicle())).hasPlayerRider()) {
            LOGGER.debug("Removing player mount");
            player.stopRiding();
            ((Entity)entity2).streamPassengersAndSelf().forEach(entity -> entity.setRemoved(Entity.RemovalReason.UNLOADED_WITH_PLAYER));
        }
        player.detach();
        for (EnderPearlEntity enderPearlEntity : player.getEnderPearls()) {
            enderPearlEntity.setRemoved(Entity.RemovalReason.UNLOADED_WITH_PLAYER);
        }
        serverWorld.removePlayer(player, Entity.RemovalReason.UNLOADED_WITH_PLAYER);
        player.getAdvancementTracker().clearCriteria();
        this.players.remove(player);
        this.server.getBossBarManager().onPlayerDisconnect(player);
        UUID uUID = player.getUuid();
        ServerPlayerEntity serverPlayerEntity = this.playerMap.get(uUID);
        if (serverPlayerEntity == player) {
            this.playerMap.remove(uUID);
            this.statisticsMap.remove(uUID);
            this.advancementTrackers.remove(uUID);
            this.server.getManagementListener().onPlayerLeft(player);
        }
        this.sendToAll(new PlayerRemoveS2CPacket(List.of(player.getUuid())));
    }

    public @Nullable Text checkCanJoin(SocketAddress address, PlayerConfigEntry configEntry) {
        if (this.bannedProfiles.contains(configEntry)) {
            BannedPlayerEntry bannedPlayerEntry = (BannedPlayerEntry)this.bannedProfiles.get(configEntry);
            MutableText mutableText = Text.translatable("multiplayer.disconnect.banned.reason", bannedPlayerEntry.getReasonText());
            if (bannedPlayerEntry.getExpiryDate() != null) {
                mutableText.append(Text.translatable("multiplayer.disconnect.banned.expiration", DATE_FORMATTER.format(bannedPlayerEntry.getExpiryDate())));
            }
            return mutableText;
        }
        if (!this.isWhitelisted(configEntry)) {
            return Text.translatable("multiplayer.disconnect.not_whitelisted");
        }
        if (this.bannedIps.isBanned(address)) {
            BannedIpEntry bannedIpEntry = this.bannedIps.get(address);
            MutableText mutableText = Text.translatable("multiplayer.disconnect.banned_ip.reason", bannedIpEntry.getReasonText());
            if (bannedIpEntry.getExpiryDate() != null) {
                mutableText.append(Text.translatable("multiplayer.disconnect.banned_ip.expiration", DATE_FORMATTER.format(bannedIpEntry.getExpiryDate())));
            }
            return mutableText;
        }
        if (this.players.size() >= this.getMaxPlayerCount() && !this.canBypassPlayerLimit(configEntry)) {
            return Text.translatable("multiplayer.disconnect.server_full");
        }
        return null;
    }

    public boolean disconnectDuplicateLogins(UUID uuid) {
        Set set = Sets.newIdentityHashSet();
        for (ServerPlayerEntity serverPlayerEntity : this.players) {
            if (!serverPlayerEntity.getUuid().equals(uuid)) continue;
            set.add(serverPlayerEntity);
        }
        ServerPlayerEntity serverPlayerEntity2 = this.playerMap.get(uuid);
        if (serverPlayerEntity2 != null) {
            set.add(serverPlayerEntity2);
        }
        for (ServerPlayerEntity serverPlayerEntity3 : set) {
            serverPlayerEntity3.networkHandler.disconnect(DUPLICATE_LOGIN_TEXT);
        }
        return !set.isEmpty();
    }

    public ServerPlayerEntity respawnPlayer(ServerPlayerEntity player, boolean alive, Entity.RemovalReason removalReason) {
        BlockPos blockPos;
        BlockState blockState;
        WorldProperties.SpawnPoint spawnPoint;
        ServerWorld serverWorld3;
        TeleportTarget teleportTarget = player.getRespawnTarget(!alive, TeleportTarget.NO_OP);
        this.players.remove(player);
        player.getEntityWorld().removePlayer(player, removalReason);
        ServerWorld serverWorld = teleportTarget.world();
        ServerPlayerEntity serverPlayerEntity = new ServerPlayerEntity(this.server, serverWorld, player.getGameProfile(), player.getClientOptions());
        serverPlayerEntity.networkHandler = player.networkHandler;
        serverPlayerEntity.copyFrom(player, alive);
        serverPlayerEntity.setId(player.getId());
        serverPlayerEntity.setMainArm(player.getMainArm());
        if (!teleportTarget.missingRespawnBlock()) {
            serverPlayerEntity.setSpawnPointFrom(player);
        }
        for (String string : player.getCommandTags()) {
            serverPlayerEntity.addCommandTag(string);
        }
        Vec3d vec3d = teleportTarget.position();
        serverPlayerEntity.refreshPositionAndAngles(vec3d.x, vec3d.y, vec3d.z, teleportTarget.yaw(), teleportTarget.pitch());
        if (teleportTarget.missingRespawnBlock()) {
            serverPlayerEntity.networkHandler.sendPacket(new GameStateChangeS2CPacket(GameStateChangeS2CPacket.NO_RESPAWN_BLOCK, 0.0f));
        }
        byte b = alive ? (byte)1 : 0;
        ServerWorld serverWorld2 = serverPlayerEntity.getEntityWorld();
        WorldProperties worldProperties = serverWorld2.getLevelProperties();
        serverPlayerEntity.networkHandler.sendPacket(new PlayerRespawnS2CPacket(serverPlayerEntity.createCommonPlayerSpawnInfo(serverWorld2), b));
        serverPlayerEntity.networkHandler.requestTeleport(serverPlayerEntity.getX(), serverPlayerEntity.getY(), serverPlayerEntity.getZ(), serverPlayerEntity.getYaw(), serverPlayerEntity.getPitch());
        serverPlayerEntity.networkHandler.sendPacket(new PlayerSpawnPositionS2CPacket(serverWorld.getSpawnPoint()));
        serverPlayerEntity.networkHandler.sendPacket(new DifficultyS2CPacket(worldProperties.getDifficulty(), worldProperties.isDifficultyLocked()));
        serverPlayerEntity.networkHandler.sendPacket(new ExperienceBarUpdateS2CPacket(serverPlayerEntity.experienceProgress, serverPlayerEntity.totalExperience, serverPlayerEntity.experienceLevel));
        this.sendStatusEffects(serverPlayerEntity);
        this.sendWorldInfo(serverPlayerEntity, serverWorld);
        this.sendCommandTree(serverPlayerEntity);
        serverWorld.onPlayerRespawned(serverPlayerEntity);
        this.players.add(serverPlayerEntity);
        this.playerMap.put(serverPlayerEntity.getUuid(), serverPlayerEntity);
        serverPlayerEntity.onSpawn();
        serverPlayerEntity.setHealth(serverPlayerEntity.getHealth());
        ServerPlayerEntity.Respawn respawn = serverPlayerEntity.getRespawn();
        if (!alive && respawn != null && (serverWorld3 = this.server.getWorld((spawnPoint = respawn.respawnData()).getDimension())) != null && (blockState = serverWorld3.getBlockState(blockPos = spawnPoint.getPos())).isOf(Blocks.RESPAWN_ANCHOR)) {
            serverPlayerEntity.networkHandler.sendPacket(new PlaySoundS2CPacket(SoundEvents.BLOCK_RESPAWN_ANCHOR_DEPLETE, SoundCategory.BLOCKS, blockPos.getX(), blockPos.getY(), blockPos.getZ(), 1.0f, 1.0f, serverWorld.getRandom().nextLong()));
        }
        return serverPlayerEntity;
    }

    public void sendStatusEffects(ServerPlayerEntity player) {
        this.sendStatusEffects(player, player.networkHandler);
    }

    public void sendStatusEffects(LivingEntity entity, ServerPlayNetworkHandler networkHandler) {
        for (StatusEffectInstance statusEffectInstance : entity.getStatusEffects()) {
            networkHandler.sendPacket(new EntityStatusEffectS2CPacket(entity.getId(), statusEffectInstance, false));
        }
    }

    public void sendCommandTree(ServerPlayerEntity player) {
        LeveledPermissionPredicate leveledPermissionPredicate = this.server.getPermissionLevel(player.getPlayerConfigEntry());
        this.sendCommandTree(player, leveledPermissionPredicate);
    }

    public void updatePlayerLatency() {
        if (++this.latencyUpdateTimer > 600) {
            this.sendToAll(new PlayerListS2CPacket(EnumSet.of(PlayerListS2CPacket.Action.UPDATE_LATENCY), this.players));
            this.latencyUpdateTimer = 0;
        }
    }

    public void sendToAll(Packet<?> packet) {
        for (ServerPlayerEntity serverPlayerEntity : this.players) {
            serverPlayerEntity.networkHandler.sendPacket(packet);
        }
    }

    public void sendToDimension(Packet<?> packet, RegistryKey<World> dimension) {
        for (ServerPlayerEntity serverPlayerEntity : this.players) {
            if (serverPlayerEntity.getEntityWorld().getRegistryKey() != dimension) continue;
            serverPlayerEntity.networkHandler.sendPacket(packet);
        }
    }

    public void sendToTeam(PlayerEntity source, Text message) {
        Team abstractTeam = source.getScoreboardTeam();
        if (abstractTeam == null) {
            return;
        }
        Collection<String> collection = ((AbstractTeam)abstractTeam).getPlayerList();
        for (String string : collection) {
            ServerPlayerEntity serverPlayerEntity = this.getPlayer(string);
            if (serverPlayerEntity == null || serverPlayerEntity == source) continue;
            serverPlayerEntity.sendMessage(message);
        }
    }

    public void sendToOtherTeams(PlayerEntity source, Text message) {
        Team abstractTeam = source.getScoreboardTeam();
        if (abstractTeam == null) {
            this.broadcast(message, false);
            return;
        }
        for (int i = 0; i < this.players.size(); ++i) {
            ServerPlayerEntity serverPlayerEntity = this.players.get(i);
            if (serverPlayerEntity.getScoreboardTeam() == abstractTeam) continue;
            serverPlayerEntity.sendMessage(message);
        }
    }

    public String[] getPlayerNames() {
        String[] strings = new String[this.players.size()];
        for (int i = 0; i < this.players.size(); ++i) {
            strings[i] = this.players.get(i).getGameProfile().name();
        }
        return strings;
    }

    public BannedPlayerList getUserBanList() {
        return this.bannedProfiles;
    }

    public BannedIpList getIpBanList() {
        return this.bannedIps;
    }

    public void addToOperators(PlayerConfigEntry player) {
        this.addToOperators(player, Optional.empty(), Optional.empty());
    }

    public void addToOperators(PlayerConfigEntry player, Optional<LeveledPermissionPredicate> permissionLevel, Optional<Boolean> canBypassPlayerLimit) {
        this.ops.add(new OperatorEntry(player, permissionLevel.orElse(this.server.getOpPermissionLevel()), canBypassPlayerLimit.orElse(this.ops.canBypassPlayerLimit(player))));
        ServerPlayerEntity serverPlayerEntity = this.getPlayer(player.id());
        if (serverPlayerEntity != null) {
            this.sendCommandTree(serverPlayerEntity);
        }
    }

    public void removeFromOperators(PlayerConfigEntry player) {
        ServerPlayerEntity serverPlayerEntity;
        if (this.ops.remove(player) && (serverPlayerEntity = this.getPlayer(player.id())) != null) {
            this.sendCommandTree(serverPlayerEntity);
        }
    }

    private void sendCommandTree(ServerPlayerEntity player, LeveledPermissionPredicate permissions) {
        if (player.networkHandler != null) {
            byte b = switch (permissions.getLevel()) {
                default -> throw new MatchException(null, null);
                case PermissionLevel.ALL -> 24;
                case PermissionLevel.MODERATORS -> 25;
                case PermissionLevel.GAMEMASTERS -> 26;
                case PermissionLevel.ADMINS -> 27;
                case PermissionLevel.OWNERS -> 28;
            };
            player.networkHandler.sendPacket(new EntityStatusS2CPacket(player, b));
        }
        this.server.getCommandManager().sendCommandTree(player);
    }

    public boolean isWhitelisted(PlayerConfigEntry player) {
        return !this.isWhitelistEnabled() || this.ops.contains(player) || this.whitelist.contains(player);
    }

    public boolean isOperator(PlayerConfigEntry player) {
        return this.ops.contains(player) || this.server.isHost(player) && this.server.getSaveProperties().areCommandsAllowed() || this.cheatsAllowed;
    }

    public @Nullable ServerPlayerEntity getPlayer(String name) {
        int i = this.players.size();
        for (int j = 0; j < i; ++j) {
            ServerPlayerEntity serverPlayerEntity = this.players.get(j);
            if (!serverPlayerEntity.getGameProfile().name().equalsIgnoreCase(name)) continue;
            return serverPlayerEntity;
        }
        return null;
    }

    public void sendToAround(@Nullable PlayerEntity player, double x, double y, double z, double distance, RegistryKey<World> worldKey, Packet<?> packet) {
        for (int i = 0; i < this.players.size(); ++i) {
            double f;
            double e;
            double d;
            ServerPlayerEntity serverPlayerEntity = this.players.get(i);
            if (serverPlayerEntity == player || serverPlayerEntity.getEntityWorld().getRegistryKey() != worldKey || !((d = x - serverPlayerEntity.getX()) * d + (e = y - serverPlayerEntity.getY()) * e + (f = z - serverPlayerEntity.getZ()) * f < distance * distance)) continue;
            serverPlayerEntity.networkHandler.sendPacket(packet);
        }
    }

    public void saveAllPlayerData() {
        for (int i = 0; i < this.players.size(); ++i) {
            this.savePlayerData(this.players.get(i));
        }
    }

    public Whitelist getWhitelist() {
        return this.whitelist;
    }

    public String[] getWhitelistedNames() {
        return this.whitelist.getNames();
    }

    public OperatorList getOpList() {
        return this.ops;
    }

    public String[] getOpNames() {
        return this.ops.getNames();
    }

    public void reloadWhitelist() {
    }

    public void sendWorldInfo(ServerPlayerEntity player, ServerWorld world) {
        WorldBorder worldBorder = world.getWorldBorder();
        player.networkHandler.sendPacket(new WorldBorderInitializeS2CPacket(worldBorder));
        player.networkHandler.sendPacket(new WorldTimeUpdateS2CPacket(world.getTime(), world.getTimeOfDay(), world.getGameRules().getValue(GameRules.ADVANCE_TIME)));
        player.networkHandler.sendPacket(new PlayerSpawnPositionS2CPacket(world.getSpawnPoint()));
        if (world.isRaining()) {
            player.networkHandler.sendPacket(new GameStateChangeS2CPacket(GameStateChangeS2CPacket.RAIN_STARTED, 0.0f));
            player.networkHandler.sendPacket(new GameStateChangeS2CPacket(GameStateChangeS2CPacket.RAIN_GRADIENT_CHANGED, world.getRainGradient(1.0f)));
            player.networkHandler.sendPacket(new GameStateChangeS2CPacket(GameStateChangeS2CPacket.THUNDER_GRADIENT_CHANGED, world.getThunderGradient(1.0f)));
        }
        player.networkHandler.sendPacket(new GameStateChangeS2CPacket(GameStateChangeS2CPacket.INITIAL_CHUNKS_COMING, 0.0f));
        this.server.getTickManager().sendPackets(player);
    }

    public void sendPlayerStatus(ServerPlayerEntity player) {
        player.playerScreenHandler.syncState();
        player.markHealthDirty();
        player.networkHandler.sendPacket(new UpdateSelectedSlotS2CPacket(player.getInventory().getSelectedSlot()));
    }

    public int getCurrentPlayerCount() {
        return this.players.size();
    }

    public int getMaxPlayerCount() {
        return this.server.getMaxPlayerCount();
    }

    public boolean isWhitelistEnabled() {
        return this.server.getUseAllowlist();
    }

    public List<ServerPlayerEntity> getPlayersByIp(String ip) {
        ArrayList list = Lists.newArrayList();
        for (ServerPlayerEntity serverPlayerEntity : this.players) {
            if (!serverPlayerEntity.getIp().equals(ip)) continue;
            list.add(serverPlayerEntity);
        }
        return list;
    }

    public int getViewDistance() {
        return this.viewDistance;
    }

    public int getSimulationDistance() {
        return this.simulationDistance;
    }

    public MinecraftServer getServer() {
        return this.server;
    }

    public @Nullable NbtCompound getUserData() {
        return null;
    }

    public void setCheatsAllowed(boolean cheatsAllowed) {
        this.cheatsAllowed = cheatsAllowed;
    }

    public void disconnectAllPlayers() {
        for (int i = 0; i < this.players.size(); ++i) {
            this.players.get((int)i).networkHandler.disconnect(Text.translatable("multiplayer.disconnect.server_shutdown"));
        }
    }

    public void broadcast(Text message, boolean overlay) {
        this.broadcast(message, (ServerPlayerEntity player) -> message, overlay);
    }

    public void broadcast(Text message, Function<ServerPlayerEntity, Text> playerMessageFactory, boolean overlay) {
        this.server.sendMessage(message);
        for (ServerPlayerEntity serverPlayerEntity : this.players) {
            Text text = playerMessageFactory.apply(serverPlayerEntity);
            if (text == null) continue;
            serverPlayerEntity.sendMessageToClient(text, overlay);
        }
    }

    public void broadcast(SignedMessage message, ServerCommandSource source, MessageType.Parameters params) {
        this.broadcast(message, source::shouldFilterText, source.getPlayer(), params);
    }

    public void broadcast(SignedMessage message, ServerPlayerEntity sender, MessageType.Parameters params) {
        this.broadcast(message, sender::shouldFilterMessagesSentTo, sender, params);
    }

    private void broadcast(SignedMessage message, Predicate<ServerPlayerEntity> shouldSendFiltered, @Nullable ServerPlayerEntity sender, MessageType.Parameters params) {
        boolean bl = this.verify(message);
        this.server.logChatMessage(message.getContent(), params, bl ? null : "Not Secure");
        SentMessage sentMessage = SentMessage.of(message);
        boolean bl2 = false;
        for (ServerPlayerEntity serverPlayerEntity : this.players) {
            boolean bl3 = shouldSendFiltered.test(serverPlayerEntity);
            serverPlayerEntity.sendChatMessage(sentMessage, bl3, params);
            bl2 |= bl3 && message.isFullyFiltered();
        }
        if (bl2 && sender != null) {
            sender.sendMessage(FILTERED_FULL_TEXT);
        }
    }

    private boolean verify(SignedMessage message) {
        return message.hasSignature() && !message.isExpiredOnServer(Instant.now());
    }

    public ServerStatHandler createStatHandler(PlayerEntity player) {
        GameProfile gameProfile = player.getGameProfile();
        return this.statisticsMap.computeIfAbsent(gameProfile.id(), uuid -> {
            Path path = this.locateStatFilePath(gameProfile);
            return new ServerStatHandler(this.server, path);
        });
    }

    private Path locateStatFilePath(GameProfile profile) {
        Path path3;
        Path path = this.server.getSavePath(WorldSavePath.STATS);
        Path path2 = path.resolve(String.valueOf(profile.id()) + ".json");
        if (Files.exists(path2, new LinkOption[0])) {
            return path2;
        }
        String string = profile.name() + ".json";
        if (PathUtil.isPathSegmentValid(string) && Files.isRegularFile(path3 = path.resolve(string), new LinkOption[0])) {
            try {
                return Files.move(path3, path2, new CopyOption[0]);
            }
            catch (IOException iOException) {
                LOGGER.warn("Failed to copy file {} to {}", (Object)string, (Object)path2);
                return path3;
            }
        }
        return path2;
    }

    public PlayerAdvancementTracker getAdvancementTracker(ServerPlayerEntity player) {
        UUID uUID = player.getUuid();
        PlayerAdvancementTracker playerAdvancementTracker = this.advancementTrackers.get(uUID);
        if (playerAdvancementTracker == null) {
            Path path = this.server.getSavePath(WorldSavePath.ADVANCEMENTS).resolve(String.valueOf(uUID) + ".json");
            playerAdvancementTracker = new PlayerAdvancementTracker(this.server.getDataFixer(), this, this.server.getAdvancementLoader(), path, player);
            this.advancementTrackers.put(uUID, playerAdvancementTracker);
        }
        playerAdvancementTracker.setOwner(player);
        return playerAdvancementTracker;
    }

    public void setViewDistance(int viewDistance) {
        this.viewDistance = viewDistance;
        this.sendToAll(new ChunkLoadDistanceS2CPacket(viewDistance));
        for (ServerWorld serverWorld : this.server.getWorlds()) {
            serverWorld.getChunkManager().applyViewDistance(viewDistance);
        }
    }

    public void setSimulationDistance(int simulationDistance) {
        this.simulationDistance = simulationDistance;
        this.sendToAll(new SimulationDistanceS2CPacket(simulationDistance));
        for (ServerWorld serverWorld : this.server.getWorlds()) {
            serverWorld.getChunkManager().applySimulationDistance(simulationDistance);
        }
    }

    public List<ServerPlayerEntity> getPlayerList() {
        return this.players;
    }

    public @Nullable ServerPlayerEntity getPlayer(UUID uuid) {
        return this.playerMap.get(uuid);
    }

    public @Nullable ServerPlayerEntity isAlreadyConnected(String playerName) {
        for (ServerPlayerEntity serverPlayerEntity : this.players) {
            if (!serverPlayerEntity.getGameProfile().name().equalsIgnoreCase(playerName)) continue;
            return serverPlayerEntity;
        }
        return null;
    }

    public boolean canBypassPlayerLimit(PlayerConfigEntry configEntry) {
        return false;
    }

    public void onDataPacksReloaded() {
        for (PlayerAdvancementTracker playerAdvancementTracker : this.advancementTrackers.values()) {
            playerAdvancementTracker.reload(this.server.getAdvancementLoader());
        }
        this.sendToAll(new SynchronizeTagsS2CPacket(TagPacketSerializer.serializeTags(this.registryManager)));
        ServerRecipeManager serverRecipeManager = this.server.getRecipeManager();
        SynchronizeRecipesS2CPacket synchronizeRecipesS2CPacket = new SynchronizeRecipesS2CPacket(serverRecipeManager.getPropertySets(), serverRecipeManager.getStonecutterRecipeForSync());
        for (ServerPlayerEntity serverPlayerEntity : this.players) {
            serverPlayerEntity.networkHandler.sendPacket(synchronizeRecipesS2CPacket);
            serverPlayerEntity.getRecipeBook().sendInitRecipesPacket(serverPlayerEntity);
        }
    }

    public boolean areCheatsAllowed() {
        return this.cheatsAllowed;
    }
}


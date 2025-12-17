/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  com.google.common.collect.Maps
 *  com.google.common.collect.Sets
 *  com.mojang.datafixers.kinds.App
 *  com.mojang.datafixers.kinds.Applicative
 *  com.mojang.serialization.Codec
 *  com.mojang.serialization.MapCodec
 *  com.mojang.serialization.codecs.RecordCodecBuilder
 *  org.jspecify.annotations.Nullable
 */
package net.minecraft.village.raid;

import com.google.common.collect.Maps;
import com.google.common.collect.Sets;
import com.mojang.datafixers.kinds.App;
import com.mojang.datafixers.kinds.Applicative;
import com.mojang.serialization.Codec;
import com.mojang.serialization.MapCodec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import java.util.Collection;
import java.util.Comparator;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Set;
import java.util.UUID;
import java.util.function.Predicate;
import java.util.stream.Stream;
import net.minecraft.SharedConstants;
import net.minecraft.advancement.criterion.Criteria;
import net.minecraft.block.Blocks;
import net.minecraft.block.entity.BannerPattern;
import net.minecraft.block.entity.BannerPatterns;
import net.minecraft.component.DataComponentTypes;
import net.minecraft.component.type.BannerPatternsComponent;
import net.minecraft.component.type.TooltipDisplayComponent;
import net.minecraft.entity.Entity;
import net.minecraft.entity.EntityType;
import net.minecraft.entity.EquipmentSlot;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.SpawnLocation;
import net.minecraft.entity.SpawnReason;
import net.minecraft.entity.SpawnRestriction;
import net.minecraft.entity.boss.BossBar;
import net.minecraft.entity.boss.ServerBossBar;
import net.minecraft.entity.effect.StatusEffectInstance;
import net.minecraft.entity.effect.StatusEffects;
import net.minecraft.entity.raid.RaiderEntity;
import net.minecraft.item.ItemStack;
import net.minecraft.item.Items;
import net.minecraft.network.packet.s2c.play.PlaySoundS2CPacket;
import net.minecraft.registry.RegistryEntryLookup;
import net.minecraft.registry.RegistryKeys;
import net.minecraft.screen.ScreenTexts;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.sound.SoundCategory;
import net.minecraft.sound.SoundEvents;
import net.minecraft.stat.Stats;
import net.minecraft.text.Text;
import net.minecraft.util.DyeColor;
import net.minecraft.util.Rarity;
import net.minecraft.util.StringIdentifiable;
import net.minecraft.util.Uuids;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.ChunkSectionPos;
import net.minecraft.util.math.MathHelper;
import net.minecraft.util.math.Vec3d;
import net.minecraft.util.math.random.Random;
import net.minecraft.world.Difficulty;
import net.minecraft.world.Heightmap;
import net.minecraft.world.LocalDifficulty;
import org.jspecify.annotations.Nullable;

public class Raid {
    public static final SpawnLocation RAVAGER_SPAWN_LOCATION = SpawnRestriction.getLocation(EntityType.RAVAGER);
    public static final MapCodec<Raid> CODEC = RecordCodecBuilder.mapCodec(instance -> instance.group((App)Codec.BOOL.fieldOf("started").forGetter(raid -> raid.started), (App)Codec.BOOL.fieldOf("active").forGetter(raid -> raid.active), (App)Codec.LONG.fieldOf("ticks_active").forGetter(raid -> raid.ticksActive), (App)Codec.INT.fieldOf("raid_omen_level").forGetter(raid -> raid.raidOmenLevel), (App)Codec.INT.fieldOf("groups_spawned").forGetter(raid -> raid.wavesSpawned), (App)Codec.INT.fieldOf("cooldown_ticks").forGetter(raid -> raid.preRaidTicks), (App)Codec.INT.fieldOf("post_raid_ticks").forGetter(raid -> raid.postRaidTicks), (App)Codec.FLOAT.fieldOf("total_health").forGetter(raid -> Float.valueOf(raid.totalHealth)), (App)Codec.INT.fieldOf("group_count").forGetter(raid -> raid.waveCount), (App)Status.CODEC.fieldOf("status").forGetter(raid -> raid.status), (App)BlockPos.CODEC.fieldOf("center").forGetter(raid -> raid.center), (App)Uuids.SET_CODEC.fieldOf("heroes_of_the_village").forGetter(raid -> raid.heroesOfTheVillage)).apply((Applicative)instance, Raid::new));
    private static final int field_53977 = 7;
    private static final int field_30676 = 2;
    private static final int field_30680 = 32;
    private static final int field_30681 = 48000;
    private static final int field_30682 = 5;
    private static final Text OMINOUS_BANNER_TRANSLATION_KEY = Text.translatable("block.minecraft.ominous_banner");
    private static final String RAIDERS_REMAINING_TRANSLATION_KEY = "event.minecraft.raid.raiders_remaining";
    public static final int field_30669 = 16;
    private static final int field_30685 = 40;
    private static final int DEFAULT_PRE_RAID_TICKS = 300;
    public static final int MAX_DESPAWN_COUNTER = 2400;
    public static final int field_30671 = 600;
    private static final int field_30687 = 30;
    public static final int field_30673 = 5;
    private static final int field_30688 = 2;
    private static final Text EVENT_TEXT = Text.translatable("event.minecraft.raid");
    private static final Text VICTORY_TITLE = Text.translatable("event.minecraft.raid.victory.full");
    private static final Text DEFEAT_TITLE = Text.translatable("event.minecraft.raid.defeat.full");
    private static final int MAX_ACTIVE_TICKS = 48000;
    private static final int field_53978 = 96;
    public static final int field_30674 = 9216;
    public static final int SQUARED_MAX_RAIDER_DISTANCE = 12544;
    private final Map<Integer, RaiderEntity> waveToCaptain = Maps.newHashMap();
    private final Map<Integer, Set<RaiderEntity>> waveToRaiders = Maps.newHashMap();
    private final Set<UUID> heroesOfTheVillage = Sets.newHashSet();
    private long ticksActive;
    private BlockPos center;
    private boolean started;
    private float totalHealth;
    private int raidOmenLevel;
    private boolean active;
    private int wavesSpawned;
    private final ServerBossBar bar = new ServerBossBar(EVENT_TEXT, BossBar.Color.RED, BossBar.Style.NOTCHED_10);
    private int postRaidTicks;
    private int preRaidTicks;
    private final Random random = Random.create();
    private final int waveCount;
    private Status status;
    private int finishCooldown;
    private Optional<BlockPos> preCalculatedRaidersSpawnLocation = Optional.empty();

    public Raid(BlockPos center, Difficulty difficulty) {
        this.active = true;
        this.preRaidTicks = 300;
        this.bar.setPercent(0.0f);
        this.center = center;
        this.waveCount = this.getMaxWaves(difficulty);
        this.status = Status.ONGOING;
    }

    private Raid(boolean started, boolean active, long ticksActive, int raidOmenLevel, int wavesSpawned, int preRaidTicks, int postRaidTicks, float totalHealth, int waveCount, Status status, BlockPos center, Set<UUID> heroesOfTheVillage) {
        this.started = started;
        this.active = active;
        this.ticksActive = ticksActive;
        this.raidOmenLevel = raidOmenLevel;
        this.wavesSpawned = wavesSpawned;
        this.preRaidTicks = preRaidTicks;
        this.postRaidTicks = postRaidTicks;
        this.totalHealth = totalHealth;
        this.center = center;
        this.waveCount = waveCount;
        this.status = status;
        this.heroesOfTheVillage.addAll(heroesOfTheVillage);
    }

    public boolean isFinished() {
        return this.hasWon() || this.hasLost();
    }

    public boolean isPreRaid() {
        return this.hasSpawned() && this.getRaiderCount() == 0 && this.preRaidTicks > 0;
    }

    public boolean hasSpawned() {
        return this.wavesSpawned > 0;
    }

    public boolean hasStopped() {
        return this.status == Status.STOPPED;
    }

    public boolean hasWon() {
        return this.status == Status.VICTORY;
    }

    public boolean hasLost() {
        return this.status == Status.LOSS;
    }

    public float getTotalHealth() {
        return this.totalHealth;
    }

    public Set<RaiderEntity> getAllRaiders() {
        HashSet set = Sets.newHashSet();
        for (Set<RaiderEntity> set2 : this.waveToRaiders.values()) {
            set.addAll(set2);
        }
        return set;
    }

    public boolean hasStarted() {
        return this.started;
    }

    public int getGroupsSpawned() {
        return this.wavesSpawned;
    }

    private Predicate<ServerPlayerEntity> isInRaidDistance() {
        return player -> {
            BlockPos blockPos = player.getBlockPos();
            return player.isAlive() && player.getEntityWorld().getRaidAt(blockPos) == this;
        };
    }

    private void updateBarToPlayers(ServerWorld world) {
        HashSet set = Sets.newHashSet(this.bar.getPlayers());
        List<ServerPlayerEntity> list = world.getPlayers(this.isInRaidDistance());
        for (ServerPlayerEntity serverPlayerEntity : list) {
            if (set.contains(serverPlayerEntity)) continue;
            this.bar.addPlayer(serverPlayerEntity);
        }
        for (ServerPlayerEntity serverPlayerEntity : set) {
            if (list.contains(serverPlayerEntity)) continue;
            this.bar.removePlayer(serverPlayerEntity);
        }
    }

    public int getMaxAcceptableBadOmenLevel() {
        return 5;
    }

    public int getBadOmenLevel() {
        return this.raidOmenLevel;
    }

    public void setBadOmenLevel(int badOmenLevel) {
        this.raidOmenLevel = badOmenLevel;
    }

    public boolean start(ServerPlayerEntity player) {
        StatusEffectInstance statusEffectInstance = player.getStatusEffect(StatusEffects.RAID_OMEN);
        if (statusEffectInstance == null) {
            return false;
        }
        this.raidOmenLevel += statusEffectInstance.getAmplifier() + 1;
        this.raidOmenLevel = MathHelper.clamp(this.raidOmenLevel, 0, this.getMaxAcceptableBadOmenLevel());
        if (!this.hasSpawned()) {
            player.incrementStat(Stats.RAID_TRIGGER);
            Criteria.VOLUNTARY_EXILE.trigger(player);
        }
        return true;
    }

    public void invalidate() {
        this.active = false;
        this.bar.clearPlayers();
        this.status = Status.STOPPED;
    }

    public void tick(ServerWorld world) {
        if (this.hasStopped()) {
            return;
        }
        if (this.status == Status.ONGOING) {
            boolean bl2;
            boolean bl = this.active;
            this.active = world.isChunkLoaded(this.center);
            if (world.getDifficulty() == Difficulty.PEACEFUL) {
                this.invalidate();
                return;
            }
            if (bl != this.active) {
                this.bar.setVisible(this.active);
            }
            if (!this.active) {
                return;
            }
            if (!world.isNearOccupiedPointOfInterest(this.center)) {
                this.moveRaidCenter(world);
            }
            if (!world.isNearOccupiedPointOfInterest(this.center)) {
                if (this.wavesSpawned > 0) {
                    this.status = Status.LOSS;
                } else {
                    this.invalidate();
                }
            }
            ++this.ticksActive;
            if (this.ticksActive >= 48000L) {
                this.invalidate();
                return;
            }
            int i = this.getRaiderCount();
            if (i == 0 && this.shouldSpawnMoreGroups()) {
                if (this.preRaidTicks > 0) {
                    boolean bl3;
                    bl2 = this.preCalculatedRaidersSpawnLocation.isPresent();
                    boolean bl4 = bl3 = !bl2 && this.preRaidTicks % 5 == 0;
                    if (bl2 && !world.shouldTickEntityAt(this.preCalculatedRaidersSpawnLocation.get())) {
                        bl3 = true;
                    }
                    if (bl3) {
                        this.preCalculatedRaidersSpawnLocation = this.getRaidersSpawnLocation(world);
                    }
                    if (this.preRaidTicks == 300 || this.preRaidTicks % 20 == 0) {
                        this.updateBarToPlayers(world);
                    }
                    --this.preRaidTicks;
                    this.bar.setPercent(MathHelper.clamp((float)(300 - this.preRaidTicks) / 300.0f, 0.0f, 1.0f));
                } else if (this.preRaidTicks == 0 && this.wavesSpawned > 0) {
                    this.preRaidTicks = 300;
                    this.bar.setName(EVENT_TEXT);
                    return;
                }
            }
            if (this.ticksActive % 20L == 0L) {
                this.updateBarToPlayers(world);
                this.removeObsoleteRaiders(world);
                if (i > 0) {
                    if (i <= 2) {
                        this.bar.setName(EVENT_TEXT.copy().append(" - ").append(Text.translatable(RAIDERS_REMAINING_TRANSLATION_KEY, i)));
                    } else {
                        this.bar.setName(EVENT_TEXT);
                    }
                } else {
                    this.bar.setName(EVENT_TEXT);
                }
            }
            if (SharedConstants.RAIDS) {
                this.bar.setName(EVENT_TEXT.copy().append(" wave: ").append("" + this.wavesSpawned).append(ScreenTexts.SPACE).append("Raiders alive: ").append("" + this.getRaiderCount()).append(ScreenTexts.SPACE).append("" + this.getCurrentRaiderHealth()).append(" / ").append("" + this.totalHealth).append(" Is bonus? ").append("" + (this.hasExtraWave() && this.hasSpawnedExtraWave())).append(" Status: ").append(this.status.asString()));
            }
            bl2 = false;
            int j = 0;
            while (this.canSpawnRaiders()) {
                BlockPos blockPos = this.preCalculatedRaidersSpawnLocation.orElseGet(() -> this.findRandomRaidersSpawnLocation(world, 20));
                if (blockPos != null) {
                    this.started = true;
                    this.spawnNextWave(world, blockPos);
                    if (!bl2) {
                        this.playRaidHorn(world, blockPos);
                        bl2 = true;
                    }
                } else {
                    ++j;
                }
                if (j <= 5) continue;
                this.invalidate();
                break;
            }
            if (this.hasStarted() && !this.shouldSpawnMoreGroups() && i == 0) {
                if (this.postRaidTicks < 40) {
                    ++this.postRaidTicks;
                } else {
                    this.status = Status.VICTORY;
                    for (UUID uUID : this.heroesOfTheVillage) {
                        Entity entity = world.getEntity(uUID);
                        if (!(entity instanceof LivingEntity)) continue;
                        LivingEntity livingEntity = (LivingEntity)entity;
                        if (entity.isSpectator()) continue;
                        livingEntity.addStatusEffect(new StatusEffectInstance(StatusEffects.HERO_OF_THE_VILLAGE, 48000, this.raidOmenLevel - 1, false, false, true));
                        if (!(livingEntity instanceof ServerPlayerEntity)) continue;
                        ServerPlayerEntity serverPlayerEntity = (ServerPlayerEntity)livingEntity;
                        serverPlayerEntity.incrementStat(Stats.RAID_WIN);
                        Criteria.HERO_OF_THE_VILLAGE.trigger(serverPlayerEntity);
                    }
                }
            }
            this.markDirty(world);
        } else if (this.isFinished()) {
            ++this.finishCooldown;
            if (this.finishCooldown >= 600) {
                this.invalidate();
                return;
            }
            if (this.finishCooldown % 20 == 0) {
                this.updateBarToPlayers(world);
                this.bar.setVisible(true);
                if (this.hasWon()) {
                    this.bar.setPercent(0.0f);
                    this.bar.setName(VICTORY_TITLE);
                } else {
                    this.bar.setName(DEFEAT_TITLE);
                }
            }
        }
    }

    private void moveRaidCenter(ServerWorld world) {
        Stream<ChunkSectionPos> stream = ChunkSectionPos.stream(ChunkSectionPos.from(this.center), 2);
        stream.filter(world::isNearOccupiedPointOfInterest).map(ChunkSectionPos::getCenterPos).min(Comparator.comparingDouble(pos -> pos.getSquaredDistance(this.center))).ifPresent(this::setCenter);
    }

    private Optional<BlockPos> getRaidersSpawnLocation(ServerWorld world) {
        BlockPos blockPos = this.findRandomRaidersSpawnLocation(world, 8);
        if (blockPos != null) {
            return Optional.of(blockPos);
        }
        return Optional.empty();
    }

    private boolean shouldSpawnMoreGroups() {
        if (this.hasExtraWave()) {
            return !this.hasSpawnedExtraWave();
        }
        return !this.hasSpawnedFinalWave();
    }

    private boolean hasSpawnedFinalWave() {
        return this.getGroupsSpawned() == this.waveCount;
    }

    private boolean hasExtraWave() {
        return this.raidOmenLevel > 1;
    }

    private boolean hasSpawnedExtraWave() {
        return this.getGroupsSpawned() > this.waveCount;
    }

    private boolean isSpawningExtraWave() {
        return this.hasSpawnedFinalWave() && this.getRaiderCount() == 0 && this.hasExtraWave();
    }

    private void removeObsoleteRaiders(ServerWorld world) {
        Iterator<Set<RaiderEntity>> iterator = this.waveToRaiders.values().iterator();
        HashSet set = Sets.newHashSet();
        while (iterator.hasNext()) {
            Set<RaiderEntity> set2 = iterator.next();
            for (RaiderEntity raiderEntity : set2) {
                BlockPos blockPos = raiderEntity.getBlockPos();
                if (raiderEntity.isRemoved() || raiderEntity.getEntityWorld().getRegistryKey() != world.getRegistryKey() || this.center.getSquaredDistance(blockPos) >= 12544.0) {
                    set.add(raiderEntity);
                    continue;
                }
                if (raiderEntity.age <= 600) continue;
                if (world.getEntity(raiderEntity.getUuid()) == null) {
                    set.add(raiderEntity);
                }
                if (!world.isNearOccupiedPointOfInterest(blockPos) && raiderEntity.getDespawnCounter() > 2400) {
                    raiderEntity.setOutOfRaidCounter(raiderEntity.getOutOfRaidCounter() + 1);
                }
                if (raiderEntity.getOutOfRaidCounter() < 30) continue;
                set.add(raiderEntity);
            }
        }
        for (RaiderEntity raiderEntity2 : set) {
            this.removeFromWave(world, raiderEntity2, true);
            if (!raiderEntity2.isPatrolLeader()) continue;
            this.removeLeader(raiderEntity2.getWave());
        }
    }

    private void playRaidHorn(ServerWorld world, BlockPos pos) {
        float f = 13.0f;
        int i = 64;
        Collection<ServerPlayerEntity> collection = this.bar.getPlayers();
        long l = this.random.nextLong();
        for (ServerPlayerEntity serverPlayerEntity : world.getPlayers()) {
            Vec3d vec3d = serverPlayerEntity.getEntityPos();
            Vec3d vec3d2 = Vec3d.ofCenter(pos);
            double d = Math.sqrt((vec3d2.x - vec3d.x) * (vec3d2.x - vec3d.x) + (vec3d2.z - vec3d.z) * (vec3d2.z - vec3d.z));
            double e = vec3d.x + 13.0 / d * (vec3d2.x - vec3d.x);
            double g = vec3d.z + 13.0 / d * (vec3d2.z - vec3d.z);
            if (!(d <= 64.0) && !collection.contains(serverPlayerEntity)) continue;
            serverPlayerEntity.networkHandler.sendPacket(new PlaySoundS2CPacket(SoundEvents.EVENT_RAID_HORN, SoundCategory.NEUTRAL, e, serverPlayerEntity.getY(), g, 64.0f, 1.0f, l));
        }
    }

    private void spawnNextWave(ServerWorld world, BlockPos pos) {
        boolean bl = false;
        int i = this.wavesSpawned + 1;
        this.totalHealth = 0.0f;
        LocalDifficulty localDifficulty = world.getLocalDifficulty(pos);
        boolean bl2 = this.isSpawningExtraWave();
        for (Member member : Member.VALUES) {
            RaiderEntity raiderEntity;
            int j = this.getCount(member, i, bl2) + this.getBonusCount(member, this.random, i, localDifficulty, bl2);
            int k = 0;
            for (int l = 0; l < j && (raiderEntity = member.type.create(world, SpawnReason.EVENT)) != null; ++l) {
                if (!bl && raiderEntity.canLead()) {
                    raiderEntity.setPatrolLeader(true);
                    this.setWaveCaptain(i, raiderEntity);
                    bl = true;
                }
                this.addRaider(world, i, raiderEntity, pos, false);
                if (member.type != EntityType.RAVAGER) continue;
                RaiderEntity raiderEntity2 = null;
                if (i == this.getMaxWaves(Difficulty.NORMAL)) {
                    raiderEntity2 = EntityType.PILLAGER.create(world, SpawnReason.EVENT);
                } else if (i >= this.getMaxWaves(Difficulty.HARD)) {
                    raiderEntity2 = k == 0 ? (RaiderEntity)EntityType.EVOKER.create(world, SpawnReason.EVENT) : (RaiderEntity)EntityType.VINDICATOR.create(world, SpawnReason.EVENT);
                }
                ++k;
                if (raiderEntity2 == null) continue;
                this.addRaider(world, i, raiderEntity2, pos, false);
                raiderEntity2.refreshPositionAndAngles(pos, 0.0f, 0.0f);
                raiderEntity2.startRiding(raiderEntity, false, false);
            }
        }
        this.preCalculatedRaidersSpawnLocation = Optional.empty();
        ++this.wavesSpawned;
        this.updateBar();
        this.markDirty(world);
    }

    public void addRaider(ServerWorld world, int wave, RaiderEntity raider, @Nullable BlockPos pos, boolean existing) {
        boolean bl = this.addToWave(world, wave, raider);
        if (bl) {
            raider.setRaid(this);
            raider.setWave(wave);
            raider.setAbleToJoinRaid(true);
            raider.setOutOfRaidCounter(0);
            if (!existing && pos != null) {
                raider.setPosition((double)pos.getX() + 0.5, (double)pos.getY() + 1.0, (double)pos.getZ() + 0.5);
                raider.initialize(world, world.getLocalDifficulty(pos), SpawnReason.EVENT, null);
                raider.addBonusForWave(world, wave, false);
                raider.setOnGround(true);
                world.spawnEntityAndPassengers(raider);
            }
        }
    }

    public void updateBar() {
        this.bar.setPercent(MathHelper.clamp(this.getCurrentRaiderHealth() / this.totalHealth, 0.0f, 1.0f));
    }

    public float getCurrentRaiderHealth() {
        float f = 0.0f;
        for (Set<RaiderEntity> set : this.waveToRaiders.values()) {
            for (RaiderEntity raiderEntity : set) {
                f += raiderEntity.getHealth();
            }
        }
        return f;
    }

    private boolean canSpawnRaiders() {
        return this.preRaidTicks == 0 && (this.wavesSpawned < this.waveCount || this.isSpawningExtraWave()) && this.getRaiderCount() == 0;
    }

    public int getRaiderCount() {
        return this.waveToRaiders.values().stream().mapToInt(Set::size).sum();
    }

    public void removeFromWave(ServerWorld world, RaiderEntity raider, boolean countHealth) {
        boolean bl;
        Set<RaiderEntity> set = this.waveToRaiders.get(raider.getWave());
        if (set != null && (bl = set.remove(raider))) {
            if (countHealth) {
                this.totalHealth -= raider.getHealth();
            }
            raider.setRaid(null);
            this.updateBar();
            this.markDirty(world);
        }
    }

    private void markDirty(ServerWorld world) {
        world.getRaidManager().markDirty();
    }

    public static ItemStack createOminousBanner(RegistryEntryLookup<BannerPattern> bannerPatternLookup) {
        ItemStack itemStack = new ItemStack(Items.WHITE_BANNER);
        BannerPatternsComponent bannerPatternsComponent = new BannerPatternsComponent.Builder().add(bannerPatternLookup, BannerPatterns.RHOMBUS, DyeColor.CYAN).add(bannerPatternLookup, BannerPatterns.STRIPE_BOTTOM, DyeColor.LIGHT_GRAY).add(bannerPatternLookup, BannerPatterns.STRIPE_CENTER, DyeColor.GRAY).add(bannerPatternLookup, BannerPatterns.BORDER, DyeColor.LIGHT_GRAY).add(bannerPatternLookup, BannerPatterns.STRIPE_MIDDLE, DyeColor.BLACK).add(bannerPatternLookup, BannerPatterns.HALF_HORIZONTAL, DyeColor.LIGHT_GRAY).add(bannerPatternLookup, BannerPatterns.CIRCLE, DyeColor.LIGHT_GRAY).add(bannerPatternLookup, BannerPatterns.BORDER, DyeColor.BLACK).build();
        itemStack.set(DataComponentTypes.BANNER_PATTERNS, bannerPatternsComponent);
        itemStack.set(DataComponentTypes.TOOLTIP_DISPLAY, TooltipDisplayComponent.DEFAULT.with(DataComponentTypes.BANNER_PATTERNS, true));
        itemStack.set(DataComponentTypes.ITEM_NAME, OMINOUS_BANNER_TRANSLATION_KEY);
        itemStack.set(DataComponentTypes.RARITY, Rarity.UNCOMMON);
        return itemStack;
    }

    public @Nullable RaiderEntity getCaptain(int wave) {
        return this.waveToCaptain.get(wave);
    }

    private @Nullable BlockPos findRandomRaidersSpawnLocation(ServerWorld world, int proximity) {
        int i = this.preRaidTicks / 20;
        float f = 0.22f * (float)i - 0.24f;
        BlockPos.Mutable mutable = new BlockPos.Mutable();
        float g = world.random.nextFloat() * ((float)Math.PI * 2);
        for (int j = 0; j < proximity; ++j) {
            int l;
            float h = g + (float)Math.PI * (float)j / 8.0f;
            int k = this.center.getX() + MathHelper.floor(MathHelper.cos(h) * 32.0f * f) + world.random.nextInt(3) * MathHelper.floor(f);
            int m = world.getTopY(Heightmap.Type.WORLD_SURFACE, k, l = this.center.getZ() + MathHelper.floor(MathHelper.sin(h) * 32.0f * f) + world.random.nextInt(3) * MathHelper.floor(f));
            if (MathHelper.abs(m - this.center.getY()) > 96) continue;
            mutable.set(k, m, l);
            if (world.isNearOccupiedPointOfInterest(mutable) && i > 7) continue;
            int n = 10;
            if (!world.isRegionLoaded(mutable.getX() - 10, mutable.getZ() - 10, mutable.getX() + 10, mutable.getZ() + 10) || !world.shouldTickEntityAt(mutable) || !RAVAGER_SPAWN_LOCATION.isSpawnPositionOk(world, mutable, EntityType.RAVAGER) && (!world.getBlockState((BlockPos)mutable.down()).isOf(Blocks.SNOW) || !world.getBlockState(mutable).isAir())) continue;
            return mutable;
        }
        return null;
    }

    private boolean addToWave(ServerWorld world, int wave, RaiderEntity raider) {
        return this.addToWave(world, wave, raider, true);
    }

    public boolean addToWave(ServerWorld world, int wave, RaiderEntity raider, boolean countHealth) {
        this.waveToRaiders.computeIfAbsent(wave, wavex -> Sets.newHashSet());
        Set<RaiderEntity> set = this.waveToRaiders.get(wave);
        RaiderEntity raiderEntity = null;
        for (RaiderEntity raiderEntity2 : set) {
            if (!raiderEntity2.getUuid().equals(raider.getUuid())) continue;
            raiderEntity = raiderEntity2;
            break;
        }
        if (raiderEntity != null) {
            set.remove(raiderEntity);
            set.add(raider);
        }
        set.add(raider);
        if (countHealth) {
            this.totalHealth += raider.getHealth();
        }
        this.updateBar();
        this.markDirty(world);
        return true;
    }

    public void setWaveCaptain(int wave, RaiderEntity entity) {
        this.waveToCaptain.put(wave, entity);
        entity.equipStack(EquipmentSlot.HEAD, Raid.createOminousBanner(entity.getRegistryManager().getOrThrow(RegistryKeys.BANNER_PATTERN)));
        entity.setEquipmentDropChance(EquipmentSlot.HEAD, 2.0f);
    }

    public void removeLeader(int wave) {
        this.waveToCaptain.remove(wave);
    }

    public BlockPos getCenter() {
        return this.center;
    }

    private void setCenter(BlockPos center) {
        this.center = center;
    }

    private int getCount(Member member, int wave, boolean extra) {
        return extra ? member.countInWave[this.waveCount] : member.countInWave[wave];
    }

    private int getBonusCount(Member member, Random random, int wave, LocalDifficulty localDifficulty, boolean extra) {
        int i;
        Difficulty difficulty = localDifficulty.getGlobalDifficulty();
        boolean bl = difficulty == Difficulty.EASY;
        boolean bl2 = difficulty == Difficulty.NORMAL;
        switch (member.ordinal()) {
            case 3: {
                if (!bl && wave > 2 && wave != 4) {
                    i = 1;
                    break;
                }
                return 0;
            }
            case 0: 
            case 2: {
                if (bl) {
                    i = random.nextInt(2);
                    break;
                }
                if (bl2) {
                    i = 1;
                    break;
                }
                i = 2;
                break;
            }
            case 4: {
                i = !bl && extra ? 1 : 0;
                break;
            }
            default: {
                return 0;
            }
        }
        return i > 0 ? random.nextInt(i + 1) : 0;
    }

    public boolean isActive() {
        return this.active;
    }

    public int getMaxWaves(Difficulty difficulty) {
        return switch (difficulty) {
            default -> throw new MatchException(null, null);
            case Difficulty.PEACEFUL -> 0;
            case Difficulty.EASY -> 3;
            case Difficulty.NORMAL -> 5;
            case Difficulty.HARD -> 7;
        };
    }

    public float getEnchantmentChance() {
        int i = this.getBadOmenLevel();
        if (i == 2) {
            return 0.1f;
        }
        if (i == 3) {
            return 0.25f;
        }
        if (i == 4) {
            return 0.5f;
        }
        if (i == 5) {
            return 0.75f;
        }
        return 0.0f;
    }

    public void addHero(Entity entity) {
        this.heroesOfTheVillage.add(entity.getUuid());
    }

    static final class Status
    extends Enum<Status>
    implements StringIdentifiable {
        public static final /* enum */ Status ONGOING = new Status("ongoing");
        public static final /* enum */ Status VICTORY = new Status("victory");
        public static final /* enum */ Status LOSS = new Status("loss");
        public static final /* enum */ Status STOPPED = new Status("stopped");
        public static final Codec<Status> CODEC;
        private final String id;
        private static final /* synthetic */ Status[] field_19031;

        public static Status[] values() {
            return (Status[])field_19031.clone();
        }

        public static Status valueOf(String string) {
            return Enum.valueOf(Status.class, string);
        }

        private Status(String id) {
            this.id = id;
        }

        @Override
        public String asString() {
            return this.id;
        }

        private static /* synthetic */ Status[] method_36666() {
            return new Status[]{ONGOING, VICTORY, LOSS, STOPPED};
        }

        static {
            field_19031 = Status.method_36666();
            CODEC = StringIdentifiable.createCodec(Status::values);
        }
    }

    static final class Member
    extends Enum<Member> {
        public static final /* enum */ Member VINDICATOR = new Member(EntityType.VINDICATOR, new int[]{0, 0, 2, 0, 1, 4, 2, 5});
        public static final /* enum */ Member EVOKER = new Member(EntityType.EVOKER, new int[]{0, 0, 0, 0, 0, 1, 1, 2});
        public static final /* enum */ Member PILLAGER = new Member(EntityType.PILLAGER, new int[]{0, 4, 3, 3, 4, 4, 4, 2});
        public static final /* enum */ Member WITCH = new Member(EntityType.WITCH, new int[]{0, 0, 0, 0, 3, 0, 0, 1});
        public static final /* enum */ Member RAVAGER = new Member(EntityType.RAVAGER, new int[]{0, 0, 0, 1, 0, 1, 0, 2});
        static final Member[] VALUES;
        final EntityType<? extends RaiderEntity> type;
        final int[] countInWave;
        private static final /* synthetic */ Member[] field_16632;

        public static Member[] values() {
            return (Member[])field_16632.clone();
        }

        public static Member valueOf(String string) {
            return Enum.valueOf(Member.class, string);
        }

        private Member(EntityType<? extends RaiderEntity> type, int[] countInWave) {
            this.type = type;
            this.countInWave = countInWave;
        }

        private static /* synthetic */ Member[] method_36667() {
            return new Member[]{VINDICATOR, EVOKER, PILLAGER, WITCH, RAVAGER};
        }

        static {
            field_16632 = Member.method_36667();
            VALUES = Member.values();
        }
    }
}


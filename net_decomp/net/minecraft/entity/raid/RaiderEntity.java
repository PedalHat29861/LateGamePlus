/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  com.google.common.collect.Lists
 *  it.unimi.dsi.fastutil.ints.Int2LongOpenHashMap
 *  org.jspecify.annotations.Nullable
 */
package net.minecraft.entity.raid;

import com.google.common.collect.Lists;
import it.unimi.dsi.fastutil.ints.Int2LongOpenHashMap;
import java.util.EnumSet;
import java.util.List;
import java.util.Objects;
import java.util.Optional;
import java.util.function.Predicate;
import net.minecraft.entity.Entity;
import net.minecraft.entity.EntityData;
import net.minecraft.entity.EntityType;
import net.minecraft.entity.EquipmentSlot;
import net.minecraft.entity.ItemEntity;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.SpawnReason;
import net.minecraft.entity.ai.NoPenaltyTargeting;
import net.minecraft.entity.ai.TargetPredicate;
import net.minecraft.entity.ai.goal.Goal;
import net.minecraft.entity.ai.goal.MoveToRaidCenterGoal;
import net.minecraft.entity.ai.pathing.Path;
import net.minecraft.entity.attribute.EntityAttributes;
import net.minecraft.entity.damage.DamageSource;
import net.minecraft.entity.data.DataTracker;
import net.minecraft.entity.data.TrackedData;
import net.minecraft.entity.data.TrackedDataHandlerRegistry;
import net.minecraft.entity.mob.IllagerEntity;
import net.minecraft.entity.mob.MobEntity;
import net.minecraft.entity.mob.PatrolEntity;
import net.minecraft.item.ItemStack;
import net.minecraft.registry.RegistryKeys;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.sound.SoundEvent;
import net.minecraft.storage.ReadView;
import net.minecraft.storage.WriteView;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Vec3d;
import net.minecraft.village.raid.Raid;
import net.minecraft.village.raid.RaidManager;
import net.minecraft.world.LocalDifficulty;
import net.minecraft.world.ServerWorldAccess;
import net.minecraft.world.World;
import net.minecraft.world.poi.PointOfInterestStorage;
import net.minecraft.world.poi.PointOfInterestTypes;
import org.jspecify.annotations.Nullable;

public abstract class RaiderEntity
extends PatrolEntity {
    protected static final TrackedData<Boolean> CELEBRATING = DataTracker.registerData(RaiderEntity.class, TrackedDataHandlerRegistry.BOOLEAN);
    static final Predicate<ItemEntity> OBTAINABLE_OMINOUS_BANNER_PREDICATE = itemEntity -> !itemEntity.cannotPickup() && itemEntity.isAlive() && ItemStack.areEqual(itemEntity.getStack(), Raid.createOminousBanner(itemEntity.getRegistryManager().getOrThrow(RegistryKeys.BANNER_PATTERN)));
    private static final int DEFAULT_WAVE = 0;
    private static final boolean DEFAULT_ABLE_TO_JOIN_RAID = false;
    protected @Nullable Raid raid;
    private int wave = 0;
    private boolean ableToJoinRaid = false;
    private int outOfRaidCounter;

    protected RaiderEntity(EntityType<? extends RaiderEntity> entityType, World world) {
        super((EntityType<? extends PatrolEntity>)entityType, world);
    }

    @Override
    protected void initGoals() {
        super.initGoals();
        this.goalSelector.add(1, new PickUpBannerAsLeaderGoal(this, this));
        this.goalSelector.add(3, new MoveToRaidCenterGoal<RaiderEntity>(this));
        this.goalSelector.add(4, new AttackHomeGoal(this, 1.05f, 1));
        this.goalSelector.add(5, new CelebrateGoal(this));
    }

    @Override
    protected void initDataTracker(DataTracker.Builder builder) {
        super.initDataTracker(builder);
        builder.add(CELEBRATING, false);
    }

    public abstract void addBonusForWave(ServerWorld var1, int var2, boolean var3);

    public boolean canJoinRaid() {
        return this.ableToJoinRaid;
    }

    public void setAbleToJoinRaid(boolean ableToJoinRaid) {
        this.ableToJoinRaid = ableToJoinRaid;
    }

    @Override
    public void tickMovement() {
        World world = this.getEntityWorld();
        if (world instanceof ServerWorld) {
            ServerWorld serverWorld = (ServerWorld)world;
            if (this.isAlive()) {
                Raid raid = this.getRaid();
                if (this.canJoinRaid()) {
                    if (raid == null) {
                        Raid raid2;
                        if (this.getEntityWorld().getTime() % 20L == 0L && (raid2 = serverWorld.getRaidAt(this.getBlockPos())) != null && RaidManager.isValidRaiderFor(this)) {
                            raid2.addRaider(serverWorld, raid2.getGroupsSpawned(), this, null, true);
                        }
                    } else {
                        LivingEntity livingEntity = this.getTarget();
                        if (livingEntity != null && (livingEntity.getType() == EntityType.PLAYER || livingEntity.getType() == EntityType.IRON_GOLEM)) {
                            this.despawnCounter = 0;
                        }
                    }
                }
            }
        }
        super.tickMovement();
    }

    @Override
    protected void updateDespawnCounter() {
        this.despawnCounter += 2;
    }

    @Override
    public void onDeath(DamageSource damageSource) {
        World world = this.getEntityWorld();
        if (world instanceof ServerWorld) {
            ServerWorld serverWorld = (ServerWorld)world;
            Entity entity = damageSource.getAttacker();
            Raid raid = this.getRaid();
            if (raid != null) {
                if (this.isPatrolLeader()) {
                    raid.removeLeader(this.getWave());
                }
                if (entity != null && entity.getType() == EntityType.PLAYER) {
                    raid.addHero(entity);
                }
                raid.removeFromWave(serverWorld, this, false);
            }
        }
        super.onDeath(damageSource);
    }

    @Override
    public boolean hasNoRaid() {
        return !this.hasActiveRaid();
    }

    public void setRaid(@Nullable Raid raid) {
        this.raid = raid;
    }

    public @Nullable Raid getRaid() {
        return this.raid;
    }

    public boolean isCaptain() {
        ItemStack itemStack = this.getEquippedStack(EquipmentSlot.HEAD);
        boolean bl = !itemStack.isEmpty() && ItemStack.areEqual(itemStack, Raid.createOminousBanner(this.getRegistryManager().getOrThrow(RegistryKeys.BANNER_PATTERN)));
        boolean bl2 = this.isPatrolLeader();
        return bl && bl2;
    }

    public boolean hasRaid() {
        World world = this.getEntityWorld();
        if (!(world instanceof ServerWorld)) {
            return false;
        }
        ServerWorld serverWorld = (ServerWorld)world;
        return this.getRaid() != null || serverWorld.getRaidAt(this.getBlockPos()) != null;
    }

    public boolean hasActiveRaid() {
        return this.getRaid() != null && this.getRaid().isActive();
    }

    public void setWave(int wave) {
        this.wave = wave;
    }

    public int getWave() {
        return this.wave;
    }

    public boolean isCelebrating() {
        return this.dataTracker.get(CELEBRATING);
    }

    public void setCelebrating(boolean celebrating) {
        this.dataTracker.set(CELEBRATING, celebrating);
    }

    @Override
    protected void writeCustomData(WriteView view) {
        World world;
        super.writeCustomData(view);
        view.putInt("Wave", this.wave);
        view.putBoolean("CanJoinRaid", this.ableToJoinRaid);
        if (this.raid != null && (world = this.getEntityWorld()) instanceof ServerWorld) {
            ServerWorld serverWorld = (ServerWorld)world;
            serverWorld.getRaidManager().getRaidId(this.raid).ifPresent(raidId -> view.putInt("RaidId", raidId));
        }
    }

    @Override
    protected void readCustomData(ReadView view) {
        super.readCustomData(view);
        this.wave = view.getInt("Wave", 0);
        this.ableToJoinRaid = view.getBoolean("CanJoinRaid", false);
        World world = this.getEntityWorld();
        if (world instanceof ServerWorld) {
            ServerWorld serverWorld = (ServerWorld)world;
            view.getOptionalInt("RaidId").ifPresent(raidId -> {
                this.raid = serverWorld.getRaidManager().getRaid((int)raidId);
                if (this.raid != null) {
                    this.raid.addToWave(serverWorld, this.wave, this, false);
                    if (this.isPatrolLeader()) {
                        this.raid.setWaveCaptain(this.wave, this);
                    }
                }
            });
        }
    }

    @Override
    protected void loot(ServerWorld world, ItemEntity itemEntity) {
        boolean bl;
        ItemStack itemStack = itemEntity.getStack();
        boolean bl2 = bl = this.hasActiveRaid() && this.getRaid().getCaptain(this.getWave()) != null;
        if (this.hasActiveRaid() && !bl && ItemStack.areEqual(itemStack, Raid.createOminousBanner(this.getRegistryManager().getOrThrow(RegistryKeys.BANNER_PATTERN)))) {
            EquipmentSlot equipmentSlot = EquipmentSlot.HEAD;
            ItemStack itemStack2 = this.getEquippedStack(equipmentSlot);
            double d = this.getEquipmentDropChances().get(equipmentSlot);
            if (!itemStack2.isEmpty() && (double)Math.max(this.random.nextFloat() - 0.1f, 0.0f) < d) {
                this.dropStack(world, itemStack2);
            }
            this.triggerItemPickedUpByEntityCriteria(itemEntity);
            this.equipStack(equipmentSlot, itemStack);
            this.sendPickup(itemEntity, itemStack.getCount());
            itemEntity.discard();
            this.getRaid().setWaveCaptain(this.getWave(), this);
            this.setPatrolLeader(true);
        } else {
            super.loot(world, itemEntity);
        }
    }

    @Override
    public boolean canImmediatelyDespawn(double distanceSquared) {
        if (this.getRaid() == null) {
            return super.canImmediatelyDespawn(distanceSquared);
        }
        return false;
    }

    @Override
    public boolean cannotDespawn() {
        return super.cannotDespawn() || this.getRaid() != null;
    }

    public int getOutOfRaidCounter() {
        return this.outOfRaidCounter;
    }

    public void setOutOfRaidCounter(int outOfRaidCounter) {
        this.outOfRaidCounter = outOfRaidCounter;
    }

    @Override
    public boolean damage(ServerWorld world, DamageSource source, float amount) {
        if (this.hasActiveRaid()) {
            this.getRaid().updateBar();
        }
        return super.damage(world, source, amount);
    }

    @Override
    public @Nullable EntityData initialize(ServerWorldAccess world, LocalDifficulty difficulty, SpawnReason spawnReason, @Nullable EntityData entityData) {
        this.setAbleToJoinRaid(this.getType() != EntityType.WITCH || spawnReason != SpawnReason.NATURAL);
        return super.initialize(world, difficulty, spawnReason, entityData);
    }

    public abstract SoundEvent getCelebratingSound();

    public static class PickUpBannerAsLeaderGoal<T extends RaiderEntity>
    extends Goal {
        private final T actor;
        private Int2LongOpenHashMap bannerItemCache = new Int2LongOpenHashMap();
        private @Nullable Path path;
        private @Nullable ItemEntity bannerItemEntity;
        final /* synthetic */ RaiderEntity field_52512;

        public PickUpBannerAsLeaderGoal(T actor) {
            this.field_52512 = raiderEntity;
            this.actor = actor;
            this.setControls(EnumSet.of(Goal.Control.MOVE));
        }

        @Override
        public boolean canStart() {
            if (this.shouldStop()) {
                return false;
            }
            Int2LongOpenHashMap int2LongOpenHashMap = new Int2LongOpenHashMap();
            double d = this.field_52512.getAttributeValue(EntityAttributes.FOLLOW_RANGE);
            List<ItemEntity> list = ((Entity)this.actor).getEntityWorld().getEntitiesByClass(ItemEntity.class, ((Entity)this.actor).getBoundingBox().expand(d, 8.0, d), OBTAINABLE_OMINOUS_BANNER_PREDICATE);
            for (ItemEntity itemEntity : list) {
                long l = this.bannerItemCache.getOrDefault(itemEntity.getId(), Long.MIN_VALUE);
                if (this.field_52512.getEntityWorld().getTime() < l) {
                    int2LongOpenHashMap.put(itemEntity.getId(), l);
                    continue;
                }
                Path path = ((MobEntity)this.actor).getNavigation().findPathTo(itemEntity, 1);
                if (path != null && path.reachesTarget()) {
                    this.path = path;
                    this.bannerItemEntity = itemEntity;
                    return true;
                }
                int2LongOpenHashMap.put(itemEntity.getId(), this.field_52512.getEntityWorld().getTime() + 600L);
            }
            this.bannerItemCache = int2LongOpenHashMap;
            return false;
        }

        @Override
        public boolean shouldContinue() {
            if (this.bannerItemEntity == null || this.path == null) {
                return false;
            }
            if (this.bannerItemEntity.isRemoved()) {
                return false;
            }
            if (this.path.isFinished()) {
                return false;
            }
            return !this.shouldStop();
        }

        private boolean shouldStop() {
            if (!((RaiderEntity)this.actor).hasActiveRaid()) {
                return true;
            }
            if (((RaiderEntity)this.actor).getRaid().isFinished()) {
                return true;
            }
            if (!((PatrolEntity)this.actor).canLead()) {
                return true;
            }
            if (ItemStack.areEqual(((LivingEntity)this.actor).getEquippedStack(EquipmentSlot.HEAD), Raid.createOminousBanner(((Entity)this.actor).getRegistryManager().getOrThrow(RegistryKeys.BANNER_PATTERN)))) {
                return true;
            }
            RaiderEntity raiderEntity = this.field_52512.raid.getCaptain(((RaiderEntity)this.actor).getWave());
            return raiderEntity != null && raiderEntity.isAlive();
        }

        @Override
        public void start() {
            ((MobEntity)this.actor).getNavigation().startMovingAlong(this.path, 1.15f);
        }

        @Override
        public void stop() {
            this.path = null;
            this.bannerItemEntity = null;
        }

        @Override
        public void tick() {
            if (this.bannerItemEntity != null && this.bannerItemEntity.isInRange((Entity)this.actor, 1.414)) {
                ((RaiderEntity)this.actor).loot(PickUpBannerAsLeaderGoal.castToServerWorld(this.field_52512.getEntityWorld()), this.bannerItemEntity);
            }
        }
    }

    static class AttackHomeGoal
    extends Goal {
        private final RaiderEntity raider;
        private final double speed;
        private BlockPos home;
        private final List<BlockPos> lastHomes = Lists.newArrayList();
        private final int distance;
        private boolean finished;

        public AttackHomeGoal(RaiderEntity raider, double speed, int distance) {
            this.raider = raider;
            this.speed = speed;
            this.distance = distance;
            this.setControls(EnumSet.of(Goal.Control.MOVE));
        }

        @Override
        public boolean canStart() {
            this.purgeMemory();
            return this.isRaiding() && this.tryFindHome() && this.raider.getTarget() == null;
        }

        private boolean isRaiding() {
            return this.raider.hasActiveRaid() && !this.raider.getRaid().isFinished();
        }

        private boolean tryFindHome() {
            ServerWorld serverWorld = (ServerWorld)this.raider.getEntityWorld();
            BlockPos blockPos = this.raider.getBlockPos();
            Optional<BlockPos> optional = serverWorld.getPointOfInterestStorage().getPosition(poi -> poi.matchesKey(PointOfInterestTypes.HOME), this::canLootHome, PointOfInterestStorage.OccupationStatus.ANY, blockPos, 48, this.raider.random);
            if (optional.isEmpty()) {
                return false;
            }
            this.home = optional.get().toImmutable();
            return true;
        }

        @Override
        public boolean shouldContinue() {
            if (this.raider.getNavigation().isIdle()) {
                return false;
            }
            return this.raider.getTarget() == null && !this.home.isWithinDistance(this.raider.getEntityPos(), (double)(this.raider.getWidth() + (float)this.distance)) && !this.finished;
        }

        @Override
        public void stop() {
            if (this.home.isWithinDistance(this.raider.getEntityPos(), (double)this.distance)) {
                this.lastHomes.add(this.home);
            }
        }

        @Override
        public void start() {
            super.start();
            this.raider.setDespawnCounter(0);
            this.raider.getNavigation().startMovingTo(this.home.getX(), this.home.getY(), this.home.getZ(), this.speed);
            this.finished = false;
        }

        @Override
        public void tick() {
            if (this.raider.getNavigation().isIdle()) {
                Vec3d vec3d = Vec3d.ofBottomCenter(this.home);
                Vec3d vec3d2 = NoPenaltyTargeting.findTo(this.raider, 16, 7, vec3d, 0.3141592741012573);
                if (vec3d2 == null) {
                    vec3d2 = NoPenaltyTargeting.findTo(this.raider, 8, 7, vec3d, 1.5707963705062866);
                }
                if (vec3d2 == null) {
                    this.finished = true;
                    return;
                }
                this.raider.getNavigation().startMovingTo(vec3d2.x, vec3d2.y, vec3d2.z, this.speed);
            }
        }

        private boolean canLootHome(BlockPos pos) {
            for (BlockPos blockPos : this.lastHomes) {
                if (!Objects.equals(pos, blockPos)) continue;
                return false;
            }
            return true;
        }

        private void purgeMemory() {
            if (this.lastHomes.size() > 2) {
                this.lastHomes.remove(0);
            }
        }
    }

    public class CelebrateGoal
    extends Goal {
        private final RaiderEntity raider;

        CelebrateGoal(RaiderEntity raider) {
            this.raider = raider;
            this.setControls(EnumSet.of(Goal.Control.MOVE));
        }

        @Override
        public boolean canStart() {
            Raid raid = this.raider.getRaid();
            return this.raider.isAlive() && this.raider.getTarget() == null && raid != null && raid.hasLost();
        }

        @Override
        public void start() {
            this.raider.setCelebrating(true);
            super.start();
        }

        @Override
        public void stop() {
            this.raider.setCelebrating(false);
            super.stop();
        }

        @Override
        public void tick() {
            if (!this.raider.isSilent() && this.raider.random.nextInt(this.getTickCount(100)) == 0) {
                RaiderEntity.this.playSound(RaiderEntity.this.getCelebratingSound());
            }
            if (!this.raider.hasVehicle() && this.raider.random.nextInt(this.getTickCount(50)) == 0) {
                this.raider.getJumpControl().setActive();
            }
            super.tick();
        }
    }

    protected static class PatrolApproachGoal
    extends Goal {
        private final RaiderEntity raider;
        private final float squaredDistance;
        public final TargetPredicate closeRaiderPredicate = TargetPredicate.createNonAttackable().setBaseMaxDistance(8.0).ignoreVisibility().ignoreDistanceScalingFactor();

        public PatrolApproachGoal(IllagerEntity raider, float distance) {
            this.raider = raider;
            this.squaredDistance = distance * distance;
            this.setControls(EnumSet.of(Goal.Control.MOVE, Goal.Control.LOOK));
        }

        @Override
        public boolean canStart() {
            LivingEntity livingEntity = this.raider.getAttacker();
            return this.raider.getRaid() == null && this.raider.isRaidCenterSet() && this.raider.getTarget() != null && !this.raider.isAttacking() && (livingEntity == null || livingEntity.getType() != EntityType.PLAYER);
        }

        @Override
        public void start() {
            super.start();
            this.raider.getNavigation().stop();
            List<RaiderEntity> list = PatrolApproachGoal.getServerWorld(this.raider).getTargets(RaiderEntity.class, this.closeRaiderPredicate, this.raider, this.raider.getBoundingBox().expand(8.0, 8.0, 8.0));
            for (RaiderEntity raiderEntity : list) {
                raiderEntity.setTarget(this.raider.getTarget());
            }
        }

        @Override
        public void stop() {
            super.stop();
            LivingEntity livingEntity = this.raider.getTarget();
            if (livingEntity != null) {
                List<RaiderEntity> list = PatrolApproachGoal.getServerWorld(this.raider).getTargets(RaiderEntity.class, this.closeRaiderPredicate, this.raider, this.raider.getBoundingBox().expand(8.0, 8.0, 8.0));
                for (RaiderEntity raiderEntity : list) {
                    raiderEntity.setTarget(livingEntity);
                    raiderEntity.setAttacking(true);
                }
                this.raider.setAttacking(true);
            }
        }

        @Override
        public boolean shouldRunEveryTick() {
            return true;
        }

        @Override
        public void tick() {
            LivingEntity livingEntity = this.raider.getTarget();
            if (livingEntity == null) {
                return;
            }
            if (this.raider.squaredDistanceTo(livingEntity) > (double)this.squaredDistance) {
                this.raider.getLookControl().lookAt(livingEntity, 30.0f, 30.0f);
                if (this.raider.random.nextInt(50) == 0) {
                    this.raider.playAmbientSound();
                }
            } else {
                this.raider.setAttacking(true);
            }
            super.tick();
        }
    }
}


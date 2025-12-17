/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  com.google.common.collect.Lists
 *  it.unimi.dsi.fastutil.longs.Long2LongOpenHashMap
 *  org.jspecify.annotations.Nullable
 */
package net.minecraft.entity.passive;

import com.google.common.collect.Lists;
import it.unimi.dsi.fastutil.longs.Long2LongOpenHashMap;
import java.util.Comparator;
import java.util.EnumSet;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;
import java.util.stream.Stream;
import net.minecraft.block.Block;
import net.minecraft.block.BlockState;
import net.minecraft.block.Blocks;
import net.minecraft.block.CropBlock;
import net.minecraft.block.Fertilizable;
import net.minecraft.block.FlowerBlock;
import net.minecraft.block.StemBlock;
import net.minecraft.block.SweetBerryBushBlock;
import net.minecraft.block.TallPlantBlock;
import net.minecraft.block.entity.BeehiveBlockEntity;
import net.minecraft.block.entity.BlockEntity;
import net.minecraft.block.entity.BlockEntityType;
import net.minecraft.block.enums.DoubleBlockHalf;
import net.minecraft.enchantment.EnchantmentHelper;
import net.minecraft.entity.Entity;
import net.minecraft.entity.EntityType;
import net.minecraft.entity.Flutterer;
import net.minecraft.entity.LazyEntityReference;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.SpawnReason;
import net.minecraft.entity.ai.AboveGroundTargeting;
import net.minecraft.entity.ai.NoPenaltySolidTargeting;
import net.minecraft.entity.ai.NoWaterTargeting;
import net.minecraft.entity.ai.control.FlightMoveControl;
import net.minecraft.entity.ai.control.LookControl;
import net.minecraft.entity.ai.goal.ActiveTargetGoal;
import net.minecraft.entity.ai.goal.AnimalMateGoal;
import net.minecraft.entity.ai.goal.FollowParentGoal;
import net.minecraft.entity.ai.goal.Goal;
import net.minecraft.entity.ai.goal.GoalSelector;
import net.minecraft.entity.ai.goal.MeleeAttackGoal;
import net.minecraft.entity.ai.goal.RevengeGoal;
import net.minecraft.entity.ai.goal.SwimGoal;
import net.minecraft.entity.ai.goal.TemptGoal;
import net.minecraft.entity.ai.goal.UniversalAngerGoal;
import net.minecraft.entity.ai.pathing.BirdNavigation;
import net.minecraft.entity.ai.pathing.EntityNavigation;
import net.minecraft.entity.ai.pathing.Path;
import net.minecraft.entity.ai.pathing.PathNodeType;
import net.minecraft.entity.attribute.DefaultAttributeContainer;
import net.minecraft.entity.attribute.EntityAttributes;
import net.minecraft.entity.damage.DamageSource;
import net.minecraft.entity.data.DataTracker;
import net.minecraft.entity.data.TrackedData;
import net.minecraft.entity.data.TrackedDataHandlerRegistry;
import net.minecraft.entity.effect.StatusEffectInstance;
import net.minecraft.entity.effect.StatusEffects;
import net.minecraft.entity.mob.Angerable;
import net.minecraft.entity.mob.MobEntity;
import net.minecraft.entity.mob.PathAwareEntity;
import net.minecraft.entity.passive.AnimalEntity;
import net.minecraft.entity.passive.PassiveEntity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.fluid.Fluid;
import net.minecraft.item.BlockItem;
import net.minecraft.item.ItemConvertible;
import net.minecraft.item.ItemStack;
import net.minecraft.particle.ParticleEffect;
import net.minecraft.particle.ParticleTypes;
import net.minecraft.registry.tag.BlockTags;
import net.minecraft.registry.tag.ItemTags;
import net.minecraft.registry.tag.PointOfInterestTypeTags;
import net.minecraft.registry.tag.TagKey;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.sound.SoundEvent;
import net.minecraft.sound.SoundEvents;
import net.minecraft.state.property.Properties;
import net.minecraft.storage.ReadView;
import net.minecraft.storage.WriteView;
import net.minecraft.util.ActionResult;
import net.minecraft.util.Hand;
import net.minecraft.util.TimeHelper;
import net.minecraft.util.annotation.Debug;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.MathHelper;
import net.minecraft.util.math.Vec3d;
import net.minecraft.util.math.intprovider.UniformIntProvider;
import net.minecraft.world.Difficulty;
import net.minecraft.world.World;
import net.minecraft.world.WorldView;
import net.minecraft.world.attribute.EnvironmentAttributes;
import net.minecraft.world.debug.DebugSubscriptionTypes;
import net.minecraft.world.debug.DebugTrackable;
import net.minecraft.world.debug.data.BeeDebugData;
import net.minecraft.world.poi.PointOfInterest;
import net.minecraft.world.poi.PointOfInterestStorage;
import org.jspecify.annotations.Nullable;

public class BeeEntity
extends AnimalEntity
implements Angerable,
Flutterer {
    public static final float field_30271 = 120.32113f;
    public static final int field_28638 = MathHelper.ceil(1.4959966f);
    private static final TrackedData<Byte> BEE_FLAGS = DataTracker.registerData(BeeEntity.class, TrackedDataHandlerRegistry.BYTE);
    private static final TrackedData<Long> ANGER_END_TIME = DataTracker.registerData(BeeEntity.class, TrackedDataHandlerRegistry.LONG);
    private static final int NEAR_TARGET_FLAG = 2;
    private static final int HAS_STUNG_FLAG = 4;
    private static final int HAS_NECTAR_FLAG = 8;
    private static final int MAX_LIFETIME_AFTER_STINGING = 1200;
    private static final int FLOWER_NAVIGATION_START_TICKS = 600;
    private static final int POLLINATION_FAIL_TICKS = 3600;
    private static final int field_30287 = 4;
    private static final int MAX_POLLINATED_CROPS = 10;
    private static final int NORMAL_DIFFICULTY_STING_POISON_DURATION = 10;
    private static final int HARD_DIFFICULTY_STING_POISON_DURATION = 18;
    private static final int TOO_FAR_DISTANCE = 48;
    private static final int field_30292 = 2;
    private static final int field_52456 = 24;
    private static final int field_52457 = 16;
    private static final int MIN_HIVE_RETURN_DISTANCE = 16;
    private static final int field_30294 = 20;
    public static final String CROPS_GROWN_SINCE_POLLINATION_KEY = "CropsGrownSincePollination";
    public static final String CANNOT_ENTER_HIVE_TICKS_KEY = "CannotEnterHiveTicks";
    public static final String TICKS_SINCE_POLLINATION_KEY = "TicksSincePollination";
    public static final String HAS_STUNG_KEY = "HasStung";
    public static final String HAS_NECTAR_KEY = "HasNectar";
    public static final String FLOWER_POS_KEY = "flower_pos";
    public static final String HIVE_POS_KEY = "hive_pos";
    public static final boolean DEFAULT_HAS_NECTAR = false;
    private static final boolean DEFAULT_HAS_STUNG = false;
    private static final int DEFAULT_TICKS_SINCE_POLLINATION = 0;
    private static final int DEFAULT_CANNOT_ENTER_HIVE_TICKS = 0;
    private static final int DEFAULT_CROPS_GROWN_SINCE_POLLINATION = 0;
    private static final UniformIntProvider ANGER_TIME_RANGE = TimeHelper.betweenSeconds(20, 39);
    private @Nullable LazyEntityReference<LivingEntity> angryAt;
    private float currentPitch;
    private float lastPitch;
    private int ticksSinceSting;
    int ticksSincePollination = 0;
    private int cannotEnterHiveTicks = 0;
    private int cropsGrownSincePollination = 0;
    private static final int field_30274 = 200;
    int ticksLeftToFindHive;
    private static final int field_30275 = 200;
    private static final int field_52454 = 20;
    private static final int field_52455 = 60;
    int ticksUntilCanPollinate = MathHelper.nextInt(this.random, 20, 60);
    @Nullable BlockPos flowerPos;
    @Nullable BlockPos hivePos;
    PollinateGoal pollinateGoal;
    MoveToHiveGoal moveToHiveGoal;
    private MoveToFlowerGoal moveToFlowerGoal;
    private int ticksInsideWater;

    public BeeEntity(EntityType<? extends BeeEntity> entityType, World world) {
        super((EntityType<? extends AnimalEntity>)entityType, world);
        this.moveControl = new FlightMoveControl(this, 20, true);
        this.lookControl = new BeeLookControl(this);
        this.setPathfindingPenalty(PathNodeType.DANGER_FIRE, -1.0f);
        this.setPathfindingPenalty(PathNodeType.WATER, -1.0f);
        this.setPathfindingPenalty(PathNodeType.WATER_BORDER, 16.0f);
        this.setPathfindingPenalty(PathNodeType.COCOA, -1.0f);
        this.setPathfindingPenalty(PathNodeType.FENCE, -1.0f);
    }

    @Override
    protected void initDataTracker(DataTracker.Builder builder) {
        super.initDataTracker(builder);
        builder.add(BEE_FLAGS, (byte)0);
        builder.add(ANGER_END_TIME, -1L);
    }

    @Override
    public float getPathfindingFavor(BlockPos pos, WorldView world) {
        if (world.getBlockState(pos).isAir()) {
            return 10.0f;
        }
        return 0.0f;
    }

    @Override
    protected void initGoals() {
        this.goalSelector.add(0, new StingGoal(this, 1.4f, true));
        this.goalSelector.add(1, new EnterHiveGoal());
        this.goalSelector.add(2, new AnimalMateGoal(this, 1.0));
        this.goalSelector.add(3, new TemptGoal(this, 1.25, stack -> stack.isIn(ItemTags.BEE_FOOD), false));
        this.goalSelector.add(3, new ValidateHiveGoal());
        this.goalSelector.add(3, new ValidateFlowerGoal());
        this.pollinateGoal = new PollinateGoal();
        this.goalSelector.add(4, this.pollinateGoal);
        this.goalSelector.add(5, new FollowParentGoal(this, 1.25));
        this.goalSelector.add(5, new FindHiveGoal());
        this.moveToHiveGoal = new MoveToHiveGoal();
        this.goalSelector.add(5, this.moveToHiveGoal);
        this.moveToFlowerGoal = new MoveToFlowerGoal();
        this.goalSelector.add(6, this.moveToFlowerGoal);
        this.goalSelector.add(7, new GrowCropsGoal());
        this.goalSelector.add(8, new BeeWanderAroundGoal());
        this.goalSelector.add(9, new SwimGoal(this));
        this.targetSelector.add(1, new BeeRevengeGoal(this).setGroupRevenge(new Class[0]));
        this.targetSelector.add(2, new StingTargetGoal(this));
        this.targetSelector.add(3, new UniversalAngerGoal<BeeEntity>(this, true));
    }

    @Override
    protected void writeCustomData(WriteView view) {
        super.writeCustomData(view);
        view.putNullable(HIVE_POS_KEY, BlockPos.CODEC, this.hivePos);
        view.putNullable(FLOWER_POS_KEY, BlockPos.CODEC, this.flowerPos);
        view.putBoolean(HAS_NECTAR_KEY, this.hasNectar());
        view.putBoolean(HAS_STUNG_KEY, this.hasStung());
        view.putInt(TICKS_SINCE_POLLINATION_KEY, this.ticksSincePollination);
        view.putInt(CANNOT_ENTER_HIVE_TICKS_KEY, this.cannotEnterHiveTicks);
        view.putInt(CROPS_GROWN_SINCE_POLLINATION_KEY, this.cropsGrownSincePollination);
        this.writeAngerToData(view);
    }

    @Override
    protected void readCustomData(ReadView view) {
        super.readCustomData(view);
        this.setHasNectar(view.getBoolean(HAS_NECTAR_KEY, false));
        this.setHasStung(view.getBoolean(HAS_STUNG_KEY, false));
        this.ticksSincePollination = view.getInt(TICKS_SINCE_POLLINATION_KEY, 0);
        this.cannotEnterHiveTicks = view.getInt(CANNOT_ENTER_HIVE_TICKS_KEY, 0);
        this.cropsGrownSincePollination = view.getInt(CROPS_GROWN_SINCE_POLLINATION_KEY, 0);
        this.hivePos = view.read(HIVE_POS_KEY, BlockPos.CODEC).orElse(null);
        this.flowerPos = view.read(FLOWER_POS_KEY, BlockPos.CODEC).orElse(null);
        this.readAngerFromData(this.getEntityWorld(), view);
    }

    @Override
    public boolean tryAttack(ServerWorld world, Entity target) {
        DamageSource damageSource = this.getDamageSources().sting(this);
        boolean bl = target.damage(world, damageSource, (int)this.getAttributeValue(EntityAttributes.ATTACK_DAMAGE));
        if (bl) {
            EnchantmentHelper.onTargetDamaged(world, target, damageSource);
            if (target instanceof LivingEntity) {
                LivingEntity livingEntity = (LivingEntity)target;
                livingEntity.setStingerCount(livingEntity.getStingerCount() + 1);
                int i = 0;
                if (this.getEntityWorld().getDifficulty() == Difficulty.NORMAL) {
                    i = 10;
                } else if (this.getEntityWorld().getDifficulty() == Difficulty.HARD) {
                    i = 18;
                }
                if (i > 0) {
                    livingEntity.addStatusEffect(new StatusEffectInstance(StatusEffects.POISON, i * 20, 0), this);
                }
            }
            this.setHasStung(true);
            this.stopAnger();
            this.playSound(SoundEvents.ENTITY_BEE_STING, 1.0f, 1.0f);
        }
        return bl;
    }

    @Override
    public void tick() {
        super.tick();
        if (this.hasNectar() && this.getCropsGrownSincePollination() < 10 && this.random.nextFloat() < 0.05f) {
            for (int i = 0; i < this.random.nextInt(2) + 1; ++i) {
                this.addParticle(this.getEntityWorld(), this.getX() - (double)0.3f, this.getX() + (double)0.3f, this.getZ() - (double)0.3f, this.getZ() + (double)0.3f, this.getBodyY(0.5), ParticleTypes.FALLING_NECTAR);
            }
        }
        this.updateBodyPitch();
    }

    private void addParticle(World world, double lastX, double x, double lastZ, double z, double y, ParticleEffect effect) {
        world.addParticleClient(effect, MathHelper.lerp(world.random.nextDouble(), lastX, x), y, MathHelper.lerp(world.random.nextDouble(), lastZ, z), 0.0, 0.0, 0.0);
    }

    void startMovingTo(BlockPos pos) {
        Vec3d vec3d2;
        Vec3d vec3d = Vec3d.ofBottomCenter(pos);
        int i = 0;
        BlockPos blockPos = this.getBlockPos();
        int j = (int)vec3d.y - blockPos.getY();
        if (j > 2) {
            i = 4;
        } else if (j < -2) {
            i = -4;
        }
        int k = 6;
        int l = 8;
        int m = blockPos.getManhattanDistance(pos);
        if (m < 15) {
            k = m / 2;
            l = m / 2;
        }
        if ((vec3d2 = NoWaterTargeting.find(this, k, l, i, vec3d, 0.3141592741012573)) == null) {
            return;
        }
        this.navigation.setRangeMultiplier(0.5f);
        this.navigation.startMovingTo(vec3d2.x, vec3d2.y, vec3d2.z, 1.0);
    }

    public @Nullable BlockPos getFlowerPos() {
        return this.flowerPos;
    }

    public boolean hasFlower() {
        return this.flowerPos != null;
    }

    public void setFlowerPos(BlockPos flowerPos) {
        this.flowerPos = flowerPos;
    }

    @Debug
    public int getMoveGoalTicks() {
        return Math.max(this.moveToHiveGoal.ticks, this.moveToFlowerGoal.ticks);
    }

    @Debug
    public List<BlockPos> getPossibleHives() {
        return this.moveToHiveGoal.possibleHives;
    }

    private boolean failedPollinatingTooLong() {
        return this.ticksSincePollination > 3600;
    }

    void clearHivePos() {
        this.hivePos = null;
        this.ticksLeftToFindHive = 200;
    }

    void clearFlowerPos() {
        this.flowerPos = null;
        this.ticksUntilCanPollinate = MathHelper.nextInt(this.random, 20, 60);
    }

    boolean canEnterHive() {
        if (this.cannotEnterHiveTicks > 0 || this.pollinateGoal.isRunning() || this.hasStung() || this.getTarget() != null) {
            return false;
        }
        boolean bl = this.hasNectar() || this.failedPollinatingTooLong() || this.getEntityWorld().getEnvironmentAttributes().getAttributeValue(EnvironmentAttributes.BEES_STAY_IN_HIVE_GAMEPLAY, this.getEntityPos()) != false;
        return bl && !this.isHiveNearFire();
    }

    public void setCannotEnterHiveTicks(int cannotEnterHiveTicks) {
        this.cannotEnterHiveTicks = cannotEnterHiveTicks;
    }

    public float getBodyPitch(float tickProgress) {
        return MathHelper.lerp(tickProgress, this.lastPitch, this.currentPitch);
    }

    private void updateBodyPitch() {
        this.lastPitch = this.currentPitch;
        this.currentPitch = this.isNearTarget() ? Math.min(1.0f, this.currentPitch + 0.2f) : Math.max(0.0f, this.currentPitch - 0.24f);
    }

    @Override
    protected void mobTick(ServerWorld world) {
        boolean bl = this.hasStung();
        this.ticksInsideWater = this.isTouchingWater() ? ++this.ticksInsideWater : 0;
        if (this.ticksInsideWater > 20) {
            this.damage(world, this.getDamageSources().drown(), 1.0f);
        }
        if (bl) {
            ++this.ticksSinceSting;
            if (this.ticksSinceSting % 5 == 0 && this.random.nextInt(MathHelper.clamp(1200 - this.ticksSinceSting, 1, 1200)) == 0) {
                this.damage(world, this.getDamageSources().generic(), this.getHealth());
            }
        }
        if (!this.hasNectar()) {
            ++this.ticksSincePollination;
        }
        this.tickAngerLogic(world, false);
    }

    public void resetPollinationTicks() {
        this.ticksSincePollination = 0;
    }

    private boolean isHiveNearFire() {
        BeehiveBlockEntity beehiveBlockEntity = this.getHive();
        return beehiveBlockEntity != null && beehiveBlockEntity.isNearFire();
    }

    @Override
    public long getAngerEndTime() {
        return this.dataTracker.get(ANGER_END_TIME);
    }

    @Override
    public void setAngerEndTime(long angerEndTime) {
        this.dataTracker.set(ANGER_END_TIME, angerEndTime);
    }

    @Override
    public @Nullable LazyEntityReference<LivingEntity> getAngryAt() {
        return this.angryAt;
    }

    @Override
    public void setAngryAt(@Nullable LazyEntityReference<LivingEntity> angryAt) {
        this.angryAt = angryAt;
    }

    @Override
    public void chooseRandomAngerTime() {
        this.setAngerDuration(ANGER_TIME_RANGE.get(this.random));
    }

    private boolean doesHiveHaveSpace(BlockPos pos) {
        BlockEntity blockEntity = this.getEntityWorld().getBlockEntity(pos);
        if (blockEntity instanceof BeehiveBlockEntity) {
            return !((BeehiveBlockEntity)blockEntity).isFullOfBees();
        }
        return false;
    }

    @Debug
    public boolean hasHivePos() {
        return this.hivePos != null;
    }

    @Debug
    public @Nullable BlockPos getHivePos() {
        return this.hivePos;
    }

    @Debug
    public GoalSelector getGoalSelector() {
        return this.goalSelector;
    }

    int getCropsGrownSincePollination() {
        return this.cropsGrownSincePollination;
    }

    private void resetCropCounter() {
        this.cropsGrownSincePollination = 0;
    }

    void addCropCounter() {
        ++this.cropsGrownSincePollination;
    }

    @Override
    public void tickMovement() {
        super.tickMovement();
        if (!this.getEntityWorld().isClient()) {
            if (this.cannotEnterHiveTicks > 0) {
                --this.cannotEnterHiveTicks;
            }
            if (this.ticksLeftToFindHive > 0) {
                --this.ticksLeftToFindHive;
            }
            if (this.ticksUntilCanPollinate > 0) {
                --this.ticksUntilCanPollinate;
            }
            boolean bl = this.hasAngerTime() && !this.hasStung() && this.getTarget() != null && this.getTarget().squaredDistanceTo(this) < 4.0;
            this.setNearTarget(bl);
            if (this.age % 20 == 0 && !this.hasValidHive()) {
                this.hivePos = null;
            }
        }
    }

    @Nullable BeehiveBlockEntity getHive() {
        if (this.hivePos == null) {
            return null;
        }
        if (this.isTooFar(this.hivePos)) {
            return null;
        }
        return this.getEntityWorld().getBlockEntity(this.hivePos, BlockEntityType.BEEHIVE).orElse(null);
    }

    boolean hasValidHive() {
        return this.getHive() != null;
    }

    public boolean hasNectar() {
        return this.getBeeFlag(8);
    }

    void setHasNectar(boolean hasNectar) {
        if (hasNectar) {
            this.resetPollinationTicks();
        }
        this.setBeeFlag(8, hasNectar);
    }

    public boolean hasStung() {
        return this.getBeeFlag(4);
    }

    private void setHasStung(boolean hasStung) {
        this.setBeeFlag(4, hasStung);
    }

    private boolean isNearTarget() {
        return this.getBeeFlag(2);
    }

    private void setNearTarget(boolean nearTarget) {
        this.setBeeFlag(2, nearTarget);
    }

    boolean isTooFar(BlockPos pos) {
        return !this.isWithinDistance(pos, 48);
    }

    private void setBeeFlag(int bit, boolean value) {
        if (value) {
            this.dataTracker.set(BEE_FLAGS, (byte)(this.dataTracker.get(BEE_FLAGS) | bit));
        } else {
            this.dataTracker.set(BEE_FLAGS, (byte)(this.dataTracker.get(BEE_FLAGS) & ~bit));
        }
    }

    private boolean getBeeFlag(int location) {
        return (this.dataTracker.get(BEE_FLAGS) & location) != 0;
    }

    public static DefaultAttributeContainer.Builder createBeeAttributes() {
        return AnimalEntity.createAnimalAttributes().add(EntityAttributes.MAX_HEALTH, 10.0).add(EntityAttributes.FLYING_SPEED, 0.6f).add(EntityAttributes.MOVEMENT_SPEED, 0.3f).add(EntityAttributes.ATTACK_DAMAGE, 2.0);
    }

    @Override
    protected EntityNavigation createNavigation(World world) {
        BirdNavigation birdNavigation = new BirdNavigation(this, world){

            @Override
            public boolean isValidPosition(BlockPos pos) {
                return !this.world.getBlockState(pos.down()).isAir();
            }

            @Override
            public void tick() {
                if (BeeEntity.this.pollinateGoal.isRunning()) {
                    return;
                }
                super.tick();
            }
        };
        birdNavigation.setCanOpenDoors(false);
        birdNavigation.setCanSwim(false);
        birdNavigation.setMaxFollowRange(48.0f);
        return birdNavigation;
    }

    @Override
    public ActionResult interactMob(PlayerEntity player, Hand hand) {
        FlowerBlock flowerBlock;
        StatusEffectInstance statusEffectInstance;
        BlockItem blockItem;
        ItemConvertible itemConvertible;
        ItemStack itemStack = player.getStackInHand(hand);
        if (this.isBreedingItem(itemStack) && (itemConvertible = itemStack.getItem()) instanceof BlockItem && (itemConvertible = (blockItem = (BlockItem)itemConvertible).getBlock()) instanceof FlowerBlock && (statusEffectInstance = (flowerBlock = (FlowerBlock)itemConvertible).getContactEffect()) != null) {
            this.eat(player, hand, itemStack);
            if (!this.getEntityWorld().isClient()) {
                this.addStatusEffect(statusEffectInstance);
            }
            return ActionResult.SUCCESS;
        }
        return super.interactMob(player, hand);
    }

    @Override
    public boolean isBreedingItem(ItemStack stack) {
        return stack.isIn(ItemTags.BEE_FOOD);
    }

    @Override
    protected void playStepSound(BlockPos pos, BlockState state) {
    }

    @Override
    protected SoundEvent getAmbientSound() {
        return null;
    }

    @Override
    protected SoundEvent getHurtSound(DamageSource source) {
        return SoundEvents.ENTITY_BEE_HURT;
    }

    @Override
    protected SoundEvent getDeathSound() {
        return SoundEvents.ENTITY_BEE_DEATH;
    }

    @Override
    protected float getSoundVolume() {
        return 0.4f;
    }

    @Override
    public @Nullable BeeEntity createChild(ServerWorld serverWorld, PassiveEntity passiveEntity) {
        return EntityType.BEE.create(serverWorld, SpawnReason.BREEDING);
    }

    @Override
    protected void fall(double heightDifference, boolean onGround, BlockState state, BlockPos landedPosition) {
    }

    @Override
    public boolean isFlappingWings() {
        return this.isInAir() && this.age % field_28638 == 0;
    }

    @Override
    public boolean isInAir() {
        return !this.isOnGround();
    }

    public void onHoneyDelivered() {
        this.setHasNectar(false);
        this.resetCropCounter();
    }

    @Override
    public boolean damage(ServerWorld world, DamageSource source, float amount) {
        if (this.isInvulnerableTo(world, source)) {
            return false;
        }
        this.pollinateGoal.cancel();
        return super.damage(world, source, amount);
    }

    @Override
    protected void swimUpward(TagKey<Fluid> fluid) {
        this.setVelocity(this.getVelocity().add(0.0, 0.01, 0.0));
    }

    @Override
    public Vec3d getLeashOffset() {
        return new Vec3d(0.0, 0.5f * this.getStandingEyeHeight(), this.getWidth() * 0.2f);
    }

    boolean isWithinDistance(BlockPos pos, int distance) {
        return pos.isWithinDistance(this.getBlockPos(), (double)distance);
    }

    public void setHivePos(BlockPos pos) {
        this.hivePos = pos;
    }

    public static boolean isAttractive(BlockState state) {
        if (state.isIn(BlockTags.BEE_ATTRACTIVE)) {
            if (state.get(Properties.WATERLOGGED, false).booleanValue()) {
                return false;
            }
            if (state.isOf(Blocks.SUNFLOWER)) {
                return state.get(TallPlantBlock.HALF) == DoubleBlockHalf.UPPER;
            }
            return true;
        }
        return false;
    }

    @Override
    public void registerTracking(ServerWorld world, DebugTrackable.Tracker tracker) {
        super.registerTracking(world, tracker);
        tracker.track(DebugSubscriptionTypes.BEES, () -> new BeeDebugData(Optional.ofNullable(this.getHivePos()), Optional.ofNullable(this.getFlowerPos()), this.getMoveGoalTicks(), this.getPossibleHives()));
    }

    @Override
    public /* synthetic */ @Nullable PassiveEntity createChild(ServerWorld world, PassiveEntity entity) {
        return this.createChild(world, entity);
    }

    class BeeLookControl
    extends LookControl {
        BeeLookControl(MobEntity entity) {
            super(entity);
        }

        @Override
        public void tick() {
            if (BeeEntity.this.hasAngerTime()) {
                return;
            }
            super.tick();
        }

        @Override
        protected boolean shouldStayHorizontal() {
            return !BeeEntity.this.pollinateGoal.isRunning();
        }
    }

    class StingGoal
    extends MeleeAttackGoal {
        StingGoal(PathAwareEntity mob, double speed, boolean pauseWhenMobIdle) {
            super(mob, speed, pauseWhenMobIdle);
        }

        @Override
        public boolean canStart() {
            return super.canStart() && BeeEntity.this.hasAngerTime() && !BeeEntity.this.hasStung();
        }

        @Override
        public boolean shouldContinue() {
            return super.shouldContinue() && BeeEntity.this.hasAngerTime() && !BeeEntity.this.hasStung();
        }
    }

    class EnterHiveGoal
    extends NotAngryGoal {
        EnterHiveGoal() {
        }

        @Override
        public boolean canBeeStart() {
            BeehiveBlockEntity beehiveBlockEntity;
            if (BeeEntity.this.hivePos != null && BeeEntity.this.canEnterHive() && BeeEntity.this.hivePos.isWithinDistance(BeeEntity.this.getEntityPos(), 2.0) && (beehiveBlockEntity = BeeEntity.this.getHive()) != null) {
                if (beehiveBlockEntity.isFullOfBees()) {
                    BeeEntity.this.hivePos = null;
                } else {
                    return true;
                }
            }
            return false;
        }

        @Override
        public boolean canBeeContinue() {
            return false;
        }

        @Override
        public void start() {
            BeehiveBlockEntity beehiveBlockEntity = BeeEntity.this.getHive();
            if (beehiveBlockEntity != null) {
                beehiveBlockEntity.tryEnterHive(BeeEntity.this);
            }
        }
    }

    class ValidateHiveGoal
    extends NotAngryGoal {
        private final int ticksUntilNextValidate;
        private long lastValidateTime;

        ValidateHiveGoal() {
            this.ticksUntilNextValidate = MathHelper.nextInt(BeeEntity.this.random, 20, 40);
            this.lastValidateTime = -1L;
        }

        @Override
        public void start() {
            if (BeeEntity.this.hivePos != null && BeeEntity.this.getEntityWorld().isPosLoaded(BeeEntity.this.hivePos) && !BeeEntity.this.hasValidHive()) {
                BeeEntity.this.clearHivePos();
            }
            this.lastValidateTime = BeeEntity.this.getEntityWorld().getTime();
        }

        @Override
        public boolean canBeeStart() {
            return BeeEntity.this.getEntityWorld().getTime() > this.lastValidateTime + (long)this.ticksUntilNextValidate;
        }

        @Override
        public boolean canBeeContinue() {
            return false;
        }
    }

    class ValidateFlowerGoal
    extends NotAngryGoal {
        private final int ticksUntilNextValidate;
        private long lastValidateTime;

        ValidateFlowerGoal() {
            this.ticksUntilNextValidate = MathHelper.nextInt(BeeEntity.this.random, 20, 40);
            this.lastValidateTime = -1L;
        }

        @Override
        public void start() {
            if (BeeEntity.this.flowerPos != null && BeeEntity.this.getEntityWorld().isPosLoaded(BeeEntity.this.flowerPos) && !this.isFlower(BeeEntity.this.flowerPos)) {
                BeeEntity.this.clearFlowerPos();
            }
            this.lastValidateTime = BeeEntity.this.getEntityWorld().getTime();
        }

        @Override
        public boolean canBeeStart() {
            return BeeEntity.this.getEntityWorld().getTime() > this.lastValidateTime + (long)this.ticksUntilNextValidate;
        }

        @Override
        public boolean canBeeContinue() {
            return false;
        }

        private boolean isFlower(BlockPos pos) {
            return BeeEntity.isAttractive(BeeEntity.this.getEntityWorld().getBlockState(pos));
        }
    }

    class PollinateGoal
    extends NotAngryGoal {
        private static final int field_30300 = 400;
        private static final double field_30303 = 0.1;
        private static final int field_30304 = 25;
        private static final float field_30305 = 0.35f;
        private static final float field_30306 = 0.6f;
        private static final float field_30307 = 0.33333334f;
        private static final int field_52458 = 5;
        private int pollinationTicks;
        private int lastPollinationTick;
        private boolean running;
        private @Nullable Vec3d nextTarget;
        private int ticks;
        private static final int field_30308 = 600;
        private Long2LongOpenHashMap unreachableFlowerPosCache;

        PollinateGoal() {
            this.unreachableFlowerPosCache = new Long2LongOpenHashMap();
            this.setControls(EnumSet.of(Goal.Control.MOVE));
        }

        @Override
        public boolean canBeeStart() {
            if (BeeEntity.this.ticksUntilCanPollinate > 0) {
                return false;
            }
            if (BeeEntity.this.hasNectar()) {
                return false;
            }
            if (BeeEntity.this.getEntityWorld().isRaining()) {
                return false;
            }
            Optional<BlockPos> optional = this.getFlower();
            if (optional.isPresent()) {
                BeeEntity.this.flowerPos = optional.get();
                BeeEntity.this.navigation.startMovingTo((double)BeeEntity.this.flowerPos.getX() + 0.5, (double)BeeEntity.this.flowerPos.getY() + 0.5, (double)BeeEntity.this.flowerPos.getZ() + 0.5, 1.2f);
                return true;
            }
            BeeEntity.this.ticksUntilCanPollinate = MathHelper.nextInt(BeeEntity.this.random, 20, 60);
            return false;
        }

        @Override
        public boolean canBeeContinue() {
            if (!this.running) {
                return false;
            }
            if (!BeeEntity.this.hasFlower()) {
                return false;
            }
            if (BeeEntity.this.getEntityWorld().isRaining()) {
                return false;
            }
            if (this.completedPollination()) {
                return BeeEntity.this.random.nextFloat() < 0.2f;
            }
            return true;
        }

        private boolean completedPollination() {
            return this.pollinationTicks > 400;
        }

        boolean isRunning() {
            return this.running;
        }

        void cancel() {
            this.running = false;
        }

        @Override
        public void start() {
            this.pollinationTicks = 0;
            this.ticks = 0;
            this.lastPollinationTick = 0;
            this.running = true;
            BeeEntity.this.resetPollinationTicks();
        }

        @Override
        public void stop() {
            if (this.completedPollination()) {
                BeeEntity.this.setHasNectar(true);
            }
            this.running = false;
            BeeEntity.this.navigation.stop();
            BeeEntity.this.ticksUntilCanPollinate = 200;
        }

        @Override
        public boolean shouldRunEveryTick() {
            return true;
        }

        @Override
        public void tick() {
            if (!BeeEntity.this.hasFlower()) {
                return;
            }
            ++this.ticks;
            if (this.ticks > 600) {
                BeeEntity.this.clearFlowerPos();
                this.running = false;
                BeeEntity.this.ticksUntilCanPollinate = 200;
                return;
            }
            Vec3d vec3d = Vec3d.ofBottomCenter(BeeEntity.this.flowerPos).add(0.0, 0.6f, 0.0);
            if (vec3d.distanceTo(BeeEntity.this.getEntityPos()) > 1.0) {
                this.nextTarget = vec3d;
                this.moveToNextTarget();
                return;
            }
            if (this.nextTarget == null) {
                this.nextTarget = vec3d;
            }
            boolean bl = BeeEntity.this.getEntityPos().distanceTo(this.nextTarget) <= 0.1;
            boolean bl2 = true;
            if (!bl && this.ticks > 600) {
                BeeEntity.this.clearFlowerPos();
                return;
            }
            if (bl) {
                boolean bl3;
                boolean bl4 = bl3 = BeeEntity.this.random.nextInt(25) == 0;
                if (bl3) {
                    this.nextTarget = new Vec3d(vec3d.getX() + (double)this.getRandomOffset(), vec3d.getY(), vec3d.getZ() + (double)this.getRandomOffset());
                    BeeEntity.this.navigation.stop();
                } else {
                    bl2 = false;
                }
                BeeEntity.this.getLookControl().lookAt(vec3d.getX(), vec3d.getY(), vec3d.getZ());
            }
            if (bl2) {
                this.moveToNextTarget();
            }
            ++this.pollinationTicks;
            if (BeeEntity.this.random.nextFloat() < 0.05f && this.pollinationTicks > this.lastPollinationTick + 60) {
                this.lastPollinationTick = this.pollinationTicks;
                BeeEntity.this.playSound(SoundEvents.ENTITY_BEE_POLLINATE, 1.0f, 1.0f);
            }
        }

        private void moveToNextTarget() {
            BeeEntity.this.getMoveControl().moveTo(this.nextTarget.getX(), this.nextTarget.getY(), this.nextTarget.getZ(), 0.35f);
        }

        private float getRandomOffset() {
            return (BeeEntity.this.random.nextFloat() * 2.0f - 1.0f) * 0.33333334f;
        }

        private Optional<BlockPos> getFlower() {
            Iterable<BlockPos> iterable = BlockPos.iterateOutwards(BeeEntity.this.getBlockPos(), 5, 5, 5);
            Long2LongOpenHashMap long2LongOpenHashMap = new Long2LongOpenHashMap();
            for (BlockPos blockPos : iterable) {
                long l = this.unreachableFlowerPosCache.getOrDefault(blockPos.asLong(), Long.MIN_VALUE);
                if (BeeEntity.this.getEntityWorld().getTime() < l) {
                    long2LongOpenHashMap.put(blockPos.asLong(), l);
                    continue;
                }
                if (!BeeEntity.isAttractive(BeeEntity.this.getEntityWorld().getBlockState(blockPos))) continue;
                Path path = BeeEntity.this.navigation.findPathTo(blockPos, 1);
                if (path != null && path.reachesTarget()) {
                    return Optional.of(blockPos);
                }
                long2LongOpenHashMap.put(blockPos.asLong(), BeeEntity.this.getEntityWorld().getTime() + 600L);
            }
            this.unreachableFlowerPosCache = long2LongOpenHashMap;
            return Optional.empty();
        }
    }

    class FindHiveGoal
    extends NotAngryGoal {
        FindHiveGoal() {
        }

        @Override
        public boolean canBeeStart() {
            return BeeEntity.this.ticksLeftToFindHive == 0 && !BeeEntity.this.hasHivePos() && BeeEntity.this.canEnterHive();
        }

        @Override
        public boolean canBeeContinue() {
            return false;
        }

        @Override
        public void start() {
            BeeEntity.this.ticksLeftToFindHive = 200;
            List<BlockPos> list = this.getNearbyFreeHives();
            if (list.isEmpty()) {
                return;
            }
            for (BlockPos blockPos : list) {
                if (BeeEntity.this.moveToHiveGoal.isPossibleHive(blockPos)) continue;
                BeeEntity.this.hivePos = blockPos;
                return;
            }
            BeeEntity.this.moveToHiveGoal.clearPossibleHives();
            BeeEntity.this.hivePos = list.get(0);
        }

        private List<BlockPos> getNearbyFreeHives() {
            BlockPos blockPos = BeeEntity.this.getBlockPos();
            PointOfInterestStorage pointOfInterestStorage = ((ServerWorld)BeeEntity.this.getEntityWorld()).getPointOfInterestStorage();
            Stream<PointOfInterest> stream = pointOfInterestStorage.getInCircle(poiType -> poiType.isIn(PointOfInterestTypeTags.BEE_HOME), blockPos, 20, PointOfInterestStorage.OccupationStatus.ANY);
            return stream.map(PointOfInterest::getPos).filter(BeeEntity.this::doesHiveHaveSpace).sorted(Comparator.comparingDouble(blockPos2 -> blockPos2.getSquaredDistance(blockPos))).collect(Collectors.toList());
        }
    }

    @Debug
    public class MoveToHiveGoal
    extends NotAngryGoal {
        public static final int field_30295 = 2400;
        int ticks;
        private static final int field_30296 = 3;
        final List<BlockPos> possibleHives;
        private @Nullable Path path;
        private static final int field_30297 = 60;
        private int ticksUntilLost;

        MoveToHiveGoal() {
            this.possibleHives = Lists.newArrayList();
            this.setControls(EnumSet.of(Goal.Control.MOVE));
        }

        @Override
        public boolean canBeeStart() {
            return BeeEntity.this.hivePos != null && !BeeEntity.this.isTooFar(BeeEntity.this.hivePos) && !BeeEntity.this.hasPositionTarget() && BeeEntity.this.canEnterHive() && !this.isCloseEnough(BeeEntity.this.hivePos) && BeeEntity.this.getEntityWorld().getBlockState(BeeEntity.this.hivePos).isIn(BlockTags.BEEHIVES);
        }

        @Override
        public boolean canBeeContinue() {
            return this.canBeeStart();
        }

        @Override
        public void start() {
            this.ticks = 0;
            this.ticksUntilLost = 0;
            super.start();
        }

        @Override
        public void stop() {
            this.ticks = 0;
            this.ticksUntilLost = 0;
            BeeEntity.this.navigation.stop();
            BeeEntity.this.navigation.resetRangeMultiplier();
        }

        @Override
        public void tick() {
            if (BeeEntity.this.hivePos == null) {
                return;
            }
            ++this.ticks;
            if (this.ticks > this.getTickCount(2400)) {
                this.makeChosenHivePossibleHive();
                return;
            }
            if (BeeEntity.this.navigation.isFollowingPath()) {
                return;
            }
            if (BeeEntity.this.isWithinDistance(BeeEntity.this.hivePos, 16)) {
                boolean bl = this.startMovingToFar(BeeEntity.this.hivePos);
                if (!bl) {
                    this.makeChosenHivePossibleHive();
                } else if (this.path != null && BeeEntity.this.navigation.getCurrentPath().equalsPath(this.path)) {
                    ++this.ticksUntilLost;
                    if (this.ticksUntilLost > 60) {
                        BeeEntity.this.clearHivePos();
                        this.ticksUntilLost = 0;
                    }
                } else {
                    this.path = BeeEntity.this.navigation.getCurrentPath();
                }
                return;
            }
            if (BeeEntity.this.isTooFar(BeeEntity.this.hivePos)) {
                BeeEntity.this.clearHivePos();
                return;
            }
            BeeEntity.this.startMovingTo(BeeEntity.this.hivePos);
        }

        private boolean startMovingToFar(BlockPos pos) {
            int i = BeeEntity.this.isWithinDistance(pos, 3) ? 1 : 2;
            BeeEntity.this.navigation.setRangeMultiplier(10.0f);
            BeeEntity.this.navigation.startMovingTo(pos.getX(), pos.getY(), pos.getZ(), i, 1.0);
            return BeeEntity.this.navigation.getCurrentPath() != null && BeeEntity.this.navigation.getCurrentPath().reachesTarget();
        }

        boolean isPossibleHive(BlockPos pos) {
            return this.possibleHives.contains(pos);
        }

        private void addPossibleHive(BlockPos pos) {
            this.possibleHives.add(pos);
            while (this.possibleHives.size() > 3) {
                this.possibleHives.remove(0);
            }
        }

        void clearPossibleHives() {
            this.possibleHives.clear();
        }

        private void makeChosenHivePossibleHive() {
            if (BeeEntity.this.hivePos != null) {
                this.addPossibleHive(BeeEntity.this.hivePos);
            }
            BeeEntity.this.clearHivePos();
        }

        private boolean isCloseEnough(BlockPos pos) {
            if (BeeEntity.this.isWithinDistance(pos, 2)) {
                return true;
            }
            Path path = BeeEntity.this.navigation.getCurrentPath();
            return path != null && path.getTarget().equals(pos) && path.reachesTarget() && path.isFinished();
        }
    }

    public class MoveToFlowerGoal
    extends NotAngryGoal {
        private static final int MAX_FLOWER_NAVIGATION_TICKS = 2400;
        int ticks;

        MoveToFlowerGoal() {
            this.setControls(EnumSet.of(Goal.Control.MOVE));
        }

        @Override
        public boolean canBeeStart() {
            return BeeEntity.this.flowerPos != null && !BeeEntity.this.hasPositionTarget() && this.shouldMoveToFlower() && !BeeEntity.this.isWithinDistance(BeeEntity.this.flowerPos, 2);
        }

        @Override
        public boolean canBeeContinue() {
            return this.canBeeStart();
        }

        @Override
        public void start() {
            this.ticks = 0;
            super.start();
        }

        @Override
        public void stop() {
            this.ticks = 0;
            BeeEntity.this.navigation.stop();
            BeeEntity.this.navigation.resetRangeMultiplier();
        }

        @Override
        public void tick() {
            if (BeeEntity.this.flowerPos == null) {
                return;
            }
            ++this.ticks;
            if (this.ticks > this.getTickCount(2400)) {
                BeeEntity.this.clearFlowerPos();
                return;
            }
            if (BeeEntity.this.navigation.isFollowingPath()) {
                return;
            }
            if (BeeEntity.this.isTooFar(BeeEntity.this.flowerPos)) {
                BeeEntity.this.clearFlowerPos();
                return;
            }
            BeeEntity.this.startMovingTo(BeeEntity.this.flowerPos);
        }

        private boolean shouldMoveToFlower() {
            return BeeEntity.this.ticksSincePollination > 600;
        }
    }

    class GrowCropsGoal
    extends NotAngryGoal {
        static final int field_30299 = 30;

        GrowCropsGoal() {
        }

        @Override
        public boolean canBeeStart() {
            if (BeeEntity.this.getCropsGrownSincePollination() >= 10) {
                return false;
            }
            if (BeeEntity.this.random.nextFloat() < 0.3f) {
                return false;
            }
            return BeeEntity.this.hasNectar() && BeeEntity.this.hasValidHive();
        }

        @Override
        public boolean canBeeContinue() {
            return this.canBeeStart();
        }

        @Override
        public void tick() {
            if (BeeEntity.this.random.nextInt(this.getTickCount(30)) != 0) {
                return;
            }
            for (int i = 1; i <= 2; ++i) {
                Fertilizable fertilizable;
                BlockPos blockPos = BeeEntity.this.getBlockPos().down(i);
                BlockState blockState = BeeEntity.this.getEntityWorld().getBlockState(blockPos);
                Block block = blockState.getBlock();
                BlockState blockState2 = null;
                if (!blockState.isIn(BlockTags.BEE_GROWABLES)) continue;
                if (block instanceof CropBlock) {
                    CropBlock cropBlock = (CropBlock)block;
                    if (!cropBlock.isMature(blockState)) {
                        blockState2 = cropBlock.withAge(cropBlock.getAge(blockState) + 1);
                    }
                } else if (block instanceof StemBlock) {
                    int j = blockState.get(StemBlock.AGE);
                    if (j < 7) {
                        blockState2 = (BlockState)blockState.with(StemBlock.AGE, j + 1);
                    }
                } else if (blockState.isOf(Blocks.SWEET_BERRY_BUSH)) {
                    int j = blockState.get(SweetBerryBushBlock.AGE);
                    if (j < 3) {
                        blockState2 = (BlockState)blockState.with(SweetBerryBushBlock.AGE, j + 1);
                    }
                } else if ((blockState.isOf(Blocks.CAVE_VINES) || blockState.isOf(Blocks.CAVE_VINES_PLANT)) && (fertilizable = (Fertilizable)((Object)blockState.getBlock())).isFertilizable(BeeEntity.this.getEntityWorld(), blockPos, blockState)) {
                    fertilizable.grow((ServerWorld)BeeEntity.this.getEntityWorld(), BeeEntity.this.random, blockPos, blockState);
                    blockState2 = BeeEntity.this.getEntityWorld().getBlockState(blockPos);
                }
                if (blockState2 == null) continue;
                BeeEntity.this.getEntityWorld().syncWorldEvent(2011, blockPos, 15);
                BeeEntity.this.getEntityWorld().setBlockState(blockPos, blockState2);
                BeeEntity.this.addCropCounter();
            }
        }
    }

    class BeeWanderAroundGoal
    extends Goal {
        BeeWanderAroundGoal() {
            this.setControls(EnumSet.of(Goal.Control.MOVE));
        }

        @Override
        public boolean canStart() {
            return BeeEntity.this.navigation.isIdle() && BeeEntity.this.random.nextInt(10) == 0;
        }

        @Override
        public boolean shouldContinue() {
            return BeeEntity.this.navigation.isFollowingPath();
        }

        @Override
        public void start() {
            Vec3d vec3d = this.getRandomLocation();
            if (vec3d != null) {
                BeeEntity.this.navigation.startMovingAlong(BeeEntity.this.navigation.findPathTo(BlockPos.ofFloored(vec3d), 1), 1.0);
            }
        }

        private @Nullable Vec3d getRandomLocation() {
            Vec3d vec3d2;
            if (BeeEntity.this.hasValidHive() && !BeeEntity.this.isWithinDistance(BeeEntity.this.hivePos, this.getMaxWanderDistance())) {
                Vec3d vec3d = Vec3d.ofCenter(BeeEntity.this.hivePos);
                vec3d2 = vec3d.subtract(BeeEntity.this.getEntityPos()).normalize();
            } else {
                vec3d2 = BeeEntity.this.getRotationVec(0.0f);
            }
            int i = 8;
            Vec3d vec3d3 = AboveGroundTargeting.find(BeeEntity.this, 8, 7, vec3d2.x, vec3d2.z, 1.5707964f, 3, 1);
            if (vec3d3 != null) {
                return vec3d3;
            }
            return NoPenaltySolidTargeting.find(BeeEntity.this, 8, 4, -2, vec3d2.x, vec3d2.z, 1.5707963705062866);
        }

        private int getMaxWanderDistance() {
            int i = BeeEntity.this.hasHivePos() || BeeEntity.this.hasFlower() ? 24 : 16;
            return 48 - i;
        }
    }

    class BeeRevengeGoal
    extends RevengeGoal {
        BeeRevengeGoal(BeeEntity bee) {
            super(bee, new Class[0]);
        }

        @Override
        public boolean shouldContinue() {
            return BeeEntity.this.hasAngerTime() && super.shouldContinue();
        }

        @Override
        protected void setMobEntityTarget(MobEntity mob, LivingEntity target) {
            if (mob instanceof BeeEntity && this.mob.canSee(target)) {
                mob.setTarget(target);
            }
        }
    }

    static class StingTargetGoal
    extends ActiveTargetGoal<PlayerEntity> {
        StingTargetGoal(BeeEntity bee) {
            super(bee, PlayerEntity.class, 10, true, false, bee::shouldAngerAt);
        }

        @Override
        public boolean canStart() {
            return this.canSting() && super.canStart();
        }

        @Override
        public boolean shouldContinue() {
            boolean bl = this.canSting();
            if (!bl || this.mob.getTarget() == null) {
                this.target = null;
                return false;
            }
            return super.shouldContinue();
        }

        private boolean canSting() {
            BeeEntity beeEntity = (BeeEntity)this.mob;
            return beeEntity.hasAngerTime() && !beeEntity.hasStung();
        }
    }

    abstract class NotAngryGoal
    extends Goal {
        NotAngryGoal() {
        }

        public abstract boolean canBeeStart();

        public abstract boolean canBeeContinue();

        @Override
        public boolean canStart() {
            return this.canBeeStart() && !BeeEntity.this.hasAngerTime();
        }

        @Override
        public boolean shouldContinue() {
            return this.canBeeContinue() && !BeeEntity.this.hasAngerTime();
        }
    }
}


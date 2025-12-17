/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  com.mojang.serialization.Codec
 *  io.netty.buffer.ByteBuf
 *  org.jspecify.annotations.Nullable
 */
package net.minecraft.entity.passive;

import com.mojang.serialization.Codec;
import io.netty.buffer.ByteBuf;
import java.util.EnumSet;
import java.util.List;
import java.util.Optional;
import java.util.function.IntFunction;
import java.util.function.Predicate;
import java.util.stream.Stream;
import net.minecraft.advancement.criterion.Criteria;
import net.minecraft.block.Block;
import net.minecraft.block.BlockState;
import net.minecraft.block.Blocks;
import net.minecraft.block.CaveVines;
import net.minecraft.block.SweetBerryBushBlock;
import net.minecraft.component.ComponentType;
import net.minecraft.component.ComponentsAccess;
import net.minecraft.component.DataComponentTypes;
import net.minecraft.entity.Entity;
import net.minecraft.entity.EntityData;
import net.minecraft.entity.EntityDimensions;
import net.minecraft.entity.EntityPose;
import net.minecraft.entity.EntityType;
import net.minecraft.entity.EquipmentSlot;
import net.minecraft.entity.ExperienceOrbEntity;
import net.minecraft.entity.ItemEntity;
import net.minecraft.entity.LazyEntityReference;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.SpawnReason;
import net.minecraft.entity.ai.TargetPredicate;
import net.minecraft.entity.ai.control.LookControl;
import net.minecraft.entity.ai.control.MoveControl;
import net.minecraft.entity.ai.goal.ActiveTargetGoal;
import net.minecraft.entity.ai.goal.AnimalMateGoal;
import net.minecraft.entity.ai.goal.DiveJumpingGoal;
import net.minecraft.entity.ai.goal.EscapeDangerGoal;
import net.minecraft.entity.ai.goal.EscapeSunlightGoal;
import net.minecraft.entity.ai.goal.FleeEntityGoal;
import net.minecraft.entity.ai.goal.Goal;
import net.minecraft.entity.ai.goal.MeleeAttackGoal;
import net.minecraft.entity.ai.goal.MoveToTargetPosGoal;
import net.minecraft.entity.ai.goal.PounceAtTargetGoal;
import net.minecraft.entity.ai.goal.PowderSnowJumpGoal;
import net.minecraft.entity.ai.goal.SwimGoal;
import net.minecraft.entity.ai.goal.WanderAroundFarGoal;
import net.minecraft.entity.ai.pathing.PathNodeType;
import net.minecraft.entity.attribute.DefaultAttributeContainer;
import net.minecraft.entity.attribute.EntityAttributes;
import net.minecraft.entity.damage.DamageSource;
import net.minecraft.entity.data.DataTracker;
import net.minecraft.entity.data.TrackedData;
import net.minecraft.entity.data.TrackedDataHandlerRegistry;
import net.minecraft.entity.mob.HostileEntity;
import net.minecraft.entity.mob.MobEntity;
import net.minecraft.entity.passive.AnimalEntity;
import net.minecraft.entity.passive.ChickenEntity;
import net.minecraft.entity.passive.FishEntity;
import net.minecraft.entity.passive.PassiveEntity;
import net.minecraft.entity.passive.PolarBearEntity;
import net.minecraft.entity.passive.RabbitEntity;
import net.minecraft.entity.passive.SchoolingFishEntity;
import net.minecraft.entity.passive.TameableEntity;
import net.minecraft.entity.passive.TurtleEntity;
import net.minecraft.entity.passive.WolfEntity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.ItemStack;
import net.minecraft.item.Items;
import net.minecraft.network.codec.PacketCodec;
import net.minecraft.network.codec.PacketCodecs;
import net.minecraft.particle.ItemStackParticleEffect;
import net.minecraft.particle.ParticleTypes;
import net.minecraft.predicate.entity.EntityPredicates;
import net.minecraft.registry.entry.RegistryEntry;
import net.minecraft.registry.tag.BiomeTags;
import net.minecraft.registry.tag.BlockTags;
import net.minecraft.registry.tag.FluidTags;
import net.minecraft.registry.tag.ItemTags;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.sound.SoundEvent;
import net.minecraft.sound.SoundEvents;
import net.minecraft.stat.Stats;
import net.minecraft.storage.ReadView;
import net.minecraft.storage.WriteView;
import net.minecraft.util.StringIdentifiable;
import net.minecraft.util.function.ValueLists;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.MathHelper;
import net.minecraft.util.math.Vec3d;
import net.minecraft.util.math.random.Random;
import net.minecraft.world.LocalDifficulty;
import net.minecraft.world.ServerWorldAccess;
import net.minecraft.world.World;
import net.minecraft.world.WorldAccess;
import net.minecraft.world.WorldView;
import net.minecraft.world.biome.Biome;
import net.minecraft.world.event.GameEvent;
import net.minecraft.world.rule.GameRules;
import org.jspecify.annotations.Nullable;

public class FoxEntity
extends AnimalEntity {
    private static final TrackedData<Integer> VARIANT = DataTracker.registerData(FoxEntity.class, TrackedDataHandlerRegistry.INTEGER);
    private static final TrackedData<Byte> FOX_FLAGS = DataTracker.registerData(FoxEntity.class, TrackedDataHandlerRegistry.BYTE);
    private static final int SITTING_FLAG = 1;
    public static final int CROUCHING_FLAG = 4;
    public static final int ROLLING_HEAD_FLAG = 8;
    public static final int CHASING_FLAG = 16;
    private static final int SLEEPING_FLAG = 32;
    private static final int WALKING_FLAG = 64;
    private static final int AGGRESSIVE_FLAG = 128;
    private static final TrackedData<Optional<LazyEntityReference<LivingEntity>>> OWNER = DataTracker.registerData(FoxEntity.class, TrackedDataHandlerRegistry.LAZY_ENTITY_REFERENCE);
    private static final TrackedData<Optional<LazyEntityReference<LivingEntity>>> OTHER_TRUSTED = DataTracker.registerData(FoxEntity.class, TrackedDataHandlerRegistry.LAZY_ENTITY_REFERENCE);
    static final Predicate<ItemEntity> PICKABLE_DROP_FILTER = item -> !item.cannotPickup() && item.isAlive();
    private static final Predicate<Entity> JUST_ATTACKED_SOMETHING_FILTER = entity -> {
        if (entity instanceof LivingEntity) {
            LivingEntity livingEntity = (LivingEntity)entity;
            return livingEntity.getAttacking() != null && livingEntity.getLastAttackTime() < livingEntity.age + 600;
        }
        return false;
    };
    static final Predicate<Entity> CHICKEN_AND_RABBIT_FILTER = entity -> entity instanceof ChickenEntity || entity instanceof RabbitEntity;
    private static final Predicate<Entity> NOTICEABLE_PLAYER_FILTER = entity -> !entity.isSneaky() && EntityPredicates.EXCEPT_CREATIVE_OR_SPECTATOR.test((Entity)entity);
    private static final int EATING_DURATION = 600;
    private static final EntityDimensions BABY_BASE_DIMENSIONS = EntityType.FOX.getDimensions().scaled(0.5f).withEyeHeight(0.2975f);
    private static final Codec<List<LazyEntityReference<LivingEntity>>> TRUSTED_ENTITIES_CODEC = LazyEntityReference.createCodec().listOf();
    private static final boolean DEFAULT_SLEEPING = false;
    private static final boolean DEFAULT_SITTING = false;
    private static final boolean DEFAULT_CROUCHING = false;
    private Goal followChickenAndRabbitGoal;
    private Goal followBabyTurtleGoal;
    private Goal followFishGoal;
    private float headRollProgress;
    private float lastHeadRollProgress;
    float extraRollingHeight;
    float lastExtraRollingHeight;
    private int eatingTime;

    public FoxEntity(EntityType<? extends FoxEntity> entityType, World world) {
        super((EntityType<? extends AnimalEntity>)entityType, world);
        this.lookControl = new FoxLookControl();
        this.moveControl = new FoxMoveControl();
        this.setPathfindingPenalty(PathNodeType.DANGER_OTHER, 0.0f);
        this.setPathfindingPenalty(PathNodeType.DAMAGE_OTHER, 0.0f);
        this.setCanPickUpLoot(true);
        this.getNavigation().setMaxFollowRange(32.0f);
    }

    @Override
    protected void initDataTracker(DataTracker.Builder builder) {
        super.initDataTracker(builder);
        builder.add(OWNER, Optional.empty());
        builder.add(OTHER_TRUSTED, Optional.empty());
        builder.add(VARIANT, Variant.DEFAULT.getIndex());
        builder.add(FOX_FLAGS, (byte)0);
    }

    @Override
    protected void initGoals() {
        this.followChickenAndRabbitGoal = new ActiveTargetGoal<AnimalEntity>(this, AnimalEntity.class, 10, false, false, (entity, world) -> entity instanceof ChickenEntity || entity instanceof RabbitEntity);
        this.followBabyTurtleGoal = new ActiveTargetGoal<TurtleEntity>(this, TurtleEntity.class, 10, false, false, TurtleEntity.BABY_TURTLE_ON_LAND_FILTER);
        this.followFishGoal = new ActiveTargetGoal<FishEntity>(this, FishEntity.class, 20, false, false, (entity, world) -> entity instanceof SchoolingFishEntity);
        this.goalSelector.add(0, new FoxSwimGoal());
        this.goalSelector.add(0, new PowderSnowJumpGoal(this, this.getEntityWorld()));
        this.goalSelector.add(1, new StopWanderingGoal());
        this.goalSelector.add(2, new EscapeWhenNotAggressiveGoal(2.2));
        this.goalSelector.add(3, new MateGoal(this, 1.0));
        this.goalSelector.add(4, new FleeEntityGoal<PlayerEntity>(this, PlayerEntity.class, 16.0f, 1.6, 1.4, entity -> NOTICEABLE_PLAYER_FILTER.test((Entity)entity) && !this.canTrust((LivingEntity)entity) && !this.isAggressive()));
        this.goalSelector.add(4, new FleeEntityGoal<WolfEntity>(this, WolfEntity.class, 8.0f, 1.6, 1.4, entity -> !((WolfEntity)entity).isTamed() && !this.isAggressive()));
        this.goalSelector.add(4, new FleeEntityGoal<PolarBearEntity>(this, PolarBearEntity.class, 8.0f, 1.6, 1.4, entity -> !this.isAggressive()));
        this.goalSelector.add(5, new MoveToHuntGoal());
        this.goalSelector.add(6, new JumpChasingGoal());
        this.goalSelector.add(6, new AvoidDaylightGoal(1.25));
        this.goalSelector.add(7, new AttackGoal((double)1.2f, true));
        this.goalSelector.add(7, new DelayedCalmDownGoal());
        this.goalSelector.add(8, new FollowParentGoal(this, 1.25));
        this.goalSelector.add(9, new GoToVillageGoal(32, 200));
        this.goalSelector.add(10, new EatBerriesGoal((double)1.2f, 12, 1));
        this.goalSelector.add(10, new PounceAtTargetGoal(this, 0.4f));
        this.goalSelector.add(11, new WanderAroundFarGoal(this, 1.0));
        this.goalSelector.add(11, new PickupItemGoal());
        this.goalSelector.add(12, new LookAtEntityGoal(this, PlayerEntity.class, 24.0f));
        this.goalSelector.add(13, new SitDownAndLookAroundGoal());
        this.targetSelector.add(3, new DefendFriendGoal(LivingEntity.class, false, false, (entity, world) -> JUST_ATTACKED_SOMETHING_FILTER.test(entity) && !this.canTrust(entity)));
    }

    @Override
    public void tickMovement() {
        if (!this.getEntityWorld().isClient() && this.isAlive() && this.canActVoluntarily()) {
            LivingEntity livingEntity;
            ++this.eatingTime;
            ItemStack itemStack = this.getEquippedStack(EquipmentSlot.MAINHAND);
            if (this.canEat(itemStack)) {
                if (this.eatingTime > 600) {
                    ItemStack itemStack2 = itemStack.finishUsing(this.getEntityWorld(), this);
                    if (!itemStack2.isEmpty()) {
                        this.equipStack(EquipmentSlot.MAINHAND, itemStack2);
                    }
                    this.eatingTime = 0;
                } else if (this.eatingTime > 560 && this.random.nextFloat() < 0.1f) {
                    this.playEatSound();
                    this.getEntityWorld().sendEntityStatus(this, (byte)45);
                }
            }
            if ((livingEntity = this.getTarget()) == null || !livingEntity.isAlive()) {
                this.setCrouching(false);
                this.setRollingHead(false);
            }
        }
        if (this.isSleeping() || this.isImmobile()) {
            this.jumping = false;
            this.sidewaysSpeed = 0.0f;
            this.forwardSpeed = 0.0f;
        }
        super.tickMovement();
        if (this.isAggressive() && this.random.nextFloat() < 0.05f) {
            this.playSound(SoundEvents.ENTITY_FOX_AGGRO, 1.0f, 1.0f);
        }
    }

    @Override
    protected boolean isImmobile() {
        return this.isDead();
    }

    private boolean canEat(ItemStack stack) {
        return this.isConsumableFood(stack) && this.getTarget() == null && this.isOnGround() && !this.isSleeping();
    }

    private boolean isConsumableFood(ItemStack stack) {
        return stack.contains(DataComponentTypes.FOOD) && stack.contains(DataComponentTypes.CONSUMABLE);
    }

    @Override
    protected void initEquipment(Random random, LocalDifficulty localDifficulty) {
        if (random.nextFloat() < 0.2f) {
            float f = random.nextFloat();
            ItemStack itemStack = f < 0.05f ? new ItemStack(Items.EMERALD) : (f < 0.2f ? new ItemStack(Items.EGG) : (f < 0.4f ? (random.nextBoolean() ? new ItemStack(Items.RABBIT_FOOT) : new ItemStack(Items.RABBIT_HIDE)) : (f < 0.6f ? new ItemStack(Items.WHEAT) : (f < 0.8f ? new ItemStack(Items.LEATHER) : new ItemStack(Items.FEATHER)))));
            this.equipStack(EquipmentSlot.MAINHAND, itemStack);
        }
    }

    @Override
    public void handleStatus(byte status) {
        if (status == 45) {
            ItemStack itemStack = this.getEquippedStack(EquipmentSlot.MAINHAND);
            if (!itemStack.isEmpty()) {
                for (int i = 0; i < 8; ++i) {
                    Vec3d vec3d = new Vec3d(((double)this.random.nextFloat() - 0.5) * 0.1, (double)this.random.nextFloat() * 0.1 + 0.1, 0.0).rotateX(-this.getPitch() * ((float)Math.PI / 180)).rotateY(-this.getYaw() * ((float)Math.PI / 180));
                    this.getEntityWorld().addParticleClient(new ItemStackParticleEffect(ParticleTypes.ITEM, itemStack), this.getX() + this.getRotationVector().x / 2.0, this.getY(), this.getZ() + this.getRotationVector().z / 2.0, vec3d.x, vec3d.y + 0.05, vec3d.z);
                }
            }
        } else {
            super.handleStatus(status);
        }
    }

    public static DefaultAttributeContainer.Builder createFoxAttributes() {
        return AnimalEntity.createAnimalAttributes().add(EntityAttributes.MOVEMENT_SPEED, 0.3f).add(EntityAttributes.MAX_HEALTH, 10.0).add(EntityAttributes.ATTACK_DAMAGE, 2.0).add(EntityAttributes.SAFE_FALL_DISTANCE, 5.0).add(EntityAttributes.FOLLOW_RANGE, 32.0);
    }

    @Override
    public @Nullable FoxEntity createChild(ServerWorld serverWorld, PassiveEntity passiveEntity) {
        FoxEntity foxEntity = EntityType.FOX.create(serverWorld, SpawnReason.BREEDING);
        if (foxEntity != null) {
            foxEntity.setVariant(this.random.nextBoolean() ? this.getVariant() : ((FoxEntity)passiveEntity).getVariant());
        }
        return foxEntity;
    }

    public static boolean canSpawn(EntityType<FoxEntity> type, WorldAccess world, SpawnReason spawnReason, BlockPos pos, Random random) {
        return world.getBlockState(pos.down()).isIn(BlockTags.FOXES_SPAWNABLE_ON) && FoxEntity.isLightLevelValidForNaturalSpawn(world, pos);
    }

    @Override
    public @Nullable EntityData initialize(ServerWorldAccess world, LocalDifficulty difficulty, SpawnReason spawnReason, @Nullable EntityData entityData) {
        RegistryEntry<Biome> registryEntry = world.getBiome(this.getBlockPos());
        Variant variant = Variant.fromBiome(registryEntry);
        boolean bl = false;
        if (entityData instanceof FoxData) {
            FoxData foxData = (FoxData)entityData;
            variant = foxData.type;
            if (foxData.getSpawnedCount() >= 2) {
                bl = true;
            }
        } else {
            entityData = new FoxData(variant);
        }
        this.setVariant(variant);
        if (bl) {
            this.setBreedingAge(-24000);
        }
        if (world instanceof ServerWorld) {
            this.addTypeSpecificGoals();
        }
        this.initEquipment(world.getRandom(), difficulty);
        return super.initialize(world, difficulty, spawnReason, entityData);
    }

    private void addTypeSpecificGoals() {
        if (this.getVariant() == Variant.RED) {
            this.targetSelector.add(4, this.followChickenAndRabbitGoal);
            this.targetSelector.add(4, this.followBabyTurtleGoal);
            this.targetSelector.add(6, this.followFishGoal);
        } else {
            this.targetSelector.add(4, this.followFishGoal);
            this.targetSelector.add(6, this.followChickenAndRabbitGoal);
            this.targetSelector.add(6, this.followBabyTurtleGoal);
        }
    }

    @Override
    protected void playEatSound() {
        this.playSound(SoundEvents.ENTITY_FOX_EAT, 1.0f, 1.0f);
    }

    @Override
    public EntityDimensions getBaseDimensions(EntityPose pose) {
        return this.isBaby() ? BABY_BASE_DIMENSIONS : super.getBaseDimensions(pose);
    }

    public Variant getVariant() {
        return Variant.byIndex(this.dataTracker.get(VARIANT));
    }

    private void setVariant(Variant variant) {
        this.dataTracker.set(VARIANT, variant.getIndex());
    }

    @Override
    public <T> @Nullable T get(ComponentType<? extends T> type) {
        if (type == DataComponentTypes.FOX_VARIANT) {
            return FoxEntity.castComponentValue(type, this.getVariant());
        }
        return super.get(type);
    }

    @Override
    protected void copyComponentsFrom(ComponentsAccess from) {
        this.copyComponentFrom(from, DataComponentTypes.FOX_VARIANT);
        super.copyComponentsFrom(from);
    }

    @Override
    protected <T> boolean setApplicableComponent(ComponentType<T> type, T value) {
        if (type == DataComponentTypes.FOX_VARIANT) {
            this.setVariant(FoxEntity.castComponentValue(DataComponentTypes.FOX_VARIANT, value));
            return true;
        }
        return super.setApplicableComponent(type, value);
    }

    Stream<LazyEntityReference<LivingEntity>> getTrustedEntities() {
        return Stream.concat(this.dataTracker.get(OWNER).stream(), this.dataTracker.get(OTHER_TRUSTED).stream());
    }

    void trust(LivingEntity entity) {
        this.trust(LazyEntityReference.of(entity));
    }

    private void trust(LazyEntityReference<LivingEntity> entity) {
        if (this.dataTracker.get(OWNER).isPresent()) {
            this.dataTracker.set(OTHER_TRUSTED, Optional.of(entity));
        } else {
            this.dataTracker.set(OWNER, Optional.of(entity));
        }
    }

    @Override
    protected void writeCustomData(WriteView view) {
        super.writeCustomData(view);
        view.put("Trusted", TRUSTED_ENTITIES_CODEC, this.getTrustedEntities().toList());
        view.putBoolean("Sleeping", this.isSleeping());
        view.put("Type", Variant.CODEC, this.getVariant());
        view.putBoolean("Sitting", this.isSitting());
        view.putBoolean("Crouching", this.isInSneakingPose());
    }

    @Override
    protected void readCustomData(ReadView view) {
        super.readCustomData(view);
        this.clearTrusted();
        view.read("Trusted", TRUSTED_ENTITIES_CODEC).orElse(List.of()).forEach(this::trust);
        this.setSleeping(view.getBoolean("Sleeping", false));
        this.setVariant(view.read("Type", Variant.CODEC).orElse(Variant.DEFAULT));
        this.setSitting(view.getBoolean("Sitting", false));
        this.setCrouching(view.getBoolean("Crouching", false));
        if (this.getEntityWorld() instanceof ServerWorld) {
            this.addTypeSpecificGoals();
        }
    }

    private void clearTrusted() {
        this.dataTracker.set(OWNER, Optional.empty());
        this.dataTracker.set(OTHER_TRUSTED, Optional.empty());
    }

    public boolean isSitting() {
        return this.getFoxFlag(1);
    }

    public void setSitting(boolean sitting) {
        this.setFoxFlag(1, sitting);
    }

    public boolean isWalking() {
        return this.getFoxFlag(64);
    }

    void setWalking(boolean walking) {
        this.setFoxFlag(64, walking);
    }

    boolean isAggressive() {
        return this.getFoxFlag(128);
    }

    void setAggressive(boolean aggressive) {
        this.setFoxFlag(128, aggressive);
    }

    @Override
    public boolean isSleeping() {
        return this.getFoxFlag(32);
    }

    void setSleeping(boolean sleeping) {
        this.setFoxFlag(32, sleeping);
    }

    private void setFoxFlag(int mask, boolean value) {
        if (value) {
            this.dataTracker.set(FOX_FLAGS, (byte)(this.dataTracker.get(FOX_FLAGS) | mask));
        } else {
            this.dataTracker.set(FOX_FLAGS, (byte)(this.dataTracker.get(FOX_FLAGS) & ~mask));
        }
    }

    private boolean getFoxFlag(int bitmask) {
        return (this.dataTracker.get(FOX_FLAGS) & bitmask) != 0;
    }

    @Override
    protected boolean canDispenserEquipSlot(EquipmentSlot slot) {
        return slot == EquipmentSlot.MAINHAND && this.canPickUpLoot();
    }

    @Override
    public boolean canPickupItem(ItemStack stack) {
        ItemStack itemStack = this.getEquippedStack(EquipmentSlot.MAINHAND);
        return itemStack.isEmpty() || this.eatingTime > 0 && this.isConsumableFood(stack) && !this.isConsumableFood(itemStack);
    }

    private void spit(ItemStack stack) {
        if (stack.isEmpty() || this.getEntityWorld().isClient()) {
            return;
        }
        ItemEntity itemEntity = new ItemEntity(this.getEntityWorld(), this.getX() + this.getRotationVector().x, this.getY() + 1.0, this.getZ() + this.getRotationVector().z, stack);
        itemEntity.setPickupDelay(40);
        itemEntity.setThrower(this);
        this.playSound(SoundEvents.ENTITY_FOX_SPIT, 1.0f, 1.0f);
        this.getEntityWorld().spawnEntity(itemEntity);
    }

    private void dropItem(ItemStack stack) {
        ItemEntity itemEntity = new ItemEntity(this.getEntityWorld(), this.getX(), this.getY(), this.getZ(), stack);
        this.getEntityWorld().spawnEntity(itemEntity);
    }

    @Override
    protected void loot(ServerWorld world, ItemEntity itemEntity) {
        ItemStack itemStack = itemEntity.getStack();
        if (this.canPickupItem(itemStack)) {
            int i = itemStack.getCount();
            if (i > 1) {
                this.dropItem(itemStack.split(i - 1));
            }
            this.spit(this.getEquippedStack(EquipmentSlot.MAINHAND));
            this.triggerItemPickedUpByEntityCriteria(itemEntity);
            this.equipStack(EquipmentSlot.MAINHAND, itemStack.split(1));
            this.setDropGuaranteed(EquipmentSlot.MAINHAND);
            this.sendPickup(itemEntity, itemStack.getCount());
            itemEntity.discard();
            this.eatingTime = 0;
        }
    }

    @Override
    public void tick() {
        super.tick();
        if (this.canActVoluntarily()) {
            boolean bl = this.isTouchingWater();
            if (bl || this.getTarget() != null || this.getEntityWorld().isThundering()) {
                this.stopSleeping();
            }
            if (bl || this.isSleeping()) {
                this.setSitting(false);
            }
            if (this.isWalking() && this.getEntityWorld().random.nextFloat() < 0.2f) {
                BlockPos blockPos = this.getBlockPos();
                BlockState blockState = this.getEntityWorld().getBlockState(blockPos);
                this.getEntityWorld().syncWorldEvent(2001, blockPos, Block.getRawIdFromState(blockState));
            }
        }
        this.lastHeadRollProgress = this.headRollProgress;
        this.headRollProgress = this.isRollingHead() ? (this.headRollProgress += (1.0f - this.headRollProgress) * 0.4f) : (this.headRollProgress += (0.0f - this.headRollProgress) * 0.4f);
        this.lastExtraRollingHeight = this.extraRollingHeight;
        if (this.isInSneakingPose()) {
            this.extraRollingHeight += 0.2f;
            if (this.extraRollingHeight > 3.0f) {
                this.extraRollingHeight = 3.0f;
            }
        } else {
            this.extraRollingHeight = 0.0f;
        }
    }

    @Override
    public boolean isBreedingItem(ItemStack stack) {
        return stack.isIn(ItemTags.FOX_FOOD);
    }

    @Override
    protected void onPlayerSpawnedChild(PlayerEntity player, MobEntity child) {
        ((FoxEntity)child).trust(player);
    }

    public boolean isChasing() {
        return this.getFoxFlag(16);
    }

    public void setChasing(boolean chasing) {
        this.setFoxFlag(16, chasing);
    }

    public boolean isFullyCrouched() {
        return this.extraRollingHeight == 3.0f;
    }

    public void setCrouching(boolean crouching) {
        this.setFoxFlag(4, crouching);
    }

    @Override
    public boolean isInSneakingPose() {
        return this.getFoxFlag(4);
    }

    public void setRollingHead(boolean rollingHead) {
        this.setFoxFlag(8, rollingHead);
    }

    public boolean isRollingHead() {
        return this.getFoxFlag(8);
    }

    public float getHeadRoll(float tickProgress) {
        return MathHelper.lerp(tickProgress, this.lastHeadRollProgress, this.headRollProgress) * 0.11f * (float)Math.PI;
    }

    public float getBodyRotationHeightOffset(float tickProgress) {
        return MathHelper.lerp(tickProgress, this.lastExtraRollingHeight, this.extraRollingHeight);
    }

    @Override
    public void setTarget(@Nullable LivingEntity target) {
        if (this.isAggressive() && target == null) {
            this.setAggressive(false);
        }
        super.setTarget(target);
    }

    void stopSleeping() {
        this.setSleeping(false);
    }

    void stopActions() {
        this.setRollingHead(false);
        this.setCrouching(false);
        this.setSitting(false);
        this.setSleeping(false);
        this.setAggressive(false);
        this.setWalking(false);
    }

    boolean wantsToPickupItem() {
        return !this.isSleeping() && !this.isSitting() && !this.isWalking();
    }

    @Override
    public void playAmbientSound() {
        SoundEvent soundEvent = this.getAmbientSound();
        if (soundEvent == SoundEvents.ENTITY_FOX_SCREECH) {
            this.playSound(soundEvent, 2.0f, this.getSoundPitch());
        } else {
            super.playAmbientSound();
        }
    }

    @Override
    protected @Nullable SoundEvent getAmbientSound() {
        List<Entity> list;
        if (this.isSleeping()) {
            return SoundEvents.ENTITY_FOX_SLEEP;
        }
        if (!this.getEntityWorld().isDay() && this.random.nextFloat() < 0.1f && (list = this.getEntityWorld().getEntitiesByClass(PlayerEntity.class, this.getBoundingBox().expand(16.0, 16.0, 16.0), EntityPredicates.EXCEPT_SPECTATOR)).isEmpty()) {
            return SoundEvents.ENTITY_FOX_SCREECH;
        }
        return SoundEvents.ENTITY_FOX_AMBIENT;
    }

    @Override
    protected @Nullable SoundEvent getHurtSound(DamageSource source) {
        return SoundEvents.ENTITY_FOX_HURT;
    }

    @Override
    protected @Nullable SoundEvent getDeathSound() {
        return SoundEvents.ENTITY_FOX_DEATH;
    }

    boolean canTrust(LivingEntity entity) {
        return this.getTrustedEntities().anyMatch(trusted -> trusted.uuidEquals(entity));
    }

    @Override
    protected void drop(ServerWorld world, DamageSource damageSource) {
        ItemStack itemStack = this.getEquippedStack(EquipmentSlot.MAINHAND);
        if (!itemStack.isEmpty()) {
            this.dropStack(world, itemStack);
            this.equipStack(EquipmentSlot.MAINHAND, ItemStack.EMPTY);
        }
        super.drop(world, damageSource);
    }

    public static boolean canJumpChase(FoxEntity fox, LivingEntity chasedEntity) {
        double d = chasedEntity.getZ() - fox.getZ();
        double e = chasedEntity.getX() - fox.getX();
        double f = d / e;
        int i = 6;
        for (int j = 0; j < 6; ++j) {
            double g = f == 0.0 ? 0.0 : d * (double)((float)j / 6.0f);
            double h = f == 0.0 ? e * (double)((float)j / 6.0f) : g / f;
            for (int k = 1; k < 4; ++k) {
                if (fox.getEntityWorld().getBlockState(BlockPos.ofFloored(fox.getX() + h, fox.getY() + (double)k, fox.getZ() + g)).isReplaceable()) continue;
                return false;
            }
        }
        return true;
    }

    @Override
    public Vec3d getLeashOffset() {
        return new Vec3d(0.0, 0.55f * this.getStandingEyeHeight(), this.getWidth() * 0.4f);
    }

    @Override
    public /* synthetic */ @Nullable PassiveEntity createChild(ServerWorld world, PassiveEntity entity) {
        return this.createChild(world, entity);
    }

    public class FoxLookControl
    extends LookControl {
        public FoxLookControl() {
            super(FoxEntity.this);
        }

        @Override
        public void tick() {
            if (!FoxEntity.this.isSleeping()) {
                super.tick();
            }
        }

        @Override
        protected boolean shouldStayHorizontal() {
            return !FoxEntity.this.isChasing() && !FoxEntity.this.isInSneakingPose() && !FoxEntity.this.isRollingHead() && !FoxEntity.this.isWalking();
        }
    }

    class FoxMoveControl
    extends MoveControl {
        public FoxMoveControl() {
            super(FoxEntity.this);
        }

        @Override
        public void tick() {
            if (FoxEntity.this.wantsToPickupItem()) {
                super.tick();
            }
        }
    }

    public static final class Variant
    extends Enum<Variant>
    implements StringIdentifiable {
        public static final /* enum */ Variant RED = new Variant(0, "red");
        public static final /* enum */ Variant SNOW = new Variant(1, "snow");
        public static final Variant DEFAULT;
        public static final StringIdentifiable.EnumCodec<Variant> CODEC;
        private static final IntFunction<Variant> INDEX_MAPPER;
        public static final PacketCodec<ByteBuf, Variant> PACKET_CODEC;
        private final int index;
        private final String id;
        private static final /* synthetic */ Variant[] field_18003;

        public static Variant[] values() {
            return (Variant[])field_18003.clone();
        }

        public static Variant valueOf(String string) {
            return Enum.valueOf(Variant.class, string);
        }

        private Variant(int index, String id) {
            this.index = index;
            this.id = id;
        }

        @Override
        public String asString() {
            return this.id;
        }

        public int getIndex() {
            return this.index;
        }

        public static Variant byIndex(int index) {
            return INDEX_MAPPER.apply(index);
        }

        public static Variant fromBiome(RegistryEntry<Biome> biome) {
            return biome.isIn(BiomeTags.SPAWNS_SNOW_FOXES) ? SNOW : RED;
        }

        private static /* synthetic */ Variant[] method_36637() {
            return new Variant[]{RED, SNOW};
        }

        static {
            field_18003 = Variant.method_36637();
            DEFAULT = RED;
            CODEC = StringIdentifiable.createCodec(Variant::values);
            INDEX_MAPPER = ValueLists.createIndexToValueFunction(Variant::getIndex, Variant.values(), ValueLists.OutOfBoundsHandling.ZERO);
            PACKET_CODEC = PacketCodecs.indexed(INDEX_MAPPER, Variant::getIndex);
        }
    }

    class FoxSwimGoal
    extends SwimGoal {
        public FoxSwimGoal() {
            super(FoxEntity.this);
        }

        @Override
        public void start() {
            super.start();
            FoxEntity.this.stopActions();
        }

        @Override
        public boolean canStart() {
            return FoxEntity.this.isTouchingWater() && FoxEntity.this.getFluidHeight(FluidTags.WATER) > 0.25 || FoxEntity.this.isInLava();
        }
    }

    class StopWanderingGoal
    extends Goal {
        int timer;

        public StopWanderingGoal() {
            this.setControls(EnumSet.of(Goal.Control.LOOK, Goal.Control.JUMP, Goal.Control.MOVE));
        }

        @Override
        public boolean canStart() {
            return FoxEntity.this.isWalking();
        }

        @Override
        public boolean shouldContinue() {
            return this.canStart() && this.timer > 0;
        }

        @Override
        public void start() {
            this.timer = this.getTickCount(40);
        }

        @Override
        public void stop() {
            FoxEntity.this.setWalking(false);
        }

        @Override
        public void tick() {
            --this.timer;
        }
    }

    class EscapeWhenNotAggressiveGoal
    extends EscapeDangerGoal {
        public EscapeWhenNotAggressiveGoal(double speed) {
            super(FoxEntity.this, speed);
        }

        @Override
        public boolean isInDanger() {
            return !FoxEntity.this.isAggressive() && super.isInDanger();
        }
    }

    class MateGoal
    extends AnimalMateGoal {
        public MateGoal(FoxEntity fox, double chance) {
            super(fox, chance);
        }

        @Override
        public void start() {
            ((FoxEntity)this.animal).stopActions();
            ((FoxEntity)this.mate).stopActions();
            super.start();
        }

        @Override
        protected void breed() {
            FoxEntity foxEntity = (FoxEntity)this.animal.createChild(this.world, this.mate);
            if (foxEntity == null) {
                return;
            }
            ServerPlayerEntity serverPlayerEntity = this.animal.getLovingPlayer();
            ServerPlayerEntity serverPlayerEntity2 = this.mate.getLovingPlayer();
            ServerPlayerEntity serverPlayerEntity3 = serverPlayerEntity;
            if (serverPlayerEntity != null) {
                foxEntity.trust(serverPlayerEntity);
            } else {
                serverPlayerEntity3 = serverPlayerEntity2;
            }
            if (serverPlayerEntity2 != null && serverPlayerEntity != serverPlayerEntity2) {
                foxEntity.trust(serverPlayerEntity2);
            }
            if (serverPlayerEntity3 != null) {
                serverPlayerEntity3.incrementStat(Stats.ANIMALS_BRED);
                Criteria.BRED_ANIMALS.trigger(serverPlayerEntity3, this.animal, this.mate, foxEntity);
            }
            this.animal.setBreedingAge(6000);
            this.mate.setBreedingAge(6000);
            this.animal.resetLoveTicks();
            this.mate.resetLoveTicks();
            foxEntity.setBreedingAge(-24000);
            foxEntity.refreshPositionAndAngles(this.animal.getX(), this.animal.getY(), this.animal.getZ(), 0.0f, 0.0f);
            this.world.spawnEntityAndPassengers(foxEntity);
            this.world.sendEntityStatus(this.animal, (byte)18);
            if (this.world.getGameRules().getValue(GameRules.DO_MOB_LOOT).booleanValue()) {
                this.world.spawnEntity(new ExperienceOrbEntity(this.world, this.animal.getX(), this.animal.getY(), this.animal.getZ(), this.animal.getRandom().nextInt(7) + 1));
            }
        }
    }

    class MoveToHuntGoal
    extends Goal {
        public MoveToHuntGoal() {
            this.setControls(EnumSet.of(Goal.Control.MOVE, Goal.Control.LOOK));
        }

        @Override
        public boolean canStart() {
            if (FoxEntity.this.isSleeping()) {
                return false;
            }
            LivingEntity livingEntity = FoxEntity.this.getTarget();
            return livingEntity != null && livingEntity.isAlive() && CHICKEN_AND_RABBIT_FILTER.test(livingEntity) && FoxEntity.this.squaredDistanceTo(livingEntity) > 36.0 && !FoxEntity.this.isInSneakingPose() && !FoxEntity.this.isRollingHead() && !FoxEntity.this.jumping;
        }

        @Override
        public void start() {
            FoxEntity.this.setSitting(false);
            FoxEntity.this.setWalking(false);
        }

        @Override
        public void stop() {
            LivingEntity livingEntity = FoxEntity.this.getTarget();
            if (livingEntity != null && FoxEntity.canJumpChase(FoxEntity.this, livingEntity)) {
                FoxEntity.this.setRollingHead(true);
                FoxEntity.this.setCrouching(true);
                FoxEntity.this.getNavigation().stop();
                FoxEntity.this.getLookControl().lookAt(livingEntity, FoxEntity.this.getMaxHeadRotation(), FoxEntity.this.getMaxLookPitchChange());
            } else {
                FoxEntity.this.setRollingHead(false);
                FoxEntity.this.setCrouching(false);
            }
        }

        @Override
        public void tick() {
            LivingEntity livingEntity = FoxEntity.this.getTarget();
            if (livingEntity == null) {
                return;
            }
            FoxEntity.this.getLookControl().lookAt(livingEntity, FoxEntity.this.getMaxHeadRotation(), FoxEntity.this.getMaxLookPitchChange());
            if (FoxEntity.this.squaredDistanceTo(livingEntity) <= 36.0) {
                FoxEntity.this.setRollingHead(true);
                FoxEntity.this.setCrouching(true);
                FoxEntity.this.getNavigation().stop();
            } else {
                FoxEntity.this.getNavigation().startMovingTo(livingEntity, 1.5);
            }
        }
    }

    public class JumpChasingGoal
    extends DiveJumpingGoal {
        @Override
        public boolean canStart() {
            if (!FoxEntity.this.isFullyCrouched()) {
                return false;
            }
            LivingEntity livingEntity = FoxEntity.this.getTarget();
            if (livingEntity == null || !livingEntity.isAlive()) {
                return false;
            }
            if (livingEntity.getMovementDirection() != livingEntity.getHorizontalFacing()) {
                return false;
            }
            boolean bl = FoxEntity.canJumpChase(FoxEntity.this, livingEntity);
            if (!bl) {
                FoxEntity.this.getNavigation().findPathTo(livingEntity, 0);
                FoxEntity.this.setCrouching(false);
                FoxEntity.this.setRollingHead(false);
            }
            return bl;
        }

        @Override
        public boolean shouldContinue() {
            LivingEntity livingEntity = FoxEntity.this.getTarget();
            if (livingEntity == null || !livingEntity.isAlive()) {
                return false;
            }
            double d = FoxEntity.this.getVelocity().y;
            return !(d * d < (double)0.05f && Math.abs(FoxEntity.this.getPitch()) < 15.0f && FoxEntity.this.isOnGround() || FoxEntity.this.isWalking());
        }

        @Override
        public boolean canStop() {
            return false;
        }

        @Override
        public void start() {
            FoxEntity.this.setJumping(true);
            FoxEntity.this.setChasing(true);
            FoxEntity.this.setRollingHead(false);
            LivingEntity livingEntity = FoxEntity.this.getTarget();
            if (livingEntity != null) {
                FoxEntity.this.getLookControl().lookAt(livingEntity, 60.0f, 30.0f);
                Vec3d vec3d = new Vec3d(livingEntity.getX() - FoxEntity.this.getX(), livingEntity.getY() - FoxEntity.this.getY(), livingEntity.getZ() - FoxEntity.this.getZ()).normalize();
                FoxEntity.this.setVelocity(FoxEntity.this.getVelocity().add(vec3d.x * 0.8, 0.9, vec3d.z * 0.8));
            }
            FoxEntity.this.getNavigation().stop();
        }

        @Override
        public void stop() {
            FoxEntity.this.setCrouching(false);
            FoxEntity.this.extraRollingHeight = 0.0f;
            FoxEntity.this.lastExtraRollingHeight = 0.0f;
            FoxEntity.this.setRollingHead(false);
            FoxEntity.this.setChasing(false);
        }

        @Override
        public void tick() {
            LivingEntity livingEntity = FoxEntity.this.getTarget();
            if (livingEntity != null) {
                FoxEntity.this.getLookControl().lookAt(livingEntity, 60.0f, 30.0f);
            }
            if (!FoxEntity.this.isWalking()) {
                Vec3d vec3d = FoxEntity.this.getVelocity();
                if (vec3d.y * vec3d.y < (double)0.03f && FoxEntity.this.getPitch() != 0.0f) {
                    FoxEntity.this.setPitch(MathHelper.lerpAngleDegrees(0.2f, FoxEntity.this.getPitch(), 0.0f));
                } else {
                    double d = vec3d.horizontalLength();
                    double e = Math.signum(-vec3d.y) * Math.acos(d / vec3d.length()) * 57.2957763671875;
                    FoxEntity.this.setPitch((float)e);
                }
            }
            if (livingEntity != null && FoxEntity.this.distanceTo(livingEntity) <= 2.0f) {
                FoxEntity.this.tryAttack(JumpChasingGoal.castToServerWorld(FoxEntity.this.getEntityWorld()), livingEntity);
            } else if (FoxEntity.this.getPitch() > 0.0f && FoxEntity.this.isOnGround() && (float)FoxEntity.this.getVelocity().y != 0.0f && FoxEntity.this.getEntityWorld().getBlockState(FoxEntity.this.getBlockPos()).isOf(Blocks.SNOW)) {
                FoxEntity.this.setPitch(60.0f);
                FoxEntity.this.setTarget(null);
                FoxEntity.this.setWalking(true);
            }
        }
    }

    class AvoidDaylightGoal
    extends EscapeSunlightGoal {
        private int timer;

        public AvoidDaylightGoal(double speed) {
            super(FoxEntity.this, speed);
            this.timer = AvoidDaylightGoal.toGoalTicks(100);
        }

        @Override
        public boolean canStart() {
            if (FoxEntity.this.isSleeping() || this.mob.getTarget() != null) {
                return false;
            }
            if (FoxEntity.this.getEntityWorld().isThundering() && FoxEntity.this.getEntityWorld().isSkyVisible(this.mob.getBlockPos())) {
                return this.targetShadedPos();
            }
            if (this.timer > 0) {
                --this.timer;
                return false;
            }
            this.timer = 100;
            BlockPos blockPos = this.mob.getBlockPos();
            return FoxEntity.this.getEntityWorld().isDay() && FoxEntity.this.getEntityWorld().isSkyVisible(blockPos) && !((ServerWorld)FoxEntity.this.getEntityWorld()).isNearOccupiedPointOfInterest(blockPos) && this.targetShadedPos();
        }

        @Override
        public void start() {
            FoxEntity.this.stopActions();
            super.start();
        }
    }

    class AttackGoal
    extends MeleeAttackGoal {
        public AttackGoal(double speed, boolean pauseWhenIdle) {
            super(FoxEntity.this, speed, pauseWhenIdle);
        }

        @Override
        protected void attack(LivingEntity target) {
            if (this.canAttack(target)) {
                this.resetCooldown();
                this.mob.tryAttack(AttackGoal.getServerWorld(this.mob), target);
                FoxEntity.this.playSound(SoundEvents.ENTITY_FOX_BITE, 1.0f, 1.0f);
            }
        }

        @Override
        public void start() {
            FoxEntity.this.setRollingHead(false);
            super.start();
        }

        @Override
        public boolean canStart() {
            return !FoxEntity.this.isSitting() && !FoxEntity.this.isSleeping() && !FoxEntity.this.isInSneakingPose() && !FoxEntity.this.isWalking() && super.canStart();
        }
    }

    class DelayedCalmDownGoal
    extends CalmDownGoal {
        private static final int MAX_CALM_DOWN_TIME = DelayedCalmDownGoal.toGoalTicks(140);
        private int timer;

        public DelayedCalmDownGoal() {
            this.timer = FoxEntity.this.random.nextInt(MAX_CALM_DOWN_TIME);
            this.setControls(EnumSet.of(Goal.Control.MOVE, Goal.Control.LOOK, Goal.Control.JUMP));
        }

        @Override
        public boolean canStart() {
            if (FoxEntity.this.sidewaysSpeed != 0.0f || FoxEntity.this.upwardSpeed != 0.0f || FoxEntity.this.forwardSpeed != 0.0f) {
                return false;
            }
            return this.canNotCalmDown() || FoxEntity.this.isSleeping();
        }

        @Override
        public boolean shouldContinue() {
            return this.canNotCalmDown();
        }

        private boolean canNotCalmDown() {
            if (this.timer > 0) {
                --this.timer;
                return false;
            }
            return FoxEntity.this.getEntityWorld().isDay() && this.isAtFavoredLocation() && !this.canCalmDown() && !FoxEntity.this.inPowderSnow;
        }

        @Override
        public void stop() {
            this.timer = FoxEntity.this.random.nextInt(MAX_CALM_DOWN_TIME);
            FoxEntity.this.stopActions();
        }

        @Override
        public void start() {
            FoxEntity.this.setSitting(false);
            FoxEntity.this.setCrouching(false);
            FoxEntity.this.setRollingHead(false);
            FoxEntity.this.setJumping(false);
            FoxEntity.this.setSleeping(true);
            FoxEntity.this.getNavigation().stop();
            FoxEntity.this.getMoveControl().moveTo(FoxEntity.this.getX(), FoxEntity.this.getY(), FoxEntity.this.getZ(), 0.0);
        }
    }

    static class FollowParentGoal
    extends net.minecraft.entity.ai.goal.FollowParentGoal {
        private final FoxEntity fox;

        public FollowParentGoal(FoxEntity fox, double speed) {
            super(fox, speed);
            this.fox = fox;
        }

        @Override
        public boolean canStart() {
            return !this.fox.isAggressive() && super.canStart();
        }

        @Override
        public boolean shouldContinue() {
            return !this.fox.isAggressive() && super.shouldContinue();
        }

        @Override
        public void start() {
            this.fox.stopActions();
            super.start();
        }
    }

    class GoToVillageGoal
    extends net.minecraft.entity.ai.goal.GoToVillageGoal {
        public GoToVillageGoal(int unused, int searchRange) {
            super(FoxEntity.this, searchRange);
        }

        @Override
        public void start() {
            FoxEntity.this.stopActions();
            super.start();
        }

        @Override
        public boolean canStart() {
            return super.canStart() && this.canGoToVillage();
        }

        @Override
        public boolean shouldContinue() {
            return super.shouldContinue() && this.canGoToVillage();
        }

        private boolean canGoToVillage() {
            return !FoxEntity.this.isSleeping() && !FoxEntity.this.isSitting() && !FoxEntity.this.isAggressive() && FoxEntity.this.getTarget() == null;
        }
    }

    public class EatBerriesGoal
    extends MoveToTargetPosGoal {
        private static final int EATING_TIME = 40;
        protected int timer;

        public EatBerriesGoal(double speed, int range, int maxYDifference) {
            super(FoxEntity.this, speed, range, maxYDifference);
        }

        @Override
        public double getDesiredDistanceToTarget() {
            return 2.0;
        }

        @Override
        public boolean shouldResetPath() {
            return this.tryingTime % 100 == 0;
        }

        @Override
        protected boolean isTargetPos(WorldView world, BlockPos pos) {
            BlockState blockState = world.getBlockState(pos);
            return blockState.isOf(Blocks.SWEET_BERRY_BUSH) && blockState.get(SweetBerryBushBlock.AGE) >= 2 || CaveVines.hasBerries(blockState);
        }

        @Override
        public void tick() {
            if (this.hasReached()) {
                if (this.timer >= 40) {
                    this.eatBerries();
                } else {
                    ++this.timer;
                }
            } else if (!this.hasReached() && FoxEntity.this.random.nextFloat() < 0.05f) {
                FoxEntity.this.playSound(SoundEvents.ENTITY_FOX_SNIFF, 1.0f, 1.0f);
            }
            super.tick();
        }

        protected void eatBerries() {
            if (!EatBerriesGoal.castToServerWorld(FoxEntity.this.getEntityWorld()).getGameRules().getValue(GameRules.DO_MOB_GRIEFING).booleanValue()) {
                return;
            }
            BlockState blockState = FoxEntity.this.getEntityWorld().getBlockState(this.targetPos);
            if (blockState.isOf(Blocks.SWEET_BERRY_BUSH)) {
                this.pickSweetBerries(blockState);
            } else if (CaveVines.hasBerries(blockState)) {
                this.pickGlowBerries(blockState);
            }
        }

        private void pickGlowBerries(BlockState state) {
            CaveVines.pickBerries(FoxEntity.this, state, FoxEntity.this.getEntityWorld(), this.targetPos);
        }

        private void pickSweetBerries(BlockState state) {
            int i = state.get(SweetBerryBushBlock.AGE);
            state.with(SweetBerryBushBlock.AGE, 1);
            int j = 1 + FoxEntity.this.getEntityWorld().random.nextInt(2) + (i == 3 ? 1 : 0);
            ItemStack itemStack = FoxEntity.this.getEquippedStack(EquipmentSlot.MAINHAND);
            if (itemStack.isEmpty()) {
                FoxEntity.this.equipStack(EquipmentSlot.MAINHAND, new ItemStack(Items.SWEET_BERRIES));
                --j;
            }
            if (j > 0) {
                Block.dropStack(FoxEntity.this.getEntityWorld(), this.targetPos, new ItemStack(Items.SWEET_BERRIES, j));
            }
            FoxEntity.this.playSound(SoundEvents.BLOCK_SWEET_BERRY_BUSH_PICK_BERRIES, 1.0f, 1.0f);
            FoxEntity.this.getEntityWorld().setBlockState(this.targetPos, (BlockState)state.with(SweetBerryBushBlock.AGE, 1), 2);
            FoxEntity.this.getEntityWorld().emitGameEvent(GameEvent.BLOCK_CHANGE, this.targetPos, GameEvent.Emitter.of(FoxEntity.this));
        }

        @Override
        public boolean canStart() {
            return !FoxEntity.this.isSleeping() && super.canStart();
        }

        @Override
        public void start() {
            this.timer = 0;
            FoxEntity.this.setSitting(false);
            super.start();
        }
    }

    class PickupItemGoal
    extends Goal {
        public PickupItemGoal() {
            this.setControls(EnumSet.of(Goal.Control.MOVE));
        }

        @Override
        public boolean canStart() {
            if (!FoxEntity.this.getEquippedStack(EquipmentSlot.MAINHAND).isEmpty()) {
                return false;
            }
            if (FoxEntity.this.getTarget() != null || FoxEntity.this.getAttacker() != null) {
                return false;
            }
            if (!FoxEntity.this.wantsToPickupItem()) {
                return false;
            }
            if (FoxEntity.this.getRandom().nextInt(PickupItemGoal.toGoalTicks(10)) != 0) {
                return false;
            }
            List<ItemEntity> list = FoxEntity.this.getEntityWorld().getEntitiesByClass(ItemEntity.class, FoxEntity.this.getBoundingBox().expand(8.0, 8.0, 8.0), PICKABLE_DROP_FILTER);
            return !list.isEmpty() && FoxEntity.this.getEquippedStack(EquipmentSlot.MAINHAND).isEmpty();
        }

        @Override
        public void tick() {
            List<ItemEntity> list = FoxEntity.this.getEntityWorld().getEntitiesByClass(ItemEntity.class, FoxEntity.this.getBoundingBox().expand(8.0, 8.0, 8.0), PICKABLE_DROP_FILTER);
            ItemStack itemStack = FoxEntity.this.getEquippedStack(EquipmentSlot.MAINHAND);
            if (itemStack.isEmpty() && !list.isEmpty()) {
                FoxEntity.this.getNavigation().startMovingTo(list.get(0), 1.2f);
            }
        }

        @Override
        public void start() {
            List<ItemEntity> list = FoxEntity.this.getEntityWorld().getEntitiesByClass(ItemEntity.class, FoxEntity.this.getBoundingBox().expand(8.0, 8.0, 8.0), PICKABLE_DROP_FILTER);
            if (!list.isEmpty()) {
                FoxEntity.this.getNavigation().startMovingTo(list.get(0), 1.2f);
            }
        }
    }

    class LookAtEntityGoal
    extends net.minecraft.entity.ai.goal.LookAtEntityGoal {
        public LookAtEntityGoal(MobEntity fox, Class<? extends LivingEntity> targetType, float range) {
            super(fox, targetType, range);
        }

        @Override
        public boolean canStart() {
            return super.canStart() && !FoxEntity.this.isWalking() && !FoxEntity.this.isRollingHead();
        }

        @Override
        public boolean shouldContinue() {
            return super.shouldContinue() && !FoxEntity.this.isWalking() && !FoxEntity.this.isRollingHead();
        }
    }

    class SitDownAndLookAroundGoal
    extends CalmDownGoal {
        private double lookX;
        private double lookZ;
        private int timer;
        private int counter;

        public SitDownAndLookAroundGoal() {
            this.setControls(EnumSet.of(Goal.Control.MOVE, Goal.Control.LOOK));
        }

        @Override
        public boolean canStart() {
            return FoxEntity.this.getAttacker() == null && FoxEntity.this.getRandom().nextFloat() < 0.02f && !FoxEntity.this.isSleeping() && FoxEntity.this.getTarget() == null && FoxEntity.this.getNavigation().isIdle() && !this.canCalmDown() && !FoxEntity.this.isChasing() && !FoxEntity.this.isInSneakingPose();
        }

        @Override
        public boolean shouldContinue() {
            return this.counter > 0;
        }

        @Override
        public void start() {
            this.chooseNewAngle();
            this.counter = 2 + FoxEntity.this.getRandom().nextInt(3);
            FoxEntity.this.setSitting(true);
            FoxEntity.this.getNavigation().stop();
        }

        @Override
        public void stop() {
            FoxEntity.this.setSitting(false);
        }

        @Override
        public void tick() {
            --this.timer;
            if (this.timer <= 0) {
                --this.counter;
                this.chooseNewAngle();
            }
            FoxEntity.this.getLookControl().lookAt(FoxEntity.this.getX() + this.lookX, FoxEntity.this.getEyeY(), FoxEntity.this.getZ() + this.lookZ, FoxEntity.this.getMaxHeadRotation(), FoxEntity.this.getMaxLookPitchChange());
        }

        private void chooseNewAngle() {
            double d = Math.PI * 2 * FoxEntity.this.getRandom().nextDouble();
            this.lookX = Math.cos(d);
            this.lookZ = Math.sin(d);
            this.timer = this.getTickCount(80 + FoxEntity.this.getRandom().nextInt(20));
        }
    }

    class DefendFriendGoal
    extends ActiveTargetGoal<LivingEntity> {
        private @Nullable LivingEntity offender;
        private @Nullable LivingEntity friend;
        private int lastAttackedTime;

        public DefendFriendGoal(Class<LivingEntity> targetEntityClass, boolean checkVisibility, @Nullable boolean checkCanNavigate, TargetPredicate.EntityPredicate targetPredicate) {
            super(FoxEntity.this, targetEntityClass, 10, checkVisibility, checkCanNavigate, targetPredicate);
        }

        @Override
        public boolean canStart() {
            if (this.reciprocalChance > 0 && this.mob.getRandom().nextInt(this.reciprocalChance) != 0) {
                return false;
            }
            ServerWorld serverWorld = DefendFriendGoal.castToServerWorld(FoxEntity.this.getEntityWorld());
            for (LazyEntityReference<LivingEntity> lazyEntityReference : FoxEntity.this.getTrustedEntities().toList()) {
                LivingEntity livingEntity = lazyEntityReference.getEntityByClass(serverWorld, LivingEntity.class);
                if (livingEntity == null) continue;
                this.friend = livingEntity;
                this.offender = livingEntity.getAttacker();
                int i = livingEntity.getLastAttackedTime();
                return i != this.lastAttackedTime && this.canTrack(this.offender, this.targetPredicate);
            }
            return false;
        }

        @Override
        public void start() {
            this.setTargetEntity(this.offender);
            this.targetEntity = this.offender;
            if (this.friend != null) {
                this.lastAttackedTime = this.friend.getLastAttackedTime();
            }
            FoxEntity.this.playSound(SoundEvents.ENTITY_FOX_AGGRO, 1.0f, 1.0f);
            FoxEntity.this.setAggressive(true);
            FoxEntity.this.stopSleeping();
            super.start();
        }
    }

    public static class FoxData
    extends PassiveEntity.PassiveData {
        public final Variant type;

        public FoxData(Variant type) {
            super(false);
            this.type = type;
        }
    }

    abstract class CalmDownGoal
    extends Goal {
        private final TargetPredicate WORRIABLE_ENTITY_PREDICATE;

        CalmDownGoal() {
            this.WORRIABLE_ENTITY_PREDICATE = TargetPredicate.createAttackable().setBaseMaxDistance(12.0).ignoreVisibility().setPredicate(new WorriableEntityFilter());
        }

        protected boolean isAtFavoredLocation() {
            BlockPos blockPos = BlockPos.ofFloored(FoxEntity.this.getX(), FoxEntity.this.getBoundingBox().maxY, FoxEntity.this.getZ());
            return !FoxEntity.this.getEntityWorld().isSkyVisible(blockPos) && FoxEntity.this.getPathfindingFavor(blockPos) >= 0.0f;
        }

        protected boolean canCalmDown() {
            return !CalmDownGoal.castToServerWorld(FoxEntity.this.getEntityWorld()).getTargets(LivingEntity.class, this.WORRIABLE_ENTITY_PREDICATE, FoxEntity.this, FoxEntity.this.getBoundingBox().expand(12.0, 6.0, 12.0)).isEmpty();
        }
    }

    public class WorriableEntityFilter
    implements TargetPredicate.EntityPredicate {
        @Override
        public boolean test(LivingEntity livingEntity, ServerWorld serverWorld) {
            PlayerEntity playerEntity;
            if (livingEntity instanceof FoxEntity) {
                return false;
            }
            if (livingEntity instanceof ChickenEntity || livingEntity instanceof RabbitEntity || livingEntity instanceof HostileEntity) {
                return true;
            }
            if (livingEntity instanceof TameableEntity) {
                return !((TameableEntity)livingEntity).isTamed();
            }
            if (livingEntity instanceof PlayerEntity && ((playerEntity = (PlayerEntity)livingEntity).isSpectator() || playerEntity.isCreative())) {
                return false;
            }
            if (FoxEntity.this.canTrust(livingEntity)) {
                return false;
            }
            return !livingEntity.isSleeping() && !livingEntity.isSneaky();
        }
    }
}


/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  com.google.common.annotations.VisibleForTesting
 *  com.google.common.base.Objects
 *  com.google.common.collect.ImmutableList
 *  com.google.common.collect.Lists
 *  com.google.common.collect.Maps
 *  com.mojang.datafixers.util.Pair
 *  com.mojang.logging.LogUtils
 *  com.mojang.serialization.Codec
 *  com.mojang.serialization.DataResult
 *  com.mojang.serialization.Dynamic
 *  com.mojang.serialization.DynamicOps
 *  com.mojang.serialization.JavaOps
 *  it.unimi.dsi.fastutil.doubles.DoubleDoubleImmutablePair
 *  it.unimi.dsi.fastutil.objects.Object2LongMap
 *  it.unimi.dsi.fastutil.objects.Object2LongOpenHashMap
 *  it.unimi.dsi.fastutil.objects.ObjectArrayList
 *  it.unimi.dsi.fastutil.objects.Reference2ObjectArrayMap
 *  it.unimi.dsi.fastutil.objects.Reference2ObjectMap
 *  org.jetbrains.annotations.Contract
 *  org.jspecify.annotations.Nullable
 *  org.slf4j.Logger
 */
package net.minecraft.entity;

import com.google.common.annotations.VisibleForTesting;
import com.google.common.base.Objects;
import com.google.common.collect.ImmutableList;
import com.google.common.collect.Lists;
import com.google.common.collect.Maps;
import com.mojang.datafixers.util.Pair;
import com.mojang.logging.LogUtils;
import com.mojang.serialization.Codec;
import com.mojang.serialization.DataResult;
import com.mojang.serialization.Dynamic;
import com.mojang.serialization.DynamicOps;
import com.mojang.serialization.JavaOps;
import it.unimi.dsi.fastutil.doubles.DoubleDoubleImmutablePair;
import it.unimi.dsi.fastutil.objects.Object2LongMap;
import it.unimi.dsi.fastutil.objects.Object2LongOpenHashMap;
import it.unimi.dsi.fastutil.objects.ObjectArrayList;
import it.unimi.dsi.fastutil.objects.Reference2ObjectArrayMap;
import it.unimi.dsi.fastutil.objects.Reference2ObjectMap;
import java.util.ArrayList;
import java.util.Collection;
import java.util.ConcurrentModificationException;
import java.util.EnumMap;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Set;
import java.util.UUID;
import java.util.function.BiConsumer;
import java.util.function.Consumer;
import java.util.function.Function;
import java.util.function.Predicate;
import net.minecraft.advancement.criterion.Criteria;
import net.minecraft.block.BedBlock;
import net.minecraft.block.BlockState;
import net.minecraft.block.Blocks;
import net.minecraft.block.HoneyBlock;
import net.minecraft.block.LadderBlock;
import net.minecraft.block.PowderSnowBlock;
import net.minecraft.block.TrapdoorBlock;
import net.minecraft.command.argument.EntityAnchorArgumentType;
import net.minecraft.component.DataComponentTypes;
import net.minecraft.component.type.AttackRangeComponent;
import net.minecraft.component.type.AttributeModifierSlot;
import net.minecraft.component.type.BlocksAttacksComponent;
import net.minecraft.component.type.DeathProtectionComponent;
import net.minecraft.component.type.EquippableComponent;
import net.minecraft.component.type.KineticWeaponComponent;
import net.minecraft.component.type.WeaponComponent;
import net.minecraft.enchantment.Enchantment;
import net.minecraft.enchantment.EnchantmentHelper;
import net.minecraft.enchantment.effect.EnchantmentLocationBasedEffect;
import net.minecraft.entity.Attackable;
import net.minecraft.entity.DamageUtil;
import net.minecraft.entity.Entity;
import net.minecraft.entity.EntityDimensions;
import net.minecraft.entity.EntityEquipment;
import net.minecraft.entity.EntityPose;
import net.minecraft.entity.EntityType;
import net.minecraft.entity.EquipmentSlot;
import net.minecraft.entity.ExperienceOrbEntity;
import net.minecraft.entity.Flutterer;
import net.minecraft.entity.ItemEntity;
import net.minecraft.entity.LazyEntityReference;
import net.minecraft.entity.LimbAnimator;
import net.minecraft.entity.MovementType;
import net.minecraft.entity.PositionInterpolator;
import net.minecraft.entity.ai.brain.Brain;
import net.minecraft.entity.attribute.AttributeContainer;
import net.minecraft.entity.attribute.DefaultAttributeContainer;
import net.minecraft.entity.attribute.DefaultAttributeRegistry;
import net.minecraft.entity.attribute.EntityAttribute;
import net.minecraft.entity.attribute.EntityAttributeInstance;
import net.minecraft.entity.attribute.EntityAttributeModifier;
import net.minecraft.entity.attribute.EntityAttributes;
import net.minecraft.entity.boss.WitherEntity;
import net.minecraft.entity.damage.DamageSource;
import net.minecraft.entity.damage.DamageTracker;
import net.minecraft.entity.damage.DamageTypes;
import net.minecraft.entity.data.DataTracker;
import net.minecraft.entity.data.TrackedData;
import net.minecraft.entity.data.TrackedDataHandlerRegistry;
import net.minecraft.entity.effect.StatusEffect;
import net.minecraft.entity.effect.StatusEffectInstance;
import net.minecraft.entity.effect.StatusEffectUtil;
import net.minecraft.entity.effect.StatusEffects;
import net.minecraft.entity.mob.ElytraFlightController;
import net.minecraft.entity.mob.PathAwareEntity;
import net.minecraft.entity.passive.WolfEntity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.entity.projectile.PersistentProjectileEntity;
import net.minecraft.entity.projectile.ProjectileEntity;
import net.minecraft.fluid.Fluid;
import net.minecraft.fluid.FluidState;
import net.minecraft.inventory.StackReference;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.item.Items;
import net.minecraft.loot.LootTable;
import net.minecraft.loot.context.LootContextParameters;
import net.minecraft.loot.context.LootContextTypes;
import net.minecraft.loot.context.LootWorldContext;
import net.minecraft.nbt.NbtOps;
import net.minecraft.network.packet.s2c.play.EntityAnimationS2CPacket;
import net.minecraft.network.packet.s2c.play.EntityEquipmentUpdateS2CPacket;
import net.minecraft.network.packet.s2c.play.EntitySpawnS2CPacket;
import net.minecraft.network.packet.s2c.play.EntityStatusEffectS2CPacket;
import net.minecraft.network.packet.s2c.play.EntityStatusS2CPacket;
import net.minecraft.network.packet.s2c.play.ItemPickupAnimationS2CPacket;
import net.minecraft.network.packet.s2c.play.RemoveEntityStatusEffectS2CPacket;
import net.minecraft.particle.BlockStateParticleEffect;
import net.minecraft.particle.ItemStackParticleEffect;
import net.minecraft.particle.ParticleEffect;
import net.minecraft.particle.ParticleTypes;
import net.minecraft.registry.RegistryKey;
import net.minecraft.registry.entry.RegistryEntry;
import net.minecraft.registry.tag.BlockTags;
import net.minecraft.registry.tag.DamageTypeTags;
import net.minecraft.registry.tag.EntityTypeTags;
import net.minecraft.registry.tag.FluidTags;
import net.minecraft.registry.tag.ItemTags;
import net.minecraft.registry.tag.TagKey;
import net.minecraft.scoreboard.Scoreboard;
import net.minecraft.scoreboard.Team;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.server.network.ServerWaypointHandler;
import net.minecraft.server.world.ServerChunkManager;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.sound.BlockSoundGroup;
import net.minecraft.sound.SoundCategory;
import net.minecraft.sound.SoundEvent;
import net.minecraft.sound.SoundEvents;
import net.minecraft.stat.Stats;
import net.minecraft.storage.ReadView;
import net.minecraft.storage.WriteView;
import net.minecraft.util.Arm;
import net.minecraft.util.Hand;
import net.minecraft.util.Identifier;
import net.minecraft.util.Util;
import net.minecraft.util.hit.HitResult;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Box;
import net.minecraft.util.math.Direction;
import net.minecraft.util.math.MathHelper;
import net.minecraft.util.math.Vec3d;
import net.minecraft.util.profiler.Profiler;
import net.minecraft.util.profiler.Profilers;
import net.minecraft.util.shape.VoxelShape;
import net.minecraft.util.shape.VoxelShapes;
import net.minecraft.world.BlockLocating;
import net.minecraft.world.CollisionView;
import net.minecraft.world.Difficulty;
import net.minecraft.world.RaycastContext;
import net.minecraft.world.World;
import net.minecraft.world.event.GameEvent;
import net.minecraft.world.rule.GameRules;
import net.minecraft.world.waypoint.ServerWaypoint;
import net.minecraft.world.waypoint.Waypoint;
import org.jetbrains.annotations.Contract;
import org.jspecify.annotations.Nullable;
import org.slf4j.Logger;

public abstract class LivingEntity
extends Entity
implements Attackable,
ServerWaypoint {
    private static final Logger LOGGER = LogUtils.getLogger();
    private static final String ACTIVE_EFFECTS_KEY = "active_effects";
    public static final String ATTRIBUTES_KEY = "attributes";
    public static final String SLEEPING_POS_KEY = "sleeping_pos";
    public static final String EQUIPMENT_KEY = "equipment";
    public static final String BRAIN_KEY = "Brain";
    public static final String FALL_FLYING_KEY = "FallFlying";
    public static final String HURT_TIME_KEY = "HurtTime";
    public static final String DEATH_TIME_KEY = "DeathTime";
    public static final String HURT_BY_TIMESTAMP_KEY = "HurtByTimestamp";
    public static final String HEALTH_KEY = "Health";
    private static final Identifier POWDER_SNOW_SPEED_MODIFIER_ID = Identifier.ofVanilla("powder_snow");
    private static final Identifier SPRINTING_SPEED_MODIFIER_ID = Identifier.ofVanilla("sprinting");
    private static final EntityAttributeModifier SPRINTING_SPEED_BOOST = new EntityAttributeModifier(SPRINTING_SPEED_MODIFIER_ID, 0.3f, EntityAttributeModifier.Operation.ADD_MULTIPLIED_TOTAL);
    public static final int EQUIPMENT_SLOT_ID = 98;
    public static final int field_30072 = 100;
    public static final int field_48827 = 105;
    public static final int field_55952 = 106;
    public static final int field_30074 = 100;
    private static final int field_30078 = 40;
    public static final double field_30075 = 0.003;
    public static final double GRAVITY = 0.08;
    public static final int DEATH_TICKS = 20;
    protected static final float field_56256 = 0.98f;
    private static final int field_30080 = 10;
    private static final int field_30081 = 2;
    public static final float field_44874 = 0.42f;
    protected static final float field_63293 = 0.4f;
    protected static final int field_63294 = 20;
    private static final double MAX_ENTITY_VIEWING_DISTANCE = 128.0;
    protected static final int USING_ITEM_FLAG = 1;
    protected static final int OFF_HAND_ACTIVE_FLAG = 2;
    protected static final int USING_RIPTIDE_FLAG = 4;
    protected static final TrackedData<Byte> LIVING_FLAGS = DataTracker.registerData(LivingEntity.class, TrackedDataHandlerRegistry.BYTE);
    private static final TrackedData<Float> HEALTH = DataTracker.registerData(LivingEntity.class, TrackedDataHandlerRegistry.FLOAT);
    private static final TrackedData<List<ParticleEffect>> POTION_SWIRLS = DataTracker.registerData(LivingEntity.class, TrackedDataHandlerRegistry.PARTICLE_LIST);
    private static final TrackedData<Boolean> POTION_SWIRLS_AMBIENT = DataTracker.registerData(LivingEntity.class, TrackedDataHandlerRegistry.BOOLEAN);
    private static final TrackedData<Integer> STUCK_ARROW_COUNT = DataTracker.registerData(LivingEntity.class, TrackedDataHandlerRegistry.INTEGER);
    private static final TrackedData<Integer> STINGER_COUNT = DataTracker.registerData(LivingEntity.class, TrackedDataHandlerRegistry.INTEGER);
    private static final TrackedData<Optional<BlockPos>> SLEEPING_POSITION = DataTracker.registerData(LivingEntity.class, TrackedDataHandlerRegistry.OPTIONAL_BLOCK_POS);
    private static final int field_49793 = 15;
    protected static final EntityDimensions SLEEPING_DIMENSIONS = EntityDimensions.fixed(0.2f, 0.2f).withEyeHeight(0.2f);
    public static final float BABY_SCALE_FACTOR = 0.5f;
    public static final float field_47756 = 0.5f;
    private static final float field_64133 = 0.04f;
    public static final Predicate<LivingEntity> NOT_WEARING_GAZE_DISGUISE_PREDICATE = entity -> {
        if (!(entity instanceof PlayerEntity)) {
            return true;
        }
        PlayerEntity playerEntity = (PlayerEntity)entity;
        ItemStack itemStack = playerEntity.getEquippedStack(EquipmentSlot.HEAD);
        return !itemStack.isIn(ItemTags.GAZE_DISGUISE_EQUIPMENT);
    };
    private static final Dynamic<?> BRAIN = new Dynamic((DynamicOps)JavaOps.INSTANCE, Map.of("memories", Map.of()));
    private final AttributeContainer attributes;
    private final DamageTracker damageTracker = new DamageTracker(this);
    private final Map<RegistryEntry<StatusEffect>, StatusEffectInstance> activeStatusEffects = Maps.newHashMap();
    private final Map<EquipmentSlot, ItemStack> lastEquipmentStacks = Util.mapEnum(EquipmentSlot.class, slot -> ItemStack.EMPTY);
    public boolean handSwinging;
    private boolean noDrag = false;
    public Hand preferredHand;
    public int handSwingTicks;
    public int stuckArrowTimer;
    public int stuckStingerTimer;
    public int hurtTime;
    public int maxHurtTime;
    public int deathTime;
    public float lastHandSwingProgress;
    public float handSwingProgress;
    protected int ticksSinceLastAttack;
    protected int ticksSinceHandEquipping;
    public final LimbAnimator limbAnimator = new LimbAnimator();
    public float bodyYaw;
    public float lastBodyYaw;
    public float headYaw;
    public float lastHeadYaw;
    public final ElytraFlightController elytraFlightController = new ElytraFlightController(this);
    protected @Nullable LazyEntityReference<PlayerEntity> attackingPlayer;
    protected int playerHitTimer;
    protected boolean dead;
    protected int despawnCounter;
    protected float lastDamageTaken;
    protected boolean jumping;
    public float sidewaysSpeed;
    public float upwardSpeed;
    public float forwardSpeed;
    protected PositionInterpolator interpolator = new PositionInterpolator(this);
    protected double serverHeadYaw;
    protected int headTrackingIncrements;
    private boolean effectsChanged = true;
    private @Nullable LazyEntityReference<LivingEntity> attackerReference;
    private int lastAttackedTime;
    private @Nullable LivingEntity attacking;
    private int lastAttackTime;
    private float movementSpeed;
    private int jumpingCooldown;
    private float absorptionAmount;
    protected ItemStack activeItemStack = ItemStack.EMPTY;
    protected int itemUseTimeLeft;
    protected int glidingTicks;
    private long lastKineticAttackTime = Integer.MIN_VALUE;
    private BlockPos lastBlockPos;
    private Optional<BlockPos> climbingPos = Optional.empty();
    private @Nullable DamageSource lastDamageSource;
    private long lastDamageTime;
    protected int riptideTicks;
    protected float riptideAttackDamage;
    protected @Nullable ItemStack riptideStack;
    protected @Nullable Object2LongMap<Entity> piercingCooldowns;
    private float leaningPitch;
    private float lastLeaningPitch;
    protected Brain<?> brain;
    private boolean experienceDroppingDisabled;
    private final EnumMap<EquipmentSlot, Reference2ObjectMap<Enchantment, Set<EnchantmentLocationBasedEffect>>> locationBasedEnchantmentEffects = new EnumMap(EquipmentSlot.class);
    protected final EntityEquipment equipment;
    private Waypoint.Config waypointConfig = new Waypoint.Config();

    protected LivingEntity(EntityType<? extends LivingEntity> entityType, World world) {
        super(entityType, world);
        this.attributes = new AttributeContainer(DefaultAttributeRegistry.get(entityType));
        this.setHealth(this.getMaxHealth());
        this.equipment = this.createEquipment();
        this.intersectionChecked = true;
        this.refreshPosition();
        this.setYaw(this.random.nextFloat() * ((float)Math.PI * 2));
        this.headYaw = this.getYaw();
        this.brain = this.deserializeBrain(BRAIN);
    }

    @Override
    public @Nullable LivingEntity getEntity() {
        return this;
    }

    @Contract(pure=true)
    protected EntityEquipment createEquipment() {
        return new EntityEquipment();
    }

    public Brain<?> getBrain() {
        return this.brain;
    }

    protected Brain.Profile<?> createBrainProfile() {
        return Brain.createProfile(ImmutableList.of(), ImmutableList.of());
    }

    protected Brain<?> deserializeBrain(Dynamic<?> dynamic) {
        return this.createBrainProfile().deserialize(dynamic);
    }

    @Override
    public void kill(ServerWorld world) {
        this.damage(world, this.getDamageSources().genericKill(), Float.MAX_VALUE);
    }

    public boolean canTarget(EntityType<?> type) {
        return true;
    }

    @Override
    protected void initDataTracker(DataTracker.Builder builder) {
        builder.add(LIVING_FLAGS, (byte)0);
        builder.add(POTION_SWIRLS, List.of());
        builder.add(POTION_SWIRLS_AMBIENT, false);
        builder.add(STUCK_ARROW_COUNT, 0);
        builder.add(STINGER_COUNT, 0);
        builder.add(HEALTH, Float.valueOf(1.0f));
        builder.add(SLEEPING_POSITION, Optional.empty());
    }

    public static DefaultAttributeContainer.Builder createLivingAttributes() {
        return DefaultAttributeContainer.builder().add(EntityAttributes.MAX_HEALTH).add(EntityAttributes.KNOCKBACK_RESISTANCE).add(EntityAttributes.MOVEMENT_SPEED).add(EntityAttributes.ARMOR).add(EntityAttributes.ARMOR_TOUGHNESS).add(EntityAttributes.MAX_ABSORPTION).add(EntityAttributes.STEP_HEIGHT).add(EntityAttributes.SCALE).add(EntityAttributes.GRAVITY).add(EntityAttributes.SAFE_FALL_DISTANCE).add(EntityAttributes.FALL_DAMAGE_MULTIPLIER).add(EntityAttributes.JUMP_STRENGTH).add(EntityAttributes.OXYGEN_BONUS).add(EntityAttributes.BURNING_TIME).add(EntityAttributes.EXPLOSION_KNOCKBACK_RESISTANCE).add(EntityAttributes.WATER_MOVEMENT_EFFICIENCY).add(EntityAttributes.MOVEMENT_EFFICIENCY).add(EntityAttributes.ATTACK_KNOCKBACK).add(EntityAttributes.CAMERA_DISTANCE).add(EntityAttributes.WAYPOINT_TRANSMIT_RANGE);
    }

    @Override
    protected void fall(double heightDifference, boolean onGround, BlockState state, BlockPos landedPosition) {
        World world;
        if (!this.isTouchingWater()) {
            this.checkWaterState();
        }
        if ((world = this.getEntityWorld()) instanceof ServerWorld) {
            ServerWorld serverWorld = (ServerWorld)world;
            if (onGround && this.fallDistance > 0.0) {
                this.applyMovementEffects(serverWorld, landedPosition);
                double d = Math.max(0, MathHelper.floor(this.getUnsafeFallDistance(this.fallDistance)));
                if (d > 0.0 && !state.isAir()) {
                    double h;
                    double e = this.getX();
                    double f = this.getY();
                    double g = this.getZ();
                    BlockPos blockPos = this.getBlockPos();
                    if (landedPosition.getX() != blockPos.getX() || landedPosition.getZ() != blockPos.getZ()) {
                        h = e - (double)landedPosition.getX() - 0.5;
                        double i = g - (double)landedPosition.getZ() - 0.5;
                        double j = Math.max(Math.abs(h), Math.abs(i));
                        e = (double)landedPosition.getX() + 0.5 + h / j * 0.5;
                        g = (double)landedPosition.getZ() + 0.5 + i / j * 0.5;
                    }
                    h = Math.min((double)0.2f + d / 15.0, 2.5);
                    int k = (int)(150.0 * h);
                    serverWorld.spawnParticles(new BlockStateParticleEffect(ParticleTypes.BLOCK, state), e, f, g, k, 0.0, 0.0, 0.0, 0.15f);
                }
            }
        }
        super.fall(heightDifference, onGround, state, landedPosition);
        if (onGround) {
            this.climbingPos = Optional.empty();
        }
    }

    public boolean canBreatheInWater() {
        return this.getType().isIn(EntityTypeTags.CAN_BREATHE_UNDER_WATER);
    }

    public float getLeaningPitch(float tickProgress) {
        return MathHelper.lerp(tickProgress, this.lastLeaningPitch, this.leaningPitch);
    }

    public boolean hasLandedInFluid() {
        return this.getVelocity().getY() < (double)1.0E-5f && this.isInFluid();
    }

    @Override
    public void baseTick() {
        LivingEntity livingEntity;
        World world;
        World world2;
        this.lastHandSwingProgress = this.handSwingProgress;
        if (this.firstUpdate) {
            this.getSleepingPosition().ifPresent(this::setPositionInBed);
        }
        if ((world2 = this.getEntityWorld()) instanceof ServerWorld) {
            ServerWorld serverWorld = (ServerWorld)world2;
            EnchantmentHelper.onTick(serverWorld, this);
        }
        super.baseTick();
        Profiler profiler = Profilers.get();
        profiler.push("livingEntityBaseTick");
        if (this.isAlive() && (world = this.getEntityWorld()) instanceof ServerWorld) {
            double e;
            double d;
            ServerWorld serverWorld2 = (ServerWorld)world;
            boolean bl = this instanceof PlayerEntity;
            if (this.isInsideWall()) {
                this.damage(serverWorld2, this.getDamageSources().inWall(), 1.0f);
            } else if (bl && !serverWorld2.getWorldBorder().contains(this.getBoundingBox()) && (d = serverWorld2.getWorldBorder().getDistanceInsideBorder(this) + serverWorld2.getWorldBorder().getSafeZone()) < 0.0 && (e = serverWorld2.getWorldBorder().getDamagePerBlock()) > 0.0) {
                this.damage(serverWorld2, this.getDamageSources().outsideBorder(), Math.max(1, MathHelper.floor(-d * e)));
            }
            if (this.isSubmergedIn(FluidTags.WATER) && !serverWorld2.getBlockState(BlockPos.ofFloored(this.getX(), this.getEyeY(), this.getZ())).isOf(Blocks.BUBBLE_COLUMN)) {
                boolean bl2;
                boolean bl3 = bl2 = !this.canBreatheInWater() && !StatusEffectUtil.hasWaterBreathing(this) && (!bl || !((PlayerEntity)this).getAbilities().invulnerable);
                if (bl2) {
                    this.setAir(this.getNextAirUnderwater(this.getAir()));
                    if (this.shouldDrown()) {
                        this.setAir(0);
                        serverWorld2.sendEntityStatus(this, (byte)67);
                        this.damage(serverWorld2, this.getDamageSources().drown(), 2.0f);
                    }
                } else if (this.getAir() < this.getMaxAir() && StatusEffectUtil.canIncreaseAirOnLand(this)) {
                    this.setAir(this.getNextAirOnLand(this.getAir()));
                }
                if (this.hasVehicle() && this.getVehicle() != null && this.getVehicle().shouldDismountUnderwater()) {
                    this.stopRiding();
                }
            } else if (this.getAir() < this.getMaxAir()) {
                this.setAir(this.getNextAirOnLand(this.getAir()));
            }
            BlockPos blockPos = this.getBlockPos();
            if (!Objects.equal((Object)this.lastBlockPos, (Object)blockPos)) {
                this.lastBlockPos = blockPos;
                this.applyMovementEffects(serverWorld2, blockPos);
            }
        }
        if (this.hurtTime > 0) {
            --this.hurtTime;
        }
        if (this.timeUntilRegen > 0 && !(this instanceof ServerPlayerEntity)) {
            --this.timeUntilRegen;
        }
        if (this.isDead() && this.getEntityWorld().shouldUpdatePostDeath(this)) {
            this.updatePostDeath();
        }
        if (this.playerHitTimer > 0) {
            --this.playerHitTimer;
        } else {
            this.attackingPlayer = null;
        }
        if (this.attacking != null && !this.attacking.isAlive()) {
            this.attacking = null;
        }
        if ((livingEntity = this.getAttacker()) != null) {
            if (!livingEntity.isAlive()) {
                this.setAttacker(null);
            } else if (this.age - this.lastAttackedTime > 100) {
                this.setAttacker(null);
            }
        }
        this.tickStatusEffects();
        this.lastHeadYaw = this.headYaw;
        this.lastBodyYaw = this.bodyYaw;
        this.lastYaw = this.getYaw();
        this.lastPitch = this.getPitch();
        profiler.pop();
    }

    protected boolean shouldDrown() {
        return this.getAir() <= -20;
    }

    @Override
    protected float getVelocityMultiplier() {
        return MathHelper.lerp((float)this.getAttributeValue(EntityAttributes.MOVEMENT_EFFICIENCY), super.getVelocityMultiplier(), 1.0f);
    }

    public float getLuck() {
        return 0.0f;
    }

    protected void removePowderSnowSlow() {
        EntityAttributeInstance entityAttributeInstance = this.getAttributeInstance(EntityAttributes.MOVEMENT_SPEED);
        if (entityAttributeInstance == null) {
            return;
        }
        if (entityAttributeInstance.getModifier(POWDER_SNOW_SPEED_MODIFIER_ID) != null) {
            entityAttributeInstance.removeModifier(POWDER_SNOW_SPEED_MODIFIER_ID);
        }
    }

    protected void addPowderSnowSlowIfNeeded() {
        int i;
        if (!this.getLandingBlockState().isAir() && (i = this.getFrozenTicks()) > 0) {
            EntityAttributeInstance entityAttributeInstance = this.getAttributeInstance(EntityAttributes.MOVEMENT_SPEED);
            if (entityAttributeInstance == null) {
                return;
            }
            float f = -0.05f * this.getFreezingScale();
            entityAttributeInstance.addTemporaryModifier(new EntityAttributeModifier(POWDER_SNOW_SPEED_MODIFIER_ID, f, EntityAttributeModifier.Operation.ADD_VALUE));
        }
    }

    protected void applyMovementEffects(ServerWorld world, BlockPos pos) {
        EnchantmentHelper.applyLocationBasedEffects(world, this);
    }

    public boolean isBaby() {
        return false;
    }

    public float getScaleFactor() {
        return this.isBaby() ? 0.5f : 1.0f;
    }

    public final float getScale() {
        AttributeContainer attributeContainer = this.getAttributes();
        if (attributeContainer == null) {
            return 1.0f;
        }
        return this.clampScale((float)attributeContainer.getValue(EntityAttributes.SCALE));
    }

    protected float clampScale(float scale) {
        return scale;
    }

    public boolean shouldSwimInFluids() {
        return true;
    }

    protected void updatePostDeath() {
        ++this.deathTime;
        if (this.deathTime >= 20 && !this.getEntityWorld().isClient() && !this.isRemoved()) {
            this.getEntityWorld().sendEntityStatus(this, (byte)60);
            this.remove(Entity.RemovalReason.KILLED);
        }
    }

    public boolean shouldDropExperience() {
        return !this.isBaby();
    }

    protected boolean shouldDropLoot(ServerWorld world) {
        return !this.isBaby() && world.getGameRules().getValue(GameRules.DO_MOB_LOOT) != false;
    }

    protected int getNextAirUnderwater(int air) {
        EntityAttributeInstance entityAttributeInstance = this.getAttributeInstance(EntityAttributes.OXYGEN_BONUS);
        double d = entityAttributeInstance != null ? entityAttributeInstance.getValue() : 0.0;
        if (d > 0.0 && this.random.nextDouble() >= 1.0 / (d + 1.0)) {
            return air;
        }
        return air - 1;
    }

    protected int getNextAirOnLand(int air) {
        return Math.min(air + 4, this.getMaxAir());
    }

    public final int getExperienceToDrop(ServerWorld world, @Nullable Entity attacker) {
        return EnchantmentHelper.getMobExperience(world, attacker, this, this.getExperienceToDrop(world));
    }

    protected int getExperienceToDrop(ServerWorld world) {
        return 0;
    }

    protected boolean shouldAlwaysDropExperience() {
        return false;
    }

    public @Nullable LivingEntity getAttacker() {
        return LazyEntityReference.getLivingEntity(this.attackerReference, this.getEntityWorld());
    }

    public @Nullable PlayerEntity getAttackingPlayer() {
        return LazyEntityReference.getPlayerEntity(this.attackingPlayer, this.getEntityWorld());
    }

    @Override
    public LivingEntity getLastAttacker() {
        return this.getAttacker();
    }

    public int getLastAttackedTime() {
        return this.lastAttackedTime;
    }

    public void setAttacking(PlayerEntity attackingPlayer, int playerHitTimer) {
        this.setAttacking(LazyEntityReference.of(attackingPlayer), playerHitTimer);
    }

    public void setAttacking(UUID attackingPlayer, int playerHitTimer) {
        this.setAttacking(LazyEntityReference.ofUUID(attackingPlayer), playerHitTimer);
    }

    private void setAttacking(LazyEntityReference<PlayerEntity> attackingPlayer, int playerHitTimer) {
        this.attackingPlayer = attackingPlayer;
        this.playerHitTimer = playerHitTimer;
    }

    public void setAttacker(@Nullable LivingEntity attacker) {
        this.attackerReference = LazyEntityReference.of(attacker);
        this.lastAttackedTime = this.age;
    }

    public @Nullable LivingEntity getAttacking() {
        return this.attacking;
    }

    public int getLastAttackTime() {
        return this.lastAttackTime;
    }

    public void onAttacking(Entity target) {
        this.attacking = target instanceof LivingEntity ? (LivingEntity)target : null;
        this.lastAttackTime = this.age;
    }

    public int getDespawnCounter() {
        return this.despawnCounter;
    }

    public void setDespawnCounter(int despawnCounter) {
        this.despawnCounter = despawnCounter;
    }

    public boolean hasNoDrag() {
        return this.noDrag;
    }

    public void setNoDrag(boolean noDrag) {
        this.noDrag = noDrag;
    }

    protected boolean isArmorSlot(EquipmentSlot slot) {
        return true;
    }

    public void onEquipStack(EquipmentSlot slot, ItemStack oldStack, ItemStack newStack) {
        if (this.getEntityWorld().isClient() || this.isSpectator()) {
            return;
        }
        if (ItemStack.areItemsAndComponentsEqual(oldStack, newStack) || this.firstUpdate) {
            return;
        }
        EquippableComponent equippableComponent = newStack.get(DataComponentTypes.EQUIPPABLE);
        if (!this.isSilent() && equippableComponent != null && slot == equippableComponent.slot()) {
            this.getEntityWorld().playSound(null, this.getX(), this.getY(), this.getZ(), this.getEquipSound(slot, newStack, equippableComponent), this.getSoundCategory(), 1.0f, 1.0f, this.random.nextLong());
        }
        if (this.isArmorSlot(slot)) {
            this.emitGameEvent(equippableComponent != null ? GameEvent.EQUIP : GameEvent.UNEQUIP);
        }
    }

    protected RegistryEntry<SoundEvent> getEquipSound(EquipmentSlot slot, ItemStack stack, EquippableComponent equippableComponent) {
        return equippableComponent.equipSound();
    }

    @Override
    public void remove(Entity.RemovalReason reason) {
        World world;
        if ((reason == Entity.RemovalReason.KILLED || reason == Entity.RemovalReason.DISCARDED) && (world = this.getEntityWorld()) instanceof ServerWorld) {
            ServerWorld serverWorld = (ServerWorld)world;
            this.onRemoval(serverWorld, reason);
        }
        super.remove(reason);
        this.brain.forgetAll();
    }

    @Override
    public void onRemove(Entity.RemovalReason reason) {
        super.onRemove(reason);
        World world = this.getEntityWorld();
        if (world instanceof ServerWorld) {
            ServerWorld serverWorld = (ServerWorld)world;
            serverWorld.getWaypointHandler().onUntrack(this);
        }
    }

    protected void onRemoval(ServerWorld world, Entity.RemovalReason reason) {
        for (StatusEffectInstance statusEffectInstance : this.getStatusEffects()) {
            statusEffectInstance.onEntityRemoval(world, this, reason);
        }
        this.activeStatusEffects.clear();
    }

    @Override
    protected void writeCustomData(WriteView view) {
        view.putFloat(HEALTH_KEY, this.getHealth());
        view.putShort(HURT_TIME_KEY, (short)this.hurtTime);
        view.putInt(HURT_BY_TIMESTAMP_KEY, this.lastAttackedTime);
        view.putShort(DEATH_TIME_KEY, (short)this.deathTime);
        view.putFloat("AbsorptionAmount", this.getAbsorptionAmount());
        view.put(ATTRIBUTES_KEY, EntityAttributeInstance.Packed.LIST_CODEC, this.getAttributes().pack());
        if (!this.activeStatusEffects.isEmpty()) {
            view.put(ACTIVE_EFFECTS_KEY, StatusEffectInstance.CODEC.listOf(), List.copyOf(this.activeStatusEffects.values()));
        }
        view.putBoolean(FALL_FLYING_KEY, this.isGliding());
        this.getSleepingPosition().ifPresent(pos -> view.put(SLEEPING_POS_KEY, BlockPos.CODEC, pos));
        DataResult dataResult = this.brain.encode(NbtOps.INSTANCE).map(nbtElement -> new Dynamic((DynamicOps)NbtOps.INSTANCE, nbtElement));
        dataResult.resultOrPartial(arg_0 -> ((Logger)LOGGER).error(arg_0)).ifPresent(brain -> view.put(BRAIN_KEY, Codec.PASSTHROUGH, brain));
        if (this.attackingPlayer != null) {
            this.attackingPlayer.writeData(view, "last_hurt_by_player");
            view.putInt("last_hurt_by_player_memory_time", this.playerHitTimer);
        }
        if (this.attackerReference != null) {
            this.attackerReference.writeData(view, "last_hurt_by_mob");
            view.putInt("ticks_since_last_hurt_by_mob", this.age - this.lastAttackedTime);
        }
        if (!this.equipment.isEmpty()) {
            view.put(EQUIPMENT_KEY, EntityEquipment.CODEC, this.equipment);
        }
        if (this.waypointConfig.hasCustomStyle()) {
            view.put("locator_bar_icon", Waypoint.Config.CODEC, this.waypointConfig);
        }
    }

    public @Nullable ItemEntity dropItem(ItemStack stack, boolean dropAtSelf, boolean retainOwnership) {
        if (stack.isEmpty()) {
            return null;
        }
        if (this.getEntityWorld().isClient()) {
            this.swingHand(Hand.MAIN_HAND);
            return null;
        }
        ItemEntity itemEntity = this.createItemEntity(stack, dropAtSelf, retainOwnership);
        if (itemEntity != null) {
            this.getEntityWorld().spawnEntity(itemEntity);
        }
        return itemEntity;
    }

    @Override
    protected void readCustomData(ReadView view) {
        this.setAbsorptionAmountUnclamped(view.getFloat("AbsorptionAmount", 0.0f));
        if (this.getEntityWorld() != null && !this.getEntityWorld().isClient()) {
            view.read(ATTRIBUTES_KEY, EntityAttributeInstance.Packed.LIST_CODEC).ifPresent(this.getAttributes()::unpack);
        }
        List list = view.read(ACTIVE_EFFECTS_KEY, StatusEffectInstance.CODEC.listOf()).orElse(List.of());
        this.activeStatusEffects.clear();
        for (StatusEffectInstance statusEffectInstance : list) {
            this.activeStatusEffects.put(statusEffectInstance.getEffectType(), statusEffectInstance);
            this.effectsChanged = true;
        }
        this.setHealth(view.getFloat(HEALTH_KEY, this.getMaxHealth()));
        this.hurtTime = view.getShort(HURT_TIME_KEY, (short)0);
        this.deathTime = view.getShort(DEATH_TIME_KEY, (short)0);
        this.lastAttackedTime = view.getInt(HURT_BY_TIMESTAMP_KEY, 0);
        view.getOptionalString("Team").ifPresent(team -> {
            boolean bl;
            Scoreboard scoreboard = this.getEntityWorld().getScoreboard();
            Team team2 = scoreboard.getTeam((String)team);
            boolean bl2 = bl = team2 != null && scoreboard.addScoreHolderToTeam(this.getUuidAsString(), team2);
            if (!bl) {
                LOGGER.warn("Unable to add mob to team \"{}\" (that team probably doesn't exist)", team);
            }
        });
        this.setFlag(7, view.getBoolean(FALL_FLYING_KEY, false));
        view.read(SLEEPING_POS_KEY, BlockPos.CODEC).ifPresentOrElse(pos -> {
            this.setSleepingPosition((BlockPos)pos);
            this.dataTracker.set(POSE, EntityPose.SLEEPING);
            if (!this.firstUpdate) {
                this.setPositionInBed((BlockPos)pos);
            }
        }, this::clearSleepingPosition);
        view.read(BRAIN_KEY, Codec.PASSTHROUGH).ifPresent(brain -> {
            this.brain = this.deserializeBrain((Dynamic<?>)brain);
        });
        this.attackingPlayer = LazyEntityReference.fromData(view, "last_hurt_by_player");
        this.playerHitTimer = view.getInt("last_hurt_by_player_memory_time", 0);
        this.attackerReference = LazyEntityReference.fromData(view, "last_hurt_by_mob");
        this.lastAttackedTime = view.getInt("ticks_since_last_hurt_by_mob", 0) + this.age;
        this.equipment.copyFrom(view.read(EQUIPMENT_KEY, EntityEquipment.CODEC).orElseGet(EntityEquipment::new));
        this.waypointConfig = view.read("locator_bar_icon", Waypoint.Config.CODEC).orElseGet(Waypoint.Config::new);
    }

    @Override
    public void beforePacketsSent() {
        super.beforePacketsSent();
        this.handleEffectsChanged();
    }

    protected void tickStatusEffects() {
        World world = this.getEntityWorld();
        if (world instanceof ServerWorld) {
            ServerWorld serverWorld = (ServerWorld)world;
            Iterator<Object> iterator = this.activeStatusEffects.keySet().iterator();
            try {
                while (iterator.hasNext()) {
                    RegistryEntry registryEntry = (RegistryEntry)iterator.next();
                    StatusEffectInstance statusEffectInstance = this.activeStatusEffects.get(registryEntry);
                    if (!statusEffectInstance.update(serverWorld, this, () -> this.onStatusEffectUpgraded(statusEffectInstance, true, null))) {
                        iterator.remove();
                        this.onStatusEffectsRemoved(List.of(statusEffectInstance));
                        continue;
                    }
                    if (statusEffectInstance.getDuration() % 600 != 0) continue;
                    this.onStatusEffectUpgraded(statusEffectInstance, false, null);
                }
            }
            catch (ConcurrentModificationException registryEntry) {}
        } else {
            for (StatusEffectInstance statusEffectInstance2 : this.activeStatusEffects.values()) {
                statusEffectInstance2.tickClient();
            }
            List<ParticleEffect> list = this.dataTracker.get(POTION_SWIRLS);
            if (!list.isEmpty()) {
                int j;
                boolean bl = this.dataTracker.get(POTION_SWIRLS_AMBIENT);
                int i = this.isInvisible() ? 15 : 4;
                int n = j = bl ? 5 : 1;
                if (this.random.nextInt(i * j) == 0) {
                    this.getEntityWorld().addParticleClient(Util.getRandom(list, this.random), this.getParticleX(0.5), this.getRandomBodyY(), this.getParticleZ(0.5), 1.0, 1.0, 1.0);
                }
            }
        }
    }

    private void handleEffectsChanged() {
        if (this.effectsChanged) {
            this.updatePotionVisibility();
            this.updateGlowing();
            this.effectsChanged = false;
        }
    }

    protected void updatePotionVisibility() {
        if (this.activeStatusEffects.isEmpty()) {
            this.clearPotionSwirls();
            this.setInvisible(false);
            return;
        }
        this.setInvisible(this.hasStatusEffect(StatusEffects.INVISIBILITY));
        this.updatePotionSwirls();
    }

    private void updatePotionSwirls() {
        List<ParticleEffect> list = this.activeStatusEffects.values().stream().filter(StatusEffectInstance::shouldShowParticles).map(StatusEffectInstance::createParticle).toList();
        this.dataTracker.set(POTION_SWIRLS, list);
        this.dataTracker.set(POTION_SWIRLS_AMBIENT, LivingEntity.containsOnlyAmbientEffects(this.activeStatusEffects.values()));
    }

    private void updateGlowing() {
        boolean bl = this.isGlowing();
        if (this.getFlag(6) != bl) {
            this.setFlag(6, bl);
        }
    }

    public double getAttackDistanceScalingFactor(@Nullable Entity entity) {
        double d = 1.0;
        if (this.isSneaky()) {
            d *= 0.8;
        }
        if (this.isInvisible()) {
            float f = this.getArmorVisibility();
            if (f < 0.1f) {
                f = 0.1f;
            }
            d *= 0.7 * (double)f;
        }
        if (entity != null) {
            ItemStack itemStack = this.getEquippedStack(EquipmentSlot.HEAD);
            EntityType<?> entityType = entity.getType();
            if (entityType == EntityType.SKELETON && itemStack.isOf(Items.SKELETON_SKULL) || entityType == EntityType.ZOMBIE && itemStack.isOf(Items.ZOMBIE_HEAD) || entityType == EntityType.PIGLIN && itemStack.isOf(Items.PIGLIN_HEAD) || entityType == EntityType.PIGLIN_BRUTE && itemStack.isOf(Items.PIGLIN_HEAD) || entityType == EntityType.CREEPER && itemStack.isOf(Items.CREEPER_HEAD)) {
                d *= 0.5;
            }
        }
        return d;
    }

    public boolean canTarget(LivingEntity target) {
        if (target instanceof PlayerEntity && this.getEntityWorld().getDifficulty() == Difficulty.PEACEFUL) {
            return false;
        }
        return target.canTakeDamage();
    }

    public boolean canTakeDamage() {
        return !this.isInvulnerable() && this.isPartOfGame();
    }

    public boolean isPartOfGame() {
        return !this.isSpectator() && this.isAlive();
    }

    public static boolean containsOnlyAmbientEffects(Collection<StatusEffectInstance> effects) {
        for (StatusEffectInstance statusEffectInstance : effects) {
            if (!statusEffectInstance.shouldShowParticles() || statusEffectInstance.isAmbient()) continue;
            return false;
        }
        return true;
    }

    protected void clearPotionSwirls() {
        this.dataTracker.set(POTION_SWIRLS, List.of());
    }

    public boolean clearStatusEffects() {
        if (this.getEntityWorld().isClient()) {
            return false;
        }
        if (this.activeStatusEffects.isEmpty()) {
            return false;
        }
        HashMap map = Maps.newHashMap(this.activeStatusEffects);
        this.activeStatusEffects.clear();
        this.onStatusEffectsRemoved(map.values());
        return true;
    }

    public Collection<StatusEffectInstance> getStatusEffects() {
        return this.activeStatusEffects.values();
    }

    public Map<RegistryEntry<StatusEffect>, StatusEffectInstance> getActiveStatusEffects() {
        return this.activeStatusEffects;
    }

    public boolean hasStatusEffect(RegistryEntry<StatusEffect> effect) {
        return this.activeStatusEffects.containsKey(effect);
    }

    public @Nullable StatusEffectInstance getStatusEffect(RegistryEntry<StatusEffect> effect) {
        return this.activeStatusEffects.get(effect);
    }

    public float getEffectFadeFactor(RegistryEntry<StatusEffect> effect, float tickProgress) {
        StatusEffectInstance statusEffectInstance = this.getStatusEffect(effect);
        if (statusEffectInstance != null) {
            return statusEffectInstance.getFadeFactor(this, tickProgress);
        }
        return 0.0f;
    }

    public final boolean addStatusEffect(StatusEffectInstance effect) {
        return this.addStatusEffect(effect, null);
    }

    public boolean addStatusEffect(StatusEffectInstance effect, @Nullable Entity source) {
        if (!this.canHaveStatusEffect(effect)) {
            return false;
        }
        StatusEffectInstance statusEffectInstance = this.activeStatusEffects.get(effect.getEffectType());
        boolean bl = false;
        if (statusEffectInstance == null) {
            this.activeStatusEffects.put(effect.getEffectType(), effect);
            this.onStatusEffectApplied(effect, source);
            bl = true;
            effect.playApplySound(this);
        } else if (statusEffectInstance.upgrade(effect)) {
            this.onStatusEffectUpgraded(statusEffectInstance, true, source);
            bl = true;
        }
        effect.onApplied(this);
        return bl;
    }

    public boolean canHaveStatusEffect(StatusEffectInstance effect) {
        if (this.getType().isIn(EntityTypeTags.IMMUNE_TO_INFESTED)) {
            return !effect.equals(StatusEffects.INFESTED);
        }
        if (this.getType().isIn(EntityTypeTags.IMMUNE_TO_OOZING)) {
            return !effect.equals(StatusEffects.OOZING);
        }
        if (this.getType().isIn(EntityTypeTags.IGNORES_POISON_AND_REGEN)) {
            return !effect.equals(StatusEffects.REGENERATION) && !effect.equals(StatusEffects.POISON);
        }
        return true;
    }

    public void setStatusEffect(StatusEffectInstance effect, @Nullable Entity source) {
        if (!this.canHaveStatusEffect(effect)) {
            return;
        }
        StatusEffectInstance statusEffectInstance = this.activeStatusEffects.put(effect.getEffectType(), effect);
        if (statusEffectInstance == null) {
            this.onStatusEffectApplied(effect, source);
        } else {
            effect.copyFadingFrom(statusEffectInstance);
            this.onStatusEffectUpgraded(effect, true, source);
        }
    }

    public boolean hasInvertedHealingAndHarm() {
        return this.getType().isIn(EntityTypeTags.INVERTED_HEALING_AND_HARM);
    }

    public final @Nullable StatusEffectInstance removeStatusEffectInternal(RegistryEntry<StatusEffect> effect) {
        return this.activeStatusEffects.remove(effect);
    }

    public boolean removeStatusEffect(RegistryEntry<StatusEffect> effect) {
        StatusEffectInstance statusEffectInstance = this.removeStatusEffectInternal(effect);
        if (statusEffectInstance != null) {
            this.onStatusEffectsRemoved(List.of(statusEffectInstance));
            return true;
        }
        return false;
    }

    protected void onStatusEffectApplied(StatusEffectInstance effect, @Nullable Entity source) {
        if (!this.getEntityWorld().isClient()) {
            this.effectsChanged = true;
            effect.getEffectType().value().onApplied(this.getAttributes(), effect.getAmplifier());
            this.sendEffectToControllingPlayer(effect);
        }
    }

    public void sendEffectToControllingPlayer(StatusEffectInstance effect) {
        for (Entity entity : this.getPassengerList()) {
            if (!(entity instanceof ServerPlayerEntity)) continue;
            ServerPlayerEntity serverPlayerEntity = (ServerPlayerEntity)entity;
            serverPlayerEntity.networkHandler.sendPacket(new EntityStatusEffectS2CPacket(this.getId(), effect, false));
        }
    }

    protected void onStatusEffectUpgraded(StatusEffectInstance effect, boolean reapplyEffect, @Nullable Entity source) {
        if (this.getEntityWorld().isClient()) {
            return;
        }
        this.effectsChanged = true;
        if (reapplyEffect) {
            StatusEffect statusEffect = effect.getEffectType().value();
            statusEffect.onRemoved(this.getAttributes());
            statusEffect.onApplied(this.getAttributes(), effect.getAmplifier());
            this.updateAttributes();
        }
        this.sendEffectToControllingPlayer(effect);
    }

    protected void onStatusEffectsRemoved(Collection<StatusEffectInstance> effects) {
        if (this.getEntityWorld().isClient()) {
            return;
        }
        this.effectsChanged = true;
        for (StatusEffectInstance statusEffectInstance : effects) {
            statusEffectInstance.getEffectType().value().onRemoved(this.getAttributes());
            for (Entity entity : this.getPassengerList()) {
                if (!(entity instanceof ServerPlayerEntity)) continue;
                ServerPlayerEntity serverPlayerEntity = (ServerPlayerEntity)entity;
                serverPlayerEntity.networkHandler.sendPacket(new RemoveEntityStatusEffectS2CPacket(this.getId(), statusEffectInstance.getEffectType()));
            }
        }
        this.updateAttributes();
    }

    private void updateAttributes() {
        Set<EntityAttributeInstance> set = this.getAttributes().getPendingUpdate();
        for (EntityAttributeInstance entityAttributeInstance : set) {
            this.updateAttribute(entityAttributeInstance.getAttribute());
        }
        set.clear();
    }

    protected void updateAttribute(RegistryEntry<EntityAttribute> attribute) {
        World world;
        if (attribute.matches(EntityAttributes.MAX_HEALTH)) {
            float f = this.getMaxHealth();
            if (this.getHealth() > f) {
                this.setHealth(f);
            }
        } else if (attribute.matches(EntityAttributes.MAX_ABSORPTION)) {
            float f = this.getMaxAbsorption();
            if (this.getAbsorptionAmount() > f) {
                this.setAbsorptionAmount(f);
            }
        } else if (attribute.matches(EntityAttributes.SCALE)) {
            this.calculateDimensions();
        } else if (attribute.matches(EntityAttributes.WAYPOINT_TRANSMIT_RANGE) && (world = this.getEntityWorld()) instanceof ServerWorld) {
            ServerWorld serverWorld = (ServerWorld)world;
            ServerWaypointHandler serverWaypointHandler = serverWorld.getWaypointHandler();
            if (this.attributes.getValue(attribute) > 0.0) {
                serverWaypointHandler.onTrack(this);
            } else {
                serverWaypointHandler.onUntrack(this);
            }
        }
    }

    public void heal(float amount) {
        float f = this.getHealth();
        if (f > 0.0f) {
            this.setHealth(f + amount);
        }
    }

    public float getHealth() {
        return this.dataTracker.get(HEALTH).floatValue();
    }

    public void setHealth(float health) {
        this.dataTracker.set(HEALTH, Float.valueOf(MathHelper.clamp(health, 0.0f, this.getMaxHealth())));
    }

    public boolean isDead() {
        return this.getHealth() <= 0.0f;
    }

    @Override
    public boolean damage(ServerWorld world, DamageSource source, float amount) {
        Entity entity;
        boolean bl3;
        boolean bl;
        if (this.isInvulnerableTo(world, source)) {
            return false;
        }
        if (this.isDead()) {
            return false;
        }
        if (source.isIn(DamageTypeTags.IS_FIRE) && this.hasStatusEffect(StatusEffects.FIRE_RESISTANCE)) {
            return false;
        }
        if (this.isSleeping()) {
            this.wakeUp();
        }
        this.despawnCounter = 0;
        if (amount < 0.0f) {
            amount = 0.0f;
        }
        float f = amount;
        ItemStack itemStack = this.getActiveItem();
        float g = this.getDamageBlockedAmount(world, source, amount);
        amount -= g;
        boolean bl2 = bl = g > 0.0f;
        if (source.isIn(DamageTypeTags.IS_FREEZING) && this.getType().isIn(EntityTypeTags.FREEZE_HURTS_EXTRA_TYPES)) {
            amount *= 5.0f;
        }
        if (source.isIn(DamageTypeTags.DAMAGES_HELMET) && !this.getEquippedStack(EquipmentSlot.HEAD).isEmpty()) {
            this.damageHelmet(source, amount);
            amount *= 0.75f;
        }
        if (Float.isNaN(amount) || Float.isInfinite(amount)) {
            amount = Float.MAX_VALUE;
        }
        boolean bl22 = true;
        if ((float)this.timeUntilRegen > 10.0f && !source.isIn(DamageTypeTags.BYPASSES_COOLDOWN)) {
            if (amount <= this.lastDamageTaken) {
                return false;
            }
            this.applyDamage(world, source, amount - this.lastDamageTaken);
            this.lastDamageTaken = amount;
            bl22 = false;
        } else {
            this.lastDamageTaken = amount;
            this.timeUntilRegen = 20;
            this.applyDamage(world, source, amount);
            this.hurtTime = this.maxHurtTime = 10;
        }
        this.becomeAngry(source);
        this.setAttackingPlayer(source);
        if (bl22) {
            BlocksAttacksComponent blocksAttacksComponent = itemStack.get(DataComponentTypes.BLOCKS_ATTACKS);
            if (bl && blocksAttacksComponent != null) {
                blocksAttacksComponent.playBlockSound(world, this);
            } else {
                world.sendEntityDamage(this, source);
            }
            if (!(source.isIn(DamageTypeTags.NO_IMPACT) || bl && !(amount > 0.0f))) {
                this.scheduleVelocityUpdate();
            }
            if (!source.isIn(DamageTypeTags.NO_KNOCKBACK)) {
                double d = 0.0;
                double e = 0.0;
                Entity entity2 = source.getSource();
                if (entity2 instanceof ProjectileEntity) {
                    ProjectileEntity projectileEntity = (ProjectileEntity)entity2;
                    DoubleDoubleImmutablePair doubleDoubleImmutablePair = projectileEntity.getKnockback(this, source);
                    d = -doubleDoubleImmutablePair.leftDouble();
                    e = -doubleDoubleImmutablePair.rightDouble();
                } else if (source.getPosition() != null) {
                    d = source.getPosition().getX() - this.getX();
                    e = source.getPosition().getZ() - this.getZ();
                }
                this.takeKnockback(0.4f, d, e);
                if (!bl) {
                    this.tiltScreen(d, e);
                }
            }
        }
        if (this.isDead()) {
            if (!this.tryUseDeathProtector(source)) {
                if (bl22) {
                    this.playSound(this.getDeathSound());
                    this.playThornsSound(source);
                }
                this.onDeath(source);
            }
        } else if (bl22) {
            this.playHurtSound(source);
            this.playThornsSound(source);
        }
        boolean bl4 = bl3 = !bl || amount > 0.0f;
        if (bl3) {
            this.lastDamageSource = source;
            this.lastDamageTime = this.getEntityWorld().getTime();
            for (StatusEffectInstance statusEffectInstance : this.getStatusEffects()) {
                statusEffectInstance.onEntityDamage(world, this, source, amount);
            }
        }
        if ((entity = this) instanceof ServerPlayerEntity) {
            ServerPlayerEntity serverPlayerEntity = (ServerPlayerEntity)entity;
            Criteria.ENTITY_HURT_PLAYER.trigger(serverPlayerEntity, source, f, amount, bl);
            if (g > 0.0f && g < 3.4028235E37f) {
                serverPlayerEntity.increaseStat(Stats.DAMAGE_BLOCKED_BY_SHIELD, Math.round(g * 10.0f));
            }
        }
        if ((entity = source.getAttacker()) instanceof ServerPlayerEntity) {
            ServerPlayerEntity serverPlayerEntity = (ServerPlayerEntity)entity;
            Criteria.PLAYER_HURT_ENTITY.trigger(serverPlayerEntity, this, source, f, amount, bl);
        }
        return bl3;
    }

    public float getDamageBlockedAmount(ServerWorld world, DamageSource source, float amount) {
        Entity entity;
        double d;
        PersistentProjectileEntity persistentProjectileEntity;
        BlocksAttacksComponent blocksAttacksComponent;
        ItemStack itemStack;
        block10: {
            block9: {
                if (amount <= 0.0f) {
                    return 0.0f;
                }
                itemStack = this.getBlockingItem();
                if (itemStack == null) {
                    return 0.0f;
                }
                blocksAttacksComponent = itemStack.get(DataComponentTypes.BLOCKS_ATTACKS);
                if (blocksAttacksComponent == null) break block9;
                if (!blocksAttacksComponent.bypassedBy().map(source::isIn).orElse(false).booleanValue()) break block10;
            }
            return 0.0f;
        }
        Entity entity2 = source.getSource();
        if (entity2 instanceof PersistentProjectileEntity && (persistentProjectileEntity = (PersistentProjectileEntity)entity2).getPierceLevel() > 0) {
            return 0.0f;
        }
        Vec3d vec3d = source.getPosition();
        if (vec3d != null) {
            Vec3d vec3d2 = this.getRotationVector(0.0f, this.getHeadYaw());
            Vec3d vec3d3 = vec3d.subtract(this.getEntityPos());
            vec3d3 = new Vec3d(vec3d3.x, 0.0, vec3d3.z).normalize();
            d = Math.acos(vec3d3.dotProduct(vec3d2));
        } else {
            d = 3.1415927410125732;
        }
        float f = blocksAttacksComponent.getDamageReductionAmount(source, amount, d);
        blocksAttacksComponent.onShieldHit(this.getEntityWorld(), itemStack, this, this.getActiveHand(), f);
        if (f > 0.0f && !source.isIn(DamageTypeTags.IS_PROJECTILE) && (entity = source.getSource()) instanceof LivingEntity) {
            LivingEntity livingEntity = (LivingEntity)entity;
            this.takeShieldHit(world, livingEntity);
        }
        return f;
    }

    private void playThornsSound(DamageSource damageSource) {
        if (damageSource.isOf(DamageTypes.THORNS)) {
            SoundCategory soundCategory = this instanceof PlayerEntity ? SoundCategory.PLAYERS : SoundCategory.HOSTILE;
            this.getEntityWorld().playSound(null, this.getEntityPos().x, this.getEntityPos().y, this.getEntityPos().z, SoundEvents.ENCHANT_THORNS_HIT, soundCategory);
        }
    }

    protected void becomeAngry(DamageSource damageSource) {
        Entity entity = damageSource.getAttacker();
        if (entity instanceof LivingEntity) {
            LivingEntity livingEntity = (LivingEntity)entity;
            if (!(damageSource.isIn(DamageTypeTags.NO_ANGER) || damageSource.isOf(DamageTypes.WIND_CHARGE) && this.getType().isIn(EntityTypeTags.NO_ANGER_FROM_WIND_CHARGE))) {
                this.setAttacker(livingEntity);
            }
        }
    }

    protected @Nullable PlayerEntity setAttackingPlayer(DamageSource damageSource) {
        WolfEntity wolfEntity;
        Entity entity = damageSource.getAttacker();
        if (entity instanceof PlayerEntity) {
            PlayerEntity playerEntity = (PlayerEntity)entity;
            this.setAttacking(playerEntity, 100);
        } else if (entity instanceof WolfEntity && (wolfEntity = (WolfEntity)entity).isTamed()) {
            if (wolfEntity.getOwnerReference() != null) {
                this.setAttacking(wolfEntity.getOwnerReference().getUuid(), 100);
            } else {
                this.attackingPlayer = null;
                this.playerHitTimer = 0;
            }
        }
        return LazyEntityReference.getPlayerEntity(this.attackingPlayer, this.getEntityWorld());
    }

    protected void takeShieldHit(ServerWorld world, LivingEntity attacker) {
        attacker.knockback(this);
    }

    protected void knockback(LivingEntity target) {
        target.takeKnockback(0.5, target.getX() - this.getX(), target.getZ() - this.getZ());
    }

    private boolean tryUseDeathProtector(DamageSource source) {
        if (source.isIn(DamageTypeTags.BYPASSES_INVULNERABILITY)) {
            return false;
        }
        ItemStack itemStack = null;
        DeathProtectionComponent deathProtectionComponent = null;
        for (Hand hand : Hand.values()) {
            ItemStack itemStack2 = this.getStackInHand(hand);
            deathProtectionComponent = itemStack2.get(DataComponentTypes.DEATH_PROTECTION);
            if (deathProtectionComponent == null) continue;
            itemStack = itemStack2.copy();
            itemStack2.decrement(1);
            break;
        }
        if (itemStack != null) {
            LivingEntity livingEntity = this;
            if (livingEntity instanceof ServerPlayerEntity) {
                ServerPlayerEntity serverPlayerEntity = (ServerPlayerEntity)livingEntity;
                serverPlayerEntity.incrementStat(Stats.USED.getOrCreateStat(itemStack.getItem()));
                Criteria.USED_TOTEM.trigger(serverPlayerEntity, itemStack);
                itemStack.emitUseGameEvent(this, GameEvent.ITEM_INTERACT_FINISH);
            }
            this.setHealth(1.0f);
            deathProtectionComponent.applyDeathEffects(itemStack, this);
            this.getEntityWorld().sendEntityStatus(this, (byte)35);
        }
        return deathProtectionComponent != null;
    }

    public @Nullable DamageSource getRecentDamageSource() {
        if (this.getEntityWorld().getTime() - this.lastDamageTime > 40L) {
            this.lastDamageSource = null;
        }
        return this.lastDamageSource;
    }

    protected void playHurtSound(DamageSource damageSource) {
        this.playSound(this.getHurtSound(damageSource));
    }

    public void playSound(@Nullable SoundEvent sound) {
        if (sound != null) {
            this.playSound(sound, this.getSoundVolume(), this.getSoundPitch());
        }
    }

    private void playEquipmentBreakEffects(ItemStack stack) {
        if (!stack.isEmpty()) {
            RegistryEntry<SoundEvent> registryEntry = stack.get(DataComponentTypes.BREAK_SOUND);
            if (registryEntry != null && !this.isSilent()) {
                this.getEntityWorld().playSoundClient(this.getX(), this.getY(), this.getZ(), registryEntry.value(), this.getSoundCategory(), 0.8f, 0.8f + this.getEntityWorld().random.nextFloat() * 0.4f, false);
            }
            this.spawnItemParticles(stack, 5);
        }
    }

    public void onDeath(DamageSource damageSource) {
        if (this.isRemoved() || this.dead) {
            return;
        }
        Entity entity = damageSource.getAttacker();
        LivingEntity livingEntity = this.getPrimeAdversary();
        if (livingEntity != null) {
            livingEntity.updateKilledAdvancementCriterion(this, damageSource);
        }
        if (this.isSleeping()) {
            this.wakeUp();
        }
        this.clearActiveItem();
        if (!this.getEntityWorld().isClient() && this.hasCustomName()) {
            LOGGER.info("Named entity {} died: {}", (Object)this, (Object)this.getDamageTracker().getDeathMessage().getString());
        }
        this.dead = true;
        this.getDamageTracker().update();
        World world = this.getEntityWorld();
        if (world instanceof ServerWorld) {
            ServerWorld serverWorld = (ServerWorld)world;
            if (entity == null || entity.onKilledOther(serverWorld, this, damageSource)) {
                this.emitGameEvent(GameEvent.ENTITY_DIE);
                this.drop(serverWorld, damageSource);
                this.onKilledBy(livingEntity);
            }
            this.getEntityWorld().sendEntityStatus(this, (byte)3);
        }
        this.setPose(EntityPose.DYING);
    }

    protected void onKilledBy(@Nullable LivingEntity adversary) {
        World world = this.getEntityWorld();
        if (!(world instanceof ServerWorld)) {
            return;
        }
        ServerWorld serverWorld = (ServerWorld)world;
        boolean bl = false;
        if (adversary instanceof WitherEntity) {
            if (serverWorld.getGameRules().getValue(GameRules.DO_MOB_GRIEFING).booleanValue()) {
                BlockPos blockPos = this.getBlockPos();
                BlockState blockState = Blocks.WITHER_ROSE.getDefaultState();
                if (this.getEntityWorld().getBlockState(blockPos).isAir() && blockState.canPlaceAt(this.getEntityWorld(), blockPos)) {
                    this.getEntityWorld().setBlockState(blockPos, blockState, 3);
                    bl = true;
                }
            }
            if (!bl) {
                ItemEntity itemEntity = new ItemEntity(this.getEntityWorld(), this.getX(), this.getY(), this.getZ(), new ItemStack(Items.WITHER_ROSE));
                this.getEntityWorld().spawnEntity(itemEntity);
            }
        }
    }

    protected void drop(ServerWorld world, DamageSource damageSource) {
        boolean bl;
        boolean bl2 = bl = this.playerHitTimer > 0;
        if (this.shouldDropLoot(world)) {
            this.dropLoot(world, damageSource, bl);
            this.dropEquipment(world, damageSource, bl);
        }
        this.dropInventory(world);
        this.dropExperience(world, damageSource.getAttacker());
    }

    protected void dropInventory(ServerWorld world) {
    }

    protected void dropExperience(ServerWorld world, @Nullable Entity attacker) {
        if (!this.isExperienceDroppingDisabled() && (this.shouldAlwaysDropExperience() || this.playerHitTimer > 0 && this.shouldDropExperience() && world.getGameRules().getValue(GameRules.DO_MOB_LOOT).booleanValue())) {
            ExperienceOrbEntity.spawn(world, this.getEntityPos(), this.getExperienceToDrop(world, attacker));
        }
    }

    protected void dropEquipment(ServerWorld world, DamageSource source, boolean causedByPlayer) {
    }

    public long getLootTableSeed() {
        return 0L;
    }

    protected float getAttackKnockbackAgainst(Entity target, DamageSource damageSource) {
        float f = (float)this.getAttributeValue(EntityAttributes.ATTACK_KNOCKBACK);
        World world = this.getEntityWorld();
        if (world instanceof ServerWorld) {
            ServerWorld serverWorld = (ServerWorld)world;
            return EnchantmentHelper.modifyKnockback(serverWorld, this.getWeaponStack(), target, damageSource, f) / 2.0f;
        }
        return f / 2.0f;
    }

    protected void dropLoot(ServerWorld world, DamageSource damageSource, boolean causedByPlayer) {
        Optional<RegistryKey<LootTable>> optional = this.getLootTableKey();
        if (optional.isEmpty()) {
            return;
        }
        this.dropLoot(world, damageSource, causedByPlayer, optional.get());
    }

    public void dropLoot(ServerWorld world, DamageSource damageSource, boolean causedByPlayer, RegistryKey<LootTable> lootTableKey) {
        this.generateLoot(world, damageSource, causedByPlayer, lootTableKey, stack -> this.dropStack(world, (ItemStack)stack));
    }

    public void generateLoot(ServerWorld world, DamageSource damageSource, boolean causedByPlayer, RegistryKey<LootTable> lootTableKey, Consumer<ItemStack> lootConsumer) {
        LootTable lootTable = world.getServer().getReloadableRegistries().getLootTable(lootTableKey);
        LootWorldContext.Builder builder = new LootWorldContext.Builder(world).add(LootContextParameters.THIS_ENTITY, this).add(LootContextParameters.ORIGIN, this.getEntityPos()).add(LootContextParameters.DAMAGE_SOURCE, damageSource).addOptional(LootContextParameters.ATTACKING_ENTITY, damageSource.getAttacker()).addOptional(LootContextParameters.DIRECT_ATTACKING_ENTITY, damageSource.getSource());
        PlayerEntity playerEntity = this.getAttackingPlayer();
        if (causedByPlayer && playerEntity != null) {
            builder = builder.add(LootContextParameters.LAST_DAMAGE_PLAYER, playerEntity).luck(playerEntity.getLuck());
        }
        LootWorldContext lootWorldContext = builder.build(LootContextTypes.ENTITY);
        lootTable.generateLoot(lootWorldContext, this.getLootTableSeed(), lootConsumer);
    }

    public boolean forEachBrushedItem(ServerWorld world, RegistryKey<LootTable> lootTableKey, @Nullable Entity interactingEntity, ItemStack tool, BiConsumer<ServerWorld, ItemStack> lootConsumer) {
        return this.forEachGeneratedItem(world, lootTableKey, parameterSetBuilder -> parameterSetBuilder.add(LootContextParameters.TARGET_ENTITY, this).addOptional(LootContextParameters.INTERACTING_ENTITY, interactingEntity).add(LootContextParameters.TOOL, tool).build(LootContextTypes.ENTITY_INTERACT), lootConsumer);
    }

    public boolean forEachGiftedItem(ServerWorld world, RegistryKey<LootTable> lootTableKey, BiConsumer<ServerWorld, ItemStack> lootConsumer) {
        return this.forEachGeneratedItem(world, lootTableKey, parameterSetBuilder -> parameterSetBuilder.add(LootContextParameters.ORIGIN, this.getEntityPos()).add(LootContextParameters.THIS_ENTITY, this).build(LootContextTypes.GIFT), lootConsumer);
    }

    protected void forEachShearedItem(ServerWorld world, RegistryKey<LootTable> lootTableKey, ItemStack tool, BiConsumer<ServerWorld, ItemStack> lootConsumer) {
        this.forEachGeneratedItem(world, lootTableKey, parameterSetBuilder -> parameterSetBuilder.add(LootContextParameters.ORIGIN, this.getEntityPos()).add(LootContextParameters.THIS_ENTITY, this).add(LootContextParameters.TOOL, tool).build(LootContextTypes.SHEARING), lootConsumer);
    }

    protected boolean forEachGeneratedItem(ServerWorld world, RegistryKey<LootTable> lootTableKey, Function<LootWorldContext.Builder, LootWorldContext> lootContextParametersFactory, BiConsumer<ServerWorld, ItemStack> lootConsumer) {
        LootWorldContext lootWorldContext;
        LootTable lootTable = world.getServer().getReloadableRegistries().getLootTable(lootTableKey);
        ObjectArrayList<ItemStack> list = lootTable.generateLoot(lootWorldContext = lootContextParametersFactory.apply(new LootWorldContext.Builder(world)));
        if (!list.isEmpty()) {
            list.forEach(stack -> lootConsumer.accept(world, (ItemStack)stack));
            return true;
        }
        return false;
    }

    public void takeKnockback(double strength, double x, double z) {
        if ((strength *= 1.0 - this.getAttributeValue(EntityAttributes.KNOCKBACK_RESISTANCE)) <= 0.0) {
            return;
        }
        this.velocityDirty = true;
        Vec3d vec3d = this.getVelocity();
        while (x * x + z * z < (double)1.0E-5f) {
            x = (this.random.nextDouble() - this.random.nextDouble()) * 0.01;
            z = (this.random.nextDouble() - this.random.nextDouble()) * 0.01;
        }
        Vec3d vec3d2 = new Vec3d(x, 0.0, z).normalize().multiply(strength);
        this.setVelocity(vec3d.x / 2.0 - vec3d2.x, this.isOnGround() ? Math.min(0.4, vec3d.y / 2.0 + strength) : vec3d.y, vec3d.z / 2.0 - vec3d2.z);
    }

    public void tiltScreen(double deltaX, double deltaZ) {
    }

    protected @Nullable SoundEvent getHurtSound(DamageSource source) {
        return SoundEvents.ENTITY_GENERIC_HURT;
    }

    protected @Nullable SoundEvent getDeathSound() {
        return SoundEvents.ENTITY_GENERIC_DEATH;
    }

    private SoundEvent getFallSound(int distance) {
        return distance > 4 ? this.getFallSounds().big() : this.getFallSounds().small();
    }

    public void disableExperienceDropping() {
        this.experienceDroppingDisabled = true;
    }

    public boolean isExperienceDroppingDisabled() {
        return this.experienceDroppingDisabled;
    }

    public float getDamageTiltYaw() {
        return 0.0f;
    }

    protected Box getHitbox() {
        Box box = this.getBoundingBox();
        Entity entity = this.getVehicle();
        if (entity != null) {
            Vec3d vec3d = entity.getPassengerRidingPos(this);
            return box.withMinY(Math.max(vec3d.y, box.minY));
        }
        return box;
    }

    public Map<Enchantment, Set<EnchantmentLocationBasedEffect>> getLocationBasedEnchantmentEffects(EquipmentSlot slot) {
        return (Map)this.locationBasedEnchantmentEffects.computeIfAbsent(slot, slotx -> new Reference2ObjectArrayMap());
    }

    public void useAttackEnchantmentEffects() {
        World world = this.getEntityWorld();
        if (world instanceof ServerWorld) {
            ServerWorld serverWorld = (ServerWorld)world;
            EnchantmentHelper.onAttack(serverWorld, this);
        }
    }

    public FallSounds getFallSounds() {
        return new FallSounds(SoundEvents.ENTITY_GENERIC_SMALL_FALL, SoundEvents.ENTITY_GENERIC_BIG_FALL);
    }

    public Optional<BlockPos> getClimbingPos() {
        return this.climbingPos;
    }

    public boolean isClimbing() {
        if (this.isSpectator()) {
            return false;
        }
        BlockPos blockPos = this.getBlockPos();
        BlockState blockState = this.getBlockStateAtPos();
        if (this.isGliding() && blockState.isIn(BlockTags.CAN_GLIDE_THROUGH)) {
            return false;
        }
        if (blockState.isIn(BlockTags.CLIMBABLE)) {
            this.climbingPos = Optional.of(blockPos);
            return true;
        }
        if (blockState.getBlock() instanceof TrapdoorBlock && this.canEnterTrapdoor(blockPos, blockState)) {
            this.climbingPos = Optional.of(blockPos);
            return true;
        }
        return false;
    }

    private boolean canEnterTrapdoor(BlockPos pos, BlockState state) {
        if (state.get(TrapdoorBlock.OPEN).booleanValue()) {
            BlockState blockState = this.getEntityWorld().getBlockState(pos.down());
            return blockState.isOf(Blocks.LADDER) && blockState.get(LadderBlock.FACING) == state.get(TrapdoorBlock.FACING);
        }
        return false;
    }

    @Override
    public boolean isAlive() {
        return !this.isRemoved() && this.getHealth() > 0.0f;
    }

    public boolean isEntityLookingAtMe(LivingEntity entity, double d, boolean bl, boolean visualShape, double ... checkedYs) {
        Vec3d vec3d = entity.getRotationVec(1.0f).normalize();
        for (double e : checkedYs) {
            Vec3d vec3d2 = new Vec3d(this.getX() - entity.getX(), e - entity.getEyeY(), this.getZ() - entity.getZ());
            double f = vec3d2.length();
            vec3d2 = vec3d2.normalize();
            double g = vec3d.dotProduct(vec3d2);
            double d2 = bl ? f : 1.0;
            if (!(g > 1.0 - d / d2) || !entity.canSee(this, visualShape ? RaycastContext.ShapeType.VISUAL : RaycastContext.ShapeType.COLLIDER, RaycastContext.FluidHandling.NONE, e)) continue;
            return true;
        }
        return false;
    }

    @Override
    public int getSafeFallDistance() {
        return this.getSafeFallDistance(0.0f);
    }

    protected final int getSafeFallDistance(float health) {
        return MathHelper.floor(health + 3.0f);
    }

    @Override
    public boolean handleFallDamage(double fallDistance, float damagePerDistance, DamageSource damageSource) {
        boolean bl = super.handleFallDamage(fallDistance, damagePerDistance, damageSource);
        int i = this.computeFallDamage(fallDistance, damagePerDistance);
        if (i > 0) {
            this.playSound(this.getFallSound(i), 1.0f, 1.0f);
            this.playBlockFallSound();
            this.serverDamage(damageSource, i);
            return true;
        }
        return bl;
    }

    protected int computeFallDamage(double fallDistance, float damagePerDistance) {
        if (this.getType().isIn(EntityTypeTags.FALL_DAMAGE_IMMUNE)) {
            return 0;
        }
        double d = this.getUnsafeFallDistance(fallDistance);
        return MathHelper.floor(d * (double)damagePerDistance * this.getAttributeValue(EntityAttributes.FALL_DAMAGE_MULTIPLIER));
    }

    private double getUnsafeFallDistance(double fallDistance) {
        return fallDistance + 1.0E-6 - this.getAttributeValue(EntityAttributes.SAFE_FALL_DISTANCE);
    }

    protected void playBlockFallSound() {
        if (this.isSilent()) {
            return;
        }
        int i = MathHelper.floor(this.getX());
        int j = MathHelper.floor(this.getY() - (double)0.2f);
        int k = MathHelper.floor(this.getZ());
        BlockState blockState = this.getEntityWorld().getBlockState(new BlockPos(i, j, k));
        if (!blockState.isAir()) {
            BlockSoundGroup blockSoundGroup = blockState.getSoundGroup();
            this.playSound(blockSoundGroup.getFallSound(), blockSoundGroup.getVolume() * 0.5f, blockSoundGroup.getPitch() * 0.75f);
        }
    }

    @Override
    public void animateDamage(float yaw) {
        this.hurtTime = this.maxHurtTime = 10;
    }

    public int getArmor() {
        return MathHelper.floor(this.getAttributeValue(EntityAttributes.ARMOR));
    }

    protected void damageArmor(DamageSource source, float amount) {
    }

    protected void damageHelmet(DamageSource source, float amount) {
    }

    protected void damageEquipment(DamageSource source, float amount, EquipmentSlot ... slots) {
        if (amount <= 0.0f) {
            return;
        }
        int i = (int)Math.max(1.0f, amount / 4.0f);
        for (EquipmentSlot equipmentSlot : slots) {
            ItemStack itemStack = this.getEquippedStack(equipmentSlot);
            EquippableComponent equippableComponent = itemStack.get(DataComponentTypes.EQUIPPABLE);
            if (equippableComponent == null || !equippableComponent.damageOnHurt() || !itemStack.isDamageable() || !itemStack.takesDamageFrom(source)) continue;
            itemStack.damage(i, this, equipmentSlot);
        }
    }

    protected float applyArmorToDamage(DamageSource source, float amount) {
        if (!source.isIn(DamageTypeTags.BYPASSES_ARMOR)) {
            this.damageArmor(source, amount);
            amount = DamageUtil.getDamageLeft(this, amount, source, this.getArmor(), (float)this.getAttributeValue(EntityAttributes.ARMOR_TOUGHNESS));
        }
        return amount;
    }

    protected float modifyAppliedDamage(DamageSource source, float amount) {
        float k;
        int i;
        int j;
        float f;
        float g;
        float h;
        if (source.isIn(DamageTypeTags.BYPASSES_EFFECTS)) {
            return amount;
        }
        if (this.hasStatusEffect(StatusEffects.RESISTANCE) && !source.isIn(DamageTypeTags.BYPASSES_RESISTANCE) && (h = (g = amount) - (amount = Math.max((f = amount * (float)(j = 25 - (i = (this.getStatusEffect(StatusEffects.RESISTANCE).getAmplifier() + 1) * 5))) / 25.0f, 0.0f))) > 0.0f && h < 3.4028235E37f) {
            if (this instanceof ServerPlayerEntity) {
                ((ServerPlayerEntity)this).increaseStat(Stats.DAMAGE_RESISTED, Math.round(h * 10.0f));
            } else if (source.getAttacker() instanceof ServerPlayerEntity) {
                ((ServerPlayerEntity)source.getAttacker()).increaseStat(Stats.DAMAGE_DEALT_RESISTED, Math.round(h * 10.0f));
            }
        }
        if (amount <= 0.0f) {
            return 0.0f;
        }
        if (source.isIn(DamageTypeTags.BYPASSES_ENCHANTMENTS)) {
            return amount;
        }
        World world = this.getEntityWorld();
        if (world instanceof ServerWorld) {
            ServerWorld serverWorld = (ServerWorld)world;
            k = EnchantmentHelper.getProtectionAmount(serverWorld, this, source);
        } else {
            k = 0.0f;
        }
        if (k > 0.0f) {
            amount = DamageUtil.getInflictedDamage(amount, k);
        }
        return amount;
    }

    protected void applyDamage(ServerWorld world, DamageSource source, float amount) {
        Entity entity;
        if (this.isInvulnerableTo(world, source)) {
            return;
        }
        amount = this.applyArmorToDamage(source, amount);
        float f = amount = this.modifyAppliedDamage(source, amount);
        amount = Math.max(amount - this.getAbsorptionAmount(), 0.0f);
        this.setAbsorptionAmount(this.getAbsorptionAmount() - (f - amount));
        float g = f - amount;
        if (g > 0.0f && g < 3.4028235E37f && (entity = source.getAttacker()) instanceof ServerPlayerEntity) {
            ServerPlayerEntity serverPlayerEntity = (ServerPlayerEntity)entity;
            serverPlayerEntity.increaseStat(Stats.DAMAGE_DEALT_ABSORBED, Math.round(g * 10.0f));
        }
        if (amount == 0.0f) {
            return;
        }
        this.getDamageTracker().onDamage(source, amount);
        this.setHealth(this.getHealth() - amount);
        this.setAbsorptionAmount(this.getAbsorptionAmount() - amount);
        this.emitGameEvent(GameEvent.ENTITY_DAMAGE);
    }

    public DamageTracker getDamageTracker() {
        return this.damageTracker;
    }

    public @Nullable LivingEntity getPrimeAdversary() {
        if (this.attackingPlayer != null) {
            return this.attackingPlayer.getEntityByClass(this.getEntityWorld(), PlayerEntity.class);
        }
        if (this.attackerReference != null) {
            return this.attackerReference.getEntityByClass(this.getEntityWorld(), LivingEntity.class);
        }
        return null;
    }

    public final float getMaxHealth() {
        return (float)this.getAttributeValue(EntityAttributes.MAX_HEALTH);
    }

    public final float getMaxAbsorption() {
        return (float)this.getAttributeValue(EntityAttributes.MAX_ABSORPTION);
    }

    public final int getStuckArrowCount() {
        return this.dataTracker.get(STUCK_ARROW_COUNT);
    }

    public final void setStuckArrowCount(int stuckArrowCount) {
        this.dataTracker.set(STUCK_ARROW_COUNT, stuckArrowCount);
    }

    public final int getStingerCount() {
        return this.dataTracker.get(STINGER_COUNT);
    }

    public final void setStingerCount(int stingerCount) {
        this.dataTracker.set(STINGER_COUNT, stingerCount);
    }

    private int getHandSwingDuration() {
        ItemStack itemStack = this.getStackInHand(Hand.MAIN_HAND);
        int i = itemStack.getSwingAnimation().duration();
        if (StatusEffectUtil.hasHaste(this)) {
            return i - (1 + StatusEffectUtil.getHasteAmplifier(this));
        }
        if (this.hasStatusEffect(StatusEffects.MINING_FATIGUE)) {
            return i + (1 + this.getStatusEffect(StatusEffects.MINING_FATIGUE).getAmplifier()) * 2;
        }
        return i;
    }

    public void swingHand(Hand hand) {
        this.swingHand(hand, false);
    }

    public void swingHand(Hand hand, boolean fromServerPlayer) {
        if (!this.handSwinging || this.handSwingTicks >= this.getHandSwingDuration() / 2 || this.handSwingTicks < 0) {
            this.handSwingTicks = -1;
            this.handSwinging = true;
            this.preferredHand = hand;
            if (this.getEntityWorld() instanceof ServerWorld) {
                EntityAnimationS2CPacket entityAnimationS2CPacket = new EntityAnimationS2CPacket(this, hand == Hand.MAIN_HAND ? 0 : 3);
                ServerChunkManager serverChunkManager = ((ServerWorld)this.getEntityWorld()).getChunkManager();
                if (fromServerPlayer) {
                    serverChunkManager.sendToNearbyPlayers(this, entityAnimationS2CPacket);
                } else {
                    serverChunkManager.sendToOtherNearbyPlayers(this, entityAnimationS2CPacket);
                }
            }
        }
    }

    @Override
    public void onDamaged(DamageSource damageSource) {
        this.limbAnimator.setSpeed(1.5f);
        this.timeUntilRegen = 20;
        this.hurtTime = this.maxHurtTime = 10;
        SoundEvent soundEvent = this.getHurtSound(damageSource);
        if (soundEvent != null) {
            this.playSound(soundEvent, this.getSoundVolume(), (this.random.nextFloat() - this.random.nextFloat()) * 0.2f + 1.0f);
        }
        this.lastDamageSource = damageSource;
        this.lastDamageTime = this.getEntityWorld().getTime();
    }

    @Override
    public void handleStatus(byte status) {
        switch (status) {
            case 3: {
                SoundEvent soundEvent = this.getDeathSound();
                if (soundEvent != null) {
                    this.playSound(soundEvent, this.getSoundVolume(), (this.random.nextFloat() - this.random.nextFloat()) * 0.2f + 1.0f);
                }
                if (this instanceof PlayerEntity) break;
                this.setHealth(0.0f);
                this.onDeath(this.getDamageSources().generic());
                break;
            }
            case 46: {
                int i = 128;
                for (int j = 0; j < 128; ++j) {
                    double d = (double)j / 127.0;
                    float f = (this.random.nextFloat() - 0.5f) * 0.2f;
                    float g = (this.random.nextFloat() - 0.5f) * 0.2f;
                    float h = (this.random.nextFloat() - 0.5f) * 0.2f;
                    double e = MathHelper.lerp(d, this.lastX, this.getX()) + (this.random.nextDouble() - 0.5) * (double)this.getWidth() * 2.0;
                    double k = MathHelper.lerp(d, this.lastY, this.getY()) + this.random.nextDouble() * (double)this.getHeight();
                    double l = MathHelper.lerp(d, this.lastZ, this.getZ()) + (this.random.nextDouble() - 0.5) * (double)this.getWidth() * 2.0;
                    this.getEntityWorld().addParticleClient(ParticleTypes.PORTAL, e, k, l, f, g, h);
                }
                break;
            }
            case 47: {
                this.playEquipmentBreakEffects(this.getEquippedStack(EquipmentSlot.MAINHAND));
                break;
            }
            case 48: {
                this.playEquipmentBreakEffects(this.getEquippedStack(EquipmentSlot.OFFHAND));
                break;
            }
            case 49: {
                this.playEquipmentBreakEffects(this.getEquippedStack(EquipmentSlot.HEAD));
                break;
            }
            case 50: {
                this.playEquipmentBreakEffects(this.getEquippedStack(EquipmentSlot.CHEST));
                break;
            }
            case 51: {
                this.playEquipmentBreakEffects(this.getEquippedStack(EquipmentSlot.LEGS));
                break;
            }
            case 52: {
                this.playEquipmentBreakEffects(this.getEquippedStack(EquipmentSlot.FEET));
                break;
            }
            case 65: {
                this.playEquipmentBreakEffects(this.getEquippedStack(EquipmentSlot.BODY));
                break;
            }
            case 68: {
                this.playEquipmentBreakEffects(this.getEquippedStack(EquipmentSlot.SADDLE));
                break;
            }
            case 54: {
                HoneyBlock.addRichParticles(this);
                break;
            }
            case 55: {
                this.swapHandStacks();
                break;
            }
            case 60: {
                this.addDeathParticles();
                break;
            }
            case 67: {
                this.addBubbleParticles();
                break;
            }
            case 2: {
                this.playKineticHitSound();
                break;
            }
            default: {
                super.handleStatus(status);
            }
        }
    }

    public float getTimeSinceLastKineticAttack(float tickProgress) {
        if (this.lastKineticAttackTime < 0L) {
            return 0.0f;
        }
        return (float)(this.getEntityWorld().getTime() - this.lastKineticAttackTime) + tickProgress;
    }

    public void addDeathParticles() {
        for (int i = 0; i < 20; ++i) {
            double d = this.random.nextGaussian() * 0.02;
            double e = this.random.nextGaussian() * 0.02;
            double f = this.random.nextGaussian() * 0.02;
            double g = 10.0;
            this.getEntityWorld().addParticleClient(ParticleTypes.POOF, this.getParticleX(1.0) - d * 10.0, this.getRandomBodyY() - e * 10.0, this.getParticleZ(1.0) - f * 10.0, d, e, f);
        }
    }

    private void addBubbleParticles() {
        Vec3d vec3d = this.getVelocity();
        for (int i = 0; i < 8; ++i) {
            double d = this.random.nextTriangular(0.0, 1.0);
            double e = this.random.nextTriangular(0.0, 1.0);
            double f = this.random.nextTriangular(0.0, 1.0);
            this.getEntityWorld().addParticleClient(ParticleTypes.BUBBLE, this.getX() + d, this.getY() + e, this.getZ() + f, vec3d.x, vec3d.y, vec3d.z);
        }
    }

    private void playKineticHitSound() {
        if (this.getEntityWorld().getTime() - this.lastKineticAttackTime <= 10L) {
            return;
        }
        this.lastKineticAttackTime = this.getEntityWorld().getTime();
        KineticWeaponComponent kineticWeaponComponent = this.activeItemStack.get(DataComponentTypes.KINETIC_WEAPON);
        if (kineticWeaponComponent == null) {
            return;
        }
        kineticWeaponComponent.playHitSound(this);
    }

    private void swapHandStacks() {
        ItemStack itemStack = this.getEquippedStack(EquipmentSlot.OFFHAND);
        this.equipStack(EquipmentSlot.OFFHAND, this.getEquippedStack(EquipmentSlot.MAINHAND));
        this.equipStack(EquipmentSlot.MAINHAND, itemStack);
    }

    @Override
    protected void tickInVoid() {
        this.serverDamage(this.getDamageSources().outOfWorld(), 4.0f);
    }

    protected void tickHandSwing() {
        int i = this.getHandSwingDuration();
        if (this.handSwinging) {
            ++this.handSwingTicks;
            if (this.handSwingTicks >= i) {
                this.handSwingTicks = 0;
                this.handSwinging = false;
            }
        } else {
            this.handSwingTicks = 0;
        }
        this.handSwingProgress = (float)this.handSwingTicks / (float)i;
    }

    public @Nullable EntityAttributeInstance getAttributeInstance(RegistryEntry<EntityAttribute> attribute) {
        return this.getAttributes().getCustomInstance(attribute);
    }

    public double getAttributeValue(RegistryEntry<EntityAttribute> attribute) {
        return this.getAttributes().getValue(attribute);
    }

    public double getAttributeBaseValue(RegistryEntry<EntityAttribute> attribute) {
        return this.getAttributes().getBaseValue(attribute);
    }

    public AttributeContainer getAttributes() {
        return this.attributes;
    }

    public ItemStack getMainHandStack() {
        return this.getEquippedStack(EquipmentSlot.MAINHAND);
    }

    public ItemStack getOffHandStack() {
        return this.getEquippedStack(EquipmentSlot.OFFHAND);
    }

    public ItemStack getStackInArm(Arm arm) {
        return this.getMainArm() == arm ? this.getMainHandStack() : this.getOffHandStack();
    }

    @Override
    public ItemStack getWeaponStack() {
        return this.getMainHandStack();
    }

    public AttackRangeComponent getAttackRange() {
        AttackRangeComponent attackRangeComponent = this.getActiveOrMainHandStack().get(DataComponentTypes.ATTACK_RANGE);
        return attackRangeComponent != null ? attackRangeComponent : AttackRangeComponent.defaultForEntity(this);
    }

    public ItemStack getActiveOrMainHandStack() {
        if (this.isUsingItem()) {
            return this.getActiveItem();
        }
        return this.getMainHandStack();
    }

    public boolean isHolding(Item item) {
        return this.isHolding((ItemStack stack) -> stack.isOf(item));
    }

    public boolean isHolding(Predicate<ItemStack> predicate) {
        return predicate.test(this.getMainHandStack()) || predicate.test(this.getOffHandStack());
    }

    public ItemStack getStackInHand(Hand hand) {
        if (hand == Hand.MAIN_HAND) {
            return this.getEquippedStack(EquipmentSlot.MAINHAND);
        }
        if (hand == Hand.OFF_HAND) {
            return this.getEquippedStack(EquipmentSlot.OFFHAND);
        }
        throw new IllegalArgumentException("Invalid hand " + String.valueOf((Object)hand));
    }

    public void setStackInHand(Hand hand, ItemStack stack) {
        if (hand == Hand.MAIN_HAND) {
            this.equipStack(EquipmentSlot.MAINHAND, stack);
        } else if (hand == Hand.OFF_HAND) {
            this.equipStack(EquipmentSlot.OFFHAND, stack);
        } else {
            throw new IllegalArgumentException("Invalid hand " + String.valueOf((Object)hand));
        }
    }

    public boolean hasStackEquipped(EquipmentSlot slot) {
        return !this.getEquippedStack(slot).isEmpty();
    }

    public boolean canUseSlot(EquipmentSlot slot) {
        return true;
    }

    public ItemStack getEquippedStack(EquipmentSlot slot) {
        return this.equipment.get(slot);
    }

    public void equipStack(EquipmentSlot slot, ItemStack stack) {
        this.onEquipStack(slot, this.equipment.put(slot, stack), stack);
    }

    public float getArmorVisibility() {
        int i = 0;
        int j = 0;
        for (EquipmentSlot equipmentSlot : AttributeModifierSlot.ARMOR) {
            if (equipmentSlot.getType() != EquipmentSlot.Type.HUMANOID_ARMOR) continue;
            ItemStack itemStack = this.getEquippedStack(equipmentSlot);
            if (!itemStack.isEmpty()) {
                ++j;
            }
            ++i;
        }
        return i > 0 ? (float)j / (float)i : 0.0f;
    }

    @Override
    public void setSprinting(boolean sprinting) {
        super.setSprinting(sprinting);
        EntityAttributeInstance entityAttributeInstance = this.getAttributeInstance(EntityAttributes.MOVEMENT_SPEED);
        entityAttributeInstance.removeModifier(SPRINTING_SPEED_BOOST.id());
        if (sprinting) {
            entityAttributeInstance.addTemporaryModifier(SPRINTING_SPEED_BOOST);
        }
    }

    protected float getSoundVolume() {
        return 1.0f;
    }

    public float getSoundPitch() {
        if (this.isBaby()) {
            return (this.random.nextFloat() - this.random.nextFloat()) * 0.2f + 1.5f;
        }
        return (this.random.nextFloat() - this.random.nextFloat()) * 0.2f + 1.0f;
    }

    protected boolean isImmobile() {
        return this.isDead();
    }

    @Override
    public void pushAwayFrom(Entity entity) {
        if (!this.isSleeping()) {
            super.pushAwayFrom(entity);
        }
    }

    private void onDismounted(Entity vehicle) {
        Vec3d vec3d;
        if (this.isRemoved()) {
            vec3d = this.getEntityPos();
        } else if (vehicle.isRemoved() || this.getEntityWorld().getBlockState(vehicle.getBlockPos()).isIn(BlockTags.PORTALS)) {
            boolean bl;
            double d = Math.max(this.getY(), vehicle.getY());
            vec3d = new Vec3d(this.getX(), d, this.getZ());
            boolean bl2 = bl = this.getWidth() <= 4.0f && this.getHeight() <= 4.0f;
            if (bl) {
                double e = (double)this.getHeight() / 2.0;
                Vec3d vec3d2 = vec3d.add(0.0, e, 0.0);
                VoxelShape voxelShape = VoxelShapes.cuboid(Box.of(vec3d2, this.getWidth(), this.getHeight(), this.getWidth()));
                vec3d = this.getEntityWorld().findClosestCollision(this, voxelShape, vec3d2, this.getWidth(), this.getHeight(), this.getWidth()).map(pos -> pos.add(0.0, -e, 0.0)).orElse(vec3d);
            }
        } else {
            vec3d = vehicle.updatePassengerForDismount(this);
        }
        this.requestTeleportAndDismount(vec3d.x, vec3d.y, vec3d.z);
    }

    @Override
    public boolean shouldRenderName() {
        return this.isCustomNameVisible();
    }

    protected float getJumpVelocity() {
        return this.getJumpVelocity(1.0f);
    }

    protected float getJumpVelocity(float strength) {
        return (float)this.getAttributeValue(EntityAttributes.JUMP_STRENGTH) * strength * this.getJumpVelocityMultiplier() + this.getJumpBoostVelocityModifier();
    }

    public float getJumpBoostVelocityModifier() {
        return this.hasStatusEffect(StatusEffects.JUMP_BOOST) ? 0.1f * ((float)this.getStatusEffect(StatusEffects.JUMP_BOOST).getAmplifier() + 1.0f) : 0.0f;
    }

    @VisibleForTesting
    public void jump() {
        float f = this.getJumpVelocity();
        if (f <= 1.0E-5f) {
            return;
        }
        Vec3d vec3d = this.getVelocity();
        this.setVelocity(vec3d.x, Math.max((double)f, vec3d.y), vec3d.z);
        if (this.isSprinting()) {
            float g = this.getYaw() * ((float)Math.PI / 180);
            this.addVelocityInternal(new Vec3d((double)(-MathHelper.sin(g)) * 0.2, 0.0, (double)MathHelper.cos(g) * 0.2));
        }
        this.velocityDirty = true;
    }

    protected void knockDownwards() {
        this.setVelocity(this.getVelocity().add(0.0, -0.04f, 0.0));
    }

    protected void swimUpward(TagKey<Fluid> fluid) {
        this.setVelocity(this.getVelocity().add(0.0, 0.04f, 0.0));
    }

    protected float getBaseWaterMovementSpeedMultiplier() {
        return 0.8f;
    }

    public boolean canWalkOnFluid(FluidState state) {
        return false;
    }

    @Override
    protected double getGravity() {
        return this.getAttributeValue(EntityAttributes.GRAVITY);
    }

    protected double getEffectiveGravity() {
        boolean bl;
        boolean bl2 = bl = this.getVelocity().y <= 0.0;
        if (bl && this.hasStatusEffect(StatusEffects.SLOW_FALLING)) {
            return Math.min(this.getFinalGravity(), 0.01);
        }
        return this.getFinalGravity();
    }

    public void travel(Vec3d movementInput) {
        if (this.isTravellingInFluid(this.getEntityWorld().getFluidState(this.getBlockPos()))) {
            this.travelInFluid(movementInput);
        } else if (this.isGliding()) {
            this.travelGliding(movementInput);
        } else {
            this.travelMidAir(movementInput);
        }
    }

    protected boolean isTravellingInFluid(FluidState state) {
        return (this.isTouchingWater() || this.isInLava()) && this.shouldSwimInFluids() && !this.canWalkOnFluid(state);
    }

    protected void travelFlying(Vec3d movementInput, float speed) {
        this.travelFlying(movementInput, 0.02f, 0.02f, speed);
    }

    protected void travelFlying(Vec3d movementInput, float inWaterSpeed, float inLavaSpeed, float regularSpeed) {
        if (this.isTouchingWater()) {
            this.updateVelocity(inWaterSpeed, movementInput);
            this.move(MovementType.SELF, this.getVelocity());
            this.setVelocity(this.getVelocity().multiply(0.8f));
        } else if (this.isInLava()) {
            this.updateVelocity(inLavaSpeed, movementInput);
            this.move(MovementType.SELF, this.getVelocity());
            this.setVelocity(this.getVelocity().multiply(0.5));
        } else {
            this.updateVelocity(regularSpeed, movementInput);
            this.move(MovementType.SELF, this.getVelocity());
            this.setVelocity(this.getVelocity().multiply(0.91f));
        }
    }

    private void travelMidAir(Vec3d movementInput) {
        BlockPos blockPos = this.getVelocityAffectingPos();
        float f = this.isOnGround() ? this.getEntityWorld().getBlockState(blockPos).getBlock().getSlipperiness() : 1.0f;
        float g = f * 0.91f;
        Vec3d vec3d = this.applyMovementInput(movementInput, f);
        double d = vec3d.y;
        StatusEffectInstance statusEffectInstance = this.getStatusEffect(StatusEffects.LEVITATION);
        d = statusEffectInstance != null ? (d += (0.05 * (double)(statusEffectInstance.getAmplifier() + 1) - vec3d.y) * 0.2) : (!this.getEntityWorld().isClient() || this.getEntityWorld().isChunkLoaded(blockPos) ? (d -= this.getEffectiveGravity()) : (this.getY() > (double)this.getEntityWorld().getBottomY() ? -0.1 : 0.0));
        if (this.hasNoDrag()) {
            this.setVelocity(vec3d.x, d, vec3d.z);
        } else {
            float h = this instanceof Flutterer ? g : 0.98f;
            this.setVelocity(vec3d.x * (double)g, d * (double)h, vec3d.z * (double)g);
        }
    }

    private void travelInFluid(Vec3d movementInput) {
        boolean bl = this.getVelocity().y <= 0.0;
        double d = this.getY();
        double e = this.getEffectiveGravity();
        if (this.isTouchingWater()) {
            this.travelInWater(movementInput, e, bl, d);
            this.floatIfRidden();
        } else {
            this.travelInLava(movementInput, e, bl, d);
        }
    }

    protected void travelInWater(Vec3d movementInput, double gravity, boolean falling, double y) {
        float f = this.isSprinting() ? 0.9f : this.getBaseWaterMovementSpeedMultiplier();
        float g = 0.02f;
        float h = (float)this.getAttributeValue(EntityAttributes.WATER_MOVEMENT_EFFICIENCY);
        if (!this.isOnGround()) {
            h *= 0.5f;
        }
        if (h > 0.0f) {
            f += (0.54600006f - f) * h;
            g += (this.getMovementSpeed() - g) * h;
        }
        if (this.hasStatusEffect(StatusEffects.DOLPHINS_GRACE)) {
            f = 0.96f;
        }
        this.updateVelocity(g, movementInput);
        this.move(MovementType.SELF, this.getVelocity());
        Vec3d vec3d = this.getVelocity();
        if (this.horizontalCollision && this.isClimbing()) {
            vec3d = new Vec3d(vec3d.x, 0.2, vec3d.z);
        }
        vec3d = vec3d.multiply(f, 0.8f, f);
        this.setVelocity(this.applyFluidMovingSpeed(gravity, falling, vec3d));
        this.resetVerticalVelocityInFluid(y);
    }

    private void travelInLava(Vec3d movementInput, double gravity, boolean falling, double y) {
        this.updateVelocity(0.02f, movementInput);
        this.move(MovementType.SELF, this.getVelocity());
        if (this.getFluidHeight(FluidTags.LAVA) <= this.getSwimHeight()) {
            this.setVelocity(this.getVelocity().multiply(0.5, 0.8f, 0.5));
            Vec3d vec3d = this.applyFluidMovingSpeed(gravity, falling, this.getVelocity());
            this.setVelocity(vec3d);
        } else {
            this.setVelocity(this.getVelocity().multiply(0.5));
        }
        if (gravity != 0.0) {
            this.setVelocity(this.getVelocity().add(0.0, -gravity / 4.0, 0.0));
        }
        this.resetVerticalVelocityInFluid(y);
    }

    private void resetVerticalVelocityInFluid(double y) {
        Vec3d vec3d = this.getVelocity();
        if (this.horizontalCollision && this.doesNotCollide(vec3d.x, vec3d.y + (double)0.6f - this.getY() + y, vec3d.z)) {
            this.setVelocity(vec3d.x, 0.3f, vec3d.z);
        }
    }

    private void floatIfRidden() {
        boolean bl = this.getType().isIn(EntityTypeTags.CAN_FLOAT_WHILE_RIDDEN);
        if (bl && this.hasPassengers() && this.getFluidHeight(FluidTags.WATER) > this.getSwimHeight()) {
            this.setVelocity(this.getVelocity().add(0.0, 0.04f, 0.0));
        }
    }

    private void travelGliding(Vec3d movementInput) {
        if (this.isClimbing()) {
            this.travelMidAir(movementInput);
            this.stopGliding();
            return;
        }
        Vec3d vec3d = this.getVelocity();
        double d = vec3d.horizontalLength();
        this.setVelocity(this.calcGlidingVelocity(vec3d));
        this.move(MovementType.SELF, this.getVelocity());
        if (!this.getEntityWorld().isClient()) {
            double e = this.getVelocity().horizontalLength();
            this.checkGlidingCollision(d, e);
        }
    }

    public void stopGliding() {
        this.setFlag(7, true);
        this.setFlag(7, false);
    }

    private Vec3d calcGlidingVelocity(Vec3d oldVelocity) {
        double i;
        Vec3d vec3d = this.getRotationVector();
        float f = this.getPitch() * ((float)Math.PI / 180);
        double d = Math.sqrt(vec3d.x * vec3d.x + vec3d.z * vec3d.z);
        double e = oldVelocity.horizontalLength();
        double g = this.getEffectiveGravity();
        double h = MathHelper.square(Math.cos(f));
        oldVelocity = oldVelocity.add(0.0, g * (-1.0 + h * 0.75), 0.0);
        if (oldVelocity.y < 0.0 && d > 0.0) {
            i = oldVelocity.y * -0.1 * h;
            oldVelocity = oldVelocity.add(vec3d.x * i / d, i, vec3d.z * i / d);
        }
        if (f < 0.0f && d > 0.0) {
            i = e * (double)(-MathHelper.sin(f)) * 0.04;
            oldVelocity = oldVelocity.add(-vec3d.x * i / d, i * 3.2, -vec3d.z * i / d);
        }
        if (d > 0.0) {
            oldVelocity = oldVelocity.add((vec3d.x / d * e - oldVelocity.x) * 0.1, 0.0, (vec3d.z / d * e - oldVelocity.z) * 0.1);
        }
        return oldVelocity.multiply(0.99f, 0.98f, 0.99f);
    }

    private void checkGlidingCollision(double oldSpeed, double newSpeed) {
        double d;
        float f;
        if (this.horizontalCollision && (f = (float)((d = oldSpeed - newSpeed) * 10.0 - 3.0)) > 0.0f) {
            this.playSound(this.getFallSound((int)f), 1.0f, 1.0f);
            this.serverDamage(this.getDamageSources().flyIntoWall(), f);
        }
    }

    private void travelControlled(PlayerEntity controllingPlayer, Vec3d movementInput) {
        Vec3d vec3d = this.getControlledMovementInput(controllingPlayer, movementInput);
        this.tickControlled(controllingPlayer, vec3d);
        if (this.canMoveVoluntarily()) {
            this.setMovementSpeed(this.getSaddledSpeed(controllingPlayer));
            this.travel(vec3d);
        } else {
            this.setVelocity(Vec3d.ZERO);
        }
    }

    protected void tickControlled(PlayerEntity controllingPlayer, Vec3d movementInput) {
    }

    protected Vec3d getControlledMovementInput(PlayerEntity controllingPlayer, Vec3d movementInput) {
        return movementInput;
    }

    protected float getSaddledSpeed(PlayerEntity controllingPlayer) {
        return this.getMovementSpeed();
    }

    public void updateLimbs(boolean flutter) {
        float f = (float)MathHelper.magnitude(this.getX() - this.lastX, flutter ? this.getY() - this.lastY : 0.0, this.getZ() - this.lastZ);
        if (this.hasVehicle() || !this.isAlive()) {
            this.limbAnimator.reset();
        } else {
            this.updateLimbs(f);
        }
    }

    protected void updateLimbs(float posDelta) {
        float f = Math.min(posDelta * 4.0f, 1.0f);
        this.limbAnimator.updateLimbs(f, 0.4f, this.isBaby() ? 3.0f : 1.0f);
    }

    private Vec3d applyMovementInput(Vec3d movementInput, float slipperiness) {
        this.updateVelocity(this.getMovementSpeed(slipperiness), movementInput);
        this.setVelocity(this.applyClimbingSpeed(this.getVelocity()));
        this.move(MovementType.SELF, this.getVelocity());
        Vec3d vec3d = this.getVelocity();
        if ((this.horizontalCollision || this.jumping) && (this.isClimbing() || this.wasInPowderSnow && PowderSnowBlock.canWalkOnPowderSnow(this))) {
            vec3d = new Vec3d(vec3d.x, 0.2, vec3d.z);
        }
        return vec3d;
    }

    public Vec3d applyFluidMovingSpeed(double gravity, boolean falling, Vec3d motion) {
        if (gravity != 0.0 && !this.isSprinting()) {
            double d = falling && Math.abs(motion.y - 0.005) >= 0.003 && Math.abs(motion.y - gravity / 16.0) < 0.003 ? -0.003 : motion.y - gravity / 16.0;
            return new Vec3d(motion.x, d, motion.z);
        }
        return motion;
    }

    private Vec3d applyClimbingSpeed(Vec3d motion) {
        if (this.isClimbing()) {
            this.onLanding();
            float f = 0.15f;
            double d = MathHelper.clamp(motion.x, (double)-0.15f, (double)0.15f);
            double e = MathHelper.clamp(motion.z, (double)-0.15f, (double)0.15f);
            double g = Math.max(motion.y, (double)-0.15f);
            if (g < 0.0 && !this.getBlockStateAtPos().isOf(Blocks.SCAFFOLDING) && this.isHoldingOntoLadder() && this instanceof PlayerEntity) {
                g = 0.0;
            }
            motion = new Vec3d(d, g, e);
        }
        return motion;
    }

    private float getMovementSpeed(float slipperiness) {
        if (this.isOnGround()) {
            return this.getMovementSpeed() * (0.21600002f / (slipperiness * slipperiness * slipperiness));
        }
        return this.getOffGroundSpeed();
    }

    protected float getOffGroundSpeed() {
        return this.getControllingPassenger() instanceof PlayerEntity ? this.getMovementSpeed() * 0.1f : 0.02f;
    }

    public float getMovementSpeed() {
        return this.movementSpeed;
    }

    public void setMovementSpeed(float movementSpeed) {
        this.movementSpeed = movementSpeed;
    }

    public boolean tryAttack(ServerWorld world, Entity target) {
        this.onAttacking(target);
        return false;
    }

    public void knockbackTarget(Entity target, float strength, Vec3d playerTargetVelocity) {
        if (strength > 0.0f && target instanceof LivingEntity) {
            LivingEntity livingEntity = (LivingEntity)target;
            livingEntity.takeKnockback(strength, MathHelper.sin(this.getYaw() * ((float)Math.PI / 180)), -MathHelper.cos(this.getYaw() * ((float)Math.PI / 180)));
            this.setVelocity(this.getVelocity().multiply(0.6, 1.0, 0.6));
        }
    }

    protected void playAttackSound() {
    }

    @Override
    public void tick() {
        super.tick();
        this.tickActiveItemStack();
        this.updateLeaningPitch();
        if (!this.getEntityWorld().isClient()) {
            int j;
            int i = this.getStuckArrowCount();
            if (i > 0) {
                if (this.stuckArrowTimer <= 0) {
                    this.stuckArrowTimer = 20 * (30 - i);
                }
                --this.stuckArrowTimer;
                if (this.stuckArrowTimer <= 0) {
                    this.setStuckArrowCount(i - 1);
                }
            }
            if ((j = this.getStingerCount()) > 0) {
                if (this.stuckStingerTimer <= 0) {
                    this.stuckStingerTimer = 20 * (30 - j);
                }
                --this.stuckStingerTimer;
                if (this.stuckStingerTimer <= 0) {
                    this.setStingerCount(j - 1);
                }
            }
            this.sendEquipmentChanges();
            if (this.age % 20 == 0) {
                this.getDamageTracker().update();
            }
            if (!(!this.isSleeping() || this.isInteractable() && this.isSleepingInBed())) {
                this.wakeUp();
            }
        }
        if (!this.isRemoved()) {
            this.tickMovement();
        }
        double d = this.getX() - this.lastX;
        double e = this.getZ() - this.lastZ;
        float f = (float)(d * d + e * e);
        float g = this.bodyYaw;
        if (f > 0.0025000002f) {
            float h = (float)MathHelper.atan2(e, d) * 57.295776f - 90.0f;
            float k = MathHelper.abs(MathHelper.wrapDegrees(this.getYaw()) - h);
            g = 95.0f < k && k < 265.0f ? h - 180.0f : h;
        }
        if (this.handSwingProgress > 0.0f) {
            g = this.getYaw();
        }
        Profiler profiler = Profilers.get();
        profiler.push("headTurn");
        this.turnHead(g);
        profiler.pop();
        profiler.push("rangeChecks");
        while (this.getYaw() - this.lastYaw < -180.0f) {
            this.lastYaw -= 360.0f;
        }
        while (this.getYaw() - this.lastYaw >= 180.0f) {
            this.lastYaw += 360.0f;
        }
        while (this.bodyYaw - this.lastBodyYaw < -180.0f) {
            this.lastBodyYaw -= 360.0f;
        }
        while (this.bodyYaw - this.lastBodyYaw >= 180.0f) {
            this.lastBodyYaw += 360.0f;
        }
        while (this.getPitch() - this.lastPitch < -180.0f) {
            this.lastPitch -= 360.0f;
        }
        while (this.getPitch() - this.lastPitch >= 180.0f) {
            this.lastPitch += 360.0f;
        }
        while (this.headYaw - this.lastHeadYaw < -180.0f) {
            this.lastHeadYaw -= 360.0f;
        }
        while (this.headYaw - this.lastHeadYaw >= 180.0f) {
            this.lastHeadYaw += 360.0f;
        }
        profiler.pop();
        this.glidingTicks = this.isGliding() ? ++this.glidingTicks : 0;
        if (this.isSleeping()) {
            this.setPitch(0.0f);
        }
        this.updateAttributes();
        this.elytraFlightController.update();
    }

    public boolean isInPiercingCooldown(Entity target, int cooldownTicks) {
        if (this.piercingCooldowns == null) {
            return false;
        }
        if (this.piercingCooldowns.containsKey((Object)target)) {
            return this.getEntityWorld().getTime() - this.piercingCooldowns.getLong((Object)target) < (long)cooldownTicks;
        }
        return false;
    }

    public void startPiercingCooldown(Entity target) {
        if (this.piercingCooldowns != null) {
            this.piercingCooldowns.put((Object)target, this.getEntityWorld().getTime());
        }
    }

    public int getPiercedEntityCount(Predicate<Entity> predicate) {
        if (this.piercingCooldowns == null) {
            return 0;
        }
        return (int)this.piercingCooldowns.keySet().stream().filter(predicate).count();
    }

    public boolean pierce(EquipmentSlot slot, Entity target, float damage, boolean dealDamage, boolean knockback, boolean dismount) {
        World world = this.getEntityWorld();
        if (!(world instanceof ServerWorld)) {
            return false;
        }
        ServerWorld serverWorld = (ServerWorld)world;
        ItemStack itemStack = this.getEquippedStack(slot);
        DamageSource damageSource = itemStack.getDamageSource(this, () -> this.getDamageSources().mobAttack(this));
        float f = EnchantmentHelper.getDamage(serverWorld, itemStack, target, damageSource, damage);
        Vec3d vec3d = target.getVelocity();
        boolean bl = knockback;
        boolean bl2 = dealDamage && target.damage(serverWorld, damageSource, f);
        bl |= bl2;
        if (knockback) {
            this.knockbackTarget(target, 0.4f + this.getAttackKnockbackAgainst(target, damageSource), vec3d);
        }
        if (dismount && target.hasVehicle()) {
            bl = true;
            target.stopRiding();
        }
        if (target instanceof LivingEntity) {
            LivingEntity livingEntity = (LivingEntity)target;
            itemStack.postHit(livingEntity, this);
        }
        if (bl2) {
            EnchantmentHelper.onTargetDamaged(serverWorld, target, damageSource);
        }
        if (!bl) {
            return false;
        }
        this.onAttacking(target);
        this.playAttackSound();
        return true;
    }

    public void beforePlayerAttack() {
    }

    private void sendEquipmentChanges() {
        Map<EquipmentSlot, ItemStack> map = this.getEquipmentChanges();
        if (map != null) {
            this.checkHandStackSwap(map);
            if (!map.isEmpty()) {
                this.sendEquipmentChanges(map);
            }
        }
    }

    private @Nullable Map<EquipmentSlot, ItemStack> getEquipmentChanges() {
        ItemStack itemStack2;
        Map map = null;
        for (EquipmentSlot equipmentSlot : EquipmentSlot.VALUES) {
            ItemStack itemStack = this.lastEquipmentStacks.get(equipmentSlot);
            if (!this.areItemsDifferent(itemStack, itemStack2 = this.getEquippedStack(equipmentSlot))) continue;
            if (map == null) {
                map = Maps.newEnumMap(EquipmentSlot.class);
            }
            map.put(equipmentSlot, itemStack2);
            AttributeContainer attributeContainer = this.getAttributes();
            if (itemStack.isEmpty()) continue;
            this.onEquipmentRemoved(itemStack, equipmentSlot, attributeContainer);
        }
        if (map != null) {
            for (Map.Entry entry : map.entrySet()) {
                EquipmentSlot equipmentSlot2 = (EquipmentSlot)entry.getKey();
                itemStack2 = (ItemStack)entry.getValue();
                if (itemStack2.isEmpty() || itemStack2.shouldBreak()) continue;
                itemStack2.applyAttributeModifiers(equipmentSlot2, (attribute, modifier) -> {
                    EntityAttributeInstance entityAttributeInstance = this.attributes.getCustomInstance((RegistryEntry<EntityAttribute>)attribute);
                    if (entityAttributeInstance != null) {
                        entityAttributeInstance.removeModifier(modifier.id());
                        entityAttributeInstance.addTemporaryModifier((EntityAttributeModifier)modifier);
                    }
                });
                World world = this.getEntityWorld();
                if (!(world instanceof ServerWorld)) continue;
                ServerWorld serverWorld = (ServerWorld)world;
                EnchantmentHelper.applyLocationBasedEffects(serverWorld, itemStack2, this, equipmentSlot2);
            }
        }
        return map;
    }

    public boolean areItemsDifferent(ItemStack stack, ItemStack stack2) {
        return !ItemStack.areEqual(stack2, stack);
    }

    private void checkHandStackSwap(Map<EquipmentSlot, ItemStack> equipmentChanges) {
        ItemStack itemStack = equipmentChanges.get(EquipmentSlot.MAINHAND);
        ItemStack itemStack2 = equipmentChanges.get(EquipmentSlot.OFFHAND);
        if (itemStack != null && itemStack2 != null && ItemStack.areEqual(itemStack, this.lastEquipmentStacks.get(EquipmentSlot.OFFHAND)) && ItemStack.areEqual(itemStack2, this.lastEquipmentStacks.get(EquipmentSlot.MAINHAND))) {
            ((ServerWorld)this.getEntityWorld()).getChunkManager().sendToOtherNearbyPlayers(this, new EntityStatusS2CPacket(this, 55));
            equipmentChanges.remove(EquipmentSlot.MAINHAND);
            equipmentChanges.remove(EquipmentSlot.OFFHAND);
            this.lastEquipmentStacks.put(EquipmentSlot.MAINHAND, itemStack.copy());
            this.lastEquipmentStacks.put(EquipmentSlot.OFFHAND, itemStack2.copy());
        }
    }

    private void sendEquipmentChanges(Map<EquipmentSlot, ItemStack> equipmentChanges) {
        ArrayList list = Lists.newArrayListWithCapacity((int)equipmentChanges.size());
        equipmentChanges.forEach((slot, stack) -> {
            ItemStack itemStack = stack.copy();
            list.add(Pair.of((Object)slot, (Object)itemStack));
            this.lastEquipmentStacks.put((EquipmentSlot)slot, itemStack);
        });
        ((ServerWorld)this.getEntityWorld()).getChunkManager().sendToOtherNearbyPlayers(this, new EntityEquipmentUpdateS2CPacket(this.getId(), list));
    }

    protected void turnHead(float bodyRotation) {
        float f = MathHelper.wrapDegrees(bodyRotation - this.bodyYaw);
        this.bodyYaw += f * 0.3f;
        float g = MathHelper.wrapDegrees(this.getYaw() - this.bodyYaw);
        float h = this.getMaxRelativeHeadRotation();
        if (Math.abs(g) > h) {
            this.bodyYaw += g - (float)MathHelper.sign(g) * h;
        }
    }

    protected float getMaxRelativeHeadRotation() {
        return 50.0f;
    }

    /*
     * Unable to fully structure code
     */
    public void tickMovement() {
        if (this.jumpingCooldown > 0) {
            --this.jumpingCooldown;
        }
        if (this.isInterpolating()) {
            this.getInterpolator().tick();
        } else if (!this.canMoveVoluntarily()) {
            this.setVelocity(this.getVelocity().multiply(0.98));
        }
        if (this.headTrackingIncrements > 0) {
            this.lerpHeadYaw(this.headTrackingIncrements, this.serverHeadYaw);
            --this.headTrackingIncrements;
        }
        this.equipment.tick(this);
        vec3d = this.getVelocity();
        d = vec3d.x;
        e = vec3d.y;
        f = vec3d.z;
        if (this.getType().equals(EntityType.PLAYER)) {
            if (vec3d.horizontalLengthSquared() < 9.0E-6) {
                d = 0.0;
                f = 0.0;
            }
        } else {
            if (Math.abs(vec3d.x) < 0.003) {
                d = 0.0;
            }
            if (Math.abs(vec3d.z) < 0.003) {
                f = 0.0;
            }
        }
        if (Math.abs(vec3d.y) < 0.003) {
            e = 0.0;
        }
        this.setVelocity(d, e, f);
        profiler = Profilers.get();
        profiler.push("ai");
        this.tickMovementInput();
        if (this.isImmobile()) {
            this.jumping = false;
            this.sidewaysSpeed = 0.0f;
            this.forwardSpeed = 0.0f;
        } else if (this.canActVoluntarily() && !this.getEntityWorld().isClient()) {
            profiler.push("newAi");
            this.tickNewAi();
            profiler.pop();
        }
        profiler.pop();
        profiler.push("jump");
        if (this.jumping && this.shouldSwimInFluids()) {
            g = this.isInLava() != false ? this.getFluidHeight(FluidTags.LAVA) : this.getFluidHeight(FluidTags.WATER);
            bl = this.isTouchingWater() != false && g > 0.0;
            h = this.getSwimHeight();
            if (bl && (!this.isOnGround() || g > h)) {
                this.swimUpward(FluidTags.WATER);
            } else if (this.isInLava() && (!this.isOnGround() || g > h)) {
                this.swimUpward(FluidTags.LAVA);
            } else if ((this.isOnGround() || bl && g <= h) && this.jumpingCooldown == 0) {
                this.jump();
                this.jumpingCooldown = 10;
            }
        } else {
            this.jumpingCooldown = 0;
        }
        profiler.pop();
        profiler.push("travel");
        if (this.isGliding()) {
            this.tickGliding();
        }
        box = this.getBoundingBox();
        vec3d2 = new Vec3d(this.sidewaysSpeed, this.upwardSpeed, this.forwardSpeed);
        if (this.hasStatusEffect(StatusEffects.SLOW_FALLING) || this.hasStatusEffect(StatusEffects.LEVITATION)) {
            this.onLanding();
        }
        if (!((var12_13 = this.getControllingPassenger()) instanceof PlayerEntity)) ** GOTO lbl-1000
        playerEntity = (PlayerEntity)var12_13;
        if (this.isAlive()) {
            this.travelControlled(playerEntity, vec3d2);
        } else if (this.canMoveVoluntarily() && this.canActVoluntarily()) {
            this.travel(vec3d2);
        }
        if (!this.getEntityWorld().isClient() || this.isLogicalSideForUpdatingMovement()) {
            this.tickBlockCollision();
        }
        if (this.getEntityWorld().isClient()) {
            this.updateLimbs(this instanceof Flutterer);
        }
        profiler.pop();
        var12_13 = this.getEntityWorld();
        if (var12_13 instanceof ServerWorld) {
            serverWorld = (ServerWorld)var12_13;
            profiler.push("freezing");
            if (!this.inPowderSnow || !this.canFreeze()) {
                this.setFrozenTicks(Math.max(0, this.getFrozenTicks() - 2));
            }
            this.removePowderSnowSlow();
            this.addPowderSnowSlowIfNeeded();
            if (this.age % 40 == 0 && this.isFrozen() && this.canFreeze()) {
                this.damage(serverWorld, this.getDamageSources().freeze(), 1.0f);
            }
            profiler.pop();
        }
        profiler.push("push");
        if (this.riptideTicks > 0) {
            --this.riptideTicks;
            this.tickRiptide(box, this.getBoundingBox());
        }
        this.tickCramming();
        profiler.pop();
        var12_13 = this.getEntityWorld();
        if (var12_13 instanceof ServerWorld) {
            serverWorld = (ServerWorld)var12_13;
            if (this.hurtByWater() && this.isTouchingWaterOrRain()) {
                this.damage(serverWorld, this.getDamageSources().drown(), 1.0f);
            }
        }
    }

    protected void tickMovementInput() {
        this.sidewaysSpeed *= 0.98f;
        this.forwardSpeed *= 0.98f;
    }

    public boolean hurtByWater() {
        return false;
    }

    public boolean isJumping() {
        return this.jumping;
    }

    protected void tickGliding() {
        this.limitFallDistance();
        if (!this.getEntityWorld().isClient()) {
            if (!this.canGlide()) {
                this.setFlag(7, false);
                return;
            }
            int i = this.glidingTicks + 1;
            if (i % 10 == 0) {
                int j = i / 10;
                if (j % 2 == 0) {
                    List<EquipmentSlot> list = EquipmentSlot.VALUES.stream().filter(slot -> LivingEntity.canGlideWith(this.getEquippedStack((EquipmentSlot)slot), slot)).toList();
                    EquipmentSlot equipmentSlot = Util.getRandom(list, this.random);
                    this.getEquippedStack(equipmentSlot).damage(1, this, equipmentSlot);
                }
                this.emitGameEvent(GameEvent.ELYTRA_GLIDE);
            }
        }
    }

    protected boolean canGlide() {
        if (this.isOnGround() || this.hasVehicle() || this.hasStatusEffect(StatusEffects.LEVITATION)) {
            return false;
        }
        for (EquipmentSlot equipmentSlot : EquipmentSlot.VALUES) {
            if (!LivingEntity.canGlideWith(this.getEquippedStack(equipmentSlot), equipmentSlot)) continue;
            return true;
        }
        return false;
    }

    protected void tickNewAi() {
    }

    protected void tickCramming() {
        ServerWorld serverWorld;
        int i;
        List<Entity> list = this.getEntityWorld().getCrammedEntities(this, this.getBoundingBox());
        if (list.isEmpty()) {
            return;
        }
        World world = this.getEntityWorld();
        if (world instanceof ServerWorld && (i = (serverWorld = (ServerWorld)world).getGameRules().getValue(GameRules.MAX_ENTITY_CRAMMING).intValue()) > 0 && list.size() > i - 1 && this.random.nextInt(4) == 0) {
            int j = 0;
            for (Entity entity : list) {
                if (entity.hasVehicle()) continue;
                ++j;
            }
            if (j > i - 1) {
                this.damage(serverWorld, this.getDamageSources().cramming(), 6.0f);
            }
        }
        for (Entity entity2 : list) {
            this.pushAway(entity2);
        }
    }

    protected void tickRiptide(Box a, Box b) {
        Box box = a.union(b);
        List<Entity> list = this.getEntityWorld().getOtherEntities(this, box);
        if (!list.isEmpty()) {
            for (Entity entity : list) {
                if (!(entity instanceof LivingEntity)) continue;
                this.attackLivingEntity((LivingEntity)entity);
                this.riptideTicks = 0;
                this.setVelocity(this.getVelocity().multiply(-0.2));
                break;
            }
        } else if (this.horizontalCollision) {
            this.riptideTicks = 0;
        }
        if (!this.getEntityWorld().isClient() && this.riptideTicks <= 0) {
            this.setLivingFlag(4, false);
            this.riptideAttackDamage = 0.0f;
            this.riptideStack = null;
        }
    }

    protected void pushAway(Entity entity) {
        entity.pushAwayFrom(this);
    }

    protected void attackLivingEntity(LivingEntity target) {
    }

    public boolean isUsingRiptide() {
        return (this.dataTracker.get(LIVING_FLAGS) & 4) != 0;
    }

    @Override
    public void stopRiding() {
        Entity entity = this.getVehicle();
        super.stopRiding();
        if (entity != null && entity != this.getVehicle() && !this.getEntityWorld().isClient()) {
            this.onDismounted(entity);
        }
    }

    @Override
    public void tickRiding() {
        super.tickRiding();
        this.onLanding();
    }

    @Override
    public PositionInterpolator getInterpolator() {
        return this.interpolator;
    }

    @Override
    public void updateTrackedHeadRotation(float yaw, int interpolationSteps) {
        this.serverHeadYaw = yaw;
        this.headTrackingIncrements = interpolationSteps;
    }

    public void setJumping(boolean jumping) {
        this.jumping = jumping;
    }

    public void triggerItemPickedUpByEntityCriteria(ItemEntity item) {
        Entity entity = item.getOwner();
        if (entity instanceof ServerPlayerEntity) {
            Criteria.THROWN_ITEM_PICKED_UP_BY_ENTITY.trigger((ServerPlayerEntity)entity, item.getStack(), this);
        }
    }

    public void sendPickup(Entity item, int count) {
        if (!item.isRemoved() && !this.getEntityWorld().isClient() && (item instanceof ItemEntity || item instanceof PersistentProjectileEntity || item instanceof ExperienceOrbEntity)) {
            ((ServerWorld)this.getEntityWorld()).getChunkManager().sendToOtherNearbyPlayers(item, new ItemPickupAnimationS2CPacket(item.getId(), this.getId(), count));
        }
    }

    public boolean canSee(Entity entity) {
        return this.canSee(entity, RaycastContext.ShapeType.COLLIDER, RaycastContext.FluidHandling.NONE, entity.getEyeY());
    }

    public boolean canSee(Entity entity, RaycastContext.ShapeType shapeType, RaycastContext.FluidHandling fluidHandling, double entityY) {
        if (entity.getEntityWorld() != this.getEntityWorld()) {
            return false;
        }
        Vec3d vec3d = new Vec3d(this.getX(), this.getEyeY(), this.getZ());
        Vec3d vec3d2 = new Vec3d(entity.getX(), entityY, entity.getZ());
        if (vec3d2.distanceTo(vec3d) > 128.0) {
            return false;
        }
        return this.getEntityWorld().raycast(new RaycastContext(vec3d, vec3d2, shapeType, fluidHandling, this)).getType() == HitResult.Type.MISS;
    }

    @Override
    public float getYaw(float tickProgress) {
        if (tickProgress == 1.0f) {
            return this.headYaw;
        }
        return MathHelper.lerpAngleDegrees(tickProgress, this.lastHeadYaw, this.headYaw);
    }

    public float getHandSwingProgress(float tickProgress) {
        float f = this.handSwingProgress - this.lastHandSwingProgress;
        if (f < 0.0f) {
            f += 1.0f;
        }
        return this.lastHandSwingProgress + f * tickProgress;
    }

    @Override
    public boolean canHit() {
        return !this.isRemoved();
    }

    @Override
    public boolean isPushable() {
        return this.isAlive() && !this.isSpectator() && !this.isClimbing();
    }

    @Override
    public float getHeadYaw() {
        return this.headYaw;
    }

    @Override
    public void setHeadYaw(float headYaw) {
        this.headYaw = headYaw;
    }

    @Override
    public void setBodyYaw(float bodyYaw) {
        this.bodyYaw = bodyYaw;
    }

    @Override
    public Vec3d positionInPortal(Direction.Axis portalAxis, BlockLocating.Rectangle portalRect) {
        return LivingEntity.positionInPortal(super.positionInPortal(portalAxis, portalRect));
    }

    public static Vec3d positionInPortal(Vec3d pos) {
        return new Vec3d(pos.x, pos.y, 0.0);
    }

    public float getAbsorptionAmount() {
        return this.absorptionAmount;
    }

    public final void setAbsorptionAmount(float absorptionAmount) {
        this.setAbsorptionAmountUnclamped(MathHelper.clamp(absorptionAmount, 0.0f, this.getMaxAbsorption()));
    }

    protected void setAbsorptionAmountUnclamped(float absorptionAmount) {
        this.absorptionAmount = absorptionAmount;
    }

    public void enterCombat() {
    }

    public void endCombat() {
    }

    protected void markEffectsDirty() {
        this.effectsChanged = true;
    }

    public abstract Arm getMainArm();

    public boolean isUsingItem() {
        return (this.dataTracker.get(LIVING_FLAGS) & 1) > 0;
    }

    public Hand getActiveHand() {
        return (this.dataTracker.get(LIVING_FLAGS) & 2) > 0 ? Hand.OFF_HAND : Hand.MAIN_HAND;
    }

    private void tickActiveItemStack() {
        if (this.isUsingItem()) {
            if (ItemStack.areItemsEqual(this.getStackInHand(this.getActiveHand()), this.activeItemStack)) {
                this.activeItemStack = this.getStackInHand(this.getActiveHand());
                this.tickItemStackUsage(this.activeItemStack);
            } else {
                this.clearActiveItem();
            }
        }
    }

    private @Nullable ItemEntity createItemEntity(ItemStack stack, boolean atSelf, boolean retainOwnership) {
        if (stack.isEmpty()) {
            return null;
        }
        double d = this.getEyeY() - (double)0.3f;
        ItemEntity itemEntity = new ItemEntity(this.getEntityWorld(), this.getX(), d, this.getZ(), stack);
        itemEntity.setPickupDelay(40);
        if (retainOwnership) {
            itemEntity.setThrower(this);
        }
        if (atSelf) {
            float f = this.random.nextFloat() * 0.5f;
            float g = this.random.nextFloat() * ((float)Math.PI * 2);
            itemEntity.setVelocity(-MathHelper.sin(g) * f, 0.2f, MathHelper.cos(g) * f);
        } else {
            float f = 0.3f;
            float g = MathHelper.sin(this.getPitch() * ((float)Math.PI / 180));
            float h = MathHelper.cos(this.getPitch() * ((float)Math.PI / 180));
            float i = MathHelper.sin(this.getYaw() * ((float)Math.PI / 180));
            float j = MathHelper.cos(this.getYaw() * ((float)Math.PI / 180));
            float k = this.random.nextFloat() * ((float)Math.PI * 2);
            float l = 0.02f * this.random.nextFloat();
            itemEntity.setVelocity((double)(-i * h * 0.3f) + Math.cos(k) * (double)l, -g * 0.3f + 0.1f + (this.random.nextFloat() - this.random.nextFloat()) * 0.1f, (double)(j * h * 0.3f) + Math.sin(k) * (double)l);
        }
        return itemEntity;
    }

    protected void tickItemStackUsage(ItemStack stack) {
        stack.usageTick(this.getEntityWorld(), this, this.getItemUseTimeLeft());
        if (--this.itemUseTimeLeft == 0 && !this.getEntityWorld().isClient() && !stack.isUsedOnRelease()) {
            this.consumeItem();
        }
    }

    private void updateLeaningPitch() {
        this.lastLeaningPitch = this.leaningPitch;
        this.leaningPitch = this.isInSwimmingPose() ? Math.min(1.0f, this.leaningPitch + 0.09f) : Math.max(0.0f, this.leaningPitch - 0.09f);
    }

    protected void setLivingFlag(int mask, boolean value) {
        int i = this.dataTracker.get(LIVING_FLAGS).byteValue();
        i = value ? (i |= mask) : (i &= ~mask);
        this.dataTracker.set(LIVING_FLAGS, (byte)i);
    }

    public void setCurrentHand(Hand hand) {
        ItemStack itemStack = this.getStackInHand(hand);
        if (itemStack.isEmpty() || this.isUsingItem()) {
            return;
        }
        this.activeItemStack = itemStack;
        this.itemUseTimeLeft = itemStack.getMaxUseTime(this);
        if (!this.getEntityWorld().isClient()) {
            this.setLivingFlag(1, true);
            this.setLivingFlag(2, hand == Hand.OFF_HAND);
            this.activeItemStack.emitUseGameEvent(this, GameEvent.ITEM_INTERACT_START);
            if (this.activeItemStack.contains(DataComponentTypes.KINETIC_WEAPON)) {
                this.piercingCooldowns = new Object2LongOpenHashMap();
            }
        }
    }

    @Override
    public void onTrackedDataSet(TrackedData<?> data) {
        super.onTrackedDataSet(data);
        if (SLEEPING_POSITION.equals(data)) {
            if (this.getEntityWorld().isClient()) {
                this.getSleepingPosition().ifPresent(this::setPositionInBed);
            }
        } else if (LIVING_FLAGS.equals(data) && this.getEntityWorld().isClient()) {
            if (this.isUsingItem() && this.activeItemStack.isEmpty()) {
                this.activeItemStack = this.getStackInHand(this.getActiveHand());
                if (!this.activeItemStack.isEmpty()) {
                    this.itemUseTimeLeft = this.activeItemStack.getMaxUseTime(this);
                }
            } else if (!this.isUsingItem() && !this.activeItemStack.isEmpty()) {
                this.activeItemStack = ItemStack.EMPTY;
                this.itemUseTimeLeft = 0;
            }
        }
    }

    @Override
    public void lookAt(EntityAnchorArgumentType.EntityAnchor anchorPoint, Vec3d target) {
        super.lookAt(anchorPoint, target);
        this.lastHeadYaw = this.headYaw;
        this.lastBodyYaw = this.bodyYaw = this.headYaw;
    }

    @Override
    public float lerpYaw(float tickProgress) {
        return MathHelper.lerp(tickProgress, this.lastBodyYaw, this.bodyYaw);
    }

    public void spawnItemParticles(ItemStack stack, int count) {
        for (int i = 0; i < count; ++i) {
            Vec3d vec3d = new Vec3d(((double)this.random.nextFloat() - 0.5) * 0.1, (double)this.random.nextFloat() * 0.1 + 0.1, 0.0);
            vec3d = vec3d.rotateX(-this.getPitch() * ((float)Math.PI / 180));
            vec3d = vec3d.rotateY(-this.getYaw() * ((float)Math.PI / 180));
            double d = (double)(-this.random.nextFloat()) * 0.6 - 0.3;
            Vec3d vec3d2 = new Vec3d(((double)this.random.nextFloat() - 0.5) * 0.3, d, 0.6);
            vec3d2 = vec3d2.rotateX(-this.getPitch() * ((float)Math.PI / 180));
            vec3d2 = vec3d2.rotateY(-this.getYaw() * ((float)Math.PI / 180));
            vec3d2 = vec3d2.add(this.getX(), this.getEyeY(), this.getZ());
            this.getEntityWorld().addParticleClient(new ItemStackParticleEffect(ParticleTypes.ITEM, stack), vec3d2.x, vec3d2.y, vec3d2.z, vec3d.x, vec3d.y + 0.05, vec3d.z);
        }
    }

    protected void consumeItem() {
        if (this.getEntityWorld().isClient() && !this.isUsingItem()) {
            return;
        }
        Hand hand = this.getActiveHand();
        if (!this.activeItemStack.equals(this.getStackInHand(hand))) {
            this.stopUsingItem();
            return;
        }
        if (!this.activeItemStack.isEmpty() && this.isUsingItem()) {
            ItemStack itemStack = this.activeItemStack.finishUsing(this.getEntityWorld(), this);
            if (itemStack != this.activeItemStack) {
                this.setStackInHand(hand, itemStack);
            }
            this.clearActiveItem();
        }
    }

    public void giveOrDropStack(ItemStack stack) {
    }

    public ItemStack getActiveItem() {
        return this.activeItemStack;
    }

    public int getItemUseTimeLeft() {
        return this.itemUseTimeLeft;
    }

    public int getItemUseTime() {
        if (this.isUsingItem()) {
            return this.activeItemStack.getMaxUseTime(this) - this.getItemUseTimeLeft();
        }
        return 0;
    }

    public float getItemUseTime(float baseTime) {
        if (!this.isUsingItem()) {
            return 0.0f;
        }
        return (float)this.getItemUseTime() + baseTime;
    }

    public void stopUsingItem() {
        ItemStack itemStack = this.getStackInHand(this.getActiveHand());
        if (!this.activeItemStack.isEmpty() && ItemStack.areItemsEqual(itemStack, this.activeItemStack)) {
            this.activeItemStack = itemStack;
            this.activeItemStack.onStoppedUsing(this.getEntityWorld(), this, this.getItemUseTimeLeft());
            if (this.activeItemStack.isUsedOnRelease()) {
                this.tickActiveItemStack();
            }
        }
        this.clearActiveItem();
    }

    public void clearActiveItem() {
        if (!this.getEntityWorld().isClient()) {
            boolean bl = this.isUsingItem();
            this.piercingCooldowns = null;
            this.setLivingFlag(1, false);
            if (bl) {
                this.activeItemStack.emitUseGameEvent(this, GameEvent.ITEM_INTERACT_FINISH);
            }
        }
        this.activeItemStack = ItemStack.EMPTY;
        this.itemUseTimeLeft = 0;
    }

    public boolean isBlocking() {
        return this.getBlockingItem() != null;
    }

    public @Nullable ItemStack getBlockingItem() {
        int i;
        if (!this.isUsingItem()) {
            return null;
        }
        BlocksAttacksComponent blocksAttacksComponent = this.activeItemStack.get(DataComponentTypes.BLOCKS_ATTACKS);
        if (blocksAttacksComponent != null && (i = this.activeItemStack.getItem().getMaxUseTime(this.activeItemStack, this) - this.itemUseTimeLeft) >= blocksAttacksComponent.getBlockDelayTicks()) {
            return this.activeItemStack;
        }
        return null;
    }

    public boolean isHoldingOntoLadder() {
        return this.isSneaking();
    }

    public boolean isGliding() {
        return this.getFlag(7);
    }

    @Override
    public boolean isInSwimmingPose() {
        return super.isInSwimmingPose() || !this.isGliding() && this.isInPose(EntityPose.GLIDING);
    }

    public int getGlidingTicks() {
        return this.glidingTicks;
    }

    public boolean teleport(double x, double y, double z, boolean particleEffects) {
        LivingEntity livingEntity;
        double d = this.getX();
        double e = this.getY();
        double f = this.getZ();
        double g = y;
        boolean bl = false;
        BlockPos blockPos = BlockPos.ofFloored(x, g, z);
        World world = this.getEntityWorld();
        if (world.isChunkLoaded(blockPos)) {
            boolean bl2 = false;
            while (!bl2 && blockPos.getY() > world.getBottomY()) {
                BlockPos blockPos2 = blockPos.down();
                BlockState blockState = world.getBlockState(blockPos2);
                if (blockState.blocksMovement()) {
                    bl2 = true;
                    continue;
                }
                g -= 1.0;
                blockPos = blockPos2;
            }
            if (bl2) {
                this.requestTeleport(x, g, z);
                if (world.isSpaceEmpty(this) && !world.containsFluid(this.getBoundingBox())) {
                    bl = true;
                }
            }
        }
        if (!bl) {
            this.requestTeleport(d, e, f);
            return false;
        }
        if (particleEffects) {
            world.sendEntityStatus(this, (byte)46);
        }
        if ((livingEntity = this) instanceof PathAwareEntity) {
            PathAwareEntity pathAwareEntity = (PathAwareEntity)livingEntity;
            pathAwareEntity.getNavigation().stop();
        }
        return true;
    }

    public boolean isAffectedBySplashPotions() {
        return !this.isDead();
    }

    public boolean isMobOrPlayer() {
        return true;
    }

    public void setNearbySongPlaying(BlockPos songPosition, boolean playing) {
    }

    public boolean canPickUpLoot() {
        return false;
    }

    @Override
    public final EntityDimensions getDimensions(EntityPose pose) {
        return pose == EntityPose.SLEEPING ? SLEEPING_DIMENSIONS : this.getBaseDimensions(pose).scaled(this.getScale());
    }

    protected EntityDimensions getBaseDimensions(EntityPose pose) {
        return this.getType().getDimensions().scaled(this.getScaleFactor());
    }

    public ImmutableList<EntityPose> getPoses() {
        return ImmutableList.of((Object)EntityPose.STANDING);
    }

    public Box getBoundingBox(EntityPose pose) {
        EntityDimensions entityDimensions = this.getDimensions(pose);
        return new Box(-entityDimensions.width() / 2.0f, 0.0, -entityDimensions.width() / 2.0f, entityDimensions.width() / 2.0f, entityDimensions.height(), entityDimensions.width() / 2.0f);
    }

    protected boolean wouldNotSuffocateInPose(EntityPose pose) {
        Box box = this.getDimensions(pose).getBoxAt(this.getEntityPos());
        return this.getEntityWorld().isBlockSpaceEmpty(this, box);
    }

    @Override
    public boolean canUsePortals(boolean allowVehicles) {
        return super.canUsePortals(allowVehicles) && !this.isSleeping();
    }

    public Optional<BlockPos> getSleepingPosition() {
        return this.dataTracker.get(SLEEPING_POSITION);
    }

    public void setSleepingPosition(BlockPos pos) {
        this.dataTracker.set(SLEEPING_POSITION, Optional.of(pos));
    }

    public void clearSleepingPosition() {
        this.dataTracker.set(SLEEPING_POSITION, Optional.empty());
    }

    public boolean isSleeping() {
        return this.getSleepingPosition().isPresent();
    }

    public void sleep(BlockPos pos) {
        BlockState blockState;
        if (this.hasVehicle()) {
            this.stopRiding();
        }
        if ((blockState = this.getEntityWorld().getBlockState(pos)).getBlock() instanceof BedBlock) {
            this.getEntityWorld().setBlockState(pos, (BlockState)blockState.with(BedBlock.OCCUPIED, true), 3);
        }
        this.setPose(EntityPose.SLEEPING);
        this.setPositionInBed(pos);
        this.setSleepingPosition(pos);
        this.setVelocity(Vec3d.ZERO);
        this.velocityDirty = true;
    }

    private void setPositionInBed(BlockPos pos) {
        this.setPosition((double)pos.getX() + 0.5, (double)pos.getY() + 0.6875, (double)pos.getZ() + 0.5);
    }

    private boolean isSleepingInBed() {
        return this.getSleepingPosition().map(pos -> this.getEntityWorld().getBlockState((BlockPos)pos).getBlock() instanceof BedBlock).orElse(false);
    }

    public void wakeUp() {
        this.getSleepingPosition().filter(this.getEntityWorld()::isChunkLoaded).ifPresent(pos -> {
            BlockState blockState = this.getEntityWorld().getBlockState((BlockPos)pos);
            if (blockState.getBlock() instanceof BedBlock) {
                Direction direction = (Direction)blockState.get(BedBlock.FACING);
                this.getEntityWorld().setBlockState((BlockPos)pos, (BlockState)blockState.with(BedBlock.OCCUPIED, false), 3);
                Vec3d vec3d = BedBlock.findWakeUpPosition(this.getType(), (CollisionView)this.getEntityWorld(), pos, direction, this.getYaw()).orElseGet(() -> {
                    BlockPos blockPos2 = pos.up();
                    return new Vec3d((double)blockPos2.getX() + 0.5, (double)blockPos2.getY() + 0.1, (double)blockPos2.getZ() + 0.5);
                });
                Vec3d vec3d2 = Vec3d.ofBottomCenter(pos).subtract(vec3d).normalize();
                float f = (float)MathHelper.wrapDegrees(MathHelper.atan2(vec3d2.z, vec3d2.x) * 57.2957763671875 - 90.0);
                this.setPosition(vec3d.x, vec3d.y, vec3d.z);
                this.setYaw(f);
                this.setPitch(0.0f);
            }
        });
        Vec3d vec3d = this.getEntityPos();
        this.setPose(EntityPose.STANDING);
        this.setPosition(vec3d.x, vec3d.y, vec3d.z);
        this.clearSleepingPosition();
    }

    public @Nullable Direction getSleepingDirection() {
        BlockPos blockPos = this.getSleepingPosition().orElse(null);
        return blockPos != null ? BedBlock.getDirection(this.getEntityWorld(), blockPos) : null;
    }

    @Override
    public boolean isInsideWall() {
        return !this.isSleeping() && super.isInsideWall();
    }

    public ItemStack getProjectileType(ItemStack stack) {
        return ItemStack.EMPTY;
    }

    private static byte getEquipmentBreakStatus(EquipmentSlot slot) {
        return switch (slot) {
            default -> throw new MatchException(null, null);
            case EquipmentSlot.MAINHAND -> 47;
            case EquipmentSlot.OFFHAND -> 48;
            case EquipmentSlot.HEAD -> 49;
            case EquipmentSlot.CHEST -> 50;
            case EquipmentSlot.FEET -> 52;
            case EquipmentSlot.LEGS -> 51;
            case EquipmentSlot.BODY -> 65;
            case EquipmentSlot.SADDLE -> 68;
        };
    }

    public void sendEquipmentBreakStatus(Item item, EquipmentSlot slot) {
        this.getEntityWorld().sendEntityStatus(this, LivingEntity.getEquipmentBreakStatus(slot));
        this.onEquipmentRemoved(this.getEquippedStack(slot), slot, this.attributes);
    }

    private void onEquipmentRemoved(ItemStack removedEquipment, EquipmentSlot slot, AttributeContainer container) {
        removedEquipment.applyAttributeModifiers(slot, (attribute, modifier) -> {
            EntityAttributeInstance entityAttributeInstance = container.getCustomInstance((RegistryEntry<EntityAttribute>)attribute);
            if (entityAttributeInstance != null) {
                entityAttributeInstance.removeModifier((EntityAttributeModifier)modifier);
            }
        });
        EnchantmentHelper.removeLocationBasedEffects(removedEquipment, this, slot);
    }

    public final boolean canEquipFromDispenser(ItemStack stack) {
        if (!this.isAlive() || this.isSpectator()) {
            return false;
        }
        EquippableComponent equippableComponent = stack.get(DataComponentTypes.EQUIPPABLE);
        if (equippableComponent == null || !equippableComponent.dispensable()) {
            return false;
        }
        EquipmentSlot equipmentSlot = equippableComponent.slot();
        if (!this.canUseSlot(equipmentSlot) || !equippableComponent.allows(this.getType())) {
            return false;
        }
        return this.getEquippedStack(equipmentSlot).isEmpty() && this.canDispenserEquipSlot(equipmentSlot);
    }

    protected boolean canDispenserEquipSlot(EquipmentSlot slot) {
        return true;
    }

    public final EquipmentSlot getPreferredEquipmentSlot(ItemStack stack) {
        EquippableComponent equippableComponent = stack.get(DataComponentTypes.EQUIPPABLE);
        if (equippableComponent != null && this.canUseSlot(equippableComponent.slot())) {
            return equippableComponent.slot();
        }
        return EquipmentSlot.MAINHAND;
    }

    public final boolean canEquip(ItemStack stack, EquipmentSlot slot) {
        EquippableComponent equippableComponent = stack.get(DataComponentTypes.EQUIPPABLE);
        if (equippableComponent == null) {
            return slot == EquipmentSlot.MAINHAND && this.canUseSlot(EquipmentSlot.MAINHAND);
        }
        return slot == equippableComponent.slot() && this.canUseSlot(equippableComponent.slot()) && equippableComponent.allows(this.getType());
    }

    private static StackReference getStackReference(LivingEntity entity, EquipmentSlot slot) {
        if (slot == EquipmentSlot.HEAD || slot == EquipmentSlot.MAINHAND || slot == EquipmentSlot.OFFHAND) {
            return StackReference.of(entity, slot);
        }
        return StackReference.of(entity, slot, stack -> stack.isEmpty() || entity.getPreferredEquipmentSlot((ItemStack)stack) == slot);
    }

    private static @Nullable EquipmentSlot getEquipmentSlot(int slotId) {
        if (slotId == 100 + EquipmentSlot.HEAD.getEntitySlotId()) {
            return EquipmentSlot.HEAD;
        }
        if (slotId == 100 + EquipmentSlot.CHEST.getEntitySlotId()) {
            return EquipmentSlot.CHEST;
        }
        if (slotId == 100 + EquipmentSlot.LEGS.getEntitySlotId()) {
            return EquipmentSlot.LEGS;
        }
        if (slotId == 100 + EquipmentSlot.FEET.getEntitySlotId()) {
            return EquipmentSlot.FEET;
        }
        if (slotId == 98) {
            return EquipmentSlot.MAINHAND;
        }
        if (slotId == 99) {
            return EquipmentSlot.OFFHAND;
        }
        if (slotId == 105) {
            return EquipmentSlot.BODY;
        }
        if (slotId == 106) {
            return EquipmentSlot.SADDLE;
        }
        return null;
    }

    @Override
    public @Nullable StackReference getStackReference(int slot) {
        EquipmentSlot equipmentSlot = LivingEntity.getEquipmentSlot(slot);
        if (equipmentSlot != null) {
            return LivingEntity.getStackReference(this, equipmentSlot);
        }
        return super.getStackReference(slot);
    }

    @Override
    public boolean canFreeze() {
        if (this.isSpectator()) {
            return false;
        }
        for (EquipmentSlot equipmentSlot : AttributeModifierSlot.ARMOR) {
            if (!this.getEquippedStack(equipmentSlot).isIn(ItemTags.FREEZE_IMMUNE_WEARABLES)) continue;
            return false;
        }
        return super.canFreeze();
    }

    @Override
    public boolean isGlowing() {
        return !this.getEntityWorld().isClient() && this.hasStatusEffect(StatusEffects.GLOWING) || super.isGlowing();
    }

    @Override
    public float getBodyYaw() {
        return this.bodyYaw;
    }

    @Override
    public void onSpawnPacket(EntitySpawnS2CPacket packet) {
        double d = packet.getX();
        double e = packet.getY();
        double f = packet.getZ();
        float g = packet.getYaw();
        float h = packet.getPitch();
        this.updateTrackedPosition(d, e, f);
        this.bodyYaw = packet.getHeadYaw();
        this.headYaw = packet.getHeadYaw();
        this.lastBodyYaw = this.bodyYaw;
        this.lastHeadYaw = this.headYaw;
        this.setId(packet.getEntityId());
        this.setUuid(packet.getUuid());
        this.updatePositionAndAngles(d, e, f, g, h);
        this.setVelocity(packet.getVelocity());
    }

    public float getWeaponDisableBlockingForSeconds() {
        ItemStack itemStack = this.getWeaponStack();
        WeaponComponent weaponComponent = itemStack.get(DataComponentTypes.WEAPON);
        return weaponComponent != null && itemStack == this.getActiveOrMainHandStack() ? weaponComponent.disableBlockingForSeconds() : 0.0f;
    }

    @Override
    public float getStepHeight() {
        float f = (float)this.getAttributeValue(EntityAttributes.STEP_HEIGHT);
        return this.getControllingPassenger() instanceof PlayerEntity ? Math.max(f, 1.0f) : f;
    }

    @Override
    public Vec3d getPassengerRidingPos(Entity passenger) {
        return this.getEntityPos().add(this.getPassengerAttachmentPos(passenger, this.getDimensions(this.getPose()), this.getScale() * this.getScaleFactor()));
    }

    protected void lerpHeadYaw(int headTrackingIncrements, double serverHeadYaw) {
        this.headYaw = (float)MathHelper.lerpAngleDegrees(1.0 / (double)headTrackingIncrements, (double)this.headYaw, serverHeadYaw);
    }

    @Override
    public void setOnFireForTicks(int ticks) {
        super.setOnFireForTicks(MathHelper.ceil((double)ticks * this.getAttributeValue(EntityAttributes.BURNING_TIME)));
    }

    public boolean isInCreativeMode() {
        return false;
    }

    public boolean isInvulnerableTo(ServerWorld world, DamageSource source) {
        return this.isAlwaysInvulnerableTo(source) || EnchantmentHelper.isInvulnerableTo(world, this, source);
    }

    public static boolean canGlideWith(ItemStack stack, EquipmentSlot slot) {
        if (!stack.contains(DataComponentTypes.GLIDER)) {
            return false;
        }
        EquippableComponent equippableComponent = stack.get(DataComponentTypes.EQUIPPABLE);
        return equippableComponent != null && slot == equippableComponent.slot() && !stack.willBreakNextUse();
    }

    @VisibleForTesting
    public int getPlayerHitTimer() {
        return this.playerHitTimer;
    }

    @Override
    public boolean hasWaypoint() {
        return this.getAttributeValue(EntityAttributes.WAYPOINT_TRANSMIT_RANGE) > 0.0;
    }

    @Override
    public Optional<ServerWaypoint.WaypointTracker> createTracker(ServerPlayerEntity receiver) {
        if (this.firstUpdate || receiver == this) {
            return Optional.empty();
        }
        if (ServerWaypoint.cannotReceive(this, receiver)) {
            return Optional.empty();
        }
        Waypoint.Config config = this.waypointConfig.withTeamColorOf(this);
        if (ServerWaypoint.shouldUseAzimuth(this, receiver)) {
            return Optional.of(new ServerWaypoint.AzimuthWaypointTracker(this, config, receiver));
        }
        if (!ServerWaypoint.canReceive(this.getChunkPos(), receiver)) {
            return Optional.of(new ServerWaypoint.ChunkWaypointTracker(this, config, receiver));
        }
        return Optional.of(new ServerWaypoint.PositionalWaypointTracker(this, config, receiver));
    }

    @Override
    public Waypoint.Config getWaypointConfig() {
        return this.waypointConfig;
    }

    public record FallSounds(SoundEvent small, SoundEvent big) {
    }
}


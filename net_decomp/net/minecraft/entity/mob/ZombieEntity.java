/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  com.google.common.annotations.VisibleForTesting
 *  org.jspecify.annotations.Nullable
 */
package net.minecraft.entity.mob;

import com.google.common.annotations.VisibleForTesting;
import java.util.List;
import java.util.function.Predicate;
import net.minecraft.block.BlockState;
import net.minecraft.block.Blocks;
import net.minecraft.entity.Entity;
import net.minecraft.entity.EntityData;
import net.minecraft.entity.EntityDimensions;
import net.minecraft.entity.EntityPose;
import net.minecraft.entity.EntityType;
import net.minecraft.entity.EquipmentSlot;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.SpawnReason;
import net.minecraft.entity.SpawnRestriction;
import net.minecraft.entity.ai.goal.ActiveTargetGoal;
import net.minecraft.entity.ai.goal.BreakDoorGoal;
import net.minecraft.entity.ai.goal.ChargeKineticWeaponGoal;
import net.minecraft.entity.ai.goal.LookAroundGoal;
import net.minecraft.entity.ai.goal.LookAtEntityGoal;
import net.minecraft.entity.ai.goal.MoveThroughVillageGoal;
import net.minecraft.entity.ai.goal.RevengeGoal;
import net.minecraft.entity.ai.goal.StepAndDestroyBlockGoal;
import net.minecraft.entity.ai.goal.WanderAroundFarGoal;
import net.minecraft.entity.ai.goal.ZombieAttackGoal;
import net.minecraft.entity.attribute.DefaultAttributeContainer;
import net.minecraft.entity.attribute.EntityAttributeInstance;
import net.minecraft.entity.attribute.EntityAttributeModifier;
import net.minecraft.entity.attribute.EntityAttributes;
import net.minecraft.entity.conversion.EntityConversionContext;
import net.minecraft.entity.damage.DamageSource;
import net.minecraft.entity.data.DataTracker;
import net.minecraft.entity.data.TrackedData;
import net.minecraft.entity.data.TrackedDataHandlerRegistry;
import net.minecraft.entity.mob.HostileEntity;
import net.minecraft.entity.mob.MobEntity;
import net.minecraft.entity.mob.PathAwareEntity;
import net.minecraft.entity.mob.ZombieVillagerEntity;
import net.minecraft.entity.mob.ZombifiedPiglinEntity;
import net.minecraft.entity.passive.ChickenEntity;
import net.minecraft.entity.passive.IronGolemEntity;
import net.minecraft.entity.passive.MerchantEntity;
import net.minecraft.entity.passive.TurtleEntity;
import net.minecraft.entity.passive.VillagerEntity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.ItemStack;
import net.minecraft.item.Items;
import net.minecraft.predicate.entity.EntityPredicates;
import net.minecraft.registry.tag.FluidTags;
import net.minecraft.registry.tag.ItemTags;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.sound.SoundCategory;
import net.minecraft.sound.SoundEvent;
import net.minecraft.sound.SoundEvents;
import net.minecraft.storage.ReadView;
import net.minecraft.storage.WriteView;
import net.minecraft.util.Holidays;
import net.minecraft.util.Identifier;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.MathHelper;
import net.minecraft.util.math.random.Random;
import net.minecraft.world.Difficulty;
import net.minecraft.world.LocalDifficulty;
import net.minecraft.world.ServerWorldAccess;
import net.minecraft.world.World;
import net.minecraft.world.WorldAccess;
import org.jspecify.annotations.Nullable;

public class ZombieEntity
extends HostileEntity {
    private static final Identifier BABY_SPEED_MODIFIER_ID = Identifier.ofVanilla("baby");
    private static final EntityAttributeModifier BABY_SPEED_BONUS = new EntityAttributeModifier(BABY_SPEED_MODIFIER_ID, 0.5, EntityAttributeModifier.Operation.ADD_MULTIPLIED_BASE);
    private static final Identifier REINFORCEMENT_CALLER_CHARGE_MODIFIER_ID = Identifier.ofVanilla("reinforcement_caller_charge");
    private static final EntityAttributeModifier REINFORCEMENT_CALLEE_CHARGE_REINFORCEMENT_BONUS = new EntityAttributeModifier(Identifier.ofVanilla("reinforcement_callee_charge"), -0.05f, EntityAttributeModifier.Operation.ADD_VALUE);
    private static final Identifier LEADER_ZOMBIE_BONUS_MODIFIER_ID = Identifier.ofVanilla("leader_zombie_bonus");
    private static final Identifier ZOMBIE_RANDOM_SPAWN_BONUS_MODIFIER_ID = Identifier.ofVanilla("zombie_random_spawn_bonus");
    private static final TrackedData<Boolean> BABY = DataTracker.registerData(ZombieEntity.class, TrackedDataHandlerRegistry.BOOLEAN);
    private static final TrackedData<Integer> ZOMBIE_TYPE = DataTracker.registerData(ZombieEntity.class, TrackedDataHandlerRegistry.INTEGER);
    private static final TrackedData<Boolean> CONVERTING_IN_WATER = DataTracker.registerData(ZombieEntity.class, TrackedDataHandlerRegistry.BOOLEAN);
    public static final float field_30519 = 0.05f;
    public static final int field_30515 = 50;
    public static final int field_30516 = 40;
    public static final int field_30517 = 7;
    private static final int field_57696 = -1;
    private static final EntityDimensions BABY_BASE_DIMENSIONS = EntityType.ZOMBIE.getDimensions().scaled(0.5f).withEyeHeight(0.93f);
    private static final float field_30518 = 0.1f;
    private static final Predicate<Difficulty> DOOR_BREAK_DIFFICULTY_CHECKER = difficulty -> difficulty == Difficulty.HARD;
    private static final boolean DEFAULT_IS_BABY = false;
    private static final boolean DEFAULT_CAN_BREAK_DOORS = false;
    private static final int DEFAULT_IN_WATER_TIME = 0;
    private final BreakDoorGoal breakDoorsGoal = new BreakDoorGoal(this, DOOR_BREAK_DIFFICULTY_CHECKER);
    private boolean canBreakDoors = false;
    private int inWaterTime = 0;
    private int ticksUntilWaterConversion;

    public ZombieEntity(EntityType<? extends ZombieEntity> entityType, World world) {
        super((EntityType<? extends HostileEntity>)entityType, world);
    }

    public ZombieEntity(World world) {
        this((EntityType<? extends ZombieEntity>)EntityType.ZOMBIE, world);
    }

    @Override
    protected void initGoals() {
        this.goalSelector.add(4, new DestroyEggGoal((PathAwareEntity)this, 1.0, 3));
        this.goalSelector.add(8, new LookAtEntityGoal(this, PlayerEntity.class, 8.0f));
        this.goalSelector.add(8, new LookAroundGoal(this));
        this.initCustomGoals();
    }

    protected void initCustomGoals() {
        this.goalSelector.add(2, new ChargeKineticWeaponGoal<ZombieEntity>(this, 1.0, 1.0, 10.0f, 2.0f));
        this.goalSelector.add(3, new ZombieAttackGoal(this, 1.0, false));
        this.goalSelector.add(6, new MoveThroughVillageGoal(this, 1.0, true, 4, this::canBreakDoors));
        this.goalSelector.add(7, new WanderAroundFarGoal(this, 1.0));
        this.targetSelector.add(1, new RevengeGoal(this, new Class[0]).setGroupRevenge(ZombifiedPiglinEntity.class));
        this.targetSelector.add(2, new ActiveTargetGoal<PlayerEntity>((MobEntity)this, PlayerEntity.class, true));
        this.targetSelector.add(3, new ActiveTargetGoal<MerchantEntity>((MobEntity)this, MerchantEntity.class, false));
        this.targetSelector.add(3, new ActiveTargetGoal<IronGolemEntity>((MobEntity)this, IronGolemEntity.class, true));
        this.targetSelector.add(5, new ActiveTargetGoal<TurtleEntity>(this, TurtleEntity.class, 10, true, false, TurtleEntity.BABY_TURTLE_ON_LAND_FILTER));
    }

    public static DefaultAttributeContainer.Builder createZombieAttributes() {
        return HostileEntity.createHostileAttributes().add(EntityAttributes.FOLLOW_RANGE, 35.0).add(EntityAttributes.MOVEMENT_SPEED, 0.23f).add(EntityAttributes.ATTACK_DAMAGE, 3.0).add(EntityAttributes.ARMOR, 2.0).add(EntityAttributes.SPAWN_REINFORCEMENTS);
    }

    @Override
    protected void initDataTracker(DataTracker.Builder builder) {
        super.initDataTracker(builder);
        builder.add(BABY, false);
        builder.add(ZOMBIE_TYPE, 0);
        builder.add(CONVERTING_IN_WATER, false);
    }

    public boolean isConvertingInWater() {
        return this.getDataTracker().get(CONVERTING_IN_WATER);
    }

    public boolean canBreakDoors() {
        return this.canBreakDoors;
    }

    public void setCanBreakDoors(boolean canBreakDoors) {
        if (this.navigation.canControlOpeningDoors()) {
            if (this.canBreakDoors != canBreakDoors) {
                this.canBreakDoors = canBreakDoors;
                this.navigation.setCanOpenDoors(canBreakDoors);
                if (canBreakDoors) {
                    this.goalSelector.add(1, this.breakDoorsGoal);
                } else {
                    this.goalSelector.remove(this.breakDoorsGoal);
                }
            }
        } else if (this.canBreakDoors) {
            this.goalSelector.remove(this.breakDoorsGoal);
            this.canBreakDoors = false;
        }
    }

    @Override
    public boolean isBaby() {
        return this.getDataTracker().get(BABY);
    }

    @Override
    protected int getExperienceToDrop(ServerWorld world) {
        if (this.isBaby()) {
            this.experiencePoints = (int)((double)this.experiencePoints * 2.5);
        }
        return super.getExperienceToDrop(world);
    }

    @Override
    public void setBaby(boolean baby) {
        this.getDataTracker().set(BABY, baby);
        if (this.getEntityWorld() != null && !this.getEntityWorld().isClient()) {
            EntityAttributeInstance entityAttributeInstance = this.getAttributeInstance(EntityAttributes.MOVEMENT_SPEED);
            entityAttributeInstance.removeModifier(BABY_SPEED_MODIFIER_ID);
            if (baby) {
                entityAttributeInstance.addTemporaryModifier(BABY_SPEED_BONUS);
            }
        }
    }

    @Override
    public void onTrackedDataSet(TrackedData<?> data) {
        if (BABY.equals(data)) {
            this.calculateDimensions();
        }
        super.onTrackedDataSet(data);
    }

    protected boolean canConvertInWater() {
        return true;
    }

    @Override
    public void tick() {
        World world = this.getEntityWorld();
        if (world instanceof ServerWorld) {
            ServerWorld serverWorld = (ServerWorld)world;
            if (this.isAlive() && !this.isAiDisabled()) {
                if (this.isConvertingInWater()) {
                    --this.ticksUntilWaterConversion;
                    if (this.ticksUntilWaterConversion < 0) {
                        this.convertInWater(serverWorld);
                    }
                } else if (this.canConvertInWater()) {
                    if (this.isSubmergedIn(FluidTags.WATER)) {
                        ++this.inWaterTime;
                        if (this.inWaterTime >= 600) {
                            this.setTicksUntilWaterConversion(300);
                        }
                    } else {
                        this.inWaterTime = -1;
                    }
                }
            }
        }
        super.tick();
    }

    private void setTicksUntilWaterConversion(int ticksUntilWaterConversion) {
        this.ticksUntilWaterConversion = ticksUntilWaterConversion;
        this.getDataTracker().set(CONVERTING_IN_WATER, true);
    }

    protected void convertInWater(ServerWorld world) {
        this.convertTo(world, EntityType.DROWNED);
        if (!this.isSilent()) {
            world.syncWorldEvent(null, 1040, this.getBlockPos(), 0);
        }
    }

    protected void convertTo(ServerWorld world, EntityType<? extends ZombieEntity> entityType) {
        this.convertTo(entityType, EntityConversionContext.create(this, true, true), newZombie -> newZombie.applyAttributeModifiers(world.getLocalDifficulty(newZombie.getBlockPos()).getClampedLocalDifficulty()));
    }

    @VisibleForTesting
    public boolean infectVillager(ServerWorld world, VillagerEntity villager) {
        ZombieVillagerEntity zombieVillagerEntity = villager.convertTo(EntityType.ZOMBIE_VILLAGER, EntityConversionContext.create(villager, true, true), zombieVillager -> {
            zombieVillager.initialize(world, world.getLocalDifficulty(zombieVillager.getBlockPos()), SpawnReason.CONVERSION, new ZombieData(false, true));
            zombieVillager.setVillagerData(villager.getVillagerData());
            zombieVillager.setGossip(villager.getGossip().copy());
            zombieVillager.setOfferData(villager.getOffers().copy());
            zombieVillager.setExperience(villager.getExperience());
            if (!this.isSilent()) {
                world.syncWorldEvent(null, 1026, this.getBlockPos(), 0);
            }
        });
        return zombieVillagerEntity != null;
    }

    protected boolean burnsInDaylight() {
        return true;
    }

    @Override
    public boolean damage(ServerWorld world, DamageSource source, float amount) {
        if (!super.damage(world, source, amount)) {
            return false;
        }
        LivingEntity livingEntity = this.getTarget();
        if (livingEntity == null && source.getAttacker() instanceof LivingEntity) {
            livingEntity = (LivingEntity)source.getAttacker();
        }
        if (livingEntity != null && world.getDifficulty() == Difficulty.HARD && (double)this.random.nextFloat() < this.getAttributeValue(EntityAttributes.SPAWN_REINFORCEMENTS) && world.shouldSpawnMonsters()) {
            int i = MathHelper.floor(this.getX());
            int j = MathHelper.floor(this.getY());
            int k = MathHelper.floor(this.getZ());
            EntityType<? extends ZombieEntity> entityType = this.getType();
            ZombieEntity zombieEntity = entityType.create(world, SpawnReason.REINFORCEMENT);
            if (zombieEntity == null) {
                return true;
            }
            for (int l = 0; l < 50; ++l) {
                int o;
                int n;
                int m = i + MathHelper.nextInt(this.random, 7, 40) * MathHelper.nextInt(this.random, -1, 1);
                BlockPos blockPos = new BlockPos(m, n = j + MathHelper.nextInt(this.random, 7, 40) * MathHelper.nextInt(this.random, -1, 1), o = k + MathHelper.nextInt(this.random, 7, 40) * MathHelper.nextInt(this.random, -1, 1));
                if (!SpawnRestriction.isSpawnPosAllowed(entityType, world, blockPos) || !SpawnRestriction.canSpawn(entityType, world, SpawnReason.REINFORCEMENT, blockPos, world.random)) continue;
                zombieEntity.setPosition(m, n, o);
                if (world.isPlayerInRange(m, n, o, 7.0) || !world.doesNotIntersectEntities(zombieEntity) || !world.isSpaceEmpty(zombieEntity) || !zombieEntity.canSpawnAsReinforcementInFluid() && world.containsFluid(zombieEntity.getBoundingBox())) continue;
                zombieEntity.setTarget(livingEntity);
                zombieEntity.initialize(world, world.getLocalDifficulty(zombieEntity.getBlockPos()), SpawnReason.REINFORCEMENT, null);
                world.spawnEntityAndPassengers(zombieEntity);
                EntityAttributeInstance entityAttributeInstance = this.getAttributeInstance(EntityAttributes.SPAWN_REINFORCEMENTS);
                EntityAttributeModifier entityAttributeModifier = entityAttributeInstance.getModifier(REINFORCEMENT_CALLER_CHARGE_MODIFIER_ID);
                double d = entityAttributeModifier != null ? entityAttributeModifier.value() : 0.0;
                entityAttributeInstance.removeModifier(REINFORCEMENT_CALLER_CHARGE_MODIFIER_ID);
                entityAttributeInstance.addPersistentModifier(new EntityAttributeModifier(REINFORCEMENT_CALLER_CHARGE_MODIFIER_ID, d - 0.05, EntityAttributeModifier.Operation.ADD_VALUE));
                zombieEntity.getAttributeInstance(EntityAttributes.SPAWN_REINFORCEMENTS).addPersistentModifier(REINFORCEMENT_CALLEE_CHARGE_REINFORCEMENT_BONUS);
                break;
            }
        }
        return true;
    }

    @Override
    public boolean tryAttack(ServerWorld world, Entity target) {
        boolean bl = super.tryAttack(world, target);
        if (bl) {
            float f = world.getLocalDifficulty(this.getBlockPos()).getLocalDifficulty();
            if (this.getMainHandStack().isEmpty() && this.isOnFire() && this.random.nextFloat() < f * 0.3f) {
                target.setOnFireFor(2 * (int)f);
            }
        }
        return bl;
    }

    @Override
    protected SoundEvent getAmbientSound() {
        return SoundEvents.ENTITY_ZOMBIE_AMBIENT;
    }

    @Override
    protected SoundEvent getHurtSound(DamageSource source) {
        return SoundEvents.ENTITY_ZOMBIE_HURT;
    }

    @Override
    protected SoundEvent getDeathSound() {
        return SoundEvents.ENTITY_ZOMBIE_DEATH;
    }

    protected SoundEvent getStepSound() {
        return SoundEvents.ENTITY_ZOMBIE_STEP;
    }

    @Override
    protected void playStepSound(BlockPos pos, BlockState state) {
        this.playSound(this.getStepSound(), 0.15f, 1.0f);
    }

    public EntityType<? extends ZombieEntity> getType() {
        return super.getType();
    }

    protected boolean canSpawnAsReinforcementInFluid() {
        return false;
    }

    @Override
    protected void initEquipment(Random random, LocalDifficulty localDifficulty) {
        super.initEquipment(random, localDifficulty);
        float f = random.nextFloat();
        float f2 = this.getEntityWorld().getDifficulty() == Difficulty.HARD ? 0.05f : 0.01f;
        if (f < f2) {
            int i = random.nextInt(6);
            if (i == 0) {
                this.equipStack(EquipmentSlot.MAINHAND, new ItemStack(Items.IRON_SWORD));
            } else if (i == 1) {
                this.equipStack(EquipmentSlot.MAINHAND, new ItemStack(Items.IRON_SPEAR));
            } else {
                this.equipStack(EquipmentSlot.MAINHAND, new ItemStack(Items.IRON_SHOVEL));
            }
        }
    }

    @Override
    protected void writeCustomData(WriteView view) {
        super.writeCustomData(view);
        view.putBoolean("IsBaby", this.isBaby());
        view.putBoolean("CanBreakDoors", this.canBreakDoors());
        view.putInt("InWaterTime", this.isTouchingWater() ? this.inWaterTime : -1);
        view.putInt("DrownedConversionTime", this.isConvertingInWater() ? this.ticksUntilWaterConversion : -1);
    }

    @Override
    protected void readCustomData(ReadView view) {
        super.readCustomData(view);
        this.setBaby(view.getBoolean("IsBaby", false));
        this.setCanBreakDoors(view.getBoolean("CanBreakDoors", false));
        this.inWaterTime = view.getInt("InWaterTime", 0);
        int i = view.getInt("DrownedConversionTime", -1);
        if (i != -1) {
            this.setTicksUntilWaterConversion(i);
        } else {
            this.getDataTracker().set(CONVERTING_IN_WATER, false);
        }
    }

    @Override
    public boolean onKilledOther(ServerWorld world, LivingEntity other, DamageSource damageSource) {
        boolean bl = super.onKilledOther(world, other, damageSource);
        if ((world.getDifficulty() == Difficulty.NORMAL || world.getDifficulty() == Difficulty.HARD) && other instanceof VillagerEntity) {
            VillagerEntity villagerEntity = (VillagerEntity)other;
            if (world.getDifficulty() != Difficulty.HARD && this.random.nextBoolean()) {
                return bl;
            }
            if (this.infectVillager(world, villagerEntity)) {
                bl = false;
            }
        }
        return bl;
    }

    @Override
    public EntityDimensions getBaseDimensions(EntityPose pose) {
        return this.isBaby() ? BABY_BASE_DIMENSIONS : super.getBaseDimensions(pose);
    }

    @Override
    public boolean canPickupItem(ItemStack stack) {
        if (stack.isIn(ItemTags.EGGS) && this.isBaby() && this.hasVehicle()) {
            return false;
        }
        return super.canPickupItem(stack);
    }

    @Override
    public boolean canGather(ServerWorld world, ItemStack stack) {
        if (stack.isOf(Items.GLOW_INK_SAC)) {
            return false;
        }
        return super.canGather(world, stack);
    }

    @Override
    public @Nullable EntityData initialize(ServerWorldAccess world, LocalDifficulty difficulty, SpawnReason spawnReason, @Nullable EntityData entityData) {
        Random random = world.getRandom();
        entityData = super.initialize(world, difficulty, spawnReason, entityData);
        float f = difficulty.getClampedLocalDifficulty();
        if (spawnReason != SpawnReason.CONVERSION) {
            this.setCanPickUpLoot(random.nextFloat() < 0.55f * f);
        }
        if (entityData == null) {
            entityData = new ZombieData(ZombieEntity.shouldBeBaby(random), true);
        }
        if (entityData instanceof ZombieData) {
            ZombieData zombieData = (ZombieData)entityData;
            if (zombieData.baby) {
                this.setBaby(true);
                if (zombieData.tryChickenJockey) {
                    ChickenEntity chickenEntity2;
                    if ((double)random.nextFloat() < 0.05) {
                        List<Entity> list = world.getEntitiesByClass(ChickenEntity.class, this.getBoundingBox().expand(5.0, 3.0, 5.0), EntityPredicates.NOT_MOUNTED);
                        if (!list.isEmpty()) {
                            ChickenEntity chickenEntity = (ChickenEntity)list.get(0);
                            chickenEntity.setHasJockey(true);
                            this.startRiding(chickenEntity, false, false);
                        }
                    } else if ((double)random.nextFloat() < 0.05 && (chickenEntity2 = EntityType.CHICKEN.create(this.getEntityWorld(), SpawnReason.JOCKEY)) != null) {
                        chickenEntity2.refreshPositionAndAngles(this.getX(), this.getY(), this.getZ(), this.getYaw(), 0.0f);
                        chickenEntity2.initialize(world, difficulty, SpawnReason.JOCKEY, null);
                        chickenEntity2.setHasJockey(true);
                        this.startRiding(chickenEntity2, false, false);
                        world.spawnEntity(chickenEntity2);
                    }
                }
            }
            this.setCanBreakDoors(random.nextFloat() < f * 0.1f);
            if (spawnReason != SpawnReason.CONVERSION) {
                this.initEquipment(random, difficulty);
                this.updateEnchantments(world, random, difficulty);
            }
        }
        if (this.getEquippedStack(EquipmentSlot.HEAD).isEmpty() && Holidays.isHalloween() && random.nextFloat() < 0.25f) {
            this.equipStack(EquipmentSlot.HEAD, new ItemStack(random.nextFloat() < 0.1f ? Blocks.JACK_O_LANTERN : Blocks.CARVED_PUMPKIN));
            this.setEquipmentDropChance(EquipmentSlot.HEAD, 0.0f);
        }
        this.applyAttributeModifiers(f);
        return entityData;
    }

    @VisibleForTesting
    public void setInWaterTime(int inWaterTime) {
        this.inWaterTime = inWaterTime;
    }

    @VisibleForTesting
    public void setTicksUntilWaterConversionDirect(int ticksUntilWaterConversion) {
        this.ticksUntilWaterConversion = ticksUntilWaterConversion;
    }

    public static boolean shouldBeBaby(Random random) {
        return random.nextFloat() < 0.05f;
    }

    protected void applyAttributeModifiers(float chanceMultiplier) {
        this.initAttributes();
        this.getAttributeInstance(EntityAttributes.KNOCKBACK_RESISTANCE).overwritePersistentModifier(new EntityAttributeModifier(RANDOM_SPAWN_BONUS_MODIFIER_ID, this.random.nextDouble() * (double)0.05f, EntityAttributeModifier.Operation.ADD_VALUE));
        double d = this.random.nextDouble() * 1.5 * (double)chanceMultiplier;
        if (d > 1.0) {
            this.getAttributeInstance(EntityAttributes.FOLLOW_RANGE).overwritePersistentModifier(new EntityAttributeModifier(ZOMBIE_RANDOM_SPAWN_BONUS_MODIFIER_ID, d, EntityAttributeModifier.Operation.ADD_MULTIPLIED_TOTAL));
        }
        if (this.random.nextFloat() < chanceMultiplier * 0.05f) {
            this.getAttributeInstance(EntityAttributes.SPAWN_REINFORCEMENTS).overwritePersistentModifier(new EntityAttributeModifier(LEADER_ZOMBIE_BONUS_MODIFIER_ID, this.random.nextDouble() * 0.25 + 0.5, EntityAttributeModifier.Operation.ADD_VALUE));
            this.getAttributeInstance(EntityAttributes.MAX_HEALTH).overwritePersistentModifier(new EntityAttributeModifier(LEADER_ZOMBIE_BONUS_MODIFIER_ID, this.random.nextDouble() * 3.0 + 1.0, EntityAttributeModifier.Operation.ADD_MULTIPLIED_TOTAL));
            this.setCanBreakDoors(true);
        }
    }

    protected void initAttributes() {
        this.getAttributeInstance(EntityAttributes.SPAWN_REINFORCEMENTS).setBaseValue(this.random.nextDouble() * (double)0.1f);
    }

    class DestroyEggGoal
    extends StepAndDestroyBlockGoal {
        DestroyEggGoal(PathAwareEntity mob, double speed, int maxYDifference) {
            super(Blocks.TURTLE_EGG, mob, speed, maxYDifference);
        }

        @Override
        public void tickStepping(WorldAccess world, BlockPos pos) {
            world.playSound(null, pos, SoundEvents.ENTITY_ZOMBIE_DESTROY_EGG, SoundCategory.HOSTILE, 0.5f, 0.9f + ZombieEntity.this.random.nextFloat() * 0.2f);
        }

        @Override
        public void onDestroyBlock(World world, BlockPos pos) {
            world.playSound(null, pos, SoundEvents.ENTITY_TURTLE_EGG_BREAK, SoundCategory.BLOCKS, 0.7f, 0.9f + world.random.nextFloat() * 0.2f);
        }

        @Override
        public double getDesiredDistanceToTarget() {
            return 1.14;
        }
    }

    public static class ZombieData
    implements EntityData {
        public final boolean baby;
        public final boolean tryChickenJockey;

        public ZombieData(boolean baby, boolean tryChickenJockey) {
            this.baby = baby;
            this.tryChickenJockey = tryChickenJockey;
        }
    }
}


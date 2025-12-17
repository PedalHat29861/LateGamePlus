/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  com.google.common.collect.ImmutableList
 *  com.mojang.serialization.Dynamic
 *  org.jspecify.annotations.Nullable
 */
package net.minecraft.entity.passive;

import com.google.common.collect.ImmutableList;
import com.mojang.serialization.Dynamic;
import net.minecraft.block.BlockState;
import net.minecraft.entity.EntityData;
import net.minecraft.entity.EntityDimensions;
import net.minecraft.entity.EntityPose;
import net.minecraft.entity.EntityType;
import net.minecraft.entity.ItemEntity;
import net.minecraft.entity.SpawnReason;
import net.minecraft.entity.ai.brain.Brain;
import net.minecraft.entity.ai.brain.MemoryModuleType;
import net.minecraft.entity.ai.brain.sensor.Sensor;
import net.minecraft.entity.ai.brain.sensor.SensorType;
import net.minecraft.entity.ai.pathing.PathNodeType;
import net.minecraft.entity.attribute.DefaultAttributeContainer;
import net.minecraft.entity.attribute.EntityAttributes;
import net.minecraft.entity.damage.DamageSource;
import net.minecraft.entity.data.DataTracker;
import net.minecraft.entity.data.TrackedData;
import net.minecraft.entity.data.TrackedDataHandlerRegistry;
import net.minecraft.entity.passive.AnimalEntity;
import net.minecraft.entity.passive.GoatBrain;
import net.minecraft.entity.passive.PassiveEntity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.GoatHornItem;
import net.minecraft.item.Instrument;
import net.minecraft.item.ItemStack;
import net.minecraft.item.ItemUsage;
import net.minecraft.item.Items;
import net.minecraft.registry.RegistryKeys;
import net.minecraft.registry.tag.BlockTags;
import net.minecraft.registry.tag.InstrumentTags;
import net.minecraft.registry.tag.ItemTags;
import net.minecraft.registry.tag.TagKey;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.sound.SoundCategory;
import net.minecraft.sound.SoundEvent;
import net.minecraft.sound.SoundEvents;
import net.minecraft.storage.ReadView;
import net.minecraft.storage.WriteView;
import net.minecraft.util.ActionResult;
import net.minecraft.util.Hand;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.MathHelper;
import net.minecraft.util.math.Vec3d;
import net.minecraft.util.math.random.Random;
import net.minecraft.util.profiler.Profiler;
import net.minecraft.util.profiler.Profilers;
import net.minecraft.world.LocalDifficulty;
import net.minecraft.world.ServerWorldAccess;
import net.minecraft.world.World;
import net.minecraft.world.WorldAccess;
import org.jspecify.annotations.Nullable;

public class GoatEntity
extends AnimalEntity {
    public static final EntityDimensions LONG_JUMPING_DIMENSIONS = EntityDimensions.changing(0.9f, 1.3f).scaled(0.7f);
    private static final int DEFAULT_ATTACK_DAMAGE = 2;
    private static final int BABY_ATTACK_DAMAGE = 1;
    protected static final ImmutableList<SensorType<? extends Sensor<? super GoatEntity>>> SENSORS = ImmutableList.of(SensorType.NEAREST_LIVING_ENTITIES, SensorType.NEAREST_PLAYERS, SensorType.NEAREST_ITEMS, SensorType.NEAREST_ADULT, SensorType.HURT_BY, SensorType.FOOD_TEMPTATIONS);
    protected static final ImmutableList<MemoryModuleType<?>> MEMORY_MODULES = ImmutableList.of(MemoryModuleType.LOOK_TARGET, MemoryModuleType.VISIBLE_MOBS, MemoryModuleType.WALK_TARGET, MemoryModuleType.CANT_REACH_WALK_TARGET_SINCE, MemoryModuleType.PATH, MemoryModuleType.ATE_RECENTLY, MemoryModuleType.BREED_TARGET, MemoryModuleType.LONG_JUMP_COOLING_DOWN, MemoryModuleType.LONG_JUMP_MID_JUMP, MemoryModuleType.TEMPTING_PLAYER, MemoryModuleType.NEAREST_VISIBLE_ADULT, MemoryModuleType.TEMPTATION_COOLDOWN_TICKS, (Object[])new MemoryModuleType[]{MemoryModuleType.IS_TEMPTED, MemoryModuleType.RAM_COOLDOWN_TICKS, MemoryModuleType.RAM_TARGET, MemoryModuleType.IS_PANICKING});
    public static final int FALL_DAMAGE_SUBTRACTOR = 10;
    public static final double SCREAMING_CHANCE = 0.02;
    public static final double field_39046 = (double)0.1f;
    private static final TrackedData<Boolean> SCREAMING = DataTracker.registerData(GoatEntity.class, TrackedDataHandlerRegistry.BOOLEAN);
    private static final TrackedData<Boolean> LEFT_HORN = DataTracker.registerData(GoatEntity.class, TrackedDataHandlerRegistry.BOOLEAN);
    private static final TrackedData<Boolean> RIGHT_HORN = DataTracker.registerData(GoatEntity.class, TrackedDataHandlerRegistry.BOOLEAN);
    private static final boolean DEFAULT_SCREAMING = false;
    private static final boolean DEFAULT_LEFT_HORN = true;
    private static final boolean DEFAULT_RIGHT_HORN = true;
    private boolean preparingRam;
    private int headPitch;

    public GoatEntity(EntityType<? extends GoatEntity> entityType, World world) {
        super((EntityType<? extends AnimalEntity>)entityType, world);
        this.getNavigation().setCanSwim(true);
        this.setPathfindingPenalty(PathNodeType.POWDER_SNOW, -1.0f);
        this.setPathfindingPenalty(PathNodeType.DANGER_POWDER_SNOW, -1.0f);
    }

    public ItemStack getGoatHornStack() {
        Random random = Random.create(this.getUuid().hashCode());
        TagKey<Instrument> tagKey = this.isScreaming() ? InstrumentTags.SCREAMING_GOAT_HORNS : InstrumentTags.REGULAR_GOAT_HORNS;
        return this.getEntityWorld().getRegistryManager().getOrThrow(RegistryKeys.INSTRUMENT).getRandomEntry(tagKey, random).map(instrument -> GoatHornItem.getStackForInstrument(Items.GOAT_HORN, instrument)).orElseGet(() -> new ItemStack(Items.GOAT_HORN));
    }

    protected Brain.Profile<GoatEntity> createBrainProfile() {
        return Brain.createProfile(MEMORY_MODULES, SENSORS);
    }

    @Override
    protected Brain<?> deserializeBrain(Dynamic<?> dynamic) {
        return GoatBrain.create(this.createBrainProfile().deserialize(dynamic));
    }

    public static DefaultAttributeContainer.Builder createGoatAttributes() {
        return AnimalEntity.createAnimalAttributes().add(EntityAttributes.MAX_HEALTH, 10.0).add(EntityAttributes.MOVEMENT_SPEED, 0.2f).add(EntityAttributes.ATTACK_DAMAGE, 2.0);
    }

    @Override
    protected void onGrowUp() {
        if (this.isBaby()) {
            this.getAttributeInstance(EntityAttributes.ATTACK_DAMAGE).setBaseValue(1.0);
            this.removeHorns();
        } else {
            this.getAttributeInstance(EntityAttributes.ATTACK_DAMAGE).setBaseValue(2.0);
            this.addHorns();
        }
    }

    @Override
    protected int computeFallDamage(double fallDistance, float damagePerDistance) {
        return super.computeFallDamage(fallDistance, damagePerDistance) - 10;
    }

    @Override
    protected SoundEvent getAmbientSound() {
        if (this.isScreaming()) {
            return SoundEvents.ENTITY_GOAT_SCREAMING_AMBIENT;
        }
        return SoundEvents.ENTITY_GOAT_AMBIENT;
    }

    @Override
    protected SoundEvent getHurtSound(DamageSource source) {
        if (this.isScreaming()) {
            return SoundEvents.ENTITY_GOAT_SCREAMING_HURT;
        }
        return SoundEvents.ENTITY_GOAT_HURT;
    }

    @Override
    protected SoundEvent getDeathSound() {
        if (this.isScreaming()) {
            return SoundEvents.ENTITY_GOAT_SCREAMING_DEATH;
        }
        return SoundEvents.ENTITY_GOAT_DEATH;
    }

    @Override
    protected void playStepSound(BlockPos pos, BlockState state) {
        this.playSound(SoundEvents.ENTITY_GOAT_STEP, 0.15f, 1.0f);
    }

    protected SoundEvent getMilkingSound() {
        if (this.isScreaming()) {
            return SoundEvents.ENTITY_GOAT_SCREAMING_MILK;
        }
        return SoundEvents.ENTITY_GOAT_MILK;
    }

    @Override
    public @Nullable GoatEntity createChild(ServerWorld serverWorld, PassiveEntity passiveEntity) {
        GoatEntity goatEntity = EntityType.GOAT.create(serverWorld, SpawnReason.BREEDING);
        if (goatEntity != null) {
            PassiveEntity goatEntity2;
            GoatBrain.resetLongJumpCooldown(goatEntity, serverWorld.getRandom());
            PassiveEntity passiveEntity2 = serverWorld.getRandom().nextBoolean() ? this : passiveEntity;
            boolean bl = passiveEntity2 instanceof GoatEntity && ((GoatEntity)(goatEntity2 = passiveEntity2)).isScreaming() || serverWorld.getRandom().nextDouble() < 0.02;
            goatEntity.setScreaming(bl);
        }
        return goatEntity;
    }

    public Brain<GoatEntity> getBrain() {
        return super.getBrain();
    }

    @Override
    protected void mobTick(ServerWorld world) {
        Profiler profiler = Profilers.get();
        profiler.push("goatBrain");
        this.getBrain().tick(world, this);
        profiler.pop();
        profiler.push("goatActivityUpdate");
        GoatBrain.updateActivities(this);
        profiler.pop();
        super.mobTick(world);
    }

    @Override
    public int getMaxHeadRotation() {
        return 15;
    }

    @Override
    public void setHeadYaw(float headYaw) {
        int i = this.getMaxHeadRotation();
        float f = MathHelper.subtractAngles(this.bodyYaw, headYaw);
        float g = MathHelper.clamp(f, (float)(-i), (float)i);
        super.setHeadYaw(this.bodyYaw + g);
    }

    @Override
    protected void playEatSound() {
        this.getEntityWorld().playSoundFromEntity(null, this, this.isScreaming() ? SoundEvents.ENTITY_GOAT_SCREAMING_EAT : SoundEvents.ENTITY_GOAT_EAT, SoundCategory.NEUTRAL, 1.0f, MathHelper.nextBetween(this.getEntityWorld().random, 0.8f, 1.2f));
    }

    @Override
    public boolean isBreedingItem(ItemStack stack) {
        return stack.isIn(ItemTags.GOAT_FOOD);
    }

    @Override
    public ActionResult interactMob(PlayerEntity player, Hand hand) {
        ItemStack itemStack = player.getStackInHand(hand);
        if (itemStack.isOf(Items.BUCKET) && !this.isBaby()) {
            player.playSound(this.getMilkingSound(), 1.0f, 1.0f);
            ItemStack itemStack2 = ItemUsage.exchangeStack(itemStack, player, Items.MILK_BUCKET.getDefaultStack());
            player.setStackInHand(hand, itemStack2);
            return ActionResult.SUCCESS;
        }
        ActionResult actionResult = super.interactMob(player, hand);
        if (actionResult.isAccepted() && this.isBreedingItem(itemStack)) {
            this.playEatSound();
        }
        return actionResult;
    }

    @Override
    public EntityData initialize(ServerWorldAccess world, LocalDifficulty difficulty, SpawnReason spawnReason, @Nullable EntityData entityData) {
        Random random = world.getRandom();
        GoatBrain.resetLongJumpCooldown(this, random);
        this.setScreaming(random.nextDouble() < 0.02);
        this.onGrowUp();
        if (!this.isBaby() && (double)random.nextFloat() < (double)0.1f) {
            TrackedData<Boolean> trackedData = random.nextBoolean() ? LEFT_HORN : RIGHT_HORN;
            this.dataTracker.set(trackedData, false);
        }
        return super.initialize(world, difficulty, spawnReason, entityData);
    }

    @Override
    public EntityDimensions getBaseDimensions(EntityPose pose) {
        return pose == EntityPose.LONG_JUMPING ? LONG_JUMPING_DIMENSIONS.scaled(this.getScaleFactor()) : super.getBaseDimensions(pose);
    }

    @Override
    protected void writeCustomData(WriteView view) {
        super.writeCustomData(view);
        view.putBoolean("IsScreamingGoat", this.isScreaming());
        view.putBoolean("HasLeftHorn", this.hasLeftHorn());
        view.putBoolean("HasRightHorn", this.hasRightHorn());
    }

    @Override
    protected void readCustomData(ReadView view) {
        super.readCustomData(view);
        this.setScreaming(view.getBoolean("IsScreamingGoat", false));
        this.dataTracker.set(LEFT_HORN, view.getBoolean("HasLeftHorn", true));
        this.dataTracker.set(RIGHT_HORN, view.getBoolean("HasRightHorn", true));
    }

    @Override
    public void handleStatus(byte status) {
        if (status == 58) {
            this.preparingRam = true;
        } else if (status == 59) {
            this.preparingRam = false;
        } else {
            super.handleStatus(status);
        }
    }

    @Override
    public void tickMovement() {
        this.headPitch = this.preparingRam ? ++this.headPitch : (this.headPitch -= 2);
        this.headPitch = MathHelper.clamp(this.headPitch, 0, 20);
        super.tickMovement();
    }

    @Override
    protected void initDataTracker(DataTracker.Builder builder) {
        super.initDataTracker(builder);
        builder.add(SCREAMING, false);
        builder.add(LEFT_HORN, true);
        builder.add(RIGHT_HORN, true);
    }

    public boolean hasLeftHorn() {
        return this.dataTracker.get(LEFT_HORN);
    }

    public boolean hasRightHorn() {
        return this.dataTracker.get(RIGHT_HORN);
    }

    public boolean dropHorn() {
        boolean bl = this.hasLeftHorn();
        boolean bl2 = this.hasRightHorn();
        if (!bl && !bl2) {
            return false;
        }
        TrackedData<Boolean> trackedData = !bl ? RIGHT_HORN : (!bl2 ? LEFT_HORN : (this.random.nextBoolean() ? LEFT_HORN : RIGHT_HORN));
        this.dataTracker.set(trackedData, false);
        Vec3d vec3d = this.getEntityPos();
        ItemStack itemStack = this.getGoatHornStack();
        double d = MathHelper.nextBetween(this.random, -0.2f, 0.2f);
        double e = MathHelper.nextBetween(this.random, 0.3f, 0.7f);
        double f = MathHelper.nextBetween(this.random, -0.2f, 0.2f);
        ItemEntity itemEntity = new ItemEntity(this.getEntityWorld(), vec3d.getX(), vec3d.getY(), vec3d.getZ(), itemStack, d, e, f);
        this.getEntityWorld().spawnEntity(itemEntity);
        return true;
    }

    public void addHorns() {
        this.dataTracker.set(LEFT_HORN, true);
        this.dataTracker.set(RIGHT_HORN, true);
    }

    public void removeHorns() {
        this.dataTracker.set(LEFT_HORN, false);
        this.dataTracker.set(RIGHT_HORN, false);
    }

    public boolean isScreaming() {
        return this.dataTracker.get(SCREAMING);
    }

    public void setScreaming(boolean screaming) {
        this.dataTracker.set(SCREAMING, screaming);
    }

    public float getHeadPitch() {
        return (float)this.headPitch / 20.0f * 30.0f * ((float)Math.PI / 180);
    }

    public static boolean canSpawn(EntityType<? extends AnimalEntity> entityType, WorldAccess world, SpawnReason spawnReason, BlockPos pos, Random random) {
        return world.getBlockState(pos.down()).isIn(BlockTags.GOATS_SPAWNABLE_ON) && GoatEntity.isLightLevelValidForNaturalSpawn(world, pos);
    }

    @Override
    public /* synthetic */ @Nullable PassiveEntity createChild(ServerWorld world, PassiveEntity entity) {
        return this.createChild(world, entity);
    }
}


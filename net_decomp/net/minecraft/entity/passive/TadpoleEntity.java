/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  com.google.common.annotations.VisibleForTesting
 *  com.google.common.collect.ImmutableList
 *  com.mojang.serialization.Dynamic
 *  org.jspecify.annotations.Nullable
 */
package net.minecraft.entity.passive;

import com.google.common.annotations.VisibleForTesting;
import com.google.common.collect.ImmutableList;
import com.mojang.serialization.Dynamic;
import net.minecraft.component.DataComponentTypes;
import net.minecraft.component.type.NbtComponent;
import net.minecraft.entity.Bucketable;
import net.minecraft.entity.EntityType;
import net.minecraft.entity.SpawnReason;
import net.minecraft.entity.ai.brain.Brain;
import net.minecraft.entity.ai.brain.MemoryModuleType;
import net.minecraft.entity.ai.brain.sensor.Sensor;
import net.minecraft.entity.ai.brain.sensor.SensorType;
import net.minecraft.entity.ai.control.AquaticMoveControl;
import net.minecraft.entity.ai.control.YawAdjustingLookControl;
import net.minecraft.entity.ai.pathing.EntityNavigation;
import net.minecraft.entity.ai.pathing.SwimNavigation;
import net.minecraft.entity.attribute.DefaultAttributeContainer;
import net.minecraft.entity.attribute.EntityAttributes;
import net.minecraft.entity.conversion.EntityConversionContext;
import net.minecraft.entity.damage.DamageSource;
import net.minecraft.entity.passive.AnimalEntity;
import net.minecraft.entity.passive.FishEntity;
import net.minecraft.entity.passive.PassiveEntity;
import net.minecraft.entity.passive.TadpoleBrain;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.ItemStack;
import net.minecraft.item.Items;
import net.minecraft.nbt.NbtCompound;
import net.minecraft.particle.ParticleTypes;
import net.minecraft.registry.tag.ItemTags;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.sound.SoundEvent;
import net.minecraft.sound.SoundEvents;
import net.minecraft.storage.ReadView;
import net.minecraft.storage.WriteView;
import net.minecraft.util.ActionResult;
import net.minecraft.util.Hand;
import net.minecraft.util.profiler.Profiler;
import net.minecraft.util.profiler.Profilers;
import net.minecraft.world.World;
import org.jspecify.annotations.Nullable;

public class TadpoleEntity
extends FishEntity {
    private static final int DEFAULT_TADPOLE_AGE = 0;
    @VisibleForTesting
    public static int MAX_TADPOLE_AGE = Math.abs(-24000);
    public static final float WIDTH = 0.4f;
    public static final float HEIGHT = 0.3f;
    private int tadpoleAge = 0;
    protected static final ImmutableList<SensorType<? extends Sensor<? super TadpoleEntity>>> SENSORS = ImmutableList.of(SensorType.NEAREST_LIVING_ENTITIES, SensorType.NEAREST_PLAYERS, SensorType.HURT_BY, SensorType.FROG_TEMPTATIONS);
    protected static final ImmutableList<MemoryModuleType<?>> MEMORY_MODULES = ImmutableList.of(MemoryModuleType.LOOK_TARGET, MemoryModuleType.VISIBLE_MOBS, MemoryModuleType.WALK_TARGET, MemoryModuleType.CANT_REACH_WALK_TARGET_SINCE, MemoryModuleType.PATH, MemoryModuleType.NEAREST_VISIBLE_ADULT, MemoryModuleType.TEMPTATION_COOLDOWN_TICKS, MemoryModuleType.IS_TEMPTED, MemoryModuleType.TEMPTING_PLAYER, MemoryModuleType.BREED_TARGET, MemoryModuleType.IS_PANICKING);

    public TadpoleEntity(EntityType<? extends FishEntity> entityType, World world) {
        super(entityType, world);
        this.moveControl = new AquaticMoveControl(this, 85, 10, 0.02f, 0.1f, true);
        this.lookControl = new YawAdjustingLookControl(this, 10);
    }

    @Override
    protected EntityNavigation createNavigation(World world) {
        return new SwimNavigation(this, world);
    }

    protected Brain.Profile<TadpoleEntity> createBrainProfile() {
        return Brain.createProfile(MEMORY_MODULES, SENSORS);
    }

    @Override
    protected Brain<?> deserializeBrain(Dynamic<?> dynamic) {
        return TadpoleBrain.create(this.createBrainProfile().deserialize(dynamic));
    }

    public Brain<TadpoleEntity> getBrain() {
        return super.getBrain();
    }

    @Override
    protected SoundEvent getFlopSound() {
        return SoundEvents.ENTITY_TADPOLE_FLOP;
    }

    @Override
    protected void mobTick(ServerWorld world) {
        Profiler profiler = Profilers.get();
        profiler.push("tadpoleBrain");
        this.getBrain().tick(world, this);
        profiler.pop();
        profiler.push("tadpoleActivityUpdate");
        TadpoleBrain.updateActivities(this);
        profiler.pop();
        super.mobTick(world);
    }

    public static DefaultAttributeContainer.Builder createTadpoleAttributes() {
        return AnimalEntity.createAnimalAttributes().add(EntityAttributes.MOVEMENT_SPEED, 1.0).add(EntityAttributes.MAX_HEALTH, 6.0);
    }

    @Override
    public void tickMovement() {
        super.tickMovement();
        if (!this.getEntityWorld().isClient()) {
            this.setTadpoleAge(this.tadpoleAge + 1);
        }
    }

    @Override
    protected void writeCustomData(WriteView view) {
        super.writeCustomData(view);
        view.putInt("Age", this.tadpoleAge);
    }

    @Override
    protected void readCustomData(ReadView view) {
        super.readCustomData(view);
        this.setTadpoleAge(view.getInt("Age", 0));
    }

    @Override
    protected @Nullable SoundEvent getAmbientSound() {
        return null;
    }

    @Override
    protected @Nullable SoundEvent getHurtSound(DamageSource source) {
        return SoundEvents.ENTITY_TADPOLE_HURT;
    }

    @Override
    protected @Nullable SoundEvent getDeathSound() {
        return SoundEvents.ENTITY_TADPOLE_DEATH;
    }

    @Override
    public ActionResult interactMob(PlayerEntity player, Hand hand) {
        ItemStack itemStack = player.getStackInHand(hand);
        if (this.isFrogFood(itemStack)) {
            this.eatSlimeBall(player, itemStack);
            return ActionResult.SUCCESS;
        }
        return Bucketable.tryBucket(player, hand, this).orElse(super.interactMob(player, hand));
    }

    @Override
    public boolean isFromBucket() {
        return true;
    }

    @Override
    public void setFromBucket(boolean fromBucket) {
    }

    @Override
    public void copyDataToStack(ItemStack stack) {
        Bucketable.copyDataToStack(this, stack);
        NbtComponent.set(DataComponentTypes.BUCKET_ENTITY_DATA, stack, nbtCompound -> nbtCompound.putInt("Age", this.getTadpoleAge()));
    }

    @Override
    public void copyDataFromNbt(NbtCompound nbt) {
        Bucketable.copyDataFromNbt(this, nbt);
        nbt.getInt("Age").ifPresent(this::setTadpoleAge);
    }

    @Override
    public ItemStack getBucketItem() {
        return new ItemStack(Items.TADPOLE_BUCKET);
    }

    @Override
    public SoundEvent getBucketFillSound() {
        return SoundEvents.ITEM_BUCKET_FILL_TADPOLE;
    }

    private boolean isFrogFood(ItemStack stack) {
        return stack.isIn(ItemTags.FROG_FOOD);
    }

    private void eatSlimeBall(PlayerEntity player, ItemStack stack) {
        this.decrementItem(player, stack);
        this.increaseAge(PassiveEntity.toGrowUpAge(this.getTicksUntilGrowth()));
        this.getEntityWorld().addParticleClient(ParticleTypes.HAPPY_VILLAGER, this.getParticleX(1.0), this.getRandomBodyY() + 0.5, this.getParticleZ(1.0), 0.0, 0.0, 0.0);
    }

    private void decrementItem(PlayerEntity player, ItemStack stack) {
        stack.decrementUnlessCreative(1, player);
    }

    private int getTadpoleAge() {
        return this.tadpoleAge;
    }

    private void increaseAge(int seconds) {
        this.setTadpoleAge(this.tadpoleAge + seconds * 20);
    }

    private void setTadpoleAge(int tadpoleAge) {
        this.tadpoleAge = tadpoleAge;
        if (this.tadpoleAge >= MAX_TADPOLE_AGE) {
            this.growUp();
        }
    }

    private void growUp() {
        World world = this.getEntityWorld();
        if (world instanceof ServerWorld) {
            ServerWorld serverWorld = (ServerWorld)world;
            this.convertTo(EntityType.FROG, EntityConversionContext.create(this, false, false), frog -> {
                frog.initialize(serverWorld, serverWorld.getLocalDifficulty(frog.getBlockPos()), SpawnReason.CONVERSION, null);
                frog.setPersistent();
                frog.recalculateDimensions(this.getDimensions(this.getPose()));
                this.playSound(SoundEvents.ENTITY_TADPOLE_GROW_UP, 0.15f, 1.0f);
            });
        }
    }

    private int getTicksUntilGrowth() {
        return Math.max(0, MAX_TADPOLE_AGE - this.tadpoleAge);
    }

    @Override
    public boolean shouldDropExperience() {
        return false;
    }
}


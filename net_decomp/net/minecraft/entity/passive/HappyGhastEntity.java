/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  com.mojang.serialization.Dynamic
 *  org.jspecify.annotations.Nullable
 */
package net.minecraft.entity.passive;

import com.mojang.serialization.Dynamic;
import net.minecraft.block.BlockState;
import net.minecraft.entity.Entity;
import net.minecraft.entity.EntityType;
import net.minecraft.entity.EquipmentSlot;
import net.minecraft.entity.Leashable;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.SpawnReason;
import net.minecraft.entity.ai.brain.Brain;
import net.minecraft.entity.ai.control.BodyControl;
import net.minecraft.entity.ai.control.FlightMoveControl;
import net.minecraft.entity.ai.control.LookControl;
import net.minecraft.entity.ai.goal.SwimGoal;
import net.minecraft.entity.ai.goal.TemptGoal;
import net.minecraft.entity.ai.pathing.BirdNavigation;
import net.minecraft.entity.ai.pathing.EntityNavigation;
import net.minecraft.entity.attribute.DefaultAttributeContainer;
import net.minecraft.entity.attribute.EntityAttributes;
import net.minecraft.entity.damage.DamageSource;
import net.minecraft.entity.data.DataTracker;
import net.minecraft.entity.data.TrackedData;
import net.minecraft.entity.data.TrackedDataHandlerRegistry;
import net.minecraft.entity.mob.GhastEntity;
import net.minecraft.entity.mob.MobEntity;
import net.minecraft.entity.passive.AnimalEntity;
import net.minecraft.entity.passive.HappyGhastBrain;
import net.minecraft.entity.passive.PassiveEntity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.ItemStack;
import net.minecraft.network.packet.s2c.play.EntityPositionSyncS2CPacket;
import net.minecraft.registry.tag.ItemTags;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.sound.SoundCategory;
import net.minecraft.sound.SoundEvent;
import net.minecraft.sound.SoundEvents;
import net.minecraft.storage.ReadView;
import net.minecraft.storage.WriteView;
import net.minecraft.util.ActionResult;
import net.minecraft.util.Hand;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Box;
import net.minecraft.util.math.MathHelper;
import net.minecraft.util.math.Vec2f;
import net.minecraft.util.math.Vec3d;
import net.minecraft.util.profiler.Profiler;
import net.minecraft.util.profiler.Profilers;
import net.minecraft.world.World;
import net.minecraft.world.WorldView;
import net.minecraft.world.biome.Biome;
import org.jspecify.annotations.Nullable;

public class HappyGhastEntity
extends AnimalEntity {
    public static final float field_59681 = 0.2375f;
    public static final int field_59682 = 16;
    public static final int field_59683 = 32;
    public static final int field_59684 = 64;
    public static final int field_59685 = 16;
    public static final int field_59686 = 20;
    public static final int field_59687 = 600;
    public static final int field_59688 = 4;
    private static final int field_61061 = 60;
    private static final int field_60551 = 10;
    public static final float field_59689 = 2.0f;
    private int ropeRemovalTimer = 0;
    private int stillTimeout;
    private static final TrackedData<Boolean> HAS_ROPES = DataTracker.registerData(HappyGhastEntity.class, TrackedDataHandlerRegistry.BOOLEAN);
    private static final TrackedData<Boolean> STAYING_STILL = DataTracker.registerData(HappyGhastEntity.class, TrackedDataHandlerRegistry.BOOLEAN);
    private static final float field_60550 = 1.0f;

    public HappyGhastEntity(EntityType<? extends HappyGhastEntity> entityType, World world) {
        super((EntityType<? extends AnimalEntity>)entityType, world);
        this.moveControl = new GhastEntity.GhastMoveControl(this, true, this::isStill);
        this.lookControl = new HappyGhastLookControl();
    }

    private void setStillTimeout(int stillTimeout) {
        World world;
        if (this.stillTimeout <= 0 && stillTimeout > 0 && (world = this.getEntityWorld()) instanceof ServerWorld) {
            ServerWorld serverWorld = (ServerWorld)world;
            this.updateTrackedPosition(this.getX(), this.getY(), this.getZ());
            serverWorld.getChunkManager().chunkLoadingManager.sendToOtherNearbyPlayers(this, EntityPositionSyncS2CPacket.create(this));
        }
        this.stillTimeout = stillTimeout;
        this.syncStayingStill();
    }

    private EntityNavigation createGhastlingNavigation(World world) {
        return new GhastlingNavigation(this, world);
    }

    @Override
    protected void initGoals() {
        this.goalSelector.add(3, new HappyGhastSwimGoal());
        this.goalSelector.add(4, new TemptGoal.HappyGhastTemptGoal((MobEntity)this, 1.0, stack -> this.isWearingBodyArmor() || this.isBaby() ? stack.isIn(ItemTags.HAPPY_GHAST_FOOD) : stack.isIn(ItemTags.HAPPY_GHAST_TEMPT_ITEMS), false, 7.0));
        this.goalSelector.add(5, new GhastEntity.FlyRandomlyGoal(this, 16));
    }

    private void initAdultHappyGhast() {
        this.moveControl = new GhastEntity.GhastMoveControl(this, true, this::isStill);
        this.lookControl = new HappyGhastLookControl();
        this.navigation = this.createNavigation(this.getEntityWorld());
        World world = this.getEntityWorld();
        if (world instanceof ServerWorld) {
            ServerWorld serverWorld = (ServerWorld)world;
            this.clearGoals(goal -> true);
            this.initGoals();
            this.brain.stopAllTasks(serverWorld, this);
            this.brain.forgetAll();
        }
    }

    private void initGhastling() {
        this.moveControl = new FlightMoveControl(this, 180, true);
        this.lookControl = new LookControl(this);
        this.navigation = this.createGhastlingNavigation(this.getEntityWorld());
        this.setStillTimeout(0);
        this.clearGoals(goal -> true);
    }

    @Override
    protected void onGrowUp() {
        if (this.isBaby()) {
            this.initGhastling();
        } else {
            this.initAdultHappyGhast();
        }
        super.onGrowUp();
    }

    public static DefaultAttributeContainer.Builder createHappyGhastAttributes() {
        return AnimalEntity.createAnimalAttributes().add(EntityAttributes.MAX_HEALTH, 20.0).add(EntityAttributes.TEMPT_RANGE, 16.0).add(EntityAttributes.FLYING_SPEED, 0.05).add(EntityAttributes.MOVEMENT_SPEED, 0.05).add(EntityAttributes.FOLLOW_RANGE, 16.0).add(EntityAttributes.CAMERA_DISTANCE, 8.0);
    }

    @Override
    protected float clampScale(float scale) {
        return Math.min(scale, 1.0f);
    }

    @Override
    protected void fall(double heightDifference, boolean onGround, BlockState state, BlockPos landedPosition) {
    }

    @Override
    public boolean isClimbing() {
        return false;
    }

    @Override
    public void travel(Vec3d movementInput) {
        float f = (float)this.getAttributeValue(EntityAttributes.FLYING_SPEED) * 5.0f / 3.0f;
        this.travelFlying(movementInput, f, f, f);
    }

    @Override
    public float getPathfindingFavor(BlockPos pos, WorldView world) {
        if (!world.isAir(pos)) {
            return 0.0f;
        }
        if (world.isAir(pos.down()) && !world.isAir(pos.down(2))) {
            return 10.0f;
        }
        return 5.0f;
    }

    @Override
    public boolean canBreatheInWater() {
        if (this.isBaby()) {
            return true;
        }
        return super.canBreatheInWater();
    }

    @Override
    protected boolean shouldFollowLeash() {
        return false;
    }

    @Override
    protected void playStepSound(BlockPos pos, BlockState state) {
    }

    @Override
    public float getSoundPitch() {
        return 1.0f;
    }

    @Override
    public SoundCategory getSoundCategory() {
        return SoundCategory.NEUTRAL;
    }

    @Override
    public int getMinAmbientSoundDelay() {
        int i = super.getMinAmbientSoundDelay();
        if (this.hasPassengers()) {
            return i * 6;
        }
        return i;
    }

    @Override
    protected SoundEvent getAmbientSound() {
        return this.isBaby() ? SoundEvents.ENTITY_GHASTLING_AMBIENT : SoundEvents.ENTITY_HAPPY_GHAST_AMBIENT;
    }

    @Override
    protected SoundEvent getHurtSound(DamageSource source) {
        return this.isBaby() ? SoundEvents.ENTITY_GHASTLING_HURT : SoundEvents.ENTITY_HAPPY_GHAST_HURT;
    }

    @Override
    protected SoundEvent getDeathSound() {
        return this.isBaby() ? SoundEvents.ENTITY_GHASTLING_DEATH : SoundEvents.ENTITY_HAPPY_GHAST_DEATH;
    }

    @Override
    protected float getSoundVolume() {
        return this.isBaby() ? 1.0f : 4.0f;
    }

    @Override
    public int getLimitPerChunk() {
        return 1;
    }

    @Override
    public @Nullable PassiveEntity createChild(ServerWorld world, PassiveEntity entity) {
        return EntityType.HAPPY_GHAST.create(world, SpawnReason.BREEDING);
    }

    @Override
    public boolean canEat() {
        return false;
    }

    @Override
    public float getScaleFactor() {
        return this.isBaby() ? 0.2375f : 1.0f;
    }

    @Override
    public boolean isBreedingItem(ItemStack stack) {
        return stack.isIn(ItemTags.HAPPY_GHAST_FOOD);
    }

    @Override
    public boolean canUseSlot(EquipmentSlot slot) {
        if (slot == EquipmentSlot.BODY) {
            return this.isAlive() && !this.isBaby();
        }
        return super.canUseSlot(slot);
    }

    @Override
    protected boolean canDispenserEquipSlot(EquipmentSlot slot) {
        return slot == EquipmentSlot.BODY;
    }

    @Override
    public ActionResult interactMob(PlayerEntity player, Hand hand) {
        ActionResult actionResult;
        if (this.isBaby()) {
            return super.interactMob(player, hand);
        }
        ItemStack itemStack = player.getStackInHand(hand);
        if (!itemStack.isEmpty() && (actionResult = itemStack.useOnEntity(player, this, hand)).isAccepted()) {
            return actionResult;
        }
        if (this.isWearingBodyArmor() && !player.shouldCancelInteraction()) {
            this.addPassenger(player);
            return ActionResult.SUCCESS;
        }
        return super.interactMob(player, hand);
    }

    private void addPassenger(PlayerEntity player) {
        if (!this.getEntityWorld().isClient()) {
            player.startRiding(this);
        }
    }

    @Override
    protected void addPassenger(Entity passenger) {
        if (!this.hasPassengers()) {
            this.getEntityWorld().playSound(null, this.getX(), this.getY(), this.getZ(), SoundEvents.ENTITY_HAPPY_GHAST_HARNESS_GOGGLES_DOWN, this.getSoundCategory(), 1.0f, 1.0f);
        }
        super.addPassenger(passenger);
        if (!this.getEntityWorld().isClient()) {
            if (!this.hasPlayerOnTop()) {
                this.setStillTimeout(0);
            } else if (this.stillTimeout > 10) {
                this.setStillTimeout(10);
            }
        }
    }

    @Override
    protected void removePassenger(Entity passenger) {
        super.removePassenger(passenger);
        if (!this.getEntityWorld().isClient()) {
            this.setStillTimeout(10);
        }
        if (!this.hasPassengers()) {
            this.clearPositionTarget();
            this.getEntityWorld().playSound(null, this.getX(), this.getY(), this.getZ(), SoundEvents.ENTITY_HAPPY_GHAST_HARNESS_GOGGLES_UP, this.getSoundCategory(), 1.0f, 1.0f);
        }
    }

    @Override
    protected boolean canAddPassenger(Entity passenger) {
        return this.getPassengerList().size() < 4;
    }

    @Override
    public @Nullable LivingEntity getControllingPassenger() {
        Entity entity = this.getFirstPassenger();
        if (this.isWearingBodyArmor() && !this.isStill() && entity instanceof PlayerEntity) {
            PlayerEntity playerEntity = (PlayerEntity)entity;
            return playerEntity;
        }
        return super.getControllingPassenger();
    }

    @Override
    protected Vec3d getControlledMovementInput(PlayerEntity controllingPlayer, Vec3d movementInput) {
        float f = controllingPlayer.sidewaysSpeed;
        float g = 0.0f;
        float h = 0.0f;
        if (controllingPlayer.forwardSpeed != 0.0f) {
            float i = MathHelper.cos(controllingPlayer.getPitch() * ((float)Math.PI / 180));
            float j = -MathHelper.sin(controllingPlayer.getPitch() * ((float)Math.PI / 180));
            if (controllingPlayer.forwardSpeed < 0.0f) {
                i *= -0.5f;
                j *= -0.5f;
            }
            h = j;
            g = i;
        }
        if (controllingPlayer.isJumping()) {
            h += 0.5f;
        }
        return new Vec3d(f, h, g).multiply((double)3.9f * this.getAttributeValue(EntityAttributes.FLYING_SPEED));
    }

    protected Vec2f getGhastRotation(LivingEntity controllingEntity) {
        return new Vec2f(controllingEntity.getPitch() * 0.5f, controllingEntity.getYaw());
    }

    @Override
    protected void tickControlled(PlayerEntity controllingPlayer, Vec3d movementInput) {
        super.tickControlled(controllingPlayer, movementInput);
        Vec2f vec2f = this.getGhastRotation(controllingPlayer);
        float f = this.getYaw();
        float g = MathHelper.wrapDegrees(vec2f.y - f);
        float h = 0.08f;
        this.setRotation(f += g * 0.08f, vec2f.x);
        this.bodyYaw = this.headYaw = f;
        this.lastYaw = this.headYaw;
    }

    protected Brain.Profile<HappyGhastEntity> createBrainProfile() {
        return HappyGhastBrain.createBrainProfile();
    }

    @Override
    protected Brain<?> deserializeBrain(Dynamic<?> dynamic) {
        return HappyGhastBrain.create(this.createBrainProfile().deserialize(dynamic));
    }

    @Override
    protected void mobTick(ServerWorld world) {
        if (this.isBaby()) {
            Profiler profiler = Profilers.get();
            profiler.push("happyGhastBrain");
            this.brain.tick(world, this);
            profiler.pop();
            profiler.push("happyGhastActivityUpdate");
            HappyGhastBrain.updateActivities(this);
            profiler.pop();
        }
        this.updatePositionTarget();
        super.mobTick(world);
    }

    @Override
    public void tick() {
        super.tick();
        if (this.getEntityWorld().isClient()) {
            return;
        }
        if (this.ropeRemovalTimer > 0) {
            --this.ropeRemovalTimer;
        }
        this.setHasRopes(this.ropeRemovalTimer > 0);
        if (this.stillTimeout > 0) {
            if (this.age > 60) {
                --this.stillTimeout;
            }
            this.setStillTimeout(this.stillTimeout);
        }
        if (this.hasPlayerOnTop()) {
            this.setStillTimeout(10);
        }
    }

    @Override
    public void tickMovement() {
        if (!this.getEntityWorld().isClient()) {
            this.setAlwaysSyncAbsolute(this.isStill());
        }
        super.tickMovement();
        this.tickRegeneration();
    }

    private int getUpdatedPositionTargetRange() {
        if (!this.isBaby() && this.getEquippedStack(EquipmentSlot.BODY).isEmpty()) {
            return 64;
        }
        return 32;
    }

    private void updatePositionTarget() {
        if (this.isLeashed() || this.hasPassengers()) {
            return;
        }
        int i = this.getUpdatedPositionTargetRange();
        if (this.hasPositionTarget() && this.getPositionTarget().isWithinDistance(this.getBlockPos(), (double)(i + 16)) && i == this.getPositionTargetRange()) {
            return;
        }
        this.setPositionTarget(this.getBlockPos(), i);
    }

    private void tickRegeneration() {
        ServerWorld serverWorld;
        block5: {
            block4: {
                World world = this.getEntityWorld();
                if (!(world instanceof ServerWorld)) break block4;
                serverWorld = (ServerWorld)world;
                if (this.isAlive() && this.deathTime == 0 && this.getMaxHealth() != this.getHealth()) break block5;
            }
            return;
        }
        boolean bl = this.isAtCloudHeight() || serverWorld.getPrecipitation(this.getBlockPos()) != Biome.Precipitation.NONE;
        if (this.age % (bl ? 20 : 600) == 0) {
            this.heal(1.0f);
        }
    }

    @Override
    protected void initDataTracker(DataTracker.Builder builder) {
        super.initDataTracker(builder);
        builder.add(HAS_ROPES, false);
        builder.add(STAYING_STILL, false);
    }

    private void setHasRopes(boolean hasRopes) {
        this.dataTracker.set(HAS_ROPES, hasRopes);
    }

    public boolean hasRopes() {
        return this.dataTracker.get(HAS_ROPES);
    }

    private void syncStayingStill() {
        this.dataTracker.set(STAYING_STILL, this.stillTimeout > 0);
    }

    public boolean isStayingStill() {
        return this.dataTracker.get(STAYING_STILL);
    }

    @Override
    public boolean hasQuadLeashAttachmentPoints() {
        return true;
    }

    @Override
    public Vec3d[] getHeldQuadLeashOffsets() {
        return Leashable.createQuadLeashOffsets(this, -0.03125, 0.4375, 0.46875, 0.03125);
    }

    @Override
    public Vec3d getLeashOffset() {
        return Vec3d.ZERO;
    }

    @Override
    public double getElasticLeashDistance() {
        return 10.0;
    }

    @Override
    public double getLeashSnappingDistance() {
        return 16.0;
    }

    @Override
    public void onLongLeashTick() {
        super.onLongLeashTick();
        this.getMoveControl().setWaiting();
    }

    @Override
    public void tickHeldLeash(Leashable leashedEntity) {
        if (leashedEntity.canUseQuadLeashAttachmentPoint()) {
            this.ropeRemovalTimer = 5;
        }
    }

    @Override
    public void writeCustomData(WriteView view) {
        super.writeCustomData(view);
        view.putInt("still_timeout", this.stillTimeout);
    }

    @Override
    public void readCustomData(ReadView view) {
        super.readCustomData(view);
        this.setStillTimeout(view.getInt("still_timeout", 0));
    }

    public boolean isStill() {
        return this.isStayingStill() || this.stillTimeout > 0;
    }

    private boolean hasPlayerOnTop() {
        Box box = this.getBoundingBox();
        Box box2 = new Box(box.minX - 1.0, box.maxY - (double)1.0E-5f, box.minZ - 1.0, box.maxX + 1.0, box.maxY + box.getLengthY() / 2.0, box.maxZ + 1.0);
        for (PlayerEntity playerEntity : this.getEntityWorld().getPlayers()) {
            Entity entity;
            if (playerEntity.isSpectator() || (entity = playerEntity.getRootVehicle()) instanceof HappyGhastEntity || !box2.contains(entity.getEntityPos())) continue;
            return true;
        }
        return false;
    }

    @Override
    protected BodyControl createBodyControl() {
        return new HappyGhastBodyControl();
    }

    @Override
    public boolean isCollidable(@Nullable Entity entity) {
        if (this.isBaby() || !this.isAlive()) {
            return false;
        }
        if (this.getEntityWorld().isClient() && entity instanceof PlayerEntity && entity.getEntityPos().y >= this.getBoundingBox().maxY) {
            return true;
        }
        if (this.hasPassengers() && entity instanceof HappyGhastEntity) {
            return true;
        }
        return this.isStill();
    }

    @Override
    public boolean isFlyingVehicle() {
        return !this.isBaby();
    }

    @Override
    public Vec3d updatePassengerForDismount(LivingEntity passenger) {
        return new Vec3d(this.getX(), this.getBoundingBox().maxY, this.getZ());
    }

    class HappyGhastLookControl
    extends LookControl {
        HappyGhastLookControl() {
            super(HappyGhastEntity.this);
        }

        @Override
        public void tick() {
            if (HappyGhastEntity.this.isStill()) {
                float f = HappyGhastLookControl.getYawToSubtract(HappyGhastEntity.this.getYaw());
                HappyGhastEntity.this.setYaw(HappyGhastEntity.this.getYaw() - f);
                HappyGhastEntity.this.setHeadYaw(HappyGhastEntity.this.getYaw());
                return;
            }
            if (this.lookAtTimer > 0) {
                --this.lookAtTimer;
                double d = this.x - HappyGhastEntity.this.getX();
                double e = this.z - HappyGhastEntity.this.getZ();
                HappyGhastEntity.this.setYaw(-((float)MathHelper.atan2(d, e)) * 57.295776f);
                HappyGhastEntity.this.headYaw = HappyGhastEntity.this.bodyYaw = HappyGhastEntity.this.getYaw();
                return;
            }
            GhastEntity.updateYaw(this.entity);
        }

        public static float getYawToSubtract(float yaw) {
            float f = yaw % 90.0f;
            if (f >= 45.0f) {
                f -= 90.0f;
            }
            if (f < -45.0f) {
                f += 90.0f;
            }
            return f;
        }
    }

    static class GhastlingNavigation
    extends BirdNavigation {
        public GhastlingNavigation(HappyGhastEntity entity, World world) {
            super(entity, world);
            this.setCanOpenDoors(false);
            this.setCanSwim(true);
            this.setMaxFollowRange(48.0f);
        }

        @Override
        protected boolean canPathDirectlyThrough(Vec3d origin, Vec3d target) {
            return GhastlingNavigation.doesNotCollide(this.entity, origin, target, false);
        }
    }

    class HappyGhastSwimGoal
    extends SwimGoal {
        public HappyGhastSwimGoal() {
            super(HappyGhastEntity.this);
        }

        @Override
        public boolean canStart() {
            return !HappyGhastEntity.this.isStill() && super.canStart();
        }
    }

    class HappyGhastBodyControl
    extends BodyControl {
        public HappyGhastBodyControl() {
            super(HappyGhastEntity.this);
        }

        @Override
        public void tick() {
            if (HappyGhastEntity.this.hasPassengers()) {
                HappyGhastEntity.this.bodyYaw = HappyGhastEntity.this.headYaw = HappyGhastEntity.this.getYaw();
            }
            super.tick();
        }
    }
}


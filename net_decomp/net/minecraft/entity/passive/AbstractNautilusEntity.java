/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  org.jspecify.annotations.Nullable
 */
package net.minecraft.entity.passive;

import net.minecraft.block.BlockState;
import net.minecraft.block.Blocks;
import net.minecraft.component.DataComponentTypes;
import net.minecraft.component.type.EquippableComponent;
import net.minecraft.component.type.FoodComponent;
import net.minecraft.entity.Entity;
import net.minecraft.entity.EntityData;
import net.minecraft.entity.EntityType;
import net.minecraft.entity.EquipmentSlot;
import net.minecraft.entity.JumpingMount;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.MovementType;
import net.minecraft.entity.RideableInventory;
import net.minecraft.entity.SpawnReason;
import net.minecraft.entity.ai.brain.MemoryModuleType;
import net.minecraft.entity.ai.control.AquaticMoveControl;
import net.minecraft.entity.ai.control.YawAdjustingLookControl;
import net.minecraft.entity.ai.pathing.EntityNavigation;
import net.minecraft.entity.ai.pathing.PathNodeType;
import net.minecraft.entity.ai.pathing.SwimNavigation;
import net.minecraft.entity.attribute.DefaultAttributeContainer;
import net.minecraft.entity.attribute.EntityAttributes;
import net.minecraft.entity.damage.DamageSource;
import net.minecraft.entity.data.DataTracker;
import net.minecraft.entity.data.TrackedData;
import net.minecraft.entity.data.TrackedDataHandlerRegistry;
import net.minecraft.entity.effect.StatusEffectInstance;
import net.minecraft.entity.effect.StatusEffects;
import net.minecraft.entity.mob.MobEntity;
import net.minecraft.entity.passive.AnimalEntity;
import net.minecraft.entity.passive.NautilusBrain;
import net.minecraft.entity.passive.TameableEntity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.inventory.Inventory;
import net.minecraft.inventory.SimpleInventory;
import net.minecraft.inventory.StackReference;
import net.minecraft.item.ItemStack;
import net.minecraft.item.ItemUsage;
import net.minecraft.item.Items;
import net.minecraft.particle.ParticleTypes;
import net.minecraft.registry.entry.RegistryEntry;
import net.minecraft.registry.tag.FluidTags;
import net.minecraft.registry.tag.ItemTags;
import net.minecraft.screen.MountScreenHandler;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.sound.SoundEvent;
import net.minecraft.sound.SoundEvents;
import net.minecraft.util.ActionResult;
import net.minecraft.util.Hand;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.MathHelper;
import net.minecraft.util.math.Vec2f;
import net.minecraft.util.math.Vec3d;
import net.minecraft.util.math.random.Random;
import net.minecraft.world.LocalDifficulty;
import net.minecraft.world.ServerWorldAccess;
import net.minecraft.world.World;
import net.minecraft.world.WorldAccess;
import net.minecraft.world.WorldView;
import net.minecraft.world.event.GameEvent;
import org.jspecify.annotations.Nullable;

public abstract class AbstractNautilusEntity
extends TameableEntity
implements RideableInventory,
JumpingMount {
    public static final int field_64484 = 500;
    public static final int field_64482 = 3;
    public static final int field_63349 = 16;
    public static final int field_63328 = 32;
    public static final int field_63329 = 8;
    private static final int field_63332 = 60;
    private static final int field_63333 = 40;
    private static final double field_63334 = 0.9;
    private static final float field_63335 = 0.011f;
    private static final float field_63336 = 0.0325f;
    private static final float field_63807 = 0.02f;
    private static final TrackedData<Boolean> DASHING = DataTracker.registerData(AbstractNautilusEntity.class, TrackedDataHandlerRegistry.BOOLEAN);
    private static final int field_63338 = 40;
    private static final int field_63339 = 5;
    private static final float field_63340 = 1.2f;
    private static final float field_63341 = 0.5f;
    private int jumpCooldown = 0;
    protected float dashStrength;
    protected SimpleInventory inventory;
    private static final double field_63343 = 0.8;
    private static final double field_63344 = 1.1;
    private static final double field_63345 = 0.25;
    private static final double field_63346 = 2.0;
    private static final float field_63347 = 0.15f;
    private static final float field_63348 = 1.0f;

    protected AbstractNautilusEntity(EntityType<? extends AbstractNautilusEntity> entityType, World world) {
        super((EntityType<? extends TameableEntity>)entityType, world);
        this.moveControl = new AquaticMoveControl(this, 85, 10, 0.011f, 0.0f, true);
        this.lookControl = new YawAdjustingLookControl(this, 10);
        this.setPathfindingPenalty(PathNodeType.WATER, 0.0f);
        this.initInventory();
    }

    @Override
    public boolean isBreedingItem(ItemStack stack) {
        return this.isTamed() || this.isBaby() ? stack.isIn(ItemTags.NAUTILUS_FOOD) : stack.isIn(ItemTags.NAUTILUS_TAMING_ITEMS);
    }

    @Override
    protected void eat(PlayerEntity player, Hand hand, ItemStack stack) {
        if (stack.isIn(ItemTags.NAUTILUS_BUCKET_FOOD)) {
            player.setStackInHand(hand, ItemUsage.exchangeStack(stack, player, new ItemStack(Items.WATER_BUCKET)));
        } else {
            super.eat(player, hand, stack);
        }
    }

    public static DefaultAttributeContainer.Builder createNautilusAttributes() {
        return AnimalEntity.createAnimalAttributes().add(EntityAttributes.MAX_HEALTH, 15.0).add(EntityAttributes.MOVEMENT_SPEED, 1.0).add(EntityAttributes.ATTACK_DAMAGE, 3.0).add(EntityAttributes.KNOCKBACK_RESISTANCE, 0.3f);
    }

    @Override
    public boolean isPushedByFluids() {
        return false;
    }

    @Override
    protected EntityNavigation createNavigation(World world) {
        return new SwimNavigation(this, world);
    }

    @Override
    public float getPathfindingFavor(BlockPos pos, WorldView world) {
        return 0.0f;
    }

    public static boolean canSpawn(EntityType<? extends AbstractNautilusEntity> type, WorldAccess world, SpawnReason reason, BlockPos pos, Random random) {
        int i = world.getSeaLevel();
        int j = i - 25;
        return pos.getY() >= j && pos.getY() <= i - 5 && world.getFluidState(pos.down()).isIn(FluidTags.WATER) && world.getBlockState(pos.up()).isOf(Blocks.WATER);
    }

    @Override
    public boolean canSpawn(WorldView world) {
        return world.doesNotIntersectEntities(this);
    }

    @Override
    public boolean canUseSlot(EquipmentSlot slot) {
        if (slot == EquipmentSlot.SADDLE || slot == EquipmentSlot.BODY) {
            return this.isAlive() && !this.isBaby() && this.isTamed();
        }
        return super.canUseSlot(slot);
    }

    @Override
    protected boolean canDispenserEquipSlot(EquipmentSlot slot) {
        return slot == EquipmentSlot.BODY || slot == EquipmentSlot.SADDLE || super.canDispenserEquipSlot(slot);
    }

    @Override
    protected boolean canAddPassenger(Entity passenger) {
        return !this.hasPassengers();
    }

    @Override
    public @Nullable LivingEntity getControllingPassenger() {
        Entity entity = this.getFirstPassenger();
        if (this.hasSaddleEquipped() && entity instanceof PlayerEntity) {
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
        return new Vec3d(f, h, g);
    }

    protected Vec2f getControlledRotation(LivingEntity entity) {
        return new Vec2f(entity.getPitch() * 0.5f, entity.getYaw());
    }

    @Override
    protected void tickControlled(PlayerEntity controllingPlayer, Vec3d movementInput) {
        super.tickControlled(controllingPlayer, movementInput);
        Vec2f vec2f = this.getControlledRotation(controllingPlayer);
        float f = this.getYaw();
        float g = MathHelper.wrapDegrees(vec2f.y - f);
        float h = 0.5f;
        this.setRotation(f += g * 0.5f, vec2f.x);
        this.bodyYaw = this.headYaw = f;
        this.lastYaw = this.headYaw;
        if (this.isLogicalSideForUpdatingMovement()) {
            if (this.dashStrength > 0.0f && !this.isJumping()) {
                this.dash(this.dashStrength, controllingPlayer);
            }
            this.dashStrength = 0.0f;
        }
    }

    @Override
    protected void travelInWater(Vec3d movementInput, double gravity, boolean falling, double y) {
        float f = this.getMovementSpeed();
        this.updateVelocity(f, movementInput);
        this.move(MovementType.SELF, this.getVelocity());
        this.setVelocity(this.getVelocity().multiply(0.9));
    }

    @Override
    protected float getSaddledSpeed(PlayerEntity controllingPlayer) {
        return this.isTouchingWater() ? 0.0325f * (float)this.getAttributeValue(EntityAttributes.MOVEMENT_SPEED) : 0.02f * (float)this.getAttributeValue(EntityAttributes.MOVEMENT_SPEED);
    }

    protected void putPlayerOnBack(PlayerEntity player) {
        if (!this.getEntityWorld().isClient()) {
            player.startRiding(this);
            if (!this.hasPassengers()) {
                this.clearPositionTarget();
            }
        }
    }

    private int getMaxTargetRange() {
        if (!this.isBaby() && this.getEquippedStack(EquipmentSlot.SADDLE).isEmpty()) {
            return 32;
        }
        return 16;
    }

    protected void tickPositionTarget() {
        if (this.isLeashed() || this.hasPassengers() || !this.isTamed()) {
            return;
        }
        int i = this.getMaxTargetRange();
        if (this.hasPositionTarget() && this.getPositionTarget().isWithinDistance(this.getBlockPos(), (double)(i + 8)) && i == this.getPositionTargetRange()) {
            return;
        }
        this.setPositionTarget(this.getBlockPos(), i);
    }

    @Override
    protected void mobTick(ServerWorld world) {
        this.tickPositionTarget();
        super.mobTick(world);
    }

    private void tickController(World world) {
        Entity entity = this.getFirstPassenger();
        if (entity instanceof PlayerEntity) {
            boolean bl2;
            PlayerEntity playerEntity = (PlayerEntity)entity;
            boolean bl = playerEntity.hasStatusEffect(StatusEffects.BREATH_OF_THE_NAUTILUS);
            boolean bl3 = bl2 = world.getTime() % 40L == 0L;
            if (!bl || bl2) {
                playerEntity.addStatusEffect(new StatusEffectInstance(StatusEffects.BREATH_OF_THE_NAUTILUS, 60, 0, true, true, true));
            }
        }
    }

    private void spawnParticles() {
        double d = this.getVelocity().length();
        double e = MathHelper.clamp(d * 2.0, (double)0.15f, 1.0);
        if ((double)this.random.nextFloat() < e) {
            float f = this.getYaw();
            float g = MathHelper.clamp(this.getPitch(), -10.0f, 10.0f);
            Vec3d vec3d = this.getRotationVector(g, f);
            double h = this.random.nextDouble() * 0.8 * (1.0 + d);
            double i = ((double)this.random.nextFloat() - 0.5) * h;
            double j = ((double)this.random.nextFloat() - 0.5) * h;
            double k = ((double)this.random.nextFloat() - 0.5) * h;
            this.getEntityWorld().addParticleClient(ParticleTypes.BUBBLE, this.getX() - vec3d.x * 1.1, this.getY() - vec3d.y + 0.25, this.getZ() - vec3d.z * 1.1, i, j, k);
        }
    }

    @Override
    public void tick() {
        super.tick();
        if (!this.getEntityWorld().isClient()) {
            this.tickController(this.getEntityWorld());
        }
        if (this.isDashing() && this.jumpCooldown < 35) {
            this.setDashing(false);
        }
        if (this.jumpCooldown > 0) {
            --this.jumpCooldown;
            if (this.jumpCooldown == 0) {
                this.playSound(this.getDashReadySound());
            }
        }
        if (this.isTouchingWater()) {
            this.spawnParticles();
        }
    }

    @Override
    public boolean canJump() {
        return this.hasSaddleEquipped();
    }

    @Override
    public void setJumpStrength(int strength) {
        if (!this.hasSaddleEquipped() || this.jumpCooldown > 0) {
            return;
        }
        this.dashStrength = this.clampJumpStrength(strength);
    }

    @Override
    protected void initDataTracker(DataTracker.Builder builder) {
        super.initDataTracker(builder);
        builder.add(DASHING, false);
    }

    public boolean isDashing() {
        return this.dataTracker.get(DASHING);
    }

    public void setDashing(boolean dashing) {
        this.dataTracker.set(DASHING, dashing);
    }

    protected void dash(float strength, PlayerEntity controller) {
        this.addVelocityInternal(controller.getRotationVector().multiply((double)((this.isTouchingWater() ? 1.2f : 0.5f) * strength) * this.getAttributeValue(EntityAttributes.MOVEMENT_SPEED) * (double)this.getVelocityMultiplier()));
        this.jumpCooldown = 40;
        this.setDashing(true);
        this.velocityDirty = true;
    }

    @Override
    public void startJumping(int height) {
        this.playSound(this.getDashSound());
        this.emitGameEvent(GameEvent.ENTITY_ACTION);
        this.setDashing(true);
    }

    @Override
    public int getJumpCooldown() {
        return this.jumpCooldown;
    }

    @Override
    public void onTrackedDataSet(TrackedData<?> data) {
        if (!this.firstUpdate && DASHING.equals(data)) {
            this.jumpCooldown = this.jumpCooldown == 0 ? 40 : this.jumpCooldown;
        }
        super.onTrackedDataSet(data);
    }

    @Override
    public void stopJumping() {
    }

    @Override
    protected void playStepSound(BlockPos pos, BlockState state) {
    }

    protected @Nullable SoundEvent getDashSound() {
        return null;
    }

    protected @Nullable SoundEvent getDashReadySound() {
        return null;
    }

    @Override
    public ActionResult interact(PlayerEntity player, Hand hand) {
        this.setPersistent();
        return super.interact(player, hand);
    }

    @Override
    public ActionResult interactMob(PlayerEntity player, Hand hand) {
        ItemStack itemStack = player.getStackInHand(hand);
        if (this.isBaby()) {
            return super.interactMob(player, hand);
        }
        if (this.isTamed() && player.shouldCancelInteraction()) {
            this.openInventory(player);
            return ActionResult.SUCCESS;
        }
        if (!itemStack.isEmpty()) {
            if (!this.getEntityWorld().isClient() && !this.isTamed() && this.isBreedingItem(itemStack)) {
                this.eat(player, hand, itemStack);
                this.tryTame(player);
                return ActionResult.SUCCESS_SERVER;
            }
            if (this.isBreedingItem(itemStack) && this.getHealth() < this.getMaxHealth()) {
                FoodComponent foodComponent = itemStack.get(DataComponentTypes.FOOD);
                this.heal(foodComponent != null ? (float)(2 * foodComponent.nutrition()) : 1.0f);
                this.eat(player, hand, itemStack);
                this.playEatSound();
                return ActionResult.SUCCESS;
            }
            ActionResult actionResult = itemStack.useOnEntity(player, this, hand);
            if (actionResult.isAccepted()) {
                return actionResult;
            }
        }
        if (this.isTamed() && !player.shouldCancelInteraction() && !this.isBreedingItem(itemStack)) {
            this.putPlayerOnBack(player);
            return ActionResult.SUCCESS;
        }
        return super.interactMob(player, hand);
    }

    private void tryTame(PlayerEntity player) {
        if (this.random.nextInt(3) == 0) {
            this.setTamedBy(player);
            this.navigation.stop();
            this.getEntityWorld().sendEntityStatus(this, (byte)7);
        } else {
            this.getEntityWorld().sendEntityStatus(this, (byte)6);
        }
        this.playEatSound();
    }

    @Override
    public boolean canImmediatelyDespawn(double distanceSquared) {
        return true;
    }

    @Override
    public boolean damage(ServerWorld world, DamageSource source, float amount) {
        Entity entity;
        boolean bl = super.damage(world, source, amount);
        if (bl && (entity = source.getAttacker()) instanceof LivingEntity) {
            LivingEntity livingEntity = (LivingEntity)entity;
            NautilusBrain.onDamage(world, this, livingEntity);
        }
        return bl;
    }

    @Override
    public boolean canHaveStatusEffect(StatusEffectInstance effect) {
        if (effect.getEffectType() == StatusEffects.POISON) {
            return false;
        }
        return super.canHaveStatusEffect(effect);
    }

    @Override
    public EntityData initialize(ServerWorldAccess world, LocalDifficulty difficulty, SpawnReason spawnReason, @Nullable EntityData entityData) {
        Random random = world.getRandom();
        NautilusBrain.initialize(this, random);
        return super.initialize(world, difficulty, spawnReason, entityData);
    }

    @Override
    protected RegistryEntry<SoundEvent> getEquipSound(EquipmentSlot slot, ItemStack stack, EquippableComponent equippableComponent) {
        if (slot == EquipmentSlot.SADDLE && this.isSubmergedInWater()) {
            return SoundEvents.ITEM_NAUTILUS_SADDLE_UNDERWATER_EQUIP;
        }
        if (slot == EquipmentSlot.SADDLE) {
            return SoundEvents.ITEM_NAUTILUS_SADDLE_EQUIP;
        }
        return super.getEquipSound(slot, stack, equippableComponent);
    }

    public final int getSlotCount() {
        return MountScreenHandler.getSlotCount(this.getInventoryColumns());
    }

    protected void initInventory() {
        SimpleInventory simpleInventory = this.inventory;
        this.inventory = new SimpleInventory(this.getSlotCount());
        if (simpleInventory != null) {
            int i = Math.min(simpleInventory.size(), this.inventory.size());
            for (int j = 0; j < i; ++j) {
                ItemStack itemStack = simpleInventory.getStack(j);
                if (itemStack.isEmpty()) continue;
                this.inventory.setStack(j, itemStack.copy());
            }
        }
    }

    @Override
    public void openInventory(PlayerEntity player) {
        if (!this.getEntityWorld().isClient() && (!this.hasPassengers() || this.hasPassenger(player)) && this.isTamed()) {
            player.openNautilusInventory(this, this.inventory);
        }
    }

    @Override
    public @Nullable StackReference getStackReference(int slot) {
        int i = slot - 500;
        if (i >= 0 && i < this.inventory.size()) {
            return this.inventory.getStackReference(i);
        }
        return super.getStackReference(slot);
    }

    public boolean areInventoriesDifferent(Inventory inventory) {
        return this.inventory != inventory;
    }

    public int getInventoryColumns() {
        return 0;
    }

    protected boolean isControlledByMob() {
        return this.getFirstPassenger() instanceof MobEntity;
    }

    protected boolean hasAttackTarget() {
        return this.getBrain().hasMemoryModule(MemoryModuleType.ANGRY_AT) || this.getBrain().hasMemoryModule(MemoryModuleType.ATTACK_TARGET);
    }
}


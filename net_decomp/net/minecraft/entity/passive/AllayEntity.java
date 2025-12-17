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
import java.util.Objects;
import java.util.Optional;
import java.util.UUID;
import java.util.function.BiConsumer;
import net.minecraft.block.BlockState;
import net.minecraft.block.Blocks;
import net.minecraft.component.DataComponentTypes;
import net.minecraft.component.EnchantmentEffectComponentTypes;
import net.minecraft.component.type.PotionContentsComponent;
import net.minecraft.enchantment.EnchantmentHelper;
import net.minecraft.entity.Entity;
import net.minecraft.entity.EntityType;
import net.minecraft.entity.EquipmentSlot;
import net.minecraft.entity.InventoryOwner;
import net.minecraft.entity.ItemEntity;
import net.minecraft.entity.SpawnReason;
import net.minecraft.entity.ai.brain.Brain;
import net.minecraft.entity.ai.brain.MemoryModuleState;
import net.minecraft.entity.ai.brain.MemoryModuleType;
import net.minecraft.entity.ai.brain.sensor.Sensor;
import net.minecraft.entity.ai.brain.sensor.SensorType;
import net.minecraft.entity.ai.brain.task.TargetUtil;
import net.minecraft.entity.ai.control.FlightMoveControl;
import net.minecraft.entity.ai.pathing.BirdNavigation;
import net.minecraft.entity.ai.pathing.EntityNavigation;
import net.minecraft.entity.attribute.DefaultAttributeContainer;
import net.minecraft.entity.attribute.EntityAttributes;
import net.minecraft.entity.damage.DamageSource;
import net.minecraft.entity.data.DataTracker;
import net.minecraft.entity.data.TrackedData;
import net.minecraft.entity.data.TrackedDataHandlerRegistry;
import net.minecraft.entity.mob.MobEntity;
import net.minecraft.entity.mob.PathAwareEntity;
import net.minecraft.entity.passive.AllayBrain;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.inventory.SimpleInventory;
import net.minecraft.item.ItemStack;
import net.minecraft.particle.ParticleTypes;
import net.minecraft.registry.entry.RegistryEntry;
import net.minecraft.registry.tag.GameEventTags;
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
import net.minecraft.util.math.GlobalPos;
import net.minecraft.util.math.MathHelper;
import net.minecraft.util.math.Vec3d;
import net.minecraft.util.math.Vec3i;
import net.minecraft.util.profiler.Profiler;
import net.minecraft.util.profiler.Profilers;
import net.minecraft.world.World;
import net.minecraft.world.event.EntityPositionSource;
import net.minecraft.world.event.GameEvent;
import net.minecraft.world.event.PositionSource;
import net.minecraft.world.event.Vibrations;
import net.minecraft.world.event.listener.EntityGameEventHandler;
import net.minecraft.world.event.listener.GameEventListener;
import net.minecraft.world.rule.GameRules;
import org.jspecify.annotations.Nullable;

public class AllayEntity
extends PathAwareEntity
implements InventoryOwner,
Vibrations {
    private static final Vec3i ITEM_PICKUP_RANGE_EXPANDER = new Vec3i(1, 1, 1);
    private static final int field_39461 = 5;
    private static final float field_39462 = 55.0f;
    private static final float field_39463 = 15.0f;
    private static final int DEFAULT_DUPLICATION_COOLDOWN = 0;
    private static final int DUPLICATION_COOLDOWN = 6000;
    private static final int field_39679 = 3;
    public static final int field_54974 = 1024;
    private static final TrackedData<Boolean> DANCING = DataTracker.registerData(AllayEntity.class, TrackedDataHandlerRegistry.BOOLEAN);
    private static final TrackedData<Boolean> CAN_DUPLICATE = DataTracker.registerData(AllayEntity.class, TrackedDataHandlerRegistry.BOOLEAN);
    protected static final ImmutableList<SensorType<? extends Sensor<? super AllayEntity>>> SENSORS = ImmutableList.of(SensorType.NEAREST_LIVING_ENTITIES, SensorType.NEAREST_PLAYERS, SensorType.HURT_BY, SensorType.NEAREST_ITEMS);
    protected static final ImmutableList<MemoryModuleType<?>> MEMORY_MODULES = ImmutableList.of(MemoryModuleType.PATH, MemoryModuleType.LOOK_TARGET, MemoryModuleType.VISIBLE_MOBS, MemoryModuleType.WALK_TARGET, MemoryModuleType.CANT_REACH_WALK_TARGET_SINCE, MemoryModuleType.HURT_BY, MemoryModuleType.NEAREST_VISIBLE_WANTED_ITEM, MemoryModuleType.LIKED_PLAYER, MemoryModuleType.LIKED_NOTEBLOCK, MemoryModuleType.LIKED_NOTEBLOCK_COOLDOWN_TICKS, MemoryModuleType.ITEM_PICKUP_COOLDOWN_TICKS, MemoryModuleType.IS_PANICKING, (Object[])new MemoryModuleType[0]);
    public static final ImmutableList<Float> THROW_SOUND_PITCHES = ImmutableList.of((Object)Float.valueOf(0.5625f), (Object)Float.valueOf(0.625f), (Object)Float.valueOf(0.75f), (Object)Float.valueOf(0.9375f), (Object)Float.valueOf(1.0f), (Object)Float.valueOf(1.0f), (Object)Float.valueOf(1.125f), (Object)Float.valueOf(1.25f), (Object)Float.valueOf(1.5f), (Object)Float.valueOf(1.875f), (Object)Float.valueOf(2.0f), (Object)Float.valueOf(2.25f), (Object[])new Float[]{Float.valueOf(2.5f), Float.valueOf(3.0f), Float.valueOf(3.75f), Float.valueOf(4.0f)});
    private final EntityGameEventHandler<Vibrations.VibrationListener> gameEventHandler;
    private Vibrations.ListenerData vibrationListenerData;
    private final Vibrations.Callback vibrationCallback;
    private final EntityGameEventHandler<JukeboxEventListener> jukeboxEventHandler;
    private final SimpleInventory inventory = new SimpleInventory(1);
    private @Nullable BlockPos jukeboxPos;
    private long duplicationCooldown = 0L;
    private float itemHoldAnimationTicks;
    private float lastItemHoldAnimationTicks;
    private float danceTicks;
    private float spinningAnimationTicks;
    private float lastSpinningAnimationTicks;

    public AllayEntity(EntityType<? extends AllayEntity> entityType, World world) {
        super((EntityType<? extends PathAwareEntity>)entityType, world);
        this.moveControl = new FlightMoveControl(this, 20, true);
        this.setCanPickUpLoot(this.canPickUpLoot());
        this.vibrationCallback = new VibrationCallback();
        this.vibrationListenerData = new Vibrations.ListenerData();
        this.gameEventHandler = new EntityGameEventHandler<Vibrations.VibrationListener>(new Vibrations.VibrationListener(this));
        this.jukeboxEventHandler = new EntityGameEventHandler<JukeboxEventListener>(new JukeboxEventListener(this.vibrationCallback.getPositionSource(), GameEvent.JUKEBOX_PLAY.value().notificationRadius()));
    }

    protected Brain.Profile<AllayEntity> createBrainProfile() {
        return Brain.createProfile(MEMORY_MODULES, SENSORS);
    }

    @Override
    protected Brain<?> deserializeBrain(Dynamic<?> dynamic) {
        return AllayBrain.create(this.createBrainProfile().deserialize(dynamic));
    }

    public Brain<AllayEntity> getBrain() {
        return super.getBrain();
    }

    public static DefaultAttributeContainer.Builder createAllayAttributes() {
        return MobEntity.createMobAttributes().add(EntityAttributes.MAX_HEALTH, 20.0).add(EntityAttributes.FLYING_SPEED, 0.1f).add(EntityAttributes.MOVEMENT_SPEED, 0.1f).add(EntityAttributes.ATTACK_DAMAGE, 2.0);
    }

    @Override
    protected EntityNavigation createNavigation(World world) {
        BirdNavigation birdNavigation = new BirdNavigation(this, world);
        birdNavigation.setCanOpenDoors(false);
        birdNavigation.setCanSwim(true);
        birdNavigation.setMaxFollowRange(48.0f);
        return birdNavigation;
    }

    @Override
    protected void initDataTracker(DataTracker.Builder builder) {
        super.initDataTracker(builder);
        builder.add(DANCING, false);
        builder.add(CAN_DUPLICATE, true);
    }

    @Override
    public void travel(Vec3d movementInput) {
        this.travelFlying(movementInput, this.getMovementSpeed());
    }

    @Override
    public boolean damage(ServerWorld world, DamageSource source, float amount) {
        if (this.isLikedBy(source.getAttacker())) {
            return false;
        }
        return super.damage(world, source, amount);
    }

    @Override
    protected boolean isInSameTeam(Entity other) {
        return this.isLikedBy(other) || super.isInSameTeam(other);
    }

    private boolean isLikedBy(@Nullable Entity player) {
        if (player instanceof PlayerEntity) {
            PlayerEntity playerEntity = (PlayerEntity)player;
            Optional<UUID> optional = this.getBrain().getOptionalRegisteredMemory(MemoryModuleType.LIKED_PLAYER);
            return optional.isPresent() && playerEntity.getUuid().equals(optional.get());
        }
        return false;
    }

    @Override
    protected void playStepSound(BlockPos pos, BlockState state) {
    }

    @Override
    protected void fall(double heightDifference, boolean onGround, BlockState state, BlockPos landedPosition) {
    }

    @Override
    protected SoundEvent getAmbientSound() {
        return this.hasStackEquipped(EquipmentSlot.MAINHAND) ? SoundEvents.ENTITY_ALLAY_AMBIENT_WITH_ITEM : SoundEvents.ENTITY_ALLAY_AMBIENT_WITHOUT_ITEM;
    }

    @Override
    protected SoundEvent getHurtSound(DamageSource source) {
        return SoundEvents.ENTITY_ALLAY_HURT;
    }

    @Override
    protected SoundEvent getDeathSound() {
        return SoundEvents.ENTITY_ALLAY_DEATH;
    }

    @Override
    protected float getSoundVolume() {
        return 0.4f;
    }

    @Override
    protected void mobTick(ServerWorld world) {
        Profiler profiler = Profilers.get();
        profiler.push("allayBrain");
        this.getBrain().tick(world, this);
        profiler.pop();
        profiler.push("allayActivityUpdate");
        AllayBrain.updateActivities(this);
        profiler.pop();
        super.mobTick(world);
    }

    @Override
    public void tickMovement() {
        super.tickMovement();
        if (!this.getEntityWorld().isClient() && this.isAlive() && this.age % 10 == 0) {
            this.heal(1.0f);
        }
        if (this.isDancing() && this.shouldStopDancing() && this.age % 20 == 0) {
            this.setDancing(false);
            this.jukeboxPos = null;
        }
        this.tickDuplicationCooldown();
    }

    @Override
    public void tick() {
        super.tick();
        if (this.getEntityWorld().isClient()) {
            this.lastItemHoldAnimationTicks = this.itemHoldAnimationTicks;
            this.itemHoldAnimationTicks = this.isHoldingItem() ? MathHelper.clamp(this.itemHoldAnimationTicks + 1.0f, 0.0f, 5.0f) : MathHelper.clamp(this.itemHoldAnimationTicks - 1.0f, 0.0f, 5.0f);
            if (this.isDancing()) {
                this.danceTicks += 1.0f;
                this.lastSpinningAnimationTicks = this.spinningAnimationTicks;
                this.spinningAnimationTicks = this.isSpinning() ? (this.spinningAnimationTicks += 1.0f) : (this.spinningAnimationTicks -= 1.0f);
                this.spinningAnimationTicks = MathHelper.clamp(this.spinningAnimationTicks, 0.0f, 15.0f);
            } else {
                this.danceTicks = 0.0f;
                this.spinningAnimationTicks = 0.0f;
                this.lastSpinningAnimationTicks = 0.0f;
            }
        } else {
            Vibrations.Ticker.tick(this.getEntityWorld(), this.vibrationListenerData, this.vibrationCallback);
            if (this.isPanicking()) {
                this.setDancing(false);
            }
        }
    }

    @Override
    public boolean canPickUpLoot() {
        return !this.isItemPickupCoolingDown() && this.isHoldingItem();
    }

    public boolean isHoldingItem() {
        return !this.getStackInHand(Hand.MAIN_HAND).isEmpty();
    }

    @Override
    protected boolean canDispenserEquipSlot(EquipmentSlot slot) {
        return false;
    }

    private boolean isItemPickupCoolingDown() {
        return this.getBrain().isMemoryInState(MemoryModuleType.ITEM_PICKUP_COOLDOWN_TICKS, MemoryModuleState.VALUE_PRESENT);
    }

    @Override
    protected ActionResult interactMob(PlayerEntity player, Hand hand) {
        ItemStack itemStack = player.getStackInHand(hand);
        ItemStack itemStack2 = this.getStackInHand(Hand.MAIN_HAND);
        if (this.isDancing() && itemStack.isIn(ItemTags.DUPLICATES_ALLAYS) && this.canDuplicate()) {
            this.duplicate();
            this.getEntityWorld().sendEntityStatus(this, (byte)18);
            this.getEntityWorld().playSoundFromEntity(player, this, SoundEvents.BLOCK_AMETHYST_BLOCK_CHIME, SoundCategory.NEUTRAL, 2.0f, 1.0f);
            this.decrementStackUnlessInCreative(player, itemStack);
            return ActionResult.SUCCESS;
        }
        if (itemStack2.isEmpty() && !itemStack.isEmpty()) {
            ItemStack itemStack3 = itemStack.copyWithCount(1);
            this.setStackInHand(Hand.MAIN_HAND, itemStack3);
            this.decrementStackUnlessInCreative(player, itemStack);
            this.getEntityWorld().playSoundFromEntity(player, this, SoundEvents.ENTITY_ALLAY_ITEM_GIVEN, SoundCategory.NEUTRAL, 2.0f, 1.0f);
            this.getBrain().remember(MemoryModuleType.LIKED_PLAYER, player.getUuid());
            return ActionResult.SUCCESS;
        }
        if (!itemStack2.isEmpty() && hand == Hand.MAIN_HAND && itemStack.isEmpty()) {
            this.equipStack(EquipmentSlot.MAINHAND, ItemStack.EMPTY);
            this.getEntityWorld().playSoundFromEntity(player, this, SoundEvents.ENTITY_ALLAY_ITEM_TAKEN, SoundCategory.NEUTRAL, 2.0f, 1.0f);
            this.swingHand(Hand.MAIN_HAND);
            for (ItemStack itemStack4 : this.getInventory().clearToList()) {
                TargetUtil.give(this, itemStack4, this.getEntityPos());
            }
            this.getBrain().forget(MemoryModuleType.LIKED_PLAYER);
            player.giveItemStack(itemStack2);
            return ActionResult.SUCCESS;
        }
        return super.interactMob(player, hand);
    }

    public void updateJukeboxPos(BlockPos jukeboxPos, boolean playing) {
        if (playing) {
            if (!this.isDancing()) {
                this.jukeboxPos = jukeboxPos;
                this.setDancing(true);
            }
        } else if (jukeboxPos.equals(this.jukeboxPos) || this.jukeboxPos == null) {
            this.jukeboxPos = null;
            this.setDancing(false);
        }
    }

    @Override
    public SimpleInventory getInventory() {
        return this.inventory;
    }

    @Override
    protected Vec3i getItemPickUpRangeExpander() {
        return ITEM_PICKUP_RANGE_EXPANDER;
    }

    @Override
    public boolean canGather(ServerWorld world, ItemStack stack) {
        ItemStack itemStack = this.getStackInHand(Hand.MAIN_HAND);
        return !itemStack.isEmpty() && world.getGameRules().getValue(GameRules.DO_MOB_GRIEFING) != false && this.inventory.canInsert(stack) && this.areItemsEqual(itemStack, stack);
    }

    private boolean areItemsEqual(ItemStack stack, ItemStack stack2) {
        return ItemStack.areItemsEqual(stack, stack2) && !this.areDifferentPotions(stack, stack2);
    }

    private boolean areDifferentPotions(ItemStack stack, ItemStack stack2) {
        PotionContentsComponent potionContentsComponent2;
        PotionContentsComponent potionContentsComponent = stack.get(DataComponentTypes.POTION_CONTENTS);
        return !Objects.equals(potionContentsComponent, potionContentsComponent2 = stack2.get(DataComponentTypes.POTION_CONTENTS));
    }

    @Override
    protected void loot(ServerWorld world, ItemEntity itemEntity) {
        InventoryOwner.pickUpItem(world, this, this, itemEntity);
    }

    @Override
    public boolean isFlappingWings() {
        return !this.isOnGround();
    }

    @Override
    public void updateEventHandler(BiConsumer<EntityGameEventHandler<?>, ServerWorld> callback) {
        World world = this.getEntityWorld();
        if (world instanceof ServerWorld) {
            ServerWorld serverWorld = (ServerWorld)world;
            callback.accept(this.gameEventHandler, serverWorld);
            callback.accept(this.jukeboxEventHandler, serverWorld);
        }
    }

    public boolean isDancing() {
        return this.dataTracker.get(DANCING);
    }

    public void setDancing(boolean dancing) {
        if (this.getEntityWorld().isClient() || !this.canActVoluntarily() || dancing && this.isPanicking()) {
            return;
        }
        this.dataTracker.set(DANCING, dancing);
    }

    private boolean shouldStopDancing() {
        return this.jukeboxPos == null || !this.jukeboxPos.isWithinDistance(this.getEntityPos(), (double)GameEvent.JUKEBOX_PLAY.value().notificationRadius()) || !this.getEntityWorld().getBlockState(this.jukeboxPos).isOf(Blocks.JUKEBOX);
    }

    public float getItemHoldAnimationTicks(float tickProgress) {
        return MathHelper.lerp(tickProgress, this.lastItemHoldAnimationTicks, this.itemHoldAnimationTicks) / 5.0f;
    }

    public boolean isSpinning() {
        float f = this.danceTicks % 55.0f;
        return f < 15.0f;
    }

    public float getSpinningAnimationTicks(float tickProgress) {
        return MathHelper.lerp(tickProgress, this.lastSpinningAnimationTicks, this.spinningAnimationTicks) / 15.0f;
    }

    @Override
    public boolean areItemsDifferent(ItemStack stack, ItemStack stack2) {
        return !this.areItemsEqual(stack, stack2);
    }

    @Override
    protected void dropInventory(ServerWorld world) {
        super.dropInventory(world);
        this.inventory.clearToList().forEach(stack -> this.dropStack(world, (ItemStack)stack));
        ItemStack itemStack = this.getEquippedStack(EquipmentSlot.MAINHAND);
        if (!itemStack.isEmpty() && !EnchantmentHelper.hasAnyEnchantmentsWith(itemStack, EnchantmentEffectComponentTypes.PREVENT_EQUIPMENT_DROP)) {
            this.dropStack(world, itemStack);
            this.equipStack(EquipmentSlot.MAINHAND, ItemStack.EMPTY);
        }
    }

    @Override
    public boolean canImmediatelyDespawn(double distanceSquared) {
        return false;
    }

    @Override
    protected void writeCustomData(WriteView view) {
        super.writeCustomData(view);
        this.writeInventory(view);
        view.put("listener", Vibrations.ListenerData.CODEC, this.vibrationListenerData);
        view.putLong("DuplicationCooldown", this.duplicationCooldown);
    }

    @Override
    protected void readCustomData(ReadView view) {
        super.readCustomData(view);
        this.readInventory(view);
        this.vibrationListenerData = view.read("listener", Vibrations.ListenerData.CODEC).orElseGet(Vibrations.ListenerData::new);
        this.setDuplicationCooldown(view.getInt("DuplicationCooldown", 0));
    }

    @Override
    protected boolean shouldFollowLeash() {
        return false;
    }

    private void tickDuplicationCooldown() {
        if (!this.getEntityWorld().isClient() && this.duplicationCooldown > 0L) {
            this.setDuplicationCooldown(this.duplicationCooldown - 1L);
        }
    }

    private void setDuplicationCooldown(long duplicationCooldown) {
        this.duplicationCooldown = duplicationCooldown;
        this.dataTracker.set(CAN_DUPLICATE, duplicationCooldown == 0L);
    }

    private void duplicate() {
        AllayEntity allayEntity = EntityType.ALLAY.create(this.getEntityWorld(), SpawnReason.BREEDING);
        if (allayEntity != null) {
            allayEntity.refreshPositionAfterTeleport(this.getEntityPos());
            allayEntity.setPersistent();
            allayEntity.startDuplicationCooldown();
            this.startDuplicationCooldown();
            this.getEntityWorld().spawnEntity(allayEntity);
        }
    }

    private void startDuplicationCooldown() {
        this.setDuplicationCooldown(6000L);
    }

    private boolean canDuplicate() {
        return this.dataTracker.get(CAN_DUPLICATE);
    }

    private void decrementStackUnlessInCreative(PlayerEntity player, ItemStack stack) {
        stack.decrementUnlessCreative(1, player);
    }

    @Override
    public Vec3d getLeashOffset() {
        return new Vec3d(0.0, (double)this.getStandingEyeHeight() * 0.6, (double)this.getWidth() * 0.1);
    }

    @Override
    public void handleStatus(byte status) {
        if (status == 18) {
            for (int i = 0; i < 3; ++i) {
                this.addHeartParticle();
            }
        } else {
            super.handleStatus(status);
        }
    }

    private void addHeartParticle() {
        double d = this.random.nextGaussian() * 0.02;
        double e = this.random.nextGaussian() * 0.02;
        double f = this.random.nextGaussian() * 0.02;
        this.getEntityWorld().addParticleClient(ParticleTypes.HEART, this.getParticleX(1.0), this.getRandomBodyY() + 0.5, this.getParticleZ(1.0), d, e, f);
    }

    @Override
    public Vibrations.ListenerData getVibrationListenerData() {
        return this.vibrationListenerData;
    }

    @Override
    public Vibrations.Callback getVibrationCallback() {
        return this.vibrationCallback;
    }

    class VibrationCallback
    implements Vibrations.Callback {
        private static final int RANGE = 16;
        private final PositionSource positionSource;

        VibrationCallback() {
            this.positionSource = new EntityPositionSource(AllayEntity.this, AllayEntity.this.getStandingEyeHeight());
        }

        @Override
        public int getRange() {
            return 16;
        }

        @Override
        public PositionSource getPositionSource() {
            return this.positionSource;
        }

        @Override
        public boolean accepts(ServerWorld world, BlockPos pos, RegistryEntry<GameEvent> event, GameEvent.Emitter emitter) {
            if (AllayEntity.this.isAiDisabled()) {
                return false;
            }
            Optional<GlobalPos> optional = AllayEntity.this.getBrain().getOptionalRegisteredMemory(MemoryModuleType.LIKED_NOTEBLOCK);
            if (optional.isEmpty()) {
                return true;
            }
            GlobalPos globalPos = optional.get();
            return globalPos.isWithinRange(world.getRegistryKey(), AllayEntity.this.getBlockPos(), 1024) && globalPos.pos().equals(pos);
        }

        @Override
        public void accept(ServerWorld world, BlockPos pos, RegistryEntry<GameEvent> event, @Nullable Entity sourceEntity, @Nullable Entity entity, float distance) {
            if (event.matches(GameEvent.NOTE_BLOCK_PLAY)) {
                AllayBrain.rememberNoteBlock(AllayEntity.this, new BlockPos(pos));
            }
        }

        @Override
        public TagKey<GameEvent> getTag() {
            return GameEventTags.ALLAY_CAN_LISTEN;
        }
    }

    class JukeboxEventListener
    implements GameEventListener {
        private final PositionSource positionSource;
        private final int range;

        public JukeboxEventListener(PositionSource positionSource, int range) {
            this.positionSource = positionSource;
            this.range = range;
        }

        @Override
        public PositionSource getPositionSource() {
            return this.positionSource;
        }

        @Override
        public int getRange() {
            return this.range;
        }

        @Override
        public boolean listen(ServerWorld world, RegistryEntry<GameEvent> event, GameEvent.Emitter emitter, Vec3d emitterPos) {
            if (event.matches(GameEvent.JUKEBOX_PLAY)) {
                AllayEntity.this.updateJukeboxPos(BlockPos.ofFloored(emitterPos), true);
                return true;
            }
            if (event.matches(GameEvent.JUKEBOX_STOP_PLAY)) {
                AllayEntity.this.updateJukeboxPos(BlockPos.ofFloored(emitterPos), false);
                return true;
            }
            return false;
        }
    }
}


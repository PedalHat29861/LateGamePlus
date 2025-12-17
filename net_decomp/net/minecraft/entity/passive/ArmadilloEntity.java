/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  com.mojang.serialization.Codec
 *  com.mojang.serialization.Dynamic
 *  io.netty.buffer.ByteBuf
 *  org.jspecify.annotations.Nullable
 */
package net.minecraft.entity.passive;

import com.mojang.serialization.Codec;
import com.mojang.serialization.Dynamic;
import io.netty.buffer.ByteBuf;
import java.util.function.IntFunction;
import net.minecraft.block.BlockState;
import net.minecraft.entity.AnimationState;
import net.minecraft.entity.Entity;
import net.minecraft.entity.EntityType;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.SpawnReason;
import net.minecraft.entity.ai.brain.Brain;
import net.minecraft.entity.ai.brain.MemoryModuleType;
import net.minecraft.entity.ai.control.BodyControl;
import net.minecraft.entity.attribute.DefaultAttributeContainer;
import net.minecraft.entity.attribute.EntityAttributes;
import net.minecraft.entity.damage.DamageSource;
import net.minecraft.entity.data.DataTracker;
import net.minecraft.entity.data.TrackedData;
import net.minecraft.entity.data.TrackedDataHandlerRegistry;
import net.minecraft.entity.passive.AnimalEntity;
import net.minecraft.entity.passive.ArmadilloBrain;
import net.minecraft.entity.passive.PassiveEntity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.ItemStack;
import net.minecraft.item.Items;
import net.minecraft.loot.LootTables;
import net.minecraft.network.codec.PacketCodec;
import net.minecraft.network.codec.PacketCodecs;
import net.minecraft.registry.tag.BlockTags;
import net.minecraft.registry.tag.DamageTypeTags;
import net.minecraft.registry.tag.EntityTypeTags;
import net.minecraft.registry.tag.ItemTags;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.sound.SoundEvent;
import net.minecraft.sound.SoundEvents;
import net.minecraft.storage.ReadView;
import net.minecraft.storage.WriteView;
import net.minecraft.util.ActionResult;
import net.minecraft.util.Hand;
import net.minecraft.util.StringIdentifiable;
import net.minecraft.util.TimeHelper;
import net.minecraft.util.function.ValueLists;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.random.Random;
import net.minecraft.util.profiler.Profiler;
import net.minecraft.util.profiler.Profilers;
import net.minecraft.world.World;
import net.minecraft.world.WorldAccess;
import net.minecraft.world.event.GameEvent;
import org.jspecify.annotations.Nullable;

public class ArmadilloEntity
extends AnimalEntity {
    public static final float field_47778 = 0.6f;
    public static final float field_48332 = 32.5f;
    public static final int field_47779 = 80;
    private static final double field_48333 = 7.0;
    private static final double field_48334 = 2.0;
    private static final TrackedData<State> STATE = DataTracker.registerData(ArmadilloEntity.class, TrackedDataHandlerRegistry.ARMADILLO_STATE);
    private long currentStateTicks = 0L;
    public final AnimationState unrollingAnimationState = new AnimationState();
    public final AnimationState rollingAnimationState = new AnimationState();
    public final AnimationState scaredAnimationState = new AnimationState();
    private int nextScuteShedCooldown;
    private boolean peeking = false;

    public ArmadilloEntity(EntityType<? extends AnimalEntity> entityType, World world) {
        super(entityType, world);
        this.getNavigation().setCanSwim(true);
        this.nextScuteShedCooldown = this.getNextScuteShedCooldown();
    }

    @Override
    public @Nullable PassiveEntity createChild(ServerWorld world, PassiveEntity entity) {
        return EntityType.ARMADILLO.create(world, SpawnReason.BREEDING);
    }

    public static DefaultAttributeContainer.Builder createArmadilloAttributes() {
        return AnimalEntity.createAnimalAttributes().add(EntityAttributes.MAX_HEALTH, 12.0).add(EntityAttributes.MOVEMENT_SPEED, 0.14);
    }

    @Override
    protected void initDataTracker(DataTracker.Builder builder) {
        super.initDataTracker(builder);
        builder.add(STATE, State.IDLE);
    }

    public boolean isNotIdle() {
        return this.dataTracker.get(STATE) != State.IDLE;
    }

    public boolean isRolledUp() {
        return this.getState().isRolledUp(this.currentStateTicks);
    }

    public boolean shouldSwitchToScaredState() {
        return this.getState() == State.ROLLING && this.currentStateTicks > (long)State.ROLLING.getLengthInTicks();
    }

    public State getState() {
        return this.dataTracker.get(STATE);
    }

    public void setState(State state) {
        this.dataTracker.set(STATE, state);
    }

    @Override
    public void onTrackedDataSet(TrackedData<?> data) {
        if (STATE.equals(data)) {
            this.currentStateTicks = 0L;
        }
        super.onTrackedDataSet(data);
    }

    protected Brain.Profile<ArmadilloEntity> createBrainProfile() {
        return ArmadilloBrain.createBrainProfile();
    }

    @Override
    protected Brain<?> deserializeBrain(Dynamic<?> dynamic) {
        return ArmadilloBrain.create(this.createBrainProfile().deserialize(dynamic));
    }

    @Override
    protected void mobTick(ServerWorld world) {
        Profiler profiler = Profilers.get();
        profiler.push("armadilloBrain");
        this.brain.tick(world, this);
        profiler.pop();
        profiler.push("armadilloActivityUpdate");
        ArmadilloBrain.updateActivities(this);
        profiler.pop();
        if (this.isAlive() && --this.nextScuteShedCooldown <= 0 && this.shouldDropLoot(world)) {
            if (this.forEachGiftedItem(world, LootTables.ARMADILLO_SHED_GAMEPLAY, this::dropStack)) {
                this.playSound(SoundEvents.ENTITY_ARMADILLO_SCUTE_DROP, 1.0f, (this.random.nextFloat() - this.random.nextFloat()) * 0.2f + 1.0f);
                this.emitGameEvent(GameEvent.ENTITY_PLACE);
            }
            this.nextScuteShedCooldown = this.getNextScuteShedCooldown();
        }
        super.mobTick(world);
    }

    private int getNextScuteShedCooldown() {
        return this.random.nextInt(20 * TimeHelper.MINUTE_IN_SECONDS * 5) + 20 * TimeHelper.MINUTE_IN_SECONDS * 5;
    }

    @Override
    public void tick() {
        super.tick();
        if (this.getEntityWorld().isClient()) {
            this.updateAnimationStates();
        }
        if (this.isNotIdle()) {
            this.clampHeadYaw();
        }
        ++this.currentStateTicks;
    }

    @Override
    public float getScaleFactor() {
        return this.isBaby() ? 0.6f : 1.0f;
    }

    private void updateAnimationStates() {
        switch (this.getState().ordinal()) {
            case 0: {
                this.unrollingAnimationState.stop();
                this.rollingAnimationState.stop();
                this.scaredAnimationState.stop();
                break;
            }
            case 3: {
                this.unrollingAnimationState.startIfNotRunning(this.age);
                this.rollingAnimationState.stop();
                this.scaredAnimationState.stop();
                break;
            }
            case 1: {
                this.unrollingAnimationState.stop();
                this.rollingAnimationState.startIfNotRunning(this.age);
                this.scaredAnimationState.stop();
                break;
            }
            case 2: {
                this.unrollingAnimationState.stop();
                this.rollingAnimationState.stop();
                if (this.peeking) {
                    this.scaredAnimationState.stop();
                    this.peeking = false;
                }
                if (this.currentStateTicks == 0L) {
                    this.scaredAnimationState.start(this.age);
                    this.scaredAnimationState.skip(State.SCARED.getLengthInTicks(), 1.0f);
                    break;
                }
                this.scaredAnimationState.startIfNotRunning(this.age);
            }
        }
    }

    @Override
    public void handleStatus(byte status) {
        if (status == 64 && this.getEntityWorld().isClient()) {
            this.peeking = true;
            this.getEntityWorld().playSoundClient(this.getX(), this.getY(), this.getZ(), SoundEvents.ENTITY_ARMADILLO_PEEK, this.getSoundCategory(), 1.0f, 1.0f, false);
        } else {
            super.handleStatus(status);
        }
    }

    @Override
    public boolean isBreedingItem(ItemStack stack) {
        return stack.isIn(ItemTags.ARMADILLO_FOOD);
    }

    public static boolean canSpawn(EntityType<ArmadilloEntity> entityType, WorldAccess world, SpawnReason spawnReason, BlockPos pos, Random random) {
        return world.getBlockState(pos.down()).isIn(BlockTags.ARMADILLO_SPAWNABLE_ON) && ArmadilloEntity.isLightLevelValidForNaturalSpawn(world, pos);
    }

    public boolean isEntityThreatening(LivingEntity entity) {
        if (!this.getBoundingBox().expand(7.0, 2.0, 7.0).intersects(entity.getBoundingBox())) {
            return false;
        }
        if (entity.getType().isIn(EntityTypeTags.UNDEAD)) {
            return true;
        }
        if (this.getAttacker() == entity) {
            return true;
        }
        if (entity instanceof PlayerEntity) {
            PlayerEntity playerEntity = (PlayerEntity)entity;
            if (playerEntity.isSpectator()) {
                return false;
            }
            return playerEntity.isSprinting() || playerEntity.hasVehicle();
        }
        return false;
    }

    @Override
    protected void writeCustomData(WriteView view) {
        super.writeCustomData(view);
        view.put("state", State.CODEC, this.getState());
        view.putInt("scute_time", this.nextScuteShedCooldown);
    }

    @Override
    protected void readCustomData(ReadView view) {
        super.readCustomData(view);
        this.setState(view.read("state", State.CODEC).orElse(State.IDLE));
        view.getOptionalInt("scute_time").ifPresent(scuteTime -> {
            this.nextScuteShedCooldown = scuteTime;
        });
    }

    public void startRolling() {
        if (this.isNotIdle()) {
            return;
        }
        this.stopMovement();
        this.resetLoveTicks();
        this.emitGameEvent(GameEvent.ENTITY_ACTION);
        this.playSound(SoundEvents.ENTITY_ARMADILLO_ROLL);
        this.setState(State.ROLLING);
    }

    public void unroll() {
        if (!this.isNotIdle()) {
            return;
        }
        this.emitGameEvent(GameEvent.ENTITY_ACTION);
        this.playSound(SoundEvents.ENTITY_ARMADILLO_UNROLL_FINISH);
        this.setState(State.IDLE);
    }

    @Override
    public boolean damage(ServerWorld world, DamageSource source, float amount) {
        if (this.isNotIdle()) {
            amount = (amount - 1.0f) / 2.0f;
        }
        return super.damage(world, source, amount);
    }

    @Override
    protected void applyDamage(ServerWorld world, DamageSource source, float amount) {
        super.applyDamage(world, source, amount);
        if (this.isAiDisabled() || this.isDead()) {
            return;
        }
        if (source.getAttacker() instanceof LivingEntity) {
            this.getBrain().remember(MemoryModuleType.DANGER_DETECTED_RECENTLY, true, 80L);
            if (this.canRollUp()) {
                this.startRolling();
            }
        } else if (source.isIn(DamageTypeTags.PANIC_ENVIRONMENTAL_CAUSES)) {
            this.unroll();
        }
    }

    @Override
    public ActionResult interactMob(PlayerEntity player, Hand hand) {
        ItemStack itemStack = player.getStackInHand(hand);
        if (itemStack.isOf(Items.BRUSH) && this.brushScute(player, itemStack)) {
            itemStack.damage(16, (LivingEntity)player, hand.getEquipmentSlot());
            return ActionResult.SUCCESS;
        }
        if (this.isNotIdle()) {
            return ActionResult.FAIL;
        }
        return super.interactMob(player, hand);
    }

    public boolean brushScute(@Nullable Entity interactingEntity, ItemStack tool) {
        if (this.isBaby()) {
            return false;
        }
        World world = this.getEntityWorld();
        if (world instanceof ServerWorld) {
            ServerWorld serverWorld = (ServerWorld)world;
            this.forEachBrushedItem(serverWorld, LootTables.ARMADILLO_BRUSH, interactingEntity, tool, this::dropStack);
            this.playSoundIfNotSilent(SoundEvents.ENTITY_ARMADILLO_BRUSH);
            this.emitGameEvent(GameEvent.ENTITY_INTERACT);
        }
        return true;
    }

    public boolean canRollUp() {
        return !this.isPanicking() && !this.isInFluid() && !this.isLeashed() && !this.hasVehicle() && !this.hasPassengers();
    }

    @Override
    public boolean canEat() {
        return super.canEat() && !this.isNotIdle();
    }

    @Override
    protected SoundEvent getAmbientSound() {
        if (this.isNotIdle()) {
            return null;
        }
        return SoundEvents.ENTITY_ARMADILLO_AMBIENT;
    }

    @Override
    protected void playEatSound() {
        this.playSound(SoundEvents.ENTITY_ARMADILLO_EAT);
    }

    @Override
    protected SoundEvent getDeathSound() {
        return SoundEvents.ENTITY_ARMADILLO_DEATH;
    }

    @Override
    protected SoundEvent getHurtSound(DamageSource source) {
        if (this.isNotIdle()) {
            return SoundEvents.ENTITY_ARMADILLO_HURT_REDUCED;
        }
        return SoundEvents.ENTITY_ARMADILLO_HURT;
    }

    @Override
    protected void playStepSound(BlockPos pos, BlockState state) {
        this.playSound(SoundEvents.ENTITY_ARMADILLO_STEP, 0.15f, 1.0f);
    }

    @Override
    public int getMaxHeadRotation() {
        if (this.isNotIdle()) {
            return 0;
        }
        return 32;
    }

    @Override
    protected BodyControl createBodyControl() {
        return new BodyControl(this){

            @Override
            public void tick() {
                if (!ArmadilloEntity.this.isNotIdle()) {
                    super.tick();
                }
            }
        };
    }

    public static abstract sealed class State
    extends Enum<State>
    implements StringIdentifiable {
        public static final /* enum */ State IDLE = new State("idle", false, 0, 0){

            @Override
            public boolean isRolledUp(long currentStateTicks) {
                return false;
            }
        };
        public static final /* enum */ State ROLLING = new State("rolling", true, 10, 1){

            @Override
            public boolean isRolledUp(long currentStateTicks) {
                return currentStateTicks > 5L;
            }
        };
        public static final /* enum */ State SCARED = new State("scared", true, 50, 2){

            @Override
            public boolean isRolledUp(long currentStateTicks) {
                return true;
            }
        };
        public static final /* enum */ State UNROLLING = new State("unrolling", true, 30, 3){

            @Override
            public boolean isRolledUp(long currentStateTicks) {
                return currentStateTicks < 26L;
            }
        };
        static final Codec<State> CODEC;
        private static final IntFunction<State> INDEX_TO_VALUE;
        public static final PacketCodec<ByteBuf, State> PACKET_CODEC;
        private final String name;
        private final boolean runRollUpTask;
        private final int lengthInTicks;
        private final int index;
        private static final /* synthetic */ State[] field_47795;

        public static State[] values() {
            return (State[])field_47795.clone();
        }

        public static State valueOf(String string) {
            return Enum.valueOf(State.class, string);
        }

        State(String name, boolean runRollUpTask, int lengthInTicks, int index) {
            this.name = name;
            this.runRollUpTask = runRollUpTask;
            this.lengthInTicks = lengthInTicks;
            this.index = index;
        }

        @Override
        public String asString() {
            return this.name;
        }

        private int getIndex() {
            return this.index;
        }

        public abstract boolean isRolledUp(long var1);

        public boolean shouldRunRollUpTask() {
            return this.runRollUpTask;
        }

        public int getLengthInTicks() {
            return this.lengthInTicks;
        }

        private static /* synthetic */ State[] method_55726() {
            return new State[]{IDLE, ROLLING, SCARED, UNROLLING};
        }

        static {
            field_47795 = State.method_55726();
            CODEC = StringIdentifiable.createCodec(State::values);
            INDEX_TO_VALUE = ValueLists.createIndexToValueFunction(State::getIndex, State.values(), ValueLists.OutOfBoundsHandling.ZERO);
            PACKET_CODEC = PacketCodecs.indexed(INDEX_TO_VALUE, State::getIndex);
        }
    }
}


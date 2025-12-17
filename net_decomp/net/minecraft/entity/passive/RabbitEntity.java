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
import java.util.function.IntFunction;
import net.minecraft.block.Block;
import net.minecraft.block.BlockState;
import net.minecraft.block.Blocks;
import net.minecraft.block.CarrotsBlock;
import net.minecraft.component.ComponentType;
import net.minecraft.component.ComponentsAccess;
import net.minecraft.component.DataComponentTypes;
import net.minecraft.entity.EntityData;
import net.minecraft.entity.EntityType;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.SpawnReason;
import net.minecraft.entity.ai.control.JumpControl;
import net.minecraft.entity.ai.control.MoveControl;
import net.minecraft.entity.ai.goal.ActiveTargetGoal;
import net.minecraft.entity.ai.goal.AnimalMateGoal;
import net.minecraft.entity.ai.goal.FleeEntityGoal;
import net.minecraft.entity.ai.goal.LookAtEntityGoal;
import net.minecraft.entity.ai.goal.MeleeAttackGoal;
import net.minecraft.entity.ai.goal.MoveToTargetPosGoal;
import net.minecraft.entity.ai.goal.PowderSnowJumpGoal;
import net.minecraft.entity.ai.goal.RevengeGoal;
import net.minecraft.entity.ai.goal.SwimGoal;
import net.minecraft.entity.ai.goal.TemptGoal;
import net.minecraft.entity.ai.goal.WanderAroundFarGoal;
import net.minecraft.entity.ai.pathing.Path;
import net.minecraft.entity.attribute.DefaultAttributeContainer;
import net.minecraft.entity.attribute.EntityAttributeModifier;
import net.minecraft.entity.attribute.EntityAttributes;
import net.minecraft.entity.damage.DamageSource;
import net.minecraft.entity.data.DataTracker;
import net.minecraft.entity.data.TrackedData;
import net.minecraft.entity.data.TrackedDataHandlerRegistry;
import net.minecraft.entity.mob.HostileEntity;
import net.minecraft.entity.mob.MobEntity;
import net.minecraft.entity.passive.AnimalEntity;
import net.minecraft.entity.passive.PassiveEntity;
import net.minecraft.entity.passive.WolfEntity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.ItemStack;
import net.minecraft.network.codec.PacketCodec;
import net.minecraft.network.codec.PacketCodecs;
import net.minecraft.registry.entry.RegistryEntry;
import net.minecraft.registry.tag.BiomeTags;
import net.minecraft.registry.tag.BlockTags;
import net.minecraft.registry.tag.ItemTags;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.sound.SoundCategory;
import net.minecraft.sound.SoundEvent;
import net.minecraft.sound.SoundEvents;
import net.minecraft.storage.ReadView;
import net.minecraft.storage.WriteView;
import net.minecraft.text.Text;
import net.minecraft.util.Identifier;
import net.minecraft.util.StringIdentifiable;
import net.minecraft.util.Util;
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

public class RabbitEntity
extends AnimalEntity {
    public static final double field_30356 = 0.6;
    public static final double field_30357 = 0.8;
    public static final double field_30358 = 1.0;
    public static final double ESCAPE_DANGER_SPEED = 2.2;
    public static final double MELEE_ATTACK_SPEED = 1.4;
    private static final TrackedData<Integer> VARIANT = DataTracker.registerData(RabbitEntity.class, TrackedDataHandlerRegistry.INTEGER);
    private static final int DEFAULT_MORE_CARROT_TICKS = 0;
    private static final Identifier KILLER_BUNNY = Identifier.ofVanilla("killer_bunny");
    private static final int field_51585 = 3;
    private static final int field_51586 = 5;
    private static final Identifier KILLER_BUNNY_ATTACK_DAMAGE_MODIFIER_ID = Identifier.ofVanilla("evil");
    private static final int field_30369 = 8;
    private static final int field_30370 = 40;
    private int jumpTicks;
    private int jumpDuration;
    private boolean lastOnGround;
    private int ticksUntilJump;
    int moreCarrotTicks = 0;

    public RabbitEntity(EntityType<? extends RabbitEntity> entityType, World world) {
        super((EntityType<? extends AnimalEntity>)entityType, world);
        this.jumpControl = new RabbitJumpControl(this);
        this.moveControl = new RabbitMoveControl(this);
        this.setSpeed(0.0);
    }

    @Override
    protected void initGoals() {
        this.goalSelector.add(1, new SwimGoal(this));
        this.goalSelector.add(1, new PowderSnowJumpGoal(this, this.getEntityWorld()));
        this.goalSelector.add(1, new EscapeDangerGoal(this, 2.2));
        this.goalSelector.add(2, new AnimalMateGoal(this, 0.8));
        this.goalSelector.add(3, new TemptGoal(this, 1.0, stack -> stack.isIn(ItemTags.RABBIT_FOOD), false));
        this.goalSelector.add(4, new FleeGoal<PlayerEntity>(this, PlayerEntity.class, 8.0f, 2.2, 2.2));
        this.goalSelector.add(4, new FleeGoal<WolfEntity>(this, WolfEntity.class, 10.0f, 2.2, 2.2));
        this.goalSelector.add(4, new FleeGoal<HostileEntity>(this, HostileEntity.class, 4.0f, 2.2, 2.2));
        this.goalSelector.add(5, new EatCarrotCropGoal(this));
        this.goalSelector.add(6, new WanderAroundFarGoal(this, 0.6));
        this.goalSelector.add(11, new LookAtEntityGoal(this, PlayerEntity.class, 10.0f));
    }

    @Override
    protected float getJumpVelocity() {
        Path path;
        float f = 0.3f;
        if (this.moveControl.getSpeed() <= 0.6) {
            f = 0.2f;
        }
        if ((path = this.navigation.getCurrentPath()) != null && !path.isFinished()) {
            Vec3d vec3d = path.getNodePosition(this);
            if (vec3d.y > this.getY() + 0.5) {
                f = 0.5f;
            }
        }
        if (this.horizontalCollision || this.jumping && this.moveControl.getTargetY() > this.getY() + 0.5) {
            f = 0.5f;
        }
        return super.getJumpVelocity(f / 0.42f);
    }

    @Override
    public void jump() {
        double e;
        super.jump();
        double d = this.moveControl.getSpeed();
        if (d > 0.0 && (e = this.getVelocity().horizontalLengthSquared()) < 0.01) {
            this.updateVelocity(0.1f, new Vec3d(0.0, 0.0, 1.0));
        }
        if (!this.getEntityWorld().isClient()) {
            this.getEntityWorld().sendEntityStatus(this, (byte)1);
        }
    }

    public float getJumpProgress(float tickProgress) {
        if (this.jumpDuration == 0) {
            return 0.0f;
        }
        return ((float)this.jumpTicks + tickProgress) / (float)this.jumpDuration;
    }

    public void setSpeed(double speed) {
        this.getNavigation().setSpeed(speed);
        this.moveControl.moveTo(this.moveControl.getTargetX(), this.moveControl.getTargetY(), this.moveControl.getTargetZ(), speed);
    }

    @Override
    public void setJumping(boolean jumping) {
        super.setJumping(jumping);
        if (jumping) {
            this.playSound(this.getJumpSound(), this.getSoundVolume(), ((this.random.nextFloat() - this.random.nextFloat()) * 0.2f + 1.0f) * 0.8f);
        }
    }

    public void startJump() {
        this.setJumping(true);
        this.jumpDuration = 10;
        this.jumpTicks = 0;
    }

    @Override
    protected void initDataTracker(DataTracker.Builder builder) {
        super.initDataTracker(builder);
        builder.add(VARIANT, Variant.DEFAULT.index);
    }

    @Override
    public void mobTick(ServerWorld world) {
        if (this.ticksUntilJump > 0) {
            --this.ticksUntilJump;
        }
        if (this.moreCarrotTicks > 0) {
            this.moreCarrotTicks -= this.random.nextInt(3);
            if (this.moreCarrotTicks < 0) {
                this.moreCarrotTicks = 0;
            }
        }
        if (this.isOnGround()) {
            RabbitJumpControl rabbitJumpControl;
            LivingEntity livingEntity;
            if (!this.lastOnGround) {
                this.setJumping(false);
                this.scheduleJump();
            }
            if (this.getVariant() == Variant.EVIL && this.ticksUntilJump == 0 && (livingEntity = this.getTarget()) != null && this.squaredDistanceTo(livingEntity) < 16.0) {
                this.lookTowards(livingEntity.getX(), livingEntity.getZ());
                this.moveControl.moveTo(livingEntity.getX(), livingEntity.getY(), livingEntity.getZ(), this.moveControl.getSpeed());
                this.startJump();
                this.lastOnGround = true;
            }
            if (!(rabbitJumpControl = (RabbitJumpControl)this.jumpControl).isActive()) {
                if (this.moveControl.isMoving() && this.ticksUntilJump == 0) {
                    Path path = this.navigation.getCurrentPath();
                    Vec3d vec3d = new Vec3d(this.moveControl.getTargetX(), this.moveControl.getTargetY(), this.moveControl.getTargetZ());
                    if (path != null && !path.isFinished()) {
                        vec3d = path.getNodePosition(this);
                    }
                    this.lookTowards(vec3d.x, vec3d.z);
                    this.startJump();
                }
            } else if (!rabbitJumpControl.canJump()) {
                this.enableJump();
            }
        }
        this.lastOnGround = this.isOnGround();
    }

    @Override
    public boolean shouldSpawnSprintingParticles() {
        return false;
    }

    private void lookTowards(double x, double z) {
        this.setYaw((float)(MathHelper.atan2(z - this.getZ(), x - this.getX()) * 57.2957763671875) - 90.0f);
    }

    private void enableJump() {
        ((RabbitJumpControl)this.jumpControl).setCanJump(true);
    }

    private void disableJump() {
        ((RabbitJumpControl)this.jumpControl).setCanJump(false);
    }

    private void doScheduleJump() {
        this.ticksUntilJump = this.moveControl.getSpeed() < 2.2 ? 10 : 1;
    }

    private void scheduleJump() {
        this.doScheduleJump();
        this.disableJump();
    }

    @Override
    public void tickMovement() {
        super.tickMovement();
        if (this.jumpTicks != this.jumpDuration) {
            ++this.jumpTicks;
        } else if (this.jumpDuration != 0) {
            this.jumpTicks = 0;
            this.jumpDuration = 0;
            this.setJumping(false);
        }
    }

    public static DefaultAttributeContainer.Builder createRabbitAttributes() {
        return AnimalEntity.createAnimalAttributes().add(EntityAttributes.MAX_HEALTH, 3.0).add(EntityAttributes.MOVEMENT_SPEED, 0.3f).add(EntityAttributes.ATTACK_DAMAGE, 3.0);
    }

    @Override
    protected void writeCustomData(WriteView view) {
        super.writeCustomData(view);
        view.put("RabbitType", Variant.INDEX_CODEC, this.getVariant());
        view.putInt("MoreCarrotTicks", this.moreCarrotTicks);
    }

    @Override
    protected void readCustomData(ReadView view) {
        super.readCustomData(view);
        this.setVariant(view.read("RabbitType", Variant.INDEX_CODEC).orElse(Variant.DEFAULT));
        this.moreCarrotTicks = view.getInt("MoreCarrotTicks", 0);
    }

    protected SoundEvent getJumpSound() {
        return SoundEvents.ENTITY_RABBIT_JUMP;
    }

    @Override
    protected SoundEvent getAmbientSound() {
        return SoundEvents.ENTITY_RABBIT_AMBIENT;
    }

    @Override
    protected SoundEvent getHurtSound(DamageSource source) {
        return SoundEvents.ENTITY_RABBIT_HURT;
    }

    @Override
    protected SoundEvent getDeathSound() {
        return SoundEvents.ENTITY_RABBIT_DEATH;
    }

    @Override
    public void playAttackSound() {
        if (this.getVariant() == Variant.EVIL) {
            this.playSound(SoundEvents.ENTITY_RABBIT_ATTACK, 1.0f, (this.random.nextFloat() - this.random.nextFloat()) * 0.2f + 1.0f);
        }
    }

    @Override
    public SoundCategory getSoundCategory() {
        return this.getVariant() == Variant.EVIL ? SoundCategory.HOSTILE : SoundCategory.NEUTRAL;
    }

    /*
     * Unable to fully structure code
     */
    @Override
    public @Nullable RabbitEntity createChild(ServerWorld serverWorld, PassiveEntity passiveEntity) {
        block2: {
            block3: {
                rabbitEntity = EntityType.RABBIT.create(serverWorld, SpawnReason.BREEDING);
                if (rabbitEntity == null) break block2;
                variant = RabbitEntity.getVariantFromPos(serverWorld, this.getBlockPos());
                if (this.random.nextInt(20) == 0) break block3;
                if (!(passiveEntity instanceof RabbitEntity)) ** GOTO lbl-1000
                rabbitEntity2 = (RabbitEntity)passiveEntity;
                if (this.random.nextBoolean()) {
                    variant = rabbitEntity2.getVariant();
                } else lbl-1000:
                // 2 sources

                {
                    variant = this.getVariant();
                }
            }
            rabbitEntity.setVariant(variant);
        }
        return rabbitEntity;
    }

    @Override
    public boolean isBreedingItem(ItemStack stack) {
        return stack.isIn(ItemTags.RABBIT_FOOD);
    }

    public Variant getVariant() {
        return Variant.byIndex(this.dataTracker.get(VARIANT));
    }

    private void setVariant(Variant variant) {
        if (variant == Variant.EVIL) {
            this.getAttributeInstance(EntityAttributes.ARMOR).setBaseValue(8.0);
            this.goalSelector.add(4, new MeleeAttackGoal(this, 1.4, true));
            this.targetSelector.add(1, new RevengeGoal(this, new Class[0]).setGroupRevenge(new Class[0]));
            this.targetSelector.add(2, new ActiveTargetGoal<PlayerEntity>((MobEntity)this, PlayerEntity.class, true));
            this.targetSelector.add(2, new ActiveTargetGoal<WolfEntity>((MobEntity)this, WolfEntity.class, true));
            this.getAttributeInstance(EntityAttributes.ATTACK_DAMAGE).updateModifier(new EntityAttributeModifier(KILLER_BUNNY_ATTACK_DAMAGE_MODIFIER_ID, 5.0, EntityAttributeModifier.Operation.ADD_VALUE));
            if (!this.hasCustomName()) {
                this.setCustomName(Text.translatable(Util.createTranslationKey("entity", KILLER_BUNNY)));
            }
        } else {
            this.getAttributeInstance(EntityAttributes.ATTACK_DAMAGE).removeModifier(KILLER_BUNNY_ATTACK_DAMAGE_MODIFIER_ID);
        }
        this.dataTracker.set(VARIANT, variant.index);
    }

    @Override
    public <T> @Nullable T get(ComponentType<? extends T> type) {
        if (type == DataComponentTypes.RABBIT_VARIANT) {
            return RabbitEntity.castComponentValue(type, this.getVariant());
        }
        return super.get(type);
    }

    @Override
    protected void copyComponentsFrom(ComponentsAccess from) {
        this.copyComponentFrom(from, DataComponentTypes.RABBIT_VARIANT);
        super.copyComponentsFrom(from);
    }

    @Override
    protected <T> boolean setApplicableComponent(ComponentType<T> type, T value) {
        if (type == DataComponentTypes.RABBIT_VARIANT) {
            this.setVariant(RabbitEntity.castComponentValue(DataComponentTypes.RABBIT_VARIANT, value));
            return true;
        }
        return super.setApplicableComponent(type, value);
    }

    @Override
    public @Nullable EntityData initialize(ServerWorldAccess world, LocalDifficulty difficulty, SpawnReason spawnReason, @Nullable EntityData entityData) {
        Variant variant = RabbitEntity.getVariantFromPos(world, this.getBlockPos());
        if (entityData instanceof RabbitData) {
            variant = ((RabbitData)entityData).variant;
        } else {
            entityData = new RabbitData(variant);
        }
        this.setVariant(variant);
        return super.initialize(world, difficulty, spawnReason, entityData);
    }

    private static Variant getVariantFromPos(WorldAccess world, BlockPos pos) {
        RegistryEntry<Biome> registryEntry = world.getBiome(pos);
        int i = world.getRandom().nextInt(100);
        if (registryEntry.isIn(BiomeTags.SPAWNS_WHITE_RABBITS)) {
            return i < 80 ? Variant.WHITE : Variant.WHITE_SPLOTCHED;
        }
        if (registryEntry.isIn(BiomeTags.SPAWNS_GOLD_RABBITS)) {
            return Variant.GOLD;
        }
        return i < 50 ? Variant.BROWN : (i < 90 ? Variant.SALT : Variant.BLACK);
    }

    public static boolean canSpawn(EntityType<RabbitEntity> entity, WorldAccess world, SpawnReason spawnReason, BlockPos pos, Random random) {
        return world.getBlockState(pos.down()).isIn(BlockTags.RABBITS_SPAWNABLE_ON) && RabbitEntity.isLightLevelValidForNaturalSpawn(world, pos);
    }

    boolean wantsCarrots() {
        return this.moreCarrotTicks <= 0;
    }

    @Override
    public void handleStatus(byte status) {
        if (status == 1) {
            this.spawnSprintingParticles();
            this.jumpDuration = 10;
            this.jumpTicks = 0;
        } else {
            super.handleStatus(status);
        }
    }

    @Override
    public Vec3d getLeashOffset() {
        return new Vec3d(0.0, 0.6f * this.getStandingEyeHeight(), this.getWidth() * 0.4f);
    }

    @Override
    public /* synthetic */ @Nullable PassiveEntity createChild(ServerWorld world, PassiveEntity entity) {
        return this.createChild(world, entity);
    }

    public static class RabbitJumpControl
    extends JumpControl {
        private final RabbitEntity rabbit;
        private boolean canJump;

        public RabbitJumpControl(RabbitEntity rabbit) {
            super(rabbit);
            this.rabbit = rabbit;
        }

        public boolean isActive() {
            return this.active;
        }

        public boolean canJump() {
            return this.canJump;
        }

        public void setCanJump(boolean canJump) {
            this.canJump = canJump;
        }

        @Override
        public void tick() {
            if (this.active) {
                this.rabbit.startJump();
                this.active = false;
            }
        }
    }

    static class RabbitMoveControl
    extends MoveControl {
        private final RabbitEntity rabbit;
        private double rabbitSpeed;

        public RabbitMoveControl(RabbitEntity owner) {
            super(owner);
            this.rabbit = owner;
        }

        @Override
        public void tick() {
            if (this.rabbit.isOnGround() && !this.rabbit.jumping && !((RabbitJumpControl)this.rabbit.jumpControl).isActive()) {
                this.rabbit.setSpeed(0.0);
            } else if (this.isMoving() || this.state == MoveControl.State.JUMPING) {
                this.rabbit.setSpeed(this.rabbitSpeed);
            }
            super.tick();
        }

        @Override
        public void moveTo(double x, double y, double z, double speed) {
            if (this.rabbit.isTouchingWater()) {
                speed = 1.5;
            }
            super.moveTo(x, y, z, speed);
            if (speed > 0.0) {
                this.rabbitSpeed = speed;
            }
        }
    }

    static class EscapeDangerGoal
    extends net.minecraft.entity.ai.goal.EscapeDangerGoal {
        private final RabbitEntity rabbit;

        public EscapeDangerGoal(RabbitEntity rabbit, double speed) {
            super(rabbit, speed);
            this.rabbit = rabbit;
        }

        @Override
        public void tick() {
            super.tick();
            this.rabbit.setSpeed(this.speed);
        }
    }

    static class FleeGoal<T extends LivingEntity>
    extends FleeEntityGoal<T> {
        private final RabbitEntity rabbit;

        public FleeGoal(RabbitEntity rabbit, Class<T> fleeFromType, float distance, double slowSpeed, double fastSpeed) {
            super(rabbit, fleeFromType, distance, slowSpeed, fastSpeed);
            this.rabbit = rabbit;
        }

        @Override
        public boolean canStart() {
            return this.rabbit.getVariant() != Variant.EVIL && super.canStart();
        }
    }

    static class EatCarrotCropGoal
    extends MoveToTargetPosGoal {
        private final RabbitEntity rabbit;
        private boolean wantsCarrots;
        private boolean hasTarget;

        public EatCarrotCropGoal(RabbitEntity rabbit) {
            super(rabbit, 0.7f, 16);
            this.rabbit = rabbit;
        }

        @Override
        public boolean canStart() {
            if (this.cooldown <= 0) {
                if (!EatCarrotCropGoal.getServerWorld(this.rabbit).getGameRules().getValue(GameRules.DO_MOB_GRIEFING).booleanValue()) {
                    return false;
                }
                this.hasTarget = false;
                this.wantsCarrots = this.rabbit.wantsCarrots();
            }
            return super.canStart();
        }

        @Override
        public boolean shouldContinue() {
            return this.hasTarget && super.shouldContinue();
        }

        @Override
        public void tick() {
            super.tick();
            this.rabbit.getLookControl().lookAt((double)this.targetPos.getX() + 0.5, this.targetPos.getY() + 1, (double)this.targetPos.getZ() + 0.5, 10.0f, this.rabbit.getMaxLookPitchChange());
            if (this.hasReached()) {
                World world = this.rabbit.getEntityWorld();
                BlockPos blockPos = this.targetPos.up();
                BlockState blockState = world.getBlockState(blockPos);
                Block block = blockState.getBlock();
                if (this.hasTarget && block instanceof CarrotsBlock) {
                    int i = blockState.get(CarrotsBlock.AGE);
                    if (i == 0) {
                        world.setBlockState(blockPos, Blocks.AIR.getDefaultState(), 2);
                        world.breakBlock(blockPos, true, this.rabbit);
                    } else {
                        world.setBlockState(blockPos, (BlockState)blockState.with(CarrotsBlock.AGE, i - 1), 2);
                        world.emitGameEvent(GameEvent.BLOCK_CHANGE, blockPos, GameEvent.Emitter.of(this.rabbit));
                        world.syncWorldEvent(2001, blockPos, Block.getRawIdFromState(blockState));
                    }
                    this.rabbit.moreCarrotTicks = 40;
                }
                this.hasTarget = false;
                this.cooldown = 10;
            }
        }

        @Override
        protected boolean isTargetPos(WorldView world, BlockPos pos) {
            BlockState blockState = world.getBlockState(pos);
            if (blockState.isOf(Blocks.FARMLAND) && this.wantsCarrots && !this.hasTarget && (blockState = world.getBlockState(pos.up())).getBlock() instanceof CarrotsBlock && ((CarrotsBlock)blockState.getBlock()).isMature(blockState)) {
                this.hasTarget = true;
                return true;
            }
            return false;
        }
    }

    public static final class Variant
    extends Enum<Variant>
    implements StringIdentifiable {
        public static final /* enum */ Variant BROWN = new Variant(0, "brown");
        public static final /* enum */ Variant WHITE = new Variant(1, "white");
        public static final /* enum */ Variant BLACK = new Variant(2, "black");
        public static final /* enum */ Variant WHITE_SPLOTCHED = new Variant(3, "white_splotched");
        public static final /* enum */ Variant GOLD = new Variant(4, "gold");
        public static final /* enum */ Variant SALT = new Variant(5, "salt");
        public static final /* enum */ Variant EVIL = new Variant(99, "evil");
        public static final Variant DEFAULT;
        private static final IntFunction<Variant> INDEX_MAPPER;
        public static final Codec<Variant> CODEC;
        @Deprecated
        public static final Codec<Variant> INDEX_CODEC;
        public static final PacketCodec<ByteBuf, Variant> PACKET_CODEC;
        final int index;
        private final String id;
        private static final /* synthetic */ Variant[] field_41572;

        public static Variant[] values() {
            return (Variant[])field_41572.clone();
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

        private static /* synthetic */ Variant[] method_47859() {
            return new Variant[]{BROWN, WHITE, BLACK, WHITE_SPLOTCHED, GOLD, SALT, EVIL};
        }

        static {
            field_41572 = Variant.method_47859();
            DEFAULT = BROWN;
            INDEX_MAPPER = ValueLists.createIndexToValueFunction(Variant::getIndex, Variant.values(), DEFAULT);
            CODEC = StringIdentifiable.createCodec(Variant::values);
            INDEX_CODEC = Codec.INT.xmap(INDEX_MAPPER::apply, Variant::getIndex);
            PACKET_CODEC = PacketCodecs.indexed(INDEX_MAPPER, Variant::getIndex);
        }
    }

    public static class RabbitData
    extends PassiveEntity.PassiveData {
        public final Variant variant;

        public RabbitData(Variant variant) {
            super(1.0f);
            this.variant = variant;
        }
    }
}


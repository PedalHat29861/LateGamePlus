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
import net.minecraft.block.BlockState;
import net.minecraft.block.Blocks;
import net.minecraft.component.ComponentType;
import net.minecraft.component.ComponentsAccess;
import net.minecraft.component.DataComponentTypes;
import net.minecraft.entity.Entity;
import net.minecraft.entity.EntityAttachmentType;
import net.minecraft.entity.EntityAttachments;
import net.minecraft.entity.EntityData;
import net.minecraft.entity.EntityDimensions;
import net.minecraft.entity.EntityPose;
import net.minecraft.entity.EntityType;
import net.minecraft.entity.EquipmentSlot;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.SpawnReason;
import net.minecraft.entity.ai.RangedAttackMob;
import net.minecraft.entity.ai.goal.ActiveTargetGoal;
import net.minecraft.entity.ai.goal.AnimalMateGoal;
import net.minecraft.entity.ai.goal.EscapeDangerGoal;
import net.minecraft.entity.ai.goal.FollowParentGoal;
import net.minecraft.entity.ai.goal.FormCaravanGoal;
import net.minecraft.entity.ai.goal.HorseBondWithPlayerGoal;
import net.minecraft.entity.ai.goal.LookAroundGoal;
import net.minecraft.entity.ai.goal.LookAtEntityGoal;
import net.minecraft.entity.ai.goal.ProjectileAttackGoal;
import net.minecraft.entity.ai.goal.RevengeGoal;
import net.minecraft.entity.ai.goal.SwimGoal;
import net.minecraft.entity.ai.goal.TemptGoal;
import net.minecraft.entity.ai.goal.WanderAroundFarGoal;
import net.minecraft.entity.attribute.DefaultAttributeContainer;
import net.minecraft.entity.damage.DamageSource;
import net.minecraft.entity.data.DataTracker;
import net.minecraft.entity.data.TrackedData;
import net.minecraft.entity.data.TrackedDataHandlerRegistry;
import net.minecraft.entity.mob.MobEntity;
import net.minecraft.entity.passive.AbstractDonkeyEntity;
import net.minecraft.entity.passive.AnimalEntity;
import net.minecraft.entity.passive.PassiveEntity;
import net.minecraft.entity.passive.WolfEntity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.entity.projectile.LlamaSpitEntity;
import net.minecraft.entity.projectile.ProjectileEntity;
import net.minecraft.item.ItemStack;
import net.minecraft.item.Items;
import net.minecraft.network.codec.PacketCodec;
import net.minecraft.network.codec.PacketCodecs;
import net.minecraft.particle.ParticleTypes;
import net.minecraft.registry.tag.ItemTags;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.sound.SoundEvent;
import net.minecraft.sound.SoundEvents;
import net.minecraft.storage.ReadView;
import net.minecraft.storage.WriteView;
import net.minecraft.util.StringIdentifiable;
import net.minecraft.util.Util;
import net.minecraft.util.function.ValueLists;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Vec3d;
import net.minecraft.util.math.random.Random;
import net.minecraft.world.LocalDifficulty;
import net.minecraft.world.ServerWorldAccess;
import net.minecraft.world.World;
import org.jspecify.annotations.Nullable;

public class LlamaEntity
extends AbstractDonkeyEntity
implements RangedAttackMob {
    private static final int MAX_STRENGTH = 5;
    private static final TrackedData<Integer> STRENGTH = DataTracker.registerData(LlamaEntity.class, TrackedDataHandlerRegistry.INTEGER);
    private static final TrackedData<Integer> VARIANT = DataTracker.registerData(LlamaEntity.class, TrackedDataHandlerRegistry.INTEGER);
    private static final EntityDimensions BABY_BASE_DIMENSIONS = EntityType.LLAMA.getDimensions().withAttachments(EntityAttachments.builder().add(EntityAttachmentType.PASSENGER, 0.0f, EntityType.LLAMA.getHeight() - 0.8125f, -0.3f)).scaled(0.5f);
    boolean spit;
    private @Nullable LlamaEntity following;
    private @Nullable LlamaEntity follower;

    public LlamaEntity(EntityType<? extends LlamaEntity> entityType, World world) {
        super((EntityType<? extends AbstractDonkeyEntity>)entityType, world);
        this.getNavigation().setMaxFollowRange(40.0f);
    }

    public boolean isTrader() {
        return false;
    }

    private void setStrength(int strength) {
        this.dataTracker.set(STRENGTH, Math.max(1, Math.min(5, strength)));
    }

    private void initializeStrength(Random random) {
        int i = random.nextFloat() < 0.04f ? 5 : 3;
        this.setStrength(1 + random.nextInt(i));
    }

    public int getStrength() {
        return this.dataTracker.get(STRENGTH);
    }

    @Override
    protected void writeCustomData(WriteView view) {
        super.writeCustomData(view);
        view.put("Variant", Variant.INDEX_CODEC, this.getVariant());
        view.putInt("Strength", this.getStrength());
    }

    @Override
    protected void readCustomData(ReadView view) {
        this.setStrength(view.getInt("Strength", 0));
        super.readCustomData(view);
        this.setVariant(view.read("Variant", Variant.INDEX_CODEC).orElse(Variant.DEFAULT));
    }

    @Override
    protected void initGoals() {
        this.goalSelector.add(0, new SwimGoal(this));
        this.goalSelector.add(1, new HorseBondWithPlayerGoal(this, 1.2));
        this.goalSelector.add(2, new FormCaravanGoal(this, 2.1f));
        this.goalSelector.add(3, new ProjectileAttackGoal(this, 1.25, 40, 20.0f));
        this.goalSelector.add(3, new EscapeDangerGoal(this, 1.2));
        this.goalSelector.add(4, new AnimalMateGoal(this, 1.0));
        this.goalSelector.add(5, new TemptGoal(this, 1.25, stack -> stack.isIn(ItemTags.LLAMA_TEMPT_ITEMS), false));
        this.goalSelector.add(6, new FollowParentGoal(this, 1.0));
        this.goalSelector.add(7, new WanderAroundFarGoal(this, 0.7));
        this.goalSelector.add(8, new LookAtEntityGoal(this, PlayerEntity.class, 6.0f));
        this.goalSelector.add(9, new LookAroundGoal(this));
        this.targetSelector.add(1, new SpitRevengeGoal(this));
        this.targetSelector.add(2, new ChaseWolvesGoal(this));
    }

    public static DefaultAttributeContainer.Builder createLlamaAttributes() {
        return LlamaEntity.createAbstractDonkeyAttributes();
    }

    @Override
    protected void initDataTracker(DataTracker.Builder builder) {
        super.initDataTracker(builder);
        builder.add(STRENGTH, 0);
        builder.add(VARIANT, 0);
    }

    public Variant getVariant() {
        return Variant.byIndex(this.dataTracker.get(VARIANT));
    }

    private void setVariant(Variant variant) {
        this.dataTracker.set(VARIANT, variant.index);
    }

    @Override
    public <T> @Nullable T get(ComponentType<? extends T> type) {
        if (type == DataComponentTypes.LLAMA_VARIANT) {
            return LlamaEntity.castComponentValue(type, this.getVariant());
        }
        return super.get(type);
    }

    @Override
    protected void copyComponentsFrom(ComponentsAccess from) {
        this.copyComponentFrom(from, DataComponentTypes.LLAMA_VARIANT);
        super.copyComponentsFrom(from);
    }

    @Override
    protected <T> boolean setApplicableComponent(ComponentType<T> type, T value) {
        if (type == DataComponentTypes.LLAMA_VARIANT) {
            this.setVariant(LlamaEntity.castComponentValue(DataComponentTypes.LLAMA_VARIANT, value));
            return true;
        }
        return super.setApplicableComponent(type, value);
    }

    @Override
    public boolean isBreedingItem(ItemStack stack) {
        return stack.isIn(ItemTags.LLAMA_FOOD);
    }

    @Override
    protected boolean receiveFood(PlayerEntity player, ItemStack item) {
        SoundEvent soundEvent;
        int i = 0;
        int j = 0;
        float f = 0.0f;
        boolean bl = false;
        if (item.isOf(Items.WHEAT)) {
            i = 10;
            j = 3;
            f = 2.0f;
        } else if (item.isOf(Blocks.HAY_BLOCK.asItem())) {
            i = 90;
            j = 6;
            f = 10.0f;
            if (this.isTame() && this.getBreedingAge() == 0 && this.canEat()) {
                bl = true;
                this.lovePlayer(player);
            }
        }
        if (this.getHealth() < this.getMaxHealth() && f > 0.0f) {
            this.heal(f);
            bl = true;
        }
        if (this.isBaby() && i > 0) {
            this.getEntityWorld().addParticleClient(ParticleTypes.HAPPY_VILLAGER, this.getParticleX(1.0), this.getRandomBodyY() + 0.5, this.getParticleZ(1.0), 0.0, 0.0, 0.0);
            if (!this.getEntityWorld().isClient()) {
                this.growUp(i);
                bl = true;
            }
        }
        if (!(j <= 0 || !bl && this.isTame() || this.getTemper() >= this.getMaxTemper() || this.getEntityWorld().isClient())) {
            this.addTemper(j);
            bl = true;
        }
        if (bl && !this.isSilent() && (soundEvent = this.getEatSound()) != null) {
            this.getEntityWorld().playSound(null, this.getX(), this.getY(), this.getZ(), this.getEatSound(), this.getSoundCategory(), 1.0f, 1.0f + (this.random.nextFloat() - this.random.nextFloat()) * 0.2f);
        }
        return bl;
    }

    @Override
    public boolean isImmobile() {
        return this.isDead() || this.isEatingGrass();
    }

    @Override
    public @Nullable EntityData initialize(ServerWorldAccess world, LocalDifficulty difficulty, SpawnReason spawnReason, @Nullable EntityData entityData) {
        Variant variant;
        Random random = world.getRandom();
        this.initializeStrength(random);
        if (entityData instanceof LlamaData) {
            variant = ((LlamaData)entityData).variant;
        } else {
            variant = Util.getRandom(Variant.values(), random);
            entityData = new LlamaData(variant);
        }
        this.setVariant(variant);
        return super.initialize(world, difficulty, spawnReason, entityData);
    }

    @Override
    protected boolean shouldAmbientStand() {
        return false;
    }

    @Override
    protected SoundEvent getAngrySound() {
        return SoundEvents.ENTITY_LLAMA_ANGRY;
    }

    @Override
    protected SoundEvent getAmbientSound() {
        return SoundEvents.ENTITY_LLAMA_AMBIENT;
    }

    @Override
    protected SoundEvent getHurtSound(DamageSource source) {
        return SoundEvents.ENTITY_LLAMA_HURT;
    }

    @Override
    protected SoundEvent getDeathSound() {
        return SoundEvents.ENTITY_LLAMA_DEATH;
    }

    @Override
    protected SoundEvent getEatSound() {
        return SoundEvents.ENTITY_LLAMA_EAT;
    }

    @Override
    protected void playStepSound(BlockPos pos, BlockState state) {
        this.playSound(SoundEvents.ENTITY_LLAMA_STEP, 0.15f, 1.0f);
    }

    @Override
    protected void playAddChestSound() {
        this.playSound(SoundEvents.ENTITY_LLAMA_CHEST, 1.0f, (this.random.nextFloat() - this.random.nextFloat()) * 0.2f + 1.0f);
    }

    @Override
    public int getInventoryColumns() {
        return this.hasChest() ? this.getStrength() : 0;
    }

    @Override
    public boolean canUseSlot(EquipmentSlot slot) {
        return true;
    }

    @Override
    public int getMaxTemper() {
        return 30;
    }

    @Override
    public boolean canBreedWith(AnimalEntity other) {
        return other != this && other instanceof LlamaEntity && this.canBreed() && ((LlamaEntity)other).canBreed();
    }

    @Override
    public @Nullable LlamaEntity createChild(ServerWorld serverWorld, PassiveEntity passiveEntity) {
        LlamaEntity llamaEntity = this.createChild();
        if (llamaEntity != null) {
            this.setChildAttributes(passiveEntity, llamaEntity);
            LlamaEntity llamaEntity2 = (LlamaEntity)passiveEntity;
            int i = this.random.nextInt(Math.max(this.getStrength(), llamaEntity2.getStrength())) + 1;
            if (this.random.nextFloat() < 0.03f) {
                ++i;
            }
            llamaEntity.setStrength(i);
            llamaEntity.setVariant(this.random.nextBoolean() ? this.getVariant() : llamaEntity2.getVariant());
        }
        return llamaEntity;
    }

    protected @Nullable LlamaEntity createChild() {
        return EntityType.LLAMA.create(this.getEntityWorld(), SpawnReason.BREEDING);
    }

    private void spitAt(LivingEntity target) {
        LlamaSpitEntity llamaSpitEntity = new LlamaSpitEntity(this.getEntityWorld(), this);
        double d = target.getX() - this.getX();
        double e = target.getBodyY(0.3333333333333333) - llamaSpitEntity.getY();
        double f = target.getZ() - this.getZ();
        double g = Math.sqrt(d * d + f * f) * (double)0.2f;
        World world = this.getEntityWorld();
        if (world instanceof ServerWorld) {
            ServerWorld serverWorld = (ServerWorld)world;
            ProjectileEntity.spawnWithVelocity(llamaSpitEntity, serverWorld, ItemStack.EMPTY, d, e + g, f, 1.5f, 10.0f);
        }
        if (!this.isSilent()) {
            this.getEntityWorld().playSound(null, this.getX(), this.getY(), this.getZ(), SoundEvents.ENTITY_LLAMA_SPIT, this.getSoundCategory(), 1.0f, 1.0f + (this.random.nextFloat() - this.random.nextFloat()) * 0.2f);
        }
        this.spit = true;
    }

    void setSpit(boolean spit) {
        this.spit = spit;
    }

    @Override
    public boolean handleFallDamage(double fallDistance, float damagePerDistance, DamageSource damageSource) {
        int i = this.computeFallDamage(fallDistance, damagePerDistance);
        if (i <= 0) {
            return false;
        }
        if (fallDistance >= 6.0) {
            this.serverDamage(damageSource, i);
            this.handleFallDamageForPassengers(fallDistance, damagePerDistance, damageSource);
        }
        this.playBlockFallSound();
        return true;
    }

    public void stopFollowing() {
        if (this.following != null) {
            this.following.follower = null;
        }
        this.following = null;
    }

    public void follow(LlamaEntity llama) {
        this.following = llama;
        this.following.follower = this;
    }

    public boolean hasFollower() {
        return this.follower != null;
    }

    public boolean isFollowing() {
        return this.following != null;
    }

    public @Nullable LlamaEntity getFollowing() {
        return this.following;
    }

    @Override
    protected double getFollowLeashSpeed() {
        return 2.0;
    }

    @Override
    public boolean canUseQuadLeashAttachmentPoint() {
        return false;
    }

    @Override
    protected void walkToParent(ServerWorld world) {
        if (!this.isFollowing() && this.isBaby()) {
            super.walkToParent(world);
        }
    }

    @Override
    public boolean eatsGrass() {
        return false;
    }

    @Override
    public void shootAt(LivingEntity target, float pullProgress) {
        this.spitAt(target);
    }

    @Override
    public Vec3d getLeashOffset() {
        return new Vec3d(0.0, 0.75 * (double)this.getStandingEyeHeight(), (double)this.getWidth() * 0.5);
    }

    @Override
    public EntityDimensions getBaseDimensions(EntityPose pose) {
        return this.isBaby() ? BABY_BASE_DIMENSIONS : super.getBaseDimensions(pose);
    }

    @Override
    protected Vec3d getPassengerAttachmentPos(Entity passenger, EntityDimensions dimensions, float scaleFactor) {
        return LlamaEntity.getPassengerAttachmentPos(this, passenger, dimensions.attachments());
    }

    @Override
    public /* synthetic */ @Nullable PassiveEntity createChild(ServerWorld world, PassiveEntity entity) {
        return this.createChild(world, entity);
    }

    public static final class Variant
    extends Enum<Variant>
    implements StringIdentifiable {
        public static final /* enum */ Variant CREAMY = new Variant(0, "creamy");
        public static final /* enum */ Variant WHITE = new Variant(1, "white");
        public static final /* enum */ Variant BROWN = new Variant(2, "brown");
        public static final /* enum */ Variant GRAY = new Variant(3, "gray");
        public static final Variant DEFAULT;
        private static final IntFunction<Variant> INDEX_MAPPER;
        public static final Codec<Variant> CODEC;
        @Deprecated
        public static final Codec<Variant> INDEX_CODEC;
        public static final PacketCodec<ByteBuf, Variant> PACKET_CODEC;
        final int index;
        private final String id;
        private static final /* synthetic */ Variant[] field_41594;

        public static Variant[] values() {
            return (Variant[])field_41594.clone();
        }

        public static Variant valueOf(String string) {
            return Enum.valueOf(Variant.class, string);
        }

        private Variant(int index, String id) {
            this.index = index;
            this.id = id;
        }

        public int getIndex() {
            return this.index;
        }

        public static Variant byIndex(int index) {
            return INDEX_MAPPER.apply(index);
        }

        @Override
        public String asString() {
            return this.id;
        }

        private static /* synthetic */ Variant[] method_47877() {
            return new Variant[]{CREAMY, WHITE, BROWN, GRAY};
        }

        static {
            field_41594 = Variant.method_47877();
            DEFAULT = CREAMY;
            INDEX_MAPPER = ValueLists.createIndexToValueFunction(Variant::getIndex, Variant.values(), ValueLists.OutOfBoundsHandling.CLAMP);
            CODEC = StringIdentifiable.createCodec(Variant::values);
            INDEX_CODEC = Codec.INT.xmap(INDEX_MAPPER::apply, Variant::getIndex);
            PACKET_CODEC = PacketCodecs.indexed(INDEX_MAPPER, Variant::getIndex);
        }
    }

    static class SpitRevengeGoal
    extends RevengeGoal {
        public SpitRevengeGoal(LlamaEntity llama) {
            super(llama, new Class[0]);
        }

        @Override
        public boolean shouldContinue() {
            MobEntity mobEntity = this.mob;
            if (mobEntity instanceof LlamaEntity) {
                LlamaEntity llamaEntity = (LlamaEntity)mobEntity;
                if (llamaEntity.spit) {
                    llamaEntity.setSpit(false);
                    return false;
                }
            }
            return super.shouldContinue();
        }
    }

    static class ChaseWolvesGoal
    extends ActiveTargetGoal<WolfEntity> {
        public ChaseWolvesGoal(LlamaEntity llama) {
            super(llama, WolfEntity.class, 16, false, true, (wolf, world) -> !((WolfEntity)wolf).isTamed());
        }

        @Override
        protected double getFollowRange() {
            return super.getFollowRange() * 0.25;
        }
    }

    static class LlamaData
    extends PassiveEntity.PassiveData {
        public final Variant variant;

        LlamaData(Variant variant) {
            super(true);
            this.variant = variant;
        }
    }
}


/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  org.jspecify.annotations.Nullable
 */
package net.minecraft.entity.passive;

import java.util.List;
import net.minecraft.block.BlockState;
import net.minecraft.entity.EntityData;
import net.minecraft.entity.EntityDimensions;
import net.minecraft.entity.EntityPose;
import net.minecraft.entity.EntityType;
import net.minecraft.entity.LazyEntityReference;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.SpawnReason;
import net.minecraft.entity.ai.goal.ActiveTargetGoal;
import net.minecraft.entity.ai.goal.EscapeDangerGoal;
import net.minecraft.entity.ai.goal.FollowParentGoal;
import net.minecraft.entity.ai.goal.LookAroundGoal;
import net.minecraft.entity.ai.goal.LookAtEntityGoal;
import net.minecraft.entity.ai.goal.MeleeAttackGoal;
import net.minecraft.entity.ai.goal.RevengeGoal;
import net.minecraft.entity.ai.goal.SwimGoal;
import net.minecraft.entity.ai.goal.UniversalAngerGoal;
import net.minecraft.entity.ai.goal.WanderAroundGoal;
import net.minecraft.entity.attribute.DefaultAttributeContainer;
import net.minecraft.entity.attribute.EntityAttributes;
import net.minecraft.entity.damage.DamageSource;
import net.minecraft.entity.data.DataTracker;
import net.minecraft.entity.data.TrackedData;
import net.minecraft.entity.data.TrackedDataHandlerRegistry;
import net.minecraft.entity.mob.Angerable;
import net.minecraft.entity.mob.MobEntity;
import net.minecraft.entity.mob.PathAwareEntity;
import net.minecraft.entity.passive.AnimalEntity;
import net.minecraft.entity.passive.FoxEntity;
import net.minecraft.entity.passive.PassiveEntity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.ItemStack;
import net.minecraft.registry.entry.RegistryEntry;
import net.minecraft.registry.tag.BiomeTags;
import net.minecraft.registry.tag.BlockTags;
import net.minecraft.registry.tag.DamageTypeTags;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.sound.SoundEvent;
import net.minecraft.sound.SoundEvents;
import net.minecraft.storage.ReadView;
import net.minecraft.storage.WriteView;
import net.minecraft.util.TimeHelper;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.MathHelper;
import net.minecraft.util.math.intprovider.UniformIntProvider;
import net.minecraft.util.math.random.Random;
import net.minecraft.world.LocalDifficulty;
import net.minecraft.world.ServerWorldAccess;
import net.minecraft.world.World;
import net.minecraft.world.WorldAccess;
import net.minecraft.world.biome.Biome;
import org.jspecify.annotations.Nullable;

public class PolarBearEntity
extends AnimalEntity
implements Angerable {
    private static final TrackedData<Boolean> WARNING = DataTracker.registerData(PolarBearEntity.class, TrackedDataHandlerRegistry.BOOLEAN);
    private static final float field_30352 = 6.0f;
    private float lastWarningAnimationProgress;
    private float warningAnimationProgress;
    private int warningSoundCooldown;
    private static final UniformIntProvider ANGER_TIME_RANGE = TimeHelper.betweenSeconds(20, 39);
    private long angerEndTime;
    private @Nullable LazyEntityReference<LivingEntity> angryAt;

    public PolarBearEntity(EntityType<? extends PolarBearEntity> entityType, World world) {
        super((EntityType<? extends AnimalEntity>)entityType, world);
    }

    @Override
    public @Nullable PassiveEntity createChild(ServerWorld world, PassiveEntity entity) {
        return EntityType.POLAR_BEAR.create(world, SpawnReason.BREEDING);
    }

    @Override
    public boolean isBreedingItem(ItemStack stack) {
        return false;
    }

    @Override
    protected void initGoals() {
        super.initGoals();
        this.goalSelector.add(0, new SwimGoal(this));
        this.goalSelector.add(1, new AttackGoal());
        this.goalSelector.add(1, new EscapeDangerGoal((PathAwareEntity)this, 2.0, polarBear -> polarBear.isBaby() ? DamageTypeTags.PANIC_CAUSES : DamageTypeTags.PANIC_ENVIRONMENTAL_CAUSES));
        this.goalSelector.add(4, new FollowParentGoal(this, 1.25));
        this.goalSelector.add(5, new WanderAroundGoal(this, 1.0));
        this.goalSelector.add(6, new LookAtEntityGoal(this, PlayerEntity.class, 6.0f));
        this.goalSelector.add(7, new LookAroundGoal(this));
        this.targetSelector.add(1, new PolarBearRevengeGoal());
        this.targetSelector.add(2, new ProtectBabiesGoal());
        this.targetSelector.add(3, new ActiveTargetGoal<PlayerEntity>(this, PlayerEntity.class, 10, true, false, this::shouldAngerAt));
        this.targetSelector.add(4, new ActiveTargetGoal<FoxEntity>(this, FoxEntity.class, 10, true, true, null));
        this.targetSelector.add(5, new UniversalAngerGoal<PolarBearEntity>(this, false));
    }

    public static DefaultAttributeContainer.Builder createPolarBearAttributes() {
        return AnimalEntity.createAnimalAttributes().add(EntityAttributes.MAX_HEALTH, 30.0).add(EntityAttributes.FOLLOW_RANGE, 20.0).add(EntityAttributes.MOVEMENT_SPEED, 0.25).add(EntityAttributes.ATTACK_DAMAGE, 6.0);
    }

    public static boolean canSpawn(EntityType<PolarBearEntity> type, WorldAccess world, SpawnReason spawnReason, BlockPos pos, Random random) {
        RegistryEntry<Biome> registryEntry = world.getBiome(pos);
        if (registryEntry.isIn(BiomeTags.POLAR_BEARS_SPAWN_ON_ALTERNATE_BLOCKS)) {
            return PolarBearEntity.isLightLevelValidForNaturalSpawn(world, pos) && world.getBlockState(pos.down()).isIn(BlockTags.POLAR_BEARS_SPAWNABLE_ON_ALTERNATE);
        }
        return PolarBearEntity.isValidNaturalSpawn(type, world, spawnReason, pos, random);
    }

    @Override
    protected void readCustomData(ReadView view) {
        super.readCustomData(view);
        this.readAngerFromData(this.getEntityWorld(), view);
    }

    @Override
    protected void writeCustomData(WriteView view) {
        super.writeCustomData(view);
        this.writeAngerToData(view);
    }

    @Override
    public void chooseRandomAngerTime() {
        this.setAngerDuration(ANGER_TIME_RANGE.get(this.random));
    }

    @Override
    public void setAngerEndTime(long angerEndTime) {
        this.angerEndTime = angerEndTime;
    }

    @Override
    public long getAngerEndTime() {
        return this.angerEndTime;
    }

    @Override
    public void setAngryAt(@Nullable LazyEntityReference<LivingEntity> angryAt) {
        this.angryAt = angryAt;
    }

    @Override
    public @Nullable LazyEntityReference<LivingEntity> getAngryAt() {
        return this.angryAt;
    }

    @Override
    protected SoundEvent getAmbientSound() {
        if (this.isBaby()) {
            return SoundEvents.ENTITY_POLAR_BEAR_AMBIENT_BABY;
        }
        return SoundEvents.ENTITY_POLAR_BEAR_AMBIENT;
    }

    @Override
    protected SoundEvent getHurtSound(DamageSource source) {
        return SoundEvents.ENTITY_POLAR_BEAR_HURT;
    }

    @Override
    protected SoundEvent getDeathSound() {
        return SoundEvents.ENTITY_POLAR_BEAR_DEATH;
    }

    @Override
    protected void playStepSound(BlockPos pos, BlockState state) {
        this.playSound(SoundEvents.ENTITY_POLAR_BEAR_STEP, 0.15f, 1.0f);
    }

    protected void playWarningSound() {
        if (this.warningSoundCooldown <= 0) {
            this.playSound(SoundEvents.ENTITY_POLAR_BEAR_WARNING);
            this.warningSoundCooldown = 40;
        }
    }

    @Override
    protected void initDataTracker(DataTracker.Builder builder) {
        super.initDataTracker(builder);
        builder.add(WARNING, false);
    }

    @Override
    public void tick() {
        super.tick();
        if (this.getEntityWorld().isClient()) {
            if (this.warningAnimationProgress != this.lastWarningAnimationProgress) {
                this.calculateDimensions();
            }
            this.lastWarningAnimationProgress = this.warningAnimationProgress;
            this.warningAnimationProgress = this.isWarning() ? MathHelper.clamp(this.warningAnimationProgress + 1.0f, 0.0f, 6.0f) : MathHelper.clamp(this.warningAnimationProgress - 1.0f, 0.0f, 6.0f);
        }
        if (this.warningSoundCooldown > 0) {
            --this.warningSoundCooldown;
        }
        if (!this.getEntityWorld().isClient()) {
            this.tickAngerLogic((ServerWorld)this.getEntityWorld(), true);
        }
    }

    @Override
    public EntityDimensions getBaseDimensions(EntityPose pose) {
        if (this.warningAnimationProgress > 0.0f) {
            float f = this.warningAnimationProgress / 6.0f;
            float g = 1.0f + f;
            return super.getBaseDimensions(pose).scaled(1.0f, g);
        }
        return super.getBaseDimensions(pose);
    }

    public boolean isWarning() {
        return this.dataTracker.get(WARNING);
    }

    public void setWarning(boolean warning) {
        this.dataTracker.set(WARNING, warning);
    }

    public float getWarningAnimationProgress(float tickProgress) {
        return MathHelper.lerp(tickProgress, this.lastWarningAnimationProgress, this.warningAnimationProgress) / 6.0f;
    }

    @Override
    protected float getBaseWaterMovementSpeedMultiplier() {
        return 0.98f;
    }

    @Override
    public EntityData initialize(ServerWorldAccess world, LocalDifficulty difficulty, SpawnReason spawnReason, @Nullable EntityData entityData) {
        if (entityData == null) {
            entityData = new PassiveEntity.PassiveData(1.0f);
        }
        return super.initialize(world, difficulty, spawnReason, entityData);
    }

    class AttackGoal
    extends MeleeAttackGoal {
        public AttackGoal() {
            super(PolarBearEntity.this, 1.25, true);
        }

        @Override
        protected void attack(LivingEntity target) {
            if (this.canAttack(target)) {
                this.resetCooldown();
                this.mob.tryAttack(AttackGoal.getServerWorld(this.mob), target);
                PolarBearEntity.this.setWarning(false);
            } else if (this.mob.squaredDistanceTo(target) < (double)((target.getWidth() + 3.0f) * (target.getWidth() + 3.0f))) {
                if (this.isCooledDown()) {
                    PolarBearEntity.this.setWarning(false);
                    this.resetCooldown();
                }
                if (this.getCooldown() <= 10) {
                    PolarBearEntity.this.setWarning(true);
                    PolarBearEntity.this.playWarningSound();
                }
            } else {
                this.resetCooldown();
                PolarBearEntity.this.setWarning(false);
            }
        }

        @Override
        public void stop() {
            PolarBearEntity.this.setWarning(false);
            super.stop();
        }
    }

    class PolarBearRevengeGoal
    extends RevengeGoal {
        public PolarBearRevengeGoal() {
            super(PolarBearEntity.this, new Class[0]);
        }

        @Override
        public void start() {
            super.start();
            if (PolarBearEntity.this.isBaby()) {
                this.callSameTypeForRevenge();
                this.stop();
            }
        }

        @Override
        protected void setMobEntityTarget(MobEntity mob, LivingEntity target) {
            if (mob instanceof PolarBearEntity && !mob.isBaby()) {
                super.setMobEntityTarget(mob, target);
            }
        }
    }

    class ProtectBabiesGoal
    extends ActiveTargetGoal<PlayerEntity> {
        public ProtectBabiesGoal() {
            super(PolarBearEntity.this, PlayerEntity.class, 20, true, true, null);
        }

        @Override
        public boolean canStart() {
            if (PolarBearEntity.this.isBaby()) {
                return false;
            }
            if (super.canStart()) {
                List<PolarBearEntity> list = PolarBearEntity.this.getEntityWorld().getNonSpectatingEntities(PolarBearEntity.class, PolarBearEntity.this.getBoundingBox().expand(8.0, 4.0, 8.0));
                for (PolarBearEntity polarBearEntity : list) {
                    if (!polarBearEntity.isBaby()) continue;
                    return true;
                }
            }
            return false;
        }

        @Override
        protected double getFollowRange() {
            return super.getFollowRange() * 0.5;
        }
    }
}


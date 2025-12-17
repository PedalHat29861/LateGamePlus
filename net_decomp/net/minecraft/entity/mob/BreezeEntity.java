/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  com.mojang.serialization.Dynamic
 *  org.jspecify.annotations.Nullable
 */
package net.minecraft.entity.mob;

import com.mojang.serialization.Dynamic;
import java.util.Optional;
import net.minecraft.block.BlockRenderType;
import net.minecraft.block.BlockState;
import net.minecraft.entity.AnimationState;
import net.minecraft.entity.Entity;
import net.minecraft.entity.EntityPose;
import net.minecraft.entity.EntityType;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.ProjectileDeflection;
import net.minecraft.entity.ai.brain.Brain;
import net.minecraft.entity.ai.brain.MemoryModuleType;
import net.minecraft.entity.ai.pathing.PathNodeType;
import net.minecraft.entity.attribute.DefaultAttributeContainer;
import net.minecraft.entity.attribute.EntityAttributes;
import net.minecraft.entity.damage.DamageSource;
import net.minecraft.entity.data.TrackedData;
import net.minecraft.entity.mob.BreezeBrain;
import net.minecraft.entity.mob.HostileEntity;
import net.minecraft.entity.mob.MobEntity;
import net.minecraft.entity.projectile.ProjectileEntity;
import net.minecraft.particle.BlockStateParticleEffect;
import net.minecraft.particle.ParticleTypes;
import net.minecraft.registry.tag.EntityTypeTags;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.sound.SoundCategory;
import net.minecraft.sound.SoundEvent;
import net.minecraft.sound.SoundEvents;
import net.minecraft.util.math.Vec3d;
import net.minecraft.util.profiler.Profiler;
import net.minecraft.util.profiler.Profilers;
import net.minecraft.world.World;
import net.minecraft.world.debug.DebugSubscriptionTypes;
import net.minecraft.world.debug.DebugTrackable;
import net.minecraft.world.debug.data.BreezeDebugData;
import org.jspecify.annotations.Nullable;

public class BreezeEntity
extends HostileEntity {
    private static final int field_47271 = 20;
    private static final int field_47272 = 1;
    private static final int field_47273 = 20;
    private static final int field_47274 = 3;
    private static final int field_47275 = 5;
    private static final int field_47276 = 10;
    private static final float field_47278 = 3.0f;
    private static final int field_47813 = 1;
    private static final int field_47814 = 80;
    public AnimationState idleAnimationState = new AnimationState();
    public AnimationState slidingAnimationState = new AnimationState();
    public AnimationState slidingBackAnimationState = new AnimationState();
    public AnimationState longJumpingAnimationState = new AnimationState();
    public AnimationState shootingAnimationState = new AnimationState();
    public AnimationState inhalingAnimationState = new AnimationState();
    private int longJumpingParticleAddCount = 0;
    private int ticksUntilWhirlSound = 0;
    private static final ProjectileDeflection PROJECTILE_DEFLECTOR = (projectile, hitEntity, random) -> {
        hitEntity.getEntityWorld().playSoundFromEntity(null, hitEntity, SoundEvents.ENTITY_BREEZE_DEFLECT, hitEntity.getSoundCategory(), 1.0f, 1.0f);
        ProjectileDeflection.SIMPLE.deflect(projectile, hitEntity, random);
    };

    public static DefaultAttributeContainer.Builder createBreezeAttributes() {
        return MobEntity.createMobAttributes().add(EntityAttributes.MOVEMENT_SPEED, 0.63f).add(EntityAttributes.MAX_HEALTH, 30.0).add(EntityAttributes.FOLLOW_RANGE, 24.0).add(EntityAttributes.ATTACK_DAMAGE, 3.0);
    }

    public BreezeEntity(EntityType<? extends HostileEntity> entityType, World world) {
        super(entityType, world);
        this.setPathfindingPenalty(PathNodeType.DANGER_TRAPDOOR, -1.0f);
        this.setPathfindingPenalty(PathNodeType.DAMAGE_FIRE, -1.0f);
        this.experiencePoints = 10;
    }

    @Override
    protected Brain<?> deserializeBrain(Dynamic<?> dynamic) {
        return BreezeBrain.create(this, this.createBrainProfile().deserialize(dynamic));
    }

    public Brain<BreezeEntity> getBrain() {
        return super.getBrain();
    }

    protected Brain.Profile<BreezeEntity> createBrainProfile() {
        return Brain.createProfile(BreezeBrain.MEMORY_MODULES, BreezeBrain.SENSORS);
    }

    @Override
    public void onTrackedDataSet(TrackedData<?> data) {
        if (this.getEntityWorld().isClient() && POSE.equals(data)) {
            this.stopAnimations();
            EntityPose entityPose = this.getPose();
            switch (entityPose) {
                case SHOOTING: {
                    this.shootingAnimationState.startIfNotRunning(this.age);
                    break;
                }
                case INHALING: {
                    this.inhalingAnimationState.startIfNotRunning(this.age);
                    break;
                }
                case SLIDING: {
                    this.slidingAnimationState.startIfNotRunning(this.age);
                }
            }
        }
        super.onTrackedDataSet(data);
    }

    private void stopAnimations() {
        this.shootingAnimationState.stop();
        this.idleAnimationState.stop();
        this.inhalingAnimationState.stop();
        this.longJumpingAnimationState.stop();
    }

    @Override
    public void tick() {
        EntityPose entityPose = this.getPose();
        switch (entityPose) {
            case SLIDING: {
                this.addBlockParticles(20);
                break;
            }
            case SHOOTING: 
            case INHALING: 
            case STANDING: {
                this.resetLongJumpingParticleAddCount().addBlockParticles(1 + this.getRandom().nextInt(1));
                break;
            }
            case LONG_JUMPING: {
                this.longJumpingAnimationState.startIfNotRunning(this.age);
                this.addLongJumpingParticles();
            }
        }
        this.idleAnimationState.startIfNotRunning(this.age);
        if (entityPose != EntityPose.SLIDING && this.slidingAnimationState.isRunning()) {
            this.slidingBackAnimationState.start(this.age);
            this.slidingAnimationState.stop();
        }
        int n = this.ticksUntilWhirlSound = this.ticksUntilWhirlSound == 0 ? this.random.nextBetween(1, 80) : this.ticksUntilWhirlSound - 1;
        if (this.ticksUntilWhirlSound == 0) {
            this.playWhirlSound();
        }
        super.tick();
    }

    public BreezeEntity resetLongJumpingParticleAddCount() {
        this.longJumpingParticleAddCount = 0;
        return this;
    }

    public void addLongJumpingParticles() {
        if (++this.longJumpingParticleAddCount > 5) {
            return;
        }
        BlockState blockState = !this.getBlockStateAtPos().isAir() ? this.getBlockStateAtPos() : this.getSteppingBlockState();
        Vec3d vec3d = this.getVelocity();
        Vec3d vec3d2 = this.getEntityPos().add(vec3d).add(0.0, 0.1f, 0.0);
        for (int i = 0; i < 3; ++i) {
            this.getEntityWorld().addParticleClient(new BlockStateParticleEffect(ParticleTypes.BLOCK, blockState), vec3d2.x, vec3d2.y, vec3d2.z, 0.0, 0.0, 0.0);
        }
    }

    public void addBlockParticles(int count) {
        BlockState blockState;
        if (this.hasVehicle()) {
            return;
        }
        Vec3d vec3d = this.getBoundingBox().getCenter();
        Vec3d vec3d2 = new Vec3d(vec3d.x, this.getEntityPos().y, vec3d.z);
        BlockState blockState2 = blockState = !this.getBlockStateAtPos().isAir() ? this.getBlockStateAtPos() : this.getSteppingBlockState();
        if (blockState.getRenderType() == BlockRenderType.INVISIBLE) {
            return;
        }
        for (int i = 0; i < count; ++i) {
            this.getEntityWorld().addParticleClient(new BlockStateParticleEffect(ParticleTypes.BLOCK, blockState), vec3d2.x, vec3d2.y, vec3d2.z, 0.0, 0.0, 0.0);
        }
    }

    @Override
    public void playAmbientSound() {
        if (this.getTarget() != null && this.isOnGround()) {
            return;
        }
        this.getEntityWorld().playSoundFromEntityClient(this, this.getAmbientSound(), this.getSoundCategory(), 1.0f, 1.0f);
    }

    public void playWhirlSound() {
        float f = 0.7f + 0.4f * this.random.nextFloat();
        float g = 0.8f + 0.2f * this.random.nextFloat();
        this.getEntityWorld().playSoundFromEntityClient(this, SoundEvents.ENTITY_BREEZE_WHIRL, this.getSoundCategory(), g, f);
    }

    @Override
    public ProjectileDeflection getProjectileDeflection(ProjectileEntity projectile) {
        if (projectile.getType() == EntityType.BREEZE_WIND_CHARGE || projectile.getType() == EntityType.WIND_CHARGE) {
            return ProjectileDeflection.NONE;
        }
        return this.getType().isIn(EntityTypeTags.DEFLECTS_PROJECTILES) ? PROJECTILE_DEFLECTOR : ProjectileDeflection.NONE;
    }

    @Override
    public SoundCategory getSoundCategory() {
        return SoundCategory.HOSTILE;
    }

    @Override
    protected SoundEvent getDeathSound() {
        return SoundEvents.ENTITY_BREEZE_DEATH;
    }

    @Override
    protected SoundEvent getHurtSound(DamageSource source) {
        return SoundEvents.ENTITY_BREEZE_HURT;
    }

    @Override
    protected SoundEvent getAmbientSound() {
        return this.isOnGround() ? SoundEvents.ENTITY_BREEZE_IDLE_GROUND : SoundEvents.ENTITY_BREEZE_IDLE_AIR;
    }

    public Optional<LivingEntity> getHurtBy() {
        return this.getBrain().getOptionalRegisteredMemory(MemoryModuleType.HURT_BY).map(DamageSource::getAttacker).filter(attacker -> attacker instanceof LivingEntity).map(livingAttacker -> (LivingEntity)livingAttacker);
    }

    public boolean isWithinShortRange(Vec3d pos) {
        Vec3d vec3d = this.getBlockPos().toCenterPos();
        return pos.isWithinRangeOf(vec3d, 4.0, 10.0);
    }

    @Override
    protected void mobTick(ServerWorld world) {
        Profiler profiler = Profilers.get();
        profiler.push("breezeBrain");
        this.getBrain().tick(world, this);
        profiler.swap("breezeActivityUpdate");
        BreezeBrain.updateActivities(this);
        profiler.pop();
        super.mobTick(world);
    }

    @Override
    public boolean canTarget(EntityType<?> type) {
        return type == EntityType.PLAYER || type == EntityType.IRON_GOLEM;
    }

    @Override
    public int getMaxHeadRotation() {
        return 30;
    }

    @Override
    public int getMaxLookYawChange() {
        return 25;
    }

    public double getChargeY() {
        return this.getY() + (double)(this.getHeight() / 2.0f) + (double)0.3f;
    }

    @Override
    public boolean isInvulnerableTo(ServerWorld world, DamageSource source) {
        return source.getAttacker() instanceof BreezeEntity || super.isInvulnerableTo(world, source);
    }

    @Override
    public double getSwimHeight() {
        return this.getStandingEyeHeight();
    }

    @Override
    public boolean handleFallDamage(double fallDistance, float damagePerDistance, DamageSource damageSource) {
        if (fallDistance > 3.0) {
            this.playSound(SoundEvents.ENTITY_BREEZE_LAND, 1.0f, 1.0f);
        }
        return super.handleFallDamage(fallDistance, damagePerDistance, damageSource);
    }

    @Override
    protected Entity.MoveEffect getMoveEffect() {
        return Entity.MoveEffect.EVENTS;
    }

    @Override
    public @Nullable LivingEntity getTarget() {
        return this.getTargetInBrain();
    }

    @Override
    public void registerTracking(ServerWorld world, DebugTrackable.Tracker tracker) {
        super.registerTracking(world, tracker);
        tracker.track(DebugSubscriptionTypes.BREEZES, () -> new BreezeDebugData(this.getBrain().getOptionalRegisteredMemory(MemoryModuleType.ATTACK_TARGET).map(Entity::getId), this.getBrain().getOptionalRegisteredMemory(MemoryModuleType.BREEZE_JUMP_TARGET)));
    }
}


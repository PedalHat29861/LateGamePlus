/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  org.jspecify.annotations.Nullable
 */
package net.minecraft.entity.passive;

import java.util.Objects;
import net.minecraft.block.BlockState;
import net.minecraft.entity.Entity;
import net.minecraft.entity.EntityData;
import net.minecraft.entity.EntityType;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.MovementType;
import net.minecraft.entity.SpawnReason;
import net.minecraft.entity.ai.goal.Goal;
import net.minecraft.entity.attribute.DefaultAttributeContainer;
import net.minecraft.entity.attribute.EntityAttributes;
import net.minecraft.entity.damage.DamageSource;
import net.minecraft.entity.effect.StatusEffects;
import net.minecraft.entity.mob.MobEntity;
import net.minecraft.entity.passive.PassiveEntity;
import net.minecraft.entity.passive.WaterAnimalEntity;
import net.minecraft.fluid.FluidState;
import net.minecraft.particle.ParticleEffect;
import net.minecraft.particle.ParticleTypes;
import net.minecraft.registry.tag.FluidTags;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.sound.SoundEvent;
import net.minecraft.sound.SoundEvents;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.MathHelper;
import net.minecraft.util.math.Vec3d;
import net.minecraft.world.LocalDifficulty;
import net.minecraft.world.ServerWorldAccess;
import net.minecraft.world.World;
import org.jspecify.annotations.Nullable;

public class SquidEntity
extends WaterAnimalEntity {
    public float tiltAngle;
    public float lastTiltAngle;
    public float rollAngle;
    public float lastRollAngle;
    public float thrustTimer;
    public float lastThrustTimer;
    public float tentacleAngle;
    public float lastTentacleAngle;
    private float swimVelocityScale;
    private float thrustTimerSpeed;
    private float turningSpeed;
    Vec3d swimVec = Vec3d.ZERO;

    public SquidEntity(EntityType<? extends SquidEntity> entityType, World world) {
        super((EntityType<? extends WaterAnimalEntity>)entityType, world);
        this.random.setSeed(this.getId());
        this.thrustTimerSpeed = 1.0f / (this.random.nextFloat() + 1.0f) * 0.2f;
    }

    @Override
    protected void initGoals() {
        this.goalSelector.add(0, new SwimGoal(this));
        this.goalSelector.add(1, new EscapeAttackerGoal());
    }

    public static DefaultAttributeContainer.Builder createSquidAttributes() {
        return MobEntity.createMobAttributes().add(EntityAttributes.MAX_HEALTH, 10.0);
    }

    @Override
    protected SoundEvent getAmbientSound() {
        return SoundEvents.ENTITY_SQUID_AMBIENT;
    }

    @Override
    protected SoundEvent getHurtSound(DamageSource source) {
        return SoundEvents.ENTITY_SQUID_HURT;
    }

    @Override
    protected SoundEvent getDeathSound() {
        return SoundEvents.ENTITY_SQUID_DEATH;
    }

    protected SoundEvent getSquirtSound() {
        return SoundEvents.ENTITY_SQUID_SQUIRT;
    }

    @Override
    public boolean canBeLeashed() {
        return true;
    }

    @Override
    protected float getSoundVolume() {
        return 0.4f;
    }

    @Override
    protected Entity.MoveEffect getMoveEffect() {
        return Entity.MoveEffect.EVENTS;
    }

    @Override
    public @Nullable PassiveEntity createChild(ServerWorld world, PassiveEntity entity) {
        return EntityType.SQUID.create(world, SpawnReason.BREEDING);
    }

    @Override
    protected double getGravity() {
        return 0.08;
    }

    @Override
    public void tickMovement() {
        super.tickMovement();
        this.lastTiltAngle = this.tiltAngle;
        this.lastRollAngle = this.rollAngle;
        this.lastThrustTimer = this.thrustTimer;
        this.lastTentacleAngle = this.tentacleAngle;
        this.thrustTimer += this.thrustTimerSpeed;
        if ((double)this.thrustTimer > Math.PI * 2) {
            if (this.getEntityWorld().isClient()) {
                this.thrustTimer = (float)Math.PI * 2;
            } else {
                this.thrustTimer -= (float)Math.PI * 2;
                if (this.random.nextInt(10) == 0) {
                    this.thrustTimerSpeed = 1.0f / (this.random.nextFloat() + 1.0f) * 0.2f;
                }
                this.getEntityWorld().sendEntityStatus(this, (byte)19);
            }
        }
        if (this.isTouchingWater()) {
            if (this.thrustTimer < (float)Math.PI) {
                float f = this.thrustTimer / (float)Math.PI;
                this.tentacleAngle = MathHelper.sin(f * f * (float)Math.PI) * (float)Math.PI * 0.25f;
                if ((double)f > 0.75) {
                    if (this.isLogicalSideForUpdatingMovement()) {
                        this.setVelocity(this.swimVec);
                    }
                    this.turningSpeed = 1.0f;
                } else {
                    this.turningSpeed *= 0.8f;
                }
            } else {
                this.tentacleAngle = 0.0f;
                if (this.isLogicalSideForUpdatingMovement()) {
                    this.setVelocity(this.getVelocity().multiply(0.9));
                }
                this.turningSpeed *= 0.99f;
            }
            Vec3d vec3d = this.getVelocity();
            double d = vec3d.horizontalLength();
            this.bodyYaw += (-((float)MathHelper.atan2(vec3d.x, vec3d.z)) * 57.295776f - this.bodyYaw) * 0.1f;
            this.setYaw(this.bodyYaw);
            this.rollAngle += (float)Math.PI * this.turningSpeed * 1.5f;
            this.tiltAngle += (-((float)MathHelper.atan2(d, vec3d.y)) * 57.295776f - this.tiltAngle) * 0.1f;
        } else {
            this.tentacleAngle = MathHelper.abs(MathHelper.sin(this.thrustTimer)) * (float)Math.PI * 0.25f;
            if (!this.getEntityWorld().isClient()) {
                double e = this.getVelocity().y;
                e = this.hasStatusEffect(StatusEffects.LEVITATION) ? 0.05 * (double)(this.getStatusEffect(StatusEffects.LEVITATION).getAmplifier() + 1) : (e -= this.getFinalGravity());
                this.setVelocity(0.0, e * (double)0.98f, 0.0);
            }
            this.tiltAngle += (-90.0f - this.tiltAngle) * 0.02f;
        }
    }

    @Override
    public boolean damage(ServerWorld world, DamageSource source, float amount) {
        if (super.damage(world, source, amount) && this.getAttacker() != null) {
            this.squirt();
            return true;
        }
        return false;
    }

    private Vec3d applyBodyRotations(Vec3d shootVector) {
        Vec3d vec3d = shootVector.rotateX(this.lastTiltAngle * ((float)Math.PI / 180));
        vec3d = vec3d.rotateY(-this.lastBodyYaw * ((float)Math.PI / 180));
        return vec3d;
    }

    private void squirt() {
        this.playSound(this.getSquirtSound());
        Vec3d vec3d = this.applyBodyRotations(new Vec3d(0.0, -1.0, 0.0)).add(this.getX(), this.getY(), this.getZ());
        for (int i = 0; i < 30; ++i) {
            Vec3d vec3d2 = this.applyBodyRotations(new Vec3d((double)this.random.nextFloat() * 0.6 - 0.3, -1.0, (double)this.random.nextFloat() * 0.6 - 0.3));
            float f = this.isBaby() ? 0.1f : 0.3f;
            Vec3d vec3d3 = vec3d2.multiply(f + this.random.nextFloat() * 2.0f);
            ((ServerWorld)this.getEntityWorld()).spawnParticles(this.getInkParticle(), vec3d.x, vec3d.y + 0.5, vec3d.z, 0, vec3d3.x, vec3d3.y, vec3d3.z, 0.1f);
        }
    }

    protected ParticleEffect getInkParticle() {
        return ParticleTypes.SQUID_INK;
    }

    @Override
    public void travel(Vec3d movementInput) {
        this.move(MovementType.SELF, this.getVelocity());
    }

    @Override
    public void handleStatus(byte status) {
        if (status == 19) {
            this.thrustTimer = 0.0f;
        } else {
            super.handleStatus(status);
        }
    }

    public boolean hasSwimmingVector() {
        return this.swimVec.lengthSquared() > (double)1.0E-5f;
    }

    @Override
    public @Nullable EntityData initialize(ServerWorldAccess world, LocalDifficulty difficulty, SpawnReason spawnReason, @Nullable EntityData entityData) {
        EntityData entityData2 = Objects.requireNonNullElseGet(entityData, () -> new PassiveEntity.PassiveData(0.05f));
        return super.initialize(world, difficulty, spawnReason, entityData2);
    }

    static class SwimGoal
    extends Goal {
        private final SquidEntity squid;

        public SwimGoal(SquidEntity squid) {
            this.squid = squid;
        }

        @Override
        public boolean canStart() {
            return true;
        }

        @Override
        public void tick() {
            int i = this.squid.getDespawnCounter();
            if (i > 100) {
                this.squid.swimVec = Vec3d.ZERO;
            } else if (this.squid.getRandom().nextInt(SwimGoal.toGoalTicks(50)) == 0 || !this.squid.touchingWater || !this.squid.hasSwimmingVector()) {
                float f = this.squid.getRandom().nextFloat() * ((float)Math.PI * 2);
                this.squid.swimVec = new Vec3d(MathHelper.cos(f) * 0.2f, -0.1f + this.squid.getRandom().nextFloat() * 0.2f, MathHelper.sin(f) * 0.2f);
            }
        }
    }

    class EscapeAttackerGoal
    extends Goal {
        private static final float field_30375 = 3.0f;
        private static final float field_30376 = 5.0f;
        private static final float field_30377 = 10.0f;
        private int timer;

        EscapeAttackerGoal() {
        }

        @Override
        public boolean canStart() {
            LivingEntity livingEntity = SquidEntity.this.getAttacker();
            if (SquidEntity.this.isTouchingWater() && livingEntity != null) {
                return SquidEntity.this.squaredDistanceTo(livingEntity) < 100.0;
            }
            return false;
        }

        @Override
        public void start() {
            this.timer = 0;
        }

        @Override
        public boolean shouldRunEveryTick() {
            return true;
        }

        @Override
        public void tick() {
            ++this.timer;
            LivingEntity livingEntity = SquidEntity.this.getAttacker();
            if (livingEntity == null) {
                return;
            }
            Vec3d vec3d = new Vec3d(SquidEntity.this.getX() - livingEntity.getX(), SquidEntity.this.getY() - livingEntity.getY(), SquidEntity.this.getZ() - livingEntity.getZ());
            BlockState blockState = SquidEntity.this.getEntityWorld().getBlockState(BlockPos.ofFloored(SquidEntity.this.getX() + vec3d.x, SquidEntity.this.getY() + vec3d.y, SquidEntity.this.getZ() + vec3d.z));
            FluidState fluidState = SquidEntity.this.getEntityWorld().getFluidState(BlockPos.ofFloored(SquidEntity.this.getX() + vec3d.x, SquidEntity.this.getY() + vec3d.y, SquidEntity.this.getZ() + vec3d.z));
            if (fluidState.isIn(FluidTags.WATER) || blockState.isAir()) {
                double d = vec3d.length();
                if (d > 0.0) {
                    vec3d.normalize();
                    double e = 3.0;
                    if (d > 5.0) {
                        e -= (d - 5.0) / 5.0;
                    }
                    if (e > 0.0) {
                        vec3d = vec3d.multiply(e);
                    }
                }
                if (blockState.isAir()) {
                    vec3d = vec3d.subtract(0.0, vec3d.y, 0.0);
                }
                SquidEntity.this.swimVec = new Vec3d(vec3d.x / 20.0, vec3d.y / 20.0, vec3d.z / 20.0);
            }
            if (this.timer % 10 == 5) {
                SquidEntity.this.getEntityWorld().addParticleClient(ParticleTypes.BUBBLE, SquidEntity.this.getX(), SquidEntity.this.getY(), SquidEntity.this.getZ(), 0.0, 0.0, 0.0);
            }
        }
    }
}


/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  org.jspecify.annotations.Nullable
 */
package net.minecraft.entity.mob;

import java.util.EnumSet;
import java.util.function.BooleanSupplier;
import net.minecraft.block.BlockState;
import net.minecraft.entity.EntityType;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.SpawnReason;
import net.minecraft.entity.ai.control.MoveControl;
import net.minecraft.entity.ai.goal.ActiveTargetGoal;
import net.minecraft.entity.ai.goal.Goal;
import net.minecraft.entity.attribute.DefaultAttributeContainer;
import net.minecraft.entity.attribute.EntityAttributes;
import net.minecraft.entity.damage.DamageSource;
import net.minecraft.entity.data.DataTracker;
import net.minecraft.entity.data.TrackedData;
import net.minecraft.entity.data.TrackedDataHandlerRegistry;
import net.minecraft.entity.mob.MobEntity;
import net.minecraft.entity.mob.Monster;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.entity.projectile.FireballEntity;
import net.minecraft.fluid.FluidState;
import net.minecraft.registry.tag.BlockTags;
import net.minecraft.registry.tag.DamageTypeTags;
import net.minecraft.registry.tag.FluidTags;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.sound.SoundCategory;
import net.minecraft.sound.SoundEvent;
import net.minecraft.sound.SoundEvents;
import net.minecraft.storage.ReadView;
import net.minecraft.storage.WriteView;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Box;
import net.minecraft.util.math.Direction;
import net.minecraft.util.math.MathHelper;
import net.minecraft.util.math.Vec3d;
import net.minecraft.util.math.random.Random;
import net.minecraft.world.BlockView;
import net.minecraft.world.Difficulty;
import net.minecraft.world.Heightmap;
import net.minecraft.world.World;
import net.minecraft.world.WorldAccess;
import org.jspecify.annotations.Nullable;

public class GhastEntity
extends MobEntity
implements Monster {
    private static final TrackedData<Boolean> SHOOTING = DataTracker.registerData(GhastEntity.class, TrackedDataHandlerRegistry.BOOLEAN);
    private static final byte DEFAULT_FIREBALL_STRENGTH = 1;
    private int fireballStrength = 1;

    public GhastEntity(EntityType<? extends GhastEntity> entityType, World world) {
        super((EntityType<? extends MobEntity>)entityType, world);
        this.experiencePoints = 5;
        this.moveControl = new GhastMoveControl(this, false, () -> false);
    }

    @Override
    protected void initGoals() {
        this.goalSelector.add(5, new FlyRandomlyGoal(this));
        this.goalSelector.add(7, new LookAtTargetGoal(this));
        this.goalSelector.add(7, new ShootFireballGoal(this));
        this.targetSelector.add(1, new ActiveTargetGoal<PlayerEntity>(this, PlayerEntity.class, 10, true, false, (entity, world) -> Math.abs(entity.getY() - this.getY()) <= 4.0));
    }

    public boolean isShooting() {
        return this.dataTracker.get(SHOOTING);
    }

    public void setShooting(boolean shooting) {
        this.dataTracker.set(SHOOTING, shooting);
    }

    public int getFireballStrength() {
        return this.fireballStrength;
    }

    private static boolean isFireballFromPlayer(DamageSource damageSource) {
        return damageSource.getSource() instanceof FireballEntity && damageSource.getAttacker() instanceof PlayerEntity;
    }

    @Override
    public boolean isInvulnerableTo(ServerWorld world, DamageSource source) {
        return this.isInvulnerable() && !source.isIn(DamageTypeTags.BYPASSES_INVULNERABILITY) || !GhastEntity.isFireballFromPlayer(source) && super.isInvulnerableTo(world, source);
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
        this.travelFlying(movementInput, 0.02f);
    }

    @Override
    public boolean damage(ServerWorld world, DamageSource source, float amount) {
        if (GhastEntity.isFireballFromPlayer(source)) {
            super.damage(world, source, 1000.0f);
            return true;
        }
        if (this.isInvulnerableTo(world, source)) {
            return false;
        }
        return super.damage(world, source, amount);
    }

    @Override
    protected void initDataTracker(DataTracker.Builder builder) {
        super.initDataTracker(builder);
        builder.add(SHOOTING, false);
    }

    public static DefaultAttributeContainer.Builder createGhastAttributes() {
        return MobEntity.createMobAttributes().add(EntityAttributes.MAX_HEALTH, 10.0).add(EntityAttributes.FOLLOW_RANGE, 100.0).add(EntityAttributes.CAMERA_DISTANCE, 8.0).add(EntityAttributes.FLYING_SPEED, 0.06);
    }

    @Override
    public SoundCategory getSoundCategory() {
        return SoundCategory.HOSTILE;
    }

    @Override
    protected SoundEvent getAmbientSound() {
        return SoundEvents.ENTITY_GHAST_AMBIENT;
    }

    @Override
    protected SoundEvent getHurtSound(DamageSource source) {
        return SoundEvents.ENTITY_GHAST_HURT;
    }

    @Override
    protected SoundEvent getDeathSound() {
        return SoundEvents.ENTITY_GHAST_DEATH;
    }

    @Override
    protected float getSoundVolume() {
        return 5.0f;
    }

    public static boolean canSpawn(EntityType<GhastEntity> type, WorldAccess world, SpawnReason spawnReason, BlockPos pos, Random random) {
        return world.getDifficulty() != Difficulty.PEACEFUL && random.nextInt(20) == 0 && GhastEntity.canMobSpawn(type, world, spawnReason, pos, random);
    }

    @Override
    public int getLimitPerChunk() {
        return 1;
    }

    @Override
    protected void writeCustomData(WriteView view) {
        super.writeCustomData(view);
        view.putByte("ExplosionPower", (byte)this.fireballStrength);
    }

    @Override
    protected void readCustomData(ReadView view) {
        super.readCustomData(view);
        this.fireballStrength = view.getByte("ExplosionPower", (byte)1);
    }

    @Override
    public boolean hasQuadLeashAttachmentPoints() {
        return true;
    }

    @Override
    public double getElasticLeashDistance() {
        return 10.0;
    }

    @Override
    public double getLeashSnappingDistance() {
        return 16.0;
    }

    public static void updateYaw(MobEntity ghast) {
        if (ghast.getTarget() == null) {
            Vec3d vec3d = ghast.getVelocity();
            ghast.setYaw(-((float)MathHelper.atan2(vec3d.x, vec3d.z)) * 57.295776f);
            ghast.bodyYaw = ghast.getYaw();
        } else {
            LivingEntity livingEntity = ghast.getTarget();
            double d = 64.0;
            if (livingEntity.squaredDistanceTo(ghast) < 4096.0) {
                double e = livingEntity.getX() - ghast.getX();
                double f = livingEntity.getZ() - ghast.getZ();
                ghast.setYaw(-((float)MathHelper.atan2(e, f)) * 57.295776f);
                ghast.bodyYaw = ghast.getYaw();
            }
        }
    }

    public static class GhastMoveControl
    extends MoveControl {
        private final MobEntity ghast;
        private int collisionCheckCooldown;
        private final boolean happy;
        private final BooleanSupplier shouldStayStill;

        public GhastMoveControl(MobEntity ghast, boolean happy, BooleanSupplier shouldStayStill) {
            super(ghast);
            this.ghast = ghast;
            this.happy = happy;
            this.shouldStayStill = shouldStayStill;
        }

        @Override
        public void tick() {
            if (this.shouldStayStill.getAsBoolean()) {
                this.state = MoveControl.State.WAIT;
                this.ghast.stopMovement();
            }
            if (this.state != MoveControl.State.MOVE_TO) {
                return;
            }
            if (this.collisionCheckCooldown-- <= 0) {
                this.collisionCheckCooldown += this.ghast.getRandom().nextInt(5) + 2;
                Vec3d vec3d = new Vec3d(this.targetX - this.ghast.getX(), this.targetY - this.ghast.getY(), this.targetZ - this.ghast.getZ());
                if (this.willCollide(vec3d)) {
                    this.ghast.setVelocity(this.ghast.getVelocity().add(vec3d.normalize().multiply(this.ghast.getAttributeValue(EntityAttributes.FLYING_SPEED) * 5.0 / 3.0)));
                } else {
                    this.state = MoveControl.State.WAIT;
                }
            }
        }

        private boolean willCollide(Vec3d movement) {
            Box box = this.ghast.getBoundingBox();
            Box box2 = box.offset(movement);
            if (this.happy) {
                for (BlockPos blockPos : BlockPos.iterate(box2.expand(1.0))) {
                    if (this.canPassThrough(this.ghast.getEntityWorld(), null, null, blockPos, false, false)) continue;
                    return false;
                }
            }
            boolean bl = this.ghast.isTouchingWater();
            boolean bl2 = this.ghast.isInLava();
            Vec3d vec3d = this.ghast.getEntityPos();
            Vec3d vec3d2 = vec3d.add(movement);
            return BlockView.collectCollisionsBetween(vec3d, vec3d2, box2, (pos, version) -> {
                if (box.contains(pos)) {
                    return true;
                }
                return this.canPassThrough(this.ghast.getEntityWorld(), vec3d, vec3d2, pos, bl, bl2);
            });
        }

        private boolean canPassThrough(BlockView world, @Nullable Vec3d oldPos, @Nullable Vec3d newPos, BlockPos blockPos, boolean waterAllowed, boolean lavaAllowed) {
            boolean bl2;
            boolean bl;
            BlockState blockState = world.getBlockState(blockPos);
            if (blockState.isAir()) {
                return true;
            }
            boolean bl3 = bl = oldPos != null && newPos != null;
            boolean bl4 = bl ? !this.ghast.collides(oldPos, newPos, blockState.getCollisionShape(world, blockPos).offset(new Vec3d(blockPos)).getBoundingBoxes()) : (bl2 = blockState.getCollisionShape(world, blockPos).isEmpty());
            if (!this.happy) {
                return bl2;
            }
            if (blockState.isIn(BlockTags.HAPPY_GHAST_AVOIDS)) {
                return false;
            }
            FluidState fluidState = world.getFluidState(blockPos);
            if (!(fluidState.isEmpty() || bl && !this.ghast.collidesWithFluid(fluidState, blockPos, oldPos, newPos))) {
                if (fluidState.isIn(FluidTags.WATER)) {
                    return waterAllowed;
                }
                if (fluidState.isIn(FluidTags.LAVA)) {
                    return lavaAllowed;
                }
            }
            return bl2;
        }
    }

    public static class FlyRandomlyGoal
    extends Goal {
        private static final int field_59707 = 64;
        private final MobEntity ghast;
        private final int blockCheckDistance;

        public FlyRandomlyGoal(MobEntity ghast) {
            this(ghast, 0);
        }

        public FlyRandomlyGoal(MobEntity ghast, int blockCheckDistance) {
            this.ghast = ghast;
            this.blockCheckDistance = blockCheckDistance;
            this.setControls(EnumSet.of(Goal.Control.MOVE));
        }

        @Override
        public boolean canStart() {
            double f;
            double e;
            MoveControl moveControl = this.ghast.getMoveControl();
            if (!moveControl.isMoving()) {
                return true;
            }
            double d = moveControl.getTargetX() - this.ghast.getX();
            double g = d * d + (e = moveControl.getTargetY() - this.ghast.getY()) * e + (f = moveControl.getTargetZ() - this.ghast.getZ()) * f;
            return g < 1.0 || g > 3600.0;
        }

        @Override
        public boolean shouldContinue() {
            return false;
        }

        @Override
        public void start() {
            Vec3d vec3d = FlyRandomlyGoal.locateTarget(this.ghast, this.blockCheckDistance);
            this.ghast.getMoveControl().moveTo(vec3d.getX(), vec3d.getY(), vec3d.getZ(), 1.0);
        }

        public static Vec3d locateTarget(MobEntity ghast, int blockCheckDistance) {
            BlockPos blockPos;
            int j;
            World world = ghast.getEntityWorld();
            Random random = ghast.getRandom();
            Vec3d vec3d = ghast.getEntityPos();
            Vec3d vec3d2 = null;
            for (int i = 0; i < 64; ++i) {
                vec3d2 = FlyRandomlyGoal.getTargetPos(ghast, vec3d, random);
                if (vec3d2 == null || !FlyRandomlyGoal.isTargetValid(world, vec3d2, blockCheckDistance)) continue;
                return vec3d2;
            }
            if (vec3d2 == null) {
                vec3d2 = FlyRandomlyGoal.addRandom(vec3d, random);
            }
            if ((j = world.getTopY(Heightmap.Type.MOTION_BLOCKING, (blockPos = BlockPos.ofFloored(vec3d2)).getX(), blockPos.getZ())) < blockPos.getY() && j > world.getBottomY()) {
                vec3d2 = new Vec3d(vec3d2.getX(), ghast.getY() - Math.abs(ghast.getY() - vec3d2.getY()), vec3d2.getZ());
            }
            return vec3d2;
        }

        private static boolean isTargetValid(World world, Vec3d pos, int blockCheckDistance) {
            if (blockCheckDistance <= 0) {
                return true;
            }
            BlockPos blockPos = BlockPos.ofFloored(pos);
            if (!world.getBlockState(blockPos).isAir()) {
                return false;
            }
            for (Direction direction : Direction.values()) {
                for (int i = 1; i < blockCheckDistance; ++i) {
                    BlockPos blockPos2 = blockPos.offset(direction, i);
                    if (world.getBlockState(blockPos2).isAir()) continue;
                    return true;
                }
            }
            return false;
        }

        private static Vec3d addRandom(Vec3d pos, Random random) {
            double d = pos.getX() + (double)((random.nextFloat() * 2.0f - 1.0f) * 16.0f);
            double e = pos.getY() + (double)((random.nextFloat() * 2.0f - 1.0f) * 16.0f);
            double f = pos.getZ() + (double)((random.nextFloat() * 2.0f - 1.0f) * 16.0f);
            return new Vec3d(d, e, f);
        }

        private static @Nullable Vec3d getTargetPos(MobEntity ghast, Vec3d pos, Random random) {
            Vec3d vec3d = FlyRandomlyGoal.addRandom(pos, random);
            if (ghast.hasPositionTarget() && !ghast.isInPositionTargetRange(vec3d)) {
                return null;
            }
            return vec3d;
        }
    }

    public static class LookAtTargetGoal
    extends Goal {
        private final MobEntity ghast;

        public LookAtTargetGoal(MobEntity ghast) {
            this.ghast = ghast;
            this.setControls(EnumSet.of(Goal.Control.LOOK));
        }

        @Override
        public boolean canStart() {
            return true;
        }

        @Override
        public boolean shouldRunEveryTick() {
            return true;
        }

        @Override
        public void tick() {
            GhastEntity.updateYaw(this.ghast);
        }
    }

    static class ShootFireballGoal
    extends Goal {
        private final GhastEntity ghast;
        public int cooldown;

        public ShootFireballGoal(GhastEntity ghast) {
            this.ghast = ghast;
        }

        @Override
        public boolean canStart() {
            return this.ghast.getTarget() != null;
        }

        @Override
        public void start() {
            this.cooldown = 0;
        }

        @Override
        public void stop() {
            this.ghast.setShooting(false);
        }

        @Override
        public boolean shouldRunEveryTick() {
            return true;
        }

        @Override
        public void tick() {
            LivingEntity livingEntity = this.ghast.getTarget();
            if (livingEntity == null) {
                return;
            }
            double d = 64.0;
            if (livingEntity.squaredDistanceTo(this.ghast) < 4096.0 && this.ghast.canSee(livingEntity)) {
                World world = this.ghast.getEntityWorld();
                ++this.cooldown;
                if (this.cooldown == 10 && !this.ghast.isSilent()) {
                    world.syncWorldEvent(null, 1015, this.ghast.getBlockPos(), 0);
                }
                if (this.cooldown == 20) {
                    double e = 4.0;
                    Vec3d vec3d = this.ghast.getRotationVec(1.0f);
                    double f = livingEntity.getX() - (this.ghast.getX() + vec3d.x * 4.0);
                    double g = livingEntity.getBodyY(0.5) - (0.5 + this.ghast.getBodyY(0.5));
                    double h = livingEntity.getZ() - (this.ghast.getZ() + vec3d.z * 4.0);
                    Vec3d vec3d2 = new Vec3d(f, g, h);
                    if (!this.ghast.isSilent()) {
                        world.syncWorldEvent(null, 1016, this.ghast.getBlockPos(), 0);
                    }
                    FireballEntity fireballEntity = new FireballEntity(world, (LivingEntity)this.ghast, vec3d2.normalize(), this.ghast.getFireballStrength());
                    fireballEntity.setPosition(this.ghast.getX() + vec3d.x * 4.0, this.ghast.getBodyY(0.5) + 0.5, fireballEntity.getZ() + vec3d.z * 4.0);
                    world.spawnEntity(fireballEntity);
                    this.cooldown = -40;
                }
            } else if (this.cooldown > 0) {
                --this.cooldown;
            }
            this.ghast.setShooting(this.cooldown > 10);
        }
    }
}


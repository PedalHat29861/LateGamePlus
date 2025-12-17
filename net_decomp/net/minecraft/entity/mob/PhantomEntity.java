/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  org.jspecify.annotations.Nullable
 */
package net.minecraft.entity.mob;

import java.util.Comparator;
import java.util.EnumSet;
import java.util.List;
import net.minecraft.block.BlockState;
import net.minecraft.entity.Entity;
import net.minecraft.entity.EntityData;
import net.minecraft.entity.EntityDimensions;
import net.minecraft.entity.EntityPose;
import net.minecraft.entity.EntityType;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.SpawnReason;
import net.minecraft.entity.ai.TargetPredicate;
import net.minecraft.entity.ai.control.BodyControl;
import net.minecraft.entity.ai.control.LookControl;
import net.minecraft.entity.ai.control.MoveControl;
import net.minecraft.entity.ai.goal.Goal;
import net.minecraft.entity.attribute.EntityAttributes;
import net.minecraft.entity.damage.DamageSource;
import net.minecraft.entity.data.DataTracker;
import net.minecraft.entity.data.TrackedData;
import net.minecraft.entity.data.TrackedDataHandlerRegistry;
import net.minecraft.entity.mob.MobEntity;
import net.minecraft.entity.mob.Monster;
import net.minecraft.entity.passive.CatEntity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.particle.ParticleTypes;
import net.minecraft.predicate.entity.EntityPredicates;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.sound.SoundCategory;
import net.minecraft.sound.SoundEvent;
import net.minecraft.sound.SoundEvents;
import net.minecraft.storage.ReadView;
import net.minecraft.storage.WriteView;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.MathHelper;
import net.minecraft.util.math.Vec3d;
import net.minecraft.world.Heightmap;
import net.minecraft.world.LocalDifficulty;
import net.minecraft.world.ServerWorldAccess;
import net.minecraft.world.World;
import org.jspecify.annotations.Nullable;

public class PhantomEntity
extends MobEntity
implements Monster {
    public static final float field_30475 = 7.448451f;
    public static final int WING_FLAP_TICKS = MathHelper.ceil(24.166098f);
    private static final TrackedData<Integer> SIZE = DataTracker.registerData(PhantomEntity.class, TrackedDataHandlerRegistry.INTEGER);
    Vec3d targetPosition = Vec3d.ZERO;
    @Nullable BlockPos circlingCenter;
    PhantomMovementType movementType = PhantomMovementType.CIRCLE;

    public PhantomEntity(EntityType<? extends PhantomEntity> entityType, World world) {
        super((EntityType<? extends MobEntity>)entityType, world);
        this.experiencePoints = 5;
        this.moveControl = new PhantomMoveControl(this);
        this.lookControl = new PhantomLookControl(this);
    }

    @Override
    public boolean isFlappingWings() {
        return (this.getWingFlapTickOffset() + this.age) % WING_FLAP_TICKS == 0;
    }

    @Override
    protected BodyControl createBodyControl() {
        return new PhantomBodyControl(this);
    }

    @Override
    protected void initGoals() {
        this.goalSelector.add(1, new StartAttackGoal());
        this.goalSelector.add(2, new SwoopMovementGoal());
        this.goalSelector.add(3, new CircleMovementGoal());
        this.targetSelector.add(1, new FindTargetGoal());
    }

    @Override
    protected void initDataTracker(DataTracker.Builder builder) {
        super.initDataTracker(builder);
        builder.add(SIZE, 0);
    }

    public void setPhantomSize(int size) {
        this.dataTracker.set(SIZE, MathHelper.clamp(size, 0, 64));
    }

    private void onSizeChanged() {
        this.calculateDimensions();
        this.getAttributeInstance(EntityAttributes.ATTACK_DAMAGE).setBaseValue(6 + this.getPhantomSize());
    }

    public int getPhantomSize() {
        return this.dataTracker.get(SIZE);
    }

    @Override
    public void onTrackedDataSet(TrackedData<?> data) {
        if (SIZE.equals(data)) {
            this.onSizeChanged();
        }
        super.onTrackedDataSet(data);
    }

    public int getWingFlapTickOffset() {
        return this.getId() * 3;
    }

    @Override
    public void tick() {
        super.tick();
        if (this.getEntityWorld().isClient()) {
            float f = MathHelper.cos((float)(this.getWingFlapTickOffset() + this.age) * 7.448451f * ((float)Math.PI / 180) + (float)Math.PI);
            float g = MathHelper.cos((float)(this.getWingFlapTickOffset() + this.age + 1) * 7.448451f * ((float)Math.PI / 180) + (float)Math.PI);
            if (f > 0.0f && g <= 0.0f) {
                this.getEntityWorld().playSoundClient(this.getX(), this.getY(), this.getZ(), SoundEvents.ENTITY_PHANTOM_FLAP, this.getSoundCategory(), 0.95f + this.random.nextFloat() * 0.05f, 0.95f + this.random.nextFloat() * 0.05f, false);
            }
            float h = this.getWidth() * 1.48f;
            float i = MathHelper.cos(this.getYaw() * ((float)Math.PI / 180)) * h;
            float j = MathHelper.sin(this.getYaw() * ((float)Math.PI / 180)) * h;
            float k = (0.3f + f * 0.45f) * this.getHeight() * 2.5f;
            this.getEntityWorld().addParticleClient(ParticleTypes.MYCELIUM, this.getX() + (double)i, this.getY() + (double)k, this.getZ() + (double)j, 0.0, 0.0, 0.0);
            this.getEntityWorld().addParticleClient(ParticleTypes.MYCELIUM, this.getX() - (double)i, this.getY() + (double)k, this.getZ() - (double)j, 0.0, 0.0, 0.0);
        }
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
        this.travelFlying(movementInput, 0.2f);
    }

    @Override
    public EntityData initialize(ServerWorldAccess world, LocalDifficulty difficulty, SpawnReason spawnReason, @Nullable EntityData entityData) {
        this.circlingCenter = this.getBlockPos().up(5);
        this.setPhantomSize(0);
        return super.initialize(world, difficulty, spawnReason, entityData);
    }

    @Override
    protected void readCustomData(ReadView view) {
        super.readCustomData(view);
        this.circlingCenter = view.read("anchor_pos", BlockPos.CODEC).orElse(null);
        this.setPhantomSize(view.getInt("size", 0));
    }

    @Override
    protected void writeCustomData(WriteView view) {
        super.writeCustomData(view);
        view.putNullable("anchor_pos", BlockPos.CODEC, this.circlingCenter);
        view.putInt("size", this.getPhantomSize());
    }

    @Override
    public boolean shouldRender(double distance) {
        return true;
    }

    @Override
    public SoundCategory getSoundCategory() {
        return SoundCategory.HOSTILE;
    }

    @Override
    protected SoundEvent getAmbientSound() {
        return SoundEvents.ENTITY_PHANTOM_AMBIENT;
    }

    @Override
    protected SoundEvent getHurtSound(DamageSource source) {
        return SoundEvents.ENTITY_PHANTOM_HURT;
    }

    @Override
    protected SoundEvent getDeathSound() {
        return SoundEvents.ENTITY_PHANTOM_DEATH;
    }

    @Override
    protected float getSoundVolume() {
        return 1.0f;
    }

    @Override
    public boolean canTarget(EntityType<?> type) {
        return true;
    }

    @Override
    public EntityDimensions getBaseDimensions(EntityPose pose) {
        int i = this.getPhantomSize();
        EntityDimensions entityDimensions = super.getBaseDimensions(pose);
        return entityDimensions.scaled(1.0f + 0.15f * (float)i);
    }

    boolean testTargetPredicate(ServerWorld world, LivingEntity target, TargetPredicate predicate) {
        return predicate.test(world, this, target);
    }

    static final class PhantomMovementType
    extends Enum<PhantomMovementType> {
        public static final /* enum */ PhantomMovementType CIRCLE = new PhantomMovementType();
        public static final /* enum */ PhantomMovementType SWOOP = new PhantomMovementType();
        private static final /* synthetic */ PhantomMovementType[] field_7316;

        public static PhantomMovementType[] values() {
            return (PhantomMovementType[])field_7316.clone();
        }

        public static PhantomMovementType valueOf(String string) {
            return Enum.valueOf(PhantomMovementType.class, string);
        }

        private static /* synthetic */ PhantomMovementType[] method_36653() {
            return new PhantomMovementType[]{CIRCLE, SWOOP};
        }

        static {
            field_7316 = PhantomMovementType.method_36653();
        }
    }

    class PhantomMoveControl
    extends MoveControl {
        private float targetSpeed;

        public PhantomMoveControl(MobEntity owner) {
            super(owner);
            this.targetSpeed = 0.1f;
        }

        @Override
        public void tick() {
            if (PhantomEntity.this.horizontalCollision) {
                PhantomEntity.this.setYaw(PhantomEntity.this.getYaw() + 180.0f);
                this.targetSpeed = 0.1f;
            }
            double d = PhantomEntity.this.targetPosition.x - PhantomEntity.this.getX();
            double e = PhantomEntity.this.targetPosition.y - PhantomEntity.this.getY();
            double f = PhantomEntity.this.targetPosition.z - PhantomEntity.this.getZ();
            double g = Math.sqrt(d * d + f * f);
            if (Math.abs(g) > (double)1.0E-5f) {
                double h = 1.0 - Math.abs(e * (double)0.7f) / g;
                g = Math.sqrt((d *= h) * d + (f *= h) * f);
                double i = Math.sqrt(d * d + f * f + e * e);
                float j = PhantomEntity.this.getYaw();
                float k = (float)MathHelper.atan2(f, d);
                float l = MathHelper.wrapDegrees(PhantomEntity.this.getYaw() + 90.0f);
                float m = MathHelper.wrapDegrees(k * 57.295776f);
                PhantomEntity.this.setYaw(MathHelper.stepUnwrappedAngleTowards(l, m, 4.0f) - 90.0f);
                PhantomEntity.this.bodyYaw = PhantomEntity.this.getYaw();
                this.targetSpeed = MathHelper.angleBetween(j, PhantomEntity.this.getYaw()) < 3.0f ? MathHelper.stepTowards(this.targetSpeed, 1.8f, 0.005f * (1.8f / this.targetSpeed)) : MathHelper.stepTowards(this.targetSpeed, 0.2f, 0.025f);
                float n = (float)(-(MathHelper.atan2(-e, g) * 57.2957763671875));
                PhantomEntity.this.setPitch(n);
                float o = PhantomEntity.this.getYaw() + 90.0f;
                double p = (double)(this.targetSpeed * MathHelper.cos(o * ((float)Math.PI / 180))) * Math.abs(d / i);
                double q = (double)(this.targetSpeed * MathHelper.sin(o * ((float)Math.PI / 180))) * Math.abs(f / i);
                double r = (double)(this.targetSpeed * MathHelper.sin(n * ((float)Math.PI / 180))) * Math.abs(e / i);
                Vec3d vec3d = PhantomEntity.this.getVelocity();
                PhantomEntity.this.setVelocity(vec3d.add(new Vec3d(p, r, q).subtract(vec3d).multiply(0.2)));
            }
        }
    }

    static class PhantomLookControl
    extends LookControl {
        public PhantomLookControl(MobEntity mobEntity) {
            super(mobEntity);
        }

        @Override
        public void tick() {
        }
    }

    class PhantomBodyControl
    extends BodyControl {
        public PhantomBodyControl(MobEntity entity) {
            super(entity);
        }

        @Override
        public void tick() {
            PhantomEntity.this.headYaw = PhantomEntity.this.bodyYaw;
            PhantomEntity.this.bodyYaw = PhantomEntity.this.getYaw();
        }
    }

    class StartAttackGoal
    extends Goal {
        private int cooldown;

        StartAttackGoal() {
        }

        @Override
        public boolean canStart() {
            LivingEntity livingEntity = PhantomEntity.this.getTarget();
            if (livingEntity != null) {
                return PhantomEntity.this.testTargetPredicate(StartAttackGoal.castToServerWorld(PhantomEntity.this.getEntityWorld()), livingEntity, TargetPredicate.DEFAULT);
            }
            return false;
        }

        @Override
        public void start() {
            this.cooldown = this.getTickCount(10);
            PhantomEntity.this.movementType = PhantomMovementType.CIRCLE;
            this.startSwoop();
        }

        @Override
        public void stop() {
            if (PhantomEntity.this.circlingCenter != null) {
                PhantomEntity.this.circlingCenter = PhantomEntity.this.getEntityWorld().getTopPosition(Heightmap.Type.MOTION_BLOCKING, PhantomEntity.this.circlingCenter).up(10 + PhantomEntity.this.random.nextInt(20));
            }
        }

        @Override
        public void tick() {
            if (PhantomEntity.this.movementType == PhantomMovementType.CIRCLE) {
                --this.cooldown;
                if (this.cooldown <= 0) {
                    PhantomEntity.this.movementType = PhantomMovementType.SWOOP;
                    this.startSwoop();
                    this.cooldown = this.getTickCount((8 + PhantomEntity.this.random.nextInt(4)) * 20);
                    PhantomEntity.this.playSound(SoundEvents.ENTITY_PHANTOM_SWOOP, 10.0f, 0.95f + PhantomEntity.this.random.nextFloat() * 0.1f);
                }
            }
        }

        private void startSwoop() {
            if (PhantomEntity.this.circlingCenter == null) {
                return;
            }
            PhantomEntity.this.circlingCenter = PhantomEntity.this.getTarget().getBlockPos().up(20 + PhantomEntity.this.random.nextInt(20));
            if (PhantomEntity.this.circlingCenter.getY() < PhantomEntity.this.getEntityWorld().getSeaLevel()) {
                PhantomEntity.this.circlingCenter = new BlockPos(PhantomEntity.this.circlingCenter.getX(), PhantomEntity.this.getEntityWorld().getSeaLevel() + 1, PhantomEntity.this.circlingCenter.getZ());
            }
        }
    }

    class SwoopMovementGoal
    extends MovementGoal {
        private static final int CAT_CHECK_INTERVAL = 20;
        private boolean catsNearby;
        private int nextCatCheckAge;

        SwoopMovementGoal() {
        }

        @Override
        public boolean canStart() {
            return PhantomEntity.this.getTarget() != null && PhantomEntity.this.movementType == PhantomMovementType.SWOOP;
        }

        @Override
        public boolean shouldContinue() {
            LivingEntity livingEntity = PhantomEntity.this.getTarget();
            if (livingEntity == null) {
                return false;
            }
            if (!livingEntity.isAlive()) {
                return false;
            }
            if (livingEntity instanceof PlayerEntity) {
                PlayerEntity playerEntity = (PlayerEntity)livingEntity;
                if (livingEntity.isSpectator() || playerEntity.isCreative()) {
                    return false;
                }
            }
            if (!this.canStart()) {
                return false;
            }
            if (PhantomEntity.this.age > this.nextCatCheckAge) {
                this.nextCatCheckAge = PhantomEntity.this.age + 20;
                List<Entity> list = PhantomEntity.this.getEntityWorld().getEntitiesByClass(CatEntity.class, PhantomEntity.this.getBoundingBox().expand(16.0), EntityPredicates.VALID_ENTITY);
                for (CatEntity catEntity : list) {
                    catEntity.hiss();
                }
                this.catsNearby = !list.isEmpty();
            }
            return !this.catsNearby;
        }

        @Override
        public void start() {
        }

        @Override
        public void stop() {
            PhantomEntity.this.setTarget(null);
            PhantomEntity.this.movementType = PhantomMovementType.CIRCLE;
        }

        @Override
        public void tick() {
            LivingEntity livingEntity = PhantomEntity.this.getTarget();
            if (livingEntity == null) {
                return;
            }
            PhantomEntity.this.targetPosition = new Vec3d(livingEntity.getX(), livingEntity.getBodyY(0.5), livingEntity.getZ());
            if (PhantomEntity.this.getBoundingBox().expand(0.2f).intersects(livingEntity.getBoundingBox())) {
                PhantomEntity.this.tryAttack(SwoopMovementGoal.castToServerWorld(PhantomEntity.this.getEntityWorld()), livingEntity);
                PhantomEntity.this.movementType = PhantomMovementType.CIRCLE;
                if (!PhantomEntity.this.isSilent()) {
                    PhantomEntity.this.getEntityWorld().syncWorldEvent(1039, PhantomEntity.this.getBlockPos(), 0);
                }
            } else if (PhantomEntity.this.horizontalCollision || PhantomEntity.this.hurtTime > 0) {
                PhantomEntity.this.movementType = PhantomMovementType.CIRCLE;
            }
        }
    }

    class CircleMovementGoal
    extends MovementGoal {
        private float angle;
        private float radius;
        private float yOffset;
        private float circlingDirection;

        CircleMovementGoal() {
        }

        @Override
        public boolean canStart() {
            return PhantomEntity.this.getTarget() == null || PhantomEntity.this.movementType == PhantomMovementType.CIRCLE;
        }

        @Override
        public void start() {
            this.radius = 5.0f + PhantomEntity.this.random.nextFloat() * 10.0f;
            this.yOffset = -4.0f + PhantomEntity.this.random.nextFloat() * 9.0f;
            this.circlingDirection = PhantomEntity.this.random.nextBoolean() ? 1.0f : -1.0f;
            this.adjustDirection();
        }

        @Override
        public void tick() {
            if (PhantomEntity.this.random.nextInt(this.getTickCount(350)) == 0) {
                this.yOffset = -4.0f + PhantomEntity.this.random.nextFloat() * 9.0f;
            }
            if (PhantomEntity.this.random.nextInt(this.getTickCount(250)) == 0) {
                this.radius += 1.0f;
                if (this.radius > 15.0f) {
                    this.radius = 5.0f;
                    this.circlingDirection = -this.circlingDirection;
                }
            }
            if (PhantomEntity.this.random.nextInt(this.getTickCount(450)) == 0) {
                this.angle = PhantomEntity.this.random.nextFloat() * 2.0f * (float)Math.PI;
                this.adjustDirection();
            }
            if (this.isNearTarget()) {
                this.adjustDirection();
            }
            if (PhantomEntity.this.targetPosition.y < PhantomEntity.this.getY() && !PhantomEntity.this.getEntityWorld().isAir(PhantomEntity.this.getBlockPos().down(1))) {
                this.yOffset = Math.max(1.0f, this.yOffset);
                this.adjustDirection();
            }
            if (PhantomEntity.this.targetPosition.y > PhantomEntity.this.getY() && !PhantomEntity.this.getEntityWorld().isAir(PhantomEntity.this.getBlockPos().up(1))) {
                this.yOffset = Math.min(-1.0f, this.yOffset);
                this.adjustDirection();
            }
        }

        private void adjustDirection() {
            if (PhantomEntity.this.circlingCenter == null) {
                PhantomEntity.this.circlingCenter = PhantomEntity.this.getBlockPos();
            }
            this.angle += this.circlingDirection * 15.0f * ((float)Math.PI / 180);
            PhantomEntity.this.targetPosition = Vec3d.of(PhantomEntity.this.circlingCenter).add(this.radius * MathHelper.cos(this.angle), -4.0f + this.yOffset, this.radius * MathHelper.sin(this.angle));
        }
    }

    class FindTargetGoal
    extends Goal {
        private final TargetPredicate PLAYERS_IN_RANGE_PREDICATE = TargetPredicate.createAttackable().setBaseMaxDistance(64.0);
        private int delay = FindTargetGoal.toGoalTicks(20);

        FindTargetGoal() {
        }

        @Override
        public boolean canStart() {
            if (this.delay > 0) {
                --this.delay;
                return false;
            }
            this.delay = FindTargetGoal.toGoalTicks(60);
            ServerWorld serverWorld = FindTargetGoal.castToServerWorld(PhantomEntity.this.getEntityWorld());
            List<PlayerEntity> list = serverWorld.getPlayers(this.PLAYERS_IN_RANGE_PREDICATE, PhantomEntity.this, PhantomEntity.this.getBoundingBox().expand(16.0, 64.0, 16.0));
            if (!list.isEmpty()) {
                list.sort(Comparator.comparing(Entity::getY).reversed());
                for (PlayerEntity playerEntity : list) {
                    if (!PhantomEntity.this.testTargetPredicate(serverWorld, playerEntity, TargetPredicate.DEFAULT)) continue;
                    PhantomEntity.this.setTarget(playerEntity);
                    return true;
                }
            }
            return false;
        }

        @Override
        public boolean shouldContinue() {
            LivingEntity livingEntity = PhantomEntity.this.getTarget();
            if (livingEntity != null) {
                return PhantomEntity.this.testTargetPredicate(FindTargetGoal.castToServerWorld(PhantomEntity.this.getEntityWorld()), livingEntity, TargetPredicate.DEFAULT);
            }
            return false;
        }
    }

    abstract class MovementGoal
    extends Goal {
        public MovementGoal() {
            this.setControls(EnumSet.of(Goal.Control.MOVE));
        }

        protected boolean isNearTarget() {
            return PhantomEntity.this.targetPosition.squaredDistanceTo(PhantomEntity.this.getX(), PhantomEntity.this.getY(), PhantomEntity.this.getZ()) < 4.0;
        }
    }
}


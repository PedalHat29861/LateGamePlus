/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  org.joml.Vector3f
 *  org.joml.Vector3fc
 *  org.jspecify.annotations.Nullable
 */
package net.minecraft.entity.mob;

import java.util.EnumSet;
import java.util.List;
import java.util.Optional;
import net.minecraft.block.BlockState;
import net.minecraft.block.Blocks;
import net.minecraft.component.ComponentType;
import net.minecraft.component.ComponentsAccess;
import net.minecraft.component.DataComponentTypes;
import net.minecraft.entity.Entity;
import net.minecraft.entity.EntityData;
import net.minecraft.entity.EntityType;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.MovementType;
import net.minecraft.entity.PositionInterpolator;
import net.minecraft.entity.SpawnReason;
import net.minecraft.entity.ai.control.BodyControl;
import net.minecraft.entity.ai.control.LookControl;
import net.minecraft.entity.ai.goal.ActiveTargetGoal;
import net.minecraft.entity.ai.goal.Goal;
import net.minecraft.entity.ai.goal.LookAroundGoal;
import net.minecraft.entity.ai.goal.LookAtEntityGoal;
import net.minecraft.entity.ai.goal.RevengeGoal;
import net.minecraft.entity.attribute.DefaultAttributeContainer;
import net.minecraft.entity.attribute.EntityAttributeModifier;
import net.minecraft.entity.attribute.EntityAttributes;
import net.minecraft.entity.damage.DamageSource;
import net.minecraft.entity.data.DataTracker;
import net.minecraft.entity.data.TrackedData;
import net.minecraft.entity.data.TrackedDataHandlerRegistry;
import net.minecraft.entity.mob.MobEntity;
import net.minecraft.entity.mob.Monster;
import net.minecraft.entity.passive.GolemEntity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.entity.projectile.PersistentProjectileEntity;
import net.minecraft.entity.projectile.ShulkerBulletEntity;
import net.minecraft.network.packet.s2c.play.EntitySpawnS2CPacket;
import net.minecraft.predicate.entity.EntityPredicates;
import net.minecraft.registry.tag.DamageTypeTags;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.sound.SoundCategory;
import net.minecraft.sound.SoundEvent;
import net.minecraft.sound.SoundEvents;
import net.minecraft.storage.ReadView;
import net.minecraft.storage.WriteView;
import net.minecraft.util.DyeColor;
import net.minecraft.util.Identifier;
import net.minecraft.util.Util;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Box;
import net.minecraft.util.math.Direction;
import net.minecraft.util.math.MathHelper;
import net.minecraft.util.math.Vec3d;
import net.minecraft.util.math.Vec3i;
import net.minecraft.world.Difficulty;
import net.minecraft.world.LocalDifficulty;
import net.minecraft.world.ServerWorldAccess;
import net.minecraft.world.World;
import net.minecraft.world.event.GameEvent;
import org.joml.Vector3f;
import org.joml.Vector3fc;
import org.jspecify.annotations.Nullable;

public class ShulkerEntity
extends GolemEntity
implements Monster {
    private static final Identifier COVERED_ARMOR_MODIFIER_ID = Identifier.ofVanilla("covered");
    private static final EntityAttributeModifier COVERED_ARMOR_BONUS = new EntityAttributeModifier(COVERED_ARMOR_MODIFIER_ID, 20.0, EntityAttributeModifier.Operation.ADD_VALUE);
    protected static final TrackedData<Direction> ATTACHED_FACE = DataTracker.registerData(ShulkerEntity.class, TrackedDataHandlerRegistry.FACING);
    protected static final TrackedData<Byte> PEEK_AMOUNT = DataTracker.registerData(ShulkerEntity.class, TrackedDataHandlerRegistry.BYTE);
    protected static final TrackedData<Byte> COLOR = DataTracker.registerData(ShulkerEntity.class, TrackedDataHandlerRegistry.BYTE);
    private static final int field_30487 = 6;
    private static final byte field_30488 = 16;
    private static final byte field_30489 = 16;
    private static final int field_30490 = 8;
    private static final int field_30491 = 8;
    private static final int field_30492 = 5;
    private static final float field_30493 = 0.05f;
    private static final byte DEFAULT_PEEK = 0;
    private static final Direction DEFAULT_ATTACHED_FACE = Direction.DOWN;
    static final Vector3f SOUTH_VECTOR = Util.make(() -> {
        Vec3i vec3i = Direction.SOUTH.getVector();
        return new Vector3f((float)vec3i.getX(), (float)vec3i.getY(), (float)vec3i.getZ());
    });
    private static final float field_48343 = 3.0f;
    private float lastOpenProgress;
    private float openProgress;
    private @Nullable BlockPos lastAttachedBlock;
    private int teleportLerpTimer;
    private static final float field_30494 = 1.0f;

    public ShulkerEntity(EntityType<? extends ShulkerEntity> entityType, World world) {
        super((EntityType<? extends GolemEntity>)entityType, world);
        this.experiencePoints = 5;
        this.lookControl = new ShulkerLookControl(this);
    }

    @Override
    protected void initGoals() {
        this.goalSelector.add(1, new LookAtEntityGoal(this, PlayerEntity.class, 8.0f, 0.02f, true));
        this.goalSelector.add(4, new ShootBulletGoal());
        this.goalSelector.add(7, new PeekGoal());
        this.goalSelector.add(8, new LookAroundGoal(this));
        this.targetSelector.add(1, new RevengeGoal(this, this.getClass()).setGroupRevenge(new Class[0]));
        this.targetSelector.add(2, new TargetPlayerGoal(this));
        this.targetSelector.add(3, new TargetOtherTeamGoal(this));
    }

    @Override
    protected Entity.MoveEffect getMoveEffect() {
        return Entity.MoveEffect.NONE;
    }

    @Override
    public SoundCategory getSoundCategory() {
        return SoundCategory.HOSTILE;
    }

    @Override
    protected SoundEvent getAmbientSound() {
        return SoundEvents.ENTITY_SHULKER_AMBIENT;
    }

    @Override
    public void playAmbientSound() {
        if (!this.isClosed()) {
            super.playAmbientSound();
        }
    }

    @Override
    protected SoundEvent getDeathSound() {
        return SoundEvents.ENTITY_SHULKER_DEATH;
    }

    @Override
    protected SoundEvent getHurtSound(DamageSource source) {
        if (this.isClosed()) {
            return SoundEvents.ENTITY_SHULKER_HURT_CLOSED;
        }
        return SoundEvents.ENTITY_SHULKER_HURT;
    }

    @Override
    protected void initDataTracker(DataTracker.Builder builder) {
        super.initDataTracker(builder);
        builder.add(ATTACHED_FACE, DEFAULT_ATTACHED_FACE);
        builder.add(PEEK_AMOUNT, (byte)0);
        builder.add(COLOR, (byte)16);
    }

    public static DefaultAttributeContainer.Builder createShulkerAttributes() {
        return MobEntity.createMobAttributes().add(EntityAttributes.MAX_HEALTH, 30.0);
    }

    @Override
    protected BodyControl createBodyControl() {
        return new ShulkerBodyControl(this);
    }

    @Override
    protected void readCustomData(ReadView view) {
        super.readCustomData(view);
        this.setAttachedFace(view.read("AttachFace", Direction.INDEX_CODEC).orElse(DEFAULT_ATTACHED_FACE));
        this.dataTracker.set(PEEK_AMOUNT, view.getByte("Peek", (byte)0));
        this.dataTracker.set(COLOR, view.getByte("Color", (byte)16));
    }

    @Override
    protected void writeCustomData(WriteView view) {
        super.writeCustomData(view);
        view.put("AttachFace", Direction.INDEX_CODEC, this.getAttachedFace());
        view.putByte("Peek", this.dataTracker.get(PEEK_AMOUNT));
        view.putByte("Color", this.dataTracker.get(COLOR));
    }

    @Override
    public void tick() {
        super.tick();
        if (!(this.getEntityWorld().isClient() || this.hasVehicle() || this.canStay(this.getBlockPos(), this.getAttachedFace()))) {
            this.tryAttachOrTeleport();
        }
        if (this.tickOpenProgress()) {
            this.moveEntities();
        }
        if (this.getEntityWorld().isClient()) {
            if (this.teleportLerpTimer > 0) {
                --this.teleportLerpTimer;
            } else {
                this.lastAttachedBlock = null;
            }
        }
    }

    private void tryAttachOrTeleport() {
        Direction direction = this.findAttachSide(this.getBlockPos());
        if (direction != null) {
            this.setAttachedFace(direction);
        } else {
            this.tryTeleport();
        }
    }

    @Override
    protected Box calculateDefaultBoundingBox(Vec3d pos) {
        float f = ShulkerEntity.getExtraLength(this.openProgress);
        Direction direction = this.getAttachedFace().getOpposite();
        return ShulkerEntity.calculateBoundingBox(this.getScale(), direction, f, pos);
    }

    private static float getExtraLength(float openProgress) {
        return 0.5f - MathHelper.sin((0.5f + openProgress) * (float)Math.PI) * 0.5f;
    }

    private boolean tickOpenProgress() {
        this.lastOpenProgress = this.openProgress;
        float f = (float)this.getPeekAmount() * 0.01f;
        if (this.openProgress == f) {
            return false;
        }
        this.openProgress = this.openProgress > f ? MathHelper.clamp(this.openProgress - 0.05f, f, 1.0f) : MathHelper.clamp(this.openProgress + 0.05f, 0.0f, f);
        return true;
    }

    private void moveEntities() {
        this.refreshPosition();
        float f = ShulkerEntity.getExtraLength(this.openProgress);
        float g = ShulkerEntity.getExtraLength(this.lastOpenProgress);
        Direction direction = this.getAttachedFace().getOpposite();
        float h = (f - g) * this.getScale();
        if (h <= 0.0f) {
            return;
        }
        List<Entity> list = this.getEntityWorld().getOtherEntities(this, ShulkerEntity.calculateBoundingBox(this.getScale(), direction, g, f, this.getEntityPos()), EntityPredicates.EXCEPT_SPECTATOR.and(entity -> !entity.isConnectedThroughVehicle(this)));
        for (Entity entity2 : list) {
            if (entity2 instanceof ShulkerEntity || entity2.noClip) continue;
            entity2.move(MovementType.SHULKER, new Vec3d(h * (float)direction.getOffsetX(), h * (float)direction.getOffsetY(), h * (float)direction.getOffsetZ()));
        }
    }

    public static Box calculateBoundingBox(float scale, Direction facing, float extraLength, Vec3d pos) {
        return ShulkerEntity.calculateBoundingBox(scale, facing, -1.0f, extraLength, pos);
    }

    public static Box calculateBoundingBox(float scale, Direction facing, float lastExtraLength, float extraLength, Vec3d pos) {
        Box box = new Box((double)(-scale) * 0.5, 0.0, (double)(-scale) * 0.5, (double)scale * 0.5, scale, (double)scale * 0.5);
        double d = Math.max(lastExtraLength, extraLength);
        double e = Math.min(lastExtraLength, extraLength);
        Box box2 = box.stretch((double)facing.getOffsetX() * d * (double)scale, (double)facing.getOffsetY() * d * (double)scale, (double)facing.getOffsetZ() * d * (double)scale).shrink((double)(-facing.getOffsetX()) * (1.0 + e) * (double)scale, (double)(-facing.getOffsetY()) * (1.0 + e) * (double)scale, (double)(-facing.getOffsetZ()) * (1.0 + e) * (double)scale);
        return box2.offset(pos.x, pos.y, pos.z);
    }

    @Override
    public boolean startRiding(Entity entity, boolean force, boolean emitEvent) {
        if (this.getEntityWorld().isClient()) {
            this.lastAttachedBlock = null;
            this.teleportLerpTimer = 0;
        }
        this.setAttachedFace(Direction.DOWN);
        return super.startRiding(entity, force, emitEvent);
    }

    @Override
    public void stopRiding() {
        super.stopRiding();
        if (this.getEntityWorld().isClient()) {
            this.lastAttachedBlock = this.getBlockPos();
        }
        this.lastBodyYaw = 0.0f;
        this.bodyYaw = 0.0f;
    }

    @Override
    public @Nullable EntityData initialize(ServerWorldAccess world, LocalDifficulty difficulty, SpawnReason spawnReason, @Nullable EntityData entityData) {
        this.setYaw(0.0f);
        this.headYaw = this.getYaw();
        this.resetPosition();
        return super.initialize(world, difficulty, spawnReason, entityData);
    }

    @Override
    public void move(MovementType type, Vec3d movement) {
        if (type == MovementType.SHULKER_BOX) {
            this.tryTeleport();
        } else {
            super.move(type, movement);
        }
    }

    @Override
    public Vec3d getVelocity() {
        return Vec3d.ZERO;
    }

    @Override
    public void setVelocity(Vec3d velocity) {
    }

    @Override
    public void setPosition(double x, double y, double z) {
        BlockPos blockPos = this.getBlockPos();
        if (this.hasVehicle()) {
            super.setPosition(x, y, z);
        } else {
            super.setPosition((double)MathHelper.floor(x) + 0.5, MathHelper.floor(y + 0.5), (double)MathHelper.floor(z) + 0.5);
        }
        if (this.age == 0) {
            return;
        }
        BlockPos blockPos2 = this.getBlockPos();
        if (!blockPos2.equals(blockPos)) {
            this.dataTracker.set(PEEK_AMOUNT, (byte)0);
            this.velocityDirty = true;
            if (this.getEntityWorld().isClient() && !this.hasVehicle() && !blockPos2.equals(this.lastAttachedBlock)) {
                this.lastAttachedBlock = blockPos;
                this.teleportLerpTimer = 6;
                this.lastRenderX = this.getX();
                this.lastRenderY = this.getY();
                this.lastRenderZ = this.getZ();
            }
        }
    }

    protected @Nullable Direction findAttachSide(BlockPos pos) {
        for (Direction direction : Direction.values()) {
            if (!this.canStay(pos, direction)) continue;
            return direction;
        }
        return null;
    }

    boolean canStay(BlockPos pos, Direction direction) {
        if (this.isInvalidPosition(pos)) {
            return false;
        }
        Direction direction2 = direction.getOpposite();
        if (!this.getEntityWorld().isDirectionSolid(pos.offset(direction), this, direction2)) {
            return false;
        }
        Box box = ShulkerEntity.calculateBoundingBox(this.getScale(), direction2, 1.0f, pos.toBottomCenterPos()).contract(1.0E-6);
        return this.getEntityWorld().isSpaceEmpty(this, box);
    }

    private boolean isInvalidPosition(BlockPos pos) {
        BlockState blockState = this.getEntityWorld().getBlockState(pos);
        if (blockState.isAir()) {
            return false;
        }
        boolean bl = blockState.isOf(Blocks.MOVING_PISTON) && pos.equals(this.getBlockPos());
        return !bl;
    }

    protected boolean tryTeleport() {
        if (this.isAiDisabled() || !this.isAlive()) {
            return false;
        }
        BlockPos blockPos = this.getBlockPos();
        for (int i = 0; i < 5; ++i) {
            Direction direction;
            BlockPos blockPos2 = blockPos.add(MathHelper.nextBetween(this.random, -8, 8), MathHelper.nextBetween(this.random, -8, 8), MathHelper.nextBetween(this.random, -8, 8));
            if (blockPos2.getY() <= this.getEntityWorld().getBottomY() || !this.getEntityWorld().isAir(blockPos2) || !this.getEntityWorld().getWorldBorder().contains(blockPos2) || !this.getEntityWorld().isSpaceEmpty(this, new Box(blockPos2).contract(1.0E-6)) || (direction = this.findAttachSide(blockPos2)) == null) continue;
            this.detach();
            this.setAttachedFace(direction);
            this.playSound(SoundEvents.ENTITY_SHULKER_TELEPORT, 1.0f, 1.0f);
            this.setPosition((double)blockPos2.getX() + 0.5, blockPos2.getY(), (double)blockPos2.getZ() + 0.5);
            this.getEntityWorld().emitGameEvent(GameEvent.TELEPORT, blockPos, GameEvent.Emitter.of(this));
            this.dataTracker.set(PEEK_AMOUNT, (byte)0);
            this.setTarget(null);
            return true;
        }
        return false;
    }

    @Override
    public PositionInterpolator getInterpolator() {
        return null;
    }

    @Override
    public boolean damage(ServerWorld world, DamageSource source, float amount) {
        Entity entity;
        if (this.isClosed() && (entity = source.getSource()) instanceof PersistentProjectileEntity) {
            return false;
        }
        if (super.damage(world, source, amount)) {
            if ((double)this.getHealth() < (double)this.getMaxHealth() * 0.5 && this.random.nextInt(4) == 0) {
                this.tryTeleport();
            } else if (source.isIn(DamageTypeTags.IS_PROJECTILE) && (entity = source.getSource()) != null && entity.getType() == EntityType.SHULKER_BULLET) {
                this.spawnNewShulker();
            }
            return true;
        }
        return false;
    }

    private boolean isClosed() {
        return this.getPeekAmount() == 0;
    }

    private void spawnNewShulker() {
        Vec3d vec3d = this.getEntityPos();
        Box box = this.getBoundingBox();
        if (this.isClosed() || !this.tryTeleport()) {
            return;
        }
        int i = this.getEntityWorld().getEntitiesByType(EntityType.SHULKER, box.expand(8.0), Entity::isAlive).size();
        float f = (float)(i - 1) / 5.0f;
        if (this.getEntityWorld().random.nextFloat() < f) {
            return;
        }
        ShulkerEntity shulkerEntity = EntityType.SHULKER.create(this.getEntityWorld(), SpawnReason.BREEDING);
        if (shulkerEntity != null) {
            shulkerEntity.setColor(this.getColorOptional());
            shulkerEntity.refreshPositionAfterTeleport(vec3d);
            this.getEntityWorld().spawnEntity(shulkerEntity);
        }
    }

    @Override
    public boolean isCollidable(@Nullable Entity entity) {
        return this.isAlive();
    }

    public Direction getAttachedFace() {
        return this.dataTracker.get(ATTACHED_FACE);
    }

    private void setAttachedFace(Direction face) {
        this.dataTracker.set(ATTACHED_FACE, face);
    }

    @Override
    public void onTrackedDataSet(TrackedData<?> data) {
        if (ATTACHED_FACE.equals(data)) {
            this.setBoundingBox(this.calculateBoundingBox());
        }
        super.onTrackedDataSet(data);
    }

    private int getPeekAmount() {
        return this.dataTracker.get(PEEK_AMOUNT).byteValue();
    }

    void setPeekAmount(int peekAmount) {
        if (!this.getEntityWorld().isClient()) {
            this.getAttributeInstance(EntityAttributes.ARMOR).removeModifier(COVERED_ARMOR_MODIFIER_ID);
            if (peekAmount == 0) {
                this.getAttributeInstance(EntityAttributes.ARMOR).addPersistentModifier(COVERED_ARMOR_BONUS);
                this.playSound(SoundEvents.ENTITY_SHULKER_CLOSE, 1.0f, 1.0f);
                this.emitGameEvent(GameEvent.CONTAINER_CLOSE);
            } else {
                this.playSound(SoundEvents.ENTITY_SHULKER_OPEN, 1.0f, 1.0f);
                this.emitGameEvent(GameEvent.CONTAINER_OPEN);
            }
        }
        this.dataTracker.set(PEEK_AMOUNT, (byte)peekAmount);
    }

    public float getOpenProgress(float tickProgress) {
        return MathHelper.lerp(tickProgress, this.lastOpenProgress, this.openProgress);
    }

    @Override
    public void onSpawnPacket(EntitySpawnS2CPacket packet) {
        super.onSpawnPacket(packet);
        this.bodyYaw = 0.0f;
        this.lastBodyYaw = 0.0f;
    }

    @Override
    public int getMaxLookPitchChange() {
        return 180;
    }

    @Override
    public int getMaxHeadRotation() {
        return 180;
    }

    @Override
    public void pushAwayFrom(Entity entity) {
    }

    public @Nullable Vec3d getRenderPositionOffset(float tickProgress) {
        if (this.lastAttachedBlock == null || this.teleportLerpTimer <= 0) {
            return null;
        }
        double d = (double)((float)this.teleportLerpTimer - tickProgress) / 6.0;
        d *= d;
        BlockPos blockPos = this.getBlockPos();
        double e = (double)(blockPos.getX() - this.lastAttachedBlock.getX()) * (d *= (double)this.getScale());
        double f = (double)(blockPos.getY() - this.lastAttachedBlock.getY()) * d;
        double g = (double)(blockPos.getZ() - this.lastAttachedBlock.getZ()) * d;
        return new Vec3d(-e, -f, -g);
    }

    @Override
    protected float clampScale(float scale) {
        return Math.min(scale, 3.0f);
    }

    private void setColor(Optional<DyeColor> color2) {
        this.dataTracker.set(COLOR, color2.map(color -> (byte)color.getIndex()).orElse((byte)16));
    }

    public Optional<DyeColor> getColorOptional() {
        return Optional.ofNullable(this.getColor());
    }

    public @Nullable DyeColor getColor() {
        byte b = this.dataTracker.get(COLOR);
        if (b == 16 || b > 15) {
            return null;
        }
        return DyeColor.byIndex(b);
    }

    @Override
    public <T> @Nullable T get(ComponentType<? extends T> type) {
        if (type == DataComponentTypes.SHULKER_COLOR) {
            return ShulkerEntity.castComponentValue(type, this.getColor());
        }
        return super.get(type);
    }

    @Override
    protected void copyComponentsFrom(ComponentsAccess from) {
        this.copyComponentFrom(from, DataComponentTypes.SHULKER_COLOR);
        super.copyComponentsFrom(from);
    }

    @Override
    protected <T> boolean setApplicableComponent(ComponentType<T> type, T value) {
        if (type == DataComponentTypes.SHULKER_COLOR) {
            this.setColor(Optional.of(ShulkerEntity.castComponentValue(DataComponentTypes.SHULKER_COLOR, value)));
            return true;
        }
        return super.setApplicableComponent(type, value);
    }

    class ShulkerLookControl
    extends LookControl {
        public ShulkerLookControl(MobEntity entity) {
            super(entity);
        }

        @Override
        protected void clampHeadYaw() {
        }

        @Override
        protected Optional<Float> getTargetYaw() {
            Direction direction = ShulkerEntity.this.getAttachedFace().getOpposite();
            Vector3f vector3f = direction.getRotationQuaternion().transform(new Vector3f((Vector3fc)SOUTH_VECTOR));
            Vec3i vec3i = direction.getVector();
            Vector3f vector3f2 = new Vector3f((float)vec3i.getX(), (float)vec3i.getY(), (float)vec3i.getZ());
            vector3f2.cross((Vector3fc)vector3f);
            double d = this.x - this.entity.getX();
            double e = this.y - this.entity.getEyeY();
            double f = this.z - this.entity.getZ();
            Vector3f vector3f3 = new Vector3f((float)d, (float)e, (float)f);
            float g = vector3f2.dot((Vector3fc)vector3f3);
            float h = vector3f.dot((Vector3fc)vector3f3);
            return Math.abs(g) > 1.0E-5f || Math.abs(h) > 1.0E-5f ? Optional.of(Float.valueOf((float)(MathHelper.atan2(-g, h) * 57.2957763671875))) : Optional.empty();
        }

        @Override
        protected Optional<Float> getTargetPitch() {
            return Optional.of(Float.valueOf(0.0f));
        }
    }

    class ShootBulletGoal
    extends Goal {
        private int counter;

        public ShootBulletGoal() {
            this.setControls(EnumSet.of(Goal.Control.MOVE, Goal.Control.LOOK));
        }

        @Override
        public boolean canStart() {
            LivingEntity livingEntity = ShulkerEntity.this.getTarget();
            if (livingEntity == null || !livingEntity.isAlive()) {
                return false;
            }
            return ShulkerEntity.this.getEntityWorld().getDifficulty() != Difficulty.PEACEFUL;
        }

        @Override
        public void start() {
            this.counter = 20;
            ShulkerEntity.this.setPeekAmount(100);
        }

        @Override
        public void stop() {
            ShulkerEntity.this.setPeekAmount(0);
        }

        @Override
        public boolean shouldRunEveryTick() {
            return true;
        }

        @Override
        public void tick() {
            if (ShulkerEntity.this.getEntityWorld().getDifficulty() == Difficulty.PEACEFUL) {
                return;
            }
            --this.counter;
            LivingEntity livingEntity = ShulkerEntity.this.getTarget();
            if (livingEntity == null) {
                return;
            }
            ShulkerEntity.this.getLookControl().lookAt(livingEntity, 180.0f, 180.0f);
            double d = ShulkerEntity.this.squaredDistanceTo(livingEntity);
            if (d < 400.0) {
                if (this.counter <= 0) {
                    this.counter = 20 + ShulkerEntity.this.random.nextInt(10) * 20 / 2;
                    ShulkerEntity.this.getEntityWorld().spawnEntity(new ShulkerBulletEntity(ShulkerEntity.this.getEntityWorld(), ShulkerEntity.this, livingEntity, ShulkerEntity.this.getAttachedFace().getAxis()));
                    ShulkerEntity.this.playSound(SoundEvents.ENTITY_SHULKER_SHOOT, 2.0f, (ShulkerEntity.this.random.nextFloat() - ShulkerEntity.this.random.nextFloat()) * 0.2f + 1.0f);
                }
            } else {
                ShulkerEntity.this.setTarget(null);
            }
            super.tick();
        }
    }

    class PeekGoal
    extends Goal {
        private int counter;

        PeekGoal() {
        }

        @Override
        public boolean canStart() {
            return ShulkerEntity.this.getTarget() == null && ShulkerEntity.this.random.nextInt(PeekGoal.toGoalTicks(40)) == 0 && ShulkerEntity.this.canStay(ShulkerEntity.this.getBlockPos(), ShulkerEntity.this.getAttachedFace());
        }

        @Override
        public boolean shouldContinue() {
            return ShulkerEntity.this.getTarget() == null && this.counter > 0;
        }

        @Override
        public void start() {
            this.counter = this.getTickCount(20 * (1 + ShulkerEntity.this.random.nextInt(3)));
            ShulkerEntity.this.setPeekAmount(30);
        }

        @Override
        public void stop() {
            if (ShulkerEntity.this.getTarget() == null) {
                ShulkerEntity.this.setPeekAmount(0);
            }
        }

        @Override
        public void tick() {
            --this.counter;
        }
    }

    class TargetPlayerGoal
    extends ActiveTargetGoal<PlayerEntity> {
        public TargetPlayerGoal(ShulkerEntity shulker) {
            super((MobEntity)shulker, PlayerEntity.class, true);
        }

        @Override
        public boolean canStart() {
            if (ShulkerEntity.this.getEntityWorld().getDifficulty() == Difficulty.PEACEFUL) {
                return false;
            }
            return super.canStart();
        }

        @Override
        protected Box getSearchBox(double distance) {
            Direction direction = ((ShulkerEntity)this.mob).getAttachedFace();
            if (direction.getAxis() == Direction.Axis.X) {
                return this.mob.getBoundingBox().expand(4.0, distance, distance);
            }
            if (direction.getAxis() == Direction.Axis.Z) {
                return this.mob.getBoundingBox().expand(distance, distance, 4.0);
            }
            return this.mob.getBoundingBox().expand(distance, 4.0, distance);
        }
    }

    static class TargetOtherTeamGoal
    extends ActiveTargetGoal<LivingEntity> {
        public TargetOtherTeamGoal(ShulkerEntity shulker) {
            super(shulker, LivingEntity.class, 10, true, false, (entity, world) -> entity instanceof Monster);
        }

        @Override
        public boolean canStart() {
            if (this.mob.getScoreboardTeam() == null) {
                return false;
            }
            return super.canStart();
        }

        @Override
        protected Box getSearchBox(double distance) {
            Direction direction = ((ShulkerEntity)this.mob).getAttachedFace();
            if (direction.getAxis() == Direction.Axis.X) {
                return this.mob.getBoundingBox().expand(4.0, distance, distance);
            }
            if (direction.getAxis() == Direction.Axis.Z) {
                return this.mob.getBoundingBox().expand(distance, distance, 4.0);
            }
            return this.mob.getBoundingBox().expand(distance, 4.0, distance);
        }
    }

    static class ShulkerBodyControl
    extends BodyControl {
        public ShulkerBodyControl(MobEntity mobEntity) {
            super(mobEntity);
        }

        @Override
        public void tick() {
        }
    }
}


/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  com.google.common.base.MoreObjects
 *  com.google.common.collect.Lists
 *  org.jspecify.annotations.Nullable
 */
package net.minecraft.entity.projectile;

import com.google.common.base.MoreObjects;
import com.google.common.collect.Lists;
import java.util.ArrayList;
import net.minecraft.enchantment.EnchantmentHelper;
import net.minecraft.entity.Entity;
import net.minecraft.entity.EntityType;
import net.minecraft.entity.LazyEntityReference;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.damage.DamageSource;
import net.minecraft.entity.data.DataTracker;
import net.minecraft.entity.effect.StatusEffectInstance;
import net.minecraft.entity.effect.StatusEffects;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.entity.projectile.ProjectileEntity;
import net.minecraft.entity.projectile.ProjectileUtil;
import net.minecraft.network.packet.s2c.play.EntitySpawnS2CPacket;
import net.minecraft.particle.ParticleTypes;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.sound.SoundCategory;
import net.minecraft.sound.SoundEvents;
import net.minecraft.storage.ReadView;
import net.minecraft.storage.WriteView;
import net.minecraft.util.Uuids;
import net.minecraft.util.hit.BlockHitResult;
import net.minecraft.util.hit.EntityHitResult;
import net.minecraft.util.hit.HitResult;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Direction;
import net.minecraft.util.math.MathHelper;
import net.minecraft.util.math.Vec3d;
import net.minecraft.world.Difficulty;
import net.minecraft.world.World;
import net.minecraft.world.event.GameEvent;
import org.jspecify.annotations.Nullable;

public class ShulkerBulletEntity
extends ProjectileEntity {
    private static final double field_30666 = 0.15;
    private @Nullable LazyEntityReference<Entity> target;
    private @Nullable Direction direction;
    private int stepCount;
    private double targetX;
    private double targetY;
    private double targetZ;

    public ShulkerBulletEntity(EntityType<? extends ShulkerBulletEntity> entityType, World world) {
        super((EntityType<? extends ProjectileEntity>)entityType, world);
        this.noClip = true;
    }

    public ShulkerBulletEntity(World world, LivingEntity owner, Entity target, Direction.Axis axis) {
        this((EntityType<? extends ShulkerBulletEntity>)EntityType.SHULKER_BULLET, world);
        this.setOwner(owner);
        Vec3d vec3d = owner.getBoundingBox().getCenter();
        this.refreshPositionAndAngles(vec3d.x, vec3d.y, vec3d.z, this.getYaw(), this.getPitch());
        this.target = LazyEntityReference.of(target);
        this.direction = Direction.UP;
        this.changeTargetDirection(axis, target);
    }

    @Override
    public SoundCategory getSoundCategory() {
        return SoundCategory.HOSTILE;
    }

    @Override
    protected void writeCustomData(WriteView view) {
        super.writeCustomData(view);
        if (this.target != null) {
            view.put("Target", Uuids.INT_STREAM_CODEC, this.target.getUuid());
        }
        view.putNullable("Dir", Direction.INDEX_CODEC, this.direction);
        view.putInt("Steps", this.stepCount);
        view.putDouble("TXD", this.targetX);
        view.putDouble("TYD", this.targetY);
        view.putDouble("TZD", this.targetZ);
    }

    @Override
    protected void readCustomData(ReadView view) {
        super.readCustomData(view);
        this.stepCount = view.getInt("Steps", 0);
        this.targetX = view.getDouble("TXD", 0.0);
        this.targetY = view.getDouble("TYD", 0.0);
        this.targetZ = view.getDouble("TZD", 0.0);
        this.direction = view.read("Dir", Direction.INDEX_CODEC).orElse(null);
        this.target = LazyEntityReference.fromData(view, "Target");
    }

    @Override
    protected void initDataTracker(DataTracker.Builder builder) {
    }

    private @Nullable Direction getDirection() {
        return this.direction;
    }

    private void setDirection(@Nullable Direction direction) {
        this.direction = direction;
    }

    private void changeTargetDirection(@Nullable Direction.Axis axis, @Nullable Entity target) {
        BlockPos blockPos;
        double d = 0.5;
        if (target == null) {
            blockPos = this.getBlockPos().down();
        } else {
            d = (double)target.getHeight() * 0.5;
            blockPos = BlockPos.ofFloored(target.getX(), target.getY() + d, target.getZ());
        }
        double e = (double)blockPos.getX() + 0.5;
        double f = (double)blockPos.getY() + d;
        double g = (double)blockPos.getZ() + 0.5;
        Direction direction = null;
        if (!blockPos.isWithinDistance(this.getEntityPos(), 2.0)) {
            BlockPos blockPos2 = this.getBlockPos();
            ArrayList list = Lists.newArrayList();
            if (axis != Direction.Axis.X) {
                if (blockPos2.getX() < blockPos.getX() && this.getEntityWorld().isAir(blockPos2.east())) {
                    list.add(Direction.EAST);
                } else if (blockPos2.getX() > blockPos.getX() && this.getEntityWorld().isAir(blockPos2.west())) {
                    list.add(Direction.WEST);
                }
            }
            if (axis != Direction.Axis.Y) {
                if (blockPos2.getY() < blockPos.getY() && this.getEntityWorld().isAir(blockPos2.up())) {
                    list.add(Direction.UP);
                } else if (blockPos2.getY() > blockPos.getY() && this.getEntityWorld().isAir(blockPos2.down())) {
                    list.add(Direction.DOWN);
                }
            }
            if (axis != Direction.Axis.Z) {
                if (blockPos2.getZ() < blockPos.getZ() && this.getEntityWorld().isAir(blockPos2.south())) {
                    list.add(Direction.SOUTH);
                } else if (blockPos2.getZ() > blockPos.getZ() && this.getEntityWorld().isAir(blockPos2.north())) {
                    list.add(Direction.NORTH);
                }
            }
            direction = Direction.random(this.random);
            if (list.isEmpty()) {
                for (int i = 5; !this.getEntityWorld().isAir(blockPos2.offset(direction)) && i > 0; --i) {
                    direction = Direction.random(this.random);
                }
            } else {
                direction = (Direction)list.get(this.random.nextInt(list.size()));
            }
            e = this.getX() + (double)direction.getOffsetX();
            f = this.getY() + (double)direction.getOffsetY();
            g = this.getZ() + (double)direction.getOffsetZ();
        }
        this.setDirection(direction);
        double h = e - this.getX();
        double j = f - this.getY();
        double k = g - this.getZ();
        double l = Math.sqrt(h * h + j * j + k * k);
        if (l == 0.0) {
            this.targetX = 0.0;
            this.targetY = 0.0;
            this.targetZ = 0.0;
        } else {
            this.targetX = h / l * 0.15;
            this.targetY = j / l * 0.15;
            this.targetZ = k / l * 0.15;
        }
        this.velocityDirty = true;
        this.stepCount = 10 + this.random.nextInt(5) * 10;
    }

    @Override
    public void checkDespawn() {
        if (this.getEntityWorld().getDifficulty() == Difficulty.PEACEFUL) {
            this.discard();
        }
    }

    @Override
    protected double getGravity() {
        return 0.04;
    }

    @Override
    public void tick() {
        Vec3d vec3d;
        super.tick();
        Entity entity = !this.getEntityWorld().isClient() ? LazyEntityReference.getEntity(this.target, this.getEntityWorld()) : null;
        HitResult hitResult = null;
        if (!this.getEntityWorld().isClient()) {
            if (entity == null) {
                this.target = null;
            }
            if (!(entity == null || !entity.isAlive() || entity instanceof PlayerEntity && entity.isSpectator())) {
                this.targetX = MathHelper.clamp(this.targetX * 1.025, -1.0, 1.0);
                this.targetY = MathHelper.clamp(this.targetY * 1.025, -1.0, 1.0);
                this.targetZ = MathHelper.clamp(this.targetZ * 1.025, -1.0, 1.0);
                vec3d = this.getVelocity();
                this.setVelocity(vec3d.add((this.targetX - vec3d.x) * 0.2, (this.targetY - vec3d.y) * 0.2, (this.targetZ - vec3d.z) * 0.2));
            } else {
                this.applyGravity();
            }
            hitResult = ProjectileUtil.getCollision(this, this::canHit);
        }
        vec3d = this.getVelocity();
        this.setPosition(this.getEntityPos().add(vec3d));
        this.tickBlockCollision();
        if (this.portalManager != null && this.portalManager.isInPortal()) {
            this.tickPortalTeleportation();
        }
        if (hitResult != null && this.isAlive() && hitResult.getType() != HitResult.Type.MISS) {
            this.hitOrDeflect(hitResult);
        }
        ProjectileUtil.setRotationFromVelocity(this, 0.5f);
        if (this.getEntityWorld().isClient()) {
            this.getEntityWorld().addParticleClient(ParticleTypes.END_ROD, this.getX() - vec3d.x, this.getY() - vec3d.y + 0.15, this.getZ() - vec3d.z, 0.0, 0.0, 0.0);
        } else if (entity != null) {
            if (this.stepCount > 0) {
                --this.stepCount;
                if (this.stepCount == 0) {
                    this.changeTargetDirection(this.direction == null ? null : this.direction.getAxis(), entity);
                }
            }
            if (this.direction != null) {
                BlockPos blockPos = this.getBlockPos();
                Direction.Axis axis = this.direction.getAxis();
                if (this.getEntityWorld().isTopSolid(blockPos.offset(this.direction), this)) {
                    this.changeTargetDirection(axis, entity);
                } else {
                    BlockPos blockPos2 = entity.getBlockPos();
                    if (axis == Direction.Axis.X && blockPos.getX() == blockPos2.getX() || axis == Direction.Axis.Z && blockPos.getZ() == blockPos2.getZ() || axis == Direction.Axis.Y && blockPos.getY() == blockPos2.getY()) {
                        this.changeTargetDirection(axis, entity);
                    }
                }
            }
        }
    }

    @Override
    protected boolean shouldTickBlockCollision() {
        return !this.isRemoved();
    }

    @Override
    protected boolean canHit(Entity entity) {
        return super.canHit(entity) && !entity.noClip;
    }

    @Override
    public boolean isOnFire() {
        return false;
    }

    @Override
    public boolean shouldRender(double distance) {
        return distance < 16384.0;
    }

    @Override
    public float getBrightnessAtEyes() {
        return 1.0f;
    }

    @Override
    protected void onEntityHit(EntityHitResult entityHitResult) {
        super.onEntityHit(entityHitResult);
        Entity entity = entityHitResult.getEntity();
        Entity entity2 = this.getOwner();
        LivingEntity livingEntity = entity2 instanceof LivingEntity ? (LivingEntity)entity2 : null;
        DamageSource damageSource = this.getDamageSources().mobProjectile(this, livingEntity);
        boolean bl = entity.sidedDamage(damageSource, 4.0f);
        if (bl) {
            World world = this.getEntityWorld();
            if (world instanceof ServerWorld) {
                ServerWorld serverWorld = (ServerWorld)world;
                EnchantmentHelper.onTargetDamaged(serverWorld, entity, damageSource);
            }
            if (entity instanceof LivingEntity) {
                LivingEntity livingEntity2 = (LivingEntity)entity;
                livingEntity2.addStatusEffect(new StatusEffectInstance(StatusEffects.LEVITATION, 200), (Entity)MoreObjects.firstNonNull((Object)entity2, (Object)this));
            }
        }
    }

    @Override
    protected void onBlockHit(BlockHitResult blockHitResult) {
        super.onBlockHit(blockHitResult);
        ((ServerWorld)this.getEntityWorld()).spawnParticles(ParticleTypes.EXPLOSION, this.getX(), this.getY(), this.getZ(), 2, 0.2, 0.2, 0.2, 0.0);
        this.playSound(SoundEvents.ENTITY_SHULKER_BULLET_HIT, 1.0f, 1.0f);
    }

    private void destroy() {
        this.discard();
        this.getEntityWorld().emitGameEvent(GameEvent.ENTITY_DAMAGE, this.getEntityPos(), GameEvent.Emitter.of(this));
    }

    @Override
    protected void onCollision(HitResult hitResult) {
        super.onCollision(hitResult);
        this.destroy();
    }

    @Override
    public boolean canHit() {
        return true;
    }

    @Override
    public boolean clientDamage(DamageSource source) {
        return true;
    }

    @Override
    public boolean damage(ServerWorld world, DamageSource source, float amount) {
        this.playSound(SoundEvents.ENTITY_SHULKER_BULLET_HURT, 1.0f, 1.0f);
        world.spawnParticles(ParticleTypes.CRIT, this.getX(), this.getY(), this.getZ(), 15, 0.2, 0.2, 0.2, 0.0);
        this.destroy();
        return true;
    }

    @Override
    public void onSpawnPacket(EntitySpawnS2CPacket packet) {
        super.onSpawnPacket(packet);
        this.setVelocity(packet.getVelocity());
    }
}


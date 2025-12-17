/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  org.jspecify.annotations.Nullable
 */
package net.minecraft.entity.projectile;

import net.minecraft.entity.Entity;
import net.minecraft.entity.EntityType;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.damage.DamageSource;
import net.minecraft.entity.data.DataTracker;
import net.minecraft.entity.projectile.ProjectileEntity;
import net.minecraft.entity.projectile.ProjectileUtil;
import net.minecraft.particle.ParticleEffect;
import net.minecraft.particle.ParticleTypes;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.storage.ReadView;
import net.minecraft.storage.WriteView;
import net.minecraft.util.hit.HitResult;
import net.minecraft.util.math.Vec3d;
import net.minecraft.world.RaycastContext;
import net.minecraft.world.World;
import org.jspecify.annotations.Nullable;

public abstract class ExplosiveProjectileEntity
extends ProjectileEntity {
    public static final double DEFAULT_ACCELERATION_POWER = 0.1;
    public static final double field_51892 = 0.5;
    public double accelerationPower = 0.1;

    protected ExplosiveProjectileEntity(EntityType<? extends ExplosiveProjectileEntity> entityType, World world) {
        super((EntityType<? extends ProjectileEntity>)entityType, world);
    }

    protected ExplosiveProjectileEntity(EntityType<? extends ExplosiveProjectileEntity> type, double x, double y, double z, World world) {
        this(type, world);
        this.setPosition(x, y, z);
    }

    public ExplosiveProjectileEntity(EntityType<? extends ExplosiveProjectileEntity> type, double x, double y, double z, Vec3d velocity, World world) {
        this(type, world);
        this.refreshPositionAndAngles(x, y, z, this.getYaw(), this.getPitch());
        this.refreshPosition();
        this.setVelocityWithAcceleration(velocity, this.accelerationPower);
    }

    public ExplosiveProjectileEntity(EntityType<? extends ExplosiveProjectileEntity> type, LivingEntity owner, Vec3d velocity, World world) {
        this(type, owner.getX(), owner.getY(), owner.getZ(), velocity, world);
        this.setOwner(owner);
        this.setRotation(owner.getYaw(), owner.getPitch());
    }

    @Override
    protected void initDataTracker(DataTracker.Builder builder) {
    }

    @Override
    public boolean shouldRender(double distance) {
        double d = this.getBoundingBox().getAverageSideLength() * 4.0;
        if (Double.isNaN(d)) {
            d = 4.0;
        }
        return distance < (d *= 64.0) * d;
    }

    protected RaycastContext.ShapeType getRaycastShapeType() {
        return RaycastContext.ShapeType.COLLIDER;
    }

    @Override
    public void tick() {
        Entity entity = this.getOwner();
        this.applyDrag();
        if (!this.getEntityWorld().isClient() && (entity != null && entity.isRemoved() || !this.getEntityWorld().isChunkLoaded(this.getBlockPos()))) {
            this.discard();
            return;
        }
        HitResult hitResult = ProjectileUtil.getCollision((Entity)this, this::canHit, this.getRaycastShapeType());
        Vec3d vec3d = hitResult.getType() != HitResult.Type.MISS ? hitResult.getPos() : this.getEntityPos().add(this.getVelocity());
        ProjectileUtil.setRotationFromVelocity(this, 0.2f);
        this.setPosition(vec3d);
        this.tickBlockCollision();
        super.tick();
        if (this.isBurning()) {
            this.setOnFireFor(1.0f);
        }
        if (hitResult.getType() != HitResult.Type.MISS && this.isAlive()) {
            this.hitOrDeflect(hitResult);
        }
        this.addParticles();
    }

    private void applyDrag() {
        float g;
        Vec3d vec3d = this.getVelocity();
        Vec3d vec3d2 = this.getEntityPos();
        if (this.isTouchingWater()) {
            for (int i = 0; i < 4; ++i) {
                float f = 0.25f;
                this.getEntityWorld().addParticleClient(ParticleTypes.BUBBLE, vec3d2.x - vec3d.x * 0.25, vec3d2.y - vec3d.y * 0.25, vec3d2.z - vec3d.z * 0.25, vec3d.x, vec3d.y, vec3d.z);
            }
            g = this.getDragInWater();
        } else {
            g = this.getDrag();
        }
        this.setVelocity(vec3d.add(vec3d.normalize().multiply(this.accelerationPower)).multiply(g));
    }

    private void addParticles() {
        ParticleEffect particleEffect = this.getParticleType();
        Vec3d vec3d = this.getEntityPos();
        if (particleEffect != null) {
            this.getEntityWorld().addParticleClient(particleEffect, vec3d.x, vec3d.y + 0.5, vec3d.z, 0.0, 0.0, 0.0);
        }
    }

    @Override
    public boolean damage(ServerWorld world, DamageSource source, float amount) {
        return false;
    }

    @Override
    protected boolean canHit(Entity entity) {
        return super.canHit(entity) && !entity.noClip;
    }

    protected boolean isBurning() {
        return true;
    }

    protected @Nullable ParticleEffect getParticleType() {
        return ParticleTypes.SMOKE;
    }

    protected float getDrag() {
        return 0.95f;
    }

    protected float getDragInWater() {
        return 0.8f;
    }

    @Override
    protected void writeCustomData(WriteView view) {
        super.writeCustomData(view);
        view.putDouble("acceleration_power", this.accelerationPower);
    }

    @Override
    protected void readCustomData(ReadView view) {
        super.readCustomData(view);
        this.accelerationPower = view.getDouble("acceleration_power", 0.1);
    }

    @Override
    public float getBrightnessAtEyes() {
        return 1.0f;
    }

    private void setVelocityWithAcceleration(Vec3d velocity, double accelerationPower) {
        this.setVelocity(velocity.normalize().multiply(accelerationPower));
        this.velocityDirty = true;
    }

    @Override
    protected void onDeflected(boolean bl) {
        super.onDeflected(bl);
        this.accelerationPower = bl ? 0.1 : (this.accelerationPower *= 0.5);
    }
}


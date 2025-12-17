/*
 * Decompiled with CFR 0.152.
 */
package net.minecraft.entity.projectile.thrown;

import net.minecraft.block.BlockState;
import net.minecraft.block.Blocks;
import net.minecraft.entity.EntityCollisionHandler;
import net.minecraft.entity.EntityType;
import net.minecraft.entity.projectile.ProjectileEntity;
import net.minecraft.entity.projectile.ProjectileUtil;
import net.minecraft.particle.ParticleTypes;
import net.minecraft.util.hit.HitResult;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Vec3d;
import net.minecraft.world.World;

public abstract class ThrownEntity
extends ProjectileEntity {
    private static final float field_52510 = 12.25f;

    protected ThrownEntity(EntityType<? extends ThrownEntity> entityType, World world) {
        super((EntityType<? extends ProjectileEntity>)entityType, world);
    }

    protected ThrownEntity(EntityType<? extends ThrownEntity> type, double x, double y, double z, World world) {
        this(type, world);
        this.setPosition(x, y, z);
    }

    @Override
    public boolean shouldRender(double distance) {
        if (this.age < 2 && distance < 12.25) {
            return false;
        }
        double d = this.getBoundingBox().getAverageSideLength() * 4.0;
        if (Double.isNaN(d)) {
            d = 4.0;
        }
        return distance < (d *= 64.0) * d;
    }

    @Override
    public boolean canUsePortals(boolean allowVehicles) {
        return true;
    }

    @Override
    public void tick() {
        this.tickInitialBubbleColumnCollision();
        this.applyGravity();
        this.applyDrag();
        HitResult hitResult = ProjectileUtil.getCollision(this, this::canHit);
        Vec3d vec3d = hitResult.getType() != HitResult.Type.MISS ? hitResult.getPos() : this.getEntityPos().add(this.getVelocity());
        this.setPosition(vec3d);
        this.updateRotation();
        this.tickBlockCollision();
        super.tick();
        if (hitResult.getType() != HitResult.Type.MISS && this.isAlive()) {
            this.hitOrDeflect(hitResult);
        }
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
            g = 0.8f;
        } else {
            g = 0.99f;
        }
        this.setVelocity(vec3d.multiply(g));
    }

    private void tickInitialBubbleColumnCollision() {
        if (this.firstUpdate) {
            for (BlockPos blockPos : BlockPos.iterate(this.getBoundingBox())) {
                BlockState blockState = this.getEntityWorld().getBlockState(blockPos);
                if (!blockState.isOf(Blocks.BUBBLE_COLUMN)) continue;
                blockState.onEntityCollision(this.getEntityWorld(), blockPos, this, EntityCollisionHandler.DUMMY, true);
            }
        }
    }

    @Override
    protected double getGravity() {
        return 0.03;
    }
}


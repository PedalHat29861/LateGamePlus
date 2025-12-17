/*
 * Decompiled with CFR 0.152.
 */
package net.minecraft.entity.projectile;

import net.minecraft.block.AbstractBlock;
import net.minecraft.enchantment.EnchantmentHelper;
import net.minecraft.entity.Entity;
import net.minecraft.entity.EntityType;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.damage.DamageSource;
import net.minecraft.entity.data.DataTracker;
import net.minecraft.entity.passive.LlamaEntity;
import net.minecraft.entity.projectile.ProjectileEntity;
import net.minecraft.entity.projectile.ProjectileUtil;
import net.minecraft.network.packet.s2c.play.EntitySpawnS2CPacket;
import net.minecraft.particle.ParticleTypes;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.util.hit.BlockHitResult;
import net.minecraft.util.hit.EntityHitResult;
import net.minecraft.util.hit.HitResult;
import net.minecraft.util.math.MathHelper;
import net.minecraft.util.math.Vec3d;
import net.minecraft.world.World;

public class LlamaSpitEntity
extends ProjectileEntity {
    public LlamaSpitEntity(EntityType<? extends LlamaSpitEntity> entityType, World world) {
        super((EntityType<? extends ProjectileEntity>)entityType, world);
    }

    public LlamaSpitEntity(World world, LlamaEntity owner) {
        this((EntityType<? extends LlamaSpitEntity>)EntityType.LLAMA_SPIT, world);
        this.setOwner(owner);
        this.setPosition(owner.getX() - (double)(owner.getWidth() + 1.0f) * 0.5 * (double)MathHelper.sin(owner.bodyYaw * ((float)Math.PI / 180)), owner.getEyeY() - (double)0.1f, owner.getZ() + (double)(owner.getWidth() + 1.0f) * 0.5 * (double)MathHelper.cos(owner.bodyYaw * ((float)Math.PI / 180)));
    }

    @Override
    protected double getGravity() {
        return 0.06;
    }

    @Override
    public void tick() {
        super.tick();
        Vec3d vec3d = this.getVelocity();
        HitResult hitResult = ProjectileUtil.getCollision(this, this::canHit);
        this.hitOrDeflect(hitResult);
        double d = this.getX() + vec3d.x;
        double e = this.getY() + vec3d.y;
        double f = this.getZ() + vec3d.z;
        this.updateRotation();
        float g = 0.99f;
        if (this.getEntityWorld().getStatesInBox(this.getBoundingBox()).noneMatch(AbstractBlock.AbstractBlockState::isAir)) {
            this.discard();
            return;
        }
        if (this.isTouchingWater()) {
            this.discard();
            return;
        }
        this.setVelocity(vec3d.multiply(0.99f));
        this.applyGravity();
        this.setPosition(d, e, f);
    }

    @Override
    protected void onEntityHit(EntityHitResult entityHitResult) {
        super.onEntityHit(entityHitResult);
        Entity entity = this.getOwner();
        if (entity instanceof LivingEntity) {
            ServerWorld serverWorld;
            LivingEntity livingEntity = (LivingEntity)entity;
            Entity entity2 = entityHitResult.getEntity();
            DamageSource damageSource = this.getDamageSources().spit(this, livingEntity);
            World world = this.getEntityWorld();
            if (world instanceof ServerWorld && entity2.damage(serverWorld = (ServerWorld)world, damageSource, 1.0f)) {
                EnchantmentHelper.onTargetDamaged(serverWorld, entity2, damageSource);
            }
        }
    }

    @Override
    protected void onBlockHit(BlockHitResult blockHitResult) {
        super.onBlockHit(blockHitResult);
        if (!this.getEntityWorld().isClient()) {
            this.discard();
        }
    }

    @Override
    protected void initDataTracker(DataTracker.Builder builder) {
    }

    @Override
    public void onSpawnPacket(EntitySpawnS2CPacket packet) {
        super.onSpawnPacket(packet);
        Vec3d vec3d = packet.getVelocity();
        for (int i = 0; i < 7; ++i) {
            double d = 0.4 + 0.1 * (double)i;
            this.getEntityWorld().addParticleClient(ParticleTypes.SPIT, this.getX(), this.getY(), this.getZ(), vec3d.x * d, vec3d.y, vec3d.z * d);
        }
        this.setVelocity(vec3d);
    }
}


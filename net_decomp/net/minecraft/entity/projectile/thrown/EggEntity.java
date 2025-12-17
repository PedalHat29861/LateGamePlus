/*
 * Decompiled with CFR 0.152.
 */
package net.minecraft.entity.projectile.thrown;

import java.util.Optional;
import net.minecraft.component.DataComponentTypes;
import net.minecraft.entity.EntityDimensions;
import net.minecraft.entity.EntityType;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.SpawnReason;
import net.minecraft.entity.passive.ChickenEntity;
import net.minecraft.entity.projectile.thrown.ThrownItemEntity;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.item.Items;
import net.minecraft.particle.ItemStackParticleEffect;
import net.minecraft.particle.ParticleTypes;
import net.minecraft.util.hit.EntityHitResult;
import net.minecraft.util.hit.HitResult;
import net.minecraft.world.World;

public class EggEntity
extends ThrownItemEntity {
    private static final EntityDimensions EMPTY_DIMENSIONS = EntityDimensions.fixed(0.0f, 0.0f);

    public EggEntity(EntityType<? extends EggEntity> entityType, World world) {
        super((EntityType<? extends ThrownItemEntity>)entityType, world);
    }

    public EggEntity(World world, LivingEntity owner, ItemStack stack) {
        super(EntityType.EGG, owner, world, stack);
    }

    public EggEntity(World world, double x, double y, double z, ItemStack stack) {
        super(EntityType.EGG, x, y, z, world, stack);
    }

    @Override
    public void handleStatus(byte status) {
        if (status == 3) {
            double d = 0.08;
            for (int i = 0; i < 8; ++i) {
                this.getEntityWorld().addParticleClient(new ItemStackParticleEffect(ParticleTypes.ITEM, this.getStack()), this.getX(), this.getY(), this.getZ(), ((double)this.random.nextFloat() - 0.5) * 0.08, ((double)this.random.nextFloat() - 0.5) * 0.08, ((double)this.random.nextFloat() - 0.5) * 0.08);
            }
        }
    }

    @Override
    protected void onEntityHit(EntityHitResult entityHitResult) {
        super.onEntityHit(entityHitResult);
        entityHitResult.getEntity().serverDamage(this.getDamageSources().thrown(this, this.getOwner()), 0.0f);
    }

    @Override
    protected void onCollision(HitResult hitResult) {
        super.onCollision(hitResult);
        if (!this.getEntityWorld().isClient()) {
            if (this.random.nextInt(8) == 0) {
                int i = 1;
                if (this.random.nextInt(32) == 0) {
                    i = 4;
                }
                for (int j = 0; j < i; ++j) {
                    ChickenEntity chickenEntity = EntityType.CHICKEN.create(this.getEntityWorld(), SpawnReason.TRIGGERED);
                    if (chickenEntity == null) continue;
                    chickenEntity.setBreedingAge(-24000);
                    chickenEntity.refreshPositionAndAngles(this.getX(), this.getY(), this.getZ(), this.getYaw(), 0.0f);
                    Optional.ofNullable(this.getStack().get(DataComponentTypes.CHICKEN_VARIANT)).flatMap(variant -> variant.resolveEntry(this.getRegistryManager())).ifPresent(chickenEntity::setVariant);
                    if (!chickenEntity.recalculateDimensions(EMPTY_DIMENSIONS)) break;
                    this.getEntityWorld().spawnEntity(chickenEntity);
                }
            }
            this.getEntityWorld().sendEntityStatus(this, (byte)3);
            this.discard();
        }
    }

    @Override
    protected Item getDefaultItem() {
        return Items.EGG;
    }
}


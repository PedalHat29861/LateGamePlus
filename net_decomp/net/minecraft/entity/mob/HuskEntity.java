/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  org.jspecify.annotations.Nullable
 */
package net.minecraft.entity.mob;

import net.minecraft.entity.Entity;
import net.minecraft.entity.EntityData;
import net.minecraft.entity.EntityType;
import net.minecraft.entity.EquipmentSlot;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.SpawnReason;
import net.minecraft.entity.damage.DamageSource;
import net.minecraft.entity.effect.StatusEffectInstance;
import net.minecraft.entity.effect.StatusEffects;
import net.minecraft.entity.mob.CamelHuskEntity;
import net.minecraft.entity.mob.ParchedEntity;
import net.minecraft.entity.mob.ZombieEntity;
import net.minecraft.item.ItemStack;
import net.minecraft.item.Items;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.sound.SoundEvent;
import net.minecraft.sound.SoundEvents;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.random.Random;
import net.minecraft.world.LocalDifficulty;
import net.minecraft.world.ServerWorldAccess;
import net.minecraft.world.World;
import org.jspecify.annotations.Nullable;

public class HuskEntity
extends ZombieEntity {
    public HuskEntity(EntityType<? extends HuskEntity> entityType, World world) {
        super((EntityType<? extends ZombieEntity>)entityType, world);
    }

    @Override
    protected boolean burnsInDaylight() {
        return false;
    }

    @Override
    protected SoundEvent getAmbientSound() {
        return SoundEvents.ENTITY_HUSK_AMBIENT;
    }

    @Override
    protected SoundEvent getHurtSound(DamageSource source) {
        return SoundEvents.ENTITY_HUSK_HURT;
    }

    @Override
    protected SoundEvent getDeathSound() {
        return SoundEvents.ENTITY_HUSK_DEATH;
    }

    @Override
    protected SoundEvent getStepSound() {
        return SoundEvents.ENTITY_HUSK_STEP;
    }

    @Override
    public boolean tryAttack(ServerWorld world, Entity target) {
        boolean bl = super.tryAttack(world, target);
        if (bl && this.getMainHandStack().isEmpty() && target instanceof LivingEntity) {
            float f = world.getLocalDifficulty(this.getBlockPos()).getLocalDifficulty();
            ((LivingEntity)target).addStatusEffect(new StatusEffectInstance(StatusEffects.HUNGER, 140 * (int)f), this);
        }
        return bl;
    }

    @Override
    protected boolean canConvertInWater() {
        return true;
    }

    @Override
    protected void convertInWater(ServerWorld world) {
        this.convertTo(world, EntityType.ZOMBIE);
        if (!this.isSilent()) {
            world.syncWorldEvent(null, 1041, this.getBlockPos(), 0);
        }
    }

    @Override
    public @Nullable EntityData initialize(ServerWorldAccess world, LocalDifficulty difficulty, SpawnReason spawnReason, @Nullable EntityData entityData) {
        Random random = world.getRandom();
        entityData = super.initialize(world, difficulty, spawnReason, entityData);
        float f = difficulty.getClampedLocalDifficulty();
        if (spawnReason != SpawnReason.CONVERSION) {
            this.setCanPickUpLoot(random.nextFloat() < 0.55f * f);
        }
        if (entityData != null) {
            entityData = new HuskData((ZombieEntity.ZombieData)entityData);
            boolean bl = ((HuskData)entityData).unnatural = spawnReason != SpawnReason.NATURAL;
        }
        if (entityData instanceof HuskData) {
            BlockPos blockPos;
            HuskData huskData = (HuskData)entityData;
            if (!huskData.unnatural && world.isSpaceEmpty(EntityType.CAMEL_HUSK.getSpawnBox((double)(blockPos = this.getBlockPos()).getX() + 0.5, blockPos.getY(), (double)blockPos.getZ() + 0.5))) {
                huskData.unnatural = true;
                if (random.nextFloat() < 0.1f) {
                    this.equipStack(EquipmentSlot.MAINHAND, new ItemStack(Items.IRON_SPEAR));
                    CamelHuskEntity camelHuskEntity = EntityType.CAMEL_HUSK.create(this.getEntityWorld(), SpawnReason.NATURAL);
                    if (camelHuskEntity != null) {
                        camelHuskEntity.setPosition(this.getX(), this.getY(), this.getZ());
                        camelHuskEntity.initialize(world, difficulty, spawnReason, null);
                        this.startRiding(camelHuskEntity, true, true);
                        world.spawnEntity(camelHuskEntity);
                        ParchedEntity parchedEntity = EntityType.PARCHED.create(this.getEntityWorld(), SpawnReason.NATURAL);
                        if (parchedEntity != null) {
                            parchedEntity.refreshPositionAndAngles(this.getX(), this.getY(), this.getZ(), this.getYaw(), 0.0f);
                            parchedEntity.initialize(world, difficulty, spawnReason, null);
                            parchedEntity.startRiding(camelHuskEntity, false, false);
                            world.spawnEntityAndPassengers(parchedEntity);
                        }
                    }
                }
            }
        }
        return entityData;
    }

    public static class HuskData
    extends ZombieEntity.ZombieData {
        public boolean unnatural = false;

        public HuskData(ZombieEntity.ZombieData data) {
            super(data.baby, data.tryChickenJockey);
        }
    }
}


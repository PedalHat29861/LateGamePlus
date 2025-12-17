/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  org.jspecify.annotations.Nullable
 */
package net.minecraft.entity.passive;

import net.minecraft.entity.EntityType;
import net.minecraft.entity.SpawnReason;
import net.minecraft.entity.damage.DamageSource;
import net.minecraft.entity.passive.AbstractDonkeyEntity;
import net.minecraft.entity.passive.AbstractHorseEntity;
import net.minecraft.entity.passive.AnimalEntity;
import net.minecraft.entity.passive.HorseEntity;
import net.minecraft.entity.passive.PassiveEntity;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.sound.SoundEvent;
import net.minecraft.sound.SoundEvents;
import net.minecraft.world.World;
import org.jspecify.annotations.Nullable;

public class DonkeyEntity
extends AbstractDonkeyEntity {
    public DonkeyEntity(EntityType<? extends DonkeyEntity> entityType, World world) {
        super((EntityType<? extends AbstractDonkeyEntity>)entityType, world);
    }

    @Override
    protected SoundEvent getAmbientSound() {
        return SoundEvents.ENTITY_DONKEY_AMBIENT;
    }

    @Override
    protected SoundEvent getAngrySound() {
        return SoundEvents.ENTITY_DONKEY_ANGRY;
    }

    @Override
    protected SoundEvent getDeathSound() {
        return SoundEvents.ENTITY_DONKEY_DEATH;
    }

    @Override
    protected SoundEvent getEatSound() {
        return SoundEvents.ENTITY_DONKEY_EAT;
    }

    @Override
    protected SoundEvent getHurtSound(DamageSource source) {
        return SoundEvents.ENTITY_DONKEY_HURT;
    }

    @Override
    public boolean canBreedWith(AnimalEntity other) {
        if (other == this) {
            return false;
        }
        if (other instanceof DonkeyEntity || other instanceof HorseEntity) {
            return this.canBreed() && ((AbstractHorseEntity)other).canBreed();
        }
        return false;
    }

    @Override
    protected void playJumpSound() {
        this.playSound(SoundEvents.ENTITY_DONKEY_JUMP, 0.4f, 1.0f);
    }

    @Override
    public @Nullable PassiveEntity createChild(ServerWorld world, PassiveEntity entity) {
        EntityType<AbstractDonkeyEntity> entityType = entity instanceof HorseEntity ? EntityType.MULE : EntityType.DONKEY;
        AbstractHorseEntity abstractHorseEntity = entityType.create(world, SpawnReason.BREEDING);
        if (abstractHorseEntity != null) {
            this.setChildAttributes(entity, abstractHorseEntity);
        }
        return abstractHorseEntity;
    }
}


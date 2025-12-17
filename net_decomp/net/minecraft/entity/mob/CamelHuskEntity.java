/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  org.jspecify.annotations.Nullable
 */
package net.minecraft.entity.mob;

import net.minecraft.block.BlockState;
import net.minecraft.entity.EntityType;
import net.minecraft.entity.damage.DamageSource;
import net.minecraft.entity.mob.MobEntity;
import net.minecraft.entity.passive.AnimalEntity;
import net.minecraft.entity.passive.CamelEntity;
import net.minecraft.entity.passive.PassiveEntity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.ItemStack;
import net.minecraft.registry.entry.RegistryEntry;
import net.minecraft.registry.tag.BlockTags;
import net.minecraft.registry.tag.ItemTags;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.sound.SoundEvent;
import net.minecraft.sound.SoundEvents;
import net.minecraft.util.ActionResult;
import net.minecraft.util.Hand;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;
import org.jspecify.annotations.Nullable;

public class CamelHuskEntity
extends CamelEntity {
    public CamelHuskEntity(EntityType<? extends CamelEntity> entityType, World world) {
        super(entityType, world);
    }

    @Override
    public boolean canImmediatelyDespawn(double distanceSquared) {
        return true;
    }

    @Override
    public boolean isControlledByMob() {
        return this.getFirstPassenger() instanceof MobEntity;
    }

    @Override
    public ActionResult interact(PlayerEntity player, Hand hand) {
        this.setPersistent();
        return super.interact(player, hand);
    }

    @Override
    public boolean canBeLeashed() {
        return !this.isControlledByMob();
    }

    @Override
    public boolean isBreedingItem(ItemStack stack) {
        return stack.isIn(ItemTags.CAMEL_HUSK_FOOD);
    }

    @Override
    protected SoundEvent getAmbientSound() {
        return SoundEvents.ENTITY_CAMEL_HUSK_AMBIENT;
    }

    @Override
    public boolean canBreedWith(AnimalEntity other) {
        return false;
    }

    @Override
    public @Nullable CamelEntity createChild(ServerWorld serverWorld, PassiveEntity passiveEntity) {
        return null;
    }

    @Override
    public boolean canEat() {
        return false;
    }

    @Override
    protected SoundEvent getDeathSound() {
        return SoundEvents.ENTITY_CAMEL_HUSK_DEATH;
    }

    @Override
    protected SoundEvent getHurtSound(DamageSource source) {
        return SoundEvents.ENTITY_CAMEL_HUSK_HURT;
    }

    @Override
    protected void playStepSound(BlockPos pos, BlockState state) {
        if (state.isIn(BlockTags.CAMEL_SAND_STEP_SOUND_BLOCKS)) {
            this.playSound(SoundEvents.ENTITY_CAMEL_HUSK_STEP_SAND, 0.4f, 1.0f);
        } else {
            this.playSound(SoundEvents.ENTITY_CAMEL_HUSK_STEP, 0.4f, 1.0f);
        }
    }

    @Override
    protected SoundEvent getDashSound() {
        return SoundEvents.ENTITY_CAMEL_HUSK_DASH;
    }

    @Override
    protected SoundEvent getDashReadySound() {
        return SoundEvents.ENTITY_CAMEL_HUSK_DASH_READY;
    }

    @Override
    protected SoundEvent getEatSound() {
        return SoundEvents.ENTITY_CAMEL_HUSK_EAT;
    }

    @Override
    protected SoundEvent getStandSound() {
        return SoundEvents.ENTITY_CAMEL_HUSK_STAND;
    }

    @Override
    protected SoundEvent getSitSound() {
        return SoundEvents.ENTITY_CAMEL_HUSK_SIT;
    }

    @Override
    protected RegistryEntry.Reference<SoundEvent> getSaddleSound() {
        return SoundEvents.ENTITY_CAMEL_HUSK_SADDLE;
    }

    @Override
    public float getRiderChargingSpeedMultiplier() {
        return 4.0f;
    }

    @Override
    public /* synthetic */ @Nullable PassiveEntity createChild(ServerWorld world, PassiveEntity entity) {
        return this.createChild(world, entity);
    }
}


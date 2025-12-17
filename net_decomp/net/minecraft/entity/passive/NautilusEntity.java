/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  com.mojang.serialization.Dynamic
 *  org.jspecify.annotations.Nullable
 */
package net.minecraft.entity.passive;

import com.mojang.serialization.Dynamic;
import net.minecraft.entity.EntityType;
import net.minecraft.entity.SpawnReason;
import net.minecraft.entity.ai.brain.Brain;
import net.minecraft.entity.damage.DamageSource;
import net.minecraft.entity.passive.AbstractNautilusEntity;
import net.minecraft.entity.passive.NautilusBrain;
import net.minecraft.entity.passive.PassiveEntity;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.sound.SoundEvent;
import net.minecraft.sound.SoundEvents;
import net.minecraft.util.profiler.Profiler;
import net.minecraft.util.profiler.Profilers;
import net.minecraft.world.World;
import org.jspecify.annotations.Nullable;

public class NautilusEntity
extends AbstractNautilusEntity {
    private static final int MAX_AIR = 300;

    public NautilusEntity(EntityType<? extends NautilusEntity> entityType, World world) {
        super((EntityType<? extends AbstractNautilusEntity>)entityType, world);
    }

    protected Brain.Profile<NautilusEntity> createBrainProfile() {
        return NautilusBrain.createProfile();
    }

    @Override
    protected Brain<?> deserializeBrain(Dynamic<?> dynamic) {
        return NautilusBrain.create(this.createBrainProfile().deserialize(dynamic));
    }

    public Brain<NautilusEntity> getBrain() {
        return super.getBrain();
    }

    @Override
    public @Nullable NautilusEntity createChild(ServerWorld serverWorld, PassiveEntity passiveEntity) {
        NautilusEntity nautilusEntity = EntityType.NAUTILUS.create(serverWorld, SpawnReason.BREEDING);
        if (nautilusEntity != null && this.isTamed()) {
            nautilusEntity.setOwner(this.getOwnerReference());
            nautilusEntity.setTamed(true, true);
        }
        return nautilusEntity;
    }

    @Override
    protected void mobTick(ServerWorld world) {
        Profiler profiler = Profilers.get();
        profiler.push("nautilusBrain");
        this.getBrain().tick(world, this);
        profiler.pop();
        profiler.push("nautilusActivityUpdate");
        NautilusBrain.updateActivities(this);
        profiler.pop();
        super.mobTick(world);
    }

    @Override
    protected SoundEvent getAmbientSound() {
        if (this.isBaby()) {
            return this.isSubmergedInWater() ? SoundEvents.ENTITY_BABY_NAUTILUS_AMBIENT : SoundEvents.ENTITY_BABY_NAUTILUS_AMBIENT_LAND;
        }
        return this.isSubmergedInWater() ? SoundEvents.ENTITY_NAUTILUS_AMBIENT : SoundEvents.ENTITY_NAUTILUS_AMBIENT_LAND;
    }

    @Override
    protected SoundEvent getHurtSound(DamageSource source) {
        if (this.isBaby()) {
            return this.isSubmergedInWater() ? SoundEvents.ENTITY_BABY_NAUTILUS_HURT : SoundEvents.ENTITY_BABY_NAUTILUS_HURT_LAND;
        }
        return this.isSubmergedInWater() ? SoundEvents.ENTITY_NAUTILUS_HURT : SoundEvents.ENTITY_NAUTILUS_HURT_LAND;
    }

    @Override
    protected SoundEvent getDeathSound() {
        if (this.isBaby()) {
            return this.isSubmergedInWater() ? SoundEvents.ENTITY_BABY_NAUTILUS_DEATH : SoundEvents.ENTITY_BABY_NAUTILUS_DEATH_LAND;
        }
        return this.isSubmergedInWater() ? SoundEvents.ENTITY_NAUTILUS_DEATH : SoundEvents.ENTITY_NAUTILUS_DEATH_LAND;
    }

    @Override
    protected SoundEvent getDashSound() {
        return this.isSubmergedInWater() ? SoundEvents.ENTITY_NAUTILUS_DASH : SoundEvents.ENTITY_NAUTILUS_DASH_LAND;
    }

    @Override
    protected SoundEvent getDashReadySound() {
        return this.isSubmergedInWater() ? SoundEvents.ENTITY_NAUTILUS_DASH_READY : SoundEvents.ENTITY_NAUTILUS_DASH_READY_LAND;
    }

    @Override
    protected void playEatSound() {
        SoundEvent soundEvent = this.isBaby() ? SoundEvents.ENTITY_BABY_NAUTILUS_EAT : SoundEvents.ENTITY_NAUTILUS_EAT;
        this.playSound(soundEvent);
    }

    @Override
    protected SoundEvent getSwimSound() {
        return this.isBaby() ? SoundEvents.ENTITY_BABY_NAUTILUS_SWIM : SoundEvents.ENTITY_NAUTILUS_SWIM;
    }

    @Override
    public int getMaxAir() {
        return 300;
    }

    protected void tickAir(ServerWorld world, int lastAir) {
        if (this.isAlive() && !this.isTouchingWater()) {
            this.setAir(lastAir - 1);
            if (this.getAir() <= -20) {
                this.setAir(0);
                this.damage(world, this.getDamageSources().dryOut(), 2.0f);
            }
        } else {
            this.setAir(300);
        }
    }

    @Override
    public void baseTick() {
        World world;
        int i = this.getAir();
        super.baseTick();
        if (!this.isAiDisabled() && (world = this.getEntityWorld()) instanceof ServerWorld) {
            ServerWorld serverWorld = (ServerWorld)world;
            this.tickAir(serverWorld, i);
        }
    }

    @Override
    public boolean canBeLeashed() {
        return !this.hasAttackTarget();
    }

    @Override
    public /* synthetic */ @Nullable PassiveEntity createChild(ServerWorld world, PassiveEntity entity) {
        return this.createChild(world, entity);
    }
}


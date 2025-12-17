/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  com.mojang.serialization.Dynamic
 *  org.jspecify.annotations.Nullable
 */
package net.minecraft.entity.mob;

import com.mojang.serialization.Dynamic;
import java.util.Optional;
import net.minecraft.component.ComponentType;
import net.minecraft.component.ComponentsAccess;
import net.minecraft.component.DataComponentTypes;
import net.minecraft.entity.EntityData;
import net.minecraft.entity.EntityType;
import net.minecraft.entity.EquipmentSlot;
import net.minecraft.entity.SpawnReason;
import net.minecraft.entity.Variants;
import net.minecraft.entity.ai.brain.Brain;
import net.minecraft.entity.attribute.DefaultAttributeContainer;
import net.minecraft.entity.attribute.EntityAttributes;
import net.minecraft.entity.damage.DamageSource;
import net.minecraft.entity.data.DataTracker;
import net.minecraft.entity.data.TrackedData;
import net.minecraft.entity.data.TrackedDataHandlerRegistry;
import net.minecraft.entity.mob.ZombieNautilusBrain;
import net.minecraft.entity.mob.ZombieNautilusVariant;
import net.minecraft.entity.mob.ZombieNautilusVariants;
import net.minecraft.entity.passive.AbstractNautilusEntity;
import net.minecraft.entity.passive.PassiveEntity;
import net.minecraft.entity.spawn.SpawnContext;
import net.minecraft.registry.RegistryKeys;
import net.minecraft.registry.entry.LazyRegistryEntryReference;
import net.minecraft.registry.entry.RegistryEntry;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.sound.SoundEvent;
import net.minecraft.sound.SoundEvents;
import net.minecraft.storage.ReadView;
import net.minecraft.storage.WriteView;
import net.minecraft.util.profiler.Profiler;
import net.minecraft.util.profiler.Profilers;
import net.minecraft.world.LocalDifficulty;
import net.minecraft.world.ServerWorldAccess;
import net.minecraft.world.World;
import org.jspecify.annotations.Nullable;

public class ZombieNautilusEntity
extends AbstractNautilusEntity {
    private static final TrackedData<RegistryEntry<ZombieNautilusVariant>> VARIANT = DataTracker.registerData(ZombieNautilusEntity.class, TrackedDataHandlerRegistry.ZOMBIE_NAUTILUS_VARIANT);

    public ZombieNautilusEntity(EntityType<? extends ZombieNautilusEntity> entityType, World world) {
        super((EntityType<? extends AbstractNautilusEntity>)entityType, world);
    }

    public static DefaultAttributeContainer.Builder createZombieNautilusAttributes() {
        return AbstractNautilusEntity.createNautilusAttributes().add(EntityAttributes.MOVEMENT_SPEED, 1.1f);
    }

    @Override
    public @Nullable ZombieNautilusEntity createChild(ServerWorld serverWorld, PassiveEntity passiveEntity) {
        return null;
    }

    @Override
    protected EquipmentSlot getDaylightProtectionSlot() {
        return EquipmentSlot.BODY;
    }

    protected Brain.Profile<ZombieNautilusEntity> createBrainProfile() {
        return ZombieNautilusBrain.createProfile();
    }

    @Override
    protected Brain<?> deserializeBrain(Dynamic<?> dynamic) {
        return ZombieNautilusBrain.create(this.createBrainProfile().deserialize(dynamic));
    }

    public Brain<ZombieNautilusEntity> getBrain() {
        return super.getBrain();
    }

    @Override
    protected void mobTick(ServerWorld world) {
        Profiler profiler = Profilers.get();
        profiler.push("zombieNautilusBrain");
        this.getBrain().tick(world, this);
        profiler.pop();
        profiler.push("zombieNautilusActivityUpdate");
        ZombieNautilusBrain.updateActivities(this);
        profiler.pop();
        super.mobTick(world);
    }

    @Override
    protected SoundEvent getAmbientSound() {
        return this.isSubmergedInWater() ? SoundEvents.ENTITY_ZOMBIE_NAUTILUS_AMBIENT : SoundEvents.ENTITY_ZOMBIE_NAUTILUS_AMBIENT_LAND;
    }

    @Override
    protected SoundEvent getHurtSound(DamageSource source) {
        return this.isSubmergedInWater() ? SoundEvents.ENTITY_ZOMBIE_NAUTILUS_HURT : SoundEvents.ENTITY_ZOMBIE_NAUTILUS_HURT_LAND;
    }

    @Override
    protected SoundEvent getDeathSound() {
        return this.isSubmergedInWater() ? SoundEvents.ENTITY_ZOMBIE_NAUTILUS_DEATH : SoundEvents.ENTITY_ZOMBIE_NAUTILUS_DEATH_LAND;
    }

    @Override
    protected SoundEvent getDashSound() {
        return this.isSubmergedInWater() ? SoundEvents.ENTITY_ZOMBIE_NAUTILUS_DASH : SoundEvents.ENTITY_ZOMBIE_NAUTILUS_DASH_LAND;
    }

    @Override
    protected SoundEvent getDashReadySound() {
        return this.isSubmergedInWater() ? SoundEvents.ENTITY_ZOMBIE_NAUTILUS_DASH_READY : SoundEvents.ENTITY_ZOMBIE_NAUTILUS_DASH_READY_LAND;
    }

    @Override
    protected void playEatSound() {
        this.playSound(SoundEvents.ENTITY_ZOMBIE_NAUTILUS_EAT);
    }

    @Override
    protected SoundEvent getSwimSound() {
        return SoundEvents.ENTITY_ZOMBIE_NAUTILUS_SWIM;
    }

    @Override
    protected void initDataTracker(DataTracker.Builder builder) {
        super.initDataTracker(builder);
        builder.add(VARIANT, Variants.getOrDefaultOrThrow(this.getRegistryManager(), ZombieNautilusVariants.TEMPERATE));
    }

    @Override
    protected void readCustomData(ReadView view) {
        super.readCustomData(view);
        Variants.fromData(view, RegistryKeys.ZOMBIE_NAUTILUS_VARIANT).ifPresent(this::setVariant);
    }

    @Override
    protected void writeCustomData(WriteView view) {
        super.writeCustomData(view);
        Variants.writeData(view, this.getVariant());
    }

    public void setVariant(RegistryEntry<ZombieNautilusVariant> variant) {
        this.dataTracker.set(VARIANT, variant);
    }

    public RegistryEntry<ZombieNautilusVariant> getVariant() {
        return this.dataTracker.get(VARIANT);
    }

    @Override
    public <T> @Nullable T get(ComponentType<? extends T> type) {
        if (type == DataComponentTypes.ZOMBIE_NAUTILUS_VARIANT) {
            return ZombieNautilusEntity.castComponentValue(type, new LazyRegistryEntryReference<ZombieNautilusVariant>(this.getVariant()));
        }
        return super.get(type);
    }

    @Override
    protected void copyComponentsFrom(ComponentsAccess from) {
        this.copyComponentFrom(from, DataComponentTypes.ZOMBIE_NAUTILUS_VARIANT);
        super.copyComponentsFrom(from);
    }

    @Override
    protected <T> boolean setApplicableComponent(ComponentType<T> type, T value) {
        if (type == DataComponentTypes.ZOMBIE_NAUTILUS_VARIANT) {
            Optional<RegistryEntry<ZombieNautilusVariant>> optional = ZombieNautilusEntity.castComponentValue(DataComponentTypes.ZOMBIE_NAUTILUS_VARIANT, value).resolveEntry(this.getRegistryManager());
            if (optional.isPresent()) {
                this.setVariant(optional.get());
                return true;
            }
            return false;
        }
        return super.setApplicableComponent(type, value);
    }

    @Override
    public EntityData initialize(ServerWorldAccess world, LocalDifficulty difficulty, SpawnReason spawnReason, @Nullable EntityData entityData) {
        Variants.select(SpawnContext.of(world, this.getBlockPos()), RegistryKeys.ZOMBIE_NAUTILUS_VARIANT).ifPresent(this::setVariant);
        return super.initialize(world, difficulty, spawnReason, entityData);
    }

    @Override
    public boolean canBeLeashed() {
        return !this.hasAttackTarget() && !this.isControlledByMob();
    }

    @Override
    public boolean isBaby() {
        return false;
    }

    @Override
    public /* synthetic */ @Nullable PassiveEntity createChild(ServerWorld world, PassiveEntity entity) {
        return this.createChild(world, entity);
    }
}


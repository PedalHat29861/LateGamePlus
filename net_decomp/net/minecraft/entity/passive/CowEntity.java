/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  org.jspecify.annotations.Nullable
 */
package net.minecraft.entity.passive;

import net.minecraft.component.ComponentType;
import net.minecraft.component.ComponentsAccess;
import net.minecraft.component.DataComponentTypes;
import net.minecraft.entity.EntityData;
import net.minecraft.entity.EntityType;
import net.minecraft.entity.SpawnReason;
import net.minecraft.entity.Variants;
import net.minecraft.entity.data.DataTracker;
import net.minecraft.entity.data.TrackedData;
import net.minecraft.entity.data.TrackedDataHandlerRegistry;
import net.minecraft.entity.passive.AbstractCowEntity;
import net.minecraft.entity.passive.CowVariant;
import net.minecraft.entity.passive.CowVariants;
import net.minecraft.entity.passive.PassiveEntity;
import net.minecraft.entity.spawn.SpawnContext;
import net.minecraft.registry.RegistryKeys;
import net.minecraft.registry.entry.RegistryEntry;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.storage.ReadView;
import net.minecraft.storage.WriteView;
import net.minecraft.world.LocalDifficulty;
import net.minecraft.world.ServerWorldAccess;
import net.minecraft.world.World;
import org.jspecify.annotations.Nullable;

public class CowEntity
extends AbstractCowEntity {
    private static final TrackedData<RegistryEntry<CowVariant>> VARIANT = DataTracker.registerData(CowEntity.class, TrackedDataHandlerRegistry.COW_VARIANT);

    public CowEntity(EntityType<? extends CowEntity> entityType, World world) {
        super((EntityType<? extends AbstractCowEntity>)entityType, world);
    }

    @Override
    protected void initDataTracker(DataTracker.Builder builder) {
        super.initDataTracker(builder);
        builder.add(VARIANT, Variants.getOrDefaultOrThrow(this.getRegistryManager(), CowVariants.TEMPERATE));
    }

    @Override
    protected void writeCustomData(WriteView view) {
        super.writeCustomData(view);
        Variants.writeData(view, this.getVariant());
    }

    @Override
    protected void readCustomData(ReadView view) {
        super.readCustomData(view);
        Variants.fromData(view, RegistryKeys.COW_VARIANT).ifPresent(this::setVariant);
    }

    @Override
    public @Nullable CowEntity createChild(ServerWorld serverWorld, PassiveEntity passiveEntity) {
        CowEntity cowEntity = EntityType.COW.create(serverWorld, SpawnReason.BREEDING);
        if (cowEntity != null && passiveEntity instanceof CowEntity) {
            CowEntity cowEntity2 = (CowEntity)passiveEntity;
            cowEntity.setVariant(this.random.nextBoolean() ? this.getVariant() : cowEntity2.getVariant());
        }
        return cowEntity;
    }

    @Override
    public EntityData initialize(ServerWorldAccess world, LocalDifficulty difficulty, SpawnReason spawnReason, @Nullable EntityData entityData) {
        Variants.select(SpawnContext.of(world, this.getBlockPos()), RegistryKeys.COW_VARIANT).ifPresent(this::setVariant);
        return super.initialize(world, difficulty, spawnReason, entityData);
    }

    public void setVariant(RegistryEntry<CowVariant> variant) {
        this.dataTracker.set(VARIANT, variant);
    }

    public RegistryEntry<CowVariant> getVariant() {
        return this.dataTracker.get(VARIANT);
    }

    @Override
    public <T> @Nullable T get(ComponentType<? extends T> type) {
        if (type == DataComponentTypes.COW_VARIANT) {
            return CowEntity.castComponentValue(type, this.getVariant());
        }
        return super.get(type);
    }

    @Override
    protected void copyComponentsFrom(ComponentsAccess from) {
        this.copyComponentFrom(from, DataComponentTypes.COW_VARIANT);
        super.copyComponentsFrom(from);
    }

    @Override
    protected <T> boolean setApplicableComponent(ComponentType<T> type, T value) {
        if (type == DataComponentTypes.COW_VARIANT) {
            this.setVariant(CowEntity.castComponentValue(DataComponentTypes.COW_VARIANT, value));
            return true;
        }
        return super.setApplicableComponent(type, value);
    }

    @Override
    public /* synthetic */ @Nullable PassiveEntity createChild(ServerWorld world, PassiveEntity entity) {
        return this.createChild(world, entity);
    }
}


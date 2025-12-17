/*
 * Decompiled with CFR 0.152.
 */
package net.minecraft.entity.conversion;

import java.util.Set;
import java.util.UUID;
import net.minecraft.component.ComponentType;
import net.minecraft.component.DataComponentTypes;
import net.minecraft.entity.Entity;
import net.minecraft.entity.EquipmentSlot;
import net.minecraft.entity.ai.brain.Brain;
import net.minecraft.entity.ai.brain.MemoryModuleState;
import net.minecraft.entity.ai.brain.MemoryModuleType;
import net.minecraft.entity.conversion.EntityConversionContext;
import net.minecraft.entity.effect.StatusEffectInstance;
import net.minecraft.entity.mob.MobEntity;
import net.minecraft.entity.mob.ZombieEntity;
import net.minecraft.entity.passive.PassiveEntity;
import net.minecraft.item.ItemStack;
import net.minecraft.scoreboard.Scoreboard;

public abstract sealed class EntityConversionType
extends Enum<EntityConversionType> {
    public static final /* enum */ EntityConversionType SINGLE = new EntityConversionType(true){

        @Override
        void setUpNewEntity(MobEntity oldEntity, MobEntity newEntity, EntityConversionContext context) {
            Entity entity3;
            Entity entity = oldEntity.getFirstPassenger();
            newEntity.copyPositionAndRotation(oldEntity);
            newEntity.setVelocity(oldEntity.getVelocity());
            if (entity != null) {
                entity.stopRiding();
                entity.ridingCooldown = 0;
                for (Entity entity2 : newEntity.getPassengerList()) {
                    entity2.stopRiding();
                    entity2.remove(Entity.RemovalReason.DISCARDED);
                }
                entity.startRiding(newEntity);
            }
            if ((entity3 = oldEntity.getVehicle()) != null) {
                oldEntity.stopRiding();
                newEntity.startRiding(entity3, false, false);
            }
            if (context.keepEquipment()) {
                for (EquipmentSlot equipmentSlot : EquipmentSlot.VALUES) {
                    ItemStack itemStack = oldEntity.getEquippedStack(equipmentSlot);
                    if (itemStack.isEmpty()) continue;
                    newEntity.equipStack(equipmentSlot, itemStack.copyAndEmpty());
                    newEntity.setEquipmentDropChance(equipmentSlot, oldEntity.getEquipmentDropChances().get(equipmentSlot));
                }
            }
            newEntity.fallDistance = oldEntity.fallDistance;
            newEntity.setFlag(7, oldEntity.isGliding());
            newEntity.playerHitTimer = oldEntity.playerHitTimer;
            newEntity.hurtTime = oldEntity.hurtTime;
            newEntity.bodyYaw = oldEntity.bodyYaw;
            newEntity.setOnGround(oldEntity.isOnGround());
            oldEntity.getSleepingPosition().ifPresent(newEntity::setSleepingPosition);
            Entity entity4 = oldEntity.getLeashHolder();
            if (entity4 != null) {
                newEntity.attachLeash(entity4, true);
            }
            this.copyData(oldEntity, newEntity, context);
        }
    };
    public static final /* enum */ EntityConversionType SPLIT_ON_DEATH = new EntityConversionType(false){

        @Override
        void setUpNewEntity(MobEntity oldEntity, MobEntity newEntity, EntityConversionContext context) {
            Entity entity2;
            Entity entity = oldEntity.getFirstPassenger();
            if (entity != null) {
                entity.stopRiding();
            }
            if ((entity2 = oldEntity.getLeashHolder()) != null) {
                oldEntity.detachLeash();
            }
            this.copyData(oldEntity, newEntity, context);
        }
    };
    private static final Set<ComponentType<?>> CUSTOM_COMPONENTS;
    private final boolean discardOldEntity;
    private static final /* synthetic */ EntityConversionType[] field_54083;

    public static EntityConversionType[] values() {
        return (EntityConversionType[])field_54083.clone();
    }

    public static EntityConversionType valueOf(String string) {
        return Enum.valueOf(EntityConversionType.class, string);
    }

    EntityConversionType(boolean discardOldEntity) {
        this.discardOldEntity = discardOldEntity;
    }

    public boolean shouldDiscardOldEntity() {
        return this.discardOldEntity;
    }

    abstract void setUpNewEntity(MobEntity var1, MobEntity var2, EntityConversionContext var3);

    void copyData(MobEntity oldEntity, MobEntity newEntity, EntityConversionContext context) {
        ZombieEntity zombieEntity;
        newEntity.setAbsorptionAmount(oldEntity.getAbsorptionAmount());
        for (StatusEffectInstance statusEffectInstance : oldEntity.getStatusEffects()) {
            newEntity.addStatusEffect(new StatusEffectInstance(statusEffectInstance));
        }
        if (oldEntity.isBaby()) {
            newEntity.setBaby(true);
        }
        if (oldEntity instanceof PassiveEntity) {
            PassiveEntity passiveEntity = (PassiveEntity)oldEntity;
            if (newEntity instanceof PassiveEntity) {
                PassiveEntity passiveEntity2 = (PassiveEntity)newEntity;
                passiveEntity2.setBreedingAge(passiveEntity.getBreedingAge());
                passiveEntity2.forcedAge = passiveEntity.forcedAge;
                passiveEntity2.happyTicksRemaining = passiveEntity.happyTicksRemaining;
            }
        }
        Brain<UUID> brain = oldEntity.getBrain();
        Brain<?> brain2 = newEntity.getBrain();
        if (brain.isMemoryInState(MemoryModuleType.ANGRY_AT, MemoryModuleState.REGISTERED) && brain.hasMemoryModule(MemoryModuleType.ANGRY_AT)) {
            brain2.remember(MemoryModuleType.ANGRY_AT, brain.getOptionalRegisteredMemory(MemoryModuleType.ANGRY_AT));
        }
        if (context.preserveCanPickUpLoot()) {
            newEntity.setCanPickUpLoot(oldEntity.canPickUpLoot());
        }
        newEntity.setLeftHanded(oldEntity.isLeftHanded());
        newEntity.setAiDisabled(oldEntity.isAiDisabled());
        if (oldEntity.isPersistent()) {
            newEntity.setPersistent();
        }
        newEntity.setCustomNameVisible(oldEntity.isCustomNameVisible());
        newEntity.setOnFire(oldEntity.isOnFire());
        newEntity.setInvulnerable(oldEntity.isInvulnerable());
        newEntity.setNoGravity(oldEntity.hasNoGravity());
        newEntity.setPortalCooldown(oldEntity.getPortalCooldown());
        newEntity.setSilent(oldEntity.isSilent());
        oldEntity.getCommandTags().forEach(newEntity::addCommandTag);
        for (ComponentType<?> componentType : CUSTOM_COMPONENTS) {
            EntityConversionType.copyComponent(oldEntity, newEntity, componentType);
        }
        if (context.team() != null) {
            Scoreboard scoreboard = newEntity.getEntityWorld().getScoreboard();
            scoreboard.addScoreHolderToTeam(newEntity.getUuidAsString(), context.team());
            if (oldEntity.getScoreboardTeam() != null && oldEntity.getScoreboardTeam() == context.team()) {
                scoreboard.removeScoreHolderFromTeam(oldEntity.getUuidAsString(), oldEntity.getScoreboardTeam());
            }
        }
        if (oldEntity instanceof ZombieEntity && (zombieEntity = (ZombieEntity)oldEntity).canBreakDoors() && newEntity instanceof ZombieEntity) {
            ZombieEntity zombieEntity2 = (ZombieEntity)newEntity;
            zombieEntity2.setCanBreakDoors(true);
        }
    }

    private static <T> void copyComponent(MobEntity oldEntity, MobEntity newEntity, ComponentType<T> type) {
        T object = oldEntity.get(type);
        if (object != null) {
            newEntity.setComponent(type, object);
        }
    }

    private static /* synthetic */ EntityConversionType[] method_63610() {
        return new EntityConversionType[]{SINGLE, SPLIT_ON_DEATH};
    }

    static {
        field_54083 = EntityConversionType.method_63610();
        CUSTOM_COMPONENTS = Set.of(DataComponentTypes.CUSTOM_NAME, DataComponentTypes.CUSTOM_DATA);
    }
}


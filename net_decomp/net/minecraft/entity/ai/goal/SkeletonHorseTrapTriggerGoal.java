/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  org.jspecify.annotations.Nullable
 */
package net.minecraft.entity.ai.goal;

import net.minecraft.component.DataComponentTypes;
import net.minecraft.component.type.ItemEnchantmentsComponent;
import net.minecraft.enchantment.EnchantmentHelper;
import net.minecraft.enchantment.provider.EnchantmentProviders;
import net.minecraft.entity.EntityType;
import net.minecraft.entity.EquipmentSlot;
import net.minecraft.entity.LightningEntity;
import net.minecraft.entity.SpawnReason;
import net.minecraft.entity.ai.goal.Goal;
import net.minecraft.entity.mob.SkeletonEntity;
import net.minecraft.entity.mob.SkeletonHorseEntity;
import net.minecraft.entity.passive.AbstractHorseEntity;
import net.minecraft.item.ItemStack;
import net.minecraft.item.Items;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.world.LocalDifficulty;
import org.jspecify.annotations.Nullable;

public class SkeletonHorseTrapTriggerGoal
extends Goal {
    private final SkeletonHorseEntity skeletonHorse;

    public SkeletonHorseTrapTriggerGoal(SkeletonHorseEntity skeletonHorse) {
        this.skeletonHorse = skeletonHorse;
    }

    @Override
    public boolean canStart() {
        return this.skeletonHorse.getEntityWorld().isPlayerInRange(this.skeletonHorse.getX(), this.skeletonHorse.getY(), this.skeletonHorse.getZ(), 10.0);
    }

    @Override
    public void tick() {
        ServerWorld serverWorld = (ServerWorld)this.skeletonHorse.getEntityWorld();
        LocalDifficulty localDifficulty = serverWorld.getLocalDifficulty(this.skeletonHorse.getBlockPos());
        this.skeletonHorse.setTrapped(false);
        this.skeletonHorse.setTame(true);
        this.skeletonHorse.setBreedingAge(0);
        LightningEntity lightningEntity = EntityType.LIGHTNING_BOLT.create(serverWorld, SpawnReason.TRIGGERED);
        if (lightningEntity == null) {
            return;
        }
        lightningEntity.refreshPositionAfterTeleport(this.skeletonHorse.getX(), this.skeletonHorse.getY(), this.skeletonHorse.getZ());
        lightningEntity.setCosmetic(true);
        serverWorld.spawnEntity(lightningEntity);
        SkeletonEntity skeletonEntity = this.getSkeleton(localDifficulty, this.skeletonHorse);
        if (skeletonEntity == null) {
            return;
        }
        skeletonEntity.startRiding(this.skeletonHorse);
        serverWorld.spawnEntityAndPassengers(skeletonEntity);
        for (int i = 0; i < 3; ++i) {
            SkeletonEntity skeletonEntity2;
            AbstractHorseEntity abstractHorseEntity = this.getHorse(localDifficulty);
            if (abstractHorseEntity == null || (skeletonEntity2 = this.getSkeleton(localDifficulty, abstractHorseEntity)) == null) continue;
            skeletonEntity2.startRiding(abstractHorseEntity);
            abstractHorseEntity.addVelocity(this.skeletonHorse.getRandom().nextTriangular(0.0, 1.1485), 0.0, this.skeletonHorse.getRandom().nextTriangular(0.0, 1.1485));
            serverWorld.spawnEntityAndPassengers(abstractHorseEntity);
        }
    }

    private @Nullable AbstractHorseEntity getHorse(LocalDifficulty localDifficulty) {
        SkeletonHorseEntity skeletonHorseEntity = EntityType.SKELETON_HORSE.create(this.skeletonHorse.getEntityWorld(), SpawnReason.TRIGGERED);
        if (skeletonHorseEntity != null) {
            skeletonHorseEntity.initialize((ServerWorld)this.skeletonHorse.getEntityWorld(), localDifficulty, SpawnReason.TRIGGERED, null);
            skeletonHorseEntity.setPosition(this.skeletonHorse.getX(), this.skeletonHorse.getY(), this.skeletonHorse.getZ());
            skeletonHorseEntity.timeUntilRegen = 60;
            skeletonHorseEntity.setPersistent();
            skeletonHorseEntity.setTame(true);
            skeletonHorseEntity.setBreedingAge(0);
        }
        return skeletonHorseEntity;
    }

    private @Nullable SkeletonEntity getSkeleton(LocalDifficulty localDifficulty, AbstractHorseEntity vehicle) {
        SkeletonEntity skeletonEntity = EntityType.SKELETON.create(vehicle.getEntityWorld(), SpawnReason.TRIGGERED);
        if (skeletonEntity != null) {
            skeletonEntity.initialize((ServerWorld)vehicle.getEntityWorld(), localDifficulty, SpawnReason.TRIGGERED, null);
            skeletonEntity.setPosition(vehicle.getX(), vehicle.getY(), vehicle.getZ());
            skeletonEntity.timeUntilRegen = 60;
            skeletonEntity.setPersistent();
            if (skeletonEntity.getEquippedStack(EquipmentSlot.HEAD).isEmpty()) {
                skeletonEntity.equipStack(EquipmentSlot.HEAD, new ItemStack(Items.IRON_HELMET));
            }
            this.enchantEquipment(skeletonEntity, EquipmentSlot.MAINHAND, localDifficulty);
            this.enchantEquipment(skeletonEntity, EquipmentSlot.HEAD, localDifficulty);
        }
        return skeletonEntity;
    }

    private void enchantEquipment(SkeletonEntity rider, EquipmentSlot slot, LocalDifficulty localDifficulty) {
        ItemStack itemStack = rider.getEquippedStack(slot);
        itemStack.set(DataComponentTypes.ENCHANTMENTS, ItemEnchantmentsComponent.DEFAULT);
        EnchantmentHelper.applyEnchantmentProvider(itemStack, rider.getEntityWorld().getRegistryManager(), EnchantmentProviders.MOB_SPAWN_EQUIPMENT, localDifficulty, rider.getRandom());
        rider.equipStack(slot, itemStack);
    }
}


/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  com.google.common.collect.ImmutableMap
 */
package net.minecraft.entity.ai.brain.task;

import com.google.common.collect.ImmutableMap;
import java.util.Map;
import java.util.Optional;
import net.minecraft.entity.EntityType;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.ai.brain.Brain;
import net.minecraft.entity.ai.brain.MemoryModuleState;
import net.minecraft.entity.ai.brain.MemoryModuleType;
import net.minecraft.entity.ai.brain.task.MultiTickTask;
import net.minecraft.entity.ai.brain.task.TargetUtil;
import net.minecraft.entity.ai.pathing.Path;
import net.minecraft.entity.passive.PassiveEntity;
import net.minecraft.entity.passive.VillagerEntity;
import net.minecraft.registry.entry.RegistryEntry;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.GlobalPos;
import net.minecraft.world.poi.PointOfInterestType;
import net.minecraft.world.poi.PointOfInterestTypes;

public class VillagerBreedTask
extends MultiTickTask<VillagerEntity> {
    private long breedEndTime;

    public VillagerBreedTask() {
        super((Map<MemoryModuleType<?>, MemoryModuleState>)ImmutableMap.of(MemoryModuleType.BREED_TARGET, (Object)((Object)MemoryModuleState.VALUE_PRESENT), MemoryModuleType.VISIBLE_MOBS, (Object)((Object)MemoryModuleState.VALUE_PRESENT)), 350, 350);
    }

    @Override
    protected boolean shouldRun(ServerWorld serverWorld, VillagerEntity villagerEntity) {
        return this.isReadyToBreed(villagerEntity);
    }

    @Override
    protected boolean shouldKeepRunning(ServerWorld serverWorld, VillagerEntity villagerEntity, long l) {
        return l <= this.breedEndTime && this.isReadyToBreed(villagerEntity);
    }

    @Override
    protected void run(ServerWorld serverWorld, VillagerEntity villagerEntity, long l) {
        PassiveEntity passiveEntity = villagerEntity.getBrain().getOptionalRegisteredMemory(MemoryModuleType.BREED_TARGET).get();
        TargetUtil.lookAtAndWalkTowardsEachOther(villagerEntity, passiveEntity, 0.5f, 2);
        serverWorld.sendEntityStatus(passiveEntity, (byte)18);
        serverWorld.sendEntityStatus(villagerEntity, (byte)18);
        int i = 275 + villagerEntity.getRandom().nextInt(50);
        this.breedEndTime = l + (long)i;
    }

    @Override
    protected void keepRunning(ServerWorld serverWorld, VillagerEntity villagerEntity, long l) {
        VillagerEntity villagerEntity2 = (VillagerEntity)villagerEntity.getBrain().getOptionalRegisteredMemory(MemoryModuleType.BREED_TARGET).get();
        if (villagerEntity.squaredDistanceTo(villagerEntity2) > 5.0) {
            return;
        }
        TargetUtil.lookAtAndWalkTowardsEachOther(villagerEntity, villagerEntity2, 0.5f, 2);
        if (l >= this.breedEndTime) {
            villagerEntity.eatForBreeding();
            villagerEntity2.eatForBreeding();
            this.goHome(serverWorld, villagerEntity, villagerEntity2);
        } else if (villagerEntity.getRandom().nextInt(35) == 0) {
            serverWorld.sendEntityStatus(villagerEntity2, (byte)12);
            serverWorld.sendEntityStatus(villagerEntity, (byte)12);
        }
    }

    private void goHome(ServerWorld world, VillagerEntity first, VillagerEntity second) {
        Optional<BlockPos> optional = this.getReachableHome(world, first);
        if (optional.isEmpty()) {
            world.sendEntityStatus(second, (byte)13);
            world.sendEntityStatus(first, (byte)13);
        } else {
            Optional<VillagerEntity> optional2 = this.createChild(world, first, second);
            if (optional2.isPresent()) {
                this.setChildHome(world, optional2.get(), optional.get());
            } else {
                world.getPointOfInterestStorage().releaseTicket(optional.get());
                world.getSubscriptionTracker().onPoiUpdated(optional.get());
            }
        }
    }

    @Override
    protected void finishRunning(ServerWorld serverWorld, VillagerEntity villagerEntity, long l) {
        villagerEntity.getBrain().forget(MemoryModuleType.BREED_TARGET);
    }

    private boolean isReadyToBreed(VillagerEntity villager) {
        Brain<VillagerEntity> brain = villager.getBrain();
        Optional<PassiveEntity> optional = brain.getOptionalRegisteredMemory(MemoryModuleType.BREED_TARGET).filter(passiveEntity -> passiveEntity.getType() == EntityType.VILLAGER);
        if (optional.isEmpty()) {
            return false;
        }
        return TargetUtil.canSee(brain, MemoryModuleType.BREED_TARGET, EntityType.VILLAGER) && villager.isReadyToBreed() && optional.get().isReadyToBreed();
    }

    private Optional<BlockPos> getReachableHome(ServerWorld world, VillagerEntity villager) {
        return world.getPointOfInterestStorage().getPosition(poiType -> poiType.matchesKey(PointOfInterestTypes.HOME), (poiType, pos) -> this.canReachHome(villager, (BlockPos)pos, (RegistryEntry<PointOfInterestType>)poiType), villager.getBlockPos(), 48);
    }

    private boolean canReachHome(VillagerEntity villager, BlockPos pos, RegistryEntry<PointOfInterestType> poiType) {
        Path path = villager.getNavigation().findPathTo(pos, poiType.value().searchDistance());
        return path != null && path.reachesTarget();
    }

    private Optional<VillagerEntity> createChild(ServerWorld world, VillagerEntity parent, VillagerEntity partner) {
        VillagerEntity villagerEntity = parent.createChild(world, partner);
        if (villagerEntity == null) {
            return Optional.empty();
        }
        parent.setBreedingAge(6000);
        partner.setBreedingAge(6000);
        villagerEntity.setBreedingAge(-24000);
        villagerEntity.refreshPositionAndAngles(parent.getX(), parent.getY(), parent.getZ(), 0.0f, 0.0f);
        world.spawnEntityAndPassengers(villagerEntity);
        world.sendEntityStatus(villagerEntity, (byte)12);
        return Optional.of(villagerEntity);
    }

    private void setChildHome(ServerWorld world, VillagerEntity child, BlockPos pos) {
        GlobalPos globalPos = GlobalPos.create(world.getRegistryKey(), pos);
        child.getBrain().remember(MemoryModuleType.HOME, globalPos);
    }

    @Override
    protected /* synthetic */ void finishRunning(ServerWorld world, LivingEntity entity, long time) {
        this.finishRunning(world, (VillagerEntity)entity, time);
    }

    @Override
    protected /* synthetic */ void run(ServerWorld world, LivingEntity entity, long time) {
        this.run(world, (VillagerEntity)entity, time);
    }
}


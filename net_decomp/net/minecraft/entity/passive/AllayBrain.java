/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  com.google.common.collect.ImmutableList
 *  com.google.common.collect.ImmutableSet
 *  com.mojang.datafixers.util.Pair
 */
package net.minecraft.entity.passive;

import com.google.common.collect.ImmutableList;
import com.google.common.collect.ImmutableSet;
import com.mojang.datafixers.util.Pair;
import java.util.List;
import java.util.Optional;
import java.util.Set;
import java.util.UUID;
import java.util.function.Predicate;
import net.minecraft.block.Blocks;
import net.minecraft.entity.Entity;
import net.minecraft.entity.ItemEntity;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.ai.brain.Activity;
import net.minecraft.entity.ai.brain.BlockPosLookTarget;
import net.minecraft.entity.ai.brain.Brain;
import net.minecraft.entity.ai.brain.EntityLookTarget;
import net.minecraft.entity.ai.brain.LookTarget;
import net.minecraft.entity.ai.brain.MemoryModuleState;
import net.minecraft.entity.ai.brain.MemoryModuleType;
import net.minecraft.entity.ai.brain.task.FleeTask;
import net.minecraft.entity.ai.brain.task.GiveInventoryToLookTargetTask;
import net.minecraft.entity.ai.brain.task.GoToLookTargetTask;
import net.minecraft.entity.ai.brain.task.LookAtMobWithIntervalTask;
import net.minecraft.entity.ai.brain.task.MoveToTargetTask;
import net.minecraft.entity.ai.brain.task.RandomTask;
import net.minecraft.entity.ai.brain.task.StayAboveWaterTask;
import net.minecraft.entity.ai.brain.task.StrollTask;
import net.minecraft.entity.ai.brain.task.Task;
import net.minecraft.entity.ai.brain.task.TickCooldownTask;
import net.minecraft.entity.ai.brain.task.UpdateLookControlTask;
import net.minecraft.entity.ai.brain.task.WaitTask;
import net.minecraft.entity.ai.brain.task.WalkTowardsLookTargetTask;
import net.minecraft.entity.ai.brain.task.WalkTowardsNearestVisibleWantedItemTask;
import net.minecraft.entity.passive.AllayEntity;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.GlobalPos;
import net.minecraft.util.math.intprovider.UniformIntProvider;
import net.minecraft.world.World;

public class AllayBrain {
    private static final float field_38406 = 1.0f;
    private static final float field_38407 = 2.25f;
    private static final float WALK_TO_ITEM_SPEED = 1.75f;
    private static final float FLEE_SPEED = 2.5f;
    private static final int field_38938 = 4;
    private static final int field_38939 = 16;
    private static final int field_38410 = 6;
    private static final int field_38411 = 30;
    private static final int field_38412 = 60;
    private static final int LIKED_NOTEBLOCK_COOLDOWN_TICKS_EXPIRY = 600;
    private static final int WALK_TO_ITEM_RADIUS = 32;
    private static final int GIVE_INVENTORY_RUN_TIME = 20;

    protected static Brain<?> create(Brain<AllayEntity> brain) {
        AllayBrain.addCoreActivities(brain);
        AllayBrain.addIdleActivities(brain);
        brain.setCoreActivities((Set<Activity>)ImmutableSet.of((Object)Activity.CORE));
        brain.setDefaultActivity(Activity.IDLE);
        brain.resetPossibleActivities();
        return brain;
    }

    private static void addCoreActivities(Brain<AllayEntity> brain) {
        brain.setTaskList(Activity.CORE, 0, (ImmutableList<Task<AllayEntity>>)ImmutableList.of(new StayAboveWaterTask(0.8f), new FleeTask(2.5f), (Object)new UpdateLookControlTask(45, 90), (Object)new MoveToTargetTask(), (Object)new TickCooldownTask(MemoryModuleType.LIKED_NOTEBLOCK_COOLDOWN_TICKS), (Object)new TickCooldownTask(MemoryModuleType.ITEM_PICKUP_COOLDOWN_TICKS)));
    }

    private static void addIdleActivities(Brain<AllayEntity> brain) {
        brain.setTaskList(Activity.IDLE, (ImmutableList<Pair<Integer, Task<AllayEntity>>>)ImmutableList.of((Object)Pair.of((Object)0, WalkTowardsNearestVisibleWantedItemTask.create(allay -> true, 1.75f, true, 32)), (Object)Pair.of((Object)1, new GiveInventoryToLookTargetTask(AllayBrain::getLookTarget, 2.25f, 20)), (Object)Pair.of((Object)2, WalkTowardsLookTargetTask.create(AllayBrain::getLookTarget, Predicate.not(AllayBrain::hasNearestVisibleWantedItem), 4, 16, 2.25f)), (Object)Pair.of((Object)3, LookAtMobWithIntervalTask.follow(6.0f, UniformIntProvider.create(30, 60))), (Object)Pair.of((Object)4, new RandomTask(ImmutableList.of((Object)Pair.of(StrollTask.createSolidTargeting(1.0f), (Object)2), (Object)Pair.of(GoToLookTargetTask.create(1.0f, 3), (Object)2), (Object)Pair.of((Object)new WaitTask(30, 60), (Object)1))))), (Set<Pair<MemoryModuleType<?>, MemoryModuleState>>)ImmutableSet.of());
    }

    public static void updateActivities(AllayEntity allay) {
        allay.getBrain().resetPossibleActivities((List<Activity>)ImmutableList.of((Object)Activity.IDLE));
    }

    public static void rememberNoteBlock(LivingEntity allay, BlockPos pos) {
        Brain<?> brain = allay.getBrain();
        GlobalPos globalPos = GlobalPos.create(allay.getEntityWorld().getRegistryKey(), pos);
        Optional<GlobalPos> optional = brain.getOptionalRegisteredMemory(MemoryModuleType.LIKED_NOTEBLOCK);
        if (optional.isEmpty()) {
            brain.remember(MemoryModuleType.LIKED_NOTEBLOCK, globalPos);
            brain.remember(MemoryModuleType.LIKED_NOTEBLOCK_COOLDOWN_TICKS, 600);
        } else if (optional.get().equals(globalPos)) {
            brain.remember(MemoryModuleType.LIKED_NOTEBLOCK_COOLDOWN_TICKS, 600);
        }
    }

    private static Optional<LookTarget> getLookTarget(LivingEntity allay) {
        Brain<?> brain = allay.getBrain();
        Optional<GlobalPos> optional = brain.getOptionalRegisteredMemory(MemoryModuleType.LIKED_NOTEBLOCK);
        if (optional.isPresent()) {
            GlobalPos globalPos = optional.get();
            if (AllayBrain.shouldGoTowardsNoteBlock(allay, brain, globalPos)) {
                return Optional.of(new BlockPosLookTarget(globalPos.pos().up()));
            }
            brain.forget(MemoryModuleType.LIKED_NOTEBLOCK);
        }
        return AllayBrain.getLikedLookTarget(allay);
    }

    private static boolean hasNearestVisibleWantedItem(LivingEntity entity) {
        Brain<ItemEntity> brain = entity.getBrain();
        return brain.hasMemoryModule(MemoryModuleType.NEAREST_VISIBLE_WANTED_ITEM);
    }

    private static boolean shouldGoTowardsNoteBlock(LivingEntity allay, Brain<?> brain, GlobalPos pos) {
        Optional<Integer> optional = brain.getOptionalRegisteredMemory(MemoryModuleType.LIKED_NOTEBLOCK_COOLDOWN_TICKS);
        World world = allay.getEntityWorld();
        return pos.isWithinRange(world.getRegistryKey(), allay.getBlockPos(), 1024) && world.getBlockState(pos.pos()).isOf(Blocks.NOTE_BLOCK) && optional.isPresent();
    }

    private static Optional<LookTarget> getLikedLookTarget(LivingEntity allay) {
        return AllayBrain.getLikedPlayer(allay).map(player -> new EntityLookTarget((Entity)player, true));
    }

    public static Optional<ServerPlayerEntity> getLikedPlayer(LivingEntity allay) {
        World world = allay.getEntityWorld();
        if (!world.isClient() && world instanceof ServerWorld) {
            ServerWorld serverWorld = (ServerWorld)world;
            Optional<UUID> optional = allay.getBrain().getOptionalRegisteredMemory(MemoryModuleType.LIKED_PLAYER);
            if (optional.isPresent()) {
                Entity entity = serverWorld.getEntity(optional.get());
                if (entity instanceof ServerPlayerEntity) {
                    ServerPlayerEntity serverPlayerEntity = (ServerPlayerEntity)entity;
                    if ((serverPlayerEntity.interactionManager.isSurvivalLike() || serverPlayerEntity.interactionManager.isCreative()) && serverPlayerEntity.isInRange(allay, 64.0)) {
                        return Optional.of(serverPlayerEntity);
                    }
                }
                return Optional.empty();
            }
        }
        return Optional.empty();
    }
}


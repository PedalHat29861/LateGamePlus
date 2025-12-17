/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  com.google.common.collect.ImmutableList
 *  com.google.common.collect.ImmutableSet
 *  com.mojang.datafixers.util.Pair
 */
package net.minecraft.entity.mob;

import com.google.common.collect.ImmutableList;
import com.google.common.collect.ImmutableSet;
import com.mojang.datafixers.util.Pair;
import java.util.List;
import java.util.Optional;
import java.util.Set;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.ai.brain.Activity;
import net.minecraft.entity.ai.brain.Brain;
import net.minecraft.entity.ai.brain.MemoryModuleState;
import net.minecraft.entity.ai.brain.MemoryModuleType;
import net.minecraft.entity.ai.brain.sensor.Sensor;
import net.minecraft.entity.ai.brain.sensor.SensorType;
import net.minecraft.entity.ai.brain.task.ForgetAttackTargetTask;
import net.minecraft.entity.ai.brain.task.GoToLookTargetTask;
import net.minecraft.entity.ai.brain.task.LookAtMobWithIntervalTask;
import net.minecraft.entity.ai.brain.task.MeleeAttackTask;
import net.minecraft.entity.ai.brain.task.MoveToTargetTask;
import net.minecraft.entity.ai.brain.task.RandomTask;
import net.minecraft.entity.ai.brain.task.RangedApproachTask;
import net.minecraft.entity.ai.brain.task.StayAboveWaterTask;
import net.minecraft.entity.ai.brain.task.StrollTask;
import net.minecraft.entity.ai.brain.task.Task;
import net.minecraft.entity.ai.brain.task.UpdateAttackTargetTask;
import net.minecraft.entity.ai.brain.task.UpdateLookControlTask;
import net.minecraft.entity.ai.brain.task.WaitTask;
import net.minecraft.entity.mob.CreakingEntity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.util.math.intprovider.UniformIntProvider;

public class CreakingBrain {
    protected static final ImmutableList<? extends SensorType<? extends Sensor<? super CreakingEntity>>> SENSORS = ImmutableList.of(SensorType.NEAREST_LIVING_ENTITIES, SensorType.NEAREST_PLAYERS);
    protected static final ImmutableList<? extends MemoryModuleType<?>> MEMORY_MODULES = ImmutableList.of(MemoryModuleType.MOBS, MemoryModuleType.VISIBLE_MOBS, MemoryModuleType.NEAREST_VISIBLE_PLAYER, MemoryModuleType.NEAREST_VISIBLE_TARGETABLE_PLAYER, MemoryModuleType.NEAREST_VISIBLE_TARGETABLE_PLAYERS, MemoryModuleType.LOOK_TARGET, MemoryModuleType.WALK_TARGET, MemoryModuleType.CANT_REACH_WALK_TARGET_SINCE, MemoryModuleType.PATH, MemoryModuleType.ATTACK_TARGET, MemoryModuleType.ATTACK_COOLING_DOWN);

    static void addCoreTasks(Brain<CreakingEntity> brain) {
        brain.setTaskList(Activity.CORE, 0, (ImmutableList<Task<CreakingEntity>>)ImmutableList.of((Object)new StayAboveWaterTask<CreakingEntity>(0.8f){

            @Override
            protected boolean shouldRun(ServerWorld serverWorld, CreakingEntity creakingEntity) {
                return creakingEntity.isUnrooted() && super.shouldRun(serverWorld, (LivingEntity)creakingEntity);
            }
        }, (Object)new UpdateLookControlTask(45, 90), (Object)new MoveToTargetTask()));
    }

    static void addIdleTasks(Brain<CreakingEntity> brain) {
        brain.setTaskList(Activity.IDLE, 10, (ImmutableList<Task<CreakingEntity>>)ImmutableList.of(UpdateAttackTargetTask.create((ServerWorld world, E creaking) -> creaking.isActive(), (ServerWorld world, E creaking) -> creaking.getBrain().getOptionalRegisteredMemory(MemoryModuleType.NEAREST_VISIBLE_TARGETABLE_PLAYER)), LookAtMobWithIntervalTask.follow(8.0f, UniformIntProvider.create(30, 60)), new RandomTask(ImmutableList.of((Object)Pair.of(StrollTask.create(0.3f), (Object)2), (Object)Pair.of(GoToLookTargetTask.create(0.3f, 3), (Object)2), (Object)Pair.of((Object)new WaitTask(30, 60), (Object)1)))));
    }

    static void addFightTasks(CreakingEntity creaking, Brain<CreakingEntity> brain) {
        brain.setTaskList(Activity.FIGHT, 10, (ImmutableList<Task<CreakingEntity>>)ImmutableList.of(RangedApproachTask.create(1.0f), MeleeAttackTask.create(CreakingEntity::isUnrooted, 40), ForgetAttackTargetTask.create((world, target) -> !CreakingBrain.canTarget(creaking, target))), (Set<Pair<MemoryModuleType<?>, MemoryModuleState>>)ImmutableSet.of((Object)Pair.of(MemoryModuleType.ATTACK_TARGET, (Object)((Object)MemoryModuleState.VALUE_PRESENT))));
    }

    private static boolean canTarget(CreakingEntity creaking, LivingEntity target) {
        Optional<List<PlayerEntity>> optional = creaking.getBrain().getOptionalRegisteredMemory(MemoryModuleType.NEAREST_VISIBLE_TARGETABLE_PLAYERS);
        return optional.map(players -> {
            PlayerEntity playerEntity;
            return target instanceof PlayerEntity && players.contains(playerEntity = (PlayerEntity)target);
        }).orElse(false);
    }

    public static Brain.Profile<CreakingEntity> createBrainProfile() {
        return Brain.createProfile(MEMORY_MODULES, SENSORS);
    }

    public static Brain<CreakingEntity> create(CreakingEntity creaking, Brain<CreakingEntity> brain) {
        CreakingBrain.addCoreTasks(brain);
        CreakingBrain.addIdleTasks(brain);
        CreakingBrain.addFightTasks(creaking, brain);
        brain.setCoreActivities((Set<Activity>)ImmutableSet.of((Object)Activity.CORE));
        brain.setDefaultActivity(Activity.IDLE);
        brain.resetPossibleActivities();
        return brain;
    }

    public static void updateActivities(CreakingEntity creaking) {
        if (!creaking.isUnrooted()) {
            creaking.getBrain().resetPossibleActivities();
        } else {
            creaking.getBrain().resetPossibleActivities((List<Activity>)ImmutableList.of((Object)Activity.FIGHT, (Object)Activity.IDLE));
        }
    }
}


/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  com.google.common.collect.ImmutableList
 *  com.google.common.collect.ImmutableMap
 *  com.google.common.collect.ImmutableSet
 *  com.mojang.datafixers.util.Pair
 */
package net.minecraft.entity.passive;

import com.google.common.collect.ImmutableList;
import com.google.common.collect.ImmutableMap;
import com.google.common.collect.ImmutableSet;
import com.mojang.datafixers.util.Pair;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.function.Predicate;
import net.minecraft.entity.EntityType;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.ai.brain.Activity;
import net.minecraft.entity.ai.brain.Brain;
import net.minecraft.entity.ai.brain.MemoryModuleState;
import net.minecraft.entity.ai.brain.MemoryModuleType;
import net.minecraft.entity.ai.brain.sensor.Sensor;
import net.minecraft.entity.ai.brain.sensor.SensorType;
import net.minecraft.entity.ai.brain.task.BreedTask;
import net.minecraft.entity.ai.brain.task.FleeTask;
import net.minecraft.entity.ai.brain.task.GoToLookTargetTask;
import net.minecraft.entity.ai.brain.task.LookAroundTask;
import net.minecraft.entity.ai.brain.task.LookAtMobWithIntervalTask;
import net.minecraft.entity.ai.brain.task.MoveToTargetTask;
import net.minecraft.entity.ai.brain.task.MultiTickTask;
import net.minecraft.entity.ai.brain.task.RandomTask;
import net.minecraft.entity.ai.brain.task.StayAboveWaterTask;
import net.minecraft.entity.ai.brain.task.StrollTask;
import net.minecraft.entity.ai.brain.task.Task;
import net.minecraft.entity.ai.brain.task.TaskTriggerer;
import net.minecraft.entity.ai.brain.task.TemptTask;
import net.minecraft.entity.ai.brain.task.TickCooldownTask;
import net.minecraft.entity.ai.brain.task.UpdateLookControlTask;
import net.minecraft.entity.ai.brain.task.WaitTask;
import net.minecraft.entity.ai.brain.task.WalkTowardsEntityTask;
import net.minecraft.entity.mob.PathAwareEntity;
import net.minecraft.entity.passive.CamelEntity;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.util.math.intprovider.UniformIntProvider;
import net.minecraft.util.math.random.Random;

public class CamelBrain {
    private static final float WALK_SPEED = 4.0f;
    private static final float field_40153 = 2.0f;
    private static final float field_40154 = 2.5f;
    private static final float field_40155 = 2.5f;
    private static final float BREED_SPEED = 1.0f;
    private static final UniformIntProvider WALK_TOWARD_ADULT_RANGE = UniformIntProvider.create(5, 16);
    private static final ImmutableList<SensorType<? extends Sensor<? super CamelEntity>>> SENSORS = ImmutableList.of(SensorType.NEAREST_LIVING_ENTITIES, SensorType.HURT_BY, SensorType.FOOD_TEMPTATIONS, SensorType.NEAREST_ADULT);
    private static final ImmutableList<MemoryModuleType<?>> MEMORY_MODULES = ImmutableList.of(MemoryModuleType.IS_PANICKING, MemoryModuleType.HURT_BY, MemoryModuleType.HURT_BY_ENTITY, MemoryModuleType.WALK_TARGET, MemoryModuleType.LOOK_TARGET, MemoryModuleType.CANT_REACH_WALK_TARGET_SINCE, MemoryModuleType.PATH, MemoryModuleType.VISIBLE_MOBS, MemoryModuleType.TEMPTING_PLAYER, MemoryModuleType.TEMPTATION_COOLDOWN_TICKS, MemoryModuleType.GAZE_COOLDOWN_TICKS, MemoryModuleType.IS_TEMPTED, (Object[])new MemoryModuleType[]{MemoryModuleType.BREED_TARGET, MemoryModuleType.NEAREST_VISIBLE_ADULT});

    protected static void initialize(CamelEntity camel, Random random) {
    }

    public static Brain.Profile<CamelEntity> createBrainProfile() {
        return Brain.createProfile(MEMORY_MODULES, SENSORS);
    }

    protected static Brain<?> create(Brain<CamelEntity> brain) {
        CamelBrain.addCoreActivities(brain);
        CamelBrain.addIdleActivities(brain);
        brain.setCoreActivities((Set<Activity>)ImmutableSet.of((Object)Activity.CORE));
        brain.setDefaultActivity(Activity.IDLE);
        brain.resetPossibleActivities();
        return brain;
    }

    private static void addCoreActivities(Brain<CamelEntity> brain) {
        brain.setTaskList(Activity.CORE, 0, (ImmutableList<Task<CamelEntity>>)ImmutableList.of(new StayAboveWaterTask(0.8f), (Object)new CamelWalkTask(4.0f), (Object)new UpdateLookControlTask(45, 90), (Object)new MoveToTargetTask(), (Object)new TickCooldownTask(MemoryModuleType.TEMPTATION_COOLDOWN_TICKS), (Object)new TickCooldownTask(MemoryModuleType.GAZE_COOLDOWN_TICKS)));
    }

    private static void addIdleActivities(Brain<CamelEntity> brain) {
        brain.setTaskList(Activity.IDLE, (ImmutableList<Pair<Integer, Task<CamelEntity>>>)ImmutableList.of((Object)Pair.of((Object)0, LookAtMobWithIntervalTask.follow(EntityType.PLAYER, 6.0f, UniformIntProvider.create(30, 60))), (Object)Pair.of((Object)1, (Object)new BreedTask(EntityType.CAMEL)), (Object)Pair.of((Object)2, new RandomTask(ImmutableList.of((Object)Pair.of((Object)new TemptTask(entity -> Float.valueOf(2.5f), entity -> entity.isBaby() ? 2.5 : 3.5), (Object)1), (Object)Pair.of(TaskTriggerer.runIf(Predicate.not(CamelEntity::isStationary), WalkTowardsEntityTask.createNearestVisibleAdult(WALK_TOWARD_ADULT_RANGE, 2.5f)), (Object)1)))), (Object)Pair.of((Object)3, (Object)new LookAroundTask(UniformIntProvider.create(150, 250), 30.0f, 0.0f, 0.0f)), (Object)Pair.of((Object)4, new RandomTask((Map<MemoryModuleType<?>, MemoryModuleState>)ImmutableMap.of(MemoryModuleType.WALK_TARGET, (Object)((Object)MemoryModuleState.VALUE_ABSENT)), ImmutableList.of((Object)Pair.of(TaskTriggerer.runIf(Predicate.not(CamelEntity::isStationary), StrollTask.create(2.0f)), (Object)1), (Object)Pair.of(TaskTriggerer.runIf(Predicate.not(CamelEntity::isStationary), GoToLookTargetTask.create(2.0f, 3)), (Object)1), (Object)Pair.of((Object)new SitOrStandTask(20), (Object)1), (Object)Pair.of((Object)new WaitTask(30, 60), (Object)1))))));
    }

    public static void updateActivities(CamelEntity camel) {
        camel.getBrain().resetPossibleActivities((List<Activity>)ImmutableList.of((Object)Activity.IDLE));
    }

    public static class CamelWalkTask
    extends FleeTask<CamelEntity> {
        public CamelWalkTask(float f) {
            super(f);
        }

        @Override
        protected boolean shouldRun(ServerWorld serverWorld, CamelEntity camelEntity) {
            return super.shouldRun(serverWorld, camelEntity) && !camelEntity.isControlledByMob();
        }

        @Override
        protected void run(ServerWorld serverWorld, CamelEntity camelEntity, long l) {
            camelEntity.setStanding();
            super.run(serverWorld, camelEntity, l);
        }

        @Override
        protected /* synthetic */ void run(ServerWorld serverWorld, PathAwareEntity pathAwareEntity, long l) {
            this.run(serverWorld, (CamelEntity)pathAwareEntity, l);
        }

        @Override
        protected /* synthetic */ void run(ServerWorld world, LivingEntity entity, long time) {
            this.run(world, (CamelEntity)entity, time);
        }
    }

    public static class SitOrStandTask
    extends MultiTickTask<CamelEntity> {
        private final int lastTimeSinceLastPoseTick;

        public SitOrStandTask(int lastPoseSecondsDelta) {
            super((Map<MemoryModuleType<?>, MemoryModuleState>)ImmutableMap.of());
            this.lastTimeSinceLastPoseTick = lastPoseSecondsDelta * 20;
        }

        @Override
        protected boolean shouldRun(ServerWorld serverWorld, CamelEntity camelEntity) {
            return !camelEntity.isTouchingWater() && camelEntity.getTimeSinceLastPoseTick() >= (long)this.lastTimeSinceLastPoseTick && !camelEntity.isLeashed() && camelEntity.isOnGround() && !camelEntity.hasControllingPassenger() && camelEntity.canChangePose();
        }

        @Override
        protected void run(ServerWorld serverWorld, CamelEntity camelEntity, long l) {
            if (camelEntity.isSitting()) {
                camelEntity.startStanding();
            } else if (!camelEntity.isPanicking()) {
                camelEntity.startSitting();
            }
        }

        @Override
        protected /* synthetic */ void run(ServerWorld world, LivingEntity entity, long time) {
            this.run(world, (CamelEntity)entity, time);
        }
    }
}


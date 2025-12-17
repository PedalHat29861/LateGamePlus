/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  com.google.common.collect.ImmutableList
 *  com.google.common.collect.ImmutableMap
 *  com.google.common.collect.ImmutableSet
 *  com.mojang.datafixers.kinds.Applicative
 *  com.mojang.datafixers.util.Pair
 *  com.mojang.serialization.Dynamic
 */
package net.minecraft.entity.mob;

import com.google.common.collect.ImmutableList;
import com.google.common.collect.ImmutableMap;
import com.google.common.collect.ImmutableSet;
import com.mojang.datafixers.kinds.Applicative;
import com.mojang.datafixers.util.Pair;
import com.mojang.serialization.Dynamic;
import java.util.List;
import java.util.Map;
import java.util.Set;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.ai.brain.Activity;
import net.minecraft.entity.ai.brain.BlockPosLookTarget;
import net.minecraft.entity.ai.brain.Brain;
import net.minecraft.entity.ai.brain.MemoryModuleState;
import net.minecraft.entity.ai.brain.MemoryModuleType;
import net.minecraft.entity.ai.brain.sensor.Sensor;
import net.minecraft.entity.ai.brain.sensor.SensorType;
import net.minecraft.entity.ai.brain.task.DigTask;
import net.minecraft.entity.ai.brain.task.DismountVehicleTask;
import net.minecraft.entity.ai.brain.task.EmergeTask;
import net.minecraft.entity.ai.brain.task.FindRoarTargetTask;
import net.minecraft.entity.ai.brain.task.ForgetAttackTargetTask;
import net.minecraft.entity.ai.brain.task.LookAtDisturbanceTask;
import net.minecraft.entity.ai.brain.task.LookAtMobTask;
import net.minecraft.entity.ai.brain.task.MeleeAttackTask;
import net.minecraft.entity.ai.brain.task.MoveToTargetTask;
import net.minecraft.entity.ai.brain.task.RandomTask;
import net.minecraft.entity.ai.brain.task.RangedApproachTask;
import net.minecraft.entity.ai.brain.task.RoarTask;
import net.minecraft.entity.ai.brain.task.SniffTask;
import net.minecraft.entity.ai.brain.task.SonicBoomTask;
import net.minecraft.entity.ai.brain.task.StartSniffingTask;
import net.minecraft.entity.ai.brain.task.StayAboveWaterTask;
import net.minecraft.entity.ai.brain.task.StrollTask;
import net.minecraft.entity.ai.brain.task.Task;
import net.minecraft.entity.ai.brain.task.TaskTriggerer;
import net.minecraft.entity.ai.brain.task.UpdateLookControlTask;
import net.minecraft.entity.ai.brain.task.WaitTask;
import net.minecraft.entity.ai.brain.task.WalkTowardsFuzzyPosTask;
import net.minecraft.entity.attribute.EntityAttributes;
import net.minecraft.entity.mob.WardenEntity;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.util.Unit;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.MathHelper;

public class WardenBrain {
    private static final float STROLL_SPEED = 0.5f;
    private static final float CELEBRATE_TIME = 0.7f;
    private static final float RANGED_APPROACH_SPEED = 1.2f;
    private static final int MELEE_ATTACK_INTERVAL = 18;
    private static final int DIG_DURATION = MathHelper.ceil(100.0f);
    public static final int EMERGE_DURATION = MathHelper.ceil(133.59999f);
    public static final int ROAR_DURATION = MathHelper.ceil(84.0f);
    private static final int SNIFF_DURATION = MathHelper.ceil(83.2f);
    public static final int DIG_COOLDOWN = 1200;
    private static final int field_38181 = 100;
    private static final List<SensorType<? extends Sensor<? super WardenEntity>>> SENSORS = List.of(SensorType.NEAREST_PLAYERS, SensorType.WARDEN_ENTITY_SENSOR);
    private static final List<MemoryModuleType<?>> MEMORY_MODULES = List.of(MemoryModuleType.MOBS, MemoryModuleType.VISIBLE_MOBS, MemoryModuleType.NEAREST_VISIBLE_PLAYER, MemoryModuleType.NEAREST_VISIBLE_TARGETABLE_PLAYER, MemoryModuleType.NEAREST_VISIBLE_NEMESIS, MemoryModuleType.LOOK_TARGET, MemoryModuleType.WALK_TARGET, MemoryModuleType.CANT_REACH_WALK_TARGET_SINCE, MemoryModuleType.PATH, MemoryModuleType.ATTACK_TARGET, MemoryModuleType.ATTACK_COOLING_DOWN, MemoryModuleType.NEAREST_ATTACKABLE, MemoryModuleType.ROAR_TARGET, MemoryModuleType.DISTURBANCE_LOCATION, MemoryModuleType.RECENT_PROJECTILE, MemoryModuleType.IS_SNIFFING, MemoryModuleType.IS_EMERGING, MemoryModuleType.ROAR_SOUND_DELAY, MemoryModuleType.DIG_COOLDOWN, MemoryModuleType.ROAR_SOUND_COOLDOWN, MemoryModuleType.SNIFF_COOLDOWN, MemoryModuleType.TOUCH_COOLDOWN, MemoryModuleType.VIBRATION_COOLDOWN, MemoryModuleType.SONIC_BOOM_COOLDOWN, MemoryModuleType.SONIC_BOOM_SOUND_COOLDOWN, MemoryModuleType.SONIC_BOOM_SOUND_DELAY);
    private static final Task<WardenEntity> RESET_DIG_COOLDOWN_TASK = TaskTriggerer.task(context -> context.group(context.queryMemoryOptional(MemoryModuleType.DIG_COOLDOWN)).apply((Applicative)context, digCooldown -> (world, entity, time) -> {
        if (context.getOptionalValue(digCooldown).isPresent()) {
            digCooldown.remember(Unit.INSTANCE, 1200L);
        }
        return true;
    }));

    public static void updateActivities(WardenEntity warden) {
        warden.getBrain().resetPossibleActivities((List<Activity>)ImmutableList.of((Object)Activity.EMERGE, (Object)Activity.DIG, (Object)Activity.ROAR, (Object)Activity.FIGHT, (Object)Activity.INVESTIGATE, (Object)Activity.SNIFF, (Object)Activity.IDLE));
    }

    protected static Brain<?> create(WardenEntity warden, Dynamic<?> dynamic) {
        Brain.Profile profile = Brain.createProfile(MEMORY_MODULES, SENSORS);
        Brain<WardenEntity> brain = profile.deserialize(dynamic);
        WardenBrain.addCoreActivities(brain);
        WardenBrain.addEmergeActivities(brain);
        WardenBrain.addDigActivities(brain);
        WardenBrain.addIdleActivities(brain);
        WardenBrain.addRoarActivities(brain);
        WardenBrain.addFightActivities(warden, brain);
        WardenBrain.addInvestigateActivities(brain);
        WardenBrain.addSniffActivities(brain);
        brain.setCoreActivities((Set<Activity>)ImmutableSet.of((Object)Activity.CORE));
        brain.setDefaultActivity(Activity.IDLE);
        brain.resetPossibleActivities();
        return brain;
    }

    private static void addCoreActivities(Brain<WardenEntity> brain) {
        brain.setTaskList(Activity.CORE, 0, (ImmutableList<Task<WardenEntity>>)ImmutableList.of(new StayAboveWaterTask(0.8f), LookAtDisturbanceTask.create(), (Object)new UpdateLookControlTask(45, 90), (Object)new MoveToTargetTask()));
    }

    private static void addEmergeActivities(Brain<WardenEntity> brain) {
        brain.setTaskList(Activity.EMERGE, 5, (ImmutableList<Task<WardenEntity>>)ImmutableList.of(new EmergeTask(EMERGE_DURATION)), MemoryModuleType.IS_EMERGING);
    }

    private static void addDigActivities(Brain<WardenEntity> brain) {
        brain.setTaskList(Activity.DIG, (ImmutableList<Pair<Integer, Task<WardenEntity>>>)ImmutableList.of((Object)Pair.of((Object)0, (Object)new DismountVehicleTask()), (Object)Pair.of((Object)1, new DigTask(DIG_DURATION))), (Set<Pair<MemoryModuleType<?>, MemoryModuleState>>)ImmutableSet.of((Object)Pair.of(MemoryModuleType.ROAR_TARGET, (Object)((Object)MemoryModuleState.VALUE_ABSENT)), (Object)Pair.of(MemoryModuleType.DIG_COOLDOWN, (Object)((Object)MemoryModuleState.VALUE_ABSENT))));
    }

    private static void addIdleActivities(Brain<WardenEntity> brain) {
        brain.setTaskList(Activity.IDLE, 10, (ImmutableList<Task<WardenEntity>>)ImmutableList.of(FindRoarTargetTask.create(WardenEntity::getPrimeSuspect), StartSniffingTask.create(), new RandomTask((Map<MemoryModuleType<?>, MemoryModuleState>)ImmutableMap.of(MemoryModuleType.IS_SNIFFING, (Object)((Object)MemoryModuleState.VALUE_ABSENT)), ImmutableList.of((Object)Pair.of(StrollTask.create(0.5f), (Object)2), (Object)Pair.of((Object)new WaitTask(30, 60), (Object)1)))));
    }

    private static void addInvestigateActivities(Brain<WardenEntity> brain) {
        brain.setTaskList(Activity.INVESTIGATE, 5, (ImmutableList<Task<WardenEntity>>)ImmutableList.of(FindRoarTargetTask.create(WardenEntity::getPrimeSuspect), WalkTowardsFuzzyPosTask.create(MemoryModuleType.DISTURBANCE_LOCATION, 2, 0.7f)), MemoryModuleType.DISTURBANCE_LOCATION);
    }

    private static void addSniffActivities(Brain<WardenEntity> brain) {
        brain.setTaskList(Activity.SNIFF, 5, (ImmutableList<Task<WardenEntity>>)ImmutableList.of(FindRoarTargetTask.create(WardenEntity::getPrimeSuspect), new SniffTask(SNIFF_DURATION)), MemoryModuleType.IS_SNIFFING);
    }

    private static void addRoarActivities(Brain<WardenEntity> brain) {
        brain.setTaskList(Activity.ROAR, 10, (ImmutableList<Task<WardenEntity>>)ImmutableList.of((Object)new RoarTask()), MemoryModuleType.ROAR_TARGET);
    }

    private static void addFightActivities(WardenEntity warden, Brain<WardenEntity> brain) {
        brain.setTaskList(Activity.FIGHT, 10, (ImmutableList<Task<WardenEntity>>)ImmutableList.of(RESET_DIG_COOLDOWN_TASK, ForgetAttackTargetTask.create((world, target) -> !warden.getAngriness().isAngry() || !warden.isValidTarget(target), WardenBrain::removeDeadSuspect, false), LookAtMobTask.create((LivingEntity entity) -> WardenBrain.isTargeting(warden, entity), (float)warden.getAttributeValue(EntityAttributes.FOLLOW_RANGE)), RangedApproachTask.create(1.2f), (Object)new SonicBoomTask(), MeleeAttackTask.create(18)), MemoryModuleType.ATTACK_TARGET);
    }

    private static boolean isTargeting(WardenEntity warden, LivingEntity entity) {
        return warden.getBrain().getOptionalRegisteredMemory(MemoryModuleType.ATTACK_TARGET).filter(entityx -> entityx == entity).isPresent();
    }

    private static void removeDeadSuspect(ServerWorld world, WardenEntity warden, LivingEntity target) {
        if (!warden.isValidTarget(target)) {
            warden.removeSuspect(target);
        }
        WardenBrain.resetDigCooldown(warden);
    }

    public static void resetDigCooldown(LivingEntity warden) {
        if (warden.getBrain().hasMemoryModule(MemoryModuleType.DIG_COOLDOWN)) {
            warden.getBrain().remember(MemoryModuleType.DIG_COOLDOWN, Unit.INSTANCE, 1200L);
        }
    }

    public static void lookAtDisturbance(WardenEntity warden, BlockPos pos) {
        if (!warden.getEntityWorld().getWorldBorder().contains(pos) || warden.getPrimeSuspect().isPresent() || warden.getBrain().getOptionalRegisteredMemory(MemoryModuleType.ATTACK_TARGET).isPresent()) {
            return;
        }
        WardenBrain.resetDigCooldown(warden);
        warden.getBrain().remember(MemoryModuleType.SNIFF_COOLDOWN, Unit.INSTANCE, 100L);
        warden.getBrain().remember(MemoryModuleType.LOOK_TARGET, new BlockPosLookTarget(pos), 100L);
        warden.getBrain().remember(MemoryModuleType.DISTURBANCE_LOCATION, pos, 100L);
        warden.getBrain().forget(MemoryModuleType.WALK_TARGET);
    }
}


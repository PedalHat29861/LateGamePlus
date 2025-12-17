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
import java.util.Optional;
import java.util.Set;
import java.util.function.Predicate;
import net.minecraft.entity.EntityType;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.ai.TargetPredicate;
import net.minecraft.entity.ai.brain.Activity;
import net.minecraft.entity.ai.brain.Brain;
import net.minecraft.entity.ai.brain.LivingTargetCache;
import net.minecraft.entity.ai.brain.MemoryModuleState;
import net.minecraft.entity.ai.brain.MemoryModuleType;
import net.minecraft.entity.ai.brain.sensor.Sensor;
import net.minecraft.entity.ai.brain.sensor.SensorType;
import net.minecraft.entity.ai.brain.task.BreedTask;
import net.minecraft.entity.ai.brain.task.CompositeTask;
import net.minecraft.entity.ai.brain.task.DashAttackTask;
import net.minecraft.entity.ai.brain.task.FleeTask;
import net.minecraft.entity.ai.brain.task.GoToLookTargetTask;
import net.minecraft.entity.ai.brain.task.MoveToTargetTask;
import net.minecraft.entity.ai.brain.task.StrollTask;
import net.minecraft.entity.ai.brain.task.TargetUtil;
import net.minecraft.entity.ai.brain.task.Task;
import net.minecraft.entity.ai.brain.task.TemptTask;
import net.minecraft.entity.ai.brain.task.TickCooldownTask;
import net.minecraft.entity.ai.brain.task.UpdateAttackTargetTask;
import net.minecraft.entity.ai.brain.task.UpdateLookControlTask;
import net.minecraft.entity.passive.AbstractNautilusEntity;
import net.minecraft.entity.passive.NautilusEntity;
import net.minecraft.item.ItemStack;
import net.minecraft.registry.tag.EntityTypeTags;
import net.minecraft.registry.tag.ItemTags;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.sound.SoundEvents;
import net.minecraft.util.math.intprovider.UniformIntProvider;
import net.minecraft.util.math.random.Random;
import net.minecraft.world.rule.GameRules;

public class NautilusBrain {
    private static final float field_63352 = 1.0f;
    private static final float field_63353 = 1.3f;
    private static final float field_63354 = 0.4f;
    private static final float field_63355 = 1.6f;
    private static final UniformIntProvider ATTACK_TARGET_COOLDOWN = UniformIntProvider.create(2400, 3600);
    private static final float field_63357 = 0.6f;
    private static final float field_63358 = 2.0f;
    private static final int field_63359 = 400;
    private static final int field_63360 = 80;
    private static final double field_63361 = 12.0;
    private static final double field_63362 = 11.0;
    protected static final TargetPredicate FIGHT_TARGET_PREDICATE = TargetPredicate.createAttackable().setPredicate((entity, world) -> (world.getGameRules().getValue(GameRules.DO_MOB_GRIEFING) != false || !entity.getType().equals(EntityType.ARMOR_STAND)) && world.getWorldBorder().contains(entity.getBoundingBox()));
    protected static final ImmutableList<SensorType<? extends Sensor<? super NautilusEntity>>> SENSORS = ImmutableList.of(SensorType.NEAREST_LIVING_ENTITIES, SensorType.NEAREST_ADULT, SensorType.NEAREST_PLAYERS, SensorType.HURT_BY, SensorType.NAUTILUS_TEMPTATIONS);
    protected static final ImmutableList<MemoryModuleType<?>> MEMORY_MODULES = ImmutableList.of(MemoryModuleType.LOOK_TARGET, MemoryModuleType.VISIBLE_MOBS, MemoryModuleType.WALK_TARGET, MemoryModuleType.CANT_REACH_WALK_TARGET_SINCE, MemoryModuleType.PATH, MemoryModuleType.NEAREST_VISIBLE_ADULT, MemoryModuleType.TEMPTATION_COOLDOWN_TICKS, MemoryModuleType.IS_TEMPTED, MemoryModuleType.TEMPTING_PLAYER, MemoryModuleType.BREED_TARGET, MemoryModuleType.IS_PANICKING, MemoryModuleType.ATTACK_TARGET, (Object[])new MemoryModuleType[]{MemoryModuleType.CHARGE_COOLDOWN_TICKS, MemoryModuleType.HURT_BY, MemoryModuleType.ANGRY_AT, MemoryModuleType.ATTACK_TARGET_COOLDOWN});

    protected static void initialize(AbstractNautilusEntity nautilus, Random random) {
        nautilus.getBrain().remember(MemoryModuleType.ATTACK_TARGET_COOLDOWN, ATTACK_TARGET_COOLDOWN.get(random));
    }

    protected static Brain.Profile<NautilusEntity> createProfile() {
        return Brain.createProfile(MEMORY_MODULES, SENSORS);
    }

    protected static Brain<?> create(Brain<NautilusEntity> brain) {
        NautilusBrain.addCoreActivities(brain);
        NautilusBrain.addIdleActivities(brain);
        NautilusBrain.addFightActivities(brain);
        brain.setCoreActivities((Set<Activity>)ImmutableSet.of((Object)Activity.CORE));
        brain.setDefaultActivity(Activity.IDLE);
        brain.resetPossibleActivities();
        return brain;
    }

    private static void addCoreActivities(Brain<NautilusEntity> brain) {
        brain.setTaskList(Activity.CORE, 0, (ImmutableList<Task<NautilusEntity>>)ImmutableList.of(new FleeTask(1.6f), (Object)new UpdateLookControlTask(45, 90), (Object)new MoveToTargetTask(), (Object)new TickCooldownTask(MemoryModuleType.TEMPTATION_COOLDOWN_TICKS), (Object)new TickCooldownTask(MemoryModuleType.CHARGE_COOLDOWN_TICKS), (Object)new TickCooldownTask(MemoryModuleType.ATTACK_TARGET_COOLDOWN)));
    }

    private static void addIdleActivities(Brain<NautilusEntity> brain) {
        brain.setTaskList(Activity.IDLE, (ImmutableList<Pair<Integer, Task<NautilusEntity>>>)ImmutableList.of((Object)Pair.of((Object)1, (Object)new BreedTask(EntityType.NAUTILUS, 0.4f, 2)), (Object)Pair.of((Object)2, (Object)new TemptTask(entity -> Float.valueOf(1.3f), entity -> entity.isBaby() ? 2.5 : 3.5)), (Object)Pair.of((Object)3, UpdateAttackTargetTask.create(NautilusBrain::findAttackTarget)), (Object)Pair.of((Object)4, new CompositeTask((Map<MemoryModuleType<?>, MemoryModuleState>)ImmutableMap.of(MemoryModuleType.WALK_TARGET, (Object)((Object)MemoryModuleState.VALUE_ABSENT)), (Set<MemoryModuleType<?>>)ImmutableSet.of(), CompositeTask.Order.ORDERED, CompositeTask.RunMode.TRY_ALL, ImmutableList.of((Object)Pair.of(StrollTask.createDynamicRadius(1.0f), (Object)2), (Object)Pair.of(GoToLookTargetTask.create(1.0f, 3), (Object)3))))));
    }

    private static void addFightActivities(Brain<NautilusEntity> brain) {
        brain.setTaskList(Activity.FIGHT, (ImmutableList<Pair<Integer, Task<NautilusEntity>>>)ImmutableList.of((Object)Pair.of((Object)0, (Object)new DashAttackTask(80, FIGHT_TARGET_PREDICATE, 0.6f, 2.0f, 12.0, 11.0, SoundEvents.ENTITY_NAUTILUS_DASH))), (Set<Pair<MemoryModuleType<?>, MemoryModuleState>>)ImmutableSet.of((Object)Pair.of(MemoryModuleType.ATTACK_TARGET, (Object)((Object)MemoryModuleState.VALUE_PRESENT)), (Object)Pair.of(MemoryModuleType.TEMPTING_PLAYER, (Object)((Object)MemoryModuleState.VALUE_ABSENT)), (Object)Pair.of(MemoryModuleType.BREED_TARGET, (Object)((Object)MemoryModuleState.VALUE_ABSENT)), (Object)Pair.of(MemoryModuleType.CHARGE_COOLDOWN_TICKS, (Object)((Object)MemoryModuleState.VALUE_ABSENT))));
    }

    protected static Optional<? extends LivingEntity> findAttackTarget(ServerWorld world, AbstractNautilusEntity nautilus) {
        if (TargetUtil.hasBreedTarget(nautilus) || !nautilus.isTouchingWater() || nautilus.isBaby() || nautilus.isTamed()) {
            return Optional.empty();
        }
        Optional<LivingEntity> optional = TargetUtil.getEntity(nautilus, MemoryModuleType.ANGRY_AT).filter(target -> target.isTouchingWater() && Sensor.testAttackableTargetPredicateIgnoreVisibility(world, nautilus, target));
        if (optional.isPresent()) {
            return optional;
        }
        if (nautilus.getBrain().hasMemoryModule(MemoryModuleType.ATTACK_TARGET_COOLDOWN)) {
            return Optional.empty();
        }
        nautilus.getBrain().remember(MemoryModuleType.ATTACK_TARGET_COOLDOWN, ATTACK_TARGET_COOLDOWN.get(world.random));
        if (world.random.nextFloat() < 0.5f) {
            return Optional.empty();
        }
        Optional<LivingEntity> optional2 = nautilus.getBrain().getOptionalRegisteredMemory(MemoryModuleType.VISIBLE_MOBS).orElse(LivingTargetCache.empty()).findFirst(NautilusBrain::isTarget);
        return optional2;
    }

    protected static void onDamage(ServerWorld world, AbstractNautilusEntity nautilus, LivingEntity attacker) {
        if (Sensor.testAttackableTargetPredicateIgnoreVisibility(world, nautilus, attacker)) {
            nautilus.getBrain().forget(MemoryModuleType.CANT_REACH_WALK_TARGET_SINCE);
            nautilus.getBrain().remember(MemoryModuleType.ANGRY_AT, attacker.getUuid(), 400L);
        }
    }

    private static boolean isTarget(LivingEntity entity) {
        return entity.isTouchingWater() && entity.getType().isIn(EntityTypeTags.NAUTILUS_HOSTILES);
    }

    public static void updateActivities(NautilusEntity nautilus) {
        nautilus.getBrain().resetPossibleActivities((List<Activity>)ImmutableList.of((Object)Activity.FIGHT, (Object)Activity.IDLE));
    }

    public static Predicate<ItemStack> getNautilusFoodPredicate() {
        return stack -> stack.isIn(ItemTags.NAUTILUS_FOOD);
    }
}


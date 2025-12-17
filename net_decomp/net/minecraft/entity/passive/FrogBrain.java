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
import net.minecraft.block.BlockState;
import net.minecraft.block.Blocks;
import net.minecraft.entity.Entity;
import net.minecraft.entity.EntityType;
import net.minecraft.entity.ai.brain.Activity;
import net.minecraft.entity.ai.brain.Brain;
import net.minecraft.entity.ai.brain.MemoryModuleState;
import net.minecraft.entity.ai.brain.MemoryModuleType;
import net.minecraft.entity.ai.brain.task.BiasedLongJumpTask;
import net.minecraft.entity.ai.brain.task.BreedTask;
import net.minecraft.entity.ai.brain.task.CompositeTask;
import net.minecraft.entity.ai.brain.task.CroakTask;
import net.minecraft.entity.ai.brain.task.FleeTask;
import net.minecraft.entity.ai.brain.task.ForgetAttackTargetTask;
import net.minecraft.entity.ai.brain.task.FrogEatEntityTask;
import net.minecraft.entity.ai.brain.task.GoToLookTargetTask;
import net.minecraft.entity.ai.brain.task.LayFrogSpawnTask;
import net.minecraft.entity.ai.brain.task.LeapingChargeTask;
import net.minecraft.entity.ai.brain.task.LongJumpTask;
import net.minecraft.entity.ai.brain.task.LookAtMobWithIntervalTask;
import net.minecraft.entity.ai.brain.task.MoveToTargetTask;
import net.minecraft.entity.ai.brain.task.RandomTask;
import net.minecraft.entity.ai.brain.task.StrollTask;
import net.minecraft.entity.ai.brain.task.TargetUtil;
import net.minecraft.entity.ai.brain.task.Task;
import net.minecraft.entity.ai.brain.task.TaskTriggerer;
import net.minecraft.entity.ai.brain.task.TemptTask;
import net.minecraft.entity.ai.brain.task.TickCooldownTask;
import net.minecraft.entity.ai.brain.task.UpdateAttackTargetTask;
import net.minecraft.entity.ai.brain.task.UpdateLookControlTask;
import net.minecraft.entity.ai.brain.task.WalkTowardsLandTask;
import net.minecraft.entity.ai.brain.task.WalkTowardsWaterTask;
import net.minecraft.entity.ai.pathing.LandPathNodeMaker;
import net.minecraft.entity.ai.pathing.PathContext;
import net.minecraft.entity.ai.pathing.PathNodeType;
import net.minecraft.entity.mob.MobEntity;
import net.minecraft.entity.passive.FrogEntity;
import net.minecraft.item.ItemStack;
import net.minecraft.registry.tag.BlockTags;
import net.minecraft.registry.tag.ItemTags;
import net.minecraft.sound.SoundEvents;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.intprovider.UniformIntProvider;
import net.minecraft.util.math.random.Random;
import net.minecraft.world.World;

public class FrogBrain {
    private static final float FLEE_SPEED = 2.0f;
    private static final float field_37471 = 1.0f;
    private static final float field_37472 = 1.0f;
    private static final float field_37473 = 0.75f;
    private static final UniformIntProvider LONG_JUMP_COOLDOWN_RANGE = UniformIntProvider.create(100, 140);
    private static final int field_37475 = 2;
    private static final int field_37476 = 4;
    private static final float field_49092 = 3.5714288f;
    private static final float TEMPT_SPEED = 1.25f;

    protected static void coolDownLongJump(FrogEntity frog, Random random) {
        frog.getBrain().remember(MemoryModuleType.LONG_JUMP_COOLING_DOWN, LONG_JUMP_COOLDOWN_RANGE.get(random));
    }

    protected static Brain<?> create(Brain<FrogEntity> brain) {
        FrogBrain.addCoreActivities(brain);
        FrogBrain.addIdleActivities(brain);
        FrogBrain.addSwimActivities(brain);
        FrogBrain.addLaySpawnActivities(brain);
        FrogBrain.addTongueActivities(brain);
        FrogBrain.addLongJumpActivities(brain);
        brain.setCoreActivities((Set<Activity>)ImmutableSet.of((Object)Activity.CORE));
        brain.setDefaultActivity(Activity.IDLE);
        brain.resetPossibleActivities();
        return brain;
    }

    private static void addCoreActivities(Brain<FrogEntity> brain) {
        brain.setTaskList(Activity.CORE, 0, (ImmutableList<Task<FrogEntity>>)ImmutableList.of(new FleeTask(2.0f), (Object)new UpdateLookControlTask(45, 90), (Object)new MoveToTargetTask(), (Object)new TickCooldownTask(MemoryModuleType.TEMPTATION_COOLDOWN_TICKS), (Object)new TickCooldownTask(MemoryModuleType.LONG_JUMP_COOLING_DOWN)));
    }

    private static void addIdleActivities(Brain<FrogEntity> brain) {
        brain.setTaskList(Activity.IDLE, (ImmutableList<Pair<Integer, Task<FrogEntity>>>)ImmutableList.of((Object)Pair.of((Object)0, LookAtMobWithIntervalTask.follow(EntityType.PLAYER, 6.0f, UniformIntProvider.create(30, 60))), (Object)Pair.of((Object)0, (Object)new BreedTask(EntityType.FROG)), (Object)Pair.of((Object)1, (Object)new TemptTask(frog -> Float.valueOf(1.25f))), (Object)Pair.of((Object)2, UpdateAttackTargetTask.create((world, frog) -> FrogBrain.isNotBreeding(frog), (world, frog) -> frog.getBrain().getOptionalRegisteredMemory(MemoryModuleType.NEAREST_ATTACKABLE))), (Object)Pair.of((Object)3, WalkTowardsLandTask.create(6, 1.0f)), (Object)Pair.of((Object)4, new RandomTask((Map<MemoryModuleType<?>, MemoryModuleState>)ImmutableMap.of(MemoryModuleType.WALK_TARGET, (Object)((Object)MemoryModuleState.VALUE_ABSENT)), ImmutableList.of((Object)Pair.of(StrollTask.create(1.0f), (Object)1), (Object)Pair.of(GoToLookTargetTask.create(1.0f, 3), (Object)1), (Object)Pair.of((Object)new CroakTask(), (Object)3), (Object)Pair.of(TaskTriggerer.predicate(Entity::isOnGround), (Object)2))))), (Set<Pair<MemoryModuleType<?>, MemoryModuleState>>)ImmutableSet.of((Object)Pair.of(MemoryModuleType.LONG_JUMP_MID_JUMP, (Object)((Object)MemoryModuleState.VALUE_ABSENT)), (Object)Pair.of(MemoryModuleType.IS_IN_WATER, (Object)((Object)MemoryModuleState.VALUE_ABSENT))));
    }

    private static void addSwimActivities(Brain<FrogEntity> brain) {
        brain.setTaskList(Activity.SWIM, (ImmutableList<Pair<Integer, Task<FrogEntity>>>)ImmutableList.of((Object)Pair.of((Object)0, LookAtMobWithIntervalTask.follow(EntityType.PLAYER, 6.0f, UniformIntProvider.create(30, 60))), (Object)Pair.of((Object)1, (Object)new TemptTask(frog -> Float.valueOf(1.25f))), (Object)Pair.of((Object)2, UpdateAttackTargetTask.create((world, frog) -> FrogBrain.isNotBreeding(frog), (world, frog) -> frog.getBrain().getOptionalRegisteredMemory(MemoryModuleType.NEAREST_ATTACKABLE))), (Object)Pair.of((Object)3, WalkTowardsLandTask.create(8, 1.5f)), (Object)Pair.of((Object)5, new CompositeTask((Map<MemoryModuleType<?>, MemoryModuleState>)ImmutableMap.of(MemoryModuleType.WALK_TARGET, (Object)((Object)MemoryModuleState.VALUE_ABSENT)), (Set<MemoryModuleType<?>>)ImmutableSet.of(), CompositeTask.Order.ORDERED, CompositeTask.RunMode.TRY_ALL, ImmutableList.of((Object)Pair.of(StrollTask.createDynamicRadius(0.75f), (Object)1), (Object)Pair.of(StrollTask.create(1.0f, true), (Object)1), (Object)Pair.of(GoToLookTargetTask.create(1.0f, 3), (Object)1), (Object)Pair.of(TaskTriggerer.predicate(Entity::isTouchingWater), (Object)5))))), (Set<Pair<MemoryModuleType<?>, MemoryModuleState>>)ImmutableSet.of((Object)Pair.of(MemoryModuleType.LONG_JUMP_MID_JUMP, (Object)((Object)MemoryModuleState.VALUE_ABSENT)), (Object)Pair.of(MemoryModuleType.IS_IN_WATER, (Object)((Object)MemoryModuleState.VALUE_PRESENT))));
    }

    private static void addLaySpawnActivities(Brain<FrogEntity> brain) {
        brain.setTaskList(Activity.LAY_SPAWN, (ImmutableList<Pair<Integer, Task<FrogEntity>>>)ImmutableList.of((Object)Pair.of((Object)0, LookAtMobWithIntervalTask.follow(EntityType.PLAYER, 6.0f, UniformIntProvider.create(30, 60))), (Object)Pair.of((Object)1, UpdateAttackTargetTask.create((world, frog) -> FrogBrain.isNotBreeding(frog), (world, frog) -> frog.getBrain().getOptionalRegisteredMemory(MemoryModuleType.NEAREST_ATTACKABLE))), (Object)Pair.of((Object)2, WalkTowardsWaterTask.create(8, 1.0f)), (Object)Pair.of((Object)3, LayFrogSpawnTask.create(Blocks.FROGSPAWN)), (Object)Pair.of((Object)4, new RandomTask(ImmutableList.of((Object)Pair.of(StrollTask.create(1.0f), (Object)2), (Object)Pair.of(GoToLookTargetTask.create(1.0f, 3), (Object)1), (Object)Pair.of((Object)new CroakTask(), (Object)2), (Object)Pair.of(TaskTriggerer.predicate(Entity::isOnGround), (Object)1))))), (Set<Pair<MemoryModuleType<?>, MemoryModuleState>>)ImmutableSet.of((Object)Pair.of(MemoryModuleType.LONG_JUMP_MID_JUMP, (Object)((Object)MemoryModuleState.VALUE_ABSENT)), (Object)Pair.of(MemoryModuleType.IS_PREGNANT, (Object)((Object)MemoryModuleState.VALUE_PRESENT))));
    }

    private static void addLongJumpActivities(Brain<FrogEntity> brain) {
        brain.setTaskList(Activity.LONG_JUMP, (ImmutableList<Pair<Integer, Task<FrogEntity>>>)ImmutableList.of((Object)Pair.of((Object)0, (Object)new LeapingChargeTask(LONG_JUMP_COOLDOWN_RANGE, SoundEvents.ENTITY_FROG_STEP)), (Object)Pair.of((Object)1, new BiasedLongJumpTask<FrogEntity>(LONG_JUMP_COOLDOWN_RANGE, 2, 4, 3.5714288f, frog -> SoundEvents.ENTITY_FROG_LONG_JUMP, BlockTags.FROG_PREFER_JUMP_TO, 0.5f, FrogBrain::shouldJumpTo))), (Set<Pair<MemoryModuleType<?>, MemoryModuleState>>)ImmutableSet.of((Object)Pair.of(MemoryModuleType.TEMPTING_PLAYER, (Object)((Object)MemoryModuleState.VALUE_ABSENT)), (Object)Pair.of(MemoryModuleType.BREED_TARGET, (Object)((Object)MemoryModuleState.VALUE_ABSENT)), (Object)Pair.of(MemoryModuleType.LONG_JUMP_COOLING_DOWN, (Object)((Object)MemoryModuleState.VALUE_ABSENT)), (Object)Pair.of(MemoryModuleType.IS_IN_WATER, (Object)((Object)MemoryModuleState.VALUE_ABSENT))));
    }

    private static void addTongueActivities(Brain<FrogEntity> brain) {
        brain.setTaskList(Activity.TONGUE, 0, (ImmutableList<Task<FrogEntity>>)ImmutableList.of(ForgetAttackTargetTask.create(), (Object)new FrogEatEntityTask(SoundEvents.ENTITY_FROG_TONGUE, SoundEvents.ENTITY_FROG_EAT)), MemoryModuleType.ATTACK_TARGET);
    }

    private static <E extends MobEntity> boolean shouldJumpTo(E frog, BlockPos pos) {
        World world = frog.getEntityWorld();
        BlockPos blockPos = pos.down();
        if (!(world.getFluidState(pos).isEmpty() && world.getFluidState(blockPos).isEmpty() && world.getFluidState(pos.up()).isEmpty())) {
            return false;
        }
        BlockState blockState = world.getBlockState(pos);
        BlockState blockState2 = world.getBlockState(blockPos);
        if (blockState.isIn(BlockTags.FROG_PREFER_JUMP_TO) || blockState2.isIn(BlockTags.FROG_PREFER_JUMP_TO)) {
            return true;
        }
        PathContext pathContext = new PathContext(frog.getEntityWorld(), frog);
        PathNodeType pathNodeType = LandPathNodeMaker.getLandNodeType(pathContext, pos.mutableCopy());
        PathNodeType pathNodeType2 = LandPathNodeMaker.getLandNodeType(pathContext, blockPos.mutableCopy());
        if (pathNodeType == PathNodeType.TRAPDOOR || blockState.isAir() && pathNodeType2 == PathNodeType.TRAPDOOR) {
            return true;
        }
        return LongJumpTask.shouldJumpTo(frog, pos);
    }

    private static boolean isNotBreeding(FrogEntity frog) {
        return !TargetUtil.hasBreedTarget(frog);
    }

    public static void updateActivities(FrogEntity frog) {
        frog.getBrain().resetPossibleActivities((List<Activity>)ImmutableList.of((Object)Activity.TONGUE, (Object)Activity.LAY_SPAWN, (Object)Activity.LONG_JUMP, (Object)Activity.SWIM, (Object)Activity.IDLE));
    }

    public static Predicate<ItemStack> getTemptItemPredicate() {
        return stack -> stack.isIn(ItemTags.FROG_FOOD);
    }
}


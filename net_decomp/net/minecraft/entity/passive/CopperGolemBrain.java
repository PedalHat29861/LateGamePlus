/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  com.google.common.collect.ImmutableList
 *  com.google.common.collect.ImmutableMap
 *  com.mojang.datafixers.util.Pair
 *  org.jspecify.annotations.Nullable
 */
package net.minecraft.entity.passive;

import com.google.common.collect.ImmutableList;
import com.google.common.collect.ImmutableMap;
import com.mojang.datafixers.util.Pair;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.function.Consumer;
import java.util.function.Predicate;
import net.minecraft.block.BlockState;
import net.minecraft.block.Blocks;
import net.minecraft.block.entity.BlockEntity;
import net.minecraft.block.entity.ChestBlockEntity;
import net.minecraft.entity.EntityType;
import net.minecraft.entity.ai.brain.Activity;
import net.minecraft.entity.ai.brain.Brain;
import net.minecraft.entity.ai.brain.MemoryModuleState;
import net.minecraft.entity.ai.brain.MemoryModuleType;
import net.minecraft.entity.ai.brain.sensor.Sensor;
import net.minecraft.entity.ai.brain.sensor.SensorType;
import net.minecraft.entity.ai.brain.task.FleeTask;
import net.minecraft.entity.ai.brain.task.LookAtMobWithIntervalTask;
import net.minecraft.entity.ai.brain.task.MoveItemsTask;
import net.minecraft.entity.ai.brain.task.MoveToTargetTask;
import net.minecraft.entity.ai.brain.task.OpenDoorsTask;
import net.minecraft.entity.ai.brain.task.RandomTask;
import net.minecraft.entity.ai.brain.task.StrollTask;
import net.minecraft.entity.ai.brain.task.Task;
import net.minecraft.entity.ai.brain.task.TickCooldownTask;
import net.minecraft.entity.ai.brain.task.UpdateLookControlTask;
import net.minecraft.entity.ai.brain.task.WaitTask;
import net.minecraft.entity.mob.PathAwareEntity;
import net.minecraft.entity.passive.CopperGolemEntity;
import net.minecraft.entity.passive.CopperGolemState;
import net.minecraft.inventory.Inventory;
import net.minecraft.registry.tag.BlockTags;
import net.minecraft.sound.SoundEvent;
import net.minecraft.sound.SoundEvents;
import net.minecraft.util.math.intprovider.UniformIntProvider;
import org.jspecify.annotations.Nullable;

public class CopperGolemBrain {
    private static final float FLEEING_SPEED = 1.5f;
    private static final float WALKING_SPEED = 1.0f;
    private static final int HORIZONTAL_RANGE = 32;
    private static final int VERTICAL_RANGE = 8;
    private static final int OPEN_INTERACTION_TICKS = 1;
    private static final int PLAY_SOUND_INTERACTION_TICKS = 9;
    private static final Predicate<BlockState> INPUT_CHEST_PREDICATE = state -> state.isIn(BlockTags.COPPER_CHESTS);
    private static final Predicate<BlockState> OUTPUT_CHEST_PREDICATE = state -> state.isOf(Blocks.CHEST) || state.isOf(Blocks.TRAPPED_CHEST);
    private static final ImmutableList<SensorType<? extends Sensor<? super CopperGolemEntity>>> SENSORS = ImmutableList.of(SensorType.NEAREST_LIVING_ENTITIES, SensorType.HURT_BY);
    private static final ImmutableList<MemoryModuleType<?>> MEMORY_MODULES = ImmutableList.of(MemoryModuleType.IS_PANICKING, MemoryModuleType.HURT_BY, MemoryModuleType.HURT_BY_ENTITY, MemoryModuleType.MOBS, MemoryModuleType.VISIBLE_MOBS, MemoryModuleType.WALK_TARGET, MemoryModuleType.LOOK_TARGET, MemoryModuleType.CANT_REACH_WALK_TARGET_SINCE, MemoryModuleType.PATH, MemoryModuleType.GAZE_COOLDOWN_TICKS, MemoryModuleType.TRANSPORT_ITEMS_COOLDOWN_TICKS, MemoryModuleType.VISITED_BLOCK_POSITIONS, (Object[])new MemoryModuleType[]{MemoryModuleType.UNREACHABLE_TRANSPORT_BLOCK_POSITIONS, MemoryModuleType.DOORS_TO_CLOSE});

    public static Brain.Profile<CopperGolemEntity> createBrainProfile() {
        return Brain.createProfile(MEMORY_MODULES, SENSORS);
    }

    protected static Brain<?> create(Brain<CopperGolemEntity> brain) {
        CopperGolemBrain.addCoreActivities(brain);
        CopperGolemBrain.addIdleActivities(brain);
        brain.setCoreActivities(Set.of(Activity.CORE));
        brain.setDefaultActivity(Activity.IDLE);
        brain.resetPossibleActivities();
        return brain;
    }

    public static void updateActivity(CopperGolemEntity entity) {
        entity.getBrain().resetPossibleActivities((List<Activity>)ImmutableList.of((Object)Activity.IDLE));
    }

    private static void addCoreActivities(Brain<CopperGolemEntity> brain) {
        brain.setTaskList(Activity.CORE, 0, (ImmutableList<Task<CopperGolemEntity>>)ImmutableList.of(new FleeTask(1.5f), (Object)new UpdateLookControlTask(45, 90), (Object)new MoveToTargetTask(), OpenDoorsTask.create(), (Object)new TickCooldownTask(MemoryModuleType.GAZE_COOLDOWN_TICKS), (Object)new TickCooldownTask(MemoryModuleType.TRANSPORT_ITEMS_COOLDOWN_TICKS)));
    }

    private static void addIdleActivities(Brain<CopperGolemEntity> brain) {
        brain.setTaskList(Activity.IDLE, (ImmutableList<Pair<Integer, Task<CopperGolemEntity>>>)ImmutableList.of((Object)Pair.of((Object)0, (Object)new MoveItemsTask(1.0f, INPUT_CHEST_PREDICATE, OUTPUT_CHEST_PREDICATE, 32, 8, CopperGolemBrain.createInteractionCallbacks(), CopperGolemBrain.createResetToIdleCallback(), CopperGolemBrain.createStoragePredicate())), (Object)Pair.of((Object)1, LookAtMobWithIntervalTask.follow(EntityType.PLAYER, 6.0f, UniformIntProvider.create(40, 80))), (Object)Pair.of((Object)2, new RandomTask((Map<MemoryModuleType<?>, MemoryModuleState>)ImmutableMap.of(MemoryModuleType.WALK_TARGET, (Object)((Object)MemoryModuleState.VALUE_ABSENT), MemoryModuleType.TRANSPORT_ITEMS_COOLDOWN_TICKS, (Object)((Object)MemoryModuleState.VALUE_PRESENT)), ImmutableList.of((Object)Pair.of(StrollTask.create(1.0f, 2, 2), (Object)1), (Object)Pair.of((Object)new WaitTask(30, 60), (Object)1))))));
    }

    private static Map<MoveItemsTask.InteractionState, MoveItemsTask.InteractionCallback> createInteractionCallbacks() {
        return Map.of(MoveItemsTask.InteractionState.PICKUP_ITEM, CopperGolemBrain.createInteractionCallback(CopperGolemState.GETTING_ITEM, SoundEvents.ENTITY_COPPER_GOLEM_NO_ITEM_GET), MoveItemsTask.InteractionState.PICKUP_NO_ITEM, CopperGolemBrain.createInteractionCallback(CopperGolemState.GETTING_NO_ITEM, SoundEvents.ENTITY_COPPER_GOLEM_NO_ITEM_NO_GET), MoveItemsTask.InteractionState.PLACE_ITEM, CopperGolemBrain.createInteractionCallback(CopperGolemState.DROPPING_ITEM, SoundEvents.ENTITY_COPPER_GOLEM_ITEM_DROP), MoveItemsTask.InteractionState.PLACE_NO_ITEM, CopperGolemBrain.createInteractionCallback(CopperGolemState.DROPPING_NO_ITEM, SoundEvents.ENTITY_COPPER_GOLEM_ITEM_NO_DROP));
    }

    private static MoveItemsTask.InteractionCallback createInteractionCallback(CopperGolemState state, @Nullable SoundEvent soundEvent) {
        return (pathAwareEntity, storage, interactionTicks) -> {
            if (pathAwareEntity instanceof CopperGolemEntity) {
                CopperGolemEntity copperGolemEntity = (CopperGolemEntity)pathAwareEntity;
                Inventory inventory = storage.inventory();
                if (interactionTicks == 1) {
                    inventory.onOpen(copperGolemEntity);
                    copperGolemEntity.setTargetContainerPos(storage.pos());
                    copperGolemEntity.setState(state);
                }
                if (interactionTicks == 9 && soundEvent != null) {
                    copperGolemEntity.playSoundIfNotSilent(soundEvent);
                }
                if (interactionTicks == 60) {
                    if (inventory.getViewingUsers().contains(pathAwareEntity)) {
                        inventory.onClose(copperGolemEntity);
                    }
                    copperGolemEntity.resetTargetContainerPos();
                }
            }
        };
    }

    private static Consumer<PathAwareEntity> createResetToIdleCallback() {
        return entity -> {
            if (entity instanceof CopperGolemEntity) {
                CopperGolemEntity copperGolemEntity = (CopperGolemEntity)entity;
                copperGolemEntity.resetTargetContainerPos();
                copperGolemEntity.setState(CopperGolemState.IDLE);
            }
        };
    }

    private static Predicate<MoveItemsTask.Storage> createStoragePredicate() {
        return storage -> {
            BlockEntity blockEntity = storage.blockEntity();
            if (blockEntity instanceof ChestBlockEntity) {
                ChestBlockEntity chestBlockEntity = (ChestBlockEntity)blockEntity;
                return !chestBlockEntity.getViewingUsers().isEmpty();
            }
            return false;
        };
    }
}


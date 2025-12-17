/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  com.google.common.collect.ImmutableMap
 *  com.google.common.collect.ImmutableMap$Builder
 */
package net.minecraft.entity.ai.brain.task;

import com.google.common.collect.ImmutableMap;
import java.util.Map;
import java.util.Optional;
import java.util.function.Function;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.ai.brain.Brain;
import net.minecraft.entity.ai.brain.EntityLookTarget;
import net.minecraft.entity.ai.brain.MemoryModuleState;
import net.minecraft.entity.ai.brain.MemoryModuleType;
import net.minecraft.entity.ai.brain.WalkTarget;
import net.minecraft.entity.ai.brain.task.MultiTickTask;
import net.minecraft.entity.mob.PathAwareEntity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.util.Util;
import net.minecraft.util.math.MathHelper;

public class TemptTask
extends MultiTickTask<PathAwareEntity> {
    public static final int TEMPTATION_COOLDOWN_TICKS = 100;
    public static final double DEFAULT_STOP_DISTANCE = 2.5;
    public static final double LARGE_ENTITY_STOP_DISTANCE = 3.5;
    private final Function<LivingEntity, Float> speed;
    private final Function<LivingEntity, Double> stopDistanceGetter;
    private final boolean useEyeHeight;

    public TemptTask(Function<LivingEntity, Float> speed) {
        this(speed, entity -> 2.5);
    }

    public TemptTask(Function<LivingEntity, Float> speed, Function<LivingEntity, Double> stopDistanceGetter) {
        this(speed, stopDistanceGetter, false);
    }

    public TemptTask(Function<LivingEntity, Float> speed, Function<LivingEntity, Double> stopDistanceGetter, boolean useEyeHeight) {
        super((Map)Util.make(() -> {
            ImmutableMap.Builder builder = ImmutableMap.builder();
            builder.put(MemoryModuleType.LOOK_TARGET, (Object)MemoryModuleState.REGISTERED);
            builder.put(MemoryModuleType.WALK_TARGET, (Object)MemoryModuleState.REGISTERED);
            builder.put(MemoryModuleType.TEMPTATION_COOLDOWN_TICKS, (Object)MemoryModuleState.VALUE_ABSENT);
            builder.put(MemoryModuleType.IS_TEMPTED, (Object)MemoryModuleState.VALUE_ABSENT);
            builder.put(MemoryModuleType.TEMPTING_PLAYER, (Object)MemoryModuleState.VALUE_PRESENT);
            builder.put(MemoryModuleType.BREED_TARGET, (Object)MemoryModuleState.VALUE_ABSENT);
            builder.put(MemoryModuleType.IS_PANICKING, (Object)MemoryModuleState.VALUE_ABSENT);
            return builder.build();
        }));
        this.speed = speed;
        this.stopDistanceGetter = stopDistanceGetter;
        this.useEyeHeight = useEyeHeight;
    }

    protected float getSpeed(PathAwareEntity entity) {
        return this.speed.apply(entity).floatValue();
    }

    private Optional<PlayerEntity> getTemptingPlayer(PathAwareEntity entity) {
        return entity.getBrain().getOptionalRegisteredMemory(MemoryModuleType.TEMPTING_PLAYER);
    }

    @Override
    protected boolean isTimeLimitExceeded(long time) {
        return false;
    }

    @Override
    protected boolean shouldKeepRunning(ServerWorld serverWorld, PathAwareEntity pathAwareEntity, long l) {
        return this.getTemptingPlayer(pathAwareEntity).isPresent() && !pathAwareEntity.getBrain().hasMemoryModule(MemoryModuleType.BREED_TARGET) && !pathAwareEntity.getBrain().hasMemoryModule(MemoryModuleType.IS_PANICKING);
    }

    @Override
    protected void run(ServerWorld serverWorld, PathAwareEntity pathAwareEntity, long l) {
        pathAwareEntity.getBrain().remember(MemoryModuleType.IS_TEMPTED, true);
    }

    @Override
    protected void finishRunning(ServerWorld serverWorld, PathAwareEntity pathAwareEntity, long l) {
        Brain<?> brain = pathAwareEntity.getBrain();
        brain.remember(MemoryModuleType.TEMPTATION_COOLDOWN_TICKS, 100);
        brain.forget(MemoryModuleType.IS_TEMPTED);
        brain.forget(MemoryModuleType.WALK_TARGET);
        brain.forget(MemoryModuleType.LOOK_TARGET);
    }

    @Override
    protected void keepRunning(ServerWorld serverWorld, PathAwareEntity pathAwareEntity, long l) {
        PlayerEntity playerEntity = this.getTemptingPlayer(pathAwareEntity).get();
        Brain<?> brain = pathAwareEntity.getBrain();
        brain.remember(MemoryModuleType.LOOK_TARGET, new EntityLookTarget(playerEntity, true));
        double d = this.stopDistanceGetter.apply(pathAwareEntity);
        if (pathAwareEntity.squaredDistanceTo(playerEntity) < MathHelper.square(d)) {
            brain.forget(MemoryModuleType.WALK_TARGET);
        } else {
            brain.remember(MemoryModuleType.WALK_TARGET, new WalkTarget(new EntityLookTarget(playerEntity, this.useEyeHeight, this.useEyeHeight), this.getSpeed(pathAwareEntity), 2));
        }
    }

    @Override
    protected /* synthetic */ void finishRunning(ServerWorld world, LivingEntity entity, long time) {
        this.finishRunning(world, (PathAwareEntity)entity, time);
    }

    @Override
    protected /* synthetic */ void keepRunning(ServerWorld world, LivingEntity entity, long time) {
        this.keepRunning(world, (PathAwareEntity)entity, time);
    }

    @Override
    protected /* synthetic */ void run(ServerWorld world, LivingEntity entity, long time) {
        this.run(world, (PathAwareEntity)entity, time);
    }
}


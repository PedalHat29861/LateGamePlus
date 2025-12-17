/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  com.google.common.collect.ImmutableMap
 */
package net.minecraft.entity.ai.brain.task;

import com.google.common.collect.ImmutableMap;
import java.util.Map;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.ai.brain.Brain;
import net.minecraft.entity.ai.brain.MemoryModuleState;
import net.minecraft.entity.ai.brain.MemoryModuleType;
import net.minecraft.entity.ai.brain.task.MultiTickTask;
import net.minecraft.entity.effect.StatusEffectInstance;
import net.minecraft.entity.effect.StatusEffects;
import net.minecraft.entity.passive.AxolotlEntity;
import net.minecraft.server.world.ServerWorld;

public class PlayDeadTask
extends MultiTickTask<AxolotlEntity> {
    public PlayDeadTask() {
        super((Map<MemoryModuleType<?>, MemoryModuleState>)ImmutableMap.of(MemoryModuleType.PLAY_DEAD_TICKS, (Object)((Object)MemoryModuleState.VALUE_PRESENT), MemoryModuleType.HURT_BY_ENTITY, (Object)((Object)MemoryModuleState.VALUE_PRESENT)), 200);
    }

    @Override
    protected boolean shouldRun(ServerWorld serverWorld, AxolotlEntity axolotlEntity) {
        return axolotlEntity.isTouchingWater();
    }

    @Override
    protected boolean shouldKeepRunning(ServerWorld serverWorld, AxolotlEntity axolotlEntity, long l) {
        return axolotlEntity.isTouchingWater() && axolotlEntity.getBrain().hasMemoryModule(MemoryModuleType.PLAY_DEAD_TICKS);
    }

    @Override
    protected void run(ServerWorld serverWorld, AxolotlEntity axolotlEntity, long l) {
        Brain<AxolotlEntity> brain = axolotlEntity.getBrain();
        brain.forget(MemoryModuleType.WALK_TARGET);
        brain.forget(MemoryModuleType.LOOK_TARGET);
        axolotlEntity.addStatusEffect(new StatusEffectInstance(StatusEffects.REGENERATION, 200, 0));
    }

    @Override
    protected /* synthetic */ void run(ServerWorld world, LivingEntity entity, long time) {
        this.run(world, (AxolotlEntity)entity, time);
    }
}


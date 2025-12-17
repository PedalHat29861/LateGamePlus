/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  com.mojang.datafixers.kinds.Applicative
 */
package net.minecraft.entity.ai.brain.task;

import com.mojang.datafixers.kinds.Applicative;
import java.util.Optional;
import java.util.UUID;
import net.minecraft.entity.EntityType;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.ai.brain.MemoryModuleType;
import net.minecraft.entity.ai.brain.task.Task;
import net.minecraft.entity.ai.brain.task.TaskTriggerer;
import net.minecraft.world.rule.GameRules;

public class ForgetAngryAtTargetTask {
    public static Task<LivingEntity> create() {
        return TaskTriggerer.task(context -> context.group(context.queryMemoryValue(MemoryModuleType.ANGRY_AT)).apply((Applicative)context, angryAt -> (world, entity, time) -> {
            Optional.ofNullable(world.getEntity((UUID)context.getValue(angryAt))).map(target -> {
                LivingEntity livingEntity;
                return target instanceof LivingEntity ? (livingEntity = (LivingEntity)target) : null;
            }).filter(LivingEntity::isDead).filter(target -> target.getType() != EntityType.PLAYER || world.getGameRules().getValue(GameRules.FORGIVE_DEAD_PLAYERS) != false).ifPresent(target -> angryAt.forget());
            return true;
        }));
    }
}


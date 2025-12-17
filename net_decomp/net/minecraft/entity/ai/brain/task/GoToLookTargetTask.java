/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  com.mojang.datafixers.kinds.Applicative
 */
package net.minecraft.entity.ai.brain.task;

import com.mojang.datafixers.kinds.Applicative;
import java.util.function.Function;
import java.util.function.Predicate;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.ai.brain.LookTarget;
import net.minecraft.entity.ai.brain.MemoryModuleType;
import net.minecraft.entity.ai.brain.WalkTarget;
import net.minecraft.entity.ai.brain.task.SingleTickTask;
import net.minecraft.entity.ai.brain.task.TaskTriggerer;

public class GoToLookTargetTask {
    public static SingleTickTask<LivingEntity> create(float speed, int completionRange) {
        return GoToLookTargetTask.create(entity -> true, entity -> Float.valueOf(speed), completionRange);
    }

    public static SingleTickTask<LivingEntity> create(Predicate<LivingEntity> predicate, Function<LivingEntity, Float> speed, int completionRange) {
        return TaskTriggerer.task(context -> context.group(context.queryMemoryAbsent(MemoryModuleType.WALK_TARGET), context.queryMemoryValue(MemoryModuleType.LOOK_TARGET)).apply((Applicative)context, (walkTarget, lookTarget) -> (world, entity, time) -> {
            if (!predicate.test(entity)) {
                return false;
            }
            walkTarget.remember(new WalkTarget((LookTarget)context.getValue(lookTarget), ((Float)speed.apply(entity)).floatValue(), completionRange));
            return true;
        }));
    }
}


/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  com.mojang.datafixers.kinds.Applicative
 */
package net.minecraft.entity.ai.brain.task;

import com.mojang.datafixers.kinds.Applicative;
import java.util.Optional;
import java.util.function.Function;
import java.util.function.Predicate;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.ai.brain.LookTarget;
import net.minecraft.entity.ai.brain.MemoryModuleType;
import net.minecraft.entity.ai.brain.WalkTarget;
import net.minecraft.entity.ai.brain.task.Task;
import net.minecraft.entity.ai.brain.task.TaskTriggerer;

public class WalkTowardsLookTargetTask {
    public static Task<LivingEntity> create(Function<LivingEntity, Optional<LookTarget>> lookTargetFunction, Predicate<LivingEntity> predicate, int completionRange, int searchRange, float speed) {
        return TaskTriggerer.task(context -> context.group(context.queryMemoryOptional(MemoryModuleType.LOOK_TARGET), context.queryMemoryOptional(MemoryModuleType.WALK_TARGET)).apply((Applicative)context, (lookTarget, walkTarget) -> (world, entity, time) -> {
            Optional optional = (Optional)lookTargetFunction.apply(entity);
            if (optional.isEmpty() || !predicate.test(entity)) {
                return false;
            }
            LookTarget lookTarget = (LookTarget)optional.get();
            if (entity.getEntityPos().isInRange(lookTarget.getPos(), searchRange)) {
                return false;
            }
            LookTarget lookTarget2 = (LookTarget)optional.get();
            lookTarget.remember(lookTarget2);
            walkTarget.remember(new WalkTarget(lookTarget2, speed, completionRange));
            return true;
        }));
    }
}


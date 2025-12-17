/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  com.mojang.datafixers.kinds.Applicative
 */
package net.minecraft.entity.ai.brain.task;

import com.mojang.datafixers.kinds.Applicative;
import java.util.function.Predicate;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.ai.brain.MemoryModuleType;
import net.minecraft.entity.ai.brain.task.Task;
import net.minecraft.entity.ai.brain.task.TaskTriggerer;
import net.minecraft.util.math.intprovider.UniformIntProvider;

public class MemoryTransferTask {
    public static <E extends LivingEntity, T> Task<E> create(Predicate<E> runPredicate, MemoryModuleType<? extends T> sourceType, MemoryModuleType<T> targetType, UniformIntProvider expiry) {
        return TaskTriggerer.task(context -> context.group(context.queryMemoryValue(sourceType), context.queryMemoryAbsent(targetType)).apply((Applicative)context, (source, target) -> (world, entity, time) -> {
            if (!runPredicate.test(entity)) {
                return false;
            }
            target.remember(context.getValue(source), expiry.get(world.random));
            return true;
        }));
    }
}


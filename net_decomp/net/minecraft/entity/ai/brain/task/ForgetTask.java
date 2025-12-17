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

public class ForgetTask {
    public static <E extends LivingEntity> Task<E> create(Predicate<E> condition, MemoryModuleType<?> memory) {
        return TaskTriggerer.task(context -> context.group(context.queryMemoryValue(memory)).apply((Applicative)context, queryResult -> (world, entity, time) -> {
            if (condition.test(entity)) {
                queryResult.forget();
                return true;
            }
            return false;
        }));
    }
}


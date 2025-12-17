/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  com.mojang.datafixers.util.Pair
 */
package net.minecraft.entity.ai.brain.task;

import com.mojang.datafixers.util.Pair;
import java.util.Iterator;
import java.util.List;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.ai.brain.task.CompositeTask;
import net.minecraft.entity.ai.brain.task.SingleTickTask;
import net.minecraft.entity.ai.brain.task.TaskRunnable;
import net.minecraft.entity.ai.brain.task.TaskTriggerer;
import net.minecraft.util.collection.WeightedList;

public class Tasks {
    public static <E extends LivingEntity> SingleTickTask<E> pickRandomly(List<Pair<? extends TaskRunnable<? super E>, Integer>> weightedTasks) {
        return Tasks.weighted(weightedTasks, CompositeTask.Order.SHUFFLED, CompositeTask.RunMode.RUN_ONE);
    }

    public static <E extends LivingEntity> SingleTickTask<E> weighted(List<Pair<? extends TaskRunnable<? super E>, Integer>> weightedTasks, CompositeTask.Order order, CompositeTask.RunMode runMode) {
        WeightedList weightedList = new WeightedList();
        weightedTasks.forEach(task -> weightedList.add((TaskRunnable)task.getFirst(), (Integer)task.getSecond()));
        return TaskTriggerer.task(context -> context.point((world, entity, time) -> {
            TaskRunnable taskRunnable;
            if (order == CompositeTask.Order.SHUFFLED) {
                weightedList.shuffle();
            }
            Iterator iterator = weightedList.iterator();
            while (iterator.hasNext() && (!(taskRunnable = (TaskRunnable)iterator.next()).trigger(world, entity, time) || runMode != CompositeTask.RunMode.RUN_ONE)) {
            }
            return true;
        }));
    }
}


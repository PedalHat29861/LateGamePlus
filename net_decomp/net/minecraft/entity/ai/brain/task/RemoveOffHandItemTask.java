/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  com.mojang.datafixers.kinds.Applicative
 */
package net.minecraft.entity.ai.brain.task;

import com.mojang.datafixers.kinds.Applicative;
import net.minecraft.component.DataComponentTypes;
import net.minecraft.entity.ai.brain.MemoryModuleType;
import net.minecraft.entity.ai.brain.task.Task;
import net.minecraft.entity.ai.brain.task.TaskTriggerer;
import net.minecraft.entity.mob.PiglinBrain;
import net.minecraft.entity.mob.PiglinEntity;

public class RemoveOffHandItemTask {
    public static Task<PiglinEntity> create() {
        return TaskTriggerer.task(context -> context.group(context.queryMemoryAbsent(MemoryModuleType.ADMIRING_ITEM)).apply((Applicative)context, admiringItem -> (world, entity, time) -> {
            if (entity.getOffHandStack().isEmpty() || entity.getOffHandStack().contains(DataComponentTypes.BLOCKS_ATTACKS)) {
                return false;
            }
            PiglinBrain.consumeOffHandItem(world, entity, true);
            return true;
        }));
    }
}


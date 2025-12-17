/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  com.mojang.datafixers.kinds.Applicative
 *  org.apache.commons.lang3.mutable.MutableLong
 */
package net.minecraft.entity.ai.brain.task;

import com.mojang.datafixers.kinds.Applicative;
import net.minecraft.entity.ai.brain.MemoryModuleType;
import net.minecraft.entity.ai.brain.WalkTarget;
import net.minecraft.entity.ai.brain.task.Task;
import net.minecraft.entity.ai.brain.task.TaskTriggerer;
import net.minecraft.entity.mob.PathAwareEntity;
import net.minecraft.util.math.GlobalPos;
import org.apache.commons.lang3.mutable.MutableLong;

public class GoToPosTask {
    public static Task<PathAwareEntity> create(MemoryModuleType<GlobalPos> posModule, float walkSpeed, int completionRange, int maxDistance) {
        MutableLong mutableLong = new MutableLong(0L);
        return TaskTriggerer.task(context -> context.group(context.queryMemoryOptional(MemoryModuleType.WALK_TARGET), context.queryMemoryValue(posModule)).apply((Applicative)context, (walkTarget, pos) -> (world, entity, time) -> {
            GlobalPos globalPos = (GlobalPos)context.getValue(pos);
            if (world.getRegistryKey() != globalPos.dimension() || !globalPos.pos().isWithinDistance(entity.getEntityPos(), (double)maxDistance)) {
                return false;
            }
            if (time <= mutableLong.longValue()) {
                return true;
            }
            walkTarget.remember(new WalkTarget(globalPos.pos(), walkSpeed, completionRange));
            mutableLong.setValue(time + 80L);
            return true;
        }));
    }
}


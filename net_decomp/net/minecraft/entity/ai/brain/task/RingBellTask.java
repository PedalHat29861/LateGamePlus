/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  com.mojang.datafixers.kinds.Applicative
 */
package net.minecraft.entity.ai.brain.task;

import com.mojang.datafixers.kinds.Applicative;
import net.minecraft.block.BellBlock;
import net.minecraft.block.BlockState;
import net.minecraft.block.Blocks;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.ai.brain.MemoryModuleType;
import net.minecraft.entity.ai.brain.task.Task;
import net.minecraft.entity.ai.brain.task.TaskTriggerer;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.GlobalPos;

public class RingBellTask {
    private static final float RUN_CHANCE = 0.95f;
    public static final int MAX_DISTANCE = 3;

    public static Task<LivingEntity> create() {
        return TaskTriggerer.task(context -> context.group(context.queryMemoryValue(MemoryModuleType.MEETING_POINT)).apply((Applicative)context, meetingPoint -> (world, entity, time) -> {
            BlockState blockState;
            if (world.random.nextFloat() <= 0.95f) {
                return false;
            }
            BlockPos blockPos = ((GlobalPos)context.getValue(meetingPoint)).pos();
            if (blockPos.isWithinDistance(entity.getBlockPos(), 3.0) && (blockState = world.getBlockState(blockPos)).isOf(Blocks.BELL)) {
                BellBlock bellBlock = (BellBlock)blockState.getBlock();
                bellBlock.ring(entity, world, blockPos, null);
            }
            return true;
        }));
    }
}


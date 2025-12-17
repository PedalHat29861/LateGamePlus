/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  com.mojang.datafixers.kinds.Applicative
 */
package net.minecraft.entity.ai.brain.task;

import com.mojang.datafixers.kinds.Applicative;
import java.util.Optional;
import net.minecraft.entity.ai.FuzzyTargeting;
import net.minecraft.entity.ai.NoPenaltyTargeting;
import net.minecraft.entity.ai.brain.MemoryModuleType;
import net.minecraft.entity.ai.brain.WalkTarget;
import net.minecraft.entity.ai.brain.task.SingleTickTask;
import net.minecraft.entity.ai.brain.task.TargetUtil;
import net.minecraft.entity.ai.brain.task.TaskTriggerer;
import net.minecraft.entity.mob.PathAwareEntity;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.ChunkSectionPos;
import net.minecraft.util.math.Vec3d;

public class GoToPointOfInterestTask {
    private static final int DEFAULT_HORIZONTAL_RANGE = 10;
    private static final int DEFAULT_VERTICAL_RANGE = 7;

    public static SingleTickTask<PathAwareEntity> create(float walkSpeed) {
        return GoToPointOfInterestTask.create(walkSpeed, 10, 7);
    }

    public static SingleTickTask<PathAwareEntity> create(float walkSpeed, int horizontalRange, int verticalRange) {
        return TaskTriggerer.task(context -> context.group(context.queryMemoryAbsent(MemoryModuleType.WALK_TARGET)).apply((Applicative)context, walkTarget -> (world, entity, time) -> {
            ChunkSectionPos chunkSectionPos;
            ChunkSectionPos chunkSectionPos2;
            BlockPos blockPos = entity.getBlockPos();
            Vec3d vec3d = world.isNearOccupiedPointOfInterest(blockPos) ? FuzzyTargeting.find(entity, horizontalRange, verticalRange) : ((chunkSectionPos2 = TargetUtil.getPosClosestToOccupiedPointOfInterest(world, chunkSectionPos = ChunkSectionPos.from(blockPos), 2)) != chunkSectionPos ? NoPenaltyTargeting.findTo(entity, horizontalRange, verticalRange, Vec3d.ofBottomCenter(chunkSectionPos2.getCenterPos()), 1.5707963705062866) : FuzzyTargeting.find(entity, horizontalRange, verticalRange));
            walkTarget.remember(Optional.ofNullable(vec3d).map(pos -> new WalkTarget((Vec3d)pos, walkSpeed, 0)));
            return true;
        }));
    }
}


/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  org.jspecify.annotations.Nullable
 */
package net.minecraft.entity.ai.goal;

import net.minecraft.entity.ai.NoPenaltyTargeting;
import net.minecraft.entity.ai.brain.task.TargetUtil;
import net.minecraft.entity.ai.goal.WanderAroundGoal;
import net.minecraft.entity.mob.PathAwareEntity;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.ChunkSectionPos;
import net.minecraft.util.math.Vec3d;
import org.jspecify.annotations.Nullable;

public class WanderAroundPointOfInterestGoal
extends WanderAroundGoal {
    private static final int HORIZONTAL_RANGE = 10;
    private static final int VERTICAL_RANGE = 7;

    public WanderAroundPointOfInterestGoal(PathAwareEntity entity, double speed, boolean canDespawn) {
        super(entity, speed, 10, canDespawn);
    }

    @Override
    public boolean canStart() {
        BlockPos blockPos;
        ServerWorld serverWorld = (ServerWorld)this.mob.getEntityWorld();
        if (serverWorld.isNearOccupiedPointOfInterest(blockPos = this.mob.getBlockPos())) {
            return false;
        }
        return super.canStart();
    }

    @Override
    protected @Nullable Vec3d getWanderTarget() {
        BlockPos blockPos;
        ChunkSectionPos chunkSectionPos;
        ServerWorld serverWorld = (ServerWorld)this.mob.getEntityWorld();
        ChunkSectionPos chunkSectionPos2 = TargetUtil.getPosClosestToOccupiedPointOfInterest(serverWorld, chunkSectionPos = ChunkSectionPos.from(blockPos = this.mob.getBlockPos()), 2);
        if (chunkSectionPos2 != chunkSectionPos) {
            return NoPenaltyTargeting.findTo(this.mob, 10, 7, Vec3d.ofBottomCenter(chunkSectionPos2.getCenterPos()), 1.5707963705062866);
        }
        return null;
    }
}


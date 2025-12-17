/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  org.jspecify.annotations.Nullable
 */
package net.minecraft.entity.ai;

import net.minecraft.entity.ai.FuzzyPositions;
import net.minecraft.entity.ai.NavigationConditions;
import net.minecraft.entity.mob.PathAwareEntity;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Vec3d;
import org.jspecify.annotations.Nullable;

public class NoPenaltySolidTargeting {
    public static @Nullable Vec3d find(PathAwareEntity entity, int horizontalRange, int verticalRange, int startHeight, double directionX, double directionZ, double rangeAngle) {
        boolean bl = NavigationConditions.isPositionTargetInRange(entity, horizontalRange);
        return FuzzyPositions.guessBestPathTarget(entity, () -> NoPenaltySolidTargeting.tryMake(entity, horizontalRange, verticalRange, startHeight, directionX, directionZ, rangeAngle, bl));
    }

    public static @Nullable BlockPos tryMake(PathAwareEntity entity, int horizontalRange, int verticalRange, int startHeight, double directionX, double directionZ, double rangeAngle, boolean posTargetInRange) {
        BlockPos blockPos = FuzzyPositions.localFuzz(entity.getRandom(), 0.0, horizontalRange, verticalRange, startHeight, directionX, directionZ, rangeAngle);
        if (blockPos == null) {
            return null;
        }
        BlockPos blockPos2 = FuzzyPositions.towardTarget(entity, horizontalRange, entity.getRandom(), blockPos);
        if (NavigationConditions.isHeightInvalid(blockPos2, entity) || NavigationConditions.isPositionTargetOutOfWalkRange(posTargetInRange, entity, blockPos2)) {
            return null;
        }
        if (NavigationConditions.hasPathfindingPenalty(entity, blockPos2 = FuzzyPositions.upWhile(blockPos2, entity.getEntityWorld().getTopYInclusive(), pos -> NavigationConditions.isSolidAt(entity, pos)))) {
            return null;
        }
        return blockPos2;
    }
}


/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  org.jspecify.annotations.Nullable
 */
package net.minecraft.entity.ai;

import net.minecraft.entity.ai.FuzzyPositions;
import net.minecraft.entity.ai.FuzzyTargeting;
import net.minecraft.entity.ai.NavigationConditions;
import net.minecraft.entity.mob.PathAwareEntity;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Vec3d;
import org.jspecify.annotations.Nullable;

public class AboveGroundTargeting {
    public static @Nullable Vec3d find(PathAwareEntity entity, int horizontalRange, int verticalRange, double x, double z, float angle, int maxAboveSolid, int minAboveSolid) {
        boolean bl = NavigationConditions.isPositionTargetInRange(entity, horizontalRange);
        return FuzzyPositions.guessBestPathTarget(entity, () -> {
            BlockPos blockPos = FuzzyPositions.localFuzz(entity.getRandom(), 0.0, horizontalRange, verticalRange, 0, x, z, angle);
            if (blockPos == null) {
                return null;
            }
            BlockPos blockPos2 = FuzzyTargeting.towardTarget(entity, horizontalRange, bl, blockPos);
            if (blockPos2 == null) {
                return null;
            }
            if (NavigationConditions.isWaterAt(entity, blockPos2 = FuzzyPositions.upWhile(blockPos2, entity.getRandom().nextInt(maxAboveSolid - minAboveSolid + 1) + minAboveSolid, entity.getEntityWorld().getTopYInclusive(), pos -> NavigationConditions.isSolidAt(entity, pos))) || NavigationConditions.hasPathfindingPenalty(entity, blockPos2)) {
                return null;
            }
            return blockPos2;
        });
    }
}


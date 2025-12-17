/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  org.jspecify.annotations.Nullable
 */
package net.minecraft.entity.ai;

import java.util.function.ToDoubleFunction;
import net.minecraft.entity.ai.FuzzyPositions;
import net.minecraft.entity.ai.NavigationConditions;
import net.minecraft.entity.mob.PathAwareEntity;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Vec3d;
import org.jspecify.annotations.Nullable;

public class FuzzyTargeting {
    public static @Nullable Vec3d find(PathAwareEntity entity, int horizontalRange, int verticalRange) {
        return FuzzyTargeting.find(entity, horizontalRange, verticalRange, entity::getPathfindingFavor);
    }

    public static @Nullable Vec3d find(PathAwareEntity entity, int horizontalRange, int verticalRange, ToDoubleFunction<BlockPos> scorer) {
        boolean bl = NavigationConditions.isPositionTargetInRange(entity, horizontalRange);
        return FuzzyPositions.guessBest(() -> {
            BlockPos blockPos = FuzzyPositions.localFuzz(entity.getRandom(), horizontalRange, verticalRange);
            BlockPos blockPos2 = FuzzyTargeting.towardTarget(entity, horizontalRange, bl, blockPos);
            if (blockPos2 == null) {
                return null;
            }
            return FuzzyTargeting.validate(entity, blockPos2);
        }, scorer);
    }

    public static @Nullable Vec3d findTo(PathAwareEntity entity, int horizontalRange, int verticalRange, Vec3d end) {
        Vec3d vec3d = end.subtract(entity.getX(), entity.getY(), entity.getZ());
        boolean bl = NavigationConditions.isPositionTargetInRange(entity, horizontalRange);
        return FuzzyTargeting.findValid(entity, 0.0, horizontalRange, verticalRange, vec3d, bl);
    }

    public static @Nullable Vec3d findFrom(PathAwareEntity entity, int horizontalRange, int verticalRange, Vec3d start) {
        return FuzzyTargeting.findFrom(entity, 0.0, horizontalRange, verticalRange, start);
    }

    public static @Nullable Vec3d findFrom(PathAwareEntity entity, double minHorizontalRange, double maxHorizontalRange, int verticalRange, Vec3d start) {
        Vec3d vec3d = entity.getEntityPos().subtract(start);
        if (vec3d.length() == 0.0) {
            vec3d = new Vec3d(entity.getRandom().nextDouble() - 0.5, 0.0, entity.getRandom().nextDouble() - 0.5);
        }
        boolean bl = NavigationConditions.isPositionTargetInRange(entity, maxHorizontalRange);
        return FuzzyTargeting.findValid(entity, minHorizontalRange, maxHorizontalRange, verticalRange, vec3d, bl);
    }

    private static @Nullable Vec3d findValid(PathAwareEntity entity, double minHorizontalRange, double maxHorizontalRange, int verticalRange, Vec3d direction, boolean posTargetInRange) {
        return FuzzyPositions.guessBestPathTarget(entity, () -> {
            BlockPos blockPos = FuzzyPositions.localFuzz(entity.getRandom(), minHorizontalRange, maxHorizontalRange, verticalRange, 0, vec3d.x, vec3d.z, 1.5707963705062866);
            if (blockPos == null) {
                return null;
            }
            BlockPos blockPos2 = FuzzyTargeting.towardTarget(entity, maxHorizontalRange, posTargetInRange, blockPos);
            if (blockPos2 == null) {
                return null;
            }
            return FuzzyTargeting.validate(entity, blockPos2);
        });
    }

    public static @Nullable BlockPos validate(PathAwareEntity entity, BlockPos pos) {
        if (NavigationConditions.isWaterAt(entity, pos = FuzzyPositions.upWhile(pos, entity.getEntityWorld().getTopYInclusive(), currentPos -> NavigationConditions.isSolidAt(entity, currentPos))) || NavigationConditions.hasPathfindingPenalty(entity, pos)) {
            return null;
        }
        return pos;
    }

    public static @Nullable BlockPos towardTarget(PathAwareEntity entity, double horizontalRange, boolean posTargetInRange, BlockPos relativeInRangePos) {
        BlockPos blockPos = FuzzyPositions.towardTarget(entity, horizontalRange, entity.getRandom(), relativeInRangePos);
        if (NavigationConditions.isHeightInvalid(blockPos, entity) || NavigationConditions.isPositionTargetOutOfWalkRange(posTargetInRange, entity, blockPos) || NavigationConditions.isInvalidPosition(entity.getNavigation(), blockPos)) {
            return null;
        }
        return blockPos;
    }
}


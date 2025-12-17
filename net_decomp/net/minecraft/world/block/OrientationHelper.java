/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  org.jspecify.annotations.Nullable
 */
package net.minecraft.world.block;

import net.minecraft.resource.featuretoggle.FeatureFlags;
import net.minecraft.util.math.Direction;
import net.minecraft.world.World;
import net.minecraft.world.block.WireOrientation;
import org.jspecify.annotations.Nullable;

public class OrientationHelper {
    public static @Nullable WireOrientation getEmissionOrientation(World world, @Nullable Direction up, @Nullable Direction front) {
        if (world.getEnabledFeatures().contains(FeatureFlags.REDSTONE_EXPERIMENTS)) {
            WireOrientation wireOrientation = WireOrientation.random(world.random).withSideBias(WireOrientation.SideBias.LEFT);
            if (front != null) {
                wireOrientation = wireOrientation.withUp(front);
            }
            if (up != null) {
                wireOrientation = wireOrientation.withFront(up);
            }
            return wireOrientation;
        }
        return null;
    }

    public static @Nullable WireOrientation withFrontNullable(@Nullable WireOrientation orientation, Direction direction) {
        return orientation == null ? null : orientation.withFront(direction);
    }
}


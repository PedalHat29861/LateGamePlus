/*
 * Decompiled with CFR 0.152.
 */
package net.minecraft.entity.ai.brain.task;

import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.attribute.EntityAttributes;
import net.minecraft.entity.mob.BreezeEntity;
import net.minecraft.util.hit.HitResult;
import net.minecraft.util.math.MathHelper;
import net.minecraft.util.math.Vec3d;
import net.minecraft.util.math.random.Random;
import net.minecraft.world.RaycastContext;

public class BreezeMovementUtil {
    private static final double MAX_MOVE_DISTANCE = 50.0;

    public static Vec3d getRandomPosBehindTarget(LivingEntity target, Random random) {
        int i = 90;
        float f = target.headYaw + 180.0f + (float)random.nextGaussian() * 90.0f / 2.0f;
        float g = MathHelper.lerp(random.nextFloat(), 4.0f, 8.0f);
        Vec3d vec3d = Vec3d.fromPolar(0.0f, f).multiply(g);
        return target.getEntityPos().add(vec3d);
    }

    public static boolean canMoveTo(BreezeEntity breeze, Vec3d pos) {
        Vec3d vec3d = new Vec3d(breeze.getX(), breeze.getY(), breeze.getZ());
        if (pos.distanceTo(vec3d) > BreezeMovementUtil.getMaxMoveDistance(breeze)) {
            return false;
        }
        return breeze.getEntityWorld().raycast(new RaycastContext(vec3d, pos, RaycastContext.ShapeType.COLLIDER, RaycastContext.FluidHandling.NONE, breeze)).getType() == HitResult.Type.MISS;
    }

    private static double getMaxMoveDistance(BreezeEntity breeze) {
        return Math.max(50.0, breeze.getAttributeValue(EntityAttributes.FOLLOW_RANGE));
    }
}


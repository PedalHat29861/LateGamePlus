/*
 * Decompiled with CFR 0.152.
 */
package net.minecraft.entity.mob;

import net.minecraft.entity.LivingEntity;
import net.minecraft.util.math.MathHelper;
import net.minecraft.util.math.Vec3d;

public class ElytraFlightController {
    private static final float field_54084 = 0.2617994f;
    private static final float field_54085 = -0.2617994f;
    private float leftWingPitch;
    private float leftWingYaw;
    private float leftWingRoll;
    private float lastLeftWingPitch;
    private float lastLeftWingYaw;
    private float lastLeftWingRoll;
    private final LivingEntity entity;

    public ElytraFlightController(LivingEntity entity) {
        this.entity = entity;
    }

    public void update() {
        float i;
        float h;
        float g;
        this.lastLeftWingPitch = this.leftWingPitch;
        this.lastLeftWingYaw = this.leftWingYaw;
        this.lastLeftWingRoll = this.leftWingRoll;
        if (this.entity.isGliding()) {
            float f = 1.0f;
            Vec3d vec3d = this.entity.getVelocity();
            if (vec3d.y < 0.0) {
                Vec3d vec3d2 = vec3d.normalize();
                f = 1.0f - (float)Math.pow(-vec3d2.y, 1.5);
            }
            g = MathHelper.lerp(f, 0.2617994f, 0.34906584f);
            h = MathHelper.lerp(f, -0.2617994f, -1.5707964f);
            i = 0.0f;
        } else if (this.entity.isInSneakingPose()) {
            g = 0.6981317f;
            h = -0.7853982f;
            i = 0.08726646f;
        } else {
            g = 0.2617994f;
            h = -0.2617994f;
            i = 0.0f;
        }
        this.leftWingPitch += (g - this.leftWingPitch) * 0.3f;
        this.leftWingYaw += (i - this.leftWingYaw) * 0.3f;
        this.leftWingRoll += (h - this.leftWingRoll) * 0.3f;
    }

    public float leftWingPitch(float tickProgress) {
        return MathHelper.lerp(tickProgress, this.lastLeftWingPitch, this.leftWingPitch);
    }

    public float leftWingYaw(float tickProgress) {
        return MathHelper.lerp(tickProgress, this.lastLeftWingYaw, this.leftWingYaw);
    }

    public float leftWingRoll(float tickProgress) {
        return MathHelper.lerp(tickProgress, this.lastLeftWingRoll, this.leftWingRoll);
    }
}


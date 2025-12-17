/*
 * Decompiled with CFR 0.152.
 */
package net.minecraft.entity;

import net.minecraft.util.math.MathHelper;

public class LimbAnimator {
    private float lastSpeed;
    private float speed;
    private float animationProgress;
    private float timeScale = 1.0f;

    public void setSpeed(float speed) {
        this.speed = speed;
    }

    public void updateLimbs(float targetSpeed, float speedChangeRate, float timeScale) {
        this.lastSpeed = this.speed;
        this.speed += (targetSpeed - this.speed) * speedChangeRate;
        this.animationProgress += this.speed;
        this.timeScale = timeScale;
    }

    public void reset() {
        this.lastSpeed = 0.0f;
        this.speed = 0.0f;
        this.animationProgress = 0.0f;
    }

    public float getSpeed() {
        return this.speed;
    }

    public float getAmplitude(float tickProgress) {
        return Math.min(MathHelper.lerp(tickProgress, this.lastSpeed, this.speed), 1.0f);
    }

    public float getAnimationProgress() {
        return this.animationProgress * this.timeScale;
    }

    public float getAnimationProgress(float tickProgress) {
        return (this.animationProgress - this.speed * (1.0f - tickProgress)) * this.timeScale;
    }

    public boolean isLimbMoving() {
        return this.speed > 1.0E-5f;
    }
}


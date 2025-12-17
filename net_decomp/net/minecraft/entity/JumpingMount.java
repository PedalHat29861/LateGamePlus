/*
 * Decompiled with CFR 0.152.
 */
package net.minecraft.entity;

import net.minecraft.entity.Mount;

public interface JumpingMount
extends Mount {
    public void setJumpStrength(int var1);

    public boolean canJump();

    public void startJumping(int var1);

    public void stopJumping();

    default public int getJumpCooldown() {
        return 0;
    }

    default public float clampJumpStrength(int strength) {
        return strength >= 90 ? 1.0f : 0.4f + 0.4f * (float)strength / 90.0f;
    }
}


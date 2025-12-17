/*
 * Decompiled with CFR 0.152.
 */
package net.minecraft.block.vault;

import net.minecraft.util.math.MathHelper;

public class VaultClientData {
    public static final float DISPLAY_ROTATION_SPEED = 10.0f;
    private float displayRotation;
    private float lastDisplayRotation;

    VaultClientData() {
    }

    public float getDisplayRotation() {
        return this.displayRotation;
    }

    public float getLastDisplayRotation() {
        return this.lastDisplayRotation;
    }

    void rotateDisplay() {
        this.lastDisplayRotation = this.displayRotation;
        this.displayRotation = MathHelper.wrapDegrees(this.displayRotation + 10.0f);
    }
}


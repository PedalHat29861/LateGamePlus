/*
 * Decompiled with CFR 0.152.
 */
package com.mojang.blaze3d.opengl;

import com.mojang.blaze3d.buffers.GpuFence;
import com.mojang.blaze3d.opengl.GlStateManager;

public class GlFence
implements GpuFence {
    private long handle = GlStateManager._glFenceSync(37143, 0);

    @Override
    public void close() {
        if (this.handle != 0L) {
            GlStateManager._glDeleteSync(this.handle);
            this.handle = 0L;
        }
    }

    @Override
    public boolean awaitCompletion(long timeoutMs) {
        if (this.handle == 0L) {
            return true;
        }
        int result = GlStateManager._glClientWaitSync(this.handle, 0, timeoutMs);
        if (result == 37147) {
            return false;
        }
        if (result == 37149) {
            throw new IllegalStateException("Failed to complete GPU fence: " + GlStateManager._getError());
        }
        return true;
    }
}


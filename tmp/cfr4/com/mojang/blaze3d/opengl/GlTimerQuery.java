/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  org.lwjgl.opengl.ARBTimerQuery
 *  org.lwjgl.opengl.GL32C
 */
package com.mojang.blaze3d.opengl;

import com.mojang.blaze3d.systems.GpuQuery;
import com.mojang.blaze3d.systems.RenderSystem;
import java.util.OptionalLong;
import org.lwjgl.opengl.ARBTimerQuery;
import org.lwjgl.opengl.GL32C;

public class GlTimerQuery
implements GpuQuery {
    private final int queryId;
    private boolean closed;
    private OptionalLong result = OptionalLong.empty();

    GlTimerQuery(int queryId) {
        this.queryId = queryId;
    }

    @Override
    public OptionalLong getValue() {
        RenderSystem.assertOnRenderThread();
        if (this.closed) {
            throw new IllegalStateException("GlTimerQuery is closed");
        }
        if (this.result.isPresent()) {
            return this.result;
        }
        if (GL32C.glGetQueryObjecti((int)this.queryId, (int)34919) == 1) {
            this.result = OptionalLong.of(ARBTimerQuery.glGetQueryObjecti64((int)this.queryId, (int)34918));
            return this.result;
        }
        return OptionalLong.empty();
    }

    @Override
    public void close() {
        RenderSystem.assertOnRenderThread();
        if (this.closed) {
            return;
        }
        this.closed = true;
        GL32C.glDeleteQueries((int)this.queryId);
    }
}


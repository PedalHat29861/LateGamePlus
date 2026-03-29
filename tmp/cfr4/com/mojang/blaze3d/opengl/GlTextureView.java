/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  it.unimi.dsi.fastutil.ints.Int2IntArrayMap
 *  it.unimi.dsi.fastutil.ints.Int2IntMap
 *  it.unimi.dsi.fastutil.ints.IntIterator
 *  org.jspecify.annotations.Nullable
 */
package com.mojang.blaze3d.opengl;

import com.mojang.blaze3d.opengl.DirectStateAccess;
import com.mojang.blaze3d.opengl.GlStateManager;
import com.mojang.blaze3d.opengl.GlTexture;
import com.mojang.blaze3d.textures.GpuTexture;
import com.mojang.blaze3d.textures.GpuTextureView;
import it.unimi.dsi.fastutil.ints.Int2IntArrayMap;
import it.unimi.dsi.fastutil.ints.Int2IntMap;
import it.unimi.dsi.fastutil.ints.IntIterator;
import org.jspecify.annotations.Nullable;

public class GlTextureView
extends GpuTextureView {
    private static final int EMPTY = -1;
    private boolean closed;
    private int firstFboId = -1;
    private int firstFboDepthId = -1;
    private @Nullable Int2IntMap fboCache;

    protected GlTextureView(GlTexture texture, int baseMipLevel, int mipLevels) {
        super(texture, baseMipLevel, mipLevels);
        texture.addViews();
    }

    @Override
    public boolean isClosed() {
        return this.closed;
    }

    @Override
    public void close() {
        if (!this.closed) {
            this.closed = true;
            this.texture().removeViews();
            if (this.firstFboId != -1) {
                GlStateManager._glDeleteFramebuffers(this.firstFboId);
            }
            if (this.fboCache != null) {
                IntIterator intIterator = this.fboCache.values().iterator();
                while (intIterator.hasNext()) {
                    int fbo = (Integer)intIterator.next();
                    GlStateManager._glDeleteFramebuffers(fbo);
                }
            }
        }
    }

    public int getFbo(DirectStateAccess dsa, @Nullable GpuTexture depth) {
        int depthId;
        int n = depthId = depth == null ? 0 : ((GlTexture)depth).id;
        if (this.firstFboDepthId == depthId) {
            return this.firstFboId;
        }
        if (this.firstFboId == -1) {
            this.firstFboId = this.createFbo(dsa, depthId);
            this.firstFboDepthId = depthId;
            return this.firstFboId;
        }
        if (this.fboCache == null) {
            this.fboCache = new Int2IntArrayMap();
        }
        return this.fboCache.computeIfAbsent(depthId, _depthId -> this.createFbo(dsa, _depthId));
    }

    private int createFbo(DirectStateAccess dsa, int depthid) {
        int fbo = dsa.createFrameBufferObject();
        dsa.bindFrameBufferTextures(fbo, this.texture().id, depthid, this.baseMipLevel(), 0);
        return fbo;
    }

    @Override
    public GlTexture texture() {
        return (GlTexture)super.texture();
    }
}


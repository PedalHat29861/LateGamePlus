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
import com.mojang.blaze3d.textures.GpuTexture;
import com.mojang.blaze3d.textures.TextureFormat;
import it.unimi.dsi.fastutil.ints.Int2IntArrayMap;
import it.unimi.dsi.fastutil.ints.Int2IntMap;
import it.unimi.dsi.fastutil.ints.IntIterator;
import org.jspecify.annotations.Nullable;

public class GlTexture
extends GpuTexture {
    private static final int EMPTY = -1;
    protected final int id;
    private int firstFboId = -1;
    private int firstFboDepthId = -1;
    private @Nullable Int2IntMap fboCache;
    protected boolean closed;
    private int views;

    protected GlTexture(@GpuTexture.Usage int usage, String label, TextureFormat format, int width, int height, int depthOrLayers, int mipLevels, int id) {
        super(usage, label, format, width, height, depthOrLayers, mipLevels);
        this.id = id;
    }

    @Override
    public void close() {
        if (this.closed) {
            return;
        }
        this.closed = true;
        if (this.views == 0) {
            this.destroyImmediately();
        }
    }

    private void destroyImmediately() {
        GlStateManager._deleteTexture(this.id);
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

    @Override
    public boolean isClosed() {
        return this.closed;
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
        dsa.bindFrameBufferTextures(fbo, this.id, depthid, 0, 0);
        return fbo;
    }

    public int glId() {
        return this.id;
    }

    public void addViews() {
        ++this.views;
    }

    public void removeViews() {
        --this.views;
        if (this.closed && this.views == 0) {
            this.destroyImmediately();
        }
    }
}


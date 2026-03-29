/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  org.jspecify.annotations.Nullable
 */
package com.mojang.blaze3d.pipeline;

import com.mojang.blaze3d.pipeline.RenderTarget;
import com.mojang.blaze3d.systems.RenderSystem;
import org.jspecify.annotations.Nullable;

public class TextureTarget
extends RenderTarget {
    public TextureTarget(@Nullable String label, int width, int height, boolean useDepth) {
        super(label, useDepth);
        RenderSystem.assertOnRenderThread();
        this.resize(width, height);
    }
}


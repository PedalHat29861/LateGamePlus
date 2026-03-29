/*
 * Decompiled with CFR 0.152.
 */
package com.mojang.blaze3d.opengl;

import com.mojang.blaze3d.opengl.GlStateManager;
import com.mojang.blaze3d.textures.TextureFormat;

public sealed interface Uniform
extends AutoCloseable {
    @Override
    default public void close() {
    }

    public record Sampler(int location, int samplerIndex) implements Uniform
    {
    }

    public record Utb(int location, int samplerIndex, TextureFormat format, int texture) implements Uniform
    {
        public Utb(int location, int samplerIndex, TextureFormat format) {
            this(location, samplerIndex, format, GlStateManager._genTexture());
        }

        @Override
        public void close() {
            GlStateManager._deleteTexture(this.texture);
        }
    }

    public record Ubo(int blockBinding) implements Uniform
    {
    }
}


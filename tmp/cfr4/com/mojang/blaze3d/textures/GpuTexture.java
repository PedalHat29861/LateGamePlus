/*
 * Decompiled with CFR 0.152.
 */
package com.mojang.blaze3d.textures;

import com.mojang.blaze3d.textures.TextureFormat;
import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

public abstract class GpuTexture
implements AutoCloseable {
    public static final int USAGE_COPY_DST = 1;
    public static final int USAGE_COPY_SRC = 2;
    public static final int USAGE_TEXTURE_BINDING = 4;
    public static final int USAGE_RENDER_ATTACHMENT = 8;
    public static final int USAGE_CUBEMAP_COMPATIBLE = 16;
    private final TextureFormat format;
    private final int width;
    private final int height;
    private final int depthOrLayers;
    private final int mipLevels;
    @Usage
    private final int usage;
    private final String label;

    public GpuTexture(@Usage int usage, String label, TextureFormat format, int width, int height, int depthOrLayers, int mipLevels) {
        this.usage = usage;
        this.label = label;
        this.format = format;
        this.width = width;
        this.height = height;
        this.depthOrLayers = depthOrLayers;
        this.mipLevels = mipLevels;
    }

    public int getWidth(int mipLevel) {
        return this.width >> mipLevel;
    }

    public int getHeight(int mipLevel) {
        return this.height >> mipLevel;
    }

    public int getDepthOrLayers() {
        return this.depthOrLayers;
    }

    public int getMipLevels() {
        return this.mipLevels;
    }

    public TextureFormat getFormat() {
        return this.format;
    }

    @Usage
    public int usage() {
        return this.usage;
    }

    public String getLabel() {
        return this.label;
    }

    @Override
    public abstract void close();

    public abstract boolean isClosed();

    @Retention(value=RetentionPolicy.CLASS)
    @Target(value={ElementType.FIELD, ElementType.PARAMETER, ElementType.LOCAL_VARIABLE, ElementType.METHOD, ElementType.TYPE_USE})
    public static @interface Usage {
    }
}


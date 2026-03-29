/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  org.jspecify.annotations.Nullable
 */
package com.mojang.blaze3d.systems;

import com.mojang.blaze3d.buffers.GpuBuffer;
import com.mojang.blaze3d.buffers.GpuBufferSlice;
import com.mojang.blaze3d.buffers.GpuFence;
import com.mojang.blaze3d.platform.NativeImage;
import com.mojang.blaze3d.systems.GpuQuery;
import com.mojang.blaze3d.systems.RenderPassBackend;
import com.mojang.blaze3d.textures.GpuTexture;
import com.mojang.blaze3d.textures.GpuTextureView;
import java.nio.ByteBuffer;
import java.util.OptionalDouble;
import java.util.OptionalInt;
import java.util.function.Supplier;
import org.jspecify.annotations.Nullable;

public interface CommandEncoderBackend {
    public RenderPassBackend createRenderPass(Supplier<String> var1, GpuTextureView var2, OptionalInt var3);

    public RenderPassBackend createRenderPass(Supplier<String> var1, GpuTextureView var2, OptionalInt var3, @Nullable GpuTextureView var4, OptionalDouble var5);

    public boolean isInRenderPass();

    public void clearColorTexture(GpuTexture var1, int var2);

    public void clearColorAndDepthTextures(GpuTexture var1, int var2, GpuTexture var3, double var4);

    public void clearColorAndDepthTextures(GpuTexture var1, int var2, GpuTexture var3, double var4, int var6, int var7, int var8, int var9);

    public void clearDepthTexture(GpuTexture var1, double var2);

    public void writeToBuffer(GpuBufferSlice var1, ByteBuffer var2);

    public GpuBuffer.MappedView mapBuffer(GpuBufferSlice var1, boolean var2, boolean var3);

    public void copyToBuffer(GpuBufferSlice var1, GpuBufferSlice var2);

    public void writeToTexture(GpuTexture var1, NativeImage var2, int var3, int var4, int var5, int var6, int var7, int var8, int var9, int var10);

    public void writeToTexture(GpuTexture var1, ByteBuffer var2, NativeImage.Format var3, int var4, int var5, int var6, int var7, int var8, int var9);

    public void copyTextureToBuffer(GpuTexture var1, GpuBuffer var2, long var3, Runnable var5, int var6);

    public void copyTextureToBuffer(GpuTexture var1, GpuBuffer var2, long var3, Runnable var5, int var6, int var7, int var8, int var9, int var10);

    public void copyTextureToTexture(GpuTexture var1, GpuTexture var2, int var3, int var4, int var5, int var6, int var7, int var8, int var9);

    public void presentTexture(GpuTextureView var1);

    public GpuFence createFence();

    public GpuQuery timerQueryBegin();

    public void timerQueryEnd(GpuQuery var1);
}


/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  org.jspecify.annotations.Nullable
 */
package com.mojang.blaze3d.systems;

import com.mojang.blaze3d.buffers.GpuBuffer;
import com.mojang.blaze3d.buffers.GpuBufferSlice;
import com.mojang.blaze3d.pipeline.RenderPipeline;
import com.mojang.blaze3d.systems.RenderPass;
import com.mojang.blaze3d.textures.GpuSampler;
import com.mojang.blaze3d.textures.GpuTextureView;
import com.mojang.blaze3d.vertex.VertexFormat;
import java.util.Collection;
import java.util.function.Supplier;
import org.jspecify.annotations.Nullable;

public interface RenderPassBackend
extends AutoCloseable {
    public void pushDebugGroup(Supplier<String> var1);

    public void popDebugGroup();

    public void setPipeline(RenderPipeline var1);

    public void bindTexture(String var1, @Nullable GpuTextureView var2, @Nullable GpuSampler var3);

    public void setUniform(String var1, GpuBuffer var2);

    public void setUniform(String var1, GpuBufferSlice var2);

    public void enableScissor(int var1, int var2, int var3, int var4);

    public void disableScissor();

    public void setVertexBuffer(int var1, GpuBuffer var2);

    public void setIndexBuffer(GpuBuffer var1, VertexFormat.IndexType var2);

    public void drawIndexed(int var1, int var2, int var3, int var4);

    public <T> void drawMultipleIndexed(Collection<RenderPass.Draw<T>> var1, @Nullable GpuBuffer var2, @Nullable VertexFormat.IndexType var3, Collection<String> var4, T var5);

    public void draw(int var1, int var2);

    @Override
    public void close();

    public boolean isClosed();
}


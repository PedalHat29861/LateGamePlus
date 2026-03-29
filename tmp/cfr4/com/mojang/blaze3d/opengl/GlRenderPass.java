/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  net.minecraft.SharedConstants
 *  org.jspecify.annotations.Nullable
 */
package com.mojang.blaze3d.opengl;

import com.mojang.blaze3d.buffers.GpuBuffer;
import com.mojang.blaze3d.buffers.GpuBufferSlice;
import com.mojang.blaze3d.opengl.GlCommandEncoder;
import com.mojang.blaze3d.opengl.GlDevice;
import com.mojang.blaze3d.opengl.GlRenderPipeline;
import com.mojang.blaze3d.opengl.GlSampler;
import com.mojang.blaze3d.opengl.GlTextureView;
import com.mojang.blaze3d.pipeline.RenderPipeline;
import com.mojang.blaze3d.systems.RenderPass;
import com.mojang.blaze3d.systems.RenderPassBackend;
import com.mojang.blaze3d.systems.ScissorState;
import com.mojang.blaze3d.textures.GpuSampler;
import com.mojang.blaze3d.textures.GpuTextureView;
import com.mojang.blaze3d.vertex.VertexFormat;
import java.util.Collection;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Set;
import java.util.function.Supplier;
import net.minecraft.SharedConstants;
import org.jspecify.annotations.Nullable;

class GlRenderPass
implements RenderPassBackend {
    protected static final int MAX_VERTEX_BUFFERS = 1;
    public static final boolean VALIDATION = SharedConstants.IS_RUNNING_IN_IDE;
    private final GlCommandEncoder encoder;
    private final GlDevice device;
    private final boolean hasDepthTexture;
    private boolean closed;
    protected @Nullable GlRenderPipeline pipeline;
    protected final @Nullable GpuBuffer[] vertexBuffers = new GpuBuffer[1];
    protected @Nullable GpuBuffer indexBuffer;
    protected VertexFormat.IndexType indexType = VertexFormat.IndexType.INT;
    private final ScissorState scissorState = new ScissorState();
    protected final HashMap<String, GpuBufferSlice> uniforms = new HashMap();
    protected final HashMap<String, TextureViewAndSampler> samplers = new HashMap();
    protected final Set<String> dirtyUniforms = new HashSet<String>();

    public GlRenderPass(GlCommandEncoder encoder, GlDevice device, boolean hasDepthTexture) {
        this.encoder = encoder;
        this.device = device;
        this.hasDepthTexture = hasDepthTexture;
    }

    public boolean hasDepthTexture() {
        return this.hasDepthTexture;
    }

    @Override
    public void pushDebugGroup(Supplier<String> label) {
        this.device.debugLabels().pushDebugGroup(label);
    }

    @Override
    public void popDebugGroup() {
        this.device.debugLabels().popDebugGroup();
    }

    @Override
    public void setPipeline(RenderPipeline pipeline) {
        if (this.pipeline == null || this.pipeline.info() != pipeline) {
            this.dirtyUniforms.addAll(this.uniforms.keySet());
            this.dirtyUniforms.addAll(this.samplers.keySet());
        }
        this.pipeline = this.device.getOrCompilePipeline(pipeline);
    }

    @Override
    public void bindTexture(String name, @Nullable GpuTextureView textureView, @Nullable GpuSampler sampler) {
        if (sampler == null) {
            this.samplers.remove(name);
        } else {
            this.samplers.put(name, new TextureViewAndSampler((GlTextureView)textureView, (GlSampler)sampler));
        }
        this.dirtyUniforms.add(name);
    }

    @Override
    public void setUniform(String name, GpuBuffer value) {
        this.uniforms.put(name, value.slice());
        this.dirtyUniforms.add(name);
    }

    @Override
    public void setUniform(String name, GpuBufferSlice value) {
        this.uniforms.put(name, value);
        this.dirtyUniforms.add(name);
    }

    @Override
    public void enableScissor(int x, int y, int width, int height) {
        this.scissorState.enable(x, y, width, height);
    }

    @Override
    public void disableScissor() {
        this.scissorState.disable();
    }

    public boolean isScissorEnabled() {
        return this.scissorState.enabled();
    }

    public int getScissorX() {
        return this.scissorState.x();
    }

    public int getScissorY() {
        return this.scissorState.y();
    }

    public int getScissorWidth() {
        return this.scissorState.width();
    }

    public int getScissorHeight() {
        return this.scissorState.height();
    }

    @Override
    public void setVertexBuffer(int slot, GpuBuffer vertexBuffer) {
        if (slot < 0 || slot >= 1) {
            throw new IllegalArgumentException("Vertex buffer slot is out of range: " + slot);
        }
        this.vertexBuffers[slot] = vertexBuffer;
    }

    @Override
    public void setIndexBuffer(@Nullable GpuBuffer indexBuffer, VertexFormat.IndexType indexType) {
        this.indexBuffer = indexBuffer;
        this.indexType = indexType;
    }

    @Override
    public void drawIndexed(int baseVertex, int firstIndex, int indexCount, int instanceCount) {
        this.encoder.executeDraw(this, baseVertex, firstIndex, indexCount, this.indexType, instanceCount);
    }

    @Override
    public <T> void drawMultipleIndexed(Collection<RenderPass.Draw<T>> draws, @Nullable GpuBuffer defaultIndexBuffer,  @Nullable VertexFormat.IndexType defaultIndexType, Collection<String> dynamicUniforms, T uniformArgument) {
        this.encoder.executeDrawMultiple(this, draws, defaultIndexBuffer, defaultIndexType, dynamicUniforms, uniformArgument);
    }

    @Override
    public void draw(int firstVertex, int vertexCount) {
        this.encoder.executeDraw(this, firstVertex, 0, vertexCount, null, 1);
    }

    @Override
    public void close() {
        if (!this.closed) {
            this.closed = true;
            this.encoder.finishRenderPass();
        }
    }

    @Override
    public boolean isClosed() {
        return this.closed;
    }

    protected record TextureViewAndSampler(GlTextureView view, GlSampler sampler) {
    }
}


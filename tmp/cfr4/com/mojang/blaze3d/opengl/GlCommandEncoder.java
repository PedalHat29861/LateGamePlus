/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  com.mojang.logging.LogUtils
 *  net.minecraft.util.ARGB
 *  org.jspecify.annotations.Nullable
 *  org.lwjgl.opengl.GL11
 *  org.lwjgl.opengl.GL11C
 *  org.lwjgl.opengl.GL31
 *  org.lwjgl.opengl.GL32
 *  org.lwjgl.opengl.GL32C
 *  org.lwjgl.opengl.GL33C
 *  org.slf4j.Logger
 */
package com.mojang.blaze3d.opengl;

import com.mojang.blaze3d.buffers.GpuBuffer;
import com.mojang.blaze3d.buffers.GpuBufferSlice;
import com.mojang.blaze3d.buffers.GpuFence;
import com.mojang.blaze3d.opengl.GlBuffer;
import com.mojang.blaze3d.opengl.GlConst;
import com.mojang.blaze3d.opengl.GlDevice;
import com.mojang.blaze3d.opengl.GlFence;
import com.mojang.blaze3d.opengl.GlProgram;
import com.mojang.blaze3d.opengl.GlRenderPass;
import com.mojang.blaze3d.opengl.GlRenderPipeline;
import com.mojang.blaze3d.opengl.GlStateManager;
import com.mojang.blaze3d.opengl.GlTexture;
import com.mojang.blaze3d.opengl.GlTextureView;
import com.mojang.blaze3d.opengl.GlTimerQuery;
import com.mojang.blaze3d.opengl.Uniform;
import com.mojang.blaze3d.pipeline.BlendFunction;
import com.mojang.blaze3d.pipeline.DepthStencilState;
import com.mojang.blaze3d.pipeline.RenderPipeline;
import com.mojang.blaze3d.platform.NativeImage;
import com.mojang.blaze3d.shaders.UniformType;
import com.mojang.blaze3d.systems.CommandEncoderBackend;
import com.mojang.blaze3d.systems.GpuQuery;
import com.mojang.blaze3d.systems.RenderPass;
import com.mojang.blaze3d.systems.RenderPassBackend;
import com.mojang.blaze3d.systems.RenderSystem;
import com.mojang.blaze3d.textures.GpuTexture;
import com.mojang.blaze3d.textures.GpuTextureView;
import com.mojang.blaze3d.textures.TextureFormat;
import com.mojang.blaze3d.vertex.VertexFormat;
import com.mojang.logging.LogUtils;
import java.lang.runtime.SwitchBootstraps;
import java.nio.ByteBuffer;
import java.util.Collection;
import java.util.Collections;
import java.util.Map;
import java.util.Objects;
import java.util.OptionalDouble;
import java.util.OptionalInt;
import java.util.function.BiConsumer;
import java.util.function.Supplier;
import net.minecraft.util.ARGB;
import org.jspecify.annotations.Nullable;
import org.lwjgl.opengl.GL11;
import org.lwjgl.opengl.GL11C;
import org.lwjgl.opengl.GL31;
import org.lwjgl.opengl.GL32;
import org.lwjgl.opengl.GL32C;
import org.lwjgl.opengl.GL33C;
import org.slf4j.Logger;

class GlCommandEncoder
implements CommandEncoderBackend {
    private static final Logger LOGGER = LogUtils.getLogger();
    private final GlDevice device;
    private final int readFbo;
    private final int drawFbo;
    private @Nullable RenderPipeline lastPipeline;
    private boolean inRenderPass;
    private @Nullable GlProgram lastProgram;
    private @Nullable GlTimerQuery activeTimerQuery;

    protected GlCommandEncoder(GlDevice device) {
        this.device = device;
        this.readFbo = device.directStateAccess().createFrameBufferObject();
        this.drawFbo = device.directStateAccess().createFrameBufferObject();
    }

    @Override
    public RenderPassBackend createRenderPass(Supplier<String> label, GpuTextureView colorTexture, OptionalInt clearColor) {
        return this.createRenderPass(label, colorTexture, clearColor, null, OptionalDouble.empty());
    }

    @Override
    public RenderPassBackend createRenderPass(Supplier<String> label, GpuTextureView colorTexture, OptionalInt clearColor, @Nullable GpuTextureView depthTexture, OptionalDouble clearDepth) {
        this.inRenderPass = true;
        this.device.debugLabels().pushDebugGroup(label);
        int fbo = ((GlTextureView)colorTexture).getFbo(this.device.directStateAccess(), depthTexture == null ? null : depthTexture.texture());
        GlStateManager._glBindFramebuffer(36160, fbo);
        int clearMask = 0;
        if (clearColor.isPresent()) {
            int argb = clearColor.getAsInt();
            GL11.glClearColor((float)ARGB.redFloat((int)argb), (float)ARGB.greenFloat((int)argb), (float)ARGB.blueFloat((int)argb), (float)ARGB.alphaFloat((int)argb));
            clearMask |= 0x4000;
        }
        if (depthTexture != null && clearDepth.isPresent()) {
            GL11.glClearDepth((double)clearDepth.getAsDouble());
            clearMask |= 0x100;
        }
        if (clearMask != 0) {
            GlStateManager._disableScissorTest();
            GlStateManager._depthMask(true);
            GlStateManager._colorMask(15);
            GlStateManager._clear(clearMask);
        }
        GlStateManager._viewport(0, 0, colorTexture.getWidth(0), colorTexture.getHeight(0));
        this.lastPipeline = null;
        return new GlRenderPass(this, this.device, depthTexture != null);
    }

    @Override
    public boolean isInRenderPass() {
        return this.inRenderPass;
    }

    @Override
    public void clearColorTexture(GpuTexture colorTexture, int clearColor) {
        this.device.directStateAccess().bindFrameBufferTextures(this.drawFbo, ((GlTexture)colorTexture).id, 0, 0, 36160);
        GL11.glClearColor((float)ARGB.redFloat((int)clearColor), (float)ARGB.greenFloat((int)clearColor), (float)ARGB.blueFloat((int)clearColor), (float)ARGB.alphaFloat((int)clearColor));
        GlStateManager._disableScissorTest();
        GlStateManager._colorMask(15);
        GlStateManager._clear(16384);
        GlStateManager._glFramebufferTexture2D(36160, 36064, 3553, 0, 0);
        GlStateManager._glBindFramebuffer(36160, 0);
    }

    @Override
    public void clearColorAndDepthTextures(GpuTexture colorTexture, int clearColor, GpuTexture depthTexture, double clearDepth) {
        int fbo = ((GlTexture)colorTexture).getFbo(this.device.directStateAccess(), depthTexture);
        GlStateManager._glBindFramebuffer(36160, fbo);
        GlStateManager._disableScissorTest();
        GL11.glClearDepth((double)clearDepth);
        GL11.glClearColor((float)ARGB.redFloat((int)clearColor), (float)ARGB.greenFloat((int)clearColor), (float)ARGB.blueFloat((int)clearColor), (float)ARGB.alphaFloat((int)clearColor));
        GlStateManager._depthMask(true);
        GlStateManager._colorMask(15);
        GlStateManager._clear(16640);
        GlStateManager._glBindFramebuffer(36160, 0);
    }

    @Override
    public void clearColorAndDepthTextures(GpuTexture colorTexture, int clearColor, GpuTexture depthTexture, double clearDepth, int regionX, int regionY, int regionWidth, int regionHeight) {
        int fbo = ((GlTexture)colorTexture).getFbo(this.device.directStateAccess(), depthTexture);
        GlStateManager._glBindFramebuffer(36160, fbo);
        GlStateManager._scissorBox(regionX, regionY, regionWidth, regionHeight);
        GlStateManager._enableScissorTest();
        GL11.glClearDepth((double)clearDepth);
        GL11.glClearColor((float)ARGB.redFloat((int)clearColor), (float)ARGB.greenFloat((int)clearColor), (float)ARGB.blueFloat((int)clearColor), (float)ARGB.alphaFloat((int)clearColor));
        GlStateManager._depthMask(true);
        GlStateManager._colorMask(15);
        GlStateManager._clear(16640);
        GlStateManager._glBindFramebuffer(36160, 0);
    }

    @Override
    public void clearDepthTexture(GpuTexture depthTexture, double clearDepth) {
        this.device.directStateAccess().bindFrameBufferTextures(this.drawFbo, 0, ((GlTexture)depthTexture).id, 0, 36160);
        GL11.glDrawBuffer((int)0);
        GL11.glClearDepth((double)clearDepth);
        GlStateManager._depthMask(true);
        GlStateManager._disableScissorTest();
        GlStateManager._clear(256);
        GL11.glDrawBuffer((int)36064);
        GlStateManager._glFramebufferTexture2D(36160, 36096, 3553, 0, 0);
        GlStateManager._glBindFramebuffer(36160, 0);
    }

    @Override
    public void writeToBuffer(GpuBufferSlice slice, ByteBuffer data) {
        GlBuffer buffer = (GlBuffer)slice.buffer();
        if (buffer.closed) {
            throw new IllegalStateException("Buffer already closed");
        }
        if ((buffer.usage() & 8) == 0) {
            throw new IllegalStateException("Buffer needs USAGE_COPY_DST to be a destination for a copy");
        }
        int length = data.remaining();
        if ((long)length > slice.length()) {
            throw new IllegalArgumentException("Cannot write more data than the slice allows (attempting to write " + length + " bytes into a slice of length " + slice.length() + ")");
        }
        if (slice.length() + slice.offset() > buffer.size()) {
            throw new IllegalArgumentException("Cannot write more data than this buffer can hold (attempting to write " + length + " bytes at offset " + slice.offset() + " to " + buffer.size() + " size buffer)");
        }
        this.device.directStateAccess().bufferSubData(buffer.handle, slice.offset(), data, buffer.usage());
    }

    @Override
    public GpuBuffer.MappedView mapBuffer(GpuBufferSlice slice, boolean read, boolean write) {
        GlBuffer buffer = (GlBuffer)slice.buffer();
        int flags = 0;
        if (read) {
            flags |= 1;
        }
        if (write) {
            flags |= 0x22;
        }
        return this.device.getBufferStorage().mapBuffer(this.device.directStateAccess(), buffer, slice.offset(), slice.length(), flags);
    }

    @Override
    public void copyToBuffer(GpuBufferSlice source, GpuBufferSlice target) {
        GlBuffer sourceBuffer = (GlBuffer)source.buffer();
        GlBuffer targetBuffer = (GlBuffer)target.buffer();
        this.device.directStateAccess().copyBufferSubData(sourceBuffer.handle, targetBuffer.handle, source.offset(), target.offset(), source.length());
    }

    @Override
    public void writeToTexture(GpuTexture destination, NativeImage source, int mipLevel, int depthOrLayer, int destX, int destY, int width, int height, int sourceX, int sourceY) {
        int target;
        if ((destination.usage() & 0x10) != 0) {
            target = GlConst.CUBEMAP_TARGETS[depthOrLayer % 6];
            GL11.glBindTexture((int)34067, (int)((GlTexture)destination).id);
        } else {
            target = 3553;
            GlStateManager._bindTexture(((GlTexture)destination).id);
        }
        GlStateManager._pixelStore(3314, source.getWidth());
        GlStateManager._pixelStore(3316, sourceX);
        GlStateManager._pixelStore(3315, sourceY);
        GlStateManager._pixelStore(3317, source.format().components());
        GlStateManager._texSubImage2D(target, mipLevel, destX, destY, width, height, GlConst.toGl(source.format()), 5121, source.getPointer());
    }

    @Override
    public void writeToTexture(GpuTexture destination, ByteBuffer source, NativeImage.Format format, int mipLevel, int depthOrLayer, int destX, int destY, int width, int height) {
        int target;
        if ((destination.usage() & 0x10) != 0) {
            target = GlConst.CUBEMAP_TARGETS[depthOrLayer % 6];
            GL11.glBindTexture((int)34067, (int)((GlTexture)destination).id);
        } else {
            target = 3553;
            GlStateManager._bindTexture(((GlTexture)destination).id);
        }
        GlStateManager._pixelStore(3314, width);
        GlStateManager._pixelStore(3316, 0);
        GlStateManager._pixelStore(3315, 0);
        GlStateManager._pixelStore(3317, format.components());
        GlStateManager._texSubImage2D(target, mipLevel, destX, destY, width, height, GlConst.toGl(format), 5121, source);
    }

    @Override
    public void copyTextureToBuffer(GpuTexture source, GpuBuffer destination, long offset, Runnable callback, int mipLevel) {
        this.copyTextureToBuffer(source, destination, offset, callback, mipLevel, 0, 0, source.getWidth(mipLevel), source.getHeight(mipLevel));
    }

    @Override
    public void copyTextureToBuffer(GpuTexture source, GpuBuffer destination, long offset, Runnable callback, int mipLevel, int x, int y, int width, int height) {
        GlStateManager.clearGlErrors();
        this.device.directStateAccess().bindFrameBufferTextures(this.readFbo, ((GlTexture)source).glId(), 0, mipLevel, 36008);
        GlStateManager._glBindBuffer(35051, ((GlBuffer)destination).handle);
        GlStateManager._pixelStore(3330, width);
        GlStateManager._readPixels(x, y, width, height, GlConst.toGlExternalId(source.getFormat()), GlConst.toGlType(source.getFormat()), offset);
        RenderSystem.queueFencedTask(callback);
        GlStateManager._glFramebufferTexture2D(36008, 36064, 3553, 0, mipLevel);
        GlStateManager._glBindFramebuffer(36008, 0);
        GlStateManager._glBindBuffer(35051, 0);
        int error = GlStateManager._getError();
        if (error != 0) {
            throw new IllegalStateException("Couldn't perform copyTobuffer for texture " + source.getLabel() + ": GL error " + error);
        }
    }

    @Override
    public void copyTextureToTexture(GpuTexture source, GpuTexture destination, int mipLevel, int destX, int destY, int sourceX, int sourceY, int width, int height) {
        GlStateManager.clearGlErrors();
        GlStateManager._disableScissorTest();
        boolean isDepth = source.getFormat().hasDepthAspect();
        int sourceId = ((GlTexture)source).glId();
        int destId = ((GlTexture)destination).glId();
        this.device.directStateAccess().bindFrameBufferTextures(this.readFbo, isDepth ? 0 : sourceId, isDepth ? sourceId : 0, 0, 0);
        this.device.directStateAccess().bindFrameBufferTextures(this.drawFbo, isDepth ? 0 : destId, isDepth ? destId : 0, 0, 0);
        this.device.directStateAccess().blitFrameBuffers(this.readFbo, this.drawFbo, sourceX, sourceY, width, height, destX, destY, width, height, isDepth ? 256 : 16384, 9728);
        int error = GlStateManager._getError();
        if (error != 0) {
            throw new IllegalStateException("Couldn't perform copyToTexture for texture " + source.getLabel() + " to " + destination.getLabel() + ": GL error " + error);
        }
    }

    @Override
    public void presentTexture(GpuTextureView textureView) {
        GlStateManager._disableScissorTest();
        GlStateManager._viewport(0, 0, textureView.getWidth(0), textureView.getHeight(0));
        GlStateManager._depthMask(true);
        GlStateManager._colorMask(15);
        this.device.directStateAccess().bindFrameBufferTextures(this.drawFbo, ((GlTexture)textureView.texture()).glId(), 0, 0, 0);
        this.device.directStateAccess().blitFrameBuffers(this.drawFbo, 0, 0, 0, textureView.getWidth(0), textureView.getHeight(0), 0, 0, textureView.getWidth(0), textureView.getHeight(0), 16384, 9728);
    }

    @Override
    public GpuFence createFence() {
        return new GlFence();
    }

    protected <T> void executeDrawMultiple(GlRenderPass renderPass, Collection<RenderPass.Draw<T>> draws, @Nullable GpuBuffer defaultIndexBuffer, @Nullable VertexFormat.IndexType defaultIndexType, Collection<String> dynamicUniforms, T uniformArgument) {
        if (!this.trySetup(renderPass, dynamicUniforms)) {
            return;
        }
        if (defaultIndexType == null) {
            defaultIndexType = VertexFormat.IndexType.SHORT;
        }
        for (RenderPass.Draw<T> draw : draws) {
            BiConsumer<T, RenderPass.UniformUploader> uniformUploaderConsumer;
            VertexFormat.IndexType indexType = draw.indexType() == null ? defaultIndexType : draw.indexType();
            renderPass.setIndexBuffer(draw.indexBuffer() == null ? defaultIndexBuffer : draw.indexBuffer(), indexType);
            renderPass.setVertexBuffer(draw.slot(), draw.vertexBuffer());
            if (GlRenderPass.VALIDATION) {
                if (renderPass.indexBuffer == null) {
                    throw new IllegalStateException("Missing index buffer");
                }
                if (renderPass.indexBuffer.isClosed()) {
                    throw new IllegalStateException("Index buffer has been closed!");
                }
                if (renderPass.vertexBuffers[0] == null) {
                    throw new IllegalStateException("Missing vertex buffer at slot 0");
                }
                if (renderPass.vertexBuffers[0].isClosed()) {
                    throw new IllegalStateException("Vertex buffer at slot 0 has been closed!");
                }
            }
            if ((uniformUploaderConsumer = draw.uniformUploaderConsumer()) != null) {
                uniformUploaderConsumer.accept(uniformArgument, (name, buffer) -> {
                    block3: {
                        Uniform patt1$temp = renderPass.pipeline.program().getUniform(name);
                        if (patt1$temp instanceof Uniform.Ubo) {
                            int blockBinding;
                            Uniform.Ubo $b$0 = (Uniform.Ubo)patt1$temp;
                            try {
                                int patt2$temp;
                                int tmp0$ = patt2$temp = $b$0.blockBinding();
                                if (!true) break block3;
                                blockBinding = patt2$temp;
                            }
                            catch (Throwable throwable) {
                                throw new MatchException(throwable.toString(), throwable);
                            }
                            GL32.glBindBufferRange((int)35345, (int)blockBinding, (int)((GlBuffer)buffer.buffer()).handle, (long)buffer.offset(), (long)buffer.length());
                        }
                    }
                });
            }
            this.drawFromBuffers(renderPass, draw.baseVertex(), draw.firstIndex(), draw.indexCount(), indexType, renderPass.pipeline, 1);
        }
    }

    protected void executeDraw(GlRenderPass renderPass, int baseVertex, int firstIndex, int drawCount, @Nullable VertexFormat.IndexType indexType, int instanceCount) {
        if (!this.trySetup(renderPass, Collections.emptyList())) {
            return;
        }
        if (GlRenderPass.VALIDATION) {
            if (indexType != null) {
                if (renderPass.indexBuffer == null) {
                    throw new IllegalStateException("Missing index buffer");
                }
                if (renderPass.indexBuffer.isClosed()) {
                    throw new IllegalStateException("Index buffer has been closed!");
                }
                if ((renderPass.indexBuffer.usage() & 0x40) == 0) {
                    throw new IllegalStateException("Index buffer must have GpuBuffer.USAGE_INDEX!");
                }
            }
            GlRenderPipeline pipeline = renderPass.pipeline;
            if (renderPass.vertexBuffers[0] == null && pipeline != null && !pipeline.info().getVertexFormat().getElements().isEmpty()) {
                throw new IllegalStateException("Vertex format contains elements but vertex buffer at slot 0 is null");
            }
            if (renderPass.vertexBuffers[0] != null && renderPass.vertexBuffers[0].isClosed()) {
                throw new IllegalStateException("Vertex buffer at slot 0 has been closed!");
            }
            if (renderPass.vertexBuffers[0] != null && (renderPass.vertexBuffers[0].usage() & 0x20) == 0) {
                throw new IllegalStateException("Vertex buffer must have GpuBuffer.USAGE_VERTEX!");
            }
        }
        this.drawFromBuffers(renderPass, baseVertex, firstIndex, drawCount, indexType, renderPass.pipeline, instanceCount);
    }

    private void drawFromBuffers(GlRenderPass renderPass, int baseVertex, int firstIndex, int drawCount, @Nullable VertexFormat.IndexType indexType, GlRenderPipeline pipeline, int instanceCount) {
        this.device.vertexArrayCache().bindVertexArray(pipeline.info().getVertexFormat(), (GlBuffer)renderPass.vertexBuffers[0]);
        if (indexType != null) {
            GlStateManager._glBindBuffer(34963, ((GlBuffer)renderPass.indexBuffer).handle);
            if (instanceCount > 1) {
                if (baseVertex > 0) {
                    GL32.glDrawElementsInstancedBaseVertex((int)GlConst.toGl(pipeline.info().getVertexFormatMode()), (int)drawCount, (int)GlConst.toGl(indexType), (long)((long)firstIndex * (long)indexType.bytes), (int)instanceCount, (int)baseVertex);
                } else {
                    GL31.glDrawElementsInstanced((int)GlConst.toGl(pipeline.info().getVertexFormatMode()), (int)drawCount, (int)GlConst.toGl(indexType), (long)((long)firstIndex * (long)indexType.bytes), (int)instanceCount);
                }
            } else if (baseVertex > 0) {
                GL32.glDrawElementsBaseVertex((int)GlConst.toGl(pipeline.info().getVertexFormatMode()), (int)drawCount, (int)GlConst.toGl(indexType), (long)((long)firstIndex * (long)indexType.bytes), (int)baseVertex);
            } else {
                GlStateManager._drawElements(GlConst.toGl(pipeline.info().getVertexFormatMode()), drawCount, GlConst.toGl(indexType), (long)firstIndex * (long)indexType.bytes);
            }
        } else if (instanceCount > 1) {
            GL31.glDrawArraysInstanced((int)GlConst.toGl(pipeline.info().getVertexFormatMode()), (int)baseVertex, (int)drawCount, (int)instanceCount);
        } else {
            GlStateManager._drawArrays(GlConst.toGl(pipeline.info().getVertexFormatMode()), baseVertex, drawCount);
        }
    }

    /*
     * WARNING - Removed back jump from a try to a catch block - possible behaviour change.
     * Enabled aggressive block sorting
     * Enabled unnecessary exception pruning
     * Enabled aggressive exception aggregation
     */
    private boolean trySetup(GlRenderPass renderPass, Collection<String> dynamicUniforms) {
        boolean differentProgram;
        if (GlRenderPass.VALIDATION) {
            if (renderPass.pipeline == null) {
                throw new IllegalStateException("Can't draw without a render pipeline");
            }
            if (renderPass.pipeline.program() == GlProgram.INVALID_PROGRAM) {
                throw new IllegalStateException("Pipeline contains invalid shader program");
            }
            for (RenderPipeline.UniformDescription uniformDescription : renderPass.pipeline.info().getUniforms()) {
                GpuBufferSlice value = renderPass.uniforms.get(uniformDescription.name());
                if (dynamicUniforms.contains(uniformDescription.name())) continue;
                if (value == null) {
                    throw new IllegalStateException("Missing uniform " + uniformDescription.name() + " (should be " + String.valueOf((Object)uniformDescription.type()) + ")");
                }
                if (uniformDescription.type() == UniformType.UNIFORM_BUFFER) {
                    if (value.buffer().isClosed()) {
                        throw new IllegalStateException("Uniform buffer " + uniformDescription.name() + " is already closed");
                    }
                    if ((value.buffer().usage() & 0x80) == 0) {
                        throw new IllegalStateException("Uniform buffer " + uniformDescription.name() + " must have GpuBuffer.USAGE_UNIFORM");
                    }
                }
                if (uniformDescription.type() != UniformType.TEXEL_BUFFER) continue;
                if (value.offset() != 0L) throw new IllegalStateException("Uniform texel buffers do not support a slice of a buffer, must be entire buffer");
                if (value.length() != value.buffer().size()) {
                    throw new IllegalStateException("Uniform texel buffers do not support a slice of a buffer, must be entire buffer");
                }
                if (uniformDescription.textureFormat() != null) continue;
                throw new IllegalStateException("Invalid uniform texel buffer " + uniformDescription.name() + " (missing a texture format)");
            }
            for (Map.Entry entry : renderPass.pipeline.program().getUniforms().entrySet()) {
                if (!(entry.getValue() instanceof Uniform.Sampler)) continue;
                String name = (String)entry.getKey();
                GlRenderPass.TextureViewAndSampler viewAndSampler = renderPass.samplers.get(name);
                if (viewAndSampler == null) {
                    throw new IllegalStateException("Missing sampler " + name);
                }
                GlTextureView textureView = viewAndSampler.view();
                if (textureView.isClosed()) {
                    throw new IllegalStateException("Texture view " + name + " (" + textureView.texture().getLabel() + ") has been closed!");
                }
                if ((textureView.texture().usage() & 4) == 0) {
                    throw new IllegalStateException("Texture view " + name + " (" + textureView.texture().getLabel() + ") must have USAGE_TEXTURE_BINDING!");
                }
                if (!viewAndSampler.sampler().isClosed()) continue;
                throw new IllegalStateException("Sampler for " + name + " (" + textureView.texture().getLabel() + ") has been closed!");
            }
            if (renderPass.pipeline.info().wantsDepthTexture() && !renderPass.hasDepthTexture()) {
                LOGGER.warn("Render pipeline {} wants a depth texture but none was provided - this is probably a bug", (Object)renderPass.pipeline.info().getLocation());
            }
        } else {
            if (renderPass.pipeline == null) return false;
            if (renderPass.pipeline.program() == GlProgram.INVALID_PROGRAM) {
                return false;
            }
        }
        RenderPipeline pipeline = renderPass.pipeline.info();
        GlProgram glProgram = renderPass.pipeline.program();
        this.applyPipelineState(pipeline);
        boolean bl = differentProgram = this.lastProgram != glProgram;
        if (differentProgram) {
            GlStateManager._glUseProgram(glProgram.getProgramId());
            this.lastProgram = glProgram;
        }
        block11: for (Map.Entry<String, Uniform> entry : glProgram.getUniforms().entrySet()) {
            int target;
            int location;
            int n;
            int n2;
            Uniform uniform;
            String name = entry.getKey();
            boolean isDirty = renderPass.dirtyUniforms.contains(name);
            Objects.requireNonNull(entry.getValue());
            int n3 = 0;
            switch (SwitchBootstraps.typeSwitch("typeSwitch", new Object[]{Uniform.Ubo.class, Uniform.Utb.class, Uniform.Sampler.class}, (Uniform)uniform, n3)) {
                default: {
                    throw new MatchException(null, null);
                }
                case 0: {
                    Uniform.Ubo ubo = (Uniform.Ubo)uniform;
                    try {
                        int n4 = n2 = ubo.blockBinding();
                    }
                    catch (Throwable throwable) {
                        throw new MatchException(throwable.toString(), throwable);
                    }
                }
                int blockBinding = n2;
                if (!isDirty) continue block11;
                GpuBufferSlice bufferView = renderPass.uniforms.get(name);
                GL32.glBindBufferRange((int)35345, (int)blockBinding, (int)((GlBuffer)bufferView.buffer()).handle, (long)bufferView.offset(), (long)bufferView.length());
                continue block11;
                case 1: {
                    TextureFormat format;
                    int samplerIndex;
                    int location2;
                    int n5;
                    Uniform.Utb utb = (Uniform.Utb)uniform;
                    {
                        TextureFormat textureFormat;
                        int n6 = n5 = utb.location();
                        location2 = n5;
                        n6 = n5 = utb.samplerIndex();
                        samplerIndex = n5;
                        format = textureFormat = utb.format();
                        n6 = n5 = utb.texture();
                    }
                    int texture = n5;
                    if (differentProgram || isDirty) {
                        GlStateManager._glUniform1i(location2, samplerIndex);
                    }
                    GlStateManager._activeTexture(33984 + samplerIndex);
                    GL11C.glBindTexture((int)35882, (int)texture);
                    if (!isDirty) continue block11;
                    GpuBufferSlice bufferView2 = renderPass.uniforms.get(name);
                    GL31.glTexBuffer((int)35882, (int)GlConst.toGlInternalId(format), (int)((GlBuffer)bufferView2.buffer()).handle);
                    continue block11;
                }
                case 2: 
            }
            Uniform.Sampler sampler = (Uniform.Sampler)uniform;
            {
                int n7 = n = sampler.location();
                location = n;
                n7 = n = sampler.samplerIndex();
            }
            int samplerIndex = n;
            GlRenderPass.TextureViewAndSampler viewAndSampler = renderPass.samplers.get(name);
            if (viewAndSampler == null) continue;
            GlTextureView textureView = viewAndSampler.view();
            if (differentProgram || isDirty) {
                GlStateManager._glUniform1i(location, samplerIndex);
            }
            GlStateManager._activeTexture(33984 + samplerIndex);
            GlTexture texture = textureView.texture();
            if ((texture.usage() & 0x10) != 0) {
                target = 34067;
                GL11.glBindTexture((int)34067, (int)texture.id);
            } else {
                target = 3553;
                GlStateManager._bindTexture(texture.id);
            }
            GL33C.glBindSampler((int)samplerIndex, (int)viewAndSampler.sampler().getId());
            GlStateManager._texParameter(target, 33084, textureView.baseMipLevel());
            GlStateManager._texParameter(target, 33085, textureView.baseMipLevel() + textureView.mipLevels() - 1);
        }
        renderPass.dirtyUniforms.clear();
        if (renderPass.isScissorEnabled()) {
            GlStateManager._enableScissorTest();
            GlStateManager._scissorBox(renderPass.getScissorX(), renderPass.getScissorY(), renderPass.getScissorWidth(), renderPass.getScissorHeight());
            return true;
        }
        GlStateManager._disableScissorTest();
        return true;
    }

    private void applyPipelineState(RenderPipeline pipeline) {
        if (this.lastPipeline == pipeline) {
            return;
        }
        this.lastPipeline = pipeline;
        DepthStencilState depthStencilState = pipeline.getDepthStencilState();
        if (depthStencilState != null) {
            GlStateManager._enableDepthTest();
            GlStateManager._depthFunc(GlConst.toGl(depthStencilState.depthTest()));
            GlStateManager._depthMask(depthStencilState.writeDepth());
            if (depthStencilState.depthBiasConstant() != 0.0f || depthStencilState.depthBiasScaleFactor() != 0.0f) {
                GlStateManager._polygonOffset(depthStencilState.depthBiasScaleFactor(), depthStencilState.depthBiasConstant());
                GlStateManager._enablePolygonOffset();
            } else {
                GlStateManager._disablePolygonOffset();
            }
        } else {
            GlStateManager._disableDepthTest();
            GlStateManager._depthMask(false);
            GlStateManager._disablePolygonOffset();
        }
        if (pipeline.isCull()) {
            GlStateManager._enableCull();
        } else {
            GlStateManager._disableCull();
        }
        if (pipeline.getColorTargetState().blendFunction().isPresent()) {
            GlStateManager._enableBlend();
            BlendFunction blendFunction = pipeline.getColorTargetState().blendFunction().get();
            GlStateManager._blendFuncSeparate(GlConst.toGl(blendFunction.sourceColor()), GlConst.toGl(blendFunction.destColor()), GlConst.toGl(blendFunction.sourceAlpha()), GlConst.toGl(blendFunction.destAlpha()));
        } else {
            GlStateManager._disableBlend();
        }
        GlStateManager._polygonMode(1032, GlConst.toGl(pipeline.getPolygonMode()));
        GlStateManager._colorMask(pipeline.getColorTargetState().writeMask());
    }

    public void finishRenderPass() {
        this.inRenderPass = false;
        GlStateManager._glBindFramebuffer(36160, 0);
        this.device.debugLabels().popDebugGroup();
    }

    @Override
    public GpuQuery timerQueryBegin() {
        RenderSystem.assertOnRenderThread();
        if (this.activeTimerQuery != null) {
            throw new IllegalStateException("A GL_TIME_ELAPSED query is already active");
        }
        int queryId = GL32C.glGenQueries();
        GL32C.glBeginQuery((int)35007, (int)queryId);
        this.activeTimerQuery = new GlTimerQuery(queryId);
        return this.activeTimerQuery;
    }

    @Override
    public void timerQueryEnd(GpuQuery query) {
        RenderSystem.assertOnRenderThread();
        if (query != this.activeTimerQuery) {
            throw new IllegalStateException("Mismatched or duplicate GpuQuery when ending timerQuery");
        }
        GL32C.glEndQuery((int)35007);
        this.activeTimerQuery = null;
    }
}


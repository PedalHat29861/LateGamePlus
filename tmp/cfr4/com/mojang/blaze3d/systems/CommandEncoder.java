/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  com.mojang.logging.LogUtils
 *  org.jspecify.annotations.Nullable
 *  org.slf4j.Logger
 */
package com.mojang.blaze3d.systems;

import com.mojang.blaze3d.buffers.GpuBuffer;
import com.mojang.blaze3d.buffers.GpuBufferSlice;
import com.mojang.blaze3d.buffers.GpuFence;
import com.mojang.blaze3d.platform.NativeImage;
import com.mojang.blaze3d.systems.CommandEncoderBackend;
import com.mojang.blaze3d.systems.GpuDeviceBackend;
import com.mojang.blaze3d.systems.GpuQuery;
import com.mojang.blaze3d.systems.RenderPass;
import com.mojang.blaze3d.textures.GpuTexture;
import com.mojang.blaze3d.textures.GpuTextureView;
import com.mojang.logging.LogUtils;
import java.nio.ByteBuffer;
import java.util.OptionalDouble;
import java.util.OptionalInt;
import java.util.function.Supplier;
import org.jspecify.annotations.Nullable;
import org.slf4j.Logger;

public class CommandEncoder {
    private static final Logger LOGGER = LogUtils.getLogger();
    private final GpuDeviceBackend device;
    private final CommandEncoderBackend backend;

    public CommandEncoder(GpuDeviceBackend device, CommandEncoderBackend backend) {
        this.device = device;
        this.backend = backend;
    }

    public RenderPass createRenderPass(Supplier<String> label, GpuTextureView colorTexture, OptionalInt clearColor) {
        return this.createRenderPass(label, colorTexture, clearColor, null, OptionalDouble.empty());
    }

    public RenderPass createRenderPass(Supplier<String> label, GpuTextureView colorTexture, OptionalInt clearColor, @Nullable GpuTextureView depthTexture, OptionalDouble clearDepth) {
        if (this.backend.isInRenderPass()) {
            throw new IllegalStateException("Close the existing render pass before creating a new one!");
        }
        if (clearDepth.isPresent() && depthTexture == null) {
            LOGGER.warn("Depth clear value was provided but no depth texture is being used");
        }
        if (colorTexture.isClosed()) {
            throw new IllegalStateException("Color texture is closed");
        }
        if ((colorTexture.texture().usage() & 8) == 0) {
            throw new IllegalStateException("Color texture must have USAGE_RENDER_ATTACHMENT");
        }
        if (colorTexture.texture().getDepthOrLayers() > 1) {
            throw new UnsupportedOperationException("Textures with multiple depths or layers are not yet supported as an attachment");
        }
        if (depthTexture != null) {
            if (depthTexture.isClosed()) {
                throw new IllegalStateException("Depth texture is closed");
            }
            if ((depthTexture.texture().usage() & 8) == 0) {
                throw new IllegalStateException("Depth texture must have USAGE_RENDER_ATTACHMENT");
            }
            if (depthTexture.texture().getDepthOrLayers() > 1) {
                throw new UnsupportedOperationException("Textures with multiple depths or layers are not yet supported as an attachment");
            }
        }
        return new RenderPass(this.backend.createRenderPass(label, colorTexture, clearColor, depthTexture, clearDepth), this.device);
    }

    public void clearColorTexture(GpuTexture colorTexture, int clearColor) {
        if (this.backend.isInRenderPass()) {
            throw new IllegalStateException("Close the existing render pass before creating a new one!");
        }
        this.verifyColorTexture(colorTexture);
        this.backend.clearColorTexture(colorTexture, clearColor);
    }

    public void clearColorAndDepthTextures(GpuTexture colorTexture, int clearColor, GpuTexture depthTexture, double clearDepth) {
        if (this.backend.isInRenderPass()) {
            throw new IllegalStateException("Close the existing render pass before creating a new one!");
        }
        this.verifyColorTexture(colorTexture);
        this.verifyDepthTexture(depthTexture);
        this.backend.clearColorAndDepthTextures(colorTexture, clearColor, depthTexture, clearDepth);
    }

    public void clearColorAndDepthTextures(GpuTexture colorTexture, int clearColor, GpuTexture depthTexture, double clearDepth, int regionX, int regionY, int regionWidth, int regionHeight) {
        if (this.backend.isInRenderPass()) {
            throw new IllegalStateException("Close the existing render pass before creating a new one!");
        }
        this.verifyColorTexture(colorTexture);
        this.verifyDepthTexture(depthTexture);
        this.verifyRegion(colorTexture, regionX, regionY, regionWidth, regionHeight);
        this.backend.clearColorAndDepthTextures(colorTexture, clearColor, depthTexture, clearDepth, regionX, regionY, regionWidth, regionHeight);
    }

    public void clearDepthTexture(GpuTexture depthTexture, double clearDepth) {
        if (this.backend.isInRenderPass()) {
            throw new IllegalStateException("Close the existing render pass before creating a new one!");
        }
        this.verifyDepthTexture(depthTexture);
        this.backend.clearDepthTexture(depthTexture, clearDepth);
    }

    public void writeToBuffer(GpuBufferSlice destination, ByteBuffer data) {
        if (this.backend.isInRenderPass()) {
            throw new IllegalStateException("Close the existing render pass before performing additional commands");
        }
        this.backend.writeToBuffer(destination, data);
    }

    public GpuBuffer.MappedView mapBuffer(GpuBuffer buffer, boolean read, boolean write) {
        return this.mapBuffer(buffer.slice(), read, write);
    }

    public GpuBuffer.MappedView mapBuffer(GpuBufferSlice slice, boolean read, boolean write) {
        if (this.backend.isInRenderPass()) {
            throw new IllegalStateException("Close the existing render pass before performing additional commands");
        }
        GpuBuffer buffer = slice.buffer();
        if (buffer.isClosed()) {
            throw new IllegalStateException("Buffer already closed");
        }
        if (!read && !write) {
            throw new IllegalArgumentException("At least read or write must be true");
        }
        if (read && (buffer.usage() & 1) == 0) {
            throw new IllegalStateException("Buffer is not readable");
        }
        if (write && (buffer.usage() & 2) == 0) {
            throw new IllegalStateException("Buffer is not writable");
        }
        if (slice.offset() + slice.length() > buffer.size()) {
            throw new IllegalArgumentException("Cannot map more data than this buffer can hold (attempting to map " + slice.length() + " bytes at offset " + slice.offset() + " from " + buffer.size() + " size buffer)");
        }
        return this.backend.mapBuffer(slice, read, write);
    }

    public void copyToBuffer(GpuBufferSlice source, GpuBufferSlice target) {
        if (this.backend.isInRenderPass()) {
            throw new IllegalStateException("Close the existing render pass before performing additional commands");
        }
        GpuBuffer sourceBuffer = source.buffer();
        if (sourceBuffer.isClosed()) {
            throw new IllegalStateException("Source buffer already closed");
        }
        if ((sourceBuffer.usage() & 0x10) == 0) {
            throw new IllegalStateException("Source buffer needs USAGE_COPY_SRC to be a source for a copy");
        }
        GpuBuffer targetBuffer = target.buffer();
        if (targetBuffer.isClosed()) {
            throw new IllegalStateException("Target buffer already closed");
        }
        if ((targetBuffer.usage() & 8) == 0) {
            throw new IllegalStateException("Target buffer needs USAGE_COPY_DST to be a destination for a copy");
        }
        if (source.length() != target.length()) {
            throw new IllegalArgumentException("Cannot copy from slice of size " + source.length() + " to slice of size " + target.length() + ", they must be equal");
        }
        if (source.offset() + source.length() > sourceBuffer.size()) {
            throw new IllegalArgumentException("Cannot copy more data than the source buffer holds (attempting to copy " + source.length() + " bytes at offset " + source.offset() + " from " + sourceBuffer.size() + " size buffer)");
        }
        if (target.offset() + target.length() > targetBuffer.size()) {
            throw new IllegalArgumentException("Cannot copy more data than the target buffer can hold (attempting to copy " + target.length() + " bytes at offset " + target.offset() + " to " + targetBuffer.size() + " size buffer)");
        }
        this.backend.copyToBuffer(source, target);
    }

    public void writeToTexture(GpuTexture destination, NativeImage source) {
        int width = destination.getWidth(0);
        int height = destination.getHeight(0);
        if (source.getWidth() != width || source.getHeight() != height) {
            throw new IllegalArgumentException("Cannot replace texture of size " + width + "x" + height + " with image of size " + source.getWidth() + "x" + source.getHeight());
        }
        if (destination.isClosed()) {
            throw new IllegalStateException("Destination texture is closed");
        }
        if ((destination.usage() & 1) == 0) {
            throw new IllegalStateException("Color texture must have USAGE_COPY_DST to be a destination for a write");
        }
        this.writeToTexture(destination, source, 0, 0, 0, 0, width, height, 0, 0);
    }

    public void writeToTexture(GpuTexture destination, NativeImage source, int mipLevel, int depthOrLayer, int destX, int destY, int width, int height, int sourceX, int sourceY) {
        if (this.backend.isInRenderPass()) {
            throw new IllegalStateException("Close the existing render pass before performing additional commands");
        }
        if (mipLevel < 0 || mipLevel >= destination.getMipLevels()) {
            throw new IllegalArgumentException("Invalid mipLevel " + mipLevel + ", must be >= 0 and < " + destination.getMipLevels());
        }
        if (sourceX + width > source.getWidth() || sourceY + height > source.getHeight()) {
            throw new IllegalArgumentException("Copy source (" + source.getWidth() + "x" + source.getHeight() + ") is not large enough to read a rectangle of " + width + "x" + height + " from " + sourceX + "x" + sourceY);
        }
        if (destX + width > destination.getWidth(mipLevel) || destY + height > destination.getHeight(mipLevel)) {
            throw new IllegalArgumentException("Dest texture (" + width + "x" + height + ") is not large enough to write a rectangle of " + width + "x" + height + " at " + destX + "x" + destY + " (at mip level " + mipLevel + ")");
        }
        if (destination.isClosed()) {
            throw new IllegalStateException("Destination texture is closed");
        }
        if ((destination.usage() & 1) == 0) {
            throw new IllegalStateException("Color texture must have USAGE_COPY_DST to be a destination for a write");
        }
        if (depthOrLayer >= destination.getDepthOrLayers()) {
            throw new UnsupportedOperationException("Depth or layer is out of range, must be >= 0 and < " + destination.getDepthOrLayers());
        }
        this.backend.writeToTexture(destination, source, mipLevel, depthOrLayer, destX, destY, width, height, sourceX, sourceY);
    }

    public void writeToTexture(GpuTexture destination, ByteBuffer source, NativeImage.Format format, int mipLevel, int depthOrLayer, int destX, int destY, int width, int height) {
        if (this.backend.isInRenderPass()) {
            throw new IllegalStateException("Close the existing render pass before performing additional commands");
        }
        if (mipLevel < 0 || mipLevel >= destination.getMipLevels()) {
            throw new IllegalArgumentException("Invalid mipLevel, must be >= 0 and < " + destination.getMipLevels());
        }
        if (width * height * format.components() > source.remaining()) {
            throw new IllegalArgumentException("Copy would overrun the source buffer (remaining length of " + source.remaining() + ", but copy is " + width + "x" + height + " of format " + String.valueOf((Object)format) + ")");
        }
        if (destX + width > destination.getWidth(mipLevel) || destY + height > destination.getHeight(mipLevel)) {
            throw new IllegalArgumentException("Dest texture (" + destination.getWidth(mipLevel) + "x" + destination.getHeight(mipLevel) + ") is not large enough to write a rectangle of " + width + "x" + height + " at " + destX + "x" + destY);
        }
        if (destination.isClosed()) {
            throw new IllegalStateException("Destination texture is closed");
        }
        if ((destination.usage() & 1) == 0) {
            throw new IllegalStateException("Color texture must have USAGE_COPY_DST to be a destination for a write");
        }
        if (depthOrLayer >= destination.getDepthOrLayers()) {
            throw new UnsupportedOperationException("Depth or layer is out of range, must be >= 0 and < " + destination.getDepthOrLayers());
        }
        this.backend.writeToTexture(destination, source, format, mipLevel, depthOrLayer, destX, destY, width, height);
    }

    public void copyTextureToBuffer(GpuTexture source, GpuBuffer destination, long offset, Runnable callback, int mipLevel) {
        if (this.backend.isInRenderPass()) {
            throw new IllegalStateException("Close the existing render pass before performing additional commands");
        }
        this.backend.copyTextureToBuffer(source, destination, offset, callback, mipLevel);
    }

    public void copyTextureToBuffer(GpuTexture source, GpuBuffer destination, long offset, Runnable callback, int mipLevel, int x, int y, int width, int height) {
        if (this.backend.isInRenderPass()) {
            throw new IllegalStateException("Close the existing render pass before performing additional commands");
        }
        if (mipLevel < 0 || mipLevel >= source.getMipLevels()) {
            throw new IllegalArgumentException("Invalid mipLevel " + mipLevel + ", must be >= 0 and < " + source.getMipLevels());
        }
        if ((long)width * (long)height * (long)source.getFormat().pixelSize() + offset > destination.size()) {
            throw new IllegalArgumentException("Buffer of size " + destination.size() + " is not large enough to hold " + width + "x" + height + " pixels (" + source.getFormat().pixelSize() + " bytes each) starting from offset " + offset);
        }
        if ((source.usage() & 2) == 0) {
            throw new IllegalArgumentException("Texture needs USAGE_COPY_SRC to be a source for a copy");
        }
        if ((destination.usage() & 8) == 0) {
            throw new IllegalArgumentException("Buffer needs USAGE_COPY_DST to be a destination for a copy");
        }
        if (x + width > source.getWidth(mipLevel) || y + height > source.getHeight(mipLevel)) {
            throw new IllegalArgumentException("Copy source texture (" + source.getWidth(mipLevel) + "x" + source.getHeight(mipLevel) + ") is not large enough to read a rectangle of " + width + "x" + height + " from " + x + "," + y);
        }
        if (source.isClosed()) {
            throw new IllegalStateException("Source texture is closed");
        }
        if (destination.isClosed()) {
            throw new IllegalStateException("Destination buffer is closed");
        }
        if (source.getDepthOrLayers() > 1) {
            throw new UnsupportedOperationException("Textures with multiple depths or layers are not yet supported for copying");
        }
        this.backend.copyTextureToBuffer(source, destination, offset, callback, mipLevel, x, y, width, height);
    }

    public void copyTextureToTexture(GpuTexture source, GpuTexture destination, int mipLevel, int destX, int destY, int sourceX, int sourceY, int width, int height) {
        if (this.backend.isInRenderPass()) {
            throw new IllegalStateException("Close the existing render pass before performing additional commands");
        }
        if (mipLevel < 0 || mipLevel >= source.getMipLevels() || mipLevel >= destination.getMipLevels()) {
            throw new IllegalArgumentException("Invalid mipLevel " + mipLevel + ", must be >= 0 and < " + source.getMipLevels() + " and < " + destination.getMipLevels());
        }
        if (destX + width > destination.getWidth(mipLevel) || destY + height > destination.getHeight(mipLevel)) {
            throw new IllegalArgumentException("Dest texture (" + destination.getWidth(mipLevel) + "x" + destination.getHeight(mipLevel) + ") is not large enough to write a rectangle of " + width + "x" + height + " at " + destX + "x" + destY);
        }
        if (sourceX + width > source.getWidth(mipLevel) || sourceY + height > source.getHeight(mipLevel)) {
            throw new IllegalArgumentException("Source texture (" + source.getWidth(mipLevel) + "x" + source.getHeight(mipLevel) + ") is not large enough to read a rectangle of " + width + "x" + height + " at " + sourceX + "x" + sourceY);
        }
        if (source.isClosed()) {
            throw new IllegalStateException("Source texture is closed");
        }
        if (destination.isClosed()) {
            throw new IllegalStateException("Destination texture is closed");
        }
        if ((source.usage() & 2) == 0) {
            throw new IllegalArgumentException("Texture needs USAGE_COPY_SRC to be a source for a copy");
        }
        if ((destination.usage() & 1) == 0) {
            throw new IllegalArgumentException("Texture needs USAGE_COPY_DST to be a destination for a copy");
        }
        if (source.getDepthOrLayers() > 1) {
            throw new UnsupportedOperationException("Textures with multiple depths or layers are not yet supported for copying");
        }
        if (destination.getDepthOrLayers() > 1) {
            throw new UnsupportedOperationException("Textures with multiple depths or layers are not yet supported for copying");
        }
        this.backend.copyTextureToTexture(source, destination, mipLevel, destX, destY, sourceX, sourceY, width, height);
    }

    public void presentTexture(GpuTextureView textureView) {
        if (this.backend.isInRenderPass()) {
            throw new IllegalStateException("Close the existing render pass before performing additional commands");
        }
        if (!textureView.texture().getFormat().hasColorAspect()) {
            throw new IllegalStateException("Cannot present a non-color texture!");
        }
        if ((textureView.texture().usage() & 8) == 0) {
            throw new IllegalStateException("Color texture must have USAGE_RENDER_ATTACHMENT to presented to the screen");
        }
        if (textureView.texture().getDepthOrLayers() > 1) {
            throw new UnsupportedOperationException("Textures with multiple depths or layers are not yet supported for presentation");
        }
        this.backend.presentTexture(textureView);
    }

    public GpuFence createFence() {
        if (this.backend.isInRenderPass()) {
            throw new IllegalStateException("Close the existing render pass before performing additional commands");
        }
        return this.backend.createFence();
    }

    public GpuQuery timerQueryBegin() {
        return this.backend.timerQueryBegin();
    }

    public void timerQueryEnd(GpuQuery query) {
        this.backend.timerQueryEnd(query);
    }

    private void verifyColorTexture(GpuTexture colorTexture) {
        if (!colorTexture.getFormat().hasColorAspect()) {
            throw new IllegalStateException("Trying to clear a non-color texture as color");
        }
        if (colorTexture.isClosed()) {
            throw new IllegalStateException("Color texture is closed");
        }
        if ((colorTexture.usage() & 8) == 0) {
            throw new IllegalStateException("Color texture must have USAGE_RENDER_ATTACHMENT");
        }
        if ((colorTexture.usage() & 1) == 0) {
            throw new IllegalStateException("Color texture must have USAGE_COPY_DST");
        }
        if (colorTexture.getDepthOrLayers() > 1) {
            throw new UnsupportedOperationException("Clearing a texture with multiple layers or depths is not yet supported");
        }
    }

    private void verifyDepthTexture(GpuTexture depthTexture) {
        if (!depthTexture.getFormat().hasDepthAspect()) {
            throw new IllegalStateException("Trying to clear a non-depth texture as depth");
        }
        if (depthTexture.isClosed()) {
            throw new IllegalStateException("Depth texture is closed");
        }
        if ((depthTexture.usage() & 8) == 0) {
            throw new IllegalStateException("Depth texture must have USAGE_RENDER_ATTACHMENT");
        }
        if ((depthTexture.usage() & 1) == 0) {
            throw new IllegalStateException("Depth texture must have USAGE_COPY_DST");
        }
        if (depthTexture.getDepthOrLayers() > 1) {
            throw new UnsupportedOperationException("Clearing a texture with multiple layers or depths is not yet supported");
        }
    }

    private void verifyRegion(GpuTexture colorTexture, int regionX, int regionY, int regionWidth, int regionHeight) {
        if (regionX < 0 || regionX >= colorTexture.getWidth(0)) {
            throw new IllegalArgumentException("regionX should not be outside of the texture");
        }
        if (regionY < 0 || regionY >= colorTexture.getHeight(0)) {
            throw new IllegalArgumentException("regionY should not be outside of the texture");
        }
        if (regionWidth <= 0) {
            throw new IllegalArgumentException("regionWidth should be greater than 0");
        }
        if (regionX + regionWidth > colorTexture.getWidth(0)) {
            throw new IllegalArgumentException("regionWidth + regionX should be less than the texture width");
        }
        if (regionHeight <= 0) {
            throw new IllegalArgumentException("regionHeight should be greater than 0");
        }
        if (regionY + regionHeight > colorTexture.getHeight(0)) {
            throw new IllegalArgumentException("regionWidth + regionX should be less than the texture height");
        }
    }
}


/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  org.jspecify.annotations.Nullable
 *  org.lwjgl.opengl.ARBBufferStorage
 *  org.lwjgl.opengl.ARBDirectStateAccess
 *  org.lwjgl.opengl.GL30
 *  org.lwjgl.opengl.GL31
 *  org.lwjgl.opengl.GLCapabilities
 */
package com.mojang.blaze3d.opengl;

import com.mojang.blaze3d.GraphicsWorkarounds;
import com.mojang.blaze3d.buffers.GpuBuffer;
import com.mojang.blaze3d.opengl.GlConst;
import com.mojang.blaze3d.opengl.GlDevice;
import com.mojang.blaze3d.opengl.GlStateManager;
import java.nio.ByteBuffer;
import java.util.Set;
import org.jspecify.annotations.Nullable;
import org.lwjgl.opengl.ARBBufferStorage;
import org.lwjgl.opengl.ARBDirectStateAccess;
import org.lwjgl.opengl.GL30;
import org.lwjgl.opengl.GL31;
import org.lwjgl.opengl.GLCapabilities;

public abstract class DirectStateAccess {
    public static DirectStateAccess create(GLCapabilities capabilities, Set<String> enabledExtensions, GraphicsWorkarounds workarounds) {
        if (capabilities.GL_ARB_direct_state_access && GlDevice.USE_GL_ARB_direct_state_access && !workarounds.isGlOnDx12()) {
            enabledExtensions.add("GL_ARB_direct_state_access");
            return new Core();
        }
        return new Emulated();
    }

    abstract int createBuffer();

    abstract void bufferData(int var1, long var2, @GpuBuffer.Usage int var4);

    abstract void bufferData(int var1, ByteBuffer var2, @GpuBuffer.Usage int var3);

    abstract void bufferSubData(int var1, long var2, ByteBuffer var4, @GpuBuffer.Usage int var5);

    abstract void bufferStorage(int var1, long var2, @GpuBuffer.Usage int var4);

    abstract void bufferStorage(int var1, ByteBuffer var2, @GpuBuffer.Usage int var3);

    abstract @Nullable ByteBuffer mapBufferRange(int var1, long var2, long var4, int var6, @GpuBuffer.Usage int var7);

    abstract void unmapBuffer(int var1, @GpuBuffer.Usage int var2);

    abstract int createFrameBufferObject();

    abstract void bindFrameBufferTextures(int var1, int var2, int var3, int var4, int var5);

    abstract void blitFrameBuffers(int var1, int var2, int var3, int var4, int var5, int var6, int var7, int var8, int var9, int var10, int var11, int var12);

    abstract void flushMappedBufferRange(int var1, long var2, long var4, @GpuBuffer.Usage int var6);

    abstract void copyBufferSubData(int var1, int var2, long var3, long var5, long var7);

    private static class Core
    extends DirectStateAccess {
        private Core() {
        }

        @Override
        int createBuffer() {
            GlStateManager.incrementTrackedBuffers();
            return ARBDirectStateAccess.glCreateBuffers();
        }

        @Override
        void bufferData(int buffer, long size, @GpuBuffer.Usage int usage) {
            ARBDirectStateAccess.glNamedBufferData((int)buffer, (long)size, (int)GlConst.bufferUsageToGlEnum(usage));
        }

        @Override
        void bufferData(int buffer, ByteBuffer data, @GpuBuffer.Usage int usage) {
            ARBDirectStateAccess.glNamedBufferData((int)buffer, (ByteBuffer)data, (int)GlConst.bufferUsageToGlEnum(usage));
        }

        @Override
        void bufferSubData(int buffer, long offset, ByteBuffer data, @GpuBuffer.Usage int usage) {
            ARBDirectStateAccess.glNamedBufferSubData((int)buffer, (long)offset, (ByteBuffer)data);
        }

        @Override
        void bufferStorage(int buffer, long size, @GpuBuffer.Usage int usage) {
            ARBDirectStateAccess.glNamedBufferStorage((int)buffer, (long)size, (int)GlConst.bufferUsageToGlFlag(usage));
        }

        @Override
        void bufferStorage(int buffer, ByteBuffer data, @GpuBuffer.Usage int usage) {
            ARBDirectStateAccess.glNamedBufferStorage((int)buffer, (ByteBuffer)data, (int)GlConst.bufferUsageToGlFlag(usage));
        }

        @Override
        @Nullable ByteBuffer mapBufferRange(int buffer, long offset, long length, int flags, @GpuBuffer.Usage int usage) {
            return ARBDirectStateAccess.glMapNamedBufferRange((int)buffer, (long)offset, (long)length, (int)flags);
        }

        @Override
        void unmapBuffer(int buffer, int usage) {
            ARBDirectStateAccess.glUnmapNamedBuffer((int)buffer);
        }

        @Override
        public int createFrameBufferObject() {
            return ARBDirectStateAccess.glCreateFramebuffers();
        }

        @Override
        public void bindFrameBufferTextures(int fbo, int color0, int depth, int mipLevel, @GpuBuffer.Usage int bindSlot) {
            ARBDirectStateAccess.glNamedFramebufferTexture((int)fbo, (int)36064, (int)color0, (int)mipLevel);
            ARBDirectStateAccess.glNamedFramebufferTexture((int)fbo, (int)36096, (int)depth, (int)mipLevel);
            if (bindSlot != 0) {
                GlStateManager._glBindFramebuffer(bindSlot, fbo);
            }
        }

        @Override
        public void blitFrameBuffers(int source, int dest, int srcX0, int srcY0, int srcX1, int srcY1, int dstX0, int dstY0, int dstX1, int dstY1, int mask, int filter) {
            ARBDirectStateAccess.glBlitNamedFramebuffer((int)source, (int)dest, (int)srcX0, (int)srcY0, (int)srcX1, (int)srcY1, (int)dstX0, (int)dstY0, (int)dstX1, (int)dstY1, (int)mask, (int)filter);
        }

        @Override
        void flushMappedBufferRange(int handle, long offset, long length, @GpuBuffer.Usage int usage) {
            ARBDirectStateAccess.glFlushMappedNamedBufferRange((int)handle, (long)offset, (long)length);
        }

        @Override
        void copyBufferSubData(int source, int target, long sourceOffset, long targetOffset, long length) {
            ARBDirectStateAccess.glCopyNamedBufferSubData((int)source, (int)target, (long)sourceOffset, (long)targetOffset, (long)length);
        }
    }

    private static class Emulated
    extends DirectStateAccess {
        private Emulated() {
        }

        private int selectBufferBindTarget(@GpuBuffer.Usage int usage) {
            if ((usage & 0x20) != 0) {
                return 34962;
            }
            if ((usage & 0x40) != 0) {
                return 34963;
            }
            if ((usage & 0x80) != 0) {
                return 35345;
            }
            return 36663;
        }

        @Override
        int createBuffer() {
            return GlStateManager._glGenBuffers();
        }

        @Override
        void bufferData(int buffer, long size, @GpuBuffer.Usage int usage) {
            int target = this.selectBufferBindTarget(usage);
            GlStateManager._glBindBuffer(target, buffer);
            GlStateManager._glBufferData(target, size, GlConst.bufferUsageToGlEnum(usage));
            GlStateManager._glBindBuffer(target, 0);
        }

        @Override
        void bufferData(int buffer, ByteBuffer data, @GpuBuffer.Usage int usage) {
            int target = this.selectBufferBindTarget(usage);
            GlStateManager._glBindBuffer(target, buffer);
            GlStateManager._glBufferData(target, data, GlConst.bufferUsageToGlEnum(usage));
            GlStateManager._glBindBuffer(target, 0);
        }

        @Override
        void bufferSubData(int buffer, long offset, ByteBuffer data, @GpuBuffer.Usage int usage) {
            int target = this.selectBufferBindTarget(usage);
            GlStateManager._glBindBuffer(target, buffer);
            GlStateManager._glBufferSubData(target, offset, data);
            GlStateManager._glBindBuffer(target, 0);
        }

        @Override
        void bufferStorage(int buffer, long size, @GpuBuffer.Usage int usage) {
            int target = this.selectBufferBindTarget(usage);
            GlStateManager._glBindBuffer(target, buffer);
            ARBBufferStorage.glBufferStorage((int)target, (long)size, (int)GlConst.bufferUsageToGlFlag(usage));
            GlStateManager._glBindBuffer(target, 0);
        }

        @Override
        void bufferStorage(int buffer, ByteBuffer data, @GpuBuffer.Usage int usage) {
            int target = this.selectBufferBindTarget(usage);
            GlStateManager._glBindBuffer(target, buffer);
            ARBBufferStorage.glBufferStorage((int)target, (ByteBuffer)data, (int)GlConst.bufferUsageToGlFlag(usage));
            GlStateManager._glBindBuffer(target, 0);
        }

        @Override
        @Nullable ByteBuffer mapBufferRange(int buffer, long offset, long length, int access, @GpuBuffer.Usage int usage) {
            int target = this.selectBufferBindTarget(usage);
            GlStateManager._glBindBuffer(target, buffer);
            ByteBuffer byteBuffer = GlStateManager._glMapBufferRange(target, offset, length, access);
            GlStateManager._glBindBuffer(target, 0);
            return byteBuffer;
        }

        @Override
        void unmapBuffer(int buffer, @GpuBuffer.Usage int usage) {
            int target = this.selectBufferBindTarget(usage);
            GlStateManager._glBindBuffer(target, buffer);
            GlStateManager._glUnmapBuffer(target);
            GlStateManager._glBindBuffer(target, 0);
        }

        @Override
        void flushMappedBufferRange(int buffer, long offset, long length, @GpuBuffer.Usage int usage) {
            int target = this.selectBufferBindTarget(usage);
            GlStateManager._glBindBuffer(target, buffer);
            GL30.glFlushMappedBufferRange((int)target, (long)offset, (long)length);
            GlStateManager._glBindBuffer(target, 0);
        }

        @Override
        void copyBufferSubData(int source, int target, long sourceOffset, long targetOffset, long length) {
            GlStateManager._glBindBuffer(36662, source);
            GlStateManager._glBindBuffer(36663, target);
            GL31.glCopyBufferSubData((int)36662, (int)36663, (long)sourceOffset, (long)targetOffset, (long)length);
            GlStateManager._glBindBuffer(36662, 0);
            GlStateManager._glBindBuffer(36663, 0);
        }

        @Override
        public int createFrameBufferObject() {
            return GlStateManager.glGenFramebuffers();
        }

        @Override
        public void bindFrameBufferTextures(int fbo, int color0, int depth, int mipLevel, int bindSlot) {
            int tempBindSlot = bindSlot == 0 ? 36009 : bindSlot;
            int oldFbo = GlStateManager.getFrameBuffer(tempBindSlot);
            GlStateManager._glBindFramebuffer(tempBindSlot, fbo);
            GlStateManager._glFramebufferTexture2D(tempBindSlot, 36064, 3553, color0, mipLevel);
            GlStateManager._glFramebufferTexture2D(tempBindSlot, 36096, 3553, depth, mipLevel);
            if (bindSlot == 0) {
                GlStateManager._glBindFramebuffer(tempBindSlot, oldFbo);
            }
        }

        @Override
        public void blitFrameBuffers(int source, int dest, int srcX0, int srcY0, int srcX1, int srcY1, int dstX0, int dstY0, int dstX1, int dstY1, int mask, int filter) {
            int oldRead = GlStateManager.getFrameBuffer(36008);
            int oldDraw = GlStateManager.getFrameBuffer(36009);
            GlStateManager._glBindFramebuffer(36008, source);
            GlStateManager._glBindFramebuffer(36009, dest);
            GlStateManager._glBlitFrameBuffer(srcX0, srcY0, srcX1, srcY1, dstX0, dstY0, dstX1, dstY1, mask, filter);
            GlStateManager._glBindFramebuffer(36008, oldRead);
            GlStateManager._glBindFramebuffer(36009, oldDraw);
        }
    }
}


/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  org.jspecify.annotations.Nullable
 *  org.lwjgl.opengl.GLCapabilities
 *  org.lwjgl.system.MemoryUtil
 */
package com.mojang.blaze3d.opengl;

import com.mojang.blaze3d.buffers.GpuBuffer;
import com.mojang.blaze3d.opengl.DirectStateAccess;
import com.mojang.blaze3d.opengl.GlBuffer;
import com.mojang.blaze3d.opengl.GlDevice;
import com.mojang.blaze3d.opengl.GlStateManager;
import java.nio.ByteBuffer;
import java.util.Set;
import java.util.function.Supplier;
import org.jspecify.annotations.Nullable;
import org.lwjgl.opengl.GLCapabilities;
import org.lwjgl.system.MemoryUtil;

public abstract class BufferStorage {
    public static BufferStorage create(GLCapabilities capabilities, Set<String> enabledExtensions) {
        if (capabilities.GL_ARB_buffer_storage && GlDevice.USE_GL_ARB_buffer_storage) {
            enabledExtensions.add("GL_ARB_buffer_storage");
            return new Immutable();
        }
        return new Mutable();
    }

    public abstract GlBuffer createBuffer(DirectStateAccess var1, @Nullable Supplier<String> var2, @GpuBuffer.Usage int var3, long var4);

    public abstract GlBuffer createBuffer(DirectStateAccess var1, @Nullable Supplier<String> var2, @GpuBuffer.Usage int var3, ByteBuffer var4);

    public abstract GlBuffer.GlMappedView mapBuffer(DirectStateAccess var1, GlBuffer var2, long var3, long var5, int var7);

    private static class Immutable
    extends BufferStorage {
        private Immutable() {
        }

        @Override
        public GlBuffer createBuffer(DirectStateAccess dsa, @Nullable Supplier<String> label, @GpuBuffer.Usage int usage, long size) {
            int buffer = dsa.createBuffer();
            dsa.bufferStorage(buffer, size, usage);
            ByteBuffer persistentBuffer = this.tryMapBufferPersistent(dsa, usage, buffer, size);
            return new GlBuffer(label, dsa, usage, size, buffer, persistentBuffer);
        }

        @Override
        public GlBuffer createBuffer(DirectStateAccess dsa, @Nullable Supplier<String> label, @GpuBuffer.Usage int usage, ByteBuffer data) {
            int buffer = dsa.createBuffer();
            int size = data.remaining();
            dsa.bufferStorage(buffer, data, usage);
            ByteBuffer persistentBuffer = this.tryMapBufferPersistent(dsa, usage, buffer, size);
            return new GlBuffer(label, dsa, usage, size, buffer, persistentBuffer);
        }

        private @Nullable ByteBuffer tryMapBufferPersistent(DirectStateAccess dsa, @GpuBuffer.Usage int usage, int buffer, long size) {
            ByteBuffer persistentBuffer;
            int mapFlags = 0;
            if ((usage & 1) != 0) {
                mapFlags |= 1;
            }
            if ((usage & 2) != 0) {
                mapFlags |= 0x12;
            }
            if (mapFlags != 0) {
                GlStateManager.clearGlErrors();
                persistentBuffer = dsa.mapBufferRange(buffer, 0L, size, mapFlags | 0x40, usage);
                if (persistentBuffer == null) {
                    throw new IllegalStateException("Can't persistently map buffer, opengl error " + GlStateManager._getError());
                }
            } else {
                persistentBuffer = null;
            }
            return persistentBuffer;
        }

        @Override
        public GlBuffer.GlMappedView mapBuffer(DirectStateAccess dsa, GlBuffer buffer, long offset, long length, int flags) {
            if (buffer.persistentBuffer == null) {
                throw new IllegalStateException("Somehow trying to map an unmappable buffer");
            }
            if (offset > Integer.MAX_VALUE || length > Integer.MAX_VALUE) {
                throw new IllegalArgumentException("Mapping buffers larger than 2GB is not supported");
            }
            if (offset < 0L || length < 0L) {
                throw new IllegalArgumentException("Offset or length must be positive integer values");
            }
            return new GlBuffer.GlMappedView(() -> {
                if ((flags & 2) != 0) {
                    dsa.flushMappedBufferRange(buffer.handle, offset, length, buffer.usage());
                }
            }, buffer, MemoryUtil.memSlice((ByteBuffer)buffer.persistentBuffer, (int)((int)offset), (int)((int)length)));
        }
    }

    private static class Mutable
    extends BufferStorage {
        private Mutable() {
        }

        @Override
        public GlBuffer createBuffer(DirectStateAccess dsa, @Nullable Supplier<String> label, @GpuBuffer.Usage int usage, long size) {
            int buffer = dsa.createBuffer();
            dsa.bufferData(buffer, size, usage);
            return new GlBuffer(label, dsa, usage, size, buffer, null);
        }

        @Override
        public GlBuffer createBuffer(DirectStateAccess dsa, @Nullable Supplier<String> label, @GpuBuffer.Usage int usage, ByteBuffer data) {
            int buffer = dsa.createBuffer();
            int size = data.remaining();
            dsa.bufferData(buffer, data, usage);
            return new GlBuffer(label, dsa, usage, size, buffer, null);
        }

        @Override
        public GlBuffer.GlMappedView mapBuffer(DirectStateAccess dsa, GlBuffer buffer, long offset, long length, int flags) {
            GlStateManager.clearGlErrors();
            ByteBuffer byteBuffer = dsa.mapBufferRange(buffer.handle, offset, length, flags, buffer.usage());
            if (byteBuffer == null) {
                throw new IllegalStateException("Can't map buffer, opengl error " + GlStateManager._getError());
            }
            return new GlBuffer.GlMappedView(() -> dsa.unmapBuffer(buffer.handle, buffer.usage()), buffer, byteBuffer);
        }
    }
}


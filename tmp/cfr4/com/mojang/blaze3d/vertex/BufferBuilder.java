/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  net.minecraft.util.ARGB
 *  net.minecraft.util.Mth
 *  org.jspecify.annotations.Nullable
 *  org.lwjgl.system.MemoryUtil
 */
package com.mojang.blaze3d.vertex;

import com.mojang.blaze3d.vertex.ByteBufferBuilder;
import com.mojang.blaze3d.vertex.DefaultVertexFormat;
import com.mojang.blaze3d.vertex.MeshData;
import com.mojang.blaze3d.vertex.VertexConsumer;
import com.mojang.blaze3d.vertex.VertexFormat;
import com.mojang.blaze3d.vertex.VertexFormatElement;
import java.nio.ByteOrder;
import java.util.stream.Collectors;
import net.minecraft.util.ARGB;
import net.minecraft.util.Mth;
import org.jspecify.annotations.Nullable;
import org.lwjgl.system.MemoryUtil;

public class BufferBuilder
implements VertexConsumer {
    private static final int MAX_VERTEX_COUNT = 0xFFFFFF;
    private static final long NOT_BUILDING = -1L;
    private static final long UNKNOWN_ELEMENT = -1L;
    private static final boolean IS_LITTLE_ENDIAN = ByteOrder.nativeOrder() == ByteOrder.LITTLE_ENDIAN;
    private final ByteBufferBuilder buffer;
    private long vertexPointer = -1L;
    private int vertices;
    private final VertexFormat format;
    private final VertexFormat.Mode mode;
    private final boolean fastFormat;
    private final boolean fullFormat;
    private final int vertexSize;
    private final int initialElementsToFill;
    private final int[] offsetsByElement;
    private int elementsToFill;
    private boolean building = true;

    public BufferBuilder(ByteBufferBuilder buffer, VertexFormat.Mode mode, VertexFormat format) {
        if (!format.contains(VertexFormatElement.POSITION)) {
            throw new IllegalArgumentException("Cannot build mesh with no position element");
        }
        this.buffer = buffer;
        this.mode = mode;
        this.format = format;
        this.vertexSize = format.getVertexSize();
        this.initialElementsToFill = format.getElementsMask() & ~VertexFormatElement.POSITION.mask();
        this.offsetsByElement = format.getOffsetsByElement();
        boolean isFullFormat = format == DefaultVertexFormat.ENTITY;
        boolean isBlockFormat = format == DefaultVertexFormat.BLOCK;
        this.fastFormat = isFullFormat || isBlockFormat;
        this.fullFormat = isFullFormat;
    }

    public @Nullable MeshData build() {
        this.ensureBuilding();
        this.endLastVertex();
        MeshData mesh = this.storeMesh();
        this.building = false;
        this.vertexPointer = -1L;
        return mesh;
    }

    public MeshData buildOrThrow() {
        MeshData buffer = this.build();
        if (buffer == null) {
            throw new IllegalStateException("BufferBuilder was empty");
        }
        return buffer;
    }

    private void ensureBuilding() {
        if (!this.building) {
            throw new IllegalStateException("Not building!");
        }
    }

    private @Nullable MeshData storeMesh() {
        if (this.vertices == 0) {
            return null;
        }
        ByteBufferBuilder.Result vertexBuffer = this.buffer.build();
        if (vertexBuffer == null) {
            return null;
        }
        int indices = this.mode.indexCount(this.vertices);
        VertexFormat.IndexType indexType = VertexFormat.IndexType.least(this.vertices);
        return new MeshData(vertexBuffer, new MeshData.DrawState(this.format, this.vertices, indices, this.mode, indexType));
    }

    private long beginVertex() {
        long pointer;
        this.ensureBuilding();
        this.endLastVertex();
        if (this.vertices >= 0xFFFFFF) {
            throw new IllegalStateException("Trying to write too many vertices (>16777215) into BufferBuilder");
        }
        ++this.vertices;
        this.vertexPointer = pointer = this.buffer.reserve(this.vertexSize);
        return pointer;
    }

    private long beginElement(VertexFormatElement element) {
        int oldElements = this.elementsToFill;
        int newElements = oldElements & ~element.mask();
        if (newElements == oldElements) {
            return -1L;
        }
        this.elementsToFill = newElements;
        long vertexPointer = this.vertexPointer;
        if (vertexPointer == -1L) {
            throw new IllegalArgumentException("Not currently building vertex");
        }
        return vertexPointer + (long)this.offsetsByElement[element.id()];
    }

    private void endLastVertex() {
        if (this.vertices == 0) {
            return;
        }
        if (this.elementsToFill != 0) {
            String missingElements = VertexFormatElement.elementsFromMask(this.elementsToFill).map(this.format::getElementName).collect(Collectors.joining(", "));
            throw new IllegalStateException("Missing elements in vertex: " + missingElements);
        }
        if (this.mode == VertexFormat.Mode.LINES) {
            long pointer = this.buffer.reserve(this.vertexSize);
            MemoryUtil.memCopy((long)(pointer - (long)this.vertexSize), (long)pointer, (long)this.vertexSize);
            ++this.vertices;
        }
    }

    private static void putRgba(long pointer, int argb) {
        int abgr = ARGB.toABGR((int)argb);
        MemoryUtil.memPutInt((long)pointer, (int)(IS_LITTLE_ENDIAN ? abgr : Integer.reverseBytes(abgr)));
    }

    private static void putPackedUv(long pointer, int packedUv) {
        if (IS_LITTLE_ENDIAN) {
            MemoryUtil.memPutInt((long)pointer, (int)packedUv);
        } else {
            MemoryUtil.memPutShort((long)pointer, (short)((short)(packedUv & 0xFFFF)));
            MemoryUtil.memPutShort((long)(pointer + 2L), (short)((short)(packedUv >> 16 & 0xFFFF)));
        }
    }

    @Override
    public VertexConsumer addVertex(float x, float y, float z) {
        long pointer = this.beginVertex() + (long)this.offsetsByElement[VertexFormatElement.POSITION.id()];
        this.elementsToFill = this.initialElementsToFill;
        MemoryUtil.memPutFloat((long)pointer, (float)x);
        MemoryUtil.memPutFloat((long)(pointer + 4L), (float)y);
        MemoryUtil.memPutFloat((long)(pointer + 8L), (float)z);
        return this;
    }

    @Override
    public VertexConsumer setColor(int r, int g, int b, int a) {
        long pointer = this.beginElement(VertexFormatElement.COLOR);
        if (pointer != -1L) {
            MemoryUtil.memPutByte((long)pointer, (byte)((byte)r));
            MemoryUtil.memPutByte((long)(pointer + 1L), (byte)((byte)g));
            MemoryUtil.memPutByte((long)(pointer + 2L), (byte)((byte)b));
            MemoryUtil.memPutByte((long)(pointer + 3L), (byte)((byte)a));
        }
        return this;
    }

    @Override
    public VertexConsumer setColor(int color) {
        long pointer = this.beginElement(VertexFormatElement.COLOR);
        if (pointer != -1L) {
            BufferBuilder.putRgba(pointer, color);
        }
        return this;
    }

    @Override
    public VertexConsumer setUv(float u, float v) {
        long pointer = this.beginElement(VertexFormatElement.UV0);
        if (pointer != -1L) {
            MemoryUtil.memPutFloat((long)pointer, (float)u);
            MemoryUtil.memPutFloat((long)(pointer + 4L), (float)v);
        }
        return this;
    }

    @Override
    public VertexConsumer setUv1(int u, int v) {
        return this.uvShort((short)u, (short)v, VertexFormatElement.UV1);
    }

    @Override
    public VertexConsumer setOverlay(int packedOverlayCoords) {
        long pointer = this.beginElement(VertexFormatElement.UV1);
        if (pointer != -1L) {
            BufferBuilder.putPackedUv(pointer, packedOverlayCoords);
        }
        return this;
    }

    @Override
    public VertexConsumer setUv2(int u, int v) {
        return this.uvShort((short)u, (short)v, VertexFormatElement.UV2);
    }

    @Override
    public VertexConsumer setLight(int packedLightCoords) {
        long pointer = this.beginElement(VertexFormatElement.UV2);
        if (pointer != -1L) {
            BufferBuilder.putPackedUv(pointer, packedLightCoords);
        }
        return this;
    }

    private VertexConsumer uvShort(short u, short v, VertexFormatElement element) {
        long pointer = this.beginElement(element);
        if (pointer != -1L) {
            MemoryUtil.memPutShort((long)pointer, (short)u);
            MemoryUtil.memPutShort((long)(pointer + 2L), (short)v);
        }
        return this;
    }

    @Override
    public VertexConsumer setNormal(float x, float y, float z) {
        long pointer = this.beginElement(VertexFormatElement.NORMAL);
        if (pointer != -1L) {
            MemoryUtil.memPutByte((long)pointer, (byte)BufferBuilder.normalIntValue(x));
            MemoryUtil.memPutByte((long)(pointer + 1L), (byte)BufferBuilder.normalIntValue(y));
            MemoryUtil.memPutByte((long)(pointer + 2L), (byte)BufferBuilder.normalIntValue(z));
        }
        return this;
    }

    @Override
    public VertexConsumer setLineWidth(float width) {
        long pointer = this.beginElement(VertexFormatElement.LINE_WIDTH);
        if (pointer != -1L) {
            MemoryUtil.memPutFloat((long)pointer, (float)width);
        }
        return this;
    }

    private static byte normalIntValue(float c) {
        return (byte)((int)(Mth.clamp((float)c, (float)-1.0f, (float)1.0f) * 127.0f) & 0xFF);
    }

    @Override
    public void addVertex(float x, float y, float z, int color, float u, float v, int overlayCoords, int lightCoords, float nx, float ny, float nz) {
        if (this.fastFormat) {
            long lightStart;
            long pointer = this.beginVertex();
            MemoryUtil.memPutFloat((long)(pointer + 0L), (float)x);
            MemoryUtil.memPutFloat((long)(pointer + 4L), (float)y);
            MemoryUtil.memPutFloat((long)(pointer + 8L), (float)z);
            BufferBuilder.putRgba(pointer + 12L, color);
            MemoryUtil.memPutFloat((long)(pointer + 16L), (float)u);
            MemoryUtil.memPutFloat((long)(pointer + 20L), (float)v);
            if (this.fullFormat) {
                BufferBuilder.putPackedUv(pointer + 24L, overlayCoords);
                lightStart = pointer + 28L;
            } else {
                lightStart = pointer + 24L;
            }
            BufferBuilder.putPackedUv(lightStart + 0L, lightCoords);
            if (this.fullFormat) {
                MemoryUtil.memPutByte((long)(lightStart + 4L), (byte)BufferBuilder.normalIntValue(nx));
                MemoryUtil.memPutByte((long)(lightStart + 5L), (byte)BufferBuilder.normalIntValue(ny));
                MemoryUtil.memPutByte((long)(lightStart + 6L), (byte)BufferBuilder.normalIntValue(nz));
            }
            return;
        }
        VertexConsumer.super.addVertex(x, y, z, color, u, v, overlayCoords, lightCoords, nx, ny, nz);
    }
}


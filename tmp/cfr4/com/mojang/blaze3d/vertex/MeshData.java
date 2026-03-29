/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  it.unimi.dsi.fastutil.ints.IntConsumer
 *  org.apache.commons.lang3.mutable.MutableLong
 *  org.jspecify.annotations.Nullable
 *  org.lwjgl.system.MemoryUtil
 */
package com.mojang.blaze3d.vertex;

import com.mojang.blaze3d.vertex.ByteBufferBuilder;
import com.mojang.blaze3d.vertex.CompactVectorArray;
import com.mojang.blaze3d.vertex.VertexFormat;
import com.mojang.blaze3d.vertex.VertexFormatElement;
import com.mojang.blaze3d.vertex.VertexSorting;
import it.unimi.dsi.fastutil.ints.IntConsumer;
import java.nio.ByteBuffer;
import java.nio.FloatBuffer;
import org.apache.commons.lang3.mutable.MutableLong;
import org.jspecify.annotations.Nullable;
import org.lwjgl.system.MemoryUtil;

public class MeshData
implements AutoCloseable {
    private final ByteBufferBuilder.Result vertexBuffer;
    private @Nullable ByteBufferBuilder.Result indexBuffer;
    private final DrawState drawState;

    public MeshData(ByteBufferBuilder.Result vertexBuffer, DrawState drawState) {
        this.vertexBuffer = vertexBuffer;
        this.drawState = drawState;
    }

    private static CompactVectorArray unpackQuadCentroids(ByteBuffer vertexBuffer, int vertices, VertexFormat format) {
        int positionOffset = format.getOffset(VertexFormatElement.POSITION);
        if (positionOffset == -1) {
            throw new IllegalArgumentException("Cannot identify quad centers with no position element");
        }
        FloatBuffer floatBuffer = vertexBuffer.asFloatBuffer();
        int vertexStride = format.getVertexSize() / 4;
        int quadStride = vertexStride * 4;
        int quads = vertices / 4;
        CompactVectorArray sortingPoints = new CompactVectorArray(quads);
        for (int i = 0; i < quads; ++i) {
            int firstPosOffset = i * quadStride + positionOffset;
            int secondPosOffset = firstPosOffset + vertexStride * 2;
            float x0 = floatBuffer.get(firstPosOffset + 0);
            float y0 = floatBuffer.get(firstPosOffset + 1);
            float z0 = floatBuffer.get(firstPosOffset + 2);
            float x1 = floatBuffer.get(secondPosOffset + 0);
            float y1 = floatBuffer.get(secondPosOffset + 1);
            float z1 = floatBuffer.get(secondPosOffset + 2);
            float xMid = (x0 + x1) / 2.0f;
            float yMid = (y0 + y1) / 2.0f;
            float zMid = (z0 + z1) / 2.0f;
            sortingPoints.set(i, xMid, yMid, zMid);
        }
        return sortingPoints;
    }

    public ByteBuffer vertexBuffer() {
        return this.vertexBuffer.byteBuffer();
    }

    public @Nullable ByteBuffer indexBuffer() {
        return this.indexBuffer != null ? this.indexBuffer.byteBuffer() : null;
    }

    public DrawState drawState() {
        return this.drawState;
    }

    public @Nullable SortState sortQuads(ByteBufferBuilder indexBufferTarget, VertexSorting sorting) {
        if (this.drawState.mode() != VertexFormat.Mode.QUADS) {
            return null;
        }
        CompactVectorArray centroids = MeshData.unpackQuadCentroids(this.vertexBuffer.byteBuffer(), this.drawState.vertexCount(), this.drawState.format());
        SortState sortState = new SortState(centroids, this.drawState.indexType());
        this.indexBuffer = sortState.buildSortedIndexBuffer(indexBufferTarget, sorting);
        return sortState;
    }

    @Override
    public void close() {
        this.vertexBuffer.close();
        if (this.indexBuffer != null) {
            this.indexBuffer.close();
        }
    }

    public record DrawState(VertexFormat format, int vertexCount, int indexCount, VertexFormat.Mode mode, VertexFormat.IndexType indexType) {
    }

    public record SortState(CompactVectorArray centroids, VertexFormat.IndexType indexType) {
        public @Nullable ByteBufferBuilder.Result buildSortedIndexBuffer(ByteBufferBuilder target, VertexSorting sorting) {
            int[] startIndices = sorting.sort(this.centroids);
            long pointer = target.reserve(startIndices.length * 6 * this.indexType.bytes);
            IntConsumer indexWriter = this.indexWriter(pointer, this.indexType);
            for (int startIndex : startIndices) {
                indexWriter.accept(startIndex * 4 + 0);
                indexWriter.accept(startIndex * 4 + 1);
                indexWriter.accept(startIndex * 4 + 2);
                indexWriter.accept(startIndex * 4 + 2);
                indexWriter.accept(startIndex * 4 + 3);
                indexWriter.accept(startIndex * 4 + 0);
            }
            return target.build();
        }

        private IntConsumer indexWriter(long pointer, VertexFormat.IndexType indexType) {
            MutableLong nextIndex = new MutableLong(pointer);
            return switch (indexType) {
                default -> throw new MatchException(null, null);
                case VertexFormat.IndexType.SHORT -> value -> MemoryUtil.memPutShort((long)nextIndex.getAndAdd(2L), (short)((short)value));
                case VertexFormat.IndexType.INT -> value -> MemoryUtil.memPutInt((long)nextIndex.getAndAdd(4L), (int)value);
            };
        }
    }
}


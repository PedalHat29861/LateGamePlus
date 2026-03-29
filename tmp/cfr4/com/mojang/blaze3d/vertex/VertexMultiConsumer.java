/*
 * Decompiled with CFR 0.152.
 */
package com.mojang.blaze3d.vertex;

import com.mojang.blaze3d.vertex.VertexConsumer;
import java.util.function.Consumer;

public class VertexMultiConsumer {
    public static VertexConsumer create() {
        throw new IllegalArgumentException();
    }

    public static VertexConsumer create(VertexConsumer consumer) {
        return consumer;
    }

    public static VertexConsumer create(VertexConsumer first, VertexConsumer second) {
        return new Double(first, second);
    }

    public static VertexConsumer create(VertexConsumer ... consumers) {
        return new Multiple(consumers);
    }

    private static class Double
    implements VertexConsumer {
        private final VertexConsumer first;
        private final VertexConsumer second;

        public Double(VertexConsumer first, VertexConsumer second) {
            if (first == second) {
                throw new IllegalArgumentException("Duplicate delegates");
            }
            this.first = first;
            this.second = second;
        }

        @Override
        public VertexConsumer addVertex(float x, float y, float z) {
            this.first.addVertex(x, y, z);
            this.second.addVertex(x, y, z);
            return this;
        }

        @Override
        public VertexConsumer setColor(int r, int g, int b, int a) {
            this.first.setColor(r, g, b, a);
            this.second.setColor(r, g, b, a);
            return this;
        }

        @Override
        public VertexConsumer setColor(int color) {
            this.first.setColor(color);
            this.second.setColor(color);
            return this;
        }

        @Override
        public VertexConsumer setUv(float u, float v) {
            this.first.setUv(u, v);
            this.second.setUv(u, v);
            return this;
        }

        @Override
        public VertexConsumer setUv1(int u, int v) {
            this.first.setUv1(u, v);
            this.second.setUv1(u, v);
            return this;
        }

        @Override
        public VertexConsumer setUv2(int u, int v) {
            this.first.setUv2(u, v);
            this.second.setUv2(u, v);
            return this;
        }

        @Override
        public VertexConsumer setNormal(float x, float y, float z) {
            this.first.setNormal(x, y, z);
            this.second.setNormal(x, y, z);
            return this;
        }

        @Override
        public VertexConsumer setLineWidth(float width) {
            this.first.setLineWidth(width);
            this.second.setLineWidth(width);
            return this;
        }

        @Override
        public void addVertex(float x, float y, float z, int color, float u, float v, int overlayCoords, int lightCoords, float nx, float ny, float nz) {
            this.first.addVertex(x, y, z, color, u, v, overlayCoords, lightCoords, nx, ny, nz);
            this.second.addVertex(x, y, z, color, u, v, overlayCoords, lightCoords, nx, ny, nz);
        }
    }

    private record Multiple(VertexConsumer[] delegates) implements VertexConsumer
    {
        private Multiple {
            for (int i = 0; i < delegates.length; ++i) {
                for (int j = i + 1; j < delegates.length; ++j) {
                    if (delegates[i] != delegates[j]) continue;
                    throw new IllegalArgumentException("Duplicate delegates");
                }
            }
        }

        private void forEach(Consumer<VertexConsumer> out) {
            for (VertexConsumer delegate : this.delegates) {
                out.accept(delegate);
            }
        }

        @Override
        public VertexConsumer addVertex(float x, float y, float z) {
            this.forEach(d -> d.addVertex(x, y, z));
            return this;
        }

        @Override
        public VertexConsumer setColor(int r, int g, int b, int a) {
            this.forEach(d -> d.setColor(r, g, b, a));
            return this;
        }

        @Override
        public VertexConsumer setColor(int color) {
            this.forEach(d -> d.setColor(color));
            return this;
        }

        @Override
        public VertexConsumer setUv(float u, float v) {
            this.forEach(d -> d.setUv(u, v));
            return this;
        }

        @Override
        public VertexConsumer setUv1(int u, int v) {
            this.forEach(d -> d.setUv1(u, v));
            return this;
        }

        @Override
        public VertexConsumer setUv2(int u, int v) {
            this.forEach(d -> d.setUv2(u, v));
            return this;
        }

        @Override
        public VertexConsumer setNormal(float x, float y, float z) {
            this.forEach(d -> d.setNormal(x, y, z));
            return this;
        }

        @Override
        public VertexConsumer setLineWidth(float width) {
            this.forEach(d -> d.setLineWidth(width));
            return this;
        }

        @Override
        public void addVertex(float x, float y, float z, int color, float u, float v, int overlayCoords, int lightCoords, float nx, float ny, float nz) {
            this.forEach(d -> d.addVertex(x, y, z, color, u, v, overlayCoords, lightCoords, nx, ny, nz));
        }
    }
}


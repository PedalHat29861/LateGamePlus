/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  org.jspecify.annotations.Nullable
 *  org.lwjgl.opengl.ARBVertexAttribBinding
 *  org.lwjgl.opengl.GLCapabilities
 */
package com.mojang.blaze3d.opengl;

import com.mojang.blaze3d.opengl.GlBuffer;
import com.mojang.blaze3d.opengl.GlConst;
import com.mojang.blaze3d.opengl.GlDebugLabel;
import com.mojang.blaze3d.opengl.GlDevice;
import com.mojang.blaze3d.opengl.GlStateManager;
import com.mojang.blaze3d.vertex.VertexFormat;
import com.mojang.blaze3d.vertex.VertexFormatElement;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import org.jspecify.annotations.Nullable;
import org.lwjgl.opengl.ARBVertexAttribBinding;
import org.lwjgl.opengl.GLCapabilities;

public abstract class VertexArrayCache {
    public static VertexArrayCache create(GLCapabilities capabilities, GlDebugLabel debugLabels, Set<String> enabledExtensions) {
        if (capabilities.GL_ARB_vertex_attrib_binding && GlDevice.USE_GL_ARB_vertex_attrib_binding) {
            enabledExtensions.add("GL_ARB_vertex_attrib_binding");
            return new Separate(debugLabels);
        }
        return new Emulated(debugLabels);
    }

    public abstract void bindVertexArray(VertexFormat var1, @Nullable GlBuffer var2);

    private static class Separate
    extends VertexArrayCache {
        private final Map<VertexFormat, VertexArray> cache = new HashMap<VertexFormat, VertexArray>();
        private final GlDebugLabel debugLabels;
        private final boolean needsMesaWorkaround;

        public Separate(GlDebugLabel debugLabels) {
            String version;
            this.debugLabels = debugLabels;
            this.needsMesaWorkaround = "Mesa".equals(GlStateManager._getString(7936)) ? (version = GlStateManager._getString(7938)).contains("25.0.0") || version.contains("25.0.1") || version.contains("25.0.2") : false;
        }

        @Override
        public void bindVertexArray(VertexFormat format, @Nullable GlBuffer vertexBuffer) {
            VertexArray vertexArray = this.cache.get(format);
            if (vertexArray == null) {
                int id = GlStateManager._glGenVertexArrays();
                GlStateManager._glBindVertexArray(id);
                if (vertexBuffer != null) {
                    List<VertexFormatElement> elements = format.getElements();
                    for (int i = 0; i < elements.size(); ++i) {
                        VertexFormatElement element = elements.get(i);
                        GlStateManager._enableVertexAttribArray(i);
                        if (!element.normalized() && element.type() != VertexFormatElement.Type.FLOAT) {
                            ARBVertexAttribBinding.glVertexAttribIFormat((int)i, (int)element.count(), (int)GlConst.toGl(element.type()), (int)format.getOffset(element));
                        } else {
                            ARBVertexAttribBinding.glVertexAttribFormat((int)i, (int)element.count(), (int)GlConst.toGl(element.type()), (boolean)element.normalized(), (int)format.getOffset(element));
                        }
                        ARBVertexAttribBinding.glVertexAttribBinding((int)i, (int)0);
                    }
                }
                if (vertexBuffer != null) {
                    ARBVertexAttribBinding.glBindVertexBuffer((int)0, (int)vertexBuffer.handle, (long)0L, (int)format.getVertexSize());
                }
                VertexArray vao = new VertexArray(id, format, vertexBuffer);
                this.debugLabels.applyLabel(vao);
                this.cache.put(format, vao);
                return;
            }
            GlStateManager._glBindVertexArray(vertexArray.id);
            if (vertexBuffer != null && vertexArray.lastVertexBuffer != vertexBuffer) {
                if (this.needsMesaWorkaround && vertexArray.lastVertexBuffer != null && vertexArray.lastVertexBuffer.handle == vertexBuffer.handle) {
                    ARBVertexAttribBinding.glBindVertexBuffer((int)0, (int)0, (long)0L, (int)0);
                }
                ARBVertexAttribBinding.glBindVertexBuffer((int)0, (int)vertexBuffer.handle, (long)0L, (int)format.getVertexSize());
                vertexArray.lastVertexBuffer = vertexBuffer;
            }
        }
    }

    private static class Emulated
    extends VertexArrayCache {
        private final Map<VertexFormat, VertexArray> cache = new HashMap<VertexFormat, VertexArray>();
        private final GlDebugLabel debugLabels;

        public Emulated(GlDebugLabel debugLabels) {
            this.debugLabels = debugLabels;
        }

        @Override
        public void bindVertexArray(VertexFormat format, @Nullable GlBuffer vertexBuffer) {
            VertexArray vertexArray = this.cache.get(format);
            if (vertexArray == null) {
                int id = GlStateManager._glGenVertexArrays();
                GlStateManager._glBindVertexArray(id);
                if (vertexBuffer != null) {
                    GlStateManager._glBindBuffer(34962, vertexBuffer.handle);
                    Emulated.setupCombinedAttributes(format, true);
                }
                VertexArray vao = new VertexArray(id, format, vertexBuffer);
                this.debugLabels.applyLabel(vao);
                this.cache.put(format, vao);
                return;
            }
            GlStateManager._glBindVertexArray(vertexArray.id);
            if (vertexBuffer != null && vertexArray.lastVertexBuffer != vertexBuffer) {
                GlStateManager._glBindBuffer(34962, vertexBuffer.handle);
                vertexArray.lastVertexBuffer = vertexBuffer;
                Emulated.setupCombinedAttributes(format, false);
            }
        }

        private static void setupCombinedAttributes(VertexFormat format, boolean enable) {
            int vertexSize = format.getVertexSize();
            List<VertexFormatElement> elements = format.getElements();
            for (int i = 0; i < elements.size(); ++i) {
                VertexFormatElement element = elements.get(i);
                if (enable) {
                    GlStateManager._enableVertexAttribArray(i);
                }
                if (!element.normalized() && element.type() != VertexFormatElement.Type.FLOAT) {
                    GlStateManager._vertexAttribIPointer(i, element.count(), GlConst.toGl(element.type()), vertexSize, format.getOffset(element));
                    continue;
                }
                GlStateManager._vertexAttribPointer(i, element.count(), GlConst.toGl(element.type()), element.normalized(), vertexSize, format.getOffset(element));
            }
        }
    }

    public static class VertexArray {
        final int id;
        final VertexFormat format;
        @Nullable GlBuffer lastVertexBuffer;

        private VertexArray(int id, VertexFormat format, @Nullable GlBuffer lastVertexBuffer) {
            this.id = id;
            this.format = format;
            this.lastVertexBuffer = lastVertexBuffer;
        }
    }
}


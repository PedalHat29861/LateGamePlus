/*
 * Decompiled with CFR 0.152.
 */
package com.mojang.blaze3d.vertex;

import com.mojang.blaze3d.vertex.VertexFormat;
import com.mojang.blaze3d.vertex.VertexFormatElement;

public class DefaultVertexFormat {
    public static final VertexFormat EMPTY = VertexFormat.builder().build();
    public static final VertexFormat BLOCK = VertexFormat.builder().add("Position", VertexFormatElement.POSITION).add("Color", VertexFormatElement.COLOR).add("UV0", VertexFormatElement.UV0).add("UV2", VertexFormatElement.UV2).build();
    public static final VertexFormat ENTITY = VertexFormat.builder().add("Position", VertexFormatElement.POSITION).add("Color", VertexFormatElement.COLOR).add("UV0", VertexFormatElement.UV0).add("UV1", VertexFormatElement.UV1).add("UV2", VertexFormatElement.UV2).add("Normal", VertexFormatElement.NORMAL).padding(1).build();
    public static final VertexFormat PARTICLE = VertexFormat.builder().add("Position", VertexFormatElement.POSITION).add("UV0", VertexFormatElement.UV0).add("Color", VertexFormatElement.COLOR).add("UV2", VertexFormatElement.UV2).build();
    public static final VertexFormat POSITION = VertexFormat.builder().add("Position", VertexFormatElement.POSITION).build();
    public static final VertexFormat POSITION_COLOR = VertexFormat.builder().add("Position", VertexFormatElement.POSITION).add("Color", VertexFormatElement.COLOR).build();
    public static final VertexFormat POSITION_COLOR_NORMAL = VertexFormat.builder().add("Position", VertexFormatElement.POSITION).add("Color", VertexFormatElement.COLOR).add("Normal", VertexFormatElement.NORMAL).padding(1).build();
    public static final VertexFormat POSITION_COLOR_LIGHTMAP = VertexFormat.builder().add("Position", VertexFormatElement.POSITION).add("Color", VertexFormatElement.COLOR).add("UV2", VertexFormatElement.UV2).build();
    public static final VertexFormat POSITION_TEX = VertexFormat.builder().add("Position", VertexFormatElement.POSITION).add("UV0", VertexFormatElement.UV0).build();
    public static final VertexFormat POSITION_TEX_COLOR = VertexFormat.builder().add("Position", VertexFormatElement.POSITION).add("UV0", VertexFormatElement.UV0).add("Color", VertexFormatElement.COLOR).build();
    public static final VertexFormat POSITION_COLOR_TEX_LIGHTMAP = VertexFormat.builder().add("Position", VertexFormatElement.POSITION).add("Color", VertexFormatElement.COLOR).add("UV0", VertexFormatElement.UV0).add("UV2", VertexFormatElement.UV2).build();
    public static final VertexFormat POSITION_TEX_LIGHTMAP_COLOR = VertexFormat.builder().add("Position", VertexFormatElement.POSITION).add("UV0", VertexFormatElement.UV0).add("UV2", VertexFormatElement.UV2).add("Color", VertexFormatElement.COLOR).build();
    public static final VertexFormat POSITION_TEX_COLOR_NORMAL = VertexFormat.builder().add("Position", VertexFormatElement.POSITION).add("UV0", VertexFormatElement.UV0).add("Color", VertexFormatElement.COLOR).add("Normal", VertexFormatElement.NORMAL).padding(1).build();
    public static final VertexFormat POSITION_COLOR_LINE_WIDTH = VertexFormat.builder().add("Position", VertexFormatElement.POSITION).add("Color", VertexFormatElement.COLOR).add("LineWidth", VertexFormatElement.LINE_WIDTH).build();
    public static final VertexFormat POSITION_COLOR_NORMAL_LINE_WIDTH = VertexFormat.builder().add("Position", VertexFormatElement.POSITION).add("Color", VertexFormatElement.COLOR).add("Normal", VertexFormatElement.NORMAL).padding(1).add("LineWidth", VertexFormatElement.LINE_WIDTH).build();
}


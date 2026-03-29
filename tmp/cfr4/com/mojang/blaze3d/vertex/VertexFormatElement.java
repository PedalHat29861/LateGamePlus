/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  org.jspecify.annotations.Nullable
 */
package com.mojang.blaze3d.vertex;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Stream;
import org.jspecify.annotations.Nullable;

public record VertexFormatElement(int id, int index, Type type, boolean normalized, int count) {
    public static final int MAX_COUNT = 32;
    private static final @Nullable VertexFormatElement[] BY_ID = new VertexFormatElement[32];
    private static final List<VertexFormatElement> ELEMENTS = new ArrayList<VertexFormatElement>(32);
    public static final VertexFormatElement POSITION = VertexFormatElement.register(0, 0, Type.FLOAT, false, 3);
    public static final VertexFormatElement COLOR = VertexFormatElement.register(1, 0, Type.UBYTE, true, 4);
    public static final VertexFormatElement UV0;
    public static final VertexFormatElement UV;
    public static final VertexFormatElement UV1;
    public static final VertexFormatElement UV2;
    public static final VertexFormatElement NORMAL;
    public static final VertexFormatElement LINE_WIDTH;

    public VertexFormatElement {
        if (id < 0 || id >= BY_ID.length) {
            throw new IllegalArgumentException("Element ID must be in range [0; " + BY_ID.length + ")");
        }
    }

    public static VertexFormatElement register(int id, int index, Type type, boolean normalized, int count) {
        VertexFormatElement element = new VertexFormatElement(id, index, type, normalized, count);
        if (BY_ID[id] != null) {
            throw new IllegalArgumentException("Duplicate element registration for: " + id);
        }
        VertexFormatElement.BY_ID[id] = element;
        ELEMENTS.add(element);
        return element;
    }

    @Override
    public String toString() {
        String string = this.count + "x" + String.valueOf((Object)this.type) + " (" + this.id + ")";
        return this.normalized ? "normalized " + string : string;
    }

    public int mask() {
        return 1 << this.id;
    }

    public int byteSize() {
        return this.type.size() * this.count;
    }

    public static @Nullable VertexFormatElement byId(int id) {
        return BY_ID[id];
    }

    public static Stream<VertexFormatElement> elementsFromMask(int mask) {
        return ELEMENTS.stream().filter(element -> (mask & element.mask()) != 0);
    }

    static {
        UV = UV0 = VertexFormatElement.register(2, 0, Type.FLOAT, false, 2);
        UV1 = VertexFormatElement.register(3, 1, Type.SHORT, false, 2);
        UV2 = VertexFormatElement.register(4, 2, Type.SHORT, false, 2);
        NORMAL = VertexFormatElement.register(5, 0, Type.BYTE, true, 3);
        LINE_WIDTH = VertexFormatElement.register(6, 0, Type.FLOAT, false, 1);
    }

    public static enum Type {
        FLOAT(4, "Float"),
        UBYTE(1, "Unsigned Byte"),
        BYTE(1, "Byte"),
        USHORT(2, "Unsigned Short"),
        SHORT(2, "Short"),
        UINT(4, "Unsigned Int"),
        INT(4, "Int");

        private final int size;
        private final String name;

        private Type(int size, String name) {
            this.size = size;
            this.name = name;
        }

        public int size() {
            return this.size;
        }

        public String toString() {
            return this.name;
        }
    }
}


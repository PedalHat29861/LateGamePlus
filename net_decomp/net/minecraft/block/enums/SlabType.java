/*
 * Decompiled with CFR 0.152.
 */
package net.minecraft.block.enums;

import net.minecraft.util.StringIdentifiable;

public final class SlabType
extends Enum<SlabType>
implements StringIdentifiable {
    public static final /* enum */ SlabType TOP = new SlabType("top");
    public static final /* enum */ SlabType BOTTOM = new SlabType("bottom");
    public static final /* enum */ SlabType DOUBLE = new SlabType("double");
    private final String name;
    private static final /* synthetic */ SlabType[] field_12680;

    public static SlabType[] values() {
        return (SlabType[])field_12680.clone();
    }

    public static SlabType valueOf(String string) {
        return Enum.valueOf(SlabType.class, string);
    }

    private SlabType(String name) {
        this.name = name;
    }

    public String toString() {
        return this.name;
    }

    @Override
    public String asString() {
        return this.name;
    }

    private static /* synthetic */ SlabType[] method_36735() {
        return new SlabType[]{TOP, BOTTOM, DOUBLE};
    }

    static {
        field_12680 = SlabType.method_36735();
    }
}


/*
 * Decompiled with CFR 0.152.
 */
package net.minecraft.block.enums;

import net.minecraft.util.StringIdentifiable;

public final class WallShape
extends Enum<WallShape>
implements StringIdentifiable {
    public static final /* enum */ WallShape NONE = new WallShape("none");
    public static final /* enum */ WallShape LOW = new WallShape("low");
    public static final /* enum */ WallShape TALL = new WallShape("tall");
    private final String name;
    private static final /* synthetic */ WallShape[] field_22182;

    public static WallShape[] values() {
        return (WallShape[])field_22182.clone();
    }

    public static WallShape valueOf(String string) {
        return Enum.valueOf(WallShape.class, string);
    }

    private WallShape(String name) {
        this.name = name;
    }

    public String toString() {
        return this.asString();
    }

    @Override
    public String asString() {
        return this.name;
    }

    private static /* synthetic */ WallShape[] method_36739() {
        return new WallShape[]{NONE, LOW, TALL};
    }

    static {
        field_22182 = WallShape.method_36739();
    }
}


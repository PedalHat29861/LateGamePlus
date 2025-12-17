/*
 * Decompiled with CFR 0.152.
 */
package net.minecraft.block.enums;

import net.minecraft.util.StringIdentifiable;

public final class StairShape
extends Enum<StairShape>
implements StringIdentifiable {
    public static final /* enum */ StairShape STRAIGHT = new StairShape("straight");
    public static final /* enum */ StairShape INNER_LEFT = new StairShape("inner_left");
    public static final /* enum */ StairShape INNER_RIGHT = new StairShape("inner_right");
    public static final /* enum */ StairShape OUTER_LEFT = new StairShape("outer_left");
    public static final /* enum */ StairShape OUTER_RIGHT = new StairShape("outer_right");
    private final String name;
    private static final /* synthetic */ StairShape[] field_12711;

    public static StairShape[] values() {
        return (StairShape[])field_12711.clone();
    }

    public static StairShape valueOf(String string) {
        return Enum.valueOf(StairShape.class, string);
    }

    private StairShape(String name) {
        this.name = name;
    }

    public String toString() {
        return this.name;
    }

    @Override
    public String asString() {
        return this.name;
    }

    private static /* synthetic */ StairShape[] method_36736() {
        return new StairShape[]{STRAIGHT, INNER_LEFT, INNER_RIGHT, OUTER_LEFT, OUTER_RIGHT};
    }

    static {
        field_12711 = StairShape.method_36736();
    }
}


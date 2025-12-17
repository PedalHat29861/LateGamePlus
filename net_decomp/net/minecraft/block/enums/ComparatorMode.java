/*
 * Decompiled with CFR 0.152.
 */
package net.minecraft.block.enums;

import net.minecraft.util.StringIdentifiable;

public final class ComparatorMode
extends Enum<ComparatorMode>
implements StringIdentifiable {
    public static final /* enum */ ComparatorMode COMPARE = new ComparatorMode("compare");
    public static final /* enum */ ComparatorMode SUBTRACT = new ComparatorMode("subtract");
    private final String name;
    private static final /* synthetic */ ComparatorMode[] field_12579;

    public static ComparatorMode[] values() {
        return (ComparatorMode[])field_12579.clone();
    }

    public static ComparatorMode valueOf(String string) {
        return Enum.valueOf(ComparatorMode.class, string);
    }

    private ComparatorMode(String name) {
        this.name = name;
    }

    public String toString() {
        return this.name;
    }

    @Override
    public String asString() {
        return this.name;
    }

    private static /* synthetic */ ComparatorMode[] method_36725() {
        return new ComparatorMode[]{COMPARE, SUBTRACT};
    }

    static {
        field_12579 = ComparatorMode.method_36725();
    }
}


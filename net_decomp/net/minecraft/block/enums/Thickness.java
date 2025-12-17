/*
 * Decompiled with CFR 0.152.
 */
package net.minecraft.block.enums;

import net.minecraft.util.StringIdentifiable;

public final class Thickness
extends Enum<Thickness>
implements StringIdentifiable {
    public static final /* enum */ Thickness TIP_MERGE = new Thickness("tip_merge");
    public static final /* enum */ Thickness TIP = new Thickness("tip");
    public static final /* enum */ Thickness FRUSTUM = new Thickness("frustum");
    public static final /* enum */ Thickness MIDDLE = new Thickness("middle");
    public static final /* enum */ Thickness BASE = new Thickness("base");
    private final String name;
    private static final /* synthetic */ Thickness[] field_28070;

    public static Thickness[] values() {
        return (Thickness[])field_28070.clone();
    }

    public static Thickness valueOf(String string) {
        return Enum.valueOf(Thickness.class, string);
    }

    private Thickness(String name) {
        this.name = name;
    }

    public String toString() {
        return this.name;
    }

    @Override
    public String asString() {
        return this.name;
    }

    private static /* synthetic */ Thickness[] method_36728() {
        return new Thickness[]{TIP_MERGE, TIP, FRUSTUM, MIDDLE, BASE};
    }

    static {
        field_28070 = Thickness.method_36728();
    }
}


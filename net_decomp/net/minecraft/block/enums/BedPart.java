/*
 * Decompiled with CFR 0.152.
 */
package net.minecraft.block.enums;

import net.minecraft.util.StringIdentifiable;

public final class BedPart
extends Enum<BedPart>
implements StringIdentifiable {
    public static final /* enum */ BedPart HEAD = new BedPart("head");
    public static final /* enum */ BedPart FOOT = new BedPart("foot");
    private final String name;
    private static final /* synthetic */ BedPart[] field_12558;

    public static BedPart[] values() {
        return (BedPart[])field_12558.clone();
    }

    public static BedPart valueOf(String string) {
        return Enum.valueOf(BedPart.class, string);
    }

    private BedPart(String name) {
        this.name = name;
    }

    public String toString() {
        return this.name;
    }

    @Override
    public String asString() {
        return this.name;
    }

    private static /* synthetic */ BedPart[] method_36722() {
        return new BedPart[]{HEAD, FOOT};
    }

    static {
        field_12558 = BedPart.method_36722();
    }
}


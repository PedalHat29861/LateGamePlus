/*
 * Decompiled with CFR 0.152.
 */
package net.minecraft.block.enums;

import net.minecraft.util.StringIdentifiable;

public final class BambooLeaves
extends Enum<BambooLeaves>
implements StringIdentifiable {
    public static final /* enum */ BambooLeaves NONE = new BambooLeaves("none");
    public static final /* enum */ BambooLeaves SMALL = new BambooLeaves("small");
    public static final /* enum */ BambooLeaves LARGE = new BambooLeaves("large");
    private final String name;
    private static final /* synthetic */ BambooLeaves[] field_12470;

    public static BambooLeaves[] values() {
        return (BambooLeaves[])field_12470.clone();
    }

    public static BambooLeaves valueOf(String string) {
        return Enum.valueOf(BambooLeaves.class, string);
    }

    private BambooLeaves(String name) {
        this.name = name;
    }

    public String toString() {
        return this.name;
    }

    @Override
    public String asString() {
        return this.name;
    }

    private static /* synthetic */ BambooLeaves[] method_36721() {
        return new BambooLeaves[]{NONE, SMALL, LARGE};
    }

    static {
        field_12470 = BambooLeaves.method_36721();
    }
}


/*
 * Decompiled with CFR 0.152.
 */
package net.minecraft.util;

public final class ClickType
extends Enum<ClickType> {
    public static final /* enum */ ClickType LEFT = new ClickType();
    public static final /* enum */ ClickType RIGHT = new ClickType();
    private static final /* synthetic */ ClickType[] field_27015;

    public static ClickType[] values() {
        return (ClickType[])field_27015.clone();
    }

    public static ClickType valueOf(String string) {
        return Enum.valueOf(ClickType.class, string);
    }

    private static /* synthetic */ ClickType[] method_36672() {
        return new ClickType[]{LEFT, RIGHT};
    }

    static {
        field_27015 = ClickType.method_36672();
    }
}


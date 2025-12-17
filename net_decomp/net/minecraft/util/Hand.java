/*
 * Decompiled with CFR 0.152.
 */
package net.minecraft.util;

import net.minecraft.entity.EquipmentSlot;

public final class Hand
extends Enum<Hand> {
    public static final /* enum */ Hand MAIN_HAND = new Hand();
    public static final /* enum */ Hand OFF_HAND = new Hand();
    private static final /* synthetic */ Hand[] field_5809;

    public static Hand[] values() {
        return (Hand[])field_5809.clone();
    }

    public static Hand valueOf(String string) {
        return Enum.valueOf(Hand.class, string);
    }

    public EquipmentSlot getEquipmentSlot() {
        return this == MAIN_HAND ? EquipmentSlot.MAINHAND : EquipmentSlot.OFFHAND;
    }

    private static /* synthetic */ Hand[] method_36598() {
        return new Hand[]{MAIN_HAND, OFF_HAND};
    }

    static {
        field_5809 = Hand.method_36598();
    }
}


/*
 * Decompiled with CFR 0.152.
 */
package net.minecraft.block.enums;

import net.minecraft.util.StringIdentifiable;

public final class DoorHinge
extends Enum<DoorHinge>
implements StringIdentifiable {
    public static final /* enum */ DoorHinge LEFT = new DoorHinge();
    public static final /* enum */ DoorHinge RIGHT = new DoorHinge();
    private static final /* synthetic */ DoorHinge[] field_12587;

    public static DoorHinge[] values() {
        return (DoorHinge[])field_12587.clone();
    }

    public static DoorHinge valueOf(String string) {
        return Enum.valueOf(DoorHinge.class, string);
    }

    public String toString() {
        return this.asString();
    }

    @Override
    public String asString() {
        return this == LEFT ? "left" : "right";
    }

    private static /* synthetic */ DoorHinge[] method_36726() {
        return new DoorHinge[]{LEFT, RIGHT};
    }

    static {
        field_12587 = DoorHinge.method_36726();
    }
}


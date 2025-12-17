/*
 * Decompiled with CFR 0.152.
 */
package net.minecraft.block.enums;

import net.minecraft.util.StringIdentifiable;

public final class SculkSensorPhase
extends Enum<SculkSensorPhase>
implements StringIdentifiable {
    public static final /* enum */ SculkSensorPhase INACTIVE = new SculkSensorPhase("inactive");
    public static final /* enum */ SculkSensorPhase ACTIVE = new SculkSensorPhase("active");
    public static final /* enum */ SculkSensorPhase COOLDOWN = new SculkSensorPhase("cooldown");
    private final String name;
    private static final /* synthetic */ SculkSensorPhase[] field_28125;

    public static SculkSensorPhase[] values() {
        return (SculkSensorPhase[])field_28125.clone();
    }

    public static SculkSensorPhase valueOf(String string) {
        return Enum.valueOf(SculkSensorPhase.class, string);
    }

    private SculkSensorPhase(String name) {
        this.name = name;
    }

    public String toString() {
        return this.name;
    }

    @Override
    public String asString() {
        return this.name;
    }

    private static /* synthetic */ SculkSensorPhase[] method_36734() {
        return new SculkSensorPhase[]{INACTIVE, ACTIVE, COOLDOWN};
    }

    static {
        field_28125 = SculkSensorPhase.method_36734();
    }
}


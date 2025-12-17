/*
 * Decompiled with CFR 0.152.
 */
package net.minecraft.entity.ai.pathing;

public final class NavigationType
extends Enum<NavigationType> {
    public static final /* enum */ NavigationType LAND = new NavigationType();
    public static final /* enum */ NavigationType WATER = new NavigationType();
    public static final /* enum */ NavigationType AIR = new NavigationType();
    private static final /* synthetic */ NavigationType[] field_49;

    public static NavigationType[] values() {
        return (NavigationType[])field_49.clone();
    }

    public static NavigationType valueOf(String string) {
        return Enum.valueOf(NavigationType.class, string);
    }

    private static /* synthetic */ NavigationType[] method_36789() {
        return new NavigationType[]{LAND, WATER, AIR};
    }

    static {
        field_49 = NavigationType.method_36789();
    }
}


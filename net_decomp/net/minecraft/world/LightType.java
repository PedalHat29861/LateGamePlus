/*
 * Decompiled with CFR 0.152.
 */
package net.minecraft.world;

public final class LightType
extends Enum<LightType> {
    public static final /* enum */ LightType SKY = new LightType();
    public static final /* enum */ LightType BLOCK = new LightType();
    private static final /* synthetic */ LightType[] field_9285;

    public static LightType[] values() {
        return (LightType[])field_9285.clone();
    }

    public static LightType valueOf(String string) {
        return Enum.valueOf(LightType.class, string);
    }

    private static /* synthetic */ LightType[] method_36696() {
        return new LightType[]{SKY, BLOCK};
    }

    static {
        field_9285 = LightType.method_36696();
    }
}


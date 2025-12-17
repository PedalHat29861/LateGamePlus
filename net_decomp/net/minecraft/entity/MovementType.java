/*
 * Decompiled with CFR 0.152.
 */
package net.minecraft.entity;

public final class MovementType
extends Enum<MovementType> {
    public static final /* enum */ MovementType SELF = new MovementType();
    public static final /* enum */ MovementType PLAYER = new MovementType();
    public static final /* enum */ MovementType PISTON = new MovementType();
    public static final /* enum */ MovementType SHULKER_BOX = new MovementType();
    public static final /* enum */ MovementType SHULKER = new MovementType();
    private static final /* synthetic */ MovementType[] field_6307;

    public static MovementType[] values() {
        return (MovementType[])field_6307.clone();
    }

    public static MovementType valueOf(String string) {
        return Enum.valueOf(MovementType.class, string);
    }

    private static /* synthetic */ MovementType[] method_36611() {
        return new MovementType[]{SELF, PLAYER, PISTON, SHULKER_BOX, SHULKER};
    }

    static {
        field_6307 = MovementType.method_36611();
    }
}


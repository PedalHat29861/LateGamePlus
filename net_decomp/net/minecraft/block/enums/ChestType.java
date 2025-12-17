/*
 * Decompiled with CFR 0.152.
 */
package net.minecraft.block.enums;

import net.minecraft.util.StringIdentifiable;

public final class ChestType
extends Enum<ChestType>
implements StringIdentifiable {
    public static final /* enum */ ChestType SINGLE = new ChestType("single");
    public static final /* enum */ ChestType LEFT = new ChestType("left");
    public static final /* enum */ ChestType RIGHT = new ChestType("right");
    private final String name;
    private static final /* synthetic */ ChestType[] field_12573;

    public static ChestType[] values() {
        return (ChestType[])field_12573.clone();
    }

    public static ChestType valueOf(String string) {
        return Enum.valueOf(ChestType.class, string);
    }

    private ChestType(String name) {
        this.name = name;
    }

    @Override
    public String asString() {
        return this.name;
    }

    public ChestType getOpposite() {
        return switch (this.ordinal()) {
            default -> throw new MatchException(null, null);
            case 0 -> SINGLE;
            case 1 -> RIGHT;
            case 2 -> LEFT;
        };
    }

    private static /* synthetic */ ChestType[] method_36724() {
        return new ChestType[]{SINGLE, LEFT, RIGHT};
    }

    static {
        field_12573 = ChestType.method_36724();
    }
}


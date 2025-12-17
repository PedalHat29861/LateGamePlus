/*
 * Decompiled with CFR 0.152.
 */
package net.minecraft.block.enums;

import net.minecraft.util.StringIdentifiable;

public final class RailShape
extends Enum<RailShape>
implements StringIdentifiable {
    public static final /* enum */ RailShape NORTH_SOUTH = new RailShape("north_south");
    public static final /* enum */ RailShape EAST_WEST = new RailShape("east_west");
    public static final /* enum */ RailShape ASCENDING_EAST = new RailShape("ascending_east");
    public static final /* enum */ RailShape ASCENDING_WEST = new RailShape("ascending_west");
    public static final /* enum */ RailShape ASCENDING_NORTH = new RailShape("ascending_north");
    public static final /* enum */ RailShape ASCENDING_SOUTH = new RailShape("ascending_south");
    public static final /* enum */ RailShape SOUTH_EAST = new RailShape("south_east");
    public static final /* enum */ RailShape SOUTH_WEST = new RailShape("south_west");
    public static final /* enum */ RailShape NORTH_WEST = new RailShape("north_west");
    public static final /* enum */ RailShape NORTH_EAST = new RailShape("north_east");
    private final String name;
    private static final /* synthetic */ RailShape[] field_12673;

    public static RailShape[] values() {
        return (RailShape[])field_12673.clone();
    }

    public static RailShape valueOf(String string) {
        return Enum.valueOf(RailShape.class, string);
    }

    private RailShape(String name) {
        this.name = name;
    }

    public String getName() {
        return this.name;
    }

    public String toString() {
        return this.name;
    }

    public boolean isAscending() {
        return this == ASCENDING_NORTH || this == ASCENDING_EAST || this == ASCENDING_SOUTH || this == ASCENDING_WEST;
    }

    @Override
    public String asString() {
        return this.name;
    }

    private static /* synthetic */ RailShape[] method_36732() {
        return new RailShape[]{NORTH_SOUTH, EAST_WEST, ASCENDING_EAST, ASCENDING_WEST, ASCENDING_NORTH, ASCENDING_SOUTH, SOUTH_EAST, SOUTH_WEST, NORTH_WEST, NORTH_EAST};
    }

    static {
        field_12673 = RailShape.method_36732();
    }
}


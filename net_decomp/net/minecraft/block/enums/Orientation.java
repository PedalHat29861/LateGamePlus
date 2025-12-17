/*
 * Decompiled with CFR 0.152.
 */
package net.minecraft.block.enums;

import net.minecraft.util.StringIdentifiable;
import net.minecraft.util.Util;
import net.minecraft.util.math.Direction;

public final class Orientation
extends Enum<Orientation>
implements StringIdentifiable {
    public static final /* enum */ Orientation DOWN_EAST = new Orientation("down_east", Direction.DOWN, Direction.EAST);
    public static final /* enum */ Orientation DOWN_NORTH = new Orientation("down_north", Direction.DOWN, Direction.NORTH);
    public static final /* enum */ Orientation DOWN_SOUTH = new Orientation("down_south", Direction.DOWN, Direction.SOUTH);
    public static final /* enum */ Orientation DOWN_WEST = new Orientation("down_west", Direction.DOWN, Direction.WEST);
    public static final /* enum */ Orientation UP_EAST = new Orientation("up_east", Direction.UP, Direction.EAST);
    public static final /* enum */ Orientation UP_NORTH = new Orientation("up_north", Direction.UP, Direction.NORTH);
    public static final /* enum */ Orientation UP_SOUTH = new Orientation("up_south", Direction.UP, Direction.SOUTH);
    public static final /* enum */ Orientation UP_WEST = new Orientation("up_west", Direction.UP, Direction.WEST);
    public static final /* enum */ Orientation WEST_UP = new Orientation("west_up", Direction.WEST, Direction.UP);
    public static final /* enum */ Orientation EAST_UP = new Orientation("east_up", Direction.EAST, Direction.UP);
    public static final /* enum */ Orientation NORTH_UP = new Orientation("north_up", Direction.NORTH, Direction.UP);
    public static final /* enum */ Orientation SOUTH_UP = new Orientation("south_up", Direction.SOUTH, Direction.UP);
    private static final int DIRECTIONS;
    private static final Orientation[] VALUES;
    private final String name;
    private final Direction rotation;
    private final Direction facing;
    private static final /* synthetic */ Orientation[] field_23397;

    public static Orientation[] values() {
        return (Orientation[])field_23397.clone();
    }

    public static Orientation valueOf(String string) {
        return Enum.valueOf(Orientation.class, string);
    }

    private static int getIndex(Direction facing, Direction rotation) {
        return facing.ordinal() * DIRECTIONS + rotation.ordinal();
    }

    private Orientation(String name, Direction facing, Direction rotation) {
        this.name = name;
        this.facing = facing;
        this.rotation = rotation;
    }

    @Override
    public String asString() {
        return this.name;
    }

    public static Orientation byDirections(Direction facing, Direction rotation) {
        return VALUES[Orientation.getIndex(facing, rotation)];
    }

    public Direction getFacing() {
        return this.facing;
    }

    public Direction getRotation() {
        return this.rotation;
    }

    private static /* synthetic */ Orientation[] method_36936() {
        return new Orientation[]{DOWN_EAST, DOWN_NORTH, DOWN_SOUTH, DOWN_WEST, UP_EAST, UP_NORTH, UP_SOUTH, UP_WEST, WEST_UP, EAST_UP, NORTH_UP, SOUTH_UP};
    }

    static {
        field_23397 = Orientation.method_36936();
        DIRECTIONS = Direction.values().length;
        VALUES = Util.make(new Orientation[DIRECTIONS * DIRECTIONS], values -> {
            Orientation[] orientationArray = Orientation.values();
            int n = orientationArray.length;
            for (int i = 0; i < n; ++i) {
                Orientation orientation;
                values[Orientation.getIndex((Direction)orientation.facing, (Direction)orientation.rotation)] = orientation = orientationArray[i];
            }
        });
    }
}


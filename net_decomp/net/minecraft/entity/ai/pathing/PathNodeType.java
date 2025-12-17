/*
 * Decompiled with CFR 0.152.
 */
package net.minecraft.entity.ai.pathing;

public final class PathNodeType
extends Enum<PathNodeType> {
    public static final /* enum */ PathNodeType BLOCKED = new PathNodeType(-1.0f);
    public static final /* enum */ PathNodeType OPEN = new PathNodeType(0.0f);
    public static final /* enum */ PathNodeType WALKABLE = new PathNodeType(0.0f);
    public static final /* enum */ PathNodeType WALKABLE_DOOR = new PathNodeType(0.0f);
    public static final /* enum */ PathNodeType TRAPDOOR = new PathNodeType(0.0f);
    public static final /* enum */ PathNodeType POWDER_SNOW = new PathNodeType(-1.0f);
    public static final /* enum */ PathNodeType DANGER_POWDER_SNOW = new PathNodeType(0.0f);
    public static final /* enum */ PathNodeType FENCE = new PathNodeType(-1.0f);
    public static final /* enum */ PathNodeType LAVA = new PathNodeType(-1.0f);
    public static final /* enum */ PathNodeType WATER = new PathNodeType(8.0f);
    public static final /* enum */ PathNodeType WATER_BORDER = new PathNodeType(8.0f);
    public static final /* enum */ PathNodeType RAIL = new PathNodeType(0.0f);
    public static final /* enum */ PathNodeType UNPASSABLE_RAIL = new PathNodeType(-1.0f);
    public static final /* enum */ PathNodeType DANGER_FIRE = new PathNodeType(8.0f);
    public static final /* enum */ PathNodeType DAMAGE_FIRE = new PathNodeType(16.0f);
    public static final /* enum */ PathNodeType DANGER_OTHER = new PathNodeType(8.0f);
    public static final /* enum */ PathNodeType DAMAGE_OTHER = new PathNodeType(-1.0f);
    public static final /* enum */ PathNodeType DOOR_OPEN = new PathNodeType(0.0f);
    public static final /* enum */ PathNodeType DOOR_WOOD_CLOSED = new PathNodeType(-1.0f);
    public static final /* enum */ PathNodeType DOOR_IRON_CLOSED = new PathNodeType(-1.0f);
    public static final /* enum */ PathNodeType BREACH = new PathNodeType(4.0f);
    public static final /* enum */ PathNodeType LEAVES = new PathNodeType(-1.0f);
    public static final /* enum */ PathNodeType STICKY_HONEY = new PathNodeType(8.0f);
    public static final /* enum */ PathNodeType COCOA = new PathNodeType(0.0f);
    public static final /* enum */ PathNodeType DAMAGE_CAUTIOUS = new PathNodeType(0.0f);
    public static final /* enum */ PathNodeType DANGER_TRAPDOOR = new PathNodeType(0.0f);
    private final float defaultPenalty;
    private static final /* synthetic */ PathNodeType[] field_24;

    public static PathNodeType[] values() {
        return (PathNodeType[])field_24.clone();
    }

    public static PathNodeType valueOf(String string) {
        return Enum.valueOf(PathNodeType.class, string);
    }

    private PathNodeType(float defaultPenalty) {
        this.defaultPenalty = defaultPenalty;
    }

    public float getDefaultPenalty() {
        return this.defaultPenalty;
    }

    private static /* synthetic */ PathNodeType[] method_36788() {
        return new PathNodeType[]{BLOCKED, OPEN, WALKABLE, WALKABLE_DOOR, TRAPDOOR, POWDER_SNOW, DANGER_POWDER_SNOW, FENCE, LAVA, WATER, WATER_BORDER, RAIL, UNPASSABLE_RAIL, DANGER_FIRE, DAMAGE_FIRE, DANGER_OTHER, DAMAGE_OTHER, DOOR_OPEN, DOOR_WOOD_CLOSED, DOOR_IRON_CLOSED, BREACH, LEAVES, STICKY_HONEY, COCOA, DAMAGE_CAUTIOUS, DANGER_TRAPDOOR};
    }

    static {
        field_24 = PathNodeType.method_36788();
    }
}


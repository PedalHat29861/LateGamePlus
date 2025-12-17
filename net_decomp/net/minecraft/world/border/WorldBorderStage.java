/*
 * Decompiled with CFR 0.152.
 */
package net.minecraft.world.border;

public final class WorldBorderStage
extends Enum<WorldBorderStage> {
    public static final /* enum */ WorldBorderStage GROWING = new WorldBorderStage(4259712);
    public static final /* enum */ WorldBorderStage SHRINKING = new WorldBorderStage(0xFF3030);
    public static final /* enum */ WorldBorderStage STATIONARY = new WorldBorderStage(2138367);
    private final int color;
    private static final /* synthetic */ WorldBorderStage[] field_12752;

    public static WorldBorderStage[] values() {
        return (WorldBorderStage[])field_12752.clone();
    }

    public static WorldBorderStage valueOf(String string) {
        return Enum.valueOf(WorldBorderStage.class, string);
    }

    private WorldBorderStage(int color) {
        this.color = color;
    }

    public int getColor() {
        return this.color;
    }

    private static /* synthetic */ WorldBorderStage[] method_36740() {
        return new WorldBorderStage[]{GROWING, SHRINKING, STATIONARY};
    }

    static {
        field_12752 = WorldBorderStage.method_36740();
    }
}


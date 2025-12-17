/*
 * Decompiled with CFR 0.152.
 */
package net.minecraft.server.world;

public final class ChunkLevelType
extends Enum<ChunkLevelType> {
    public static final /* enum */ ChunkLevelType INACCESSIBLE = new ChunkLevelType();
    public static final /* enum */ ChunkLevelType FULL = new ChunkLevelType();
    public static final /* enum */ ChunkLevelType BLOCK_TICKING = new ChunkLevelType();
    public static final /* enum */ ChunkLevelType ENTITY_TICKING = new ChunkLevelType();
    private static final /* synthetic */ ChunkLevelType[] field_13878;

    public static ChunkLevelType[] values() {
        return (ChunkLevelType[])field_13878.clone();
    }

    public static ChunkLevelType valueOf(String string) {
        return Enum.valueOf(ChunkLevelType.class, string);
    }

    public boolean isAfter(ChunkLevelType levelType) {
        return this.ordinal() >= levelType.ordinal();
    }

    private static /* synthetic */ ChunkLevelType[] method_36576() {
        return new ChunkLevelType[]{INACCESSIBLE, FULL, BLOCK_TICKING, ENTITY_TICKING};
    }

    static {
        field_13878 = ChunkLevelType.method_36576();
    }
}


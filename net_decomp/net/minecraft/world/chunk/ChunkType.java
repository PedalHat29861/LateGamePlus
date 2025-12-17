/*
 * Decompiled with CFR 0.152.
 */
package net.minecraft.world.chunk;

public final class ChunkType
extends Enum<ChunkType> {
    public static final /* enum */ ChunkType PROTOCHUNK = new ChunkType();
    public static final /* enum */ ChunkType LEVELCHUNK = new ChunkType();
    private static final /* synthetic */ ChunkType[] field_12806;

    public static ChunkType[] values() {
        return (ChunkType[])field_12806.clone();
    }

    public static ChunkType valueOf(String string) {
        return Enum.valueOf(ChunkType.class, string);
    }

    private static /* synthetic */ ChunkType[] method_36741() {
        return new ChunkType[]{PROTOCHUNK, LEVELCHUNK};
    }

    static {
        field_12806 = ChunkType.method_36741();
    }
}


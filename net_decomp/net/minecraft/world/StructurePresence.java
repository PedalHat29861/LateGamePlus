/*
 * Decompiled with CFR 0.152.
 */
package net.minecraft.world;

public final class StructurePresence
extends Enum<StructurePresence> {
    public static final /* enum */ StructurePresence START_PRESENT = new StructurePresence();
    public static final /* enum */ StructurePresence START_NOT_PRESENT = new StructurePresence();
    public static final /* enum */ StructurePresence CHUNK_LOAD_NEEDED = new StructurePresence();
    private static final /* synthetic */ StructurePresence[] field_36242;

    public static StructurePresence[] values() {
        return (StructurePresence[])field_36242.clone();
    }

    public static StructurePresence valueOf(String string) {
        return Enum.valueOf(StructurePresence.class, string);
    }

    private static /* synthetic */ StructurePresence[] method_39843() {
        return new StructurePresence[]{START_PRESENT, START_NOT_PRESENT, CHUNK_LOAD_NEEDED};
    }

    static {
        field_36242 = StructurePresence.method_39843();
    }
}


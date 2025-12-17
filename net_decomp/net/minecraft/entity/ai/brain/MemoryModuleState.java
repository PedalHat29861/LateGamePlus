/*
 * Decompiled with CFR 0.152.
 */
package net.minecraft.entity.ai.brain;

public final class MemoryModuleState
extends Enum<MemoryModuleState> {
    public static final /* enum */ MemoryModuleState VALUE_PRESENT = new MemoryModuleState();
    public static final /* enum */ MemoryModuleState VALUE_ABSENT = new MemoryModuleState();
    public static final /* enum */ MemoryModuleState REGISTERED = new MemoryModuleState();
    private static final /* synthetic */ MemoryModuleState[] field_18459;

    public static MemoryModuleState[] values() {
        return (MemoryModuleState[])field_18459.clone();
    }

    public static MemoryModuleState valueOf(String string) {
        return Enum.valueOf(MemoryModuleState.class, string);
    }

    private static /* synthetic */ MemoryModuleState[] method_36624() {
        return new MemoryModuleState[]{VALUE_PRESENT, VALUE_ABSENT, REGISTERED};
    }

    static {
        field_18459 = MemoryModuleState.method_36624();
    }
}


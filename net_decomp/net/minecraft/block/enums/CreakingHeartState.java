/*
 * Decompiled with CFR 0.152.
 */
package net.minecraft.block.enums;

import net.minecraft.util.StringIdentifiable;

public final class CreakingHeartState
extends Enum<CreakingHeartState>
implements StringIdentifiable {
    public static final /* enum */ CreakingHeartState UPROOTED = new CreakingHeartState("uprooted");
    public static final /* enum */ CreakingHeartState DORMANT = new CreakingHeartState("dormant");
    public static final /* enum */ CreakingHeartState AWAKE = new CreakingHeartState("awake");
    private final String id;
    private static final /* synthetic */ CreakingHeartState[] field_55835;

    public static CreakingHeartState[] values() {
        return (CreakingHeartState[])field_55835.clone();
    }

    public static CreakingHeartState valueOf(String string) {
        return Enum.valueOf(CreakingHeartState.class, string);
    }

    private CreakingHeartState(String id) {
        this.id = id;
    }

    public String toString() {
        return this.id;
    }

    @Override
    public String asString() {
        return this.id;
    }

    private static /* synthetic */ CreakingHeartState[] method_66479() {
        return new CreakingHeartState[]{UPROOTED, DORMANT, AWAKE};
    }

    static {
        field_55835 = CreakingHeartState.method_66479();
    }
}


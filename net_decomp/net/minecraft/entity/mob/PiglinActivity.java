/*
 * Decompiled with CFR 0.152.
 */
package net.minecraft.entity.mob;

public final class PiglinActivity
extends Enum<PiglinActivity> {
    public static final /* enum */ PiglinActivity ATTACKING_WITH_MELEE_WEAPON = new PiglinActivity();
    public static final /* enum */ PiglinActivity CROSSBOW_HOLD = new PiglinActivity();
    public static final /* enum */ PiglinActivity CROSSBOW_CHARGE = new PiglinActivity();
    public static final /* enum */ PiglinActivity ADMIRING_ITEM = new PiglinActivity();
    public static final /* enum */ PiglinActivity DANCING = new PiglinActivity();
    public static final /* enum */ PiglinActivity DEFAULT = new PiglinActivity();
    private static final /* synthetic */ PiglinActivity[] field_22387;

    public static PiglinActivity[] values() {
        return (PiglinActivity[])field_22387.clone();
    }

    public static PiglinActivity valueOf(String string) {
        return Enum.valueOf(PiglinActivity.class, string);
    }

    private static /* synthetic */ PiglinActivity[] method_36659() {
        return new PiglinActivity[]{ATTACKING_WITH_MELEE_WEAPON, CROSSBOW_HOLD, CROSSBOW_CHARGE, ADMIRING_ITEM, DANCING, DEFAULT};
    }

    static {
        field_22387 = PiglinActivity.method_36659();
    }
}


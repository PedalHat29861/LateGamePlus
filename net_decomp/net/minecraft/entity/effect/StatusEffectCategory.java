/*
 * Decompiled with CFR 0.152.
 */
package net.minecraft.entity.effect;

import net.minecraft.util.Formatting;

public final class StatusEffectCategory
extends Enum<StatusEffectCategory> {
    public static final /* enum */ StatusEffectCategory BENEFICIAL = new StatusEffectCategory(Formatting.BLUE);
    public static final /* enum */ StatusEffectCategory HARMFUL = new StatusEffectCategory(Formatting.RED);
    public static final /* enum */ StatusEffectCategory NEUTRAL = new StatusEffectCategory(Formatting.BLUE);
    private final Formatting formatting;
    private static final /* synthetic */ StatusEffectCategory[] field_18275;

    public static StatusEffectCategory[] values() {
        return (StatusEffectCategory[])field_18275.clone();
    }

    public static StatusEffectCategory valueOf(String string) {
        return Enum.valueOf(StatusEffectCategory.class, string);
    }

    private StatusEffectCategory(Formatting format) {
        this.formatting = format;
    }

    public Formatting getFormatting() {
        return this.formatting;
    }

    private static /* synthetic */ StatusEffectCategory[] method_36600() {
        return new StatusEffectCategory[]{BENEFICIAL, HARMFUL, NEUTRAL};
    }

    static {
        field_18275 = StatusEffectCategory.method_36600();
    }
}


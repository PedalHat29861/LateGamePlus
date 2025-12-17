/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  com.mojang.serialization.Codec
 */
package net.minecraft.entity.damage;

import com.mojang.serialization.Codec;
import net.minecraft.util.StringIdentifiable;

public final class DamageScaling
extends Enum<DamageScaling>
implements StringIdentifiable {
    public static final /* enum */ DamageScaling NEVER = new DamageScaling("never");
    public static final /* enum */ DamageScaling WHEN_CAUSED_BY_LIVING_NON_PLAYER = new DamageScaling("when_caused_by_living_non_player");
    public static final /* enum */ DamageScaling ALWAYS = new DamageScaling("always");
    public static final Codec<DamageScaling> CODEC;
    private final String id;
    private static final /* synthetic */ DamageScaling[] field_42290;

    public static DamageScaling[] values() {
        return (DamageScaling[])field_42290.clone();
    }

    public static DamageScaling valueOf(String string) {
        return Enum.valueOf(DamageScaling.class, string);
    }

    private DamageScaling(String id) {
        this.id = id;
    }

    @Override
    public String asString() {
        return this.id;
    }

    private static /* synthetic */ DamageScaling[] method_48788() {
        return new DamageScaling[]{NEVER, WHEN_CAUSED_BY_LIVING_NON_PLAYER, ALWAYS};
    }

    static {
        field_42290 = DamageScaling.method_48788();
        CODEC = StringIdentifiable.createCodec(DamageScaling::values);
    }
}


/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  com.mojang.serialization.Codec
 */
package net.minecraft.entity.damage;

import com.mojang.serialization.Codec;
import net.minecraft.util.StringIdentifiable;

public final class DeathMessageType
extends Enum<DeathMessageType>
implements StringIdentifiable {
    public static final /* enum */ DeathMessageType DEFAULT = new DeathMessageType("default");
    public static final /* enum */ DeathMessageType FALL_VARIANTS = new DeathMessageType("fall_variants");
    public static final /* enum */ DeathMessageType INTENTIONAL_GAME_DESIGN = new DeathMessageType("intentional_game_design");
    public static final Codec<DeathMessageType> CODEC;
    private final String id;
    private static final /* synthetic */ DeathMessageType[] field_42366;

    public static DeathMessageType[] values() {
        return (DeathMessageType[])field_42366.clone();
    }

    public static DeathMessageType valueOf(String string) {
        return Enum.valueOf(DeathMessageType.class, string);
    }

    private DeathMessageType(String id) {
        this.id = id;
    }

    @Override
    public String asString() {
        return this.id;
    }

    private static /* synthetic */ DeathMessageType[] method_48840() {
        return new DeathMessageType[]{DEFAULT, FALL_VARIANTS, INTENTIONAL_GAME_DESIGN};
    }

    static {
        field_42366 = DeathMessageType.method_48840();
        CODEC = StringIdentifiable.createCodec(DeathMessageType::values);
    }
}


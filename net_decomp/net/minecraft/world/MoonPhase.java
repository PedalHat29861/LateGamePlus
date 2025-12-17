/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  com.mojang.serialization.Codec
 */
package net.minecraft.world;

import com.mojang.serialization.Codec;
import net.minecraft.util.StringIdentifiable;

public final class MoonPhase
extends Enum<MoonPhase>
implements StringIdentifiable {
    public static final /* enum */ MoonPhase FULL_MOON = new MoonPhase(0, "full_moon");
    public static final /* enum */ MoonPhase WANING_GIBBOUS = new MoonPhase(1, "waning_gibbous");
    public static final /* enum */ MoonPhase THIRD_QUARTER = new MoonPhase(2, "third_quarter");
    public static final /* enum */ MoonPhase WANING_CRESCENT = new MoonPhase(3, "waning_crescent");
    public static final /* enum */ MoonPhase NEW_MOON = new MoonPhase(4, "new_moon");
    public static final /* enum */ MoonPhase WAXING_CRESCENT = new MoonPhase(5, "waxing_crescent");
    public static final /* enum */ MoonPhase FIRST_QUARTER = new MoonPhase(6, "first_quarter");
    public static final /* enum */ MoonPhase WAXING_GIBBOUS = new MoonPhase(7, "waxing_gibbous");
    public static final Codec<MoonPhase> CODEC;
    public static final int COUNT;
    public static final int DAY_LENGTH = 24000;
    private final int index;
    private final String name;
    private static final /* synthetic */ MoonPhase[] field_63436;

    public static MoonPhase[] values() {
        return (MoonPhase[])field_63436.clone();
    }

    public static MoonPhase valueOf(String string) {
        return Enum.valueOf(MoonPhase.class, string);
    }

    private MoonPhase(int index, String name) {
        this.index = index;
        this.name = name;
    }

    public int getIndex() {
        return this.index;
    }

    public int phaseTicks() {
        return this.index * 24000;
    }

    @Override
    public String asString() {
        return this.name;
    }

    private static /* synthetic */ MoonPhase[] method_75262() {
        return new MoonPhase[]{FULL_MOON, WANING_GIBBOUS, THIRD_QUARTER, WANING_CRESCENT, NEW_MOON, WAXING_CRESCENT, FIRST_QUARTER, WAXING_GIBBOUS};
    }

    static {
        field_63436 = MoonPhase.method_75262();
        CODEC = StringIdentifiable.createCodec(MoonPhase::values);
        COUNT = MoonPhase.values().length;
    }
}


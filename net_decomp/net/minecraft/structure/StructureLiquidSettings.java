/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  com.mojang.serialization.Codec
 */
package net.minecraft.structure;

import com.mojang.serialization.Codec;
import net.minecraft.util.StringIdentifiable;

public final class StructureLiquidSettings
extends Enum<StructureLiquidSettings>
implements StringIdentifiable {
    public static final /* enum */ StructureLiquidSettings IGNORE_WATERLOGGING = new StructureLiquidSettings("ignore_waterlogging");
    public static final /* enum */ StructureLiquidSettings APPLY_WATERLOGGING = new StructureLiquidSettings("apply_waterlogging");
    public static Codec<StructureLiquidSettings> codec;
    private final String id;
    private static final /* synthetic */ StructureLiquidSettings[] field_52241;

    public static StructureLiquidSettings[] values() {
        return (StructureLiquidSettings[])field_52241.clone();
    }

    public static StructureLiquidSettings valueOf(String string) {
        return Enum.valueOf(StructureLiquidSettings.class, string);
    }

    private StructureLiquidSettings(String id) {
        this.id = id;
    }

    @Override
    public String asString() {
        return this.id;
    }

    private static /* synthetic */ StructureLiquidSettings[] method_61019() {
        return new StructureLiquidSettings[]{IGNORE_WATERLOGGING, APPLY_WATERLOGGING};
    }

    static {
        field_52241 = StructureLiquidSettings.method_61019();
        codec = StringIdentifiable.createBasicCodec(StructureLiquidSettings::values);
    }
}


/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  com.mojang.serialization.Codec
 */
package net.minecraft.block.enums;

import com.mojang.serialization.Codec;
import net.minecraft.text.Text;
import net.minecraft.util.StringIdentifiable;
import net.minecraft.util.dynamic.Codecs;

public final class StructureBlockMode
extends Enum<StructureBlockMode>
implements StringIdentifiable {
    public static final /* enum */ StructureBlockMode SAVE = new StructureBlockMode("save");
    public static final /* enum */ StructureBlockMode LOAD = new StructureBlockMode("load");
    public static final /* enum */ StructureBlockMode CORNER = new StructureBlockMode("corner");
    public static final /* enum */ StructureBlockMode DATA = new StructureBlockMode("data");
    @Deprecated
    public static final Codec<StructureBlockMode> CODEC;
    private final String name;
    private final Text text;
    private static final /* synthetic */ StructureBlockMode[] field_12700;

    public static StructureBlockMode[] values() {
        return (StructureBlockMode[])field_12700.clone();
    }

    public static StructureBlockMode valueOf(String string) {
        return Enum.valueOf(StructureBlockMode.class, string);
    }

    private StructureBlockMode(String name) {
        this.name = name;
        this.text = Text.translatable("structure_block.mode_info." + name);
    }

    @Override
    public String asString() {
        return this.name;
    }

    public Text asText() {
        return this.text;
    }

    private static /* synthetic */ StructureBlockMode[] method_36737() {
        return new StructureBlockMode[]{SAVE, LOAD, CORNER, DATA};
    }

    static {
        field_12700 = StructureBlockMode.method_36737();
        CODEC = Codecs.enumByName(StructureBlockMode::valueOf);
    }
}


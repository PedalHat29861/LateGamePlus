/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  com.mojang.serialization.Codec
 */
package net.minecraft.world.gen;

import com.mojang.serialization.Codec;
import net.minecraft.util.StringIdentifiable;

public final class StructureTerrainAdaptation
extends Enum<StructureTerrainAdaptation>
implements StringIdentifiable {
    public static final /* enum */ StructureTerrainAdaptation NONE = new StructureTerrainAdaptation("none");
    public static final /* enum */ StructureTerrainAdaptation BURY = new StructureTerrainAdaptation("bury");
    public static final /* enum */ StructureTerrainAdaptation BEARD_THIN = new StructureTerrainAdaptation("beard_thin");
    public static final /* enum */ StructureTerrainAdaptation BEARD_BOX = new StructureTerrainAdaptation("beard_box");
    public static final /* enum */ StructureTerrainAdaptation ENCAPSULATE = new StructureTerrainAdaptation("encapsulate");
    public static final Codec<StructureTerrainAdaptation> CODEC;
    private final String name;
    private static final /* synthetic */ StructureTerrainAdaptation[] field_28925;

    public static StructureTerrainAdaptation[] values() {
        return (StructureTerrainAdaptation[])field_28925.clone();
    }

    public static StructureTerrainAdaptation valueOf(String string) {
        return Enum.valueOf(StructureTerrainAdaptation.class, string);
    }

    private StructureTerrainAdaptation(String name) {
        this.name = name;
    }

    @Override
    public String asString() {
        return this.name;
    }

    private static /* synthetic */ StructureTerrainAdaptation[] method_36756() {
        return new StructureTerrainAdaptation[]{NONE, BURY, BEARD_THIN, BEARD_BOX, ENCAPSULATE};
    }

    static {
        field_28925 = StructureTerrainAdaptation.method_36756();
        CODEC = StringIdentifiable.createCodec(StructureTerrainAdaptation::values);
    }
}


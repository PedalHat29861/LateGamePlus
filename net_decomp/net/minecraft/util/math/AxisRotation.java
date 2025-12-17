/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  com.google.gson.JsonParseException
 *  com.mojang.serialization.Codec
 *  com.mojang.serialization.DataResult
 */
package net.minecraft.util.math;

import com.google.gson.JsonParseException;
import com.mojang.serialization.Codec;
import com.mojang.serialization.DataResult;
import net.minecraft.util.math.DirectionTransformation;
import net.minecraft.util.math.MathHelper;

public final class AxisRotation
extends Enum<AxisRotation> {
    public static final /* enum */ AxisRotation R0 = new AxisRotation(0, DirectionTransformation.IDENTITY, DirectionTransformation.IDENTITY, DirectionTransformation.IDENTITY);
    public static final /* enum */ AxisRotation R90 = new AxisRotation(1, DirectionTransformation.field_64508, DirectionTransformation.field_64511, DirectionTransformation.field_64514);
    public static final /* enum */ AxisRotation R180 = new AxisRotation(2, DirectionTransformation.field_64507, DirectionTransformation.field_64510, DirectionTransformation.field_64513);
    public static final /* enum */ AxisRotation R270 = new AxisRotation(3, DirectionTransformation.field_64506, DirectionTransformation.field_64509, DirectionTransformation.field_64512);
    public static final Codec<AxisRotation> CODEC;
    public final int index;
    public final DirectionTransformation field_64521;
    public final DirectionTransformation field_64522;
    public final DirectionTransformation field_64523;
    private static final /* synthetic */ AxisRotation[] field_57035;

    public static AxisRotation[] values() {
        return (AxisRotation[])field_57035.clone();
    }

    public static AxisRotation valueOf(String string) {
        return Enum.valueOf(AxisRotation.class, string);
    }

    private AxisRotation(int index, DirectionTransformation directionTransformation, DirectionTransformation directionTransformation2, DirectionTransformation directionTransformation3) {
        this.index = index;
        this.field_64521 = directionTransformation;
        this.field_64522 = directionTransformation2;
        this.field_64523 = directionTransformation3;
    }

    @Deprecated
    public static AxisRotation fromDegrees(int degrees) {
        return switch (MathHelper.floorMod(degrees, 360)) {
            case 0 -> R0;
            case 90 -> R90;
            case 180 -> R180;
            case 270 -> R270;
            default -> throw new JsonParseException("Invalid rotation " + degrees + " found, only 0/90/180/270 allowed");
        };
    }

    public static DirectionTransformation method_76599(AxisRotation axisRotation, AxisRotation axisRotation2) {
        return axisRotation2.field_64522.prepend(axisRotation.field_64521);
    }

    public static DirectionTransformation method_76600(AxisRotation axisRotation, AxisRotation axisRotation2, AxisRotation axisRotation3) {
        return axisRotation3.field_64523.prepend(axisRotation2.field_64522.prepend(axisRotation.field_64521));
    }

    public int rotate(int index) {
        return (index + this.index) % 4;
    }

    private static /* synthetic */ AxisRotation[] method_68063() {
        return new AxisRotation[]{R0, R90, R180, R270};
    }

    static {
        field_57035 = AxisRotation.method_68063();
        CODEC = Codec.INT.comapFlatMap(degrees -> switch (MathHelper.floorMod(degrees, 360)) {
            case 0 -> DataResult.success((Object)((Object)R0));
            case 90 -> DataResult.success((Object)((Object)R90));
            case 180 -> DataResult.success((Object)((Object)R180));
            case 270 -> DataResult.success((Object)((Object)R270));
            default -> DataResult.error(() -> "Invalid rotation " + degrees + " found, only 0/90/180/270 allowed");
        }, rotation -> switch (rotation.ordinal()) {
            default -> throw new MatchException(null, null);
            case 0 -> 0;
            case 1 -> 90;
            case 2 -> 180;
            case 3 -> 270;
        });
    }
}


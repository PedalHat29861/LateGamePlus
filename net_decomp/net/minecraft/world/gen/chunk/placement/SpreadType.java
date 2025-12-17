/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  com.mojang.serialization.Codec
 */
package net.minecraft.world.gen.chunk.placement;

import com.mojang.serialization.Codec;
import net.minecraft.util.StringIdentifiable;
import net.minecraft.util.math.random.Random;

public final class SpreadType
extends Enum<SpreadType>
implements StringIdentifiable {
    public static final /* enum */ SpreadType LINEAR = new SpreadType("linear");
    public static final /* enum */ SpreadType TRIANGULAR = new SpreadType("triangular");
    public static final Codec<SpreadType> CODEC;
    private final String name;
    private static final /* synthetic */ SpreadType[] field_36426;

    public static SpreadType[] values() {
        return (SpreadType[])field_36426.clone();
    }

    public static SpreadType valueOf(String string) {
        return Enum.valueOf(SpreadType.class, string);
    }

    private SpreadType(String name) {
        this.name = name;
    }

    @Override
    public String asString() {
        return this.name;
    }

    public int get(Random random, int bound) {
        return switch (this.ordinal()) {
            default -> throw new MatchException(null, null);
            case 0 -> random.nextInt(bound);
            case 1 -> (random.nextInt(bound) + random.nextInt(bound)) / 2;
        };
    }

    private static /* synthetic */ SpreadType[] method_40175() {
        return new SpreadType[]{LINEAR, TRIANGULAR};
    }

    static {
        field_36426 = SpreadType.method_40175();
        CODEC = StringIdentifiable.createCodec(SpreadType::values);
    }
}


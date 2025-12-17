/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  com.mojang.serialization.Codec
 *  io.netty.buffer.ByteBuf
 */
package net.minecraft.util;

import com.mojang.serialization.Codec;
import io.netty.buffer.ByteBuf;
import java.util.function.IntFunction;
import net.minecraft.network.codec.PacketCodec;
import net.minecraft.network.codec.PacketCodecs;
import net.minecraft.util.Formatting;
import net.minecraft.util.StringIdentifiable;
import net.minecraft.util.function.ValueLists;

public final class Rarity
extends Enum<Rarity>
implements StringIdentifiable {
    public static final /* enum */ Rarity COMMON = new Rarity(0, "common", Formatting.WHITE);
    public static final /* enum */ Rarity UNCOMMON = new Rarity(1, "uncommon", Formatting.YELLOW);
    public static final /* enum */ Rarity RARE = new Rarity(2, "rare", Formatting.AQUA);
    public static final /* enum */ Rarity EPIC = new Rarity(3, "epic", Formatting.LIGHT_PURPLE);
    public static final Codec<Rarity> CODEC;
    public static final IntFunction<Rarity> ID_TO_VALUE;
    public static final PacketCodec<ByteBuf, Rarity> PACKET_CODEC;
    private final int index;
    private final String name;
    private final Formatting formatting;
    private static final /* synthetic */ Rarity[] field_8905;

    public static Rarity[] values() {
        return (Rarity[])field_8905.clone();
    }

    public static Rarity valueOf(String string) {
        return Enum.valueOf(Rarity.class, string);
    }

    private Rarity(int index, String name, Formatting formatting) {
        this.index = index;
        this.name = name;
        this.formatting = formatting;
    }

    public Formatting getFormatting() {
        return this.formatting;
    }

    @Override
    public String asString() {
        return this.name;
    }

    private static /* synthetic */ Rarity[] method_36683() {
        return new Rarity[]{COMMON, UNCOMMON, RARE, EPIC};
    }

    static {
        field_8905 = Rarity.method_36683();
        CODEC = StringIdentifiable.createBasicCodec(Rarity::values);
        ID_TO_VALUE = ValueLists.createIndexToValueFunction(value -> value.index, Rarity.values(), ValueLists.OutOfBoundsHandling.ZERO);
        PACKET_CODEC = PacketCodecs.indexed(ID_TO_VALUE, value -> value.index);
    }
}


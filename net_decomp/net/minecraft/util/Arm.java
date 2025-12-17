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
import net.minecraft.text.Text;
import net.minecraft.util.StringIdentifiable;
import net.minecraft.util.function.ValueLists;

public final class Arm
extends Enum<Arm>
implements StringIdentifiable {
    public static final /* enum */ Arm LEFT = new Arm(0, "left", "options.mainHand.left");
    public static final /* enum */ Arm RIGHT = new Arm(1, "right", "options.mainHand.right");
    public static final Codec<Arm> CODEC;
    private static final IntFunction<Arm> BY_ID;
    public static final PacketCodec<ByteBuf, Arm> PACKET_CODEC;
    private final int id;
    private final String name;
    private final Text text;
    private static final /* synthetic */ Arm[] field_6180;

    public static Arm[] values() {
        return (Arm[])field_6180.clone();
    }

    public static Arm valueOf(String string) {
        return Enum.valueOf(Arm.class, string);
    }

    private Arm(int id, String name, String translationKey) {
        this.id = id;
        this.name = name;
        this.text = Text.translatable(translationKey);
    }

    public Arm getOpposite() {
        return switch (this.ordinal()) {
            default -> throw new MatchException(null, null);
            case 0 -> RIGHT;
            case 1 -> LEFT;
        };
    }

    public Text getText() {
        return this.text;
    }

    @Override
    public String asString() {
        return this.name;
    }

    private static /* synthetic */ Arm[] method_36606() {
        return new Arm[]{LEFT, RIGHT};
    }

    static {
        field_6180 = Arm.method_36606();
        CODEC = StringIdentifiable.createCodec(Arm::values);
        BY_ID = ValueLists.createIndexToValueFunction(arm -> arm.id, Arm.values(), ValueLists.OutOfBoundsHandling.ZERO);
        PACKET_CODEC = PacketCodecs.indexed(BY_ID, arm -> arm.id);
    }
}


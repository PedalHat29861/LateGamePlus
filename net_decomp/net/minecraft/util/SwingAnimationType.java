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
import net.minecraft.util.StringIdentifiable;
import net.minecraft.util.function.ValueLists;

public final class SwingAnimationType
extends Enum<SwingAnimationType>
implements StringIdentifiable {
    public static final /* enum */ SwingAnimationType NONE = new SwingAnimationType(0, "none");
    public static final /* enum */ SwingAnimationType WHACK = new SwingAnimationType(1, "whack");
    public static final /* enum */ SwingAnimationType STAB = new SwingAnimationType(2, "stab");
    private static final IntFunction<SwingAnimationType> BY_PACKET_ID;
    public static final Codec<SwingAnimationType> CODEC;
    public static final PacketCodec<ByteBuf, SwingAnimationType> PACKET_CODEC;
    private final int packetId;
    private final String name;
    private static final /* synthetic */ SwingAnimationType[] field_63406;

    public static SwingAnimationType[] values() {
        return (SwingAnimationType[])field_63406.clone();
    }

    public static SwingAnimationType valueOf(String string) {
        return Enum.valueOf(SwingAnimationType.class, string);
    }

    private SwingAnimationType(int packetId, String name) {
        this.packetId = packetId;
        this.name = name;
    }

    public int getPacketId() {
        return this.packetId;
    }

    @Override
    public String asString() {
        return this.name;
    }

    private static /* synthetic */ SwingAnimationType[] method_75227() {
        return new SwingAnimationType[]{NONE, WHACK, STAB};
    }

    static {
        field_63406 = SwingAnimationType.method_75227();
        BY_PACKET_ID = ValueLists.createIndexToValueFunction(SwingAnimationType::getPacketId, SwingAnimationType.values(), ValueLists.OutOfBoundsHandling.ZERO);
        CODEC = StringIdentifiable.createCodec(SwingAnimationType::values);
        PACKET_CODEC = PacketCodecs.indexed(BY_PACKET_ID, SwingAnimationType::getPacketId);
    }
}


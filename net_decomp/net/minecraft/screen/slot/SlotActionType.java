/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  io.netty.buffer.ByteBuf
 */
package net.minecraft.screen.slot;

import io.netty.buffer.ByteBuf;
import java.util.function.IntFunction;
import net.minecraft.network.codec.PacketCodec;
import net.minecraft.network.codec.PacketCodecs;
import net.minecraft.util.function.ValueLists;

public final class SlotActionType
extends Enum<SlotActionType> {
    public static final /* enum */ SlotActionType PICKUP = new SlotActionType(0);
    public static final /* enum */ SlotActionType QUICK_MOVE = new SlotActionType(1);
    public static final /* enum */ SlotActionType SWAP = new SlotActionType(2);
    public static final /* enum */ SlotActionType CLONE = new SlotActionType(3);
    public static final /* enum */ SlotActionType THROW = new SlotActionType(4);
    public static final /* enum */ SlotActionType QUICK_CRAFT = new SlotActionType(5);
    public static final /* enum */ SlotActionType PICKUP_ALL = new SlotActionType(6);
    private static final IntFunction<SlotActionType> INDEX_MAPPER;
    public static final PacketCodec<ByteBuf, SlotActionType> PACKET_CODEC;
    private final int index;
    private static final /* synthetic */ SlotActionType[] field_7792;

    public static SlotActionType[] values() {
        return (SlotActionType[])field_7792.clone();
    }

    public static SlotActionType valueOf(String string) {
        return Enum.valueOf(SlotActionType.class, string);
    }

    private SlotActionType(int index) {
        this.index = index;
    }

    public int getIndex() {
        return this.index;
    }

    private static /* synthetic */ SlotActionType[] method_36673() {
        return new SlotActionType[]{PICKUP, QUICK_MOVE, SWAP, CLONE, THROW, QUICK_CRAFT, PICKUP_ALL};
    }

    static {
        field_7792 = SlotActionType.method_36673();
        INDEX_MAPPER = ValueLists.createIndexToValueFunction(SlotActionType::getIndex, SlotActionType.values(), ValueLists.OutOfBoundsHandling.ZERO);
        PACKET_CODEC = PacketCodecs.indexed(INDEX_MAPPER, SlotActionType::getIndex);
    }
}


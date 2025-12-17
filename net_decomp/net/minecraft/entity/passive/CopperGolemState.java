/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  com.mojang.serialization.Codec
 *  io.netty.buffer.ByteBuf
 */
package net.minecraft.entity.passive;

import com.mojang.serialization.Codec;
import io.netty.buffer.ByteBuf;
import java.util.function.IntFunction;
import net.minecraft.network.codec.PacketCodec;
import net.minecraft.network.codec.PacketCodecs;
import net.minecraft.util.StringIdentifiable;
import net.minecraft.util.function.ValueLists;

public final class CopperGolemState
extends Enum<CopperGolemState>
implements StringIdentifiable {
    public static final /* enum */ CopperGolemState IDLE = new CopperGolemState("idle", 0);
    public static final /* enum */ CopperGolemState GETTING_ITEM = new CopperGolemState("getting_item", 1);
    public static final /* enum */ CopperGolemState GETTING_NO_ITEM = new CopperGolemState("getting_no_item", 2);
    public static final /* enum */ CopperGolemState DROPPING_ITEM = new CopperGolemState("dropping_item", 3);
    public static final /* enum */ CopperGolemState DROPPING_NO_ITEM = new CopperGolemState("dropping_no_item", 4);
    public static final Codec<CopperGolemState> CODEC;
    private static final IntFunction<CopperGolemState> INDEX_MAPPER;
    public static final PacketCodec<ByteBuf, CopperGolemState> PACKET_CODEC;
    private final String id;
    private final int index;
    private static final /* synthetic */ CopperGolemState[] field_61302;

    public static CopperGolemState[] values() {
        return (CopperGolemState[])field_61302.clone();
    }

    public static CopperGolemState valueOf(String string) {
        return Enum.valueOf(CopperGolemState.class, string);
    }

    private CopperGolemState(String id, int index) {
        this.id = id;
        this.index = index;
    }

    @Override
    public String asString() {
        return this.id;
    }

    private int getIndex() {
        return this.index;
    }

    private static /* synthetic */ CopperGolemState[] method_72495() {
        return new CopperGolemState[]{IDLE, GETTING_ITEM, GETTING_NO_ITEM, DROPPING_ITEM, DROPPING_NO_ITEM};
    }

    static {
        field_61302 = CopperGolemState.method_72495();
        CODEC = StringIdentifiable.createCodec(CopperGolemState::values);
        INDEX_MAPPER = ValueLists.createIndexToValueFunction(CopperGolemState::getIndex, CopperGolemState.values(), ValueLists.OutOfBoundsHandling.ZERO);
        PACKET_CODEC = PacketCodecs.indexed(INDEX_MAPPER, CopperGolemState::getIndex);
    }
}


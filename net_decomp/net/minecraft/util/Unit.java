/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  com.mojang.serialization.Codec
 *  com.mojang.serialization.MapCodec
 *  io.netty.buffer.ByteBuf
 */
package net.minecraft.util;

import com.mojang.serialization.Codec;
import com.mojang.serialization.MapCodec;
import io.netty.buffer.ByteBuf;
import net.minecraft.network.codec.PacketCodec;

public final class Unit
extends Enum<Unit> {
    public static final /* enum */ Unit INSTANCE = new Unit();
    public static final Codec<Unit> CODEC;
    public static final PacketCodec<ByteBuf, Unit> PACKET_CODEC;
    private static final /* synthetic */ Unit[] field_17275;

    public static Unit[] values() {
        return (Unit[])field_17275.clone();
    }

    public static Unit valueOf(String string) {
        return Enum.valueOf(Unit.class, string);
    }

    private static /* synthetic */ Unit[] method_36588() {
        return new Unit[]{INSTANCE};
    }

    static {
        field_17275 = Unit.method_36588();
        CODEC = MapCodec.unitCodec((Object)((Object)INSTANCE));
        PACKET_CODEC = PacketCodec.unit(INSTANCE);
    }
}


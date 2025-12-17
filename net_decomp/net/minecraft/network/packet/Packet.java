/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  io.netty.buffer.ByteBuf
 */
package net.minecraft.network.packet;

import io.netty.buffer.ByteBuf;
import net.minecraft.network.codec.PacketCodec;
import net.minecraft.network.codec.PacketDecoder;
import net.minecraft.network.codec.ValueFirstEncoder;
import net.minecraft.network.listener.PacketListener;
import net.minecraft.network.packet.PacketType;

public interface Packet<T extends PacketListener> {
    public PacketType<? extends Packet<T>> getPacketType();

    public void apply(T var1);

    default public boolean isWritingErrorSkippable() {
        return false;
    }

    default public boolean transitionsNetworkState() {
        return false;
    }

    public static <B extends ByteBuf, T extends Packet<?>> PacketCodec<B, T> createCodec(ValueFirstEncoder<B, T> encoder, PacketDecoder<B, T> decoder) {
        return PacketCodec.of(encoder, decoder);
    }
}


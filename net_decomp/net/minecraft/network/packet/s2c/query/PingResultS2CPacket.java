/*
 * Decompiled with CFR 0.152.
 */
package net.minecraft.network.packet.s2c.query;

import java.lang.invoke.MethodHandle;
import java.lang.runtime.ObjectMethods;
import net.minecraft.network.PacketByteBuf;
import net.minecraft.network.codec.PacketCodec;
import net.minecraft.network.listener.ClientPingResultPacketListener;
import net.minecraft.network.packet.Packet;
import net.minecraft.network.packet.PacketType;
import net.minecraft.network.packet.PingPackets;

public record PingResultS2CPacket(long startTime) implements Packet<ClientPingResultPacketListener>
{
    public static final PacketCodec<PacketByteBuf, PingResultS2CPacket> CODEC = Packet.createCodec(PingResultS2CPacket::write, PingResultS2CPacket::new);

    private PingResultS2CPacket(PacketByteBuf buf) {
        this(buf.readLong());
    }

    private void write(PacketByteBuf buf) {
        buf.writeLong(this.startTime);
    }

    @Override
    public PacketType<PingResultS2CPacket> getPacketType() {
        return PingPackets.PONG_RESPONSE;
    }

    @Override
    public void apply(ClientPingResultPacketListener clientPingResultPacketListener) {
        clientPingResultPacketListener.onPingResult(this);
    }

    @Override
    public final String toString() {
        return ObjectMethods.bootstrap("toString", new MethodHandle[]{PingResultS2CPacket.class, "time", "startTime"}, this);
    }

    @Override
    public final int hashCode() {
        return (int)ObjectMethods.bootstrap("hashCode", new MethodHandle[]{PingResultS2CPacket.class, "time", "startTime"}, this);
    }

    @Override
    public final boolean equals(Object object) {
        return (boolean)ObjectMethods.bootstrap("equals", new MethodHandle[]{PingResultS2CPacket.class, "time", "startTime"}, this, object);
    }
}


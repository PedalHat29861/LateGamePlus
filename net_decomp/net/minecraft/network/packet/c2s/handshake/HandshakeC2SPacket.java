/*
 * Decompiled with CFR 0.152.
 */
package net.minecraft.network.packet.c2s.handshake;

import java.lang.invoke.MethodHandle;
import java.lang.runtime.ObjectMethods;
import net.minecraft.network.PacketByteBuf;
import net.minecraft.network.codec.PacketCodec;
import net.minecraft.network.listener.ServerHandshakePacketListener;
import net.minecraft.network.packet.HandshakePackets;
import net.minecraft.network.packet.Packet;
import net.minecraft.network.packet.PacketType;
import net.minecraft.network.packet.c2s.handshake.ConnectionIntent;

public record HandshakeC2SPacket(int protocolVersion, String address, int port, ConnectionIntent intendedState) implements Packet<ServerHandshakePacketListener>
{
    public static final PacketCodec<PacketByteBuf, HandshakeC2SPacket> CODEC = Packet.createCodec(HandshakeC2SPacket::write, HandshakeC2SPacket::new);
    private static final int MAX_ADDRESS_LENGTH = 255;

    private HandshakeC2SPacket(PacketByteBuf buf) {
        this(buf.readVarInt(), buf.readString(255), buf.readUnsignedShort(), ConnectionIntent.byId(buf.readVarInt()));
    }

    private void write(PacketByteBuf buf) {
        buf.writeVarInt(this.protocolVersion);
        buf.writeString(this.address);
        buf.writeShort(this.port);
        buf.writeVarInt(this.intendedState.getId());
    }

    @Override
    public PacketType<HandshakeC2SPacket> getPacketType() {
        return HandshakePackets.INTENTION;
    }

    @Override
    public void apply(ServerHandshakePacketListener serverHandshakePacketListener) {
        serverHandshakePacketListener.onHandshake(this);
    }

    @Override
    public boolean transitionsNetworkState() {
        return true;
    }

    @Override
    public final String toString() {
        return ObjectMethods.bootstrap("toString", new MethodHandle[]{HandshakeC2SPacket.class, "protocolVersion;hostName;port;intention", "protocolVersion", "address", "port", "intendedState"}, this);
    }

    @Override
    public final int hashCode() {
        return (int)ObjectMethods.bootstrap("hashCode", new MethodHandle[]{HandshakeC2SPacket.class, "protocolVersion;hostName;port;intention", "protocolVersion", "address", "port", "intendedState"}, this);
    }

    @Override
    public final boolean equals(Object object) {
        return (boolean)ObjectMethods.bootstrap("equals", new MethodHandle[]{HandshakeC2SPacket.class, "protocolVersion;hostName;port;intention", "protocolVersion", "address", "port", "intendedState"}, this, object);
    }
}


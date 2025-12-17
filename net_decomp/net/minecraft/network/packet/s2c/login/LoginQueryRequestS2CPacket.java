/*
 * Decompiled with CFR 0.152.
 */
package net.minecraft.network.packet.s2c.login;

import java.lang.invoke.MethodHandle;
import java.lang.runtime.ObjectMethods;
import net.minecraft.network.PacketByteBuf;
import net.minecraft.network.codec.PacketCodec;
import net.minecraft.network.listener.ClientLoginPacketListener;
import net.minecraft.network.packet.LoginPackets;
import net.minecraft.network.packet.Packet;
import net.minecraft.network.packet.PacketType;
import net.minecraft.network.packet.s2c.login.LoginQueryRequestPayload;
import net.minecraft.network.packet.s2c.login.UnknownLoginQueryRequestPayload;
import net.minecraft.util.Identifier;

public record LoginQueryRequestS2CPacket(int queryId, LoginQueryRequestPayload payload) implements Packet<ClientLoginPacketListener>
{
    public static final PacketCodec<PacketByteBuf, LoginQueryRequestS2CPacket> CODEC = Packet.createCodec(LoginQueryRequestS2CPacket::write, LoginQueryRequestS2CPacket::new);
    private static final int MAX_PAYLOAD_SIZE = 0x100000;

    private LoginQueryRequestS2CPacket(PacketByteBuf buf) {
        this(buf.readVarInt(), LoginQueryRequestS2CPacket.readPayload(buf.readIdentifier(), buf));
    }

    private static LoginQueryRequestPayload readPayload(Identifier id, PacketByteBuf buf) {
        return LoginQueryRequestS2CPacket.readUnknownPayload(id, buf);
    }

    private static UnknownLoginQueryRequestPayload readUnknownPayload(Identifier id, PacketByteBuf buf) {
        int i = buf.readableBytes();
        if (i < 0 || i > 0x100000) {
            throw new IllegalArgumentException("Payload may not be larger than 1048576 bytes");
        }
        buf.skipBytes(i);
        return new UnknownLoginQueryRequestPayload(id);
    }

    private void write(PacketByteBuf buf) {
        buf.writeVarInt(this.queryId);
        buf.writeIdentifier(this.payload.id());
        this.payload.write(buf);
    }

    @Override
    public PacketType<LoginQueryRequestS2CPacket> getPacketType() {
        return LoginPackets.CUSTOM_QUERY;
    }

    @Override
    public void apply(ClientLoginPacketListener clientLoginPacketListener) {
        clientLoginPacketListener.onQueryRequest(this);
    }

    @Override
    public final String toString() {
        return ObjectMethods.bootstrap("toString", new MethodHandle[]{LoginQueryRequestS2CPacket.class, "transactionId;payload", "queryId", "payload"}, this);
    }

    @Override
    public final int hashCode() {
        return (int)ObjectMethods.bootstrap("hashCode", new MethodHandle[]{LoginQueryRequestS2CPacket.class, "transactionId;payload", "queryId", "payload"}, this);
    }

    @Override
    public final boolean equals(Object object) {
        return (boolean)ObjectMethods.bootstrap("equals", new MethodHandle[]{LoginQueryRequestS2CPacket.class, "transactionId;payload", "queryId", "payload"}, this, object);
    }
}


/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  org.jspecify.annotations.Nullable
 */
package net.minecraft.network.packet.c2s.login;

import java.lang.invoke.MethodHandle;
import java.lang.runtime.ObjectMethods;
import net.minecraft.network.PacketByteBuf;
import net.minecraft.network.codec.PacketCodec;
import net.minecraft.network.listener.ServerLoginPacketListener;
import net.minecraft.network.packet.LoginPackets;
import net.minecraft.network.packet.Packet;
import net.minecraft.network.packet.PacketType;
import net.minecraft.network.packet.c2s.login.LoginQueryResponsePayload;
import net.minecraft.network.packet.c2s.login.UnknownLoginQueryResponsePayload;
import org.jspecify.annotations.Nullable;

public record LoginQueryResponseC2SPacket(int queryId, @Nullable LoginQueryResponsePayload response) implements Packet<ServerLoginPacketListener>
{
    public static final PacketCodec<PacketByteBuf, LoginQueryResponseC2SPacket> CODEC = Packet.createCodec(LoginQueryResponseC2SPacket::write, LoginQueryResponseC2SPacket::read);
    private static final int MAX_PAYLOAD_SIZE = 0x100000;

    private static LoginQueryResponseC2SPacket read(PacketByteBuf buf) {
        int i = buf.readVarInt();
        return new LoginQueryResponseC2SPacket(i, LoginQueryResponseC2SPacket.readPayload(i, buf));
    }

    private static LoginQueryResponsePayload readPayload(int queryId, PacketByteBuf buf) {
        return LoginQueryResponseC2SPacket.getVanillaPayload(buf);
    }

    private static LoginQueryResponsePayload getVanillaPayload(PacketByteBuf buf) {
        int i = buf.readableBytes();
        if (i < 0 || i > 0x100000) {
            throw new IllegalArgumentException("Payload may not be larger than 1048576 bytes");
        }
        buf.skipBytes(i);
        return UnknownLoginQueryResponsePayload.INSTANCE;
    }

    private void write(PacketByteBuf buf2) {
        buf2.writeVarInt(this.queryId);
        buf2.writeNullable(this.response, (buf, response) -> response.write((PacketByteBuf)((Object)buf)));
    }

    @Override
    public PacketType<LoginQueryResponseC2SPacket> getPacketType() {
        return LoginPackets.CUSTOM_QUERY_ANSWER;
    }

    @Override
    public void apply(ServerLoginPacketListener serverLoginPacketListener) {
        serverLoginPacketListener.onQueryResponse(this);
    }

    @Override
    public final String toString() {
        return ObjectMethods.bootstrap("toString", new MethodHandle[]{LoginQueryResponseC2SPacket.class, "transactionId;payload", "queryId", "response"}, this);
    }

    @Override
    public final int hashCode() {
        return (int)ObjectMethods.bootstrap("hashCode", new MethodHandle[]{LoginQueryResponseC2SPacket.class, "transactionId;payload", "queryId", "response"}, this);
    }

    @Override
    public final boolean equals(Object object) {
        return (boolean)ObjectMethods.bootstrap("equals", new MethodHandle[]{LoginQueryResponseC2SPacket.class, "transactionId;payload", "queryId", "response"}, this, object);
    }
}


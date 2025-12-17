/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  org.jspecify.annotations.Nullable
 */
package net.minecraft.network.packet.c2s.play;

import java.lang.invoke.MethodHandle;
import java.lang.runtime.ObjectMethods;
import java.time.Instant;
import net.minecraft.network.PacketByteBuf;
import net.minecraft.network.codec.PacketCodec;
import net.minecraft.network.listener.ServerPlayPacketListener;
import net.minecraft.network.message.LastSeenMessageList;
import net.minecraft.network.message.MessageSignatureData;
import net.minecraft.network.packet.Packet;
import net.minecraft.network.packet.PacketType;
import net.minecraft.network.packet.PlayPackets;
import org.jspecify.annotations.Nullable;

public record ChatMessageC2SPacket(String chatMessage, Instant timestamp, long salt, @Nullable MessageSignatureData signature, LastSeenMessageList.Acknowledgment acknowledgment) implements Packet<ServerPlayPacketListener>
{
    public static final PacketCodec<PacketByteBuf, ChatMessageC2SPacket> CODEC = Packet.createCodec(ChatMessageC2SPacket::write, ChatMessageC2SPacket::new);

    private ChatMessageC2SPacket(PacketByteBuf buf) {
        this(buf.readString(256), buf.readInstant(), buf.readLong(), buf.readNullable(MessageSignatureData::fromBuf), new LastSeenMessageList.Acknowledgment(buf));
    }

    private void write(PacketByteBuf buf) {
        buf.writeString(this.chatMessage, 256);
        buf.writeInstant(this.timestamp);
        buf.writeLong(this.salt);
        buf.writeNullable(this.signature, MessageSignatureData::write);
        this.acknowledgment.write(buf);
    }

    @Override
    public PacketType<ChatMessageC2SPacket> getPacketType() {
        return PlayPackets.CHAT;
    }

    @Override
    public void apply(ServerPlayPacketListener serverPlayPacketListener) {
        serverPlayPacketListener.onChatMessage(this);
    }

    @Override
    public final String toString() {
        return ObjectMethods.bootstrap("toString", new MethodHandle[]{ChatMessageC2SPacket.class, "message;timeStamp;salt;signature;lastSeenMessages", "chatMessage", "timestamp", "salt", "signature", "acknowledgment"}, this);
    }

    @Override
    public final int hashCode() {
        return (int)ObjectMethods.bootstrap("hashCode", new MethodHandle[]{ChatMessageC2SPacket.class, "message;timeStamp;salt;signature;lastSeenMessages", "chatMessage", "timestamp", "salt", "signature", "acknowledgment"}, this);
    }

    @Override
    public final boolean equals(Object object) {
        return (boolean)ObjectMethods.bootstrap("equals", new MethodHandle[]{ChatMessageC2SPacket.class, "message;timeStamp;salt;signature;lastSeenMessages", "chatMessage", "timestamp", "salt", "signature", "acknowledgment"}, this, object);
    }
}


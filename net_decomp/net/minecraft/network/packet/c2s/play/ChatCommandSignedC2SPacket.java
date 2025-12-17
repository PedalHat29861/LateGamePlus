/*
 * Decompiled with CFR 0.152.
 */
package net.minecraft.network.packet.c2s.play;

import java.lang.invoke.MethodHandle;
import java.lang.runtime.ObjectMethods;
import java.time.Instant;
import net.minecraft.network.PacketByteBuf;
import net.minecraft.network.codec.PacketCodec;
import net.minecraft.network.listener.ServerPlayPacketListener;
import net.minecraft.network.message.ArgumentSignatureDataMap;
import net.minecraft.network.message.LastSeenMessageList;
import net.minecraft.network.packet.Packet;
import net.minecraft.network.packet.PacketType;
import net.minecraft.network.packet.PlayPackets;

public record ChatCommandSignedC2SPacket(String command, Instant timestamp, long salt, ArgumentSignatureDataMap argumentSignatures, LastSeenMessageList.Acknowledgment lastSeenMessages) implements Packet<ServerPlayPacketListener>
{
    public static final PacketCodec<PacketByteBuf, ChatCommandSignedC2SPacket> CODEC = Packet.createCodec(ChatCommandSignedC2SPacket::write, ChatCommandSignedC2SPacket::new);

    private ChatCommandSignedC2SPacket(PacketByteBuf buf) {
        this(buf.readString(), buf.readInstant(), buf.readLong(), new ArgumentSignatureDataMap(buf), new LastSeenMessageList.Acknowledgment(buf));
    }

    private void write(PacketByteBuf buf) {
        buf.writeString(this.command);
        buf.writeInstant(this.timestamp);
        buf.writeLong(this.salt);
        this.argumentSignatures.write(buf);
        this.lastSeenMessages.write(buf);
    }

    @Override
    public PacketType<ChatCommandSignedC2SPacket> getPacketType() {
        return PlayPackets.CHAT_COMMAND_SIGNED;
    }

    @Override
    public void apply(ServerPlayPacketListener serverPlayPacketListener) {
        serverPlayPacketListener.onChatCommandSigned(this);
    }

    @Override
    public final String toString() {
        return ObjectMethods.bootstrap("toString", new MethodHandle[]{ChatCommandSignedC2SPacket.class, "command;timeStamp;salt;argumentSignatures;lastSeenMessages", "command", "timestamp", "salt", "argumentSignatures", "lastSeenMessages"}, this);
    }

    @Override
    public final int hashCode() {
        return (int)ObjectMethods.bootstrap("hashCode", new MethodHandle[]{ChatCommandSignedC2SPacket.class, "command;timeStamp;salt;argumentSignatures;lastSeenMessages", "command", "timestamp", "salt", "argumentSignatures", "lastSeenMessages"}, this);
    }

    @Override
    public final boolean equals(Object object) {
        return (boolean)ObjectMethods.bootstrap("equals", new MethodHandle[]{ChatCommandSignedC2SPacket.class, "command;timeStamp;salt;argumentSignatures;lastSeenMessages", "command", "timestamp", "salt", "argumentSignatures", "lastSeenMessages"}, this, object);
    }
}


/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  org.jspecify.annotations.Nullable
 */
package net.minecraft.network.packet.s2c.play;

import java.lang.invoke.MethodHandle;
import java.lang.runtime.ObjectMethods;
import java.util.UUID;
import net.minecraft.network.PacketByteBuf;
import net.minecraft.network.RegistryByteBuf;
import net.minecraft.network.codec.PacketCodec;
import net.minecraft.network.listener.ClientPlayPacketListener;
import net.minecraft.network.message.FilterMask;
import net.minecraft.network.message.MessageBody;
import net.minecraft.network.message.MessageSignatureData;
import net.minecraft.network.message.MessageType;
import net.minecraft.network.packet.Packet;
import net.minecraft.network.packet.PacketType;
import net.minecraft.network.packet.PlayPackets;
import net.minecraft.text.Text;
import net.minecraft.text.TextCodecs;
import org.jspecify.annotations.Nullable;

public record ChatMessageS2CPacket(int globalIndex, UUID sender, int index, @Nullable MessageSignatureData signature, MessageBody.Serialized body, @Nullable Text unsignedContent, FilterMask filterMask, MessageType.Parameters serializedParameters) implements Packet<ClientPlayPacketListener>
{
    public static final PacketCodec<RegistryByteBuf, ChatMessageS2CPacket> CODEC = Packet.createCodec(ChatMessageS2CPacket::write, ChatMessageS2CPacket::new);

    private ChatMessageS2CPacket(RegistryByteBuf buf) {
        this(buf.readVarInt(), buf.readUuid(), buf.readVarInt(), buf.readNullable(MessageSignatureData::fromBuf), new MessageBody.Serialized(buf), PacketByteBuf.readNullable(buf, TextCodecs.UNLIMITED_REGISTRY_PACKET_CODEC), FilterMask.readMask(buf), (MessageType.Parameters)MessageType.Parameters.CODEC.decode(buf));
    }

    private void write(RegistryByteBuf buf) {
        buf.writeVarInt(this.globalIndex);
        buf.writeUuid(this.sender);
        buf.writeVarInt(this.index);
        buf.writeNullable(this.signature, MessageSignatureData::write);
        this.body.write(buf);
        PacketByteBuf.writeNullable(buf, this.unsignedContent, TextCodecs.UNLIMITED_REGISTRY_PACKET_CODEC);
        FilterMask.writeMask(buf, this.filterMask);
        MessageType.Parameters.CODEC.encode(buf, this.serializedParameters);
    }

    @Override
    public PacketType<ChatMessageS2CPacket> getPacketType() {
        return PlayPackets.PLAYER_CHAT;
    }

    @Override
    public void apply(ClientPlayPacketListener clientPlayPacketListener) {
        clientPlayPacketListener.onChatMessage(this);
    }

    @Override
    public boolean isWritingErrorSkippable() {
        return true;
    }

    @Override
    public final String toString() {
        return ObjectMethods.bootstrap("toString", new MethodHandle[]{ChatMessageS2CPacket.class, "globalIndex;sender;index;signature;body;unsignedContent;filterMask;chatType", "globalIndex", "sender", "index", "signature", "body", "unsignedContent", "filterMask", "serializedParameters"}, this);
    }

    @Override
    public final int hashCode() {
        return (int)ObjectMethods.bootstrap("hashCode", new MethodHandle[]{ChatMessageS2CPacket.class, "globalIndex;sender;index;signature;body;unsignedContent;filterMask;chatType", "globalIndex", "sender", "index", "signature", "body", "unsignedContent", "filterMask", "serializedParameters"}, this);
    }

    @Override
    public final boolean equals(Object object) {
        return (boolean)ObjectMethods.bootstrap("equals", new MethodHandle[]{ChatMessageS2CPacket.class, "globalIndex;sender;index;signature;body;unsignedContent;filterMask;chatType", "globalIndex", "sender", "index", "signature", "body", "unsignedContent", "filterMask", "serializedParameters"}, this, object);
    }
}


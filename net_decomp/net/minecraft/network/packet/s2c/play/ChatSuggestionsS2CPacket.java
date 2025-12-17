/*
 * Decompiled with CFR 0.152.
 */
package net.minecraft.network.packet.s2c.play;

import java.util.List;
import net.minecraft.network.PacketByteBuf;
import net.minecraft.network.codec.PacketCodec;
import net.minecraft.network.listener.ClientPlayPacketListener;
import net.minecraft.network.packet.Packet;
import net.minecraft.network.packet.PacketType;
import net.minecraft.network.packet.PlayPackets;

public record ChatSuggestionsS2CPacket(Action action, List<String> entries) implements Packet<ClientPlayPacketListener>
{
    public static final PacketCodec<PacketByteBuf, ChatSuggestionsS2CPacket> CODEC = Packet.createCodec(ChatSuggestionsS2CPacket::write, ChatSuggestionsS2CPacket::new);

    private ChatSuggestionsS2CPacket(PacketByteBuf buf) {
        this(buf.readEnumConstant(Action.class), buf.readList(PacketByteBuf::readString));
    }

    private void write(PacketByteBuf buf) {
        buf.writeEnumConstant(this.action);
        buf.writeCollection(this.entries, PacketByteBuf::writeString);
    }

    @Override
    public PacketType<ChatSuggestionsS2CPacket> getPacketType() {
        return PlayPackets.CUSTOM_CHAT_COMPLETIONS;
    }

    @Override
    public void apply(ClientPlayPacketListener clientPlayPacketListener) {
        clientPlayPacketListener.onChatSuggestions(this);
    }

    public static final class Action
    extends Enum<Action> {
        public static final /* enum */ Action ADD = new Action();
        public static final /* enum */ Action REMOVE = new Action();
        public static final /* enum */ Action SET = new Action();
        private static final /* synthetic */ Action[] field_39804;

        public static Action[] values() {
            return (Action[])field_39804.clone();
        }

        public static Action valueOf(String string) {
            return Enum.valueOf(Action.class, string);
        }

        private static /* synthetic */ Action[] method_44784() {
            return new Action[]{ADD, REMOVE, SET};
        }

        static {
            field_39804 = Action.method_44784();
        }
    }
}


/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  com.google.common.primitives.Ints
 *  com.mojang.serialization.Codec
 */
package net.minecraft.network.message;

import com.google.common.primitives.Ints;
import com.mojang.serialization.Codec;
import java.lang.invoke.MethodHandle;
import java.lang.runtime.ObjectMethods;
import java.security.SignatureException;
import java.util.ArrayList;
import java.util.BitSet;
import java.util.List;
import java.util.Optional;
import net.minecraft.network.PacketByteBuf;
import net.minecraft.network.encryption.SignatureUpdatable;
import net.minecraft.network.message.MessageSignatureData;
import net.minecraft.network.message.MessageSignatureStorage;

public record LastSeenMessageList(List<MessageSignatureData> entries) {
    public static final Codec<LastSeenMessageList> CODEC = MessageSignatureData.CODEC.listOf().xmap(LastSeenMessageList::new, LastSeenMessageList::entries);
    public static LastSeenMessageList EMPTY = new LastSeenMessageList(List.of());
    public static final int MAX_ENTRIES = 20;

    public void updateSignatures(SignatureUpdatable.SignatureUpdater updater) throws SignatureException {
        updater.update(Ints.toByteArray((int)this.entries.size()));
        for (MessageSignatureData messageSignatureData : this.entries) {
            updater.update(messageSignatureData.data());
        }
    }

    public Indexed pack(MessageSignatureStorage storage) {
        return new Indexed(this.entries.stream().map(signature -> signature.pack(storage)).toList());
    }

    public byte calculateChecksum() {
        int i = 1;
        for (MessageSignatureData messageSignatureData : this.entries) {
            i = 31 * i + messageSignatureData.calculateChecksum();
        }
        byte b = (byte)i;
        return b == 0 ? (byte)1 : b;
    }

    public record Indexed(List<MessageSignatureData.Indexed> buf) {
        public static final Indexed EMPTY = new Indexed(List.of());

        public Indexed(PacketByteBuf buf) {
            this(buf.readCollection(PacketByteBuf.getMaxValidator(ArrayList::new, 20), MessageSignatureData.Indexed::fromBuf));
        }

        public void write(PacketByteBuf buf) {
            buf.writeCollection(this.buf, MessageSignatureData.Indexed::write);
        }

        public Optional<LastSeenMessageList> unpack(MessageSignatureStorage storage) {
            ArrayList<MessageSignatureData> list = new ArrayList<MessageSignatureData>(this.buf.size());
            for (MessageSignatureData.Indexed indexed : this.buf) {
                Optional<MessageSignatureData> optional = indexed.getSignature(storage);
                if (optional.isEmpty()) {
                    return Optional.empty();
                }
                list.add(optional.get());
            }
            return Optional.of(new LastSeenMessageList(list));
        }

        @Override
        public final String toString() {
            return ObjectMethods.bootstrap("toString", new MethodHandle[]{Indexed.class, "entries", "buf"}, this);
        }

        @Override
        public final int hashCode() {
            return (int)ObjectMethods.bootstrap("hashCode", new MethodHandle[]{Indexed.class, "entries", "buf"}, this);
        }

        @Override
        public final boolean equals(Object object) {
            return (boolean)ObjectMethods.bootstrap("equals", new MethodHandle[]{Indexed.class, "entries", "buf"}, this, object);
        }
    }

    public record Acknowledgment(int offset, BitSet acknowledged, byte checksum) {
        public static final byte NO_CHECKSUM = 0;

        public Acknowledgment(PacketByteBuf buf) {
            this(buf.readVarInt(), buf.readBitSet(20), buf.readByte());
        }

        public void write(PacketByteBuf buf) {
            buf.writeVarInt(this.offset);
            buf.writeBitSet(this.acknowledged, 20);
            buf.writeByte(this.checksum);
        }

        public boolean checksumEquals(LastSeenMessageList lastSeenMessages) {
            return this.checksum == 0 || this.checksum == lastSeenMessages.calculateChecksum();
        }
    }
}


/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  com.google.common.primitives.Ints
 *  com.google.common.primitives.Longs
 *  com.mojang.datafixers.kinds.App
 *  com.mojang.datafixers.kinds.Applicative
 *  com.mojang.serialization.Codec
 *  com.mojang.serialization.MapCodec
 *  com.mojang.serialization.codecs.RecordCodecBuilder
 */
package net.minecraft.network.message;

import com.google.common.primitives.Ints;
import com.google.common.primitives.Longs;
import com.mojang.datafixers.kinds.App;
import com.mojang.datafixers.kinds.Applicative;
import com.mojang.serialization.Codec;
import com.mojang.serialization.MapCodec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import java.lang.invoke.MethodHandle;
import java.lang.runtime.ObjectMethods;
import java.nio.charset.StandardCharsets;
import java.security.SignatureException;
import java.time.Instant;
import java.util.Optional;
import net.minecraft.network.PacketByteBuf;
import net.minecraft.network.encryption.SignatureUpdatable;
import net.minecraft.network.message.LastSeenMessageList;
import net.minecraft.network.message.MessageSignatureStorage;
import net.minecraft.util.dynamic.Codecs;

public record MessageBody(String content, Instant timestamp, long salt, LastSeenMessageList lastSeenMessages) {
    public static final MapCodec<MessageBody> CODEC = RecordCodecBuilder.mapCodec(instance -> instance.group((App)Codec.STRING.fieldOf("content").forGetter(MessageBody::content), (App)Codecs.INSTANT.fieldOf("time_stamp").forGetter(MessageBody::timestamp), (App)Codec.LONG.fieldOf("salt").forGetter(MessageBody::salt), (App)LastSeenMessageList.CODEC.optionalFieldOf("last_seen", (Object)LastSeenMessageList.EMPTY).forGetter(MessageBody::lastSeenMessages)).apply((Applicative)instance, MessageBody::new));

    public static MessageBody ofUnsigned(String content) {
        return new MessageBody(content, Instant.now(), 0L, LastSeenMessageList.EMPTY);
    }

    public void update(SignatureUpdatable.SignatureUpdater updater) throws SignatureException {
        updater.update(Longs.toByteArray((long)this.salt));
        updater.update(Longs.toByteArray((long)this.timestamp.getEpochSecond()));
        byte[] bs = this.content.getBytes(StandardCharsets.UTF_8);
        updater.update(Ints.toByteArray((int)bs.length));
        updater.update(bs);
        this.lastSeenMessages.updateSignatures(updater);
    }

    public Serialized toSerialized(MessageSignatureStorage storage) {
        return new Serialized(this.content, this.timestamp, this.salt, this.lastSeenMessages.pack(storage));
    }

    @Override
    public final String toString() {
        return ObjectMethods.bootstrap("toString", new MethodHandle[]{MessageBody.class, "content;timeStamp;salt;lastSeen", "content", "timestamp", "salt", "lastSeenMessages"}, this);
    }

    @Override
    public final int hashCode() {
        return (int)ObjectMethods.bootstrap("hashCode", new MethodHandle[]{MessageBody.class, "content;timeStamp;salt;lastSeen", "content", "timestamp", "salt", "lastSeenMessages"}, this);
    }

    @Override
    public final boolean equals(Object object) {
        return (boolean)ObjectMethods.bootstrap("equals", new MethodHandle[]{MessageBody.class, "content;timeStamp;salt;lastSeen", "content", "timestamp", "salt", "lastSeenMessages"}, this, object);
    }

    public record Serialized(String content, Instant timestamp, long salt, LastSeenMessageList.Indexed lastSeen) {
        public Serialized(PacketByteBuf buf) {
            this(buf.readString(256), buf.readInstant(), buf.readLong(), new LastSeenMessageList.Indexed(buf));
        }

        public void write(PacketByteBuf buf) {
            buf.writeString(this.content, 256);
            buf.writeInstant(this.timestamp);
            buf.writeLong(this.salt);
            this.lastSeen.write(buf);
        }

        public Optional<MessageBody> toBody(MessageSignatureStorage storage) {
            return this.lastSeen.unpack(storage).map(lastSeenMessages -> new MessageBody(this.content, this.timestamp, this.salt, (LastSeenMessageList)lastSeenMessages));
        }

        @Override
        public final String toString() {
            return ObjectMethods.bootstrap("toString", new MethodHandle[]{Serialized.class, "content;timeStamp;salt;lastSeen", "content", "timestamp", "salt", "lastSeen"}, this);
        }

        @Override
        public final int hashCode() {
            return (int)ObjectMethods.bootstrap("hashCode", new MethodHandle[]{Serialized.class, "content;timeStamp;salt;lastSeen", "content", "timestamp", "salt", "lastSeen"}, this);
        }

        @Override
        public final boolean equals(Object object) {
            return (boolean)ObjectMethods.bootstrap("equals", new MethodHandle[]{Serialized.class, "content;timeStamp;salt;lastSeen", "content", "timestamp", "salt", "lastSeen"}, this, object);
        }
    }
}


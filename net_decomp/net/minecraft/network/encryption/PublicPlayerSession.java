/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  com.mojang.authlib.GameProfile
 */
package net.minecraft.network.encryption;

import com.mojang.authlib.GameProfile;
import java.lang.invoke.MethodHandle;
import java.lang.runtime.ObjectMethods;
import java.time.Duration;
import java.util.UUID;
import net.minecraft.network.PacketByteBuf;
import net.minecraft.network.encryption.PlayerPublicKey;
import net.minecraft.network.encryption.SignatureVerifier;
import net.minecraft.network.message.MessageChain;
import net.minecraft.network.message.MessageVerifier;

public record PublicPlayerSession(UUID sessionId, PlayerPublicKey publicKeyData) {
    public MessageVerifier createVerifier(Duration gracePeriod) {
        return new MessageVerifier.Impl(this.publicKeyData.createSignatureInstance(), () -> this.publicKeyData.data().isExpired(gracePeriod));
    }

    public MessageChain.Unpacker createUnpacker(UUID sender) {
        return new MessageChain(sender, this.sessionId).getUnpacker(this.publicKeyData);
    }

    public Serialized toSerialized() {
        return new Serialized(this.sessionId, this.publicKeyData.data());
    }

    public boolean isKeyExpired() {
        return this.publicKeyData.data().isExpired();
    }

    @Override
    public final String toString() {
        return ObjectMethods.bootstrap("toString", new MethodHandle[]{PublicPlayerSession.class, "sessionId;profilePublicKey", "sessionId", "publicKeyData"}, this);
    }

    @Override
    public final int hashCode() {
        return (int)ObjectMethods.bootstrap("hashCode", new MethodHandle[]{PublicPlayerSession.class, "sessionId;profilePublicKey", "sessionId", "publicKeyData"}, this);
    }

    @Override
    public final boolean equals(Object object) {
        return (boolean)ObjectMethods.bootstrap("equals", new MethodHandle[]{PublicPlayerSession.class, "sessionId;profilePublicKey", "sessionId", "publicKeyData"}, this, object);
    }

    public record Serialized(UUID sessionId, PlayerPublicKey.PublicKeyData publicKeyData) {
        public static Serialized fromBuf(PacketByteBuf buf) {
            return new Serialized(buf.readUuid(), new PlayerPublicKey.PublicKeyData(buf));
        }

        public static void write(PacketByteBuf buf, Serialized serialized) {
            buf.writeUuid(serialized.sessionId);
            serialized.publicKeyData.write(buf);
        }

        public PublicPlayerSession toSession(GameProfile gameProfile, SignatureVerifier servicesSignatureVerifier) throws PlayerPublicKey.PublicKeyException {
            return new PublicPlayerSession(this.sessionId, PlayerPublicKey.verifyAndDecode(servicesSignatureVerifier, gameProfile.id(), this.publicKeyData));
        }

        @Override
        public final String toString() {
            return ObjectMethods.bootstrap("toString", new MethodHandle[]{Serialized.class, "sessionId;profilePublicKey", "sessionId", "publicKeyData"}, this);
        }

        @Override
        public final int hashCode() {
            return (int)ObjectMethods.bootstrap("hashCode", new MethodHandle[]{Serialized.class, "sessionId;profilePublicKey", "sessionId", "publicKeyData"}, this);
        }

        @Override
        public final boolean equals(Object object) {
            return (boolean)ObjectMethods.bootstrap("equals", new MethodHandle[]{Serialized.class, "sessionId;profilePublicKey", "sessionId", "publicKeyData"}, this, object);
        }
    }
}


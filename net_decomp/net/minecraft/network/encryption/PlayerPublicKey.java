/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  com.mojang.datafixers.kinds.App
 *  com.mojang.datafixers.kinds.Applicative
 *  com.mojang.serialization.Codec
 *  com.mojang.serialization.codecs.RecordCodecBuilder
 */
package net.minecraft.network.encryption;

import com.mojang.datafixers.kinds.App;
import com.mojang.datafixers.kinds.Applicative;
import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import java.lang.invoke.MethodHandle;
import java.lang.runtime.ObjectMethods;
import java.nio.ByteBuffer;
import java.nio.ByteOrder;
import java.security.PublicKey;
import java.time.Duration;
import java.time.Instant;
import java.util.Arrays;
import java.util.UUID;
import net.minecraft.network.PacketByteBuf;
import net.minecraft.network.encryption.NetworkEncryptionUtils;
import net.minecraft.network.encryption.SignatureVerifier;
import net.minecraft.text.Text;
import net.minecraft.util.TextifiedException;
import net.minecraft.util.dynamic.Codecs;

public record PlayerPublicKey(PublicKeyData data) {
    public static final Text EXPIRED_PUBLIC_KEY_TEXT = Text.translatable("multiplayer.disconnect.expired_public_key");
    private static final Text INVALID_PUBLIC_KEY_SIGNATURE_TEXT = Text.translatable("multiplayer.disconnect.invalid_public_key_signature");
    public static final Duration EXPIRATION_GRACE_PERIOD = Duration.ofHours(8L);
    public static final Codec<PlayerPublicKey> CODEC = PublicKeyData.CODEC.xmap(PlayerPublicKey::new, PlayerPublicKey::data);

    public static PlayerPublicKey verifyAndDecode(SignatureVerifier servicesSignatureVerifier, UUID playerUuid, PublicKeyData publicKeyData) throws PublicKeyException {
        if (!publicKeyData.verifyKey(servicesSignatureVerifier, playerUuid)) {
            throw new PublicKeyException(INVALID_PUBLIC_KEY_SIGNATURE_TEXT);
        }
        return new PlayerPublicKey(publicKeyData);
    }

    public SignatureVerifier createSignatureInstance() {
        return SignatureVerifier.create(this.data.key, "SHA256withRSA");
    }

    public static final class PublicKeyData
    extends Record {
        private final Instant expiresAt;
        final PublicKey key;
        private final byte[] keySignature;
        private static final int KEY_SIGNATURE_MAX_SIZE = 4096;
        public static final Codec<PublicKeyData> CODEC = RecordCodecBuilder.create(instance -> instance.group((App)Codecs.INSTANT.fieldOf("expires_at").forGetter(PublicKeyData::expiresAt), (App)NetworkEncryptionUtils.RSA_PUBLIC_KEY_CODEC.fieldOf("key").forGetter(PublicKeyData::key), (App)Codecs.BASE_64.fieldOf("signature_v2").forGetter(PublicKeyData::keySignature)).apply((Applicative)instance, PublicKeyData::new));

        public PublicKeyData(PacketByteBuf buf) {
            this(buf.readInstant(), buf.readPublicKey(), buf.readByteArray(4096));
        }

        public PublicKeyData(Instant expiresAt, PublicKey key, byte[] keySignature) {
            this.expiresAt = expiresAt;
            this.key = key;
            this.keySignature = keySignature;
        }

        public void write(PacketByteBuf buf) {
            buf.writeInstant(this.expiresAt);
            buf.writePublicKey(this.key);
            buf.writeByteArray(this.keySignature);
        }

        boolean verifyKey(SignatureVerifier servicesSignatureVerifier, UUID playerUuid) {
            return servicesSignatureVerifier.validate(this.toSerializedString(playerUuid), this.keySignature);
        }

        private byte[] toSerializedString(UUID playerUuid) {
            byte[] bs = this.key.getEncoded();
            byte[] cs = new byte[24 + bs.length];
            ByteBuffer byteBuffer = ByteBuffer.wrap(cs).order(ByteOrder.BIG_ENDIAN);
            byteBuffer.putLong(playerUuid.getMostSignificantBits()).putLong(playerUuid.getLeastSignificantBits()).putLong(this.expiresAt.toEpochMilli()).put(bs);
            return cs;
        }

        public boolean isExpired() {
            return this.expiresAt.isBefore(Instant.now());
        }

        public boolean isExpired(Duration gracePeriod) {
            return this.expiresAt.plus(gracePeriod).isBefore(Instant.now());
        }

        @Override
        public boolean equals(Object o) {
            if (o instanceof PublicKeyData) {
                PublicKeyData publicKeyData = (PublicKeyData)o;
                return this.expiresAt.equals(publicKeyData.expiresAt) && this.key.equals(publicKeyData.key) && Arrays.equals(this.keySignature, publicKeyData.keySignature);
            }
            return false;
        }

        @Override
        public final String toString() {
            return ObjectMethods.bootstrap("toString", new MethodHandle[]{PublicKeyData.class, "expiresAt;key;keySignature", "expiresAt", "key", "keySignature"}, this);
        }

        @Override
        public final int hashCode() {
            return (int)ObjectMethods.bootstrap("hashCode", new MethodHandle[]{PublicKeyData.class, "expiresAt;key;keySignature", "expiresAt", "key", "keySignature"}, this);
        }

        public Instant expiresAt() {
            return this.expiresAt;
        }

        public PublicKey key() {
            return this.key;
        }

        public byte[] keySignature() {
            return this.keySignature;
        }
    }

    public static class PublicKeyException
    extends TextifiedException {
        public PublicKeyException(Text text) {
            super(text);
        }
    }
}


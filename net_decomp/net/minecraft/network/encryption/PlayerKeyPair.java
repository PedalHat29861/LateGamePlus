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
import java.security.PrivateKey;
import java.time.Instant;
import net.minecraft.network.encryption.NetworkEncryptionUtils;
import net.minecraft.network.encryption.PlayerPublicKey;
import net.minecraft.util.dynamic.Codecs;

public record PlayerKeyPair(PrivateKey privateKey, PlayerPublicKey publicKey, Instant refreshedAfter) {
    public static final Codec<PlayerKeyPair> CODEC = RecordCodecBuilder.create(instance -> instance.group((App)NetworkEncryptionUtils.RSA_PRIVATE_KEY_CODEC.fieldOf("private_key").forGetter(PlayerKeyPair::privateKey), (App)PlayerPublicKey.CODEC.fieldOf("public_key").forGetter(PlayerKeyPair::publicKey), (App)Codecs.INSTANT.fieldOf("refreshed_after").forGetter(PlayerKeyPair::refreshedAfter)).apply((Applicative)instance, PlayerKeyPair::new));

    public boolean needsRefreshing() {
        return this.refreshedAfter.isBefore(Instant.now());
    }
}


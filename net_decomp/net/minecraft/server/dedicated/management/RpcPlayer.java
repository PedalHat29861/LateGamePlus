/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  com.mojang.authlib.GameProfile
 *  com.mojang.datafixers.kinds.App
 *  com.mojang.datafixers.kinds.Applicative
 *  com.mojang.serialization.Codec
 *  com.mojang.serialization.MapCodec
 *  com.mojang.serialization.codecs.RecordCodecBuilder
 */
package net.minecraft.server.dedicated.management;

import com.mojang.authlib.GameProfile;
import com.mojang.datafixers.kinds.App;
import com.mojang.datafixers.kinds.Applicative;
import com.mojang.serialization.Codec;
import com.mojang.serialization.MapCodec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import java.util.Optional;
import java.util.UUID;
import net.minecraft.server.PlayerConfigEntry;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.util.Uuids;

public record RpcPlayer(Optional<UUID> id, Optional<String> name) {
    public static final MapCodec<RpcPlayer> CODEC = RecordCodecBuilder.mapCodec(instance -> instance.group((App)Uuids.STRING_CODEC.optionalFieldOf("id").forGetter(RpcPlayer::id), (App)Codec.STRING.optionalFieldOf("name").forGetter(RpcPlayer::name)).apply((Applicative)instance, RpcPlayer::new));

    public static RpcPlayer of(GameProfile profile) {
        return new RpcPlayer(Optional.of(profile.id()), Optional.of(profile.name()));
    }

    public static RpcPlayer of(PlayerConfigEntry player) {
        return new RpcPlayer(Optional.of(player.id()), Optional.of(player.name()));
    }

    public static RpcPlayer of(ServerPlayerEntity player) {
        GameProfile gameProfile = player.getGameProfile();
        return RpcPlayer.of(gameProfile);
    }
}


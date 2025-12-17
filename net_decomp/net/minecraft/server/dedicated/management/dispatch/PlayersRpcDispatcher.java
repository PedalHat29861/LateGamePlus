/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  com.mojang.datafixers.kinds.App
 *  com.mojang.datafixers.kinds.Applicative
 *  com.mojang.serialization.MapCodec
 *  com.mojang.serialization.codecs.RecordCodecBuilder
 *  org.jspecify.annotations.Nullable
 */
package net.minecraft.server.dedicated.management.dispatch;

import com.mojang.datafixers.kinds.App;
import com.mojang.datafixers.kinds.Applicative;
import com.mojang.serialization.MapCodec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import java.lang.invoke.MethodHandle;
import java.lang.runtime.ObjectMethods;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import net.minecraft.server.dedicated.management.RpcKickReason;
import net.minecraft.server.dedicated.management.RpcPlayer;
import net.minecraft.server.dedicated.management.dispatch.ManagementHandlerDispatcher;
import net.minecraft.server.dedicated.management.network.ManagementConnectionId;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.text.Text;
import org.jspecify.annotations.Nullable;

public class PlayersRpcDispatcher {
    private static final Text DEFAULT_KICK_REASON = Text.translatable("multiplayer.disconnect.kicked");

    public static List<RpcPlayer> get(ManagementHandlerDispatcher dispatcher) {
        return dispatcher.getPlayerListHandler().getPlayerList().stream().map(RpcPlayer::of).toList();
    }

    public static List<RpcPlayer> kick(ManagementHandlerDispatcher dispatcher, List<RpcEntry> list, ManagementConnectionId remote) {
        ArrayList<RpcPlayer> list2 = new ArrayList<RpcPlayer>();
        for (RpcEntry rpcEntry : list) {
            ServerPlayerEntity serverPlayerEntity = PlayersRpcDispatcher.getPlayer(dispatcher, rpcEntry.player());
            if (serverPlayerEntity == null) continue;
            dispatcher.getPlayerListHandler().removePlayer(serverPlayerEntity, remote);
            serverPlayerEntity.networkHandler.disconnect(rpcEntry.message.flatMap(RpcKickReason::toText).orElse(DEFAULT_KICK_REASON));
            list2.add(rpcEntry.player());
        }
        return list2;
    }

    private static @Nullable ServerPlayerEntity getPlayer(ManagementHandlerDispatcher dispatcher, RpcPlayer player) {
        if (player.id().isPresent()) {
            return dispatcher.getPlayerListHandler().getPlayer(player.id().get());
        }
        if (player.name().isPresent()) {
            return dispatcher.getPlayerListHandler().getPlayer(player.name().get());
        }
        return null;
    }

    public static final class RpcEntry
    extends Record {
        private final RpcPlayer player;
        final Optional<RpcKickReason> message;
        public static final MapCodec<RpcEntry> CODEC = RecordCodecBuilder.mapCodec(instance -> instance.group((App)RpcPlayer.CODEC.codec().fieldOf("player").forGetter(RpcEntry::player), (App)RpcKickReason.CODEC.optionalFieldOf("message").forGetter(RpcEntry::message)).apply((Applicative)instance, RpcEntry::new));

        public RpcEntry(RpcPlayer player, Optional<RpcKickReason> message) {
            this.player = player;
            this.message = message;
        }

        @Override
        public final String toString() {
            return ObjectMethods.bootstrap("toString", new MethodHandle[]{RpcEntry.class, "player;message", "player", "message"}, this);
        }

        @Override
        public final int hashCode() {
            return (int)ObjectMethods.bootstrap("hashCode", new MethodHandle[]{RpcEntry.class, "player;message", "player", "message"}, this);
        }

        @Override
        public final boolean equals(Object object) {
            return (boolean)ObjectMethods.bootstrap("equals", new MethodHandle[]{RpcEntry.class, "player;message", "player", "message"}, this, object);
        }

        public RpcPlayer player() {
            return this.player;
        }

        public Optional<RpcKickReason> message() {
            return this.message;
        }
    }
}


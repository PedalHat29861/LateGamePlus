/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  com.mojang.datafixers.kinds.App
 *  com.mojang.datafixers.kinds.Applicative
 *  com.mojang.serialization.Codec
 *  com.mojang.serialization.codecs.RecordCodecBuilder
 */
package net.minecraft.server.dedicated.management.dispatch;

import com.mojang.datafixers.kinds.App;
import com.mojang.datafixers.kinds.Applicative;
import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import java.util.List;
import java.util.Optional;
import net.minecraft.server.ServerMetadata;
import net.minecraft.server.dedicated.management.RpcKickReason;
import net.minecraft.server.dedicated.management.RpcPlayer;
import net.minecraft.server.dedicated.management.dispatch.ManagementHandlerDispatcher;
import net.minecraft.server.dedicated.management.dispatch.PlayersRpcDispatcher;
import net.minecraft.server.dedicated.management.network.ManagementConnectionId;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.text.Text;

public class ServerRpcDispatcher {
    public static RpcStatus status(ManagementHandlerDispatcher dispatcher) {
        if (!dispatcher.getServerHandler().isLoading()) {
            return RpcStatus.EMPTY;
        }
        return new RpcStatus(true, PlayersRpcDispatcher.get(dispatcher), ServerMetadata.Version.create());
    }

    public static boolean save(ManagementHandlerDispatcher dispatcher, boolean flush, ManagementConnectionId remote) {
        return dispatcher.getServerHandler().save(true, flush, true, remote);
    }

    public static boolean stop(ManagementHandlerDispatcher dispatcher, ManagementConnectionId remote) {
        dispatcher.submit(() -> dispatcher.getServerHandler().stop(false, remote));
        return true;
    }

    public static boolean systemMessage(ManagementHandlerDispatcher dispatcher, RpcSystemMessage message, ManagementConnectionId remote) {
        Text text = message.message().toText().orElse(null);
        if (text == null) {
            return false;
        }
        if (message.receivingPlayers().isPresent()) {
            if (message.receivingPlayers().get().isEmpty()) {
                return false;
            }
            for (RpcPlayer rpcPlayer : message.receivingPlayers().get()) {
                ServerPlayerEntity serverPlayerEntity;
                if (rpcPlayer.id().isPresent()) {
                    serverPlayerEntity = dispatcher.getPlayerListHandler().getPlayer(rpcPlayer.id().get());
                } else {
                    if (!rpcPlayer.name().isPresent()) continue;
                    serverPlayerEntity = dispatcher.getPlayerListHandler().getPlayer(rpcPlayer.name().get());
                }
                if (serverPlayerEntity == null) continue;
                serverPlayerEntity.sendMessageToClient(text, message.overlay());
            }
        } else {
            dispatcher.getServerHandler().broadcastMessage(text, message.overlay(), remote);
        }
        return true;
    }

    public record RpcStatus(boolean started, List<RpcPlayer> players, ServerMetadata.Version version) {
        public static final Codec<RpcStatus> CODEC = RecordCodecBuilder.create(instance -> instance.group((App)Codec.BOOL.fieldOf("started").forGetter(RpcStatus::started), (App)RpcPlayer.CODEC.codec().listOf().lenientOptionalFieldOf("players", List.of()).forGetter(RpcStatus::players), (App)ServerMetadata.Version.CODEC.fieldOf("version").forGetter(RpcStatus::version)).apply((Applicative)instance, RpcStatus::new));
        public static final RpcStatus EMPTY = new RpcStatus(false, List.of(), ServerMetadata.Version.create());
    }

    public record RpcSystemMessage(RpcKickReason message, boolean overlay, Optional<List<RpcPlayer>> receivingPlayers) {
        public static final Codec<RpcSystemMessage> CODEC = RecordCodecBuilder.create(instance -> instance.group((App)RpcKickReason.CODEC.fieldOf("message").forGetter(RpcSystemMessage::message), (App)Codec.BOOL.fieldOf("overlay").forGetter(RpcSystemMessage::overlay), (App)RpcPlayer.CODEC.codec().listOf().lenientOptionalFieldOf("receivingPlayers").forGetter(RpcSystemMessage::receivingPlayers)).apply((Applicative)instance, RpcSystemMessage::new));
    }
}


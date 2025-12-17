/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  com.mojang.datafixers.kinds.App
 *  com.mojang.datafixers.kinds.Applicative
 *  com.mojang.serialization.Codec
 *  com.mojang.serialization.MapCodec
 *  com.mojang.serialization.codecs.RecordCodecBuilder
 *  org.jspecify.annotations.Nullable
 */
package net.minecraft.server.dedicated.management.dispatch;

import com.mojang.datafixers.kinds.App;
import com.mojang.datafixers.kinds.Applicative;
import com.mojang.serialization.Codec;
import com.mojang.serialization.MapCodec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import java.time.Instant;
import java.util.Date;
import java.util.List;
import java.util.Objects;
import java.util.Optional;
import java.util.Set;
import java.util.concurrent.CompletableFuture;
import java.util.stream.Collectors;
import net.minecraft.server.BannedPlayerEntry;
import net.minecraft.server.PlayerConfigEntry;
import net.minecraft.server.dedicated.management.RpcPlayer;
import net.minecraft.server.dedicated.management.dispatch.ManagementHandlerDispatcher;
import net.minecraft.server.dedicated.management.network.ManagementConnectionId;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.text.Text;
import net.minecraft.util.Util;
import net.minecraft.util.dynamic.Codecs;
import org.jspecify.annotations.Nullable;

public class PlayerBansRpcDispatcher {
    private static final String DEFAULT_SOURCE = "Management server";

    public static List<RpcEntry> get(ManagementHandlerDispatcher dispatcher) {
        return dispatcher.getBanHandler().getUserBanList().stream().filter(entry -> entry.getKey() != null).map(ConfigEntry::of).map(RpcEntry::of).toList();
    }

    public static List<RpcEntry> add(ManagementHandlerDispatcher dispatcher, List<RpcEntry> players, ManagementConnectionId remote) {
        List<CompletableFuture> list = players.stream().map(player -> dispatcher.getPlayerListHandler().getPlayerAsync(player.player().id(), player.player().name()).thenApply(playerEntry -> playerEntry.map(player::toConfigEntry))).toList();
        for (Optional optional : Util.combineSafe(list).join()) {
            if (optional.isEmpty()) continue;
            ConfigEntry configEntry = (ConfigEntry)optional.get();
            dispatcher.getBanHandler().addPlayer(configEntry.toBannedPlayerEntry(), remote);
            ServerPlayerEntity serverPlayerEntity = dispatcher.getPlayerListHandler().getPlayer(((ConfigEntry)optional.get()).player().id());
            if (serverPlayerEntity == null) continue;
            serverPlayerEntity.networkHandler.disconnect(Text.translatable("multiplayer.disconnect.banned"));
        }
        return PlayerBansRpcDispatcher.get(dispatcher);
    }

    public static List<RpcEntry> clear(ManagementHandlerDispatcher dispatcher, ManagementConnectionId remote) {
        dispatcher.getBanHandler().clearBanList(remote);
        return PlayerBansRpcDispatcher.get(dispatcher);
    }

    public static List<RpcEntry> remove(ManagementHandlerDispatcher dispatcher, List<RpcPlayer> players, ManagementConnectionId remote) {
        List<CompletableFuture> list = players.stream().map(player -> dispatcher.getPlayerListHandler().getPlayerAsync(player.id(), player.name())).toList();
        for (Optional optional : Util.combineSafe(list).join()) {
            if (optional.isEmpty()) continue;
            dispatcher.getBanHandler().removePlayer((PlayerConfigEntry)optional.get(), remote);
        }
        return PlayerBansRpcDispatcher.get(dispatcher);
    }

    public static List<RpcEntry> set(ManagementHandlerDispatcher dispatcher, List<RpcEntry> players, ManagementConnectionId remote) {
        List<CompletableFuture> list = players.stream().map(player -> dispatcher.getPlayerListHandler().getPlayerAsync(player.player().id(), player.player().name()).thenApply(playerEntry -> playerEntry.map(player::toConfigEntry))).toList();
        Set set = Util.combineSafe(list).join().stream().flatMap(Optional::stream).collect(Collectors.toSet());
        Set set2 = dispatcher.getBanHandler().getUserBanList().stream().filter(entry -> entry.getKey() != null).map(ConfigEntry::of).collect(Collectors.toSet());
        set2.stream().filter(player -> !set.contains(player)).forEach(player -> dispatcher.getBanHandler().removePlayer(player.player(), remote));
        set.stream().filter(player -> !set2.contains(player)).forEach(player -> {
            dispatcher.getBanHandler().addPlayer(player.toBannedPlayerEntry(), remote);
            ServerPlayerEntity serverPlayerEntity = dispatcher.getPlayerListHandler().getPlayer(player.player().id());
            if (serverPlayerEntity != null) {
                serverPlayerEntity.networkHandler.disconnect(Text.translatable("multiplayer.disconnect.banned"));
            }
        });
        return PlayerBansRpcDispatcher.get(dispatcher);
    }

    record ConfigEntry(PlayerConfigEntry player, @Nullable String reason, String source, Optional<Instant> expires) {
        static ConfigEntry of(BannedPlayerEntry entry) {
            return new ConfigEntry(Objects.requireNonNull((PlayerConfigEntry)entry.getKey()), entry.getReason(), entry.getSource(), Optional.ofNullable(entry.getExpiryDate()).map(Date::toInstant));
        }

        BannedPlayerEntry toBannedPlayerEntry() {
            return new BannedPlayerEntry(new PlayerConfigEntry(this.player().id(), this.player().name()), null, this.source(), (Date)this.expires().map(Date::from).orElse(null), this.reason());
        }
    }

    public record RpcEntry(RpcPlayer player, Optional<String> reason, Optional<String> source, Optional<Instant> expires) {
        public static final MapCodec<RpcEntry> CODEC = RecordCodecBuilder.mapCodec(instance -> instance.group((App)RpcPlayer.CODEC.codec().fieldOf("player").forGetter(RpcEntry::player), (App)Codec.STRING.optionalFieldOf("reason").forGetter(RpcEntry::reason), (App)Codec.STRING.optionalFieldOf("source").forGetter(RpcEntry::source), (App)Codecs.INSTANT.optionalFieldOf("expires").forGetter(RpcEntry::expires)).apply((Applicative)instance, RpcEntry::new));

        private static RpcEntry of(ConfigEntry entry) {
            return new RpcEntry(RpcPlayer.of(entry.player()), Optional.ofNullable(entry.reason()), Optional.of(entry.source()), entry.expires());
        }

        public static RpcEntry of(BannedPlayerEntry entry) {
            return RpcEntry.of(ConfigEntry.of(entry));
        }

        private ConfigEntry toConfigEntry(PlayerConfigEntry player) {
            return new ConfigEntry(player, this.reason().orElse(null), this.source().orElse(PlayerBansRpcDispatcher.DEFAULT_SOURCE), this.expires());
        }
    }
}


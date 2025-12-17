/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  com.google.common.net.InetAddresses
 *  com.mojang.datafixers.kinds.App
 *  com.mojang.datafixers.kinds.Applicative
 *  com.mojang.serialization.Codec
 *  com.mojang.serialization.MapCodec
 *  com.mojang.serialization.codecs.RecordCodecBuilder
 *  org.jspecify.annotations.Nullable
 */
package net.minecraft.server.dedicated.management.dispatch;

import com.google.common.net.InetAddresses;
import com.mojang.datafixers.kinds.App;
import com.mojang.datafixers.kinds.Applicative;
import com.mojang.serialization.Codec;
import com.mojang.serialization.MapCodec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import java.lang.invoke.MethodHandle;
import java.lang.runtime.ObjectMethods;
import java.time.Instant;
import java.util.Collection;
import java.util.Date;
import java.util.List;
import java.util.Objects;
import java.util.Optional;
import java.util.Set;
import java.util.stream.Collectors;
import net.minecraft.server.BannedIpEntry;
import net.minecraft.server.dedicated.management.RpcPlayer;
import net.minecraft.server.dedicated.management.dispatch.ManagementHandlerDispatcher;
import net.minecraft.server.dedicated.management.network.ManagementConnectionId;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.text.Text;
import net.minecraft.util.dynamic.Codecs;
import org.jspecify.annotations.Nullable;

public class IpBansRpcDispatcher {
    private static final String DEFAULT_SOURCE = "Management server";

    public static List<IpBanData> get(ManagementHandlerDispatcher dispatcher) {
        return dispatcher.getBanHandler().getIpBanList().stream().map(IpBanInfo::fromBannedIpEntry).map(IpBanData::fromIpBanInfo).toList();
    }

    public static List<IpBanData> add(ManagementHandlerDispatcher dispatcher, List<IncomingRpcIpBanData> entries, ManagementConnectionId remote) {
        entries.stream().map(ipAddress -> IpBansRpcDispatcher.banIpFromRpcEntry(dispatcher, ipAddress, remote)).flatMap(Collection::stream).forEach(ipBannedPlayer -> ipBannedPlayer.networkHandler.disconnect(Text.translatable("multiplayer.disconnect.ip_banned")));
        return IpBansRpcDispatcher.get(dispatcher);
    }

    private static List<ServerPlayerEntity> banIpFromRpcEntry(ManagementHandlerDispatcher dispatcher, IncomingRpcIpBanData entry, ManagementConnectionId remote) {
        Optional<ServerPlayerEntity> optional;
        IpBanInfo ipBanInfo = entry.toIpBanInfoOrNull();
        if (ipBanInfo != null) {
            return IpBansRpcDispatcher.banIp(dispatcher, ipBanInfo, remote);
        }
        if (entry.player().isPresent() && (optional = dispatcher.getPlayerListHandler().getPlayer(entry.player().get().id(), entry.player().get().name())).isPresent()) {
            return IpBansRpcDispatcher.banIp(dispatcher, entry.toIpBanInfoFromPlayer(optional.get()), remote);
        }
        return List.of();
    }

    private static List<ServerPlayerEntity> banIp(ManagementHandlerDispatcher dispatcher, IpBanInfo ipBanInfo, ManagementConnectionId remote) {
        dispatcher.getBanHandler().addIpAddress(ipBanInfo.toBannedIpEntry(), remote);
        return dispatcher.getPlayerListHandler().getPlayersByIpAddress(ipBanInfo.ipAddress());
    }

    public static List<IpBanData> clearIpBans(ManagementHandlerDispatcher dispatcher, ManagementConnectionId remote) {
        dispatcher.getBanHandler().clearIpBanList(remote);
        return IpBansRpcDispatcher.get(dispatcher);
    }

    public static List<IpBanData> remove(ManagementHandlerDispatcher dispatcher, List<String> ipAddresses, ManagementConnectionId remote) {
        ipAddresses.forEach(address -> dispatcher.getBanHandler().removeIpAddress((String)address, remote));
        return IpBansRpcDispatcher.get(dispatcher);
    }

    public static List<IpBanData> set(ManagementHandlerDispatcher dispatcher, List<IpBanData> entries, ManagementConnectionId remote) {
        Set set = entries.stream().filter(ipBanInfo -> InetAddresses.isInetAddress((String)ipBanInfo.ipAddress())).map(IpBanData::toIpBanInfo).collect(Collectors.toSet());
        Set set2 = dispatcher.getBanHandler().getIpBanList().stream().map(IpBanInfo::fromBannedIpEntry).collect(Collectors.toSet());
        set2.stream().filter(ipBanInfo -> !set.contains(ipBanInfo)).forEach(ipBanInfoToRemove -> dispatcher.getBanHandler().removeIpAddress(ipBanInfoToRemove.ipAddress(), remote));
        set.stream().filter(ipBanInfo -> !set2.contains(ipBanInfo)).forEach(ipBanInfoToAdd -> dispatcher.getBanHandler().addIpAddress(ipBanInfoToAdd.toBannedIpEntry(), remote));
        set.stream().filter(ipBanInfo -> !set2.contains(ipBanInfo)).flatMap(newAddedIpBanInfo -> dispatcher.getPlayerListHandler().getPlayersByIpAddress(newAddedIpBanInfo.ipAddress()).stream()).forEach(ipBanInfo -> ipBanInfo.networkHandler.disconnect(Text.translatable("multiplayer.disconnect.ip_banned")));
        return IpBansRpcDispatcher.get(dispatcher);
    }

    public record IncomingRpcIpBanData(Optional<RpcPlayer> player, Optional<String> ipAddress, Optional<String> reason, Optional<String> source, Optional<Instant> expires) {
        public static final MapCodec<IncomingRpcIpBanData> CODEC = RecordCodecBuilder.mapCodec(instance -> instance.group((App)RpcPlayer.CODEC.codec().optionalFieldOf("player").forGetter(IncomingRpcIpBanData::player), (App)Codec.STRING.optionalFieldOf("ip").forGetter(IncomingRpcIpBanData::ipAddress), (App)Codec.STRING.optionalFieldOf("reason").forGetter(IncomingRpcIpBanData::reason), (App)Codec.STRING.optionalFieldOf("source").forGetter(IncomingRpcIpBanData::source), (App)Codecs.INSTANT.optionalFieldOf("expires").forGetter(IncomingRpcIpBanData::expires)).apply((Applicative)instance, IncomingRpcIpBanData::new));

        IpBanInfo toIpBanInfoFromPlayer(ServerPlayerEntity player) {
            return new IpBanInfo(player.getIp(), this.reason().orElse(null), this.source().orElse(IpBansRpcDispatcher.DEFAULT_SOURCE), this.expires());
        }

        @Nullable IpBanInfo toIpBanInfoOrNull() {
            if (this.ipAddress().isEmpty() || !InetAddresses.isInetAddress((String)this.ipAddress().get())) {
                return null;
            }
            return new IpBanInfo(this.ipAddress().get(), this.reason().orElse(null), this.source().orElse(IpBansRpcDispatcher.DEFAULT_SOURCE), this.expires());
        }

        @Override
        public final String toString() {
            return ObjectMethods.bootstrap("toString", new MethodHandle[]{IncomingRpcIpBanData.class, "player;ip;reason;source;expires", "player", "ipAddress", "reason", "source", "expires"}, this);
        }

        @Override
        public final int hashCode() {
            return (int)ObjectMethods.bootstrap("hashCode", new MethodHandle[]{IncomingRpcIpBanData.class, "player;ip;reason;source;expires", "player", "ipAddress", "reason", "source", "expires"}, this);
        }

        @Override
        public final boolean equals(Object object) {
            return (boolean)ObjectMethods.bootstrap("equals", new MethodHandle[]{IncomingRpcIpBanData.class, "player;ip;reason;source;expires", "player", "ipAddress", "reason", "source", "expires"}, this, object);
        }
    }

    record IpBanInfo(String ipAddress, @Nullable String reason, String source, Optional<Instant> expires) {
        static IpBanInfo fromBannedIpEntry(BannedIpEntry bannedIpEntry) {
            return new IpBanInfo(Objects.requireNonNull((String)bannedIpEntry.getKey()), bannedIpEntry.getReason(), bannedIpEntry.getSource(), Optional.ofNullable(bannedIpEntry.getExpiryDate()).map(Date::toInstant));
        }

        BannedIpEntry toBannedIpEntry() {
            return new BannedIpEntry(this.ipAddress(), null, this.source(), (Date)this.expires().map(Date::from).orElse(null), this.reason());
        }

        @Override
        public final String toString() {
            return ObjectMethods.bootstrap("toString", new MethodHandle[]{IpBanInfo.class, "ip;reason;source;expires", "ipAddress", "reason", "source", "expires"}, this);
        }

        @Override
        public final int hashCode() {
            return (int)ObjectMethods.bootstrap("hashCode", new MethodHandle[]{IpBanInfo.class, "ip;reason;source;expires", "ipAddress", "reason", "source", "expires"}, this);
        }

        @Override
        public final boolean equals(Object object) {
            return (boolean)ObjectMethods.bootstrap("equals", new MethodHandle[]{IpBanInfo.class, "ip;reason;source;expires", "ipAddress", "reason", "source", "expires"}, this, object);
        }
    }

    public record IpBanData(String ipAddress, Optional<String> reason, Optional<String> source, Optional<Instant> expires) {
        public static final MapCodec<IpBanData> CODEC = RecordCodecBuilder.mapCodec(instance -> instance.group((App)Codec.STRING.fieldOf("ip").forGetter(IpBanData::ipAddress), (App)Codec.STRING.optionalFieldOf("reason").forGetter(IpBanData::reason), (App)Codec.STRING.optionalFieldOf("source").forGetter(IpBanData::source), (App)Codecs.INSTANT.optionalFieldOf("expires").forGetter(IpBanData::expires)).apply((Applicative)instance, IpBanData::new));

        private static IpBanData fromIpBanInfo(IpBanInfo ipBanInfo) {
            return new IpBanData(ipBanInfo.ipAddress(), Optional.ofNullable(ipBanInfo.reason()), Optional.of(ipBanInfo.source()), ipBanInfo.expires());
        }

        public static IpBanData fromBannedIpEntry(BannedIpEntry bannedIpEntry) {
            return IpBanData.fromIpBanInfo(IpBanInfo.fromBannedIpEntry(bannedIpEntry));
        }

        private IpBanInfo toIpBanInfo() {
            return new IpBanInfo(this.ipAddress(), this.reason().orElse(null), this.source().orElse(IpBansRpcDispatcher.DEFAULT_SOURCE), this.expires());
        }

        @Override
        public final String toString() {
            return ObjectMethods.bootstrap("toString", new MethodHandle[]{IpBanData.class, "ip;reason;source;expires", "ipAddress", "reason", "source", "expires"}, this);
        }

        @Override
        public final int hashCode() {
            return (int)ObjectMethods.bootstrap("hashCode", new MethodHandle[]{IpBanData.class, "ip;reason;source;expires", "ipAddress", "reason", "source", "expires"}, this);
        }

        @Override
        public final boolean equals(Object object) {
            return (boolean)ObjectMethods.bootstrap("equals", new MethodHandle[]{IpBanData.class, "ip;reason;source;expires", "ipAddress", "reason", "source", "expires"}, this, object);
        }
    }
}


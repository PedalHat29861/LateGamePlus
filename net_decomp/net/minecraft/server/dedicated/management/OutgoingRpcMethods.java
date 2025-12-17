/*
 * Decompiled with CFR 0.152.
 */
package net.minecraft.server.dedicated.management;

import net.minecraft.registry.entry.RegistryEntry;
import net.minecraft.server.dedicated.management.OutgoingRpcMethod;
import net.minecraft.server.dedicated.management.RpcPlayer;
import net.minecraft.server.dedicated.management.dispatch.GameRuleRpcDispatcher;
import net.minecraft.server.dedicated.management.dispatch.IpBansRpcDispatcher;
import net.minecraft.server.dedicated.management.dispatch.OperatorsRpcDispatcher;
import net.minecraft.server.dedicated.management.dispatch.PlayerBansRpcDispatcher;
import net.minecraft.server.dedicated.management.dispatch.ServerRpcDispatcher;
import net.minecraft.server.dedicated.management.schema.RpcSchema;

public class OutgoingRpcMethods {
    public static final RegistryEntry.Reference<OutgoingRpcMethod<Void, Void>> SERVER_STARTED = OutgoingRpcMethod.createSimpleBuilder().description("Server started").buildAndRegisterVanilla("server/started");
    public static final RegistryEntry.Reference<OutgoingRpcMethod<Void, Void>> SERVER_STOPPING = OutgoingRpcMethod.createSimpleBuilder().description("Server shutting down").buildAndRegisterVanilla("server/stopping");
    public static final RegistryEntry.Reference<OutgoingRpcMethod<Void, Void>> SERVER_SAVING = OutgoingRpcMethod.createSimpleBuilder().description("Server save started").buildAndRegisterVanilla("server/saving");
    public static final RegistryEntry.Reference<OutgoingRpcMethod<Void, Void>> SERVER_SAVED = OutgoingRpcMethod.createSimpleBuilder().description("Server save completed").buildAndRegisterVanilla("server/saved");
    public static final RegistryEntry.Reference<OutgoingRpcMethod<Void, Void>> SERVER_ACTIVITY = OutgoingRpcMethod.createSimpleBuilder().description("Server activity occurred. Rate limited to 1 notification per 30 seconds").buildAndRegisterVanilla("server/activity");
    public static final RegistryEntry.Reference<OutgoingRpcMethod<RpcPlayer, Void>> PLAYER_JOINED = OutgoingRpcMethod.createNotificationBuilder().requestParameter("player", RpcSchema.PLAYER.ref()).description("Player joined").buildAndRegisterVanilla("players/joined");
    public static final RegistryEntry.Reference<OutgoingRpcMethod<RpcPlayer, Void>> PLAYER_LEFT = OutgoingRpcMethod.createNotificationBuilder().requestParameter("player", RpcSchema.PLAYER.ref()).description("Player left").buildAndRegisterVanilla("players/left");
    public static final RegistryEntry.Reference<OutgoingRpcMethod<OperatorsRpcDispatcher.RpcEntry, Void>> OPERATOR_ADDED = OutgoingRpcMethod.createNotificationBuilder().requestParameter("player", RpcSchema.OPERATOR.ref()).description("Player was oped").buildAndRegisterVanilla("operators/added");
    public static final RegistryEntry.Reference<OutgoingRpcMethod<OperatorsRpcDispatcher.RpcEntry, Void>> OPERATOR_REMOVED = OutgoingRpcMethod.createNotificationBuilder().requestParameter("player", RpcSchema.OPERATOR.ref()).description("Player was deoped").buildAndRegisterVanilla("operators/removed");
    public static final RegistryEntry.Reference<OutgoingRpcMethod<RpcPlayer, Void>> ALLOWLIST_ADDED = OutgoingRpcMethod.createNotificationBuilder().requestParameter("player", RpcSchema.PLAYER.ref()).description("Player was added to allowlist").buildAndRegisterVanilla("allowlist/added");
    public static final RegistryEntry.Reference<OutgoingRpcMethod<RpcPlayer, Void>> ALLOWLIST_REMOVED = OutgoingRpcMethod.createNotificationBuilder().requestParameter("player", RpcSchema.PLAYER.ref()).description("Player was removed from allowlist").buildAndRegisterVanilla("allowlist/removed");
    public static final RegistryEntry.Reference<OutgoingRpcMethod<IpBansRpcDispatcher.IpBanData, Void>> IP_BAN_ADDED = OutgoingRpcMethod.createNotificationBuilder().requestParameter("player", RpcSchema.IP_BAN.ref()).description("Ip was added to ip ban list").buildAndRegisterVanilla("ip_bans/added");
    public static final RegistryEntry.Reference<OutgoingRpcMethod<String, Void>> IP_BAN_REMOVED = OutgoingRpcMethod.createNotificationBuilder().requestParameter("player", RpcSchema.STRING).description("Ip was removed from ip ban list").buildAndRegisterVanilla("ip_bans/removed");
    public static final RegistryEntry.Reference<OutgoingRpcMethod<PlayerBansRpcDispatcher.RpcEntry, Void>> BAN_ADDED = OutgoingRpcMethod.createNotificationBuilder().requestParameter("player", RpcSchema.USER_BAN.ref()).description("Player was added to ban list").buildAndRegisterVanilla("bans/added");
    public static final RegistryEntry.Reference<OutgoingRpcMethod<RpcPlayer, Void>> BAN_REMOVED = OutgoingRpcMethod.createNotificationBuilder().requestParameter("player", RpcSchema.PLAYER.ref()).description("Player was removed from ban list").buildAndRegisterVanilla("bans/removed");
    public static final RegistryEntry.Reference<OutgoingRpcMethod<GameRuleRpcDispatcher.RuleEntry<?>, Void>> GAMERULE_UPDATED = OutgoingRpcMethod.createNotificationBuilder().requestParameter("gamerule", RpcSchema.TYPED_GAME_RULE.ref()).description("Gamerule was changed").buildAndRegisterVanilla("gamerules/updated");
    public static final RegistryEntry.Reference<OutgoingRpcMethod<ServerRpcDispatcher.RpcStatus, Void>> SERVER_STATUS_HEARTBEAT = OutgoingRpcMethod.createNotificationBuilder().requestParameter("status", RpcSchema.SERVER_STATE.ref()).description("Server status heartbeat").buildAndRegisterVanilla("server/status");
}


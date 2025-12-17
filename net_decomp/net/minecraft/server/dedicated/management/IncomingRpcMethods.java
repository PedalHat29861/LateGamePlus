/*
 * Decompiled with CFR 0.152.
 */
package net.minecraft.server.dedicated.management;

import net.minecraft.registry.Registry;
import net.minecraft.server.dedicated.management.IncomingRpcMethod;
import net.minecraft.server.dedicated.management.RpcDiscover;
import net.minecraft.server.dedicated.management.dispatch.AllowlistRpcDispatcher;
import net.minecraft.server.dedicated.management.dispatch.GameRuleRpcDispatcher;
import net.minecraft.server.dedicated.management.dispatch.IpBansRpcDispatcher;
import net.minecraft.server.dedicated.management.dispatch.OperatorsRpcDispatcher;
import net.minecraft.server.dedicated.management.dispatch.PlayerBansRpcDispatcher;
import net.minecraft.server.dedicated.management.dispatch.PlayersRpcDispatcher;
import net.minecraft.server.dedicated.management.dispatch.PropertiesRpcDispatcher;
import net.minecraft.server.dedicated.management.dispatch.ServerRpcDispatcher;
import net.minecraft.server.dedicated.management.schema.RpcSchema;

public class IncomingRpcMethods {
    public static IncomingRpcMethod<?, ?> registerAndGetDefault(Registry<IncomingRpcMethod<?, ?>> registry) {
        IncomingRpcMethods.registerAllowlist(registry);
        IncomingRpcMethods.registerBans(registry);
        IncomingRpcMethods.registerIpBans(registry);
        IncomingRpcMethods.registerPlayers(registry);
        IncomingRpcMethods.registerOperators(registry);
        IncomingRpcMethods.registerServer(registry);
        IncomingRpcMethods.registerProperties(registry);
        IncomingRpcMethods.registerGameRule(registry);
        return IncomingRpcMethod.createParameterlessBuilder(dispatcher -> RpcDiscover.handleRpcDiscover(RpcSchema.getRegisteredSchemas())).noRequireMainThread().notDiscoverable().result("result", RpcSchema.DOCUMENT).buildAndRegisterVanilla(registry, "rpc.discover");
    }

    private static void registerAllowlist(Registry<IncomingRpcMethod<?, ?>> registry) {
        IncomingRpcMethod.createParameterlessBuilder(AllowlistRpcDispatcher::get).description("Get the allowlist").result("allowlist", RpcSchema.PLAYER.array()).buildAndRegisterVanilla(registry, "allowlist");
        IncomingRpcMethod.createParameterizedBuilder(AllowlistRpcDispatcher::set).description("Set the allowlist").parameter("players", RpcSchema.PLAYER.array()).result("allowlist", RpcSchema.PLAYER.array()).buildAndRegisterVanilla(registry, "allowlist/set");
        IncomingRpcMethod.createParameterizedBuilder(AllowlistRpcDispatcher::add).description("Add players to allowlist").parameter("add", RpcSchema.PLAYER.array()).result("allowlist", RpcSchema.PLAYER.array()).buildAndRegisterVanilla(registry, "allowlist/add");
        IncomingRpcMethod.createParameterizedBuilder(AllowlistRpcDispatcher::remove).description("Remove players from allowlist").parameter("remove", RpcSchema.PLAYER.array()).result("allowlist", RpcSchema.PLAYER.array()).buildAndRegisterVanilla(registry, "allowlist/remove");
        IncomingRpcMethod.createParameterlessBuilder(AllowlistRpcDispatcher::clear).description("Clear all players in allowlist").result("allowlist", RpcSchema.PLAYER.array()).buildAndRegisterVanilla(registry, "allowlist/clear");
    }

    private static void registerBans(Registry<IncomingRpcMethod<?, ?>> registry) {
        IncomingRpcMethod.createParameterlessBuilder(PlayerBansRpcDispatcher::get).description("Get the ban list").result("banlist", RpcSchema.USER_BAN.array()).buildAndRegisterVanilla(registry, "bans");
        IncomingRpcMethod.createParameterizedBuilder(PlayerBansRpcDispatcher::set).description("Set the banlist").parameter("bans", RpcSchema.USER_BAN.array()).result("banlist", RpcSchema.USER_BAN.array()).buildAndRegisterVanilla(registry, "bans/set");
        IncomingRpcMethod.createParameterizedBuilder(PlayerBansRpcDispatcher::add).description("Add players to ban list").parameter("add", RpcSchema.USER_BAN.array()).result("banlist", RpcSchema.USER_BAN.array()).buildAndRegisterVanilla(registry, "bans/add");
        IncomingRpcMethod.createParameterizedBuilder(PlayerBansRpcDispatcher::remove).description("Remove players from ban list").parameter("remove", RpcSchema.PLAYER.array()).result("banlist", RpcSchema.USER_BAN.array()).buildAndRegisterVanilla(registry, "bans/remove");
        IncomingRpcMethod.createParameterlessBuilder(PlayerBansRpcDispatcher::clear).description("Clear all players in ban list").result("banlist", RpcSchema.USER_BAN.array()).buildAndRegisterVanilla(registry, "bans/clear");
    }

    private static void registerIpBans(Registry<IncomingRpcMethod<?, ?>> registry) {
        IncomingRpcMethod.createParameterlessBuilder(IpBansRpcDispatcher::get).description("Get the ip ban list").result("banlist", RpcSchema.IP_BAN.array()).buildAndRegisterVanilla(registry, "ip_bans");
        IncomingRpcMethod.createParameterizedBuilder(IpBansRpcDispatcher::set).description("Set the ip banlist").parameter("banlist", RpcSchema.IP_BAN.array()).result("banlist", RpcSchema.IP_BAN.array()).buildAndRegisterVanilla(registry, "ip_bans/set");
        IncomingRpcMethod.createParameterizedBuilder(IpBansRpcDispatcher::add).description("Add ip to ban list").parameter("add", RpcSchema.INCOMING_IP_BAN.array()).result("banlist", RpcSchema.IP_BAN.array()).buildAndRegisterVanilla(registry, "ip_bans/add");
        IncomingRpcMethod.createParameterizedBuilder(IpBansRpcDispatcher::remove).description("Remove ip from ban list").parameter("ip", RpcSchema.STRING.asArray()).result("banlist", RpcSchema.IP_BAN.array()).buildAndRegisterVanilla(registry, "ip_bans/remove");
        IncomingRpcMethod.createParameterlessBuilder(IpBansRpcDispatcher::clearIpBans).description("Clear all ips in ban list").result("banlist", RpcSchema.IP_BAN.array()).buildAndRegisterVanilla(registry, "ip_bans/clear");
    }

    private static void registerPlayers(Registry<IncomingRpcMethod<?, ?>> registry) {
        IncomingRpcMethod.createParameterlessBuilder(PlayersRpcDispatcher::get).description("Get all connected players").result("players", RpcSchema.PLAYER.array()).buildAndRegisterVanilla(registry, "players");
        IncomingRpcMethod.createParameterizedBuilder(PlayersRpcDispatcher::kick).description("Kick players").parameter("kick", RpcSchema.KICK_PLAYER.array()).result("kicked", RpcSchema.PLAYER.array()).buildAndRegisterVanilla(registry, "players/kick");
    }

    private static void registerOperators(Registry<IncomingRpcMethod<?, ?>> registry) {
        IncomingRpcMethod.createParameterlessBuilder(OperatorsRpcDispatcher::get).description("Get all oped players").result("operators", RpcSchema.OPERATOR.array()).buildAndRegisterVanilla(registry, "operators");
        IncomingRpcMethod.createParameterizedBuilder(OperatorsRpcDispatcher::set).description("Set all oped players").parameter("operators", RpcSchema.OPERATOR.array()).result("operators", RpcSchema.OPERATOR.array()).buildAndRegisterVanilla(registry, "operators/set");
        IncomingRpcMethod.createParameterizedBuilder(OperatorsRpcDispatcher::add).description("Op players").parameter("add", RpcSchema.OPERATOR.array()).result("operators", RpcSchema.OPERATOR.array()).buildAndRegisterVanilla(registry, "operators/add");
        IncomingRpcMethod.createParameterizedBuilder(OperatorsRpcDispatcher::remove).description("Deop players").parameter("remove", RpcSchema.PLAYER.array()).result("operators", RpcSchema.OPERATOR.array()).buildAndRegisterVanilla(registry, "operators/remove");
        IncomingRpcMethod.createParameterlessBuilder(OperatorsRpcDispatcher::clear).description("Deop all players").result("operators", RpcSchema.OPERATOR.array()).buildAndRegisterVanilla(registry, "operators/clear");
    }

    private static void registerServer(Registry<IncomingRpcMethod<?, ?>> registry) {
        IncomingRpcMethod.createParameterlessBuilder(ServerRpcDispatcher::status).description("Get server status").result("status", RpcSchema.SERVER_STATE.ref()).buildAndRegisterVanilla(registry, "server/status");
        IncomingRpcMethod.createParameterizedBuilder(ServerRpcDispatcher::save).description("Save server state").parameter("flush", RpcSchema.BOOLEAN).result("saving", RpcSchema.BOOLEAN).buildAndRegisterVanilla(registry, "server/save");
        IncomingRpcMethod.createParameterlessBuilder(ServerRpcDispatcher::stop).description("Stop server").result("stopping", RpcSchema.BOOLEAN).buildAndRegisterVanilla(registry, "server/stop");
        IncomingRpcMethod.createParameterizedBuilder(ServerRpcDispatcher::systemMessage).description("Send a system message").parameter("message", RpcSchema.SYSTEM_MESSAGE.ref()).result("sent", RpcSchema.BOOLEAN).buildAndRegisterVanilla(registry, "server/system_message");
    }

    private static void registerProperties(Registry<IncomingRpcMethod<?, ?>> registry) {
        IncomingRpcMethod.createParameterlessBuilder(PropertiesRpcDispatcher::getAutosave).description("Get whether automatic world saving is enabled on the server").result("enabled", RpcSchema.BOOLEAN).buildAndRegisterVanilla(registry, "serversettings/autosave");
        IncomingRpcMethod.createParameterizedBuilder(PropertiesRpcDispatcher::setAutosave).description("Enable or disable automatic world saving on the server").parameter("enable", RpcSchema.BOOLEAN).result("enabled", RpcSchema.BOOLEAN).buildAndRegisterVanilla(registry, "serversettings/autosave/set");
        IncomingRpcMethod.createParameterlessBuilder(PropertiesRpcDispatcher::getDifficulty).description("Get the current difficulty level of the server").result("difficulty", RpcSchema.DIFFICULTY.ref()).buildAndRegisterVanilla(registry, "serversettings/difficulty");
        IncomingRpcMethod.createParameterizedBuilder(PropertiesRpcDispatcher::setDifficulty).description("Set the difficulty level of the server").parameter("difficulty", RpcSchema.DIFFICULTY.ref()).result("difficulty", RpcSchema.DIFFICULTY.ref()).buildAndRegisterVanilla(registry, "serversettings/difficulty/set");
        IncomingRpcMethod.createParameterlessBuilder(PropertiesRpcDispatcher::getEnforceAllowlist).description("Get whether allowlist enforcement is enabled (kicks players immediately when removed from allowlist)").result("enforced", RpcSchema.BOOLEAN).buildAndRegisterVanilla(registry, "serversettings/enforce_allowlist");
        IncomingRpcMethod.createParameterizedBuilder(PropertiesRpcDispatcher::setEnforceAllowlist).description("Enable or disable allowlist enforcement (when enabled, players are kicked immediately upon removal from allowlist)").parameter("enforce", RpcSchema.BOOLEAN).result("enforced", RpcSchema.BOOLEAN).buildAndRegisterVanilla(registry, "serversettings/enforce_allowlist/set");
        IncomingRpcMethod.createParameterlessBuilder(PropertiesRpcDispatcher::getUseAllowlist).description("Get whether the allowlist is enabled on the server").result("used", RpcSchema.BOOLEAN).buildAndRegisterVanilla(registry, "serversettings/use_allowlist");
        IncomingRpcMethod.createParameterizedBuilder(PropertiesRpcDispatcher::setUseAllowlist).description("Enable or disable the allowlist on the server (controls whether only allowlisted players can join)").parameter("use", RpcSchema.BOOLEAN).result("used", RpcSchema.BOOLEAN).buildAndRegisterVanilla(registry, "serversettings/use_allowlist/set");
        IncomingRpcMethod.createParameterlessBuilder(PropertiesRpcDispatcher::getMaxPlayers).description("Get the maximum number of players allowed to connect to the server").result("max", RpcSchema.INTEGER).buildAndRegisterVanilla(registry, "serversettings/max_players");
        IncomingRpcMethod.createParameterizedBuilder(PropertiesRpcDispatcher::setMaxPlayers).description("Set the maximum number of players allowed to connect to the server").parameter("max", RpcSchema.INTEGER).result("max", RpcSchema.INTEGER).buildAndRegisterVanilla(registry, "serversettings/max_players/set");
        IncomingRpcMethod.createParameterlessBuilder(PropertiesRpcDispatcher::getPauseWhenEmptySeconds).description("Get the number of seconds before the game is automatically paused when no players are online").result("seconds", RpcSchema.INTEGER).buildAndRegisterVanilla(registry, "serversettings/pause_when_empty_seconds");
        IncomingRpcMethod.createParameterizedBuilder(PropertiesRpcDispatcher::setPauseWhenEmptySeconds).description("Set the number of seconds before the game is automatically paused when no players are online").parameter("seconds", RpcSchema.INTEGER).result("seconds", RpcSchema.INTEGER).buildAndRegisterVanilla(registry, "serversettings/pause_when_empty_seconds/set");
        IncomingRpcMethod.createParameterlessBuilder(PropertiesRpcDispatcher::getPlayerIdleTimeout).description("Get the number of seconds before idle players are automatically kicked from the server").result("seconds", RpcSchema.INTEGER).buildAndRegisterVanilla(registry, "serversettings/player_idle_timeout");
        IncomingRpcMethod.createParameterizedBuilder(PropertiesRpcDispatcher::setPlayerIdleTimeout).description("Set the number of seconds before idle players are automatically kicked from the server").parameter("seconds", RpcSchema.INTEGER).result("seconds", RpcSchema.INTEGER).buildAndRegisterVanilla(registry, "serversettings/player_idle_timeout/set");
        IncomingRpcMethod.createParameterlessBuilder(PropertiesRpcDispatcher::getAllowFlight).description("Get whether flight is allowed for players in Survival mode").result("allowed", RpcSchema.BOOLEAN).buildAndRegisterVanilla(registry, "serversettings/allow_flight");
        IncomingRpcMethod.createParameterizedBuilder(PropertiesRpcDispatcher::setAllowFlight).description("Allow or disallow flight for players in Survival mode").parameter("allow", RpcSchema.BOOLEAN).result("allowed", RpcSchema.BOOLEAN).buildAndRegisterVanilla(registry, "serversettings/allow_flight/set");
        IncomingRpcMethod.createParameterlessBuilder(PropertiesRpcDispatcher::getMotd).description("Get the server's message of the day displayed to players").result("message", RpcSchema.STRING).buildAndRegisterVanilla(registry, "serversettings/motd");
        IncomingRpcMethod.createParameterizedBuilder(PropertiesRpcDispatcher::setMotd).description("Set the server's message of the day displayed to players").parameter("message", RpcSchema.STRING).result("message", RpcSchema.STRING).buildAndRegisterVanilla(registry, "serversettings/motd/set");
        IncomingRpcMethod.createParameterlessBuilder(PropertiesRpcDispatcher::getSpawnProtectionRadius).description("Get the spawn protection radius in blocks (only operators can edit within this area)").result("radius", RpcSchema.INTEGER).buildAndRegisterVanilla(registry, "serversettings/spawn_protection_radius");
        IncomingRpcMethod.createParameterizedBuilder(PropertiesRpcDispatcher::setSpawnProtectionRadius).description("Set the spawn protection radius in blocks (only operators can edit within this area)").parameter("radius", RpcSchema.INTEGER).result("radius", RpcSchema.INTEGER).buildAndRegisterVanilla(registry, "serversettings/spawn_protection_radius/set");
        IncomingRpcMethod.createParameterlessBuilder(PropertiesRpcDispatcher::getForceGameMode).description("Get whether players are forced to use the server's default game mode").result("forced", RpcSchema.BOOLEAN).buildAndRegisterVanilla(registry, "serversettings/force_game_mode");
        IncomingRpcMethod.createParameterizedBuilder(PropertiesRpcDispatcher::setForceGameMode).description("Enable or disable forcing players to use the server's default game mode").parameter("force", RpcSchema.BOOLEAN).result("forced", RpcSchema.BOOLEAN).buildAndRegisterVanilla(registry, "serversettings/force_game_mode/set");
        IncomingRpcMethod.createParameterlessBuilder(PropertiesRpcDispatcher::getGameMode).description("Get the server's default game mode").result("mode", RpcSchema.GAME_MODE.ref()).buildAndRegisterVanilla(registry, "serversettings/game_mode");
        IncomingRpcMethod.createParameterizedBuilder(PropertiesRpcDispatcher::setGameMode).description("Set the server's default game mode").parameter("mode", RpcSchema.GAME_MODE.ref()).result("mode", RpcSchema.GAME_MODE.ref()).buildAndRegisterVanilla(registry, "serversettings/game_mode/set");
        IncomingRpcMethod.createParameterlessBuilder(PropertiesRpcDispatcher::getViewDistance).description("Get the server's view distance in chunks").result("distance", RpcSchema.INTEGER).buildAndRegisterVanilla(registry, "serversettings/view_distance");
        IncomingRpcMethod.createParameterizedBuilder(PropertiesRpcDispatcher::setViewDistance).description("Set the server's view distance in chunks").parameter("distance", RpcSchema.INTEGER).result("distance", RpcSchema.INTEGER).buildAndRegisterVanilla(registry, "serversettings/view_distance/set");
        IncomingRpcMethod.createParameterlessBuilder(PropertiesRpcDispatcher::getSimulationDistance).description("Get the server's simulation distance in chunks").result("distance", RpcSchema.INTEGER).buildAndRegisterVanilla(registry, "serversettings/simulation_distance");
        IncomingRpcMethod.createParameterizedBuilder(PropertiesRpcDispatcher::setSimulationDistance).description("Set the server's simulation distance in chunks").parameter("distance", RpcSchema.INTEGER).result("distance", RpcSchema.INTEGER).buildAndRegisterVanilla(registry, "serversettings/simulation_distance/set");
        IncomingRpcMethod.createParameterlessBuilder(PropertiesRpcDispatcher::getAcceptTransfers).description("Get whether the server accepts player transfers from other servers").result("accepted", RpcSchema.BOOLEAN).buildAndRegisterVanilla(registry, "serversettings/accept_transfers");
        IncomingRpcMethod.createParameterizedBuilder(PropertiesRpcDispatcher::setAcceptTransfers).description("Enable or disable accepting player transfers from other servers").parameter("accept", RpcSchema.BOOLEAN).result("accepted", RpcSchema.BOOLEAN).buildAndRegisterVanilla(registry, "serversettings/accept_transfers/set");
        IncomingRpcMethod.createParameterlessBuilder(PropertiesRpcDispatcher::getStatusHeartbeatInterval).description("Get the interval in seconds between server status heartbeats").result("seconds", RpcSchema.INTEGER).buildAndRegisterVanilla(registry, "serversettings/status_heartbeat_interval");
        IncomingRpcMethod.createParameterizedBuilder(PropertiesRpcDispatcher::setStatusHeartbeatInterval).description("Set the interval in seconds between server status heartbeats").parameter("seconds", RpcSchema.INTEGER).result("seconds", RpcSchema.INTEGER).buildAndRegisterVanilla(registry, "serversettings/status_heartbeat_interval/set");
        IncomingRpcMethod.createParameterlessBuilder(PropertiesRpcDispatcher::getOperatorUserPermissionLevel).description("Get default operator permission level").result("level", RpcSchema.PERMISSION_LEVEL).buildAndRegisterVanilla(registry, "serversettings/operator_user_permission_level");
        IncomingRpcMethod.createParameterizedBuilder(PropertiesRpcDispatcher::setOperatorUserPermissionLevel).description("Set default operator permission level").parameter("level", RpcSchema.PERMISSION_LEVEL).result("level", RpcSchema.PERMISSION_LEVEL).buildAndRegisterVanilla(registry, "serversettings/operator_user_permission_level/set");
        IncomingRpcMethod.createParameterlessBuilder(PropertiesRpcDispatcher::getHideOnlinePlayers).description("Get whether the server hides online player information from status queries").result("hidden", RpcSchema.BOOLEAN).buildAndRegisterVanilla(registry, "serversettings/hide_online_players");
        IncomingRpcMethod.createParameterizedBuilder(PropertiesRpcDispatcher::setHideOnlinePlayers).description("Enable or disable hiding online player information from status queries").parameter("hide", RpcSchema.BOOLEAN).result("hidden", RpcSchema.BOOLEAN).buildAndRegisterVanilla(registry, "serversettings/hide_online_players/set");
        IncomingRpcMethod.createParameterlessBuilder(PropertiesRpcDispatcher::getStatusReplies).description("Get whether the server responds to connection status requests").result("enabled", RpcSchema.BOOLEAN).buildAndRegisterVanilla(registry, "serversettings/status_replies");
        IncomingRpcMethod.createParameterizedBuilder(PropertiesRpcDispatcher::setStatusReplies).description("Enable or disable the server responding to connection status requests").parameter("enable", RpcSchema.BOOLEAN).result("enabled", RpcSchema.BOOLEAN).buildAndRegisterVanilla(registry, "serversettings/status_replies/set");
        IncomingRpcMethod.createParameterlessBuilder(PropertiesRpcDispatcher::getEntityBroadcastRange).description("Get the entity broadcast range as a percentage").result("percentage_points", RpcSchema.INTEGER).buildAndRegisterVanilla(registry, "serversettings/entity_broadcast_range");
        IncomingRpcMethod.createParameterizedBuilder(PropertiesRpcDispatcher::setEntityBroadcastRange).description("Set the entity broadcast range as a percentage").parameter("percentage_points", RpcSchema.INTEGER).result("percentage_points", RpcSchema.INTEGER).buildAndRegisterVanilla(registry, "serversettings/entity_broadcast_range/set");
    }

    private static void registerGameRule(Registry<IncomingRpcMethod<?, ?>> registry) {
        IncomingRpcMethod.createParameterlessBuilder(GameRuleRpcDispatcher::get).description("Get the available game rule keys and their current values").result("gamerules", RpcSchema.TYPED_GAME_RULE.ref().asArray()).buildAndRegisterVanilla(registry, "gamerules");
        IncomingRpcMethod.createParameterizedBuilder(GameRuleRpcDispatcher::updateRule).description("Update game rule value").parameter("gamerule", RpcSchema.UNTYPED_GAME_RULE.ref()).result("gamerule", RpcSchema.TYPED_GAME_RULE.ref()).buildAndRegisterVanilla(registry, "gamerules/update");
    }
}


/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  com.mojang.brigadier.CommandDispatcher
 *  com.mojang.brigadier.arguments.StringArgumentType
 *  com.mojang.brigadier.builder.LiteralArgumentBuilder
 *  com.mojang.brigadier.builder.RequiredArgumentBuilder
 *  com.mojang.brigadier.context.CommandContext
 */
package net.minecraft.server.command;

import com.mojang.brigadier.CommandDispatcher;
import com.mojang.brigadier.arguments.StringArgumentType;
import com.mojang.brigadier.builder.LiteralArgumentBuilder;
import com.mojang.brigadier.builder.RequiredArgumentBuilder;
import com.mojang.brigadier.context.CommandContext;
import java.nio.charset.StandardCharsets;
import java.util.Optional;
import java.util.UUID;
import net.minecraft.command.argument.UuidArgumentType;
import net.minecraft.network.packet.Packet;
import net.minecraft.network.packet.s2c.common.ResourcePackRemoveS2CPacket;
import net.minecraft.network.packet.s2c.common.ResourcePackSendS2CPacket;
import net.minecraft.server.command.CommandManager;
import net.minecraft.server.command.ServerCommandSource;

public class ServerPackCommand {
    public static void register(CommandDispatcher<ServerCommandSource> dispatcher) {
        dispatcher.register((LiteralArgumentBuilder)((LiteralArgumentBuilder)((LiteralArgumentBuilder)CommandManager.literal("serverpack").requires(CommandManager.requirePermissionLevel(CommandManager.GAMEMASTERS_CHECK))).then(CommandManager.literal("push").then(((RequiredArgumentBuilder)CommandManager.argument("url", StringArgumentType.string()).then(((RequiredArgumentBuilder)CommandManager.argument("uuid", UuidArgumentType.uuid()).then(CommandManager.argument("hash", StringArgumentType.word()).executes(context -> ServerPackCommand.executePush((ServerCommandSource)context.getSource(), StringArgumentType.getString((CommandContext)context, (String)"url"), Optional.of(UuidArgumentType.getUuid((CommandContext<ServerCommandSource>)context, "uuid")), Optional.of(StringArgumentType.getString((CommandContext)context, (String)"hash")))))).executes(context -> ServerPackCommand.executePush((ServerCommandSource)context.getSource(), StringArgumentType.getString((CommandContext)context, (String)"url"), Optional.of(UuidArgumentType.getUuid((CommandContext<ServerCommandSource>)context, "uuid")), Optional.empty())))).executes(context -> ServerPackCommand.executePush((ServerCommandSource)context.getSource(), StringArgumentType.getString((CommandContext)context, (String)"url"), Optional.empty(), Optional.empty()))))).then(CommandManager.literal("pop").then(CommandManager.argument("uuid", UuidArgumentType.uuid()).executes(context -> ServerPackCommand.executePop((ServerCommandSource)context.getSource(), UuidArgumentType.getUuid((CommandContext<ServerCommandSource>)context, "uuid"))))));
    }

    private static void sendToAll(ServerCommandSource source, Packet<?> packet) {
        source.getServer().getNetworkIo().getConnections().forEach(connection -> connection.send(packet));
    }

    private static int executePush(ServerCommandSource source, String url, Optional<UUID> uuid, Optional<String> hash) {
        UUID uUID = uuid.orElseGet(() -> UUID.nameUUIDFromBytes(url.getBytes(StandardCharsets.UTF_8)));
        String string = hash.orElse("");
        ResourcePackSendS2CPacket resourcePackSendS2CPacket = new ResourcePackSendS2CPacket(uUID, url, string, false, null);
        ServerPackCommand.sendToAll(source, resourcePackSendS2CPacket);
        return 0;
    }

    private static int executePop(ServerCommandSource source, UUID uuid) {
        ResourcePackRemoveS2CPacket resourcePackRemoveS2CPacket = new ResourcePackRemoveS2CPacket(Optional.of(uuid));
        ServerPackCommand.sendToAll(source, resourcePackRemoveS2CPacket);
        return 0;
    }
}


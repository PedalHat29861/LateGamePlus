/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  com.mojang.brigadier.CommandDispatcher
 *  com.mojang.brigadier.Message
 *  com.mojang.brigadier.arguments.IntegerArgumentType
 *  com.mojang.brigadier.arguments.StringArgumentType
 *  com.mojang.brigadier.builder.LiteralArgumentBuilder
 *  com.mojang.brigadier.builder.RequiredArgumentBuilder
 *  com.mojang.brigadier.context.CommandContext
 *  com.mojang.brigadier.exceptions.CommandSyntaxException
 *  com.mojang.brigadier.exceptions.SimpleCommandExceptionType
 */
package net.minecraft.server.dedicated.command;

import com.mojang.brigadier.CommandDispatcher;
import com.mojang.brigadier.Message;
import com.mojang.brigadier.arguments.IntegerArgumentType;
import com.mojang.brigadier.arguments.StringArgumentType;
import com.mojang.brigadier.builder.LiteralArgumentBuilder;
import com.mojang.brigadier.builder.RequiredArgumentBuilder;
import com.mojang.brigadier.context.CommandContext;
import com.mojang.brigadier.exceptions.CommandSyntaxException;
import com.mojang.brigadier.exceptions.SimpleCommandExceptionType;
import java.util.Collection;
import java.util.List;
import net.minecraft.command.argument.EntityArgumentType;
import net.minecraft.network.packet.s2c.common.ServerTransferS2CPacket;
import net.minecraft.server.command.CommandManager;
import net.minecraft.server.command.ServerCommandSource;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.text.Text;

public class TransferCommand {
    private static final SimpleCommandExceptionType NO_PLAYERS_EXCEPTION = new SimpleCommandExceptionType((Message)Text.translatable("commands.transfer.error.no_players"));

    public static void register(CommandDispatcher<ServerCommandSource> dispatcher) {
        dispatcher.register((LiteralArgumentBuilder)((LiteralArgumentBuilder)CommandManager.literal("transfer").requires(CommandManager.requirePermissionLevel(CommandManager.ADMINS_CHECK))).then(((RequiredArgumentBuilder)CommandManager.argument("hostname", StringArgumentType.string()).executes(context -> TransferCommand.executeTransfer((ServerCommandSource)context.getSource(), StringArgumentType.getString((CommandContext)context, (String)"hostname"), 25565, List.of(((ServerCommandSource)context.getSource()).getPlayerOrThrow())))).then(((RequiredArgumentBuilder)CommandManager.argument("port", IntegerArgumentType.integer((int)1, (int)65535)).executes(context -> TransferCommand.executeTransfer((ServerCommandSource)context.getSource(), StringArgumentType.getString((CommandContext)context, (String)"hostname"), IntegerArgumentType.getInteger((CommandContext)context, (String)"port"), List.of(((ServerCommandSource)context.getSource()).getPlayerOrThrow())))).then(CommandManager.argument("players", EntityArgumentType.players()).executes(context -> TransferCommand.executeTransfer((ServerCommandSource)context.getSource(), StringArgumentType.getString((CommandContext)context, (String)"hostname"), IntegerArgumentType.getInteger((CommandContext)context, (String)"port"), EntityArgumentType.getPlayers((CommandContext<ServerCommandSource>)context, "players")))))));
    }

    private static int executeTransfer(ServerCommandSource source, String host, int port, Collection<ServerPlayerEntity> players) throws CommandSyntaxException {
        if (players.isEmpty()) {
            throw NO_PLAYERS_EXCEPTION.create();
        }
        for (ServerPlayerEntity serverPlayerEntity : players) {
            serverPlayerEntity.networkHandler.sendPacket(new ServerTransferS2CPacket(host, port));
        }
        if (players.size() == 1) {
            source.sendFeedback(() -> Text.translatable("commands.transfer.success.single", ((ServerPlayerEntity)players.iterator().next()).getDisplayName(), host, port), true);
        } else {
            source.sendFeedback(() -> Text.translatable("commands.transfer.success.multiple", players.size(), host, port), true);
        }
        return players.size();
    }
}


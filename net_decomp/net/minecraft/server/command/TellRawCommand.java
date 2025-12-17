/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  com.mojang.brigadier.CommandDispatcher
 *  com.mojang.brigadier.builder.LiteralArgumentBuilder
 *  com.mojang.brigadier.context.CommandContext
 */
package net.minecraft.server.command;

import com.mojang.brigadier.CommandDispatcher;
import com.mojang.brigadier.builder.LiteralArgumentBuilder;
import com.mojang.brigadier.context.CommandContext;
import net.minecraft.command.CommandRegistryAccess;
import net.minecraft.command.argument.EntityArgumentType;
import net.minecraft.command.argument.TextArgumentType;
import net.minecraft.server.command.CommandManager;
import net.minecraft.server.command.ServerCommandSource;
import net.minecraft.server.network.ServerPlayerEntity;

public class TellRawCommand {
    public static void register(CommandDispatcher<ServerCommandSource> dispatcher, CommandRegistryAccess registryAccess) {
        dispatcher.register((LiteralArgumentBuilder)((LiteralArgumentBuilder)CommandManager.literal("tellraw").requires(CommandManager.requirePermissionLevel(CommandManager.GAMEMASTERS_CHECK))).then(CommandManager.argument("targets", EntityArgumentType.players()).then(CommandManager.argument("message", TextArgumentType.text(registryAccess)).executes(context -> {
            int i = 0;
            for (ServerPlayerEntity serverPlayerEntity : EntityArgumentType.getPlayers((CommandContext<ServerCommandSource>)context, "targets")) {
                serverPlayerEntity.sendMessageToClient(TextArgumentType.parseTextArgument((CommandContext<ServerCommandSource>)context, "message", serverPlayerEntity), false);
                ++i;
            }
            return i;
        }))));
    }
}


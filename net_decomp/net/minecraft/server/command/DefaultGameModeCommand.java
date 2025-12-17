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
import net.minecraft.command.argument.GameModeArgumentType;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.command.CommandManager;
import net.minecraft.server.command.ServerCommandSource;
import net.minecraft.text.Text;
import net.minecraft.world.GameMode;

public class DefaultGameModeCommand {
    public static void register(CommandDispatcher<ServerCommandSource> dispatcher) {
        dispatcher.register((LiteralArgumentBuilder)((LiteralArgumentBuilder)CommandManager.literal("defaultgamemode").requires(CommandManager.requirePermissionLevel(CommandManager.GAMEMASTERS_CHECK))).then(CommandManager.argument("gamemode", GameModeArgumentType.gameMode()).executes(commandContext -> DefaultGameModeCommand.execute((ServerCommandSource)commandContext.getSource(), GameModeArgumentType.getGameMode((CommandContext<ServerCommandSource>)commandContext, "gamemode")))));
    }

    private static int execute(ServerCommandSource source, GameMode defaultGameMode) {
        MinecraftServer minecraftServer = source.getServer();
        minecraftServer.setDefaultGameMode(defaultGameMode);
        int i = minecraftServer.changeGameModeGlobally(minecraftServer.getForcedGameMode());
        source.sendFeedback(() -> Text.translatable("commands.defaultgamemode.success", defaultGameMode.getTranslatableName()), true);
        return i;
    }
}


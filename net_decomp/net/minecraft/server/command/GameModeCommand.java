/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  com.mojang.brigadier.CommandDispatcher
 *  com.mojang.brigadier.builder.LiteralArgumentBuilder
 *  com.mojang.brigadier.builder.RequiredArgumentBuilder
 *  com.mojang.brigadier.context.CommandContext
 */
package net.minecraft.server.command;

import com.mojang.brigadier.CommandDispatcher;
import com.mojang.brigadier.builder.LiteralArgumentBuilder;
import com.mojang.brigadier.builder.RequiredArgumentBuilder;
import com.mojang.brigadier.context.CommandContext;
import java.util.Collection;
import java.util.Collections;
import net.minecraft.command.DefaultPermissions;
import net.minecraft.command.argument.EntityArgumentType;
import net.minecraft.command.argument.GameModeArgumentType;
import net.minecraft.command.permission.PermissionCheck;
import net.minecraft.server.command.CommandManager;
import net.minecraft.server.command.ServerCommandSource;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.text.MutableText;
import net.minecraft.text.Text;
import net.minecraft.world.GameMode;
import net.minecraft.world.rule.GameRules;

public class GameModeCommand {
    public static final PermissionCheck PERMISSION_CHECK = new PermissionCheck.Require(DefaultPermissions.GAMEMASTERS);

    public static void register(CommandDispatcher<ServerCommandSource> dispatcher) {
        dispatcher.register((LiteralArgumentBuilder)((LiteralArgumentBuilder)CommandManager.literal("gamemode").requires(CommandManager.requirePermissionLevel(PERMISSION_CHECK))).then(((RequiredArgumentBuilder)CommandManager.argument("gamemode", GameModeArgumentType.gameMode()).executes(context -> GameModeCommand.execute((CommandContext<ServerCommandSource>)context, Collections.singleton(((ServerCommandSource)context.getSource()).getPlayerOrThrow()), GameModeArgumentType.getGameMode((CommandContext<ServerCommandSource>)context, "gamemode")))).then(CommandManager.argument("target", EntityArgumentType.players()).executes(context -> GameModeCommand.execute((CommandContext<ServerCommandSource>)context, EntityArgumentType.getPlayers((CommandContext<ServerCommandSource>)context, "target"), GameModeArgumentType.getGameMode((CommandContext<ServerCommandSource>)context, "gamemode"))))));
    }

    private static void sendFeedback(ServerCommandSource source, ServerPlayerEntity player, GameMode gameMode) {
        MutableText text = Text.translatable("gameMode." + gameMode.getId());
        if (source.getEntity() == player) {
            source.sendFeedback(() -> Text.translatable("commands.gamemode.success.self", text), true);
        } else {
            if (source.getWorld().getGameRules().getValue(GameRules.SEND_COMMAND_FEEDBACK).booleanValue()) {
                player.sendMessage(Text.translatable("gameMode.changed", text));
            }
            source.sendFeedback(() -> Text.translatable("commands.gamemode.success.other", player.getDisplayName(), text), true);
        }
    }

    private static int execute(CommandContext<ServerCommandSource> context, Collection<ServerPlayerEntity> targets, GameMode gameMode) {
        int i = 0;
        for (ServerPlayerEntity serverPlayerEntity : targets) {
            if (!GameModeCommand.execute((ServerCommandSource)context.getSource(), serverPlayerEntity, gameMode)) continue;
            ++i;
        }
        return i;
    }

    public static void execute(ServerPlayerEntity target, GameMode gameMode) {
        GameModeCommand.execute(target.getCommandSource(), target, gameMode);
    }

    private static boolean execute(ServerCommandSource source, ServerPlayerEntity target, GameMode gameMode) {
        if (target.changeGameMode(gameMode)) {
            GameModeCommand.sendFeedback(source, target, gameMode);
            return true;
        }
        return false;
    }
}


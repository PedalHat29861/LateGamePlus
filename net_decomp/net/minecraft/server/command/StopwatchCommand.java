/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  com.mojang.brigadier.CommandDispatcher
 *  com.mojang.brigadier.arguments.DoubleArgumentType
 *  com.mojang.brigadier.builder.LiteralArgumentBuilder
 *  com.mojang.brigadier.builder.RequiredArgumentBuilder
 *  com.mojang.brigadier.context.CommandContext
 *  com.mojang.brigadier.exceptions.CommandSyntaxException
 *  com.mojang.brigadier.exceptions.DynamicCommandExceptionType
 *  com.mojang.brigadier.suggestion.SuggestionProvider
 */
package net.minecraft.server.command;

import com.mojang.brigadier.CommandDispatcher;
import com.mojang.brigadier.arguments.DoubleArgumentType;
import com.mojang.brigadier.builder.LiteralArgumentBuilder;
import com.mojang.brigadier.builder.RequiredArgumentBuilder;
import com.mojang.brigadier.context.CommandContext;
import com.mojang.brigadier.exceptions.CommandSyntaxException;
import com.mojang.brigadier.exceptions.DynamicCommandExceptionType;
import com.mojang.brigadier.suggestion.SuggestionProvider;
import net.minecraft.command.CommandSource;
import net.minecraft.command.argument.IdentifierArgumentType;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.command.CommandManager;
import net.minecraft.server.command.ServerCommandSource;
import net.minecraft.text.Text;
import net.minecraft.util.Identifier;
import net.minecraft.world.timer.stopwatch.Stopwatch;
import net.minecraft.world.timer.stopwatch.StopwatchPersistentState;

public class StopwatchCommand {
    private static final DynamicCommandExceptionType ALREADY_EXISTS_EXCEPTION = new DynamicCommandExceptionType(name -> Text.stringifiedTranslatable("commands.stopwatch.already_exists", name));
    public static final DynamicCommandExceptionType DOES_NOT_EXIST_EXCEPTION = new DynamicCommandExceptionType(name -> Text.stringifiedTranslatable("commands.stopwatch.does_not_exist", name));
    public static final SuggestionProvider<ServerCommandSource> STOPWATCH_SUGGESTION_PROVIDER = (context, builder) -> CommandSource.suggestIdentifiers(((ServerCommandSource)context.getSource()).getServer().getStopwatchPersistentState().keys(), builder);

    public static void register(CommandDispatcher<ServerCommandSource> dispatcher) {
        dispatcher.register((LiteralArgumentBuilder)((LiteralArgumentBuilder)((LiteralArgumentBuilder)((LiteralArgumentBuilder)((LiteralArgumentBuilder)CommandManager.literal("stopwatch").requires(CommandManager.requirePermissionLevel(CommandManager.GAMEMASTERS_CHECK))).then(CommandManager.literal("create").then(CommandManager.argument("id", IdentifierArgumentType.identifier()).executes(context -> StopwatchCommand.executeCreate((ServerCommandSource)context.getSource(), IdentifierArgumentType.getIdentifier((CommandContext<ServerCommandSource>)context, "id")))))).then(CommandManager.literal("query").then(((RequiredArgumentBuilder)CommandManager.argument("id", IdentifierArgumentType.identifier()).suggests(STOPWATCH_SUGGESTION_PROVIDER).then(CommandManager.argument("scale", DoubleArgumentType.doubleArg()).executes(context -> StopwatchCommand.executeQuery((ServerCommandSource)context.getSource(), IdentifierArgumentType.getIdentifier((CommandContext<ServerCommandSource>)context, "id"), DoubleArgumentType.getDouble((CommandContext)context, (String)"scale"))))).executes(context -> StopwatchCommand.executeQuery((ServerCommandSource)context.getSource(), IdentifierArgumentType.getIdentifier((CommandContext<ServerCommandSource>)context, "id"), 1.0))))).then(CommandManager.literal("restart").then(CommandManager.argument("id", IdentifierArgumentType.identifier()).suggests(STOPWATCH_SUGGESTION_PROVIDER).executes(context -> StopwatchCommand.executeRestart((ServerCommandSource)context.getSource(), IdentifierArgumentType.getIdentifier((CommandContext<ServerCommandSource>)context, "id")))))).then(CommandManager.literal("remove").then(CommandManager.argument("id", IdentifierArgumentType.identifier()).suggests(STOPWATCH_SUGGESTION_PROVIDER).executes(context -> StopwatchCommand.executeRemove((ServerCommandSource)context.getSource(), IdentifierArgumentType.getIdentifier((CommandContext<ServerCommandSource>)context, "id"))))));
    }

    private static int executeCreate(ServerCommandSource source, Identifier id) throws CommandSyntaxException {
        Stopwatch stopwatch;
        MinecraftServer minecraftServer = source.getServer();
        StopwatchPersistentState stopwatchPersistentState = minecraftServer.getStopwatchPersistentState();
        if (!stopwatchPersistentState.add(id, stopwatch = new Stopwatch(StopwatchPersistentState.getTimeMs()))) {
            throw ALREADY_EXISTS_EXCEPTION.create((Object)id);
        }
        source.sendFeedback(() -> Text.translatable("commands.stopwatch.create.success", Text.of(id)), true);
        return 1;
    }

    private static int executeQuery(ServerCommandSource source, Identifier id, double scale) throws CommandSyntaxException {
        MinecraftServer minecraftServer = source.getServer();
        StopwatchPersistentState stopwatchPersistentState = minecraftServer.getStopwatchPersistentState();
        Stopwatch stopwatch = stopwatchPersistentState.get(id);
        if (stopwatch == null) {
            throw DOES_NOT_EXIST_EXCEPTION.create((Object)id);
        }
        long l = StopwatchPersistentState.getTimeMs();
        double d = stopwatch.getElapsedTimeSeconds(l);
        source.sendFeedback(() -> Text.translatable("commands.stopwatch.query", Text.of(id), d), true);
        return (int)(d * scale);
    }

    private static int executeRestart(ServerCommandSource source, Identifier id) throws CommandSyntaxException {
        MinecraftServer minecraftServer = source.getServer();
        StopwatchPersistentState stopwatchPersistentState = minecraftServer.getStopwatchPersistentState();
        if (!stopwatchPersistentState.update(id, stopwatch -> new Stopwatch(StopwatchPersistentState.getTimeMs()))) {
            throw DOES_NOT_EXIST_EXCEPTION.create((Object)id);
        }
        source.sendFeedback(() -> Text.translatable("commands.stopwatch.restart.success", Text.of(id)), true);
        return 1;
    }

    private static int executeRemove(ServerCommandSource source, Identifier id) throws CommandSyntaxException {
        MinecraftServer minecraftServer = source.getServer();
        StopwatchPersistentState stopwatchPersistentState = minecraftServer.getStopwatchPersistentState();
        if (!stopwatchPersistentState.remove(id)) {
            throw DOES_NOT_EXIST_EXCEPTION.create((Object)id);
        }
        source.sendFeedback(() -> Text.translatable("commands.stopwatch.remove.success", Text.of(id)), true);
        return 1;
    }
}


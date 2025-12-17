/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  com.mojang.brigadier.CommandDispatcher
 *  com.mojang.brigadier.Message
 *  com.mojang.brigadier.arguments.BoolArgumentType
 *  com.mojang.brigadier.arguments.IntegerArgumentType
 *  com.mojang.brigadier.builder.LiteralArgumentBuilder
 *  com.mojang.brigadier.builder.RequiredArgumentBuilder
 *  com.mojang.brigadier.context.CommandContext
 *  com.mojang.brigadier.exceptions.CommandSyntaxException
 *  com.mojang.brigadier.exceptions.DynamicCommandExceptionType
 *  com.mojang.brigadier.exceptions.SimpleCommandExceptionType
 *  org.jspecify.annotations.Nullable
 */
package net.minecraft.server.command;

import com.mojang.brigadier.CommandDispatcher;
import com.mojang.brigadier.Message;
import com.mojang.brigadier.arguments.BoolArgumentType;
import com.mojang.brigadier.arguments.IntegerArgumentType;
import com.mojang.brigadier.builder.LiteralArgumentBuilder;
import com.mojang.brigadier.builder.RequiredArgumentBuilder;
import com.mojang.brigadier.context.CommandContext;
import com.mojang.brigadier.exceptions.CommandSyntaxException;
import com.mojang.brigadier.exceptions.DynamicCommandExceptionType;
import com.mojang.brigadier.exceptions.SimpleCommandExceptionType;
import net.minecraft.command.argument.GameModeArgumentType;
import net.minecraft.server.command.CommandManager;
import net.minecraft.server.command.ServerCommandSource;
import net.minecraft.text.MutableText;
import net.minecraft.text.Text;
import net.minecraft.text.Texts;
import net.minecraft.util.NetworkUtils;
import net.minecraft.world.GameMode;
import org.jspecify.annotations.Nullable;

public class PublishCommand {
    private static final SimpleCommandExceptionType FAILED_EXCEPTION = new SimpleCommandExceptionType((Message)Text.translatable("commands.publish.failed"));
    private static final DynamicCommandExceptionType ALREADY_PUBLISHED_EXCEPTION = new DynamicCommandExceptionType(port -> Text.stringifiedTranslatable("commands.publish.alreadyPublished", port));

    public static void register(CommandDispatcher<ServerCommandSource> dispatcher) {
        dispatcher.register((LiteralArgumentBuilder)((LiteralArgumentBuilder)((LiteralArgumentBuilder)CommandManager.literal("publish").requires(CommandManager.requirePermissionLevel(CommandManager.OWNERS_CHECK))).executes(context -> PublishCommand.execute((ServerCommandSource)context.getSource(), NetworkUtils.findLocalPort(), false, null))).then(((RequiredArgumentBuilder)CommandManager.argument("allowCommands", BoolArgumentType.bool()).executes(context -> PublishCommand.execute((ServerCommandSource)context.getSource(), NetworkUtils.findLocalPort(), BoolArgumentType.getBool((CommandContext)context, (String)"allowCommands"), null))).then(((RequiredArgumentBuilder)CommandManager.argument("gamemode", GameModeArgumentType.gameMode()).executes(context -> PublishCommand.execute((ServerCommandSource)context.getSource(), NetworkUtils.findLocalPort(), BoolArgumentType.getBool((CommandContext)context, (String)"allowCommands"), GameModeArgumentType.getGameMode((CommandContext<ServerCommandSource>)context, "gamemode")))).then(CommandManager.argument("port", IntegerArgumentType.integer((int)0, (int)65535)).executes(context -> PublishCommand.execute((ServerCommandSource)context.getSource(), IntegerArgumentType.getInteger((CommandContext)context, (String)"port"), BoolArgumentType.getBool((CommandContext)context, (String)"allowCommands"), GameModeArgumentType.getGameMode((CommandContext<ServerCommandSource>)context, "gamemode")))))));
    }

    private static int execute(ServerCommandSource source, int port, boolean allowCommands, @Nullable GameMode gameMode) throws CommandSyntaxException {
        if (source.getServer().isRemote()) {
            throw ALREADY_PUBLISHED_EXCEPTION.create((Object)source.getServer().getServerPort());
        }
        if (!source.getServer().openToLan(gameMode, allowCommands, port)) {
            throw FAILED_EXCEPTION.create();
        }
        source.sendFeedback(() -> PublishCommand.getStartedText(port), true);
        return port;
    }

    public static MutableText getStartedText(int port) {
        MutableText text = Texts.bracketedCopyable(String.valueOf(port));
        return Text.translatable("commands.publish.started", text);
    }
}


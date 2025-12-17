/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  com.mojang.brigadier.CommandDispatcher
 *  com.mojang.brigadier.Message
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
import com.mojang.brigadier.builder.LiteralArgumentBuilder;
import com.mojang.brigadier.builder.RequiredArgumentBuilder;
import com.mojang.brigadier.context.CommandContext;
import com.mojang.brigadier.exceptions.CommandSyntaxException;
import com.mojang.brigadier.exceptions.DynamicCommandExceptionType;
import com.mojang.brigadier.exceptions.SimpleCommandExceptionType;
import net.minecraft.command.argument.EntityArgumentType;
import net.minecraft.entity.Entity;
import net.minecraft.server.command.CommandManager;
import net.minecraft.server.command.ServerCommandSource;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.text.Text;
import org.jspecify.annotations.Nullable;

public class SpectateCommand {
    private static final SimpleCommandExceptionType SPECTATE_SELF_EXCEPTION = new SimpleCommandExceptionType((Message)Text.translatable("commands.spectate.self"));
    private static final DynamicCommandExceptionType NOT_SPECTATOR_EXCEPTION = new DynamicCommandExceptionType(playerName -> Text.stringifiedTranslatable("commands.spectate.not_spectator", playerName));
    private static final DynamicCommandExceptionType CANNOT_SPECTATE_EXCEPTION = new DynamicCommandExceptionType(entityName -> Text.stringifiedTranslatable("commands.spectate.cannot_spectate", entityName));

    public static void register(CommandDispatcher<ServerCommandSource> dispatcher) {
        dispatcher.register((LiteralArgumentBuilder)((LiteralArgumentBuilder)((LiteralArgumentBuilder)CommandManager.literal("spectate").requires(CommandManager.requirePermissionLevel(CommandManager.GAMEMASTERS_CHECK))).executes(context -> SpectateCommand.execute((ServerCommandSource)context.getSource(), null, ((ServerCommandSource)context.getSource()).getPlayerOrThrow()))).then(((RequiredArgumentBuilder)CommandManager.argument("target", EntityArgumentType.entity()).executes(context -> SpectateCommand.execute((ServerCommandSource)context.getSource(), EntityArgumentType.getEntity((CommandContext<ServerCommandSource>)context, "target"), ((ServerCommandSource)context.getSource()).getPlayerOrThrow()))).then(CommandManager.argument("player", EntityArgumentType.player()).executes(context -> SpectateCommand.execute((ServerCommandSource)context.getSource(), EntityArgumentType.getEntity((CommandContext<ServerCommandSource>)context, "target"), EntityArgumentType.getPlayer((CommandContext<ServerCommandSource>)context, "player"))))));
    }

    private static int execute(ServerCommandSource source, @Nullable Entity entity, ServerPlayerEntity player) throws CommandSyntaxException {
        if (player == entity) {
            throw SPECTATE_SELF_EXCEPTION.create();
        }
        if (!player.isSpectator()) {
            throw NOT_SPECTATOR_EXCEPTION.create((Object)player.getDisplayName());
        }
        if (entity != null && entity.getType().getMaxTrackDistance() == 0) {
            throw CANNOT_SPECTATE_EXCEPTION.create((Object)entity.getDisplayName());
        }
        player.setCameraEntity(entity);
        if (entity != null) {
            source.sendFeedback(() -> Text.translatable("commands.spectate.success.started", entity.getDisplayName()), false);
        } else {
            source.sendFeedback(() -> Text.translatable("commands.spectate.success.stopped"), false);
        }
        return 1;
    }
}


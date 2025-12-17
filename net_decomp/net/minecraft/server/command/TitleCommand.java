/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  com.mojang.brigadier.CommandDispatcher
 *  com.mojang.brigadier.arguments.IntegerArgumentType
 *  com.mojang.brigadier.builder.LiteralArgumentBuilder
 *  com.mojang.brigadier.builder.RequiredArgumentBuilder
 *  com.mojang.brigadier.context.CommandContext
 *  com.mojang.brigadier.exceptions.CommandSyntaxException
 */
package net.minecraft.server.command;

import com.mojang.brigadier.CommandDispatcher;
import com.mojang.brigadier.arguments.IntegerArgumentType;
import com.mojang.brigadier.builder.LiteralArgumentBuilder;
import com.mojang.brigadier.builder.RequiredArgumentBuilder;
import com.mojang.brigadier.context.CommandContext;
import com.mojang.brigadier.exceptions.CommandSyntaxException;
import java.util.Collection;
import java.util.function.Function;
import net.minecraft.command.CommandRegistryAccess;
import net.minecraft.command.argument.EntityArgumentType;
import net.minecraft.command.argument.TextArgumentType;
import net.minecraft.command.argument.TimeArgumentType;
import net.minecraft.entity.Entity;
import net.minecraft.network.packet.Packet;
import net.minecraft.network.packet.s2c.play.ClearTitleS2CPacket;
import net.minecraft.network.packet.s2c.play.OverlayMessageS2CPacket;
import net.minecraft.network.packet.s2c.play.SubtitleS2CPacket;
import net.minecraft.network.packet.s2c.play.TitleFadeS2CPacket;
import net.minecraft.network.packet.s2c.play.TitleS2CPacket;
import net.minecraft.server.command.CommandManager;
import net.minecraft.server.command.ServerCommandSource;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.text.Text;
import net.minecraft.text.Texts;

public class TitleCommand {
    public static void register(CommandDispatcher<ServerCommandSource> dispatcher, CommandRegistryAccess registryAccess) {
        dispatcher.register((LiteralArgumentBuilder)((LiteralArgumentBuilder)CommandManager.literal("title").requires(CommandManager.requirePermissionLevel(CommandManager.GAMEMASTERS_CHECK))).then(((RequiredArgumentBuilder)((RequiredArgumentBuilder)((RequiredArgumentBuilder)((RequiredArgumentBuilder)((RequiredArgumentBuilder)CommandManager.argument("targets", EntityArgumentType.players()).then(CommandManager.literal("clear").executes(context -> TitleCommand.executeClear((ServerCommandSource)context.getSource(), EntityArgumentType.getPlayers((CommandContext<ServerCommandSource>)context, "targets"))))).then(CommandManager.literal("reset").executes(context -> TitleCommand.executeReset((ServerCommandSource)context.getSource(), EntityArgumentType.getPlayers((CommandContext<ServerCommandSource>)context, "targets"))))).then(CommandManager.literal("title").then(CommandManager.argument("title", TextArgumentType.text(registryAccess)).executes(context -> TitleCommand.executeTitle((ServerCommandSource)context.getSource(), EntityArgumentType.getPlayers((CommandContext<ServerCommandSource>)context, "targets"), TextArgumentType.getTextArgument((CommandContext<ServerCommandSource>)context, "title"), "title", TitleS2CPacket::new))))).then(CommandManager.literal("subtitle").then(CommandManager.argument("title", TextArgumentType.text(registryAccess)).executes(context -> TitleCommand.executeTitle((ServerCommandSource)context.getSource(), EntityArgumentType.getPlayers((CommandContext<ServerCommandSource>)context, "targets"), TextArgumentType.getTextArgument((CommandContext<ServerCommandSource>)context, "title"), "subtitle", SubtitleS2CPacket::new))))).then(CommandManager.literal("actionbar").then(CommandManager.argument("title", TextArgumentType.text(registryAccess)).executes(context -> TitleCommand.executeTitle((ServerCommandSource)context.getSource(), EntityArgumentType.getPlayers((CommandContext<ServerCommandSource>)context, "targets"), TextArgumentType.getTextArgument((CommandContext<ServerCommandSource>)context, "title"), "actionbar", OverlayMessageS2CPacket::new))))).then(CommandManager.literal("times").then(CommandManager.argument("fadeIn", TimeArgumentType.time()).then(CommandManager.argument("stay", TimeArgumentType.time()).then(CommandManager.argument("fadeOut", TimeArgumentType.time()).executes(context -> TitleCommand.executeTimes((ServerCommandSource)context.getSource(), EntityArgumentType.getPlayers((CommandContext<ServerCommandSource>)context, "targets"), IntegerArgumentType.getInteger((CommandContext)context, (String)"fadeIn"), IntegerArgumentType.getInteger((CommandContext)context, (String)"stay"), IntegerArgumentType.getInteger((CommandContext)context, (String)"fadeOut")))))))));
    }

    private static int executeClear(ServerCommandSource source, Collection<ServerPlayerEntity> targets) {
        ClearTitleS2CPacket clearTitleS2CPacket = new ClearTitleS2CPacket(false);
        for (ServerPlayerEntity serverPlayerEntity : targets) {
            serverPlayerEntity.networkHandler.sendPacket(clearTitleS2CPacket);
        }
        if (targets.size() == 1) {
            source.sendFeedback(() -> Text.translatable("commands.title.cleared.single", ((ServerPlayerEntity)targets.iterator().next()).getDisplayName()), true);
        } else {
            source.sendFeedback(() -> Text.translatable("commands.title.cleared.multiple", targets.size()), true);
        }
        return targets.size();
    }

    private static int executeReset(ServerCommandSource source, Collection<ServerPlayerEntity> targets) {
        ClearTitleS2CPacket clearTitleS2CPacket = new ClearTitleS2CPacket(true);
        for (ServerPlayerEntity serverPlayerEntity : targets) {
            serverPlayerEntity.networkHandler.sendPacket(clearTitleS2CPacket);
        }
        if (targets.size() == 1) {
            source.sendFeedback(() -> Text.translatable("commands.title.reset.single", ((ServerPlayerEntity)targets.iterator().next()).getDisplayName()), true);
        } else {
            source.sendFeedback(() -> Text.translatable("commands.title.reset.multiple", targets.size()), true);
        }
        return targets.size();
    }

    private static int executeTitle(ServerCommandSource source, Collection<ServerPlayerEntity> targets, Text title, String titleType, Function<Text, Packet<?>> constructor) throws CommandSyntaxException {
        for (ServerPlayerEntity serverPlayerEntity : targets) {
            serverPlayerEntity.networkHandler.sendPacket(constructor.apply(Texts.parse(source, title, (Entity)serverPlayerEntity, 0)));
        }
        if (targets.size() == 1) {
            source.sendFeedback(() -> Text.translatable("commands.title.show." + titleType + ".single", ((ServerPlayerEntity)targets.iterator().next()).getDisplayName()), true);
        } else {
            source.sendFeedback(() -> Text.translatable("commands.title.show." + titleType + ".multiple", targets.size()), true);
        }
        return targets.size();
    }

    private static int executeTimes(ServerCommandSource source, Collection<ServerPlayerEntity> targets, int fadeIn, int stay, int fadeOut) {
        TitleFadeS2CPacket titleFadeS2CPacket = new TitleFadeS2CPacket(fadeIn, stay, fadeOut);
        for (ServerPlayerEntity serverPlayerEntity : targets) {
            serverPlayerEntity.networkHandler.sendPacket(titleFadeS2CPacket);
        }
        if (targets.size() == 1) {
            source.sendFeedback(() -> Text.translatable("commands.title.times.single", ((ServerPlayerEntity)targets.iterator().next()).getDisplayName()), true);
        } else {
            source.sendFeedback(() -> Text.translatable("commands.title.times.multiple", targets.size()), true);
        }
        return targets.size();
    }
}


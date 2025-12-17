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
 *  com.mojang.brigadier.exceptions.SimpleCommandExceptionType
 *  org.jspecify.annotations.Nullable
 */
package net.minecraft.server.dedicated.command;

import com.mojang.brigadier.CommandDispatcher;
import com.mojang.brigadier.Message;
import com.mojang.brigadier.builder.LiteralArgumentBuilder;
import com.mojang.brigadier.builder.RequiredArgumentBuilder;
import com.mojang.brigadier.context.CommandContext;
import com.mojang.brigadier.exceptions.CommandSyntaxException;
import com.mojang.brigadier.exceptions.SimpleCommandExceptionType;
import java.util.Collection;
import net.minecraft.command.argument.GameProfileArgumentType;
import net.minecraft.command.argument.MessageArgumentType;
import net.minecraft.server.BannedPlayerEntry;
import net.minecraft.server.BannedPlayerList;
import net.minecraft.server.PlayerConfigEntry;
import net.minecraft.server.command.CommandManager;
import net.minecraft.server.command.ServerCommandSource;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.text.Text;
import org.jspecify.annotations.Nullable;

public class BanCommand {
    private static final SimpleCommandExceptionType ALREADY_BANNED_EXCEPTION = new SimpleCommandExceptionType((Message)Text.translatable("commands.ban.failed"));

    public static void register(CommandDispatcher<ServerCommandSource> dispatcher) {
        dispatcher.register((LiteralArgumentBuilder)((LiteralArgumentBuilder)CommandManager.literal("ban").requires(CommandManager.requirePermissionLevel(CommandManager.ADMINS_CHECK))).then(((RequiredArgumentBuilder)CommandManager.argument("targets", GameProfileArgumentType.gameProfile()).executes(context -> BanCommand.ban((ServerCommandSource)context.getSource(), GameProfileArgumentType.getProfileArgument((CommandContext<ServerCommandSource>)context, "targets"), null))).then(CommandManager.argument("reason", MessageArgumentType.message()).executes(context -> BanCommand.ban((ServerCommandSource)context.getSource(), GameProfileArgumentType.getProfileArgument((CommandContext<ServerCommandSource>)context, "targets"), MessageArgumentType.getMessage((CommandContext<ServerCommandSource>)context, "reason"))))));
    }

    private static int ban(ServerCommandSource source, Collection<PlayerConfigEntry> targets, @Nullable Text reason) throws CommandSyntaxException {
        BannedPlayerList bannedPlayerList = source.getServer().getPlayerManager().getUserBanList();
        int i = 0;
        for (PlayerConfigEntry playerConfigEntry : targets) {
            if (bannedPlayerList.contains(playerConfigEntry)) continue;
            BannedPlayerEntry bannedPlayerEntry = new BannedPlayerEntry(playerConfigEntry, null, source.getName(), null, reason == null ? null : reason.getString());
            bannedPlayerList.add(bannedPlayerEntry);
            ++i;
            source.sendFeedback(() -> Text.translatable("commands.ban.success", Text.literal(playerConfigEntry.name()), bannedPlayerEntry.getReasonText()), true);
            ServerPlayerEntity serverPlayerEntity = source.getServer().getPlayerManager().getPlayer(playerConfigEntry.id());
            if (serverPlayerEntity == null) continue;
            serverPlayerEntity.networkHandler.disconnect(Text.translatable("multiplayer.disconnect.banned"));
        }
        if (i == 0) {
            throw ALREADY_BANNED_EXCEPTION.create();
        }
        return i;
    }
}


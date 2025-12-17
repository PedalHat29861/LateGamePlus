/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  com.google.common.net.InetAddresses
 *  com.mojang.brigadier.CommandDispatcher
 *  com.mojang.brigadier.Message
 *  com.mojang.brigadier.arguments.StringArgumentType
 *  com.mojang.brigadier.builder.LiteralArgumentBuilder
 *  com.mojang.brigadier.context.CommandContext
 *  com.mojang.brigadier.exceptions.CommandSyntaxException
 *  com.mojang.brigadier.exceptions.SimpleCommandExceptionType
 */
package net.minecraft.server.dedicated.command;

import com.google.common.net.InetAddresses;
import com.mojang.brigadier.CommandDispatcher;
import com.mojang.brigadier.Message;
import com.mojang.brigadier.arguments.StringArgumentType;
import com.mojang.brigadier.builder.LiteralArgumentBuilder;
import com.mojang.brigadier.context.CommandContext;
import com.mojang.brigadier.exceptions.CommandSyntaxException;
import com.mojang.brigadier.exceptions.SimpleCommandExceptionType;
import net.minecraft.command.CommandSource;
import net.minecraft.server.BannedIpList;
import net.minecraft.server.command.CommandManager;
import net.minecraft.server.command.ServerCommandSource;
import net.minecraft.text.Text;

public class PardonIpCommand {
    private static final SimpleCommandExceptionType INVALID_IP_EXCEPTION = new SimpleCommandExceptionType((Message)Text.translatable("commands.pardonip.invalid"));
    private static final SimpleCommandExceptionType ALREADY_UNBANNED_EXCEPTION = new SimpleCommandExceptionType((Message)Text.translatable("commands.pardonip.failed"));

    public static void register(CommandDispatcher<ServerCommandSource> dispatcher) {
        dispatcher.register((LiteralArgumentBuilder)((LiteralArgumentBuilder)CommandManager.literal("pardon-ip").requires(CommandManager.requirePermissionLevel(CommandManager.ADMINS_CHECK))).then(CommandManager.argument("target", StringArgumentType.word()).suggests((context, builder) -> CommandSource.suggestMatching(((ServerCommandSource)context.getSource()).getServer().getPlayerManager().getIpBanList().getNames(), builder)).executes(context -> PardonIpCommand.pardonIp((ServerCommandSource)context.getSource(), StringArgumentType.getString((CommandContext)context, (String)"target")))));
    }

    private static int pardonIp(ServerCommandSource source, String target) throws CommandSyntaxException {
        if (!InetAddresses.isInetAddress((String)target)) {
            throw INVALID_IP_EXCEPTION.create();
        }
        BannedIpList bannedIpList = source.getServer().getPlayerManager().getIpBanList();
        if (!bannedIpList.isBanned(target)) {
            throw ALREADY_UNBANNED_EXCEPTION.create();
        }
        bannedIpList.remove(target);
        source.sendFeedback(() -> Text.translatable("commands.pardonip.success", target), true);
        return 1;
    }
}


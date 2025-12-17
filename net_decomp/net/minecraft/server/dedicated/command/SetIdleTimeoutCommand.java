/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  com.mojang.brigadier.CommandDispatcher
 *  com.mojang.brigadier.arguments.IntegerArgumentType
 *  com.mojang.brigadier.builder.LiteralArgumentBuilder
 *  com.mojang.brigadier.context.CommandContext
 */
package net.minecraft.server.dedicated.command;

import com.mojang.brigadier.CommandDispatcher;
import com.mojang.brigadier.arguments.IntegerArgumentType;
import com.mojang.brigadier.builder.LiteralArgumentBuilder;
import com.mojang.brigadier.context.CommandContext;
import net.minecraft.server.command.CommandManager;
import net.minecraft.server.command.ServerCommandSource;
import net.minecraft.text.Text;

public class SetIdleTimeoutCommand {
    public static void register(CommandDispatcher<ServerCommandSource> dispatcher) {
        dispatcher.register((LiteralArgumentBuilder)((LiteralArgumentBuilder)CommandManager.literal("setidletimeout").requires(CommandManager.requirePermissionLevel(CommandManager.ADMINS_CHECK))).then(CommandManager.argument("minutes", IntegerArgumentType.integer((int)0)).executes(context -> SetIdleTimeoutCommand.execute((ServerCommandSource)context.getSource(), IntegerArgumentType.getInteger((CommandContext)context, (String)"minutes")))));
    }

    private static int execute(ServerCommandSource source, int minutes) {
        source.getServer().setPlayerIdleTimeout(minutes);
        if (minutes > 0) {
            source.sendFeedback(() -> Text.translatable("commands.setidletimeout.success", minutes), true);
        } else {
            source.sendFeedback(() -> Text.translatable("commands.setidletimeout.success.disabled"), true);
        }
        return minutes;
    }
}


/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  com.mojang.brigadier.CommandDispatcher
 *  com.mojang.brigadier.Message
 *  com.mojang.brigadier.builder.LiteralArgumentBuilder
 *  com.mojang.brigadier.exceptions.SimpleCommandExceptionType
 */
package net.minecraft.server.dedicated.command;

import com.mojang.brigadier.CommandDispatcher;
import com.mojang.brigadier.Message;
import com.mojang.brigadier.builder.LiteralArgumentBuilder;
import com.mojang.brigadier.exceptions.SimpleCommandExceptionType;
import net.minecraft.server.command.CommandManager;
import net.minecraft.server.command.ServerCommandSource;
import net.minecraft.text.Text;

public class SaveOffCommand {
    private static final SimpleCommandExceptionType ALREADY_OFF_EXCEPTION = new SimpleCommandExceptionType((Message)Text.translatable("commands.save.alreadyOff"));

    public static void register(CommandDispatcher<ServerCommandSource> dispatcher) {
        dispatcher.register((LiteralArgumentBuilder)((LiteralArgumentBuilder)CommandManager.literal("save-off").requires(CommandManager.requirePermissionLevel(CommandManager.OWNERS_CHECK))).executes(context -> {
            ServerCommandSource serverCommandSource = (ServerCommandSource)context.getSource();
            boolean bl = serverCommandSource.getServer().setAutosave(false);
            if (!bl) {
                throw ALREADY_OFF_EXCEPTION.create();
            }
            serverCommandSource.sendFeedback(() -> Text.translatable("commands.save.disabled"), true);
            return 1;
        }));
    }
}


/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  com.mojang.brigadier.CommandDispatcher
 *  com.mojang.brigadier.builder.LiteralArgumentBuilder
 */
package net.minecraft.server.command;

import com.mojang.brigadier.CommandDispatcher;
import com.mojang.brigadier.builder.LiteralArgumentBuilder;
import java.util.function.Consumer;
import net.minecraft.GameVersion;
import net.minecraft.SharedConstants;
import net.minecraft.resource.ResourceType;
import net.minecraft.server.command.CommandManager;
import net.minecraft.server.command.ServerCommandSource;
import net.minecraft.text.Text;

public class VersionCommand {
    private static final Text HEADER_TEXT = Text.translatable("commands.version.header");
    private static final Text STABLE_YES_TEXT = Text.translatable("commands.version.stable.yes");
    private static final Text STABLE_NO_TEXT = Text.translatable("commands.version.stable.no");

    public static void register(CommandDispatcher<ServerCommandSource> dispatcher, boolean dedicated) {
        dispatcher.register((LiteralArgumentBuilder)((LiteralArgumentBuilder)CommandManager.literal("version").requires(CommandManager.requirePermissionLevel(dedicated ? CommandManager.GAMEMASTERS_CHECK : CommandManager.ALWAYS_PASS_CHECK))).executes(context -> {
            ServerCommandSource serverCommandSource = (ServerCommandSource)context.getSource();
            serverCommandSource.sendMessage(HEADER_TEXT);
            VersionCommand.acceptInfo(serverCommandSource::sendMessage);
            return 1;
        }));
    }

    public static void acceptInfo(Consumer<Text> sender) {
        GameVersion gameVersion = SharedConstants.getGameVersion();
        sender.accept(Text.translatable("commands.version.id", gameVersion.id()));
        sender.accept(Text.translatable("commands.version.name", gameVersion.name()));
        sender.accept(Text.translatable("commands.version.data", gameVersion.dataVersion().id()));
        sender.accept(Text.translatable("commands.version.series", gameVersion.dataVersion().series()));
        sender.accept(Text.translatable("commands.version.protocol", gameVersion.protocolVersion(), "0x" + Integer.toHexString(gameVersion.protocolVersion())));
        sender.accept(Text.translatable("commands.version.build_time", Text.of(gameVersion.buildTime())));
        sender.accept(Text.translatable("commands.version.pack.resource", gameVersion.packVersion(ResourceType.CLIENT_RESOURCES).toString()));
        sender.accept(Text.translatable("commands.version.pack.data", gameVersion.packVersion(ResourceType.SERVER_DATA).toString()));
        sender.accept(gameVersion.stable() ? STABLE_YES_TEXT : STABLE_NO_TEXT);
    }
}


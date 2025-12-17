/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  com.google.common.collect.BiMap
 *  com.google.common.collect.ImmutableBiMap
 *  com.mojang.brigadier.CommandDispatcher
 *  com.mojang.brigadier.arguments.IntegerArgumentType
 *  com.mojang.brigadier.arguments.StringArgumentType
 *  com.mojang.brigadier.builder.LiteralArgumentBuilder
 *  com.mojang.brigadier.builder.RequiredArgumentBuilder
 *  com.mojang.brigadier.context.CommandContext
 *  com.mojang.logging.LogUtils
 *  org.jspecify.annotations.Nullable
 *  org.slf4j.Logger
 */
package net.minecraft.server.command;

import com.google.common.collect.BiMap;
import com.google.common.collect.ImmutableBiMap;
import com.mojang.brigadier.CommandDispatcher;
import com.mojang.brigadier.arguments.IntegerArgumentType;
import com.mojang.brigadier.arguments.StringArgumentType;
import com.mojang.brigadier.builder.LiteralArgumentBuilder;
import com.mojang.brigadier.builder.RequiredArgumentBuilder;
import com.mojang.brigadier.context.CommandContext;
import com.mojang.logging.LogUtils;
import java.io.IOException;
import net.minecraft.registry.RegistryKey;
import net.minecraft.server.chase.ChaseClient;
import net.minecraft.server.chase.ChaseServer;
import net.minecraft.server.command.CommandManager;
import net.minecraft.server.command.ServerCommandSource;
import net.minecraft.text.Text;
import net.minecraft.world.World;
import org.jspecify.annotations.Nullable;
import org.slf4j.Logger;

public class ChaseCommand {
    private static final Logger LOGGER = LogUtils.getLogger();
    private static final String LOCALHOST = "localhost";
    private static final String BIND_ALL = "0.0.0.0";
    private static final int DEFAULT_PORT = 10000;
    private static final int INTERVAL = 100;
    public static BiMap<String, RegistryKey<World>> DIMENSIONS = ImmutableBiMap.of((Object)"o", World.OVERWORLD, (Object)"n", World.NETHER, (Object)"e", World.END);
    private static @Nullable ChaseServer server;
    private static @Nullable ChaseClient client;

    public static void register(CommandDispatcher<ServerCommandSource> dispatcher) {
        dispatcher.register((LiteralArgumentBuilder)((LiteralArgumentBuilder)((LiteralArgumentBuilder)CommandManager.literal("chase").then(((LiteralArgumentBuilder)CommandManager.literal("follow").then(((RequiredArgumentBuilder)CommandManager.argument("host", StringArgumentType.string()).executes(context -> ChaseCommand.startClient((ServerCommandSource)context.getSource(), StringArgumentType.getString((CommandContext)context, (String)"host"), 10000))).then(CommandManager.argument("port", IntegerArgumentType.integer((int)1, (int)65535)).executes(context -> ChaseCommand.startClient((ServerCommandSource)context.getSource(), StringArgumentType.getString((CommandContext)context, (String)"host"), IntegerArgumentType.getInteger((CommandContext)context, (String)"port")))))).executes(context -> ChaseCommand.startClient((ServerCommandSource)context.getSource(), LOCALHOST, 10000)))).then(((LiteralArgumentBuilder)CommandManager.literal("lead").then(((RequiredArgumentBuilder)CommandManager.argument("bind_address", StringArgumentType.string()).executes(context -> ChaseCommand.startServer((ServerCommandSource)context.getSource(), StringArgumentType.getString((CommandContext)context, (String)"bind_address"), 10000))).then(CommandManager.argument("port", IntegerArgumentType.integer((int)1024, (int)65535)).executes(context -> ChaseCommand.startServer((ServerCommandSource)context.getSource(), StringArgumentType.getString((CommandContext)context, (String)"bind_address"), IntegerArgumentType.getInteger((CommandContext)context, (String)"port")))))).executes(context -> ChaseCommand.startServer((ServerCommandSource)context.getSource(), BIND_ALL, 10000)))).then(CommandManager.literal("stop").executes(context -> ChaseCommand.stop((ServerCommandSource)context.getSource()))));
    }

    private static int stop(ServerCommandSource source) {
        if (client != null) {
            client.stop();
            source.sendFeedback(() -> Text.literal("You have now stopped chasing"), false);
            client = null;
        }
        if (server != null) {
            server.stop();
            source.sendFeedback(() -> Text.literal("You are no longer being chased"), false);
            server = null;
        }
        return 0;
    }

    private static boolean isRunning(ServerCommandSource source) {
        if (server != null) {
            source.sendError(Text.literal("Chase server is already running. Stop it using /chase stop"));
            return true;
        }
        if (client != null) {
            source.sendError(Text.literal("You are already chasing someone. Stop it using /chase stop"));
            return true;
        }
        return false;
    }

    private static int startServer(ServerCommandSource source, String ip, int port) {
        if (ChaseCommand.isRunning(source)) {
            return 0;
        }
        server = new ChaseServer(ip, port, source.getServer().getPlayerManager(), 100);
        try {
            server.start();
            source.sendFeedback(() -> Text.literal("Chase server is now running on port " + port + ". Clients can follow you using /chase follow <ip> <port>"), false);
        }
        catch (IOException iOException) {
            LOGGER.error("Failed to start chase server", (Throwable)iOException);
            source.sendError(Text.literal("Failed to start chase server on port " + port));
            server = null;
        }
        return 0;
    }

    private static int startClient(ServerCommandSource source, String ip, int port) {
        if (ChaseCommand.isRunning(source)) {
            return 0;
        }
        client = new ChaseClient(ip, port, source.getServer());
        client.start();
        source.sendFeedback(() -> Text.literal("You are now chasing " + ip + ":" + port + ". If that server does '/chase lead' then you will automatically go to the same position. Use '/chase stop' to stop chasing."), false);
        return 0;
    }
}


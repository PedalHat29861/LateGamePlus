/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  com.mojang.brigadier.CommandDispatcher
 *  com.mojang.brigadier.builder.LiteralArgumentBuilder
 *  com.mojang.brigadier.builder.RequiredArgumentBuilder
 *  com.mojang.brigadier.context.CommandContext
 */
package net.minecraft.server.command;

import com.mojang.brigadier.CommandDispatcher;
import com.mojang.brigadier.builder.LiteralArgumentBuilder;
import com.mojang.brigadier.builder.RequiredArgumentBuilder;
import com.mojang.brigadier.context.CommandContext;
import net.minecraft.command.argument.BlockPosArgumentType;
import net.minecraft.command.argument.DefaultPosArgument;
import net.minecraft.command.argument.PosArgument;
import net.minecraft.command.argument.RotationArgumentType;
import net.minecraft.server.command.CommandManager;
import net.minecraft.server.command.ServerCommandSource;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.text.Text;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Vec2f;
import net.minecraft.world.WorldProperties;

public class SetWorldSpawnCommand {
    public static void register(CommandDispatcher<ServerCommandSource> dispatcher) {
        dispatcher.register((LiteralArgumentBuilder)((LiteralArgumentBuilder)((LiteralArgumentBuilder)CommandManager.literal("setworldspawn").requires(CommandManager.requirePermissionLevel(CommandManager.GAMEMASTERS_CHECK))).executes(context -> SetWorldSpawnCommand.execute((ServerCommandSource)context.getSource(), BlockPos.ofFloored(((ServerCommandSource)context.getSource()).getPosition()), DefaultPosArgument.DEFAULT_ROTATION))).then(((RequiredArgumentBuilder)CommandManager.argument("pos", BlockPosArgumentType.blockPos()).executes(context -> SetWorldSpawnCommand.execute((ServerCommandSource)context.getSource(), BlockPosArgumentType.getValidBlockPos((CommandContext<ServerCommandSource>)context, "pos"), DefaultPosArgument.DEFAULT_ROTATION))).then(CommandManager.argument("rotation", RotationArgumentType.rotation()).executes(context -> SetWorldSpawnCommand.execute((ServerCommandSource)context.getSource(), BlockPosArgumentType.getValidBlockPos((CommandContext<ServerCommandSource>)context, "pos"), RotationArgumentType.getRotation((CommandContext<ServerCommandSource>)context, "rotation"))))));
    }

    private static int execute(ServerCommandSource source, BlockPos pos, PosArgument rotation) {
        ServerWorld serverWorld = source.getWorld();
        Vec2f vec2f = rotation.getRotation(source);
        float f = vec2f.y;
        float g = vec2f.x;
        WorldProperties.SpawnPoint spawnPoint = WorldProperties.SpawnPoint.create(serverWorld.getRegistryKey(), pos, f, g);
        serverWorld.setSpawnPoint(spawnPoint);
        source.sendFeedback(() -> Text.translatable("commands.setworldspawn.success", pos.getX(), pos.getY(), pos.getZ(), Float.valueOf(spawnPoint.yaw()), Float.valueOf(spawnPoint.pitch()), serverWorld.getRegistryKey().getValue().toString()), true);
        return 1;
    }
}


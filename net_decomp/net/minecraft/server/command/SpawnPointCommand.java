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
import java.util.Collection;
import java.util.Collections;
import net.minecraft.command.argument.BlockPosArgumentType;
import net.minecraft.command.argument.DefaultPosArgument;
import net.minecraft.command.argument.EntityArgumentType;
import net.minecraft.command.argument.PosArgument;
import net.minecraft.command.argument.RotationArgumentType;
import net.minecraft.registry.RegistryKey;
import net.minecraft.server.command.CommandManager;
import net.minecraft.server.command.ServerCommandSource;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.text.Text;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.MathHelper;
import net.minecraft.util.math.Vec2f;
import net.minecraft.world.World;
import net.minecraft.world.WorldProperties;

public class SpawnPointCommand {
    public static void register(CommandDispatcher<ServerCommandSource> dispatcher) {
        dispatcher.register((LiteralArgumentBuilder)((LiteralArgumentBuilder)((LiteralArgumentBuilder)CommandManager.literal("spawnpoint").requires(CommandManager.requirePermissionLevel(CommandManager.GAMEMASTERS_CHECK))).executes(context -> SpawnPointCommand.execute((ServerCommandSource)context.getSource(), Collections.singleton(((ServerCommandSource)context.getSource()).getPlayerOrThrow()), BlockPos.ofFloored(((ServerCommandSource)context.getSource()).getPosition()), DefaultPosArgument.DEFAULT_ROTATION))).then(((RequiredArgumentBuilder)CommandManager.argument("targets", EntityArgumentType.players()).executes(context -> SpawnPointCommand.execute((ServerCommandSource)context.getSource(), EntityArgumentType.getPlayers((CommandContext<ServerCommandSource>)context, "targets"), BlockPos.ofFloored(((ServerCommandSource)context.getSource()).getPosition()), DefaultPosArgument.DEFAULT_ROTATION))).then(((RequiredArgumentBuilder)CommandManager.argument("pos", BlockPosArgumentType.blockPos()).executes(context -> SpawnPointCommand.execute((ServerCommandSource)context.getSource(), EntityArgumentType.getPlayers((CommandContext<ServerCommandSource>)context, "targets"), BlockPosArgumentType.getValidBlockPos((CommandContext<ServerCommandSource>)context, "pos"), DefaultPosArgument.DEFAULT_ROTATION))).then(CommandManager.argument("rotation", RotationArgumentType.rotation()).executes(context -> SpawnPointCommand.execute((ServerCommandSource)context.getSource(), EntityArgumentType.getPlayers((CommandContext<ServerCommandSource>)context, "targets"), BlockPosArgumentType.getValidBlockPos((CommandContext<ServerCommandSource>)context, "pos"), RotationArgumentType.getRotation((CommandContext<ServerCommandSource>)context, "rotation")))))));
    }

    private static int execute(ServerCommandSource source, Collection<ServerPlayerEntity> targets, BlockPos pos, PosArgument rotation) {
        RegistryKey<World> registryKey = source.getWorld().getRegistryKey();
        Vec2f vec2f = rotation.getRotation(source);
        float f = MathHelper.wrapDegrees(vec2f.y);
        float g = MathHelper.clamp(vec2f.x, -90.0f, 90.0f);
        for (ServerPlayerEntity serverPlayerEntity : targets) {
            serverPlayerEntity.setSpawnPoint(new ServerPlayerEntity.Respawn(WorldProperties.SpawnPoint.create(registryKey, pos, f, g), true), false);
        }
        String string = registryKey.getValue().toString();
        if (targets.size() == 1) {
            source.sendFeedback(() -> Text.translatable("commands.spawnpoint.success.single", pos.getX(), pos.getY(), pos.getZ(), Float.valueOf(f), Float.valueOf(g), string, ((ServerPlayerEntity)targets.iterator().next()).getDisplayName()), true);
        } else {
            source.sendFeedback(() -> Text.translatable("commands.spawnpoint.success.multiple", pos.getX(), pos.getY(), pos.getZ(), Float.valueOf(f), Float.valueOf(g), string, targets.size()), true);
        }
        return targets.size();
    }
}


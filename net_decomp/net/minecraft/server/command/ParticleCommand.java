/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  com.mojang.brigadier.CommandDispatcher
 *  com.mojang.brigadier.Message
 *  com.mojang.brigadier.arguments.FloatArgumentType
 *  com.mojang.brigadier.arguments.IntegerArgumentType
 *  com.mojang.brigadier.builder.LiteralArgumentBuilder
 *  com.mojang.brigadier.builder.RequiredArgumentBuilder
 *  com.mojang.brigadier.context.CommandContext
 *  com.mojang.brigadier.exceptions.CommandSyntaxException
 *  com.mojang.brigadier.exceptions.SimpleCommandExceptionType
 */
package net.minecraft.server.command;

import com.mojang.brigadier.CommandDispatcher;
import com.mojang.brigadier.Message;
import com.mojang.brigadier.arguments.FloatArgumentType;
import com.mojang.brigadier.arguments.IntegerArgumentType;
import com.mojang.brigadier.builder.LiteralArgumentBuilder;
import com.mojang.brigadier.builder.RequiredArgumentBuilder;
import com.mojang.brigadier.context.CommandContext;
import com.mojang.brigadier.exceptions.CommandSyntaxException;
import com.mojang.brigadier.exceptions.SimpleCommandExceptionType;
import java.util.Collection;
import net.minecraft.command.CommandRegistryAccess;
import net.minecraft.command.argument.EntityArgumentType;
import net.minecraft.command.argument.ParticleEffectArgumentType;
import net.minecraft.command.argument.Vec3ArgumentType;
import net.minecraft.particle.ParticleEffect;
import net.minecraft.registry.Registries;
import net.minecraft.server.command.CommandManager;
import net.minecraft.server.command.ServerCommandSource;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.text.Text;
import net.minecraft.util.math.Vec3d;

public class ParticleCommand {
    private static final SimpleCommandExceptionType FAILED_EXCEPTION = new SimpleCommandExceptionType((Message)Text.translatable("commands.particle.failed"));

    public static void register(CommandDispatcher<ServerCommandSource> dispatcher, CommandRegistryAccess registryAccess) {
        dispatcher.register((LiteralArgumentBuilder)((LiteralArgumentBuilder)CommandManager.literal("particle").requires(CommandManager.requirePermissionLevel(CommandManager.GAMEMASTERS_CHECK))).then(((RequiredArgumentBuilder)CommandManager.argument("name", ParticleEffectArgumentType.particleEffect(registryAccess)).executes(context -> ParticleCommand.execute((ServerCommandSource)context.getSource(), ParticleEffectArgumentType.getParticle((CommandContext<ServerCommandSource>)context, "name"), ((ServerCommandSource)context.getSource()).getPosition(), Vec3d.ZERO, 0.0f, 0, false, ((ServerCommandSource)context.getSource()).getServer().getPlayerManager().getPlayerList()))).then(((RequiredArgumentBuilder)CommandManager.argument("pos", Vec3ArgumentType.vec3()).executes(context -> ParticleCommand.execute((ServerCommandSource)context.getSource(), ParticleEffectArgumentType.getParticle((CommandContext<ServerCommandSource>)context, "name"), Vec3ArgumentType.getVec3((CommandContext<ServerCommandSource>)context, "pos"), Vec3d.ZERO, 0.0f, 0, false, ((ServerCommandSource)context.getSource()).getServer().getPlayerManager().getPlayerList()))).then(CommandManager.argument("delta", Vec3ArgumentType.vec3(false)).then(CommandManager.argument("speed", FloatArgumentType.floatArg((float)0.0f)).then(((RequiredArgumentBuilder)((RequiredArgumentBuilder)CommandManager.argument("count", IntegerArgumentType.integer((int)0)).executes(context -> ParticleCommand.execute((ServerCommandSource)context.getSource(), ParticleEffectArgumentType.getParticle((CommandContext<ServerCommandSource>)context, "name"), Vec3ArgumentType.getVec3((CommandContext<ServerCommandSource>)context, "pos"), Vec3ArgumentType.getVec3((CommandContext<ServerCommandSource>)context, "delta"), FloatArgumentType.getFloat((CommandContext)context, (String)"speed"), IntegerArgumentType.getInteger((CommandContext)context, (String)"count"), false, ((ServerCommandSource)context.getSource()).getServer().getPlayerManager().getPlayerList()))).then(((LiteralArgumentBuilder)CommandManager.literal("force").executes(context -> ParticleCommand.execute((ServerCommandSource)context.getSource(), ParticleEffectArgumentType.getParticle((CommandContext<ServerCommandSource>)context, "name"), Vec3ArgumentType.getVec3((CommandContext<ServerCommandSource>)context, "pos"), Vec3ArgumentType.getVec3((CommandContext<ServerCommandSource>)context, "delta"), FloatArgumentType.getFloat((CommandContext)context, (String)"speed"), IntegerArgumentType.getInteger((CommandContext)context, (String)"count"), true, ((ServerCommandSource)context.getSource()).getServer().getPlayerManager().getPlayerList()))).then(CommandManager.argument("viewers", EntityArgumentType.players()).executes(context -> ParticleCommand.execute((ServerCommandSource)context.getSource(), ParticleEffectArgumentType.getParticle((CommandContext<ServerCommandSource>)context, "name"), Vec3ArgumentType.getVec3((CommandContext<ServerCommandSource>)context, "pos"), Vec3ArgumentType.getVec3((CommandContext<ServerCommandSource>)context, "delta"), FloatArgumentType.getFloat((CommandContext)context, (String)"speed"), IntegerArgumentType.getInteger((CommandContext)context, (String)"count"), true, EntityArgumentType.getPlayers((CommandContext<ServerCommandSource>)context, "viewers")))))).then(((LiteralArgumentBuilder)CommandManager.literal("normal").executes(context -> ParticleCommand.execute((ServerCommandSource)context.getSource(), ParticleEffectArgumentType.getParticle((CommandContext<ServerCommandSource>)context, "name"), Vec3ArgumentType.getVec3((CommandContext<ServerCommandSource>)context, "pos"), Vec3ArgumentType.getVec3((CommandContext<ServerCommandSource>)context, "delta"), FloatArgumentType.getFloat((CommandContext)context, (String)"speed"), IntegerArgumentType.getInteger((CommandContext)context, (String)"count"), false, ((ServerCommandSource)context.getSource()).getServer().getPlayerManager().getPlayerList()))).then(CommandManager.argument("viewers", EntityArgumentType.players()).executes(context -> ParticleCommand.execute((ServerCommandSource)context.getSource(), ParticleEffectArgumentType.getParticle((CommandContext<ServerCommandSource>)context, "name"), Vec3ArgumentType.getVec3((CommandContext<ServerCommandSource>)context, "pos"), Vec3ArgumentType.getVec3((CommandContext<ServerCommandSource>)context, "delta"), FloatArgumentType.getFloat((CommandContext)context, (String)"speed"), IntegerArgumentType.getInteger((CommandContext)context, (String)"count"), false, EntityArgumentType.getPlayers((CommandContext<ServerCommandSource>)context, "viewers")))))))))));
    }

    private static int execute(ServerCommandSource source, ParticleEffect parameters, Vec3d pos, Vec3d delta, float speed, int count, boolean force, Collection<ServerPlayerEntity> viewers) throws CommandSyntaxException {
        int i = 0;
        for (ServerPlayerEntity serverPlayerEntity : viewers) {
            if (!source.getWorld().spawnParticles(serverPlayerEntity, parameters, force, false, pos.x, pos.y, pos.z, count, delta.x, delta.y, delta.z, speed)) continue;
            ++i;
        }
        if (i == 0) {
            throw FAILED_EXCEPTION.create();
        }
        source.sendFeedback(() -> Text.translatable("commands.particle.success", Registries.PARTICLE_TYPE.getId(parameters.getType()).toString()), true);
        return i;
    }
}


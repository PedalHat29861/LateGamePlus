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
 *  com.mojang.brigadier.exceptions.Dynamic2CommandExceptionType
 *  com.mojang.brigadier.exceptions.DynamicCommandExceptionType
 *  com.mojang.brigadier.exceptions.SimpleCommandExceptionType
 */
package net.minecraft.server.command;

import com.mojang.brigadier.CommandDispatcher;
import com.mojang.brigadier.Message;
import com.mojang.brigadier.builder.LiteralArgumentBuilder;
import com.mojang.brigadier.builder.RequiredArgumentBuilder;
import com.mojang.brigadier.context.CommandContext;
import com.mojang.brigadier.exceptions.CommandSyntaxException;
import com.mojang.brigadier.exceptions.Dynamic2CommandExceptionType;
import com.mojang.brigadier.exceptions.DynamicCommandExceptionType;
import com.mojang.brigadier.exceptions.SimpleCommandExceptionType;
import net.minecraft.command.argument.EntityArgumentType;
import net.minecraft.entity.Entity;
import net.minecraft.entity.EntityType;
import net.minecraft.server.command.CommandManager;
import net.minecraft.server.command.ServerCommandSource;
import net.minecraft.text.Text;

public class RideCommand {
    private static final DynamicCommandExceptionType NOT_RIDING_EXCEPTION = new DynamicCommandExceptionType(entity -> Text.stringifiedTranslatable("commands.ride.not_riding", entity));
    private static final Dynamic2CommandExceptionType ALREADY_RIDING_EXCEPTION = new Dynamic2CommandExceptionType((rider, vehicle) -> Text.stringifiedTranslatable("commands.ride.already_riding", rider, vehicle));
    private static final Dynamic2CommandExceptionType GENERIC_FAILURE_EXCEPTION = new Dynamic2CommandExceptionType((rider, vehicle) -> Text.stringifiedTranslatable("commands.ride.mount.failure.generic", rider, vehicle));
    private static final SimpleCommandExceptionType CANT_RIDE_PLAYERS_EXCEPTION = new SimpleCommandExceptionType((Message)Text.translatable("commands.ride.mount.failure.cant_ride_players"));
    private static final SimpleCommandExceptionType RIDE_LOOP_EXCEPTION = new SimpleCommandExceptionType((Message)Text.translatable("commands.ride.mount.failure.loop"));
    private static final SimpleCommandExceptionType WRONG_DIMENSION_EXCEPTION = new SimpleCommandExceptionType((Message)Text.translatable("commands.ride.mount.failure.wrong_dimension"));

    public static void register(CommandDispatcher<ServerCommandSource> dispatcher) {
        dispatcher.register((LiteralArgumentBuilder)((LiteralArgumentBuilder)CommandManager.literal("ride").requires(CommandManager.requirePermissionLevel(CommandManager.GAMEMASTERS_CHECK))).then(((RequiredArgumentBuilder)CommandManager.argument("target", EntityArgumentType.entity()).then(CommandManager.literal("mount").then(CommandManager.argument("vehicle", EntityArgumentType.entity()).executes(context -> RideCommand.executeMount((ServerCommandSource)context.getSource(), EntityArgumentType.getEntity((CommandContext<ServerCommandSource>)context, "target"), EntityArgumentType.getEntity((CommandContext<ServerCommandSource>)context, "vehicle")))))).then(CommandManager.literal("dismount").executes(context -> RideCommand.executeDismount((ServerCommandSource)context.getSource(), EntityArgumentType.getEntity((CommandContext<ServerCommandSource>)context, "target"))))));
    }

    private static int executeMount(ServerCommandSource source, Entity rider, Entity vehicle) throws CommandSyntaxException {
        Entity entity = rider.getVehicle();
        if (entity != null) {
            throw ALREADY_RIDING_EXCEPTION.create((Object)rider.getDisplayName(), (Object)entity.getDisplayName());
        }
        if (vehicle.getType() == EntityType.PLAYER) {
            throw CANT_RIDE_PLAYERS_EXCEPTION.create();
        }
        if (rider.streamSelfAndPassengers().anyMatch(passenger -> passenger == vehicle)) {
            throw RIDE_LOOP_EXCEPTION.create();
        }
        if (rider.getEntityWorld() != vehicle.getEntityWorld()) {
            throw WRONG_DIMENSION_EXCEPTION.create();
        }
        if (!rider.startRiding(vehicle, true, true)) {
            throw GENERIC_FAILURE_EXCEPTION.create((Object)rider.getDisplayName(), (Object)vehicle.getDisplayName());
        }
        source.sendFeedback(() -> Text.translatable("commands.ride.mount.success", rider.getDisplayName(), vehicle.getDisplayName()), true);
        return 1;
    }

    private static int executeDismount(ServerCommandSource source, Entity rider) throws CommandSyntaxException {
        Entity entity = rider.getVehicle();
        if (entity == null) {
            throw NOT_RIDING_EXCEPTION.create((Object)rider.getDisplayName());
        }
        rider.stopRiding();
        source.sendFeedback(() -> Text.translatable("commands.ride.dismount.success", rider.getDisplayName(), entity.getDisplayName()), true);
        return 1;
    }
}


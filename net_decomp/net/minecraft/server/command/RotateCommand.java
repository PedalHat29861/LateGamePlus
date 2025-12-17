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
import net.minecraft.command.argument.EntityAnchorArgumentType;
import net.minecraft.command.argument.EntityArgumentType;
import net.minecraft.command.argument.PosArgument;
import net.minecraft.command.argument.RotationArgumentType;
import net.minecraft.command.argument.Vec3ArgumentType;
import net.minecraft.entity.Entity;
import net.minecraft.server.command.CommandManager;
import net.minecraft.server.command.LookTarget;
import net.minecraft.server.command.ServerCommandSource;
import net.minecraft.text.Text;
import net.minecraft.util.math.Vec2f;

public class RotateCommand {
    public static void register(CommandDispatcher<ServerCommandSource> dispatcher) {
        dispatcher.register((LiteralArgumentBuilder)((LiteralArgumentBuilder)CommandManager.literal("rotate").requires(CommandManager.requirePermissionLevel(CommandManager.GAMEMASTERS_CHECK))).then(((RequiredArgumentBuilder)CommandManager.argument("target", EntityArgumentType.entity()).then(CommandManager.argument("rotation", RotationArgumentType.rotation()).executes(context -> RotateCommand.rotateToPos((ServerCommandSource)context.getSource(), EntityArgumentType.getEntity((CommandContext<ServerCommandSource>)context, "target"), RotationArgumentType.getRotation((CommandContext<ServerCommandSource>)context, "rotation"))))).then(((LiteralArgumentBuilder)CommandManager.literal("facing").then(CommandManager.literal("entity").then(((RequiredArgumentBuilder)CommandManager.argument("facingEntity", EntityArgumentType.entity()).executes(context -> RotateCommand.rotateFacingLookTarget((ServerCommandSource)context.getSource(), EntityArgumentType.getEntity((CommandContext<ServerCommandSource>)context, "target"), new LookTarget.LookAtEntity(EntityArgumentType.getEntity((CommandContext<ServerCommandSource>)context, "facingEntity"), EntityAnchorArgumentType.EntityAnchor.FEET)))).then(CommandManager.argument("facingAnchor", EntityAnchorArgumentType.entityAnchor()).executes(context -> RotateCommand.rotateFacingLookTarget((ServerCommandSource)context.getSource(), EntityArgumentType.getEntity((CommandContext<ServerCommandSource>)context, "target"), new LookTarget.LookAtEntity(EntityArgumentType.getEntity((CommandContext<ServerCommandSource>)context, "facingEntity"), EntityAnchorArgumentType.getEntityAnchor((CommandContext<ServerCommandSource>)context, "facingAnchor")))))))).then(CommandManager.argument("facingLocation", Vec3ArgumentType.vec3()).executes(context -> RotateCommand.rotateFacingLookTarget((ServerCommandSource)context.getSource(), EntityArgumentType.getEntity((CommandContext<ServerCommandSource>)context, "target"), new LookTarget.LookAtPosition(Vec3ArgumentType.getVec3((CommandContext<ServerCommandSource>)context, "facingLocation"))))))));
    }

    private static int rotateToPos(ServerCommandSource source, Entity entity, PosArgument pos) {
        Vec2f vec2f = pos.getRotation(source);
        float f = pos.isYRelative() ? vec2f.y - entity.getYaw() : vec2f.y;
        float g = pos.isXRelative() ? vec2f.x - entity.getPitch() : vec2f.x;
        entity.rotate(f, pos.isYRelative(), g, pos.isXRelative());
        source.sendFeedback(() -> Text.translatable("commands.rotate.success", entity.getDisplayName()), true);
        return 1;
    }

    private static int rotateFacingLookTarget(ServerCommandSource source, Entity entity, LookTarget lookTarget) {
        lookTarget.look(source, entity);
        source.sendFeedback(() -> Text.translatable("commands.rotate.success", entity.getDisplayName()), true);
        return 1;
    }
}


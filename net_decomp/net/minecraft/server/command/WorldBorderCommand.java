/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  com.mojang.brigadier.CommandDispatcher
 *  com.mojang.brigadier.Message
 *  com.mojang.brigadier.arguments.DoubleArgumentType
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
import com.mojang.brigadier.arguments.DoubleArgumentType;
import com.mojang.brigadier.arguments.FloatArgumentType;
import com.mojang.brigadier.arguments.IntegerArgumentType;
import com.mojang.brigadier.builder.LiteralArgumentBuilder;
import com.mojang.brigadier.builder.RequiredArgumentBuilder;
import com.mojang.brigadier.context.CommandContext;
import com.mojang.brigadier.exceptions.CommandSyntaxException;
import com.mojang.brigadier.exceptions.SimpleCommandExceptionType;
import java.util.Locale;
import net.minecraft.command.argument.TimeArgumentType;
import net.minecraft.command.argument.Vec2ArgumentType;
import net.minecraft.server.command.CommandManager;
import net.minecraft.server.command.ServerCommandSource;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.text.Text;
import net.minecraft.util.math.MathHelper;
import net.minecraft.util.math.Vec2f;
import net.minecraft.world.border.WorldBorder;

public class WorldBorderCommand {
    private static final SimpleCommandExceptionType CENTER_FAILED_EXCEPTION = new SimpleCommandExceptionType((Message)Text.translatable("commands.worldborder.center.failed"));
    private static final SimpleCommandExceptionType SET_FAILED_NO_CHANGE_EXCEPTION = new SimpleCommandExceptionType((Message)Text.translatable("commands.worldborder.set.failed.nochange"));
    private static final SimpleCommandExceptionType SET_FAILED_SMALL_EXCEPTION = new SimpleCommandExceptionType((Message)Text.translatable("commands.worldborder.set.failed.small"));
    private static final SimpleCommandExceptionType SET_FAILED_BIG_EXCEPTION = new SimpleCommandExceptionType((Message)Text.translatable("commands.worldborder.set.failed.big", 5.9999968E7));
    private static final SimpleCommandExceptionType SET_FAILED_FAR_EXCEPTION = new SimpleCommandExceptionType((Message)Text.translatable("commands.worldborder.set.failed.far", 2.9999984E7));
    private static final SimpleCommandExceptionType WARNING_TIME_FAILED_EXCEPTION = new SimpleCommandExceptionType((Message)Text.translatable("commands.worldborder.warning.time.failed"));
    private static final SimpleCommandExceptionType WARNING_DISTANCE_FAILED_EXCEPTION = new SimpleCommandExceptionType((Message)Text.translatable("commands.worldborder.warning.distance.failed"));
    private static final SimpleCommandExceptionType DAMAGE_BUFFER_FAILED_EXCEPTION = new SimpleCommandExceptionType((Message)Text.translatable("commands.worldborder.damage.buffer.failed"));
    private static final SimpleCommandExceptionType DAMAGE_AMOUNT_FAILED_EXCEPTION = new SimpleCommandExceptionType((Message)Text.translatable("commands.worldborder.damage.amount.failed"));

    public static void register(CommandDispatcher<ServerCommandSource> dispatcher) {
        dispatcher.register((LiteralArgumentBuilder)((LiteralArgumentBuilder)((LiteralArgumentBuilder)((LiteralArgumentBuilder)((LiteralArgumentBuilder)((LiteralArgumentBuilder)((LiteralArgumentBuilder)CommandManager.literal("worldborder").requires(CommandManager.requirePermissionLevel(CommandManager.GAMEMASTERS_CHECK))).then(CommandManager.literal("add").then(((RequiredArgumentBuilder)CommandManager.argument("distance", DoubleArgumentType.doubleArg((double)-5.9999968E7, (double)5.9999968E7)).executes(context -> WorldBorderCommand.executeSet((ServerCommandSource)context.getSource(), ((ServerCommandSource)context.getSource()).getWorld().getWorldBorder().getSize() + DoubleArgumentType.getDouble((CommandContext)context, (String)"distance"), 0L))).then(CommandManager.argument("time", TimeArgumentType.time(0)).executes(context -> WorldBorderCommand.executeSet((ServerCommandSource)context.getSource(), ((ServerCommandSource)context.getSource()).getWorld().getWorldBorder().getSize() + DoubleArgumentType.getDouble((CommandContext)context, (String)"distance"), ((ServerCommandSource)context.getSource()).getWorld().getWorldBorder().getSizeLerpTime() + (long)IntegerArgumentType.getInteger((CommandContext)context, (String)"time"))))))).then(CommandManager.literal("set").then(((RequiredArgumentBuilder)CommandManager.argument("distance", DoubleArgumentType.doubleArg((double)-5.9999968E7, (double)5.9999968E7)).executes(context -> WorldBorderCommand.executeSet((ServerCommandSource)context.getSource(), DoubleArgumentType.getDouble((CommandContext)context, (String)"distance"), 0L))).then(CommandManager.argument("time", TimeArgumentType.time(0)).executes(context -> WorldBorderCommand.executeSet((ServerCommandSource)context.getSource(), DoubleArgumentType.getDouble((CommandContext)context, (String)"distance"), IntegerArgumentType.getInteger((CommandContext)context, (String)"time"))))))).then(CommandManager.literal("center").then(CommandManager.argument("pos", Vec2ArgumentType.vec2()).executes(context -> WorldBorderCommand.executeCenter((ServerCommandSource)context.getSource(), Vec2ArgumentType.getVec2((CommandContext<ServerCommandSource>)context, "pos")))))).then(((LiteralArgumentBuilder)CommandManager.literal("damage").then(CommandManager.literal("amount").then(CommandManager.argument("damagePerBlock", FloatArgumentType.floatArg((float)0.0f)).executes(context -> WorldBorderCommand.executeDamage((ServerCommandSource)context.getSource(), FloatArgumentType.getFloat((CommandContext)context, (String)"damagePerBlock")))))).then(CommandManager.literal("buffer").then(CommandManager.argument("distance", FloatArgumentType.floatArg((float)0.0f)).executes(context -> WorldBorderCommand.executeBuffer((ServerCommandSource)context.getSource(), FloatArgumentType.getFloat((CommandContext)context, (String)"distance"))))))).then(CommandManager.literal("get").executes(context -> WorldBorderCommand.executeGet((ServerCommandSource)context.getSource())))).then(((LiteralArgumentBuilder)CommandManager.literal("warning").then(CommandManager.literal("distance").then(CommandManager.argument("distance", IntegerArgumentType.integer((int)0)).executes(context -> WorldBorderCommand.executeWarningDistance((ServerCommandSource)context.getSource(), IntegerArgumentType.getInteger((CommandContext)context, (String)"distance")))))).then(CommandManager.literal("time").then(CommandManager.argument("time", TimeArgumentType.time(0)).executes(context -> WorldBorderCommand.executeWarningTime((ServerCommandSource)context.getSource(), IntegerArgumentType.getInteger((CommandContext)context, (String)"time")))))));
    }

    private static int executeBuffer(ServerCommandSource source, float distance) throws CommandSyntaxException {
        WorldBorder worldBorder = source.getWorld().getWorldBorder();
        if (worldBorder.getSafeZone() == (double)distance) {
            throw DAMAGE_BUFFER_FAILED_EXCEPTION.create();
        }
        worldBorder.setSafeZone(distance);
        source.sendFeedback(() -> Text.translatable("commands.worldborder.damage.buffer.success", String.format(Locale.ROOT, "%.2f", Float.valueOf(distance))), true);
        return (int)distance;
    }

    private static int executeDamage(ServerCommandSource source, float damagePerBlock) throws CommandSyntaxException {
        WorldBorder worldBorder = source.getWorld().getWorldBorder();
        if (worldBorder.getDamagePerBlock() == (double)damagePerBlock) {
            throw DAMAGE_AMOUNT_FAILED_EXCEPTION.create();
        }
        worldBorder.setDamagePerBlock(damagePerBlock);
        source.sendFeedback(() -> Text.translatable("commands.worldborder.damage.amount.success", String.format(Locale.ROOT, "%.2f", Float.valueOf(damagePerBlock))), true);
        return (int)damagePerBlock;
    }

    private static int executeWarningTime(ServerCommandSource source, int time) throws CommandSyntaxException {
        WorldBorder worldBorder = source.getWorld().getWorldBorder();
        if (worldBorder.getWarningTime() == time) {
            throw WARNING_TIME_FAILED_EXCEPTION.create();
        }
        worldBorder.setWarningTime(time);
        source.sendFeedback(() -> Text.translatable("commands.worldborder.warning.time.success", WorldBorderCommand.toSeconds(time)), true);
        return time;
    }

    private static int executeWarningDistance(ServerCommandSource source, int distance) throws CommandSyntaxException {
        WorldBorder worldBorder = source.getWorld().getWorldBorder();
        if (worldBorder.getWarningBlocks() == distance) {
            throw WARNING_DISTANCE_FAILED_EXCEPTION.create();
        }
        worldBorder.setWarningBlocks(distance);
        source.sendFeedback(() -> Text.translatable("commands.worldborder.warning.distance.success", distance), true);
        return distance;
    }

    private static int executeGet(ServerCommandSource source) {
        double d = source.getWorld().getWorldBorder().getSize();
        source.sendFeedback(() -> Text.translatable("commands.worldborder.get", String.format(Locale.ROOT, "%.0f", d)), false);
        return MathHelper.floor(d + 0.5);
    }

    private static int executeCenter(ServerCommandSource source, Vec2f pos) throws CommandSyntaxException {
        WorldBorder worldBorder = source.getWorld().getWorldBorder();
        if (worldBorder.getCenterX() == (double)pos.x && worldBorder.getCenterZ() == (double)pos.y) {
            throw CENTER_FAILED_EXCEPTION.create();
        }
        if ((double)Math.abs(pos.x) > 2.9999984E7 || (double)Math.abs(pos.y) > 2.9999984E7) {
            throw SET_FAILED_FAR_EXCEPTION.create();
        }
        worldBorder.setCenter(pos.x, pos.y);
        source.sendFeedback(() -> Text.translatable("commands.worldborder.center.success", String.format(Locale.ROOT, "%.2f", Float.valueOf(vec2f.x)), String.format(Locale.ROOT, "%.2f", Float.valueOf(vec2f.y))), true);
        return 0;
    }

    private static int executeSet(ServerCommandSource source, double distance, long time) throws CommandSyntaxException {
        ServerWorld serverWorld = source.getWorld();
        WorldBorder worldBorder = serverWorld.getWorldBorder();
        double d = worldBorder.getSize();
        if (d == distance) {
            throw SET_FAILED_NO_CHANGE_EXCEPTION.create();
        }
        if (distance < 1.0) {
            throw SET_FAILED_SMALL_EXCEPTION.create();
        }
        if (distance > 5.9999968E7) {
            throw SET_FAILED_BIG_EXCEPTION.create();
        }
        String string = String.format(Locale.ROOT, "%.1f", distance);
        if (time > 0L) {
            worldBorder.interpolateSize(d, distance, time, serverWorld.getTime());
            if (distance > d) {
                source.sendFeedback(() -> Text.translatable("commands.worldborder.set.grow", string, WorldBorderCommand.toSeconds(time)), true);
            } else {
                source.sendFeedback(() -> Text.translatable("commands.worldborder.set.shrink", string, WorldBorderCommand.toSeconds(time)), true);
            }
        } else {
            worldBorder.setSize(distance);
            source.sendFeedback(() -> Text.translatable("commands.worldborder.set.immediate", string), true);
        }
        return (int)(distance - d);
    }

    private static String toSeconds(long ticks) {
        return String.format(Locale.ROOT, "%.2f", (double)ticks / 20.0);
    }
}


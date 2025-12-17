/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  com.mojang.brigadier.CommandDispatcher
 *  com.mojang.brigadier.arguments.FloatArgumentType
 *  com.mojang.brigadier.arguments.IntegerArgumentType
 *  com.mojang.brigadier.builder.LiteralArgumentBuilder
 *  com.mojang.brigadier.context.CommandContext
 */
package net.minecraft.server.command;

import com.mojang.brigadier.CommandDispatcher;
import com.mojang.brigadier.arguments.FloatArgumentType;
import com.mojang.brigadier.arguments.IntegerArgumentType;
import com.mojang.brigadier.builder.LiteralArgumentBuilder;
import com.mojang.brigadier.context.CommandContext;
import java.util.Arrays;
import java.util.Locale;
import net.minecraft.command.CommandSource;
import net.minecraft.command.argument.TimeArgumentType;
import net.minecraft.server.ServerTickManager;
import net.minecraft.server.command.CommandManager;
import net.minecraft.server.command.ServerCommandSource;
import net.minecraft.text.Text;
import net.minecraft.util.TimeHelper;

public class TickCommand {
    private static final float MAX_TICK_RATE = 10000.0f;
    private static final String DEFAULT_TICK_RATE_STRING = String.valueOf(20);

    public static void register(CommandDispatcher<ServerCommandSource> dispatcher) {
        dispatcher.register((LiteralArgumentBuilder)((LiteralArgumentBuilder)((LiteralArgumentBuilder)((LiteralArgumentBuilder)((LiteralArgumentBuilder)((LiteralArgumentBuilder)((LiteralArgumentBuilder)CommandManager.literal("tick").requires(CommandManager.requirePermissionLevel(CommandManager.ADMINS_CHECK))).then(CommandManager.literal("query").executes(context -> TickCommand.executeQuery((ServerCommandSource)context.getSource())))).then(CommandManager.literal("rate").then(CommandManager.argument("rate", FloatArgumentType.floatArg((float)1.0f, (float)10000.0f)).suggests((context, suggestionsBuilder) -> CommandSource.suggestMatching(new String[]{DEFAULT_TICK_RATE_STRING}, suggestionsBuilder)).executes(context -> TickCommand.executeRate((ServerCommandSource)context.getSource(), FloatArgumentType.getFloat((CommandContext)context, (String)"rate")))))).then(((LiteralArgumentBuilder)((LiteralArgumentBuilder)CommandManager.literal("step").executes(context -> TickCommand.executeStep((ServerCommandSource)context.getSource(), 1))).then(CommandManager.literal("stop").executes(context -> TickCommand.executeStopStep((ServerCommandSource)context.getSource())))).then(CommandManager.argument("time", TimeArgumentType.time(1)).suggests((context, suggestionsBuilder) -> CommandSource.suggestMatching(new String[]{"1t", "1s"}, suggestionsBuilder)).executes(context -> TickCommand.executeStep((ServerCommandSource)context.getSource(), IntegerArgumentType.getInteger((CommandContext)context, (String)"time")))))).then(((LiteralArgumentBuilder)CommandManager.literal("sprint").then(CommandManager.literal("stop").executes(context -> TickCommand.executeStopSprint((ServerCommandSource)context.getSource())))).then(CommandManager.argument("time", TimeArgumentType.time(1)).suggests((context, suggestionsBuilder) -> CommandSource.suggestMatching(new String[]{"60s", "1d", "3d"}, suggestionsBuilder)).executes(context -> TickCommand.executeSprint((ServerCommandSource)context.getSource(), IntegerArgumentType.getInteger((CommandContext)context, (String)"time")))))).then(CommandManager.literal("unfreeze").executes(context -> TickCommand.executeFreeze((ServerCommandSource)context.getSource(), false)))).then(CommandManager.literal("freeze").executes(context -> TickCommand.executeFreeze((ServerCommandSource)context.getSource(), true))));
    }

    private static String format(long nanos) {
        return String.format(Locale.ROOT, "%.1f", Float.valueOf((float)nanos / (float)TimeHelper.MILLI_IN_NANOS));
    }

    private static int executeRate(ServerCommandSource source, float rate) {
        ServerTickManager serverTickManager = source.getServer().getTickManager();
        serverTickManager.setTickRate(rate);
        String string = String.format(Locale.ROOT, "%.1f", Float.valueOf(rate));
        source.sendFeedback(() -> Text.translatable("commands.tick.rate.success", string), true);
        return (int)rate;
    }

    private static int executeQuery(ServerCommandSource source) {
        ServerTickManager serverTickManager = source.getServer().getTickManager();
        String string = TickCommand.format(source.getServer().getAverageNanosPerTick());
        float f = serverTickManager.getTickRate();
        String string2 = String.format(Locale.ROOT, "%.1f", Float.valueOf(f));
        if (serverTickManager.isSprinting()) {
            source.sendFeedback(() -> Text.translatable("commands.tick.status.sprinting"), false);
            source.sendFeedback(() -> Text.translatable("commands.tick.query.rate.sprinting", string2, string), false);
        } else {
            if (serverTickManager.isFrozen()) {
                source.sendFeedback(() -> Text.translatable("commands.tick.status.frozen"), false);
            } else if (serverTickManager.getNanosPerTick() < source.getServer().getAverageNanosPerTick()) {
                source.sendFeedback(() -> Text.translatable("commands.tick.status.lagging"), false);
            } else {
                source.sendFeedback(() -> Text.translatable("commands.tick.status.running"), false);
            }
            String string3 = TickCommand.format(serverTickManager.getNanosPerTick());
            source.sendFeedback(() -> Text.translatable("commands.tick.query.rate.running", string2, string, string3), false);
        }
        long[] ls = Arrays.copyOf(source.getServer().getTickTimes(), source.getServer().getTickTimes().length);
        Arrays.sort(ls);
        String string4 = TickCommand.format(ls[ls.length / 2]);
        String string5 = TickCommand.format(ls[(int)((double)ls.length * 0.95)]);
        String string6 = TickCommand.format(ls[(int)((double)ls.length * 0.99)]);
        source.sendFeedback(() -> Text.translatable("commands.tick.query.percentiles", string4, string5, string6, ls.length), false);
        return (int)f;
    }

    private static int executeSprint(ServerCommandSource source, int ticks) {
        boolean bl = source.getServer().getTickManager().startSprint(ticks);
        if (bl) {
            source.sendFeedback(() -> Text.translatable("commands.tick.sprint.stop.success"), true);
        }
        source.sendFeedback(() -> Text.translatable("commands.tick.status.sprinting"), true);
        return 1;
    }

    private static int executeFreeze(ServerCommandSource source, boolean frozen) {
        ServerTickManager serverTickManager = source.getServer().getTickManager();
        if (frozen) {
            if (serverTickManager.isSprinting()) {
                serverTickManager.stopSprinting();
            }
            if (serverTickManager.isStepping()) {
                serverTickManager.stopStepping();
            }
        }
        serverTickManager.setFrozen(frozen);
        if (frozen) {
            source.sendFeedback(() -> Text.translatable("commands.tick.status.frozen"), true);
        } else {
            source.sendFeedback(() -> Text.translatable("commands.tick.status.running"), true);
        }
        return frozen ? 1 : 0;
    }

    private static int executeStep(ServerCommandSource source, int steps) {
        ServerTickManager serverTickManager = source.getServer().getTickManager();
        boolean bl = serverTickManager.step(steps);
        if (bl) {
            source.sendFeedback(() -> Text.translatable("commands.tick.step.success", steps), true);
        } else {
            source.sendError(Text.translatable("commands.tick.step.fail"));
        }
        return 1;
    }

    private static int executeStopStep(ServerCommandSource source) {
        ServerTickManager serverTickManager = source.getServer().getTickManager();
        boolean bl = serverTickManager.stopStepping();
        if (bl) {
            source.sendFeedback(() -> Text.translatable("commands.tick.step.stop.success"), true);
            return 1;
        }
        source.sendError(Text.translatable("commands.tick.step.stop.fail"));
        return 0;
    }

    private static int executeStopSprint(ServerCommandSource source) {
        ServerTickManager serverTickManager = source.getServer().getTickManager();
        boolean bl = serverTickManager.stopSprinting();
        if (bl) {
            source.sendFeedback(() -> Text.translatable("commands.tick.sprint.stop.success"), true);
            return 1;
        }
        source.sendError(Text.translatable("commands.tick.sprint.stop.fail"));
        return 0;
    }
}


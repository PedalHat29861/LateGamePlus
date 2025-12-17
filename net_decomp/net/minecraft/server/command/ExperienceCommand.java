/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  com.mojang.brigadier.CommandDispatcher
 *  com.mojang.brigadier.Message
 *  com.mojang.brigadier.arguments.IntegerArgumentType
 *  com.mojang.brigadier.builder.LiteralArgumentBuilder
 *  com.mojang.brigadier.builder.RequiredArgumentBuilder
 *  com.mojang.brigadier.context.CommandContext
 *  com.mojang.brigadier.exceptions.CommandSyntaxException
 *  com.mojang.brigadier.exceptions.SimpleCommandExceptionType
 *  com.mojang.brigadier.tree.CommandNode
 *  com.mojang.brigadier.tree.LiteralCommandNode
 */
package net.minecraft.server.command;

import com.mojang.brigadier.CommandDispatcher;
import com.mojang.brigadier.Message;
import com.mojang.brigadier.arguments.IntegerArgumentType;
import com.mojang.brigadier.builder.LiteralArgumentBuilder;
import com.mojang.brigadier.builder.RequiredArgumentBuilder;
import com.mojang.brigadier.context.CommandContext;
import com.mojang.brigadier.exceptions.CommandSyntaxException;
import com.mojang.brigadier.exceptions.SimpleCommandExceptionType;
import com.mojang.brigadier.tree.CommandNode;
import com.mojang.brigadier.tree.LiteralCommandNode;
import java.util.Collection;
import java.util.function.BiConsumer;
import java.util.function.BiPredicate;
import java.util.function.ToIntFunction;
import net.minecraft.command.argument.EntityArgumentType;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.server.command.CommandManager;
import net.minecraft.server.command.ServerCommandSource;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.text.Text;
import net.minecraft.util.math.MathHelper;

public class ExperienceCommand {
    private static final SimpleCommandExceptionType SET_POINT_INVALID_EXCEPTION = new SimpleCommandExceptionType((Message)Text.translatable("commands.experience.set.points.invalid"));

    public static void register(CommandDispatcher<ServerCommandSource> dispatcher) {
        LiteralCommandNode literalCommandNode = dispatcher.register((LiteralArgumentBuilder)((LiteralArgumentBuilder)((LiteralArgumentBuilder)((LiteralArgumentBuilder)CommandManager.literal("experience").requires(CommandManager.requirePermissionLevel(CommandManager.GAMEMASTERS_CHECK))).then(CommandManager.literal("add").then(CommandManager.argument("target", EntityArgumentType.players()).then(((RequiredArgumentBuilder)((RequiredArgumentBuilder)CommandManager.argument("amount", IntegerArgumentType.integer()).executes(context -> ExperienceCommand.executeAdd((ServerCommandSource)context.getSource(), EntityArgumentType.getPlayers((CommandContext<ServerCommandSource>)context, "target"), IntegerArgumentType.getInteger((CommandContext)context, (String)"amount"), Component.POINTS))).then(CommandManager.literal("points").executes(context -> ExperienceCommand.executeAdd((ServerCommandSource)context.getSource(), EntityArgumentType.getPlayers((CommandContext<ServerCommandSource>)context, "target"), IntegerArgumentType.getInteger((CommandContext)context, (String)"amount"), Component.POINTS)))).then(CommandManager.literal("levels").executes(context -> ExperienceCommand.executeAdd((ServerCommandSource)context.getSource(), EntityArgumentType.getPlayers((CommandContext<ServerCommandSource>)context, "target"), IntegerArgumentType.getInteger((CommandContext)context, (String)"amount"), Component.LEVELS))))))).then(CommandManager.literal("set").then(CommandManager.argument("target", EntityArgumentType.players()).then(((RequiredArgumentBuilder)((RequiredArgumentBuilder)CommandManager.argument("amount", IntegerArgumentType.integer((int)0)).executes(context -> ExperienceCommand.executeSet((ServerCommandSource)context.getSource(), EntityArgumentType.getPlayers((CommandContext<ServerCommandSource>)context, "target"), IntegerArgumentType.getInteger((CommandContext)context, (String)"amount"), Component.POINTS))).then(CommandManager.literal("points").executes(context -> ExperienceCommand.executeSet((ServerCommandSource)context.getSource(), EntityArgumentType.getPlayers((CommandContext<ServerCommandSource>)context, "target"), IntegerArgumentType.getInteger((CommandContext)context, (String)"amount"), Component.POINTS)))).then(CommandManager.literal("levels").executes(context -> ExperienceCommand.executeSet((ServerCommandSource)context.getSource(), EntityArgumentType.getPlayers((CommandContext<ServerCommandSource>)context, "target"), IntegerArgumentType.getInteger((CommandContext)context, (String)"amount"), Component.LEVELS))))))).then(CommandManager.literal("query").then(((RequiredArgumentBuilder)CommandManager.argument("target", EntityArgumentType.player()).then(CommandManager.literal("points").executes(context -> ExperienceCommand.executeQuery((ServerCommandSource)context.getSource(), EntityArgumentType.getPlayer((CommandContext<ServerCommandSource>)context, "target"), Component.POINTS)))).then(CommandManager.literal("levels").executes(context -> ExperienceCommand.executeQuery((ServerCommandSource)context.getSource(), EntityArgumentType.getPlayer((CommandContext<ServerCommandSource>)context, "target"), Component.LEVELS))))));
        dispatcher.register((LiteralArgumentBuilder)((LiteralArgumentBuilder)CommandManager.literal("xp").requires(CommandManager.requirePermissionLevel(CommandManager.GAMEMASTERS_CHECK))).redirect((CommandNode)literalCommandNode));
    }

    private static int executeQuery(ServerCommandSource source, ServerPlayerEntity player, Component component) {
        int i = component.getter.applyAsInt(player);
        source.sendFeedback(() -> Text.translatable("commands.experience.query." + component.name, player.getDisplayName(), i), false);
        return i;
    }

    private static int executeAdd(ServerCommandSource source, Collection<? extends ServerPlayerEntity> targets, int amount, Component component) {
        for (ServerPlayerEntity serverPlayerEntity : targets) {
            component.adder.accept(serverPlayerEntity, amount);
        }
        if (targets.size() == 1) {
            source.sendFeedback(() -> Text.translatable("commands.experience.add." + component.name + ".success.single", amount, ((ServerPlayerEntity)targets.iterator().next()).getDisplayName()), true);
        } else {
            source.sendFeedback(() -> Text.translatable("commands.experience.add." + component.name + ".success.multiple", amount, targets.size()), true);
        }
        return targets.size();
    }

    private static int executeSet(ServerCommandSource source, Collection<? extends ServerPlayerEntity> targets, int amount, Component component) throws CommandSyntaxException {
        int i = 0;
        for (ServerPlayerEntity serverPlayerEntity : targets) {
            if (!component.setter.test(serverPlayerEntity, amount)) continue;
            ++i;
        }
        if (i == 0) {
            throw SET_POINT_INVALID_EXCEPTION.create();
        }
        if (targets.size() == 1) {
            source.sendFeedback(() -> Text.translatable("commands.experience.set." + component.name + ".success.single", amount, ((ServerPlayerEntity)targets.iterator().next()).getDisplayName()), true);
        } else {
            source.sendFeedback(() -> Text.translatable("commands.experience.set." + component.name + ".success.multiple", amount, targets.size()), true);
        }
        return targets.size();
    }

    static final class Component
    extends Enum<Component> {
        public static final /* enum */ Component POINTS = new Component("points", PlayerEntity::addExperience, (player, experience) -> {
            if (experience >= player.getNextLevelExperience()) {
                return false;
            }
            player.setExperiencePoints((int)experience);
            return true;
        }, player -> MathHelper.floor(player.experienceProgress * (float)player.getNextLevelExperience()));
        public static final /* enum */ Component LEVELS = new Component("levels", ServerPlayerEntity::addExperienceLevels, (player, level) -> {
            player.setExperienceLevel((int)level);
            return true;
        }, player -> player.experienceLevel);
        public final BiConsumer<ServerPlayerEntity, Integer> adder;
        public final BiPredicate<ServerPlayerEntity, Integer> setter;
        public final String name;
        final ToIntFunction<ServerPlayerEntity> getter;
        private static final /* synthetic */ Component[] field_13640;

        public static Component[] values() {
            return (Component[])field_13640.clone();
        }

        public static Component valueOf(String string) {
            return Enum.valueOf(Component.class, string);
        }

        private Component(String name, BiConsumer<ServerPlayerEntity, Integer> adder, BiPredicate<ServerPlayerEntity, Integer> setter, ToIntFunction<ServerPlayerEntity> getter) {
            this.adder = adder;
            this.name = name;
            this.setter = setter;
            this.getter = getter;
        }

        private static /* synthetic */ Component[] method_36967() {
            return new Component[]{POINTS, LEVELS};
        }

        static {
            field_13640 = Component.method_36967();
        }
    }
}


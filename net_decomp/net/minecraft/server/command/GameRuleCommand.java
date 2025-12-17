/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  com.mojang.brigadier.CommandDispatcher
 *  com.mojang.brigadier.builder.LiteralArgumentBuilder
 *  com.mojang.brigadier.context.CommandContext
 */
package net.minecraft.server.command;

import com.mojang.brigadier.CommandDispatcher;
import com.mojang.brigadier.builder.LiteralArgumentBuilder;
import com.mojang.brigadier.context.CommandContext;
import net.minecraft.command.CommandRegistryAccess;
import net.minecraft.server.command.CommandManager;
import net.minecraft.server.command.ServerCommandSource;
import net.minecraft.text.Text;
import net.minecraft.world.rule.GameRule;
import net.minecraft.world.rule.GameRuleVisitor;
import net.minecraft.world.rule.GameRules;

public class GameRuleCommand {
    public static void register(CommandDispatcher<ServerCommandSource> dispatcher, CommandRegistryAccess commandRegistryAccess) {
        final LiteralArgumentBuilder literalArgumentBuilder = (LiteralArgumentBuilder)CommandManager.literal("gamerule").requires(CommandManager.requirePermissionLevel(CommandManager.GAMEMASTERS_CHECK));
        new GameRules(commandRegistryAccess.getEnabledFeatures()).accept(new GameRuleVisitor(){

            @Override
            public <T> void visit(GameRule<T> rule) {
                LiteralArgumentBuilder<ServerCommandSource> literalArgumentBuilder3 = CommandManager.literal(rule.toShortString());
                LiteralArgumentBuilder<ServerCommandSource> literalArgumentBuilder2 = CommandManager.literal(rule.getId().toString());
                ((LiteralArgumentBuilder)literalArgumentBuilder.then(GameRuleCommand.appendRule(rule, literalArgumentBuilder3))).then(GameRuleCommand.appendRule(rule, literalArgumentBuilder2));
            }
        });
        dispatcher.register(literalArgumentBuilder);
    }

    static <T> LiteralArgumentBuilder<ServerCommandSource> appendRule(GameRule<T> rule, LiteralArgumentBuilder<ServerCommandSource> builder) {
        return (LiteralArgumentBuilder)((LiteralArgumentBuilder)builder.executes(context -> GameRuleCommand.executeQuery((ServerCommandSource)context.getSource(), rule))).then(CommandManager.argument("value", rule.getArgumentType()).executes(context -> GameRuleCommand.executeSet((CommandContext<ServerCommandSource>)context, rule)));
    }

    private static <T> int executeSet(CommandContext<ServerCommandSource> context, GameRule<T> key) {
        ServerCommandSource serverCommandSource = (ServerCommandSource)context.getSource();
        Object object = context.getArgument("value", key.getValueClass());
        serverCommandSource.getWorld().getGameRules().setValue(key, object, ((ServerCommandSource)context.getSource()).getServer());
        serverCommandSource.sendFeedback(() -> Text.translatable("commands.gamerule.set", key.toShortString(), key.getValueName(object)), true);
        return key.getCommandResult(object);
    }

    private static <T> int executeQuery(ServerCommandSource source, GameRule<T> key) {
        Object object = source.getWorld().getGameRules().getValue(key);
        source.sendFeedback(() -> Text.translatable("commands.gamerule.query", key.toShortString(), key.getValueName(object)), false);
        return key.getCommandResult(object);
    }
}


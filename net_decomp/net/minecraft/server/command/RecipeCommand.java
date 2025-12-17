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
 *  com.mojang.brigadier.exceptions.SimpleCommandExceptionType
 */
package net.minecraft.server.command;

import com.mojang.brigadier.CommandDispatcher;
import com.mojang.brigadier.Message;
import com.mojang.brigadier.builder.LiteralArgumentBuilder;
import com.mojang.brigadier.builder.RequiredArgumentBuilder;
import com.mojang.brigadier.context.CommandContext;
import com.mojang.brigadier.exceptions.CommandSyntaxException;
import com.mojang.brigadier.exceptions.SimpleCommandExceptionType;
import java.util.Collection;
import java.util.Collections;
import net.minecraft.command.argument.EntityArgumentType;
import net.minecraft.command.argument.RegistryKeyArgumentType;
import net.minecraft.recipe.RecipeEntry;
import net.minecraft.registry.RegistryKeys;
import net.minecraft.server.command.CommandManager;
import net.minecraft.server.command.ServerCommandSource;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.text.Text;

public class RecipeCommand {
    private static final SimpleCommandExceptionType GIVE_FAILED_EXCEPTION = new SimpleCommandExceptionType((Message)Text.translatable("commands.recipe.give.failed"));
    private static final SimpleCommandExceptionType TAKE_FAILED_EXCEPTION = new SimpleCommandExceptionType((Message)Text.translatable("commands.recipe.take.failed"));

    public static void register(CommandDispatcher<ServerCommandSource> dispatcher) {
        dispatcher.register((LiteralArgumentBuilder)((LiteralArgumentBuilder)((LiteralArgumentBuilder)CommandManager.literal("recipe").requires(CommandManager.requirePermissionLevel(CommandManager.GAMEMASTERS_CHECK))).then(CommandManager.literal("give").then(((RequiredArgumentBuilder)CommandManager.argument("targets", EntityArgumentType.players()).then(CommandManager.argument("recipe", RegistryKeyArgumentType.registryKey(RegistryKeys.RECIPE)).executes(context -> RecipeCommand.executeGive((ServerCommandSource)context.getSource(), EntityArgumentType.getPlayers((CommandContext<ServerCommandSource>)context, "targets"), Collections.singleton(RegistryKeyArgumentType.getRecipeEntry((CommandContext<ServerCommandSource>)context, "recipe")))))).then(CommandManager.literal("*").executes(context -> RecipeCommand.executeGive((ServerCommandSource)context.getSource(), EntityArgumentType.getPlayers((CommandContext<ServerCommandSource>)context, "targets"), ((ServerCommandSource)context.getSource()).getServer().getRecipeManager().values())))))).then(CommandManager.literal("take").then(((RequiredArgumentBuilder)CommandManager.argument("targets", EntityArgumentType.players()).then(CommandManager.argument("recipe", RegistryKeyArgumentType.registryKey(RegistryKeys.RECIPE)).executes(context -> RecipeCommand.executeTake((ServerCommandSource)context.getSource(), EntityArgumentType.getPlayers((CommandContext<ServerCommandSource>)context, "targets"), Collections.singleton(RegistryKeyArgumentType.getRecipeEntry((CommandContext<ServerCommandSource>)context, "recipe")))))).then(CommandManager.literal("*").executes(context -> RecipeCommand.executeTake((ServerCommandSource)context.getSource(), EntityArgumentType.getPlayers((CommandContext<ServerCommandSource>)context, "targets"), ((ServerCommandSource)context.getSource()).getServer().getRecipeManager().values()))))));
    }

    private static int executeGive(ServerCommandSource source, Collection<ServerPlayerEntity> targets, Collection<RecipeEntry<?>> recipes) throws CommandSyntaxException {
        int i = 0;
        for (ServerPlayerEntity serverPlayerEntity : targets) {
            i += serverPlayerEntity.unlockRecipes(recipes);
        }
        if (i == 0) {
            throw GIVE_FAILED_EXCEPTION.create();
        }
        if (targets.size() == 1) {
            source.sendFeedback(() -> Text.translatable("commands.recipe.give.success.single", recipes.size(), ((ServerPlayerEntity)targets.iterator().next()).getDisplayName()), true);
        } else {
            source.sendFeedback(() -> Text.translatable("commands.recipe.give.success.multiple", recipes.size(), targets.size()), true);
        }
        return i;
    }

    private static int executeTake(ServerCommandSource source, Collection<ServerPlayerEntity> targets, Collection<RecipeEntry<?>> recipes) throws CommandSyntaxException {
        int i = 0;
        for (ServerPlayerEntity serverPlayerEntity : targets) {
            i += serverPlayerEntity.lockRecipes(recipes);
        }
        if (i == 0) {
            throw TAKE_FAILED_EXCEPTION.create();
        }
        if (targets.size() == 1) {
            source.sendFeedback(() -> Text.translatable("commands.recipe.take.success.single", recipes.size(), ((ServerPlayerEntity)targets.iterator().next()).getDisplayName()), true);
        } else {
            source.sendFeedback(() -> Text.translatable("commands.recipe.take.success.multiple", recipes.size(), targets.size()), true);
        }
        return i;
    }
}


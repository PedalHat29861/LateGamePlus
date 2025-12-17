/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  com.google.common.collect.ImmutableList
 *  com.mojang.brigadier.CommandDispatcher
 *  com.mojang.brigadier.Message
 *  com.mojang.brigadier.arguments.BoolArgumentType
 *  com.mojang.brigadier.arguments.IntegerArgumentType
 *  com.mojang.brigadier.builder.LiteralArgumentBuilder
 *  com.mojang.brigadier.builder.RequiredArgumentBuilder
 *  com.mojang.brigadier.context.CommandContext
 *  com.mojang.brigadier.exceptions.CommandSyntaxException
 *  com.mojang.brigadier.exceptions.SimpleCommandExceptionType
 *  org.jspecify.annotations.Nullable
 */
package net.minecraft.server.command;

import com.google.common.collect.ImmutableList;
import com.mojang.brigadier.CommandDispatcher;
import com.mojang.brigadier.Message;
import com.mojang.brigadier.arguments.BoolArgumentType;
import com.mojang.brigadier.arguments.IntegerArgumentType;
import com.mojang.brigadier.builder.LiteralArgumentBuilder;
import com.mojang.brigadier.builder.RequiredArgumentBuilder;
import com.mojang.brigadier.context.CommandContext;
import com.mojang.brigadier.exceptions.CommandSyntaxException;
import com.mojang.brigadier.exceptions.SimpleCommandExceptionType;
import java.util.Collection;
import net.minecraft.command.CommandRegistryAccess;
import net.minecraft.command.argument.EntityArgumentType;
import net.minecraft.command.argument.RegistryEntryReferenceArgumentType;
import net.minecraft.entity.Entity;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.effect.StatusEffect;
import net.minecraft.entity.effect.StatusEffectInstance;
import net.minecraft.registry.RegistryKeys;
import net.minecraft.registry.entry.RegistryEntry;
import net.minecraft.server.command.CommandManager;
import net.minecraft.server.command.ServerCommandSource;
import net.minecraft.text.Text;
import org.jspecify.annotations.Nullable;

public class EffectCommand {
    private static final SimpleCommandExceptionType GIVE_FAILED_EXCEPTION = new SimpleCommandExceptionType((Message)Text.translatable("commands.effect.give.failed"));
    private static final SimpleCommandExceptionType CLEAR_EVERYTHING_FAILED_EXCEPTION = new SimpleCommandExceptionType((Message)Text.translatable("commands.effect.clear.everything.failed"));
    private static final SimpleCommandExceptionType CLEAR_SPECIFIC_FAILED_EXCEPTION = new SimpleCommandExceptionType((Message)Text.translatable("commands.effect.clear.specific.failed"));

    public static void register(CommandDispatcher<ServerCommandSource> dispatcher, CommandRegistryAccess registryAccess) {
        dispatcher.register((LiteralArgumentBuilder)((LiteralArgumentBuilder)((LiteralArgumentBuilder)CommandManager.literal("effect").requires(CommandManager.requirePermissionLevel(CommandManager.GAMEMASTERS_CHECK))).then(((LiteralArgumentBuilder)CommandManager.literal("clear").executes(context -> EffectCommand.executeClear((ServerCommandSource)context.getSource(), (Collection<? extends Entity>)ImmutableList.of((Object)((ServerCommandSource)context.getSource()).getEntityOrThrow())))).then(((RequiredArgumentBuilder)CommandManager.argument("targets", EntityArgumentType.entities()).executes(context -> EffectCommand.executeClear((ServerCommandSource)context.getSource(), EntityArgumentType.getEntities((CommandContext<ServerCommandSource>)context, "targets")))).then(CommandManager.argument("effect", RegistryEntryReferenceArgumentType.registryEntry(registryAccess, RegistryKeys.STATUS_EFFECT)).executes(context -> EffectCommand.executeClear((ServerCommandSource)context.getSource(), EntityArgumentType.getEntities((CommandContext<ServerCommandSource>)context, "targets"), RegistryEntryReferenceArgumentType.getStatusEffect((CommandContext<ServerCommandSource>)context, "effect"))))))).then(CommandManager.literal("give").then(CommandManager.argument("targets", EntityArgumentType.entities()).then(((RequiredArgumentBuilder)((RequiredArgumentBuilder)CommandManager.argument("effect", RegistryEntryReferenceArgumentType.registryEntry(registryAccess, RegistryKeys.STATUS_EFFECT)).executes(context -> EffectCommand.executeGive((ServerCommandSource)context.getSource(), EntityArgumentType.getEntities((CommandContext<ServerCommandSource>)context, "targets"), RegistryEntryReferenceArgumentType.getStatusEffect((CommandContext<ServerCommandSource>)context, "effect"), null, 0, true))).then(((RequiredArgumentBuilder)CommandManager.argument("seconds", IntegerArgumentType.integer((int)1, (int)1000000)).executes(context -> EffectCommand.executeGive((ServerCommandSource)context.getSource(), EntityArgumentType.getEntities((CommandContext<ServerCommandSource>)context, "targets"), RegistryEntryReferenceArgumentType.getStatusEffect((CommandContext<ServerCommandSource>)context, "effect"), IntegerArgumentType.getInteger((CommandContext)context, (String)"seconds"), 0, true))).then(((RequiredArgumentBuilder)CommandManager.argument("amplifier", IntegerArgumentType.integer((int)0, (int)255)).executes(context -> EffectCommand.executeGive((ServerCommandSource)context.getSource(), EntityArgumentType.getEntities((CommandContext<ServerCommandSource>)context, "targets"), RegistryEntryReferenceArgumentType.getStatusEffect((CommandContext<ServerCommandSource>)context, "effect"), IntegerArgumentType.getInteger((CommandContext)context, (String)"seconds"), IntegerArgumentType.getInteger((CommandContext)context, (String)"amplifier"), true))).then(CommandManager.argument("hideParticles", BoolArgumentType.bool()).executes(context -> EffectCommand.executeGive((ServerCommandSource)context.getSource(), EntityArgumentType.getEntities((CommandContext<ServerCommandSource>)context, "targets"), RegistryEntryReferenceArgumentType.getStatusEffect((CommandContext<ServerCommandSource>)context, "effect"), IntegerArgumentType.getInteger((CommandContext)context, (String)"seconds"), IntegerArgumentType.getInteger((CommandContext)context, (String)"amplifier"), !BoolArgumentType.getBool((CommandContext)context, (String)"hideParticles"))))))).then(((LiteralArgumentBuilder)CommandManager.literal("infinite").executes(context -> EffectCommand.executeGive((ServerCommandSource)context.getSource(), EntityArgumentType.getEntities((CommandContext<ServerCommandSource>)context, "targets"), RegistryEntryReferenceArgumentType.getStatusEffect((CommandContext<ServerCommandSource>)context, "effect"), -1, 0, true))).then(((RequiredArgumentBuilder)CommandManager.argument("amplifier", IntegerArgumentType.integer((int)0, (int)255)).executes(context -> EffectCommand.executeGive((ServerCommandSource)context.getSource(), EntityArgumentType.getEntities((CommandContext<ServerCommandSource>)context, "targets"), RegistryEntryReferenceArgumentType.getStatusEffect((CommandContext<ServerCommandSource>)context, "effect"), -1, IntegerArgumentType.getInteger((CommandContext)context, (String)"amplifier"), true))).then(CommandManager.argument("hideParticles", BoolArgumentType.bool()).executes(context -> EffectCommand.executeGive((ServerCommandSource)context.getSource(), EntityArgumentType.getEntities((CommandContext<ServerCommandSource>)context, "targets"), RegistryEntryReferenceArgumentType.getStatusEffect((CommandContext<ServerCommandSource>)context, "effect"), -1, IntegerArgumentType.getInteger((CommandContext)context, (String)"amplifier"), !BoolArgumentType.getBool((CommandContext)context, (String)"hideParticles"))))))))));
    }

    private static int executeGive(ServerCommandSource source, Collection<? extends Entity> targets, RegistryEntry<StatusEffect> statusEffect, @Nullable Integer seconds, int amplifier, boolean showParticles) throws CommandSyntaxException {
        StatusEffect statusEffect2 = statusEffect.value();
        int i = 0;
        int j = seconds != null ? (statusEffect2.isInstant() ? seconds : (seconds == -1 ? -1 : seconds * 20)) : (statusEffect2.isInstant() ? 1 : 600);
        for (Entity entity : targets) {
            StatusEffectInstance statusEffectInstance;
            if (!(entity instanceof LivingEntity) || !((LivingEntity)entity).addStatusEffect(statusEffectInstance = new StatusEffectInstance(statusEffect, j, amplifier, false, showParticles), source.getEntity())) continue;
            ++i;
        }
        if (i == 0) {
            throw GIVE_FAILED_EXCEPTION.create();
        }
        if (targets.size() == 1) {
            source.sendFeedback(() -> Text.translatable("commands.effect.give.success.single", statusEffect2.getName(), ((Entity)targets.iterator().next()).getDisplayName(), j / 20), true);
        } else {
            source.sendFeedback(() -> Text.translatable("commands.effect.give.success.multiple", statusEffect2.getName(), targets.size(), j / 20), true);
        }
        return i;
    }

    private static int executeClear(ServerCommandSource source, Collection<? extends Entity> targets) throws CommandSyntaxException {
        int i = 0;
        for (Entity entity : targets) {
            if (!(entity instanceof LivingEntity) || !((LivingEntity)entity).clearStatusEffects()) continue;
            ++i;
        }
        if (i == 0) {
            throw CLEAR_EVERYTHING_FAILED_EXCEPTION.create();
        }
        if (targets.size() == 1) {
            source.sendFeedback(() -> Text.translatable("commands.effect.clear.everything.success.single", ((Entity)targets.iterator().next()).getDisplayName()), true);
        } else {
            source.sendFeedback(() -> Text.translatable("commands.effect.clear.everything.success.multiple", targets.size()), true);
        }
        return i;
    }

    private static int executeClear(ServerCommandSource source, Collection<? extends Entity> targets, RegistryEntry<StatusEffect> statusEffect) throws CommandSyntaxException {
        StatusEffect statusEffect2 = statusEffect.value();
        int i = 0;
        for (Entity entity : targets) {
            if (!(entity instanceof LivingEntity) || !((LivingEntity)entity).removeStatusEffect(statusEffect)) continue;
            ++i;
        }
        if (i == 0) {
            throw CLEAR_SPECIFIC_FAILED_EXCEPTION.create();
        }
        if (targets.size() == 1) {
            source.sendFeedback(() -> Text.translatable("commands.effect.clear.specific.success.single", statusEffect2.getName(), ((Entity)targets.iterator().next()).getDisplayName()), true);
        } else {
            source.sendFeedback(() -> Text.translatable("commands.effect.clear.specific.success.multiple", statusEffect2.getName(), targets.size()), true);
        }
        return i;
    }
}


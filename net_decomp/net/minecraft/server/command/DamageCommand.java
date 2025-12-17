/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  com.mojang.brigadier.CommandDispatcher
 *  com.mojang.brigadier.Message
 *  com.mojang.brigadier.arguments.FloatArgumentType
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
import com.mojang.brigadier.builder.LiteralArgumentBuilder;
import com.mojang.brigadier.builder.RequiredArgumentBuilder;
import com.mojang.brigadier.context.CommandContext;
import com.mojang.brigadier.exceptions.CommandSyntaxException;
import com.mojang.brigadier.exceptions.SimpleCommandExceptionType;
import net.minecraft.command.CommandRegistryAccess;
import net.minecraft.command.argument.EntityArgumentType;
import net.minecraft.command.argument.RegistryEntryReferenceArgumentType;
import net.minecraft.command.argument.Vec3ArgumentType;
import net.minecraft.entity.Entity;
import net.minecraft.entity.damage.DamageSource;
import net.minecraft.registry.RegistryKeys;
import net.minecraft.server.command.CommandManager;
import net.minecraft.server.command.ServerCommandSource;
import net.minecraft.text.Text;

public class DamageCommand {
    private static final SimpleCommandExceptionType INVULNERABLE_EXCEPTION = new SimpleCommandExceptionType((Message)Text.translatable("commands.damage.invulnerable"));

    public static void register(CommandDispatcher<ServerCommandSource> dispatcher, CommandRegistryAccess registryAccess) {
        dispatcher.register((LiteralArgumentBuilder)((LiteralArgumentBuilder)CommandManager.literal("damage").requires(CommandManager.requirePermissionLevel(CommandManager.GAMEMASTERS_CHECK))).then(CommandManager.argument("target", EntityArgumentType.entity()).then(((RequiredArgumentBuilder)CommandManager.argument("amount", FloatArgumentType.floatArg((float)0.0f)).executes(context -> DamageCommand.execute((ServerCommandSource)context.getSource(), EntityArgumentType.getEntity((CommandContext<ServerCommandSource>)context, "target"), FloatArgumentType.getFloat((CommandContext)context, (String)"amount"), ((ServerCommandSource)context.getSource()).getWorld().getDamageSources().generic()))).then(((RequiredArgumentBuilder)((RequiredArgumentBuilder)CommandManager.argument("damageType", RegistryEntryReferenceArgumentType.registryEntry(registryAccess, RegistryKeys.DAMAGE_TYPE)).executes(context -> DamageCommand.execute((ServerCommandSource)context.getSource(), EntityArgumentType.getEntity((CommandContext<ServerCommandSource>)context, "target"), FloatArgumentType.getFloat((CommandContext)context, (String)"amount"), new DamageSource(RegistryEntryReferenceArgumentType.getRegistryEntry((CommandContext<ServerCommandSource>)context, "damageType", RegistryKeys.DAMAGE_TYPE))))).then(CommandManager.literal("at").then(CommandManager.argument("location", Vec3ArgumentType.vec3()).executes(context -> DamageCommand.execute((ServerCommandSource)context.getSource(), EntityArgumentType.getEntity((CommandContext<ServerCommandSource>)context, "target"), FloatArgumentType.getFloat((CommandContext)context, (String)"amount"), new DamageSource(RegistryEntryReferenceArgumentType.getRegistryEntry((CommandContext<ServerCommandSource>)context, "damageType", RegistryKeys.DAMAGE_TYPE), Vec3ArgumentType.getVec3((CommandContext<ServerCommandSource>)context, "location"))))))).then(CommandManager.literal("by").then(((RequiredArgumentBuilder)CommandManager.argument("entity", EntityArgumentType.entity()).executes(context -> DamageCommand.execute((ServerCommandSource)context.getSource(), EntityArgumentType.getEntity((CommandContext<ServerCommandSource>)context, "target"), FloatArgumentType.getFloat((CommandContext)context, (String)"amount"), new DamageSource(RegistryEntryReferenceArgumentType.getRegistryEntry((CommandContext<ServerCommandSource>)context, "damageType", RegistryKeys.DAMAGE_TYPE), EntityArgumentType.getEntity((CommandContext<ServerCommandSource>)context, "entity"))))).then(CommandManager.literal("from").then(CommandManager.argument("cause", EntityArgumentType.entity()).executes(context -> DamageCommand.execute((ServerCommandSource)context.getSource(), EntityArgumentType.getEntity((CommandContext<ServerCommandSource>)context, "target"), FloatArgumentType.getFloat((CommandContext)context, (String)"amount"), new DamageSource(RegistryEntryReferenceArgumentType.getRegistryEntry((CommandContext<ServerCommandSource>)context, "damageType", RegistryKeys.DAMAGE_TYPE), EntityArgumentType.getEntity((CommandContext<ServerCommandSource>)context, "entity"), EntityArgumentType.getEntity((CommandContext<ServerCommandSource>)context, "cause"))))))))))));
    }

    private static int execute(ServerCommandSource source, Entity target, float amount, DamageSource damageSource) throws CommandSyntaxException {
        if (target.damage(source.getWorld(), damageSource, amount)) {
            source.sendFeedback(() -> Text.translatable("commands.damage.success", Float.valueOf(amount), target.getDisplayName()), true);
            return 1;
        }
        throw INVULNERABLE_EXCEPTION.create();
    }
}


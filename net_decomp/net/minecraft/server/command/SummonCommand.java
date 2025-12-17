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
import net.minecraft.command.CommandRegistryAccess;
import net.minecraft.command.argument.NbtCompoundArgumentType;
import net.minecraft.command.argument.RegistryEntryReferenceArgumentType;
import net.minecraft.command.argument.Vec3ArgumentType;
import net.minecraft.command.suggestion.SuggestionProviders;
import net.minecraft.entity.Entity;
import net.minecraft.entity.EntityType;
import net.minecraft.entity.SpawnReason;
import net.minecraft.entity.mob.MobEntity;
import net.minecraft.nbt.NbtCompound;
import net.minecraft.registry.RegistryKeys;
import net.minecraft.registry.entry.RegistryEntry;
import net.minecraft.server.command.CommandManager;
import net.minecraft.server.command.ServerCommandSource;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.text.Text;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Vec3d;
import net.minecraft.world.Difficulty;
import net.minecraft.world.World;

public class SummonCommand {
    private static final SimpleCommandExceptionType FAILED_EXCEPTION = new SimpleCommandExceptionType((Message)Text.translatable("commands.summon.failed"));
    private static final SimpleCommandExceptionType FAILED_PEACEFUL_EXCEPTION = new SimpleCommandExceptionType((Message)Text.translatable("commands.summon.failed.peaceful"));
    private static final SimpleCommandExceptionType FAILED_UUID_EXCEPTION = new SimpleCommandExceptionType((Message)Text.translatable("commands.summon.failed.uuid"));
    private static final SimpleCommandExceptionType INVALID_POSITION_EXCEPTION = new SimpleCommandExceptionType((Message)Text.translatable("commands.summon.invalidPosition"));

    public static void register(CommandDispatcher<ServerCommandSource> dispatcher, CommandRegistryAccess registryAccess) {
        dispatcher.register((LiteralArgumentBuilder)((LiteralArgumentBuilder)CommandManager.literal("summon").requires(CommandManager.requirePermissionLevel(CommandManager.GAMEMASTERS_CHECK))).then(((RequiredArgumentBuilder)CommandManager.argument("entity", RegistryEntryReferenceArgumentType.registryEntry(registryAccess, RegistryKeys.ENTITY_TYPE)).suggests(SuggestionProviders.cast(SuggestionProviders.SUMMONABLE_ENTITIES)).executes(context -> SummonCommand.execute((ServerCommandSource)context.getSource(), RegistryEntryReferenceArgumentType.getSummonableEntityType((CommandContext<ServerCommandSource>)context, "entity"), ((ServerCommandSource)context.getSource()).getPosition(), new NbtCompound(), true))).then(((RequiredArgumentBuilder)CommandManager.argument("pos", Vec3ArgumentType.vec3()).executes(context -> SummonCommand.execute((ServerCommandSource)context.getSource(), RegistryEntryReferenceArgumentType.getSummonableEntityType((CommandContext<ServerCommandSource>)context, "entity"), Vec3ArgumentType.getVec3((CommandContext<ServerCommandSource>)context, "pos"), new NbtCompound(), true))).then(CommandManager.argument("nbt", NbtCompoundArgumentType.nbtCompound()).executes(context -> SummonCommand.execute((ServerCommandSource)context.getSource(), RegistryEntryReferenceArgumentType.getSummonableEntityType((CommandContext<ServerCommandSource>)context, "entity"), Vec3ArgumentType.getVec3((CommandContext<ServerCommandSource>)context, "pos"), NbtCompoundArgumentType.getNbtCompound(context, "nbt"), false))))));
    }

    public static Entity summon(ServerCommandSource source, RegistryEntry.Reference<EntityType<?>> entityType, Vec3d pos, NbtCompound nbt, boolean initialize) throws CommandSyntaxException {
        BlockPos blockPos = BlockPos.ofFloored(pos);
        if (!World.isValid(blockPos)) {
            throw INVALID_POSITION_EXCEPTION.create();
        }
        if (source.getWorld().getDifficulty() == Difficulty.PEACEFUL && !entityType.value().isAllowedInPeaceful()) {
            throw FAILED_PEACEFUL_EXCEPTION.create();
        }
        NbtCompound nbtCompound = nbt.copy();
        nbtCompound.putString("id", entityType.registryKey().getValue().toString());
        ServerWorld serverWorld = source.getWorld();
        Entity entity2 = EntityType.loadEntityWithPassengers(nbtCompound, (World)serverWorld, SpawnReason.COMMAND, entity -> {
            entity.refreshPositionAndAngles(vec3d.x, vec3d.y, vec3d.z, entity.getYaw(), entity.getPitch());
            return entity;
        });
        if (entity2 == null) {
            throw FAILED_EXCEPTION.create();
        }
        if (initialize && entity2 instanceof MobEntity) {
            MobEntity mobEntity = (MobEntity)entity2;
            mobEntity.initialize(source.getWorld(), source.getWorld().getLocalDifficulty(entity2.getBlockPos()), SpawnReason.COMMAND, null);
        }
        if (!serverWorld.spawnNewEntityAndPassengers(entity2)) {
            throw FAILED_UUID_EXCEPTION.create();
        }
        return entity2;
    }

    private static int execute(ServerCommandSource source, RegistryEntry.Reference<EntityType<?>> entityType, Vec3d pos, NbtCompound nbt, boolean initialize) throws CommandSyntaxException {
        Entity entity = SummonCommand.summon(source, entityType, pos, nbt, initialize);
        source.sendFeedback(() -> Text.translatable("commands.summon.success", entity.getDisplayName()), true);
        return 1;
    }
}


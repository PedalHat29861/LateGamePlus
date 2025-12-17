/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  com.google.common.annotations.VisibleForTesting
 *  com.google.common.collect.Lists
 *  com.mojang.brigadier.Command
 *  com.mojang.brigadier.CommandDispatcher
 *  com.mojang.brigadier.Message
 *  com.mojang.brigadier.RedirectModifier
 *  com.mojang.brigadier.arguments.DoubleArgumentType
 *  com.mojang.brigadier.builder.ArgumentBuilder
 *  com.mojang.brigadier.builder.LiteralArgumentBuilder
 *  com.mojang.brigadier.builder.RequiredArgumentBuilder
 *  com.mojang.brigadier.context.CommandContext
 *  com.mojang.brigadier.context.ContextChain
 *  com.mojang.brigadier.exceptions.CommandSyntaxException
 *  com.mojang.brigadier.exceptions.Dynamic2CommandExceptionType
 *  com.mojang.brigadier.exceptions.DynamicCommandExceptionType
 *  com.mojang.brigadier.exceptions.SimpleCommandExceptionType
 *  com.mojang.brigadier.tree.CommandNode
 *  com.mojang.brigadier.tree.LiteralCommandNode
 *  com.mojang.logging.LogUtils
 *  it.unimi.dsi.fastutil.ints.IntList
 *  org.jspecify.annotations.Nullable
 *  org.slf4j.Logger
 */
package net.minecraft.server.command;

import com.google.common.annotations.VisibleForTesting;
import com.google.common.collect.Lists;
import com.mojang.brigadier.Command;
import com.mojang.brigadier.CommandDispatcher;
import com.mojang.brigadier.Message;
import com.mojang.brigadier.RedirectModifier;
import com.mojang.brigadier.arguments.DoubleArgumentType;
import com.mojang.brigadier.builder.ArgumentBuilder;
import com.mojang.brigadier.builder.LiteralArgumentBuilder;
import com.mojang.brigadier.builder.RequiredArgumentBuilder;
import com.mojang.brigadier.context.CommandContext;
import com.mojang.brigadier.context.ContextChain;
import com.mojang.brigadier.exceptions.CommandSyntaxException;
import com.mojang.brigadier.exceptions.Dynamic2CommandExceptionType;
import com.mojang.brigadier.exceptions.DynamicCommandExceptionType;
import com.mojang.brigadier.exceptions.SimpleCommandExceptionType;
import com.mojang.brigadier.tree.CommandNode;
import com.mojang.brigadier.tree.LiteralCommandNode;
import com.mojang.logging.LogUtils;
import it.unimi.dsi.fastutil.ints.IntList;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.List;
import java.util.Optional;
import java.util.OptionalInt;
import java.util.function.Function;
import java.util.function.IntFunction;
import java.util.function.IntPredicate;
import java.util.function.Predicate;
import java.util.stream.Stream;
import net.minecraft.block.BlockState;
import net.minecraft.block.Blocks;
import net.minecraft.block.entity.BlockEntity;
import net.minecraft.block.pattern.CachedBlockPosition;
import net.minecraft.command.ArgumentGetter;
import net.minecraft.command.CommandFunctionAction;
import net.minecraft.command.CommandRegistryAccess;
import net.minecraft.command.DataCommandObject;
import net.minecraft.command.ExecutionControl;
import net.minecraft.command.ExecutionFlags;
import net.minecraft.command.FallthroughCommandAction;
import net.minecraft.command.Forkable;
import net.minecraft.command.ReturnValueConsumer;
import net.minecraft.command.argument.BlockPosArgumentType;
import net.minecraft.command.argument.BlockPredicateArgumentType;
import net.minecraft.command.argument.CommandFunctionArgumentType;
import net.minecraft.command.argument.DimensionArgumentType;
import net.minecraft.command.argument.EntityAnchorArgumentType;
import net.minecraft.command.argument.EntityArgumentType;
import net.minecraft.command.argument.HeightmapArgumentType;
import net.minecraft.command.argument.IdentifierArgumentType;
import net.minecraft.command.argument.ItemPredicateArgumentType;
import net.minecraft.command.argument.NbtPathArgumentType;
import net.minecraft.command.argument.NumberRangeArgumentType;
import net.minecraft.command.argument.RegistryEntryArgumentType;
import net.minecraft.command.argument.RegistryEntryPredicateArgumentType;
import net.minecraft.command.argument.RegistryEntryReferenceArgumentType;
import net.minecraft.command.argument.RotationArgumentType;
import net.minecraft.command.argument.ScoreHolderArgumentType;
import net.minecraft.command.argument.ScoreboardObjectiveArgumentType;
import net.minecraft.command.argument.SlotRangeArgumentType;
import net.minecraft.command.argument.SwizzleArgumentType;
import net.minecraft.command.argument.Vec3ArgumentType;
import net.minecraft.command.suggestion.SuggestionProviders;
import net.minecraft.entity.Attackable;
import net.minecraft.entity.Entity;
import net.minecraft.entity.EntityType;
import net.minecraft.entity.Leashable;
import net.minecraft.entity.Ownable;
import net.minecraft.entity.Tameable;
import net.minecraft.entity.Targeter;
import net.minecraft.entity.boss.CommandBossBar;
import net.minecraft.inventory.Inventory;
import net.minecraft.inventory.SlotRange;
import net.minecraft.inventory.StackReference;
import net.minecraft.inventory.StackReferenceGetter;
import net.minecraft.item.ItemStack;
import net.minecraft.loot.condition.LootCondition;
import net.minecraft.loot.context.LootContext;
import net.minecraft.loot.context.LootContextParameters;
import net.minecraft.loot.context.LootContextTypes;
import net.minecraft.loot.context.LootWorldContext;
import net.minecraft.nbt.NbtByte;
import net.minecraft.nbt.NbtCompound;
import net.minecraft.nbt.NbtDouble;
import net.minecraft.nbt.NbtElement;
import net.minecraft.nbt.NbtFloat;
import net.minecraft.nbt.NbtInt;
import net.minecraft.nbt.NbtLong;
import net.minecraft.nbt.NbtShort;
import net.minecraft.predicate.NumberRange;
import net.minecraft.registry.DynamicRegistryManager;
import net.minecraft.registry.RegistryKeys;
import net.minecraft.registry.entry.RegistryEntry;
import net.minecraft.scoreboard.ReadableScoreboardScore;
import net.minecraft.scoreboard.ScoreAccess;
import net.minecraft.scoreboard.ScoreHolder;
import net.minecraft.scoreboard.ScoreboardObjective;
import net.minecraft.scoreboard.ServerScoreboard;
import net.minecraft.server.command.AbstractServerCommandSource;
import net.minecraft.server.command.BossBarCommand;
import net.minecraft.server.command.CommandManager;
import net.minecraft.server.command.DataCommand;
import net.minecraft.server.command.FunctionCommand;
import net.minecraft.server.command.ItemCommand;
import net.minecraft.server.command.ServerCommandSource;
import net.minecraft.server.command.StopwatchCommand;
import net.minecraft.server.command.SummonCommand;
import net.minecraft.server.function.CommandFunction;
import net.minecraft.server.function.Procedure;
import net.minecraft.server.world.ChunkLevelType;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.storage.NbtWriteView;
import net.minecraft.text.Text;
import net.minecraft.util.ErrorReporter;
import net.minecraft.util.Identifier;
import net.minecraft.util.math.BlockBox;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.ChunkPos;
import net.minecraft.util.math.ChunkSectionPos;
import net.minecraft.util.math.MathHelper;
import net.minecraft.util.math.Vec3d;
import net.minecraft.world.chunk.WorldChunk;
import net.minecraft.world.timer.stopwatch.Stopwatch;
import net.minecraft.world.timer.stopwatch.StopwatchPersistentState;
import org.jspecify.annotations.Nullable;
import org.slf4j.Logger;

public class ExecuteCommand {
    private static final Logger LOGGER = LogUtils.getLogger();
    private static final int MAX_BLOCKS = 32768;
    private static final Dynamic2CommandExceptionType BLOCKS_TOOBIG_EXCEPTION = new Dynamic2CommandExceptionType((maxCount, count) -> Text.stringifiedTranslatable("commands.execute.blocks.toobig", maxCount, count));
    private static final SimpleCommandExceptionType CONDITIONAL_FAIL_EXCEPTION = new SimpleCommandExceptionType((Message)Text.translatable("commands.execute.conditional.fail"));
    private static final DynamicCommandExceptionType CONDITIONAL_FAIL_COUNT_EXCEPTION = new DynamicCommandExceptionType(count -> Text.stringifiedTranslatable("commands.execute.conditional.fail_count", count));
    @VisibleForTesting
    public static final Dynamic2CommandExceptionType INSTANTIATION_FAILURE_EXCEPTION = new Dynamic2CommandExceptionType((function, message) -> Text.stringifiedTranslatable("commands.execute.function.instantiationFailure", function, message));

    public static void register(CommandDispatcher<ServerCommandSource> dispatcher, CommandRegistryAccess commandRegistryAccess) {
        LiteralCommandNode literalCommandNode = dispatcher.register((LiteralArgumentBuilder)CommandManager.literal("execute").requires(CommandManager.requirePermissionLevel(CommandManager.GAMEMASTERS_CHECK)));
        dispatcher.register((LiteralArgumentBuilder)((LiteralArgumentBuilder)((LiteralArgumentBuilder)((LiteralArgumentBuilder)((LiteralArgumentBuilder)((LiteralArgumentBuilder)((LiteralArgumentBuilder)((LiteralArgumentBuilder)((LiteralArgumentBuilder)((LiteralArgumentBuilder)((LiteralArgumentBuilder)((LiteralArgumentBuilder)((LiteralArgumentBuilder)((LiteralArgumentBuilder)((LiteralArgumentBuilder)CommandManager.literal("execute").requires(CommandManager.requirePermissionLevel(CommandManager.GAMEMASTERS_CHECK))).then(CommandManager.literal("run").redirect((CommandNode)dispatcher.getRoot()))).then(ExecuteCommand.addConditionArguments((CommandNode<ServerCommandSource>)literalCommandNode, CommandManager.literal("if"), true, commandRegistryAccess))).then(ExecuteCommand.addConditionArguments((CommandNode<ServerCommandSource>)literalCommandNode, CommandManager.literal("unless"), false, commandRegistryAccess))).then(CommandManager.literal("as").then(CommandManager.argument("targets", EntityArgumentType.entities()).fork((CommandNode)literalCommandNode, context -> {
            ArrayList list = Lists.newArrayList();
            for (Entity entity : EntityArgumentType.getOptionalEntities((CommandContext<ServerCommandSource>)context, "targets")) {
                list.add(((ServerCommandSource)context.getSource()).withEntity(entity));
            }
            return list;
        })))).then(CommandManager.literal("at").then(CommandManager.argument("targets", EntityArgumentType.entities()).fork((CommandNode)literalCommandNode, context -> {
            ArrayList list = Lists.newArrayList();
            for (Entity entity : EntityArgumentType.getOptionalEntities((CommandContext<ServerCommandSource>)context, "targets")) {
                list.add(((ServerCommandSource)context.getSource()).withWorld((ServerWorld)entity.getEntityWorld()).withPosition(entity.getEntityPos()).withRotation(entity.getRotationClient()));
            }
            return list;
        })))).then(((LiteralArgumentBuilder)CommandManager.literal("store").then(ExecuteCommand.addStoreArguments((LiteralCommandNode<ServerCommandSource>)literalCommandNode, CommandManager.literal("result"), true))).then(ExecuteCommand.addStoreArguments((LiteralCommandNode<ServerCommandSource>)literalCommandNode, CommandManager.literal("success"), false)))).then(((LiteralArgumentBuilder)((LiteralArgumentBuilder)CommandManager.literal("positioned").then(CommandManager.argument("pos", Vec3ArgumentType.vec3()).redirect((CommandNode)literalCommandNode, context -> ((ServerCommandSource)context.getSource()).withPosition(Vec3ArgumentType.getVec3((CommandContext<ServerCommandSource>)context, "pos")).withEntityAnchor(EntityAnchorArgumentType.EntityAnchor.FEET)))).then(CommandManager.literal("as").then(CommandManager.argument("targets", EntityArgumentType.entities()).fork((CommandNode)literalCommandNode, context -> {
            ArrayList list = Lists.newArrayList();
            for (Entity entity : EntityArgumentType.getOptionalEntities((CommandContext<ServerCommandSource>)context, "targets")) {
                list.add(((ServerCommandSource)context.getSource()).withPosition(entity.getEntityPos()));
            }
            return list;
        })))).then(CommandManager.literal("over").then(CommandManager.argument("heightmap", HeightmapArgumentType.heightmap()).redirect((CommandNode)literalCommandNode, context -> {
            Vec3d vec3d = ((ServerCommandSource)context.getSource()).getPosition();
            ServerWorld serverWorld = ((ServerCommandSource)context.getSource()).getWorld();
            double d = vec3d.getX();
            double e = vec3d.getZ();
            if (!serverWorld.isChunkLoaded(ChunkSectionPos.getSectionCoordFloored(d), ChunkSectionPos.getSectionCoordFloored(e))) {
                throw BlockPosArgumentType.UNLOADED_EXCEPTION.create();
            }
            int i = serverWorld.getTopY(HeightmapArgumentType.getHeightmap((CommandContext<ServerCommandSource>)context, "heightmap"), MathHelper.floor(d), MathHelper.floor(e));
            return ((ServerCommandSource)context.getSource()).withPosition(new Vec3d(d, i, e));
        }))))).then(((LiteralArgumentBuilder)CommandManager.literal("rotated").then(CommandManager.argument("rot", RotationArgumentType.rotation()).redirect((CommandNode)literalCommandNode, context -> ((ServerCommandSource)context.getSource()).withRotation(RotationArgumentType.getRotation((CommandContext<ServerCommandSource>)context, "rot").getRotation((ServerCommandSource)context.getSource()))))).then(CommandManager.literal("as").then(CommandManager.argument("targets", EntityArgumentType.entities()).fork((CommandNode)literalCommandNode, context -> {
            ArrayList list = Lists.newArrayList();
            for (Entity entity : EntityArgumentType.getOptionalEntities((CommandContext<ServerCommandSource>)context, "targets")) {
                list.add(((ServerCommandSource)context.getSource()).withRotation(entity.getRotationClient()));
            }
            return list;
        }))))).then(((LiteralArgumentBuilder)CommandManager.literal("facing").then(CommandManager.literal("entity").then(CommandManager.argument("targets", EntityArgumentType.entities()).then(CommandManager.argument("anchor", EntityAnchorArgumentType.entityAnchor()).fork((CommandNode)literalCommandNode, context -> {
            ArrayList list = Lists.newArrayList();
            EntityAnchorArgumentType.EntityAnchor entityAnchor = EntityAnchorArgumentType.getEntityAnchor((CommandContext<ServerCommandSource>)context, "anchor");
            for (Entity entity : EntityArgumentType.getOptionalEntities((CommandContext<ServerCommandSource>)context, "targets")) {
                list.add(((ServerCommandSource)context.getSource()).withLookingAt(entity, entityAnchor));
            }
            return list;
        }))))).then(CommandManager.argument("pos", Vec3ArgumentType.vec3()).redirect((CommandNode)literalCommandNode, context -> ((ServerCommandSource)context.getSource()).withLookingAt(Vec3ArgumentType.getVec3((CommandContext<ServerCommandSource>)context, "pos")))))).then(CommandManager.literal("align").then(CommandManager.argument("axes", SwizzleArgumentType.swizzle()).redirect((CommandNode)literalCommandNode, context -> ((ServerCommandSource)context.getSource()).withPosition(((ServerCommandSource)context.getSource()).getPosition().floorAlongAxes(SwizzleArgumentType.getSwizzle((CommandContext<ServerCommandSource>)context, "axes"))))))).then(CommandManager.literal("anchored").then(CommandManager.argument("anchor", EntityAnchorArgumentType.entityAnchor()).redirect((CommandNode)literalCommandNode, context -> ((ServerCommandSource)context.getSource()).withEntityAnchor(EntityAnchorArgumentType.getEntityAnchor((CommandContext<ServerCommandSource>)context, "anchor")))))).then(CommandManager.literal("in").then(CommandManager.argument("dimension", DimensionArgumentType.dimension()).redirect((CommandNode)literalCommandNode, context -> ((ServerCommandSource)context.getSource()).withWorld(DimensionArgumentType.getDimensionArgument((CommandContext<ServerCommandSource>)context, "dimension")))))).then(CommandManager.literal("summon").then(CommandManager.argument("entity", RegistryEntryReferenceArgumentType.registryEntry(commandRegistryAccess, RegistryKeys.ENTITY_TYPE)).suggests(SuggestionProviders.cast(SuggestionProviders.SUMMONABLE_ENTITIES)).redirect((CommandNode)literalCommandNode, context -> ExecuteCommand.summon((ServerCommandSource)context.getSource(), RegistryEntryReferenceArgumentType.getSummonableEntityType((CommandContext<ServerCommandSource>)context, "entity")))))).then(ExecuteCommand.addOnArguments((CommandNode<ServerCommandSource>)literalCommandNode, CommandManager.literal("on"))));
    }

    private static ArgumentBuilder<ServerCommandSource, ?> addStoreArguments(LiteralCommandNode<ServerCommandSource> node, LiteralArgumentBuilder<ServerCommandSource> builder, boolean requestResult) {
        builder.then(CommandManager.literal("score").then(CommandManager.argument("targets", ScoreHolderArgumentType.scoreHolders()).suggests(ScoreHolderArgumentType.SUGGESTION_PROVIDER).then(CommandManager.argument("objective", ScoreboardObjectiveArgumentType.scoreboardObjective()).redirect(node, context -> ExecuteCommand.executeStoreScore((ServerCommandSource)context.getSource(), ScoreHolderArgumentType.getScoreboardScoreHolders((CommandContext<ServerCommandSource>)context, "targets"), ScoreboardObjectiveArgumentType.getObjective((CommandContext<ServerCommandSource>)context, "objective"), requestResult)))));
        builder.then(CommandManager.literal("bossbar").then(((RequiredArgumentBuilder)CommandManager.argument("id", IdentifierArgumentType.identifier()).suggests(BossBarCommand.SUGGESTION_PROVIDER).then(CommandManager.literal("value").redirect(node, context -> ExecuteCommand.executeStoreBossbar((ServerCommandSource)context.getSource(), BossBarCommand.getBossBar((CommandContext<ServerCommandSource>)context), true, requestResult)))).then(CommandManager.literal("max").redirect(node, context -> ExecuteCommand.executeStoreBossbar((ServerCommandSource)context.getSource(), BossBarCommand.getBossBar((CommandContext<ServerCommandSource>)context), false, requestResult)))));
        for (DataCommand.ObjectType objectType : DataCommand.TARGET_OBJECT_TYPES) {
            objectType.addArgumentsToBuilder((ArgumentBuilder<ServerCommandSource, ?>)builder, builderx -> builderx.then(((RequiredArgumentBuilder)((RequiredArgumentBuilder)((RequiredArgumentBuilder)((RequiredArgumentBuilder)((RequiredArgumentBuilder)CommandManager.argument("path", NbtPathArgumentType.nbtPath()).then(CommandManager.literal("int").then(CommandManager.argument("scale", DoubleArgumentType.doubleArg()).redirect((CommandNode)node, context -> ExecuteCommand.executeStoreData((ServerCommandSource)context.getSource(), objectType.getObject((CommandContext<ServerCommandSource>)context), NbtPathArgumentType.getNbtPath((CommandContext<ServerCommandSource>)context, "path"), result -> NbtInt.of((int)((double)result * DoubleArgumentType.getDouble((CommandContext)context, (String)"scale"))), requestResult))))).then(CommandManager.literal("float").then(CommandManager.argument("scale", DoubleArgumentType.doubleArg()).redirect((CommandNode)node, context -> ExecuteCommand.executeStoreData((ServerCommandSource)context.getSource(), objectType.getObject((CommandContext<ServerCommandSource>)context), NbtPathArgumentType.getNbtPath((CommandContext<ServerCommandSource>)context, "path"), result -> NbtFloat.of((float)((double)result * DoubleArgumentType.getDouble((CommandContext)context, (String)"scale"))), requestResult))))).then(CommandManager.literal("short").then(CommandManager.argument("scale", DoubleArgumentType.doubleArg()).redirect((CommandNode)node, context -> ExecuteCommand.executeStoreData((ServerCommandSource)context.getSource(), objectType.getObject((CommandContext<ServerCommandSource>)context), NbtPathArgumentType.getNbtPath((CommandContext<ServerCommandSource>)context, "path"), result -> NbtShort.of((short)((double)result * DoubleArgumentType.getDouble((CommandContext)context, (String)"scale"))), requestResult))))).then(CommandManager.literal("long").then(CommandManager.argument("scale", DoubleArgumentType.doubleArg()).redirect((CommandNode)node, context -> ExecuteCommand.executeStoreData((ServerCommandSource)context.getSource(), objectType.getObject((CommandContext<ServerCommandSource>)context), NbtPathArgumentType.getNbtPath((CommandContext<ServerCommandSource>)context, "path"), result -> NbtLong.of((long)((double)result * DoubleArgumentType.getDouble((CommandContext)context, (String)"scale"))), requestResult))))).then(CommandManager.literal("double").then(CommandManager.argument("scale", DoubleArgumentType.doubleArg()).redirect((CommandNode)node, context -> ExecuteCommand.executeStoreData((ServerCommandSource)context.getSource(), objectType.getObject((CommandContext<ServerCommandSource>)context), NbtPathArgumentType.getNbtPath((CommandContext<ServerCommandSource>)context, "path"), result -> NbtDouble.of((double)result * DoubleArgumentType.getDouble((CommandContext)context, (String)"scale")), requestResult))))).then(CommandManager.literal("byte").then(CommandManager.argument("scale", DoubleArgumentType.doubleArg()).redirect((CommandNode)node, context -> ExecuteCommand.executeStoreData((ServerCommandSource)context.getSource(), objectType.getObject((CommandContext<ServerCommandSource>)context), NbtPathArgumentType.getNbtPath((CommandContext<ServerCommandSource>)context, "path"), result -> NbtByte.of((byte)((double)result * DoubleArgumentType.getDouble((CommandContext)context, (String)"scale"))), requestResult))))));
        }
        return builder;
    }

    private static ServerCommandSource executeStoreScore(ServerCommandSource source, Collection<ScoreHolder> targets, ScoreboardObjective objective, boolean requestResult) {
        ServerScoreboard scoreboard = source.getServer().getScoreboard();
        return source.mergeReturnValueConsumers((successful, returnValue) -> {
            for (ScoreHolder scoreHolder : targets) {
                ScoreAccess scoreAccess = scoreboard.getOrCreateScore(scoreHolder, objective);
                int i = requestResult ? returnValue : (successful ? 1 : 0);
                scoreAccess.setScore(i);
            }
        }, ReturnValueConsumer::chain);
    }

    private static ServerCommandSource executeStoreBossbar(ServerCommandSource source, CommandBossBar bossBar, boolean storeInValue, boolean requestResult) {
        return source.mergeReturnValueConsumers((successful, returnValue) -> {
            int i;
            int n = requestResult ? returnValue : (i = successful ? 1 : 0);
            if (storeInValue) {
                bossBar.setValue(i);
            } else {
                bossBar.setMaxValue(i);
            }
        }, ReturnValueConsumer::chain);
    }

    private static ServerCommandSource executeStoreData(ServerCommandSource source, DataCommandObject object, NbtPathArgumentType.NbtPath path, IntFunction<NbtElement> nbtSetter, boolean requestResult) {
        return source.mergeReturnValueConsumers((successful, returnValue) -> {
            try {
                NbtCompound nbtCompound = object.getNbt();
                int i = requestResult ? returnValue : (successful ? 1 : 0);
                path.put(nbtCompound, (NbtElement)nbtSetter.apply(i));
                object.setNbt(nbtCompound);
            }
            catch (CommandSyntaxException commandSyntaxException) {
                // empty catch block
            }
        }, ReturnValueConsumer::chain);
    }

    private static boolean isLoaded(ServerWorld world, BlockPos pos) {
        ChunkPos chunkPos = new ChunkPos(pos);
        WorldChunk worldChunk = world.getChunkManager().getWorldChunk(chunkPos.x, chunkPos.z);
        if (worldChunk != null) {
            return worldChunk.getLevelType() == ChunkLevelType.ENTITY_TICKING && world.isChunkLoaded(chunkPos.toLong());
        }
        return false;
    }

    private static ArgumentBuilder<ServerCommandSource, ?> addConditionArguments(CommandNode<ServerCommandSource> root, LiteralArgumentBuilder<ServerCommandSource> argumentBuilder, boolean positive, CommandRegistryAccess commandRegistryAccess) {
        ((LiteralArgumentBuilder)((LiteralArgumentBuilder)((LiteralArgumentBuilder)((LiteralArgumentBuilder)((LiteralArgumentBuilder)((LiteralArgumentBuilder)((LiteralArgumentBuilder)((LiteralArgumentBuilder)((LiteralArgumentBuilder)((LiteralArgumentBuilder)argumentBuilder.then(CommandManager.literal("block").then(CommandManager.argument("pos", BlockPosArgumentType.blockPos()).then(ExecuteCommand.addConditionLogic(root, CommandManager.argument("block", BlockPredicateArgumentType.blockPredicate(commandRegistryAccess)), positive, context -> BlockPredicateArgumentType.getBlockPredicate((CommandContext<ServerCommandSource>)context, "block").test(new CachedBlockPosition(((ServerCommandSource)context.getSource()).getWorld(), BlockPosArgumentType.getLoadedBlockPos((CommandContext<ServerCommandSource>)context, "pos"), true))))))).then(CommandManager.literal("biome").then(CommandManager.argument("pos", BlockPosArgumentType.blockPos()).then(ExecuteCommand.addConditionLogic(root, CommandManager.argument("biome", RegistryEntryPredicateArgumentType.registryEntryPredicate(commandRegistryAccess, RegistryKeys.BIOME)), positive, context -> RegistryEntryPredicateArgumentType.getRegistryEntryPredicate((CommandContext<ServerCommandSource>)context, "biome", RegistryKeys.BIOME).test(((ServerCommandSource)context.getSource()).getWorld().getBiome(BlockPosArgumentType.getLoadedBlockPos((CommandContext<ServerCommandSource>)context, "pos")))))))).then(CommandManager.literal("loaded").then(ExecuteCommand.addConditionLogic(root, CommandManager.argument("pos", BlockPosArgumentType.blockPos()), positive, context -> ExecuteCommand.isLoaded(((ServerCommandSource)context.getSource()).getWorld(), BlockPosArgumentType.getBlockPos((CommandContext<ServerCommandSource>)context, "pos")))))).then(CommandManager.literal("dimension").then(ExecuteCommand.addConditionLogic(root, CommandManager.argument("dimension", DimensionArgumentType.dimension()), positive, context -> DimensionArgumentType.getDimensionArgument((CommandContext<ServerCommandSource>)context, "dimension") == ((ServerCommandSource)context.getSource()).getWorld())))).then(CommandManager.literal("score").then(CommandManager.argument("target", ScoreHolderArgumentType.scoreHolder()).suggests(ScoreHolderArgumentType.SUGGESTION_PROVIDER).then(((RequiredArgumentBuilder)((RequiredArgumentBuilder)((RequiredArgumentBuilder)((RequiredArgumentBuilder)((RequiredArgumentBuilder)CommandManager.argument("targetObjective", ScoreboardObjectiveArgumentType.scoreboardObjective()).then(CommandManager.literal("=").then(CommandManager.argument("source", ScoreHolderArgumentType.scoreHolder()).suggests(ScoreHolderArgumentType.SUGGESTION_PROVIDER).then(ExecuteCommand.addConditionLogic(root, CommandManager.argument("sourceObjective", ScoreboardObjectiveArgumentType.scoreboardObjective()), positive, context -> ExecuteCommand.testScoreCondition((CommandContext<ServerCommandSource>)context, (targetScore, sourceScore) -> targetScore == sourceScore)))))).then(CommandManager.literal("<").then(CommandManager.argument("source", ScoreHolderArgumentType.scoreHolder()).suggests(ScoreHolderArgumentType.SUGGESTION_PROVIDER).then(ExecuteCommand.addConditionLogic(root, CommandManager.argument("sourceObjective", ScoreboardObjectiveArgumentType.scoreboardObjective()), positive, context -> ExecuteCommand.testScoreCondition((CommandContext<ServerCommandSource>)context, (targetScore, sourceScore) -> targetScore < sourceScore)))))).then(CommandManager.literal("<=").then(CommandManager.argument("source", ScoreHolderArgumentType.scoreHolder()).suggests(ScoreHolderArgumentType.SUGGESTION_PROVIDER).then(ExecuteCommand.addConditionLogic(root, CommandManager.argument("sourceObjective", ScoreboardObjectiveArgumentType.scoreboardObjective()), positive, context -> ExecuteCommand.testScoreCondition((CommandContext<ServerCommandSource>)context, (targetScore, sourceScore) -> targetScore <= sourceScore)))))).then(CommandManager.literal(">").then(CommandManager.argument("source", ScoreHolderArgumentType.scoreHolder()).suggests(ScoreHolderArgumentType.SUGGESTION_PROVIDER).then(ExecuteCommand.addConditionLogic(root, CommandManager.argument("sourceObjective", ScoreboardObjectiveArgumentType.scoreboardObjective()), positive, context -> ExecuteCommand.testScoreCondition((CommandContext<ServerCommandSource>)context, (targetScore, sourceScore) -> targetScore > sourceScore)))))).then(CommandManager.literal(">=").then(CommandManager.argument("source", ScoreHolderArgumentType.scoreHolder()).suggests(ScoreHolderArgumentType.SUGGESTION_PROVIDER).then(ExecuteCommand.addConditionLogic(root, CommandManager.argument("sourceObjective", ScoreboardObjectiveArgumentType.scoreboardObjective()), positive, context -> ExecuteCommand.testScoreCondition((CommandContext<ServerCommandSource>)context, (targetScore, sourceScore) -> targetScore >= sourceScore)))))).then(CommandManager.literal("matches").then(ExecuteCommand.addConditionLogic(root, CommandManager.argument("range", NumberRangeArgumentType.intRange()), positive, context -> ExecuteCommand.testScoreMatch((CommandContext<ServerCommandSource>)context, NumberRangeArgumentType.IntRangeArgumentType.getRangeArgument((CommandContext<ServerCommandSource>)context, "range"))))))))).then(CommandManager.literal("blocks").then(CommandManager.argument("start", BlockPosArgumentType.blockPos()).then(CommandManager.argument("end", BlockPosArgumentType.blockPos()).then(((RequiredArgumentBuilder)CommandManager.argument("destination", BlockPosArgumentType.blockPos()).then(ExecuteCommand.addBlocksConditionLogic(root, CommandManager.literal("all"), positive, false))).then(ExecuteCommand.addBlocksConditionLogic(root, CommandManager.literal("masked"), positive, true))))))).then(CommandManager.literal("entity").then(((RequiredArgumentBuilder)CommandManager.argument("entities", EntityArgumentType.entities()).fork(root, context -> ExecuteCommand.getSourceOrEmptyForConditionFork((CommandContext<ServerCommandSource>)context, positive, !EntityArgumentType.getOptionalEntities((CommandContext<ServerCommandSource>)context, "entities").isEmpty()))).executes(ExecuteCommand.getExistsConditionExecute(positive, context -> EntityArgumentType.getOptionalEntities((CommandContext<ServerCommandSource>)context, "entities").size()))))).then(CommandManager.literal("predicate").then(ExecuteCommand.addConditionLogic(root, CommandManager.argument("predicate", RegistryEntryArgumentType.lootCondition(commandRegistryAccess)), positive, context -> ExecuteCommand.testLootCondition((ServerCommandSource)context.getSource(), RegistryEntryArgumentType.getLootCondition((CommandContext<ServerCommandSource>)context, "predicate")))))).then(CommandManager.literal("function").then(CommandManager.argument("name", CommandFunctionArgumentType.commandFunction()).suggests(FunctionCommand.SUGGESTION_PROVIDER).fork(root, (RedirectModifier)new IfUnlessRedirector(positive))))).then(((LiteralArgumentBuilder)CommandManager.literal("items").then(CommandManager.literal("entity").then(CommandManager.argument("entities", EntityArgumentType.entities()).then(CommandManager.argument("slots", SlotRangeArgumentType.slotRange()).then(((RequiredArgumentBuilder)CommandManager.argument("item_predicate", ItemPredicateArgumentType.itemPredicate(commandRegistryAccess)).fork(root, context -> ExecuteCommand.getSourceOrEmptyForConditionFork((CommandContext<ServerCommandSource>)context, positive, ExecuteCommand.countMatchingItems(EntityArgumentType.getEntities((CommandContext<ServerCommandSource>)context, "entities"), SlotRangeArgumentType.getSlotRange((CommandContext<ServerCommandSource>)context, "slots"), ItemPredicateArgumentType.getItemStackPredicate((CommandContext<ServerCommandSource>)context, "item_predicate")) > 0))).executes(ExecuteCommand.getExistsConditionExecute(positive, context -> ExecuteCommand.countMatchingItems(EntityArgumentType.getEntities((CommandContext<ServerCommandSource>)context, "entities"), SlotRangeArgumentType.getSlotRange((CommandContext<ServerCommandSource>)context, "slots"), ItemPredicateArgumentType.getItemStackPredicate((CommandContext<ServerCommandSource>)context, "item_predicate"))))))))).then(CommandManager.literal("block").then(CommandManager.argument("pos", BlockPosArgumentType.blockPos()).then(CommandManager.argument("slots", SlotRangeArgumentType.slotRange()).then(((RequiredArgumentBuilder)CommandManager.argument("item_predicate", ItemPredicateArgumentType.itemPredicate(commandRegistryAccess)).fork(root, context -> ExecuteCommand.getSourceOrEmptyForConditionFork((CommandContext<ServerCommandSource>)context, positive, ExecuteCommand.countMatchingItems((ServerCommandSource)context.getSource(), BlockPosArgumentType.getLoadedBlockPos((CommandContext<ServerCommandSource>)context, "pos"), SlotRangeArgumentType.getSlotRange((CommandContext<ServerCommandSource>)context, "slots"), ItemPredicateArgumentType.getItemStackPredicate((CommandContext<ServerCommandSource>)context, "item_predicate")) > 0))).executes(ExecuteCommand.getExistsConditionExecute(positive, context -> ExecuteCommand.countMatchingItems((ServerCommandSource)context.getSource(), BlockPosArgumentType.getLoadedBlockPos((CommandContext<ServerCommandSource>)context, "pos"), SlotRangeArgumentType.getSlotRange((CommandContext<ServerCommandSource>)context, "slots"), ItemPredicateArgumentType.getItemStackPredicate((CommandContext<ServerCommandSource>)context, "item_predicate")))))))))).then(CommandManager.literal("stopwatch").then(CommandManager.argument("id", IdentifierArgumentType.identifier()).suggests(StopwatchCommand.STOPWATCH_SUGGESTION_PROVIDER).then(ExecuteCommand.addConditionLogic(root, CommandManager.argument("range", NumberRangeArgumentType.floatRange()), positive, context -> ExecuteCommand.testStopwatchRange((CommandContext<ServerCommandSource>)context, NumberRangeArgumentType.FloatRangeArgumentType.getRangeArgument((CommandContext<ServerCommandSource>)context, "range"))))));
        for (DataCommand.ObjectType objectType : DataCommand.SOURCE_OBJECT_TYPES) {
            argumentBuilder.then(objectType.addArgumentsToBuilder((ArgumentBuilder<ServerCommandSource, ?>)CommandManager.literal("data"), builder -> builder.then(((RequiredArgumentBuilder)CommandManager.argument("path", NbtPathArgumentType.nbtPath()).fork(root, context -> ExecuteCommand.getSourceOrEmptyForConditionFork((CommandContext<ServerCommandSource>)context, positive, ExecuteCommand.countPathMatches(objectType.getObject((CommandContext<ServerCommandSource>)context), NbtPathArgumentType.getNbtPath((CommandContext<ServerCommandSource>)context, "path")) > 0))).executes(ExecuteCommand.getExistsConditionExecute(positive, context -> ExecuteCommand.countPathMatches(objectType.getObject((CommandContext<ServerCommandSource>)context), NbtPathArgumentType.getNbtPath((CommandContext<ServerCommandSource>)context, "path")))))));
        }
        return argumentBuilder;
    }

    private static int countMatchingItems(Iterable<? extends StackReferenceGetter> entities, SlotRange slotRange, Predicate<ItemStack> predicate) {
        int i = 0;
        for (StackReferenceGetter stackReferenceGetter : entities) {
            IntList intList = slotRange.getSlotIds();
            for (int j = 0; j < intList.size(); ++j) {
                ItemStack itemStack;
                int k = intList.getInt(j);
                StackReference stackReference = stackReferenceGetter.getStackReference(k);
                if (stackReference == null || !predicate.test(itemStack = stackReference.get())) continue;
                i += itemStack.getCount();
            }
        }
        return i;
    }

    private static int countMatchingItems(ServerCommandSource source, BlockPos pos, SlotRange slotRange, Predicate<ItemStack> predicate) throws CommandSyntaxException {
        int i = 0;
        Inventory inventory = ItemCommand.getInventoryAtPos(source, pos, ItemCommand.NOT_A_CONTAINER_SOURCE_EXCEPTION);
        int j = inventory.size();
        IntList intList = slotRange.getSlotIds();
        for (int k = 0; k < intList.size(); ++k) {
            ItemStack itemStack;
            int l = intList.getInt(k);
            if (l < 0 || l >= j || !predicate.test(itemStack = inventory.getStack(l))) continue;
            i += itemStack.getCount();
        }
        return i;
    }

    private static Command<ServerCommandSource> getExistsConditionExecute(boolean positive, ExistsCondition condition) {
        if (positive) {
            return context -> {
                int i = condition.test((CommandContext<ServerCommandSource>)context);
                if (i > 0) {
                    ((ServerCommandSource)context.getSource()).sendFeedback(() -> Text.translatable("commands.execute.conditional.pass_count", i), false);
                    return i;
                }
                throw CONDITIONAL_FAIL_EXCEPTION.create();
            };
        }
        return context -> {
            int i = condition.test((CommandContext<ServerCommandSource>)context);
            if (i == 0) {
                ((ServerCommandSource)context.getSource()).sendFeedback(() -> Text.translatable("commands.execute.conditional.pass"), false);
                return 1;
            }
            throw CONDITIONAL_FAIL_COUNT_EXCEPTION.create((Object)i);
        };
    }

    private static int countPathMatches(DataCommandObject object, NbtPathArgumentType.NbtPath path) throws CommandSyntaxException {
        return path.count(object.getNbt());
    }

    private static boolean testScoreCondition(CommandContext<ServerCommandSource> context, ScoreComparisonPredicate predicate) throws CommandSyntaxException {
        ScoreHolder scoreHolder = ScoreHolderArgumentType.getScoreHolder(context, "target");
        ScoreboardObjective scoreboardObjective = ScoreboardObjectiveArgumentType.getObjective(context, "targetObjective");
        ScoreHolder scoreHolder2 = ScoreHolderArgumentType.getScoreHolder(context, "source");
        ScoreboardObjective scoreboardObjective2 = ScoreboardObjectiveArgumentType.getObjective(context, "sourceObjective");
        ServerScoreboard scoreboard = ((ServerCommandSource)context.getSource()).getServer().getScoreboard();
        ReadableScoreboardScore readableScoreboardScore = scoreboard.getScore(scoreHolder, scoreboardObjective);
        ReadableScoreboardScore readableScoreboardScore2 = scoreboard.getScore(scoreHolder2, scoreboardObjective2);
        if (readableScoreboardScore == null || readableScoreboardScore2 == null) {
            return false;
        }
        return predicate.test(readableScoreboardScore.getScore(), readableScoreboardScore2.getScore());
    }

    private static boolean testScoreMatch(CommandContext<ServerCommandSource> context, NumberRange.IntRange range) throws CommandSyntaxException {
        ScoreHolder scoreHolder = ScoreHolderArgumentType.getScoreHolder(context, "target");
        ScoreboardObjective scoreboardObjective = ScoreboardObjectiveArgumentType.getObjective(context, "targetObjective");
        ServerScoreboard scoreboard = ((ServerCommandSource)context.getSource()).getServer().getScoreboard();
        ReadableScoreboardScore readableScoreboardScore = scoreboard.getScore(scoreHolder, scoreboardObjective);
        if (readableScoreboardScore == null) {
            return false;
        }
        return range.test(readableScoreboardScore.getScore());
    }

    private static boolean testStopwatchRange(CommandContext<ServerCommandSource> context, NumberRange.DoubleRange range) throws CommandSyntaxException {
        Identifier identifier = IdentifierArgumentType.getIdentifier(context, "id");
        StopwatchPersistentState stopwatchPersistentState = ((ServerCommandSource)context.getSource()).getServer().getStopwatchPersistentState();
        Stopwatch stopwatch = stopwatchPersistentState.get(identifier);
        if (stopwatch == null) {
            throw StopwatchCommand.DOES_NOT_EXIST_EXCEPTION.create((Object)identifier);
        }
        long l = StopwatchPersistentState.getTimeMs();
        double d = stopwatch.getElapsedTimeSeconds(l);
        return range.test(d);
    }

    private static boolean testLootCondition(ServerCommandSource source, RegistryEntry<LootCondition> lootCondition) {
        ServerWorld serverWorld = source.getWorld();
        LootWorldContext lootWorldContext = new LootWorldContext.Builder(serverWorld).add(LootContextParameters.ORIGIN, source.getPosition()).addOptional(LootContextParameters.THIS_ENTITY, source.getEntity()).build(LootContextTypes.COMMAND);
        LootContext lootContext = new LootContext.Builder(lootWorldContext).build(Optional.empty());
        lootContext.markActive(LootContext.predicate(lootCondition.value()));
        return lootCondition.value().test(lootContext);
    }

    private static Collection<ServerCommandSource> getSourceOrEmptyForConditionFork(CommandContext<ServerCommandSource> context, boolean positive, boolean value) {
        if (value == positive) {
            return Collections.singleton((ServerCommandSource)context.getSource());
        }
        return Collections.emptyList();
    }

    private static ArgumentBuilder<ServerCommandSource, ?> addConditionLogic(CommandNode<ServerCommandSource> root, ArgumentBuilder<ServerCommandSource, ?> builder, boolean positive, Condition condition) {
        return builder.fork(root, context -> ExecuteCommand.getSourceOrEmptyForConditionFork((CommandContext<ServerCommandSource>)context, positive, condition.test((CommandContext<ServerCommandSource>)context))).executes(context -> {
            if (positive == condition.test((CommandContext<ServerCommandSource>)context)) {
                ((ServerCommandSource)context.getSource()).sendFeedback(() -> Text.translatable("commands.execute.conditional.pass"), false);
                return 1;
            }
            throw CONDITIONAL_FAIL_EXCEPTION.create();
        });
    }

    private static ArgumentBuilder<ServerCommandSource, ?> addBlocksConditionLogic(CommandNode<ServerCommandSource> root, ArgumentBuilder<ServerCommandSource, ?> builder, boolean positive, boolean masked) {
        return builder.fork(root, context -> ExecuteCommand.getSourceOrEmptyForConditionFork((CommandContext<ServerCommandSource>)context, positive, ExecuteCommand.testBlocksCondition((CommandContext<ServerCommandSource>)context, masked).isPresent())).executes(positive ? context -> ExecuteCommand.executePositiveBlockCondition((CommandContext<ServerCommandSource>)context, masked) : context -> ExecuteCommand.executeNegativeBlockCondition((CommandContext<ServerCommandSource>)context, masked));
    }

    private static int executePositiveBlockCondition(CommandContext<ServerCommandSource> context, boolean masked) throws CommandSyntaxException {
        OptionalInt optionalInt = ExecuteCommand.testBlocksCondition(context, masked);
        if (optionalInt.isPresent()) {
            ((ServerCommandSource)context.getSource()).sendFeedback(() -> Text.translatable("commands.execute.conditional.pass_count", optionalInt.getAsInt()), false);
            return optionalInt.getAsInt();
        }
        throw CONDITIONAL_FAIL_EXCEPTION.create();
    }

    private static int executeNegativeBlockCondition(CommandContext<ServerCommandSource> context, boolean masked) throws CommandSyntaxException {
        OptionalInt optionalInt = ExecuteCommand.testBlocksCondition(context, masked);
        if (optionalInt.isPresent()) {
            throw CONDITIONAL_FAIL_COUNT_EXCEPTION.create((Object)optionalInt.getAsInt());
        }
        ((ServerCommandSource)context.getSource()).sendFeedback(() -> Text.translatable("commands.execute.conditional.pass"), false);
        return 1;
    }

    private static OptionalInt testBlocksCondition(CommandContext<ServerCommandSource> context, boolean masked) throws CommandSyntaxException {
        return ExecuteCommand.testBlocksCondition(((ServerCommandSource)context.getSource()).getWorld(), BlockPosArgumentType.getLoadedBlockPos(context, "start"), BlockPosArgumentType.getLoadedBlockPos(context, "end"), BlockPosArgumentType.getLoadedBlockPos(context, "destination"), masked);
    }

    private static OptionalInt testBlocksCondition(ServerWorld world, BlockPos start, BlockPos end, BlockPos destination, boolean masked) throws CommandSyntaxException {
        BlockBox blockBox = BlockBox.create(start, end);
        BlockBox blockBox2 = BlockBox.create(destination, destination.add(blockBox.getDimensions()));
        BlockPos blockPos = new BlockPos(blockBox2.getMinX() - blockBox.getMinX(), blockBox2.getMinY() - blockBox.getMinY(), blockBox2.getMinZ() - blockBox.getMinZ());
        int i = blockBox.getBlockCountX() * blockBox.getBlockCountY() * blockBox.getBlockCountZ();
        if (i > 32768) {
            throw BLOCKS_TOOBIG_EXCEPTION.create((Object)32768, (Object)i);
        }
        int j = 0;
        DynamicRegistryManager dynamicRegistryManager = world.getRegistryManager();
        try (ErrorReporter.Logging logging = new ErrorReporter.Logging(LOGGER);){
            for (int k = blockBox.getMinZ(); k <= blockBox.getMaxZ(); ++k) {
                for (int l = blockBox.getMinY(); l <= blockBox.getMaxY(); ++l) {
                    for (int m = blockBox.getMinX(); m <= blockBox.getMaxX(); ++m) {
                        BlockPos blockPos2 = new BlockPos(m, l, k);
                        BlockPos blockPos3 = blockPos2.add(blockPos);
                        BlockState blockState = world.getBlockState(blockPos2);
                        if (masked && blockState.isOf(Blocks.AIR)) continue;
                        if (blockState != world.getBlockState(blockPos3)) {
                            OptionalInt optionalInt = OptionalInt.empty();
                            return optionalInt;
                        }
                        BlockEntity blockEntity = world.getBlockEntity(blockPos2);
                        BlockEntity blockEntity2 = world.getBlockEntity(blockPos3);
                        if (blockEntity != null) {
                            OptionalInt optionalInt;
                            if (blockEntity2 == null) {
                                optionalInt = OptionalInt.empty();
                                return optionalInt;
                            }
                            if (blockEntity2.getType() != blockEntity.getType()) {
                                optionalInt = OptionalInt.empty();
                                return optionalInt;
                            }
                            if (!blockEntity.getComponents().equals(blockEntity2.getComponents())) {
                                optionalInt = OptionalInt.empty();
                                return optionalInt;
                            }
                            NbtWriteView nbtWriteView = NbtWriteView.create(logging.makeChild(blockEntity.getReporterContext()), dynamicRegistryManager);
                            blockEntity.writeComponentlessData(nbtWriteView);
                            NbtCompound nbtCompound = nbtWriteView.getNbt();
                            NbtWriteView nbtWriteView2 = NbtWriteView.create(logging.makeChild(blockEntity2.getReporterContext()), dynamicRegistryManager);
                            blockEntity2.writeComponentlessData(nbtWriteView2);
                            NbtCompound nbtCompound2 = nbtWriteView2.getNbt();
                            if (!nbtCompound.equals(nbtCompound2)) {
                                OptionalInt optionalInt2 = OptionalInt.empty();
                                return optionalInt2;
                            }
                        }
                        ++j;
                    }
                }
            }
        }
        return OptionalInt.of(j);
    }

    private static RedirectModifier<ServerCommandSource> createEntityModifier(Function<Entity, Optional<Entity>> function) {
        return context -> {
            ServerCommandSource serverCommandSource = (ServerCommandSource)context.getSource();
            Entity entity2 = serverCommandSource.getEntity();
            if (entity2 == null) {
                return List.of();
            }
            return ((Optional)function.apply(entity2)).filter(entity -> !entity.isRemoved()).map(entity -> List.of(serverCommandSource.withEntity((Entity)entity))).orElse(List.of());
        };
    }

    private static RedirectModifier<ServerCommandSource> createMultiEntityModifier(Function<Entity, Stream<Entity>> function) {
        return context -> {
            ServerCommandSource serverCommandSource = (ServerCommandSource)context.getSource();
            Entity entity2 = serverCommandSource.getEntity();
            if (entity2 == null) {
                return List.of();
            }
            return ((Stream)function.apply(entity2)).filter(entity -> !entity.isRemoved()).map(serverCommandSource::withEntity).toList();
        };
    }

    private static LiteralArgumentBuilder<ServerCommandSource> addOnArguments(CommandNode<ServerCommandSource> node, LiteralArgumentBuilder<ServerCommandSource> builder) {
        return (LiteralArgumentBuilder)((LiteralArgumentBuilder)((LiteralArgumentBuilder)((LiteralArgumentBuilder)((LiteralArgumentBuilder)((LiteralArgumentBuilder)((LiteralArgumentBuilder)((LiteralArgumentBuilder)builder.then(CommandManager.literal("owner").fork(node, ExecuteCommand.createEntityModifier(entity -> {
            Optional<Object> optional;
            if (entity instanceof Tameable) {
                Tameable tameable = (Tameable)((Object)entity);
                optional = Optional.ofNullable(tameable.getOwner());
            } else {
                optional = Optional.empty();
            }
            return optional;
        })))).then(CommandManager.literal("leasher").fork(node, ExecuteCommand.createEntityModifier(entity -> {
            Optional<Object> optional;
            if (entity instanceof Leashable) {
                Leashable leashable = (Leashable)((Object)entity);
                optional = Optional.ofNullable(leashable.getLeashHolder());
            } else {
                optional = Optional.empty();
            }
            return optional;
        })))).then(CommandManager.literal("target").fork(node, ExecuteCommand.createEntityModifier(entity -> {
            Optional<Object> optional;
            if (entity instanceof Targeter) {
                Targeter targeter = (Targeter)((Object)entity);
                optional = Optional.ofNullable(targeter.getTarget());
            } else {
                optional = Optional.empty();
            }
            return optional;
        })))).then(CommandManager.literal("attacker").fork(node, ExecuteCommand.createEntityModifier(entity -> {
            Optional<Object> optional;
            if (entity instanceof Attackable) {
                Attackable attackable = (Attackable)((Object)entity);
                optional = Optional.ofNullable(attackable.getLastAttacker());
            } else {
                optional = Optional.empty();
            }
            return optional;
        })))).then(CommandManager.literal("vehicle").fork(node, ExecuteCommand.createEntityModifier(entity -> Optional.ofNullable(entity.getVehicle()))))).then(CommandManager.literal("controller").fork(node, ExecuteCommand.createEntityModifier(entity -> Optional.ofNullable(entity.getControllingPassenger()))))).then(CommandManager.literal("origin").fork(node, ExecuteCommand.createEntityModifier(entity -> {
            Optional<Object> optional;
            if (entity instanceof Ownable) {
                Ownable ownable = (Ownable)((Object)entity);
                optional = Optional.ofNullable(ownable.getOwner());
            } else {
                optional = Optional.empty();
            }
            return optional;
        })))).then(CommandManager.literal("passengers").fork(node, ExecuteCommand.createMultiEntityModifier(entity -> entity.getPassengerList().stream())));
    }

    private static ServerCommandSource summon(ServerCommandSource source, RegistryEntry.Reference<EntityType<?>> entityType) throws CommandSyntaxException {
        Entity entity = SummonCommand.summon(source, entityType, source.getPosition(), new NbtCompound(), true);
        return source.withEntity(entity);
    }

    /*
     * Exception decompiling
     */
    public static <T extends AbstractServerCommandSource<T>> void enqueueExecutions(T baseSource, List<T> sources, Function<T, T> functionSourceGetter, IntPredicate predicate, ContextChain<T> contextChain, @Nullable NbtCompound args, ExecutionControl<T> control, ArgumentGetter<CommandContext<T>, Collection<CommandFunction<T>>> functionNamesGetter, ExecutionFlags flags) {
        /*
         * This method has failed to decompile.  When submitting a bug report, please provide this stack trace, and (if you hold appropriate legal rights) the relevant class file.
         * 
         * org.benf.cfr.reader.util.ConfusedCFRException: Started 2 blocks at once
         *     at org.benf.cfr.reader.bytecode.analysis.opgraph.Op04StructuredStatement.getStartingBlocks(Op04StructuredStatement.java:412)
         *     at org.benf.cfr.reader.bytecode.analysis.opgraph.Op04StructuredStatement.buildNestedBlocks(Op04StructuredStatement.java:487)
         *     at org.benf.cfr.reader.bytecode.analysis.opgraph.Op03SimpleStatement.createInitialStructuredBlock(Op03SimpleStatement.java:736)
         *     at org.benf.cfr.reader.bytecode.CodeAnalyser.getAnalysisInner(CodeAnalyser.java:850)
         *     at org.benf.cfr.reader.bytecode.CodeAnalyser.getAnalysisOrWrapFail(CodeAnalyser.java:278)
         *     at org.benf.cfr.reader.bytecode.CodeAnalyser.getAnalysis(CodeAnalyser.java:201)
         *     at org.benf.cfr.reader.entities.attributes.AttributeCode.analyse(AttributeCode.java:94)
         *     at org.benf.cfr.reader.entities.Method.analyse(Method.java:531)
         *     at org.benf.cfr.reader.entities.ClassFile.analyseMid(ClassFile.java:1055)
         *     at org.benf.cfr.reader.entities.ClassFile.analyseTop(ClassFile.java:942)
         *     at org.benf.cfr.reader.Driver.doJarVersionTypes(Driver.java:257)
         *     at org.benf.cfr.reader.Driver.doJar(Driver.java:139)
         *     at org.benf.cfr.reader.CfrDriverImpl.analyse(CfrDriverImpl.java:76)
         *     at org.benf.cfr.reader.Main.main(Main.java:54)
         */
        throw new IllegalStateException("Decompilation failed");
    }

    private static /* synthetic */ void method_54852(List list, AbstractServerCommandSource abstractServerCommandSource, ExecutionControl newControl) {
        for (Procedure procedure : list) {
            newControl.enqueueAction(new CommandFunctionAction<AbstractServerCommandSource>(procedure, newControl.getFrame().returnValueConsumer(), true).bind(abstractServerCommandSource));
        }
        newControl.enqueueAction(FallthroughCommandAction.getInstance());
    }

    private static /* synthetic */ void method_54853(IntPredicate intPredicate, List list, AbstractServerCommandSource abstractServerCommandSource, boolean successful, int returnValue) {
        if (intPredicate.test(returnValue)) {
            list.add(abstractServerCommandSource);
        }
    }

    @FunctionalInterface
    static interface Condition {
        public boolean test(CommandContext<ServerCommandSource> var1) throws CommandSyntaxException;
    }

    @FunctionalInterface
    static interface ExistsCondition {
        public int test(CommandContext<ServerCommandSource> var1) throws CommandSyntaxException;
    }

    static class IfUnlessRedirector
    implements Forkable.RedirectModifier<ServerCommandSource> {
        private final IntPredicate predicate;

        IfUnlessRedirector(boolean success) {
            this.predicate = success ? result -> result != 0 : result -> result == 0;
        }

        @Override
        public void execute(ServerCommandSource serverCommandSource, List<ServerCommandSource> list, ContextChain<ServerCommandSource> contextChain, ExecutionFlags executionFlags, ExecutionControl<ServerCommandSource> executionControl) {
            ExecuteCommand.enqueueExecutions(serverCommandSource, list, FunctionCommand::createFunctionCommandSource, this.predicate, contextChain, null, executionControl, context -> CommandFunctionArgumentType.getFunctions((CommandContext<ServerCommandSource>)context, "name"), executionFlags);
        }
    }

    @FunctionalInterface
    static interface ScoreComparisonPredicate {
        public boolean test(int var1, int var2);
    }
}


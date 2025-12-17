/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  com.mojang.brigadier.CommandDispatcher
 *  com.mojang.brigadier.Message
 *  com.mojang.brigadier.arguments.FloatArgumentType
 *  com.mojang.brigadier.arguments.IntegerArgumentType
 *  com.mojang.brigadier.builder.LiteralArgumentBuilder
 *  com.mojang.brigadier.builder.RequiredArgumentBuilder
 *  com.mojang.brigadier.context.CommandContext
 *  com.mojang.brigadier.exceptions.CommandSyntaxException
 *  com.mojang.brigadier.exceptions.DynamicCommandExceptionType
 *  com.mojang.brigadier.exceptions.SimpleCommandExceptionType
 *  com.mojang.brigadier.suggestion.SuggestionProvider
 */
package net.minecraft.server.command;

import com.mojang.brigadier.CommandDispatcher;
import com.mojang.brigadier.Message;
import com.mojang.brigadier.arguments.FloatArgumentType;
import com.mojang.brigadier.arguments.IntegerArgumentType;
import com.mojang.brigadier.builder.LiteralArgumentBuilder;
import com.mojang.brigadier.builder.RequiredArgumentBuilder;
import com.mojang.brigadier.context.CommandContext;
import com.mojang.brigadier.exceptions.CommandSyntaxException;
import com.mojang.brigadier.exceptions.DynamicCommandExceptionType;
import com.mojang.brigadier.exceptions.SimpleCommandExceptionType;
import com.mojang.brigadier.suggestion.SuggestionProvider;
import java.util.Optional;
import net.minecraft.block.entity.StructureBlockBlockEntity;
import net.minecraft.command.CommandSource;
import net.minecraft.command.argument.BlockMirrorArgumentType;
import net.minecraft.command.argument.BlockPosArgumentType;
import net.minecraft.command.argument.BlockRotationArgumentType;
import net.minecraft.command.argument.IdentifierArgumentType;
import net.minecraft.command.argument.RegistryKeyArgumentType;
import net.minecraft.registry.RegistryKeys;
import net.minecraft.registry.entry.RegistryEntry;
import net.minecraft.server.command.CommandManager;
import net.minecraft.server.command.ServerCommandSource;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.structure.StructurePlacementData;
import net.minecraft.structure.StructureStart;
import net.minecraft.structure.StructureTemplate;
import net.minecraft.structure.StructureTemplateManager;
import net.minecraft.structure.pool.StructurePool;
import net.minecraft.structure.pool.StructurePoolBasedGenerator;
import net.minecraft.structure.processor.BlockRotStructureProcessor;
import net.minecraft.text.Text;
import net.minecraft.util.BlockMirror;
import net.minecraft.util.BlockRotation;
import net.minecraft.util.Identifier;
import net.minecraft.util.InvalidIdentifierException;
import net.minecraft.util.math.BlockBox;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.ChunkPos;
import net.minecraft.util.math.ChunkSectionPos;
import net.minecraft.world.gen.chunk.ChunkGenerator;
import net.minecraft.world.gen.feature.ConfiguredFeature;
import net.minecraft.world.gen.structure.Structure;

public class PlaceCommand {
    private static final SimpleCommandExceptionType FEATURE_FAILED_EXCEPTION = new SimpleCommandExceptionType((Message)Text.translatable("commands.place.feature.failed"));
    private static final SimpleCommandExceptionType JIGSAW_FAILED_EXCEPTION = new SimpleCommandExceptionType((Message)Text.translatable("commands.place.jigsaw.failed"));
    private static final SimpleCommandExceptionType STRUCTURE_FAILED_EXCEPTION = new SimpleCommandExceptionType((Message)Text.translatable("commands.place.structure.failed"));
    private static final DynamicCommandExceptionType TEMPLATE_INVALID_EXCEPTION = new DynamicCommandExceptionType(id -> Text.stringifiedTranslatable("commands.place.template.invalid", id));
    private static final SimpleCommandExceptionType TEMPLATE_FAILED_EXCEPTION = new SimpleCommandExceptionType((Message)Text.translatable("commands.place.template.failed"));
    private static final SuggestionProvider<ServerCommandSource> SUGGESTION_PROVIDER = (context, builder) -> {
        StructureTemplateManager structureTemplateManager = ((ServerCommandSource)context.getSource()).getWorld().getStructureTemplateManager();
        return CommandSource.suggestIdentifiers(structureTemplateManager.streamTemplates(), builder);
    };

    public static void register(CommandDispatcher<ServerCommandSource> dispatcher) {
        dispatcher.register((LiteralArgumentBuilder)((LiteralArgumentBuilder)((LiteralArgumentBuilder)((LiteralArgumentBuilder)((LiteralArgumentBuilder)CommandManager.literal("place").requires(CommandManager.requirePermissionLevel(CommandManager.GAMEMASTERS_CHECK))).then(CommandManager.literal("feature").then(((RequiredArgumentBuilder)CommandManager.argument("feature", RegistryKeyArgumentType.registryKey(RegistryKeys.CONFIGURED_FEATURE)).executes(context -> PlaceCommand.executePlaceFeature((ServerCommandSource)context.getSource(), RegistryKeyArgumentType.getConfiguredFeatureEntry((CommandContext<ServerCommandSource>)context, "feature"), BlockPos.ofFloored(((ServerCommandSource)context.getSource()).getPosition())))).then(CommandManager.argument("pos", BlockPosArgumentType.blockPos()).executes(context -> PlaceCommand.executePlaceFeature((ServerCommandSource)context.getSource(), RegistryKeyArgumentType.getConfiguredFeatureEntry((CommandContext<ServerCommandSource>)context, "feature"), BlockPosArgumentType.getLoadedBlockPos((CommandContext<ServerCommandSource>)context, "pos"))))))).then(CommandManager.literal("jigsaw").then(CommandManager.argument("pool", RegistryKeyArgumentType.registryKey(RegistryKeys.TEMPLATE_POOL)).then(CommandManager.argument("target", IdentifierArgumentType.identifier()).then(((RequiredArgumentBuilder)CommandManager.argument("max_depth", IntegerArgumentType.integer((int)1, (int)20)).executes(context -> PlaceCommand.executePlaceJigsaw((ServerCommandSource)context.getSource(), RegistryKeyArgumentType.getStructurePoolEntry((CommandContext<ServerCommandSource>)context, "pool"), IdentifierArgumentType.getIdentifier((CommandContext<ServerCommandSource>)context, "target"), IntegerArgumentType.getInteger((CommandContext)context, (String)"max_depth"), BlockPos.ofFloored(((ServerCommandSource)context.getSource()).getPosition())))).then(CommandManager.argument("position", BlockPosArgumentType.blockPos()).executes(context -> PlaceCommand.executePlaceJigsaw((ServerCommandSource)context.getSource(), RegistryKeyArgumentType.getStructurePoolEntry((CommandContext<ServerCommandSource>)context, "pool"), IdentifierArgumentType.getIdentifier((CommandContext<ServerCommandSource>)context, "target"), IntegerArgumentType.getInteger((CommandContext)context, (String)"max_depth"), BlockPosArgumentType.getLoadedBlockPos((CommandContext<ServerCommandSource>)context, "position"))))))))).then(CommandManager.literal("structure").then(((RequiredArgumentBuilder)CommandManager.argument("structure", RegistryKeyArgumentType.registryKey(RegistryKeys.STRUCTURE)).executes(context -> PlaceCommand.executePlaceStructure((ServerCommandSource)context.getSource(), RegistryKeyArgumentType.getStructureEntry((CommandContext<ServerCommandSource>)context, "structure"), BlockPos.ofFloored(((ServerCommandSource)context.getSource()).getPosition())))).then(CommandManager.argument("pos", BlockPosArgumentType.blockPos()).executes(context -> PlaceCommand.executePlaceStructure((ServerCommandSource)context.getSource(), RegistryKeyArgumentType.getStructureEntry((CommandContext<ServerCommandSource>)context, "structure"), BlockPosArgumentType.getLoadedBlockPos((CommandContext<ServerCommandSource>)context, "pos"))))))).then(CommandManager.literal("template").then(((RequiredArgumentBuilder)CommandManager.argument("template", IdentifierArgumentType.identifier()).suggests(SUGGESTION_PROVIDER).executes(context -> PlaceCommand.executePlaceTemplate((ServerCommandSource)context.getSource(), IdentifierArgumentType.getIdentifier((CommandContext<ServerCommandSource>)context, "template"), BlockPos.ofFloored(((ServerCommandSource)context.getSource()).getPosition()), BlockRotation.NONE, BlockMirror.NONE, 1.0f, 0, false))).then(((RequiredArgumentBuilder)CommandManager.argument("pos", BlockPosArgumentType.blockPos()).executes(context -> PlaceCommand.executePlaceTemplate((ServerCommandSource)context.getSource(), IdentifierArgumentType.getIdentifier((CommandContext<ServerCommandSource>)context, "template"), BlockPosArgumentType.getLoadedBlockPos((CommandContext<ServerCommandSource>)context, "pos"), BlockRotation.NONE, BlockMirror.NONE, 1.0f, 0, false))).then(((RequiredArgumentBuilder)CommandManager.argument("rotation", BlockRotationArgumentType.blockRotation()).executes(context -> PlaceCommand.executePlaceTemplate((ServerCommandSource)context.getSource(), IdentifierArgumentType.getIdentifier((CommandContext<ServerCommandSource>)context, "template"), BlockPosArgumentType.getLoadedBlockPos((CommandContext<ServerCommandSource>)context, "pos"), BlockRotationArgumentType.getBlockRotation((CommandContext<ServerCommandSource>)context, "rotation"), BlockMirror.NONE, 1.0f, 0, false))).then(((RequiredArgumentBuilder)CommandManager.argument("mirror", BlockMirrorArgumentType.blockMirror()).executes(context -> PlaceCommand.executePlaceTemplate((ServerCommandSource)context.getSource(), IdentifierArgumentType.getIdentifier((CommandContext<ServerCommandSource>)context, "template"), BlockPosArgumentType.getLoadedBlockPos((CommandContext<ServerCommandSource>)context, "pos"), BlockRotationArgumentType.getBlockRotation((CommandContext<ServerCommandSource>)context, "rotation"), BlockMirrorArgumentType.getBlockMirror((CommandContext<ServerCommandSource>)context, "mirror"), 1.0f, 0, false))).then(((RequiredArgumentBuilder)CommandManager.argument("integrity", FloatArgumentType.floatArg((float)0.0f, (float)1.0f)).executes(context -> PlaceCommand.executePlaceTemplate((ServerCommandSource)context.getSource(), IdentifierArgumentType.getIdentifier((CommandContext<ServerCommandSource>)context, "template"), BlockPosArgumentType.getLoadedBlockPos((CommandContext<ServerCommandSource>)context, "pos"), BlockRotationArgumentType.getBlockRotation((CommandContext<ServerCommandSource>)context, "rotation"), BlockMirrorArgumentType.getBlockMirror((CommandContext<ServerCommandSource>)context, "mirror"), FloatArgumentType.getFloat((CommandContext)context, (String)"integrity"), 0, false))).then(((RequiredArgumentBuilder)CommandManager.argument("seed", IntegerArgumentType.integer()).executes(context -> PlaceCommand.executePlaceTemplate((ServerCommandSource)context.getSource(), IdentifierArgumentType.getIdentifier((CommandContext<ServerCommandSource>)context, "template"), BlockPosArgumentType.getLoadedBlockPos((CommandContext<ServerCommandSource>)context, "pos"), BlockRotationArgumentType.getBlockRotation((CommandContext<ServerCommandSource>)context, "rotation"), BlockMirrorArgumentType.getBlockMirror((CommandContext<ServerCommandSource>)context, "mirror"), FloatArgumentType.getFloat((CommandContext)context, (String)"integrity"), IntegerArgumentType.getInteger((CommandContext)context, (String)"seed"), false))).then(CommandManager.literal("strict").executes(context -> PlaceCommand.executePlaceTemplate((ServerCommandSource)context.getSource(), IdentifierArgumentType.getIdentifier((CommandContext<ServerCommandSource>)context, "template"), BlockPosArgumentType.getLoadedBlockPos((CommandContext<ServerCommandSource>)context, "pos"), BlockRotationArgumentType.getBlockRotation((CommandContext<ServerCommandSource>)context, "rotation"), BlockMirrorArgumentType.getBlockMirror((CommandContext<ServerCommandSource>)context, "mirror"), FloatArgumentType.getFloat((CommandContext)context, (String)"integrity"), IntegerArgumentType.getInteger((CommandContext)context, (String)"seed"), true)))))))))));
    }

    public static int executePlaceFeature(ServerCommandSource source, RegistryEntry.Reference<ConfiguredFeature<?, ?>> feature, BlockPos pos) throws CommandSyntaxException {
        ServerWorld serverWorld = source.getWorld();
        ConfiguredFeature<?, ?> configuredFeature = feature.value();
        ChunkPos chunkPos = new ChunkPos(pos);
        PlaceCommand.throwOnUnloadedPos(serverWorld, new ChunkPos(chunkPos.x - 1, chunkPos.z - 1), new ChunkPos(chunkPos.x + 1, chunkPos.z + 1));
        if (!configuredFeature.generate(serverWorld, serverWorld.getChunkManager().getChunkGenerator(), serverWorld.getRandom(), pos)) {
            throw FEATURE_FAILED_EXCEPTION.create();
        }
        String string = feature.registryKey().getValue().toString();
        source.sendFeedback(() -> Text.translatable("commands.place.feature.success", string, pos.getX(), pos.getY(), pos.getZ()), true);
        return 1;
    }

    public static int executePlaceJigsaw(ServerCommandSource source, RegistryEntry<StructurePool> structurePool, Identifier id, int maxDepth, BlockPos pos) throws CommandSyntaxException {
        ServerWorld serverWorld = source.getWorld();
        ChunkPos chunkPos = new ChunkPos(pos);
        PlaceCommand.throwOnUnloadedPos(serverWorld, chunkPos, chunkPos);
        if (!StructurePoolBasedGenerator.generate(serverWorld, structurePool, id, maxDepth, pos, false)) {
            throw JIGSAW_FAILED_EXCEPTION.create();
        }
        source.sendFeedback(() -> Text.translatable("commands.place.jigsaw.success", pos.getX(), pos.getY(), pos.getZ()), true);
        return 1;
    }

    public static int executePlaceStructure(ServerCommandSource source, RegistryEntry.Reference<Structure> structure, BlockPos pos) throws CommandSyntaxException {
        ServerWorld serverWorld = source.getWorld();
        Structure structure2 = structure.value();
        ChunkGenerator chunkGenerator = serverWorld.getChunkManager().getChunkGenerator();
        StructureStart structureStart = structure2.createStructureStart(structure, serverWorld.getRegistryKey(), source.getRegistryManager(), chunkGenerator, chunkGenerator.getBiomeSource(), serverWorld.getChunkManager().getNoiseConfig(), serverWorld.getStructureTemplateManager(), serverWorld.getSeed(), new ChunkPos(pos), 0, serverWorld, biome -> true);
        if (!structureStart.hasChildren()) {
            throw STRUCTURE_FAILED_EXCEPTION.create();
        }
        BlockBox blockBox = structureStart.getBoundingBox();
        ChunkPos chunkPos2 = new ChunkPos(ChunkSectionPos.getSectionCoord(blockBox.getMinX()), ChunkSectionPos.getSectionCoord(blockBox.getMinZ()));
        ChunkPos chunkPos22 = new ChunkPos(ChunkSectionPos.getSectionCoord(blockBox.getMaxX()), ChunkSectionPos.getSectionCoord(blockBox.getMaxZ()));
        PlaceCommand.throwOnUnloadedPos(serverWorld, chunkPos2, chunkPos22);
        ChunkPos.stream(chunkPos2, chunkPos22).forEach(chunkPos -> structureStart.place(serverWorld, serverWorld.getStructureAccessor(), chunkGenerator, serverWorld.getRandom(), new BlockBox(chunkPos.getStartX(), serverWorld.getBottomY(), chunkPos.getStartZ(), chunkPos.getEndX(), serverWorld.getTopYInclusive() + 1, chunkPos.getEndZ()), (ChunkPos)chunkPos));
        String string = structure.registryKey().getValue().toString();
        source.sendFeedback(() -> Text.translatable("commands.place.structure.success", string, pos.getX(), pos.getY(), pos.getZ()), true);
        return 1;
    }

    public static int executePlaceTemplate(ServerCommandSource source, Identifier id, BlockPos pos, BlockRotation rotation, BlockMirror mirror, float integrity, int seed, boolean strict) throws CommandSyntaxException {
        boolean bl;
        Optional<StructureTemplate> optional;
        ServerWorld serverWorld = source.getWorld();
        StructureTemplateManager structureTemplateManager = serverWorld.getStructureTemplateManager();
        try {
            optional = structureTemplateManager.getTemplate(id);
        }
        catch (InvalidIdentifierException invalidIdentifierException) {
            throw TEMPLATE_INVALID_EXCEPTION.create((Object)id);
        }
        if (optional.isEmpty()) {
            throw TEMPLATE_INVALID_EXCEPTION.create((Object)id);
        }
        StructureTemplate structureTemplate = optional.get();
        PlaceCommand.throwOnUnloadedPos(serverWorld, new ChunkPos(pos), new ChunkPos(pos.add(structureTemplate.getSize())));
        StructurePlacementData structurePlacementData = new StructurePlacementData().setMirror(mirror).setRotation(rotation).setUpdateNeighbors(strict);
        if (integrity < 1.0f) {
            structurePlacementData.clearProcessors().addProcessor(new BlockRotStructureProcessor(integrity)).setRandom(StructureBlockBlockEntity.createRandom(seed));
        }
        if (!(bl = structureTemplate.place(serverWorld, pos, pos, structurePlacementData, StructureBlockBlockEntity.createRandom(seed), 2 | (strict ? 816 : 0)))) {
            throw TEMPLATE_FAILED_EXCEPTION.create();
        }
        source.sendFeedback(() -> Text.translatable("commands.place.template.success", Text.of(id), pos.getX(), pos.getY(), pos.getZ()), true);
        return 1;
    }

    private static void throwOnUnloadedPos(ServerWorld world, ChunkPos pos1, ChunkPos pos2) throws CommandSyntaxException {
        if (ChunkPos.stream(pos1, pos2).filter(pos -> !world.isPosLoaded(pos.getStartPos())).findAny().isPresent()) {
            throw BlockPosArgumentType.UNLOADED_EXCEPTION.create();
        }
    }
}


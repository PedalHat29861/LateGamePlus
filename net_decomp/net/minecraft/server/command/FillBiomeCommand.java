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
 *  com.mojang.brigadier.exceptions.Dynamic2CommandExceptionType
 *  com.mojang.brigadier.exceptions.SimpleCommandExceptionType
 *  com.mojang.datafixers.util.Either
 *  org.apache.commons.lang3.mutable.MutableInt
 */
package net.minecraft.server.command;

import com.mojang.brigadier.CommandDispatcher;
import com.mojang.brigadier.Message;
import com.mojang.brigadier.builder.LiteralArgumentBuilder;
import com.mojang.brigadier.builder.RequiredArgumentBuilder;
import com.mojang.brigadier.context.CommandContext;
import com.mojang.brigadier.exceptions.CommandSyntaxException;
import com.mojang.brigadier.exceptions.Dynamic2CommandExceptionType;
import com.mojang.brigadier.exceptions.SimpleCommandExceptionType;
import com.mojang.datafixers.util.Either;
import java.util.ArrayList;
import java.util.Optional;
import java.util.function.Consumer;
import java.util.function.Predicate;
import java.util.function.Supplier;
import net.minecraft.command.CommandRegistryAccess;
import net.minecraft.command.argument.BlockPosArgumentType;
import net.minecraft.command.argument.RegistryEntryPredicateArgumentType;
import net.minecraft.command.argument.RegistryEntryReferenceArgumentType;
import net.minecraft.registry.RegistryKeys;
import net.minecraft.registry.entry.RegistryEntry;
import net.minecraft.server.command.CommandManager;
import net.minecraft.server.command.ServerCommandSource;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.text.Text;
import net.minecraft.util.math.BlockBox;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.ChunkSectionPos;
import net.minecraft.world.biome.Biome;
import net.minecraft.world.biome.source.BiomeCoords;
import net.minecraft.world.biome.source.BiomeSupplier;
import net.minecraft.world.chunk.Chunk;
import net.minecraft.world.chunk.ChunkStatus;
import net.minecraft.world.rule.GameRules;
import org.apache.commons.lang3.mutable.MutableInt;

public class FillBiomeCommand {
    public static final SimpleCommandExceptionType UNLOADED_EXCEPTION = new SimpleCommandExceptionType((Message)Text.translatable("argument.pos.unloaded"));
    private static final Dynamic2CommandExceptionType TOO_BIG_EXCEPTION = new Dynamic2CommandExceptionType((maximum, specified) -> Text.stringifiedTranslatable("commands.fillbiome.toobig", maximum, specified));

    public static void register(CommandDispatcher<ServerCommandSource> dispatcher, CommandRegistryAccess commandRegistryAccess) {
        dispatcher.register((LiteralArgumentBuilder)((LiteralArgumentBuilder)CommandManager.literal("fillbiome").requires(CommandManager.requirePermissionLevel(CommandManager.GAMEMASTERS_CHECK))).then(CommandManager.argument("from", BlockPosArgumentType.blockPos()).then(CommandManager.argument("to", BlockPosArgumentType.blockPos()).then(((RequiredArgumentBuilder)CommandManager.argument("biome", RegistryEntryReferenceArgumentType.registryEntry(commandRegistryAccess, RegistryKeys.BIOME)).executes(context -> FillBiomeCommand.execute((ServerCommandSource)context.getSource(), BlockPosArgumentType.getLoadedBlockPos((CommandContext<ServerCommandSource>)context, "from"), BlockPosArgumentType.getLoadedBlockPos((CommandContext<ServerCommandSource>)context, "to"), RegistryEntryReferenceArgumentType.getRegistryEntry((CommandContext<ServerCommandSource>)context, "biome", RegistryKeys.BIOME), registryEntry -> true))).then(CommandManager.literal("replace").then(CommandManager.argument("filter", RegistryEntryPredicateArgumentType.registryEntryPredicate(commandRegistryAccess, RegistryKeys.BIOME)).executes(context -> FillBiomeCommand.execute((ServerCommandSource)context.getSource(), BlockPosArgumentType.getLoadedBlockPos((CommandContext<ServerCommandSource>)context, "from"), BlockPosArgumentType.getLoadedBlockPos((CommandContext<ServerCommandSource>)context, "to"), RegistryEntryReferenceArgumentType.getRegistryEntry((CommandContext<ServerCommandSource>)context, "biome", RegistryKeys.BIOME), RegistryEntryPredicateArgumentType.getRegistryEntryPredicate((CommandContext<ServerCommandSource>)context, "filter", RegistryKeys.BIOME)))))))));
    }

    private static int convertCoordinate(int coordinate) {
        return BiomeCoords.toBlock(BiomeCoords.fromBlock(coordinate));
    }

    private static BlockPos convertPos(BlockPos pos) {
        return new BlockPos(FillBiomeCommand.convertCoordinate(pos.getX()), FillBiomeCommand.convertCoordinate(pos.getY()), FillBiomeCommand.convertCoordinate(pos.getZ()));
    }

    private static BiomeSupplier createBiomeSupplier(MutableInt counter, Chunk chunk, BlockBox box, RegistryEntry<Biome> biome, Predicate<RegistryEntry<Biome>> filter) {
        return (x, y, z, noise) -> {
            int i = BiomeCoords.toBlock(x);
            int j = BiomeCoords.toBlock(y);
            int k = BiomeCoords.toBlock(z);
            RegistryEntry<Biome> registryEntry2 = chunk.getBiomeForNoiseGen(x, y, z);
            if (box.contains(i, j, k) && filter.test(registryEntry2)) {
                counter.increment();
                return biome;
            }
            return registryEntry2;
        };
    }

    public static Either<Integer, CommandSyntaxException> fillBiome(ServerWorld world, BlockPos from, BlockPos to, RegistryEntry<Biome> biome) {
        return FillBiomeCommand.fillBiome(world, from, to, biome, biomex -> true, feedbackSupplier -> {});
    }

    public static Either<Integer, CommandSyntaxException> fillBiome(ServerWorld world, BlockPos from, BlockPos to, RegistryEntry<Biome> biome, Predicate<RegistryEntry<Biome>> filter, Consumer<Supplier<Text>> feedbackConsumer) {
        int j;
        BlockPos blockPos2;
        BlockPos blockPos = FillBiomeCommand.convertPos(from);
        BlockBox blockBox = BlockBox.create(blockPos, blockPos2 = FillBiomeCommand.convertPos(to));
        int i = blockBox.getBlockCountX() * blockBox.getBlockCountY() * blockBox.getBlockCountZ();
        if (i > (j = world.getGameRules().getValue(GameRules.MAX_BLOCK_MODIFICATIONS).intValue())) {
            return Either.right((Object)((Object)TOO_BIG_EXCEPTION.create((Object)j, (Object)i)));
        }
        ArrayList<Chunk> list = new ArrayList<Chunk>();
        for (int k = ChunkSectionPos.getSectionCoord(blockBox.getMinZ()); k <= ChunkSectionPos.getSectionCoord(blockBox.getMaxZ()); ++k) {
            for (int l = ChunkSectionPos.getSectionCoord(blockBox.getMinX()); l <= ChunkSectionPos.getSectionCoord(blockBox.getMaxX()); ++l) {
                Chunk chunk = world.getChunk(l, k, ChunkStatus.FULL, false);
                if (chunk == null) {
                    return Either.right((Object)((Object)UNLOADED_EXCEPTION.create()));
                }
                list.add(chunk);
            }
        }
        MutableInt mutableInt = new MutableInt(0);
        for (Chunk chunk : list) {
            chunk.populateBiomes(FillBiomeCommand.createBiomeSupplier(mutableInt, chunk, blockBox, biome, filter), world.getChunkManager().getNoiseConfig().getMultiNoiseSampler());
            chunk.markNeedsSaving();
        }
        world.getChunkManager().chunkLoadingManager.sendChunkBiomePackets(list);
        feedbackConsumer.accept(() -> Text.translatable("commands.fillbiome.success.count", mutableInt.intValue(), blockBox.getMinX(), blockBox.getMinY(), blockBox.getMinZ(), blockBox.getMaxX(), blockBox.getMaxY(), blockBox.getMaxZ()));
        return Either.left((Object)mutableInt.intValue());
    }

    private static int execute(ServerCommandSource source, BlockPos from, BlockPos to, RegistryEntry.Reference<Biome> biome, Predicate<RegistryEntry<Biome>> filter) throws CommandSyntaxException {
        Either<Integer, CommandSyntaxException> either = FillBiomeCommand.fillBiome(source.getWorld(), from, to, biome, filter, feedbackSupplier -> source.sendFeedback((Supplier<Text>)feedbackSupplier, true));
        Optional optional = either.right();
        if (optional.isPresent()) {
            throw (CommandSyntaxException)((Object)optional.get());
        }
        return (Integer)either.left().get();
    }
}


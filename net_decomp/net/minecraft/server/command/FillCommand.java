/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  com.google.common.collect.Lists
 *  com.mojang.brigadier.CommandDispatcher
 *  com.mojang.brigadier.Message
 *  com.mojang.brigadier.builder.ArgumentBuilder
 *  com.mojang.brigadier.builder.LiteralArgumentBuilder
 *  com.mojang.brigadier.context.CommandContext
 *  com.mojang.brigadier.exceptions.CommandSyntaxException
 *  com.mojang.brigadier.exceptions.Dynamic2CommandExceptionType
 *  com.mojang.brigadier.exceptions.SimpleCommandExceptionType
 *  org.jspecify.annotations.Nullable
 */
package net.minecraft.server.command;

import com.google.common.collect.Lists;
import com.mojang.brigadier.CommandDispatcher;
import com.mojang.brigadier.Message;
import com.mojang.brigadier.builder.ArgumentBuilder;
import com.mojang.brigadier.builder.LiteralArgumentBuilder;
import com.mojang.brigadier.context.CommandContext;
import com.mojang.brigadier.exceptions.CommandSyntaxException;
import com.mojang.brigadier.exceptions.Dynamic2CommandExceptionType;
import com.mojang.brigadier.exceptions.SimpleCommandExceptionType;
import java.lang.invoke.MethodHandle;
import java.lang.runtime.ObjectMethods;
import java.util.ArrayList;
import java.util.Collections;
import java.util.function.Predicate;
import net.minecraft.block.BlockState;
import net.minecraft.block.Blocks;
import net.minecraft.block.pattern.CachedBlockPosition;
import net.minecraft.command.ArgumentGetter;
import net.minecraft.command.CommandRegistryAccess;
import net.minecraft.command.argument.BlockPosArgumentType;
import net.minecraft.command.argument.BlockPredicateArgumentType;
import net.minecraft.command.argument.BlockStateArgument;
import net.minecraft.command.argument.BlockStateArgumentType;
import net.minecraft.server.command.CommandManager;
import net.minecraft.server.command.ServerCommandSource;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.text.Text;
import net.minecraft.util.math.BlockBox;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Vec3i;
import net.minecraft.world.rule.GameRules;
import org.jspecify.annotations.Nullable;

public class FillCommand {
    private static final Dynamic2CommandExceptionType TOO_BIG_EXCEPTION = new Dynamic2CommandExceptionType((maxCount, count) -> Text.stringifiedTranslatable("commands.fill.toobig", maxCount, count));
    static final BlockStateArgument AIR_BLOCK_ARGUMENT = new BlockStateArgument(Blocks.AIR.getDefaultState(), Collections.emptySet(), null);
    private static final SimpleCommandExceptionType FAILED_EXCEPTION = new SimpleCommandExceptionType((Message)Text.translatable("commands.fill.failed"));

    public static void register(CommandDispatcher<ServerCommandSource> dispatcher, CommandRegistryAccess commandRegistryAccess) {
        dispatcher.register((LiteralArgumentBuilder)((LiteralArgumentBuilder)CommandManager.literal("fill").requires(CommandManager.requirePermissionLevel(CommandManager.GAMEMASTERS_CHECK))).then(CommandManager.argument("from", BlockPosArgumentType.blockPos()).then(CommandManager.argument("to", BlockPosArgumentType.blockPos()).then(FillCommand.buildModeTree(commandRegistryAccess, CommandManager.argument("block", BlockStateArgumentType.blockState(commandRegistryAccess)), context -> BlockPosArgumentType.getLoadedBlockPos((CommandContext<ServerCommandSource>)context, "from"), context -> BlockPosArgumentType.getLoadedBlockPos((CommandContext<ServerCommandSource>)context, "to"), context -> BlockStateArgumentType.getBlockState((CommandContext<ServerCommandSource>)context, "block"), context -> null).then(((LiteralArgumentBuilder)CommandManager.literal("replace").executes(context -> FillCommand.execute((ServerCommandSource)context.getSource(), BlockBox.create(BlockPosArgumentType.getLoadedBlockPos((CommandContext<ServerCommandSource>)context, "from"), BlockPosArgumentType.getLoadedBlockPos((CommandContext<ServerCommandSource>)context, "to")), BlockStateArgumentType.getBlockState((CommandContext<ServerCommandSource>)context, "block"), Mode.REPLACE, null, false))).then(FillCommand.buildModeTree(commandRegistryAccess, CommandManager.argument("filter", BlockPredicateArgumentType.blockPredicate(commandRegistryAccess)), context -> BlockPosArgumentType.getLoadedBlockPos((CommandContext<ServerCommandSource>)context, "from"), context -> BlockPosArgumentType.getLoadedBlockPos((CommandContext<ServerCommandSource>)context, "to"), context -> BlockStateArgumentType.getBlockState((CommandContext<ServerCommandSource>)context, "block"), context -> BlockPredicateArgumentType.getBlockPredicate((CommandContext<ServerCommandSource>)context, "filter")))).then(CommandManager.literal("keep").executes(context -> FillCommand.execute((ServerCommandSource)context.getSource(), BlockBox.create(BlockPosArgumentType.getLoadedBlockPos((CommandContext<ServerCommandSource>)context, "from"), BlockPosArgumentType.getLoadedBlockPos((CommandContext<ServerCommandSource>)context, "to")), BlockStateArgumentType.getBlockState((CommandContext<ServerCommandSource>)context, "block"), Mode.REPLACE, pos -> pos.getWorld().isAir(pos.getBlockPos()), false)))))));
    }

    private static ArgumentBuilder<ServerCommandSource, ?> buildModeTree(CommandRegistryAccess registries, ArgumentBuilder<ServerCommandSource, ?> argumentBuilder, ArgumentGetter<CommandContext<ServerCommandSource>, BlockPos> from, ArgumentGetter<CommandContext<ServerCommandSource>, BlockPos> to, ArgumentGetter<CommandContext<ServerCommandSource>, BlockStateArgument> state, OptionalArgumentResolver<CommandContext<ServerCommandSource>, Predicate<CachedBlockPosition>> filter) {
        return argumentBuilder.executes(context -> FillCommand.execute((ServerCommandSource)context.getSource(), BlockBox.create((Vec3i)from.apply(context), (Vec3i)to.apply(context)), (BlockStateArgument)state.apply(context), Mode.REPLACE, (Predicate)filter.apply(context), false)).then(CommandManager.literal("outline").executes(context -> FillCommand.execute((ServerCommandSource)context.getSource(), BlockBox.create((Vec3i)from.apply(context), (Vec3i)to.apply(context)), (BlockStateArgument)state.apply(context), Mode.OUTLINE, (Predicate)filter.apply(context), false))).then(CommandManager.literal("hollow").executes(context -> FillCommand.execute((ServerCommandSource)context.getSource(), BlockBox.create((Vec3i)from.apply(context), (Vec3i)to.apply(context)), (BlockStateArgument)state.apply(context), Mode.HOLLOW, (Predicate)filter.apply(context), false))).then(CommandManager.literal("destroy").executes(context -> FillCommand.execute((ServerCommandSource)context.getSource(), BlockBox.create((Vec3i)from.apply(context), (Vec3i)to.apply(context)), (BlockStateArgument)state.apply(context), Mode.DESTROY, (Predicate)filter.apply(context), false))).then(CommandManager.literal("strict").executes(context -> FillCommand.execute((ServerCommandSource)context.getSource(), BlockBox.create((Vec3i)from.apply(context), (Vec3i)to.apply(context)), (BlockStateArgument)state.apply(context), Mode.REPLACE, (Predicate)filter.apply(context), true)));
    }

    private static int execute(ServerCommandSource source, BlockBox range, BlockStateArgument block, Mode mode, @Nullable Predicate<CachedBlockPosition> filter, boolean strict) throws CommandSyntaxException {
        final class Replaced
        extends Record {
            final BlockPos pos;
            final BlockState oldState;

            Replaced(BlockPos pos, BlockState oldState) {
                this.pos = pos;
                this.oldState = oldState;
            }

            @Override
            public final String toString() {
                return ObjectMethods.bootstrap("toString", new MethodHandle[]{Replaced.class, "pos;oldState", "pos", "oldState"}, this);
            }

            @Override
            public final int hashCode() {
                return (int)ObjectMethods.bootstrap("hashCode", new MethodHandle[]{Replaced.class, "pos;oldState", "pos", "oldState"}, this);
            }

            @Override
            public final boolean equals(Object object) {
                return (boolean)ObjectMethods.bootstrap("equals", new MethodHandle[]{Replaced.class, "pos;oldState", "pos", "oldState"}, this, object);
            }

            public BlockPos pos() {
                return this.pos;
            }

            public BlockState oldState() {
                return this.oldState;
            }
        }
        int j;
        int i = range.getBlockCountX() * range.getBlockCountY() * range.getBlockCountZ();
        if (i > (j = source.getWorld().getGameRules().getValue(GameRules.MAX_BLOCK_MODIFICATIONS).intValue())) {
            throw TOO_BIG_EXCEPTION.create((Object)j, (Object)i);
        }
        ArrayList list = Lists.newArrayList();
        ServerWorld serverWorld = source.getWorld();
        if (serverWorld.isDebugWorld()) {
            throw FAILED_EXCEPTION.create();
        }
        int k = 0;
        for (BlockPos blockPos : BlockPos.iterate(range.getMinX(), range.getMinY(), range.getMinZ(), range.getMaxX(), range.getMaxY(), range.getMaxZ())) {
            BlockStateArgument blockStateArgument;
            if (filter != null && !filter.test(new CachedBlockPosition(serverWorld, blockPos, true))) continue;
            BlockState blockState = serverWorld.getBlockState(blockPos);
            boolean bl = false;
            if (mode.postProcessor.affect(serverWorld, blockPos)) {
                bl = true;
            }
            if ((blockStateArgument = mode.filter.filter(range, blockPos, block, serverWorld)) == null) {
                if (!bl) continue;
                ++k;
                continue;
            }
            if (!blockStateArgument.setBlockState(serverWorld, blockPos, 2 | (strict ? 816 : 256))) {
                if (!bl) continue;
                ++k;
                continue;
            }
            if (!strict) {
                list.add(new Replaced(blockPos.toImmutable(), blockState));
            }
            ++k;
        }
        for (Replaced replaced : list) {
            serverWorld.onStateReplacedWithCommands(replaced.pos, replaced.oldState);
        }
        if (k == 0) {
            throw FAILED_EXCEPTION.create();
        }
        int l = k;
        source.sendFeedback(() -> Text.translatable("commands.fill.success", l), true);
        return k;
    }

    @FunctionalInterface
    static interface OptionalArgumentResolver<T, R> {
        public @Nullable R apply(T var1) throws CommandSyntaxException;
    }

    static final class Mode
    extends Enum<Mode> {
        public static final /* enum */ Mode REPLACE = new Mode(PostProcessor.EMPTY, Filter.IDENTITY);
        public static final /* enum */ Mode OUTLINE = new Mode(PostProcessor.EMPTY, (range, pos, block, world) -> {
            if (pos.getX() == range.getMinX() || pos.getX() == range.getMaxX() || pos.getY() == range.getMinY() || pos.getY() == range.getMaxY() || pos.getZ() == range.getMinZ() || pos.getZ() == range.getMaxZ()) {
                return block;
            }
            return null;
        });
        public static final /* enum */ Mode HOLLOW = new Mode(PostProcessor.EMPTY, (range, pos, block, world) -> {
            if (pos.getX() == range.getMinX() || pos.getX() == range.getMaxX() || pos.getY() == range.getMinY() || pos.getY() == range.getMaxY() || pos.getZ() == range.getMinZ() || pos.getZ() == range.getMaxZ()) {
                return block;
            }
            return AIR_BLOCK_ARGUMENT;
        });
        public static final /* enum */ Mode DESTROY = new Mode((world, pos) -> world.breakBlock(pos, true), Filter.IDENTITY);
        public final Filter filter;
        public final PostProcessor postProcessor;
        private static final /* synthetic */ Mode[] field_13653;

        public static Mode[] values() {
            return (Mode[])field_13653.clone();
        }

        public static Mode valueOf(String string) {
            return Enum.valueOf(Mode.class, string);
        }

        private Mode(PostProcessor postProcessor, Filter filter) {
            this.postProcessor = postProcessor;
            this.filter = filter;
        }

        private static /* synthetic */ Mode[] method_36968() {
            return new Mode[]{REPLACE, OUTLINE, HOLLOW, DESTROY};
        }

        static {
            field_13653 = Mode.method_36968();
        }
    }

    @FunctionalInterface
    public static interface PostProcessor {
        public static final PostProcessor EMPTY = (world, pos) -> false;

        public boolean affect(ServerWorld var1, BlockPos var2);
    }

    @FunctionalInterface
    public static interface Filter {
        public static final Filter IDENTITY = (box, pos, block, world) -> block;

        public @Nullable BlockStateArgument filter(BlockBox var1, BlockPos var2, BlockStateArgument var3, ServerWorld var4);
    }
}


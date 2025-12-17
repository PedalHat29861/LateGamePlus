/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  com.google.common.collect.Lists
 *  com.mojang.brigadier.CommandDispatcher
 *  com.mojang.brigadier.Message
 *  com.mojang.brigadier.builder.ArgumentBuilder
 *  com.mojang.brigadier.builder.LiteralArgumentBuilder
 *  com.mojang.brigadier.builder.RequiredArgumentBuilder
 *  com.mojang.brigadier.context.CommandContext
 *  com.mojang.brigadier.exceptions.CommandSyntaxException
 *  com.mojang.brigadier.exceptions.Dynamic2CommandExceptionType
 *  com.mojang.brigadier.exceptions.SimpleCommandExceptionType
 *  com.mojang.logging.LogUtils
 *  org.jspecify.annotations.Nullable
 *  org.slf4j.Logger
 */
package net.minecraft.server.command;

import com.google.common.collect.Lists;
import com.mojang.brigadier.CommandDispatcher;
import com.mojang.brigadier.Message;
import com.mojang.brigadier.builder.ArgumentBuilder;
import com.mojang.brigadier.builder.LiteralArgumentBuilder;
import com.mojang.brigadier.builder.RequiredArgumentBuilder;
import com.mojang.brigadier.context.CommandContext;
import com.mojang.brigadier.exceptions.CommandSyntaxException;
import com.mojang.brigadier.exceptions.Dynamic2CommandExceptionType;
import com.mojang.brigadier.exceptions.SimpleCommandExceptionType;
import com.mojang.logging.LogUtils;
import java.lang.invoke.MethodHandle;
import java.lang.runtime.ObjectMethods;
import java.util.ArrayList;
import java.util.LinkedList;
import java.util.List;
import java.util.function.Predicate;
import net.minecraft.block.BlockState;
import net.minecraft.block.Blocks;
import net.minecraft.block.entity.BlockEntity;
import net.minecraft.block.pattern.CachedBlockPosition;
import net.minecraft.command.ArgumentGetter;
import net.minecraft.command.CommandRegistryAccess;
import net.minecraft.command.argument.BlockPosArgumentType;
import net.minecraft.command.argument.BlockPredicateArgumentType;
import net.minecraft.command.argument.DimensionArgumentType;
import net.minecraft.component.ComponentMap;
import net.minecraft.nbt.NbtCompound;
import net.minecraft.server.command.CommandManager;
import net.minecraft.server.command.ServerCommandSource;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.storage.NbtReadView;
import net.minecraft.storage.NbtWriteView;
import net.minecraft.text.Text;
import net.minecraft.util.ErrorReporter;
import net.minecraft.util.math.BlockBox;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.rule.GameRules;
import net.minecraft.world.tick.WorldTickScheduler;
import org.jspecify.annotations.Nullable;
import org.slf4j.Logger;

public class CloneCommand {
    private static final Logger LOGGER = LogUtils.getLogger();
    private static final SimpleCommandExceptionType OVERLAP_EXCEPTION = new SimpleCommandExceptionType((Message)Text.translatable("commands.clone.overlap"));
    private static final Dynamic2CommandExceptionType TOO_BIG_EXCEPTION = new Dynamic2CommandExceptionType((maxCount, count) -> Text.stringifiedTranslatable("commands.clone.toobig", maxCount, count));
    private static final SimpleCommandExceptionType FAILED_EXCEPTION = new SimpleCommandExceptionType((Message)Text.translatable("commands.clone.failed"));
    public static final Predicate<CachedBlockPosition> IS_AIR_PREDICATE = pos -> !pos.getBlockState().isAir();

    public static void register(CommandDispatcher<ServerCommandSource> dispatcher, CommandRegistryAccess commandRegistryAccess) {
        dispatcher.register((LiteralArgumentBuilder)((LiteralArgumentBuilder)((LiteralArgumentBuilder)CommandManager.literal("clone").requires(CommandManager.requirePermissionLevel(CommandManager.GAMEMASTERS_CHECK))).then(CloneCommand.createSourceArgs(commandRegistryAccess, context -> ((ServerCommandSource)context.getSource()).getWorld()))).then(CommandManager.literal("from").then(CommandManager.argument("sourceDimension", DimensionArgumentType.dimension()).then(CloneCommand.createSourceArgs(commandRegistryAccess, context -> DimensionArgumentType.getDimensionArgument((CommandContext<ServerCommandSource>)context, "sourceDimension"))))));
    }

    private static ArgumentBuilder<ServerCommandSource, ?> createSourceArgs(CommandRegistryAccess commandRegistryAccess, ArgumentGetter<CommandContext<ServerCommandSource>, ServerWorld> worldGetter) {
        return CommandManager.argument("begin", BlockPosArgumentType.blockPos()).then(((RequiredArgumentBuilder)CommandManager.argument("end", BlockPosArgumentType.blockPos()).then(CloneCommand.createDestinationArgs(commandRegistryAccess, worldGetter, context -> ((ServerCommandSource)context.getSource()).getWorld()))).then(CommandManager.literal("to").then(CommandManager.argument("targetDimension", DimensionArgumentType.dimension()).then(CloneCommand.createDestinationArgs(commandRegistryAccess, worldGetter, context -> DimensionArgumentType.getDimensionArgument((CommandContext<ServerCommandSource>)context, "targetDimension"))))));
    }

    private static DimensionalPos createDimensionalPos(CommandContext<ServerCommandSource> context, ServerWorld world, String name) throws CommandSyntaxException {
        BlockPos blockPos = BlockPosArgumentType.getLoadedBlockPos(context, world, name);
        return new DimensionalPos(world, blockPos);
    }

    private static ArgumentBuilder<ServerCommandSource, ?> createDestinationArgs(CommandRegistryAccess registries, ArgumentGetter<CommandContext<ServerCommandSource>, ServerWorld> currentWorldGetter, ArgumentGetter<CommandContext<ServerCommandSource>, ServerWorld> targetWorldGetter) {
        ArgumentGetter<CommandContext<ServerCommandSource>, DimensionalPos> argumentGetter = context -> CloneCommand.createDimensionalPos((CommandContext<ServerCommandSource>)context, (ServerWorld)currentWorldGetter.apply((CommandContext<ServerCommandSource>)context), "begin");
        ArgumentGetter<CommandContext<ServerCommandSource>, DimensionalPos> argumentGetter2 = context -> CloneCommand.createDimensionalPos((CommandContext<ServerCommandSource>)context, (ServerWorld)currentWorldGetter.apply((CommandContext<ServerCommandSource>)context), "end");
        ArgumentGetter<CommandContext<ServerCommandSource>, DimensionalPos> argumentGetter3 = context -> CloneCommand.createDimensionalPos((CommandContext<ServerCommandSource>)context, (ServerWorld)targetWorldGetter.apply((CommandContext<ServerCommandSource>)context), "destination");
        return CloneCommand.appendMode(registries, argumentGetter, argumentGetter2, argumentGetter3, false, CommandManager.argument("destination", BlockPosArgumentType.blockPos())).then(CloneCommand.appendMode(registries, argumentGetter, argumentGetter2, argumentGetter3, true, CommandManager.literal("strict")));
    }

    private static ArgumentBuilder<ServerCommandSource, ?> appendMode(CommandRegistryAccess registries, ArgumentGetter<CommandContext<ServerCommandSource>, DimensionalPos> beginPosGetter, ArgumentGetter<CommandContext<ServerCommandSource>, DimensionalPos> endPosGetter, ArgumentGetter<CommandContext<ServerCommandSource>, DimensionalPos> destinationPosGetter, boolean strict, ArgumentBuilder<ServerCommandSource, ?> builder) {
        return builder.executes(context -> CloneCommand.execute((ServerCommandSource)context.getSource(), (DimensionalPos)beginPosGetter.apply(context), (DimensionalPos)endPosGetter.apply(context), (DimensionalPos)destinationPosGetter.apply(context), pos -> true, Mode.NORMAL, strict)).then(CloneCommand.createModeArgs(beginPosGetter, endPosGetter, destinationPosGetter, context -> pos -> true, strict, CommandManager.literal("replace"))).then(CloneCommand.createModeArgs(beginPosGetter, endPosGetter, destinationPosGetter, context -> IS_AIR_PREDICATE, strict, CommandManager.literal("masked"))).then(CommandManager.literal("filtered").then(CloneCommand.createModeArgs(beginPosGetter, endPosGetter, destinationPosGetter, context -> BlockPredicateArgumentType.getBlockPredicate((CommandContext<ServerCommandSource>)context, "filter"), strict, CommandManager.argument("filter", BlockPredicateArgumentType.blockPredicate(registries)))));
    }

    private static ArgumentBuilder<ServerCommandSource, ?> createModeArgs(ArgumentGetter<CommandContext<ServerCommandSource>, DimensionalPos> beginPosGetter, ArgumentGetter<CommandContext<ServerCommandSource>, DimensionalPos> endPosGetter, ArgumentGetter<CommandContext<ServerCommandSource>, DimensionalPos> destinationPosGetter, ArgumentGetter<CommandContext<ServerCommandSource>, Predicate<CachedBlockPosition>> filterGetter, boolean strict, ArgumentBuilder<ServerCommandSource, ?> builder) {
        return builder.executes(context -> CloneCommand.execute((ServerCommandSource)context.getSource(), (DimensionalPos)beginPosGetter.apply(context), (DimensionalPos)endPosGetter.apply(context), (DimensionalPos)destinationPosGetter.apply(context), (Predicate)filterGetter.apply(context), Mode.NORMAL, strict)).then(CommandManager.literal("force").executes(context -> CloneCommand.execute((ServerCommandSource)context.getSource(), (DimensionalPos)beginPosGetter.apply(context), (DimensionalPos)endPosGetter.apply(context), (DimensionalPos)destinationPosGetter.apply(context), (Predicate)filterGetter.apply(context), Mode.FORCE, strict))).then(CommandManager.literal("move").executes(context -> CloneCommand.execute((ServerCommandSource)context.getSource(), (DimensionalPos)beginPosGetter.apply(context), (DimensionalPos)endPosGetter.apply(context), (DimensionalPos)destinationPosGetter.apply(context), (Predicate)filterGetter.apply(context), Mode.MOVE, strict))).then(CommandManager.literal("normal").executes(context -> CloneCommand.execute((ServerCommandSource)context.getSource(), (DimensionalPos)beginPosGetter.apply(context), (DimensionalPos)endPosGetter.apply(context), (DimensionalPos)destinationPosGetter.apply(context), (Predicate)filterGetter.apply(context), Mode.NORMAL, strict)));
    }

    private static int execute(ServerCommandSource source, DimensionalPos begin, DimensionalPos end, DimensionalPos destination, Predicate<CachedBlockPosition> filter, Mode mode, boolean strict) throws CommandSyntaxException {
        int j;
        BlockPos blockPos = begin.position();
        BlockPos blockPos2 = end.position();
        BlockBox blockBox = BlockBox.create(blockPos, blockPos2);
        BlockPos blockPos3 = destination.position();
        BlockPos blockPos4 = blockPos3.add(blockBox.getDimensions());
        BlockBox blockBox2 = BlockBox.create(blockPos3, blockPos4);
        ServerWorld serverWorld = begin.dimension();
        ServerWorld serverWorld2 = destination.dimension();
        if (!mode.allowsOverlap() && serverWorld == serverWorld2 && blockBox2.intersects(blockBox)) {
            throw OVERLAP_EXCEPTION.create();
        }
        int i = blockBox.getBlockCountX() * blockBox.getBlockCountY() * blockBox.getBlockCountZ();
        if (i > (j = source.getWorld().getGameRules().getValue(GameRules.MAX_BLOCK_MODIFICATIONS).intValue())) {
            throw TOO_BIG_EXCEPTION.create((Object)j, (Object)i);
        }
        if (!serverWorld.isRegionLoaded(blockPos, blockPos2) || !serverWorld2.isRegionLoaded(blockPos3, blockPos4)) {
            throw BlockPosArgumentType.UNLOADED_EXCEPTION.create();
        }
        if (serverWorld2.isDebugWorld()) {
            throw FAILED_EXCEPTION.create();
        }
        ArrayList list = Lists.newArrayList();
        ArrayList list2 = Lists.newArrayList();
        ArrayList list3 = Lists.newArrayList();
        LinkedList deque = Lists.newLinkedList();
        int k = 0;
        try (ErrorReporter.Logging logging = new ErrorReporter.Logging(LOGGER);){
            int m;
            int l;
            BlockPos blockPos5 = new BlockPos(blockBox2.getMinX() - blockBox.getMinX(), blockBox2.getMinY() - blockBox.getMinY(), blockBox2.getMinZ() - blockBox.getMinZ());
            for (l = blockBox.getMinZ(); l <= blockBox.getMaxZ(); ++l) {
                for (m = blockBox.getMinY(); m <= blockBox.getMaxY(); ++m) {
                    for (int n = blockBox.getMinX(); n <= blockBox.getMaxX(); ++n) {
                        BlockPos blockPos6 = new BlockPos(n, m, l);
                        BlockPos blockPos7 = blockPos6.add(blockPos5);
                        CachedBlockPosition cachedBlockPosition = new CachedBlockPosition(serverWorld, blockPos6, false);
                        BlockState blockState = cachedBlockPosition.getBlockState();
                        if (!filter.test(cachedBlockPosition)) continue;
                        BlockEntity blockEntity = serverWorld.getBlockEntity(blockPos6);
                        if (blockEntity != null) {
                            NbtWriteView nbtWriteView = NbtWriteView.create(logging.makeChild(blockEntity.getReporterContext()), source.getRegistryManager());
                            blockEntity.writeComponentlessData(nbtWriteView);
                            BlockEntityInfo blockEntityInfo = new BlockEntityInfo(nbtWriteView.getNbt(), blockEntity.getComponents());
                            list2.add(new BlockInfo(blockPos7, blockState, blockEntityInfo, serverWorld2.getBlockState(blockPos7)));
                            deque.addLast(blockPos6);
                            continue;
                        }
                        if (blockState.isOpaqueFullCube() || blockState.isFullCube(serverWorld, blockPos6)) {
                            list.add(new BlockInfo(blockPos7, blockState, null, serverWorld2.getBlockState(blockPos7)));
                            deque.addLast(blockPos6);
                            continue;
                        }
                        list3.add(new BlockInfo(blockPos7, blockState, null, serverWorld2.getBlockState(blockPos7)));
                        deque.addFirst(blockPos6);
                    }
                }
            }
            l = 2 | (strict ? 816 : 0);
            if (mode == Mode.MOVE) {
                for (BlockPos blockPos8 : deque) {
                    serverWorld.setBlockState(blockPos8, Blocks.BARRIER.getDefaultState(), l | 0x330);
                }
                m = strict ? l : 3;
                for (BlockPos blockPos6 : deque) {
                    serverWorld.setBlockState(blockPos6, Blocks.AIR.getDefaultState(), m);
                }
            }
            ArrayList list4 = Lists.newArrayList();
            list4.addAll(list);
            list4.addAll(list2);
            list4.addAll(list3);
            List list5 = Lists.reverse((List)list4);
            for (BlockInfo blockInfo : list5) {
                serverWorld2.setBlockState(blockInfo.pos, Blocks.BARRIER.getDefaultState(), l | 0x330);
            }
            for (BlockInfo blockInfo : list4) {
                if (!serverWorld2.setBlockState(blockInfo.pos, blockInfo.state, l)) continue;
                ++k;
            }
            for (BlockInfo blockInfo : list2) {
                BlockEntity blockEntity2 = serverWorld2.getBlockEntity(blockInfo.pos);
                if (blockInfo.blockEntityInfo != null && blockEntity2 != null) {
                    blockEntity2.readComponentlessData(NbtReadView.create(logging.makeChild(blockEntity2.getReporterContext()), serverWorld2.getRegistryManager(), blockInfo.blockEntityInfo.nbt));
                    blockEntity2.setComponents(blockInfo.blockEntityInfo.components);
                    blockEntity2.markDirty();
                }
                serverWorld2.setBlockState(blockInfo.pos, blockInfo.state, l);
            }
            if (!strict) {
                for (BlockInfo blockInfo : list5) {
                    serverWorld2.onStateReplacedWithCommands(blockInfo.pos, blockInfo.previousStateAtDestination);
                }
            }
            ((WorldTickScheduler)serverWorld2.getBlockTickScheduler()).scheduleTicks(serverWorld.getBlockTickScheduler(), blockBox, blockPos5);
        }
        if (k == 0) {
            throw FAILED_EXCEPTION.create();
        }
        int o = k;
        source.sendFeedback(() -> Text.translatable("commands.clone.success", o), true);
        return k;
    }

    record DimensionalPos(ServerWorld dimension, BlockPos position) {
    }

    static final class Mode
    extends Enum<Mode> {
        public static final /* enum */ Mode FORCE = new Mode(true);
        public static final /* enum */ Mode MOVE = new Mode(true);
        public static final /* enum */ Mode NORMAL = new Mode(false);
        private final boolean allowsOverlap;
        private static final /* synthetic */ Mode[] field_13501;

        public static Mode[] values() {
            return (Mode[])field_13501.clone();
        }

        public static Mode valueOf(String string) {
            return Enum.valueOf(Mode.class, string);
        }

        private Mode(boolean allowsOverlap) {
            this.allowsOverlap = allowsOverlap;
        }

        public boolean allowsOverlap() {
            return this.allowsOverlap;
        }

        private static /* synthetic */ Mode[] method_36966() {
            return new Mode[]{FORCE, MOVE, NORMAL};
        }

        static {
            field_13501 = Mode.method_36966();
        }
    }

    static final class BlockEntityInfo
    extends Record {
        final NbtCompound nbt;
        final ComponentMap components;

        BlockEntityInfo(NbtCompound nbt, ComponentMap components) {
            this.nbt = nbt;
            this.components = components;
        }

        @Override
        public final String toString() {
            return ObjectMethods.bootstrap("toString", new MethodHandle[]{BlockEntityInfo.class, "tag;components", "nbt", "components"}, this);
        }

        @Override
        public final int hashCode() {
            return (int)ObjectMethods.bootstrap("hashCode", new MethodHandle[]{BlockEntityInfo.class, "tag;components", "nbt", "components"}, this);
        }

        @Override
        public final boolean equals(Object object) {
            return (boolean)ObjectMethods.bootstrap("equals", new MethodHandle[]{BlockEntityInfo.class, "tag;components", "nbt", "components"}, this, object);
        }

        public NbtCompound nbt() {
            return this.nbt;
        }

        public ComponentMap components() {
            return this.components;
        }
    }

    static final class BlockInfo
    extends Record {
        final BlockPos pos;
        final BlockState state;
        final @Nullable BlockEntityInfo blockEntityInfo;
        final BlockState previousStateAtDestination;

        BlockInfo(BlockPos pos, BlockState state, @Nullable BlockEntityInfo blockEntityInfo, BlockState previousStateAtDestination) {
            this.pos = pos;
            this.state = state;
            this.blockEntityInfo = blockEntityInfo;
            this.previousStateAtDestination = previousStateAtDestination;
        }

        @Override
        public final String toString() {
            return ObjectMethods.bootstrap("toString", new MethodHandle[]{BlockInfo.class, "pos;state;blockEntityInfo;previousStateAtDestination", "pos", "state", "blockEntityInfo", "previousStateAtDestination"}, this);
        }

        @Override
        public final int hashCode() {
            return (int)ObjectMethods.bootstrap("hashCode", new MethodHandle[]{BlockInfo.class, "pos;state;blockEntityInfo;previousStateAtDestination", "pos", "state", "blockEntityInfo", "previousStateAtDestination"}, this);
        }

        @Override
        public final boolean equals(Object object) {
            return (boolean)ObjectMethods.bootstrap("equals", new MethodHandle[]{BlockInfo.class, "pos;state;blockEntityInfo;previousStateAtDestination", "pos", "state", "blockEntityInfo", "previousStateAtDestination"}, this, object);
        }

        public BlockPos pos() {
            return this.pos;
        }

        public BlockState state() {
            return this.state;
        }

        public @Nullable BlockEntityInfo blockEntityInfo() {
            return this.blockEntityInfo;
        }

        public BlockState previousStateAtDestination() {
            return this.previousStateAtDestination;
        }
    }
}


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
 *  org.jspecify.annotations.Nullable
 */
package net.minecraft.server.command;

import com.mojang.brigadier.CommandDispatcher;
import com.mojang.brigadier.Message;
import com.mojang.brigadier.builder.LiteralArgumentBuilder;
import com.mojang.brigadier.builder.RequiredArgumentBuilder;
import com.mojang.brigadier.context.CommandContext;
import com.mojang.brigadier.exceptions.CommandSyntaxException;
import com.mojang.brigadier.exceptions.SimpleCommandExceptionType;
import java.util.function.Predicate;
import net.minecraft.block.BlockState;
import net.minecraft.block.pattern.CachedBlockPosition;
import net.minecraft.command.CommandRegistryAccess;
import net.minecraft.command.argument.BlockPosArgumentType;
import net.minecraft.command.argument.BlockStateArgument;
import net.minecraft.command.argument.BlockStateArgumentType;
import net.minecraft.server.command.CommandManager;
import net.minecraft.server.command.ServerCommandSource;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.text.Text;
import net.minecraft.util.math.BlockPos;
import org.jspecify.annotations.Nullable;

public class SetBlockCommand {
    private static final SimpleCommandExceptionType FAILED_EXCEPTION = new SimpleCommandExceptionType((Message)Text.translatable("commands.setblock.failed"));

    public static void register(CommandDispatcher<ServerCommandSource> dispatcher, CommandRegistryAccess commandRegistryAccess) {
        Predicate<CachedBlockPosition> predicate = pos -> pos.getWorld().isAir(pos.getBlockPos());
        dispatcher.register((LiteralArgumentBuilder)((LiteralArgumentBuilder)CommandManager.literal("setblock").requires(CommandManager.requirePermissionLevel(CommandManager.GAMEMASTERS_CHECK))).then(CommandManager.argument("pos", BlockPosArgumentType.blockPos()).then(((RequiredArgumentBuilder)((RequiredArgumentBuilder)((RequiredArgumentBuilder)((RequiredArgumentBuilder)CommandManager.argument("block", BlockStateArgumentType.blockState(commandRegistryAccess)).executes(context -> SetBlockCommand.execute((ServerCommandSource)context.getSource(), BlockPosArgumentType.getLoadedBlockPos((CommandContext<ServerCommandSource>)context, "pos"), BlockStateArgumentType.getBlockState((CommandContext<ServerCommandSource>)context, "block"), Mode.REPLACE, null, false))).then(CommandManager.literal("destroy").executes(context -> SetBlockCommand.execute((ServerCommandSource)context.getSource(), BlockPosArgumentType.getLoadedBlockPos((CommandContext<ServerCommandSource>)context, "pos"), BlockStateArgumentType.getBlockState((CommandContext<ServerCommandSource>)context, "block"), Mode.DESTROY, null, false)))).then(CommandManager.literal("keep").executes(context -> SetBlockCommand.execute((ServerCommandSource)context.getSource(), BlockPosArgumentType.getLoadedBlockPos((CommandContext<ServerCommandSource>)context, "pos"), BlockStateArgumentType.getBlockState((CommandContext<ServerCommandSource>)context, "block"), Mode.REPLACE, predicate, false)))).then(CommandManager.literal("replace").executes(context -> SetBlockCommand.execute((ServerCommandSource)context.getSource(), BlockPosArgumentType.getLoadedBlockPos((CommandContext<ServerCommandSource>)context, "pos"), BlockStateArgumentType.getBlockState((CommandContext<ServerCommandSource>)context, "block"), Mode.REPLACE, null, false)))).then(CommandManager.literal("strict").executes(context -> SetBlockCommand.execute((ServerCommandSource)context.getSource(), BlockPosArgumentType.getLoadedBlockPos((CommandContext<ServerCommandSource>)context, "pos"), BlockStateArgumentType.getBlockState((CommandContext<ServerCommandSource>)context, "block"), Mode.REPLACE, null, true))))));
    }

    private static int execute(ServerCommandSource source, BlockPos pos, BlockStateArgument block, Mode mode, @Nullable Predicate<CachedBlockPosition> condition, boolean strict) throws CommandSyntaxException {
        boolean bl;
        ServerWorld serverWorld = source.getWorld();
        if (serverWorld.isDebugWorld()) {
            throw FAILED_EXCEPTION.create();
        }
        if (condition != null && !condition.test(new CachedBlockPosition(serverWorld, pos, true))) {
            throw FAILED_EXCEPTION.create();
        }
        if (mode == Mode.DESTROY) {
            serverWorld.breakBlock(pos, true);
            bl = !block.getBlockState().isAir() || !serverWorld.getBlockState(pos).isAir();
        } else {
            bl = true;
        }
        BlockState blockState = serverWorld.getBlockState(pos);
        if (bl && !block.setBlockState(serverWorld, pos, 2 | (strict ? 816 : 256))) {
            throw FAILED_EXCEPTION.create();
        }
        if (!strict) {
            serverWorld.onStateReplacedWithCommands(pos, blockState);
        }
        source.sendFeedback(() -> Text.translatable("commands.setblock.success", pos.getX(), pos.getY(), pos.getZ()), true);
        return 1;
    }

    public static final class Mode
    extends Enum<Mode> {
        public static final /* enum */ Mode REPLACE = new Mode();
        public static final /* enum */ Mode DESTROY = new Mode();
        private static final /* synthetic */ Mode[] field_13720;

        public static Mode[] values() {
            return (Mode[])field_13720.clone();
        }

        public static Mode valueOf(String string) {
            return Enum.valueOf(Mode.class, string);
        }

        private static /* synthetic */ Mode[] method_36969() {
            return new Mode[]{REPLACE, DESTROY};
        }

        static {
            field_13720 = Mode.method_36969();
        }
    }
}


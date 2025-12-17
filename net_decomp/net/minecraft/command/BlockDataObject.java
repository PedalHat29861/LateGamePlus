/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  com.mojang.brigadier.Message
 *  com.mojang.brigadier.builder.ArgumentBuilder
 *  com.mojang.brigadier.context.CommandContext
 *  com.mojang.brigadier.exceptions.CommandSyntaxException
 *  com.mojang.brigadier.exceptions.SimpleCommandExceptionType
 *  com.mojang.logging.LogUtils
 *  org.slf4j.Logger
 */
package net.minecraft.command;

import com.mojang.brigadier.Message;
import com.mojang.brigadier.builder.ArgumentBuilder;
import com.mojang.brigadier.context.CommandContext;
import com.mojang.brigadier.exceptions.CommandSyntaxException;
import com.mojang.brigadier.exceptions.SimpleCommandExceptionType;
import com.mojang.logging.LogUtils;
import java.util.Locale;
import java.util.function.Function;
import net.minecraft.block.BlockState;
import net.minecraft.block.entity.BlockEntity;
import net.minecraft.command.DataCommandObject;
import net.minecraft.command.argument.BlockPosArgumentType;
import net.minecraft.command.argument.NbtPathArgumentType;
import net.minecraft.nbt.NbtCompound;
import net.minecraft.nbt.NbtElement;
import net.minecraft.nbt.NbtHelper;
import net.minecraft.server.command.CommandManager;
import net.minecraft.server.command.DataCommand;
import net.minecraft.server.command.ServerCommandSource;
import net.minecraft.storage.NbtReadView;
import net.minecraft.text.Text;
import net.minecraft.util.ErrorReporter;
import net.minecraft.util.math.BlockPos;
import org.slf4j.Logger;

public class BlockDataObject
implements DataCommandObject {
    private static final Logger LOGGER = LogUtils.getLogger();
    static final SimpleCommandExceptionType INVALID_BLOCK_EXCEPTION = new SimpleCommandExceptionType((Message)Text.translatable("commands.data.block.invalid"));
    public static final Function<String, DataCommand.ObjectType> TYPE_FACTORY = argumentName -> new DataCommand.ObjectType((String)argumentName){
        final /* synthetic */ String argumentName;
        {
            this.argumentName = string;
        }

        @Override
        public DataCommandObject getObject(CommandContext<ServerCommandSource> context) throws CommandSyntaxException {
            BlockPos blockPos = BlockPosArgumentType.getLoadedBlockPos(context, this.argumentName + "Pos");
            BlockEntity blockEntity = ((ServerCommandSource)context.getSource()).getWorld().getBlockEntity(blockPos);
            if (blockEntity == null) {
                throw INVALID_BLOCK_EXCEPTION.create();
            }
            return new BlockDataObject(blockEntity, blockPos);
        }

        @Override
        public ArgumentBuilder<ServerCommandSource, ?> addArgumentsToBuilder(ArgumentBuilder<ServerCommandSource, ?> argument, Function<ArgumentBuilder<ServerCommandSource, ?>, ArgumentBuilder<ServerCommandSource, ?>> argumentAdder) {
            return argument.then(CommandManager.literal("block").then(argumentAdder.apply((ArgumentBuilder<ServerCommandSource, ?>)CommandManager.argument(this.argumentName + "Pos", BlockPosArgumentType.blockPos()))));
        }
    };
    private final BlockEntity blockEntity;
    private final BlockPos pos;

    public BlockDataObject(BlockEntity blockEntity, BlockPos pos) {
        this.blockEntity = blockEntity;
        this.pos = pos;
    }

    @Override
    public void setNbt(NbtCompound nbt) {
        BlockState blockState = this.blockEntity.getWorld().getBlockState(this.pos);
        try (ErrorReporter.Logging logging = new ErrorReporter.Logging(this.blockEntity.getReporterContext(), LOGGER);){
            this.blockEntity.read(NbtReadView.create(logging, this.blockEntity.getWorld().getRegistryManager(), nbt));
            this.blockEntity.markDirty();
            this.blockEntity.getWorld().updateListeners(this.pos, blockState, blockState, 3);
        }
    }

    @Override
    public NbtCompound getNbt() {
        return this.blockEntity.createNbtWithIdentifyingData(this.blockEntity.getWorld().getRegistryManager());
    }

    @Override
    public Text feedbackModify() {
        return Text.translatable("commands.data.block.modified", this.pos.getX(), this.pos.getY(), this.pos.getZ());
    }

    @Override
    public Text feedbackQuery(NbtElement element) {
        return Text.translatable("commands.data.block.query", this.pos.getX(), this.pos.getY(), this.pos.getZ(), NbtHelper.toPrettyPrintedText(element));
    }

    @Override
    public Text feedbackGet(NbtPathArgumentType.NbtPath path, double scale, int result) {
        return Text.translatable("commands.data.block.get", path.getString(), this.pos.getX(), this.pos.getY(), this.pos.getZ(), String.format(Locale.ROOT, "%.2f", scale), result);
    }
}


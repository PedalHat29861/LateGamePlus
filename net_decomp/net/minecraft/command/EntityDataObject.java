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
import java.util.UUID;
import java.util.function.Function;
import net.minecraft.command.DataCommandObject;
import net.minecraft.command.argument.EntityArgumentType;
import net.minecraft.command.argument.NbtPathArgumentType;
import net.minecraft.entity.Entity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.nbt.NbtCompound;
import net.minecraft.nbt.NbtElement;
import net.minecraft.nbt.NbtHelper;
import net.minecraft.predicate.NbtPredicate;
import net.minecraft.server.command.CommandManager;
import net.minecraft.server.command.DataCommand;
import net.minecraft.server.command.ServerCommandSource;
import net.minecraft.storage.NbtReadView;
import net.minecraft.text.Text;
import net.minecraft.util.ErrorReporter;
import org.slf4j.Logger;

public class EntityDataObject
implements DataCommandObject {
    private static final Logger LOGGER = LogUtils.getLogger();
    private static final SimpleCommandExceptionType INVALID_ENTITY_EXCEPTION = new SimpleCommandExceptionType((Message)Text.translatable("commands.data.entity.invalid"));
    public static final Function<String, DataCommand.ObjectType> TYPE_FACTORY = argumentName -> new DataCommand.ObjectType((String)argumentName){
        final /* synthetic */ String argumentName;
        {
            this.argumentName = string;
        }

        @Override
        public DataCommandObject getObject(CommandContext<ServerCommandSource> context) throws CommandSyntaxException {
            return new EntityDataObject(EntityArgumentType.getEntity(context, this.argumentName));
        }

        @Override
        public ArgumentBuilder<ServerCommandSource, ?> addArgumentsToBuilder(ArgumentBuilder<ServerCommandSource, ?> argument, Function<ArgumentBuilder<ServerCommandSource, ?>, ArgumentBuilder<ServerCommandSource, ?>> argumentAdder) {
            return argument.then(CommandManager.literal("entity").then(argumentAdder.apply((ArgumentBuilder<ServerCommandSource, ?>)CommandManager.argument(this.argumentName, EntityArgumentType.entity()))));
        }
    };
    private final Entity entity;

    public EntityDataObject(Entity entity) {
        this.entity = entity;
    }

    @Override
    public void setNbt(NbtCompound nbt) throws CommandSyntaxException {
        if (this.entity instanceof PlayerEntity) {
            throw INVALID_ENTITY_EXCEPTION.create();
        }
        UUID uUID = this.entity.getUuid();
        try (ErrorReporter.Logging logging = new ErrorReporter.Logging(this.entity.getErrorReporterContext(), LOGGER);){
            this.entity.readData(NbtReadView.create(logging, this.entity.getRegistryManager(), nbt));
            this.entity.setUuid(uUID);
        }
    }

    @Override
    public NbtCompound getNbt() {
        return NbtPredicate.entityToNbt(this.entity);
    }

    @Override
    public Text feedbackModify() {
        return Text.translatable("commands.data.entity.modified", this.entity.getDisplayName());
    }

    @Override
    public Text feedbackQuery(NbtElement element) {
        return Text.translatable("commands.data.entity.query", this.entity.getDisplayName(), NbtHelper.toPrettyPrintedText(element));
    }

    @Override
    public Text feedbackGet(NbtPathArgumentType.NbtPath path, double scale, int result) {
        return Text.translatable("commands.data.entity.get", path.getString(), this.entity.getDisplayName(), String.format(Locale.ROOT, "%.2f", scale), result);
    }
}


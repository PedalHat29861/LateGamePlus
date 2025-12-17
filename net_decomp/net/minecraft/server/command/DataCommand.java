/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  com.google.common.collect.ImmutableList
 *  com.google.common.collect.Iterables
 *  com.mojang.brigadier.CommandDispatcher
 *  com.mojang.brigadier.Message
 *  com.mojang.brigadier.arguments.DoubleArgumentType
 *  com.mojang.brigadier.arguments.IntegerArgumentType
 *  com.mojang.brigadier.builder.ArgumentBuilder
 *  com.mojang.brigadier.builder.LiteralArgumentBuilder
 *  com.mojang.brigadier.builder.RequiredArgumentBuilder
 *  com.mojang.brigadier.context.CommandContext
 *  com.mojang.brigadier.exceptions.CommandSyntaxException
 *  com.mojang.brigadier.exceptions.Dynamic2CommandExceptionType
 *  com.mojang.brigadier.exceptions.DynamicCommandExceptionType
 *  com.mojang.brigadier.exceptions.SimpleCommandExceptionType
 */
package net.minecraft.server.command;

import com.google.common.collect.ImmutableList;
import com.google.common.collect.Iterables;
import com.mojang.brigadier.CommandDispatcher;
import com.mojang.brigadier.Message;
import com.mojang.brigadier.arguments.DoubleArgumentType;
import com.mojang.brigadier.arguments.IntegerArgumentType;
import com.mojang.brigadier.builder.ArgumentBuilder;
import com.mojang.brigadier.builder.LiteralArgumentBuilder;
import com.mojang.brigadier.builder.RequiredArgumentBuilder;
import com.mojang.brigadier.context.CommandContext;
import com.mojang.brigadier.exceptions.CommandSyntaxException;
import com.mojang.brigadier.exceptions.Dynamic2CommandExceptionType;
import com.mojang.brigadier.exceptions.DynamicCommandExceptionType;
import com.mojang.brigadier.exceptions.SimpleCommandExceptionType;
import java.lang.runtime.SwitchBootstraps;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Iterator;
import java.util.List;
import java.util.Objects;
import java.util.function.BiConsumer;
import java.util.function.Function;
import net.minecraft.command.BlockDataObject;
import net.minecraft.command.DataCommandObject;
import net.minecraft.command.EntityDataObject;
import net.minecraft.command.StorageDataObject;
import net.minecraft.command.argument.NbtCompoundArgumentType;
import net.minecraft.command.argument.NbtElementArgumentType;
import net.minecraft.command.argument.NbtPathArgumentType;
import net.minecraft.nbt.AbstractNbtList;
import net.minecraft.nbt.AbstractNbtNumber;
import net.minecraft.nbt.NbtCompound;
import net.minecraft.nbt.NbtElement;
import net.minecraft.nbt.NbtEnd;
import net.minecraft.nbt.NbtPrimitive;
import net.minecraft.nbt.NbtString;
import net.minecraft.server.command.CommandManager;
import net.minecraft.server.command.ServerCommandSource;
import net.minecraft.text.Text;
import net.minecraft.util.math.MathHelper;

public class DataCommand {
    private static final SimpleCommandExceptionType MERGE_FAILED_EXCEPTION = new SimpleCommandExceptionType((Message)Text.translatable("commands.data.merge.failed"));
    private static final DynamicCommandExceptionType GET_INVALID_EXCEPTION = new DynamicCommandExceptionType(path -> Text.stringifiedTranslatable("commands.data.get.invalid", path));
    private static final DynamicCommandExceptionType GET_UNKNOWN_EXCEPTION = new DynamicCommandExceptionType(path -> Text.stringifiedTranslatable("commands.data.get.unknown", path));
    private static final SimpleCommandExceptionType GET_MULTIPLE_EXCEPTION = new SimpleCommandExceptionType((Message)Text.translatable("commands.data.get.multiple"));
    private static final DynamicCommandExceptionType MODIFY_EXPECTED_OBJECT_EXCEPTION = new DynamicCommandExceptionType(nbt -> Text.stringifiedTranslatable("commands.data.modify.expected_object", nbt));
    private static final DynamicCommandExceptionType MODIFY_EXPECTED_VALUE_EXCEPTION = new DynamicCommandExceptionType(nbt -> Text.stringifiedTranslatable("commands.data.modify.expected_value", nbt));
    private static final Dynamic2CommandExceptionType MODIFY_INVALID_SUBSTRING_EXCEPTION = new Dynamic2CommandExceptionType((startIndex, endIndex) -> Text.stringifiedTranslatable("commands.data.modify.invalid_substring", startIndex, endIndex));
    public static final List<Function<String, ObjectType>> OBJECT_TYPE_FACTORIES = ImmutableList.of(EntityDataObject.TYPE_FACTORY, BlockDataObject.TYPE_FACTORY, StorageDataObject.TYPE_FACTORY);
    public static final List<ObjectType> TARGET_OBJECT_TYPES = (List)OBJECT_TYPE_FACTORIES.stream().map(factory -> (ObjectType)factory.apply("target")).collect(ImmutableList.toImmutableList());
    public static final List<ObjectType> SOURCE_OBJECT_TYPES = (List)OBJECT_TYPE_FACTORIES.stream().map(factory -> (ObjectType)factory.apply("source")).collect(ImmutableList.toImmutableList());

    public static void register(CommandDispatcher<ServerCommandSource> dispatcher) {
        LiteralArgumentBuilder literalArgumentBuilder = (LiteralArgumentBuilder)CommandManager.literal("data").requires(CommandManager.requirePermissionLevel(CommandManager.GAMEMASTERS_CHECK));
        for (ObjectType objectType : TARGET_OBJECT_TYPES) {
            ((LiteralArgumentBuilder)((LiteralArgumentBuilder)((LiteralArgumentBuilder)literalArgumentBuilder.then(objectType.addArgumentsToBuilder((ArgumentBuilder<ServerCommandSource, ?>)CommandManager.literal("merge"), builder -> builder.then(CommandManager.argument("nbt", NbtCompoundArgumentType.nbtCompound()).executes(context -> DataCommand.executeMerge((ServerCommandSource)context.getSource(), objectType.getObject((CommandContext<ServerCommandSource>)context), NbtCompoundArgumentType.getNbtCompound(context, "nbt"))))))).then(objectType.addArgumentsToBuilder((ArgumentBuilder<ServerCommandSource, ?>)CommandManager.literal("get"), builder -> builder.executes(context -> DataCommand.executeGet((ServerCommandSource)context.getSource(), objectType.getObject((CommandContext<ServerCommandSource>)context))).then(((RequiredArgumentBuilder)CommandManager.argument("path", NbtPathArgumentType.nbtPath()).executes(context -> DataCommand.executeGet((ServerCommandSource)context.getSource(), objectType.getObject((CommandContext<ServerCommandSource>)context), NbtPathArgumentType.getNbtPath((CommandContext<ServerCommandSource>)context, "path")))).then(CommandManager.argument("scale", DoubleArgumentType.doubleArg()).executes(context -> DataCommand.executeGet((ServerCommandSource)context.getSource(), objectType.getObject((CommandContext<ServerCommandSource>)context), NbtPathArgumentType.getNbtPath((CommandContext<ServerCommandSource>)context, "path"), DoubleArgumentType.getDouble((CommandContext)context, (String)"scale")))))))).then(objectType.addArgumentsToBuilder((ArgumentBuilder<ServerCommandSource, ?>)CommandManager.literal("remove"), builder -> builder.then(CommandManager.argument("path", NbtPathArgumentType.nbtPath()).executes(context -> DataCommand.executeRemove((ServerCommandSource)context.getSource(), objectType.getObject((CommandContext<ServerCommandSource>)context), NbtPathArgumentType.getNbtPath((CommandContext<ServerCommandSource>)context, "path"))))))).then(DataCommand.addModifyArgument((builder, modifier) -> builder.then(CommandManager.literal("insert").then(CommandManager.argument("index", IntegerArgumentType.integer()).then(modifier.create((context, sourceNbt, path, elements) -> path.insert(IntegerArgumentType.getInteger((CommandContext)context, (String)"index"), sourceNbt, elements))))).then(CommandManager.literal("prepend").then(modifier.create((context, sourceNbt, path, elements) -> path.insert(0, sourceNbt, elements)))).then(CommandManager.literal("append").then(modifier.create((context, sourceNbt, path, elements) -> path.insert(-1, sourceNbt, elements)))).then(CommandManager.literal("set").then(modifier.create((context, sourceNbt, path, elements) -> path.put(sourceNbt, (NbtElement)Iterables.getLast((Iterable)elements))))).then(CommandManager.literal("merge").then(modifier.create((context, element, path, elements) -> {
                NbtCompound nbtCompound = new NbtCompound();
                for (NbtElement nbtElement : elements) {
                    if (NbtPathArgumentType.NbtPath.isTooDeep(nbtElement, 0)) {
                        throw NbtPathArgumentType.TOO_DEEP_EXCEPTION.create();
                    }
                    if (nbtElement instanceof NbtCompound) {
                        NbtCompound nbtCompound2 = (NbtCompound)nbtElement;
                        nbtCompound.copyFrom(nbtCompound2);
                        continue;
                    }
                    throw MODIFY_EXPECTED_OBJECT_EXCEPTION.create((Object)nbtElement);
                }
                List<NbtElement> collection = path.getOrInit(element, NbtCompound::new);
                int i = 0;
                for (NbtElement nbtElement2 : collection) {
                    if (!(nbtElement2 instanceof NbtCompound)) {
                        throw MODIFY_EXPECTED_OBJECT_EXCEPTION.create((Object)nbtElement2);
                    }
                    NbtCompound nbtCompound3 = (NbtCompound)nbtElement2;
                    NbtCompound nbtCompound4 = nbtCompound3.copy();
                    nbtCompound3.copyFrom(nbtCompound);
                    i += nbtCompound4.equals(nbtCompound3) ? 0 : 1;
                }
                return i;
            })))));
        }
        dispatcher.register(literalArgumentBuilder);
    }

    /*
     * Enabled aggressive block sorting
     * Enabled unnecessary exception pruning
     * Enabled aggressive exception aggregation
     */
    private static String asString(NbtElement nbt) throws CommandSyntaxException {
        NbtElement nbtElement = nbt;
        Objects.requireNonNull(nbtElement);
        NbtElement nbtElement2 = nbtElement;
        int n = 0;
        switch (SwitchBootstraps.typeSwitch("typeSwitch", new Object[]{NbtString.class, NbtPrimitive.class}, (Object)nbtElement2, n)) {
            case 0: {
                String string;
                NbtString nbtString = (NbtString)nbtElement2;
                try {
                    String string2;
                    String string22;
                    string = string22 = (string2 = nbtString.value());
                    return string;
                }
                catch (Throwable throwable) {
                    throw new MatchException(throwable.toString(), throwable);
                }
            }
            case 1: {
                NbtPrimitive nbtPrimitive = (NbtPrimitive)nbtElement2;
                String string = nbtPrimitive.toString();
                return string;
            }
        }
        throw MODIFY_EXPECTED_VALUE_EXCEPTION.create((Object)nbt);
    }

    private static List<NbtElement> mapValues(List<NbtElement> list, Processor processor) throws CommandSyntaxException {
        ArrayList<NbtElement> list2 = new ArrayList<NbtElement>(list.size());
        for (NbtElement nbtElement : list) {
            String string = DataCommand.asString(nbtElement);
            list2.add(NbtString.of(processor.process(string)));
        }
        return list2;
    }

    private static ArgumentBuilder<ServerCommandSource, ?> addModifyArgument(BiConsumer<ArgumentBuilder<ServerCommandSource, ?>, ModifyArgumentCreator> subArgumentAdder) {
        LiteralArgumentBuilder<ServerCommandSource> literalArgumentBuilder = CommandManager.literal("modify");
        for (ObjectType objectType : TARGET_OBJECT_TYPES) {
            objectType.addArgumentsToBuilder((ArgumentBuilder<ServerCommandSource, ?>)literalArgumentBuilder, builder -> {
                RequiredArgumentBuilder<ServerCommandSource, NbtPathArgumentType.NbtPath> argumentBuilder = CommandManager.argument("targetPath", NbtPathArgumentType.nbtPath());
                for (ObjectType objectType2 : SOURCE_OBJECT_TYPES) {
                    subArgumentAdder.accept((ArgumentBuilder<ServerCommandSource, ?>)argumentBuilder, operation -> objectType2.addArgumentsToBuilder((ArgumentBuilder<ServerCommandSource, ?>)CommandManager.literal("from"), builderx -> builderx.executes(context -> DataCommand.executeModify((CommandContext<ServerCommandSource>)context, objectType, operation, DataCommand.getValues((CommandContext<ServerCommandSource>)context, objectType2))).then(CommandManager.argument("sourcePath", NbtPathArgumentType.nbtPath()).executes(context -> DataCommand.executeModify((CommandContext<ServerCommandSource>)context, objectType, operation, DataCommand.getValuesByPath((CommandContext<ServerCommandSource>)context, objectType2))))));
                    subArgumentAdder.accept((ArgumentBuilder<ServerCommandSource, ?>)argumentBuilder, operation -> objectType2.addArgumentsToBuilder((ArgumentBuilder<ServerCommandSource, ?>)CommandManager.literal("string"), builderx -> builderx.executes(context -> DataCommand.executeModify((CommandContext<ServerCommandSource>)context, objectType, operation, DataCommand.mapValues(DataCommand.getValues((CommandContext<ServerCommandSource>)context, objectType2), value -> value))).then(((RequiredArgumentBuilder)CommandManager.argument("sourcePath", NbtPathArgumentType.nbtPath()).executes(context -> DataCommand.executeModify((CommandContext<ServerCommandSource>)context, objectType, operation, DataCommand.mapValues(DataCommand.getValuesByPath((CommandContext<ServerCommandSource>)context, objectType2), value -> value)))).then(((RequiredArgumentBuilder)CommandManager.argument("start", IntegerArgumentType.integer()).executes(context -> DataCommand.executeModify((CommandContext<ServerCommandSource>)context, objectType, operation, DataCommand.mapValues(DataCommand.getValuesByPath((CommandContext<ServerCommandSource>)context, objectType2), value -> DataCommand.substring(value, IntegerArgumentType.getInteger((CommandContext)context, (String)"start")))))).then(CommandManager.argument("end", IntegerArgumentType.integer()).executes(context -> DataCommand.executeModify((CommandContext<ServerCommandSource>)context, objectType, operation, DataCommand.mapValues(DataCommand.getValuesByPath((CommandContext<ServerCommandSource>)context, objectType2), value -> DataCommand.substring(value, IntegerArgumentType.getInteger((CommandContext)context, (String)"start"), IntegerArgumentType.getInteger((CommandContext)context, (String)"end"))))))))));
                }
                subArgumentAdder.accept((ArgumentBuilder<ServerCommandSource, ?>)argumentBuilder, modifier -> CommandManager.literal("value").then(CommandManager.argument("value", NbtElementArgumentType.nbtElement()).executes(context -> {
                    List<NbtElement> list = Collections.singletonList(NbtElementArgumentType.getNbtElement(context, "value"));
                    return DataCommand.executeModify((CommandContext<ServerCommandSource>)context, objectType, modifier, list);
                })));
                return builder.then(argumentBuilder);
            });
        }
        return literalArgumentBuilder;
    }

    private static String substringInternal(String string, int startIndex, int endIndex) throws CommandSyntaxException {
        if (startIndex < 0 || endIndex > string.length() || startIndex > endIndex) {
            throw MODIFY_INVALID_SUBSTRING_EXCEPTION.create((Object)startIndex, (Object)endIndex);
        }
        return string.substring(startIndex, endIndex);
    }

    private static String substring(String string, int startIndex, int endIndex) throws CommandSyntaxException {
        int i = string.length();
        int j = DataCommand.getSubstringIndex(startIndex, i);
        int k = DataCommand.getSubstringIndex(endIndex, i);
        return DataCommand.substringInternal(string, j, k);
    }

    private static String substring(String string, int startIndex) throws CommandSyntaxException {
        int i = string.length();
        return DataCommand.substringInternal(string, DataCommand.getSubstringIndex(startIndex, i), i);
    }

    private static int getSubstringIndex(int index, int length) {
        return index >= 0 ? index : length + index;
    }

    private static List<NbtElement> getValues(CommandContext<ServerCommandSource> context, ObjectType objectType) throws CommandSyntaxException {
        DataCommandObject dataCommandObject = objectType.getObject(context);
        return Collections.singletonList(dataCommandObject.getNbt());
    }

    private static List<NbtElement> getValuesByPath(CommandContext<ServerCommandSource> context, ObjectType objectType) throws CommandSyntaxException {
        DataCommandObject dataCommandObject = objectType.getObject(context);
        NbtPathArgumentType.NbtPath nbtPath = NbtPathArgumentType.getNbtPath(context, "sourcePath");
        return nbtPath.get(dataCommandObject.getNbt());
    }

    private static int executeModify(CommandContext<ServerCommandSource> context, ObjectType objectType, ModifyOperation modifier, List<NbtElement> elements) throws CommandSyntaxException {
        DataCommandObject dataCommandObject = objectType.getObject(context);
        NbtPathArgumentType.NbtPath nbtPath = NbtPathArgumentType.getNbtPath(context, "targetPath");
        NbtCompound nbtCompound = dataCommandObject.getNbt();
        int i = modifier.modify(context, nbtCompound, nbtPath, elements);
        if (i == 0) {
            throw MERGE_FAILED_EXCEPTION.create();
        }
        dataCommandObject.setNbt(nbtCompound);
        ((ServerCommandSource)context.getSource()).sendFeedback(() -> dataCommandObject.feedbackModify(), true);
        return i;
    }

    private static int executeRemove(ServerCommandSource source, DataCommandObject object, NbtPathArgumentType.NbtPath path) throws CommandSyntaxException {
        NbtCompound nbtCompound = object.getNbt();
        int i = path.remove(nbtCompound);
        if (i == 0) {
            throw MERGE_FAILED_EXCEPTION.create();
        }
        object.setNbt(nbtCompound);
        source.sendFeedback(() -> object.feedbackModify(), true);
        return i;
    }

    public static NbtElement getNbt(NbtPathArgumentType.NbtPath path, DataCommandObject object) throws CommandSyntaxException {
        List<NbtElement> collection = path.get(object.getNbt());
        Iterator iterator = collection.iterator();
        NbtElement nbtElement = (NbtElement)iterator.next();
        if (iterator.hasNext()) {
            throw GET_MULTIPLE_EXCEPTION.create();
        }
        return nbtElement;
    }

    /*
     * Loose catch block
     */
    private static int executeGet(ServerCommandSource source, DataCommandObject object, NbtPathArgumentType.NbtPath path) throws CommandSyntaxException {
        NbtElement nbtElement;
        NbtElement nbtElement2 = nbtElement = DataCommand.getNbt(path, object);
        Objects.requireNonNull(nbtElement2);
        NbtElement nbtElement3 = nbtElement2;
        int n = 0;
        int i = switch (SwitchBootstraps.typeSwitch("typeSwitch", new Object[]{AbstractNbtNumber.class, AbstractNbtList.class, NbtCompound.class, NbtString.class, NbtEnd.class}, (Object)nbtElement3, n)) {
            default -> throw new MatchException(null, null);
            case 0 -> {
                AbstractNbtNumber abstractNbtNumber = (AbstractNbtNumber)nbtElement3;
                yield MathHelper.floor(abstractNbtNumber.doubleValue());
            }
            case 1 -> {
                AbstractNbtList abstractNbtList = (AbstractNbtList)nbtElement3;
                yield abstractNbtList.size();
            }
            case 2 -> {
                NbtCompound nbtCompound = (NbtCompound)nbtElement3;
                yield nbtCompound.getSize();
            }
            case 3 -> {
                String var12_11;
                NbtString var10_10 = (NbtString)nbtElement3;
                String string = var12_11 = var10_10.value();
                yield string.length();
            }
            case 4 -> {
                NbtEnd nbtEnd = (NbtEnd)nbtElement3;
                throw GET_UNKNOWN_EXCEPTION.create((Object)path.toString());
            }
        };
        source.sendFeedback(() -> object.feedbackQuery(nbtElement), false);
        return i;
        catch (Throwable throwable) {
            throw new MatchException(throwable.toString(), throwable);
        }
    }

    private static int executeGet(ServerCommandSource source, DataCommandObject object, NbtPathArgumentType.NbtPath path, double scale) throws CommandSyntaxException {
        NbtElement nbtElement = DataCommand.getNbt(path, object);
        if (!(nbtElement instanceof AbstractNbtNumber)) {
            throw GET_INVALID_EXCEPTION.create((Object)path.toString());
        }
        int i = MathHelper.floor(((AbstractNbtNumber)nbtElement).doubleValue() * scale);
        source.sendFeedback(() -> object.feedbackGet(path, scale, i), false);
        return i;
    }

    private static int executeGet(ServerCommandSource source, DataCommandObject object) throws CommandSyntaxException {
        NbtCompound nbtCompound = object.getNbt();
        source.sendFeedback(() -> object.feedbackQuery(nbtCompound), false);
        return 1;
    }

    private static int executeMerge(ServerCommandSource source, DataCommandObject object, NbtCompound nbt) throws CommandSyntaxException {
        NbtCompound nbtCompound = object.getNbt();
        if (NbtPathArgumentType.NbtPath.isTooDeep(nbt, 0)) {
            throw NbtPathArgumentType.TOO_DEEP_EXCEPTION.create();
        }
        NbtCompound nbtCompound2 = nbtCompound.copy().copyFrom(nbt);
        if (nbtCompound.equals(nbtCompound2)) {
            throw MERGE_FAILED_EXCEPTION.create();
        }
        object.setNbt(nbtCompound2);
        source.sendFeedback(() -> object.feedbackModify(), true);
        return 1;
    }

    public static interface ObjectType {
        public DataCommandObject getObject(CommandContext<ServerCommandSource> var1) throws CommandSyntaxException;

        public ArgumentBuilder<ServerCommandSource, ?> addArgumentsToBuilder(ArgumentBuilder<ServerCommandSource, ?> var1, Function<ArgumentBuilder<ServerCommandSource, ?>, ArgumentBuilder<ServerCommandSource, ?>> var2);
    }

    @FunctionalInterface
    static interface Processor {
        public String process(String var1) throws CommandSyntaxException;
    }

    @FunctionalInterface
    static interface ModifyOperation {
        public int modify(CommandContext<ServerCommandSource> var1, NbtCompound var2, NbtPathArgumentType.NbtPath var3, List<NbtElement> var4) throws CommandSyntaxException;
    }

    @FunctionalInterface
    static interface ModifyArgumentCreator {
        public ArgumentBuilder<ServerCommandSource, ?> create(ModifyOperation var1);
    }
}


/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  com.mojang.brigadier.ImmutableStringReader
 *  com.mojang.brigadier.StringReader
 *  com.mojang.brigadier.arguments.ArgumentType
 *  com.mojang.brigadier.context.CommandContext
 *  com.mojang.brigadier.exceptions.CommandSyntaxException
 *  com.mojang.brigadier.exceptions.DynamicCommandExceptionType
 *  com.mojang.brigadier.suggestion.Suggestions
 *  com.mojang.brigadier.suggestion.SuggestionsBuilder
 */
package net.minecraft.command.argument;

import com.mojang.brigadier.ImmutableStringReader;
import com.mojang.brigadier.StringReader;
import com.mojang.brigadier.arguments.ArgumentType;
import com.mojang.brigadier.context.CommandContext;
import com.mojang.brigadier.exceptions.CommandSyntaxException;
import com.mojang.brigadier.exceptions.DynamicCommandExceptionType;
import com.mojang.brigadier.suggestion.Suggestions;
import com.mojang.brigadier.suggestion.SuggestionsBuilder;
import java.util.Arrays;
import java.util.Collection;
import java.util.concurrent.CompletableFuture;
import net.minecraft.command.CommandSource;
import net.minecraft.command.argument.ArgumentReaderUtils;
import net.minecraft.inventory.SlotRange;
import net.minecraft.inventory.SlotRanges;
import net.minecraft.server.command.ServerCommandSource;
import net.minecraft.text.Text;

public class ItemSlotArgumentType
implements ArgumentType<Integer> {
    private static final Collection<String> EXAMPLES = Arrays.asList("container.5", "weapon");
    private static final DynamicCommandExceptionType UNKNOWN_SLOT_EXCEPTION = new DynamicCommandExceptionType(name -> Text.stringifiedTranslatable("slot.unknown", name));
    private static final DynamicCommandExceptionType ONLY_SINGLE_ALLOWED_EXCEPTION = new DynamicCommandExceptionType(name -> Text.stringifiedTranslatable("slot.only_single_allowed", name));

    public static ItemSlotArgumentType itemSlot() {
        return new ItemSlotArgumentType();
    }

    public static int getItemSlot(CommandContext<ServerCommandSource> context, String name) {
        return (Integer)context.getArgument(name, Integer.class);
    }

    public Integer parse(StringReader stringReader) throws CommandSyntaxException {
        String string = ArgumentReaderUtils.readWhileMatching(stringReader, c -> c != ' ');
        SlotRange slotRange = SlotRanges.fromName(string);
        if (slotRange == null) {
            throw UNKNOWN_SLOT_EXCEPTION.createWithContext((ImmutableStringReader)stringReader, (Object)string);
        }
        if (slotRange.getSlotCount() != 1) {
            throw ONLY_SINGLE_ALLOWED_EXCEPTION.createWithContext((ImmutableStringReader)stringReader, (Object)string);
        }
        return slotRange.getSlotIds().getInt(0);
    }

    public <S> CompletableFuture<Suggestions> listSuggestions(CommandContext<S> context, SuggestionsBuilder builder) {
        return CommandSource.suggestMatching(SlotRanges.streamSingleSlotNames(), builder);
    }

    public Collection<String> getExamples() {
        return EXAMPLES;
    }

    public /* synthetic */ Object parse(StringReader reader) throws CommandSyntaxException {
        return this.parse(reader);
    }
}


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
import net.minecraft.server.command.ServerCommandSource;
import net.minecraft.text.Text;
import net.minecraft.util.math.ColorHelper;

public class HexColorArgumentType
implements ArgumentType<Integer> {
    private static final Collection<String> EXAMPLES = Arrays.asList("F00", "FF0000");
    public static final DynamicCommandExceptionType INVALID_HEX_COLOR_EXCEPTION = new DynamicCommandExceptionType(hexColor -> Text.stringifiedTranslatable("argument.hexcolor.invalid", hexColor));

    private HexColorArgumentType() {
    }

    public static HexColorArgumentType hexColor() {
        return new HexColorArgumentType();
    }

    public static Integer getArgbColor(CommandContext<ServerCommandSource> context, String hex) {
        return (Integer)context.getArgument(hex, Integer.class);
    }

    public Integer parse(StringReader stringReader) throws CommandSyntaxException {
        String string = stringReader.readUnquotedString();
        return switch (string.length()) {
            case 3 -> ColorHelper.getArgb(HexColorArgumentType.expand(Integer.parseInt(string, 0, 1, 16)), HexColorArgumentType.expand(Integer.parseInt(string, 1, 2, 16)), HexColorArgumentType.expand(Integer.parseInt(string, 2, 3, 16)));
            case 6 -> ColorHelper.getArgb(Integer.parseInt(string, 0, 2, 16), Integer.parseInt(string, 2, 4, 16), Integer.parseInt(string, 4, 6, 16));
            default -> throw INVALID_HEX_COLOR_EXCEPTION.createWithContext((ImmutableStringReader)stringReader, (Object)string);
        };
    }

    private static int expand(int digit) {
        return digit * 17;
    }

    public <S> CompletableFuture<Suggestions> listSuggestions(CommandContext<S> context, SuggestionsBuilder builder) {
        return CommandSource.suggestMatching(EXAMPLES, builder);
    }

    public Collection<String> getExamples() {
        return EXAMPLES;
    }

    public /* synthetic */ Object parse(StringReader reader) throws CommandSyntaxException {
        return this.parse(reader);
    }
}


/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  com.mojang.brigadier.StringReader
 *  com.mojang.brigadier.exceptions.CommandSyntaxException
 *  com.mojang.brigadier.suggestion.Suggestions
 *  com.mojang.brigadier.suggestion.SuggestionsBuilder
 */
package net.minecraft.util.packrat;

import com.mojang.brigadier.StringReader;
import com.mojang.brigadier.exceptions.CommandSyntaxException;
import com.mojang.brigadier.suggestion.Suggestions;
import com.mojang.brigadier.suggestion.SuggestionsBuilder;
import java.util.List;
import java.util.Optional;
import java.util.concurrent.CompletableFuture;
import java.util.stream.Collectors;
import net.minecraft.command.CommandSource;
import net.minecraft.util.packrat.CursorExceptionType;
import net.minecraft.util.packrat.IdentifierSuggestable;
import net.minecraft.util.packrat.ParseError;
import net.minecraft.util.packrat.ParseErrorList;
import net.minecraft.util.packrat.Parser;
import net.minecraft.util.packrat.ParsingRuleEntry;
import net.minecraft.util.packrat.ParsingRules;
import net.minecraft.util.packrat.ParsingState;
import net.minecraft.util.packrat.ReaderBackedParsingState;
import net.minecraft.util.packrat.Suggestable;

public record PackratParser<T>(ParsingRules<StringReader> rules, ParsingRuleEntry<StringReader, T> top) implements Parser<T>
{
    public PackratParser {
        rules.ensureBound();
    }

    public Optional<T> startParsing(ParsingState<StringReader> state) {
        return state.startParsing(this.top);
    }

    @Override
    public T parse(StringReader reader) throws CommandSyntaxException {
        Object r;
        ParseErrorList.Impl<StringReader> impl = new ParseErrorList.Impl<StringReader>();
        ReaderBackedParsingState readerBackedParsingState = new ReaderBackedParsingState(impl, reader);
        Optional<T> optional = this.startParsing(readerBackedParsingState);
        if (optional.isPresent()) {
            return optional.get();
        }
        List<ParseError<StringReader>> list = impl.getErrors();
        List list2 = list.stream().mapMulti((error, callback) -> {
            Object object = error.reason();
            if (object instanceof CursorExceptionType) {
                CursorExceptionType cursorExceptionType = (CursorExceptionType)object;
                callback.accept(cursorExceptionType.create(reader.getString(), error.cursor()));
            } else {
                object = error.reason();
                if (object instanceof Exception) {
                    Exception exception = (Exception)object;
                    callback.accept(exception);
                }
            }
        }).toList();
        for (Exception exception : list2) {
            if (!(exception instanceof CommandSyntaxException)) continue;
            CommandSyntaxException commandSyntaxException = (CommandSyntaxException)((Object)exception);
            throw commandSyntaxException;
        }
        if (list2.size() == 1 && (r = list2.get(0)) instanceof RuntimeException) {
            RuntimeException runtimeException = (RuntimeException)r;
            throw runtimeException;
        }
        throw new IllegalStateException("Failed to parse: " + list.stream().map(ParseError::toString).collect(Collectors.joining(", ")));
    }

    @Override
    public CompletableFuture<Suggestions> listSuggestions(SuggestionsBuilder builder) {
        StringReader stringReader = new StringReader(builder.getInput());
        stringReader.setCursor(builder.getStart());
        ParseErrorList.Impl<StringReader> impl = new ParseErrorList.Impl<StringReader>();
        ReaderBackedParsingState readerBackedParsingState = new ReaderBackedParsingState(impl, stringReader);
        this.startParsing(readerBackedParsingState);
        List<ParseError<StringReader>> list = impl.getErrors();
        if (list.isEmpty()) {
            return builder.buildFuture();
        }
        SuggestionsBuilder suggestionsBuilder = builder.createOffset(impl.getCursor());
        for (ParseError<StringReader> parseError : list) {
            Suggestable<StringReader> suggestable = parseError.suggestions();
            if (suggestable instanceof IdentifierSuggestable) {
                IdentifierSuggestable identifierSuggestable = (IdentifierSuggestable)suggestable;
                CommandSource.suggestIdentifiers(identifierSuggestable.possibleIds(), suggestionsBuilder);
                continue;
            }
            CommandSource.suggestMatching(parseError.suggestions().possibleValues(readerBackedParsingState), suggestionsBuilder);
        }
        return suggestionsBuilder.buildFuture();
    }
}


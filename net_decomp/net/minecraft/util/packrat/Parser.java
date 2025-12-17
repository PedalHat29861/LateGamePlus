/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  com.mojang.brigadier.ImmutableStringReader
 *  com.mojang.brigadier.StringReader
 *  com.mojang.brigadier.exceptions.CommandSyntaxException
 *  com.mojang.brigadier.exceptions.DynamicCommandExceptionType
 *  com.mojang.brigadier.suggestion.Suggestions
 *  com.mojang.brigadier.suggestion.SuggestionsBuilder
 *  com.mojang.serialization.Codec
 *  com.mojang.serialization.DataResult
 *  com.mojang.serialization.DynamicOps
 */
package net.minecraft.util.packrat;

import com.mojang.brigadier.ImmutableStringReader;
import com.mojang.brigadier.StringReader;
import com.mojang.brigadier.exceptions.CommandSyntaxException;
import com.mojang.brigadier.exceptions.DynamicCommandExceptionType;
import com.mojang.brigadier.suggestion.Suggestions;
import com.mojang.brigadier.suggestion.SuggestionsBuilder;
import com.mojang.serialization.Codec;
import com.mojang.serialization.DataResult;
import com.mojang.serialization.DynamicOps;
import java.util.concurrent.CompletableFuture;
import java.util.function.Function;

public interface Parser<T> {
    public T parse(StringReader var1) throws CommandSyntaxException;

    public CompletableFuture<Suggestions> listSuggestions(SuggestionsBuilder var1);

    default public <S> Parser<S> map(final Function<T, S> mapper) {
        return new Parser<S>(){

            @Override
            public S parse(StringReader reader) throws CommandSyntaxException {
                return mapper.apply(Parser.this.parse(reader));
            }

            @Override
            public CompletableFuture<Suggestions> listSuggestions(SuggestionsBuilder builder) {
                return Parser.this.listSuggestions(builder);
            }
        };
    }

    default public <T, O> Parser<T> withDecoding(final DynamicOps<O> ops, final Parser<O> encodedParser, final Codec<T> codec, final DynamicCommandExceptionType invalidDataError) {
        return new Parser<T>(){

            @Override
            public T parse(StringReader reader) throws CommandSyntaxException {
                int i = reader.getCursor();
                Object object = encodedParser.parse(reader);
                DataResult dataResult = codec.parse(ops, object);
                return dataResult.getOrThrow(error -> {
                    reader.setCursor(i);
                    return invalidDataError.createWithContext((ImmutableStringReader)reader, error);
                });
            }

            @Override
            public CompletableFuture<Suggestions> listSuggestions(SuggestionsBuilder builder) {
                return Parser.this.listSuggestions(builder);
            }
        };
    }
}


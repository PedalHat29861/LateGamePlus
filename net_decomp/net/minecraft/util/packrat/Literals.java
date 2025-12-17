/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  com.mojang.brigadier.StringReader
 *  com.mojang.brigadier.exceptions.CommandSyntaxException
 *  it.unimi.dsi.fastutil.chars.CharList
 */
package net.minecraft.util.packrat;

import com.mojang.brigadier.StringReader;
import com.mojang.brigadier.exceptions.CommandSyntaxException;
import it.unimi.dsi.fastutil.chars.CharList;
import java.util.stream.Collectors;
import java.util.stream.Stream;
import net.minecraft.util.packrat.CursorExceptionType;
import net.minecraft.util.packrat.Cut;
import net.minecraft.util.packrat.ParseResults;
import net.minecraft.util.packrat.ParsingState;
import net.minecraft.util.packrat.Suggestable;
import net.minecraft.util.packrat.Term;

public interface Literals {
    public static Term<StringReader> string(String string) {
        return new StringLiteral(string);
    }

    public static Term<StringReader> character(final char c) {
        return new CharacterLiteral(CharList.of((char)c)){

            @Override
            protected boolean accepts(char c2) {
                return c == c2;
            }
        };
    }

    public static Term<StringReader> character(final char c1, final char c2) {
        return new CharacterLiteral(CharList.of((char)c1, (char)c2)){

            @Override
            protected boolean accepts(char c) {
                return c == c1 || c == c2;
            }
        };
    }

    public static StringReader createReader(String string, int cursor) {
        StringReader stringReader = new StringReader(string);
        stringReader.setCursor(cursor);
        return stringReader;
    }

    public static final class StringLiteral
    implements Term<StringReader> {
        private final String value;
        private final CursorExceptionType<CommandSyntaxException> exception;
        private final Suggestable<StringReader> suggestions;

        public StringLiteral(String value) {
            this.value = value;
            this.exception = CursorExceptionType.create(CommandSyntaxException.BUILT_IN_EXCEPTIONS.literalIncorrect(), value);
            this.suggestions = state -> Stream.of(value);
        }

        @Override
        public boolean matches(ParsingState<StringReader> state, ParseResults results, Cut cut) {
            state.getReader().skipWhitespace();
            int i = state.getCursor();
            String string = state.getReader().readUnquotedString();
            if (!string.equals(this.value)) {
                state.getErrors().add(i, this.suggestions, this.exception);
                return false;
            }
            return true;
        }

        public String toString() {
            return "terminal[" + this.value + "]";
        }
    }

    public static abstract class CharacterLiteral
    implements Term<StringReader> {
        private final CursorExceptionType<CommandSyntaxException> exception;
        private final Suggestable<StringReader> suggestions;

        public CharacterLiteral(CharList values) {
            String string = values.intStream().mapToObj(Character::toString).collect(Collectors.joining("|"));
            this.exception = CursorExceptionType.create(CommandSyntaxException.BUILT_IN_EXCEPTIONS.literalIncorrect(), string);
            this.suggestions = state -> values.intStream().mapToObj(Character::toString);
        }

        @Override
        public boolean matches(ParsingState<StringReader> state, ParseResults results, Cut cut) {
            state.getReader().skipWhitespace();
            int i = state.getCursor();
            if (!state.getReader().canRead() || !this.accepts(state.getReader().read())) {
                state.getErrors().add(i, this.suggestions, this.exception);
                return false;
            }
            return true;
        }

        protected abstract boolean accepts(char var1);
    }
}


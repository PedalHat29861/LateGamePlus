/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  com.mojang.brigadier.StringReader
 *  com.mojang.brigadier.exceptions.CommandSyntaxException
 *  org.jspecify.annotations.Nullable
 */
package net.minecraft.util.packrat;

import com.mojang.brigadier.StringReader;
import com.mojang.brigadier.exceptions.CommandSyntaxException;
import net.minecraft.util.packrat.CursorExceptionType;
import net.minecraft.util.packrat.ParsingRule;
import net.minecraft.util.packrat.ParsingState;
import org.jspecify.annotations.Nullable;

public abstract class NumeralParsingRule
implements ParsingRule<StringReader, String> {
    private final CursorExceptionType<CommandSyntaxException> invalidCharException;
    private final CursorExceptionType<CommandSyntaxException> unexpectedUnderscoreException;

    public NumeralParsingRule(CursorExceptionType<CommandSyntaxException> invalidCharException, CursorExceptionType<CommandSyntaxException> unexpectedUnderscoreException) {
        this.invalidCharException = invalidCharException;
        this.unexpectedUnderscoreException = unexpectedUnderscoreException;
    }

    @Override
    public @Nullable String parse(ParsingState<StringReader> parsingState) {
        int i;
        int j;
        StringReader stringReader = parsingState.getReader();
        stringReader.skipWhitespace();
        String string = stringReader.getString();
        for (j = i = stringReader.getCursor(); j < string.length() && this.accepts(string.charAt(j)); ++j) {
        }
        int k = j - i;
        if (k == 0) {
            parsingState.getErrors().add(parsingState.getCursor(), this.invalidCharException);
            return null;
        }
        if (string.charAt(i) == '_' || string.charAt(j - 1) == '_') {
            parsingState.getErrors().add(parsingState.getCursor(), this.unexpectedUnderscoreException);
            return null;
        }
        stringReader.setCursor(j);
        return string.substring(i, j);
    }

    protected abstract boolean accepts(char var1);

    @Override
    public /* synthetic */ @Nullable Object parse(ParsingState state) {
        return this.parse(state);
    }
}


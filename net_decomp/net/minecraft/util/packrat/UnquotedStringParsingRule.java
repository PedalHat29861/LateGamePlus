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

public class UnquotedStringParsingRule
implements ParsingRule<StringReader, String> {
    private final int minLength;
    private final CursorExceptionType<CommandSyntaxException> tooShortException;

    public UnquotedStringParsingRule(int minLength, CursorExceptionType<CommandSyntaxException> tooShortException) {
        this.minLength = minLength;
        this.tooShortException = tooShortException;
    }

    @Override
    public @Nullable String parse(ParsingState<StringReader> parsingState) {
        parsingState.getReader().skipWhitespace();
        int i = parsingState.getCursor();
        String string = parsingState.getReader().readUnquotedString();
        if (string.length() < this.minLength) {
            parsingState.getErrors().add(i, this.tooShortException);
            return null;
        }
        return string;
    }

    @Override
    public /* synthetic */ @Nullable Object parse(ParsingState state) {
        return this.parse(state);
    }
}


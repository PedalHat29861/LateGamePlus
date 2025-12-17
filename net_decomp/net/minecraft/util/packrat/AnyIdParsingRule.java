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
import net.minecraft.util.Identifier;
import net.minecraft.util.packrat.ParsingRule;
import net.minecraft.util.packrat.ParsingState;
import org.jspecify.annotations.Nullable;

public class AnyIdParsingRule
implements ParsingRule<StringReader, Identifier> {
    public static final ParsingRule<StringReader, Identifier> INSTANCE = new AnyIdParsingRule();

    private AnyIdParsingRule() {
    }

    @Override
    public @Nullable Identifier parse(ParsingState<StringReader> parsingState) {
        parsingState.getReader().skipWhitespace();
        try {
            return Identifier.fromCommandInputNonEmpty(parsingState.getReader());
        }
        catch (CommandSyntaxException commandSyntaxException) {
            return null;
        }
    }

    @Override
    public /* synthetic */ @Nullable Object parse(ParsingState state) {
        return this.parse(state);
    }
}


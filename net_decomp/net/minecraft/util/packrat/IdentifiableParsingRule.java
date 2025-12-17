/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  com.mojang.brigadier.ImmutableStringReader
 *  com.mojang.brigadier.StringReader
 *  com.mojang.brigadier.exceptions.CommandSyntaxException
 *  org.jspecify.annotations.Nullable
 */
package net.minecraft.util.packrat;

import com.mojang.brigadier.ImmutableStringReader;
import com.mojang.brigadier.StringReader;
import com.mojang.brigadier.exceptions.CommandSyntaxException;
import net.minecraft.util.Identifier;
import net.minecraft.util.packrat.CursorExceptionType;
import net.minecraft.util.packrat.IdentifierSuggestable;
import net.minecraft.util.packrat.ParsingRule;
import net.minecraft.util.packrat.ParsingRuleEntry;
import net.minecraft.util.packrat.ParsingState;
import org.jspecify.annotations.Nullable;

public abstract class IdentifiableParsingRule<C, V>
implements ParsingRule<StringReader, V>,
IdentifierSuggestable {
    private final ParsingRuleEntry<StringReader, Identifier> idParsingRule;
    protected final C callbacks;
    private final CursorExceptionType<CommandSyntaxException> exception;

    protected IdentifiableParsingRule(ParsingRuleEntry<StringReader, Identifier> idParsingRule, C callbacks) {
        this.idParsingRule = idParsingRule;
        this.callbacks = callbacks;
        this.exception = CursorExceptionType.create(Identifier.COMMAND_EXCEPTION);
    }

    @Override
    public @Nullable V parse(ParsingState<StringReader> state) {
        state.getReader().skipWhitespace();
        int i = state.getCursor();
        Identifier identifier = state.parse(this.idParsingRule);
        if (identifier != null) {
            try {
                return this.parse((ImmutableStringReader)state.getReader(), identifier);
            }
            catch (Exception exception) {
                state.getErrors().add(i, this, exception);
                return null;
            }
        }
        state.getErrors().add(i, this, this.exception);
        return null;
    }

    protected abstract V parse(ImmutableStringReader var1, Identifier var2) throws Exception;
}

